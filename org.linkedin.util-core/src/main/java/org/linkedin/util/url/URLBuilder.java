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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.linkedin.util.text.StringSplitter;

/**
 * Convenient class to build a URL. (not thread safe!).
 *
 * @author ypujante@linkedin.com
 */
public class URLBuilder implements URL, Serializable
{
  private static final long serialVersionUID = 1L;

  private static StringSplitter SS = new StringSplitter('/');

  private String _scheme = null;
  private String _userInfo = null;
  private String _host = null;
  private int _port = -1;
  private String _path = null;
  private QueryBuilder _query = null;
  private String _fragment = null;
  private boolean _escapeFragment = true;

  /**
   * Constructor */
  public URLBuilder()
  {
    _query = new QueryBuilder();
  }

  /**
   * Copy Constructor
   * @param other
   */
  private URLBuilder(URLBuilder other)
  {
    _scheme = other._scheme;
    _userInfo = other._userInfo;
    _host = other._host;
    _port = other._port;
    _path = other._path;
    _query = other._query.deepClone();
    _fragment = other._fragment;
  }

  @Override
  public String getScheme()
  {
    return _scheme;
  }

  public void setScheme(String scheme)
  {
    _scheme = scheme;
  }

  @Override
  public boolean getHasScheme()
  {
    return _scheme != null;
  }

  @Override
  public String getUserInfo()
  {
    return _userInfo;
  }

  public void setUserInfo(String userInfo)
  {
    _userInfo = userInfo;
  }

  @Override
  public boolean getHasUserInfo()
  {
    return _userInfo != null;
  }

  @Override
  public String getHost()
  {
    return _host;
  }

  public void setHost(String host)
  {
    _host = host;
  }

  @Override
  public boolean getHasHost()
  {
    return _host != null;
  }

  @Override
  public int getPort()
  {
    return _port;
  }

  public void setPort(int port)
  {
    _port = port;
  }

  @Override
  public boolean getHasPort()
  {
    return _port > -1;
  }

  @Override
  public String getPath()
  {
    return _path;
  }

  /**
   * No check or escaping is done. Use this call to set a relative path.
   *
   * @param path the value to set */
  public void setPath(String path)
  {
    if("".equals(path))
      path = null;

    _path = path;
  }

  /**
   * Appends the given path. The path is escaped: all / are turned into %2d.
   * After this call, <code>newpath = oldpath + / + path</code>.
   * As a consequence, this method cannot be used to set a relative path.
   *
   * @param path the path to append */
  public URLBuilder appendPath(String path)
  {
    appendPath(path, true);
    return this;
  }

  /**
   * Appends the given path. The path is conditionaly escaped.
   * After this call, <code>newpath = oldpath + / + path</code>.
   * As a consequence, this method cannot be used to set a relative path.
   *
   * @param path the path to append
   * @param encodePath <code>true</code> to encode the path */
  public void appendPath(String path, boolean encodePath)
  {
    if(path == null)
      return;

    if(encodePath)
      path = urlEncode(path);

    if(path.startsWith("/"))
      path = path.substring(1);

    if(_path == null || "".equals(_path))
    {
      StringBuilder sb = new StringBuilder("/");
      sb.append(path);
      _path = sb.toString();
    }
    else
    {
      StringBuilder sb = new StringBuilder(_path);

      if(_path.endsWith("/"))
        sb.append(path);
      else
        sb.append('/').append(path);

      _path = sb.toString();
    }
  }

  /**
   * @return <code>true</code> if a path has been set */
  @Override
  public boolean getHasPath()
  {
    return _path != null && _path.length() > 0;
  }

