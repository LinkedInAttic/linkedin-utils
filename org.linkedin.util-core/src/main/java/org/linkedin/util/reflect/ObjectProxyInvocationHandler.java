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

package org.linkedin.util.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Very basic implementation of the proxy which simply delegates all calls to the proxied object.
 *
 * @author ypujante@linkedin.com
 *
 */
public class ObjectProxyInvocationHandler<T> implements ObjectProxy<T>, InvocationHandler
{
  private final T _proxiedObject;

  /**
   * Constructor
   */
  public ObjectProxyInvocationHandler(T proxiedObject)
  {
    _proxiedObject = proxiedObject;
  }

  /**
   * @return the object proxied
   */
  @Override
  public T getProxiedObject()
  {
    return _proxiedObject;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
  {
    try
    {
      return method.invoke(_proxiedObject, args);
    }
    catch(InvocationTargetException e)
    {
      throw e.getTargetException();
    }
  }

  /**
   * This is a convenient call when you want to proxy an object with the unique given
   * interface. Note that you can use the more general call {@link Proxy#newProxyInstance(ClassLoader, Class[], InvocationHandler)}
   * if you want to do something different.
   *
   * @param interfaceToProxy the interface to proxy
   * @param objectToProxy the object to proxy (note that it must implement interfaceToProxy!)
   * @return the proxied object  */
  @SuppressWarnings("unchecked")
  public static <T> T createProxy(Class<T> interfaceToProxy, T objectToProxy)
  {
    // since there is only one interface in the array, we know that the proxy must implement it!
    return (T) Proxy.newProxyInstance(interfaceToProxy.getClassLoader(),
                                      new Class<?>[] {interfaceToProxy},
                                      new ObjectProxyInvocationHandler(objectToProxy));
  }
}
