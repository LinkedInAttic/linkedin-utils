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
public class SettableClock extends BaseClock
{
  private static final long serialVersionUID = 1L;

  private long _currentTimeMillis;

  private long _checkpointedCurrentTimeMillis = -1;

  /**
   * Constructor
   */
  public SettableClock()
  {
    this(System.currentTimeMillis());
  }

  /**
   * Constructor
   */
  public SettableClock(long currentTimeMillis)
  {
    setCurrentTimeMillis(currentTimeMillis);
  }

  /**
   * @return the current time of this clock in milliseconds.
   */
  @Override
  public long currentTimeMillis()
  {
    return _currentTimeMillis;
  }

  public void setCurrentTimeMillis(long currentTimeMillis)
  {
    _currentTimeMillis = currentTimeMillis;
  }

  public void addDuration(Timespan duration)
  {
    setCurrentTimeMillis(duration.futureTimeMillis(this));
  }

  public void subtractDuration(Timespan duration)
  {
    setCurrentTimeMillis(duration.pastTimeMillis(this));
  }

  /**
   * Creates a checkpoint (memorizes current time milliseconds)
   */
  public void checkpoint()
  {
    _checkpointedCurrentTimeMillis = _currentTimeMillis;
  }

  /**
   * Reverts to previous checkpoint
   * @return the new current time millis
   */
  public long revertToCheckpoint()
  {
    if(_checkpointedCurrentTimeMillis != -1)
    {
      _currentTimeMillis = _checkpointedCurrentTimeMillis;
      _checkpointedCurrentTimeMillis = -1;
    }
    return _currentTimeMillis;
  }
}
