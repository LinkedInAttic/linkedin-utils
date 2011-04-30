/*
 * Copyright 2010-2010 LinkedIn, Inc
 * Portions Copyright (c) 2011 Yan Pujante
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

package org.linkedin.util.io.resource;

import junit.framework.TestCase;
import org.linkedin.util.clock.SystemClock;
import org.linkedin.util.concurrent.ExternalCommand;
import org.linkedin.util.io.IOUtils;
import org.linkedin.util.io.PathUtils;
import org.linkedin.util.io.ram.RAMDirectory;
import org.linkedin.util.io.resource.internal.InternalResource;
import org.linkedin.util.io.resource.internal.InternalResourceProvider;
import org.linkedin.util.io.resource.internal.ResourceProviderChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ypujante@linkedin.com
 *
 */
public class TestResource extends TestCase
{
  public static final String MODULE = TestResource.class.getName();
  public static final Logger log = LoggerFactory.getLogger(MODULE);

  private final static int ROOT = 0;
  private final static int A = 1;
  private final static int B = 2;
  private final static int C = 3;

  private final static int EMPTY = 4;

  private final static int D = 5;

  private final static String WEB_XML_CONTENT =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "\n" +
    "<!DOCTYPE web-app PUBLIC '-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN' 'http://java.sun.com/dtd/web-app_2_3.dtd'>\n" +
    "\n" +
    "<web-app>\n" +
    "  <servlet>\n" +
    "    <servlet-name>TestServletContextResource</servlet-name>\n" +
    "    <servlet-class>" +
    TestResource.class.getName() +
    "</servlet-class>\n" +
    "    <load-on-startup>1</load-on-startup>\n" +
    "  </servlet>\n" +
    "\n" +
    "  <servlet-mapping>\n" +
    "    <servlet-name>TestServletContextResource</servlet-name>\n" +
    "    <url-pattern>/tscr</url-pattern>\n" +
    "  </servlet-mapping>\n" +
    "</web-app>";

  private final List<File> _deleteOnExit = new ArrayList<File>();

  /**
   * Constructor
   */
  public TestResource(String name)
  {
    super(name);
  }

  @Override
  protected void tearDown() throws Exception
  {
    try
    {
      Collections.reverse(_deleteOnExit);
      for(File file : _deleteOnExit)
      {
        try
        {
          IOUtils.deleteFile(file);
        }
        catch(IOException e)
        {
          log.warn("Exception [ignored] while deleting file " + file, e);
        }
      }
    }
    finally
    {
      super.tearDown();
    }
  }

  protected String getWebXmlContent()
  {
    return WEB_XML_CONTENT;
  }

  public static abstract class Checker
  {
    protected void checkIsModifiedSince(Resource resource)
    {
      long lastModified = resource.lastModified();

      if(lastModified == 0L)
      {
        assertFalse(resource.isModifiedSince(lastModified));
        assertFalse(resource.isModifiedSince(1000L));
      }
      else
      {
        // 1ms in the past it was
        assertTrue(resource.isModifiedSince(lastModified - 1));

        // last modified or future
        assertFalse(resource.isModifiedSince(lastModified));
        assertFalse(resource.isModifiedSince(lastModified + 1));

        // 0 (modified)
        assertTrue(resource.isModifiedSince(0));
      }
    }

    protected void checkLastModified(ResourceInfo info, File file) throws IOException
    {
      assertEquals(file.lastModified(), info.getLastModified());
    }

    protected void checkInfo(Resource resource, File file) throws IOException
    {
      ResourceInfo info = resource.getInfo();
      checkLastModified(info, file);
      checkContentLength(info, file);
      checkIsModifiedSince(resource);
    }

    protected void checkContentLength(ResourceInfo info, File file) throws IOException
    {
      assertEquals(file.isDirectory() ? 0 : file.length(), info.getContentLength());
    }

    /**
     * By default the content of a directory is not readable (not as an input stream..)
     */
    protected void checkDirectoryContent(Resource resource) throws IOException
    {
      try
      {
        readContent(resource);
        fail("cannot read content of a directory");
      }
      catch(IOException e)
      {
        // expected
      }
    }

    protected void checkIsDirectory(Resource resource)
    {
      assertTrue(resource.isDirectory());
    }

    protected abstract void checkURI(Resource resource, File file) throws URISyntaxException;

    protected void checkListResource(Resource resource, File file) throws IOException
    {
      Resource[] resources = resource.list();
      assertEquals(file.list().length, resources.length);
    }

