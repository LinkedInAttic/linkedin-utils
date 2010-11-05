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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * @author ypujante@linkedin.com
 *
 */
public class FilteredResource extends AbstractResource
{
  private final Resource _resource;

  /**
   * Constructor
   */

  public FilteredResource(InternalResourceProvider resourceProvider,
                             String path,
                             Resource resource)
  {
    super(resourceProvider, path);
    _resource = resource;
  }

  /**
   * @return the filtered resource
   */
  public Resource getFilteredResource()
  {
    return _resource;
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
}
