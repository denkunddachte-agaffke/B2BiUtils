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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.denkunddachte.exception.ApiException;

public class SterlingConnectDirectNetmapXref extends ApiClient {
  protected static final String SVC_NAME    = "sterlingconnectdirectnetmapxrefs";
  private static final String   ID_PROPERTY = "netMapName";

  private String                netMapName;
  private String                netMapDescription;
  private String                nodeName;
  private final Set<String>     nodes       = new HashSet<>();

  public SterlingConnectDirectNetmapXref() {
    super();
  }

  public SterlingConnectDirectNetmapXref(String netMapName, Set<String> nodes) {
    super();
    this.netMapName = netMapName;
    this.nodes.addAll(nodes);
  }

  private SterlingConnectDirectNetmapXref(JSONObject json) throws JSONException {
    super();
    this.readJSON(json);
  }

  @Override
  public String getServiceName() {
    return SVC_NAME;
  }

  @Override
  protected SterlingConnectDirectNetmapXref readJSON(JSONObject json) throws JSONException {
    super.init(json);
    this.netMapName = json.getString("netMapName");
    this.netMapDescription = json.getString("netMapDescription");
    this.nodeName = json.getString("nodeName");
    return this;
  }

  @Override
  public JSONObject toJSON() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("netMapName", netMapName);
    JSONArray a = new JSONArray();
    for (String s : nodes) {
      a.put((new JSONObject()).put("nodeName", s));
    }
    json.put("nodes", a);
    return json;
  }

  @Override
  public String getIdProperty() {
    return ID_PROPERTY;
  }

  public String getNetMapName() {
    return netMapName;
  }

  public String getNodeName() {
    return nodeName;
  }

  public String getNetMapDescription() {
    return netMapDescription;
  }

  public Set<String> getNodes() {
    return this.nodes;
  }

  public boolean addNode(String nodeName) {
    return this.nodes.add(nodeName);
  }

  public void setNode(Set<String> nodeNames) {
    clearNodes();
    this.nodes.addAll(nodeNames);
  }

  public void clearNodes() {
    this.nodes.clear();
  }

  @Override
  public String getId() {
    return getGeneratedId() == null ? netMapName : getGeneratedId();
  }

  @Override
  public String toString() {
    return "SterlingConnectDirectNetmapXref [netMapName=" + netMapName + ", netMapDescription=" + netMapDescription + ", nodes=" + nodes + "]";
  }

  @Override
  public boolean update() throws ApiException {
    throw new ApiException("Update is not a supported operation for the SterlingConnectDirectNetmapXref API!");
  }

  // static lookup methods:
  public static List<SterlingConnectDirectNetmapXref> findAll() throws ApiException {
    return find(null);
  }

  public static List<SterlingConnectDirectNetmapXref> find(String netmapName, String... includeFields) throws ApiException {
    List<SterlingConnectDirectNetmapXref> result = new ArrayList<>();
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("offset", 0);
      params.put("includeFields", includeFields);
      if ( netmapName != null )
        params.put("searchFor", netmapName);
      while (true) {
        JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
        for (int i = 0; i < jsonObjects.length(); i++) {
          result.add(new SterlingConnectDirectNetmapXref(jsonObjects.getJSONObject(i)));
        }
        if (jsonObjects.length() < API_RANGESIZE)
          break;
      }
    } catch (JSONException | UnsupportedEncodingException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public static boolean exists(SterlingConnectDirectNetmapXref netMapXref) throws ApiException {
    return findByKey(SVC_NAME, netMapXref.getId()) != null;
  }
}
