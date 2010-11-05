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

package org.linkedin.util.xml;

import java.util.Map;

/**
 * Define some methods in terms of others so that there is less code to
 * implement in further implementation.
 *
 * @author  ypujante@linkedin.com */
public abstract class AbstractXMLBuilder implements XMLBuilder
{
  /**
   * Constructor */
  public AbstractXMLBuilder()
  {
  }
  
  /**
   * Adds a tag
   *
   * @param tagName the name of the tag to add
   * @param value the value of the tag to add */
  @Override
  public void addTag(String tagName, int value)
  {
    addTag(tagName, String.valueOf(value));
  }

  /**
   * Adds a tag
   *
   * @param tagName the name of the tag to add
   * @param value the value of the tag to add */
  @Override
  public void addTag(String tagName, double value)
  {
    addTag(tagName, String.valueOf(value));
  }

  /**
   * Adds a tag which contains an attribute
   *
   * @param tagName the name of the tag to add
   * @param value the value of the tag to add
   * @param attrName the name of the attribute
   * @param attrValue the value of the attribute */
  @Override
  public void addTag(String tagName,
                     int value,
                     String attrName,
                     String attrValue)
  {
    addTag(tagName, String.valueOf(value), attrName, attrValue);
  }

  /**
   * Adds a tag which contains an attribute
   *
   * @param tagName the name of the tag to add
   * @param value the value of the tag to add
   * @param attrName the name of the attribute
   * @param attrValue the value of the attribute */
  @Override
  public void addTag(String tagName,
                     double value,
                     String attrName,
                     String attrValue)
  {
    addTag(tagName, String.valueOf(value), attrName, attrValue);
  }

  /**
   * Adds a tag which contains the attributes specified. All the
   * properties will be turned into attributes.
   *
   * @param tagName the name of the tag to add
   * @param value the value of the tag to add
   * @param attributes the attributes */
  @Override
  public void addTag(String tagName, int value, Map attributes)
  {
    addTag(tagName, String.valueOf(value), attributes);
  }

  /**
   * Adds a tag which contains the attributes specified. All the
   * properties will be turned into attributes.
   *
   * @param tagName the name of the tag to add
   * @param value the value of the tag to add
   * @param attributes the attributes */
  @Override
  public void addTag(String tagName, double value, Map attributes)
  {
    addTag(tagName, String.valueOf(value), attributes);
  }
}