    /**
     * Apply the filter to the list and check that the result matches the expected elements.
     *
     * @param resource
     * @param filter
     * @param expected
     * @throws IOException
     */
    protected void checkListResources(Resource resource, final String filter, Resource... expected)
      throws IOException
    {
      Resource[] filteredResources = resource.list(new ResourceFilter()
      {
        @Override
        public boolean accept(Resource resource)
        {
          return resource.getFilename().endsWith(filter);
        }
      });
      assertEqualsOrderDontCare(filter, filteredResources, expected);
    }
  }

  public static class FileChecker extends TestResource.Checker
  {
    @Override
    public void checkURI(Resource resource, File file)
    {
      assertEquals(file.toURI(), resource.toURI());
    }
  }

  public static class RAMChecker extends TestResource.Checker
  {
    private final URI _fileBaseURI;

    public RAMChecker(URI fileBaseURI)
    {
      _fileBaseURI = fileBaseURI;
    }

    @Override
    protected void checkURI(Resource resource, File file) throws URISyntaxException
    {
      String fileURI = file.toURI().toString().substring(_fileBaseURI.toString().length());
      assertEquals(new URI("ram:///" + fileURI), resource.toURI());
    }
  }

  public static class JarChecker extends TestResource.Checker
  {
    private final URI _fileBaseURI;
    private final URI _jarBaseURI;

    public JarChecker(URI jarBaseURI, URI fileBaseURI)
    {
      _fileBaseURI = fileBaseURI;
      _jarBaseURI = jarBaseURI;
    }

    /**
     * It seems that jar and filesystem differ on this field... I wrote a separate program to test
     * my intuition and indeed some (but not all!) last modified are off by a second. My guess is
     * that jar and file round up to the nearest second but they don't seem to do it exactly the
     * same way....
     */
    @Override
    public void checkLastModified(ResourceInfo info, File file) throws IOException
    {
      assertTrue(Math.abs(file.lastModified() - info.getLastModified()) <= 1000);
    }

    /**
     * We recreate the uri resource from the base uri
     */
    @Override
    public void checkURI(Resource resource, File file) throws URISyntaxException
    {
      String fileURI = file.toURI().toString().substring(_fileBaseURI.toString().length());

      assertEquals(new URI("jar:" + PathUtils.removeTrailingSlash(_jarBaseURI.toString()) +
                           "!/" + fileURI),
                   resource.toURI());
    }

    /*
     Test code that proves what I say about last modified...
 public static void main(String[] args) throws IOException, InterruptedException
 {
   File root = new File("/tmp/ypujante/TestJarURL4");

   readFiles(root, new JarFile(new File("/tmp/ypujante/toto.jar")));
   //createFiles(root);
 }

 private static void readFiles(File root, JarFile jarFile)
 {
   for(int i = 0; i < 100; i++)
   {
     File file = new File(root, "f" + i);
     JarEntry jarEntry = jarFile.getJarEntry("TestJarURL4/f" + i);

     if(file.lastModified() != jarEntry.getTime())
       System.out.println(file.toString() + " | " + jarEntry.toString() + " =>" +
                          file.lastModified() + " (" + new Date(file.lastModified()) + ") " +
                          "/" +
                          jarEntry.getTime() + " (" + new Date(jarEntry.getTime()) + ") ");
   }
 }

 private static void createFiles(File root)
   throws IOException, InterruptedException
 {

   root.mkdirs();

   for(int i = 0; i < 100; i++)
   {
     File file = new File(root, "f" + i);
     FileWriter fw = new FileWriter(file);
     try
     {
       BufferedWriter writer = new BufferedWriter(fw);
       writer.write("content " + i);
       writer.flush();
     }
     finally
     {
       fw.close();
     }

     System.out.println(file.toString() + " =>" + file.lastModified());
     Thread.sleep(100);
   }
 }

    */
  }

  /**
   * Test with a non existent file
   *
   * @throws IOException
   */
  public void testNonExistentFile() throws IOException, URISyntaxException
  {
    // we create a directory and then we delete it to make sure it does not exist...
    File root = createTempDirectory(TestResource.class.getName(), "testNonExistentFile");
    IOUtils.deleteFile(root);

    checkNonExistentResource(FileResource.createFromRoot(root), root, new TestResource.FileChecker());
  }

  /**
   * Convenient call which checks that a resource does not exists...
   *
   * @param resource the resource to check
   */
  public static void checkNonExistentResource(Resource resource,
                                              File nonExistenFile,
                                              TestResource.Checker checker)
    throws IOException, URISyntaxException
  {
    assertFalse(resource.exists());
    assertNull(resource.list());
    try
    {
      resource.getInfo();
      fail("non exsitent");
    }
    catch(IOException e)
    {
      // expected
    }

    try
    {
      resource.getInputStream();
      fail("it is a directory...");
    }
    catch(IOException e)
    {
      // expected
    }

    checker.checkURI(resource, nonExistenFile);

    assertFalse(resource.createRelative("bar").exists());
  }

