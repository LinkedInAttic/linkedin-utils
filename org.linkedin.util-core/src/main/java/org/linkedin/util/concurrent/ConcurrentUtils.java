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


package org.linkedin.util.concurrent;

import org.linkedin.util.clock.Clock;
import org.linkedin.util.clock.ClockUtils;
import org.linkedin.util.clock.SystemClock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;

/**
 * @author ypujante@linkedin.com
 *
 */
public class ConcurrentUtils
{
  /**
   * Waits on the condition but if end time is expired then throws an
   * exception. On purpose this method does not synchronize on the lock
   * because it must be called from a block which synchronizes on it
   *
   * @param condition the condition to wait on
   * @param endTime the time after which an exception is thrown (compared
   * to {@link java.lang.System#currentTimeMillis()}. If &lt; 0 then wait
   * indefinitely (so <code>TimeOutException</code> will not be thrown!)
   * @throws InterruptedException if the thread is interrupted while waiting
   * @throws TimeoutException if the time has been reached */
  public static void awaitUntil(Condition condition, long endTime)
    throws InterruptedException, TimeoutException
  {
    awaitUntil(SystemClock.INSTANCE, condition, endTime);
  }

  /**
   * Waits on the condition but if end time is expired then throws an
   * exception. On purpose this method does not synchronize on the lock
   * because it must be called from a block which synchronizes on it
   *
   * @param condition the condition to wait on
   * @param endTime the time after which an exception is thrown (compared
   * to {@link java.lang.System#currentTimeMillis()}. If &lt; 0 then wait
   * indefinitely (so <code>TimeOutException</code> will not be thrown!)
   * @throws InterruptedException if the thread is interrupted while waiting
   * @throws TimeoutException if the time has been reached */
  public static void awaitUntil(Clock clock, Condition condition, long endTime)
    throws InterruptedException, TimeoutException
  {
    if(endTime <= 0)
      condition.await();
    else
    {
      long now = clock.currentTimeMillis();
      if(now >= endTime)
        throw new TimeoutException("timeout reached while waiting on the lock: "
                                   + condition);

      if(!condition.await(endTime - now, TimeUnit.MILLISECONDS))
        throw new TimeoutException("timeout reached while waiting on the lock: "
                                   + condition);
    }
  }

  /**
   * Waits on the lock but if end time is expired then throws an
   * exception. On purpose this method does not synchronize on the lock
   * because it must be called from a block which synchronizes on it
   *
   * @param lock the lock to wait on
   * @param endTime the time after which an exception is thrown (compared
   * to {@link java.lang.System#currentTimeMillis()}. If &lt; 0 then wait
   * indefinitely (so <code>TimeOutException</code> will not be thrown!)
   * @throws InterruptedException if the thread is interrupted while waiting
   * @throws TimeoutException if the time has been reached */
  public static void awaitUntil(Object lock, long endTime)
    throws InterruptedException, TimeoutException
  {
    awaitUntil(SystemClock.INSTANCE, lock, endTime);
  }

  /**
   * Waits on the lock but if end time is expired then throws an
   * exception. On purpose this method does not synchronize on the lock
   * because it must be called from a block which synchronizes on it
   *
   * @param lock the lock to wait on
   * @param endTime the time after which an exception is thrown (compared
   * to {@link java.lang.System#currentTimeMillis()}. If &lt; 0 then wait
   * indefinitely (so <code>TimeOutException</code> will not be thrown!)
   * @throws InterruptedException if the thread is interrupted while waiting
   * @throws TimeoutException if the time has been reached */
  public static void awaitUntil(Clock clock, Object lock, long endTime)
    throws InterruptedException, TimeoutException
  {
    if(endTime <= 0)
      lock.wait();
    else
    {
      long now = clock.currentTimeMillis();
      if(now >= endTime)
        throw new TimeoutException("timeout reached while waiting on the lock: "
                                   + lock);
      lock.wait(endTime - now);
    }
  }

  /**
   * Joins on the thread but if end time is expired then throws an exception.
   *
   * @param thread the thread to join on
   * @param endTime the time after which an exception is thrown (compared
   * to {@link java.lang.System#currentTimeMillis()}. If &lt;= 0 then wait
   * indefinitely (so <code>TimeOutException</code> will not be thrown!)
   * @throws InterruptedException if the thread is interrupted while waiting
   * @throws TimeoutException if the time has been reached
   */
  public static void joinUntil(Clock clock, Thread thread, long endTime)
    throws InterruptedException, TimeoutException
  {
    if(endTime <= 0)
      thread.join();
    else
    {
      while(thread.isAlive())
      {
        long now = clock.currentTimeMillis();
        if(now >= endTime)
          throw new TimeoutException("timeout reached while joining on: " + thread);
        thread.join(endTime - now);
      }
    }
  }

  /**
   * Joins on the thread but if end time is expired then throws an exception.
   *
   * @param thread the thread to join on
   * @param timeout how long to join for (<code>null</code> or <= 0 means forever)
   * @throws InterruptedException if the thread is interrupted while waiting
   * @throws TimeoutException if the time has been reached
   */
  public static void joinFor(Clock clock, Thread thread, Object timeout)
    throws InterruptedException, TimeoutException
  {
    joinUntil(clock, thread, ClockUtils.toEndTime(timeout));
  }


  /**
   * Constructor
   */
  private ConcurrentUtils()
  {
  }
}
