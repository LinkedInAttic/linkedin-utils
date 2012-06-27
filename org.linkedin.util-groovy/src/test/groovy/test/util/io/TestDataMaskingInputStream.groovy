package test.util.io

import org.linkedin.groovy.util.io.DataMaskingInputStream

/**
 * Created by IntelliJ IDEA.
 * User: hhan
 * Date: 6/18/12
 * Time: 3:16 PM
 */
class TestDataMaskingInputStream extends GroovyTestCase {


    void testOracleDBContent() {
        def temp = File.createTempFile("cfg2", "properties")
        temp.write '<property name="db.member2.db_url" value="jdbc:oracle:thin:Encrypted-AES/CBC/PKCS5Padding(3QIdAjOKfAqcetGKhHEWez,0VWjpS2ewydmPFX8y-F3M_,umlHnS9A)@//test.prod.linkedin.com:1521/PROD_PMEM2_MEMBER2" /> \n'
        String line = new DataMaskingInputStream(temp.newDataInputStream()).readLines()[0]
        String expected = '<property name="db.member2.db_url" value="jdbc:oracle:thin:Encrypted-********/********@********/********" />'
        assertFalse (line.contains("AES/CBC/PKCS5Padding(3QIdAjOKfAqcetGKhHEWez"))
        temp.deleteOnExit();
    }

    void testMySQLDBContent() {
        def temp = File.createTempFile("cfg2", "properties")
        temp.write '<property name="repdb.mysql.dbURL" value="jdbc:mysql://localhost/repdb_db?user=repdb&amp;password=test" /> \n'

        String line = new DataMaskingInputStream(temp.newDataInputStream()).readLines()[0]
        String expected = '<property name="repdb.mysql.dbURL" value="jdbc:mysql://localhost/repdb_db?user=repdb&amp;password=********" />'
        assertFalse(line.contains("password=test"))
        temp.deleteOnExit();
    }
}
