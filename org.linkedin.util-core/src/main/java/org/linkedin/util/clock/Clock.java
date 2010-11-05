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

import java.util.Date;

/**
 * Abstraction of a clock.
 *
 * @author ypujante@linkedin.com
 */
public interface Clock
{
  // some commonly used constants
  public static final long SECOND_IN_MS = Timespan.TimeUnit.SECOND.getMillisecondsCount();
  public static final long MINUTE_IN_MS = Timespan.TimeUnit.MINUTE.getMillisecondsCount();
  public static final long HOUR_IN_MS = Timespan.TimeUnit.HOUR.getMillisecondsCount();
  public static final long DAY_IN_MS = Timespan.TimeUnit.DAY.getMillisecondsCount();

  /**
   * @return the current time of this clock in milliseconds. */
  long currentTimeMillis();

  /**
   * @return the current date of this clock. */
  Date currentDate();
}