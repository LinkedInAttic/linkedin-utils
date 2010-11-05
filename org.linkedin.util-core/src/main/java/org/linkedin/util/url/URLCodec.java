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

package org.linkedin.util.url;

import org.linkedin.util.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.net.URLDecoder;

/**
 * @author ypujante@linkedin.com
 *
 */
public class URLCodec implements Serializable
{
  private static final long serialVersionUID = 1L;

  public static final URLCodec INSTANCE = new URLCodec(false);

  public static URLCodec instance()
  {
    return INSTANCE;
  }

  public static final String CHARACTER_ENCODING = "UTF-8";
  
  private final boolean _encodeDot;
  private final String _characterEncoding;

  /**
   * Constructor
   */
  public URLCodec(boolean encodeDot, String characterEncoding) 
    throws UnsupportedEncodingException
  {
    _encodeDot = encodeDot;
    "".getBytes(characterEncoding); // checks for availabitity of encoding
    _characterEncoding = characterEncoding;
  }

  /**
   * Constructor
   */
  public URLCodec(boolean encodeDot)
  {
    _encodeDot = encodeDot;
    _characterEncoding = CHARACTER_ENCODING; // we know that it is supported!
  }

  /**
   * Constructor
   */
  public URLCodec()
  {
    this(false);
  }

  public String getCharacterEncoding()
  {
    return _characterEncoding;
  }

  public boolean isEncodeDot()
  {
    return _encodeDot;
  }

  /**
   * Encapsulates the call to encoding a URL
   *
   * @param original the string to encode
   * @return the encoded url */
  public String urlEncode(String original)
  {
    // see http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars
    // for an explanation of the character encoding
    String s = null;
    try
    {
      s = URLEncoder.encode(original, _characterEncoding);
    }
    catch(UnsupportedEncodingException e)
    {
      throw new RuntimeException(e);
    }

    if(_encodeDot)
    {
      // we encode . as well because it can cause problems in url in emails.
      s = TextUtils.searchAndReplace(s, ".", "%2E");
    }

    return s;
  }

  /**
   * Encapsulates the call to decoding a URL so that we don't have to deal
   * with the encoding.
   *
   * @param original the string to decode
   * @return the encoded url */
  public String urlDecode(String original)
  {
    try
    {
      return URLDecoder.decode(original, _characterEncoding);
    }
    catch(UnsupportedEncodingException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean equals(Object o)
  {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;

    URLCodec urlCodec = (URLCodec) o;

    if(_encodeDot != urlCodec._encodeDot) return false;
    if(_characterEncoding != null ?
      !_characterEncoding.equals(urlCodec._characterEncoding) :
      urlCodec._characterEncoding != null) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = (_encodeDot ? 1 : 0);
    result = 31 * result + (_characterEncoding != null ? _characterEncoding.hashCode() : 0);
    return result;
  }
}
