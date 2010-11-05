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
import org.linkedin.util.io.resource.internal.NullResourceProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Implements the null pattern for resource (does not exist)
 * @author ypujante@linkedin.com
 */
public class NullResource extends AbstractResource
{
  public static final NullResource INSTANCE = create();

  public static NullResource instance()
  {
    return INSTANCE;
  }

  public static class NullResourceException extends IOException
  {
    private static final long serialVersionUID = 1L;

    public NullResourceException(String s)
    {
      super(s);
    }
  }

  private final URI _uri;

  /**
   * Constructor
   */
  public NullResource(InternalResourceProvider resourceProvider, String path, String fullPath)
  {
    super(resourceProvider, path);
    try
    {
      _uri = new URI("nullResource:" + fullPath);
    }
    catch(URISyntaxException e)
    {
      // should not happen
      throw new RuntimeException(e);
    }
  }


  /**
   * @return <code>true</code> if the resource exists.
   */
  @Override
  public boolean exists()
  {
    return false;
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
    throw new IOException("not supported");
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
    throw new NullResourceException(toURI().toString());
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
    throw new NullResourceException(toURI().toString());
  }


  /**
   * @return <code>true</code> if this resource represents a directory.
   */
  @Override
  public boolean isDirectory()
  {
    return false;
  }

  /**
   * @return a uri representation of the resource
   */
  @Override
  public URI toURI()
  {
    return _uri;
  }

  /**
   * Creates a null resource
   */
  public static NullResource createFromRoot(String root)
  {
    return (NullResource) new NullResourceProvider(root).getRootResource();
  }

  /**
   * Creates a null resource
   */
  public static NullResource create()
  {
    return createFromRoot("/");
  }
}