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

package org.linkedin.groovy.util.encryption

/**
 * User: mdubey
 * Date: Jul 20, 2010
 * Time: 1:46:36 PM
 * @author mdubey@linkedin.com
 */
/**
 * InputStream filter that decrypts encrypted data in the stream.
 */
import java.security.GeneralSecurityException;


public class EncryptedInputStream extends FilterInputStream {
  private char[] lineBuffer = null; // reusable byte buffer for reading input
  private byte[] o_buffer;
  private int index; // index of the bytes to return from o_buffer
  private boolean finished;
  private Map<String, byte[]> secretKeys;

  /**
   * Constructor to process inputstream of encrypted data
   * @param is
   * @param keyFile
   */
  public EncryptedInputStream(InputStream is, Map<String, byte[]> secretKeys) {
    super (is);
    this.secretKeys = secretKeys;
  }

  /**
   * read a byte of data from stream
   */
  @Override
  public int read() throws IOException {

    // return stuff from decrypted buffer if there is data in it
    if (finished) {
      return ((o_buffer == null) || (index == o_buffer.length)) ? -1 : o_buffer[index++] & 0xFF;
    }

    if ((o_buffer != null) && (index < o_buffer.length)) {
      return o_buffer[index++] & 0xFF;
    }

    // exausted decrypted buffer, start a new one
    index = 0;
    o_buffer = null;

    // a resuable buffer to read data from underlying stream into
    char[] buf = lineBuffer;
    if (buf == null) {
      buf = lineBuffer = new char[256];
    }

    int c1;
    int room = buf.length;
    int offset = 0;

    // read another line of data from underlying stream and decrypt it.
    while ((c1 = super.in.read()) != -1 ) {
      if (--room < 0) { // No room, need to grow.
        buf = new char[offset + 256];
        room = buf.length - offset - 1;
        System.arraycopy(lineBuffer, 0, buf, 0, offset);
        lineBuffer = buf;
      }
      buf[offset++] = (char)c1;

      if (c1 == '\n') // Got NL -- have enough to try to decrypt now
        break;
    }

    if (c1 == -1) {
      finished = true;
    }

    if (offset > 0) {
      // have read in another buffer full of encrypted data, decrypt it and store in
      // decrypted buffer
      try {
        o_buffer = EncryptionUtils.decryptBuffer(String.copyValueOf(buf, 0, offset), secretKeys).getBytes();
      } catch (GeneralSecurityException e) {
        throw new IOException(e.getMessage());
      }
    }

    return read();

//		while (o_buffer == null) {
//			if ((num_read = in.read(i_buffer)) == -1) {
//				try {
//					o_buffer = cipher.doFinal();
//				} catch (Exception e) {
//					throw new IOException(e.getMessage());
//				}
//				finished = true;
//				break;
//			}
//			o_buffer = cipher.update(i_buffer, 0, num_read);
//		}
//		return read();
  }

  /**
   * read a buffer full of data
   */
  @Override
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }


  /**
   * read in len bytes of data into buffer starting at offset off
   */
  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (super.in == null) {
      throw new NullPointerException(
              "Underlying input stream is null");
    }

    int read_b;
    int i;
    for (i = 0; i < len; i++) {
      if ((read_b = read()) == -1) {
        return (i == 0) ? -1 : i;
      }
      if (b != null) {
        b[off + i] = (byte) read_b;
      }
    }
    return i;
  }

  /**
   * skip n bytes of data
   */
  @Override
  public long skip(long n) throws IOException {
    long i = 0;
    int available = available();
    if (available < n) {
      n = available;
    }
    while ((i < n) && (read() != -1)) {
      i++;
    }
    return i;
  }

  /**
   *
   */
  @Override
  public int available() throws IOException {
    return 0;
  }

  /**
   *
   */
  @Override
  public boolean markSupported() {
    return false;
  }
}

