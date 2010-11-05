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


package org.linkedin.util.io.resource;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * @author ypujante@linkedin.com
 *
 */
public class TestNullResource extends TestCase
{
  /**
   * Constructor
   */
  public TestNullResource(String name)
  {
    super(name);
  }

  public void testNullResource() throws IOException
  {
    NullResource r = NullResource.createFromRoot("/a/b");

    checkNullResource(r);
    assertEquals("/", r.getPath());
    assertEquals("", r.getFilename());
    assertEquals("nullResource:/a/b", r.toURI().toString());

    r = (NullResource) r.createRelative("/c");
    checkNullResource(r);
    assertEquals("/c", r.getPath());
    assertEquals("c", r.getFilename());
    assertEquals("nullResource:/a/b/c", r.toURI().toString());

    r = (NullResource) r.createRelative("/d");
    checkNullResource(r);
    assertEquals("/c/d", r.getPath());
    assertEquals("d", r.getFilename());
    assertEquals("nullResource:/a/b/c/d", r.toURI().toString());

    r = (NullResource) r.getParentResource();
    checkNullResource(r);
    assertEquals("/c", r.getPath());
    assertEquals("c", r.getFilename());
    assertEquals("nullResource:/a/b/c", r.toURI().toString());
  }

  private void checkNullResource(Resource r) throws IOException
  {
    assertFalse(r.exists());
    assertFalse(r.isDirectory());
    assertEquals(0L, r.lastModified());
    assertEquals(0L, r.length());
    assertNull(r.list());

    try
    {
      r.getInfo();
      fail("should fail with excpetion");
    }
    catch(IOException e)
    {
      // ok
    }
    try
    {
      r.getInputStream();
      fail("should fail with excpetion");
    }
    catch(IOException e)
    {
      // ok
    }
    try
    {
      r.getFile();
      fail("should fail with excpetion");
    }
    catch(IOException e)
    {
      // ok
    }
  }
}
