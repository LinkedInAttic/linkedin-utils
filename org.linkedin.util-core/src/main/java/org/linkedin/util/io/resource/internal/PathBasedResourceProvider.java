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

package org.linkedin.util.io.resource.internal;

import org.linkedin.util.io.PathUtils;

import java.net.URI;

/**
 * Case were there is a root path...
 * 
 * @author ypujante@linkedin.com
 *
 */
public abstract class PathBasedResourceProvider extends AbstractResourceProvider
{
  private final String _root;

  /**
   * Constructor
   */
  public PathBasedResourceProvider(String root)
  {
    root = URI.create(root).normalize().getPath();
    _root = PathUtils.addLeadingSlash(root);
  }

  protected String getFullPath(String path)
  {
    return PathUtils.addPaths(_root, path);
  }

  protected String getRelativePath(String fullPath)
  {
    return PathUtils.addLeadingSlash(fullPath.substring(_root.length()));
  }

  public String getRoot()
  {
    return _root;
  }

  @Override
  public boolean equals(Object o)
  {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;

    PathBasedResourceProvider that = (PathBasedResourceProvider) o;

    if(!_root.equals(that._root)) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    return _root.hashCode();
  }
}
