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

import org.linkedin.util.io.resource.Resource;
import org.linkedin.util.io.resource.ResourceChain;
import org.linkedin.util.io.resource.ResourceFilter;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Implements a 'chain' of resource providers... it will delegate to a list of providers.
 * The order is important as the first one which exists will win.
 * 
 * @author ypujante@linkedin.com
 *
 */
public class ResourceProviderChain extends AbstractResourceProvider
{
  private final List<InternalResourceProvider> _resourceProviders;

  /**
   * Constructor
   */
  public ResourceProviderChain(InternalResourceProvider... resourceProviders)
  {
    this(Arrays.asList(resourceProviders));
  }

  /**
   * Constructor
   */
  public ResourceProviderChain(List<InternalResourceProvider> resourceProviders)
  {
    if(resourceProviders.size() == 0)
      throw new IllegalArgumentException("list is emtpy...");
    
    _resourceProviders = resourceProviders;
  }

  /**
   * @return the internal list...
   */
  public List<InternalResourceProvider> getResourceProviders()
  {
    return Collections.unmodifiableList(_resourceProviders);
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
    // since the list is not empty, this variable will never remain null...
    InternalResource resource = null;

    for(InternalResourceProvider resourceProvider : _resourceProviders)
    {
      resource = resourceProvider.doBuildResource(path);
      if(resource.exists())
        break;
    }

    return new ResourceChain(this, path, resource);
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
    List<InternalResourceProvider> resourceProviders =
      new ArrayList<InternalResourceProvider>(_resourceProviders.size());

    for(InternalResourceProvider resourceProvider : _resourceProviders)
    {
      resourceProviders.add(resourceProvider.doCreateResourceProvider(rootPath));
    }

    return new ResourceProviderChain(resourceProviders);
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
  public boolean doList(String path, final ResourceFilter filter)
  {
    boolean isDirectory = false;

    ResourceFilter newFilter = new ResourceFilter()
    {
      @Override
      public boolean accept(Resource resource)
      {
        return filter.accept(new ResourceChain(ResourceProviderChain.this,
                                               resource.getPath(),
                                               resource));
      }
    };

    for(InternalResourceProvider resourceProvider : _resourceProviders)
    {
      isDirectory |= resourceProvider.doList(path, newFilter);
    }

    return isDirectory;
  }

  @Override
  public boolean equals(Object o)
  {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;

    ResourceProviderChain that = (ResourceProviderChain) o;

    if(!_resourceProviders.equals(that._resourceProviders)) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    return _resourceProviders.hashCode();
  }
}
