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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.denkunddachte.enums.CipherSuite;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.ft.CDNetMap;
import de.denkunddachte.ft.CDNode;

public class SterlingConnectDirectNetmap extends ApiClient implements CDNetMap {
  private final static Logger      LOGGER      = Logger.getLogger(SterlingConnectDirectNetmap.class.getName());
  protected static final String    SVC_NAME    = "sterlingconnectdirectnetmaps";
  private static final String      ID_PROPERTY = "name";

  private String                   name;
  private String                   description;
  private final Map<String, Entry> sfgNodes    = new HashMap<>(200);
  private final Map<String, Entry> delNodes    = new HashMap<>(200);

  public SterlingConnectDirectNetmap() {
    super();
  }

  public SterlingConnectDirectNetmap(String name, String description) {
    super();
    this.name = name;
    this.description = description;
  }

  private SterlingConnectDirectNetmap(JSONObject json) throws JSONException {
    super();
    this.readJSON(json);
  }

  @Override
  public boolean isSSPNetmap() {
    return false;
  }

  @Override
  public String getServiceName() {
    return SVC_NAME;
  }

  @Override
  protected SterlingConnectDirectNetmap readJSON(JSONObject json) throws JSONException {
    super.init(json);
    this.name = json.getString("name");
    this.description = json.getString("description");
    return this;
  }

