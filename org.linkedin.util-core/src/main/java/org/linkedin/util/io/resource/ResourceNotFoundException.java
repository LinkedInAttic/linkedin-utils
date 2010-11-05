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

import java.io.FileNotFoundException;
import java.net.URI;

/**
 * Thrown when a resource is not found.
 * 
 * @author ypujante@linkedin.com
 *
 */
public class ResourceNotFoundException extends FileNotFoundException
{
  private static final long serialVersionUID = 1L;

  private final URI _uri;

  public ResourceNotFoundException(URI uri)
  {
    super(uri.toString());
    _uri = uri;
  }

  public ResourceNotFoundException(URI uri, Throwable cause)
  {
    super(uri.toString());
    _uri = uri;
    initCause(cause);
  }

  public ResourceNotFoundException(URI uri, String message)
  {
    super(uri.toString() + ": " + message);
    _uri = uri;
  }

  public ResourceNotFoundException(URI uri, String message, Throwable cause)
  {
    super(uri.toString() + ": " + message);
    _uri = uri;
    initCause(cause);
  }

  public URI getURI()
  {
    return _uri;
  }
}
