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

import java.net.URI;

/**
 * @author ypujante@linkedin.com
 *
 */
public abstract class SchemeURIResourceFactory
{
  /**
   * Constructor
   */
  public SchemeURIResourceFactory()
  {
  }

  /**
   * @return the scheme that this factory handles
   */
  public abstract String getScheme();


  /**
   * Based on the URI returns the right resource
   *
   * @param uri
   * @return the resource
   */
  public Resource createResource(URI uri, URIResourceFactory parent)
  {
    if(!getScheme().equals(uri.getScheme()))
      throw new IllegalArgumentException("cannot handle: " + uri.getScheme());

    return doCreateResource(uri, parent);
  }

  /**
   * Method that needs to be implemented by children..
   *
   * @param uri
   * @param parent the parent resource factory
   * @return the resource
   */
  protected abstract Resource doCreateResource(URI uri, URIResourceFactory parent);
}
