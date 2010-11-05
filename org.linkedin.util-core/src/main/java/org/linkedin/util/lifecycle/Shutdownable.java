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

import org.linkedin.util.clock.ClockUtils;
import org.linkedin.util.clock.Timespan;

import java.util.concurrent.TimeoutException;

/**
 * Defines the method of an entity that is shutdownable
 *
 * @author  ypujante@linkedin.com */
public interface Shutdownable extends Terminable
{
  /**
   * This methods sets the entity in shutdown mode. Any method call on this
   * entity after shutdown should be either rejected
   * (<code>IllegalStateException</code>) or discarded. This method should
   * not block and return immediately.
   *
   * @see #waitForShutdown() */
  public void shutdown();

  /**
   * Waits for shutdown to be completed. After calling shutdown, there may
   * still be some pending work that needs to be accomplised. This method
   * will block until it is done.
   *
   * @exception InterruptedException if interrupted while waiting
   * @exception IllegalStateException if shutdown has not been called */
  public void waitForShutdown()
    throws InterruptedException, IllegalStateException;

  /**
   * Waits for shutdown to be completed. After calling shutdown, there may
   * still be some pending work that needs to be accomplised. This method
   * will block until it is done but no longer than the timeout.
   *
   * @param timeout how long to wait maximum for the shutdown
   *                (see {@link ClockUtils#toTimespan(Object)})
   * @exception InterruptedException if interrupted while waiting
   * @exception IllegalStateException if shutdown has not been called
   * @exception TimeoutException if shutdown still not complete after timeout */
  public void waitForShutdown(Object timeout)
    throws InterruptedException, IllegalStateException, TimeoutException;
}
