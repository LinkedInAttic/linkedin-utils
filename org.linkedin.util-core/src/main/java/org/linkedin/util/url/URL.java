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

import java.util.List;

/**
 * Interface which defines a url. Only accessors.
 *
 * @author ypujante@linkedin.com
 */
public interface URL
{
  /**
   * @return the path */
  String getPath();

  /**
   * If the path is /a/b/c then returns a list containing 3 elements "a", "b" and "c"
   * @return the path splitted at each '/' and decoded
   */
  List<String> getPathComponents();

  /**
   * @return the fragment */
  String getFragment();

  /**
   * @return the query string */
  String getQueryString();

  /**
   * @return the query */
  Query getQuery();

  /**
   * @return <code>true</code> if a path has been set */
  boolean getHasPath();

  /**
   * @return <code>true</code> if the fragment has been set */
  boolean getHasFragment();

  /**
   * @return <code>true</code> if query parameters have been added */
  boolean getHasQueryParameters();

  /**
   * @return the url */
  String getURL();

  String getScheme();

  boolean getHasScheme();

  String getUserInfo();

  boolean getHasUserInfo();

  String getHost();

  boolean getHasHost();

  int getPort();

  boolean getHasPort();

  /**
   * @return a url which is relative (does not contain anything before path)
   */
  public URL createRelativeURL();

  /**
   * @return this object as a {@link java.net.URL}
   */
  public java.net.URL toJavaURL();
}