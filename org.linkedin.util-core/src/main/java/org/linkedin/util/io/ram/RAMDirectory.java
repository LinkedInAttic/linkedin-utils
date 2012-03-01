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

package org.linkedin.util.io.ram;

import org.linkedin.util.clock.Clock;
import org.linkedin.util.clock.SystemClock;
import org.linkedin.util.io.resource.RAMResource;
import org.linkedin.util.io.resource.Resource;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.FileNotFoundException;

/**
 * @author ypujante@linkedin.com
 *
 */
public class RAMDirectory extends RAMEntry
{
  private final Map<String, RAMEntry> _directoryContent;

  public RAMDirectory()
  {
    this(SystemClock.instance(), "");
  }

  public RAMDirectory(Clock clock)
  {
    this(clock, "");
  }

  public RAMDirectory(Clock clock,
                      String name,
                      Map<String, RAMEntry> directoryContent)
  {
    super(clock, name);
    _directoryContent = directoryContent;
  }

  public RAMDirectory(Clock clock,
                      String name,
                      long lastModifiedDate,
                      Map<String, RAMEntry> directoryContent)
  {
    super(clock, name, lastModifiedDate);
    _directoryContent = directoryContent;
  }

  /**
   * Creates empty directory
   * 
   * @param lastModifiedDate
   */
  public RAMDirectory(Clock clock, String name, long lastModifiedDate)
  {
    this(clock, name, lastModifiedDate, new HashMap<String, RAMEntry>());
  }

  /**
   * Creates empty directory
   */
  public RAMDirectory(Clock clock, String name)
  {
    this(clock, name, new HashMap<String, RAMEntry>());
  }

  public RAMDirectory cd(String path) throws FileNotFoundException
  {
    RAMEntry ramEntry = getEntryByPath(path);
    if(ramEntry instanceof RAMDirectory)
      return (RAMDirectory) ramEntry;
    else
      throw new FileNotFoundException(path);
  }

  public RAMEntry getEntry(String name)
  {
    return _directoryContent.get(name);
  }

  /**
   * @param path
   * @return the entry or <code>null</code> if not found
   */
  public RAMEntry getEntryByPath(String path)
  {
    RAMEntry entry = this;

    String[] pathComponents = path.split("/");

    for(String pathComponent : pathComponents)
    {
      // we skip empty path components (caused by leading / + double //)
      if(pathComponent.equals(""))
        continue;

      if(entry instanceof RAMDirectory)
      {
        RAMDirectory ramDirectory = (RAMDirectory) entry;
        entry = ramDirectory.getEntry(pathComponent);
      }
      else
      {
        entry = null;
      }

      if(entry == null)
        break;
    }

    return entry;
  }

  @Override
  public long getContentLength()
  {
    return 0;
  }

  /**
   * Equivalent to 'touch' unix command: updates the last modified date to the value provided
   * and if the entry does not create it, it creates an empty file!
   *
   * @param name
   * @return the entry (either a new file or the entry that was modified)
   */
  public RAMEntry touch(String name)
  {
    return touch(name, _clock.currentTimeMillis());
  }

  /**
   * Equivalent to 'touch' unix command: updates the last modified date to the value provided
   * and if the entry does not create it, it creates an empty file!
   *
   * @param name
   * @param lastModifiedDate
   * @return the entry (either a new file or the entry that was modified)
   */
  public RAMEntry touch(String name, long lastModifiedDate)
  {
    RAMEntry ramEntry = _directoryContent.get(name);
    if(ramEntry == null)
    {
      ramEntry = add(new RAMFile(_clock, name, new byte[0]));
    }

    ramEntry.touch(lastModifiedDate);

    return ramEntry;
  }

  /**
   * Copy the entry in this directory with the provided name
   * 
   * @param entry
   * @return the touched entry
   */
  public RAMEntry add(RAMEntry entry)
  {
    touch();
    _directoryContent.put(entry.name(), entry);
    return entry;
  }

  /**
   * Creates a file with the content and add it to the directory with this name
   *
   * @param name
   * @return the created file
   */
  public RAMFile add(String name, byte[] content)
  {
    return (RAMFile) add(new RAMFile(_clock, name, content));
  }

  /**
   * Creates a file with the content and add it to the directory with this name
   *
   * @param name
   * @param content as a string
   * @return the created file
   */
  public RAMFile add(String name, String content)
  {
    try
    {
      return add(name, content.getBytes("UTF-8"));
    }
    catch(UnsupportedEncodingException e)
    {
      // should not happen
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates an empty directory and add it to this directory with this name. If the directory
   * already exists, it does not recreate it.
   *
   * @param name
   * @return the created directory
   * @throws IOException if the name represents a file
   */
  public RAMDirectory mkdir(String name) throws IOException
  {
    RAMEntry entry = getEntry(name);
    if(entry instanceof RAMDirectory)
    {
      RAMDirectory ramDirectory = (RAMDirectory) entry;
      return ramDirectory;
    }
    else
    {
      if(entry == null)
      {
        RAMDirectory directory = new RAMDirectory(_clock, name);
        return (RAMDirectory) add(directory);
      }
      else
      {
        throw new IOException("File exists: " + name);
      }
    }
  }

  /**
   * Removes the entry (whether it is a directory or not)
   *
   * @param name
   * @return the entry removed (<code>null</code> if did not exist)
   */
  public RAMEntry rm(String name)
  {
    return _directoryContent.remove(name);
  }

  /**
   * Creates an empty directory and add it to this directory with this path (equivalent
   * to unix command): create all intermediary directories
   *
   * @param path
   * @return the created directory
   */
  public RAMDirectory mkdirhier(String path) throws IOException
  {
    RAMDirectory directory = this;

    String[] pathComponents = path.split("/");

    for(String pathComponent : pathComponents)
    {
      // we skip empty path components (caused by leading / + double //)
      if(pathComponent.equals(""))
        continue;

      RAMEntry entry = directory.getEntry(pathComponent);

      if(entry == null)
      {
        directory = directory.mkdir(pathComponent);
      }
      else
      {
        if(entry instanceof RAMDirectory)
        {
          directory = (RAMDirectory) entry;
        }
        else
        {
          throw new IOException("File exists: " + pathComponent);
        }
      }
    }

    return directory;
  }

  /**
   * @return the content of the directory
   */
  public Collection<RAMEntry> ls()
  {
    return _directoryContent.values();
  }

  /**
   * @return this object wrapped in a resource
   */
  public Resource toResource()
  {
    return RAMResource.create(this);
  }
}
