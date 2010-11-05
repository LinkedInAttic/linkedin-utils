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


package org.linkedin.util.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The purpose of this annotion is to specify that a public field can be initialized
 * prior to use of the instance, during the initialization phase. It should not be modified after the
 * fact and there is no guarantee of what will happen if you do.
 *
 * @author ypujante@linkedin.com
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Initializable
{
  /**
   * if set to <code>true</code> it means that the field must be initialized prior to use
   * the instance as there is no default value. If set to <code>false</code>, it means that 
   * the field has a default value.
   */
  boolean required() default false;

  /**
   * Optional description
   */
  String description() default "";
}
