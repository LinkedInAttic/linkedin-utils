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

package org.linkedin.util.xml;



/**
 * Contains XML related utilities
 *
 * @author ypujante@linkedin.com */
public class XMLUtils
{
  private final static String[] _chars;

  static
  {
    // we create a static array that will convert all the characters from 0 to 127 into their xml
    // representation. Note that the characters from [0-20[ are not valid except tab, CR and
    // linefeed. So we replace them with <space>.
    _chars = new String[128];

    for(int i = ' '; i < _chars.length; i++)
     _chars[i] = String.valueOf((char) i);

    for(int i = 0; i < ' '; i++)
      _chars[i] = " ";

    // valid xml characters
    _chars['\t'] = "\t";
    _chars['\n'] = "\n";
    _chars['\r'] = "\r";

    // the ones who need encoding
    _chars['<'] = "&lt;";
    _chars['>'] = "&gt;";
    _chars['&'] = "&amp;";
    _chars['"'] = "&quot;";
  }

  /**
   * Encodes the string so that it is XML safe: converts &lt;, &gt; &amp;
   * &apos; into their equivalent (&amp;lt;, &amp;gt; &amp;amp;
   * &amp;apos;)
   *
   * @param original the string to encode
   * @return the encoded string */
  public static String xmlEncode(String original)
  {
    if(original == null)
      return null;

    char[] chars = original.toCharArray();
    final int len = chars.length;
    StringBuilder sb = new StringBuilder(len);

    for(int i = 0; i < chars.length; i++)
    {
      char c = chars[i];
      if(c < _chars.length)
      {
        sb.append(_chars[c]);
      }
      else if (Character.isHighSurrogate(c))
      {
        int j = i+1;
        if (j < len && Character.isLowSurrogate(chars[j]))
        {
          // use character reference anyway on this character, since the handling
          // of some big code blocks might not be well defined in certain encodings.
          sb.append("&#x").append(Integer.toHexString(Character.toCodePoint(c, chars[j]))).append(';');
          i = j;
        }
        // else ignore this character, a single high surrogate is an invalid char
      }
      else if (!Character.isLowSurrogate(c))
      {
        sb.append(c);
      }
    }

    return sb.toString();
  }

  /**
   * Encodes the string so that it is XML safe: all characters &lt;= 127
   * will not be encoded (converts &lt;, &gt; &amp; &apos; into their
   * equivalent (&amp;lt;, &amp;gt; &amp;amp; &amp;apos;)). All characters
   * &gt; 127 will be encoded as &amp;#xxxx; with xxxx the hexadecimal
   * value of the character. This method differs from
   * <code>xmlEncode</code> because it encodes all values &gt; 127 into a
   * pure ascii xml safe string. In most cases, this method is NOT
   * necessary because with an xml message you can specify the encoding, so
   * there is no need to encode those characters.
   *
   * @param s the string to encode
   * @return the encoded string */
  public static String xmlEncodeRaw(String s)
  {
    if(s == null)
      return null;

    char[] chars = s.toCharArray();
    final int len = chars.length;
    StringBuilder sb = new StringBuilder(len);

    for(int i = 0; i < len; i++)
    {
      char c = chars[i];
      if(c < _chars.length)
      {
        sb.append(_chars[c]);
      }
      else if (Character.isHighSurrogate(c))
      {
        int j = i+1;
        if (j < len && Character.isLowSurrogate(chars[j]))
        {
          sb.append("&#x").append(Integer.toHexString(Character.toCodePoint(c, chars[j]))).append(';');
          i = j;
        }
        // else ignore this character, a single high surrogate is an invalid char
      }
      else if (!Character.isLowSurrogate(c))
      {
        sb.append("&#x").append(Integer.toHexString(c)).append(';');
      }
    }

    return sb.toString();
  }

  /**
   * Encapsulates what to do for html. Delegate to {@link #xmlEncodeRaw(String)}.
   *
   * @param s
   * @return  the htmlized string
   * @see #xmlEncodeRaw(String) */
  public static String htmlEncode(String s)
  {
    return xmlEncodeRaw(s);
  }

