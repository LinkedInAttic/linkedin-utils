package org.linkedin.util.io;


import java.util.EnumMap;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.codehaus.jackson.map.ser.std.ToStringSerializer;
import org.codehaus.jackson.map.type.MapType;


/**
 * Custom serializer factory, to introduce the map-sorting serializer
 */
public class SortingSerializerFactory extends CustomSerializerFactory
{
  protected SortingSerializerFactory() {
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
    for (Serializers serializers : customSerializers()) {
      JsonSerializer<?> ser = serializers.findMapSerializer(config, type, beanDesc, property,
                                                            keySerializer, elementTypeSerializer, elementValueSerializer);
      if (ser != null) {
        return ser;
      }
    }
    if (EnumMap.class.isAssignableFrom(type.getRawClass())) {
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
