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


package org.linkedin.util.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;

/**
 * @author ypujante@linkedin.com
 *
 */
public class IOUtils
{
  /**
   * Deletes the file provided. If it is a directory, recursively deletes a directory and its content.
   *
   * @param file
   * @return <code>true</code> if the file was deleted, <code>false</code>
   * if didn't exist
   * @throws IOException
   */
  public static boolean deleteFile(File file) throws IOException
  {
    if(!file.exists())
      return false;

    File[] files = file.listFiles();

    if(files != null)
    {
      for(int i = 0; i < files.length; i++)
      {
        File childFile = files[i];
        if(childFile.equals(file))
          continue;

        if(childFile.isDirectory())
          deleteFile(childFile);
        else
          childFile.delete();
      }
    }

    return file.delete();
  }

  /**
   * Creates a new directory. This method creates automatically all the parent
   * directories if necesary. Contrary to <code>File.mkdirs</code>, this method
   * will fail if the directory cannot be created. The returned value is also
   * different in meaning: <code>false</code> means that the directory was not
   * created because it already existed as opposed to it was not created because
   * we don't know.
   *
   * @param directory the directory to create
   * @return <code>true</code> if the directory was created, <code>false</code>
   * if it existed already.
   * @throws IOException when there is a problem creating the directory */
  public static boolean createNewDirectory(File directory) throws IOException
  {
    if(directory.exists())
      return false;

    if(!directory.mkdirs())
      throw new IOException("cannot create the directory: " + directory);

    return true;
  }

  /**
   * Creates a temporary directory.
   *
   * @param namespace
   * @param name      root name of the temporary directory
   * @return the temp dir
   * @throws IOException if there is a problem
   */
  public static File createTempDirectory(String namespace, String name) throws IOException
  {
    File dir = File.createTempFile(namespace, "");

    if(dir.exists())
      deleteFile(dir);

    createNewDirectory(dir);
    // we make sure that the root directory will be deleted when the test finishes
    dir.deleteOnExit();

    File tempDir = new File(dir, name);

    createNewDirectory(tempDir);
    // we make sure that the directory will be deleted when the test finishes
    tempDir.deleteOnExit();

    return tempDir.getCanonicalFile();
  }

  /**
   * Turns the object into a <code>byte[]</code> by serializing it in memory.
   *
   * @param ser the serializable object
   * @exception IOException if there is a problem in the serialization step */
  public static <T extends Serializable> byte[] serialize(T ser)
    throws IOException
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    try
    {
      oos.writeObject(ser);
    }
    finally
    {
      oos.close();
    }
    return baos.toByteArray();
  }

  /**
   * This is the opposite of {@link #serialize(Serializable)}.
   *
   * @param array the previously serialized object
   * @exception IOException if there is a problem in the deserialization step
   * @exception ClassNotFoundException if problem with the serialized object
   */
  @SuppressWarnings("unchecked")
  public static <T extends Serializable> T deserialize(byte[] array)
    throws IOException, ClassNotFoundException
  {
    return (T) (deserialize(array, false));
  }

  /**
   * This is the opposite of {@link #serialize(Serializable)}.
   *
   * @param array the previously serialized object
   * @param useContextClassLoader true to use the context classLoader, false
   * to use the current classLoader
   * @exception IOException if there is a problem in the deserialization step
   * @exception ClassNotFoundException if problem with the serialized object
   */
  @SuppressWarnings("unchecked")
  public static <T extends Serializable> T deserialize(byte[] array, boolean useContextClassLoader)
    throws IOException, ClassNotFoundException
  {
    ByteArrayInputStream bais = new ByteArrayInputStream(array);
    ObjectInputStream ois;

    if (useContextClassLoader)
    {
      ois = new ObjectInputStreamWithClassLoader(bais, null);
    }
    else
    {
      ois = new ObjectInputStream(bais);
    }

    try
    {
      return (T) ois.readObject();
    }
    finally
    {
      ois.close();
    }
  }

  /**
   * This is the opposite of {@link #serialize(Serializable)}.
   *
   * @param array the previously serialized object
   * @exception IOException if there is a problem in the deserialization step
   * @exception ClassNotFoundException if problem with the serialized object
   */
  @SuppressWarnings("unchecked")
  public static <T extends Serializable> T deserialize(byte[] array, ClassLoader classLoader)
    throws IOException, ClassNotFoundException
  {
    final ByteArrayInputStream bais = new ByteArrayInputStream(array);

    final ObjectInputStream ois;

    if(classLoader != null)
    {
      ois = new ObjectInputStreamWithClassLoader(bais, classLoader);
    }
    else
    {
      ois = new ObjectInputStream(bais);
    }

    try
    {
      return (T) ois.readObject();
    }
    finally
    {
      ois.close();
    }
  }

  /**
   * Copies the input stream into the output stream (all)
   *
   * @param in the input stream to read data
   * @param out the output stream to write data */
  public static void copy(InputStream in, OutputStream out) throws IOException
  {
    copy(in, out, -1);
  }

  /**
   * Copies the input stream into the output stream (num_bytes)
   *
   * @param in the input stream to read data
   * @param out the output stream to write data
   * @param num_bytes the number of bytes to copy */
  public static void copy(InputStream in, OutputStream out, int num_bytes)
    throws IOException
  {
    if(num_bytes == 0)
      return;

    int n;

    if(num_bytes < 0)
    {
      byte[] b = new byte[2048];
      while((n = in.read(b, 0, b.length)) > 0)
        out.write(b, 0, n);
    }
    else
    {
      int offset = 0;
      byte[] b = new byte[num_bytes];
      while(num_bytes > 0 && (n = in.read(b, offset, num_bytes)) > 0)
      {
        offset += n;
        num_bytes -= n;
      }
      out.write(b);
    }
  }

  /**
     * Copies the reader into the writer (num_bytes)
     *
     * @param in the reader to read data
     * @param out the writer to write data */
    public static void copy(Reader in, Writer out)
      throws IOException
  {
    copy(in, out, -1);
  }

  /**
   * Copies the reader into the writer (num_bytes)
   *
   * @param in the reader to read data
   * @param out the writer to write data
   * @param num_bytes the number of chars to copy */
  public static void copy(Reader in, Writer out, int num_bytes)
    throws IOException
  {
    if(num_bytes == 0)
      return;

    int n;

    if(num_bytes < 0)
    {
      char[] b = new char[2048];
      while((n = in.read(b, 0, b.length)) > 0)
        out.write(b, 0, n);
    }
    else
    {
      int offset = 0;
      char[] b = new char[num_bytes];
      while(num_bytes > 0 && (n = in.read(b, offset, num_bytes)) > 0)
      {
        offset += n;
        num_bytes -= n;
      }
      out.write(b);
    }
  }

  
  /**
   * Constructor
   */
  private IOUtils()
  {
  }
}
