/*
 * Copyright 2010-2010 LinkedIn, Inc
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

package org.linkedin.util.text;

import java.io.StringReader;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * This class encapsulates the notion of indentation. It maintains
 * internally a 'level' of indentation that you should modify with
 * <code>inc</code> or <code>dec</code>. You can also specify which
 * characters will be used to display the indentation (most of the time it
 * will be spaces).
 *
 *
 * @author ypujante@linkedin.com */
public class Indent
{
  /**
   * The level of indentation */
  private int _level;

  /**
   * The <code>String</code> that will be used to display the indentation */
  private String _indent_string;

  private String _indentation = null;
  /**
   * Constructor. Initializes the indentation to start at level 0 and to
   * have a display of 2 spaces for each level. */
  public Indent()
  {
    this("  ", 0);
  }

  /**
   * Constructor. Initializes with the specified string and a starting
   * level of 0.
   *
   * @param indent_string the indentation representation */
  public Indent(String indent_string)
  {
    this(indent_string, 0);
  }

  /**
   * Constructor. Initializes with the specified level and a 2 spaces string.
   *
   * @param level the level of indetation to start */
  public Indent(int level)
  {
    this("  ", level);
  }

  /**
   * Constructor. Initializes with provided values.
   *
   * @param indent_string the indentation representation
   * @param level the level of indentation to start */
  public Indent(String indent_string, int level)
  {
    _indent_string = indent_string;
    _level = level;
    calculateIndentationString();
  }

  /**
   * Access function.
   *
   * @return the string representation of the indentation */
  public String getIndentString()
  {
    return _indent_string;
  }

  /**
   * Access function.
   *
   * @param indent_string the string representation of the indentation */
  public void setIndentString(String indent_string)
  {
    _indent_string = indent_string;
    calculateIndentationString();
  }
  
  /**
   * Access function.
   *
   * @return the indentation level */
  public int getLevel()
  {
    return _level;
  }

  /**
   * Access function. You should not use this method to modify the level of
   * indentation but use <code>inc</code> or <code>dec</code> instead.
   *
   * @param level the indentation level */
  public void setLevel(int level)
  {
    _level = level;
    if(_level < 0)
      _level = 0;

    calculateIndentationString();
  }

  /**
   * Increments the level of indentation */
  public void inc()
  {
    _level++;
    calculateIndentationString();
  }

  /**
   * Decrements the level of indentation */
  public void dec()
  {
    _level--;
    if(_level < 0) {
      _level = 0;
  }
    calculateIndentationString();
  }
  
  /**
   * Returns the indentation depending on the level.
   *
   * @return the indentation */
  public String getIndentation()
  {
    return _indentation;
  }

  private void calculateIndentationString() {
    if(_level == 0) {
      _indentation = "";
      return;
    }

    // simple optim
    if(_level == 1) {
      _indentation = _indent_string;
      return;
    }

    StringBuilder sb = new StringBuilder(_level + _indent_string.length());

    for(int i = 0; i < _level; i++)
      sb.append(_indent_string);

    _indentation = sb.toString();
  }
  
  /**
   * Convenient method : returns the indentation depending on the level.
   *
   * @return the indentation */
  public String toString()
  {
    return getIndentation();
  }

  /**
   * Indents a block of text. You provide the block of text as a String and
   * the indent object and it returns another String with each line
   * properly indented.
   *
   * @param block the block of text to indent
   * @param indent the indentation object to use
   * @return the indented block */
  public static String indentBlock(String block, Indent indent)
  {
    StringBuilder sb = new StringBuilder();

    BufferedReader br = new BufferedReader(new StringReader(block));
    String line;
    try
    {
      while((line = br.readLine()) != null)
        sb.append(indent).append(line).append('\n');
    }
    catch(IOException ex)
    {
      // on a String ? I doubt...
    }

    return sb.toString();
  }
}
