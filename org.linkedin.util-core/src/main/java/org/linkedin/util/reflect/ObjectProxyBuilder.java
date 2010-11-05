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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author ypujante@linkedin.com
 *
 */
public class ObjectProxyBuilder
{
  /**
   * Convenient call which creates a proxy using the handler and the interface. It will also
   * check that the object bien proxied (if implements <code>ObjectProxy</code>) properly
   * implement the interface...
   *
   * @return the proxy (which implement all the interface)
   */
  @SuppressWarnings("unchecked")
  public static <T> T createProxy(InvocationHandler handler, Class<T> itface)
  {
    if(handler instanceof ObjectProxy)
    {
      ObjectProxy proxy = (ObjectProxy) handler;
      if(!ReflectUtils.isSubClassOrInterfaceOf(proxy.getProxiedObject().getClass(), itface))
        throw new IllegalArgumentException(proxy.getProxiedObject().getClass() + " does not extend " + itface);
    }

    return (T) Proxy.newProxyInstance(itface.getClassLoader(),
                                      new Class<?>[] {itface},
                                      handler);
  }

  /**
   * Convenient call which creates a proxy using the handler and the interfaces. It will also
   * check that the object bien proxied (if implements <code>ObjectProxy</code>) properly
   * implement the right interfaces...
   *
   * @return the proxy (which implement all the provided interfaces)
   */
  public static Object createProxy(InvocationHandler handler, Class<?>... itfaces)
  {
    if(handler instanceof ObjectProxy)
    {
      ObjectProxy proxy = (ObjectProxy) handler;
      Class<?> proxyClass = proxy.getProxiedObject().getClass();
      for(Class<?> itface : itfaces)
      {
        if(!ReflectUtils.isSubClassOrInterfaceOf(proxyClass, itface))
          throw new IllegalArgumentException(proxyClass + " does not extend " + itface);
      }
    }

    List<ClassLoader> classLoaders = new ArrayList<ClassLoader>();
    for(Class<?> itface : itfaces)
    {
      classLoaders.add(itface.getClassLoader());
    }

    ClassLoader classLoader = ClassLoaderChain.createChain(classLoaders);

    return Proxy.newProxyInstance(classLoader,
                                  itfaces,
                                  handler);
  }

  /**
   * @see #createProxy(InvocationHandler, Class[])
   */
  public static Object createProxy(InvocationHandler handler, Collection<Class<?>> interfaces)
  {
    return createProxy(handler, interfaces.toArray(new Class[interfaces.size()]));
  }
}
