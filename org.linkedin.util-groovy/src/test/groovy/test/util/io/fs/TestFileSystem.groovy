/*
 * Copyright 2010-2010 LinkedIn, Inc
 * Copyright (c) 2011 Yan Pujante
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


package test.util.io.fs

import org.linkedin.groovy.util.io.fs.FileSystemImpl
import org.linkedin.util.io.resource.Resource
import org.linkedin.groovy.util.ant.AntUtils
import org.linkedin.groovy.util.io.GroovyIOUtils

/**
 * Test for FileSystem class
 *
 * @author ypujante@linkedin.com
 */
class TestFileSystem extends GroovyTestCase
{
  FileSystemImpl fs

  protected void setUp()
  {
    super.setUp();

    fs = FileSystemImpl.createTempFileSystem()
  }

  protected void tearDown()
  {
    fs.destroy()
  }

  void testDirs()
  {
    assertTrue(fs.root.file.path.startsWith(new File(System.getProperty('java.io.tmpdir')).canonicalPath))
    assertEquals('/', fs.root.path)

    // creating a directory
    assertFalse(fs.root.createRelative('a/b/c').exists())
    assertEquals(fs.root.createRelative('a/b/c'), fs.mkdirs('a/b/c'))
    assertEquals(fs.root.createRelative('a/b/c'), fs.mkdirs('/a/b/c'))
    assertTrue(fs.root.createRelative('a/b/c').exists())

    // essentially does nothing... because it is a directory, not a file!
    fs.rm('a/b')
    assertTrue(fs.root.createRelative('a/b/c').exists())

    // now this one should work
    fs.rmdirs('a/b')
    assertFalse(fs.root.createRelative('a/b').exists())
    assertTrue(fs.root.createRelative('a').exists())
  }

  void testRelative()
  {
    assertEquals('/', fs.toResource(fs.root).path)
    assertEquals('/a/b', fs.toResource('a/b').path)
    assertEquals('/a/b', fs.toResource('/a/b').path)
    assertEquals('/a/b', fs.toResource(new File(fs.root.file, '/a/b')).path)
  }

  void testContent()
  {
    assertEquals(fs.root.createRelative('a/b/c.txt'), fs.saveContent('a/b/c.txt', 'test string'))
    assertTrue fs.root.createRelative('a/b/c.txt').exists()
    assertEquals('test string', fs.root.createRelative('a/b/c.txt').file.getText())
    assertEquals('test string', fs.readContent('a/b/c.txt'))
  }

  void testListFiles()
  {
    File test1 = fs.saveContent('a/b/c.txt', 'test1').file
    assertEquals(fs.root.createRelative('a/b/c.txt').file, test1)
    File test2 = fs.saveContent('a/b/c/d.txt', 'test2').file
    assertEquals(fs.root.createRelative('a/b/c/d.txt').file, test2)
    File test3 = fs.saveContent('b/c/d.txt', 'test3').file
    assertEquals(fs.root.createRelative('b/c/d.txt').file, test3)
    File test4 = fs.saveContent('a.txt', 'test4').file
    assertEquals(fs.root.createRelative('a.txt').file, test4)

    def files = fs.ls() {
      include(name: '**/*.txt')
    }

    assertEquals([test4, test1, test2, test3], files.file)

    files = fs.ls() {
      include(name: '**/*.txt')
      exclude(name: 'a/**/*.txt')
    }

    files = fs.ls('a') {
      include(name: '**/*.txt')
    }

    assertEquals([test1, test2], files.file)
  }

  /**
   * we make sure that the files returned by the filesystem can be reused as is...
   */
  void testFileReuse()
  {
    File test1 = fs.saveContent('a/b/c.txt', 'test1').file
    File test2 = fs.saveContent('a/b/c/d.txt', 'test2').file
    File test3 = fs.saveContent('b/c/d.txt', 'test3').file
    File test4 = fs.saveContent('a.txt', 'test4').file

    assertTrue(test1.exists())

    def files = fs.ls() {
      include(name: '**/*')
    }

    // contains files and directories
    assertEquals(9, files.size())
    assertTrue(files.file.contains(test4))
    
    fs.rm(test4)

    files = fs.ls() {
      include(name: '**/*')
    }

    assertEquals(8, files.size())
    assertFalse(files.file.contains(test4))

    assertTrue(files.file.contains(test1))

    def fs2 = FileSystemImpl.createTempFileSystem()
    try
    {
      File testFS2_1 = fs2.saveContent('a/b/c.txt', 'test1').file

      // we ensure that using a file produced by one fs does not work in a different fs...
      fs.rm(testFS2_1)
      assertTrue(files.file.contains(test1))
    }
    finally
    {
      fs2.destroy()
    }
  }


