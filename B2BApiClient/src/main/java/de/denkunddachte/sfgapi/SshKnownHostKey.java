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
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Base64;
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

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import de.denkunddachte.exception.ApiException;
import de.denkunddachte.ft.SshKey;

public class SshKnownHostKey extends AbstractSfgKey {
  private static final Logger                       LOGGER           = Logger.getLogger(SshKnownHostKey.class.getName());
  protected static final String                     SVC_NAME         = "sshknownhostkeys";
  protected static final String                     SVC_NAME_GRABBER = "sshhostidentitykeygrabbers";
  private static final Map<String, SshKnownHostKey> hostkeys         = new HashMap<>();

  private String                                    hostname;
  private int                                       port;

  public SshKnownHostKey() {
    super();
  }

  public SshKnownHostKey(String keyName, String keyString, boolean keyStatusEnabled) throws InvalidKeyException {
    super(keyName, keyString, keyStatusEnabled);
  }

  private SshKnownHostKey(JSONObject json) throws JSONException, ApiException {
    super();
    this.readJSON(json);
  }

  @Override
  public String getServiceName() {
    return SVC_NAME;
  }

  @Override
  protected SshKnownHostKey readJSON(JSONObject json) throws JSONException, ApiException {
    super.init(json);
    this.keyName = json.optString("keyName");
    if (json.has("keyData")) {
      try {
        String decoded = new String(Base64.getDecoder().decode(json.getString("keyData")));
        this.sshKey = new SshKey(decoded.replaceFirst(".*(ssh-rsa|ssh-dss|ecdsa-sha2-\\S+|ssh-ed\\d+|rsa-sha2-\\d+\\s+.+)", "$1"));
      } catch (Exception ike) {
        throw new ApiException(ike);
      }
    }
    if (json.has("keyStatusEnabled"))
      this.keyStatusEnabled = json.getJSONObject("keyStatusEnabled").getBoolean("code");

    this.keyId = json.optString("keyId");
    this.keyFingerPrint = json.optString("keyFingerPrint");
    this.keyLength = json.optInt("keyLength");
    return this;
  }

  @Override
  public String toString() {
    return "SshKnownHostKey [toString()=" + super.toString() + "]";
  }

  public void setKeyName(String keyName) {
    this.keyName = keyName;
  }

  @Override
  public void setKeyStatusEnabled(boolean enabled) {
    this.keyStatusEnabled = enabled;
  }

  @Override
  public boolean create() throws ApiException {
    boolean result = super.create();
    if (result) {
      String name = getKeyName().toLowerCase();
      if (getHostname() != null) {
        name = getHostname().toLowerCase() + ":" + getPort();
      }
      hostkeys.put(name, this);
    }
    return result;
  }

  public String getHostname() {
    return hostname;
  }

  public int getPort() {
    return port;
  }

  // static lookup methods:
  public static List<SshKnownHostKey> findAll() throws ApiException {
    return findAll(null);
  }

  public static List<SshKnownHostKey> findAll(String filter, String... includeFields) throws ApiException {
    if (useWsApi(SVC_NAME)) {
      return findAllWithWSApi(filter);
    } else {
      return findAllWithRESTApi(filter, includeFields);
    }
  }

  private static List<SshKnownHostKey> findAllWithWSApi(String filter) throws ApiException {
    List<SshKnownHostKey> result = new ArrayList<>();
    try {
      Document        xmlDoc       = getXmlDocumentFromWsApi(SVC_NAME, (filter != null ? "&searchFor=" + urlEncode(filter.replace('*', '%')) : null));
      XPathFactory    xPathFactory = XPathFactory.newInstance();
      XPath           xpath        = xPathFactory.newXPath();
      XPathExpression expr         = xpath.compile("/result/row");
      NodeList        nl           = (NodeList) expr.evaluate(xmlDoc, XPathConstants.NODESET);

      for (int i = 0; i < nl.getLength(); i++) {
        Node            n  = nl.item(i);
        SshKnownHostKey uk = new SshKnownHostKey((String) xpath.evaluate("./keyName", n, XPathConstants.STRING),
            (String) xpath.evaluate("./keyData", n, XPathConstants.STRING), true);
        uk.setGeneratedId(uk.getId());
        LOGGER.log(Level.FINER, "Got SshKnownHostKey: {0}", uk);
        result.add(uk);
      }
    } catch (UnsupportedEncodingException | XPathExpressionException | InvalidKeyException e) {
      throw new ApiException(e);
    }
    return result;
  }

