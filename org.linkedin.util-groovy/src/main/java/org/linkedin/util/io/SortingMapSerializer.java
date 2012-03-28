/**
 * This is a temporary solution to allow having map keys sorted when serializing with Jackson
 * This class should be removed (and JsonUtils adapted accordingly) as soon as Jackson 2.0 is released
 * This code is a very small adaptation of MapSerializer
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

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.ser.std.MapSerializer;
import org.codehaus.jackson.type.JavaType;

/**
  * Custom serializer to ensure serialized descendants of Map have their keys alphabetically sorted
  */
  public class SortingMapSerializer extends MapSerializer
  {
    protected SortingMapSerializer(HashSet<String> ignoredEntries,
                                   JavaType keyType, JavaType valueType, boolean valueTypeIsStatic,
                                   TypeSerializer vts,
                                   JsonSerializer<Object> keySerializer, JsonSerializer<Object> valueSerializer,
                                   BeanProperty property)
    {
      super(ignoredEntries, keyType, valueType, valueTypeIsStatic, vts, keySerializer, valueSerializer, property);
    }


  @Override
  public void serialize(Map<?,?> value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException, JsonGenerationException
  {
    if (SortedMap.class.isInstance(value))
    {
      super.serialize(value, jgen, provider);
    }
    else
    {
      super.serialize(new TreeMap<Object, Object>(value), jgen, provider);
    }
  }

  private static HashSet<String> toSortedSet(String[] ignoredEntries) {
    if (ignoredEntries == null || ignoredEntries.length == 0)
    {
      return null;
    }
    HashSet<String> result = new HashSet<String>(ignoredEntries.length);
    for (String prop : ignoredEntries)
    {
      result.add(prop);
    }
    return result;
  }

  public static SortingMapSerializer constructSorted(String[] ignoredList, JavaType mapType,
                                        boolean staticValueType, TypeSerializer vts, BeanProperty property,
                                        JsonSerializer<Object> keySerializer, JsonSerializer<Object> valueSerializer)
  {
    HashSet<String> ignoredEntries = toSortedSet(ignoredList);
    JavaType keyType, valueType;

    if (mapType == null)
    {
      keyType = valueType = UNSPECIFIED_TYPE;
    }
    else
    {
      keyType = mapType.getKeyType();
      valueType = mapType.getContentType();
    }
    // If value type is final, it's same as forcing static value typing:
    if (!staticValueType)
    {
      staticValueType = (valueType != null && valueType.isFinal());
    }
    return new SortingMapSerializer(ignoredEntries, keyType, valueType, staticValueType, vts,
                             keySerializer, valueSerializer, property);
  }


}
