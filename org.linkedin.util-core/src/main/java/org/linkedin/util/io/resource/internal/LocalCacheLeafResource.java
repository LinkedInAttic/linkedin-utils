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

import org.linkedin.util.io.GCFile;
import org.linkedin.util.io.IOUtils;
import org.linkedin.util.io.resource.Resource;
import org.linkedin.util.io.resource.ResourceInfo;
import org.linkedin.util.io.resource.StaticInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.net.URI;

/**
 * This implementation caches the resource locally (file): the input stream gets copied into
 * a local file and the info about the remote resource is cached as well.
 * 
 * @author ypujante@linkedin.com
 *
 */
public class LocalCacheLeafResource implements LeafResource
{
  private final Resource _resource;

  private File _localFile = null;
  private StaticInfo _resourceInfo = null;
  private IOException _ioException = null;

  /**
   * Constructor
   */
  public LocalCacheLeafResource(Resource resource)
  {
    _resource = resource;
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
    return new FileInputStream(getFile());
  }

  /**
   * @return <code>true</code> if the resource exists.
   */
  @Override
  public boolean exists()
  {
    init();
    return _resourceInfo.getLastModified() > 0;
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
    init();

    if(_ioException != null)
      throw _ioException;

    return _localFile;
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
    init();

    if(_ioException != null)
      throw _ioException;

    return _resourceInfo;
  }

  /**
   * Shortcut to <code>getInfo().getLastModified()</code> with the same meaning as the
   * <code>File.lastModified</code> api: returns <code>0L</code> if the resource does not exist or
   * if an IO error occurs.
   *
   * @return the last modified date of this resource.
   */
  @Override
  public long lastModified()
  {
    init();
    return _resourceInfo.getLastModified();
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
    return lastModified() > time;
  }

  /**
   * Shortcut to <code>getInfo().getContentLength()</code> with the same meaning as the
   * <code>File.length()</code> api: returns <code>0L</code> if the resource does not exist or if an
   * IO error occurs.
   *
   * @return the length in bytes of the resource.
   */
  @Override
  public long length()
  {
    init();
    return _resourceInfo.getContentLength();
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
   * @return the filename portion of this resource. If it is a directory, returns the name of the
   *         directory.
   */
  @Override
  public String getFilename()
  {
    return _resource.getFilename();
  }

  /**
   * @return the path to the resource (within the context of the resource provider)
   */
  @Override
  public String getPath()
  {
    return _resource.getPath();
  }

  /**
   * Initializes the fields (safe to call multiple times). Thread safe.
   */
  private synchronized void init()
  {
    // already initialized
    if(_resourceInfo != null)
      return;

    // make a local copy of the file
    try
    {
      InputStream is = _resource.getInputStream();
      try
      {
        File file = GCFile.createTempFile(LocalCacheLeafResource.class.getName(),
                                          _resource.getFilename());
        FileOutputStream fos = new FileOutputStream(file);
        try
        {
          BufferedOutputStream out = new BufferedOutputStream(fos);
          IOUtils.copy(new BufferedInputStream(is), out);
          out.flush();
        }
        finally
        {
          fos.close();
        }

        ResourceInfo info = _resource.getInfo();

        _localFile = file;
        _resourceInfo = new StaticInfo(info.getContentLength(), info.getLastModified());
        _ioException = null;
      }
      finally
      {
        is.close();
      }
    }
    catch(IOException e)
    {
      _localFile = null;
      _resourceInfo = new StaticInfo(0, 0);
      _ioException = e;
    }

  }

  /**
   * Decorates the resource to implement a caching strategy.
   *
   * @param resource the resource to cache locally
   * @return the (locally cached) resource
   */
  public static LeafResource create(Resource resource)
  {
    try
    {
      // if we can access the file then there is no reason to decorate it...
      resource.getFile();
      return new LeafResourceImpl(resource);
    }
    catch(IOException e)
    {
      return new LocalCacheLeafResource(resource);
    }
  }

}
