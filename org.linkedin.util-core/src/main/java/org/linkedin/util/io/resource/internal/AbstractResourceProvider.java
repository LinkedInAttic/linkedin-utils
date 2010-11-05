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
import org.linkedin.util.io.resource.Resource;
import org.linkedin.util.io.resource.ResourceFilter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ypujante@linkedin.com
 *
 */
public abstract class AbstractResourceProvider implements InternalResourceProvider
{
  /**
   * Constructor
   */
  public AbstractResourceProvider()
  {
  }

  /**
   * The path to the resource is absolute. Whether it starts with / or not it will be made
   * absolute.
   * <p/>
   * Note that it follows the same convention as {@link File} class in the sense that a resource
   * simply represents a handle and does not imply that the resource exists.
   *
   * @param path the path to the resource
   * @return the resource given the path
   */
  @Override
  public Resource createResource(String path)
  {
    if(path == null)
      return null;

    URI uri = URI.create(path).normalize();

    if(uri.isAbsolute())
      throw new IllegalArgumentException(path + " => only path with no scheme are supported...");

    path = uri.toString();

    return doBuildResource(PathUtils.addLeadingSlash(path));
  }

  /**
   * @param resource
   * @return the parent of a resource
   */
  @Override
  public InternalResource getParentResource(InternalResource resource)
  {
    String parentPath = PathUtils.getParentPath(resource.getPath());

    if(parentPath.equals(resource.getPath()))
      // we are at the root...
      return resource;
    else
      return doBuildResource(parentPath);
  }

  /**
   * @return the root resource
   */
  @Override
  public InternalResource getRootResource()
  {
    return doBuildResource("/");
  }

  /**
   * @param resource
   * @param relativePath the relative path
   * @return a resource relative to the resource
   */
  @Override
  public final InternalResource createRelative(InternalResource resource, String relativePath)
  {
    if(relativePath == null)
      return null;

    URI relativeURI = URI.create(relativePath).normalize(); // normalize removes leading . 

    if(relativeURI.isAbsolute())
      throw new IllegalArgumentException(relativePath + " is absolute");

    relativePath = relativeURI.getPath();

    if(!"/".equals(relativePath))
      // we remove the leading / only when it is not the only thing
      relativePath = PathUtils.removeLeadingSlash(relativePath);
    
    if("".equals(relativePath))
      return resource;

    if("..".equals(relativePath))
      return getParentResource(resource);

    if(relativePath.startsWith("../"))
      return (InternalResource) getParentResource(resource).createRelative(relativePath.substring(3));

    return doBuildResource(PathUtils.addPaths(resource.getPath(), relativePath));
  }


  /**
   * Returns a new resource with the root resource set to this resource.
   *
   * @return the new resource
   */
  @Override
  public InternalResource chroot(InternalResource resource)
  {
    String path = resource.getPath();

    // when it is a directory, we simply use the provided resource
    if(resource.isDirectory())
    {
      return doCreateResourceProvider(path).getRootResource();
    }
    else
    {
      // when not a directory, we do a chroot to the parent and then we return the resource
      // that points to it
      return (InternalResource) resource.chroot("..").createRelative(resource.getFilename());
    }
  }

  /**
   * If this resource denotes a directory, then it will return all resources that are contained in
   * the directory.
   *
   * @param filter the filter to include only some resources in the result
   * @return all the resources contained in the directory or <code>null</code> if not a directory
   * @throws IOException if there is an error accessing the resource
   */
  @Override
  public InternalResource[] list(InternalResource resource, final ResourceFilter filter)
    throws IOException
  {
    final List<Resource> resources = new ArrayList<Resource>();

    String path = PathUtils.addTrailingSlash(resource.getPath());

    if(doList(path, new ResourceFilter()
    {
      @Override
      public boolean accept(Resource resource)
      {
        boolean res = filter.accept(resource);
        if(res)
          resources.add(resource);
        return res;
      }
    }))
    {
      return resources.toArray(new InternalResource[resources.size()]);
    }
    else
      return null;
  }
}
