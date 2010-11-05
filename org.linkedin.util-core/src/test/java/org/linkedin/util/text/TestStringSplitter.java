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

package org.linkedin.util.text;

import junit.framework.TestCase;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 *
 * @author  ypujante@linkedin.com */
public class TestStringSplitter extends TestCase
{
  private final static String S1 = "param1=value1&param2=value2=value3=value4";
  private final static String S2 =
    "param1=value1&param2='value2=value3=value4'";
  private final static String S3 =
    "param1=value1&param2=<|>value2=value3=value4<|>";

  private final static String[] EMPTY_STRING_ARRAY = new String[0];

  /**
   * Test of the string splitter */
  public void testStringSplitter()
  {
    StringSplitter ss1 = new StringSplitter('=');
    StringSplitter ss2 = new StringSplitter('=', '\'');
    StringSplitter ss3 = new StringSplitter('=', "<|>");


    // use ss1
    checkResult(ss1.split(null), null);

    checkResult(ss1.split(""), EMPTY_STRING_ARRAY);

    checkResult(ss1.split("="), new String[] { "", "" });

    checkResult(ss1.split(S1), new String[] { "param1", "value1&param2",
                                              "value2", "value3",
                                              "value4" });

    checkResult(ss1.split(S2), new String[] { "param1", "value1&param2",
                                              "'value2", "value3",
                                              "value4'" });

    checkResult(ss1.split("'='"), new String[] { "'", "'" });

    // use ss2
    checkResult(ss2.split(null), null);

    checkResult(ss2.split(""), EMPTY_STRING_ARRAY);

    checkResult(ss2.split("="), new String[] { "", "" });

    checkResult(ss2.split(S1), new String[] { "param1", "value1&param2",
                                              "value2", "value3",
                                              "value4" });

    checkResult(ss2.split(S2), new String[] { "param1", "value1&param2",
                                              "'value2=value3=value4'" });

    checkResult(ss2.split("'='"), new String[] { "'='" });

    // use ss3
    checkResult(ss3.split(null), null);

    checkResult(ss3.split(""), EMPTY_STRING_ARRAY);

    checkResult(ss3.split("="), new String[] { "", "" });

    checkResult(ss3.split(S1), new String[] { "param1", "value1&param2",
                                              "value2", "value3",
                                              "value4" });

    checkResult(ss3.split(S2), new String[] { "param1", "value1&param2",
                                              "'value2", "value3",
                                              "value4'" });

    checkResult(ss3.split(S3), new String[] { "param1", "value1&param2",
                                              "<|>value2=value3=value4<|>" });

    checkResult(ss3.split("<|>=<|>"), new String[] { "<|>=<|>" });
  }

  /**
   * Compares the 2 arrays and throws appropriate exceptions */
  private void checkResult(String[] res, String[] expected)
  {
    if(expected == null)
    {
      assertEquals(res, null);
      return;
    }

    assertNotNull(res);

    assertEquals(expected.length, res.length);
    for(int i = 0; i < res.length; i++)
      assertEquals(expected[i], res[i]);
  }

  public void testStringSplitterIterator()
  {
    StringSplitter ss1 = new StringSplitter('=');

    checkResult(ss1.splitToIterator(null), null);

    checkResult(ss1.splitToIterator(""), EMPTY_STRING_ARRAY);

    checkResult(ss1.splitToIterator("="), new String[] { "", "" });

    checkResult(ss1.splitToIterator(S1), new String[] { "param1", "value1&param2",
                                                        "value2", "value3",
                                                        "value4" });

    checkResult(ss1.splitToIterator(S2), new String[] { "param1", "value1&param2",
                                                        "'value2", "value3",
                                                        "value4'" });

    checkResult(ss1.splitToIterator("'='"), new String[] { "'", "'" });
  }

  /**
   * Compares the 2 arrays and throws appropriate exceptions */
  private void checkResult(Iterator<String> iter, String[] expected)
  {
    if(expected == null)
    {
      assertEquals(iter, null);
      return;
    }

    assertNotNull(iter);

    int i = 0;
    while(iter.hasNext())
      assertEquals(expected[i++], iter.next());

    try
    {
      iter.next();
      fail("iterator should fail");
    }
    catch(NoSuchElementException ex)
    {
      // ok
    }
  }
}
