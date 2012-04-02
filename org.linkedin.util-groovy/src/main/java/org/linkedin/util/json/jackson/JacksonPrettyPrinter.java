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
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import java.io.IOException;

/**
 * Handles indentation levels + tighten output slightly
 *
 * @author yan@pongasoft.com
 */
public class JacksonPrettyPrinter implements PrettyPrinter
{
  public static final DefaultPrettyPrinter.Indenter LF_2_SPACES_INDENTER =
    new DefaultPrettyPrinter.Lf2SpacesIndenter();

  private final DefaultPrettyPrinter.Indenter _indenter;
  private int _nesting = 0;

  public JacksonPrettyPrinter(int indent)
  {
    super();
    if(indent == 2)
      _indenter = LF_2_SPACES_INDENTER;
    else
      _indenter = new LfNSpacesIndenter(indent);
  }

  @Override
  public void writeRootValueSeparator(JsonGenerator jg) throws IOException
  {
  }

  private void writeIndentation(JsonGenerator jg) throws IOException
  {
    _indenter.writeIndentation(jg, _nesting);
  }

  @Override
  public void writeStartObject(JsonGenerator jg) throws IOException
  {
    jg.writeRaw('{');
    _nesting++;
  }

  @Override
  public void writeEndObject(JsonGenerator jg, int nrOfEntries)
    throws IOException
  {
    _nesting--;
    writeIndentation(jg);
    jg.writeRaw('}');
  }

  @Override
  public void writeObjectEntrySeparator(JsonGenerator jg)
    throws IOException
  {
    jg.writeRaw(',');
    writeIndentation(jg);
  }

  @Override
  public void writeObjectFieldValueSeparator(JsonGenerator jg)
    throws IOException
  {
    jg.writeRaw(": ");
  }

  @Override
  public void writeStartArray(JsonGenerator jg) throws IOException
  {
    jg.writeRaw('[');
    _nesting++;
  }

  @Override
  public void writeEndArray(JsonGenerator jg, int nrOfValues)
    throws IOException
  {
    _nesting--;
    writeIndentation(jg);
    jg.writeRaw(']');
  }

  @Override
  public void writeArrayValueSeparator(JsonGenerator jg) throws IOException
  {
    jg.writeRaw(',');
    writeIndentation(jg);
  }

  @Override
  public void beforeArrayValues(JsonGenerator jg) throws IOException
  {
    writeIndentation(jg);
  }

  @Override
  public void beforeObjectEntries(JsonGenerator jg) throws IOException
  {
    writeIndentation(jg);
  }
}
