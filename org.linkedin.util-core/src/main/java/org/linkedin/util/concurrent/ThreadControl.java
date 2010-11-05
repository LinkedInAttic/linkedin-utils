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

package org.linkedin.util.concurrent;

import org.slf4j.Logger;
import org.linkedin.util.clock.Timespan;
import org.linkedin.util.lang.LangUtils;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

/**
 * This utility class is meant to facilitate the implementation of tests that use multiple threads.
 * Usually mock objects will call {@link #block(Object)} on this class and the main thread will
 * call {@link #unblock(Object, Object)} to unblock the mock object and to provide it with the value
 * to return. There is a global timeout so that we don't end up waiting forever...
 *
 * @author ypujante@linkedin.com
 *
 */
public class ThreadControl
{
  public static final String MODULE = ThreadControl.class.getName();
  public static final Logger log = org.slf4j.LoggerFactory.getLogger(MODULE);

  private final Lock _lock = new ReentrantLock();

  private final Condition _unblockCondition = _lock.newCondition();
  private final Condition _blockCondition = _lock.newCondition();

  private final Map<Object, Object> _waitingForUnblock = new HashMap<Object, Object>();
  private final Set<Object> _waitingForBlock = new HashSet<Object>();

  private final Timespan _timeout;

  /**
   * Constructor
   */
  public ThreadControl()
  {
    this(Timespan.ONE_SECOND);
  }

  /**
   * Constructor
   */
  public ThreadControl(Timespan timeout)
  {
    _timeout = timeout;
  }

  public Timespan getTimeout()
  {
    return _timeout;
  }

  /**
   * This method is usually called from within a mock object to execute the call associated
   * to the given key. It will block until another thread calls one of the <code>unblock</code>
   * method with the object to return.
   *
   * @param key same key as used in <code>unblock</code>
   * @return the object provided to <code>unblock</code>
   */
  public Object block(Object key)
  {
    if(log.isDebugEnabled())
      log.debug(LangUtils.shortIdentityString(this) + ".block(" + key + ")");

    _lock.lock();
    try
    {
      _waitingForBlock.add(key);
      _blockCondition.signalAll();

      long endTime = System.currentTimeMillis() + _timeout.getDurationInMilliseconds();

      while(!_waitingForUnblock.containsKey(key))
      {
        try
        {
          ConcurrentUtils.awaitUntil(_unblockCondition, endTime);
        }
        catch(InterruptedException e)
        {
          throw new RuntimeException(e);
        }
        catch(TimeoutException e)
        {
          throw new RuntimeException(e);
        }
      }

      return _waitingForUnblock.remove(key);
    }
    finally
    {
      _lock.unlock();
    }
  }

  /**
   * This method is usually called from within a mock object to execute the call associated
   * to the given key. It will block until another thread calls one of the <code>unblock</code>
   * method with the object to return. If the object to return is an exception it will throw
   * it instead!
   *
   * @param key same key as used in <code>unblock</code>
   * @return the object provided to <code>unblock</code>
   */
  public Object blockWithException(Object key) throws Exception
  {
    Object value = block(key);

    if(value instanceof Exception)
    {
      Exception exception = (Exception) value;
      throw exception;
    }

    return value;
  }

  /**
   * Equivalent to <code>unblock(key, null)</code>.
   */
  public void unblock(Object key)
  {
    unblock(key, null);
  }

  /**
   * This method is called from one thread while another is going to call <code>block</code>. It
   * is basically a synchronization point as this method will block until
   * <code>block</code> is called and will then release the thread who called <code>block</code>
   * in the first place.
   *
   * @param key the same key used in <code>block</code>
   * @param value the value that will be returned by <code>block</code>
   */
  public void unblock(Object key, Object value)
  {
    if(log.isDebugEnabled())
      log.debug(LangUtils.shortIdentityString(this) + ".unblock(" + key + ", " + value + ")");
    
    _lock.lock();
    try
    {
      try
      {
        waitForBlock(key);
      }
      catch(InterruptedException e)
      {
        throw new RuntimeException(e);
      }
      catch(TimeoutException e)
      {
        throw new RuntimeException(e);
      }

      _waitingForBlock.remove(key);
      _waitingForUnblock.put(key, value);
      _unblockCondition.signalAll();
    }
    finally
    {
      _lock.unlock();
    }
  }

  /**
   * Blocking call to wait for a thread to call <code>block</code>.
   *
   * @param key the key we are waiting for
   * @throws TimeOutException
   * @throws InterruptedException
   */
  public void waitForBlock(Object key) throws TimeoutException, InterruptedException
  {
    waitForBlock(key, _timeout);
  }

  /**
   * Blocking call to wait for a thread to call <code>block</code>.
   *
   * @param key the key we are waiting for
   * @throws TimeOutException
   * @throws InterruptedException
   */
  public void waitForBlock(Object key, Timespan timeout) throws TimeoutException, InterruptedException
  {
    _lock.lock();
    try
    {
      long endTime = System.currentTimeMillis() + timeout.getDurationInMilliseconds();

      while(!_waitingForBlock.contains(key))
      {
        ConcurrentUtils.awaitUntil(_blockCondition, endTime);
      }
    }
    finally
    {
      _lock.unlock();
    }
  }
}
