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
import org.linkedin.util.io.resource.NullResource;
import org.linkedin.util.io.resource.ResourceFilter;

/**
 * Implements the null pattern for resource (does not exist)
 * @author ypujante@linkedin.com
 */
public class NullResourceProvider extends PathBasedResourceProvider
{


  /**
   * Constructor
   */
  public NullResourceProvider()
  {
    this("/");
  }

  /**
   * Constructor
   */
  public NullResourceProvider(String root)
  {
    super(normalizePath(root));
  }

  /**
   * Builds a resource given a path. Only subclasses know how to do that.
   *
   * @param path the path to the new resource (always starts with /)
   * @return the resource
   */
  @Override
  public InternalResource doBuildResource(String path)
  {
    return new NullResource(this, normalizePath(path), normalizePath(getFullPath(path)));
  }

  /**
   * The path never represents a directory => should not end with /
   */
  private static String normalizePath(String path)
  {
    path = PathUtils.removeTrailingSlash(path);
    path = PathUtils.addLeadingSlash(path);
    return path;
  }

  /**
   * Creates a new resource provider given the new path.
   *
   * @param rootPath
   * @return the new resource provider
   */
  @Override
  public InternalResourceProvider doCreateResourceProvider(String rootPath)
  {
    return new NullResourceProvider(getFullPath(rootPath));
  }

  /**
   * If the path denotes a directory, then it will return all resources that are contained in the
   * directory.
   *
   * @param path   the path to the resource
   * @param filter the filter to include only some resources in the result
   * @return <code>true</code> if it was a directory, <code>false</code> otherwise
   */
  @Override
  public boolean doList(String path, ResourceFilter filter)
  {
    return false;
  }
}