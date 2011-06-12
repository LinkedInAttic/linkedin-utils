/*
 * Copyright 2010-2010 LinkedIn, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


package org.linkedin.groovy.util.state

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeoutException
import org.linkedin.util.clock.Clock
import org.linkedin.util.clock.SystemClock
import org.linkedin.util.annotations.Initializable
import org.linkedin.groovy.util.concurrent.GroovyConcurrentUtils
import org.linkedin.groovy.util.lang.GroovyLangUtils
import org.linkedin.util.lang.LangUtils

/**
 * Represents the state machine (states, transitions and action to take on each)
 *
 * Here is an example of a state machine transitions
 * <pre>
 *
 * def static DEFAULT_TRANSITIONS =
 * [
 *   NONE: [[to: 'installed', action: 'install']],
 *   installed: [[to: 'stopped', action: 'configure'], [to: NONE, action: 'uninstall']],
 *   stopped: [[to: 'running', action: 'start'], [to: 'stopped', action: 'configure'], [to: NONE, action: 'uninstall']],
 *   running: [[to: 'stopped', action: 'stop']]
 * ]
 * </pre>
 * @author ypujante@linkedin.com
 */
def class StateMachineImpl implements StateMachine
{
  public static final String MODULE = StateMachineImpl.class.getName();
  public static final Logger log = LoggerFactory.getLogger(MODULE);

  @Initializable
  Clock clock = SystemClock.INSTANCE

  private final Map<String, Collection<Map<String, String>>> _transitions
  private final Map<String, Integer> _depths
  private final def _shortestPathsCache = [:]
  private StateChangeListener _stateChangeListener
  private volatile def _currentState = NONE
  private volatile def _transitionAction = null
  private volatile def _transitionState = null
  private volatile def _error

  // the lock to use for synchronization
  def lock

  def StateMachineImpl(args)
  {
    if(!args.transitions)
      throw new IllegalArgumentException("missing transitions")

    _transitions = LangUtils.<Map<String, Collection<Map<String, String>>>>deepClone(args.transitions)
    _stateChangeListener = args.stateChangeListener
    _currentState = args.currentState ?: NONE
    _transitionAction = args.transitionAction
    _transitionState = args.transitionState
    _error = args.error
    _depths = [:]
    _transitions.keySet().each { state ->
      _depths[state] = computeDepth(state)
    }
    lock = args.lock ?: new Object()
  }

  private int computeDepth(state)
  {
    findPaths(NONE, state).min { c1, c2 ->
      return c1.size - c2.size
    }.size()
  }

  /**
   * Returns all available actions
   */
  def getAvailableActions()
  {
    def actions = new LinkedHashSet()

    _transitions.values().each { transition ->
      actions.addAll(transition.action)
    }

    return actions
  }

  /**
   * returns all available states 
   */
  def getAvailableStates()
  {
    return _transitions.collect { k, v -> k}
  }

  def getAvailableTransitions()
  {
    def availableTransitions = []

    _transitions.each { k, v ->
      availableTransitions.addAll(v.to.collect { [k, it]})
    }

    return availableTransitions
  }


  /**
   * {@inheritdoc}
   */
  def findPaths(toState)
  {
    return findPaths(_currentState, toState)
  }

  /**
   * {@inheritdoc}
   */
  def findPaths(fromState, toState)
  {
    [fromState, toState].each { state ->
      if(!_transitions[state])
        throw new IllegalArgumentException("${state} is not a valid state")
    }

    def paths = doFindPaths(fromState, toState, new HashSet())

    if(fromState == toState)
    {
      paths = [[], *paths]
    }

    return paths
  }

  def findShortestPath(toState)
  {
    return findShortestPath(_currentState, toState)
  }

  def findShortestPath(fromState, toState)
  {
    def key = [fromState, toState]

    synchronized(_shortestPathsCache)
    {
      def res = _shortestPathsCache[key]
      if(res != null)
      {
        return res
      }
      else
      {
        def paths = findPaths(fromState, toState)
        // keep the path of minimum size
        paths = new TreeMap(paths.groupBy { it.size() }).values().iterator().next()
        if(!paths)
          return []
        if(paths.size() > 1)
        {
          int toDepth = _depths[toState]
          paths = paths.groupBy { path ->
            path.to.sum { Math.abs(_depths[it] - toDepth) }
          }
          paths = new TreeMap(paths).values().iterator().next()
        }
        res = paths[0]
        _shortestPathsCache[key] = res
      }
      return res
    }
  }

  /**
   * Recursive call.
   */
  private def doFindPaths(fromState, toState, visited)
  {
    def res = []

    def recurse = []

    _transitions[fromState].each { transition ->
      if(transition.to == toState)
      {
        res << [transition]
      }
      else
      {
        if(!visited.contains(transition))
        {
          recurse << transition
        }
      }
    }

    recurse.each { transition ->
      def newVisited = new HashSet(visited)
      newVisited << transition
      doFindPaths(transition.to, toState, newVisited).each { actionList ->
        res << [transition, *actionList]
      }
    }

    return res
  }

  def getTransitions()
  {
    return _transitions
  }

  @Override
  int getDepth()
  {
    return getDepth(currentState)
  }

  @Override
  int getDepth(state)
  {
    if(!_depths.containsKey(state))
      throw new IllegalArgumentException("${state} is not a valid state")
    return _depths[state]
  }

  @Override
  int getDistance(fromState, toState)
  {
    int distance = findShortestPath(fromState, toState).size()
    if(getDepth(fromState) > getDepth(toState))
      distance = -distance;
    return distance
  }

  /**
   * Returns the end state that the state machine would be if the action were to be executed.
   *
   * @throws IllegalStateException if it is not possible to execute the action because the state
   * does not allow it
   */
  def findEndState(action)
  {
    synchronized(lock)
    {
      def endState

      if(_transitionState != null)
        throw new IllegalStateException("already in transition ${getTransitionState()}")

      if(_error != null)
        throw new IllegalStateException("state machine in error: ${getState()} (call clearError to move forward)")

      def transition = _transitions[_currentState]

       endState = transition.find {it.action == action}?.to

      if(!endState)
        throw new IllegalStateException("no valid transition found for '${action}' from ${state}: valid action(s) ${transition.action}")

      return endState
    }
  }

  /**
   * Returns the end state associated to a specific action in the state machine.
   * No action is actually performed.
   * It returns <code>null</code> if the given action was not found. 
   */
  def getEndState(action)
  {
    def res
    _transitions.each { k, t ->
      if (!res)
      {
        res = t.find {it.action == action}?.to
      }
    }
    return res
  }

  /**
   * Execute the action: sets the state machine in transition state, execute the closure and then
   * move to the final state. All this provided that the state transition and current state
   * is authorizing the action. This call is a blocking call!
   */
  def executeAction(action, closure)
  {
    def endState
    synchronized(lock)
    {
      endState = findEndState(action)

      changeState {
        _transitionState = [_currentState, endState]
        _transitionAction = action
      }
    }

    // it is VERY important that the action is executed outside of the synchronized block
    // otherwise the state cannot even be retrieved while the action is happening
    def res = doExecute(closure)

    synchronized(lock)
    {
      try
      {
        if(res.error)
        {
          changeState {
            _transitionState = null
            _transitionAction = null
            _error = res.error
          }
          throw res.error
        }
        else
        {
          changeState {
            _transitionState = null
            _transitionAction = null
            _currentState = endState
          }
          return res.result
        }
      }
      finally
      {
        lock.notifyAll()
      }
    }
  }

  /**
   * Wait for the state machine to be in the provided state. Does not wait longer than the timeout.
   *
   * @param timeout may be <code>null</code> for unlimited waiting
   * @return <code>true</code> if the state was reached during the timeout,
   *         <code>false</code> otherwise
   */
  def waitForState(state, timeout)
  {
    try
    {
      GroovyConcurrentUtils.awaitFor(clock, timeout, lock) {
        // this code is synchronized (see awaitFor documentation!)
        if(_error)
          throwError(_error);

        return !_transitionState && _currentState == state
      }
    }
    catch(TimeoutException e)
    {
      return false;
    }

    return true
  }

  private void throwError(errorToThrow)
  {
    if(errorToThrow instanceof Throwable)
      throw errorToThrow
    else
      throw new IllegalStateException("state machine in error: [${errorToThrow}]")
  }

  private def doExecute(closure)
  {
    def res
    try
    {
      return [result: closure(_transitionState)]
    }
    catch(Throwable th)
    {
      return [error: th]
    }
  }

  def getState()
  {
    synchronized(lock)
    {
      def res = [currentState: _currentState]
      if(_transitionState)
      {
        res.transitionState = getTransitionState()
        res.transitionAction = getTransitionAction()
      }
      if(_error)
        res.error = error
      return res
    }
  }

  def getCurrentState()
  {
    synchronized(lock)
    {
      return _currentState
    }
  }

  /**
   * This method should be used to restore the state only
   */
  void setCurrentState(currentState)
  {
    synchronized(lock)
    {
      checkCurrentStateValidity(currentState)

      changeState {
        _currentState = currentState
      }
    }
  }

  def getError()
  {
    synchronized(lock)
    {
      return _error
    }
  }

  /**
   * This method should be used to restore the state only
   */
  void setError(error)
  {
    synchronized(lock)
    {
      changeState {
        _error = error
      }
    }
  }

  private void checkCurrentStateValidity(currentState)
  {
    if(!availableStates.find {it == currentState} )
      throw new IllegalArgumentException("invalid state ${currentState}")
  }

  /**
   * This method is used to change the state and should be used carefully. This method cannot
   * be used when in transition state
   *
   * @param currentState the new current state (can be <code>null</code> in which case we keep
   *                     the current one)
   * @param error the new error state
   */
  void forceChangeState(currentState, error)
  {
    synchronized(lock)
    {
      if(_transitionState != null)
        throw new IllegalStateException("cannot change state when in transition ${getTransitionState()}")

      if(currentState != null)
      {
        checkCurrentStateValidity(currentState)
      }

      changeState {
        if(currentState != null)
          _currentState = currentState
        _error = error
      }
    }
  }

  def clearError()
  {
    synchronized(lock)
    {
      changeState {
        _error = null
      }
    }
  }

  def getTransitionState()
  {
    synchronized(lock)
    {
      return computeTransitionStateString(_transitionState)
    }
  }

  def getTransitionAction()
  {
    synchronized(lock)
    {
      return _transitionAction
    }
  }

  void setStateChangeListener(StateChangeListener stateChangeListener)
  {
    synchronized(lock)
    {
      _stateChangeListener = stateChangeListener
    }
  }

  private def computeTransitionStateString(transitionState)
  {
    if(transitionState)
    {
      return "${transitionState[0]}->${transitionState[1]}".toString()
    }
    else
      return null
  }

  public String toString()
  {
    synchronized(lock)
    {
      return [
          currentState: _currentState,
          transitionState: _transitionState,
          transitionAction: _transitionAction,
          error: _error
      ].toString()
    }
  }

  private def changeState(closure)
  {
    if(_stateChangeListener)
    {
      def oldState = getState()
      closure()
      def newState = getState()
      if(oldState != newState)
      {
        GroovyLangUtils.noException {
          _stateChangeListener.onStateChange(oldState, newState)
        }
      }
    }
    else
    {
      closure()
    }
  }
}
