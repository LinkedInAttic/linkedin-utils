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
import org.linkedin.util.io.PathUtils;
import org.linkedin.util.io.resource.JarResource;
import org.linkedin.util.io.resource.Resource;
import org.linkedin.util.io.resource.ResourceFilter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author ypujante@linkedin.com
 *
 */
public class JarResourceProvider extends PathBasedResourceProvider
{
  public static final String MODULE = JarResourceProvider.class.getName();
  public static final Logger log = LoggerFactory.getLogger(MODULE);

  private final LeafResource _jarResource;

  /**
   * Constructor
   */
  public JarResourceProvider(Resource jarResource)
  {
    this(jarResource, "/");
  }

  /**
   * Constructor
   */
  public JarResourceProvider(Resource jarResource, String root)
  {
    super(root);
    // we need to have access to a File no matter what...
    _jarResource = LocalCacheLeafResource.create(jarResource);
  }

  /**
   * Constructor
   */
  private JarResourceProvider(LeafResource jarResource, String root)
  {
    super(root);
    _jarResource = jarResource;
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
    return new JarResourceProvider(_jarResource, getFullPath(rootPath));
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
    return new JarResource(this, path, _jarResource, getFullPath(path));
  }

  /**
   * If the path denotes a directory, then it will return all resources that are contained in the
   * directory.
   *
   * @param path   the path to the resource (it already ends with /)
   * @param filter the filter to include only some resources in the result
   * @return <code>true</code> if it was a directory, <code>false</code> otherwise
   */
  @Override
  public boolean doList(String path, ResourceFilter filter)
  {
    try
    {
      JarFile jarFile = new JarFile(_jarResource.getFile());
      try
      {
        return doList(path, filter, jarFile);
      }
      finally
      {
        jarFile.close();
      }
    }
    catch(IOException e)
    {
      if(log.isDebugEnabled())
        log.debug("exception (ignored) while listing path " + path, e);

      return false;
    }
  }

  private boolean doList(String path, ResourceFilter filter, JarFile jarFile)
  {
    String directory = PathUtils.removeLeadingSlash(getFullPath(path));

    boolean isDirectory = false;

    // we iterate over ALL entries in the jar file (unfortunately there is no other api to
    // do that :( ).
    Enumeration<JarEntry> iter = jarFile.entries();
    while(iter.hasMoreElements())
    {
      JarEntry entry = iter.nextElement();
      String entryName = entry.getName();

      // we found the root entry => we know for sure it is a directory! but we don't add it to
      // the resultset
      if(entryName.equals(directory))
      {
        isDirectory = true;
        continue;
      }

      if(entryName.startsWith(directory))
      {
        int idx = entryName.indexOf("/", directory.length());
        // if it is a simple file or a subdir then accept it
        if(idx == -1 || idx == (entryName.length() - 1))
        {
          isDirectory = true;
          Resource resource = new JarResource(this,
                                              getRelativePath(PathUtils.addLeadingSlash(entryName)),
                                              _jarResource,
                                              entryName);

          filter.accept(resource);
        }
      }
    }

    return isDirectory;
  }


  @Override
  public boolean equals(Object o)
  {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    if(!super.equals(o)) return false;

    JarResourceProvider that = (JarResourceProvider) o;

    if(!_jarResource.equals(that._jarResource)) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = super.hashCode();
    result = 31 * result + _jarResource.hashCode();
    return result;
  }
}
