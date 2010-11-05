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

import org.linkedin.util.text.Indent;

import java.util.Map;

/**
 * This class is a utility class to write an xml properly indented very
 * conveniently. It decorates the XML class to add indentation/new lines
 *
 * @author ypujante@linkedin.com
 * @see XMLBuilder
 * @see Indent */
public class XMLIndent extends AbstractXMLBuilder
{
  /**
   * The indent to use */
  private final Indent _indent;

  /**
   * Contains the xml document as it is generated */
  private final XML _xml = new XML();

  /**
   * Constructor */
  public XMLIndent()
  {
    this(new Indent());
  }

  /**
   * Constructor
   *
   * @param indent you can specify which indentation to use */
  public XMLIndent(Indent indent)
  {
    _indent = indent;
  }

  /**
   * Adds the XML declaration (<code>&lt;?xml version="1.0" ?&gt;</code>)
   *
   * @param versionInfo the version info */
  @Override
  public void addXMLDecl(String versionInfo)
  {
    _xml.addXMLDecl(versionInfo);
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
    _xml.addXMLDecl(versionInfo, encoding, standalone);
  }

  /**
   * Adds an opening tag
   *
   * @param tagName the name of the opening tag to add */
  @Override
  public void addOpeningTag(String tagName)
  {
    _xml.addXML(_indent.toString());
    _xml.addOpeningTag(tagName);
    _xml.addXML("\n");
    _indent.inc();
  }

  /**
   * Adds a closing tag
   *
   * @param tagName the name of the closing tag to add */
  @Override
  public void addClosingTag(String tagName)
  {
    _indent.dec();
    _xml.addXML(_indent.toString());
    _xml.addClosingTag(tagName);
    _xml.addXML("\n");
  }

  /**
   * Adds an empty tag
   *
   * @param tagName the name of the empty tag to add */
  @Override
  public void addEmptyTag(String tagName)
  {
    _xml.addXML(_indent.toString());
    _xml.addEmptyTag(tagName);
    _xml.addXML("\n");
  }

  /**
   * Adds a tag
   *
   * @param tagName the name of the tag to add
   * @param tagValue the value of the tag to add */
  @Override
  public void addTag(String tagName, String tagValue)
  {
    _xml.addXML(_indent.toString());
    _xml.addTag(tagName, tagValue);
    _xml.addXML("\n");
  }

  /**
   * Adds the given block of XML. If it is not a properly formatter block
   * of XML then the output will not be properly formatted.
   *
   * @param xml the block of XML to add */
  @Override
  public void addXML(String xml)
  {
    _xml.addXML(xml);
  }

  /**
   * Returns the xml generated */
  @Override
  public String getXML()
  {
    return _xml.getXML();
  }

  /**
   * Call this method when you want to reuse the visitor */
  @Override
  public void reset()
  {
    _xml.reset();
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
    _xml.addXML(_indent.toString());
    _xml.addEmptyTag(tagName, attributes);
    _xml.addXML("\n");
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
    _xml.addXML(_indent.toString());
    _xml.addEmptyTag(tagName, attrName, attrValue);
    _xml.addXML("\n");
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
    _xml.addXML(_indent.toString());
    _xml.addOpeningTag(tagName, attributes);
    _xml.addXML("\n");
    _indent.inc();
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
    _xml.addXML(_indent.toString());
    _xml.addOpeningTag(tagName, attrName, attrValue);
    _xml.addXML("\n");
    _indent.inc();
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
    _xml.addXML(_indent.toString());
    _xml.addTag(tagName, tagValue, attributes);
    _xml.addXML("\n");
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
    _xml.addXML(_indent.toString());
    _xml.addTag(tagName, tagValue, attrName, attrValue);
    _xml.addXML("\n");
  }

  public String toString()
  {
    return getXML();
  }
}
