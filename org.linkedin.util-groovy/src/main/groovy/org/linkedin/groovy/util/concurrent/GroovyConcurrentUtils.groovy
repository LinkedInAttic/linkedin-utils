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


package org.linkedin.groovy.util.concurrent

import java.util.concurrent.TimeoutException
import org.linkedin.util.clock.Timespan
import org.linkedin.util.concurrent.ConcurrentUtils
import org.linkedin.util.lifecycle.Shutdownable
import org.linkedin.util.clock.ClockUtils

/**
 *
 *
 * @author ypujante@linkedin.com
 */
def class GroovyConcurrentUtils extends ConcurrentUtils
{
  /**
   * When multiple waits need to happen with a timeout the logic is a little bit complicated
   * because the timeout applies as a whole, not as individual waits. This method takes care
   * of it by calling each closure with the 'remaining' timeout.
   * @param timeout can be <code>null</code> if you want to wait forever, otherwise
   * you can provide a <code>Timespan</code>, a <code>String</code> (which has a format recognized
   * by <code>Timespan</code>) or a number of milliseconds.
   */
  def static waitMultiple(clock, timeout, closures)
  {
    long endTime = ClockUtils.toEndTime(clock, timeout)

    def res

    closures.each { closure ->
      if(timeout)
      {
        long duration = endTime - clock.currentTimeMillis()
        if(duration <= 0)
          throw new TimeoutException()
        else
          timeout = new Timespan(duration)
      }
      
      res = closure(timeout)
    }

    return res
  }

  /**
   * Handle the logic when you need to wait for shutdown with a timeout on multiple shutdownable...
   * the collective time must not be beyond the timeout
   */
  def static waitForShutdownMultiple(clock, timeout, Collection<Shutdownable> shutdownables)
  {
    long endTime = ClockUtils.toEndTime(clock, timeout)

    if(endTime == 0)
    {
      // this is respecting the meaning of timeout = 0 in the Shutdownable concept
      shutdownables.each { it.waitForShutdown() }
    }
    else
    {
      shutdownables.each {
        long duration = endTime - clock.currentTimeMillis()
        if(duration <= 0)
          throw new TimeoutException()
        else
          it.waitForShutdown(duration)
      }
    }
  }

  /**
   * This call will wait until the condition as provided by the closure is <code>true</code>.
   * Waits no longer than the timeout provided. Note that it is going to sleep as it is not a
   * wait/notify pattern. The sleeping time will increase between each calls to the closure.
   * Passing <code>null</code> for the timeout will block until the condition is <code>true</code>.
   * @param heartbeat how long to sleep between each closure call
   */
  static void waitForCondition(clock, timeout, heartbeat, Closure condition)
  {
    long startTime = clock.currentTimeMillis()
    long endTime = ClockUtils.toEndTime(clock, timeout)
    heartbeat = (Timespan.parse(heartbeat?.toString()) ?: Timespan.ONE_SECOND).durationInMilliseconds

    while(!condition(new Timespan(clock.currentTimeMillis() - startTime)))
    {
      long now = clock.currentTimeMillis()
      if(endTime > now)
      {
        Thread.sleep(Math.min(heartbeat, endTime - now))
      }
      else
      {
        if(endTime == 0)
          Thread.sleep(heartbeat)
        else
          throw new TimeoutException()
      }
    }
  }

  /**
   * The closure is the condition to wait on: it should return <code>false</code> until
   * the condition is met. When met, it should return <code>true</code>.
   *
   * @param lock the lock to wait on. Note that the method will do a synchronized on it, so you
   * don't have to...
   * @param timeout can be <code>null</code> if you want to wait forever, otherwise
   * you can provide a <code>Timespan</code>, a <code>String</code> (which has a format recognized
   * by <code>Timespan</code>) or a number of milliseconds.
   */
  def static awaitFor(clock, timeout, lock, Closure closure)
  {
    long endTime = ClockUtils.toEndTime(clock, timeout)

    synchronized(lock)
    {
      while(!closure(endTime))
      {
        awaitUntil(clock, lock, endTime)
      }
    }
  }
}
