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

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * @author ypujante@linkedin.com
*
*/ /*
* A useful utility class that will enumerate over an array of
* enumerations.
*/
public class CompoundEnumeration<E> implements Enumeration<E>
{
  private final Enumeration<E>[] _enums;
  private int index = 0;

  public CompoundEnumeration(Enumeration<E>[] enums)
  {
    _enums = enums;
  }

  private boolean next()
  {
    while(index < _enums.length)
    {
      if(_enums[index] != null && _enums[index].hasMoreElements())
      {
        return true;
      }
      index++;
    }
    return false;
  }

  @Override
  public boolean hasMoreElements()
  {
    return next();
  }

  @Override
  public E nextElement()
  {
    if(!next())
    {
      throw new NoSuchElementException();
    }
    return _enums[index].nextElement();
  }
}
