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

package org.linkedin.groovy.util.cli

/**
 * Utilities methods for cli (cli builder)
 *
 * @author ypujante@linkedin.com
 */
class CliUtils
{
  /**
   * Parses the cli options and returns them as well as a <code>ConfigObject</code> where each key
   * is the long option and value is the value of the option
   *
   * @param params.cli the <code>CliBuilder</code> instance (required)
   * @param params.args the command line arguments (required)
   * @param params.configFileOption the (short) name of an option that would contain a config file
   * to read (optional)
   * @return a map <code>[config: c, options: o]</code> 
   */

  static def parseCliOptions(params)
  {
    def options = params.cli.parse(params.args)

    if(!options)
    {
      params.cli.usage()
      return null
    }

    Properties properties = new Properties()

    if(params.configFileOption)
    {
      def configFile = options."${params.configFileOption}"
      if(configFile)
      {
        new File(configFile).withInputStream {
          properties.load(it)
        }
      }
    }

    params.cli.options.options.each { option ->
      def value = options."${option.opt}"
      if(value != false)
        properties[option.longOpt] = value
    }

    [
        config: new ConfigSlurper().parse(properties),
        options: options
    ]
  }
}
