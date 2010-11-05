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

import org.linkedin.util.collections.CollectionsUtils;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.EnumSet;

/**
 * This class represents memory size in terms of size and unit. 
 * The size units supported range from bytes to tera bytes. Size 
 * can be positive or negative. Instances of this class are immutable 
 * and thread safe
 *   
 * @author qsu
 *
 */
public class MemorySize implements Comparable<MemorySize>, Serializable
{
  private static final long serialVersionUID = 1L;
  public enum SizeUnit
  {
    // IMPORTANT: the order of enums below needs to be preserved
    // as from smallest to largest unit
    BYTE(1L, ""), 
    KILO_BYTE(1024L * BYTE.getBytesCount(), "k"), 
    MEGA_BYTE(1024L * KILO_BYTE.getBytesCount(), "m"), 
    GIGA_BYTE(1024L * MEGA_BYTE.getBytesCount(), "g"), 
    TERA_BYTE(1024L * GIGA_BYTE.getBytesCount(), "t");

    private final long   _bytesCount;
    private final String _displayChar;

    /**
     * Private constructor to ensure singleton for each enum type 
     * 
     * @param bytesCount
     * @param displayChar
     */
    private SizeUnit(long bytesCount, String displayChar)
    {
      _bytesCount = bytesCount;
      _displayChar = displayChar;
    }

    public long getBytesCount()
    {
      return _bytesCount;
    }

    public String getDisplayChar()
    {
      return _displayChar;
    }
  }

  public final static MemorySize                     ZERO_BYTES      = new MemorySize(0,
                                                                                      SizeUnit.BYTE);
  public final static MemorySize                     ZERO_KILO_BYTES = new MemorySize(0,
                                                                                      SizeUnit.KILO_BYTE);
  public final static MemorySize                     ZERO_MEGA_BYTES = new MemorySize(0,
                                                                                      SizeUnit.MEGA_BYTE);
  public final static MemorySize                     ZERO_GIGA_BYTES = new MemorySize(0,
                                                                                      SizeUnit.GIGA_BYTE);
  public final static MemorySize                     ZERO_TERA_BYTES = new MemorySize(0,
                                                                                      SizeUnit.TERA_BYTE);

  /** a complete list of size units ordered descendingly */
  private final static SizeUnit[]                    ORDERED_SIZE_UNIT = CollectionsUtils.reverse(SizeUnit.values());
  
  private final static EnumMap<SizeUnit, MemorySize> ZERO_SIZES      = new EnumMap<SizeUnit, MemorySize>(SizeUnit.class);

  static
  {
    ZERO_SIZES.put(SizeUnit.BYTE, ZERO_BYTES);
    ZERO_SIZES.put(SizeUnit.KILO_BYTE, ZERO_KILO_BYTES);
    ZERO_SIZES.put(SizeUnit.MEGA_BYTE, ZERO_MEGA_BYTES);
    ZERO_SIZES.put(SizeUnit.GIGA_BYTE, ZERO_GIGA_BYTES);
    ZERO_SIZES.put(SizeUnit.TERA_BYTE, ZERO_TERA_BYTES);
  }

  private final long _size;
  private final SizeUnit _sizeUnit;

  /**
   * Constructor
   * 
   * @param sizeInBytes
   */
  public MemorySize(long sizeInBytes)
  {
    this(sizeInBytes, SizeUnit.BYTE);
  }

  /**
   * Constructor
   * 
   * @param size
   * @param sizeUnit
   */
  public MemorySize(long size, SizeUnit sizeUnit)
  {
    _size = size;
    _sizeUnit = sizeUnit;
  }

  public long getSize()
  {
    return _size;
  }

  public SizeUnit getSizeUnit()
  {
    return _sizeUnit;
  }

  /**
   * @return size in bytes
   */
  public long getSizeInBytes()
  {
    return getSize() * getSizeUnit().getBytesCount();
  }

  /**
   * @return size in kilo bytes, the remaining size if any istruncated
   */
  public long getSizeInKiloBytes()
  {
    return getSizeInBytes() / SizeUnit.KILO_BYTE.getBytesCount();
  }

  /**
   * @return size in mega bytes, the remaining size if any istruncated
   */
  public long getSizeInMegaBytes()
  {
    return getSizeInBytes() / SizeUnit.MEGA_BYTE.getBytesCount();
  }

  /**
   * @return size in giga bytes, the remaining size if any istruncated
   */
  public long getSizeInGigaBytes()
  {
    return getSizeInBytes() / SizeUnit.GIGA_BYTE.getBytesCount();
  }

  /**
   * @return size in tera bytes, the remaining size if any istruncated
   */
  public long getSizeInTeraBytes()
  {
    return getSizeInBytes() / SizeUnit.TERA_BYTE.getBytesCount();
  }

  /**
   * Return size in the given unit to floating point precision, useful when 
   * truncating behavior is not desired
   * 
   * @param sizeUnit
   * @return size in the given unit to floating point precision
   */
  public double getFractionalSize(SizeUnit sizeUnit)
  {
    return (double) getSizeInBytes() / sizeUnit.getBytesCount();
  }