  /**
   * @return the path splitted at each '/' and decoded
   */
  @Override
  public List<String> getPathComponents()
  {
    if(getHasPath())
    {
      String path = getPath();

      if("/".equals(path))
        return Collections.emptyList();

      // if path starts with "/"
      if(path.startsWith("/"))
        path = path.substring(1);

      if(path.endsWith("/"))
        path = path.substring(0, path.length() - 1);

      List<String> pathComponents = SS.splitAsList(path);

      ListIterator<String> iter = pathComponents.listIterator();
      while(iter.hasNext())
      {
        String pathComponent = iter.next();
        iter.set(urlDecode(pathComponent));
      }

      return pathComponents;
    }
    else
    {
      return Collections.emptyList();
    }
  }

  @Override
  public String getFragment()
  {
    return _fragment;
  }

  /**
   * @param fragment the fragment to set (after #)
   */
  public void setFragment(String fragment)
  {
    _fragment = fragment;
  }

  /**
   * Changes whether we'll uri escape the fragment.  If this is supplied the fragment must be
   * in *uric format (see RFC2396)
   *
   * Note that this class incorrectly encodes the fragment.
   *
   * @param escapeFragment whether or not to escape the fragment
   */
  public URLBuilder setEscapeFragment(boolean escapeFragment) {
    _escapeFragment = escapeFragment;
    return this;
  }

  /**
   * @return <code>true</code> if the fragment has been set */
  @Override
  public boolean getHasFragment()
  {
    return _fragment != null;
  }

  @Override
  public String getQueryString()
  {
    return _query.getQuery();
  }

  @Override
  public Query getQuery()
  {
    return _query;
  }

//  public QueryBuilder getQueryBuilder()
//  {
//    return _query;
//  }

  /**
   * @return <code>true</code> if query parameters have been added */
  @Override
  public boolean getHasQueryParameters()
  {
    return _query.getHasQueryParameters();
  }

  /**
   * @return the url */
  @Override
  public String getURL()
  {
    String query = _query.getQuery();
    // this code has been copied from URI class
    StringBuilder sb = new StringBuilder();
    if(_scheme != null)
    {
      sb.append(_scheme);
      sb.append(':');
    }

    if(_host != null)
    {
      sb.append("//");
      if(_userInfo != null)
      {
        sb.append(_userInfo);
        sb.append('@');
      }

      boolean needBrackets = ((_host.indexOf(':') >= 0)
                              && !_host.startsWith("[")
                              && !_host.endsWith("]"));
      if(needBrackets) sb.append('[');
      sb.append(_host);
      if(needBrackets) sb.append(']');
      if(_port != -1)
      {
        sb.append(':');
        sb.append(_port);
      }
    }
    if (_path != null)
      sb.append(_path);
    if(!"".equals(query))
      sb.append('?').append(query);
    if(_fragment != null) {
      sb.append('#').append(_escapeFragment ? urlEncode(_fragment) : _fragment);
    }
    return sb.toString();
  }

  /**
   * @return this object as a {@link URL}
   */
  @Override
  public java.net.URL toJavaURL()
  {
    try
    {
      return new java.net.URL(getURL());
    }
    catch(MalformedURLException e)
    {
      // shouldn't happen because this class always builds well formed URLs...
      throw new RuntimeException(e);
    }
  }

  /**
   * Adds the query parameter (no value)
   *
   * @param name
   * @return this object for chaining */
  public URLBuilder addQueryParameter(String name)
  {
    _query.addParameter(name);
    return this;
  }

  /**
   * Adds the query parameter
   *
   * @param name
   * @param value
   * @return this object for chaining */
  public URLBuilder addQueryParameter(String name, String value)
  {
    if(value == null)
      throw new IllegalArgumentException("value is null for " + name);

    _query.addParameter(name, value);
    return this;
  }

  /**
   * Adds the query parameter
   *
   * @param name
   * @param value
   * @return this object for chaining */
  public URLBuilder addQueryParameter(String name, int value)
  {
    _query.addParameter(name, value);
    return this;
  }
  
  /**
   * Adds the query parameter
   *
   * @param name
   * @param value
   * @return this object for chaining */
  public URLBuilder addQueryParameter(String name, boolean value)
  {
    _query.addBooleanParameter(name, value);
    return this;
  }

