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

import junit.framework.TestCase;

public class TestXMLUtils extends TestCase
{
  public void testNull() throws Exception
  {
    testCodec(null, null, null);
    // shouldn't blow up
  }

  public void testEncodeASCII_doNothing()
  {
    String strToEncode = "hello, @world!!! This call changes nothing on this string;";
    testCodec(strToEncode, strToEncode, strToEncode);
  }

  public void testEncodeControlChars() throws Exception
  {
    testCodec("preserves newlines\r\n\t but these should be replaced by 'sp' :   ",
              "preserves newlines\r\n\t but these should be replaced by 'sp' :   ",
              "preserves newlines\r\n\t but these should be replaced by 'sp' :\f\u0000\u0004",
              false); // control chars are lost forever, so no point testing decode
  }

  public void testEncodeXMLChars() throws Exception
  {
    testCodec("&lt;mytag&gt;A &amp; B +&quot;C&quot;* &amp;lt;&lt;/mytag&gt;",
              "&lt;mytag&gt;A &amp; B +&quot;C&quot;* &amp;lt;&lt;/mytag&gt;",
              "<mytag>A & B +\"C\"* &lt;</mytag>");
  }

  public void testEncodeNonAsciiChars() throws Exception
  {
    testCodec("can you read this ? &quot;\u4eba\u4e4b\u521d\uff0c\u6027\u672c\u5584&quot;",
              "can you read this ? &quot;&#x4eba;&#x4e4b;&#x521d;&#xff0c;&#x6027;&#x672c;&#x5584;&quot;",
              "can you read this ? \"\u4eba\u4e4b\u521d\uff0c\u6027\u672c\u5584\"");
  }

  public void testEncodeValidSurrogatePairs() throws Exception
  {
    testCodec("These are 2 old persian chars: &#x103b0;&#x103b8;\n",
              "These are 2 old persian chars: &#x103b0;&#x103b8;\n",
              "These are 2 old persian chars: \ud800\udfb0\ud800\udfb8\n");
  }

  public void testEncodeHighSurrogateOnly() throws Exception
  {
    testCodec("This character  alone is not a valid character, should be omitted.",
              "This character  alone is not a valid character, should be omitted.",
              "This character \ud801 alone is not a valid character, should be omitted.",
              false);

    testCodec("High surrogate followed by non-surrogate (\u1122) should keep only the latter.",
              "High surrogate followed by non-surrogate (&#x1122;) should keep only the latter.",
              "High surrogate followed by non-surrogate (\ud810\u1122) should keep only the latter.",
              false);

    testCodec("unpaired high at end of str: ",
              "unpaired high at end of str: ",
              "unpaired high at end of str: \ud901",
              false);
  }

  public void testEncodeLowSurrogateOnly() throws Exception
  {
    testCodec("This character  alone is not a valid character, should be omitted.",
              "This character  alone is not a valid character, should be omitted.",
              "This character \uddff alone is not a valid character, should be omitted.",
              false);

    testCodec("wrong order, omit both: ",
              "wrong order, omit both: ",
              "wrong order, omit both: \udcff\ud910",
              false);
  }

  public void testDecodeCharacterReference() throws Exception
  {
    String expectedLt128 = "hello 'W'";
    String expectedGt127 = "hello '\u01a9'";
    String expectedSurrogatePairs = "hello '\ud800\udf33'";

    assertEquals(expectedLt128, XMLUtils.xmlDecode("hello '&#87;'"));
    assertEquals(expectedLt128, XMLUtils.xmlDecode("hello '&#x57;'"));

    assertEquals(expectedGt127, XMLUtils.xmlDecode("hello '&#425;'"));
    assertEquals(expectedGt127, XMLUtils.xmlDecode("hello '&#x1a9;'"));

    assertEquals(expectedSurrogatePairs, XMLUtils.xmlDecode("hello '&#66355;'"));
    assertEquals(expectedSurrogatePairs, XMLUtils.xmlDecode("hello '&#x10333;'"));
  }

  public void testDecodeCharacterReferenceException() throws Exception
  {
    try
    {
      XMLUtils.xmlDecode("bad entity : &#x-1234;");
      fail("should not be able to decode &#x-1234;");
    }
    catch (IllegalArgumentException x)
    {
    }

    try
    {
      XMLUtils.xmlDecode("bad entity : &#x10h1;");
      fail("should not be able to decode &#10h1;");
    }
    catch (IllegalArgumentException x)
    {
    }
  }

  private void testCodec(String expected,
                         String expectedRaw,
                         String input)
  {
    testCodec(expected, expectedRaw, input, true);
  }

  private void testCodec(String expected,
                         String expectedRaw,
                         String input,
                         boolean decode)
  {
    assertEquals(expected,    XMLUtils.xmlEncode(input));
    assertEquals(expectedRaw, XMLUtils.xmlEncodeRaw(input));
    if (decode)
    {
      assertEquals(input, XMLUtils.xmlDecode(XMLUtils.xmlEncode(input)));
      assertEquals(input, XMLUtils.xmlDecode(XMLUtils.xmlEncodeRaw(input)));
    }
  }
}