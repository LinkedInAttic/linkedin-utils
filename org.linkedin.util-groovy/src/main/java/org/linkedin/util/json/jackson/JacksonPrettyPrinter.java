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

/**
 * Handles indentation levels + tighten output slightly
 *
 * @author yan@pongasoft.com
 */
public class JacksonPrettyPrinter extends DefaultPrettyPrinter
{
  public static final Indenter FIXED_SPACE_INDENTER = new FixedSpaceIndenter();
  public static final Indenter LF_2_SPACES_INDENTER = new Lf2SpacesIndenter();

  public JacksonPrettyPrinter(int indent)
  {
    super();
    indentArraysWith(FIXED_SPACE_INDENTER);
    if(indent == 2)
      indentObjectsWith(LF_2_SPACES_INDENTER);
    else
      indentObjectsWith(new LfNSpacesIndenter(indent));
  }

  @Override
  public void beforeArrayValues(JsonGenerator jg)
  {
    // nothing
  }

  @Override
  public void writeEndArray(JsonGenerator jg, int nrOfValues)
    throws IOException
  {
    jg.writeRaw(']');
  }

  @Override
  public void writeObjectFieldValueSeparator(JsonGenerator jg)
    throws IOException
  {
    jg.writeRaw(": ");
  }
}
