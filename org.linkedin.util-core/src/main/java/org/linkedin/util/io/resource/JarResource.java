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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.linkedin.util.io.PathUtils;
import org.linkedin.util.io.resource.internal.AbstractResource;
import org.linkedin.util.io.resource.internal.InternalResourceProvider;
import org.linkedin.util.io.resource.internal.JarResourceProvider;
import org.linkedin.util.io.resource.internal.LeafResource;
import org.linkedin.util.io.resource.internal.ResourceProvider;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author ypujante@linkedin.com
 *
 */
public class JarResource extends AbstractResource
{
  public static final String MODULE = JarResource.class.getName();
  public static final Logger log = LoggerFactory.getLogger(MODULE);

  private final String _fullPath;
  private final LeafResource _jarResource;

  /**
   * Make sure we close the jar file when the input stream gets closed
   */
  private static class CloseJarFileInputStream extends FilterInputStream
  {
    private final JarFile _jarFile;

    public CloseJarFileInputStream(Resource resource, JarFile jarFile, String location) throws IOException
    {
      super(extractInputStream(jarFile, location, resource));
      _jarFile = jarFile;
    }

    private static InputStream extractInputStream(JarFile jarFile, String location, Resource resource)
      throws IOException
    {
      JarEntry jarEntry = jarFile.getJarEntry(location);

      if(jarEntry == null)
        throw new IOException("cannot get input stream for entry " + location + " for " + resource.toURI());

      if(jarEntry.getSize() == 0)
      {
        JarEntry directoryEntry =
          jarFile.getJarEntry(PathUtils.addTrailingSlash(location));
        if(directoryEntry != null)
          throw new IOException("cannot read directory for " + resource.toURI());

      }
      InputStream is = jarFile.getInputStream(jarEntry);
      if(is == null)
      {
        throw new IOException("cannot get input stream for entry " + jarEntry + " for " + resource.toURI());
      }
      return is;
    }

    @Override
    public void close() throws IOException
    {
      // YP Note: I know it looks like a bug because in general we do super.close in the finally
      // but in this case the input stream is actually associated to the jar file so we need
      // to close it first before closing the jar file...
      try
      {
        super.close();
      }
      finally
      {
        _jarFile.close();
      }
    }
  }

  /**
   * Constructor
   */
  public JarResource(InternalResourceProvider resourceProvider,
                        String path,
                        LeafResource jarResource,
                        String fullPath)
  {
    super(resourceProvider, path);

    _jarResource = jarResource;
    // the full path within the jar should not start with '/'
    _fullPath = PathUtils.removeLeadingSlash(fullPath);
  }

  /**
   * @return the content jar file (please close it when done!)
   * @throws IOException
   */
  private JarFile getContentJarFile() throws IOException
  {
    return new JarFile(_jarResource.getFile());
  }

  /**
   * Returns a <code>File</code> handle for this resource.
   *
   * @throws IOException if the resource cannot be resolved as a <code>File</code> handle, i.e. it
   *                     is not available on the file system (or it cannot be made available).
   */
  @Override
  public File getFile() throws IOException
  {
    throw new IOException("not supported");
  }

  /**
   * @return <code>true</code> if the resource exists.
   */
  @Override
  public boolean exists()
  {
    // represents the root...
    if(_fullPath.equals(""))
    {
      return _jarResource.exists();
    }
    
    try
    {
      JarFile contentJarFile = getContentJarFile();
      try
      {
        JarEntry jarEntry = contentJarFile.getJarEntry(_fullPath);
        return jarEntry != null;
      }
      finally
      {
        contentJarFile.close();
      }
    }
    catch(IOException e)
    {
      if(log.isDebugEnabled())
        log.debug("exception (ignored) while getting entry " + _fullPath, e);

      return false;
    }
  }

  /**
   * Important note: the caller of this method is responsible for properly closing the input
   * stream!
   *
   * @return an input stream to the resource.
   * @throws IOException if cannot get an input stream
   */
  @Override
  public InputStream getInputStream() throws IOException
  {
    JarFile contentJarFile = getContentJarFile();
    try
    {
      CloseJarFileInputStream jarFileInputStream =
        new CloseJarFileInputStream(this, contentJarFile, _fullPath);

      // we are delegating the closure of the jar file to the input stream...
      contentJarFile = null;
      return jarFileInputStream;
    }
    finally
    {
      if(contentJarFile != null)
        contentJarFile.close();
    }
  }


