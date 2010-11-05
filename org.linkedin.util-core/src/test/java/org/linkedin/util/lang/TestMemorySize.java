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

package org.linkedin.util.lang;

import junit.framework.TestCase;

import java.util.EnumSet;

public class TestMemorySize extends TestCase
{
  /**
   * Constructor
   */
  public TestMemorySize(String name)
  {
    super(name);
  }
  
  public void testParseMemorySize()
  {
    long expectedSizeInBytes = (long)(12 * Math.pow(2, 30) + 3 * Math.pow(2, 20) + 4 * Math.pow(2, 10) + 5);
    long sizeInBytes = MemorySize.parseMemorySize("12g3m4k5").getSizeInBytes();
    assertTrue("expected size in bytes: " + expectedSizeInBytes + "but was: " + sizeInBytes,  sizeInBytes == expectedSizeInBytes);
    
    expectedSizeInBytes = (long)(2 * Math.pow(2, 30) + 3 * Math.pow(2, 20) + 5);
    sizeInBytes = MemorySize.parseMemorySize("2g3m5").getSizeInBytes();
    assertTrue("expected size in bytes: " + expectedSizeInBytes + "but was: " + sizeInBytes,  sizeInBytes == expectedSizeInBytes);
    
    expectedSizeInBytes = (long)(2 * Math.pow(2, 30) + 4 * Math.pow(2, 10));
    sizeInBytes = MemorySize.parseMemorySize("2g4k").getSizeInBytes();
    assertTrue("expected size in bytes: " + expectedSizeInBytes + "but was: " + sizeInBytes,  sizeInBytes == expectedSizeInBytes);
    
    expectedSizeInBytes = (long)(5);
    sizeInBytes = MemorySize.parseMemorySize("5").getSizeInBytes();
    assertTrue("expected size in bytes: " + expectedSizeInBytes + "but was: " + sizeInBytes,  sizeInBytes == expectedSizeInBytes);
    
    try{
      MemorySize.parseMemorySize("3d");
      assertFalse(true);
    }
    catch(Exception e)
    {
      
    }
    
    try{
      MemorySize.parseMemorySize("3t5.2");
      assertFalse(true);
    }
    catch(Exception e)
    {
      
    }

    try
    {
      MemorySize.parseMemorySize("25%");
      fail("Should have thrown an IllegalArgumentException");
    }
    catch (IllegalArgumentException e)
    {
      // Expected case
    }
  }
  
  public void testGetAsString()
  {
    MemorySize memorySize = MemorySize.parseMemorySize("2g3m4k");
    
    EnumSet<MemorySize.SizeUnit> res = EnumSet.noneOf(MemorySize.SizeUnit.class);
    res.add(MemorySize.SizeUnit.MEGA_BYTE);
    res.add(MemorySize.SizeUnit.GIGA_BYTE);
    
    
    assertEquals("2g3m", memorySize.getAsString(res));
    
    res.clear();
    res.add(MemorySize.SizeUnit.KILO_BYTE);
    res.add(MemorySize.SizeUnit.GIGA_BYTE);
    
    assertEquals("2g3076k", memorySize.getAsString(res));
    
    memorySize = MemorySize.parseMemorySize("3m4k");
    
    res.clear();
    res.add(MemorySize.SizeUnit.TERA_BYTE);
    res.add(MemorySize.SizeUnit.GIGA_BYTE);
    
    assertEquals("0g", memorySize.getAsString(res));
    
    memorySize = MemorySize.parseMemorySize("2g3m");
    
    res.clear();
    res.add(MemorySize.SizeUnit.KILO_BYTE);
    res.add(MemorySize.SizeUnit.BYTE);
    
    assertEquals("2100224k", memorySize.getAsString(res));
  }
  
  public void testGetCanonicalString()
  {
    MemorySize memorySize = MemorySize.parseMemorySize("2g3076k");
    
    assertEquals("2g3m4k", memorySize.getCanonicalString());
    
    memorySize = MemorySize.parseMemorySize("1030");
    
    assertEquals("1k6", memorySize.getCanonicalString());
  }

  public void testGetFractionalSizeAsString()
  {
    assertEquals("0", MemorySize.parseMemorySize("0g").getFractionalSizeAsString());
    assertEquals("2.00g", MemorySize.parseMemorySize("2g").getFractionalSizeAsString());
    assertEquals("2.25g", MemorySize.parseMemorySize("2g256m").getFractionalSizeAsString());
    assertEquals("2.25g", MemorySize.parseMemorySize("2304m").getFractionalSizeAsString());
    assertEquals("2.25g", MemorySize.parseMemorySize("2g261m").getFractionalSizeAsString()); // 261 / 1024 = 0.2548... < 0.255
    assertEquals("2.26g", MemorySize.parseMemorySize("2g262m").getFractionalSizeAsString()); // 262 / 1024 = 0.2558... > 0.255
    assertEquals("2.50g", MemorySize.parseMemorySize("2g512m").getFractionalSizeAsString());
    assertEquals("100", MemorySize.parseMemorySize("100").getFractionalSizeAsString());
  }
}
