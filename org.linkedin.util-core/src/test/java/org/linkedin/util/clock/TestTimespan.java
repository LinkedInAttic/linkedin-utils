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

import junit.framework.TestCase;

import java.util.EnumMap;

/**
 * Tests for class {@link Timespan}
 *
 * @author ypujante@linkedin.com
 *
 */
public class TestTimespan extends TestCase
{
  // those constants are redefined here to make sure that the tests are not biased
  private static final long SECOND_IN_MS = 1000L;
  private static final long MINUTE_IN_MS = SECOND_IN_MS * 60L;
  private static final long HOUR_IN_MS = MINUTE_IN_MS * 60L;
  private static final long DAY_IN_MS = HOUR_IN_MS * 24L;
  private static final long WEEK_IN_MS = DAY_IN_MS * 7L;

  /**
   * Constructor
   */
  public TestTimespan(String name)
  {
    super(name);
  }

  /**
   * Test with 0 timespan
   */
  public void testZeroTimespan()
  {
    for(Timespan.TimeUnit timeUnit: Timespan.TimeUnit.values())
      checkZeroTimespan(timeUnit);
  }

  /**
   * Internal checks for 0 timespan
   *
   * @param timeUnit
   */
  private void checkZeroTimespan(Timespan.TimeUnit timeUnit)
  {
    Timespan timespan = new Timespan(0, timeUnit);
    assertEquals(timeUnit, timespan.getTimeUnit());
    assertEquals(0, timespan.getDurationInMilliseconds());
    assertTrue("equals to zero", timespan.equalsDurationInMilliseconds(Timespan.ZERO_MILLISECONDS));
    assertEquals("canonical string should be 0" + timeUnit.getDisplayChar(),
                 "0" + timeUnit.getDisplayChar(),
                 timespan.getCanonicalString());

    EnumMap<Timespan.TimeUnit,Timespan> timespans = timespan.getCanonicalTimespans();
    assertEquals(8, timespans.size());
    for(Timespan tspan : timespans.values())
    {
      assertTrue("equals to zero", tspan.equalsDurationInMilliseconds(Timespan.ZERO_MILLISECONDS));
    }
  }

  /**
   * Test for milliseconds
   */
  public void testTimespanMillisecond()
  {
    final long milliseconds = 950;
    Timespan timespan = new Timespan(milliseconds, Timespan.TimeUnit.MILLISECOND);
    assertEquals(Timespan.TimeUnit.MILLISECOND, timespan.getTimeUnit());

    assertEquals(milliseconds, timespan.getDuration());
    assertEquals(milliseconds, timespan.getDurationInMilliseconds());

    assertEquals("950", timespan.getCanonicalString());

    assertEquals("950", timespan.getAsString(Timespan.TimeUnit.MILLISECOND));
    assertEquals("0", timespan.getAsString(Timespan.TimeUnit.SECOND));
    assertEquals("0", timespan.getAsString(Timespan.TimeUnit.MINUTE));
    assertEquals("0", timespan.getAsString(Timespan.TimeUnit.HOUR));
    assertEquals("0", timespan.getAsString(Timespan.TimeUnit.DAY));
    assertEquals("0", timespan.getAsString(Timespan.TimeUnit.WEEK));
  }

  /**
   * Test for seconds
   */
  public void testTimespanSecond()
  {
    final long seconds = 55;
    Timespan timespan = new Timespan(seconds, Timespan.TimeUnit.SECOND);
    assertEquals(Timespan.TimeUnit.SECOND, timespan.getTimeUnit());

    assertEquals(seconds, timespan.getDuration());
    assertEquals(seconds * SECOND_IN_MS, timespan.getDurationInMilliseconds());

    assertEquals("55s", timespan.getCanonicalString());

    assertEquals("55s", timespan.getAsString(Timespan.TimeUnit.SECOND));
    assertEquals("55000", timespan.getAsString(Timespan.TimeUnit.MILLISECOND));
    assertEquals("0", timespan.getAsString(Timespan.TimeUnit.MINUTE));
    assertEquals("0", timespan.getAsString(Timespan.TimeUnit.HOUR));
    assertEquals("0", timespan.getAsString(Timespan.TimeUnit.DAY));
    assertEquals("0", timespan.getAsString(Timespan.TimeUnit.WEEK));
  }

