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

import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.UnrecoverableEntryException
import java.security.cert.CertificateException
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.linkedin.util.codec.Base64Codec

public class EncryptionUtils
{
  private static final String ALGORITHM = "AES";
  private static final String MODE = "CBC";
  private static final String PADDING = "PKCS5Padding";
  private static final String KEYSTORETYPE = "JCEKS"
  private static final String CRYPTO = ALGORITHM + "/" + MODE + "/" + PADDING;

  private static final SecureRandom RANDOM = new SecureRandom();
  private static final String ENCRYPTEDPREFIX = "Encrypted-" + CRYPTO + "(";
  private static final String ENCRYPTEDSUFFIX = ")";
  private static final String REGEXTOMATCH = "Encrypted-" + CRYPTO + "\\(" + "[^\\" + ENCRYPTEDSUFFIX + "]+\\" + ENCRYPTEDSUFFIX;
  private static final Pattern ENCRYPTEDPATTERN = Pattern.compile("(" + REGEXTOMATCH + ")");

  private static final Base64Codec BASE64CODEC = new Base64Codec();

  /**
   * Create a file which stores random key which will be used for encrypt and decrypt
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public static void createSecretKey(File keyFile, String keyName, String keyStorePassword, String secretKeyPassword) throws GeneralSecurityException, IOException
  {
    boolean genKey = false;

    if (!keyFile.exists()) {
      genKey = true;
    } else {
      Map<String, byte[]> secretKeys = getSecretKeys(keyFile, keyStorePassword, secretKeyPassword)
      if (!secretKeys[keyName]) {
        genKey = true;
      }
    }

    if (genKey) {
      KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
      keyGen.init(128); // 192 and 256 bits may not be available
      SecretKey sk = keyGen.generateKey();
      saveSecretKey(sk, keyFile, keyName, keyStorePassword, secretKeyPassword);
    }
  }

  private static KeyStore loadKeyStore(File keyFile, String keyStorePassword)
  {
    KeyStore ks = KeyStore.getInstance(KEYSTORETYPE);

    if (keyFile.exists()) {
      java.io.FileInputStream fis = null;
      try {
        fis = new java.io.FileInputStream(keyFile);
        ks.load(fis, keyStorePassword.toCharArray());
      } finally {
        if (fis != null) {
          fis.close();
        }
      }
    } else {
      ks.load(null, keyStorePassword.toCharArray());
    }

    return ks;
  }

  public static void saveSecretKey(SecretKey sk, File keyFile, String keyName, String keyStorePassword, String secretKeyPassword) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException
  {
    KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(sk);

    KeyStore ks = loadKeyStore(keyFile, keyStorePassword)

    ks.setEntry(keyName, skEntry, new KeyStore.PasswordProtection(secretKeyPassword.toCharArray()));

    // store away the keystore
    java.io.FileOutputStream fos = null;
    try {
      fos = new java.io.FileOutputStream(keyFile);
      ks.store(fos, keyStorePassword.toCharArray());
    } finally {
      if (fos != null) {
        fos.close();
      }
    }
  }

  public static SecretKey getSecretKey(File keyFile, String keyName, String keyStorePassword, String secretKeyPassword) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableEntryException
  {
    KeyStore ks = loadKeyStore(keyFile, keyStorePassword)

    SecretKey sk = null;
    if (ks.isKeyEntry(keyName)) {
      KeyStore.SecretKeyEntry skEntry = (KeyStore.SecretKeyEntry) ks.getEntry(keyName, new KeyStore.PasswordProtection(secretKeyPassword.toCharArray()));
      sk = skEntry.getSecretKey();
    }

    return sk;
  }

  public static Map<String, byte[]> getSecretKeys(String keyFileName, String keyStorePassword, String secretKeyPassword)
  {
    File keyFile = new File(keyFileName)
    return getSecretKeys(keyFile, keyStorePassword, secretKeyPassword);
  }

  public static Map<String, byte[]> getSecretKeys(File keyFile, String keyStorePassword, String secretKeyPassword)
  {
    def secretKeys = [:]
    KeyStore ks = loadKeyStore(keyFile, keyStorePassword)

    ks.aliases().each { alias ->
      if (ks.entryInstanceOf(alias, KeyStore.SecretKeyEntry)) {
        KeyStore.SecretKeyEntry skEntry = (KeyStore.SecretKeyEntry) ks.getEntry(alias, new KeyStore.PasswordProtection(secretKeyPassword.toCharArray()));
        secretKeys.put(alias, skEntry.getSecretKey().getEncoded());
      }

    }

    return secretKeys;
  }

  /**
   * encrypt a value using the key in keyFile
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public static String encrypt(String value, Map<String, byte[]> secretKeys, String keyName) throws GeneralSecurityException, IOException
  {
    SecretKeySpec sks = getSecretKeySpec(secretKeys, keyName);
    Cipher cipher = Cipher.getInstance(CRYPTO);

    def iv = generateIV(cipher);
    cipher.init(Cipher.ENCRYPT_MODE, sks, new IvParameterSpec(iv), RANDOM);
    byte[] encrypted = cipher.doFinal(value.getBytes());
    return encode(encrypted, iv, keyName);
  }

  /**
   * decrypt a value using key in keyFile
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public static String decrypt(String message, Map<String, byte[]> secretKeys) throws GeneralSecurityException, IOException
  {
    byte[][] decoded = decode(message);
    byte[] toDecrypt = decoded[0];
    byte[] usingIv = decoded[1];
    String keyName = new String(decoded[2]);

    SecretKeySpec sks = getSecretKeySpec(secretKeys, keyName);
    Cipher cipher = Cipher.getInstance(CRYPTO);

    cipher.init(Cipher.DECRYPT_MODE, sks, new IvParameterSpec(usingIv), RANDOM);
    byte[] decrypted = cipher.doFinal(toDecrypt);
    return new String(decrypted);
  }

  /**
   * Given a buffer with embedded encrypted strings, decrypt it. Keeping the plain text part intact.
   *
   * @param message
   * @param secretKeys
   * @return decrypted buffer
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public static String decryptBuffer(String message, Map<String, byte[]> secretKeys) throws GeneralSecurityException, IOException
  {
    return processBuffer(message, secretKeys) { line, encrypted, keys ->
      return decrypt(encrypted, secretKeys)
    }
  }

  /**
   * Process a buffer with embedded encrypted strings, using the closure provided.
   * Closure will receive three arguments: original string that was passed in, encrypted block that is embedded in the
   * buffer and the secret keys that can used to decrypt it.
   *
   * @param message
   * @param secretKeys
   * @param c
   * @return String as processed by the closure
   */
  public static String processBuffer(String message, Map<String, byte[]> secretKeys, Closure c)
  {
    Matcher matcher = ENCRYPTEDPATTERN.matcher(message);
    StringBuffer destination = new StringBuffer();
    while(matcher.find()) {
      matcher.appendReplacement(destination, "");
      destination.append(c(message, matcher.group(1), secretKeys));
    }
    matcher.appendTail(destination);
    return destination.toString();

  }

