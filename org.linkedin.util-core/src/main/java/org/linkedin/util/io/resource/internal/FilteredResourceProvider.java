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

import org.linkedin.util.io.resource.FilteredResource;
import org.linkedin.util.io.resource.Resource;
import org.linkedin.util.io.resource.ResourceFilter;

/**
 * @author ypujante@linkedin.com
 *
 */
public class FilteredResourceProvider extends AbstractResourceProvider
{
  private final InternalResourceProvider _resourceProvider;

  /**
   * Constructor
   */
  public FilteredResourceProvider(InternalResourceProvider resourceProvider)
  {
    _resourceProvider = resourceProvider;
  }

  /**
   * Constructor
   */
  public FilteredResourceProvider(Resource resource)
  {
    this((InternalResourceProvider) ((InternalResource) resource).getResourceProvider());
  }

  /**
   * @return the filtered resource provider
   */
  public InternalResourceProvider getFilteredResourceProvider()
  {
    return _resourceProvider;
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
    return doBuildResource(_resourceProvider.doBuildResource(path));
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
    return doCreateResourceProvider(_resourceProvider.doCreateResourceProvider(rootPath));
  }

  /**
   * This method will be implemented by subclasses to create the right kind of resource provider
   *
   * @param resourceProvider
   * @return the resource provider
   */
  protected InternalResourceProvider doCreateResourceProvider(InternalResourceProvider resourceProvider)
  {
    return new FilteredResourceProvider(resourceProvider);
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
  public boolean doList(String path, final ResourceFilter filter)
  {
    ResourceFilter newFilter = new ResourceFilter()
    {
      @Override
      public boolean accept(Resource resource)
      {
        return filter.accept(doBuildResource(resource));
      }
    };

    return _resourceProvider.doList(path, newFilter);
  }

  /**
   * This method will be implemented by subclasses to create the right kind of resource.
   *
   * @param filteredResource
   * @return the resource
   */
  protected InternalResource doBuildResource(Resource filteredResource)
  {
    return new FilteredResource(this, filteredResource.getPath(), filteredResource);
  }

  @Override
  public boolean equals(Object o)
  {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;

    FilteredResourceProvider that = (FilteredResourceProvider) o;

    if(!_resourceProvider.equals(that._resourceProvider)) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    return _resourceProvider.hashCode();
  }
}
