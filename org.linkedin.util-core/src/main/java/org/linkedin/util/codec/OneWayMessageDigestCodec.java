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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This codec will computes the digest of the input and then encode the result
 * with the codec. It is one way only because there is no way to go back to
 * the input. This class is thread safe!
 *
 * @author ypujante@linkedin.com
 */
public class OneWayMessageDigestCodec implements OneWayCodec
{
  public static final String MODULE = OneWayMessageDigestCodec.class.getName();

  private final MessageDigest _md;
  private final OneWayCodec _codec;

  /**
   * Constructor.
   *
   * @param algorithm the algorithm to use for the message digest
   * @param codec the codec to do the <code>byte</code> to <code>String</code>
   * encoding
   * @throws NoSuchAlgorithmException if the algorithm is invalid
   * @throws CloneNotSupportedException if the message digest is not cloneable */
  public OneWayMessageDigestCodec(String algorithm, OneWayCodec codec)
    throws NoSuchAlgorithmException, CloneNotSupportedException
  {
    this(algorithm, null, codec);
  }

  /**
   * Constructor
   *
   * @param algorithm the algorithm to use for the message digest
   * @param password the password to initialize the message digest
   * @param codec the codec to do the <code>byte</code> to <code>String</code>
   * encoding
   * @throws NoSuchAlgorithmException if the algorithm is invalid
   * @throws CloneNotSupportedException if the message digest is not cloneable */
  public OneWayMessageDigestCodec(String algorithm,
                                  String password,
                                  OneWayCodec codec)

    throws NoSuchAlgorithmException, CloneNotSupportedException
  {
    _md = MessageDigest.getInstance(algorithm);
    if(password != null)
      _md.update(password.getBytes());
    _md.clone(); // verify that it is cloneable
    _codec = codec;
  }

  /**
   * Constructor */
  public OneWayMessageDigestCodec(MessageDigest md, OneWayCodec codec)
    throws CloneNotSupportedException
  {
    _md = md;
    _md.clone(); // verify that it is cloneable
    _codec = codec;
  }

  /**
   * Encode the array into a <code>String</code>
   *
   * @param byteArray the array to encode
   * @return the encoded <code>String</code> */
  @Override
  public String encode(byte[] byteArray)
  {
    MessageDigest md = null;

    try
    {
      md = (MessageDigest) _md.clone();
    }
    catch(CloneNotSupportedException e)
    {
      // should not happen... already tested in constructor!!
      throw new RuntimeException(e);
    }

    return _codec.encode(md.digest(byteArray));
  }

  /**
   * Convenient call which create an instance based on the MD5 algorithm.
   *
   * @param codec the codec to do the <code>byte</code> to <code>String</code>
   * encoding
   * @return the codec */
  public static OneWayMessageDigestCodec createMD5Instance(OneWayCodec codec)
  {
    return createMD5Instance(null, codec);
  }

  /**
   * Convenient call which create an instance based on the MD5 algorithm.
   *
   * @param password the password to use for MD5
   * @param codec the codec to do the <code>byte</code> to <code>String</code>
   * encoding
   * @return the codec */
  public static OneWayMessageDigestCodec createMD5Instance(String password,
                                                           OneWayCodec codec)
  {
    return createWellKnownInstance("MD5", password, codec);
  }

  /**
   * Convenient call which create an instance based on the SHA-1 algorithm.
   *
   * @param codec the codec to do the <code>byte</code> to <code>String</code>
   * encoding
   * @return the codec */
  public static OneWayMessageDigestCodec createSHA1Instance(OneWayCodec codec)
  {
    return createSHA1Instance(null, codec);
  }

  /**
   * Convenient call which create an instance based on the SHA-1 algorithm.
   *
   * @param password the password to use for SHA-1
   * @param codec the codec to do the <code>byte</code> to <code>String</code>
   * encoding
   * @return the codec */
  public static OneWayMessageDigestCodec createSHA1Instance(String password,
                                                            OneWayCodec codec)
  {
    return createWellKnownInstance("SHA-1", password, codec);
  }

  /**
   * This creates a well known instance.. the difference with the constructor
   * is that all exceptions are caught and rethrown as RuntimeExcpetion
   * because they should not happen.
   *
   * @param algorithm
   * @param password
   * @param codec
   * @return the codec  */
  private static OneWayMessageDigestCodec createWellKnownInstance(String algorithm,
                                                                  String password,
                                                                  OneWayCodec codec)
  {
    try
    {
      return new OneWayMessageDigestCodec(algorithm, password, codec);
    }
    catch(NoSuchAlgorithmException e)
    {
      throw new RuntimeException(e);
    }
    catch(CloneNotSupportedException e)
    {
      throw new RuntimeException(e);
    }
  }
}
