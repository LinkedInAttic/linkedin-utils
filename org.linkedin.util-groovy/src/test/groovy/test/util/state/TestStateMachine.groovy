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


package test.util.state

import java.util.concurrent.Callable
import org.linkedin.groovy.util.state.StateMachine
import org.linkedin.groovy.util.state.StateMachineImpl
import org.linkedin.groovy.util.state.StateChangeListener
import org.linkedin.util.concurrent.ThreadControl
import org.linkedin.util.concurrent.ThreadPerTaskExecutor

/**
 * Test for state machine
 *
 * @author ypujante@linkedin.com
 */
def class TestStateMachine extends GroovyTestCase
{
  def static TRANSITIONS =
  [
    NONE: [[to: 'installed', action: 'install']],
    installed: [[to: 'stopped', action: 'configure'], [to: 'NONE', action: 'uninstall']],
    stopped: [[to: 'running', action: 'start'], [to: 'stopped', action: 'configure'], [to: 'NONE', action: 'uninstall']],
    running: [[to: 'stopped', action: 'stop']]
  ]

  def static TRANSITIONS2 =
  [
    NONE: [[to: 'installed', action: 'install']],
    installed: [[to: 'stopped', action: 'configure'], [to: 'NONE', action: 'uninstall']],
    stopped: [[to: 'running', action: 'start'], [to: 'installed', action: 'unconfigure']],
    running: [[to: 'stopped', action: 'stop']]
  ]

  def static TRANSITIONS3 =
  [
    NONE: [[to: 'installedscript', action: 'installscript']],
    installedscript: [[to: 'installed', action: 'install'], [to: 'NONE', action: 'uninstallscript']],
    installed: [[to: 'stopped', action: 'configure'], [to: 'installedscript', action: 'uninstall']],
    stopped: [[to: 'running', action: 'start'], [to: 'installed', action: 'unconfigure']],
    running: [[to: 'stopped', action: 'stop']]
  ]


  StateMachine sm
  def states
  Object o = new Object()

  protected void setUp()
  {
    super.setUp();
    def stateChangeListener = { oldState, newState ->
      assertEquals(states[-1], oldState)
      states << newState
    }

    sm = new StateMachineImpl(transitions: TRANSITIONS,
                              stateChangeListener: stateChangeListener as StateChangeListener)
    states = [[currentState: StateMachine.NONE]]
  }

  /**
   * The state machines has a default value.. we verify that the various methods work appropriately
   */
  void testDefaultTransitions()
  {
    assertEquals(new HashSet(['install', 'configure', 'start', 'stop', 'uninstall']),
                 sm.availableActions)
    assertEquals(new HashSet([StateMachine.NONE, 'installed', 'stopped', 'running']),
                 new HashSet(sm.availableStates))
    assertEquals([[StateMachine.NONE, 'installed'], ['installed', 'stopped'],
                  ['installed', StateMachine.NONE], ['stopped', 'running'],
                  ['stopped', 'stopped'], ['stopped', StateMachine.NONE],
                  ['running', 'stopped']],
                 sm.availableTransitions)
  }

  /**
   * Simple happy path verifying that only the correct actions can be executed dependening on
   * the state.. walk through the entire 'normal' flow
   */
  void testExecute()
  {
    checkState([currentState: StateMachine.NONE])

    // we check that we can execute install only
    checkForbiddenActions(['configure', 'start', 'stop', 'uninstall'])

    // we execute install
    checkState([currentState: StateMachine.NONE])
    assert sm.executeAction('install') { return o }.is(o)
    checkState([currentState: 'installed'])
    assertEquals(3, states.size())

    // we check for forbidden actions (ok are configure and uninstall)
    checkForbiddenActions(['install', 'start', 'stop'])
    checkState([currentState: 'installed'])
    assert sm.executeAction('configure') { return o }.is(o)
    checkState([currentState: 'stopped'])
    assertEquals(5, states.size())

    // we check for forbidden actions (ok are configure uninstall and start)
    checkForbiddenActions(['install', 'stop'])
    checkState([currentState: 'stopped'])
    assert sm.executeAction('start') { return o }.is(o)
    checkState([currentState: 'running'])
    assertEquals(7, states.size())

    // we check for forbidden actions (ok are stop)
    checkForbiddenActions(['install', 'start', 'configure', 'uninstall'])
    checkState([currentState: 'running'])
    assert sm.executeAction('stop') { return o }.is(o)
    checkState([currentState: 'stopped'])
    assertEquals(9, states.size())

    // we check for forbidden actions (ok are configure uninstall and start)
    checkForbiddenActions(['install', 'stop'])
    checkState([currentState: 'stopped'])
    assert sm.executeAction('uninstall') { return o }.is(o)
    checkState([currentState: StateMachine.NONE])
    assertEquals(11, states.size())
  }


  /**
   * This test focuses on the transition phase between states: we use a thread control to manage the
   * timing between threads and verify the correct behavior
   */
  void testTransitionState()
  {
    checkState([currentState: StateMachine.NONE])

    def tc = new ThreadControl()

    def future = ThreadPerTaskExecutor.execute({
      sm.executeAction('install') {
        return tc.block('s1')
      }
    } as Callable);

    tc.waitForBlock('s1')
    checkState([currentState: StateMachine.NONE,
               transitionState: "${StateMachine.NONE}->installed".toString(),
               transitionAction: 'install'])
    tc.unblock('s1', o)

    assert future.get().is(o)
    checkState([currentState: 'installed'])

    // now with error
    future = ThreadPerTaskExecutor.execute({
      sm.executeAction('configure') {
        tc.blockWithException('s1')
      }
    } as Callable);

    tc.waitForBlock('s1')
    checkState([currentState: 'installed', transitionState: 'installed->stopped', transitionAction: 'configure'])
    assertNull sm.error
    def ex = new Exception('abc')
    tc.unblock('s1', ex)

    try
    {
      future.get()
    }
    catch(Exception e)
    {
      assert e.cause.cause.is(ex)
    }

    // the state machine stayed where it was before the exception
    checkState([currentState: 'installed', error: ex])

    // it should be in error mode
    assert sm.error.is(ex)

    // while in error mode, cannot execute an action
    shouldFail(IllegalStateException) {
      sm.executeAction('configure') {}
    }

    // clearing the error allows to execute an action again
    sm.clearError()
    assertNull sm.error
    sm.executeAction('configure') {}
    checkState([currentState: 'stopped'])
  }


  /**
   * Test for asynchronous blocking / waiting for state to be done
   */
  void testWaitForState()
  {
    assertTrue sm.waitForState(StateMachine.NONE, 0)

    def tc = new ThreadControl()

    def f1 = ThreadPerTaskExecutor.execute({
      sm.executeAction('install') {
        return tc.block('f1')
      }
    } as Callable);

    def f2 = ThreadPerTaskExecutor.execute({
      tc.block('f2.1')
      assertFalse sm.waitForState('installed', 200)
      tc.block('f2.2')
      assertTrue("should be installed ${sm.toString()}", sm.waitForState('installed', 0))
    } as Callable);

    tc.waitForBlock('f1')
    tc.waitForBlock('f2.1')
    tc.unblock('f2.1')
    tc.waitForBlock('f2.2')
    tc.unblock('f2.2')
    tc.unblock('f1')

    f1.get()
    f2.get()
  }

  // GLU-169: StateMachine does not return the correct shortest path
  void testGLU169()
  {
    sm = new StateMachineImpl(transitions: TRANSITIONS3)

    assertEquals([[to: 'installed', action: 'unconfigure'],
                  [to: 'installedscript', action: 'uninstall'],
                  [to: 'NONE', action: 'uninstallscript']], sm.findShortestPath('stopped', 'NONE'))

  }

  void testFindPaths()
  {
    assertEquals([['uninstall'], ['configure', 'uninstall'], ['configure', 'start', 'stop', 'uninstall']],
                 sm.findPaths('installed', 'NONE').action)
    assertEquals([[[to: 'NONE', action: 'uninstall']],
                  [[to: 'stopped', action: 'configure'], [to: 'NONE', action: 'uninstall']],
                  [[to: 'stopped', action: 'configure'], [to: 'running', action: 'start'], [to: 'stopped', action: 'stop'], [to: 'NONE', action: 'uninstall']]],
                 sm.findPaths('installed', 'NONE'))

    // we use a different state machine
    sm = new StateMachineImpl(transitions: TRANSITIONS2)

    assertEquals([[[to: 'installed', action: 'install'],
                   [to: 'stopped', action: 'configure'],
                   [to: 'running', action: 'start']]], sm.findPaths('NONE', 'running'))

    assertEquals([[[to: 'stopped', action: 'stop'],
                   [to: 'installed', action: 'unconfigure'],
                   [to: 'NONE', action: 'uninstall']]], sm.findPaths('running', 'NONE'))

    assertEquals([[[to: 'NONE', action: 'uninstall']],
                  [[to: 'stopped', action: 'configure'],
                   [to: 'running', action: 'start'],
                   [to: 'stopped', action: 'stop'],
                   [to: 'installed', action: 'unconfigure'],
                   [to: 'NONE', action: 'uninstall']],
                  [[to: 'stopped', action: 'configure'],
                   [to: 'installed', action: 'unconfigure'],
                   [to: 'NONE', action: 'uninstall']]], sm.findPaths('installed', 'NONE'))

    assertEquals([[to: 'installed', action: 'install'],
                  [to: 'stopped', action: 'configure'],
                  [to: 'running', action: 'start']], sm.findShortestPath('NONE', 'running'))

    assertEquals([[to: 'stopped', action: 'stop'],
                  [to: 'installed', action: 'unconfigure'],
                  [to: 'NONE', action: 'uninstall']], sm.findShortestPath('running', 'NONE'))

    assertEquals([[to: 'NONE', action: 'uninstall']], sm.findShortestPath('installed', 'NONE'))

    assertEquals([], sm.findShortestPath('NONE', 'NONE'))
  }

  public void testGetState()
  {
    assertEquals('installed', sm.getEndState('install'))
    assertEquals('stopped', sm.getEndState('configure'))
    assertEquals('NONE', sm.getEndState('uninstall'))
    assertEquals('running', sm.getEndState('start'))
    assertEquals('stopped', sm.getEndState('configure'))
    assertEquals('NONE', sm.getEndState('uninstall'))
    assertEquals('stopped', sm.getEndState('stop'))
  }

  void testForceChangeState()
  {
    checkState([currentState: StateMachine.NONE])

    // we execute install
    checkState([currentState: StateMachine.NONE])
    assert sm.executeAction('install') { return o }.is(o)
    checkState([currentState: 'installed'])
    assertEquals(3, states.size())

    def e = new Exception('e1')
    sm.forceChangeState(null, e)
    checkState([currentState: 'installed', error: e])
    assertEquals(4, states.size())

    sm.forceChangeState('running', null)
    checkState([currentState: 'running'])
    assertEquals(5, states.size())

    shouldFail(IllegalArgumentException) { sm.forceChangeState('invalid', null) }
    checkState([currentState: 'running'])
    assertEquals(5, states.size())

    def tc = new ThreadControl()

    def future = ThreadPerTaskExecutor.execute({
      sm.executeAction('stop') {
        return tc.block('s1')
      }
    } as Callable);

    tc.waitForBlock('s1')
    checkState([currentState: 'running',
               transitionState: "running->stopped".toString(),
               transitionAction: 'stop'])
    assertEquals(6, states.size())

    // in transition state, you cannot force change the state
    shouldFail(IllegalStateException) { sm.forceChangeState('installed', null) }
    checkState([currentState: 'running',
               transitionState: "running->stopped".toString(),
               transitionAction: 'stop'])
    assertEquals(6, states.size())

    tc.unblock('s1', o)
    future.get()
    assertEquals(7, states.size())

    // once the transition is over it should work
    sm.forceChangeState('installed', null)
    checkState([currentState: 'installed'])
    assertEquals(8, states.size())
  }

  private def checkState(state)
  {
    assertEquals state.transitionState, sm.transitionState
    assertEquals state.currentState, sm.currentState
    assertEquals state.error, sm.error

    assertEquals (state, sm.state)
    assertEquals (state, states[-1])
  }

  private def checkForbiddenActions(actions)
  {
    actions.each { action ->
      shouldFail(IllegalStateException) {
        sm.executeAction(action) { println it}
      }
    }
  }
}
