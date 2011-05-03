/*
 * Copyright (c) 2011 Yan Pujante
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
package org.linkedin.groovy.util.lang

import org.linkedin.util.lang.LangUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author yan@pongasoft.com */
public class GroovyLangUtils extends LangUtils
{
  public static final String MODULE = GroovyLangUtils.class.getName();
  public static final Logger log = LoggerFactory.getLogger(MODULE);

  /**
   * Returned by {@link #noException(Closure)} when an exception happens
   */
  static final Object NOEXCEPTION_ERROR = new Object()

  /**
   * The closure will be executed and no exception will ever be thrown. The normal usage of this
   * method is from a finally block. It is very dangerous to execute some code in a finally block
   * that throws an exception because it will mask the original exception if there was one.
   *
   * This method will make all possible attemps to log the error in the {@link #log} first (warning
   * will contain the message of the exception, and debug will contain the full stack trace), and if
   * it fails it will try <code>System.err</code>, and if this fails as well then there won't be
   * any message logged.
   *
   * @param closure
   * @return whatever the closure returns or {@link #NOEXCEPTION_ERROR} if exception
   */
  static def noException(Closure closure)
  {
    try
    {
      return closure()
    }
    catch(Throwable t)
    {
      try
      {
        log.warn("Detected unexpected exception [ignored]: ${t.message}")
        if(log.isDebugEnabled())
          log.debug("Detected unexpected exception [ignored]", t)
      }
      catch (Throwable t2)
      {
        try
        {
          System.err.println("Error detected while logging output.. trying System.err")
          t.printStackTrace(System.err)
          t2.printStackTrace(System.err)
        }
        catch (Throwable t3)
        {
          // this is desperate.. there is really nothing we can do
        }
      }
    }

    return NOEXCEPTION_ERROR
  }
}