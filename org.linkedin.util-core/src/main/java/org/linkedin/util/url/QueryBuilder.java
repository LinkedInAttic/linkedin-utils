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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.linkedin.util.lang.LangUtils;
import org.linkedin.util.text.StringSplitter;
import org.linkedin.util.text.TextUtils;

/**
 * Represents the query part in the URL
 *
 * @author ypujante@linkedin.com
 */
public class QueryBuilder implements Query, Serializable
{
  private static final long serialVersionUID = 1L;

  private static StringSplitter SS = new StringSplitter('&');

  /**
   * key = name (<code>String</code>), value = values (<code>String[]</code>) */
  private Map<String, String[]> _queryParameters = null;
  private final StringBuilder _query = new StringBuilder();
  private final URLCodec _urlCodec;

  /**
   * Constructor */
  public QueryBuilder()
  {
    _urlCodec = URLCodec.instance();
  }

  /**
   * Constructor */
  public QueryBuilder(String encoding) throws UnsupportedEncodingException
  {
    // the purpose is to test right away that the encoding is supported
    // rather than later in the code
    _urlCodec = new URLCodec(true, encoding);
  }

  /**
   * Constructor */
  public QueryBuilder(URLCodec urlCodec)
  {
    _urlCodec = urlCodec;
  }

  /**
   * @return the encoding used by the query */
  @Override
  public String getEncoding()
  {
    return _urlCodec.getCharacterEncoding();
  }

  /**
   * @return <code>true</code> if query parameters have been added */
  @Override
  public boolean getHasQueryParameters()
  {
    return _query.length() > 0;
  }

  /**
   * @return the iterator of parameter names  */
  @Override
  public Iterator<String> getParameterNames()
  {
    return getParameterMap().keySet().iterator();
  }

  /**
   * @return the query as a string */
  @Override
  public String getQuery()
  {
    return _query.toString();
  }

  /**
   * Gets the parameters given its name
   *
   * @param name the name of the parameter
   * @return the parameters or <code>null</code> if none found*/
  @Override
  public String[] getParameterValues(String name)
  {
    return getMap().get(name);
  }

  /**
   * @return the parameter map */
  @Override
  public Map<String,String[]> getParameterMap()
  {
    return Collections.unmodifiableMap(getMap());
  }

  /**
   * Get the first parameter given its name
   *
   * @param name the name of the parameter
   * @return the first parameter  */
  @Override
  public String getParameter(String name)
  {
    String[] params = getParameterValues(name);
    if(params == null)
      return null;

    return params[0];
  }


  /**
   * @see org.linkedin.util.url.Query#getBooleanParameter(java.lang.String)
   */
  @Override
  public boolean getBooleanParameter(String name)
  {
    String paramValue = getParameter(name);
    
    if (!TextUtils.isEmptyString(paramValue) && paramValue.equals(name))
    {
      return true;
    }
    else 
    {
      return LangUtils.convertToBoolean(paramValue);
    }
  }
  
  /**
   * @see org.linkedin.util.url.Query#getIntParameter(java.lang.String, int)
   */
  @Override
  public int getIntParameter(String name, int defaultValue)
  {
    String paramValue = getParameter(name);
    if (TextUtils.isEmptyString(paramValue))
    {
      return defaultValue;
    }
    else
    {
      try 
      {
        return Integer.parseInt(paramValue);
      }
      catch (NumberFormatException e)
      {
        return defaultValue;
      }
    }
  }

  
  /**
   * Adds the query parameter (no value)
   *
   * @param name */
  public void addParameter(String name)
  {
    addParameter(name, "");
  }

  /**
   * Add query parameter to query string.
   * 
   * @param name
   * @param value
   */
  private void addQueryParameter(String name, String value)
  {
    if(_query.length() > 0)
      _query.append('&');
    _query.append(encode(name));
    _query.append('=');
    _query.append(encode(value));
  }
  
  /**
   * Adds the query parameter
   *
   * @param name
   * @param value */
  public void addParameter(String name, String value)
  {
    addQueryParameter(name, value);
    
    if(_queryParameters != null)
      addParameterToMap(name, value);
  }

  /**
   * Adds the query parameter
   *
   * @param name
   * @param value */
  private void addParameterToMap(String name, String value)
  {
    String[] params = getParameterValues(name);
    if(params == null)
    {
      params = new String[1];
      params[0] = value;
      _queryParameters.put(name, params);
    }
    else
    {
      int len = params.length;
      String[] newParams = new String[len + 1];
      System.arraycopy(params, 0, newParams, 0, len);
      newParams[len] = value;
      _queryParameters.put(name, newParams);
    }
  }

  /**
   * Adds the query parameter
   *
   * @param name
   * @param values */
  public void addParameters(String name, String[] values)
  {
    if(_query.length() > 0)
      _query.append('&');

    String encodedName = encode(name);
    for(int i = 0; i < values.length; i++)
    {
      if(i > 0)
        _query.append('&');
      _query.append(encodedName);
      _query.append('=');
      _query.append(encode(values[i]));
    }

    if(_queryParameters != null)
      addParametersToMap(name, values);
  }