  // static lookup methods:
  private static List<SshKnownHostKey> findAllWithRESTApi(String globPattern, String... includeFields) throws ApiException {
    List<SshKnownHostKey> result = new ArrayList<>();
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("offset", 0);
      params.put("includeFields", includeFields);
      params.put("searchFor", (globPattern != null ? globPattern.replace('*', '%') : null));
      while (true) {
        JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
        for (int i = 0; i < jsonObjects.length(); i++) {
          result.add(new SshKnownHostKey(jsonObjects.getJSONObject(i)));
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
  public static SshKnownHostKey find(String keyName) throws ApiException {
    SshKnownHostKey result = null;
    JSONObject      json   = findByKey(SVC_NAME, keyName);
    try {
      if (json.has(ERROR_CODE)) {
        LOGGER.log(Level.FINER, "SshKnownHostKey {0} not found: errorCode={1}, errorDescription={2}.",
            new Object[] { keyName, json.getInt(ERROR_CODE), json.get(ERROR_DESCRIPTION) });
      } else {
        result = new SshKnownHostKey(json);
        LOGGER.log(Level.FINER, "Found SshKnownHostKey {0}: {1}", new Object[] { keyName, result });
      }
    } catch (JSONException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public static SshKnownHostKey grab(String host, int port) throws ApiException {
    SshKnownHostKey result = null;
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("offset", 0);
      params.put("hostName", host);
      params.put("port", port);
      JSONObject json = getJSON(get(SVC_NAME_GRABBER, params));
      if (json.has(ERROR_CODE)) {
        LOGGER.log(Level.FINER, "Could not grab host key for host {0}, port {1}: errorCode={2}, errorDescription={3}.",
            new Object[] { host, port, json.getInt(ERROR_CODE), json.get(ERROR_DESCRIPTION) });
      } else {
        result = new SshKnownHostKey(json);
        result.hostname = json.getString("hostName");
        result.port = json.getInt("port");
        result.keyName = result.hostname.toLowerCase() + ":" + result.port;
        LOGGER.log(Level.FINER, "Found SshKnownHostKey: {0}", result);
      }
    } catch (JSONException | UnsupportedEncodingException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public static SshKnownHostKey grabWithSsh(String host, int port) throws ApiException {
    Session sshSession = null;
    HostKey hostKey    = null;
    try {
      JSch jsch = new JSch();
      sshSession = jsch.getSession(null, host, port);
      sshSession.connect();
      hostKey = sshSession.getHostKey();
    } catch (JSchException e) {
      if (sshSession != null) {
        hostKey = sshSession.getHostKey();
      } else {
        throw new ApiException("Could not get host key from server " + host + ":" + port + "!", e);
      }
    }

    if (hostKey == null) {
      throw new ApiException("Could not get host key from server " + host + ":" + port + "!");
    }
    try {
      return new SshKnownHostKey(host.toLowerCase() + ":" + port, hostKey.getKey(), true);
    } catch (InvalidKeyException e) {
      throw new ApiException(e);
    }
  }

  public static SshKnownHostKey find(String host, int port) throws ApiException {
    return find(host, port, false, false, false);
  }

  public static SshKnownHostKey find(String host, int port, boolean fetchMissing, boolean isExternal, boolean create) throws ApiException {
    if (hostkeys.isEmpty()) {
      for (SshKnownHostKey key : findAll()) {
        if (key.getKeyName().indexOf(':') > 1) {
          hostkeys.put(key.getKeyName().toLowerCase(), key);
        }
      }
    }
    port = port > 0 ? port : 22;
    String keyname = host.toLowerCase() + ":" + port;
    if (hostkeys.containsKey(keyname)) {
      return hostkeys.get(keyname);
    }

    SshKnownHostKey hostkey = null;
    try {
      Inet4Address ia   = (Inet4Address) Inet4Address.getByName(host);
      String       name = ia.getHostName().toLowerCase();
      String       ip   = ia.getHostAddress();
      hostkey = hostkeys.get(name + ":" + port);
      if (hostkey == null) {
        hostkey = hostkeys.get(ip + ":" + port);
      }
      if (hostkey == null && fetchMissing) {
        if (isExternal) {
          LOGGER.log(Level.INFO, "SSH host key for host {0}:{1} not found. Try to grab with grabber service.", new Object[] { host, port });
          hostkey = grab(host, port);
        } else {
          LOGGER.log(Level.INFO, "SSH host key for host {0}:{1} not found. Try to grab with SSH.", new Object[] { host, port });
          hostkey = grabWithSsh(host, port);
        }
        if (hostkey != null) {
          LOGGER.log(Level.INFO, "Successfully grabbed key from {0}:{1}: {2}", new Object[] { host, port, hostkey.getSshKey().getKeyDigestInfo("SHA-256") });
          if (create && hostkey.create()) {
            LOGGER.log(Level.INFO, "Added key {0}", hostkey.getKeyName());
          }
        }
      }
    } catch (UnknownHostException uke) {
      throw new ApiException(uke);
    }
    return hostkey;
  }

  public static boolean exists(SshKnownHostKey key) throws ApiException {
    return exists(key.getId());
  }

  public static boolean exists(String keyName) throws ApiException {
    JSONObject json = findByKey(SVC_NAME, keyName);
    return json.has(ID_PROPERTY);
  }

}
