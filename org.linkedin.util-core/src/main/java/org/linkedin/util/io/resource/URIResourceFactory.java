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

import org.linkedin.util.io.resource.internal.FileURIResourceFactory;
import org.linkedin.util.io.resource.internal.JarURIResourceFactory;
import org.linkedin.util.io.resource.internal.SchemeURLResourceFactory;

import java.net.URI;

/**
 * @author ypujante@linkedin.com
 *
 */
public interface URIResourceFactory
{
  /**
   * The default factory to use when not provided (which should be most of the cases)
   */
  URIResourceFactory DEFAULT =
    new URIResourceFactoryImpl(new FileURIResourceFactory(),
                               new JarURIResourceFactory(),
                               new SchemeURLResourceFactory("http"),
                               new SchemeURLResourceFactory("https"),
                               new SchemeURLResourceFactory("ftp"));

  /**
   * Based on the URI returns the right resource. Here the rootPath is based on the location
   * of the resource itself.
   *
   * @param uri
   * @return the resource
   */
  Resource createResource(URI uri) throws UnsupportedURIException;
}