  /**
   * Test for minutes
   */
  public void testTimespanMinute()
  {
    final long minutes = 55;
    Timespan timespan = new Timespan(minutes, Timespan.TimeUnit.MINUTE);
    assertEquals(Timespan.TimeUnit.MINUTE, timespan.getTimeUnit());

    assertEquals(minutes, timespan.getDuration());
    assertEquals(minutes * MINUTE_IN_MS, timespan.getDurationInMilliseconds());

    assertEquals("55m", timespan.getCanonicalString());

    assertEquals("55m", timespan.getAsString(Timespan.TimeUnit.MINUTE));
    assertEquals("3300s", timespan.getAsString(Timespan.TimeUnit.SECOND));
    assertEquals("3300000", timespan.getAsString(Timespan.TimeUnit.MILLISECOND));
    assertEquals("0", timespan.getAsString(Timespan.TimeUnit.HOUR));
    assertEquals("0", timespan.getAsString(Timespan.TimeUnit.DAY));
    assertEquals("0", timespan.getAsString(Timespan.TimeUnit.WEEK));
  }

  /**
   * Test for hours
   */
  public void testTimespanHour()
  {
    final long hours = 23;
    Timespan timespan = new Timespan(hours, Timespan.TimeUnit.HOUR);
    assertEquals(Timespan.TimeUnit.HOUR, timespan.getTimeUnit());

    assertEquals(hours, timespan.getDuration());
    assertEquals(hours * HOUR_IN_MS, timespan.getDurationInMilliseconds());

    assertEquals("23h", timespan.getCanonicalString());

    assertEquals("23h", timespan.getAsString(Timespan.TimeUnit.HOUR));
    assertEquals("1380m", timespan.getAsString(Timespan.TimeUnit.MINUTE));
    assertEquals("82800s", timespan.getAsString(Timespan.TimeUnit.SECOND));
    assertEquals("82800000", timespan.getAsString(Timespan.TimeUnit.MILLISECOND));
    assertEquals("0", timespan.getAsString(Timespan.TimeUnit.DAY));
    assertEquals("0", timespan.getAsString(Timespan.TimeUnit.WEEK));
  }

  /**
   * Test for days
   */
  public void testTimespanDay()
  {
    final long days = 6;
    Timespan timespan = new Timespan(days, Timespan.TimeUnit.DAY);
    assertEquals(Timespan.TimeUnit.DAY, timespan.getTimeUnit());

    assertEquals(days, timespan.getDuration());
    assertEquals(days * DAY_IN_MS, timespan.getDurationInMilliseconds());

    assertEquals("6d", timespan.getCanonicalString());

    assertEquals("6d", timespan.getAsString(Timespan.TimeUnit.DAY));
    assertEquals("144h", timespan.getAsString(Timespan.TimeUnit.HOUR));
    assertEquals("8640m", timespan.getAsString(Timespan.TimeUnit.MINUTE));
    assertEquals("518400s", timespan.getAsString(Timespan.TimeUnit.SECOND));
    assertEquals("518400000", timespan.getAsString(Timespan.TimeUnit.MILLISECOND));
    assertEquals("0", timespan.getAsString(Timespan.TimeUnit.WEEK));
  }

  /**
   * Test for weeks
   */
  public void testTimespanWeek()
  {
    final long weeks = 2;
    Timespan timespan = new Timespan(weeks, Timespan.TimeUnit.WEEK);
    assertEquals(Timespan.TimeUnit.WEEK, timespan.getTimeUnit());

    assertEquals(weeks, timespan.getDuration());
    assertEquals(weeks * WEEK_IN_MS, timespan.getDurationInMilliseconds());

    assertEquals("2w", timespan.getCanonicalString());

    assertEquals("2w", timespan.getAsString(Timespan.TimeUnit.WEEK));
    assertEquals("14d", timespan.getAsString(Timespan.TimeUnit.DAY));
    assertEquals("336h", timespan.getAsString(Timespan.TimeUnit.HOUR));
    assertEquals("20160m", timespan.getAsString(Timespan.TimeUnit.MINUTE));
    assertEquals("1209600s", timespan.getAsString(Timespan.TimeUnit.SECOND));
    assertEquals("1209600000", timespan.getAsString(Timespan.TimeUnit.MILLISECOND));
  }

