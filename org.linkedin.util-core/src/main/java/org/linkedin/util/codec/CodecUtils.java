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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * @author ypujante@linkedin.com
 *
 */
public class CodecUtils
{
  /**
   * Encodes the string using the codec provided.
   *
   * @param codec encode the string
   * @param s the string to encode
   * @return the encoded string */
  public static String encodeString(OneWayCodec codec, String s)
  {
    try
    {
      return codec.encode(s.getBytes("UTF-8"));
    }
    catch(UnsupportedEncodingException ex)
    {
      // shouldn't happen
      throw new RuntimeException(ex);
    }
  }

  /**
   * Decodes the string using the codec provided. Returns a string
   *
   * @param codec encode the string
   * @param s the string to encode
   * @return the encoded string
   * @throws Codec.CannotDecodeException if cannot decode the string */
  public static String decodeString(Codec codec, String s)
    throws Codec.CannotDecodeException
  {
    try
    {
      return new String(codec.decode(s), "UTF-8");
    }
    catch(UnsupportedEncodingException ex)
    {
      // shouldn't happen
      throw new RuntimeException(ex);
    }
  }

  /**
   * Create a <code>Random</code> object by extracting the seed from the password.
   */
  public static Random createRandom(String password)
  {
    if(password == null)
      return null;

    byte[] bytes;

    try
    {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      bytes = md.digest(password.getBytes("UTF-8"));
    }
    catch(NoSuchAlgorithmException e)
    {
      // should not happen
      throw new RuntimeException(e);
    }
    catch(UnsupportedEncodingException e)
    {
      // should not happen
      throw new RuntimeException(e);
    }

    int len = bytes.length;

    // we only take at most 6 bytes => 48 bits
    if(len > 6)
      len = 6;

    long seed = 0;

    if(len > 0)
    {
      seed = bytes[0];

      for(int i = 1; i < len; i++)
      {
        seed <<=  8;
        seed |= bytes[i];
      }
    }

    int k = 0;

    for(int i = len; i < bytes.length; i++)
    {
      seed ^= (((long) bytes[i]) << (k * 8));
      if(k++ == 5)
        k = 0;
    }

    return new Random(seed);
  }

  /**
   * Constructor
   */
  private CodecUtils()
  {
  }
}