  /**
   * We make sure that weird characters in file paths still work 
   */
  void testWeirdCharactersInPath()
  {
    File test1 = fs.saveContent('a b/c d/d.txt', 'test1').file

    assertTrue(test1.exists())

    def files = fs.ls() {
      include(name: '**/*')
    }

    assertEquals(3, files.size())
  }

  void testSerialization()
  {
    // TODO MED YP:  add more tests...
  }

  void testDestroy()
  {
    // TODO MED YP:  add more tests...
  }

  void testNewFilesystem()
  {
    // TODO MED YP:  add more tests...
  }

  void testCopy()
  {
    def test1 = fs.saveContent('/a/b/c/d.txt', 'test1')
    assertFalse(fs.toResource('/e/f.txt').exists())
    def test2 = fs.cp(test1, '/e/f.txt')
    assertEquals('/e/f.txt', test2.path)
    assertEquals('test1', test2.file.getText())

    // /e/g does not exist => file
    def test3 = fs.cp(test1, '/e/g')
    assertEquals('/e/g', test3.path)
    assertEquals('test1', test3.file.getText())

    // /e/h is a directory => copied into the directory
    fs.mkdirs('/e/h')
    def test4 = fs.cp(test1, '/e/h')
    assertEquals('/e/h/d.txt', test4.path)
    assertEquals('test1', test4.file.getText())

    // /e/i/ is a directory and does not exist
    shouldFail(FileNotFoundException) { fs.cp(test1, '/e/i/') }
  }

  void testMove()
  {
    def test1 = fs.saveContent('/a/b/c/d.txt', 'test1')
    assertFalse(fs.toResource('/e/f.txt').exists())
    def test2 = fs.mv(test1, '/e/f.txt')
    assertEquals('/e/f.txt', test2.path)
    assertEquals('test1', test2.file.getText())
    assertFalse(test1.exists())

    // /e/g does not exist => file
    def test3 = fs.mv('/e/f.txt', '/e/g')
    assertEquals('/e/g', test3.path)
    assertEquals('test1', test3.file.getText())
    assertFalse(test2.exists())

    // /e/h is a directory => moved into the directory
    fs.mkdirs('/e/h')
    def test4 = fs.mv('/e/g', '/e/h')
    assertEquals('/e/h/g', test4.path)
    assertEquals('test1', test4.file.getText())
    assertFalse(test3.exists())

    fs.saveContent('/e/j/i.txt', 'test2')
    def testz = fs.mv('/e/j', '/e/z')
    assertTrue(testz.'i.txt'.exists())
    assertEquals('test2', fs.toResource('/e/z/i.txt').file.text)
    assertFalse(fs.toResource('/e/j/i.txt').exists())

    // /e/i/ is a directory and does not exist
    shouldFail(FileNotFoundException) { fs.mv('/e/h/g', '/e/i/') }

    def test5 = fs.mv('/e/h/g', '/e/i/d.txt')
    assertEquals('/e/i/d.txt', test5.path)
    assertEquals('test1', test5.file.getText())
    assertFalse(test4.exists())
  }

