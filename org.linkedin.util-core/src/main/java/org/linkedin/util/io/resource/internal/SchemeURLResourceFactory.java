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

import org.linkedin.util.io.resource.Resource;
import org.linkedin.util.io.resource.URIResourceFactory;
import org.linkedin.util.io.resource.URLResource;

import java.net.MalformedURLException;
import java.net.URI;

/**
 * Handles generic URL.. the scheme is provided. (ex: http, ftp...)
 *
 * @author ypujante@linkedin.com
 *
 */
public class SchemeURLResourceFactory extends SchemeURIResourceFactory
{
  private final String _scheme;

  /**
   * Constructor
   */
  public SchemeURLResourceFactory(String scheme)
  {
    _scheme = scheme;
  }

  /**
   * @return the scheme that this factory handles
   */
  @Override
  public String getScheme()
  {
    return _scheme;
  }


  /**
   * Method that needs to be implemented by children..
   *
   * @param uri
   * @param parent the parent resource factory
   * @return the resource
   */
  @Override
  protected Resource doCreateResource(URI uri, URIResourceFactory parent)
  {
    try
    {
      return URLResource.createFromRoot(uri.toURL());
    }
    catch(MalformedURLException e)
    {
      throw new IllegalArgumentException(uri.toString(), e);
    }
  }
}
