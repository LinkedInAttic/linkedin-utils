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

package org.linkedin.util.url;

import java.util.Iterator;
import java.util.Map;

/**
 * The interface which defines the query in a url. Only accessors.
 *
 * @author ypujante@linkedin.com
 */
public interface Query
{
  /**
   * @return <code>true</code> if query parameters have been added */
  boolean getHasQueryParameters();

  /**
   * @return the iterator of parameter names  */
  Iterator<String> getParameterNames();

  /**
   * @return the query as a string */
  String getQuery();

  /**
   * Gets the parameters given its name
   *
   * @param name the name of the parameter
   * @return the parameters or <code>null</code> if none found*/
  String[] getParameterValues(String name);

  /**
   * @return the parameter map */
  Map<String, String[]> getParameterMap();

  /**
   * Get the first parameter given its name
   *
   * @param name the name of the parameter
   * @return the first parameter  */
  String getParameter(String name);
  
  /**
   * @return the encoding used by the query */ 
  String getEncoding();
  
  /**
   * Gets the first parameter given its name and converts it to the given boolean type.
   * 
   * @param name the name of the parameter
   * @return true if parmaeterValue equals the name that is given, or if it is one of the following strings
   *         'yes', 'true', 'on'
   */
  boolean getBooleanParameter(String name);
  
  /**
   * Gets the first parameter given its name and converts it to int
   * @param name
   * @param defaultValue
   * @return return an int, if parameter not found or parse exception, return default value
   */
  int getIntParameter(String name, int defaultValue);
}