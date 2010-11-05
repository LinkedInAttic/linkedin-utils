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

import org.linkedin.util.io.ram.RAMDirectory;
import org.linkedin.util.io.ram.RAMEntry;
import org.linkedin.util.io.ram.RAMFile;
import org.linkedin.util.io.resource.internal.AbstractResource;
import org.linkedin.util.io.resource.internal.RAMResourceProvider;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author ypujante@linkedin.com
 *
 */
public class RAMResource extends AbstractResource
{
  private final String _fullPath;
  private final RAMResourceProvider _resourceProvider;

  /**
   * Constructor
   */
  public RAMResource(RAMResourceProvider resourceProvider,
                        String fullPath,
                        String path)
  {
    super(resourceProvider, path);
    _resourceProvider = resourceProvider;
    _fullPath = fullPath;
  }

  /**
   * @return the ram entry
   */
  public RAMEntry getRAMEntry()
  {
    return _resourceProvider.getRAMEntry(_fullPath);
  }

  /**
   * @return <code>true</code> if the resource exists.
   */
  @Override
  public boolean exists()
  {
    return getRAMEntry() != null;
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
    throw new IOException("not a file");
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
    RAMEntry entry = getRAMEntry();

    if(entry == null || entry instanceof RAMDirectory)
      throw new ResourceNotFoundException(toURI());

    return new ByteArrayInputStream(((RAMFile) entry).getContent());
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
    RAMEntry entry = getRAMEntry();

    if(entry != null)
      return new StaticInfo(entry.getContentLength(),
                            entry.lastModified());
    else
      throw new ResourceNotFoundException(toURI());
  }


  /**
   * @return <code>true</code> if this resource represents a directory.
   */
  @Override
  public boolean isDirectory()
  {
    return getRAMEntry() instanceof RAMDirectory;
  }


  /**
   * @return a uri representation of the resource
   */
  @Override
  public URI toURI()
  {
    try
    {
      return new URI("ram://" + _fullPath);
    }
    catch(URISyntaxException e)
    {
      // should not happen
      throw new RuntimeException(e);
    }
  }

  /**
   * Factory of {@link RAMResource} (based on a {@link RAMDirectory}
   *
   * @param root
   * @return
   */
  public static Resource create(RAMDirectory root)
  {
    return new RAMResourceProvider(root).getRootResource();
  }
}