  /**
   * Adds the query parameter
   *
   * @param name
   * @param values
   * @return this object for chaining */
  public URLBuilder addQueryParameter(String name, int [] values)
  {
    String [] params = new String[values.length];
    int idx = 0;
    
    if (values.length > 0)
    {
      for (int value: values)
      {
        params[idx++] = String.valueOf(value); 
      }
    
      _query.addParameters(name, params);   
    }
    
    return this;
  }
  
  /**
   * Adds the query parameter
   * 
   * @param name
   * @param values
   * @return this object for chaining
   */
  public URLBuilder addQueryParameter(String name, String[] values)
  {
    if (values != null && values.length > 0)
    {
      _query.addParameters(name, values);
    }
    return this;
  }

  
  /**
   * Adds the query parameter
   *
   * @param name
   * @param value
   * @return this object for chaining */
  public URLBuilder addQueryParameter(String name, long value)
  {
    _query.addParameter(name, value);
    return this;
  }

  /**
   * @see QueryBuilder#addIndexedParameter(String, int, int)
   * @return this object for chaining */
  public URLBuilder addQueryIndexedParameter(String name, int value, int index)
  {
    _query.addIndexedParameter(name, value, index);
    return this;
  }

  /**
   * @see QueryBuilder#addIndexedParameter(String, int, int...)
   * @return this object for chaining */
  public URLBuilder addQueryIndexedParameter(String name, int value, int... indices)
  {
    _query.addIndexedParameter(name, value, indices);
    return this;
  }

  /**
   * @see QueryBuilder#addIndexedParameter(String, String, int)
   * @return this object for chaining */
  public URLBuilder addQueryIndexedParameter(String name, String value, int index)
  {
    _query.addIndexedParameter(name, value, index);
    return this;
  }

  /**
   * @see QueryBuilder#addIndexedParameter(String, String, int...)
   * @return this object for chaining */
  public URLBuilder addQueryIndexedParameter(String name, String value, int... indices)
  {
    _query.addIndexedParameter(name, value, indices);
    return this;
  }

  /**
   * Adds the query parameter. If <code>value</code> is <code>true</code> then
   * adds the name as the value, else don't add it at all.
   *
   * @param name
   * @param value
   * @return this object for chaining */
  public URLBuilder addQueryBooleanParameter(String name, boolean value)
  {
    _query.addBooleanParameter(name, value);
    return this;
  }

  /**
   * Adds the query parameters
   * 
   * @param parameters
   * @return this object for chaining
   */
  public URLBuilder addQueryParameters(Map<String, String[]> parameters)
  {
    _query.addParameters(parameters);
    return this;
  }

  /**
   * Simply adds the query provided. It is assumed that the query is properly
   * encoded for url!
   *
   * @param query the properly url encoded query
   * @return this object for chaining  */
  public URLBuilder addQuery(String query) throws URISyntaxException
  {
    _query.addQuery(query);
    return this;
  }

  /**
   * Simply adds the query provided.
   *
   * @param query the query */
  public URLBuilder addQuery(Query query)
  {
    _query.addQuery(query);
    return this;
  }

  /**
   * Simply adds the query provided.
   *
   * @param uri the query */
  public URLBuilder addQuery(URI uri)
  {
    _query.addQuery(uri);
    return this;
  }
  
  /**
   * Get the first query parameter given its name
   *
   * @param name the name of the parameter
   * @return the first parameter  
   */
  public String getQueryParameter(String name)
  {
    return _query.getParameter(name);
  }
  
  /**
   * Gets the parameters given its name
   *
   * @param name the name of the parameter
   * @return the parameters or <code>null</code> if none found
   */
  public String[] getQueryParameterValues(String name)
  {
    return _query.getParameterValues(name);
  }
  
