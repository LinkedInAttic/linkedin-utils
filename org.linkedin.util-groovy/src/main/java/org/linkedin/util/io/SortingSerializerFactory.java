/**
 * This is a temporary solution to allow having map keys sorted when serializing with Jackson
 * This class should be removed (and JsonUtils adapted accordingly) as soon as Jackson 2.0 is released
 * This code is a very small adaptation of CustomSerializerFactory
 *
 * Copyright 2012 LinkedIn, Inc
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

package org.linkedin.util.io;

import java.util.EnumMap;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.Serializers;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.codehaus.jackson.map.type.MapType;

/**
 * Custom serializer factory, to introduce the map-sorting serializer
 */
public class SortingSerializerFactory extends CustomSerializerFactory
{
  protected SortingSerializerFactory()
  {
    super();
  }
  /**
   * Helper method that handles configuration details when constructing serializers for
   * {@link java.util.Map} types.
   *<p>
   * Note: signature changed in 1.8, to take 'staticTyping' argument
   */
  protected JsonSerializer<?> buildMapSerializer(SerializationConfig config, MapType type,
                                                 BasicBeanDescription beanDesc, BeanProperty property,
                                                 boolean staticTyping,
                                                 JsonSerializer<Object> keySerializer,
                                                 TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer)
  {
    for (Serializers serializers : customSerializers())
    {
      JsonSerializer<?> ser = serializers.findMapSerializer(config, type, beanDesc, property,
                                                            keySerializer, elementTypeSerializer, elementValueSerializer);
      if (ser != null)
      {
        return ser;
      }
    }
    if (EnumMap.class.isAssignableFrom(type.getRawClass()))
    {
      return buildEnumMapSerializer(config, type, beanDesc, property, staticTyping,
                                    elementTypeSerializer, elementValueSerializer);
    }
    return SortingMapSerializer.constructSorted(config.getAnnotationIntrospector().findPropertiesToIgnore(beanDesc.getClassInfo()),
                                                type,
                                                staticTyping,
                                                elementTypeSerializer,
                                                property,
                                                keySerializer,
                                                elementValueSerializer);
  }

}