  /**
   * We test with an empty directory.
   */
  public void testEmptyDirectory() throws IOException, URISyntaxException
  {
    File root = createTempDirectory(TestResource.class.getName(), "testEmptyDirectory");
    try
    {
      checkEmptyDirectory(root, FileResource.createFromRoot(root), new TestResource.FileChecker());
    }
    finally
    {
      IOUtils.deleteFile(root);
    }
  }

  /**
   * Checks that the resource points to an empty directory
   *
   * @param emptyDirectory
   * @param resource
   * @throws IOException
   */
  private static void checkEmptyDirectory(File emptyDirectory, Resource resource, TestResource.Checker checker)
    throws IOException, URISyntaxException
  {
    assertTrue(resource.exists());

    checker.checkIsDirectory(resource);

    // empty directory => 0 entries returned
    checker.checkListResource(resource, emptyDirectory);

    // info should be the same as what file returns (since in this case it is a thin wrapper)
    checker.checkInfo(resource, emptyDirectory);

    checker.checkDirectoryContent(resource);

    checker.checkURI(resource, emptyDirectory);

    // the directory is empty so it will point to a non existent resource
    checkNonExistentResource(resource.createRelative("foo"),
                             new File(emptyDirectory, "foo"),
                             checker);
  }

  /**
   * test for file resource
   */
  public void testTreeDirectoryFileResource() throws IOException, URISyntaxException
  {
    File root = createTempDirectory(TestResource.class.getName(), "testTreeDirectoryFileResource");
    try
    {
      File[][] directoryStructure = createDirectoryStructure(root, getWebXmlContent());

      // root
      Resource rootResource = FileResource.createFromRoot(root);

      checkTreeResource(rootResource, directoryStructure, new TestResource.FileChecker());
    }
    finally
    {
      IOUtils.deleteFile(root);
    }
  }

  /**
   * Test for RAM resource
   */
  public void testTreeDirectoryRAMResource() throws IOException, URISyntaxException
  {
    File root = createTempDirectory(TestResource.class.getName(), "testTreeDirectoryRAMResource");
    try
    {
      File[][] directoryStructure = createDirectoryStructure(root, getWebXmlContent());

      // root
      Resource rootResource = RAMResource.create(createRAMDirectory(root));

      checkTreeResource(rootResource, directoryStructure, new TestResource.RAMChecker(root.toURI()));
    }
    finally
    {
      IOUtils.deleteFile(root);
    }
  }

  /**
   * Duplicates the directory structure on the filesystem in a RAMDirectory
   */
  private RAMDirectory createRAMDirectory(File root) throws IOException
  {
    RAMDirectory ramRoot = new RAMDirectory(SystemClock.instance(), "");

    populateRAMDirectory(ramRoot, root);

    return ramRoot;
  }

  /**
   * Recursively populates the ram directory
   */
  private void populateRAMDirectory(RAMDirectory ramDirectory, File directory) throws IOException
  {
    String[] names = directory.list();

    for(String name : names)
    {
      File file = new File(directory, name);
      if(file.isDirectory())
      {
        RAMDirectory subdir = ramDirectory.mkdir(name);
        subdir.touch(file.lastModified());
        populateRAMDirectory(subdir, file);
      }
      else
      {
        ramDirectory.add(name, readContent(file)).touch(file.lastModified());
      }
    }
  }

  /**
   * Test for resource chain: we create 2 directory structures with some similarities and
   * dissimilarities. root1 is the same as the one created in {@link #createDirectoryStructure2(File)}
   *
   * <pre>
   * root2/
   *      a.html
   *      d.txt
   *      d1/
   *         a.html
   *         d.txt
   *         d1_1/
   *              a.html
   *              d.txt
   *              empty/
   *      d3/
   *         a.html
   *         d.txt
   * <p/>
   * </pre>
   *
   * @throws IOException
   * @throws URISyntaxException
   */
  public void testTreeDirectoryResourceChain() throws IOException, URISyntaxException
  {
    File root1 = createTempDirectory(TestResource.class.getName(), "testTreeDirectoryResourceChain1");
    try
    {
      // root1
      File[][] directoryStructure1 = createDirectoryStructure(root1, getWebXmlContent());

      // chain should work the same if there is only one element in it!
      checkTreeResource(ResourceChain.create(FileResource.createFromRoot(root1)),
                                             directoryStructure1,
                                             new TestResource.FileChecker());

      // chain should work the same if there is only one element in it!
      // I need to do this otherwise the previous create method bypass the chain entirely...
      InternalResourceProvider provider =
        (InternalResourceProvider) ((InternalResource) FileResource.createFromRoot(root1)).getResourceProvider();
      checkTreeResource(new ResourceProviderChain(provider).getRootResource(),
                        directoryStructure1,
                        new TestResource.FileChecker());

      File root2 = createTempDirectory(TestResource.class.getName(), "testTreeDirectoryResourceChain2");
      try
      {

        // root2
        File[][] directoryStructure2 = createDirectoryStructure2(root2);

        // here we create a chain where both resources are at the root...
        Resource root = ResourceChain.create(FileResource.createFromRoot(root1),
                                             FileResource.createFromRoot(root2));

        checkChain(root, directoryStructure1, directoryStructure2, new TestResource.FileChecker());
      }
      finally
      {
        IOUtils.deleteFile(root2);
      }
    }
    finally
    {
      IOUtils.deleteFile(root1);
    }
  }

