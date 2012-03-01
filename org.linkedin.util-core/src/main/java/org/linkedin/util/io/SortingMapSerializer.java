package org.linkedin.util.io;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.ser.std.*;
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
    if (SortedMap.class.isInstance(value)) {
      super.serialize(value, jgen, provider);
    } else {
      super.serialize(new TreeMap<Object, Object>(value), jgen, provider);
    }
  }

  private static HashSet<String> toSortedSet(String[] ignoredEntries) {
    if (ignoredEntries == null || ignoredEntries.length == 0) {
      return null;
    }
    HashSet<String> result = new HashSet<String>(ignoredEntries.length);
    for (String prop : ignoredEntries) {
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

    if (mapType == null) {
      keyType = valueType = UNSPECIFIED_TYPE;
    } else {
      keyType = mapType.getKeyType();
      valueType = mapType.getContentType();
    }
    // If value type is final, it's same as forcing static value typing:
    if (!staticValueType) {
      staticValueType = (valueType != null && valueType.isFinal());
    }
    return new SortingMapSerializer(ignoredEntries, keyType, valueType, staticValueType, vts,
                             keySerializer, valueSerializer, property);
  }


}
