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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.linkedin.util.io.resource.FileResource;
import org.linkedin.util.io.resource.ResourceFilter;

import java.io.File;
import java.io.IOException;

/**
 * @author ypujante@linkedin.com
 *
 */
public class FileResourceProvider extends AbstractResourceProvider
{
  public static final String MODULE = FileResourceProvider.class.getName();
  public static final Logger log = LoggerFactory.getLogger(MODULE);

  private final File _root;

  /**
   * Constructor
   */
  public FileResourceProvider(File root) throws IOException
  {
    _root = root.getCanonicalFile();
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
    return new FileResource(this, path, new File(_root, path));
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
    try
    {
      return new FileResourceProvider(new File(_root, rootPath));
    }
    catch(IOException e)
    {
      throw new RuntimeException(e);
    }
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
    File file = null;
    try
    {
      file = new File(_root, path).getCanonicalFile();
    }
    catch(IOException e)
    {
      if(log.isDebugEnabled())
        log.debug("exception (ignored) while converting canonical file " + new File(_root, path), e);
      
      return false;
    }

    if(!file.isDirectory())
      return false;

    File[] files = file.listFiles();

    for(File f : files)
    {
      filter.accept(new FileResource(this, path + f.getName(), f));
    }
    
    return true;
  }

  @Override
  public boolean equals(Object o)
  {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;

    FileResourceProvider that = (FileResourceProvider) o;

    if(!_root.equals(that._root)) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    return _root.hashCode();
  }
}