  /**
   * Verifies that the chain is working properly.
   */
  private void checkChain(Resource root,
                          File[][] ds1,
                          File[][] ds2,
                          Checker checker) throws IOException, URISyntaxException
  {
    checkResource(root, "a.html", ds1[0][A], checker); // in root1 (precedence)
    checkResource(root, "b.txt", ds1[0][B], checker);  // in root1 (only there)
    checkResource(root, "d.txt", ds2[0][D], checker);  // in root2 (only there)
    checkNonExistentResource(root.createRelative("e.txt"), new File(ds2[0][ROOT], "e.txt"), checker);

    // now we go in d1:
    Resource d1 = root.createRelative("d1");
    checkResource(d1, "a.html", ds1[1][A], checker); // in root1/d1 (precedence)
    checkResource(d1, "b.txt", ds1[1][B], checker);  // in root1/d1 (only there)
    checkResource(d1, "d.txt", ds2[1][D], checker);  // in root2/d1 (only there)

    // now we go in d1_1:
    Resource d1_1 = d1.createRelative("d1_1");
    checkResource(d1_1, "a.html", ds1[2][A], checker); // in root1/d1/d1_1 (precedence)
    checkResource(d1_1, "b.txt", ds1[2][B], checker);  // in root1/d1/d1_1 (only there)
    checkResource(d1_1, "d.txt", ds2[2][D], checker);  // in root2/d1/d1_1 (only there)

    // now we go in d2:
    Resource d2 = root.createRelative("d2");
    checkResource(d2, "a.html", ds1[3][A], checker); // in root1/d2 (only there)
    checkResource(d2, "b.txt", ds1[3][B], checker);  // in root1/d2 (only there)
    checkNonExistentResource(d2.createRelative("d.txt"), new File(ds2[0][ROOT], "d2/d.txt"), checker);

    // now we go in d3:
    Resource d3 = root.createRelative("d3");
    checkResource(d3, "a.html", ds2[3][A], checker); // in root2/d3 (only there)
    checkResource(d3, "d.txt", ds2[3][D], checker);  // in root2/d3 (only there)
    checkNonExistentResource(d3.createRelative("b.txt"), new File(ds2[0][ROOT], "d3/b.txt"), checker);

    checker.checkListResources(d1_1, ".txt",
                               d1_1.createRelative("b.txt"),
                               d1_1.createRelative("c.txt"),
                               d1_1.createRelative("d.txt"));

    checker.checkListResources(d3, ".txt",
                               d3.createRelative("d.txt"));
  }

  /**
   * Create the following directory structure
   * <pre>
   * root/
   *      a.html
   *      b.txt
   *      c.txt
   *      empty/
   *      d1/
   *         a.html
   *         b.txt
   *         c.txt
   *         empty/
   *         d1_1/
   *              a.html
   *              b.txt
   *              c.txt
   *              empty/
   *      d2/
   *         a.html
   *         b.txt
   *         c.txt
   *         empty/
   *      WEB-INF/
   *         web.xml
   * <p/>
   * </pre>
   *
   * @throws IOException
   */
  protected File[][] createDirectoryStructure(File root, String webXmlContent) throws IOException
  {
    File[] rootFiles = createSubTree(root);

    File[] d1Files = createSubTree(createDir(new File(rootFiles[ROOT], "d1")));

    File[] d2Files = createSubTree(createDir(new File(rootFiles[ROOT], "d2")));

    File[] d1_1Files = createSubTree(createDir(new File(d1Files[ROOT], "d1_1")));

    File[] webInfFiles = createWebInfFiles(createDir(new File(rootFiles[ROOT], "WEB-INF")),
                                           webXmlContent);

    return new File[][]{rootFiles, d1Files, d1_1Files, d2Files, webInfFiles};
  }