  /**
   * Adds the map of parameters
   * @param parameters
   */
  public void addParameters(Map<String, String[]> parameters)
  {
    for(Map.Entry<String, String[]> entry : parameters.entrySet())
    {
      addParameters(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Adds the query parameter
   *
   * @param name
   * @param values */
  private void addParametersToMap(String name, String[] values)
  {
    String[] params = getParameterValues(name);
    if(params == null)
    {
      _queryParameters.put(name, values);
    }
    else
    {
      int len = params.length;
      String[] newParams = new String[len + values.length];
      System.arraycopy(params, 0, newParams, 0, len);
      System.arraycopy(values, 0, newParams, len, values.length);
      _queryParameters.put(name, newParams);
    }
  }

  /**
   * Adds the query parameter
   *
   * @param name
   * @param value */
  public void addParameter(String name, int value)
  {
    addParameter(name, String.valueOf(value));
  }

  /**
   * Adds the query parameter
   *
   * @param name
   * @param value */
  public void addParameter(String name, long value)
  {
    addParameter(name, String.valueOf(value));
  }

  /**
   * Adds a parameter that comes from an array at the provided index
   *
   * @param name the name of the parameter
   * @param value the value
   * @param index the index in the array
   */
  public void addIndexedParameter(String name, String value, int index)
  {
    addParameter(getIndexedParamName(name, index), value);
  }

  /**
   * Adds a parameter that comes from an array at the provided index (can have multiple dimensions!)
   *
   * @param name the name of the parameter
   * @param value the value
   * @param indices the indices in the array (multiple dimensions)
   */
  public void addIndexedParameter(String name, String value, int... indices)
  {
    addParameter(getIndexedParamName(name, indices), value);
  }

  /**
   * Adds a parameter that comes from an array at the provided index
   *
   * @param name the name of the parameter
   * @param value the value
   * @param index the index in the array
   */
  public void addIndexedParameter(String name, int value, int index)
  {
    addParameter(getIndexedParamName(name, index), value);
  }

  /**
   * Adds a parameter that comes from an array at the provided index (can have multiple dimensions!)
   *
   * @param name the name of the parameter
   * @param value the value
   * @param indices the indices in the array (multiple dimensions)
   */
  public void addIndexedParameter(String name, int value, int... indices)
  {
    addParameter(getIndexedParamName(name, indices), value);
  }

  /**
   * Adds the query parameter. If <code>value</code> is <code>true</code> then
   * adds the name as the value, else don't add it at all.
   *
   * @param name
   * @param value */
  public void addBooleanParameter(String name, boolean value)
  {
    if(value)
      addParameter(name, name);
  }

  /**
   * Simply adds the query provided. It is assumed that the query is properly
   * encoded for url!
   *
   * @param query the properly url encoded query (<code>null</code> is ok) */
  public void addQuery(String query) throws URISyntaxException
  {
    addQuery(query, true);
  }

  /**
   * Adds the query extracted from the URI. No Exception is thrown because the
   * uri could not be built if it was not correct.
   * 
   * @param uri  */
  public void addQuery(URI uri)
  {
    try
    {
      addQuery(uri.getRawQuery(), false);
    }
    catch(URISyntaxException e)
    {
      // should not happen!
      throw new RuntimeException(e);
    }
  }
  /**
   * Simply adds the query provided. It is assumed that the query is properly
   * encoded for url!
   *
   * @param query the properly url encoded query (<code>null</code> is ok)
   * @param validate <code>true</code> true for validating (should be called
   * with <code>false</code> only when sure that it is valid..) */
  private void addQuery(String query, boolean validate) throws URISyntaxException
  {
    if(query == null)
      return;

    if("".equals(query))
      return;

    if(validate)
      validateQuery(query);

    if(_query.length() > 0)
      _query.append('&');

    _query.append(query);

    if(_queryParameters != null)
      addQueryToMap(query);
  }

  /**
   * Internal method in charge of validating the query provided. Exception
   * when query is not valid.
   *
   * @param query the query to validate
   * @throws URISyntaxException if query invalid */
  private void validateQuery(String query) throws URISyntaxException
  {
    if(query.length() == 0)
      return;

    Iterator<String> iter = SS.splitToIterator(query);

    while(iter.hasNext())
    {
      String s = iter.next();
      if(s.length() > 0)
      {
        int idx = s.indexOf('=');
        if(idx == -1)
          throw new URISyntaxException(query, "missing equal sign in " + s);
        if(s.lastIndexOf('=') != idx)
          throw new URISyntaxException(query, "extra equal sign in " + s);
      }
    }
  }

  /**
   * Simply adds the query provided. It is assumed that the query is properly
   * encoded for url!
   *
   * @param query the properly url encoded query */
  private void addQueryToMap(String query)
  {
    String[] params = SS.split(query);
    for(int i = 0; i < params.length; i++)
    {
      String param = params[i];
      if(param.length() == 0)
        continue;

      int idx = param.indexOf('=');
      if(idx == -1)
        addParameterToMap(decode(param), null);
      else
        addParameterToMap(decode(param.substring(0, idx)),
            decode(param.substring(idx + 1)));
    }
  }

  /**
   * Simply adds the query provided.
   *
   * @param query the query (<code>null</code> is ok) */
  public void addQuery(Query query)
  {
    if(query == null)
      return;
    try
    {
      if(!query.getEncoding().equals(getEncoding()))
        throw new RuntimeException("TODO");

      addQuery(query.getQuery(), false);
    }
    catch(URISyntaxException e)
    {
      // shouldn't happen since a query is already properly formatted
      throw new RuntimeException(e);
    }
  }

  /**
   * Call this method when you want to reset the internal string to start
   * from scratch again */
  public void reset()
  {
    _query.setLength(0);
    _queryParameters = null;
  }

  /**
   * @return as a string   */
  @Override
  public String toString()
  {
    return getQuery();
  }

  @Override
  public boolean equals(Object o)
  {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;

    final QueryBuilder that = (QueryBuilder) o;

    if(!_urlCodec.equals(that._urlCodec)) return false;
    if(!_query.toString().equals(that._query.toString())) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    int result;
    result = _query.hashCode();
    result = 29 * result + _urlCodec.hashCode();
    return result;
  }

  /**
   * Internal call that will return the map. If <code>null</code> then first
   * populate it
   * @return the map  */
  private Map<String,String[]> getMap()
  {
    if(_queryParameters != null)
      return _queryParameters;

    _queryParameters = new LinkedHashMap<String,String[]>();
    addQueryToMap(_query.toString());

    return _queryParameters;
  }

  /**
   * @return a new cloned object */
  public QueryBuilder deepClone()
  {
    QueryBuilder query = new QueryBuilder(_urlCodec);
    query.addQuery(this);
    return query;
  }

  /**
   * Encodes the string
   *
   * @param s
   * @return the string encoded */
  private String encode(String s)
  {
    return _urlCodec.urlEncode(s);
  }

  /**
   * Decodes the string
   *
   * @param s
   * @return the string decoded */
  private String decode(String s)
  {
    return _urlCodec.urlDecode(s);
  }

  /**
   * Returns the name of the param that represent an entry in an array. This is the method
   * used by <code>addIndexed*</code> methods.
   *
   * @param paramPrefix the prefix for the param
   * @param index the index in the array
   * @return the name of the param that represent an entry in an array
   */
  public static String getIndexedParamName(String paramPrefix, int index)
  {
    StringBuilder sb = new StringBuilder(paramPrefix);
    sb.append('_').append(index);

    return sb.toString();
  }

  /**
   * Returns the name of the param that represent an entry in an array. This is the method
   * used by <code>addIndexed*</code> methods.
   *
   * @param paramPrefix the prefix for the param
   * @param indices the indices in the array (multiple dimensions)
   * @return the name of the param that represent an entry in an array
   */
  public static String getIndexedParamName(String paramPrefix, int... indices)
  {
    StringBuilder sb = new StringBuilder(paramPrefix);
    for(int index : indices)
    {
      sb.append('_').append(index);
    }

    return sb.toString();
  }

  /**
   * Rebuild query instance with values in the map. 
   */
  private void rebuildQuery()
  {
    Map<String, String[]> map = getMap();
    
    // reset query instance and re-populate it again
    _query.setLength(0);
    for (String key : map.keySet())
    {
      String[] parameters = map.get(key);
      for (String param : parameters)
      {
        addQueryParameter(key, param);
      }
    }
  }
  
  /**
   * Remove parameter with given name and
   * return its previous value.
   *  
   * @param parameterNames
   */
  public void removeParameters(String... parameterNames)
  {
    Map<String, String[]> map = getMap();
    boolean needsRebuild = false;
    for (String name : parameterNames)
    {
      if(map.remove(name) != null)
        needsRebuild = true;
    }
    if(needsRebuild)
      rebuildQuery();
  }
  
  /**
   * Remove parameter with given name and
   * return its previous value.
   *  
   * @param name parameter to remove
   * @return previous value or null if parameter doesn't exist
   */
  public String[] removeParameter(String name)
  {
    Map<String, String[]> map = getMap();
    String[] v = map.remove(name);
    if(v != null)
      rebuildQuery();    
    return v;
  }
  
  /**
   * Replace existing or add new parameter with given
   * value. Any existing parameter values are
   * deleted prior to adding the new value.
   *  
   * @param name parameter to replace
   * @return previous values or null if parameter doesn't exist
   */
  public String[] replaceParameter(String name, String value)
  {
    Map<String, String[]> map = getMap();
    String[] v = map.put(name, new String[] { value });
    rebuildQuery();
    return v;
  }
  
}
