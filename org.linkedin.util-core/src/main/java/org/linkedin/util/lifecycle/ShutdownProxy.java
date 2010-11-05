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

package org.linkedin.util.lifecycle;


import org.slf4j.Logger;
import org.linkedin.util.annotations.Initializer;
import org.linkedin.util.reflect.ObjectProxy;
import org.linkedin.util.reflect.ReflectUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Checks for shutdown then dispatches the call and let <code>Exception</code> go through and logs 
 * a message for all other exceptions.
 *
 * @author ypujante@linkedin.com
 *
 */
public class ShutdownProxy implements InvocationHandler, Serializable, ObjectProxy
{
  private static final long serialVersionUID = 1L;

  public static final String MODULE = ShutdownProxy.class.getName();
  public static final Logger log = org.slf4j.LoggerFactory.getLogger(MODULE);

  private Object _object;
  private Shutdown _shutdown;

  /**
   * Constructor
   */
  public ShutdownProxy(Object object, Shutdown shutdown)
  {
    _object = object;
    _shutdown = shutdown;
  }

  /**
   * Constructor
   */
  public ShutdownProxy()
  {
  }

  public Object getObject()
  {
    return _object;
  }

  @Initializer(required = true)
  public void setObject(Object object)
  {
    _object = object;
  }

  public Shutdown getShutdown()
  {
    return _shutdown;
  }

  @Initializer(required = true)
  public void setShutdown(Shutdown shutdown)
  {
    _shutdown = shutdown;
  }

  /**
   * @return the object proxied
   */
  @Override
  public Object getProxiedObject()
  {
    return _object;
  }

  /**
   * Method from the interface. Checks for shutdown then dispatches the call
   *
   * @param o
   * @param method
   * @param objects
   * @return
   * @throws Throwable
   */
  @Override
  public Object invoke(Object o, Method method, Object[] objects)
    throws Throwable
  {
    // throws ShutdownRequestedException when in shutdown mode
    _shutdown.startCall();
    try
    {
      return method.invoke(_object, objects);
    }
    catch(InvocationTargetException e)
    {
      Throwable th = e.getTargetException();

      try
      {
        throw th;
      }
      catch(Exception ex)
      {
        throw ex;
      }
      catch(Throwable throwable)
      {
        log.error(method.toString(), throwable);
        throw throwable;
      }
    }
    finally
    {
      _shutdown.endCall();
    }
  }
  /**
   * Creates the proxy to check for shutdown. Uses all interfaces defined by
   * this object (and recursively).
   *
   * @param o
   * @param shutdown
   * @return the proxy */
  public static Object createShutdownProxy(Object o,
                                           Shutdown shutdown)
  {
    return createShutdownProxy(o,
                               null,
                               shutdown);

  }

  /**
   * Creates the proxy to check for shutdown.
   *
   * @param o
   * @param interfaces restriction on which interface to apply the proxy on
   * @param shutdown
   * @return the proxy */
  public static Object createShutdownProxy(Object o,
                                           Class[] interfaces,
                                           Shutdown shutdown)
  {
    if(interfaces == null)
      interfaces = ReflectUtils.extractAllInterfaces(o);

    return Proxy.newProxyInstance(o.getClass().getClassLoader(),
                                  interfaces,
                                  new ShutdownProxy(o, shutdown));

  }

}