  /**
   * Create the following directory structure
   * <pre>
   * root2/
   *      a.html
   *      d.txt
   *      d1/
   *         a.html
   *         d.txt
   *         d1_1/
   *              a.html
   *              d.txt
   *              empty/
   *      d3/
   *         a.html
   *         d.txt
   * <p/>
   * </pre>
   *
   * @throws IOException
   */
  private File[][] createDirectoryStructure2(File root) throws IOException
  {
    File[] rootFiles = createSubTree(root, "a.html", null, null, null, "d.txt", "d1/", "d3/");

    File[] d1Files = createSubTree(rootFiles[6], "a.html", null, null, null, "d.txt", "d1_1/");

    File[] d3Files = createSubTree(rootFiles[7], "a.html", null, null, "empty/", "d.txt");

    File[] d1_1Files = createSubTree(d1Files[6], "a.html", null, null, null, "d.txt");

    return new File[][]{rootFiles, d1Files, d1_1Files, d3Files};
  }

  /**
   * test for jar resource
   */
  public void testTreeDirectoryJarResource()
    throws IOException, InterruptedException, URISyntaxException
  {
    File root = createTempDirectory(TestResource.class.getName(), "testTreeDirectoryJarResource");
    try
    {
      File nestedRoot = createDir(new File(root, "root"));
      
      File[][] directoryStructure = createDirectoryStructure(nestedRoot, getWebXmlContent());

      // first test where root is /
      File jarFile = createJarFile(TestResource.class.getName(), nestedRoot);
      try
      {
        // the following calls should be equivalent
        checkTreeResource(JarResource.create(FileResource.createFromRoot(jarFile)),
                          directoryStructure,
                          new TestResource.JarChecker(jarFile.toURI(), nestedRoot.toURI()));
        checkTreeResource(JarResource.create(URI.create("jar:" + jarFile.toURI() + "!/")),
                          directoryStructure,
                          new TestResource.JarChecker(jarFile.toURI(), nestedRoot.toURI()));
      }
      finally
      {
        jarFile.delete();
      }

      // second test where root is /root
      jarFile = createJarFile(TestResource.class.getName(), root);
      try
      {
        // the following calls should be equivalent
        checkTreeResource(JarResource.create(FileResource.createFromRoot(jarFile), "/root"),
                          directoryStructure,
                          new TestResource.JarChecker(jarFile.toURI(), root.toURI()));
      }
      finally
      {
        jarFile.delete();
      }

    }
    finally
    {
      IOUtils.deleteFile(root);
    }
  }

  /**
   * Checks the tree resource.
   */
  public static void checkTreeResource(Resource rootResource,
                                       File[][] files,
                                       TestResource.Checker checker)
    throws IOException, URISyntaxException
  {
    assertTrue(rootResource.exists());
    
    checkSubTree(rootResource, files[0], rootResource, files[0], checker);
    checker.checkListResource(rootResource, files[0][ROOT]);

    // d1
    Resource d1Resource = rootResource.createRelative("d1/");
    checkSubTree(rootResource, files[0], d1Resource, files[1], checker);
    checker.checkListResource(d1Resource, files[1][ROOT]);

    // we make sure that a combination of / is not affecting the result (note that since
    // d1 is at the root /d1 and d1 are equivalent)
    d1Resource = rootResource.createRelative("d1/");
    checkSubTree(rootResource, files[0], d1Resource, files[1], checker);
    checker.checkListResource(d1Resource, files[1][ROOT]);

    d1Resource = rootResource.createRelative("/d1");
    checkSubTree(rootResource, files[0], d1Resource, files[1], checker);
    checker.checkListResource(d1Resource, files[1][ROOT]);

    d1Resource = rootResource.createRelative("/d1/");
    checkSubTree(rootResource, files[0], d1Resource, files[1], checker);
    checker.checkListResource(d1Resource, files[1][ROOT]);

    // d1_1
    Resource d1_1Resource = d1Resource.createRelative("d1_1");
    checkSubTree(rootResource, files[0], d1_1Resource, files[2], checker);
    checker.checkListResource(d1_1Resource, files[2][ROOT]);

    // /d1_1 does not exists...
    checkNonExistentResource(d1Resource.getRootResource().createRelative("/d1_1"),
                             new File(files[0][ROOT], "d1_1"), checker);

    // test of chroot
    checkChroot(d1Resource, files[2][A], checker);

    // d2
    Resource d2Resource = rootResource.createRelative("d2");
    checkSubTree(rootResource, files[0], d2Resource, files[3], checker);
    checker.checkListResource(d2Resource, files[3][ROOT]);

    // WEB-INF
    Resource webInfResource = rootResource.createRelative("/WEB-INF");
    checker.checkListResource(webInfResource, files[4][ROOT]);
    checkResource(webInfResource, "web.xml", files[4][1], checker);
  }

