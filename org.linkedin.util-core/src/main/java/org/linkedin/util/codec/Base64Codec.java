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

package org.linkedin.util.codec;

import java.util.Random;

/**
 * This class will encode/decode array of bytes into ASCII charcters. It is
 * roughly equivalent to a base64 algorithm.
 *
 * @author ypujante@linkedin.com */
public class Base64Codec implements Codec
{
  
  /** static instance of base64 codec */
  public static final Base64Codec INSTANCE = new Base64Codec();
  
  /** number of bits in base64 digit */
  public static final int NUM_BITS_PER_DIGIT = 6;
  
  /**
   * The array for fast lookup for the radix */
  private char[] _radix = 
  {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
    'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
    't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
    'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
    'X', 'Y', 'Z', '-', '_'
  };

  /**
   * The array for reversing the radix */
  private int[] _reverseRadix;

  /**
   * The INT mask for the radix */
  private final static int INT_RADIX_MASK = 0x0000003F;


  /**
   * Constructor. Creates a basic codec object with no shuffling of the
   * characters */
  public Base64Codec()
  {
    init();
  }

  /**
   * Constructor. Shuffles the radix with the provided random object */
  public Base64Codec(Random rnd)
  {
    shuffle(_radix, rnd);
    init();
  }

  /**
   * Constructor. Shuffles the radix with the random object created from the provided password */
  public Base64Codec(String password)
  {
    this(CodecUtils.createRandom(password));
  }

  /**
   * Initializes the reverse radix array */
  private void init()
  {
    _reverseRadix = new int[128];
    // we only take into account characters in the range 0-127 which is
    // enough!
    for(int i = 0; i < _reverseRadix.length; i++)
      _reverseRadix[i] = -1;

    for(int i = 0; i < _radix.length; i++)
      _reverseRadix[(int) _radix[i]] = i;

    _reverseRadix['.'] = _reverseRadix['-'];
  }


  /**
   * Encode the array using the radix
   *
   * @param byteArray the array to encode
   * @return the encoded String */
  @Override
  public String encode(byte[] byteArray)
  {
    int l = (byteArray.length * 8) / 6;
    if(l % 4 != 0)
    {
      l++;
    }

    char [] conv = new char[l];

    int i = 0;
    int k = conv.length - 1;
    int b;
    int c;
    int d;
    

    while(i <= (byteArray.length - 3))
    {
      b = ((int) byteArray[i++]) & 0x000000FF;
      conv[k--] = _radix[b & INT_RADIX_MASK];

      b >>>= 6;
      d = ((int) byteArray[i++]) & 0x000000FF;
      c = (d << 2) & 0x0000003C;
      b |= c;
      conv[k--] = _radix[b];

      b = d >>> 4;
      d = ((int) byteArray[i++]) & 0x000000FF;
      c = (d << 4) & 0x00000030;
      b |= c;
      conv[k--] = _radix[b];

      b = d >>> 2;
      conv[k--] = _radix[b];
    }

    if(i == (byteArray.length - 2))
    {
      b = ((int) byteArray[i++]) & 0x000000FF;
      conv[k--] = _radix[b & INT_RADIX_MASK];

      b >>>= 6;
      d = ((int) byteArray[i]) & 0x000000FF;
      c = (d << 2) & 0x0000003C;
      b |= c;
      conv[k--] = _radix[b];

      b = d >>> 4;
      conv[k] = _radix[b];

      return new String(conv);
    }

    if(i == (byteArray.length - 1))
    {
      b = ((int) byteArray[i]) & 0x000000FF;
      conv[k--] = _radix[b & INT_RADIX_MASK];
      
      b >>>= 6;
      conv[k] = _radix[b];
    }

    return new String(conv);
  }

