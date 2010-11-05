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
 * A one way codec simply define a method to encode.
 *
 * @author ypujante@linkedin.com
 */
public interface OneWayCodec
{
  /**
   * Encode the array into a <code>String</code>
   *
   * @param byteArray the array to encode
   * @return the encoded <code>String</code> */
  String encode(byte[] byteArray);
}