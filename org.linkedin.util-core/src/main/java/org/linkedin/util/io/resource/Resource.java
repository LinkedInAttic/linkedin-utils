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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Abstraction to a resource. Note that like {@link java.io.File}, a <code>Resource</code> simply
 * represents a handle to a resource and you need to call exists to figure out if it exists or not.
 *
 * @author ypujante@linkedin.com
 *
 */
public interface Resource
{
  /**
   * @return <code>true</code> if the resource exists.
   */
  boolean exists();

  /**
   * @return <code>true</code> if this resource represents a directory.
   */
  boolean isDirectory();

  /**
   * Returns a <code>File</code> handle for this resource.
   *
   * @throws IOException if the resource cannot be resolved as a <code>File</code> handle, i.e.
   * it is not available on the file system (or it cannot be made available).
   */
  File getFile() throws IOException;

  /**
   * Creates a resource relative to the current one. Leading / is optional. It is technically
   * equivalent to: <code>getRootResource().createRelative(getPath() + "/" + relativePath)</code>.
   * The following properties apply:
   *
   * <ul>
   * <li>
   * <code>createRelative("abc")</code> is the same as
   * <code>createRelative("/abc")</code>
   * </li>
   * <li>
   * <code>createRelative("/")</code> will add a trailing / to the path if there isn't already 
   * one</code>
   * </li>
   * <li>
   * <code>createRelative("") == createRelative(".") == createRelative("./") == this</code>
   * </li>
   * <li>
   * <code>createRelative("..")</code> is the same as <code>getParentResource()</code>
   * </li>
   * <li>
   * if this resource == root resource then <code>createRelative("..")</code> is the same as
   * <code>createRelative(".")</code> (you cannot go higher than the root...)
   * </li>
   * <li>
   * <code>createRelative("../abc")</code> is the same as
   * <code>getParentResource().createRelative("abc")</code>
   * </li>
   * </ul>
   *
   * @param relativePath the relative path
   * @return the new resource
   */
  Resource createRelative(String relativePath);

  /**
   * Returns a new resource with the root resource set to the relative path provided. If the
   * relative path ends with '/' then the root will be the relative path otherwise it will be
   * the parent. Ex:
   * <pre>
   * resource.chroot("/d1/d2"); will point to resource with root /d1 and path /d2
   * resource.chroot("/d1/d2/"); will point to resource with root /d1/d2 and path /
   * This works well for real files:
   * resource.chroot("/a/b/c.html") will point to a resource with root /a/b and path is /c.html
   * </pre>
   *
   * @param relativePath same meaning as {@link #createRelative(String)} meaning
   * @return the new resource
   */
  Resource chroot(String relativePath);

  /**
   * @return the parent resource. Same meaning as <code>File.getParentFile()</code>.
   */
  Resource getParentResource();

  /**
   * @return the root resource
   * @see #createRelative(String)
   */
  Resource getRootResource();

  /**
   * If this resource denotes a directory, then it will return all resources that are contained
   * in the directory.
   *
   * @return all the resources contained in the directory or <code>null</code> if not a directory
   * @throws IOException if there is an error accessing the resource
   */
  Resource[] list() throws IOException;

  /**
   * If this resource denotes a directory, then it will return all resources that are contained
   * in the directory.
   *
   * @param filter the filter to include only some resources in the result
   * @return all the resources contained in the directory or <code>null</code> if not a directory
   * @throws IOException if there is an error accessing the resource
   */
  Resource[] list(ResourceFilter filter) throws IOException;

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
   * Important note: the caller of this method is responsible for properly closing the
   * input stream!
   *
   * @return an input stream to the resource.
   * @throws IOException if cannot get an input stream
   */
  InputStream getInputStream() throws IOException;

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
}