  /**
   * This method descodes a string that was previously encoded for being XML
   * safe. It is the exact opposite of xmlEncode
   *
   * @param s the string to decode
   * @return the decoded string
   * @exception IllegalArgumentException if the string cannot be decoded */
  public static String xmlDecode(String s) throws IllegalArgumentException
  {
    if(s == null)
      return s;

    int idxS = s.indexOf('&');
    if(idxS < 0)
      return s;

    StringBuilder sb = new StringBuilder(s.length());

    int idxE, idx, size;
    char c, c_1;
    int prev = 0;

    while(idxS != -1)
    {
      idxE = s.indexOf(';', idxS);
      if(idxE < 0)
        throw new IllegalArgumentException("missing ';' in: " +
                                           s.substring(idxS));

      sb.append(s.substring(prev, idxS));

      idx = idxS + 1;
      size = idxE - idxS - 1;
      if(size < 2)
        throw new IllegalArgumentException("invalid escape tag: " +
                                           s.substring(idxS, idxE + 1));
      c = s.charAt(idx);
      c_1 = s.charAt(idx + 1);
      switch(c)
      {
        case 'l':
          if(size != 2)
            throw new IllegalArgumentException("invalid escape tag: " +
                                               s.substring(idxS, idxE + 1));
          if(c_1 == 't')
            sb.append('<');
          else
            throw new IllegalArgumentException("invalid escape tag: " +
                                               s.substring(idxS, idxE + 1));
          break;

        case 'g':
          if(size != 2)
            throw new IllegalArgumentException("invalid escape tag: " +
                                               s.substring(idxS, idxE + 1));
          if(c_1 == 't')
            sb.append('>');
          else
            throw new IllegalArgumentException("invalid escape tag: " +
                                               s.substring(idxS, idxE + 1));
          break;

        case 'q':
          if(size != 4)
            throw new IllegalArgumentException("invalid escape tag: " +
                                               s.substring(idxS, idxE + 1));
          if(c_1 != 'u' || s.charAt(idx + 2) != 'o' ||
             s.charAt(idx + 3) != 't')
            throw new IllegalArgumentException("invalid escape tag: " +
                                               s.substring(idxS, idxE + 1));
          else
            sb.append('"');
          break;

        case 'a':
          if(size == 3)
          {
            if(c_1 != 'm' || s.charAt(idx + 2) != 'p')
            throw new IllegalArgumentException("invalid escape tag: " +
                                               s.substring(idxS, idxE + 1));
            else
              sb.append('&');
          }
          else if(size == 4)
          {
            if(c_1 != 'p' || s.charAt(idx + 2) != 'o' ||
               s.charAt(idx + 3) != 's')
            throw new IllegalArgumentException("invalid escape tag: " +
                                               s.substring(idxS, idxE + 1));
            else
              sb.append('\'');
          }
          else
            throw new IllegalArgumentException("invalid escape tag: " +
                                               s.substring(idxS, idxE + 1));
          break;

        case '#':
          int codePoint;
          try
          {
            codePoint = (c_1 == 'x')
              ? Integer.parseInt(s.substring(idx + 2, idxE), 16)
              : Integer.parseInt(s.substring(idx + 1, idxE));
          }
          catch(NumberFormatException ex)
          {
            throw new IllegalArgumentException("invalid escape tag: " +
                                               s.substring(idxS, idxE + 1));
          }

          if (codePoint < 0)
            throw new IllegalArgumentException("invalid character codepoint: " +
                                               s.substring(idxS, idxE + 1));

          if (codePoint < Character.MIN_SUPPLEMENTARY_CODE_POINT)
            sb.append((char) codePoint);
          else
            sb.append(Character.toChars(codePoint));
          break;

        default:
            throw new IllegalArgumentException("invalid escape tag: " +
                                               s.substring(idxS, idxE + 1));
      }
      prev = idxE + 1;
      idxS = s.indexOf("&", prev);
    }
    if(prev < s.length())
      sb.append(s.substring(prev));

    return sb.toString();
  }
  