  @Override
  public JSONObject toJSON() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("name", name);
    json.put("description", description);
    return json;
  }

  @Override
  public String getIdProperty() {
    return ID_PROPERTY;
  }

  @Override
  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String getId() {
    return getGeneratedId() == null ? name : getGeneratedId();
  }

  @Override
  public String toString() {
    return "SterlingConnectDirectNetmap [name=" + name + ", description=" + description + "]";
  }

  private Map<String, Entry> getNodemap() throws ApiException {
    if (this.sfgNodes.isEmpty()) {
      for (SterlingConnectDirectNetmapXref xref : SterlingConnectDirectNetmapXref.find(name)) {
        this.sfgNodes.put(xref.getNodeName(), new Entry(xref, null));
      }
    }
    return this.sfgNodes;
  }

  @Override
  public Set<String> getNodeNames() throws ApiException {
    Set<String> result = new HashSet<>(getNodemap().size());
    result.addAll(getNodemap().keySet());
    return result;
  }

  @Override
  public boolean hasNode(String nodeName) throws ApiException {
    return getNodemap().containsKey(nodeName);
  }

  @Override
  public boolean hasNode(CDNode node) throws ApiException {
    return getNodemap().containsKey(node.getNodeName());
  }

  public Entry addNode(String nodeName) throws ApiException {
    return getNodemap().put(nodeName, null);
  }

  public Entry addNode(SterlingConnectDirectNode node) throws ApiException {
    return getNodemap().put(node.getServerNodeName(), new Entry(null, node));
  }

  public Entry addNode(SterlingConnectDirectNetmapXref xref) throws ApiException {
    return getNodemap().put(xref.getNodeName(), new Entry(xref, null));
  }

  @Override
  public CDNode getCDNode(String nodeName) throws ApiException {
    CDNode cdnd = null;
    SterlingConnectDirectNode nd = getNode(nodeName);
    if (nd != null) {
      cdnd = new CDNode(nd.getServerNodeName(), nd.getServerHost(), nd.getServerPort(), nd.isSecurePlusOptionEnabled());
      cdnd.setMaxPnodeSessions(nd.getMaxLocallyInitiatedPnodeSessionsAllowed());
      cdnd.setMaxSnodeSessions(nd.getMaxRemotelyInitiatedSnodeSessionsAllowed());
      cdnd.setAltCommInfo(nd.getAlternateCommInfo());
      if (nd.isSecurePlusOptionEnabled()) {
        cdnd.setSecurityProtocol(nd.getSecurityProtocol());
        cdnd.setCertificateCommonName(nd.getCertificateCommonName());
        cdnd.setRequireClientAuthentication(nd.isRequireClientAuthentication());
        cdnd.setSystemCertificateName(nd.getSystemCertificateName());
        for (String caCert : nd.getCaCertificateNames()) {
          cdnd.addCaCertificate(caCert, null);
        }
        for (CipherSuite cs : nd.getCipherSuites()) {
          cdnd.addCipherSuite(cs);
        }
      }
    }
    return cdnd;
  }

  public SterlingConnectDirectNode getNode(String nodeName) throws ApiException {
    SterlingConnectDirectNode node = null;
    if (getNodemap().containsKey(nodeName)) {
      node = getNodemap().get(nodeName).getNode();
      if (node == null) {
        node = SterlingConnectDirectNode.find(nodeName);
        getNodemap().get(nodeName).setNode(node);
      }
      return node;
    } else {
      return null;
    }
  }

  public SterlingConnectDirectNetmapXref getNodeXref(String nodeName) throws ApiException {
    if (getNodemap().containsKey(nodeName)) {
      return getNodemap().get(nodeName).getXref();
    } else {
      return null;
    }
  }

  @Override
  public boolean addNewNode(String nodeName, String serverAddress, int port, boolean securePlusEnabled) throws ApiException {
    SterlingConnectDirectNode node = SterlingConnectDirectNode.find(nodeName);
    if (node == null) {
      node = new SterlingConnectDirectNode(nodeName, serverAddress, port, securePlusEnabled);
      if (!node.create()) {
        throw new ApiException("Could not create C:D node " + nodeName + " (" + serverAddress + ":" + port + ", sec+=" + securePlusEnabled + ")");
      }
    }
    addNode(node);
    return true;
  }

  @Override
  public boolean addNewNode(CDNode node) throws ApiException {
    if (hasNode(node.getNodeName())) {
      throw new ApiException("Node " + node.getNodeName() + " already exists in netmap " + name + "!");
    }

    SterlingConnectDirectNode nd = SterlingConnectDirectNode.find(node.getNodeName());
    if (nd == null) {
      nd = new SterlingConnectDirectNode(node);
      if (!nd.create()) {
        throw new ApiException("Could not create C:D node " + node.getNodeName() + ": " + ApiClient.getApiErrorMsg());
      }
    }
    addNode(nd);
    return true;
  }

  @Override
  public boolean addOrUpdateNode(CDNode node, boolean modifyExisting) throws ApiException {
    final String nodeName = node.getNodeName();
    SterlingConnectDirectNode nd = SterlingConnectDirectNode.find(nodeName);
    SterlingConnectDirectNode newNode = new SterlingConnectDirectNode(node);
    boolean modified = false;
    if (nd != null) {
      modified = !nd.toJSON().similar(newNode.toJSON());
      if (modified && modifyExisting) {
        LOGGER.log(Level.FINE, "Update C:D node {0}: {1} -> {2}", new Object[] { nodeName, nd, newNode });
        if (!newNode.update())
          throw new ApiException("Could not update C:D node " + nodeName + " (" + node + ")");
        nd.refresh();
      }
    } else {
      nd = newNode;
      if (!nd.create()) {
        throw new ApiException("Could not create C:D node " + nodeName + " (" + node + ")");
      }
    }
    if (!hasNode(nodeName)) {
      addNode(nd);
      modified = true;
    }
    return modified;
  }

  @Override
  public boolean removeNode(String nodeName) throws ApiException {
    if (hasNode(nodeName)) {
      delNodes.put(nodeName, sfgNodes.remove(nodeName));
      return true;
    } else {
      throw new ApiException("CD node " + nodeName + " not included in netmap " + name + "!");
    }
  }

  @Override
  public boolean removeNode(CDNode node) throws ApiException {
    return removeNode(node.getNodeName());
  }

  public void removeNode(SterlingConnectDirectNode node) throws ApiException {
    removeNode(node.getServerNodeName());
  }

  @Override
  public boolean create() throws ApiException {
    boolean result = super.create();
    if (result && !sfgNodes.isEmpty()) {
      SterlingConnectDirectNetmapXref xref = new SterlingConnectDirectNetmapXref(name, getNodeNames());
      if (!xref.create()) {
        throw new ApiException("Could not add netmap nodes: " + ApiClient.getApiErrorMsg());
      }
    }
    return result;
  }

  @Override
  public boolean update() throws ApiException {
    boolean result = super.update();

    if (result) {
      for (java.util.Map.Entry<String, Entry> e : delNodes.entrySet()) {
        if (e.getValue().getXref() != null) {
          if (!e.getValue().getXref().delete()) {
            throw new ApiException("Could not delete node " + e.getKey() + " from netmap " + name + ": " + ApiClient.getApiErrorMsg());
          }
        } else {
          LOGGER.log(Level.FINE, "Will not delete node Xref for {0} from netmap {1} because it was not persisted.", new Object[] { e.getKey(), name });
        }
      }
      if (!sfgNodes.isEmpty()) {
        SterlingConnectDirectNetmapXref xref = new SterlingConnectDirectNetmapXref(name, getNodeNames());
        if (!xref.create()) {
          throw new ApiException("Could not update netmap nodes: " + ApiClient.getApiErrorMsg());
        }
      }
    }
    return result;
  }

  // static lookup methods:
  public static List<SterlingConnectDirectNetmap> findAll() throws ApiException {
    return findAll(null);
  }

  public static List<SterlingConnectDirectNetmap> findAll(String globPattern, String... includeFields) throws ApiException {
    List<SterlingConnectDirectNetmap> result = new ArrayList<>();
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("offset", 0);
      params.put("includeFields", includeFields);
      params.put("searchFor", (globPattern != null ? globPattern.replace('*', '%') : null));
      while (true) {
        JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
        for (int i = 0; i < jsonObjects.length(); i++) {
          result.add(new SterlingConnectDirectNetmap(jsonObjects.getJSONObject(i)));
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
  public static SterlingConnectDirectNetmap find(String netMapName) throws ApiException {
    SterlingConnectDirectNetmap result = null;
    JSONObject json = findByKey(SVC_NAME, netMapName);
    try {
      if (json.has(ERROR_CODE)) {
        LOGGER.log(Level.FINER, "SterlingConnectDirectNetmap {0} not found: errorCode={1}, errorDescription={2}.",
            new Object[] { netMapName, json.getInt(ERROR_CODE), json.get(ERROR_DESCRIPTION) });
      } else {
        result = new SterlingConnectDirectNetmap(json);
        LOGGER.log(Level.FINER, "Found SterlingConnectDirectNetmap {0}: {1}", new Object[] { netMapName, result });
      }
    } catch (JSONException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public static boolean exists(SterlingConnectDirectNetmap netmap) throws ApiException {
    return exists(netmap.getId());
  }

  public static boolean exists(String netMapName) throws ApiException {
    JSONObject json = findByKey(SVC_NAME, netMapName);
    return json.has(ID_PROPERTY);
  }

  static class Entry {
    SterlingConnectDirectNetmapXref xref;
    SterlingConnectDirectNode       node;

    public Entry(SterlingConnectDirectNetmapXref xref, SterlingConnectDirectNode node) {
      this.xref = xref;
      this.node = node;
    }

    public SterlingConnectDirectNetmapXref getXref() {
      return xref;
    }

    public SterlingConnectDirectNode getNode() {
      return node;
    }

    public String getNodeName() {
      if (xref != null) {
        return xref.getNodeName();
      } else if (node != null) {
        return node.getServerNodeName();
      }
      return null;
    }

    // private void setXref(SterlingConnectDirectNetmapXref xref) {
    // this.xref = xref;
    // }

    private void setNode(SterlingConnectDirectNode node) {
      this.node = node;
    }
  }
}
