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
package de.denkunddachte.sspcmapi;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import de.denkunddachte.util.ApiConfig;
import de.denkunddachte.exception.ApiException;

public abstract class AbstractSspApiClient implements AutoCloseable {
  private static final Logger    LOGGER              = Logger.getLogger(AbstractSspApiClient.class.getName());
  protected static final String  SESSIONSVC          = "session";
  protected static final String  SLASH               = "/";
  private static final String    XML_RESPONSE        = "XmlResponse";
  protected static final String  RESULTSKEY          = "XML";
  protected static final String  CONTENT_TYPE        = "Content-Type";
  protected static final String  ACCEPT              = "Accept";
  protected static final String  ACCEPT_ENCODING     = "Accept-Encoding";
  protected static final String  API_ACCEPT_ENCODING = "gzip";
  protected static final String  APPLICATION_JSON    = "application/json";
  private static final String    APPLICATION_XML     = "application/xml";
  protected static final String  X_AUTHENTICATION    = "X-Authentication";
  protected static final String  X_PASSPHRASE        = "X-Passphrase";
  public static final String     API_DRYRUN_PROPERTY = "apiclient.dryrun";
  protected static final boolean DRYRUN              = Boolean.parseBoolean(System.getProperty(API_DRYRUN_PROPERTY));

  public enum RequestType {
    GET, POST, PUT, DELETE
  }

  public enum ResponseType {
    UNDEF, XML
  }

  // instance variables
  private String      sessionToken;
  CloseableHttpClient httpclient;
  private ApiConfig   apicfg = null;
  private int         apiReturnCode;
  private String      apiErrorMsg;
  private String      apiBaseURI;
  private String      passphrase;

  public AbstractSspApiClient() throws ApiException {
    init();
  }

  protected void init() throws ApiException {
    apicfg = ApiConfig.getInstance();
    final RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(300 * 60 * 1000).setConnectionRequestTimeout(300 * 60 * 1000)
        .setSocketTimeout(300 * 60 * 1000).setContentCompressionEnabled(false).build();
    apicfg.getHttpClientConnectionManager().closeExpiredConnections();
    httpclient = HttpClientBuilder.create().setConnectionManager(apicfg.getHttpClientConnectionManager()).setDefaultRequestConfig(requestConfig).build();
    apiBaseURI = apicfg.getSspBaseURI();
    if (login(apicfg.getSspUser(), apicfg.getSspPassword())) {
      LOGGER.log(Level.FINER, "Login succeeded. Token: {0}", sessionToken);
    }
  }

  protected boolean login(String user, String password) throws ApiException {
    if (sessionToken == null) {
      LOGGER.log(Level.FINER, "Login to SSP CM {0} with user {1}", new Object[] { apiBaseURI, user });
      JSONObject json = new JSONObject();
      json.put("userId", user);
      json.put("password", password);
      XmlResponse xr = doPost(SESSIONSVC, null, null, json.toString());
      if (xr.getHttpCode() == HttpStatus.SC_OK) {
        JSONObject obj = new JSONObject(xr.getObjectsList().get(0));
        this.sessionToken = obj.getString("sessionToken");
        return true;
      } else {
        LOGGER.log(Level.SEVERE, "Login request returns HTTP code {0}!", xr.getHttpCode());
        return false;
      }
    } else {
      LOGGER.log(Level.FINER, "Already logged in. Use sessionToken: {0}", sessionToken);
      return true;
    }
  }

  public boolean logoff() throws ApiException {
    XmlResponse xr = doDelete(SESSIONSVC, null, null);
    LOGGER.log(Level.FINEST, "Logoff {0}", sessionToken);
    sessionToken = null;
    return xr.getHttpCode() == HttpStatus.SC_OK;
  }

  public XmlResponse doGet(String method, String object) throws ApiException {
    return doGet(getServiceName(), method, object);
  }

  private XmlResponse doGet(String svc, String method, String object) throws ApiException {
    LOGGER.log(Level.FINEST, "doGet() service={0}, method={1}, object={2}", new Object[] { svc, method, object });
    HttpGet req = new HttpGet(apiBaseURI + SLASH + svc + (method == null ? "" : SLASH + method) + (object == null ? "" : SLASH + urlEncode(object)));
    req.addHeader(X_AUTHENTICATION, sessionToken);
    req.addHeader(X_PASSPHRASE, getPassphrase());
    clearApiError();
    LOGGER.log(Level.FINER, "Execute request {0}", req);
    try (CloseableHttpResponse response = httpclient.execute(req)) {
      return getXmlResponse(response.getEntity());
    } catch (IOException e) {
      throw new ApiException("Could not execute request " + req + "!", e);
    }
  }

