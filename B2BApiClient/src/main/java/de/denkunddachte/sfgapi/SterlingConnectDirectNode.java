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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.denkunddachte.enums.CipherSuite;
import de.denkunddachte.enums.TlsVersion;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.ft.CDNode;

public class SterlingConnectDirectNode extends ApiClient {
  private final static Logger                     LOGGER                                   = Logger.getLogger(SterlingConnectDirectNode.class.getName());
  protected static final String                   SVC_NAME                                 = "sterlingconnectdirectnodes";
  private static final String                     ID_PROPERTY                              = "serverNodeName";
  private String                                  serverNodeName;
  private String                                  serverHost;
  private int                                     serverPort                               = 1364;
  private boolean                                 securePlusOptionEnabled                  = false;
  private int                                     maxLocallyInitiatedPnodeSessionsAllowed  = 0;
  private int                                     maxRemotelyInitiatedSnodeSessionsAllowed = 0;
  private String                                  alternateCommInfo;
  private TlsVersion                              securityProtocol;
  private final Map<String, CADigitalCertificate> caCertificates                           = new LinkedHashMap<>();
  private String                                  systemCertificateName;
  private SystemDigitalCertificate                systemCertificate;
  private String                                  certificateCommonName;
  private final Set<CipherSuite>                  cipherSuites                             = new LinkedHashSet<>();
  private boolean                                 requireClientAuthentication              = false;

  public SterlingConnectDirectNode() {
    super();
  }

  public SterlingConnectDirectNode(String serverNodeName, String serverHost, int serverPort, boolean securePlusOptionEnabled) {
    super();
    this.serverNodeName = serverNodeName;
    this.serverHost = serverHost;
    this.serverPort = serverPort;
    this.securePlusOptionEnabled = securePlusOptionEnabled;
  }

  private SterlingConnectDirectNode(JSONObject json) throws JSONException {
    super();
    this.readJSON(json);
  }

  public SterlingConnectDirectNode(CDNode node) {
    this();
    this.serverNodeName = node.getNodeName();
    this.serverHost = node.getTcpAddress();
    this.serverPort = node.getPort();
    this.alternateCommInfo = node.getAltCommInfo();
    this.setMaxLocallyInitiatedPnodeSessionsAllowed(node.getMaxPnodeSessions());
    this.setMaxRemotelyInitiatedSnodeSessionsAllowed(node.getMaxSnodeSessions());
    if (node.isSecurePlus()) {
      this.securePlusOptionEnabled = true;
      this.securityProtocol = node.getSecurityProtocol();
      this.systemCertificateName = node.getSystemCertificateName();
      this.caCertificates.putAll(node.getCaCertificates());
      this.cipherSuites.addAll(node.getCipherSuites());
    } else {
      this.securePlusOptionEnabled = false;
    }
  }

  @Override
  public String getServiceName() {
    return SVC_NAME;
  }

  @Override
  protected SterlingConnectDirectNode readJSON(JSONObject json) throws JSONException {
    super.init(json);
    this.serverNodeName = json.getString(ID_PROPERTY);
    this.serverHost = json.getString("serverHost");
    this.serverPort = json.getInt("serverPort");

    this.maxLocallyInitiatedPnodeSessionsAllowed = json.optInt("maxLocallyInitiatedPnodeSessionsAllowed");
    this.maxRemotelyInitiatedSnodeSessionsAllowed = json.optInt("maxRemotelyInitiatedSnodeSessionsAllowed");
    this.alternateCommInfo = json.optString("alternateCommInfo");

    this.securePlusOptionEnabled = getStringCode(json, "securePlusOption").equalsIgnoreCase("ENABLED");
    if (securePlusOptionEnabled) {
      if (json.has("securityProtocol")) {
        this.securityProtocol = TlsVersion.getByCode(getIntCode(json, "securityProtocol"));
      } else {
        // API Bug: GET does not return securityProtocol, so assume TLS 1.2
        this.securityProtocol = TlsVersion.TLS_V12;
      }
      if (json.has("caCertificates")) {
        JSONArray a = json.getJSONArray("caCertificates");
        for (int i = 0; i < a.length(); i++) {
          caCertificates.put(a.getJSONObject(i).getString("caCertName"), null);
        }
      }
      this.systemCertificateName = json.optString("systemCertificate");
      this.certificateCommonName = json.optString("certificateCommonName");
      if (json.has("cipherSuites")) {
        JSONArray a = json.getJSONArray("cipherSuites");
        for (int i = 0; i < a.length(); i++) {
          cipherSuites.add(CipherSuite.byCode(a.getJSONObject(i).getString("cipherSuiteName")));
        }
      }
      if (json.has("requireClientAuthentication")) {
        this.requireClientAuthentication = getStringCode(json, "requireClientAuthentication").equalsIgnoreCase("YES");
      }
    }
    return this;
  }

