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

package org.linkedin.util.io;

import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;
import java.io.FileFilter;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Encapsulates a file that will go away when ready to be garbage collected.
 * 
 * @author ypujante@linkedin.com
 *
 */
public class GCFile extends File
{
  private static final long serialVersionUID = 1L;

  private File _file;

  public GCFile(File file)
  {
    super(file.getPath());
    _file = file;
    deleteOnExit();
  }

  @Override
  public boolean canRead()
  {
    return _file.canRead();
  }

  @Override
  public boolean canWrite()
  {
    return _file.canWrite();
  }

  @Override
  public int compareTo(File pathname)
  {
    return _file.compareTo(pathname);
  }

  @Override
  public boolean createNewFile()
    throws IOException
  {
    return _file.createNewFile();
  }

  public static GCFile createTempFile(String prefix, String suffix)
    throws IOException
  {
    return new GCFile(File.createTempFile(prefix, suffix));
  }

  public static GCFile createTempFile(String prefix, String suffix, File directory)
    throws IOException
  {
    return new GCFile(File.createTempFile(prefix, suffix, directory));
  }

  @Override
  public boolean delete()
  {
    return _file.delete();
  }

  @Override
  public void deleteOnExit()
  {
    _file.deleteOnExit();
  }

  @Override
  public boolean equals(Object obj)
  {
    return _file.equals(obj);
  }

  @Override
  public boolean exists()
  {
    return _file.exists();
  }

  @Override
  public File getAbsoluteFile()
  {
    return _file.getAbsoluteFile();
  }

  @Override
  public String getAbsolutePath()
  {
    return _file.getAbsolutePath();
  }

  @Override
  public File getCanonicalFile()
    throws IOException
  {
    return _file.getCanonicalFile();
  }

  @Override
  public String getCanonicalPath()
    throws IOException
  {
    return _file.getCanonicalPath();
  }

  @Override
  public String getName()
  {
    return _file.getName();
  }

  @Override
  public String getParent()
  {
    return _file.getParent();
  }

  @Override
  public File getParentFile()
  {
    return _file.getParentFile();
  }

  @Override
  public String getPath()
  {
    return _file.getPath();
  }

  @Override
  public int hashCode()
  {
    return _file.hashCode();
  }

  @Override
  public boolean isAbsolute()
  {
    return _file.isAbsolute();
  }

  @Override
  public boolean isDirectory()
  {
    return _file.isDirectory();
  }

  @Override
  public boolean isFile()
  {
    return _file.isFile();
  }

  @Override
  public boolean isHidden()
  {
    return _file.isHidden();
  }

  @Override
  public long lastModified()
  {
    return _file.lastModified();
  }

  @Override
  public long length()
  {
    return _file.length();
  }

  @Override
  public String[] list()
  {
    return _file.list();
  }

  @Override
  public String[] list(FilenameFilter filter)
  {
    return _file.list(filter);
  }

  @Override
  public File[] listFiles()
  {
    return _file.listFiles();
  }

  @Override
  public File[] listFiles(FileFilter filter)
  {
    return _file.listFiles(filter);
  }

  @Override
  public File[] listFiles(FilenameFilter filter)
  {
    return _file.listFiles(filter);
  }

  @Override
  public boolean mkdir()
  {
    return _file.mkdir();
  }

  @Override
  public boolean mkdirs()
  {
    return _file.mkdirs();
  }

  @Override
  public boolean renameTo(File dest)
  {
    return _file.renameTo(dest);
  }

  @Override
  public boolean setLastModified(long time)
  {
    return _file.setLastModified(time);
  }

  @Override
  public boolean setReadOnly()
  {
    return _file.setReadOnly();
  }

  @Override
  public String toString()
  {
    return _file.toString();
  }

  @Override
  public URI toURI()
  {
    return _file.toURI();
  }

  @Override
  public URL toURL()
    throws MalformedURLException
  {
    return _file.toURL();
  }

  /**
   * Deletes the file when the garbage collector reclaims it.
   * 
   * @throws Throwable
   */
  @Override
  protected void finalize() throws Throwable
  {
    super.finalize();
    delete();
  }
}