  public XmlResponse doPost(String method, String object, String data) throws ApiException {
    return doPost(getServiceName(), method, object, data);
  }

  private XmlResponse doPost(String svc, String method, String object, String data) throws ApiException {
    LOGGER.log(Level.FINEST, "doPost() service={0}, method={1}, object={2}, data:\n{3}", new Object[] { svc, method, object, data });
    if (!SESSIONSVC.equals(svc) && DRYRUN) {
      LOGGER.log(Level.INFO, "DRY RUN: Skip operation={0}, method={1}, object={2}.", new Object[] { "POST", method, object });
      LOGGER.log(Level.FINER, "DRY RUN: data: {0}.", data);
      return new XmlResponse(HttpStatus.SC_OK, "DRY-RUN", true);
    }
    HttpPost req = new HttpPost(apiBaseURI + SLASH + svc + (method == null ? "" : SLASH + method) + (object == null ? "" : SLASH + urlEncode(object)));
    if (sessionToken != null)
      req.addHeader(X_AUTHENTICATION, sessionToken);
    clearApiError();
    try {
      StringEntity entity = new StringEntity(data);
      entity.setContentType(data.charAt(0) == '<' ? APPLICATION_XML : APPLICATION_JSON);
      req.setEntity(entity);
      LOGGER.log(Level.FINER, "Execute request {0}", req);
      try (CloseableHttpResponse response = httpclient.execute(req)) {
        return getXmlResponse(response.getEntity());
      }
    } catch (IOException e) {
      throw new ApiException("Could not execute request " + req + "!", e);
    }
  }

  public XmlResponse doPut(String method, String object, String data) throws ApiException {
    return doPut(getServiceName(), method, object, data);
  }

  private XmlResponse doPut(String svc, String method, String object, String data) throws ApiException {
    LOGGER.log(Level.FINEST, "doPut() service={0}, method={1}, object={2}, data:\n{3}", new Object[] { svc, method, object, data });
    if (DRYRUN) {
      LOGGER.log(Level.INFO, "DRY RUN: Skip operation={0}, method={1}, object={2}.", new Object[] { "PUT", method, object });
      LOGGER.log(Level.FINER, "DRY RUN: data: {0}.", data);
      return new XmlResponse(HttpStatus.SC_OK, "DRY-RUN", true);
    }
    HttpPut req = new HttpPut(apiBaseURI + SLASH + svc + (method == null ? "" : SLASH + method) + (object == null ? "" : SLASH + urlEncode(object)));
    req.addHeader(X_AUTHENTICATION, sessionToken);
    req.addHeader(X_PASSPHRASE, getPassphrase());
    clearApiError();
    try {
      StringEntity entity = new StringEntity(data);
      entity.setContentType(data.charAt(0) == '<' ? APPLICATION_XML : APPLICATION_JSON);
      req.setEntity(entity);
      LOGGER.log(Level.FINER, "Execute request {0}", req);
      try (CloseableHttpResponse response = httpclient.execute(req)) {
        return getXmlResponse(response.getEntity());
      }
    } catch (IOException e) {
      throw new ApiException("Could not execute request " + req + "!", e);
    }
  }

  public XmlResponse doDelete(String method, String object) throws ApiException {
    return doDelete(getServiceName(), method, object);
  }

  private XmlResponse doDelete(String svc, String method, String object) throws ApiException {
    LOGGER.log(Level.FINEST, "doDelete() service={0}, method={1}, object={2}", new Object[] { svc, method, object });
    if (DRYRUN) {
      LOGGER.log(Level.INFO, "DRY RUN: Skip operation={0}, method={1}, object={2}.", new Object[] { "DELETE", method, object });
      return new XmlResponse(HttpStatus.SC_OK, "DRY-RUN", true);
    }
    HttpDelete req = new HttpDelete(apiBaseURI + SLASH + svc + (method == null ? "" : SLASH + method) + (object == null ? "" : SLASH + urlEncode(object)));
    req.addHeader(X_AUTHENTICATION, sessionToken);
    clearApiError();
    try {
      LOGGER.log(Level.FINER, "Execute request {0}", req);
      try (CloseableHttpResponse response = httpclient.execute(req)) {
        return getXmlResponse(response.getEntity());
      }
    } catch (IOException e) {
      throw new ApiException("Could not execute request " + req + "!", e);
    }
  }