  /**
   * Takes an input directory (fromDir) and recursively finds and decrypt all files in it into new
   * output directory (toDir)
   *
   * @param fromDir
   * @param toDir
   * @param secretKeys
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static decryptFiles(File fromDir, File toDir, Map<String, byte[]> secretKeys) throws  FileNotFoundException, IOException
  {
    File[] fromFiles = fromDir.listFiles();
    fromFiles.each { fromFile ->

      File toFile = new File(toDir, fromFile.getName())
      if (fromFile.isFile()) {
        // read from fromFile and write out toFile decrypting any encrypted strings
        fromFile.withInputStream { fis ->
          EncryptedInputStream cis = new EncryptedInputStream((fis), secretKeys)
          toFile.withOutputStream { fos ->
            fos << cis
          }
        }
      } else {
        if (!toFile.exists()) {
          toFile.mkdir()
        }
        decryptFiles(fromFile, toFile, secretKeys)
      }
    }
  }

  /**
   * given a string see if it encrypted.
   * This is done by checking the marker prefix/suffix that was added to the string.
   * @param encrypted
   * @return true if the string passed in was encrypted.
   */
  public static boolean isEncrypted(String encrypted)
  {
    int startIndex = encrypted.indexOf(ENCRYPTEDPREFIX);
    return (startIndex > -1);
  }

  public static String encryptionKeyName(String encrypted)
  {
    if (isEncrypted(encrypted)) {
      byte[][] decoded = decode(encrypted);
      return new String(decoded[2]);
    } else {
      return null;
    }
  }

  private static SecretKeySpec getSecretKeySpec(Map<String, byte[]> secretKeys, String keyName) throws NoSuchAlgorithmException, IOException
  {
    SecretKeySpec sks = null
    if (secretKeys[keyName]) {
      byte[] secretKey =  secretKeys[keyName]
      sks = new SecretKeySpec(secretKey, ALGORITHM);
    }
    return sks;
  }

  private static String encode(byte[] bytes, byte[] iv, String keyName)
  {
    StringBuilder builder = new StringBuilder();
    builder.append(ENCRYPTEDPREFIX);
    builder.append(encodeToBase64(bytes));
    builder.append(",");
    builder.append(encodeToBase64(iv));
    builder.append(",");
    builder.append(encodeToBase64(keyName.getBytes()));
    builder.append(ENCRYPTEDSUFFIX);
    return builder.toString();
  }

  private static byte[][] decode(String encoded) throws IOException
  {
    byte[][] decoded = new byte[3];
    int startIndex = encoded.indexOf(ENCRYPTEDPREFIX);
    if (startIndex > -1) {
      startIndex += ENCRYPTEDPREFIX.length();
      int endIndex = encoded.indexOf(ENCRYPTEDSUFFIX, startIndex);
      if (endIndex > -1) {
        String stringToDecode = encoded.substring(startIndex, endIndex);
        int i = 0;
        for (String part : stringToDecode.split(",")) {
          decoded[i++] = decodeFromBase64(part);
        }
        return decoded;
      } else {
        throw new IOException("Incorrrectly encoded string, Missing ENCRYPTEDSUFFIX '" + ENCRYPTEDSUFFIX + "'");
      }

    } else {
      throw new IOException("Input not correctly encoded, missing ENCRYPTEDPREFIX '" + ENCRYPTEDPREFIX + "'");
    }
  }

  private static byte[] generateIV(Cipher cipher)
  {
    byte [] ivBytes = new byte[cipher.getBlockSize()];
    RANDOM.nextBytes(ivBytes);
    return ivBytes;
  }

  private static String encodeToBase64(byte[] b)
  {
    return BASE64CODEC.encode(b);
  }


  private static byte[] decodeFromBase64(String s)
  {
    return BASE64CODEC.decode(s);
  }
}
