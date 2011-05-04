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
package test.util.lang

import org.linkedin.groovy.util.lang.GroovyLangUtils
import org.linkedin.util.lang.LangUtils

/**
 * @author yan@pongasoft.com */
public class TestGroovyLangUtils extends GroovyTestCase
{
  /**
   * Test no exception with closure only
   */
  public void testNoExceptionClosureOnly()
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream()
    System.err = new PrintStream(baos)

    def mock = new TestGroovyLangUtilsMockLogger()
    GroovyLangUtils.log = mock

    // no exception thrown / no debug
    mock.reset(false)
    assertEquals(3, GroovyLangUtils.noException { return 3 })
    assertEquals([], mock.warnings)
    assertEquals([], mock.debugs)

    // no exception thrown / debug
    mock.reset(true)
    assertEquals(3, GroovyLangUtils.noException { return 3 })
    assertEquals([], mock.warnings)
    assertEquals([], mock.debugs)

    // exception thrown / no debug
    mock.reset(false)
    assertEquals(GroovyLangUtils.NOEXCEPTION_ERROR,
                 GroovyLangUtils.noException { throw new Exception("e1") })

    assertEquals(["Detected unexpected exception [ignored]: java.lang.Exception: e1"], mock.warnings)
    assertEquals([], mock.debugs)

    // exception thrown / debug
    mock.reset(true)
    Exception e2 = new Exception("e2")
    assertEquals(GroovyLangUtils.NOEXCEPTION_ERROR,
                 GroovyLangUtils.noException { throw e2 })

    assertEquals(["Detected unexpected exception [ignored]: java.lang.Exception: e2"], mock.warnings)
    assertEquals([["Detected unexpected exception [ignored]", e2]], mock.debugs)

