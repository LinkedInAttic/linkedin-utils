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


package org.linkedin.groovy.util.net

/**
 *  Contains utilities for net
 *
 * @author ypujante@linkedin.com
 */
class GroovyNetUtils
{
  /**
   * Converts the provided parameter into a URI... handle: File, strings, URI, URL, <code>null</code>
   */
  static URI toURI(s)
  {
    if(s == null)
      return null

    if(s instanceof URI)
      return s

    if(s instanceof File)
      return s.canonicalFile.toURI()

    if(s.metaClass.respondsTo(s, 'toURI', []))
    {
      return s.toURI()
    }

    def uri
    try
    {
      uri = new URI(s.toString())
    }
    catch(MalformedURLException e)
    {
      // ok will handle below
    }

    if(!uri?.scheme)
    {
      uri = new File(s.toString()).toURI()
    }

    return uri
  }


  /**
   * Encapsulates the creation of a URL by first trying the normal way and if it fails, it
   * falls back to the <code>SingletonURLStreamHandlerFactory</code>
   * 
   * @return the url
   */
  static URL toURL(s)
  {
    if(s == null)
      return null

    if(s instanceof URL)
      return s

    URI uri = toURI(s)

    try
    {
      return uri.toURL()
    }
    catch(MalformedURLException e)
    {
      def handler = SingletonURLStreamHandlerFactory.INSTANCE.createURLStreamHandler(uri.scheme)
      if(!handler)
        throw e
      return new URL(null, uri.toString(), handler)
    }
  }

  /**
   * Method which tries to infer the filename from the uri.
   */
  static String guessFilename(URI uri)
  {
    String filename = null

    if(uri?.scheme == 'ivy')
    {
      def connection = uri.toURL().openConnection()
      connection.connect()
      try
      {
        filename = connection.files?.getAt(0)?.name
      }
      finally
      {
        connection.disconnect()
      }
    }

    if(!filename)
    {
      filename = (uri?.path?.split('/') ?: ['unknown'])[-1]
    }

    return filename
  }
}
