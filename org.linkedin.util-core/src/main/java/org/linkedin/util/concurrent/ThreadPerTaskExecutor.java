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

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * This executor simply delegates to the thread factory to create a new thread everytime.
 *
 * @author ypujante@linkedin.com
 *
 */
public class ThreadPerTaskExecutor implements Executor
{
  private final ThreadFactory _threadFactory;

  /**
   * Constructor
   */
  public ThreadPerTaskExecutor()
  {
    this(new ThreadFactory()
    {
      @Override
      public Thread newThread(Runnable r)
      {
        return new Thread(r);
      }
    });
  }

  /**
   * Constructor
   */
  public ThreadPerTaskExecutor(ThreadFactory threadFactory)
  {
    _threadFactory = threadFactory;
  }

  @Override
  public void execute(Runnable task)
  {
    Thread thread = _threadFactory.newThread(task);
    thread.start();
  }

  /**
   * Executes the callable in a separate thread and return the future to get the result. Note
   * that this implementation is not efficient and should be used very carefully.
   *
   * @param callable
   * @return the future to get the result.
   */
  public static <V> Future<V> execute(Callable<V> callable)
  {
    FutureTask<V> futureTask = new FutureTask<V>(callable);

    new Thread(futureTask).start();

    return futureTask;
  }
}
