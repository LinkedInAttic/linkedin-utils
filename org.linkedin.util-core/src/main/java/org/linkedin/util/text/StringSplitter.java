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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import java.util.Collections;

/**
 * This is a utility class to split up a string
 * 
 * @author ypujante@linkedin.com */
public class StringSplitter
{
  private final char _delim;

  private final String _blockIgnore;

  /**
   * Constructor
   *
   * @param delim the character to use as a delimiter for splitting */
  public StringSplitter(char delim)
  {
    this(delim, null);
  }

  /**
   * Constructor. This one takes another character that will be used to
   * ignore delim. For example:
   * <code>"param1=value1&param2='toto=3'&param3=value3"</code>. In this
   * string the delimiter <code>=</code> should be ignored within the block
   * delimited by <code>'</code>.
   *
   * @param delim the character to use as a delimiter for splitting
   * @param blockIgnore the character to use to ignore delim within it */
  public StringSplitter(char delim, char blockIgnore)
  {
    this(delim, String.valueOf(blockIgnore));
  }

  /**
   * Constructor. This one takes another character that will be used to
   * ignore delim. For example:
   * <code>"param1=value1&param2='toto=3'&param3=value3"</code>. In this
   * string the delimiter <code>=</code> should be ignored within the block
   * delimited by <code>'</code>.
   *
   * @param delim the character to use as a delimiter for splitting
   * @param blockIgnore the character to use to ignore delim within it */
  public StringSplitter(char delim, String blockIgnore)
  {
    _delim = delim;
    if(blockIgnore != null && blockIgnore.length() == 0)
      throw new IllegalArgumentException("block ignore cannot be the empty string");
    _blockIgnore = blockIgnore;
  }

  /**
   * Splits the string into an array of strings using the delimiter
   * provided in the constructor.
   *
   * @param s the string to split
   * @return the splitted string (<code>null</code> if <code>s</code> is
   *         <code>null</code>) */
  public String[] split(String s)
  {
    List<String> ss = splitAsList(s);
    if(ss == null)
      return null;

    return ss.toArray(new String[ss.size()]);
  }

  /**
   * Splits the string into an array of strings using the delimiter
   * provided in the constructor.
   *
   * @param s the string to split
   * @return the splitted string (<code>null</code> if <code>s</code> is
   *         <code>null</code>) */
  public List<String> splitAsList(String s)
  {
    if(s == null)
      return null;

    int len = s.length();

    if(len == 0)
    {
      return Collections.emptyList();
    }

    if(_blockIgnore == null)
      return splitNoBlockIgnore(s);
    else
      return splitBlockIgnore(s);
  }

  /**
   * Splits the string into an array of strings using the delimiter
   * provided in the constructor.
   *
   * @param s the string to split
   * @return the set of string (<code>null</code> if <code>s</code> is
   *         <code>null</code>) */
  public Set<String> splitAsSet(String s)
  {
    String[] ss = split(s);
    if(ss == null)
      return null;

    Set<String> res = new TreeSet<String>();
    for(int i = 0; i < ss.length; i++)
      res.add(ss[i]);

    return res;
  }

  /**
   * Splits the string and return an iterator using the delimiter
   * provided in the constructor.
   *
   * @param s the string to split
   * @return iterator of <code>String</code> (<code>null</code> if <code>s</code> is
   *         <code>null</code>) */
  public Iterator<String> splitToIterator(String s)
  {
    if(s == null)
      return null;

    if(_blockIgnore == null)
      return new SplitNoBlockIgnoreIterator(s);
    else
      throw new RuntimeException("TODO");
  }

  /**
   * Called when no block ignore */
  private List<String> splitNoBlockIgnore(String s)
  {
    int len = s.length();

    ArrayList<String> res = new ArrayList<String>();

    int idx = 0;
    int prev = 0;

    while(true)
    {
      idx = s.indexOf(_delim, idx);

      if(idx == -1)
      {
        res.add((prev == len) ? "" : s.substring(prev));
        break;
      }

      if(prev == idx)
      {
        res.add("");
      }
      else
      {
        res.add(s.substring(prev, idx));
      }

      prev = ++idx;
    }

    return res;
  }

  /**
   * The iterator */
  private class SplitNoBlockIgnoreIterator implements Iterator<String>
  {
    private final String _s;
    private int _idx = 0;
    private int _prev = 0;
    private String _next = null;

    private SplitNoBlockIgnoreIterator(String s)
    {
      _s = s;
      if(_s.length() == 0)
      {
        _next = null;
        _idx = -1;
      }
      else
        _next = advanceNext();
    }

    @Override
    public boolean hasNext()
    {
      return _next != null;
    }

    @Override
    public String next()
    {
      String next = _next;
      if(next == null)
        throw new NoSuchElementException();
      _next = advanceNext();
      return next;
    }

    private String advanceNext()
    {
      if(_idx == -1)
        return null;

      _idx = _s.indexOf(_delim, _idx);

      if(_idx == -1)
        return (_prev == _s.length()) ? "" : _s.substring(_prev);

      try
      {
        if(_prev == _idx)
          return "";
        else
          return _s.substring(_prev, _idx);
      }
      finally
      {
        _prev = ++_idx;
      }
    }

    @Override
    public void remove()
    {
      throw new UnsupportedOperationException("not supported!");
    }
  }

  /**
   * Called when block ignore */
  private List<String> splitBlockIgnore(String s)
  {
    if(s.indexOf(_blockIgnore) == -1)
      // if the character is not present then we can call the other method
      return splitNoBlockIgnore(s);

    boolean inBlockIgnore = false;
    int blockIgnoreIdx = 0;
    StringBuffer sb = new StringBuffer();
    ArrayList<String> al = new ArrayList<String>();

    int len = s.length();

    for(int i = 0; i < len; i++)
    {
      char c = s.charAt(i);
      if(c == _blockIgnore.charAt(blockIgnoreIdx))
      {
        blockIgnoreIdx++;
        if(_blockIgnore.length() == blockIgnoreIdx)
        {
          blockIgnoreIdx = 0;
          inBlockIgnore = !inBlockIgnore;
        }
        sb.append(c);
      }
      else
      {
        blockIgnoreIdx = 0;
        if(c == _delim)
        {
          if(inBlockIgnore)
            sb.append(c);
          else
          {
            al.add(sb.toString());
            sb = new StringBuffer();
          }
        }
        else
          sb.append(c);
      }
    }

    al.add(sb.toString());

//     if(inBlockIgnore)
//       throw new IllegalArgumentException("Wrong block ignore in string " + s);

    return al;
  }
}
