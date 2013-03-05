/*
 * Copyright 2010-2010 LinkedIn, Inc
 * Portions Copyright (c) 2011-2013 Yan Pujante
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

import org.linkedin.groovy.util.ant.AntUtils
import org.linkedin.groovy.util.io.GroovyIOUtils
import org.linkedin.util.io.resource.FileResource
import org.linkedin.util.io.resource.Resource
import org.linkedin.util.lifecycle.Destroyable

/**
 * Represents methods available for the file system
 *
 * @author ypujante@linkedin.com
 */
def class FileSystemImpl implements FileSystem, Destroyable
{
  final Resource _root
  final Resource _tmpRoot

  FileSystemImpl(File rootDir)
  {
    this(rootDir, AntUtils.tempFile(prefix: 'FileSystemImpl', suffix: '.tmp'))
  }

  FileSystemImpl(File rootDir, File tempDir)
  {
    _root = toSerializableResource(FileResource.createFromRoot(AntUtils.mkdirs(rootDir)))
    _tmpRoot = toSerializableResource(FileResource.createFromRoot(AntUtils.mkdirs(tempDir)))
  }

  /**
   * the root of the file system. All files created or returned by any methods on this class will
   * be under this root.
   */
  Resource getRoot()
  {
    return _root
  }

  public Resource getTmpRoot()
  {
    return _tmpRoot
  }

  /**
   * Returns a new file system where the root is set to the provided file (effectively making it
   * a sub file system of this one...)  
   */
  FileSystem newFileSystem(newRoot)
  {
    return new FileSystemImpl(toFile(newRoot), _tmpRoot.file)
  }

  public FileSystem newFileSystem(newRoot, newTmpRoot)
  {
    return new FileSystemImpl(toFile(newRoot), toFile(newTmpRoot))
  }

  Resource mkdirs(dir)
  {
    Resource resource = toResource(dir)
    AntUtils.mkdirs(resource.file)
    return resource
  }

  void rm(file)
  {
    AntUtils.withBuilder { it.delete(file: toFile(file)) }
  }

  void rmdirs(dir)
  {
    AntUtils.withBuilder { it.delete(dir: toFile(dir)) }
  }

  void rmEmptyDirs(dir)
  {
    dir = toResource(dir)

    while(true)
    {
      def emptyDirs = GroovyIOUtils.findAll(dir) { it.isDirectory() && it.ls().size() == 0}
      if(emptyDirs)
      {
        emptyDirs.each { rmdirs(it) }
      }
      else
      {
        break // of while
      }
    }

    if(dir.isDirectory() && dir.ls().size() == 0)
      rmdirs(dir)
  }

  def findAll(dir, closure)
  {
    dir = toResource(dir)
    return GroovyIOUtils.findAll(dir, closure)
  }

  Resource eachChildRecurse(dir, closure)
  {
    dir = toResource(dir)
    return GroovyIOUtils.eachChildRecurse(dir, closure)
  }

  Resource saveContent(file, String content)
  {
    Resource resource = toResourceWithParents(file, true)

    withOutputStream(resource.file) { fos ->
      fos.write(content.getBytes('UTF-8'))
    }
    
    return resource
  }

  public String readContent(file)
  {
    return toFile(file).getText()
  }

  Resource serializeToFile(file, serializable)
  {
    Resource resource = toResourceWithParents(file, true)

    withObjectOutputStream(resource) { oos ->
      oos.writeObject(serializable)
    }

    return resource
  }

  def deserializeFromFile(file)
  {
    return withObjectInputStream(file) { ois ->
      return ois.readObject()
    }
  }

  private def createClosureWithVariableParams(File file, closure)
  {
    if(closure.maximumNumberOfParameters == 1)
    {
      return closure
    }
    else
    {
      def newClosure = { out -> closure(file, out) }
      return newClosure
    }
  }

  def withOutputStream(file, closure)
  {
    File localFile = toFile(file, true)
    return safeOverwrite(localFile) { Resource localResource ->
      localResource.file.withOutputStream(createClosureWithVariableParams(localFile, closure))
    }
  }

  def withObjectOutputStream(file, closure)
  {
    File localFile = toFile(file, true)
    return safeOverwrite(localFile) { Resource localResource ->
      localResource.file.withObjectOutputStream(createClosureWithVariableParams(localFile, closure))
    }
  }

  def withInputStream(file, closure)
  {
    file = toFile(file)
    return file.withInputStream(createClosureWithVariableParams(file, closure))
  }

  def withObjectInputStream(file, closure)
  {
    file = toFile(file)
    return file.withObjectInputStream(createClosureWithVariableParams(file, closure))
  }

  def chmod(file, perm)
  {
    Resource localResource = toResource(file)

    File localFile = localResource?.file

    if(localFile.exists())
    {
      AntUtils.withBuilder { ant ->
        if(localFile.isDirectory())
        {
          ant.chmod(dir: localFile, perm: perm)
        }
        else
        {
          ant.chmod(file: localFile, perm: perm)
        }
      }
    }

    return localResource
  }

  @Override
  def safeOverwrite(file, Closure closure)
  {
    GroovyIOUtils.safeOverwrite(toResource(file)?.file) { File newFile ->
      closure(toResource(newFile))
    }
  }

  public Resource tempFile()
  {
    return tempFile(null)
  }

  /**
   * Creates a temp file:
   *
   * @param args.destdir where the file should be created (optional)
   * @param args.prefix a prefix for the file (optional)
   * @param args.suffix a suffix for the file (optional)
   * @param args.deleteonexit if the temp file should be deleted on exit (default to
   *                          <code>false</code>)
   * @param args.createParents if the parent directories should be created (default to
   * <code>true</code>)
   * @return a file (note that it is just a file object and that the actual file has *not* been
   *         created and the parents may have been depending on the args.createParents value)
   */
  Resource tempFile(args)
  {
    args = args ?: [:]
    args = new HashMap(args)
    args.destdir = args.destdir ? toFile(args.destdir): toFile(_tmpRoot)
    args.prefix = args.prefix ?: '__tmp'
    args.deleteonexit = args.deleteonexit ?: false

    return toResource(AntUtils.tempFile(args))
  }

  Resource createTempDir()
  {
    return createTempDir(suffix: 'Dir');
  }

  Resource createTempDir(args)
  {
    def tempDir = tempFile(args)
    mkdirs(tempDir)
    return tempDir;
  }

  public ls()
  {
    return ls(_root)
  }

  public ls(Closure closure)
  {
    return ls(_root, closure)
  }

  public ls(dir, Closure closure)
  {
    dir = toFile(dir)
    def res = []
    ['fileset', 'dirset'].each { method ->
      AntUtils.withBuilder { ant ->
        ant."${method}"(dir: dir, closure).each { res << toResource(it.file) }
      }
    }
    return res
  }

  def ls(dir) {
    return ls(dir) {
      include(name: '*')
    }
  }

  /**
   * Copy from to to...
   *
   * @return to as a resource
   */
  Resource cp(from, to)
  {
    def toIsDirectory = to.toString().endsWith('/')

    from = toResource(from)

    if(!from.exists())
      throw new FileNotFoundException(from.toString())

    to = toResource(to)

    if(toIsDirectory && !to.exists())
      throw new FileNotFoundException(to.toString())

    if(to.isDirectory())
    {
      to = to.createRelative(from.filename)
    }

    mkdirs(to.parentResource)
    
    def copyArgs = [overwrite: true, file: from.file, tofile: to.file]


    AntUtils.withBuilder { ant ->
      ant.copy(copyArgs)
    }

    return to
  }

  /**
   * Move from to to... (rename if file)
   *
   * @return to as a resource
   */
  Resource mv(from, to)
  {
    def toIsDirectory = to.toString().endsWith('/')

    from = toResource(from)

    if(!from.exists())
      throw new FileNotFoundException(from.toString())

    to = toResource(to)

    if(toIsDirectory && !to.exists())
      throw new FileNotFoundException(to.toString())

    if(to.isDirectory())
    {
      to = to.createRelative(from.filename)
    }

    mkdirs(to.parentResource)

    def moveArgs = [overwrite: true, file: from.file, tofile: to.file]

    AntUtils.withBuilder { ant ->
      ant.move(moveArgs)
    }

    return to
  }

  void destroy()
  {
    rmdirs(_root)
    rmdirs(_tmpRoot)
  }

  /**
   * Convenient call mainly used for testing purposes...
   */
  public static FileSystem createTempFileSystem()
  {
    return new FileSystemImpl(AntUtils.tempFile(prefix: 'FileSystemImpl'))
  }

  /**
   * Convenient call mainly used for testing purposes...
   */
  public static void createTempFileSystem(Closure closure)
  {
    def fs = createTempFileSystem()
    try
    {
      closure(fs)
    }
    finally
    {
      fs.destroy()
    }
  }

  private File toFile(file)
  {
    return toFile(file, false)
  }

  private File toFile(file, boolean createParents)
  {
    return toResourceWithParents(file, createParents).file
  }

  Resource toResource(file)
  {
    return toResourceWithParents(file, false)
  }

  private Resource toResourceWithParents(file, boolean createParents)
  {
    // first convert into a file
    file = GroovyIOUtils.toFile(file, tmpRoot.file)

    if(file == null)
      throw new IOException('Unknown null file')

    Resource res

    File child = GroovyIOUtils.makeRelativeToParent(_root.file, file)
    if(child)
    {
      res = _root.createRelative(computePath(child))
    }
    else
    {
      child = GroovyIOUtils.makeRelativeToParent(_tmpRoot.file, file)
      if(child)
      {
        res = _tmpRoot.createRelative(computePath(child))
      }
      else
      {
        res = _root.createRelative(computePath(file))
      }
    }

    if(createParents)
    {
      AntUtils.mkdirs(res.parentResource.file)
    }

    return toSerializableResource(res)
  }

  private def computePath(File file)
  {
    // bug with weird character...
    def path = file.path
    if(path == '/')
      return path
    path = path.split('/').collect { URLEncoder.encode(it, 'UTF-8') }.join('/')
    return path
  }

  /**
   * Makes the resource serializable
   */
  private Resource toSerializableResource(resource)
  {
    return SerializableFileResource.toFR(resource)
  }
}
