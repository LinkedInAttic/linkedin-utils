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
 * This interface defines a convenient API to build a (simple) XML document.
 *
 * @author  ypujante@linkedin.com */
public interface XMLBuilder
{
  /**
   * Adds the XML declaration (<code>&lt;?xml version="1.0" ?&gt;</code>)
   *
   * @param versionInfo the version info */
  public void addXMLDecl(String versionInfo);

  /**
   * Adds the XML declaration (<code>&lt;?xml version="1.0" encoding="UTF-8" 
   * standalone="yes"?&gt;</code>)
   *
   * @param versionInfo the version info
   * @param encoding the encoding (eg: UTF-8)
   * @param standalone <code>true</code> for standalone */
  public void addXMLDecl(String versionInfo,
                         String encoding,
                         boolean standalone);

  /**
   * Adds an opening tag
   *
   * @param tagName the name of the opening tag to add */
  public void addOpeningTag(String tagName);

  /**
   * Adds an opening tag which contains an attribute
   *
   * @param tagName the name of the opening tag to add
   * @param attrName the name of the attribute
   * @param attrValue the value of the attribute */
  public void addOpeningTag(String tagName, String attrName, String attrValue);

  /**
   * Adds an opening tag which contains the attributes specified. All the
   * properties will be turned into attributes.
   *
   * @param tagName the name of the opening tag to add
   * @param attributes the attributes */
  public void addOpeningTag(String tagName, Map attributes);

  /**
   * Adds a closing tag
   *
   * @param tagName the name of the closing tag to add */
  public void addClosingTag(String tagName);

  /**
   * Adds an empty tag
   *
   * @param tagName the name of the empty tag to add */
  public void addEmptyTag(String tagName);

  /**
   * Adds an empty tag which contains an attribute
   *
   * @param tagName the name of the empty tag to add
   * @param attrName the name of the attribute
   * @param attrValue the value of the attribute */
  public void addEmptyTag(String tagName, String attrName, String attrValue);

  /**
   * Adds an empty tag which contains the attributes specified. All the
   * properties will be turned into attributes.
   *
   * @param tagName the name of the empty tag to add
   * @param attributes the attributes */
  public void addEmptyTag(String tagName, Map attributes);

  /**
   * Adds a tag
   *
   * @param tagName the name of the tag to add
   * @param tagValue the value of the tag to add */
  public void addTag(String tagName, String tagValue);

  /**
   * Adds a tag
   *
   * @param tagName the name of the tag to add
   * @param value the value of the tag to add */
  public void addTag(String tagName, int value);

  /**
   * Adds a tag
   *
   * @param tagName the name of the tag to add
   * @param value the value of the tag to add */
  public void addTag(String tagName, double value);

  /**
   * Adds a tag which contains an attribute
   *
   * @param tagName the name of the tag to add
   * @param tagValue the value of the tag to add
   * @param attrName the name of the attribute
   * @param attrValue the value of the attribute */
  public void addTag(String tagName,
                     String tagValue,
                     String attrName,
                     String attrValue);

  /**
   * Adds a tag which contains an attribute
   *
   * @param tagName the name of the tag to add
   * @param value the value of the tag to add
   * @param attrName the name of the attribute
   * @param attrValue the value of the attribute */
  public void addTag(String tagName,
                     int value,
                     String attrName,
                     String attrValue);

  /**
   * Adds a tag which contains an attribute
   *
   * @param tagName the name of the tag to add
   * @param value the value of the tag to add
   * @param attrName the name of the attribute
   * @param attrValue the value of the attribute */
  public void addTag(String tagName,
                     double value,
                     String attrName,
                     String attrValue);

  /**
   * Adds a tag which contains the attributes specified. All the
   * properties will be turned into attributes.
   *
   * @param tagName the name of the tag to add
   * @param tagValue the value of the tag to add
   * @param attributes the attributes */
  public void addTag(String tagName, String tagValue, Map attributes);

  /**
   * Adds a tag which contains the attributes specified. All the
   * properties will be turned into attributes.
   *
   * @param tagName the name of the tag to add
   * @param value the value of the tag to add
   * @param attributes the attributes */
  public void addTag(String tagName, int value, Map attributes);

  /**
   * Adds a tag which contains the attributes specified. All the
   * properties will be turned into attributes.
   *
   * @param tagName the name of the tag to add
   * @param value the value of the tag to add
   * @param attributes the attributes */
  public void addTag(String tagName, double value, Map attributes);

  /**
   * Adds the given block of XML. If it is not a properly formatter block
   * of XML then the output will not be properly formatted.
   *
   * @param xml the block of XML to add */
  public void addXML(String xml);

  /**
   * Returns the xml generated
   *
   * @return the XML generated */
  public String getXML();
  
  /**
   * Call this method when you want to reset the internal string to start
   * from scratch again */
  public void reset();
}
