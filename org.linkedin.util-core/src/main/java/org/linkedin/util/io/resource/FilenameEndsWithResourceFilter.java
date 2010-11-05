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

package org.linkedin.util.io.resource;

import java.io.IOException;

/**
 * Accepts only resources where the filename ends with the filter... equivalent to *filter in shell.
 * 
 * @author ypujante@linkedin.com
 *
 */
public class FilenameEndsWithResourceFilter implements ResourceFilter
{
  private final String _filter;

  /**
   * Constructor
   */
  public FilenameEndsWithResourceFilter(String filter)
  {
    _filter = filter;
  }

  /**
   * Tests whether this resource should be included in the result.
   *
   * @param resource the resource to check
   * @return <code>true</code> if the resource should be included
   * @throws IOException
   */
  @Override
  public boolean accept(Resource resource)
  {
    return resource.getFilename().endsWith(_filter);
  }
}
