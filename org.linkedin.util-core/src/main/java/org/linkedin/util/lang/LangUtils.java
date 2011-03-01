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

package org.linkedin.util.lang;

import org.linkedin.util.exceptions.InternalException;
import org.linkedin.util.io.IOUtils;
import org.linkedin.util.text.IdentityString;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;

/**
 * Contains useful global utils methods.
 *
 * @author ypujante@linkedin.com */
public class LangUtils
{
  /*
   * @param o the object you want the identity string (note that <code>null</code> is ok)
   * @return a string representing the identity of an object which is what <code>o.toString()</code>
   * would return if both {@link Object#toString()} and {@link Object#hashCode()} were not overriden.
   */
  public static String identityString(Object o)
  {
    return IdentityString.FULL_IDENTITY_STRING.getIdentity(o);
  }

  /**
   * This version returns a short version of the class name
   *
   * @param o the object you want the identity string (note that <code>null</code> is ok)
   * @return a string representing the identity of an object which is what <code>o.toString()</code>
   *         would return if both {@link Object#toString()} and {@link Object#hashCode()} were not
   *         overriden.
   */
  public static String shortIdentityString(Object o)
  {
    return IdentityString.SHORT_IDENTITY_STRING.getIdentity(o);
  }

  /**
   * Compares 2 values. Calls compareTo when they are not null
   *
   * @param o1
   * @param o2
   * @return the result of compareTo */
  public static <T extends Comparable<T>> int compare(T o1, T o2)
  {
    if(o1 == null)
    {
      if(o2 == null)
        return 0;
      else
        return -1;
    }

    if(o2 == null)
    {
      // here d1 is not null (already tested..)
      return 1;
    }

    return o1.compareTo(o2);
  }

  /**
   * Equivalent to <code>o1.equals(o2)</code> but handle <code>null</code> properly
   */
  public static boolean isEqual(Object o1, Object o2)
  {
    if(o1 == null)
      return o2 == null;

    if(o2 == null)
      return false;

    return o1.equals(o2);
  }

  /**
   * Compares 2 ints. The purpose of this method is to return 0,1,-1 depending on how
   * o1 and o2 compares
   *
   * @param o1
   * @param o2
   * @return 0 if o1==o2, -1 if o1 &lt; o2, 1, if o1 &gt; o2 */
  public static int compare(int o1, int o2)
  {
    if(o1 == o2)
      return 0;

    if(o1 < o2)
      return -1;

    return 1;
  }

  /**
   * Compares 2 longs. The purpose of this method is to return 0,1,-1 depending on how
   * o1 and o2 compares
   *
   * @param o1
   * @param o2
   * @return 0 if o1==o2, -1 if o1 &lt; o2, 1, if o1 &gt; o2 */
  public static int compare(long o1, long o2)
  {
    if(o1 == o2)
      return 0;

    if(o1 < o2)
      return -1;

    return 1;
  }

  /**
   * Compares 2 floats. The purpose of this method is to return 0,1,-1 depending on how
   * o1 and o2 compares
   *
   * @param o1
   * @param o2
   * @return 0 if o1==o2, -1 if o1 &lt; o2, 1, if o1 &gt; o2 */
  public static int compare(float o1, float o2)
  {
    if(o1 == o2)
      return 0;

    if(o1 < o2)
      return -1;

    return 1;
  }

  /**
   * This method compares 2 dates. It is mainly written to account for a bug
   * in jdk15 which throws a ClassCastException when calling d1.compareTo(d2).
   * It also adds handling of <code>null</code> values.
   *
   * @param d1
   * @param d2
   * @return 0 if equal, &lt; 0 if d1 &lt; d2 and &gt; 0 if d1 &gt; d2
   */
  public static int compareTo(Date d1, Date d2)
  {
    if(d1 == null)
    {
      if(d2 == null)
        return 0;
      else
        return -1;
    }

    if(d2 == null)
    {
      // here d1 is not null (already tested..)
      return 1;
    }

    // here d1 and d2 are not null
    long delta = d1.getTime() - d2.getTime();

    if (delta > 0)
    {
      return 1;
    }
    else if (delta < 0)
    {
      return -1;
    }
    else
    {
      return 0;
    }
  }

  /**
   * Clone by serializing / deserializing... only works if the object is actually serializable!
   */
  @SuppressWarnings("unchecked")
  public static <T extends Serializable> T deepClone(T serializable)
  {
    if(serializable == null)
      return null;

    try
    {
      return (T) IOUtils.deserialize(IOUtils.serialize(serializable));
    }
    catch(IOException e)
    {
      throw new InternalException(e);
    }
    catch(ClassNotFoundException e)
    {
      throw new InternalException(e);
    }
  }

  /**
   * Clone by serializing / deserializing... only works if the object is actually serializable!
   */
  @SuppressWarnings("unchecked")
  public static <T extends Serializable> T deepClone(T serializable, ClassLoader classLoader)
  {
    if(serializable == null)
      return null;

    try
    {
      return (T) IOUtils.deserialize(IOUtils.serialize(serializable), classLoader);
    }
    catch(IOException e)
    {
      throw new InternalException(e);
    }
    catch(ClassNotFoundException e)
    {
      throw new InternalException(e);
    }
  }

  /**
   * Converts the object into a boolean value.
   *
   * @param o the object to convert
   * @return a <code>boolean</code>  */
  public static boolean convertToBoolean(Object o)
  {
    if(o == null)
      return false;

    if(o instanceof Boolean)
    {
      return (Boolean) o;
    }

    return convertToBoolean(o.toString());
  }

  /**
   * Converts the string into a boolean value. <code>true</code> or
   * <code>yes</code> are considered to be <code>true</code>. <code>false</code>
   * or <code>no</code> are <code>false</code>.
   *
   * @param s the string to convert
   * @return a <code>boolean</code>  */
  public static boolean convertToBoolean(String s)
  {
    if(s == null)
      return false;

    if(s.equals("false") || s.equals("no") || s.equals("off"))
      return false;

//    if(s.equals("true") || s.equals("yes") || s.equals("on"))
//      return true;

    return true;
  }

  /**
   * Returns the stack trace of the throwable as a string
   *
   * @param th the throwable
   * @return the stack trace as a string */
  public static String getStackTrace(Throwable th)
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    th.printStackTrace(pw);
    return sw.toString();
  }
}
