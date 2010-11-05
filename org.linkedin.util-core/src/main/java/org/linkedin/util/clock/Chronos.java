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

import java.io.PrintWriter;
import java.io.OutputStream;

/**
 * This class is used to measure time intervals... Each time you tick, you
 * get the amount of time ellapsed since the last one
 *
 *
 * @author ypujante@linkedin.com */
public class Chronos
{
  /**
   * Where to print information
   */
  private final PrintWriter _str;

  /**
   * The last tick
   */
  private long _tick = 0;

  /**
   * When the chronos is started  */
  private long _startTime;

  /**
   * The clock to use  */
  private final Clock _clock;

  /**
   * Constructor : initialize the tick and sets no stream */
  public Chronos()
  {
    this((PrintWriter) null);
  }

  /**
   * Constructor : initialize the tick and set the stream to the one given
   *
   * @param str the print stream for the output */
  public Chronos(PrintWriter str)
  {
    this(new SystemClock(), str);
  }

  /**
   * Constructor : initialize the tick and set the stream to the one given
   *
   * @param str the print stream for the output */
  public Chronos(OutputStream str)
  {
    this(new PrintWriter(str));
  }

  /**
   * Constructor : initialize the tick and sets no stream */
  public Chronos(Clock clock)
  {
    this(clock, (PrintWriter) null);
  }

  /**
   * Constructor : initialize the tick and set the stream to the one given
   *
   * @param str the print stream for the output */
  public Chronos(Clock clock, PrintWriter str)
  {
    _str = str;
    _clock = clock;
    _startTime = _clock.currentTimeMillis();
    tick();
  }

  /**
   * Constructor : initialize the tick and set the stream to the one given
   *
   * @param str the print stream for the output */
  public Chronos(Clock clock, OutputStream str)
  {
    this(clock, new PrintWriter(str));
  }

  /**
   * Returns the number of milliseconds ellapsed since the last call to
   * this function.
   *
   * @return the number of milliseconds since last call*/
  public long tick()
  {
    long tick = _clock.currentTimeMillis();
    long diff = tick - _tick;

    _tick = tick;

    return diff;
  }

  /**
   * @return the total time since start of this chronos */
  public long getTotalTime()
  {
    return _clock.currentTimeMillis() - _startTime;
  }

  /**
   * Display the time ellapsed since the last call (with no message) */
  public void displayElapsedTime()
  {
    this.displayElapsedTime("");
  }

  /**
   * Display the time ellapsed since the last call (with an additional
   * message)
   *
   * @param mess the message to display first */
  public void displayElapsedTime(String mess)
  {
    if(_str != null)
      _str.println(mess + getElapsedTime());
  }

  /**
   * Returns a string that represents the time elapsed
   *
   * @return the elapsed time as a string */
  public String getElapsedTime()
  {
    StringBuilder sb = new StringBuilder("Time: ");
    sb.append(this.tick());
    sb.append(" ms");

    return sb.toString();
  }

  /**
   * @return the elapsed time as hour/minute/second */
  public String getElapsedTimeAsHMS()
  {
    return new Timespan(tick()).getCanonicalString();
  }

  /**
   * Flushes the underlying writer */
  public void flush()
  {
    if(_str != null)
      _str.flush();
  }
}
