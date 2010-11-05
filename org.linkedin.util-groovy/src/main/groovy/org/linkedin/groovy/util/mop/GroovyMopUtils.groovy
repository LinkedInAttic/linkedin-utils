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


package org.linkedin.groovy.util.mop

/**
 * This class contains utility methods for meta programming
 *
 * @author ypujante@linkedin.com
 */
class GroovyMopUtils
{
  /**
   * This method should be called from a missingMethod handler. It looks up a set of delegates
   * as an alternate for the method call (note that the order of the delegates matters). If a
   * delegate is found, it is automatically added as a new method to not have any penalty
   * with future calls
   */
  static def missingMethodDelegate(o, name, args, delegates)
  {
    // try to locate a delegate which can answer the call
    def delegate = delegates.find {it?.metaClass?.respondsTo(it, name, args) }

    if(delegate)
    {
      def methods = delegate.metaClass.respondsTo(delegate, name, args)
      // add the closure to o
      o.metaClass."${name}" << delegate.&"${name}"
      return methods[0].invoke(delegate, args)
    }
    else
    {
      throw new MissingMethodException("Unsupported call ${name}", o.class, args)
    }
  }
}
