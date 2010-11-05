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

import org.linkedin.util.io.resource.internal.SchemeURIResourceFactory;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic implementation based on {@link SchemeURIResourceFactory}.
 *
 * @author ypujante@linkedin.com
 *
 */
public class URIResourceFactoryImpl implements URIResourceFactory
{
  private final Map<String, SchemeURIResourceFactory> _factories =
    new HashMap<String, SchemeURIResourceFactory>();

  /**
   * Constructor
   */
  public URIResourceFactoryImpl(SchemeURIResourceFactory... factories)
  {
    for(SchemeURIResourceFactory factory : factories)
    {
      _factories.put(factory.getScheme(), factory);
    }
  }

  /**
   * Based on the URI returns the right resource
   *
   * @param uri
   * @return the resource
   * @throws UnsupportedURIException if the uri is not supported by this factory
   */
  @Override
  public Resource createResource(URI uri) throws UnsupportedURIException
  {
    if(uri.getScheme() == null)
      return FileResource.createFromRoot(new File(uri.getPath()));

    SchemeURIResourceFactory factory = _factories.get(uri.getScheme());
    if(factory == null)
      throw new UnsupportedURIException("unsupported scheme " + uri.getScheme());

    return factory.createResource(uri, this);
  }
}
