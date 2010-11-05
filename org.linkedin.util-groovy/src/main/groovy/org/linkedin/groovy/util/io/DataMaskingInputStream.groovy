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

package org.linkedin.groovy.util.io

/**
 * @author mdubey@linkedin.com
 *
 * Created: Sep 13, 2010 10:49:57 AM
 *
 *
 * Takes input stream which might have data like below (config file), and transforms the values that might represent sensitive data
 *
 * <property name="geoService#db.geo.db_url" value="jdbc:oracle:thin:geouser/geopass@geodbhost.qa:1521:DB">
 *   <src:contributor name="db.geo.db_url" source="file:/Users/acabrera/dev/BR_ENG_TOOLS_CONFIG_2B/config/fabric/ei2.fabric" value="jdbc:oracle:thin:geouser/geopass@geodbhost.qa:1521:DB" />
 * <property name="db.geo.db_url" value="jdbc:oracle:thin:geouser/geopass@geodbhost.qa:1521:DB">
 *  <src:contributor name="db.geo.db_url" source="file:/Users/acabrera/dev/BR_ENG_TOOLS_CONFIG_2B/config/fabric/ei2.fabric" value="jdbc:oracle:thin:geouser/geopass@geodbhost.qa:1521:DB" />
 *  <property name="geoService#DBSource#db_url" value="jdbc:oracle:thin:geouser/geopass@geodbhost.qa:1521:DB">
 *  <src:contributor name="db.geo.db_url" source="file:/Users/acabrera/dev/BR_ENG_TOOLS_CONFIG_2B/config/fabric/ei2.fabric" value="jdbc:oracle:thin:geouser/geopass@geodbhost.qa:1521:DB" />
 *   
 */
public class DataMaskingInputStream extends FilterInputStream {
/**
 * InputStream filter that masks sensitive data in the stream.
 */

  private char[] lineBuffer = null; // reusable byte buffer for reading input
  private byte[] o_buffer;
  private int index; // index of the bytes to return from o_buffer
  private boolean finished;
  private def keyValPattern = /(.*name=")([^"]*)(".*value=")([^"]*)(".*)/

  public DataMaskingInputStream(InputStream is) {
    super (is)
  }
  /**
   * read a byte of data from stream
   */
  @Override
  public int read() throws IOException {

    // return stuff from masked buffer if there is data in it
    if (finished) {
      return ((o_buffer == null) || (index == o_buffer.length)) ? -1 : o_buffer[index++] & 0xFF;
    }

    if ((o_buffer != null) && (index < o_buffer.length)) {
      return o_buffer[index++] & 0xFF;
    }

    // exausted masked buffer, start a new one
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

    // read another line of data from underlying stream and mask it.
    while ((c1 = super.in.read()) != -1 ) {
      if (--room < 0) { // No room, need to grow.
        buf = new char[offset + 256];
        room = buf.length - offset - 1;
        System.arraycopy(lineBuffer, 0, buf, 0, offset);
        lineBuffer = buf;
      }
      buf[offset++] = (char)c1;

      if (c1 == '\n') // Got NL -- have enough to try to mask now
        break;
    }

    if (c1 == -1) {
      finished = true;
    }

    if (offset > 0) {
      // have read in another buffer full of data, mask it and store in
      // another buffer
      o_buffer = String.copyValueOf(buf, 0, offset).replaceAll(keyValPattern) { Object[] it ->
        def prefix = it[1]  // <property name="
        def key    = it[2]  // geoService#db.geo.db_url
        def middle = it[3]  // " value="
        def value  = it[4]  // jdbc:oracle:thin:geo/geo@ei1-db1.qa:1521:DB
        def suffix = it[5]  // ">

        // The following two masking pattern were copied from
        // HealthCheck and also appear in following code:
        //
        // com.linkedin.spring.core.audit.LoggingAuditor.cleanSensitiveData
        // com.linkedin.healthcheck.gui.ConsoleHelper.removeSensitiveData
        //
        if (key.contains('secret') || key.contains('password')) {
          value = "********"
        }

        if (value.contains('oracle')) {
          value = value.replaceAll("\\w*/\\w*", '********/********')
        }

        return "${prefix}${key}${middle}${value}${suffix}"
      }.getBytes();
    }

    return read();
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