  /**
   * Checks chroot
   */
  private static void checkChroot(Resource d1Resource, File expectedFile, TestResource.Checker checker)
    throws IOException, URISyntaxException
  {
    Resource d1_1Resource = d1Resource.chroot("d1_1/");
    assertEquals(d1_1Resource, d1_1Resource.getRootResource());
    assertEquals(d1_1Resource, d1_1Resource.getParentResource());
    checkResource(d1_1Resource, "a.html", expectedFile, checker);

    d1_1Resource = d1Resource.chroot("d1_1");
    assertEquals(d1_1Resource, d1_1Resource.getRootResource());
    assertEquals(d1_1Resource, d1_1Resource.getParentResource());
    checkResource(d1_1Resource, "a.html", expectedFile, checker);

    d1_1Resource = d1Resource.getRootResource().chroot("/d1/d1_1/");
    assertEquals(d1_1Resource, d1_1Resource.getRootResource());
    assertEquals(d1_1Resource, d1_1Resource.getParentResource());
    checkResource(d1_1Resource, "a.html", expectedFile, checker);

    d1_1Resource = d1_1Resource.chroot(".");
    assertEquals(d1_1Resource, d1_1Resource.getRootResource());
    assertEquals(d1_1Resource, d1_1Resource.getParentResource());
    checkResource(d1_1Resource, "a.html", expectedFile, checker);

    d1_1Resource = d1Resource.createRelative("d1_1/empty/").chroot("../");
    assertEquals(d1_1Resource, d1_1Resource.getRootResource());
    assertEquals(d1_1Resource, d1_1Resource.getParentResource());
    checkResource(d1_1Resource, "a.html", expectedFile, checker);

    Resource ar = d1Resource.createRelative("d1_1/a.html").chroot("");
    checkResource(ar, expectedFile, checker);
    assertEquals("/a.html", ar.getPath());

    ar = d1Resource.chroot("d1_1/a.html");
    checkResource(ar, expectedFile, checker);
    assertEquals("/a.html", ar.getPath());

  }

  /**
   * Checks the tree resource.
   */
  public static void checkTreeResourceForURL(Resource rootResource,
                                             File[][] files,
                                             TestResource.Checker checker)
    throws IOException, URISyntaxException
  {
    checkSubTree(rootResource, files[0], rootResource, files[0], checker);
    checker.checkListResource(rootResource, files[0][ROOT]);

    // d1
    Resource d1Resource = rootResource.createRelative("d1/");
    checkSubTree(rootResource, files[0], d1Resource, files[1], checker);
    checker.checkListResource(d1Resource, files[1][ROOT]);

    // d1_1
    Resource d1_1Resource = d1Resource.createRelative("d1_1/");
    checkSubTree(rootResource, files[0], d1_1Resource, files[2], checker);
    checker.checkListResource(d1_1Resource, files[2][ROOT]);

    // d2
    Resource d2Resource = rootResource.createRelative("d2/");
    checkSubTree(rootResource, files[0], d2Resource, files[3], checker);
    checker.checkListResource(d2Resource, files[3][ROOT]);
  }

  /**
   * Check the subtree: verifies createRelative(filter), and each individual resource
   */
  private static void checkSubTree(Resource rooResource,
                                   File[] rootFiles,
                                   Resource resource,
                                   File[] files,
                                   TestResource.Checker checker)
    throws IOException, URISyntaxException
  {
    assertTrue(rooResource.exists());
    
    checker.checkListResources(resource, ".foo");
    checker.checkListResources(resource, ".html", resource.createRelative("a.html"));
    checker.checkListResources(resource,
                               ".txt",
                               resource.createRelative("b.txt"),
                               resource.createRelative("c.txt"));
    checker.checkListResources(resource, "empty", resource.createRelative("empty/"));
    checker.checkListResources(resource, "empty", resource.createRelative("empty"));

    checker.checkListResources(resource, "empty", resource.createRelative("/empty/"));
    checker.checkListResources(resource, "empty", resource.createRelative("/empty"));

    checker.checkListResources(rooResource, "empty", resource.getRootResource().createRelative("/empty/"));
    checker.checkListResources(rooResource, "empty", resource.getRootResource().createRelative("/empty"));

    checkResource(resource, "a.html", files[A], checker);
    checkResource(resource, "b.txt", files[B], checker);
    checkResource(resource, "c.txt", files[C], checker);
    checkResource(resource, "empty/", files[EMPTY], checker);

    checkResource(resource.getRootResource(), "/a.html", rootFiles[A], checker);
    checkResource(resource.getRootResource(), "/b.txt", rootFiles[B], checker);
    checkResource(resource.getRootResource(), "/c.txt", rootFiles[C], checker);
    checkResource(resource.getRootResource(), "/empty/", rootFiles[EMPTY], checker);

    // checking that . ./ and "" are working correctly
    assertEquals(resource.createRelative("empty").toURI(),
                 resource.createRelative("empty").createRelative(".").toURI());

    assertEquals(resource.createRelative("empty/").toURI(),
                 resource.createRelative("empty/").createRelative(".").toURI());

    assertEquals(resource.createRelative("empty/").toURI(),
                 resource.createRelative("empty/").createRelative("./").toURI());

    assertEquals(resource.createRelative("empty").toURI(),
                 resource.createRelative("empty").createRelative("").toURI());

    assertTrue(resource.createRelative("/").getPath().endsWith("/"));
    assertTrue(resource.createRelative("a.html").createRelative("/").getPath().endsWith("/"));

    checkEmptyDirectory(files[EMPTY], resource.createRelative("empty/"), checker);

    checkParentResource(resource);
    checkRootResource(resource, rooResource);
  }