  /**
   * Test that canonical returns values in h/m/s and approximate returns the full blown thing.
   */
  public void testCanonical()
  {
    Timespan timespan = Timespan.create(new Timespan(1, Timespan.TimeUnit.YEAR),
                                        new Timespan(8, Timespan.TimeUnit.MONTH),
                                        new Timespan(2, Timespan.TimeUnit.WEEK),
                                        new Timespan(4, Timespan.TimeUnit.DAY),
                                        new Timespan(7, Timespan.TimeUnit.HOUR),
                                        new Timespan(15, Timespan.TimeUnit.MINUTE),
                                        new Timespan(40, Timespan.TimeUnit.SECOND),
                                        new Timespan(500, Timespan.TimeUnit.MILLISECOND));

    assertEquals("1y8M2w4d7h15m40s500", timespan.getCanonicalString());

    EnumMap<Timespan.TimeUnit, Timespan> ts = timespan.getCanonicalTimespans();
    assertEquals(8, ts.size());
    assertEquals(1, ts.get(Timespan.TimeUnit.YEAR).getDuration());
    assertEquals(8, ts.get(Timespan.TimeUnit.MONTH).getDuration());
    assertEquals(2, ts.get(Timespan.TimeUnit.WEEK).getDuration());
    assertEquals(4, ts.get(Timespan.TimeUnit.DAY).getDuration());
    assertEquals(7, ts.get(Timespan.TimeUnit.HOUR).getDuration());
    assertEquals(15, ts.get(Timespan.TimeUnit.MINUTE).getDuration());
    assertEquals(40, ts.get(Timespan.TimeUnit.SECOND).getDuration());
    assertEquals(500, ts.get(Timespan.TimeUnit.MILLISECOND).getDuration());

    timespan = Timespan.create(new Timespan(2, Timespan.TimeUnit.WEEK),
                               new Timespan(4, Timespan.TimeUnit.DAY),
                               new Timespan(15, Timespan.TimeUnit.MINUTE),
                               new Timespan(40, Timespan.TimeUnit.SECOND),
                               new Timespan(500, Timespan.TimeUnit.MILLISECOND));

    assertEquals("2w4d15m40s500", timespan.getCanonicalString());

    ts = timespan.getCanonicalTimespans();
    assertEquals(8, ts.size());
    assertEquals(2, ts.get(Timespan.TimeUnit.WEEK).getDuration());
    assertEquals(4, ts.get(Timespan.TimeUnit.DAY).getDuration());
    assertEquals(0, ts.get(Timespan.TimeUnit.HOUR).getDuration());
    assertEquals(15, ts.get(Timespan.TimeUnit.MINUTE).getDuration());
    assertEquals(40, ts.get(Timespan.TimeUnit.SECOND).getDuration());
    assertEquals(500, ts.get(Timespan.TimeUnit.MILLISECOND).getDuration());
  }

  public void testFilter()
  {
    // simple tests
    checkFilter("1", "1", Timespan.TimeUnit.MILLISECOND);
    checkFilter("1s", "1s", Timespan.TimeUnit.SECOND);
    checkFilter("1m", "1m", Timespan.TimeUnit.MINUTE);
    checkFilter("1h", "1h", Timespan.TimeUnit.HOUR);
    checkFilter("1d", "1d", Timespan.TimeUnit.DAY);
    checkFilter("1w", "1w", Timespan.TimeUnit.WEEK);

    checkFilter("1s", "0m", Timespan.TimeUnit.MINUTE);

    checkFilter("3h4m2s512", "3h4m2s", Timespan.TimeUnit.HOUR, Timespan.TimeUnit.MINUTE, Timespan.TimeUnit.SECOND);
    checkFilter("3h4m2s512", "3h2s", Timespan.TimeUnit.HOUR, Timespan.TimeUnit.SECOND);
    checkFilter("3h4m2s512", "3h", Timespan.TimeUnit.HOUR);

  }

  private void checkFilter(String timespan, String expectedTimespan, Timespan.TimeUnit... timeUnits)
  {
    Timespan ts = Timespan.parseTimespan(timespan);
    ts = ts.filter(timeUnits);
    assertEquals(expectedTimespan, ts.getCanonicalString());
  }