    assertEquals(0, baos.size())
  }

  /**
   * Test no exception when provided a value on exception
   */
  public void testNoExceptionWithValueOnException()
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream()
    System.err = new PrintStream(baos)

    def mock = new TestGroovyLangUtilsMockLogger()
    GroovyLangUtils.log = mock

    // no exception thrown / no debug
    mock.reset(false)
    assertEquals(3, GroovyLangUtils.noExceptionWithValueOnException(4) { return 3 })
    assertEquals([], mock.warnings)
    assertEquals([], mock.debugs)

    // no exception thrown / debug
    mock.reset(true)
    assertEquals(3, GroovyLangUtils.noExceptionWithValueOnException(4) { return 3 })
    assertEquals([], mock.warnings)
    assertEquals([], mock.debugs)

    // exception thrown / no debug
    mock.reset(false)
    assertEquals(4,
                 GroovyLangUtils.noExceptionWithValueOnException(4) { throw new Exception("e1") })

    assertEquals(["Detected unexpected exception [ignored]: java.lang.Exception: e1"], mock.warnings)
    assertEquals([], mock.debugs)

    // exception thrown / debug
    mock.reset(true)
    Exception e2 = new Exception("e2")
    assertEquals(4,
                 GroovyLangUtils.noExceptionWithValueOnException(4) { throw e2 })

    assertEquals(["Detected unexpected exception [ignored]: java.lang.Exception: e2"], mock.warnings)
    assertEquals([["Detected unexpected exception [ignored]", e2]], mock.debugs)

    // there should be nothing in system.err by now
    assertEquals(0, baos.size())


    // exception thrown within warning / no debug
    mock.reset(false)
    Exception w3 = new Exception("w3")
    mock.warnClosure = { String msg -> mock.warnings << msg; throw w3; }
    Exception e3 = new Exception("e3")
    assertEquals(4,
                 GroovyLangUtils.noExceptionWithValueOnException(4) { throw e3 })

    assertEquals(["Detected unexpected exception [ignored]: java.lang.Exception: e3"], mock.warnings)
    assertEquals([], mock.debugs)
    assertEquals("""Error detected while logging output.. trying System.err
${LangUtils.getStackTrace(e3)}${LangUtils.getStackTrace(w3)}""", baos.toString())

    // exception thrown within warning / debug (debug never reached)
    baos = new ByteArrayOutputStream()
    System.err = new PrintStream(baos)
    mock.reset(true)
    Exception w4 = new Exception("w4")
    mock.warnClosure = { String msg -> mock.warnings << msg; throw w4; }
    Exception e4 = new Exception("e4")
    assertEquals(4,
                 GroovyLangUtils.noExceptionWithValueOnException(4) { throw e4 })

    assertEquals(["Detected unexpected exception [ignored]: java.lang.Exception: e4"], mock.warnings)
    assertEquals([], mock.debugs)
    assertEquals("""Error detected while logging output.. trying System.err
${LangUtils.getStackTrace(e4)}${LangUtils.getStackTrace(w4)}""", baos.toString())

    // exception thrown within debug
    baos = new ByteArrayOutputStream()
    System.err = new PrintStream(baos)
    mock.reset(true)
    Exception w5 = new Exception("w5")
    mock.debugClosure = { String msg, Throwable th -> mock.debugs << [msg, th]; throw w5; }
    Exception e5 = new Exception("e5")
    assertEquals(4,
                 GroovyLangUtils.noExceptionWithValueOnException(4) { throw e5 })

    assertEquals(["Detected unexpected exception [ignored]: java.lang.Exception: e5"], mock.warnings)
    assertEquals([["Detected unexpected exception [ignored]", e5]], mock.debugs)
    assertEquals("""Error detected while logging output.. trying System.err
${LangUtils.getStackTrace(e5)}${LangUtils.getStackTrace(w5)}""", baos.toString())
  }

  /**
   * Test no exception when provided a value on exception and message
   */
  public void testNoException3()
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream()
    System.err = new PrintStream(baos)

    def mock = new TestGroovyLangUtilsMockLogger()
    GroovyLangUtils.log = mock

    // no exception thrown / no debug
    mock.reset(false)
    assertEquals(3, GroovyLangUtils.noException("m1", 4) { return 3 })
    assertEquals([], mock.warnings)
    assertEquals([], mock.debugs)

    // no exception thrown / debug
    mock.reset(true)
    assertEquals(3, GroovyLangUtils.noException("m1", 4) { return 3 })
    assertEquals([], mock.warnings)
    assertEquals([], mock.debugs)

    // exception thrown / no debug
    mock.reset(false)
    assertEquals(4,
                 GroovyLangUtils.noException("m1", 4) { throw new Exception("e1") })

    assertEquals(["Detected unexpected exception [m1] [ignored]: java.lang.Exception: e1"], mock.warnings)
    assertEquals([], mock.debugs)

    // exception thrown / debug
    mock.reset(true)
    Exception e2 = new Exception("e2")
    assertEquals(4,
                 GroovyLangUtils.noException("m2", 4) { throw e2 })

    assertEquals(["Detected unexpected exception [m2] [ignored]: java.lang.Exception: e2"], mock.warnings)
    assertEquals([["Detected unexpected exception [m2] [ignored]", e2]], mock.debugs)

    // there should be nothing in system.err by now
    assertEquals(0, baos.size())


    // exception thrown within warning / no debug
    mock.reset(false)
    Exception w3 = new Exception("w3")
    mock.warnClosure = { String msg -> mock.warnings << msg; throw w3; }
    Exception e3 = new Exception("e3")
    assertEquals(4,
                 GroovyLangUtils.noException("m3", 4) { throw e3 })

    assertEquals(["Detected unexpected exception [m3] [ignored]: java.lang.Exception: e3"], mock.warnings)
    assertEquals([], mock.debugs)
    assertEquals("""Error detected while logging output.. trying System.err
${LangUtils.getStackTrace(e3)}${LangUtils.getStackTrace(w3)}""", baos.toString())

    // exception thrown within warning / debug (debug never reached)
    baos = new ByteArrayOutputStream()
    System.err = new PrintStream(baos)
    mock.reset(true)
    Exception w4 = new Exception("w4")
    mock.warnClosure = { String msg -> mock.warnings << msg; throw w4; }
    Exception e4 = new Exception("e4")
    assertEquals(4,
                 GroovyLangUtils.noException("m4", 4) { throw e4 })

    assertEquals(["Detected unexpected exception [m4] [ignored]: java.lang.Exception: e4"], mock.warnings)
    assertEquals([], mock.debugs)
    assertEquals("""Error detected while logging output.. trying System.err
${LangUtils.getStackTrace(e4)}${LangUtils.getStackTrace(w4)}""", baos.toString())

    // exception thrown within debug
    baos = new ByteArrayOutputStream()
    System.err = new PrintStream(baos)
    mock.reset(true)
    Exception w5 = new Exception("w5")
    mock.debugClosure = { String msg, Throwable th -> mock.debugs << [msg, th]; throw w5; }
    Exception e5 = new Exception("e5")
    assertEquals(4,
                 GroovyLangUtils.noException("m5", 4) { throw e5 })

    assertEquals(["Detected unexpected exception [m5] [ignored]: java.lang.Exception: e5"], mock.warnings)
    assertEquals([["Detected unexpected exception [m5] [ignored]", e5]], mock.debugs)
    assertEquals("""Error detected while logging output.. trying System.err
${LangUtils.getStackTrace(e5)}${LangUtils.getStackTrace(w5)}""", baos.toString())

    // toString throws exception / debug
    baos = new ByteArrayOutputStream()
    System.err = new PrintStream(baos)
    mock.reset(true)
    Exception e6 = new Exception("e6")
    Exception e7 = new Exception("e7")
    assertEquals(4,
                 GroovyLangUtils.noException(new ToStringThrowsExeption(e: e6), 4) { throw e7 })

    assertEquals(["Detected unexpected exception [ignored]: java.lang.Exception: e6", "Detected unexpected exception [ignored]: java.lang.Exception: e7"], mock.warnings)
    assertEquals([["Detected unexpected exception [ignored]", e6], ["Detected unexpected exception [ignored]", e7]], mock.debugs)

    // there should be nothing in system.err
    assertEquals(0, baos.size())
  }
}

class TestGroovyLangUtilsMockLogger
{
  Closure warnClosure
  Closure debugClosure
  
  def warnings = []
  def debugs = []

  boolean isDebugEnabled = false

  void warn(String msg)
  {
    warnClosure(msg)
  }

  boolean isDebugEnabled()
  {
    isDebugEnabled
  }

  void debug(String msg, Throwable th)
  {
    debugClosure(msg, th)
  }

  void reset()
  {
    warnings = []
    debugs = []
    warnClosure = { String msg -> warnings << msg}
    debugClosure = { String msg, Throwable th -> debugs << [msg, th] }
  }

  void reset(boolean isDebugEnabled)
  {
    reset()
    this.isDebugEnabled = isDebugEnabled
  }
}

class ToStringThrowsExeption
{
  Exception e

  @Override
  String toString()
  {
    throw e
  }
}