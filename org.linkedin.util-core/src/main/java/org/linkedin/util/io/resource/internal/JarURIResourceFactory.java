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

import org.linkedin.util.io.resource.JarResource;
import org.linkedin.util.io.resource.Resource;
import org.linkedin.util.io.resource.URIResourceFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author ypujante@linkedin.com
 *
 */
public class JarURIResourceFactory extends SchemeURIResourceFactory
{
  /**
   * Constructor
   */
  public JarURIResourceFactory()
  {
  }

  /**
   * @return the protocol that this factory handles
   */
  @Override
  public String getScheme()
  {
    return "jar";
  }

  /**
   * Method that needs to be implemented by children..
   *
   * @param uri
   * @param parent the parent resource factory
   * @return the resource
   * @throws IOException if there is a problem creating the resource
   */
  @Override
  protected Resource doCreateResource(URI uri, URIResourceFactory parent)
  {
    try
    {
      return JarResource.create(uri, parent);
    }
    catch(URISyntaxException e)
    {
      throw new IllegalArgumentException(uri.toString(), e);
    }
  }
}
