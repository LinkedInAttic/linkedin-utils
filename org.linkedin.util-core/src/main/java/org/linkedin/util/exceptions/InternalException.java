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

package org.linkedin.util.exceptions;

import java.lang.reflect.InvocationTargetException;

/**
 * Exception thrown when there is an internal problem. Should *not* be used for business style
 * exception (bad example: <code>NoSuchMemberException</code>). Usually used to wrap other low
 * level exceptions (ex: a <code>SQLException</code> reprensenting the fact that the db is down).
 *
 *
 * @author ypujante@linkedin.com */
public class InternalException extends RuntimeException
{
  private static final long serialVersionUID = 1L;

  public InternalException(String module, Throwable detail)
  {
    super(module, detail);
  }

  public InternalException(String module, String detail)
  {
    super(module + ":" + detail);
  }

  public InternalException(String module, String detail, Throwable th)
  {
    super(module + ":" + detail, th);
  }

  public InternalException(String module)
  {
    super(module);
  }

  public InternalException(Throwable th)
  {
    super(th);
  }

  public InternalException()
  {
    super();
  }

  /**
   * Convenient call to adapt the <code>InvocationTargetException</code> into an
   * <code>InternalException</code>
   *
   * @param module the module
   * @param ite the invocation target exception (coming from <code>Method.invoke</code>)
   * @throws InternalException this method always throw an exception at the end: either a
   * <code>RuntimeException</code> or <code>Error</code> or <code>InternalException</code>
   */
  public static InternalException throwInternalException(String module,
                                                         InvocationTargetException ite)
    throws InternalException
  {
    try
    {
      throw ite.getTargetException();
    }
    catch(Error e)
    {
      throw e;
    }
    catch(RuntimeException e)
    {
      throw e;
    }
    catch(Throwable e)
    {
      throw new InternalException(module, e);
    }
  }
}
