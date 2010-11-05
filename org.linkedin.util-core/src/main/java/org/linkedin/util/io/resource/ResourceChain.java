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

package org.linkedin.util.io.resource;

import org.linkedin.util.io.resource.internal.AbstractResource;
import org.linkedin.util.io.resource.internal.InternalResourceProvider;
import org.linkedin.util.io.resource.internal.InternalResource;
import org.linkedin.util.io.resource.internal.ResourceProviderChain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * @author ypujante@linkedin.com
 *
 */
public class ResourceChain extends AbstractResource
{
  private final Resource _resource;

  /**
   * Constructor
   */
  public ResourceChain(InternalResourceProvider resourceProvider, String path, Resource resource)
  {
    super(resourceProvider, path);
    _resource = resource;
  }


  /**
   * @return <code>true</code> if the resource exists.
   */
  @Override
  public boolean exists()
  {
    return _resource.exists();
  }

  /**
   * Returns a <code>File</code> handle for this resource.
   *
   * @throws IOException if the resource cannot be resolved as a <code>File</code> handle, i.e. it
   *                     is not available on the file system (or it cannot be made available).
   */
  @Override
  public File getFile() throws IOException
  {
    return _resource.getFile();
  }

  /**
   * Important note: the caller of this method is responsible for properly closing the input
   * stream!
   *
   * @return an input stream to the resource.
   * @throws IOException if cannot get an input stream
   */
  @Override
  public InputStream getInputStream() throws IOException
  {
    return _resource.getInputStream();
  }

  /**
   * Efficiently returns all information about the resource.
   *
   * @return information about this resource.
   * @throws IOException if cannot get information
   */
  @Override
  public ResourceInfo getInfo() throws IOException
  {
    return _resource.getInfo();
  }


  /**
   * @return <code>true</code> if this resource represents a directory.
   */
  @Override
  public boolean isDirectory()
  {
    return _resource.isDirectory();
  }


  /**
   * @return a uri representation of the resource
   */
  @Override
  public URI toURI()
  {
    return _resource.toURI();
  }

  /**
   * Returns <code>true</code> if this resource was modified since the time provided. A trivial
   * implementation is <code>return lastModified() &gt; time</code>, but various implementations can
   * provide better alternatives. If the resource does not exsit then it returns
   * <code>false</code>.
   *
   * @param time the time to check against
   * @return a boolean
   */
  @Override
  public boolean isModifiedSince(long time)
  {
    return _resource.isModifiedSince(time);
  }

  /**
   * Convenient method to create a chain from a list of resources.
   *
   * @param resources the list of resources.
   * @return the resource
   */
  public static Resource create(Resource... resources)
  {
    return create(Arrays.asList(resources));
  }

  /**
   * Convenient method to create a chain from a list of resources. All the resources are turned into
   * roots before chaining (otherwise it does not make a lot of sense...)
   *
   * @param resources the list of resources.
   * @return the resource
   */
  public static Resource create(List<Resource> resources)
  {
    if(resources.size() == 1)
      return resources.get(0).chroot(".");

    List<InternalResourceProvider> providers =
      new ArrayList<InternalResourceProvider>(resources.size());

    for(Resource resource : resources)
    {
      resource = resource.chroot(".");
      
      InternalResourceProvider provider =
        (InternalResourceProvider) ((InternalResource) resource).getResourceProvider();

      if(provider instanceof ResourceProviderChain)
      {
        ResourceProviderChain resourceProviderChain = (ResourceProviderChain) provider;
        providers.addAll(resourceProviderChain.getResourceProviders());
      }
      else
      {
        providers.add(provider);
      }
    }

    return new ResourceProviderChain(providers).getRootResource();
  }
}
