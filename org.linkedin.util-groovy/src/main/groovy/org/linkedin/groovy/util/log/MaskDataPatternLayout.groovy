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

import org.apache.log4j.PatternLayout
import org.apache.log4j.helpers.PatternParser

/**
 * Add support for filtering log message based on regex. This is mainly used to make sure we do not log sensitive information
 * in log files.
 *
 * This is achieved by adding a new option '%w' (inverted m) that processed the message for filtering. In log4j config MaskingRegex
 * should also be defined that contains these filters and actions as a groovy closure.
 *
 * Ex:
 *
 *  <layout class="org.linkedin.groovy.util.log.MaskDataPatternLayout">
 *    <param name="ConversionPattern" value="%d{yyyy/MM/dd HH:mm:ss.SSS} %p [%c{1}] %w%n" />
 *    <!-- Define a map that contains key as the regex that should be filtered.
 *         and value is a closure that specifies how it should be filtered.
 *    -->
 *    <param name="MaskingRegex" value="[
 *              /(encryptedKeys\\s*:\\s*\\[)(.*?\\]\\s*)(\\])/:
 *                     { Object[] it ->
 *                              it[1] + '*** MASKED ***' + it[3]
 *                      }
 *              ]" />
 *  </layout>
 *
 * @author mdubey@linkedin.com
 *
 * Created: Aug 2, 2010 12:57:15 PM
 */
public class MaskDataPatternLayout extends PatternLayout {

  String maskingRegex;

  @Override
  public void activateOptions()
  {
    // This call on super forces a call to createPatternParser, which we need to get Parser setup correctly
    setConversionPattern(getConversionPattern())
  }
  
  @Override
  protected PatternParser createPatternParser(String pattern) {
    return new MaskDataPatternParser(pattern, getMaskingRegex());
  }

  public void setMaskingRegex(String maskingRegex)
  {
    this.maskingRegex = maskingRegex
  }

  public String getMaskingRegex()
  {
    return maskingRegex;
  }

  @Override
  public boolean ignoresThrowable()
  {
      return false;
  }

}
