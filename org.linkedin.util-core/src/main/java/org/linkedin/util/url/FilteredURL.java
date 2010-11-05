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

package org.linkedin.util.url;

import java.io.Serializable;
import java.util.List;

/**
 * Simple filter on URL
 *
 * @author ypujante@linkedin.com
 *
 */
public class FilteredURL implements URL, Serializable
{
  private final static long serialVersionUID = 1L;
  private final URL _urlToFilter;
  
  /**
   * Constructor
   */
  public FilteredURL(URL urlToFilter)
  {
    _urlToFilter = urlToFilter;
  }

  public URL getFilteredURL()
  {
    return _urlToFilter;
  }

  @Override
  public URL createRelativeURL()
  {
    return _urlToFilter.createRelativeURL();
  }

  @Override
  public String getFragment()
  {
    return _urlToFilter.getFragment();
  }

  @Override
  public boolean getHasFragment()
  {
    return _urlToFilter.getHasFragment();
  }

  @Override
  public boolean getHasHost()
  {
    return _urlToFilter.getHasHost();
  }

  @Override
  public boolean getHasPath()
  {
    return _urlToFilter.getHasPath();
  }

  @Override
  public boolean getHasPort()
  {
    return _urlToFilter.getHasPort();
  }

  @Override
  public boolean getHasQueryParameters()
  {
    return _urlToFilter.getHasQueryParameters();
  }

  @Override
  public boolean getHasScheme()
  {
    return _urlToFilter.getHasScheme();
  }

  @Override
  public boolean getHasUserInfo()
  {
    return _urlToFilter.getHasUserInfo();
  }

  @Override
  public String getHost()
  {
    return _urlToFilter.getHost();
  }

  @Override
  public String getPath()
  {
    return _urlToFilter.getPath();
  }

  @Override
  public List<String> getPathComponents()
  {
    return _urlToFilter.getPathComponents();
  }

  @Override
  public int getPort()
  {
    return _urlToFilter.getPort();
  }

  @Override
  public Query getQuery()
  {
    return _urlToFilter.getQuery();
  }

  @Override
  public String getQueryString()
  {
    return _urlToFilter.getQueryString();
  }

  @Override
  public String getScheme()
  {
    return _urlToFilter.getScheme();
  }

  @Override
  public String getURL()
  {
    return _urlToFilter.getURL();
  }

  @Override
  public String getUserInfo()
  {
    return _urlToFilter.getUserInfo();
  }

  /**
   * @return this object as a {@link URL}
   */
  @Override
  public java.net.URL toJavaURL()
  {
    return _urlToFilter.toJavaURL();
  }
}
