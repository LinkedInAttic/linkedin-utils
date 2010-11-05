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


package org.linkedin.util.io;

/**
 * Utility methods for path related computation.
 *
 * @author ypujante@linkedin.com
 *
 */
public class PathUtils
{
  /**
   * Adds 2 paths taking into consideration the /
   *
   * @param path1
   * @param path2
   * @return path1 + path2
   */
  public static String addPaths(String path1, String path2)
  {
    if(path1.endsWith("/"))
    {
      if(path2.startsWith("/"))
        return path1.substring(0, path1.length() - 1) + path2;
      else
        return path1 + path2;
    }
    else
    {
      if(path2.startsWith("/"))
        return path1 + path2;
      else
        return path1 + "/" + path2;
    }
  }

  /**
   * Adds a leading slash if not already exists
   *
   * @param path
   * @return the path with a leading /
   */
  public static String addLeadingSlash(String path)
  {
    if(!path.startsWith("/"))
      return "/" + path;
    else
      return path;
  }

  /**
   * Removes a leading slash if exists
   *
   * @param path
   * @return the path without a leading /
   */
  public static String removeLeadingSlash(String path)
  {
    if(path.startsWith("/"))
      return path.substring(1);
    else
      return path;
  }

  /**
   * Adds a trailing slash if not already exists
   *
   * @param path
   * @return the path with a trailing /
   */
  public static String addTrailingSlash(String path)
  {
    if(!path.endsWith("/"))
      return path + "/";
    else
      return path;
  }

  /**
   * Removes a trailing slash if exists
   *
   * @param path
   * @return the path without a trailing /
   */
  public static String removeTrailingSlash(String path)
  {
    if(path.endsWith("/"))
      return path.substring(0, path.length() - 1);
    else
      return path;
  }

  /**
   * @param path
   * @return the parent path given a path
   */
  public static String getParentPath(String path)
  {
    String parentPath = path;

    if("/".equals(parentPath))
      return parentPath;

    parentPath = PathUtils.removeTrailingSlash(parentPath);

    int idx = parentPath.lastIndexOf("/");
    if(idx >= 0)
      parentPath = parentPath.substring(0, idx + 1);
    else
      parentPath = "/";

    return parentPath;
  }

  /**
   * Constructor
   */
  public PathUtils()
  {
  }
}
