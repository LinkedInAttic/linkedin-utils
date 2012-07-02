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

package test.util.io

import org.linkedin.groovy.util.io.DataMaskingInputStream

/**
 * User: hhan
 * Date: 6/18/12
 * Time: 3:16 PM
 * @author hhan@linkedin.com
 */
class TestDataMaskingInputStream extends GroovyTestCase {

  void testOracleDBContent()
  {
    def input = '<property name="db.member2.db_url" value="jdbc:oracle:thin:Encrypted-AES/CBC/PKCS5Padding(3QIdAjOKfAqcetGKhHEWez,0VWjpS2ewydmPFX8y-F3M_,umlHnS9A)@//test.prod.linkedin.com:1521/PROD_PMEM2_MEMBER2" /> \n'

    DataMaskingInputStream stream = new DataMaskingInputStream(new ByteArrayInputStream(input.getBytes("UTF-8")))
    def lines = stream.readLines()
    stream.close()
    assertTrue(lines.size() == 1)

    String line = lines[0].trim()
    String expected = '<property name="db.member2.db_url" value="jdbc:oracle:thin:Encrypted-********/********@********/********" />'
    assertEquals(line, expected)
  }

  void testMySQLDBContent()
  {
    def input = '<property name="repdb.mysql.dbURL" value="jdbc:mysql://localhost/repdb_db?user=repdb&amp;password=test!123#^" /> \n'

    DataMaskingInputStream stream = new DataMaskingInputStream(new ByteArrayInputStream(input.getBytes("UTF-8")))
    def lines = stream.readLines()
    stream.close()
    assertTrue(lines.size() == 1)

    String line = lines[0].trim()
    String expected = '<property name="repdb.mysql.dbURL" value="jdbc:mysql://localhost/repdb_db?user=repdb&amp;password=********" />'
    assertEquals(line, expected)
  }

}
