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
import java.util.Iterator;

/**
 * Implements the XMLBuilder interface to create XML documents using a 
 * <code>StringBuffer</code>. This should be much more lightweight than using
 * the DOM API.
 *
 * @author  ypujante@linkedin.com
 * @see XMLBuilder */
public class XML extends AbstractXMLBuilder
{
  /**
   * Contains the xml document as it is generated */
  private final StringBuilder _xml = new StringBuilder();

  /**
   * Constructor */
  public XML()
  {
  }

  /**
   * Adds the XML declaration (<code>&lt;?xml version="1.0" ?&gt;</code>)
   *
   * @param versionInfo the version info */
  @Override
  public void addXMLDecl(String versionInfo)
  {
    addXMLDecl(_xml, versionInfo);
  }

  /**
   * Adds the XML declaration (<code>&lt;?xml version="1.0" ?&gt;</code>)
   *
   * @param xml which buffer to append to
   * @param versionInfo the version info */
  static void addXMLDecl(StringBuilder xml, String versionInfo)
  {
    xml.append("<?xml version=\"").append(versionInfo).append("\"?>\n");
  }
  
  /**
   * Adds the XML declaration (<code>&lt;?xml version="1.0" encoding="UTF-8" 
   * standalone="yes"?&gt;</code>)
   *
   * @param versionInfo the version info
   * @param encoding the encoding (eg: UTF-8)
   * @param standalone <code>true</code> for standalone */
  @Override
  public void addXMLDecl(String versionInfo,
                         String encoding,
                         boolean standalone)
  {
    _xml.append("<?xml version=\"").append(versionInfo);
    _xml.append("\" encoding=\"");
    _xml.append(encoding).append("\" standalone=\"");
    _xml.append(standalone ? "yes" : "no");
    _xml.append("\"?>\n");
  }

  /**
   * Adds an opening tag
   *
   * @param tagName the name of the opening tag to add */
  @Override
  public void addOpeningTag(String tagName)
  {
    _xml.append('<').append(tagName).append('>');
  }

  /**
   * Adds a closing tag
   *
   * @param tagName the name of the closing tag to add */
  @Override
  public void addClosingTag(String tagName)
  {
    _xml.append("</").append(tagName).append('>');
  }

  /**
   * Adds an empty tag
   *
   * @param tagName the name of the empty tag to add */
  @Override
  public void addEmptyTag(String tagName)
  {
    _xml.append('<').append(tagName).append(" />");    
  }

  /**
   * Adds a tag
   *
   * @param tagName the name of the tag to add
   * @param tagValue the value of the tag to add */
  @Override
  public void addTag(String tagName, String tagValue)
  {
    if(tagValue == null)
      addEmptyTag(tagName);
    else
    {
      addOpeningTag(tagName);
      _xml.append(XMLUtils.xmlEncode(tagValue));
      addClosingTag(tagName);
    }
  }

  /**
   * Adds the given block of XML. If it is not a properly formatter block
   * of XML then the output will not be properly formatted.
   *
   * @param xml the block of XML to add */
  @Override
  public void addXML(String xml)
  {
    _xml.append(xml);
  }

  /**
   * Returns the xml generated */
  @Override
  public String getXML()
  {
    return _xml.toString();
  }

  /**
   * Call this method when you want to reset the internal string to start
   * from scratch again */
  @Override
  public void reset()
  {
    _xml.setLength(0);
  }
  
  /**
   * Adds an empty tag which contains the attributes specified. All the
   * properties will be turned into attributes.
   *
   * @param tagName the name of the empty tag to add
   * @param attributes the attributes  */
  @Override
  public void addEmptyTag(String tagName, Map attributes)
  {
    _xml.append('<').append(tagName);
    addAttributes(attributes);
    _xml.append(" />");
  }
  
  /**
   * Adds an empty tag which contains an attribute
   *
   * @param tagName the name of the empty tag to add
   * @param attrName the name of the attribute
   * @param attrValue the value of the attribute  */
  @Override
  public void addEmptyTag(String tagName, String attrName, String attrValue)
  {
    _xml.append('<').append(tagName);
    addAttribute(attrName, attrValue);
    _xml.append(" />");
  }
  
  /**
   * Adds an opening tag which contains the attributes specified. All the
   * properties will be turned into attributes.
   *
   * @param tagName the name of the opening tag to add
   * @param attributes the attributes  */
  @Override
  public void addOpeningTag(String tagName, Map attributes)
  {
   _xml.append('<').append(tagName);
   addAttributes(attributes);
   _xml.append('>');
  }
  
  /**
   * Adds an opening tag which contains an attribute
   *
   * @param tagName the name of the opening tag to add
   * @param attrName the name of the attribute
   * @param attrValue the value of the attribute  */
  @Override
  public void addOpeningTag(String tagName, String attrName, String attrValue)
  {
    _xml.append('<').append(tagName);
    addAttribute(attrName, attrValue);
    _xml.append('>');
  }
  
  /**
   * Adds a tag which contains the attributes specified. All the
   * properties will be turned into attributes.
   *
   * @param tagName the name of the tag to add
   * @param tagValue the value of the tag to add
   * @param attributes the attributes  */
  @Override
  public void addTag(String tagName, String tagValue, Map attributes)
  {
    if(tagValue == null)
      addEmptyTag(tagName, attributes);
    else
    {
      addOpeningTag(tagName, attributes);
      _xml.append(XMLUtils.xmlEncode(tagValue));
      addClosingTag(tagName);
    }
  }
  
  /**
   * Adds a tag which contains an attribute
   *
   * @param tagName the name of the tag to add
   * @param tagValue the value of the tag to add
   * @param attrName the name of the attribute
   * @param attrValue the value of the attribute  */
  @Override
  public void addTag(String tagName,
                     String tagValue,
                     String attrName,
                     String attrValue)
  {
    if(tagValue == null)
      addEmptyTag(tagName, attrName, attrValue);
    else
    {
      addOpeningTag(tagName, attrName, attrValue);
      _xml.append(XMLUtils.xmlEncode(tagValue));
      addClosingTag(tagName);
    }
  }
  
  /**
   * Adds the attribute
   *
   * @param attrName name of the attribute to add
   * @param attrValue value of the attribute to add */
  private void addAttribute(String attrName, String attrValue)
  {
    if(attrName == null)
      return;
    
    _xml.append(' ');
    _xml.append(attrName).append("=\"");
    _xml.append(XMLUtils.xmlEncode(attrValue)).append('"');
  }

  /**
   * Adds the attributes. All the properties will be used as attributes
   *
   * @param attributes the attributes to add */
  private void addAttributes(Map attributes)
  {
    if(attributes == null)
      return;

    Iterator iter = attributes.keySet().iterator();
    while(iter.hasNext())
    {
      String attrName = (String) iter.next();
      Object attrValue = attributes.get(attrName);
      if(attrValue != null)
        addAttribute(attrName, attrValue.toString());
    }
  }
}
