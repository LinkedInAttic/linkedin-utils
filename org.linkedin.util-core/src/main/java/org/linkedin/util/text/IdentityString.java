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

package org.linkedin.util.text;

import org.linkedin.util.reflect.ObjectProxy;

import java.lang.reflect.Array;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;

/**
 * @author ypujante@linkedin.com
 *
 */
public class IdentityString
{
  public final static IdentityString FULL_IDENTITY_STRING =
    new IdentityString(new ClassNameExtractor()
    {
      @Override
      public String getClassName(Object o)
      {
        return o.getClass().getName();
      }
    });

  public final static IdentityString SHORT_IDENTITY_STRING =
    new IdentityString(new ClassNameExtractor()
    {
      @Override
      public String getClassName(Object o)
      {
        return o.getClass().getSimpleName();
      }
    });

  private static interface ClassNameExtractor
  {
    String getClassName(Object o);
  }

  private final ClassNameExtractor _classNameExtractor;

  /**
   * Constructor
   */
  public IdentityString(ClassNameExtractor classNameExtractor)
  {
    _classNameExtractor = classNameExtractor;
  }

  public String getIdentity(Object o)
  {
    if(o == null)
      return "null";

    if(o.getClass().isArray())
    {
      int len = Array.getLength(o);
      StringBuilder sb = new StringBuilder();
      for(int i = 0; i < len; i++)
      {
        if(i > 0)
          sb.append(",");
        sb.append(getIdentity(Array.get(o, i)));
      }
      return sb.toString();
    }

    StringBuilder sb = new StringBuilder();

    sb.append(_classNameExtractor.getClassName(o)).append('@').append(Integer.toHexString(System.identityHashCode(o)));

    if(Proxy.isProxyClass(o.getClass()))
    {
      InvocationHandler invocationHandler = Proxy.getInvocationHandler(o);
      if(invocationHandler instanceof ObjectProxy)
      {
        ObjectProxy objectProxy = (ObjectProxy) invocationHandler;
        Object proxiedObject = objectProxy.getProxiedObject();

        sb.append(" (proxy of: ").append(getIdentity(proxiedObject)).append(")");
      }
    }

    return sb.toString();
  }
}
