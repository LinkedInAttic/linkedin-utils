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

/**
 * Represents the state machine (states, transitions and action to take on each)
 *
 * @author ypujante@linkedin.com
 */
def interface StateMachine
{
  // NONE is the initial state
  def static NONE = "NONE"

  /**
   * Returns all available actions
   */
  def getAvailableActions()

  /**
   * returns all available states 
   */
  def getAvailableStates()

  /**
   * Returns the end state associated to a specific action in the state machine.
   * No action is actually performed.
   * It returns <code>null</code> if the given action was not found.
   */
  def getEndState(action)

  def getAvailableTransitions()

  def getTransitions()

  /**
   * Execute the action: sets the state machine in transition state, execute the closure and then
   * move to the final state. All this provided that the state transition and current state
   * is authorizing the action. This call is a blocking call!
   */
  def executeAction(action, closure)

  /**
   * Returns the end state that the state machine would be if the action were to be executed.
   *
   * @throws IllegalStateException if it is not possible to execute the action because the state
   * does not allow it
   */
  def findEndState(action)

  /**
   * Equivalent to <code>findPaths(currentState, toState)</code>
   * @see #findPaths(Object, Object)
   */
  def findPaths(toState)

  /**
   * Returns a list of paths to go from <code>fromState</code> to <code>toState</code>. If there
   * is no paths, then an empty list is returned. There can be mulitple paths, this is why it
   * returns a list. Each path is a list of transitions (same as what is returned in
   * {@link #getTransitions()}).
   *
   * <pre>
   * Example of result:
   * [[[to: 'NONE', action: 'uninstall']],
   *  [[to: 'stopped', action: 'configure'], [to: 'NONE', action: 'uninstall']],
   *  ...]
   * </pre>
   *
   */
  def findPaths(fromState, toState)

  /**
   * Equivalent to <code>findShortestPath(currentState, toState)</code>
   * @see #findShortestPath(Object, Object)
   */
  def findShortestPath(toState)

  /**
   * Returns the shortest path (list of transitions) to go from <code>fromState</code> to
   * <code>toState</code>
   *
   * This call returns the shortest path (if there is more than one shortest path, one of them
   * will be returned. Although no guarantee is made on which one would be returned, the same
   * one would be returned if you call the method mutliple times)
   *
   * @see #findPaths(Object, Object)
   */
  def findShortestPath(fromState, toState)

  /**
   * Wait for the state machine to be in the provided state. Does not wait longer than the timeout.
   *
   * @param timeout may be <code>null</code> for unlimited waiting
   * @return <code>true</code> if the state was reached during the timeout,
   *         <code>false</code> otherwise
   */
  def waitForState(state, timeout)

  /**
   * Return a coherent view of the state (like calling each individual methods, but guaranteed to
   * be atomic).
   *
   * @return a map <code>[currentState: x, transitionState: x, transitionAction: x, error: x]</code>
   * (<code>transition*</code> and <code>error</code> are optional)
   */
  def getState()

  def getCurrentState()

  def getError()

  def clearError()

  def getTransitionAction()

  def getTransitionState()

  /**
   * Sets a listener to know when the state changes
   */
  void setStateChangeListener(StateChangeListener stateChangeListener)

  /**
   * This method is used to change the state and should be used carefully. This method cannot
   * be used when in transition state
   *
   * @param currentState the new current state (can be <code>null</code> in which case we keep
   *                     the current one)
   * @param error the new error state
   */
  void forceChangeState(currentState, error)
}