  /**
   * Efficiently returns all information about the resource.
   *
   * @return information about this resource.
   * @throws IOException if cannot get information
   */
  @Override
  public ResourceInfo getInfo() throws IOException
  {
    // represents the root...
    if(_fullPath.equals(""))
    {
      return new StaticInfo(0, // root is a directory...
                            _jarResource.getInfo().getLastModified()); // last modified is the jar file itself!
    }

    JarFile contentJarFile = getContentJarFile();
    try
    {
      JarEntry jarEntry = contentJarFile.getJarEntry(_fullPath);

      if(jarEntry == null)
        throw new ResourceNotFoundException(toURI());

      return new StaticInfo(jarEntry.getSize(),
                            jarEntry.getTime());
    }
    finally
    {
      contentJarFile.close();
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
    // for jar resource there is a big optimization... if the last modified time of the jar resource
    // itself is in the past, then there is no need to check inside the jar file at all!
    if(_jarResource.isModifiedSince(time))
    {
      return super.isModifiedSince(time);
    }
    else
      return false;
  }

  /**
   * @return <code>true</code> if this resource represents a directory.
   */
  @Override
  public boolean isDirectory()
  {
    // represents the root...
    if(_fullPath.equals(""))
    {
      return true;
    }

    try
    {
      String fullPath = PathUtils.removeTrailingSlash(_fullPath);

      JarFile contentJarFile = getContentJarFile();
      try
      {
        JarEntry entry = contentJarFile.getJarEntry(fullPath);

        if(entry != null && entry.getSize() == 0)
        {
          // we know that a directory reports a size of 0, so we need to check whether it is
          // actually a directory or a file of size 0...
          JarEntry directoryEntry = 
            contentJarFile.getJarEntry(PathUtils.addTrailingSlash(fullPath));
          if(directoryEntry != null)
            return true;
        }

        return false;
      }
      finally
      {
        contentJarFile.close();
      }
    }
    catch(IOException e)
    {
      if(log.isDebugEnabled())
        log.debug("exception (ignored) while getting entry " + _fullPath, e);

      return false;
    }
  }

  /**
   * @return a uri representation of the resource
   */
  @Override
  public URI toURI()
  {
    return toURI(_fullPath);
  }

  /**
   * @return a uri representation of the resource
   */
  private URI toURI(String location)
  {
    StringBuilder sb = new StringBuilder("jar:");
    sb.append(_jarResource.toURI().toString());
    sb.append("!/");
    sb.append(location);

    return URI.create(sb.toString());
  }

  /**
   * Factory method.
   *
   * @param uri the uri to the jar... note that it can either be jar: or a plain uri to the
   * jar (ex: file://tmp/foo.jar and jar:file://tmp/foo.jar!/ are both valid and pointing to the
   * same resource!)
   */
  public static Resource create(URI uri) throws URISyntaxException, IOException
  {
    return create(uri, URIResourceFactory.DEFAULT);
  }

  /**
   * Factory method.
   *
   * @param uri the uri to the jar... note that it can either be jar: or a plain uri to the
   * jar (ex: file://tmp/foo.jar and jar:file://tmp/foo.jar!/ are both valid and pointing to the
   * same resource!)
   */
  public static Resource create(URI uri, URIResourceFactory factory)
    throws URISyntaxException
  {
    String path = "/";
    
    // case when the URI is already a jar uri
    if("jar".equals(uri.getScheme()))
    {
      String schemeSpecificPart = uri.getSchemeSpecificPart();

      if(schemeSpecificPart == null)
        throw new URISyntaxException(uri.toString(), "no scheme specific part found...");

      int idx = schemeSpecificPart.indexOf("!/");
      if(idx == -1)
        throw new URISyntaxException(uri.toString(), "couldn't find !/");

      uri = new URI(schemeSpecificPart.substring(0, idx));
      path = schemeSpecificPart.substring(idx + 1);
    }

    // case when the URI points directly to the jar resource
    try
    {
      ResourceProvider provider =
        new JarResourceProvider(factory.createResource(uri));

      return provider.createResource("/").createRelative(path);
    }
    catch(UnsupportedURIException e)
    {
      URISyntaxException ex = new URISyntaxException(uri.toString(), "cannot create resource");
      ex.initCause(e);
      throw ex;
    }
  }

  /**
   * Convenient call to create a resource from the jar resource (points at the root of the
   * jar...)
   *
   * @param jarResource
   * @return the resource pointing at the root of the jar
   */
  public static Resource create(Resource jarResource)
  {
    return create(jarResource, "/");
  }

  /**
   * Convenient call to create a resource from the jar resource (points at rootPath in the
   * jar...)
   *
   * @param jarResource
   * @param rootPath where is the root in the jar
   * @return the resource with root at rootPath
   */
  public static Resource create(Resource jarResource, String rootPath)
  {
    return new JarResourceProvider(jarResource, rootPath).getRootResource();
  }
}
