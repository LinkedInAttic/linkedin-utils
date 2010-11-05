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

import org.linkedin.util.io.PathUtils;
import org.linkedin.util.io.resource.AcceptAllResourceFilter;
import org.linkedin.util.io.resource.Resource;
import org.linkedin.util.io.resource.ResourceFilter;

import java.io.IOException;

/**
 * Base implementation of all resources. Implement the basic behavior and will delegate to
 * subclasses for actual implementation (strategy pattern).
 *
 * @author ypujante@linkedin.com
 *
 */
public abstract class AbstractResource implements InternalResource
{
  private final InternalResourceProvider _resourceProvider;
  private final String _path;

  /**
   * Constructor
   */
  protected AbstractResource(InternalResourceProvider resourceProvider, String path)
  {
    if(!path.startsWith("/"))
      throw new IllegalArgumentException(path + " must start with /");
    if(resourceProvider == null)
      throw new NullPointerException("resource provider cannot be null");
    
    _resourceProvider = resourceProvider;
    _path = path;
  }

  /**
   * The path always starts with '/'.
   * 
   * @return the path to the resource (within the context of the resource provider)
   */
  @Override
  public String getPath()
  {
    return _path;
  }

  /**
   * @return the resource provider that created this resource
   */
  @Override
  public ResourceProvider getResourceProvider()
  {
    return _resourceProvider;
  }

  /**
   * If this resource denotes a directory, then it will return all resources that are contained in
   * the directory.
   *
   * @return all the resources contained in the directory or <code>null</code> if not a directory
   * @throws IOException if there is an error accessing the resource
   */
  @Override
  public final Resource[] list() throws IOException
  {
    return list(AcceptAllResourceFilter.INSTANCE);
  }

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
  @Override
  public Resource createRelative(String relativePath)
  {
    return _resourceProvider.createRelative(this, relativePath);
  }


  /**
   * Returns a new resource with the root resource set to the relative path provided. Note that the
   * new resource points at the new root as well.
   *
   * @param relativePath
   * @return the new resource
   */
  @Override
  public Resource chroot(String relativePath)
  {
    return _resourceProvider.chroot((InternalResource) createRelative(relativePath));
  }

  /**
   * @return the parent resource. Same meaning as <code>File.getParentFile()</code>.
   */
  @Override
  public Resource getParentResource()
  {
    return _resourceProvider.getParentResource(this);
  }

  /**
   * @return the root resource
   * @see #createRelative(String)
   */
  @Override
  public Resource getRootResource()
  {
    return _resourceProvider.getRootResource();
  }

  /**
   * If this resource denotes a directory, then it will return all resources that are contained in
   * the directory.
   *
   * @param filter the filter to include only some resources in the result
   * @return all the resources contained in the directory or <code>null</code> if not a directory
   * @throws IOException if there is an error accessing the resource
   */
  @Override
  public Resource[] list(ResourceFilter filter) throws IOException
  {
    return _resourceProvider.list(this, filter);
  }

  /**
   * Shortcut to <code>getInfo().getLastModified()</code> with the same meaning as the
   * <code>File.lastModified</code> api: returns <code>0L</code> if the resource does not exist or
   * if an IO error occurs.
   *
   * @return the last modified date of this resource.
   */
  @Override
  public long lastModified()
  {
    if(!exists())
      return 0L;

    try
    {
      return getInfo().getLastModified();
    }
    catch(IOException e)
    {
      return 0L;
    }
  }


  /**
   * Returns <code>true</code> if this resource was modified since the time provided. A trivial
   * implementation is <code>return lastModified() &gt; time</code>, but various implementations can
   * provide better alternatives. If the resource does not exsit then it returns
   * <code>false</code>.
   *
   * @param time the time to check against
   * @return a boolean
   */
  @Override
  public boolean isModifiedSince(long time)
  {
    return lastModified() > time;
  }

  /**
   * Shortcut to <code>getInfo().getContentLength()</code> with the same meaning as the
   * <code>File.length()</code> api: returns <code>0L</code> if the resource does not exist or if an
   * IO error occurs.
   *
   * @return the length in bytes of the resource.
   */
  @Override
  public long length()
  {
    if(!exists())
      return 0L;

    try
    {
      return getInfo().getContentLength();
    }
    catch(IOException e)
    {
      return 0L;
    }
  }

  /**
   * @return the filename portion of this resource. If it is a directory, returns the name of the
   *         directory.
   */
  @Override
  public String getFilename()
  {
    String path = PathUtils.removeTrailingSlash(_path);

    int idx = path.lastIndexOf("/");
    if(idx >= 0)
      path = path.substring(idx + 1);
    return path;
  }


  /**
   * Returns a string representation of the object. In general, the <code>toString</code> method
   * returns a string that "textually represents" this object. The result should be a concise but
   * informative representation that is easy for a person to read. It is recommended that all
   * subclasses override this method.
   * <p/>
   * The <code>toString</code> method for class <code>Object</code> returns a string consisting of
   * the name of the class of which the object is an instance, the at-sign character
   * `<code>@</code>', and the unsigned hexadecimal representation of the hash code of the object.
   * In other words, this method returns a string equal to the value of: <blockquote>
   * <pre>
   * getClass().getName() + '@' + Integer.toHexString(hashCode())
   * </pre></blockquote>
   *
   * @return a string representation of the object.
   */
  @Override
  public String toString()
  {
    return toURI().toString();
  }

  @Override
  public boolean equals(Object o)
  {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;

    AbstractResource that = (AbstractResource) o;

    if(!_path.equals(that._path)) return false;
    if(!_resourceProvider.equals(that._resourceProvider)) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    int result;
    result = _resourceProvider.hashCode();
    result = 31 * result + _path.hashCode();
    return result;
  }
}