  /**
   * Returns this memory size as a fractional size representation using the biggest unit size
   * possible. ex: 1.23g
   *
   * @return a string representing a this memory size as a fraction using the biggest size unit
   * possible
   */
  public String getFractionalSizeAsString()
  {
    if(_size == 0)
      return "0";

    long sizeInBytes = getSizeInBytes();

    // determine the biggest size unit with non 0 size
    for(SizeUnit sizeUnit : ORDERED_SIZE_UNIT)
    {
      if(sizeUnit == SizeUnit.BYTE)
        return String.valueOf(sizeInBytes);
      
      if(sizeInBytes >= sizeUnit.getBytesCount())
      {
        double fractionalSize = (double) sizeInBytes / sizeUnit.getBytesCount();
        return String.format("%.2f%s", fractionalSize, sizeUnit.getDisplayChar());
      }
    }

    throw new RuntimeException("should not reach this line...");
  }

  /**
   * Return a new instance of MemorySize that is truncated to the given unit
   * 
   * @param sizeUnit
   * @return a new instance of MemorySize that is truncated to the given unit
   */
  public MemorySize truncate(SizeUnit sizeUnit)
  {
    if (getSizeUnit() == sizeUnit)
      return this;

    long sizeInBytes = getSizeInBytes();

    if (sizeInBytes >= sizeUnit.getBytesCount())
    {
      return new MemorySize(sizeInBytes / sizeUnit.getBytesCount(), sizeUnit);
    }
    else
    {
      return ZERO_SIZES.get(sizeUnit);
    }
  }
  
  /**
   * Return a new instance of MemorySize that adds up the size of this object
   * with that of the 'other'
   * 
   * @param other
   * @return a new instance of MemorySize that adds up the size of this object
   * with the that of the 'other'
   */
  public MemorySize add(MemorySize other)
  {
    if(other == null)
      throw new NullPointerException();
    
    if(getSizeUnit() == other.getSizeUnit())
      return new MemorySize(getSize() + other.getSize(), getSizeUnit());

    return new MemorySize(getSizeInBytes() + other.getSizeInBytes(),
                        SizeUnit.BYTE);
  }
  
  /**
   * Return a new instance of MemorySize that subtract from this object 
   * the size of the other object
   * 
   * @param other
   * @return a new instance of MemorySize that subtract from this object 
   * the size of the other object
   */
  public MemorySize subtract(MemorySize other)
  {
    if(other == null)
      throw new NullPointerException();
    
    if(getSizeUnit() == other.getSizeUnit())
      return new MemorySize(getSize() - other.getSize(), getSizeUnit());

    return new MemorySize(getSizeInBytes() - other.getSizeInBytes(),
                        SizeUnit.BYTE);
  }
  
  /**
   * Return a string representation of this instance using the given units
   * E.g. 2g3m4k given units g/m -> "2g3m"
   *      2g3m4k given units g/k -> "2g3076k"
   *        3m4k given units t/g -> "0g"
   *        2g3m given units k/b -> "2100224k"
   * 
   * @param sizeUnits the sizeUnits you want in the decomposition
   * @return a string representation of this instance using the given units.
   */
  public String getAsString(SizeUnit...sizeUnits)
  {
    if(sizeUnits == null || sizeUnits.length == 0)
      return toString();
    
    return getAsString(CollectionsUtils.toEnumSet(SizeUnit.class, sizeUnits));
  }
  
  /**
   * Return a string representation of this instance using the given units
   * E.g. 2g3m4k given units g/m -> "2g3m"
   *      2g3m4k given units g/k -> "2g3076k"
   *        3m4k given units t/g -> "0g"
   *        2g3m given units k/b -> "2100224k"
   * 
   * @param sizeUnits the sizeUnits you want in the decomposition
   * @return a string representation of this instance using the given units.
   */
  public String getAsString(EnumSet<SizeUnit> sizeUnits)
  {
    if(sizeUnits == null || sizeUnits.size() == 0)
      return toString();
    
    StringBuilder sb = new StringBuilder();
    MemorySize ms = this;
    MemorySize prevMs;

    // IMPORTANT: 
    // according to javadoc for EnumSet:
    // "The iterator returned by the iteratormethod traverses the elements in their natural order 
    // (the order in which the enum constants are declared)" and the toArray() method of AbstractCollection
    // guarantees such an order
    Object[] unitsAsArr = sizeUnits.toArray();

    int len = unitsAsArr.length;
    for (int i = len - 1; i > -1; i--)
    {
      prevMs = ms;
      ms = ms.truncate((SizeUnit)unitsAsArr[i]);
      if (ms.getSize() > 0)
      {
        sb.append(ms.getSize()).append(ms.getSizeUnit().getDisplayChar());
      }
      ms = prevMs.subtract(ms);
    }

    if(sb.length() == 0)
    {
      SizeUnit smallestUnit = (SizeUnit)unitsAsArr[0];
      sb.append("0").append(smallestUnit.getDisplayChar());
    }
      
    return sb.toString();
  }
  