  @Override
  public JSONObject toJSON() throws JSONException {
    JSONObject json = new JSONObject();
    json.put(ID_PROPERTY, serverNodeName);
    json.put("serverHost", serverHost);
    json.put("serverPort", serverPort);
    json.put("alternateCommInfo", alternateCommInfo);
    if (maxLocallyInitiatedPnodeSessionsAllowed > 0) {
      json.put("maxLocallyInitiatedPnodeSessionsAllowed", maxLocallyInitiatedPnodeSessionsAllowed);
    }
    if (maxRemotelyInitiatedSnodeSessionsAllowed > 0) {
      json.put("maxRemotelyInitiatedSnodeSessionsAllowed", maxRemotelyInitiatedSnodeSessionsAllowed);
    }

    if (securePlusOptionEnabled) {
      json.put("securePlusOption", "ENABLED");
      json.put("securityProtocol", securityProtocol.getCode());
      if (!caCertificates.isEmpty()) {
        JSONArray a = new JSONArray();
        for (String s : caCertificates.keySet()) {
          a.put((new JSONObject()).put("caCertName", s));
        }
        json.put("caCertificates", a);
      }
      json.put("certificateCommonName", certificateCommonName);
      json.put("systemCertificate", systemCertificateName);
      if (!cipherSuites.isEmpty()) {
        JSONArray a = new JSONArray();
        for (CipherSuite s : cipherSuites) {
          a.put((new JSONObject()).put("cipherSuiteName", s.b2biCode()));
        }
        json.put("cipherSuites", a);
      }
      json.put("requireClientAuthentication", (requireClientAuthentication ? "YES" : "NO"));
    } else {
      json.put("securePlusOption", "DISABLED");
    }
    return json;
  }

  @Override
  public String getIdProperty() {
    return ID_PROPERTY;
  }

  public String getServerNodeName() {
    return serverNodeName;
  }

  public String getServerHost() {
    return serverHost;
  }

  public void setServerHost(String serverHost) {
    this.serverHost = serverHost;
  }

