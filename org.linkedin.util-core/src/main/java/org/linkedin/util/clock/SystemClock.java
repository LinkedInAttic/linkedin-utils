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

package org.linkedin.util.clock;

import java.io.Serializable;

/**
 * Implementation of a clock using the system clock.
 *
 * @author ypujante@linkedin.com
 */
public class SystemClock extends BaseClock implements Serializable
{
  private static final long serialVersionUID = 1L;

  public static final SystemClock INSTANCE = new SystemClock();

  public static SystemClock instance()
  {
    return INSTANCE;
  }

  /**
   * Constructor */
  public SystemClock()
  {
  }

  /**
   * @return the current time of this clock in milliseconds. */
  @Override
  public long currentTimeMillis()
  {
    return System.currentTimeMillis();
  }
}
