/*
 * Copyright 2010-2010 LinkedIn, Inc
 * Portions Copyright (c) 2013 Yan Pujante
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

package org.linkedin.groovy.util.log

import java.util.logging.LogManager
import org.slf4j.bridge.SLF4JBridgeHandler

/**
 * @author ypujante@linkedin.com
 */
class JulToSLF4jBridge
{
  /**
   * The problem with <code>SLF4JBridgeHandler.install()</code> method is that it does not
   * remove the other handlers and as a result logs still make it the 'old' way.
   */
  synchronized static void installBridge()
  {
    if(!SLF4JBridgeHandler.isInstalled())
    {
      def rootLogger = LogManager.getLogManager().getLogger("")

      rootLogger.handlers.each {
        rootLogger.removeHandler(it)
      }

      rootLogger.addHandler(new SLF4JBridgeHandler())
    }
  }
}
