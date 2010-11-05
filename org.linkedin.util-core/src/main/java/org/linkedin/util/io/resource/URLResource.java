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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.linkedin.util.clock.Timespan;
import org.linkedin.util.io.PathUtils;
import org.linkedin.util.io.resource.internal.URLResourceProvider;
import org.linkedin.util.url.URLBuilder;
import org.linkedin.util.io.resource.internal.AbstractResource;
import org.linkedin.util.io.resource.internal.InternalResourceProvider;

/**
 * @author ypujante@linkedin.com
 *
 */
public class URLResource extends AbstractResource
{
  private final URL _url;
  private final Timespan _connectTimeout;
  private final Timespan _readTimeout;
  
  public static final Timespan DEFAULT_CONNECT_TIMEOUT = Timespan.ZERO_MILLISECONDS;
  public static final Timespan DEFAULT_READ_TIMEOUT    = Timespan.ZERO_MILLISECONDS;

  /**
   * Constructor
   */
  protected URLResource(InternalResourceProvider resourceProvider, String path, URL url)
  {
    // 0's are the default values for these respective timeouts in URLConnection
    // see http://java.sun.com/j2se/1.5.0/docs/api/java/net/URLConnection.html
    this(resourceProvider, path, url, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);  
  }
  
  /**
   * @see java.net.URLConnection for an explanation of what the timeout values are all about.
   * 
   * @param resourceProvider
   * @param path
   * @param url
   * @param connectTimeout
   * @param readTimeout
   */
  public URLResource(InternalResourceProvider resourceProvider, String path, URL url, Timespan connectTimeout, Timespan readTimeout)
  {
    super(resourceProvider, path);
    _url = url;
    
    _connectTimeout = connectTimeout;
    _readTimeout = readTimeout;
  }

  /**
   * @return <code>true</code> if this resource represents a directory.
   */
  @Override
  public boolean isDirectory()
  {
    return getPath().endsWith("/");
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
   * Efficiently returns all information about the resource.
   *
   * @return information about this resource.
   * @throws IOException if cannot get information
   */
  @Override
  public ResourceInfo getInfo() throws IOException
  {
    return extractInfo(_url);
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
    URLConnection urlConnection = _url.openConnection();

    urlConnection.setDoInput(true);
    urlConnection.setDoOutput(false);
    urlConnection.setUseCaches(false);
    urlConnection.setConnectTimeout(new Long(_connectTimeout.getDurationInMilliseconds()).intValue());
    urlConnection.setReadTimeout(new Long(_readTimeout.getDurationInMilliseconds()).intValue());
    urlConnection.connect();
    
    return urlConnection.getInputStream();
  }

  /**
   * @return <code>true</code> if the resource exists.
   */
  @Override
  public boolean exists()
  {
    try
    {
      InputStream is = getInputStream();
      try
      {
        return true;
      }
      finally
      {
        is.close();
      }
    }
    catch(IOException e)
    {
      return false;
    }
  }
  
  /**
   * @return a uri representation of the resource
   */
  @Override
  public URI toURI()
  {
    try
    {
      return _url.toURI();
    }
    catch(URISyntaxException e)
    {
      throw new RuntimeException(e);
    }
  }

  /**
   * Extracts the info from the url
   */
  public static ResourceInfo extractInfo(URL url) throws IOException
  {
    URLConnection urlConnection = url.openConnection();

    urlConnection.setDoInput(true);
    urlConnection.setDoOutput(false);
    urlConnection.setUseCaches(false);
    urlConnection.connect();
    InputStream is = urlConnection.getInputStream();
    try
    {
      return new StaticInfo(urlConnection.getContentLength(), urlConnection.getLastModified());
    }
    finally
    {
      is.close();
    }
  }

  /**
   * Creates the resource from a url...
   *
   * @param url
   * @return the resource
   */
  public static Resource create(URL url)
  {
    URLBuilder rootURL = URLBuilder.createFromPath("/");
    rootURL.setScheme(url.getProtocol());
    rootURL.setHost(url.getHost());
    rootURL.setPort(url.getPort());
    rootURL.setUserInfo(url.getUserInfo());

    return create(rootURL.toJavaURL(), "/").createRelative(url.getPath());
  }

  /**
   * Creates the resource from a url... The root will be at the path of this url (ex:
   * if url is http://localhost/a/b/c/foo.html then root is /a/b/c).
   *
   * @param url
   * @return the resource
   */
  public static Resource createFromRoot(URL url)
  {
    String urlPath = url.getPath();
    String path = "/";

    if(!urlPath.endsWith("/"))
    {
      urlPath = PathUtils.addLeadingSlash(urlPath);
      int idx = urlPath.lastIndexOf("/"); // there will always be one due to previous line...
      path = urlPath.substring(idx + 1);
      urlPath = urlPath.substring(0, idx);
    }


    URLBuilder rootURL = URLBuilder.createFromPath(urlPath);
    rootURL.setScheme(url.getProtocol());
    rootURL.setHost(url.getHost());
    rootURL.setPort(url.getPort());
    rootURL.setUserInfo(url.getUserInfo());

    return create(rootURL.toJavaURL(), "/").createRelative(path);
  }

  /**
   * Creates the resource from a url...
   *
   * @param url
   * @return the resource
   */
  public static Resource create(URL url, String rootPath)
  {
    try
    {
      return new URLResourceProvider(url, rootPath).getRootResource();
    }
    catch(UnsupportedURIException e)
    {
      throw new RuntimeException(e);
    }
    catch(URISyntaxException e)
    {
      throw new RuntimeException(e);
    }
  }
}
