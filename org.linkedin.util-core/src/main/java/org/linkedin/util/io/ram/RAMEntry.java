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

package org.linkedin.util.io.ram;

import org.linkedin.util.clock.Clock;

/**
 * @author ypujante@linkedin.com
 *
 */
public abstract class RAMEntry
{
  protected final Clock _clock;

  private final String _name;
  
  private long _lastModifiedDate;

  public RAMEntry(Clock clock, String name)
  {
    this(clock, name, clock.currentTimeMillis());
  }

  public RAMEntry(Clock clock,
                  String name,
                  long lastModifiedDate)
  {
    _clock = clock;
    _name = name;
    _lastModifiedDate = lastModifiedDate;
  }

  public String name()
  {
    return _name;
  }

  public long lastModified()
  {
    return _lastModifiedDate;
  }

  public void touch(long lastModifiedDate)
  {
    _lastModifiedDate = lastModifiedDate;
  }

  public void touch()
  {
    _lastModifiedDate = _clock.currentTimeMillis();
  }

  public abstract long getContentLength();
}
