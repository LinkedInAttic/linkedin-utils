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

package org.linkedin.groovy.util.config

/**
 * @author ypujante@linkedin.com
 */
class Config
{
  static String getRequiredString(config, String name)
  {
    String value = getOptionalString(config, name, null)
    if(value == null)
      throw new MissingConfigParameterException(name)
    return value
  }

  static String getOptionalString(config, String name, String defaultValue)
  {
    def value = config?."${name}"

    if(value == null)
      value = defaultValue

    return value?.toString()
  }

  static boolean getOptionalBoolean(config, String name, boolean defaultValue)
  {
    def value = config?."${name}"

    String param = value?.toString()?.toLowerCase()

    if(param == null)
      return defaultValue

    switch(param)
    {
      case 'true':
      case 'yes':
      case 'on':
        return true

      case 'false':
      case 'no':
      case 'off':
        return false
    }
    
    throw new IllegalArgumentException("not a boolean : " + param);
  }

  static int getOptionalInt(config, String name, int defaultValue)
  {
    def value = config?."${name}"

    if(value == null)
      return defaultValue
    
    return value as int
  }
}
