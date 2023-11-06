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

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.denkunddachte.exception.ApiException;

public class SshAuthorizedUserKey extends AbstractSfgKey {
  private final static Logger   LOGGER   = Logger.getLogger(SshAuthorizedUserKey.class.getName());
  protected static final String SVC_NAME = "sshauthorizeduserkeys";

  public SshAuthorizedUserKey() {
    super();
  }

  public SshAuthorizedUserKey(String keyName, String keyString, boolean keyStatusEnabled) throws InvalidKeyException {
    super(keyName, keyString, keyStatusEnabled);
  }

  private SshAuthorizedUserKey(JSONObject json) throws JSONException, ApiException {
    super();
    this.readJSON(json);
  }

  @Override
  public String getServiceName() {
    return SVC_NAME;
  }

  @Override
  protected SshAuthorizedUserKey readJSON(JSONObject json) throws JSONException, ApiException {
    super.readJSON(json);
    return this;
  }

  @Override
  public String toString() {
    return "SshAuthorizedUserKey [" + super.toString() + "]";
  }

  // static lookup methods:
  public static List<SshAuthorizedUserKey> findAll() throws ApiException {
    return findAll(null);
  }

  public static List<SshAuthorizedUserKey> findAll(String filter, String... includeFields) throws ApiException {
    if (useWsApi(SVC_NAME)) {
      return findAllWithWSApi(filter);
    } else {
      return findAllWithRESTApi(filter, includeFields);
    }
  }

  private static List<SshAuthorizedUserKey> findAllWithWSApi(String filter, String... includeFields) throws ApiException {
    List<SshAuthorizedUserKey> result = new ArrayList<SshAuthorizedUserKey>();
    try {
      Document        xmlDoc       = getXmlDocumentFromWsApi(SVC_NAME, (filter != null ? "&searchFor=" + urlEncode(filter.replace('*', '%')) : null));
      XPathFactory    xPathFactory = XPathFactory.newInstance();
      XPath           xpath        = xPathFactory.newXPath();
      XPathExpression expr         = xpath.compile("/result/row");
      NodeList        nl           = (NodeList) expr.evaluate(xmlDoc, XPathConstants.NODESET);

      for (int i = 0; i < nl.getLength(); i++) {
        Node n = nl.item(i);
        try {
          SshAuthorizedUserKey uk = new SshAuthorizedUserKey((String) xpath.evaluate("./keyName", n, XPathConstants.STRING),
              (String) xpath.evaluate("./keyData", n, XPathConstants.STRING), true);
          uk.setGeneratedId(uk.getId());
          LOGGER.finer("Got SshAuthorizedUserKey: " + uk);
          result.add(uk);
        } catch (InvalidKeyException e) {
          final String keyName = (String) xpath.evaluate("./keyName", n, XPathConstants.STRING);
          LOGGER.log(Level.WARNING, e, () -> "Found invalid SSH key " + keyName + ": " + e.getMessage());
        }
      }
    } catch (UnsupportedEncodingException | XPathExpressionException e) {
      throw new ApiException(e);
    }
    return result;
  }

  // static lookup methods:
  private static List<SshAuthorizedUserKey> findAllWithRESTApi(String globPattern, String... includeFields) throws ApiException {
    List<SshAuthorizedUserKey> result = new ArrayList<SshAuthorizedUserKey>();
    try {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("offset", 0);
      params.put("includeFields", includeFields);
      params.put("searchFor", (globPattern != null ? globPattern.replace('*', '%') : null));
      while (true) {
        JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
        for (int i = 0; i < jsonObjects.length(); i++) {
          try {
            result.add(new SshAuthorizedUserKey(jsonObjects.getJSONObject(i)));
          } catch (ApiException e) {
            final String keyName = jsonObjects.getJSONObject(i).optString("keyName");
            LOGGER.log(Level.WARNING, e, () -> "Found invalid SSH key " + keyName + ": " + e.getMessage());
          }
        }
        if (jsonObjects.length() < API_RANGESIZE)
          break;
      }
    } catch (JSONException | UnsupportedEncodingException e) {
      throw new ApiException(e);
    }
    return result;
  }

  // find by key
  public static SshAuthorizedUserKey find(String keyName) throws ApiException {
    SshAuthorizedUserKey result = null;
    JSONObject           json   = findByKey(SVC_NAME, keyName);
    try {
      if (json.has("errorCode")) {
        LOGGER.finer("SshAuthorizedUserKey " + keyName + " not found: errorCode=" + json.getInt("errorCode") + ", errorDescription="
            + json.get("errorDescription") + ".");
      } else {
        result = new SshAuthorizedUserKey(json);
        LOGGER.finer("Found SshAuthorizedUserKey " + keyName + ": " + result);
      }
    } catch (JSONException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public static boolean exists(SshAuthorizedUserKey key) throws ApiException {
    return exists(key.getId());
  }

  public static boolean exists(String keyName) throws ApiException {
    JSONObject json = findByKey(SVC_NAME, keyName);
    return json.has(ID_PROPERTY);
  }
}
