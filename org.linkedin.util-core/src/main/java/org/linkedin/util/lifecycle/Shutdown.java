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

package org.linkedin.util.lifecycle;

import org.linkedin.util.annotations.Initializable;
import org.linkedin.util.clock.Timespan;
import org.linkedin.util.concurrent.WaitableCounter;

import java.io.Serializable;
import java.util.concurrent.TimeoutException;

/**
 * Simple object that has the knowledge wether we are in shutdown or not. Works in close relation
 * with {@link ShutdownProxy}.
 *
 * @author ypujante@linkedin.com
 *
 */
public class Shutdown implements Serializable, Shutdownable
{
  private static final long serialVersionUID = 1L;

  private boolean _shutdown = false;

  @Initializable
  public WaitableCounter pendingCallsCount = new WaitableCounter();

  @Initializable
  public String module = Shutdown.class.getName();

  /**
   * Constructor
   */
  public Shutdown()
  {
  }

  /**
   * Sets the system is shutdown */
  @Override
  public synchronized void shutdown()
  {
    _shutdown = true;
  }

  /**
   * Waits for shutdown to be completed. After calling shutdown, there may still be some pending
   * work that needs to be accomplised. This method will block until it is done.
   *
   * @throws InterruptedException  if interrupted while waiting
   * @throws IllegalStateException if shutdown has not been called
   */
  @Override
  public void waitForShutdown() throws InterruptedException, IllegalStateException
  {
    if(!_shutdown) throw new IllegalStateException("call shutdown first");
    pendingCallsCount.waitForCounter();
  }

  /**
   * Waits for shutdown to be completed. After calling shutdown, there may still be some pending work
   * that needs to be accomplised. This method will block until it is done but no longer than the
   * timeout.
   *
   * @param timeout how long to wait maximum for the shutdown
   * @throws InterruptedException  if interrupted while waiting
   * @throws IllegalStateException if shutdown has not been called
   * @throws TimeoutException      if shutdown still not complete after timeout
   */
  @Override
  public void waitForShutdown(Object timeout)
    throws InterruptedException, IllegalStateException, TimeoutException
  {
    if(!_shutdown) throw new IllegalStateException("call shutdown first");
    pendingCallsCount.waitForCounter(timeout);
  }

  /**
   * Called right before executing a call
   *
   * @throws ShutdownRequestedException
   */
  synchronized void startCall() throws ShutdownRequestedException
  {
    if(_shutdown)
      throw new ShutdownRequestedException(module);

    pendingCallsCount.inc();
  }

  /**
   * MUST be called if {@link #startCall()} is called after the call (typically in a
   * <code>finally</code>)
   */
  void endCall()
  {
    pendingCallsCount.dec();
  }

  /**
   * @return the number of currently pending calls
   */
  public int getPendingCallsCount()
  {
    return pendingCallsCount.getCounter();
  }
}
