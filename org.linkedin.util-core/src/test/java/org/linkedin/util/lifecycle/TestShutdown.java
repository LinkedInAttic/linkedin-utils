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

package org.linkedin.util.lifecycle;

import junit.framework.TestCase;
import org.linkedin.util.clock.Timespan;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author ypujante@linkedin.com
 *
 */
public class TestShutdown extends TestCase
{
  private static class MyException extends Exception {
    private static final long serialVersionUID = 1L;
  }

  public interface MyInterface
  {
    public int myMethod(boolean throwException) throws Exception;
  }

  public static class MyClass implements MyInterface
  {
    private final Lock _lock = new ReentrantLock();
    private final Condition _condition =_lock.newCondition();

    private boolean _wait = false;
    private boolean _waiting = false;

    @Override
    public int myMethod(boolean throwException) throws Exception
    {
      _lock.lock();
      try
      {
        if(_wait)
        {
          _waiting = true;
          while(_waiting)
          {
            _condition.await();
          }
        }
      }
      finally
      {
        _lock.unlock();
      }

      if(throwException)
        throw new MyException();

      return 10;
    }

    public void setWait(boolean wait)
    {
      _lock.lock();
      try
      {
        _wait = wait;
      }
      finally
      {
        _lock.unlock();
      }
    }

    public void wakeUp()
    {
      _lock.lock();
      try
      {
        _waiting = false;
        _condition.signalAll();
      }
      finally
      {
        _lock.unlock();
      }
    }
  }

  /**
   * Constructor
   */
  public TestShutdown(String name)
  {
    super(name);
  }

  /**
   * Test shutdown. WARNING: This piece of code MUST NOT be taken as an example of usage
   * pattern as the proper pattern is to use try/finally... here I am not on purpose!
   * @throws Exception
   */
  public void testShutdown() throws Exception
  {
    final MyClass o = new MyClass();

    Shutdown shutdown = new Shutdown();

    final MyInterface proxy = (MyInterface) ShutdownProxy.createShutdownProxy(o, shutdown);

    // first we verify that the proxy decorates properly
    assertEquals(10, proxy.myMethod(false));

    try
    {
      proxy.myMethod(true);
      fail("MyException should be thrown");
    }
    catch(MyException ex)
    {
      // expected
    }

    assertEquals(0, shutdown.getPendingCallsCount());

    // now we put the object in wait mode: the call starts but never end until we call wakeup
    o.setWait(true);

    ExecutorService executorService = Executors.newFixedThreadPool(2);

    // we execute 2 methods in separate threads: one which returns a result and one which throws
    // an exception
    Future<Integer> futureWithRes = executorService.submit(new Callable<Integer>()
    {
      @Override
      public Integer call() throws Exception
      {
        return proxy.myMethod(false);
      }
    });

    Future<Integer> futureWithException = executorService.submit(new Callable<Integer>()
    {
      @Override
      public Integer call() throws Exception
      {
        return proxy.myMethod(true);
      }
    });

    // now we wait for both task to be scheduled and blocked
    while(shutdown.getPendingCallsCount() != 2)
      Thread.sleep(100);

    assertFalse(futureWithRes.isDone());
    assertFalse(futureWithException.isDone());

    // we put the system in shutdown mode
    shutdown.shutdown();

    // we first verify that we cannot invoke new methods on the proxy and they fail with the proper
    // exception
    try
    {
      proxy.myMethod(false);
      fail("ShutdownRequestedException should be thrown");
    }
    catch(ShutdownRequestedException ex)
    {
      // expected
    }

    assertFalse(futureWithRes.isDone());
    assertFalse(futureWithException.isDone());

    try
    {
      proxy.myMethod(true);
      fail("ShutdownRequestedException should be thrown");
    }
    catch(ShutdownRequestedException ex)
    {
      // expected
    }

    assertFalse(futureWithRes.isDone());
    assertFalse(futureWithException.isDone());

    // now we verify that shutdown is still not complete (until both tasks are complete)
    try
    {
      shutdown.waitForShutdown(new Timespan(200));
      fail("should timeout");
    }
    catch(TimeoutException e)
    {
      // expected
    }

    assertFalse(futureWithRes.isDone());
    assertFalse(futureWithException.isDone());

    // we finally unblock the tasks
    o.wakeUp();

    // we get both normal results
    assertEquals(10, (int) futureWithRes.get());
    try
    {
      futureWithException.get();
      fail("MyException should be thrown");
    }
    catch(ExecutionException ex)
    {
      assertTrue(ex.getCause() instanceof MyException);
    }

    // now we should be able to shutdown
    shutdown.waitForShutdown(new Timespan(200));

    executorService.shutdown();
    executorService.awaitTermination(200, TimeUnit.MILLISECONDS);
  }
}
