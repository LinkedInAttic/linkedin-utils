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
}