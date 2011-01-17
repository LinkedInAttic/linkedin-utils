/*
 * Copyright 2010-2010 LinkedIn, Inc
 * Copyright (c) 2011 Yan Pujante
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


package org.linkedin.groovy.util.io

import org.linkedin.util.io.resource.Resource
import org.linkedin.util.io.IOUtils
import org.linkedin.util.io.PathUtils
import org.linkedin.groovy.util.net.GroovyNetUtils
import org.linkedin.groovy.util.ant.AntUtils

/**
 * IO related utilities
 *
 * @author ypujante@linkedin.com
 */
class GroovyIOUtils extends IOUtils
{

  /**
   * returns a file... handles <code>File</code>, URI, URL, string, <code>null</code>
   */
  static File toFile(s)
  {
    if(s == null)
      return null

    if(s instanceof File)
      return s

    if(s instanceof Resource)
    {
      return s.getFile()
    }

    if(s instanceof String)
    {
      def uri
      try
      {
        uri = new URI(s)
      }
      catch(URISyntaxException e)
      {
        // ok will handle below
      }

      if(!uri?.scheme)
      {
        return new File(s)
      }
    }

    URI uri = GroovyNetUtils.toURI(s)

    if(uri.scheme == 'file')
      return new File(uri.path)

    // this is not a local file => make it local...
    File tempFile = File.createTempFile('toFile', '')
    
    tempFile.deleteOnExit()

    tempFile.withOutputStream { out ->
      uri.toURL().withInputStream { ins ->
        out << ins
      }
    }

    return tempFile
  }

  /**
   * <code>true</code> if child is really a child of parent or in other words if child
   * is located in a subpath of parent (handle canonical path properly)
   */
  static boolean isChild(File parent, File child)
  {
    if(child == null || parent == null)
      return false

    return child.canonicalPath.startsWith(parent.canonicalPath)
  }

  /**
   * Ex: parent=/a/b/c child='/a/b/c/d/e'... would return d/e
   * @return a file object which contains the relative part from the child to the parent.
   * <code>null</code> if child is not a child of parent! If child is relative the return child
   */
  static File makeRelativeToParent(File parent, File child)
  {
    if(child == null || parent == null)
      return null

    if(!child.isAbsolute())
      return child

    String parentPath = parent.canonicalPath
    String childPath = child.canonicalPath

    if(childPath.startsWith(parentPath))
    {
      if(childPath == parentPath)
        return new File('')
      else
        return new File(PathUtils.removeLeadingSlash(childPath[parentPath.size()..-1]))
    }
    return null
  }

  /**
   * The difference between <code>reader.eachLine()</code> and this method is that
   * as soon as the closure returns <code>false</code> then the iteration is stopped. There is no
   * way to stop the iteration with <code>reader.eachLine()</code>.
   */
  static void eachLine(Reader reader, Closure closure)
  {
    if(!(reader instanceof BufferedReader))
    {
      reader = new BufferedReader(reader)
    }

    String line = null

    while((line = reader.readLine()) != null)
    {
      if(!closure(line))
        return
    }
  }

  /**
   * Convenient call which calls {@link #eachLine(Reader, Closure).
   */
  static void eachLine(URL url, Closure closure)
  {
    url.withReader { reader ->
      eachLine(reader, closure)
    }
  }

  /**
   * Every child resource of this resource (recursively) is being passed to the closure. If the
   * closure returns <code>true</code> then it will be part of the result.
   */
  static def findAll(Resource resource, Closure closure)
  {
    def matchingResources = []

    eachChildRecurse(resource) { Resource r ->
      if(closure(r))
        matchingResources << r
    }

    return matchingResources
  }

  /**
   * The closure will be called for every child (recursively) of the provided resource
   * @return the resource passed it
   */
  static Resource eachChildRecurse(Resource resource, Closure closure)
  {
    def dirs = []

    resource.list().each { Resource r ->
      closure(r)
      if(r.isDirectory())
        dirs << r
    }

    dirs.each { eachChildRecurse(it, closure) }

    return resource
  }

  /**
   * Creates the directory and parents of the provided directory. Returns dir.
   */
  static File mkdirs(File dir)
  {
    if(dir)
      return AntUtils.mkdirs(dir)
    else
      return null
  }

  /**
   * This convenient call takes a file you want to (over)write to and a closure. The closure is
   * called back with another file in the same folder that you can write to and then rename the file
   * to the one you wanted. The fact that it is in the same folder ensures that the rename should
   * be quick and not really require any copy thus is less likely to fail. If the rename fails it
   * throws an exception, thus ensuring that if there was an original file it won't be in a partial
   * state.
   *
   * @param toFile the final file where you want your output to be
   * @param closure takes a <code>File</code> as a parameter that you should use
   * @return whatever the closure returns
   */
  static def safeOverwrite(File toFile, Closure closure)
  {
    if(toFile == null)
      return null

    File dir = mkdirs(toFile.parentFile)

    File newFile = new File(dir, "${toFile.name}.${UUID.randomUUID().toString()}")

    def res = closure(newFile)

    if(!newFile.renameTo(toFile))
    {
      // somehow the rename operation did not work => delete new file and throw an exception
      // thus effectively leaving the file system in the same state as when the method was
      // called
      newFile.delete()
      throw new IOException("Unable to rename ${newFile} to ${toFile}")
    }

    return res
  }

  /**
   * Fetches the file pointed to by the location. The location can be <code>File</code>,
   * a <code>String</code> or <code>URI</code> and must contain a scheme. Example of locations:
   * <code>http://locahost:8080/file.txt'</code>, <code>file:/tmp/file.txt</code>,
   * <code>ivy:/org.linkedin/util-core/1.0.0</code>.
   *
   * @param location where is the content you want to retrieve locally
   * @param destination where to store the content locally
   * @return <code>destination</code>
   */
  static File fetchContent(location, File destination) throws IOException
  {
    URI uri = GroovyNetUtils.toURI(location)

    if(uri == null)
      throw new FileNotFoundException(location.toString())

    // See http://download.oracle.com/javase/1.4.2/docs/api/java/net/URI.html#getUserInfo()
    // Extract the user:pass if it exists
    String username = null
    String password = null
    def userInfo = uri.userInfo
    if(userInfo)
    {
      userInfo = userInfo.split(":")
      if(userInfo.length == 2)
      {
        username = userInfo[0]
        password = userInfo[1]
      }
    }
    
    try
    {
      AntUtils.withBuilder { ant ->
        ant.get(src: uri, dest: destination, username: username, password: password)
      }
    }
    catch(IOException e)
    {
      throw e
    }
    catch(Exception e)
    {
      IOException ioe = new FileNotFoundException(location?.toString())
      ioe.initCause(e)
      throw ioe
    }

    return destination
  }

  /**
   * Returns the content of the location as a <code>String</code>
   *
   * @throws IOException if the file is not reachable
   */
  static String cat(location) throws IOException
  {
    File tempFile = File.createTempFile('GroovyIOUtils.cat', '.txt')

    tempFile.deleteOnExit()

    try
    {
      fetchContent(location, tempFile)
      return tempFile.text
    }
    finally
    {
      tempFile.delete()
    }
  }
}
