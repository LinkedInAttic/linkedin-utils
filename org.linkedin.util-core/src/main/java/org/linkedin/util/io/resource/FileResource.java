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

import org.linkedin.util.io.PathUtils;
import org.linkedin.util.io.resource.internal.AbstractResource;
import org.linkedin.util.io.resource.internal.FileResourceProvider;
import org.linkedin.util.io.resource.internal.InternalResourceProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * @author ypujante@linkedin.com
 *
 */
// TODO MED YP:  toURI should preserve trailing slash...
public class FileResource extends AbstractResource
{
  private final File _file;

  /**
   * Basic implementation for file...
   */
  private class FileInfo implements ResourceInfo
  {
    @Override
    public long getContentLength() throws IOException
    {
      return _file.isDirectory() ? 0 : _file.length();
    }

    @Override
    public long getLastModified() throws IOException
    {
      return _file.lastModified();
    }
  }
  
  /**
   * Constructor
   */
  public FileResource(InternalResourceProvider resourceProvider, String path, File file)
  {
    super(resourceProvider, path);
    _file = file;
  }


  /**
   * @return <code>true</code> if the resource exists.
   */
  @Override
  public boolean exists()
  {
    return _file.exists();
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
    return _file;
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
    return new FileInputStream(_file);
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
    if(_file.exists())
      return new FileInfo();
    else
      throw new FileNotFoundException(_file.getPath());
  }


  /**
   * @return <code>true</code> if this resource represents a directory.
   */
  @Override
  public boolean isDirectory()
  {
    return _file.isDirectory();
  }


  /**
   * @return a uri representation of the resource
   */
  @Override
  public URI toURI()
  {
    return _file.toURI();
  }

  /**
   * Creates a file resource from a file
   *
   * @param filename name of the file
   * @return the resource
   */
  public static Resource create(String filename)
  {
    return create(new File(filename));
  }

  /**
   * Creates a file resource from a file
   *
   * @param file the file
   * @return the resource (points to this file)
   */
  public static Resource create(File file)
  {
    try
    {
      String path = file.getCanonicalPath();
      if(file.isDirectory())
        path = PathUtils.addTrailingSlash(path);
      return create(new File("/"), path);
    }
    catch(IOException e)
    {
      throw new IllegalArgumentException("invalid file " + file, e);
    }
  }

  /**
   * Creates a file resource from a file, with the root as this file (if it is a directory
   * otherwise its parent).
   *
   * @param rootFile the root file
   * @return the resource (points to this file)
   */
  public static Resource createFromRoot(File rootFile)
  {
    File root = rootFile;
    String path = "/";

    if(!root.isDirectory())
    {
      root = rootFile.getParentFile();
      path = rootFile.getName();
    }

    return create(root, path);
  }

  /**
   * Creates a file resource with the root provided and the path (relative to the root).
   *
   * @param root the root of the resource
   * @param path the path (relative to root)
   * @return the resource
   */
  public static Resource create(File root, String path)
  {
    FileResourceProvider frp = null;
    try
    {
      frp = new FileResourceProvider(root);
    }
    catch(IOException e)
    {
      throw new RuntimeException(e);
    }
    return frp.createResource(path);
  }
}
