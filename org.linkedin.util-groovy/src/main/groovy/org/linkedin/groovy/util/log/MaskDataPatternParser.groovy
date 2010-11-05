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

import org.apache.log4j.helpers.PatternParser

/**
 * @author mdubey@linkedin.com
 *
 * Created: Aug 2, 2010 1:29:10 PM
 */
class MaskDataPatternParser extends PatternParser
{
  private static final char MASKED_MESSAGE_CHAR = 'w' // looks like inverted m and is unused by PatternLayout
  private static final char MASKED_THROWABLE_CHAR = 's' // for use by printStacktrace  

  private String maskingRegex

  public MaskDataPatternParser(String pattern, String maskingRegex) {
    super(pattern);
    this.maskingRegex = maskingRegex;
  }

  @Override
  protected void finalizeConverter(char c) {
    switch (c) {
      case MASKED_MESSAGE_CHAR:
        currentLiteral.setLength(0);
        addConverter(new MaskDataPatternConverter(maskingRegex, false));
        break;
      case MASKED_THROWABLE_CHAR:
        currentLiteral.setLength(0);
        addConverter(new MaskDataPatternConverter(maskingRegex, true));
        break;
      default:
        super.finalizeConverter(c);
    }
  }
}