  /**
   * Return a canonical representation of size where:
   * byte is &lt; 1024, kilo byte is &lt; 1024, mega byte is &lt; 1024,
   * giga byte is &lt; 1024, tera byte is &lt; 1024. 
   * 
   * Unless it is representing the smallest unit, 0 size of a unit is not displayed
   * 
   * @return canonical representation of size   
   */
  public String getCanonicalString()
  {
    return getAsString(ORDERED_SIZE_UNIT);
  }
  
  /**
   * Return a canonical representation of size
   * see {@link #getCanonicalString()}
   * 
   * @return canonical representation of size
   */
  @Override
  public String toString()
  {
    return getFractionalSizeAsString();
  }

  /**
   * Compare with another instance of MemorySize based on size in bytes
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(MemorySize memorySize)
  {
    // according to javadoc for interface Comparable<T>, a NullPointerException
    // should be thrown when the object to be compared with is null
    // http://java.sun.com/j2se/1.5.0/docs/api/java/lang/Comparable.html#compareTo(T)
    if (memorySize == null)
      throw new NullPointerException();

    if (getSizeUnit() == memorySize.getSizeUnit())
      return LangUtils.compare(getSize(), memorySize.getSize());

    return LangUtils.compare(getSizeInBytes(), memorySize.getSizeInBytes());
  }

  /**
   * Override java.lang.Object#equals(java.lang.Object)
   * 
   * @return true if o is of class MemorySize and their size in bytes
   * are the same. Consistent with compareTo() 
   */
  @Override
  public boolean equals(Object o)
  {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    return compareTo((MemorySize) o) == 0;
  }

  /**
   * Override java.lang.Object#hashCode()
   * 
   * @return hashCode of this object based on its size in byte
   */
  @Override
  public int hashCode()
  {
    long size = getSizeInBytes();
    return (int) (size ^ (size >>> 32));
  }
  
  /**
   * Create a new instance of MemorySize by adding up the given
   * array of memorySizes
   * 
   * @param memorySizes
   * @return a new instance of MemorySize
   */
  public static MemorySize create(MemorySize...memorySizes)
  {
    if(memorySizes == null)
      return null;

    if(memorySizes.length == 0)
      return ZERO_BYTES;

    MemorySize res = memorySizes[0];

    for(int i = 1; i < memorySizes.length; i++)
    {
      MemorySize memorySize = memorySizes[i];
      if(memorySize != null)
        res = res.add(memorySize);
    }

    return res;
  }

  /**
   * Synonym...
   * 
   * @param memorySizeAsStr
   * @return a new instance of MemorySize
   */
  public static MemorySize parse(String memorySizeAsStr)
  {
    return parseMemorySize(memorySizeAsStr);
  }

  /**
   * Create a new instance of MemorySize by parsing the given string
   * of format e.g. 2g3m4k5
   * 
   * @param memorySizeAsStr
   * @return a new instance of MemorySize 
   */
  public static MemorySize parseMemorySize(String memorySizeAsStr)
  {
    if(memorySizeAsStr == null || memorySizeAsStr.length() == 0)
      return null;

    int len = memorySizeAsStr.length();

    int orderedSizeUnitIdx = 0;
    int orderedSizeUnitLen = ORDERED_SIZE_UNIT.length;

    int startDigitsIdx = 0;
    boolean expectingDigits = true;

    MemorySize ms = ZERO_BYTES;
    for(int i = 0; i < len; i++)
    {
      char c = memorySizeAsStr.charAt(i);
      if(c >= '0' && c <= '9')
      {
        expectingDigits = false;
        continue;
      }

      if(expectingDigits)
        throw new IllegalArgumentException("Unable to parse '" + memorySizeAsStr + "': found '" + c
            + "' at pos " + i + ", was expecting a digit");

      for(; orderedSizeUnitIdx < orderedSizeUnitLen; orderedSizeUnitIdx++)
      {
        SizeUnit sizeUnit = ORDERED_SIZE_UNIT[orderedSizeUnitIdx];
        String displayChar = sizeUnit.getDisplayChar();
        
        if(displayChar.length() > 0 && c == displayChar.charAt(0))
        {
          try
          {
            long size = Long.parseLong(memorySizeAsStr.substring(startDigitsIdx, i));
            ms = ms.add(new MemorySize(size, sizeUnit));
            orderedSizeUnitIdx++;
            startDigitsIdx = i + 1;
            expectingDigits = true;
            break;
          }
          catch(NumberFormatException e)
          {
            throw new IllegalArgumentException("Unable to parse '" + memorySizeAsStr + "'", e);
          }
        }
      }
      
      if(orderedSizeUnitIdx == orderedSizeUnitLen)
        throw new IllegalArgumentException("Unable to parse '" + memorySizeAsStr
            + "': found invalid character '" + c + "' at pos " + i);
    }

    if(startDigitsIdx < len)
    {
      try
      {
        long size = Long.parseLong(memorySizeAsStr.substring(startDigitsIdx, len));
        ms = ms.add(new MemorySize(size, SizeUnit.BYTE));
      }
      catch(NumberFormatException e)
      {
        throw new IllegalArgumentException("Unable to parse '" + memorySizeAsStr + "'", e);
      }
    }
    return ms;
  }
}
