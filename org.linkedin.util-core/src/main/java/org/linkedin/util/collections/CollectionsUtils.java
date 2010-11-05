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

package org.linkedin.util.collections;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.EnumSet;
import java.util.Properties;

/**
 * Contains useful global utils methods.
 *
 * @author ypujante@linkedin.com */
public class CollectionsUtils
{
  /**
   * Reverses the array in place.
   * @param array the array to reverse
   * @return the <em>same</em> array provided NOT a new one!
   */
  public static <T> T[] reverse(T[] array)
  {
    if(array == null)
      return array;

    int s = 0;
    int e = array.length - 1;
    while(s < e)
    {
      // swap index e and s
      T tmp = array[e];
      array[e] = array[s];
      array[s] = tmp;

      s++;
      e--;
    }

    return array;
  }

  /**
   * Turns an array of enumeration values into an enum set
   *
   * @param clazz the type of the enum
   * @param ts the array of enums
   * @return the enum set containing all the values from the array
   */
  public static <T extends Enum<T>> EnumSet<T> toEnumSet(Class<T> clazz, T... ts)
  {
    if(ts == null)
      return null;

    EnumSet<T> res = EnumSet.noneOf(clazz);
    for(T t : ts)
    {
      res.add(t);
    }
    return res;
  }

  /**
   * Convenient call to load a properties file from the provided file
   */
  public static Properties loadProperties(File file) throws IOException
  {
    if(file == null)
      return null;

    FileReader reader = new FileReader(file);
    try
    {
      return loadProperties(reader);
    }
    finally
    {
      reader.close();
    }
  }

  /**
   * Convenient call to load a properties file from the provided reader
   */
  public static Properties loadProperties(Reader reader) throws IOException
  {
    if(reader == null)
      return null;

    Properties properties = new Properties();
    properties.load(reader);
    return properties;
  }
}
