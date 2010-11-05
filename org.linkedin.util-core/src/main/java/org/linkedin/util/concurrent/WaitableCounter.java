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

import org.linkedin.util.annotations.Initializable;
import org.linkedin.util.clock.Clock;
import org.linkedin.util.clock.ClockUtils;
import org.linkedin.util.clock.SystemClock;

import java.util.concurrent.TimeoutException;

/**
 * This class is a utility class that just encapsulates a counter on which
 * you can wait until it reaches 0.
 *
 * @author ypujante@linkedin.com */
public class WaitableCounter
{
  @Initializable
  public Clock clock = SystemClock.INSTANCE;

  private int _counter;
  
  public WaitableCounter(int counter)
  {
    _counter = counter;
  }

  public WaitableCounter()
  {
    this(0);
  }

  public synchronized void dec()
  {
    _counter--;
    
    if(_counter == 0)
      notifyAll();
  }

  public synchronized void inc()
  {
    _counter++;
  }

  public synchronized int getCounter()
  {
    return _counter;
  }

  public synchronized void waitForCounter() throws InterruptedException
  {
    while(_counter > 0)
      wait();
  }

  public synchronized void waitForCounter(Object timeout)
    throws InterruptedException, TimeoutException
  {
    long endTime = ClockUtils.toEndTime(clock, timeout);

    while(_counter > 0)
      ConcurrentUtils.awaitUntil(clock, this, endTime);
  }
}
