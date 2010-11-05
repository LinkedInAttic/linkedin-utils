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

/**
 * @author ypujante@linkedin.com
 *
 */
public class ClockUtils
{
  /**
   * Converts the object into a timespan. If already a timespan then simply return the object.
   * Otherwise call {@link Timespan#parse(String)} on <code>t.toString()</code>. Works well for
   * long and <code>String</code>
   *
   * @return a timespan (<code>null</code> if t is <code>null</code>)
   */
  public static Timespan toTimespan(Object t)
  {
    if(t == null)
      return null;

    if(t instanceof Timespan)
    {
      return (Timespan) t;
    }

    return Timespan.parse(t.toString());
  }

  /**
   * Computes the end time = now + timeout
   *
   * @param timeout (see {@link #toTimespan(Object)})
   */
  public static long toEndTime(Object timeout)
  {
    return toEndTime(null, timeout);
  }

  /**
   * Computes the end time = now + timeout
   *
   * @param clock can be <code>null</code> to use system clock
   * @param timeout (see {@link #toTimespan(Object)})
   * @return 0 if 
   */
  public static long toEndTime(Clock clock, Object timeout)
  {
    Timespan t = toTimespan(timeout);
    if(t == null)
      return 0;
    if(clock == null)
      clock = SystemClock.INSTANCE;

    if(t.getDurationInMilliseconds() == 0)
      return 0;

    return t.futureTimeMillis(clock);
  }

  /**
   * Constructor
   */
  private ClockUtils()
  {
  }
}
