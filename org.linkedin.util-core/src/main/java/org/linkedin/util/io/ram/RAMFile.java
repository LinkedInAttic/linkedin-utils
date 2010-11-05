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
public class RAMFile extends RAMEntry
{
  private final byte[] _content;


  public RAMFile(Clock clock, String name, byte[] content)
  {
    super(clock, name);
    _content = content;
  }

  public RAMFile(Clock clock, String name, long lastModifiedDate, byte[] content)
  {
    super(clock, name, lastModifiedDate);
    _content = content;
  }

  public byte[] getContent()
  {
    return _content;
  }

  @Override
  public long getContentLength()
  {
    return _content.length;
  }
}
