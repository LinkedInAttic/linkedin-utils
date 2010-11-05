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


package org.linkedin.groovy.util.io.fs;

import org.linkedin.util.io.resource.Resource
import org.linkedin.util.io.resource.ResourceInfo
import org.linkedin.util.io.resource.ResourceFilter
import org.linkedin.util.io.resource.FileResource

/**
 * FileResource is not serializable
 *
 * @author ypujante@linkedin.com
 */
class SerializableFileResource implements Resource, Serializable
{
  private static final long serialVersionUID = 1L;

  transient FileResource _fileResource

  SerializableFileResource(FileResource fileResource)
  {
    _fileResource = fileResource;
  }

  FileResource getFileResource()
  {
    return _fileResource
  }

  static SerializableFileResource toFR(resource)
  {
    if(resource instanceof SerializableFileResource)
      return resource

    if(resource instanceof FileResource)
      return new SerializableFileResource(resource)

    throw new IllegalArgumentException("Unsupported resource type ${resource.class}")
  }

  static Resource[] toFRArray(Resource[] resources)
  {
    def array = new Resource[resources.size()]
    resources.eachWithIndex { e, i ->
      array[i] = toFR(e)
    }

    return array
  }

  public boolean exists()
  {
    return _fileResource.exists();
  }

  public File getFile()
    throws IOException
  {
    return _fileResource.getFile();
  }

  public ResourceInfo getInfo()
    throws IOException
  {
    return _fileResource.getInfo();
  }

  public InputStream getInputStream()
    throws IOException
  {
    return _fileResource.getInputStream();
  }

  def withInputStream(Closure closure)
  {
    InputStream is = getInputStream()
    try
    {
      closure(is)
    }
    finally
    {
      is.close()
    }
  }

  public boolean isDirectory()
  {
    return _fileResource.isDirectory();
  }

  public URI toURI()
  {
    return _fileResource.toURI();
  }

  public Resource chroot(String s)
  {
    return toFR(_fileResource.chroot(s))
  }

  public Resource createRelative(String s)
  {
    return toFR(_fileResource.createRelative(s))
  }

  public String getFilename()
  {
    return _fileResource.getFilename();
  }

  public Resource getParentResource()
  {
    return toFR(_fileResource.getParentResource())
  }

  public String getPath()
  {
    return _fileResource.getPath();
  }

  public Resource getRootResource()
  {
    return toFR(_fileResource.getRootResource())
  }

  public boolean isModifiedSince(long l)
  {
    return _fileResource.isModifiedSince(l);
  }

  public long lastModified()
  {
    return _fileResource.lastModified();
  }

  public long length()
  {
    return _fileResource.length();
  }

  public long size()
  {
    return length()
  }

  public Resource[] list()
    throws IOException
  {
    return toFRArray(_fileResource.list())
  }

  /**
   * shortcut to {@link #list}.. returns a <code>List</code>
   */
  def ls()
  {
    return list().toList()
  }

  public Resource[] list(ResourceFilter resourceFilter)
    throws IOException
  {
    return toFRArray(_fileResource.list({ resourceFilter.accept(toFR(it)) } as ResourceFilter))
  }

  /**
   * shortcut to {@link #list}.. returns a <code>List</code>
   */
  def ls(ResourceFilter resourceFilter)
  {
    return list(resourceFilter).toList()
  }

  /**
   * Define property missing to allow for r.foo.bar notation 
   */
  def propertyMissing(String name)
  {
    return createRelative(name)
  }

  def getAt(String name)
  {
    return createRelative(name)
  }

  private void writeObject(ObjectOutputStream out) throws IOException
  {
    out.writeObject(_fileResource.rootResource.file)
    out.writeObject(_fileResource.path)
  }

  private void readObject(ObjectInputStream ins) throws IOException, ClassNotFoundException
  {
    _fileResource = FileResource.create(ins.readObject(), ins.readObject())
  }

  boolean equals(o)
  {
    if(this.is(o)) return true

    if(!o || getClass() != o.class) return false

    if(_fileResource != o._fileResource) return false

    return true
  }

  int hashCode()
  {
    return _fileResource.hashCode()
  }

  public String toString()
  {
    return _fileResource.toString()
  }
}
