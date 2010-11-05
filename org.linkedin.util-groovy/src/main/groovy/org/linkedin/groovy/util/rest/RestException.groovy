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

package org.linkedin.groovy.util.rest

import org.linkedin.groovy.util.json.JsonUtils

/**
 * Exception which will contain the original exception (which may not be available in this VM)...
 * @author ypujante@linkedin.com */
class RestException extends Exception
{
  private static final long serialVersionUID = 1L;

  public final String originalClassName
  public final String originalMessage

  RestException(String className, String message, StackTraceElement[] stackTrace)
  {
    super("class: ${className}, message: ${message}".toString())
    originalClassName = className
    originalMessage = message
    setStackTrace(stackTrace)
  }

  /**
   * From a json representation (as built by {@link #toJSON(Throwable)) builds a rest exception
   */
  static RestException fromJSON(jsonRepresentation)
  {
    def res = null
    def parent = null
    jsonRepresentation?.exception?.each { cause ->
      def ex = new RestException(cause.name, cause.message, rebuildStackTrace(cause.stackTrace))
      if(res == null)
        res = ex
      parent?.initCause(ex)
      parent = ex
    }
    return res
  }

  /**
   * 'Serializes' the throwable into a json representation in order to be able to rebuild it later.
   */
  static def toJSON(Throwable th)
  {
    if(th)
    {
      return JsonUtils.toJSON([exception: extractFullStackTrace(th, [])])
    }
    else
      return null
  }

  private static StackTraceElement[] rebuildStackTrace(stackTrace)
  {
    def elements = []

    stackTrace.each { ste ->
      elements << new StackTraceElement(ste.dc, ste.mn, ste.fn, ste.ln as int)
    }

    return elements as StackTraceElement[]
  }

  private static def extractFullStackTrace(exception, out)
  {
    if(exception)
    {
      out << [name: exception.getClass().name, message: exception.message, stackTrace: extractStackTrace(exception)]
      extractFullStackTrace(exception.cause, out)
    }

    return out
  }

  private static def extractStackTrace(exception)
  {
    def stackTrace = []

    exception?.stackTrace?.each { ste ->
      stackTrace << [dc: ste.className, mn: ste.methodName, fn: ste.fileName, ln: ste.lineNumber]
    }

    return stackTrace
  }
}