  /**
   * Remove parameter with given name and
   * return its previous value.
   *  
   * @param name parameter to remove
   * @return previous value or null if parameter doesn't exist
   */
  public String[] replaceQueryParameter(String name, String value)
  {
    return _query.replaceParameter(name, value);
  }
  
  /**
   * Remove parameter with given name and
   * return its previous value.
   *  
   * @param name parameter to remove
   * @return previous value or null if parameter doesn't exist
   */
  public String[] removeQueryParameter(String name)
  {
    return _query.removeParameter(name);
  }
  
  /**
   * Remove parameter with given name and
   * return its previous value.
   *  
   * @param names parameter to remove
   */
  public void removeQueryParameters(String... names)
  {
    _query.removeParameters(names);
  }
  
  /**
   * Call this method when you want to reset the internal string to start
   * from scratch again */
  public void reset()
  {
    _scheme = null;
    _userInfo = null;
    _host = null;
    _port = -1;
    _path = null;
    _query = new QueryBuilder();
    _fragment = null;
    _escapeFragment = true;
  }

  public void resetQuery()
  {
    _query = new QueryBuilder();
  }
  
  /**
   * @return a url which is relative (does not contain anything before path)
   */
  @Override
  public URL createRelativeURL()
  {
    URLBuilder url = URLBuilder.createFromPath(getPath());
    url.addQuery(getQuery());
    url.setFragment(getFragment());
    return url;
  }
  /**
   * @return a new cloned object */
  public URLBuilder deepClone()
  {
    return new URLBuilder(this);
  }

  @Override
  public String toString()
  {
    return getURL();
  }

  @Override
  public boolean equals(Object o)
  {
    if(this == o) return true;
    if(o == null || getClass() != o.getClass()) return false;

    final URLBuilder that = (URLBuilder) o;

    if(_port != that._port) return false;
    if(_fragment != null ? !_fragment.equals(that._fragment) : that._fragment != null) return false;
    if(_host != null ? !_host.equals(that._host) : that._host != null) return false;
    if(_path != null ? !_path.equals(that._path) : that._path != null) return false;
    if(!_query.equals(that._query)) return false;
    if(_scheme != null ? !_scheme.equals(that._scheme) : that._scheme != null) return false;
    if(_userInfo != null ? !_userInfo.equals(that._userInfo) : that._userInfo != null) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    int result;
    result = (_scheme != null ? _scheme.hashCode() : 0);
    result = 29 * result + (_userInfo != null ? _userInfo.hashCode() : 0);
    result = 29 * result + (_host != null ? _host.hashCode() : 0);
    result = 29 * result + _port;
    result = 29 * result + (_path != null ? _path.hashCode() : 0);
    result = 29 * result + _query.hashCode();
    result = 29 * result + (_fragment != null ? _fragment.hashCode() : 0);
    return result;
  }

  /**
   * Encapsulates the call to encoding a URL so that we don't have to deal
   * with the encoding.
   *
   * @param original the string to encode
   * @return the encoded url */
  public static String urlEncode(String original)
  {
    return URLCodec.instance().urlEncode(original);
  }

  /**
   * Encapsulates the call to encoding a URL
   *
   * @param original the string to encode
   * @param encoding the encoding to use
   * @return the encoded url */
  public static String urlEncode(String original, String encoding)
    throws UnsupportedEncodingException
  {
    return new URLCodec(true, encoding).urlEncode(original);
  }

  /**
   * Encapsulates the call to decoding a URL so that we don't have to deal
   * with the encoding.
   *
   * @param original the string to decode
   * @return the encoded url */
  public static String urlDecode(String original)
  {
    return URLCodec.instance().urlDecode(original);
  }

  /**
   * Encapsulates the call to decoding a URL so that we don't have to deal
   * with the encoding.
   *
   * @param original the string to decode
   * @param encoding the encoding to use
   * @return the encoded url */
  public static String urlDecode(String original, String encoding)
    throws UnsupportedEncodingException
  {
    return new URLCodec(true, encoding).urlDecode(original);
  }

