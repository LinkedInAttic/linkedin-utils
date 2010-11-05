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

/**
 * This is the base notion of a provider of resources. There will be different implementation
 * depending on the type of resource that needs to be returned.
 *
 * @author ypujante@linkedin.com
 *
 */
public interface ResourceProvider
{
  /**
   * The path to the resource is absolute. Whether it starts with / or not it will be made
   * absolute.
   *
   * Note that it follows the same convention as {@link java.io.File} class in the sense
   * that a resource simply represent a handle and does not imply that the resource exists.
   *
   * @param path the path to the resource
   * @return the resource given the path
   */
  Resource createResource(String path);
}
