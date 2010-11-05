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

/**
 * This interface defines the API of an entity that is destroyable. The main
 * difference between <code>Destroyable</code> and <code>Shutdownable</code> 
 * is that for the entity there is nothing to wait for (in other words all the
 * calls to the entity are blocking calls). It is also possible to have an
 * entity that implements both <code>Destroyable</code> and
 * <code>Shutdownable</code> in which case, the proper way would be:
 *
 * <pre>
 *   entity.shutdown();
 *   entity.waitForShutdown();
 *   // here we know that all the non blocking calls are terminated
 *   entity.destroy();
 * </pre>
 *
 * @author  ypujante@linkedin.com
 * @see Shutdownable */
public interface Destroyable extends Terminable
{
  /**
   * This method destroys the entity, cleaning up any resource that needs to
   * be cleaned up, like closing files, database connection.. */
  public void destroy();
}
