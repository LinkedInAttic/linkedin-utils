/*
 * Copyright 2010-2010 LinkedIn, Inc
 * Portions Copyright (c) 2011 Yan Pujante
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

package org.linkedin.groovy.util.ivy

import groovy.xml.NamespaceBuilder
import org.linkedin.groovy.util.io.GroovyIOUtils
import org.linkedin.util.io.PathUtils
import org.linkedin.groovy.util.ant.AntUtils

/**
 * Handle <code>ivy:/organisation/name/version</code> style urls
 *
 * @author ypujante@linkedin.com */
class IvyURLHandler extends URLStreamHandler
{
  private final def _defaultIvySettings

  IvyURLHandler(defaultIvySettings)
  {
    _defaultIvySettings = defaultIvySettings
  }

  protected URLConnection openConnection(URL url)
  {
    def ivyCoordinates = PathUtils.removeLeadingSlash(url.path).split('/')
    return new IvyURLConnection(url, _defaultIvySettings, ivyCoordinates);
  }

  protected void parseURL(URL u, String spec, int start, int limit)
  {
    super.parseURL(u, spec, start, limit)

    if(u.getHost() || u.getPort() != -1)
      throw new UnsupportedOperationException("host/port not supported yet")

    if(u.getQuery())
      throw new IllegalArgumentException("no query string is allowed")

    def ivyCoordinates = PathUtils.removeLeadingSlash(u.path).split('/')
    if(ivyCoordinates.size() < 3 || ivyCoordinates.size() > 4)
      throw new IllegalArgumentException("Bad ivy coordinates: ${u}")
  }
}

/**
 * Handle <code>ivy:/organisation/name/version</code> style urls
 *
 * @author ypujante@linkedin.com */
class IvyURLConnection extends URLConnection
{
  private final def _ivySettings
  private final def _ivyCoordinates

  private def _files

  IvyURLConnection(URL url, ivySettings, ivyCoordinates)
  {
    super(url)
    _ivySettings = ivySettings
    _ivyCoordinates = ivyCoordinates
  }

  public void connect()
  {
    AntUtils.withBuilder { ant ->
      def ivy = NamespaceBuilder.newInstance(ant, 'antlib:org.apache.ivy.ant')

      GroovyIOUtils.withFile(_ivySettings) { File ivySettingsFile ->
        ivy.settings(file: ivySettingsFile)

        ivy.cachefileset(setid: 'fetchFromIvy',
                         conf: _ivyCoordinates.size() == 4 ? _ivyCoordinates[3] : 'default',
                         inline: true,
                         organisation: _ivyCoordinates[0],
                         module: _ivyCoordinates[1],
                         revision: _ivyCoordinates[2])

        _files = ant.project.getReference('fetchFromIvy').collect { it.file }
      }
    }
    connected = true
  }

  void disconnect()
  {
    connected = false
    // nothing to do...
  }

  def getFiles()
  {
    return _files
  }

  public InputStream getInputStream()
  {
    if (!connected)
      connect()
    
    if(_files.size() != 1)
    {
      throw new IOException("not 1 artifact downloaded for ${_ivyCoordinates}: ${_files}")
    }

    return new FileInputStream(_files[0])
  }
}
