/*
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
package test.util.io

import org.linkedin.groovy.util.io.fs.FileSystemImpl
import org.linkedin.groovy.util.io.fs.FileSystem
import org.linkedin.util.io.resource.Resource
import org.linkedin.groovy.util.io.GroovyIOUtils

import org.linkedin.groovy.util.net.GroovyNetUtils
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.Headers
import org.linkedin.groovy.util.json.JsonUtils

/**
 * @author yan@pongasoft.com */
public class TestGroovyIOUtils extends GroovyTestCase
{
  public void testCat()
  {
    FileSystemImpl.createTempFileSystem { FileSystem fs ->

      Resource r1 = fs.saveContent('/dir/file1', 'abcd')

      assertEquals('abcd', GroovyIOUtils.cat(r1.toURI()))
      assertEquals('abcd', GroovyIOUtils.cat(r1))
      assertEquals('abcd', GroovyIOUtils.cat(r1.file.canonicalPath))
    }
  }

  public void testJsonSortedSerialization()
  {
    def str1 = 'foo'
    def str2 = 'bar'
    def map = [b: 'v1', a: "${str1}!=${str2}"]    // This map contains a groovy.lang.GString descendant
    def deserialized_map = JsonUtils.fromJSON(JsonUtils.compactPrint(map))
    assertEquals(map['b'], deserialized_map['b'])
    assertEquals(map['a'], deserialized_map['a'])
    assertEquals(map.size(), deserialized_map.size())
    assertEquals("""{
  "a" : "foo!=bar",
  "b" : "v1"
}""", JsonUtils.prettyPrint(map))
    map = [z: null, a: 'v1', d: ['t2', 't1'], c: 'v3', b: [a: 'b1', c:  'b2', d: ['foo', 'bar'], b: 'b3']];
    assertEquals("""{
  "a" : "v1",
  "b" : {
    "a" : "b1",
    "b" : "b3",
    "c" : "b2",
    "d" : [ "foo", "bar" ]
  },
  "c" : "v3",
  "d" : [ "t2", "t1" ]
}""", JsonUtils.prettyPrint(map))
    map = [a: 'v1', d: 'v2', c: 'v3', b: 'v4'];
    assertEquals("""{
  "a" : "v1",
  "b" : "v4",
  "c" : "v3",
  "d" : "v2"
}""", JsonUtils.prettyPrint(map))
  }

  public void testWithFile()
  {
    GroovyNetUtils.withHttpEchoServer { int port ->
      FileSystemImpl.createTempFileSystem { FileSystem fs ->
        assertEquals("abcd", GroovyIOUtils.cat(new URI("http://localhost:${port}/echo?msg=abcd")))
        Resource r1 = fs.saveContent('/dir/file1', 'abcd')

        GroovyIOUtils.withFile(r1) { File f ->
          assertEquals(r1.file, f)
          assertEquals("abcd", f.text)
        }

        GroovyIOUtils.withFile(r1.toURI()) { File f ->
          assertEquals(r1.file, f)
          assertEquals("abcd", f.text)
        }

        GroovyIOUtils.withFile(r1.file) { File f ->
          assertEquals(r1.file, f)
          assertEquals("abcd", f.text)
        }

        GroovyIOUtils.withFile(r1.file.canonicalPath) { File f ->
          assertEquals(r1.file, f)
          assertEquals("abcd", f.text)
        }

        GroovyIOUtils.withFile(null) { File f ->
          assertNull(f)
        }

        // this pattern is exactly the kind of pattern that should *not* be followed!
        // here I am testing that once the closure is over the file is properly deleted
        File tempFile = null

        GroovyIOUtils.withFile("http://localhost:${port}/echo?msg=abcd") { File f ->
          tempFile = f
          assertTrue(tempFile.exists())
          assertEquals("abcd", f.text)
        }

        assertFalse(tempFile.exists())

        // r1 should still exists
        assertTrue(r1.exists())
      }
    }
  }

  /**
   * Verifys that testSafeOverwrite properly works
   */
  public void testSafeOverwrite()
  {
    FileSystemImpl.createTempFileSystem { FileSystem fs ->
      File root = fs.root.file

      File tmpFile = null
      GroovyIOUtils.safeOverwrite(new File(root, "foo.txt")) { File f ->
        tmpFile = f

        // should be a non existent file
        assertFalse(f.exists())

        // should be in the same folder as 'foo.txt'
        assertEquals(root, f.parentFile)
        assertTrue(f.name.startsWith('++tmp.'))
        assertTrue(f.name.endsWith('.tmp++'))

        f.text = "abcd"
      }
      // after the call the file should be deleted
      assertFalse(tmpFile.exists())

      // the file foo.txt should have been created
      assertEquals("abcd", new File(root, "foo.txt").text)

      // error cases
      File foo2 = new File(root, "foo2.txt")
      foo2.text = 'defg'

      assertEquals("forcing: klmn", shouldFail(Exception.class) {
        GroovyIOUtils.safeOverwrite(foo2) { File f ->
          tmpFile = f
          f.text = 'klmn'
          throw new Exception('forcing: klmn')
        }
      })

      // after the call the file should be deleted
      assertFalse(tmpFile.exists())

      // the original file should be the same
      assertEquals('defg', foo2.text)

      // if file is never written, it means the original should not exist
      GroovyIOUtils.safeOverwrite(foo2) { File f ->
        tmpFile = f
        // don't do anything with f
      }

      // after the call the file should be deleted
      assertFalse(tmpFile.exists())

      // the original file should be the same
      assertEquals('defg', foo2.text)

      // now test for factory
      GroovyIOUtils.safeOverwrite(foo2, {File f -> new File(f.parentFile, "lolo")}) { File f ->
        tmpFile = f
        assertEquals(new File(root, 'lolo'), f)
        f.text = 'hijk'
      }

      // after the call the file should be deleted
      assertFalse(tmpFile.exists())

      // the original file should have been updated
      assertEquals('hijk', foo2.text)
    }
  }

  /**
   * Test for fetch content. Make sure that authorization works properly
   */
  public void testFetchContent()
  {
    String response
    Headers requestHeaders

    def handler = { HttpExchange t ->
      requestHeaders = t.requestHeaders
      t.sendResponseHeaders(200, response.length());
      OutputStream os = t.getResponseBody();
      os.write(response.getBytes());
      os.close();
    }

    GroovyNetUtils.withHttpServer(0, ['/content': handler]) { int port ->

      FileSystemImpl.createTempFileSystem { FileSystem fs ->
        File root = fs.root.file

        File tmpFile = new File(root, 'foo.txt')

        response = "abc"

        GroovyIOUtils.fetchContent("http://localhost:${port}/content", tmpFile)
        assertEquals("abc", tmpFile.text)
        // no authorization header should be present!
        assertFalse(requestHeaders.containsKey('Authorization'))

        response = "def"
        GroovyIOUtils.fetchContent("http://u1:p1@localhost:${port}/content", tmpFile)
        assertEquals("def", tmpFile.text)
        // authorization header should be present and properly base64ified
        assertEquals("Basic ${'u1:p1'.bytes.encodeBase64()}",
                     requestHeaders['Authorization'].iterator().next())
      }
    }
  }
}