  /**
   * Test for parsing a timespan
   */
  public void testParseTimespan()
  {
    checkParseTimespan("6");
    checkParseTimespan("6s");
    checkParseTimespan("6m");
    checkParseTimespan("6h");
    checkParseTimespan("6d");
    checkParseTimespan("3w");
    checkParseTimespan("6M");
    checkParseTimespan("6y");

    checkParseTimespan("1");
    checkParseTimespan("1s");
    checkParseTimespan("1m");
    checkParseTimespan("1h");
    checkParseTimespan("1d");
    checkParseTimespan("1w");
    checkParseTimespan("1M");
    checkParseTimespan("1y");

    checkParseTimespan("0");
    checkParseTimespan("0s");
    checkParseTimespan("0m");
    checkParseTimespan("0h");
    checkParseTimespan("0d");
    checkParseTimespan("0w");
    checkParseTimespan("0M");
    checkParseTimespan("0y");

    checkParseTimespan("3w6d59m59s999");
    checkParseTimespan("2w8d25h62m62s1002", "3w2d2h3m3s2");
    checkParseTimespan("8d25h62m62s1002", "1w2d2h3m3s2");
    checkParseTimespan("2w25h62m62s1002", "2w1d2h3m3s2");
    checkParseTimespan("2w8d62m62s1002", "3w1d1h3m3s2");
    checkParseTimespan("2w8d25h62s1002", "3w2d1h1m3s2");
    checkParseTimespan("2w8d25h62m1002", "3w2d2h2m1s2");
    checkParseTimespan("2w8d25h62m62s", "3w2d2h3m2s");

    checkParseTimespan("1d0h3m0s0", "1d3m");

    try
    {
      Timespan.parseTimespan("abc");
      fail("abc is not valid");
    }
    catch(IllegalArgumentException e)
    {
      // expected
    }

    try
    {
      Timespan.parseTimespan("2m2h");
      fail("2m2h is not valid");
    }
    catch(IllegalArgumentException e)
    {
      // expected
    }
    try
    {
      Timespan.parseTimespan("2h2h");
      fail("2h2h is not valid");
    }
    catch(IllegalArgumentException e)
    {
      // expected
    }
  }

  private void checkParseTimespan(String timespanString)
  {
    checkParseTimespan(timespanString, timespanString);
  }

  private void checkParseTimespan(String timespanString, String expectedCanonicalString)
  {
    Timespan timespan = Timespan.parseTimespan(timespanString);
    assertEquals(expectedCanonicalString, timespan.getCanonicalString());
  }

  /**
   * Test the truncate feature
   */
  public void testTruncate()
  {
    Timespan timespan = Timespan.parseTimespan("1h20m5s");

    assertEquals("0w", timespan.truncate(Timespan.TimeUnit.WEEK).getCanonicalString());
    assertEquals("0d", timespan.truncate(Timespan.TimeUnit.DAY).getCanonicalString());
    assertEquals("1h", timespan.truncate(Timespan.TimeUnit.HOUR).getCanonicalString());
    assertEquals("1h20m", timespan.truncate(Timespan.TimeUnit.MINUTE).getCanonicalString());
    assertEquals("1h20m5s", timespan.truncate(Timespan.TimeUnit.SECOND).getCanonicalString());
    assertEquals("1h20m5s", timespan.truncate(Timespan.TimeUnit.MILLISECOND).getCanonicalString());

    assertEquals(0, timespan.getDuration(Timespan.TimeUnit.WEEK));
    assertEquals(0, timespan.getDuration(Timespan.TimeUnit.DAY));
    assertEquals(1, timespan.getDuration(Timespan.TimeUnit.HOUR));
    assertEquals(80, timespan.getDuration(Timespan.TimeUnit.MINUTE));
    assertEquals(4805, timespan.getDuration(Timespan.TimeUnit.SECOND));
    assertEquals(4805000, timespan.getDuration(Timespan.TimeUnit.MILLISECOND));

    assertEquals(1, timespan.getDurationInHours());
    assertEquals(80, timespan.getDurationInMinutes());
    assertEquals(4805, timespan.getDurationInSeconds());
    assertEquals(4805000, timespan.getDurationInMilliseconds());

  }
  
  /**
   * Test subtracting timespans
   */
  public void testSubtract()
  {
    Timespan ts0sec = Timespan.parse("0s");
    Timespan ts1sec = Timespan.parse("1s");
    Timespan ts2sec = Timespan.parse("2s");
    Timespan ts3sec = Timespan.parse("3s");
    
    Timespan ts2min = Timespan.parse("2m");
    Timespan ts1min58sec = Timespan.parse("1m58s");
    
    Timespan diff = ts3sec.substractWithZeroFloor(ts2sec);
    assertEquals(ts1sec, diff);
    
    diff = ts2sec.substractWithZeroFloor(ts3sec);
    assertEquals(ts0sec, diff);
    
    diff = ts2min.substractWithZeroFloor(ts2sec);
    assertEquals(ts1min58sec, diff);
    
    diff = ts2sec.substractWithZeroFloor(ts2min);
    assertEquals(Timespan.ZERO_MILLISECONDS, diff);
  }
}