  /**
   * Decodes the array into a byte array using the radix.
   *
   * @param s the string to decode
   * @return the byte array decoded
   * @exception CannotDecodeException if the string has not been encoded
   * with this codec */
  @Override
  public byte[] decode(String s) throws CannotDecodeException
  {
    char[] array = s.toCharArray();
    int start = 0;
    int len = array.length - start;
    int l = (len * 6) / 8;
    boolean checkLast = false;
    if((l % 3) != 0)
    {
      l++;
      checkLast = true;
    }

    byte[] res = new byte[l];
    int i = 0;
    int k = array.length - 1;
    int b;
    int c;
    int d;

    try
    {
      while(k >= (start + 3))
      {
        b = _reverseRadix[(int) array[k--]];
        c = b;
        
        b = _reverseRadix[(int) array[k--]];
        d = (b << 6) & 0x000000C0;
        c |= d;
        res[i++] = (byte) c;
        
        c = b >>> 2;
        b = _reverseRadix[(int) array[k--]];
        d = (b << 4) & 0x000000F0;
        c |= d;
        res[i++] = (byte) c;
        
        c = b >>> 4;
        b = _reverseRadix[(int) array[k--]];
        d = (b << 2) & 0x000000FC;
        c |= d;
        res[i++] = (byte) c;
      }
      
      if(k == (start + 2))
      {
        b = _reverseRadix[(int) array[k--]];
        c = b;
        
        b = _reverseRadix[(int) array[k--]];
        d = (b << 6) & 0x000000C0;
        c |= d;
        res[i++] = (byte) c;
        
        c = b >>> 2;
        b = _reverseRadix[(int) array[k]];
        d = (b << 4) & 0x000000F0;
        c |= d;
        res[i] = (byte) c;
      }
      else
      {
        if(k == (start + 1))
        {
          b = _reverseRadix[(int) array[k--]];
          c = b;
          
          b = _reverseRadix[(int) array[k]];
          d = (b << 6) & 0x000000C0;
          c |= d;
          res[i++] = (byte) c;
          
          c = b >>> 2;
          res[i] = (byte) c;
        }
        else
        {
          if(k == start)
          {
            res[i] = (byte) _reverseRadix[(int) array[k]];
          }
        }
      }
      
      
      if(checkLast)
      {
        if(res[l - 1] == 0)
        {
          byte[] res2 = new byte[l - 1];
          System.arraycopy(res, 0, res2, 0, l - 1);
          res = res2;
        }
      }
    }
    catch(Exception ex)
    {
      throw new CannotDecodeException(s);
    }

    return res;
  }  

  /**
   * Shuffles the array using the random generator provided 
   *
   * @param array the array to shuffle
   * @param random the random number generator
   * @return the shuffled array */
  public static char[] shuffle(char[] array, Random random)
  {
    for(int i = array.length - 1; i > 2; --i)
    {
      int idx = random.nextInt(i);
      char tmp = array[idx];
      array[idx] = array[i];
      array[i] = tmp;
    }
     
    return array;
  }  

  /**
   * Encodes (LSB first) numbits of bits passed in the long.
   * 
   * @param bits - long value containing the bits to encode
   * @param numbits - number of bits to encode
   * @return
   */
  public String encode(long bits, int numbits)
  {
    char[] encoded = new char[(numbits + 5) / 6];
    for (int i = 0; i < encoded.length; ++i)
    {
      encoded[i] = _radix[(byte) bits & INT_RADIX_MASK];
      bits >>>= 6;
    }
    return new String(encoded);
  }

  /**
   * Decodes numbits of bits from the string. Invalid or missing digits are treated as 0.
   * @param s - base64-encoded string to decode
   * @param numbits - number of bits to extract (LSB first)
   * @return
   */
  public long decode(String s, int numbits)
  {
    if (numbits > 64) numbits = 64;
    int numDigits = (numbits + 5) / 6;
    if (s.length() < numDigits) numDigits = s.length();
    long bits = 0L;
    for (int i = numDigits - 1; 0 <= i; --i)
    {
      bits <<= 6;
      char c = s.charAt(i);
      int digit = 0 <= c && c < _reverseRadix.length ? _reverseRadix[c] : -1;
      if (digit != -1) bits |= digit;
    }
    return bits;
  }

  public byte decode(char c)
  {
    return (byte) _reverseRadix[c & 127];
  }
}
