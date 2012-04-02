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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import java.io.IOException;
import java.util.Arrays;

/**
 * Copied/adapted from jackson to handle N spaces instead of 2
 *
 * @author yan@pongasoft.com
 */
public class LfNSpacesIndenter implements DefaultPrettyPrinter.Indenter
{
  private final int _numSpaces;

  /**
   * Constructor
   */
  public LfNSpacesIndenter(int numSpaces)
  {
    if(numSpaces <= 0)
      throw new IllegalArgumentException(numSpaces + " must be > 0");

    _numSpaces = numSpaces;
  }

  final static String SYSTEM_LINE_SEPARATOR;

  static
  {
    String lf = null;
    try
    {
      lf = System.getProperty("line.separator");
    }
    catch(Throwable t)
    {
      t.printStackTrace(System.err);
    } // access exception?
    SYSTEM_LINE_SEPARATOR = (lf == null) ? "\n" : lf;
  }

  final static int SPACE_COUNT = 64;
  final static char[] SPACES = new char[SPACE_COUNT];

  static
  {
    Arrays.fill(SPACES, ' ');
  }

  @Override
  public boolean isInline()
  {
    return false;
  }

  @Override
  public void writeIndentation(JsonGenerator jg, int level)
    throws IOException
  {
    jg.writeRaw(SYSTEM_LINE_SEPARATOR);
    // N spaces per level
    int numSpaces;
    // faster to add than multiply...
    switch(_numSpaces)
    {
      case 1:
        numSpaces = level;
        break;

      case 2:
        numSpaces = level + level;
        break;

      case 3:
        numSpaces = level + level + level;
        break;

      case 4:
        numSpaces = level + level + level + level;
        break;

      default:
        numSpaces = level * _numSpaces;
        break;
    }

    while(numSpaces > SPACE_COUNT)
    { // should never happen but...
      jg.writeRaw(SPACES, 0, SPACE_COUNT);
      numSpaces -= SPACES.length;
    }
    jg.writeRaw(SPACES, 0, numSpaces);
  }
}