  void testRmEmptyDirs()
  {
    def root = fs.mkdirs('/root')

    fs.saveContent('/root/a', 'a1')
    fs.mkdirs('/root/dir1')
    fs.saveContent('/root/dir2/a', 'a2')
    fs.mkdirs('/root/dir2/dir1')
    fs.saveContent('/root/dir2/dir2/a', 'a3')

    fs.rmEmptyDirs(root)

    assertEquals('a1', fs.readContent('/root/a'))
    assertEquals('a2', fs.readContent('/root/dir2/a'))
    assertEquals('a3', fs.readContent('/root/dir2/dir2/a'))

    assertFalse(fs.toResource('/root/dir1').exists())
    assertFalse(fs.toResource('/root/dir2/dir1').exists())

    fs.rm('/root/dir2/dir2/a')
    fs.rmEmptyDirs(root)

    assertEquals('a1', fs.readContent('/root/a'))
    assertEquals('a2', fs.readContent('/root/dir2/a'))
    assertFalse(fs.toResource('/root/dir2/dir2').exists())

    fs.rm('/root/dir2/a')
    fs.rmEmptyDirs(root)

    assertEquals('a1', fs.readContent('/root/a'))
    assertFalse(fs.toResource('/root/dir2').exists())

    fs.rm('/root/a')
    fs.rmEmptyDirs(root)

    assertFalse(root.exists())

    // only empty dirst to start
    root = fs.mkdirs('/root/dir1/dir2/dir3')
    assertTrue(fs.toResource('/root/dir1/dir2/dir3').exists())
    fs.rmEmptyDirs(root)
    assertFalse(fs.toResource('/root/dir1/dir2/dir3').exists())
    assertFalse(root.exists())
  }

  void testSafeOverwrite()
  {
    Resource fileToCreate = fs.toResource('/dir/file1')

    assertFalse(fileToCreate.exists())

    Object expectedResult = new Object()

    Object res = fs.safeOverwrite(fileToCreate) { Resource resource ->
      assertNotSame(resource, fileToCreate)
      fs.saveContent(resource, "abcd")
      return expectedResult
    }

    assertEquals(expectedResult, res)
    assertTrue(fileToCreate.exists())
    assertEquals("abcd", fs.readContent(fileToCreate))

    // there should be only 1 file in dir
    assertEquals(1, fs.ls('/dir').size())

    // now it should replace the file
    res = fs.safeOverwrite(fileToCreate) { Resource resource ->
      assertNotSame(resource, fileToCreate)
      fs.saveContent(resource, "efgh")
      return expectedResult
    }

    assertEquals(expectedResult, res)
    assertTrue(fileToCreate.exists())
    assertEquals("efgh", fs.readContent(fileToCreate))

    // there should be only 1 file in dir
    assertEquals(1, fs.ls('/dir').size())

    fs.chmod('/dir', '500')

    shouldFail(IOException) {
      fs.safeOverwrite(fileToCreate) { Resource resource ->
        assertNotSame(resource, fileToCreate)
        fs.saveContent(resource, "ijkl")
        return expectedResult
      }
    }

    assertTrue(fileToCreate.exists())
    assertEquals("efgh", fs.readContent(fileToCreate)) // not changed

    // there should be only 1 file in dir
    assertEquals(1, fs.ls('/dir').size())

    // restore access rights
    fs.chmod('/dir', '755')
  }

  /**
   * Test to fix the bug reported in glu (https://github.com/linkedin/glu/issues/95)
   */
  public void testToResource()
  {
    def path1 = "/foo/toto.xml"
    def path2 = "foo/toto.xml"

    Resource r = fs.toResource(path1)

    assertEquals(r, fs.toResource(path1))
    assertEquals(r, fs.toResource("${path1}"))
    assertEquals(r, fs.toResource(path2))
    assertEquals(r, fs.toResource("${path2}"))
  }

  /**
   * Test for making sure that that symlinks are working, for fixing a but reported in glu
   * (https://github.com/linkedin/glu/issues/165)
   */
  public void testSymlinks()
  {
    def topdir = fs.mkdirs('/topdir')
    def topdir2 = fs.mkdirs('/topdir2')

    assertEquals([], fs.ls(topdir))

    def atxt = fs.saveContent(topdir.'a.txt', "a-content")

    assertEquals([atxt], fs.ls(topdir))

    def btxt = topdir2.'b.txt'

    assertFalse(btxt.exists())

    "ln -s ${atxt.file} ${btxt.file}".execute().waitFor()

    assertEquals([atxt], fs.ls(topdir))
    assertEquals([btxt], fs.ls(topdir2))

    assertTrue(btxt.exists())
    assertEquals("a-content", btxt.file.text)

  }
}