  protected XmlResponse getXmlResponse(HttpEntity entity) throws ApiException {
    try {
      StringReader rd = new StringReader(IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8.name()));
      rd.mark(1000);
      char[] cb = new char[100];
      if (rd.read(cb) < 20 || !(new String(cb)).contains(XML_RESPONSE)) {
        throw new ApiException("Did not receive a valid XlmResponse!");
      }
      rd.reset();
      JAXBContext jctx = JAXBContext.newInstance(XmlResponse.class);
      Unmarshaller um = jctx.createUnmarshaller();
      XmlResponse xmlResponse = (XmlResponse) um.unmarshal(rd);
      if (xmlResponse != null) {
        apiReturnCode = xmlResponse.getHttpCode();
        apiErrorMsg = (xmlResponse.getValidationErrors() != null ? xmlResponse.getValidationErrors()
            : "[" + xmlResponse.getMessageLevel() + "]: " + xmlResponse.getMessage());
        LOGGER.log(Level.FINER, "Got XmlResponse {0}/{1}, validationErrors: {2}, messageLevel={3}, message={4}", new Object[] { xmlResponse.getHttpCode(),
            xmlResponse.getHttpStatus(), xmlResponse.getValidationErrorsList(), xmlResponse.getMessageLevel(), xmlResponse.getMessage() });
        return xmlResponse;
      } else {
        throw new ApiException("XmlResponse is null!");
      }
    } catch (IOException | JAXBException e) {
      throw new ApiException(e);
    }
  }

  protected Object unmarshalResult(XmlResponse xr, Class<?> clazz) throws ApiException {
    if (xr.getResults().get(RESULTSKEY) == null) {
      throw new ApiException("XmlResponse does not contain XML result node!");
    }
    try {
      JAXBContext ctx = JAXBContext.newInstance(clazz);
      Unmarshaller unmarshaller = ctx.createUnmarshaller();
      return unmarshaller.unmarshal(new InputSource(new StringReader(xr.getResults().get(RESULTSKEY))));
    } catch (JAXBException e) {
      throw new ApiException(e);
    }
  }

  protected String marshalObject(Object in, Class<?> clazz) throws ApiException {
    try {
      JAXBContext ctx = JAXBContext.newInstance(clazz);
      Marshaller marshaller = ctx.createMarshaller();
      StringWriter wr = new StringWriter();
      marshaller.marshal(in, wr);
      return wr.toString();
    } catch (JAXBException e) {
      throw new ApiException(e);
    }
  }

  public int getApiReturnCode() {
    return apiReturnCode;
  }

  public String getApiErrorMsg() {
    return apiErrorMsg;
  }

  protected void clearApiError() {
    apiErrorMsg = null;
    apiReturnCode = 0;
  }

  protected static String urlEncode(String str) throws ApiException {
    try {
      return URLEncoder.encode(str.replace("/", "*SLASH*"), "UTF-8").replace("*SLASH*", "/");
    } catch (UnsupportedEncodingException e) {
      throw new ApiException(e);
    }
  }

  /**
   * 
   * @param results
   * @return list of results
   */
  public static List<String> convertToList(String results) {
    String s = results;
    List<String> l = new ArrayList<>();
    if (s == null || s.isEmpty()) {
      return l;
    }
    if (s.startsWith("[") && s.endsWith("]")) {
      s = s.substring(1, s.length() - 1);
      String[] split = s.split(",");
      for (String x : split) {
        if (x.startsWith("\"") && x.endsWith("\"")) {
          x = x.substring(1, x.length() - 1);
        }
        l.add(x);
      }
    }
    return l;
  }

  @Override
  public void close() throws ApiException {

    if (sessionToken != null) {
      logoff();
    }
  }

  abstract String getServiceName();

  public String getPassphrase() {
    if (passphrase == null) {
      byte[] p = new byte[8];
      for (int i = 0; i < p.length; i++) {
        p[i] = (byte) (Math.random() * 255);
      }
      passphrase = Base64.getEncoder().encodeToString(p);
    }
    return passphrase;
  }

  public void setPassphrase(String passphrase) {
    this.passphrase = passphrase;
  }

}
