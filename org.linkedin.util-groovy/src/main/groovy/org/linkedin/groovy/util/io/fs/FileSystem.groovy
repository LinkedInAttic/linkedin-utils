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

/**
 * Abstraction to the filesystem
 *
 * @author ypujante@linkedin.com
 */
interface FileSystem
{
  /**
   * the root of the file system. All files created or returned by any methods on this class will
   * be under this root (except for temp files)
   */
  Resource getRoot()

  /**
   * the tmp root of the file system. All temp files created will be under this root
   */
  Resource getTmpRoot()

  /**
   * Returns a new file system where the root is set to the provided file (effectively making it
   * a sub file system of this one...)
   */
  FileSystem newFileSystem(file)

  /**
   * Returns a new file system where the root is set to the provided file (effectively making it
   * a sub file system of this one...)
   */
  FileSystem newFileSystem(newRoot, newTmpRoot)

  /**
   * Returns a resource relative to this filesystem
   */
  Resource toResource(file)

  /**
   * @param dir starting point for listing
   * @param closure the closure (dsl) containing include(name: '') and exclude(name: '') values
   */
  def ls(dir, Closure closure)

  /**
   * list all the files under the provided directory (or root if not provided) (not recursive)
   *
   */
  def ls(dir)

  /**
   * list all the files under root only (not recursive)
   */
  def ls()

  /**
   * Same as the other <code>ls</code>, but starts at root
   */
  def ls(Closure closure)

  Resource mkdirs(dir)

  void rm(file)

  void rmdirs(dir)

  /**
   * Remove all empty directories (that are children (recurisvely) of the provided directory).
   */
  void rmEmptyDirs(dir)

  /**
   * creates a file and populate its content with the provided (<code>String</code>) content
   */
  Resource saveContent(file, String content)

  /**
   * reads the content from the file and return it as a <code>String</code>
   */
  String readContent(file)

  Resource serializeToFile(file, serializable)

  def deserializeFromFile(file)

  def withOutputStream(file, closure)

  def withObjectOutputStream(file, closure)

  def withInputStream(file, closure)

  def withObjectInputStream(file, closure)
  
  def chmod(file, perm)

  def findAll(dir, closure)

  Resource eachChildRecurse(dir, closure)

  /**
   * Copy from to to...
   *
   * @return to as a resource
   */
  Resource cp(from, to)

  /**
   * Move from to to... (rename if file)
   *
   * @return to as a resource
   */
  Resource mv(from, to)

  /**
   * Creates a temp file:
   *
   * @param args.destdir where the file should be created (optional)
   * @param args.prefix a prefix for the file (optional)
   * @param args.suffix a suffix for the file (optional)
   * @param args.deleteonexit if the temp file should be deleted on exit (default to
   * @param args.createParents if the parent directories should be created (default to
   * <code>true</code>)
   * @return a file (note that it is just a file object and that the actual file has *not* been
   *         created and the parents may have been depending on the args.createParents value)
   */
  Resource tempFile(args)

  /**
   * Creates a temp file with all default values
   */
  Resource tempFile()

  /**
   * Create a temporary directory
   */
  Resource createTempDir()

  /**
   * Create a temporary directory
   * @see #tempFile(Object) for details on the options
   */
  Resource createTempDir(args)
}
