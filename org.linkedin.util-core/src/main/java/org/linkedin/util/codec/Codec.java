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

/**
 * Defines the API of a codec. There is a method to encode an array of byte
 * into a <code>String</code> and one that does the opposite.
 *
 * @author ypujante@linkedin.com */
public interface Codec extends OneWayCodec
{
  /**
   * Defines the exception thrown when the string cannot be decoded */
  public static class CannotDecodeException extends Exception
  {
    private static final long serialVersionUID = 1L;

    public CannotDecodeException(String msg)
    {
      super(msg);
    }

    public CannotDecodeException(String message, Throwable cause)
    {
      super(message, cause);
    }
  }

  /**
   * Decodes the <code>String</code> into a byte array
   *
   * @param s the string to decode
   * @return the byte array decoded
   * @exception CannotDecodeException if the string has not been encoded
   * with this codec */
  public byte[] decode(String s) throws CannotDecodeException;
}