  /**
   * We check the parent resource functionnality
   *
   * @param resourceDir
   * @throws IOException
   */
  private static void checkParentResource(Resource resourceDir) throws IOException
  {
    checkResource(resourceDir.createRelative("a.html").getParentResource(), resourceDir);
    checkResource(resourceDir.createRelative("a.html").createRelative(".."), resourceDir);
    checkResource(resourceDir.createRelative("a.html").createRelative("../b.txt"),
                  resourceDir.createRelative("b.txt"));
  }

  /**
   * Compares 2 resources..
   *
   * @param resource
   * @param expectedResource
   */
  private static void checkResource(Resource resource, Resource expectedResource)
  {
    assertEquals(expectedResource.isDirectory(), resource.isDirectory());
    assertEquals(expectedResource.getFilename(), resource.getFilename());
    checkPath(expectedResource.getPath(), resource.getPath());
    assertEquals(PathUtils.removeTrailingSlash(expectedResource.toURI().toString()),
                 PathUtils.removeTrailingSlash(resource.toURI().toString()));
  }

  /**
   * The problem with paths is that there may be a trailing slash or not depending on the
   * implementation...
   * 
   * @param path
   * @param expectedPath
   */
  private static void checkPath(String path, String expectedPath)
  {
    if(expectedPath.equals("/"))
      assertEquals("/", path);
    else
      assertEquals(PathUtils.removeTrailingSlash(expectedPath),
                   PathUtils.removeTrailingSlash(path));
  }

  /**
   * We check the root resource functionnality
   *
   * @throws IOException
   */
  private static void checkRootResource(Resource resource, Resource expectedRootResource) throws IOException
  {
    checkResource(resource.getRootResource(), expectedRootResource);
    checkResource(resource.getRootResource().createRelative("/"), expectedRootResource);
    checkResource(resource.getRootResource().createRelative("/.././"), expectedRootResource);
  }

  /**
   * Check an individual resource and compares it to the original file. Even read the content
   */
  private static void checkResource(Resource root, String relativePath, File file, TestResource.Checker checker)
    throws IOException, URISyntaxException
  {
    checkResource(root.createRelative(relativePath), file, checker);
  }

  /**
   * Check an individual resource and compares it to the original file. Even read the content
   */
  private static void checkResource(Resource resource, File file, TestResource.Checker checker)
    throws IOException, URISyntaxException
  {
    checker.checkInfo(resource, file);

    if(file.isDirectory())
    {
      checker.checkDirectoryContent(resource);
    }
    else
    {
      assertEquals(readContent(file), readContent(resource));
    }

    checker.checkURI(resource, file);

    assertEquals(file.getName(), resource.getFilename());
  }

  /**
   * Compare the 2 arrays without caring about the order of the elements in the array
   *
   * @param msg
   * @param computed
   * @param expected
   * @throws IOException
   */
  private static void assertEqualsOrderDontCare(String msg, Resource[] computed, Resource... expected)
    throws IOException
  {
    if(expected == null)
    {
      assertNull("computed should be null", computed);
      return;
    }

    assertNotNull("computed should not be null", computed);

    assertEquals(expected.length, computed.length);

    if(expected.length == 0)
      return;
    
    if(expected.length > 1)
    {
      Set<String> expectedURIs = new HashSet<String>(expected.length);
      for(Resource resource : expected)
      {
        String uri = resource.toString();

        uri = PathUtils.removeTrailingSlash(uri);

        expectedURIs.add(uri);
      }

      Set<String> computedURIs = new HashSet<String>(computed.length);
      for(Resource resource : computed)
      {
        String uri = resource.toString();

        uri = PathUtils.removeTrailingSlash(uri);

        computedURIs.add(uri);
      }

      assertEquals(msg, expectedURIs, computedURIs);
    }
    else
    {
      checkResource(computed[0], expected[0]);
    }
  }

