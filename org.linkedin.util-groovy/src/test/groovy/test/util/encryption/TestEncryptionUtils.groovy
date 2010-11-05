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

package test.util.encryption

import org.linkedin.groovy.util.io.fs.FileSystemImpl
import org.linkedin.groovy.util.io.fs.FileSystem
import org.linkedin.groovy.util.encryption.EncryptionUtils

/**
 * User: mdubey
 * Date: Jul 26, 2010
 * Time: 8:23:00 AM
 * @author mdubey@linkedin.com
 */
class TestEncryptionUtils extends GroovyTestCase
{
  FileSystem fileSystem

  protected void setUp()
  {
    super.setUp();

    fileSystem = FileSystemImpl.createTempFileSystem()
  }

  protected void tearDown()
  {
    fileSystem.destroy()
  }

  public void testKeyStore()
  {
    String keyFileName = "testkeystore.key";
    String keyStorePassword = "keystorepassword"
    String secretKeyPassword =  'keypassword';

    File keyFile = fileSystem.toResource(keyFileName).file

    EncryptionUtils.createSecretKey(keyFile, "key1", keyStorePassword, secretKeyPassword)
    EncryptionUtils.createSecretKey(keyFile, "key2", keyStorePassword, secretKeyPassword)

    Map<String, byte[]> secretKeys = EncryptionUtils.getSecretKeys(keyFile, keyStorePassword, secretKeyPassword)

    assertTrue(secretKeys.size() == 2)
    assertTrue(secretKeys['key1'] != null)
    assertTrue(secretKeys['key2'] != null)

    keyFile.delete()
  }

  public void testEncryptDecrypt()
  {
    String passwordFileName = "testencryption.properties";
    File propertiesFile = fileSystem.toResource(passwordFileName).file

    String clearPwd= "[Clear Password]";
    String anotherPwd = "[Another Password]"

    Map<String, byte[]> secretKeys = [
            'key1': 'encryptionkey123'.getBytes(),
            'key2': 'encryptionkey999'.getBytes()
    ]

    // encrypt properties
    Properties p1 = new Properties();

    p1.put("user", "Real");
    String encryptedPwd = EncryptionUtils.encrypt(clearPwd, secretKeys, 'key1');
    String anotherEncryptedPwd = EncryptionUtils.encrypt(anotherPwd, secretKeys, 'key2');
    p1.put("pwd", encryptedPwd);
    String value = "This is a prefix, password = (" + encryptedPwd + "), suffix. Another embedded password (" + anotherEncryptedPwd + ") ends here."
    p1.put("Multiple", value);
    p1.store(new FileWriter(propertiesFile), "");

    // decrypt properties
    Properties p2 = new Properties();

    p2.load(new FileReader(propertiesFile));
    encryptedPwd = p2.getProperty("pwd");

    assertTrue(clearPwd == EncryptionUtils.decrypt(encryptedPwd, secretKeys))
    anotherEncryptedPwd = p2.getProperty("Multiple");
    assertTrue("This is a prefix, password = (" + clearPwd + "), suffix. Another embedded password (" + anotherPwd + ") ends here." == EncryptionUtils.decryptBuffer(anotherEncryptedPwd, secretKeys));

    propertiesFile.delete()
  }

  public void testDirDecrypt()
  {
    File testDir = fileSystem.toResource('testEncryption').file
    File inputDir = new File(testDir, "input")
    File outputDir = new File(testDir, "output")
    String plainText1 = "This is a plain text string"
    String plainText2 = "This is a plain text string for deeper file"
    String plainText3 = "This is an unencrypted file."

    Map<String, byte[]> secretKeys = [
            'key1': 'encryptionkey123'.getBytes(),
            'key2': 'encryptionkey999'.getBytes()
    ]

    String encrypted1 = EncryptionUtils.encrypt(plainText1, secretKeys, 'key1');
    String encrypted2 = EncryptionUtils.encrypt(plainText2, secretKeys, 'key2');

    inputDir.mkdirs()
    outputDir.mkdirs()

    File a = new File(inputDir, "a.txt")
    a.setText(encrypted1)

    File secondDir = new File (inputDir, "subdir")
    secondDir.mkdirs()

    File b = new File(secondDir, "b.txt")
    b.setText(encrypted2)

    File c = new File(secondDir, "c.txt")
    c.setText(plainText3)

    EncryptionUtils.decryptFiles(inputDir, outputDir, secretKeys);

    File aout = new File (outputDir, "a.txt")
    secondDir = new File (outputDir, "subdir")
    File bout = new File(secondDir, "b.txt")
    File cout = new File(secondDir, "c.txt")

    assertTrue(aout.exists())
    assertTrue(bout.exists())
    assertTrue(cout.exists())

    assertTrue(aout.getText() == plainText1)
    assertTrue(bout.getText() == plainText2)
    assertTrue(cout.getText() == plainText3)

    testDir.deleteDir()
  }
}
