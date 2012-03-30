/*
 * Copyright (c) 2012 Yan Pujante
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
package org.linkedin.util.json.jackson;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import groovy.lang.GString;

/**
 * The purpose of this class is to handle groovy specific classes (currently only GString)
 * 
 * @author yan@pongasoft.com
 */
public class GroovySerializerFactory extends BeanSerializerFactory
{
  /**
   * Constructor
   */
  public GroovySerializerFactory(SerializerFactoryConfig config)
  {
    super(addGroovySerializers(config));
  }

  /**
   * Constructor
   */
  public GroovySerializerFactory()
  {
    this(null);
  }

  @Override
  public JsonSerializer<Object> createSerializer(SerializerProvider prov,
                                                 JavaType origType,
                                                 BeanProperty property) throws JsonMappingException
  {
    return super.createSerializer(prov, origType, property);
  }

  public static SerializerFactoryConfig addGroovySerializers(SerializerFactoryConfig config)
  {
    if(config == null)
      config = new SerializerFactoryConfig();
    SimpleSerializers serializers = new SimpleSerializers();
    serializers.addSerializer(GString.class, ToStringSerializer.instance);
    config = config.withAdditionalSerializers(serializers);
    return config;
  }
}