  public int getServerPort() {
    return serverPort;
  }

  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
  }

  public boolean isSecurePlusOptionEnabled() {
    return securePlusOptionEnabled;
  }

  public void setSecurePlusOptionEnabled(boolean securePlusOptionEnabled) {
    this.securePlusOptionEnabled = securePlusOptionEnabled;
  }

  public int getMaxLocallyInitiatedPnodeSessionsAllowed() {
    return maxLocallyInitiatedPnodeSessionsAllowed;
  }

  public void setMaxLocallyInitiatedPnodeSessionsAllowed(int maxLocallyInitiatedPnodeSessionsAllowed) {
    this.maxLocallyInitiatedPnodeSessionsAllowed = maxLocallyInitiatedPnodeSessionsAllowed;
  }

  public int getMaxRemotelyInitiatedSnodeSessionsAllowed() {
    return maxRemotelyInitiatedSnodeSessionsAllowed;
  }

  public void setMaxRemotelyInitiatedSnodeSessionsAllowed(int maxRemotelyInitiatedSnodeSessionsAllowed) {
    this.maxRemotelyInitiatedSnodeSessionsAllowed = maxRemotelyInitiatedSnodeSessionsAllowed;
  }

  public String getAlternateCommInfo() {
    return alternateCommInfo;
  }

  public void setAlternateCommInfo(String alternateCommInfo) {
    this.alternateCommInfo = alternateCommInfo;
  }

  public TlsVersion getSecurityProtocol() {
    return securityProtocol;
  }

  public void setSecurityProtocol(TlsVersion securityProtocol) {
    this.securityProtocol = securityProtocol;
  }

  public Collection<String> getCaCertificateNames() {
    return caCertificates.keySet();
  }

  public Collection<CADigitalCertificate> getCaCertificates() throws ApiException {
    Collection<CADigitalCertificate> result = new ArrayList<>(caCertificates.size());
    Iterator<String> iter = caCertificates.keySet().iterator();
    while (iter.hasNext()) {
      String certName = iter.next();
      if (caCertificates.get(certName) == null) {
        caCertificates.put(certName, CADigitalCertificate.find(certName));
        if (caCertificates.get(certName) == null) {
          LOGGER.log(Level.WARNING, "CADigitalCertificate {0} not found but assigned to CDNode {1}!", new Object[] { certName, getId() });
          caCertificates.remove(certName);
          continue;
        }
      }
      result.add(caCertificates.get(certName));
    }
    return result;
  }

  public void setCaCertificates(Set<CADigitalCertificate> caCertificates) {
    this.caCertificates.clear();
    for (CADigitalCertificate cert : caCertificates) {
      addCaCertificate(cert);
    }
  }

  public void addCaCertificate(CADigitalCertificate caCertificate) {
    caCertificates.put(caCertificate.getCertName(), caCertificate);
  }

  public String getSystemCertificateName() {
    return this.systemCertificateName;
  }

  public SystemDigitalCertificate getSystemCertificate() throws ApiException {
    if (systemCertificate == null && systemCertificateName != null) {
      systemCertificate = SystemDigitalCertificate.find(systemCertificateName);
      if (systemCertificate == null) {
        LOGGER.log(Level.WARNING, "SystemDigitalCertificate {0} not found but assigned to CDNode {1}!", new Object[] { systemCertificateName, getId() });
        systemCertificateName = null;
      }
    }
    return systemCertificate;
  }

  public void setSystemCertificate(SystemDigitalCertificate systemCertificate) {
    this.systemCertificate = systemCertificate;
  }

  public String getCertificateCommonName() {
    return certificateCommonName;
  }

  public void setCertificateCommonName(String certificateCommonName) {
    this.certificateCommonName = certificateCommonName;
  }

  public Collection<CipherSuite> getCipherSuites() {
    return new ArrayList<>(cipherSuites);
  }

  public void setCipherSuites(Set<CipherSuite> cipherSuites) {
    this.cipherSuites.clear();
    for (CipherSuite suite : cipherSuites) {
      addCipherSuite(suite);
    }
  }

  public boolean addCipherSuite(CipherSuite cipherSuite) {
    return cipherSuites.add(cipherSuite);
  }

  public boolean isRequireClientAuthentication() {
    return requireClientAuthentication;
  }

  public void setRequireClientAuthentication(boolean requireClientAuthentication) {
    this.requireClientAuthentication = requireClientAuthentication;
  }

  @Override
  public String getId() {
    return getGeneratedId() == null ? serverNodeName : getGeneratedId();
  }

  @Override
  public String toString() {
    return "SterlingConnectDirectNode [serverNodeName=" + serverNodeName + ", serverHost=" + serverHost + ", serverPort=" + serverPort
        + ", securePlusOptionEnabled=" + securePlusOptionEnabled + ", maxLocallyInitiatedPnodeSessionsAllowed=" + maxLocallyInitiatedPnodeSessionsAllowed
        + ", maxRemotelyInitiatedSnodeSessionsAllowed=" + maxRemotelyInitiatedSnodeSessionsAllowed + ", alternateCommInfo=" + alternateCommInfo
        + ", securityProtocol=" + securityProtocol + ", caCertificates=" + caCertificates + ", systemCertificateName=" + systemCertificateName
        + ", certificateCommonName=" + certificateCommonName + ", cipherSuites=" + cipherSuites + ", requireClientAuthentication=" + requireClientAuthentication
        + "]";
  }

  // static lookup methods:
  public static List<SterlingConnectDirectNode> findAll() throws ApiException {
    return findAll(null, null);
  }

  public static List<SterlingConnectDirectNode> findAll(String nodeName, String netmapName, String... includeFields) throws ApiException {
    List<SterlingConnectDirectNode> result = new ArrayList<>();
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("offset", 0);
      params.put("includeFields", includeFields);
      if (nodeName != null)
        params.put("searchByNodeName", nodeName.replace('*', '%'));
      if (netmapName != null)
        params.put("searchByNetMap", netmapName.replace('*', '%'));
      while (true) {
        JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
        for (int i = 0; i < jsonObjects.length(); i++) {
          result.add(new SterlingConnectDirectNode(jsonObjects.getJSONObject(i)));
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
  public static SterlingConnectDirectNode find(String nodeName) throws ApiException {
    SterlingConnectDirectNode result = null;
    JSONObject json = findByKey(SVC_NAME, nodeName);
    try {
      if (json.has(ERROR_CODE)) {
        LOGGER.log(Level.FINER, "SterlingConnectDirectNode {0} not found: errorCode={1}, errorDescription={2}.",
            new Object[] { nodeName, json.getInt(ERROR_CODE), json.get(ERROR_DESCRIPTION) });
      } else {
        result = new SterlingConnectDirectNode(json);
        LOGGER.log(Level.FINER, "Found SterlingConnectDirectNode {0}: {1}", new Object[] { nodeName, result });
      }
    } catch (JSONException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public static boolean exists(SterlingConnectDirectNode node) throws ApiException {
    return exists(node.getId());
  }

  public static boolean exists(String nodeName) throws ApiException {
    JSONObject json = findByKey(SVC_NAME, nodeName);
    return json.has(ID_PROPERTY);
  }
}
