/*
 * Copyright (c) 2012 Yan Pujante
 * Portions Copyright 2012 LinkedIn, Inc
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
package test.util.json

import org.linkedin.groovy.util.json.JsonUtils
import org.json.JSONObject

/**
 * @author yan@pongasoft.com */
public class TestJsonUtils extends GroovyTestCase
{
  public void testJsonSortedSerialization()
  {
    def str1 = 'foo'
    def str2 = 'bar'
    def map = [b: 'v1', a: "${str1}!=${str2}"]    // This map contains a groovy.lang.GString descendant
    // Descendants of groovy.lang.GString (example: org.codehaus.groovy.runtime.GstringImpl)
    // must be deserialized by Jackson using toString() instead of Java reflexion.
    // Otherwise 'statusInfo' will be deserialized in a weird way, something like:
    // "statusInfo": { "values": ["running", "stopped"], "strings": ["", "!=", ""], "valueCount": 2 }
    // instead of:
    // "statusInfo": "running!=stopped"
    def deserialized_map = JsonUtils.fromJSON(JsonUtils.compactPrint(map))
    assertEquals(map['b'], deserialized_map['b'])
    assertEquals(map['a'], deserialized_map['a'])
    assertEquals(map.size(), deserialized_map.size())
    assertEquals("""{
  "a": "foo!=bar",
  "b": "v1"
}""", JsonUtils.prettyPrint(map))
    map = [z: null, a: 'v1', d: ['t2', 't1'], c: 'v3', b: [a: 'b1', c:  'b2', d: ['foo', 'bar'], b: 'b3']];
    assertEquals("""{
  "a": "v1",
  "b": {
    "a": "b1",
    "b": "b3",
    "c": "b2",
    "d": [
      "foo",
      "bar"
    ]
  },
  "c": "v3",
  "d": [
    "t2",
    "t1"
  ]
}""", JsonUtils.prettyPrint(map))
    map = [a: 'v1', d: 'v2', c: 'v3', b: 'v4'];
    assertEquals("""{
  "a": "v1",
  "b": "v4",
  "c": "v3",
  "d": "v2"
}""", JsonUtils.prettyPrint(map))
  }

  public void testSerialization()
  {
    int[] array = new int[1]
    array[0] = 5

    def map = [
      z: 1,
      y: new Integer(2),
      x: "abc",
      w: [3, [cc: new MyBean(foo: 'g', bar: 7), bb: 12.4 as float, aa: [14.2]]],
      v: [
        a: 4 as long
      ],
      u: array,
      t: new MyBean(foo:'f', bar: 6),
      s: true,
      r: Boolean.FALSE
    ]

    String expected = """{
  "r": false,
  "s": true,
  "t": "f/6",
  "u": [
    5
  ],
  "v": {
    "a": 4
  },
  "w": [
    3,
    {
      "aa": [
        14.2
      ],
      "bb": 12.4,
      "cc": "g/7"
    }
  ],
  "x": "abc",
  "y": 2,
  "z": 1
}"""
    assertEquals(expected, JsonUtils.prettyPrint(map))
    assertEquals(expected, JsonUtils.prettyPrint(JsonUtils.fromJSON(expected)))
  }

  private String withIndent0 = '{"z":3,"b":[1,3,4],"y":[],"w":["4"],"a":{"h":["foo","bar",{"zz":1,"aa":2}],"l":5},"k":{"v":5},"x":{}}'

  private String withIndent1 = """{
 "a": {
  "h": [
   "foo",
   "bar",
   {
    "aa": 2,
    "zz": 1
   }
  ],
  "l": 5
 },
 "b": [
  1,
  3,
  4
 ],
 "k": {
  "v": 5
 },
 "w": [
  "4"
 ],
 "x": {
 },
 "y": [
 ],
 "z": 3
}"""

  private String withIndent2 = """{
  "a": {
    "h": [
      "foo",
      "bar",
      {
        "aa": 2,
        "zz": 1
      }
    ],
    "l": 5
  },
  "b": [
    1,
    3,
    4
  ],
  "k": {
    "v": 5
  },
  "w": [
    "4"
  ],
  "x": {
  },
  "y": [
  ],
  "z": 3
}"""

  private String withIndent6 = """{
      "a": {
            "h": [
                  "foo",
                  "bar",
                  {
                        "aa": 2,
                        "zz": 1
                  }
            ],
            "l": 5
      },
      "b": [
            1,
            3,
            4
      ],
      "k": {
            "v": 5
      },
      "w": [
            "4"
      ],
      "x": {
      },
      "y": [
      ],
      "z": 3
}"""

  public void testIndent()
  {
    def map = [
      z: 3,
      b: [1,3,4],
      y: [],
      w: ["4"],
      a: [
        h: ["foo", "bar", [zz: 1, aa: 2]],
        l: 5
      ],
      o: null,
      k: [v: 5],
      x: [:]
    ]

    assertEquals(withIndent0, JsonUtils.compactPrint(map))
    assertEquals(JsonUtils.compactPrint(map), JsonUtils.prettyPrint(map, 0))


    [1,2,6].each { i ->
      assertEquals(this."withIndent${i}", JsonUtils.prettyPrint(map, i))
    }

    assertEquals(JsonUtils.prettyPrint(map), JsonUtils.prettyPrint(map, 2))
    assertEquals(JsonUtils.compactPrint(map), JsonUtils.prettyPrint(map, 0))
    assertTrue(JsonUtils.prettyPrint(map, 1) != JsonUtils.prettyPrint(map, 2))
  }

  /**
   * We make sure that pretty print still works with <code>JSONObject/JSONArray</code>
   */
  public void testPrettyPrintBackwardCompatibility()
  {
    def map = [
      z: 3,
      b: [1,3,4],
      y: [],
      w: ["4"],
      a: [
        h: ["foo", "bar", [zz: 1, aa: 2]],
        l: 5
      ],
      o: null,
      k: [v: 5],
      x: [:]
    ]

    def jsonObject = JsonUtils.toJSON(map)
    assertTrue(jsonObject instanceof JSONObject)
    assertEquals(withIndent2, JsonUtils.prettyPrint(jsonObject))
    assertEquals(withIndent2, JsonUtils.prettyPrint(jsonObject, 2))
  }
}

class MyBean
{
  String foo
  int bar

  @Override
  String toString()
  {
    return "${foo}/${bar}"
  }


}