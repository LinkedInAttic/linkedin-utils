/*
 * Copyright 2010-2012 LinkedIn, Inc
 * Portions Copyright 2012 Yan Pujante
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


package org.linkedin.groovy.util.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.json.JSONArray
import org.json.JSONObject
import org.linkedin.util.json.jackson.GroovySerializerFactory
import org.linkedin.util.json.jackson.JacksonPrettyPrinter

/**
 * Contains utilities for json.
 *
 * @author ypujante@linkedin.com
 */
class JsonUtils
{
  private static final def JACKSON_PARSER
  private static final JACKSON_MAPPER = newJacksonMapper(false)
  private static final JACKSON_SORTING_MAPPER = newJacksonMapper(true)

  static {
    // creating the parser => allowing comments, single quotes and no quotes around
    // field names
    JACKSON_PARSER = new ObjectMapper()
    JACKSON_PARSER.configure(JsonParser.Feature.ALLOW_COMMENTS, true)
    JACKSON_PARSER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
    JACKSON_PARSER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
  }

  static ObjectMapper newJacksonMapper(sorting)
  {
    def mapper = new ObjectMapper()
    mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true)
    if(sorting)
      mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
    mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
    mapper.setSerializerFactory(new GroovySerializerFactory())
    return mapper
  }

  /**
   * Represents the value in JSON, nicely indented and human readable
   */
  static String prettyPrint(value)
  {
    return prettyPrint(value, 2)
  }

  /**
   * Represents the value in JSON, nicely indented and human readable depending on 'indent'
   */
  static String prettyPrint(value, int indent)
  {
    if(indent > 0)
    {
      // handles JSONObject/JSONArray
      value = toValue(value)

      if (value == null)
        return null

      def prettyPrinter = new JacksonPrettyPrinter(indent)
      return JACKSON_SORTING_MAPPER.writer(prettyPrinter).writeValueAsString(value)
    }
    else
      return compactPrint(value)
  }

  /**
   * Represents the value in JSON, compact form (keys are not sorted)
   */
  static String compactPrint(value)
  {
    // handles JSONObject/JSONArray
    value = toValue(value)

    if (value == null)
      return null

    return JACKSON_MAPPER.writeValueAsString(value)
  }

  /**
   * Given a json string, convert it to a value (map / list)
   * depending on if the json starts with <code>[</code> or <code>{</code>
   * (with proper <code>null</code> handling).
   */
  static def fromJSON(String json)
  {
    if(json == null)
      return null
    json = json.trim()
    if(json.startsWith('['))
      return JACKSON_PARSER.readValue(json, ArrayList.class)
    else
      return JACKSON_PARSER.readValue(json, LinkedHashMap.class)
  }

  /**
   * Converts the value into its non JSON counter part: if the value is a {@link org.json.JSONObject}
   * or a {@link org.json.JSONArray} then it will call the appropriate method, otherwise simply return the
   * value.
   */
  static def toValue(value)
  {
    if(value == null)
      return null

    if(value instanceof JSONObject)
    {
      return toMap((JSONObject) value)
    }
    else if(value instanceof JSONArray)
    {
      return toList((JSONArray) value)
    }

    return value
  }

  /**
   * Converts a json object into a map (recursive call).
   */
  static def toMap(JSONObject json)
  {
    if(json == null)
      return null

    def map = [:]
    json?.keys().each { key ->
      map[key] = toValue(json.get(key))
    }
    return map
  }

  /**
   * Converts the json array into a list (recursive call).
   */
  static def toList(JSONArray array)
  {
    if(array == null)
      return null

    def list = []

    (0..<array.length()).each { idx ->
      list << toValue(array.get(idx))
    }

    return list
  }

  /**
   * Reverse method which converts a value into a json value
   */
  static toJSON(value)
  {
    if(value == null)
      return null

    if(value instanceof Map)
    {
      return mapToJSON((Map) value)
    }

    if(value instanceof Collection)
    {
      return collectionToJSON((Collection) value)
    }

    return value
  }

  static mapToJSON(Map map)
  {
    if(map == null)
      return null

    JSONObject json = new JSONObject()
    map.each {k, v ->
      json.put(k?.toString(), toJSON(v))
    }
    return json
  }

  static collectionToJSON(Collection list)
  {
    if(list == null)
      return null

    JSONArray array = new JSONArray()
    list.each { elt ->
      array.put(toJSON(elt))
    }

    return array
  }
}
