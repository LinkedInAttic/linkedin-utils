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
import com.fasterxml.jackson.databind.ser.std.BooleanSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import groovy.lang.GString;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * The purpose of this class is to handle only metadata style entities, which is to say,
 * collections and maps and arrays (and recursively) of primitive types or wrapper types
 * and <code>String</code>. Any other type will be serialized by calling <code>toString</code>
 * on it.
 * 
 * @author yan@pongasoft.com
 */
public class MetadataStyleSerializerFactory extends BeanSerializerFactory
{
  protected final static Map<String, JsonSerializer<?>> SERIALIZERS;

  static
  {
    SERIALIZERS = new HashMap<String, JsonSerializer<?>>();

    NumberSerializers.IntLikeSerializer integerLikeSerializer =
      new NumberSerializers.IntLikeSerializer();
    NumberSerializers.IntegerSerializer integerSerializer =
      new NumberSerializers.IntegerSerializer();
    NumberSerializers.NumberSerializer numberSerializer =
      new NumberSerializers.NumberSerializer();
    NumberSerializers.LongSerializer longSerializer =
      new NumberSerializers.LongSerializer();
    NumberSerializers.FloatSerializer floatSerializer =
      new NumberSerializers.FloatSerializer();
    NumberSerializers.DoubleSerializer doubleSerializer =
      new NumberSerializers.DoubleSerializer();

    // boolean
    SERIALIZERS.put(boolean.class.getName(), new BooleanSerializer(true));
    SERIALIZERS.put(Boolean.class.getName(), new BooleanSerializer(false));

    // char
    SERIALIZERS.put(char.class.getName(), ToStringSerializer.instance);
    SERIALIZERS.put(Character.class.getName(), ToStringSerializer.instance);

    // byte
    SERIALIZERS.put(byte.class.getName(), integerLikeSerializer);
    SERIALIZERS.put(Byte.class.getName(), integerLikeSerializer);

    // short
    SERIALIZERS.put(short.class.getName(), integerLikeSerializer);
    SERIALIZERS.put(Short.class.getName(), integerLikeSerializer);

    // int
    SERIALIZERS.put(int.class.getName(), integerSerializer);
    SERIALIZERS.put(Integer.class.getName(), integerSerializer);
    SERIALIZERS.put(BigInteger.class.getName(), numberSerializer);

    // long
    SERIALIZERS.put(long.class.getName(), longSerializer);
    SERIALIZERS.put(Long.class.getName(), longSerializer);

    // float
    SERIALIZERS.put(float.class.getName(), floatSerializer);
    SERIALIZERS.put(Float.class.getName(), floatSerializer);

    // double
    SERIALIZERS.put(double.class.getName(), doubleSerializer);
    SERIALIZERS.put(Double.class.getName(), doubleSerializer);
    SERIALIZERS.put(BigDecimal.class.getName(), numberSerializer);

    // String
    SERIALIZERS.put(String.class.getName(), new StringSerializer());
  }

  /**
   * Constructor
   */
  public MetadataStyleSerializerFactory(SerializerFactoryConfig config)
  {
    super(addGroovySerializers(config));
  }

  /**
   * Constructor
   */
  public MetadataStyleSerializerFactory()
  {
    this(null);
  }

  @Override
  @SuppressWarnings("unchecked")
  public JsonSerializer<Object> createSerializer(SerializerProvider prov,
                                                 JavaType origType,
                                                 BeanProperty property) throws JsonMappingException
  {
    JsonSerializer<Object> serializer =
      (JsonSerializer<Object>) SERIALIZERS.get(origType.getRawClass().getName());

    if(serializer != null)
      return serializer;

    if(origType.isContainerType())
      return super.createSerializer(prov, origType, property);
    else
      return ToStringSerializer.instance;
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
