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

import junit.framework.TestCase;

/**
 * Test the functionalities of URLBuilder
 */
public class TestURLBuilder extends TestCase
{

  /**
   * Even though hostnames with underscores aren't technically valid, some older systems
   * seem to allow it.
   */
  public void testCreateFromURLWithUnderscoreInHostname()
  {
    String url = "http://jim_stoll.home.comcast.net";
    try
    {
      URLBuilder b = URLBuilder.createFromURL(url);
      assertEquals("same url", url, b.toString());
    }
    catch (Exception e)
    {
      fail("createFromURL() threw an exception: " + e.getMessage());
    }
  }

  /**
   * Test with a simple URL
   */
  public void testCreateFromSimpleURL()
  {
    String url = "http://www.google.com/";
    try
    {
      URLBuilder b = URLBuilder.createFromURL(url);
      assertEquals("same url", url, b.toString());
    }
    catch (Exception e)
    {
      fail("createFromURL() threw an exception: " + e.getMessage());
    }
  }

  /**
   * Test with a simple URL with fragment
   */
  public void testCreateFromSimpleURLWithFragment()
  {
    String url = "http://en.wikipedia.org/wiki/Domain_name#Examples";
    try
    {
      URLBuilder b = URLBuilder.createFromURL(url);
      assertEquals("same url", url, b.toString());
    }
    catch (Exception e)
    {
      fail("createFromURL() threw an exception: " + e.getMessage());
    }
  }

  public void testOpaqueURIFails()
  {
    String uri = "news:comp.lang.java";
    try
    {
      URLBuilder b = URLBuilder.createFromURL(uri);
      assertEquals("same uri", uri, b.toString());
    }
    catch (Exception e)
    {
      return;
    }
    fail("No exception thrown");
  }
  
  public void testParameterRemoval()
  {
    String url = "http://en.wikipedia.org/wiki/Domain_name#Examples";
    try
    {
      URLBuilder b = URLBuilder.createFromURL(url);
      assertEquals("same url", url, b.toString());

      b.addQueryParameter("p1");
      assertEquals("same url", "http://en.wikipedia.org/wiki/Domain_name?p1=#Examples", b.toString());

      b.addQueryParameter("p2", true);
      assertEquals("same url", "http://en.wikipedia.org/wiki/Domain_name?p1=&p2=p2#Examples", b.toString());

      b.addQueryParameter("p3", "tada");
      assertEquals("same url", "http://en.wikipedia.org/wiki/Domain_name?p1=&p2=p2&p3=tada#Examples", b.toString());

      b.addQueryParameter("p4", "yahoo");
      assertEquals("same url", "http://en.wikipedia.org/wiki/Domain_name?p1=&p2=p2&p3=tada&p4=yahoo#Examples", b.toString());

      b.removeQueryParameters("p2", "p3");
      assertEquals("same url", "http://en.wikipedia.org/wiki/Domain_name?p1=&p4=yahoo#Examples", b.toString());

      b.removeQueryParameter("pUnknown");
      assertEquals("same url", "http://en.wikipedia.org/wiki/Domain_name?p1=&p4=yahoo#Examples", b.toString());

      b.removeQueryParameters("p1", "pUnknown");
      assertEquals("same url", "http://en.wikipedia.org/wiki/Domain_name?p4=yahoo#Examples", b.toString());
    }
    catch (Exception e)
    {
      fail("createFromURL() threw an exception: " + e.getMessage());
    }
  }

  public void testParameterReplacement()
  {
    String url = "http://en.wikipedia.org/wiki/Domain_name#Examples";
    try
    {
      URLBuilder b = URLBuilder.createFromURL(url);
      assertEquals("same url", url, b.toString());

      b.addQueryParameter("p0");
      assertEquals("same url", "http://en.wikipedia.org/wiki/Domain_name?p0=#Examples", b.toString());

      b.addQueryParameter("p1");
      assertEquals("same url", "http://en.wikipedia.org/wiki/Domain_name?p0=&p1=#Examples", b.toString());

      b.addQueryParameter("p1", true);
      assertEquals("same url", "http://en.wikipedia.org/wiki/Domain_name?p0=&p1=&p1=p1#Examples", b.toString());

      b.addQueryParameter("p1", "tada");
      assertEquals("same url", "http://en.wikipedia.org/wiki/Domain_name?p0=&p1=&p1=p1&p1=tada#Examples", b.toString());

      String[] v = b.replaceQueryParameter("p1", "yahoo");
      assertEquals("same url", "http://en.wikipedia.org/wiki/Domain_name?p0=&p1=yahoo#Examples", b.toString());
      assertEquals(3, v.length);
      assertEquals("", v[0]);
      assertEquals("p1", v[1]);
      assertEquals("tada", v[2]);
    }
    catch (Exception e)
    {
      fail("createFromURL() threw an exception: " + e.getMessage());
    }
  }
}