  public static String xmlDecodeSafely(String str)
  {
    // The following code is a copy of XMLUtils.xmlDecode() but without any IllegalArgumentExceptions thrown on unidentified or malformed entities. 
    // We need this ability because our input may or may not have properly escaped entities, and we should be able to handle both cases gracefully.
    // It also replaces &nbsp; occurrences with a space.
    // In casual perf tests this method performs better (~4x faster) than a corresponding method that uses a series of String.replace() calls. 
    int idxS = str.indexOf("&");
    if(idxS < 0)
    {
      // No entities to decode, so return String as is.
      return str;
    }

    StringBuilder sb = new StringBuilder(str.length());
    int idxE, idx, size;
    char c;
    int prev = 0;

    while(idxS != -1)
    {
      if (prev < idxS)
      {
        sb.append(str.substring(prev, idxS));
        // Update prev variable to indicate what's been consumed so far.
        prev = idxS;
      }

      idxE = str.indexOf(";", idxS);
      if(idxE < 0)
      {
        // No more properly formed entities to decode from this point on.
        break;
      }

      idx = idxS + 1;
      size = idxE - idxS - 1;
      if(size < 2)
      {
        // Unknown entity, so just append the faux entity as is and move on.
        sb.append(str.substring(idxS, idxE + 1));
      }
      else
      {
        c = str.charAt(idx);
        switch(c)
        {
          case 'l':
            if (!xmlDecodeLT(str, idx, sb, size))
            {
              // Unknown entity, so just consume the entity start (i.e. the ampersand) and move on.
              sb.append("&");
              idxE = idxS;
            }
            break;

          case 'g':
            if (!xmlDecodeGT(str, idx, sb, size))
            {
              // Unknown entity, so just consume the entity start (i.e. the ampersand) and move on.
              sb.append("&");
              idxE = idxS;
            }
            break;

          case 'q':
            if (!xmlDecodeQUOT(str, idx, sb, size))
            {
              // Unknown entity, so just consume the entity start (i.e. the ampersand) and move on.
              sb.append("&");
              idxE = idxS;
            }
            break;

          case 'a':
            if (!xmlDecodeAMPAPOS(str, idx, sb, size))
            {
              // Unknown entity, so just consume the entity start (i.e. the ampersand) and move on.
              sb.append("&");
              idxE = idxS;
            }
            break;

          case 'n':
            if (!xmlDecodeNBSP(str, idx, sb, size))
            {
              // Unknown entity, so just consume the entity start (i.e. the ampersand) and move on.
              sb.append("&");
              idxE = idxS;
            }
            break;
            
          case '#':
            if (!xmlDecodeNumber(str, idx, sb, idxE))
            {
              // Unknown entity, so just consume the entity start (i.e. the ampersand) and move on.
              sb.append("&");
              idxE = idxS;
            }
            break;

          default:
            // Unknown entity, so just consume the entity start (i.e. the ampersand) and move on.
            sb.append("&");
            idxE = idxS;
        }
      }
      
      prev = idxE + 1;
      idxS = str.indexOf("&", prev);
    }
    
    if (prev < str.length())
    {
      sb.append(str.substring(prev));
    }
    
    return sb.toString();
  }
  
  private static boolean xmlDecodeLT(String str, int idx, StringBuilder sb, int size)
  {
    boolean isRecognized = true;
    char c_1 = str.charAt(idx + 1);
    
    if ((size != 2) || (c_1 != 't'))
    {
      isRecognized = false;
    }
    else
    {
      // lt
      sb.append('<');
    }
    
    return isRecognized;
  }
  
  private static boolean xmlDecodeGT(String str, int idx, StringBuilder sb, int size)
  {
    boolean isRecognized = true;
    char c_1 = str.charAt(idx + 1);
    if ((size != 2) || (c_1 != 't'))
    {
      isRecognized = false;
    }
    else
    {
      // gt
      sb.append('>');
    }

    return isRecognized;
  }

  private static boolean xmlDecodeQUOT(String str, int idx, StringBuilder sb, int size)
  {
    boolean isRecognized = true;
    char c_1 = str.charAt(idx + 1);
    if ((size != 4) || (c_1 != 'u' || str.charAt(idx + 2) != 'o' || str.charAt(idx + 3) != 't'))
    {
      isRecognized = false;
    }
    else
    {
      // quot
      sb.append('"');
    }

    return isRecognized;
  }

  private static boolean xmlDecodeAMPAPOS(String str, int idx, StringBuilder sb, int size)
  {
    boolean isRecognized = true;
    char c_1 = str.charAt(idx + 1);
    if ((size == 3) && (c_1 == 'm') && (str.charAt(idx + 2) == 'p'))
    {
      // amp
      sb.append('&');
    }
    else if ((size == 4) && (c_1 == 'p') && (str.charAt(idx + 2) == 'o') && (str.charAt(idx + 3) == 's'))
    {
      // apos
      sb.append('\'');
    }
    else
    {
      isRecognized = false;
    }

    return isRecognized;
  }

  private static boolean xmlDecodeNBSP(String str, int idx, StringBuilder sb, int size)
  {
    boolean isRecognized = true;
    char c_1 = str.charAt(idx + 1);

    if ((size != 4) || (c_1 != 'b' || str.charAt(idx + 2) != 's' || str.charAt(idx + 3) != 'p'))
    {
      isRecognized = false;
    }
    else
    {
      // nbsp
      sb.append(' ');
    }

    return isRecognized;
  }

  private static boolean xmlDecodeNumber(String str, int idx, StringBuilder sb, int idxE)
  {
    boolean isRecognized = true;
    
    try
    {
      char c = 0;
      char c_1 = str.charAt(idx + 1);
      if(c_1 == 'x')
      {
        c = (char) Integer.parseInt(str.substring(idx + 2, idxE), 16);
      }
      else
      {
        c = (char) Integer.parseInt(str.substring(idx + 1, idxE));
      }
      sb.append(c);
    }
    catch(NumberFormatException ex)
    {
      isRecognized = false;
    }
    
    return isRecognized;
  }

  /**
   * Constructor. Do not use */
  private XMLUtils()
  {}
}