  /**
   * Factory method from a path
   *
   * @param path the path (will be url encoded!)
   * @return the builder */
  public static URLBuilder createFromPath(String path)
  {
    URLBuilder res = new URLBuilder();
    res.setPath(path);
    return res;
  }

  /**
   * Factory method from a url
   *
   * @param url the url
   * @return the builder
   * @throws URISyntaxException if the url is invalid */
  public static URLBuilder createFromURL(String url)
      throws URISyntaxException
  {
    if(url == null)
    {
      return null;
    }
    else
    {
      url = url.trim();
    }
    
    URI uri = new URI(url);
    if (uri.isOpaque())
      throw new URISyntaxException(url, "opaque uris not supported");

    URLBuilder res = new URLBuilder();
    res.setScheme(uri.getScheme());
    res.setUserInfo(uri.getUserInfo());

    // CA-1599 -- Java's URI fails to parse the hostname if it has underscores in it.
    // this is because _ is not a valid char in hostnames, yet there are systems out there
    // that have it. Java's URL, however, handles it fine.
    if (uri.getHost() == null && res.getHasScheme())
    {
      try
      {
        java.net.URL u = new java.net.URL(url);
        res.setHost(u.getHost());
        res.setPort(u.getPort());
      }
      catch (java.net.MalformedURLException e)
      {
        URISyntaxException uex = new URISyntaxException(url, e.getMessage());
        uex.initCause(e); // keep the original exception
        throw uex;
      }
    }
    else
    {
      res.setHost(uri.getHost());
      res.setPort(uri.getPort());
    }

    res.setPath(uri.getRawPath());
    res.setFragment(uri.getFragment());
    res.addQuery(uri);

    return res;

  }

  /**
   * Factory method from a url
   *
   * @param url the url
   * @return the builder */
  public static URLBuilder createFromURL(URL url)
  {
    if(url == null)
      return null;
    
    // fast implementation
    if(url instanceof URLBuilder)
    {
      URLBuilder urlBuilder = (URLBuilder) url;
      return urlBuilder.deepClone();
    }

    try
    {
      return createFromURL(url.getURL());
    }
    catch(URISyntaxException e)
    {
      throw new RuntimeException("bad url!" + url.getURL());
    }
  }

  /**
   * Convenient call which adds a query parameter to the give url.
   *
   * @param url the original URL: will stay unchanged
   * @param name the name of the query parameter
   * @param value the value of the query parameter
   * @return the new URL */
  public static URL addQueryParameter(URL url, String name, String value)
  {
    URLBuilder ub = createFromURL(url);
    ub.addQueryParameter(name, value);
    return ub;
  }

  /**
   * Returns an array of the '/' delimited components for a URL path, starting
   * just after the servlet path. For example, if requestURI is leo/a/b/c/d/
   * and servletPath is /a, then the array returned will be {b,c,d}. The path
   * components are returned decoded (not htmlified)
   * @param requestURI request URI (obtained via request.getRequestURI())
   * @param servletPath servlet path (obtained via request.getServletPath())
   * @return an array of the components in the URI, starting just after the
   * specified servletPath.
   */
  public static String[] getPathComponents(String requestURI, String servletPath)
  {
    ArrayList<String> tokens = new ArrayList<String>();

    if (requestURI != null && servletPath != null && servletPath.length() > 0)
    {
      int servletPathStartPos = requestURI.indexOf(servletPath);
      if (servletPathStartPos != -1)
      {
        String components = requestURI.substring(servletPathStartPos + servletPath.length());
        StringTokenizer st = new StringTokenizer(components, "/");
        while (st.hasMoreTokens())
        {
          tokens.add(urlDecode(st.nextToken()));
        }
      }
    }

    return tokens.toArray(new String[tokens.size()]);
  }
}
