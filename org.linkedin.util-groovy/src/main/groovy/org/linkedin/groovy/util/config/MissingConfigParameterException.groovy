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

package org.linkedin.groovy.util.config

/**
 * @author ypujante@linkedin.com
 */
class MissingConfigParameterException extends RuntimeException
{
  private static final long serialVersionUID = 1L;

  public MissingConfigParameterException() { super(); }
  public MissingConfigParameterException(String msg) { super(msg); }
}
