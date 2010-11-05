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

package org.linkedin.groovy.util.log

import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.spi.LoggingEvent
import org.apache.log4j.spi.ThrowableInformation
import java.util.regex.Pattern
import java.util.regex.Matcher

/**
 * @author mdubey@linkedin.com
 *
 * Created: Aug 2, 2010 1:32:04 PM
 */
class MaskDataPatternConverter extends PatternConverter
{
  boolean processThrowable
  def maskingMap

  public MaskDataPatternConverter(String maskingRegex, boolean processThrowable)
  {
    super();
    this.processThrowable = processThrowable

    // load the maskingRegex as a map
    if (maskingRegex) {
      GroovyShell groovysh = new GroovyShell()
      this.maskingMap = groovysh.evaluate(maskingRegex)
      // optimzation to store precompiled pattern in the map
//      def newMap = [:]
//      maskingMap.each { k, v ->
//        if(k instanceof String)
//          k = Pattern.compile(k)
//        newMap[k] = v
//      }
//      maskingMap = newMap
    }
  }

  @Override
  protected String convert(LoggingEvent event)
  {
    if (processThrowable) {
      ThrowableInformation information = event.getThrowableInformation()
      StringBuilder builder = new StringBuilder()

      if (information != null) {
        String[] stringRep = information.getThrowableStrRep()

        int length = stringRep.length;
        for (int i = 0; i < length; i++) {
            String string = stringRep[i]
            builder.append(maskSensitiveData(string)).append("\n")
        }
      }
      return builder.toString()

    } else {
      // convert message to filter out any encrypted data
      return maskSensitiveData(event.getRenderedMessage())

    }

  }

  private String maskSensitiveData(String message)
  {
    if (maskingMap) {
      maskingMap.each { entry ->
        def pat = entry.key
        Closure c = entry.value
        message = message.replaceAll(pat) { Object[] it ->
          def updated = c(it)
          return updated
        }
      }
    }
    return message
  }
}
