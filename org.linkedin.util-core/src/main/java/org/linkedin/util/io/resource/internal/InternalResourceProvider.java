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

import org.linkedin.util.io.resource.ResourceFilter;

import java.io.IOException;

/**
 * All resource providers need to implement this internal interface.
 *
 * @author ypujante@linkedin.com
 *
 */
public interface InternalResourceProvider extends ResourceProvider
{
  /**
   * @param resource
   * @return the parent of a resource
   */
  InternalResource getParentResource(InternalResource resource);

  /**
   * @param resource
   * @param relativePath the relative path
   * @return a resource relative to the resource
   */
  InternalResource createRelative(InternalResource resource, String relativePath);

  /**
   * Returns a new resource with the root resource set to this resource.
   * 
   * @return the new resource
   */
  InternalResource chroot(InternalResource resource);

  /**
   * If this resource denotes a directory, then it will return all resources that are contained
   * in the directory.
   *
   * @param filter the filter to include only some resources in the result
   * @return all the resources contained in the directory or <code>null</code> if not a directory
   * @throws IOException if there is an error accessing the resource
   */
  InternalResource[] list(InternalResource resource, ResourceFilter filter) throws IOException;

  /**
   * @return the root resource
   */
  InternalResource getRootResource();

  /**
   * Builds a resource given a path. Only subclasses know how to do that.
   *
   * @param path the path to the new resource (always starts with /)
   * @return the resource
   */
  InternalResource doBuildResource(String path);

  /**
   * If the path denotes a directory, then it will return all resources that are contained in
   * the directory.
   *
   * @param path the path to the resource (it already ends with /)
   * @param filter the filter to include only some resources in the result
   * @return <code>true</code> if it was a directory, <code>false</code> otherwise
   */
  boolean doList(String path, ResourceFilter filter);

  /**
   * Creates a new resource provider given the new path.
   *
   * @param rootPath
   * @return the new resource provider
   */
  InternalResourceProvider doCreateResourceProvider(String rootPath);
}
