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
import org.linkedin.util.io.ram.RAMDirectory;
import org.linkedin.util.io.ram.RAMEntry;
import org.linkedin.util.io.resource.RAMResource;
import org.linkedin.util.io.resource.ResourceFilter;

import java.io.IOException;

/**
 * @author ypujante@linkedin.com
 *
 */
public class RAMResourceProvider extends PathBasedResourceProvider
{
  private final RAMDirectory _root;

  /**
   * Constructor
   */
  public RAMResourceProvider(RAMDirectory root)
  {
    this(root, "/");
  }

  /**
   * Constructor
   */
  public RAMResourceProvider(RAMDirectory root, String rootPath)
  {
    super(rootPath);
    _root = root;
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
    return new RAMResource(this, getFullPath(path), path);
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
    return new RAMResourceProvider(_root, getFullPath(rootPath));
  }

  /**
   * If the path denotes a directory, then it will return all resources that are contained in the
   * directory.
   *
   * @param path   the path to the resource
   * @param filter the filter to include only some resources in the result
   * @return <code>true</code> if it was a directory, <code>false</code> otherwise
   * @throws IOException if there is an error accessing the resource
   */
  @Override
  public boolean doList(String path, ResourceFilter filter)
  {
    if(_root == null)
      return false;

    RAMEntry entry = _root.getEntryByPath(path);

    if(entry instanceof RAMDirectory)
    {
      RAMDirectory ramDirectory = (RAMDirectory) entry;
      for(RAMEntry ramEntry : ramDirectory.ls())
      {
        String resourcePath = PathUtils.addPaths(path, ramEntry.name());
        filter.accept(new RAMResource(this,
                                      getFullPath(resourcePath),
                                      resourcePath));
      }
      return true;
    }
    else
      return false;
  }

  public RAMEntry getRAMEntry(String fullpath)
  {
    return _root.getEntryByPath(fullpath);
  }

  @Override
  public boolean equals(Object o)
  {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    if(!super.equals(o)) return false;

    RAMResourceProvider that = (RAMResourceProvider) o;

    if(_root != null ? !_root.equals(that._root) : that._root != null) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = super.hashCode();
    result = 31 * result + (_root != null ? _root.hashCode() : 0);
    return result;
  }
}