  /**
   * Creates a subtree in the given directory.
   *
   * @param root
   * @return the file objects created (using the constants)
   * @throws IOException
   */
  private File[] createSubTree(File root) throws IOException
  {
    return createSubTree(root, "a.html", "b.txt", "c.txt", "empty/");
  }

  /**
   * Creates a subtree in the given directory.
   *
   * @param root
   * @return the file objects created (using the constants)
   * @throws IOException
   */
  private File[] createSubTree(File root, String... paths) throws IOException
  {
    File[] res = new File[paths.length + 1];

    res[ROOT] = root;
    for(int i = 0; i < paths.length; i++)
    {
      File file = null;

      String path = paths[i];

      if(path != null)
      {
        if(path.endsWith("/"))
        file = createDir(new File(root, path));
        else
          file = createFile(new File(root, path));
      }

      res[i+1] = file;

    }

    return res;
  }

  /**
   * Creates the web-inf files. Currently web.xml.
   */
  private File[] createWebInfFiles(File dir, String webXmlContent) throws IOException
  {
    File[] res = new File[2];

    res[0] = dir;
    if(webXmlContent != null)
      res[1] = createFile(new File(dir, "web.xml"), webXmlContent);

    return res;
  }

  /**
   * Creates a temporary directory.
   *
   * @param namespace
   * @param name      root name of the temporary directory
   * @return the temp dir
   * @throws IOException if there is a problem
   */
  public File createTempDirectory(String namespace, String name) throws IOException
  {
    File tempDirectory = IOUtils.createTempDirectory(namespace, name);
    _deleteOnExit.add(tempDirectory.getParentFile());
    return tempDirectory;
  }

  /**
   * Creates a file and populate with the content.
   *
   * @param file
   * @return the file provided for convenience...
   */
  public static File createFile(File file) throws IOException
  {
    return createFile(file, "{" + file.getCanonicalPath() + "}");
  }

  /**
   * Creates a file and populate with the content.
   *
   * @param file
   * @param content
   * @return the file provided for convenience...
   */
  public static File createFile(File file, String content) throws IOException
  {
    file.deleteOnExit();

    FileWriter fw = new FileWriter(file);
    try
    {
      BufferedWriter writer = new BufferedWriter(fw);
      writer.write(content);
      writer.flush();

      return file.getCanonicalFile();
    }
    finally
    {
      fw.close();
    }
  }

  /**
   * Encapsulates the creation of a directory
   *
   * @param dir
   * @return the directory provided for convenience
   * @throws IOException
   */
  public static File createDir(File dir) throws IOException
  {
    IOUtils.createNewDirectory(dir);
    dir.deleteOnExit();
    return dir.getCanonicalFile();
  }

  /**
   * Reads the full content of the resource
   *
   * @param resource
   * @return the full content
   * @throws IOException
   */
  public static String readContent(Resource resource) throws IOException
  {
    InputStream is = resource.getInputStream();
    try
    {
      return readContent(is);
    }
    finally
    {
      is.close();
    }
  }

  /**
   * Reads the full content of the file
   *
   * @param file
   * @return the full content
   * @throws IOException
   */
  public static String readContent(File file) throws IOException
  {
    InputStream is = new FileInputStream(file);
    try
    {
      return readContent(is);
    }
    finally
    {
      is.close();
    }
  }

  /**
   * Reads the content of the input stream and return it as a string.
   *
   * @param is
   * @return the content as a string
   * @throws IOException
   */
  public static String readContent(InputStream is) throws IOException
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    IOUtils.copy(is, baos);
    return new String(baos.toByteArray());
  }

  /**
   * Creates a jar file with all the files contained in root. (Uses jar command)
   *
   * @param namespace
   * @param root
   * @return the jar file
   * @throws IOException
   * @throws InterruptedException
   */
  public File createJarFile(String namespace, File root)
    throws IOException, InterruptedException
  {
    File jarDirectory = createTempDirectory(namespace, root.getName() + "_jar");
    File jarFile = new File(jarDirectory, root.getName() + ".jar");
    return copyToJar(jarFile, root, false);
  }

  /**
   * Creates a jar file with all the files contained in root. (Uses jar command)
   *
   * @param jarFile to put the files in
   * @param root
   * @return the jar file
   * @throws IOException
   * @throws InterruptedException
   */
  public static File copyToJar(File jarFile, File root, boolean update)
    throws IOException, InterruptedException
  {
    ExternalCommand cmd =
      ExternalCommand.create("jar", "-" + (update ? "u" : "c") +  "vfM",
                             jarFile.getCanonicalPath(), 
                             ".");
    cmd.setWorkingDirectory(root);
    cmd.start();
    cmd.waitFor();

    assertEquals(0, cmd.exitValue());

    jarFile.deleteOnExit();
    return jarFile.getCanonicalFile();
  }

}
