/*
  Copyright 2018 - 2023 denk & dachte Software GmbH

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package de.denkunddachte.sfgapi;

import de.denkunddachte.util.ApiConfig;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.ft.Exportable;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ApiClient implements Exportable {
  private static final Logger    LOGGER                   = Logger.getLogger(ApiClient.class.getName());

  // JSON fields
  protected static final String  ID                       = "_id";
  protected static final String  HREF                     = "href";
  protected static final String  SLASH                    = "/";
  protected static final String  LOCATION                 = "Location";
  protected static final String  ERROR_DESCRIPTION        = "errorDescription";
  protected static final String  ERROR_CODE               = "errorCode";
  protected static final String  ROWS_AFFECTED            = "rowsAffected";
  protected static final String  CODE                     = "code";
  protected static final String  CONTENT_TYPE             = "Content-Type";
  protected static final String  ACCEPT                   = "Accept";
  protected static final String  ACCEPT_ENCODING          = "Accept-Encoding";
  protected static final String  API_ACCEPT_ENCODING      = "gzip";
  protected static final String  APPLICATION_JSON         = "application/json";
  protected static final String  AUTHORIZATION            = "Authorization";

  protected static final boolean ID_MATCH_CASE_SENSITVE   = true;
  protected static final boolean ID_MATCH_CASE_INSENSITVE = false;
  protected static final int     API_RANGESIZE;
  public static final String     API_DRYRUN_PROPERTY      = "apiclient.dryrun";

  // common API fields
  protected String               _id;
  protected String               href;

  // internal
  private boolean                refreshRequired;
  private JSONObject             origJSON;
  protected static ApiConfig     apicfg                   = null;
  private static int             apiReturnCode;
  private static String          apiErrorMsg;
  protected static final boolean DRYRUN                   = Boolean.parseBoolean(System.getProperty(API_DRYRUN_PROPERTY));

  static {
    try {
      apicfg = ApiConfig.getInstance();
    } catch (ApiException e) {
      LOGGER.log(Level.SEVERE, MessageFormat.format("Fatal error: {0}", e.getMessage()), e);
      System.exit(1);
    }
    API_RANGESIZE = apicfg.getApiRangeSize();
  }

  public enum RequestType {
    GET, POST, PUT, DELETE
  }

  public enum ResponseType {
    RAW, XML, JSON, JSON_ARRAY
  }

  protected void init() {
  }

  public abstract String getServiceName();

  public abstract String getIdProperty();

  public abstract JSONObject toJSON() throws JSONException;

  protected abstract ApiClient readJSON(JSONObject json) throws JSONException, ApiException;

  public abstract String getId();

  protected static boolean useWsApi(String svcName) {
    return apicfg.useWsApi(svcName);
  }
  
  public static void setConfig(ApiConfig cfg) {
    if (cfg != null) {
      apicfg = cfg;
    }
  }

  protected Date getDate(String isoDateTime) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    TemporalAccessor  ta        = formatter.parse(isoDateTime);
    Instant           i         = Instant.from(ta);
    return Date.from(i);
  }

  protected boolean matchIdCaseSensitive() {
    return ID_MATCH_CASE_SENSITVE;
  }

  protected void init(JSONObject json) throws JSONException {
    this._id = json.optString(ID);
    this.href = json.optString(HREF);
    this.origJSON = json;
  }

  public String getGeneratedId() {
    return _id;
  }

  protected void setGeneratedId(String id) {
    this._id = id;
    this.href = apicfg.getApiBaseURI() + SLASH + getServiceName() + SLASH + id;
    this.refreshRequired = true;
  }

  protected JSONObject getOrigJSON() {
    return origJSON;
  }

  public boolean isRefreshRequired() {
    return (refreshRequired || isModified());
  }

  protected void setRefreshRequired(boolean val) {
    refreshRequired = val;
  }

  protected void setRefreshRequired() {
    refreshRequired = true;
  }

  /**
   * Check if object is is new. Note: After a create() operation, the object is not updated, so isNew() will still return true.
   * 
   * @return true if object was not created from JSON
   */
  public boolean isNew() {
    return _id == null;
  }

  /**
   * Compares JSON output of current object state with the original (by creating JSON
   * 
   * @return true if object was modified
   */
  public boolean isModified() {
    if (origJSON == null)
      return true;
    try {
      Class<?>       clazz            = Class.forName(this.getClass().getName());
      Constructor<?> con              = clazz.getConstructor();
      ApiClient      orig             = ((ApiClient) con.newInstance()).readJSON(origJSON);
      JSONObject     jsonFromOriginal = orig.toJSON();
      return !this.toJSON().similar(jsonFromOriginal);
    } catch (ReflectiveOperationException | IllegalArgumentException | JSONException | ApiException e) {
      e.printStackTrace();
    }
    return true;
  }

  public static int getApiReturnCode() {
    return apiReturnCode;
  }

  public static String getApiErrorMsg() {
    return apiErrorMsg;
  }

  protected static void clearApiError() {
    apiErrorMsg = null;
    apiReturnCode = 0;
  }

  protected static void setApiError(int code, String error) {
    apiErrorMsg = error;
    apiReturnCode = code;
  }

  public void refresh() throws ApiException {
    if (isNew()) {
      // get newly created objects from server via REST Api:
      try {
        Method    find      = this.getClass().getDeclaredMethod("find", String.class);
        ApiClient newObject = (ApiClient) find.invoke(null, getId());
        if (newObject != null) {
          LOGGER.log(Level.FINER, "Refresh newly created object with json={0}", newObject.getOrigJSON());
          this.readJSON(newObject.getOrigJSON());
        } else {
          throw new ApiException("Could not get new " + this.getClass().getName() + " object with id " + getId() + " from server!");
        }
      } catch (Exception e) {
        throw new ApiException("Could not refresh new object " + this.getClass().getName() + " object with id " + getId() + "!", e);
      }
    } else {
      try (CloseableHttpResponse response = executeRequest(createRequest(RequestType.GET, new URI(this.href), null))) {
        JSONObject json = getJSON(getJSONResponse(response));
        if (json == null || json.has(ERROR_CODE)) {
          throw new ApiException("Could not refresh object " + this.getClass().getName() + " object with id " + getId() + ": "
              + (json != null ? json.getString(ERROR_DESCRIPTION) : "null") + "!");
        } else {
          this.readJSON(json);
          this.refreshRequired = false;
        }
      } catch (IOException | URISyntaxException | JSONException e) {
        throw new ApiException(e);
      }
    }
  }

  protected static HttpRequestBase createRequest(final RequestType requestType, final URI uri, final String data) throws UnsupportedEncodingException {
    HttpRequestBase httpRequest = null;
    switch (requestType) {
    case POST:
      httpRequest = new HttpPost(uri);
      if (data != null)
        ((HttpPost) httpRequest).setEntity(new StringEntity(data));
      break;
    case PUT:
      httpRequest = new HttpPut(uri);
      if (data != null)
        ((HttpPut) httpRequest).setEntity(new StringEntity(data));
      break;
    case DELETE:
      httpRequest = new HttpDelete(uri);
      break;
    // default request type is GET:
    case GET:
    default:
      httpRequest = new HttpGet(uri);
      break;
    }
    httpRequest.setHeader(CONTENT_TYPE, APPLICATION_JSON);
    httpRequest.setHeader(ACCEPT, APPLICATION_JSON);
    httpRequest.setHeader(AUTHORIZATION, apicfg.getApiCredentials());
    if (!httpRequest.containsHeader(ACCEPT_ENCODING)) {
      httpRequest.setHeader(ACCEPT_ENCODING, API_ACCEPT_ENCODING);
    }
    return httpRequest;
  }

  protected static HttpRequestBase createRequest(final RequestType requestType, final String svcURI, final String data)
      throws UnsupportedEncodingException, URISyntaxException {
    LOGGER.log(Level.FINEST, "Enter: requestType={0}, svcURI={1}, data={2}", new Object[] { requestType, svcURI, data });
    HttpRequestBase httpRequest = createRequest(requestType, new URI(apicfg.getApiBaseURI() + SLASH + svcURI), data);
    LOGGER.log(Level.FINEST, "Return: httpRequest={0}", httpRequest);
    return httpRequest;
  }

  protected static CloseableHttpResponse executeRequest(HttpRequestBase request) throws ApiException {
    LOGGER.log(Level.FINEST, "Enter: request={0}", request);
    final RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(300 * 60 * 1000).setConnectionRequestTimeout(300 * 60 * 1000)
        .setSocketTimeout(300 * 60 * 1000).setContentCompressionEnabled(false).build();
    apicfg.getHttpClientConnectionManager().closeExpiredConnections();
    apicfg.getHttpClientConnectionManager().closeIdleConnections(30, TimeUnit.SECONDS);
    CloseableHttpClient httpclient = HttpClientBuilder.create().setConnectionManager(apicfg.getHttpClientConnectionManager())
        .setRetryHandler(new DefaultHttpRequestRetryHandler(apicfg.getApiRequestRetries(), false)).setDefaultRequestConfig(requestConfig).build();
    clearApiError();

    try {
      return httpclient.execute(request);
    } catch (IOException e) {
      throw new ApiException("Error executing HTTP request: " + request + ": " + e.getMessage(), e);
    }
  }

  protected static String getJSONResponse(CloseableHttpResponse response) throws ApiException {
    int    httpCode   = response.getStatusLine().getStatusCode();
    String httpReason = response.getStatusLine().getReasonPhrase();
    apiReturnCode = httpCode;
    LOGGER.log(Level.FINEST, "Enter: response={0}, httpCode={1}, httpReason={2}", new Object[] { response, httpCode, httpReason });
    if (httpCode != 200 && httpCode != 201 && httpCode != 400 && httpCode != 404) {
      throw new ApiException("HTTP " + httpCode + " " + httpReason);
    }

    HttpEntity entity     = response.getEntity();
    String     jsonString = null;
    try {
      jsonString = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8.name());
    } catch (IOException e) {
      throw new ApiException(e);
    }
    if (httpCode == 400 || httpCode == 404) {
      LOGGER.log(Level.FINE, "Response: httpCode={0}, httpReason={1}, jsonString={2}", new Object[] { httpCode, httpReason, jsonString });
      if (jsonString.charAt(0) == '{') {
        JSONObject o = new JSONObject(jsonString);
        apiErrorMsg = o.getString(ERROR_DESCRIPTION);
      } else {
        JSONObject o = new JSONObject();
        o.put(ERROR_CODE, httpCode);
        o.put(ERROR_DESCRIPTION, "HTTP" + httpCode + "/" + httpReason);
        jsonString = o.toString(2);
        apiErrorMsg = httpReason;
      }
    }
    LOGGER.log(Level.FINEST, "Return: jsonString={0}", jsonString);
    return jsonString;
  }

  protected static String getCommaSeparatedList(Entry<String, Object> parameter) throws ApiException {
    if (parameter.getValue() == null) {
      return "";
    }
    if (parameter.getValue() instanceof String) {
      return ((String) parameter.getValue()).trim();
    } else if (parameter.getValue() instanceof String[]) {
      return String.join(",", (String[]) parameter.getValue());
    } else {
      throw new ApiException("Parameter " + parameter.getKey() + " requires String or String[] value!");
    }
  }

  /*
   * WS API returns JSON created from XML with /result/row structure (-> {"result":{"row": [ or { ...).
   * Remove the /result/row tiers from JSON string.
   */
  private static String convertJSONFromWsApi(String json, ResponseType type) {
    if (json.length() < 13 || !json.substring(0, (json.length() < 20 ? json.length() : 19)).contains("\"result\"")) {
      return json;
    }
    if (!json.contains("\"row\"") && type == ResponseType.JSON_ARRAY) {
      return "[]";
    }
    int p1 = json.indexOf('[');
    int p2 = json.length() - 1;

    if (p1 > -1 && p1 <= 32) {
      return json.substring(p1, json.lastIndexOf(']') + 1);
    }
    p1 = 0;

    int i = 2;
    while (i-- > 0) {
      p1 = json.indexOf('{', p1 + 1);
      p2 = json.lastIndexOf('}', p2 - 1);
    }
    if (type == ResponseType.JSON_ARRAY) {
      return "[" + json.substring(p1, p2 + 1) + "]";
    } else {
      return json.substring(p1, p2 + 1);
    }
  }

  private static Object getWithWsApi(final String svcName, final String parameter, ResponseType responseFormat) throws ApiException {
    LOGGER.log(Level.FINEST, "Enter: svcName={0}, parameter={1}", new Object[] { svcName, parameter });
    Object  ret     = null;
    boolean getJSON = responseFormat == ResponseType.JSON || responseFormat == ResponseType.JSON_ARRAY;
    try {
      HttpRequestBase httpRequest = createRequest(RequestType.GET,
          new URI(apicfg.getWsApiBaseURI() + "?api=" + svcName + (getJSON ? "&json=1" : "") + (parameter == null ? "" : parameter)), null);
      LOGGER.log(Level.FINEST, "httpRequest={0}", httpRequest);
      try (CloseableHttpResponse response = executeRequest(httpRequest)) {
        int    httpCode   = response.getStatusLine().getStatusCode();
        String httpReason = response.getStatusLine().getReasonPhrase();
        LOGGER.log(Level.FINEST, "Enter: response={0}, httpCode={1}, httpReason={2}", new Object[] { response, httpCode, httpReason });
        if (httpCode != 200 && httpCode != 201 && httpCode != 400 && httpCode != 404) {
          throw new ApiException("HTTP " + httpCode + " " + httpReason);
        }
        HttpEntity entity = response.getEntity();
        if (getJSON) {
          ret = convertJSONFromWsApi(getJSONResponse(response), responseFormat);
        } else if (responseFormat == ResponseType.XML) {
          ret = parseXml(new InputSource(entity.getContent()));
        } else {
          StringBuilder sb       = new StringBuilder();
          Charset       encoding = Charset.defaultCharset();
          if (entity.getContentEncoding() != null) {
            encoding = Charset.forName(entity.getContentEncoding().getValue());
          }
          try (Reader rd = new BufferedReader(new InputStreamReader(entity.getContent(), encoding))) {
            int c = 0;
            while ((c = rd.read()) != -1) {
              sb.append((char) c);
            }
          }
          return sb.toString();
        }
      }
    } catch (IOException | URISyntaxException e) {
      throw new ApiException(e);
    }
    return ret;
  }

  protected static Document getXmlDocumentFromWsApi(final String svcName, final String parameter) throws ApiException {
    return (Document) getWithWsApi(svcName, parameter, ResponseType.XML);
  }

  protected static String getFromWsApi(final String svcName, final String parameter) throws ApiException {
    return (String) getWithWsApi(svcName, parameter, ResponseType.RAW);
  }

  protected static String getJSONFromWsApi(final String svcName, final String parameter, boolean jsonArray) throws ApiException {
    return (String) getWithWsApi(svcName, parameter, (jsonArray ? ResponseType.JSON_ARRAY : ResponseType.JSON));
  }

  protected static String get(final String svcName) throws ApiException {
    return get(svcName, (String) null);
  }

  protected static Document parseXml(String xmlData) throws ApiException {
    return parseXml(new InputSource(new StringReader(xmlData)));
  }

  protected static Document parseXml(File xmlFile) throws ApiException {
    try {
      return parseXml(new InputSource(new FileReader(xmlFile)));
    } catch (FileNotFoundException e) {
      throw new ApiException(e);
    }
  }

  protected static Document parseXml(InputSource is) throws ApiException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      dbf.setXIncludeAware(false);
      dbf.setExpandEntityReferences(false);
      return dbf.newDocumentBuilder().parse(is);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      throw new ApiException(e);
    }
  }

  protected String xmlToString(Document doc, int indent) throws ApiException {
    if (doc == null) {
      return null;
    }
    TransformerFactory tf = TransformerFactory.newInstance();
    tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    try {
      Transformer t = tf.newTransformer();
      if (indent > 0) {
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(indent));
      }
      DOMSource    src = new DOMSource(doc);
      StringWriter wr  = new StringWriter();
      t.transform(src, new StreamResult(wr));
      return wr.getBuffer().toString();
    } catch (TransformerFactoryConfigurationError | TransformerException e) {
      throw new ApiException(e);
    }
  }

  protected static String get(final String svcName, final Map<String, Object> parameters) throws ApiException, UnsupportedEncodingException {
    if (parameters != null) {
      int           i  = 0;
      StringBuilder sb = new StringBuilder();
      sb.append('?');
      for (Entry<String, Object> e : parameters.entrySet()) {
        if (e.getValue() == null)
          continue;
        if (i++ > 0)
          sb.append('&');
        switch (e.getKey()) {
        case "offset":
          sb.append("_range=").append(e.getValue()).append('-').append((int) e.getValue() + API_RANGESIZE - 1);
          e.setValue((int) e.getValue() + API_RANGESIZE); // next offset -> 1000
          break;
        case "includeFields":
          sb.append("_include=").append(urlEncode(getCommaSeparatedList(e)));
          break;
        case "excludeFields":
          sb.append("_exclude=").append(urlEncode(getCommaSeparatedList(e)));
          break;
        case "sort":
          sb.append("_sort=").append(urlEncode(getCommaSeparatedList(e)));
          break;
        default:
          sb.append(e.getKey()).append('=').append(urlEncode(String.valueOf(e.getValue())));
        }
      }
      return get(svcName, sb.toString());
    } else {
      return get(svcName);
    }
  }

  protected static String get(final String svcName, final String parameter) throws ApiException {
    String result    = null;
    File   cacheFile = null;
    if (apicfg.isCacheResults()) {
      try {
        cacheFile = new File(apicfg.getCacheDir(), urlEncode(svcName + SLASH + parameter));
      } catch (UnsupportedEncodingException e) {
        // ignore
      }
      if (cacheFile != null && cacheFile.exists() && cacheFile.lastModified() > (System.currentTimeMillis() - apicfg.getCacheExpiryMillis())) {
        try (FileInputStream fis = new FileInputStream(cacheFile)) {
          byte[] d = new byte[(int) cacheFile.length()];
          fis.read(d);
          result = new String(d, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
          LOGGER.finer("Could not open cache file " + cacheFile + ": " + e.getMessage());
        }
      }
    }
    if (result == null) {
      try {
        HttpRequestBase req = createRequest(RequestType.GET, getSvcUri(svcName, parameter), null);
        try (CloseableHttpResponse response = executeRequest(req)) {
          result = getJSONResponse(response);
          if (cacheFile != null) {
            try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
              fos.write(result.getBytes(StandardCharsets.UTF_8.name()));
            }
          }
        }
      } catch (IOException | URISyntaxException e) {
        throw new ApiException(e);
      }
    }
    return result;
  }

  public boolean create() throws ApiException {
    boolean result = false;
    try {
      LOGGER.log(Level.FINER, "create(); JSON={0}", toJSON());
      JSONObject json = new JSONObject(create(getServiceName(), toJSON()));
      if (json.has(LOCATION)) {
        LOGGER.log(Level.FINE, "Created {0}, Location={1}", new Object[] { this, json.getString(LOCATION) });
        result = true;
      } else {
        LOGGER.log(Level.WARNING, "Could not create {0}, errorCode={1}, errorDescription={2}.",
            new Object[] { this, json.getInt(ERROR_CODE), json.get(ERROR_DESCRIPTION) });
      }
    } catch (IllegalStateException | JSONException e) {
      throw new ApiException(e);
    }

    return result;
  }

  protected String create(final String svcName, final JSONObject data) throws ApiException {
    if (DRYRUN) {
      LOGGER.log(Level.INFO, "DRY RUN: Skip operation={0}, service={1}.", new Object[] { "CREATE", svcName });
      LOGGER.log(Level.FINER, "DRY RUN: data: {0}.", data);
      JSONObject o = new JSONObject();
      o.put(LOCATION, "Skipped (DRY RUN)");
      return o.toString();
    }
    String result = null;
    try {
      HttpRequestBase req = createRequest(RequestType.POST, getSvcUri(svcName, null), data.toString());
      try (CloseableHttpResponse response = executeRequest(req)) {
        result = getJSONResponse(response);
      }
    } catch (IllegalStateException | IOException | URISyntaxException e) {
      throw new ApiException(e);
    }

    return result;
  }

  public boolean update() throws ApiException {
    return update(getId());
  }

  protected boolean update(String id) throws ApiException {
    boolean result = false;
    try {
      LOGGER.log(Level.FINER, "update(); JSON={0}", toJSON());
      JSONObject json = new JSONObject(update(getServiceName(), id, toJSON()));
      if (json.has(ROWS_AFFECTED) && json.getInt(ROWS_AFFECTED) == 1) {
        LOGGER.log(Level.FINE, "Updated {0}: {1}", new Object[] { id, this });
        result = true;
      } else {
        LOGGER.log(Level.WARNING, "Could not update {0}, errorCode={1}, errorDescription={2}.",
            new Object[] { this, json.getInt(ERROR_CODE), json.get(ERROR_DESCRIPTION) });
      }
    } catch (IllegalStateException | JSONException e) {
      throw new ApiException(e);
    }
    return result;
  }

  protected String update(final String svcName, final String parameter, final JSONObject data) throws ApiException {
    if (DRYRUN) {
      LOGGER.log(Level.INFO, "DRY RUN: Skip operation={0}, service={1}, params={2}.", new Object[] { "CREATE", svcName, parameter });
      LOGGER.log(Level.FINER, "DRY RUN: data: {0}.", data);
      JSONObject o = new JSONObject();
      o.put(ROWS_AFFECTED, 1);
      return o.toString();
    }
    String result = null;
    try {
      LOGGER.log(Level.FINER, "update(); data={0}", data);
      HttpRequestBase req = createRequest(RequestType.PUT, getSvcUri(svcName, parameter), data.toString());
      try (CloseableHttpResponse response = executeRequest(req)) {
        result = getJSONResponse(response);
        clearCache(svcName);
      }
    } catch (IllegalStateException | IOException | URISyntaxException e) {
      throw new ApiException(e);
    }
    return result;
  }

  protected static JSONObject findByKey(String svcName, String key, Map<String, Object> params) throws ApiException {
    JSONObject json = null;
    try {
      json = getJSON(get(svcName + SLASH + key, params));
    } catch (JSONException | UnsupportedEncodingException e) {
      throw new ApiException(e);
    }
    return json;
  }

  protected static JSONObject findByKey(String svcName, String key) throws ApiException {
    return findByKey(svcName, key, null);
  }

  protected void clearCache(String svcName) throws IOException {
    if (apicfg.isCacheResults()) {
      File cacheDir = apicfg.getCacheDir();
      if (!cacheDir.exists())
        return;
      for (File f : cacheDir.listFiles()) {
        if (f.getName().startsWith(svcName))
          Files.delete(f.toPath());
      }
    }
  }

  public boolean delete() throws ApiException {
    return delete(getId());
  }

  protected boolean delete(String id) throws ApiException {
    boolean result = false;
    try {
      JSONObject json = new JSONObject(delete(getServiceName(), id));
      if (json.has(ROWS_AFFECTED) && json.getInt(ROWS_AFFECTED) == 1) {
        LOGGER.log(Level.FINE, "Deleted {0}: {1}", new Object[] { id, this });
        result = true;
      } else {
        LOGGER.log(Level.WARNING, "Could not delete {0}, errorCode={1}, errorDescription={2}.",
            new Object[] { getId(), json.getInt(ERROR_CODE), json.get(ERROR_DESCRIPTION) });
      }
    } catch (JSONException jse) {
      throw new ApiException(jse);
    }
    return result;
  }

  protected String delete(final String svcName, final String parameter) throws ApiException {
    if (DRYRUN) {
      LOGGER.log(Level.INFO, "DRY RUN: Skip operation={0}, service={1}, params={2}.", new Object[] { "CREATE", svcName, parameter });
      JSONObject o = new JSONObject();
      o.put(ROWS_AFFECTED, 1);
      return o.toString();
    }
    String result = null;
    try {
      HttpRequestBase req = createRequest(RequestType.DELETE, getSvcUri(svcName, parameter), null);
      try (CloseableHttpResponse response = executeRequest(req)) {
        result = getJSONResponse(response);
        clearCache(svcName);
      }
    } catch (IllegalStateException | IOException | URISyntaxException e) {
      throw new ApiException(e);
    }
    return result;
  }

  protected String keyStringValue(Object value) {
    if (value != null) {
      return String.valueOf(value);
    } else {
      return "null";
    }
  }

  protected static String getSvcUri(final String svcName, final String parameter) {
    StringBuilder sb = new StringBuilder(svcName);
    sb.append(SLASH);
    if (parameter != null) {
      sb.append(parameter);
    }
    return sb.toString();
  }

  protected static JSONArray getJSONArray(final String jsonString) throws JSONException {
    JSONArray result = new JSONArray();
    if (jsonString != null && !jsonString.isEmpty()) {
      if (jsonString.charAt(0) == '[') {
        result = new JSONArray(jsonString);
      } else {
        JSONObject o = new JSONObject(jsonString);
        if (o.getInt(ERROR_CODE) != 400 && o.getInt(ERROR_CODE) != 404) {
          throw new JSONException("Unexpected JSON result for query: " + jsonString);
        }
      }
    }
    return result;
  }

  protected static JSONObject getJSON(final String jsonString) throws JSONException {
    return getJSON(jsonString, null, null, false);
  }

  protected static JSONObject getJSON(final String jsonString, String key, String matchValue, boolean caseSensitive) throws JSONException {
    JSONObject result = null;
    if (jsonString != null && !jsonString.isEmpty()) {
      if (jsonString.charAt(0) == '[') {
        JSONArray a = new JSONArray(jsonString);
        for (int i = 0; i < a.length(); i++) {
          if (key == null || (a.getJSONObject(i).has(key)
              && (caseSensitive ? matchValue.equals(a.getJSONObject(i).getString(key)) : matchValue.equalsIgnoreCase(a.getJSONObject(i).getString(key))))) {
            result = a.getJSONObject(i);
            break;
          }
        }
        if (result == null) {
          result = new JSONObject();
          result.put(ERROR_CODE, 404);
          result.put(ERROR_DESCRIPTION, "SFGAPI001: API found " + a.length() + " results but no exact match for: " + matchValue);
        }
      } else {
        result = new JSONObject(jsonString);
      }
    }
    return result;
  }

  protected static String urlEncode(String str) throws UnsupportedEncodingException {
    if (str.indexOf('/') > -1) {
      List<String> sl = new ArrayList<>();
      for (String s : str.split("/")) {
        sl.add(URLEncoder.encode(s, "UTF-8"));
      }
      return String.join("/", sl);
    } else {
      return URLEncoder.encode(str, "UTF-8");
    }
  }

  protected static OffsetDateTime toOffsetDateTime(String isoTimestampWithOffeset) {
    OffsetDateTime odt = null;
    if (isoTimestampWithOffeset != null && !isoTimestampWithOffeset.trim().isEmpty()) {
      try {
        odt = OffsetDateTime.parse(isoTimestampWithOffeset.replaceAll("([+-])(\\d\\d)(\\d\\d)$", "$1$2:$3"), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
      } catch (DateTimeParseException e) {
        LOGGER.log(Level.WARNING, "Could not parse ISO timestamp: {0}", isoTimestampWithOffeset);
      }
    }
    return odt;
  }

  protected boolean isNullOrEmpty(String val) {
    return val == null || val.trim().isEmpty();
  }

  protected String getWsApiBaseURI() {
    return apicfg.getWsApiBaseURI();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof ApiClient)) {
      return false;
    }
    ApiClient castOther = (ApiClient) other;
    if (!this.getServiceName().equals(castOther.getServiceName())) {
      return false;
    }
    if (matchIdCaseSensitive()) {
      return this.getIdProperty().equals(castOther.getIdProperty()) && this.getId() != null && this.getId().equals(castOther.getId());
    } else {
      return this.getIdProperty().equals(castOther.getIdProperty()) && this.getId() != null && this.getId().equalsIgnoreCase(castOther.getId());
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int       hash  = 17;
    hash = hash * prime + this.getIdProperty().hashCode();
    if (this.getId() != null)
      hash = hash * prime + (matchIdCaseSensitive() ? this.getId().hashCode() : this.getId().toLowerCase().hashCode());
    return hash;
  }

  // Interface Exportable:
  @Override
  public void export(PrintWriter out) throws ApiException {
    export(out, true, true);
  }

  @Override
  public void export(PrintWriter out, boolean prettyPrint, boolean suppressNullValues) throws ApiException {
    JSONObject copy = new JSONObject(origJSON == null ? toJSON() : origJSON);
    copy.put("b2bApiClass", this.getClass().getName());
    out.append(copy.toString(prettyPrint ? 2 : 0));
  }

  @Override
  public String getBasename() {
    return getId();
  }

  @Override
  public Mode getExportMode() {
    return Mode.JSON;
  }

  public static String getWsApiVersion() throws JSONException, ApiException {
    if (useWsApi("version")) {
      JSONObject o = new JSONObject(getJSONFromWsApi("version", "&json=1", false));
      if (o.has("API_VERSION")) {
        return (o.has("API_BP_NAME") ? o.getString("API_BP_NAME") : apicfg.getWsApiBpName()) + "-" + o.getString("API_VERSION");
      }
      throw new ApiException("WS API is not deployed or does not support the \"version\" API: " + o.optString(ERROR_DESCRIPTION) + "!");
    } else {
      return "<0.2.2 (\"version\" API not activated)";
    }
  }
}
