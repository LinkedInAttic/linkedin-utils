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
import org.linkedin.util.io.resource.ResourceInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.net.URI;

/**
 * Subset of the interface {@link Resource} when we have a leaf (meaning a file). Note that this
 * api does not have any 'navigation' features (like <code>createRelative</code> or
 * <code>getParentResource</code>).
 * 
 * @author ypujante@linkedin.com
 *
 */
public interface LeafResource
{
  /**
   * @return <code>true</code> if the resource exists.
   */
  boolean exists();

  /**
   * Returns a <code>File</code> handle for this resource.
   *
   * @throws IOException if the resource cannot be resolved as a <code>File</code> handle, i.e.
   * it is not available on the file system (or it cannot be made available).
   */
  File getFile() throws IOException;

  /**
   * Efficiently returns all information about the resource.
   *
   * @return information about this resource.
   * @throws IOException if cannot get information
   */
  ResourceInfo getInfo() throws IOException;

  /**
   * Shortcut to <code>getInfo().getLastModified()</code> with the same meaning as the
   * <code>File.lastModified</code> api: returns <code>0L</code> if the resource does not
   * exist or if an IO error occurs.
   *
   * @return the last modified date of this resource.
   */
  long lastModified();

  /**
   * Returns <code>true</code> if this resource was modified since the time provided. A trivial
   * implementation is <code>return lastModified() &gt; time</code>, but various implementations
   * can provide better alternatives. If the resource does not exsit then it returns
   * <code>false</code>.
   *
   * @param time the time to check against
   * @return a boolean
   */
  boolean isModifiedSince(long time);

  /**
   * Shortcut to <code>getInfo().getContentLength()</code> with the same meaning as the
   * <code>File.length()</code> api: returns <code>0L</code> if the resource does not
   * exist or if an IO error occurs.
   *
   * @return the length in bytes of the resource.
   */
  long length();

  /**
   * @return a uri representation of the resource
   */
  URI toURI();

  /**
   * @return the filename portion of this resource. If it is a directory, returns the name of the
   * directory.
   */
  String getFilename();

  /**
   * @return the path to the resource (within the context of the resource provider)
   */
  String getPath();

  /**
   * Important note: the caller of this method is responsible for properly closing the
   * input stream!
   *
   * @return an input stream to the resource.
   * @throws IOException if cannot get an input stream
   */
  InputStream getInputStream() throws IOException;
}