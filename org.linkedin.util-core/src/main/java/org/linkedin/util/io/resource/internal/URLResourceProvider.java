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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.linkedin.util.clock.Timespan;
import org.linkedin.util.io.PathUtils;
import org.linkedin.util.io.resource.ResourceFilter;
import org.linkedin.util.io.resource.URLResource;
import org.linkedin.util.io.resource.UnsupportedURIException;

/**
 * @author ypujante@linkedin.com
 *
 */
public class URLResourceProvider extends PathBasedResourceProvider
{
  private final URL _baseURL;
  private final Timespan _connectTimeout;
  private final Timespan _readTimeout;

  /**
   * Constructor
   */
  public URLResourceProvider(URL baseURL) throws UnsupportedURIException, URISyntaxException
  {
    this(baseURL, "/");
  }
  
  /**
   * Constructor
   */
  public URLResourceProvider(URL baseURL, Timespan connectTimeout, Timespan readTimeout) throws UnsupportedURIException, URISyntaxException
  {
    this(baseURL, "/", connectTimeout, readTimeout);
  }

  /**
   * Constructor
   */
  public URLResourceProvider(URL baseURL, String root) throws UnsupportedURIException, URISyntaxException
  {
    this(baseURL, root, URLResource.DEFAULT_CONNECT_TIMEOUT, URLResource.DEFAULT_READ_TIMEOUT);
  }
  
  /**
   * Constructor
   */
  public URLResourceProvider(URL baseURL, String root, Timespan connectTimeout, Timespan readTimeout) throws UnsupportedURIException, URISyntaxException
  {
    super(PathUtils.addPaths(baseURL.getPath(), root));

    if(baseURL.toURI().isOpaque())
      throw new UnsupportedURIException(baseURL + " is opaque");

    _baseURL = baseURL;
    _connectTimeout = connectTimeout;
    _readTimeout = readTimeout;
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
    try
    {
      return new URLResourceProvider(_baseURL, getFullPath(rootPath));
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

  /**
   * Builds a resource given a path. Only subclasses know how to do that.
   *
   * @param path the path to the new resource (always starts with /)
   * @return the resource
   */
  @Override
  public InternalResource doBuildResource(String path)
  {
    try
    {
      URI uri = _baseURL.toURI();

      URI newURI = new URI(uri.getScheme(),
                           uri.getUserInfo(),
                           uri.getHost(),
                           uri.getPort(),
                           getFullPath(path),
                           null,
                           null);

      return new URLResource(this, path, newURI.toURL(), _connectTimeout, _readTimeout);
    }
    catch(URISyntaxException e)
    {
      // should not happen
      throw new RuntimeException(e);
    }
    catch(MalformedURLException e)
    {
      // should not happen
      throw new RuntimeException(e);
    }
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
  public boolean doList(String path, ResourceFilter filter)
  {
    return false;
  }

  @Override
  public boolean equals(Object o)
  {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;
    if(!super.equals(o)) return false;

    URLResourceProvider that = (URLResourceProvider) o;

    if(!_baseURL.equals(that._baseURL)) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = super.hashCode();
    result = 31 * result + _baseURL.hashCode();
    return result;
  }
}
