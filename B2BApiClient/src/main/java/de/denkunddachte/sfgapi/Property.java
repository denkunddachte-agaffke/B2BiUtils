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

import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.denkunddachte.exception.ApiException;

public class Property extends ApiClient {
  private final static Logger   LOGGER              = Logger.getLogger(Property.class.getName());
  protected static final String PROPERTY_NODE_VALUE = "propertyNodeValue";
  protected static final String SYSTEM_DEFINED      = "systemDefined";
  protected static final String PROPERTY_ID         = "propertyId";
  protected static final String LAST_UPDATED_ON     = "lastUpdatedOn";
  protected static final String LAST_UPDATED_BY     = "lastUpdatedBy";
  protected static final String CREATED_ON          = "createdOn";
  protected static final String CREATED_BY          = "createdBy";
  protected static final String PROPERTY_FILE       = "propertyFile";
  protected static final String PROPERTY_KEY        = "propertyKey";
  protected static final String PROPERTY_VALUE      = "propertyValue";

  protected static final String SVC_NAME            = "properties";
  protected static final String ID_PROPERTY         = PROPERTY_ID;
  private int                   propertyId;

  private String                createdBy;
  private Date                  createdOn;
  private String                lastUpdatedBy;
  private Date                  lastUpdatedOn;
  private String                propertyKey;
  private String                propertyValue;
  private PropertyFiles         propertyFile;
  private boolean               systemDefined;
  private int                   propertyFileId;
  private boolean               doUpdate            = false;
  private String                propertyNodeValueURL;

  List<PropertyNodeValue>       nodeValues;

  protected Property() {
    super();
  }

  public Property(PropertyFiles propertyFile, String propertyKey, String propertyValue) {
    super();
    this.propertyFile = propertyFile;
    this.propertyFileId = propertyFile.getPropertyFileId();
    this.propertyKey = propertyKey;
    this.propertyValue = propertyValue;
    setPropertyNodeValueURL("/propertyfiles/" + propertyFile.getPropertyFilePrefix() + "/property/" + propertyKey + "/propertyNodeValue/");
  }

  protected Property(JSONObject json) throws JSONException {
    super();
    this.readJSON(json);
  }

  @Override
  public String getId() {
    return getGeneratedId() == null ? Integer.toString(propertyId) : getGeneratedId();
  }

  @Override
  public String getIdProperty() {
    return ID_PROPERTY;
  }

  @Override
  public String getServiceName() {
    return SVC_NAME;
  }

  @Override
  public boolean update() throws ApiException {
    doUpdate = true;
    for (PropertyNodeValue pnv : getNodeValues()) {
      if (pnv.isNew() && !pnv.create()) {
        throw new ApiException("Could not create node value for [" + getPropertyFile().getPropertyFilePrefix() + "]" + propertyKey + " (" + pnv.getNodeName()
            + "): " + ApiClient.getApiErrorMsg());
      }
    }
    return super.update();
  }

  @Override
  public boolean create() throws ApiException {
    doUpdate = false;
    boolean result = super.create();
    if (result) {
      Property p = Property.find(getPropertyFile().getPropertyFilePrefix(), this.propertyKey);
      if (p == null) {
        throw new ApiException(
            "Could not refresh new property [" + getPropertyFile().getPropertyFilePrefix() + "]" + propertyKey + ": " + ApiClient.getApiErrorMsg());
      }
      this.readJSON(p.getOrigJSON());
      for (PropertyNodeValue pnv : getNodeValues()) {
        pnv.setProperty(this);
        if (!pnv.create()) {
          throw new ApiException("Could not create node value for [" + getPropertyFile().getPropertyFilePrefix() + "]" + propertyKey + " (" + pnv.getNodeName()
              + "): " + ApiClient.getApiErrorMsg());
        }
      }
    }
    return result;
  }

  @Override
  public void refresh() throws ApiException {
    super.refresh();
    this.nodeValues = null;
  }

  @Override
  public JSONObject toJSON() throws JSONException {
    JSONObject json = new JSONObject();
    if (!doUpdate) {
      json.put(PROPERTY_KEY, propertyKey);
      json.put(PROPERTY_FILE, propertyFileId);
    }
    json.put(PROPERTY_VALUE, propertyValue == null || propertyValue.isEmpty() ? null : propertyValue);

    try {
      JSONArray pnvs = new JSONArray();
      for (PropertyNodeValue pnv : getNodeValues()) {
        pnvs.put(pnv.toJSON(true));
      }
      if (!pnvs.isEmpty()) {
        json.put(PROPERTY_NODE_VALUE, pnvs);
      }
    } catch (ApiException e) {
      LOGGER.log(Level.SEVERE, "Error getting PropertyNodeValues!", e);
    }
    return json;
  }

  @Override
  protected Property readJSON(JSONObject json) throws JSONException {
    super.init(json);
    this.propertyFileId = json.getInt(PropertyFiles.PROPERTY_FILE_ID);
    this.createdBy = json.getString(CREATED_BY);
    this.createdOn = getDate(json.getString(CREATED_ON));
    if (json.has(LAST_UPDATED_BY)) {
      this.lastUpdatedBy = json.getString(LAST_UPDATED_BY);
    }
    if (json.has(LAST_UPDATED_ON)) {
      this.lastUpdatedOn = getDate(json.getString(LAST_UPDATED_ON));
    }
    this.propertyId = json.getInt(PROPERTY_ID);
    this.propertyKey = json.getString(PROPERTY_KEY);
    if (json.has(PROPERTY_VALUE)) {
      this.propertyValue = json.getString(PROPERTY_VALUE);
    }
    this.systemDefined = "Y".equalsIgnoreCase(json.getJSONObject(SYSTEM_DEFINED).getString(CODE));
    if (json.has(PROPERTY_NODE_VALUE)) {
      setPropertyNodeValueURL(json.getJSONObject(PROPERTY_NODE_VALUE).getString(HREF));
    }
    return this;
  }

  private void setPropertyNodeValueURL(String url) {
    if (url == null) {
      return;
    }
    if (url.startsWith("http")) {
      this.propertyNodeValueURL = url.substring(apicfg.getApiBaseURI().length());
    } else {
      this.propertyNodeValueURL = url;
    }
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public Date getCreatedOn() {
    return createdOn;
  }

  public String getLastUpdatedBy() {
    return lastUpdatedBy;
  }

  public Date getLastUpdatedOn() {
    return lastUpdatedOn;
  }

  public boolean isSystemDefined() {
    return systemDefined;
  }

  private List<PropertyNodeValue> nodeValues() throws ApiException {
    if (nodeValues == null) {
      nodeValues = new ArrayList<>();
      if (propertyNodeValueURL != null) {
        try {
          JSONArray jsonObjects = getJSONArray(get(propertyNodeValueURL));
          for (int i = 0; i < jsonObjects.length(); i++) {
            PropertyNodeValue pnv  = new PropertyNodeValue(jsonObjects.getJSONObject(i));
            int               node = Integer.parseInt(pnv.getNodeName().substring(4));
            putPropertyNodeValue(node, pnv);
          }
        } catch (ApiException ae) {
          // B2B REST API delivers api urls with unencoded brackets which cause a URISyntaxException. On the other hand, REST API does not accept
          // encoded brackets (%5B, %5D)... so we'll issue a warning for now :-(
          if (ae.getCause() instanceof URISyntaxException) {
            LOGGER.log(Level.WARNING, "Could not get PropertyNodeValue for property " + this.propertyKey + ": " + ae.getMessage());
          } else {
            throw ae;
          }
        } catch (JSONException e) {
          throw new ApiException(e);
        }
      }
    }
    return nodeValues;
  }

  public Collection<PropertyNodeValue> getNodeValues() throws ApiException {
    return nodeValues().stream().filter(Objects::nonNull).collect(Collectors.toList());
  }

  public String getPropertyValue() {
    return propertyValue;
  }

  public void setPropertyValue(String propertyValue) {
    this.propertyValue = propertyValue;
  }

  private PropertyNodeValue getPropertyNodeValue(int node) throws ApiException {
    PropertyNodeValue pnv = null;
    if (node <= nodeValues().size()) {
      pnv = nodeValues().get(node - 1);
    }
    return pnv;
  }

  private void putPropertyNodeValue(int node, PropertyNodeValue pnv) throws ApiException {
    while (nodeValues().size() < node)
      nodeValues.add(null);
    nodeValues.set(node - 1, pnv);
  }

  public String getNodeValue(int node) throws ApiException {
    PropertyNodeValue pnv = getPropertyNodeValue(node);
    return (pnv == null ? null : pnv.getPropertyValue());
  }

  public void setNodeValue(int node, String propertyValue) throws ApiException {
    PropertyNodeValue pnv = getPropertyNodeValue(node);
    if (pnv == null) {
      putPropertyNodeValue(node, new PropertyNodeValue(this, node, propertyValue));
    } else {
      pnv.setPropertyValue(propertyValue);
    }
  }

  public boolean hasNodeValue(int node) throws ApiException {
    return getPropertyNodeValue(node) != null;
  }

  public void deleteNodeValue(int node) throws ApiException {
    PropertyNodeValue pnv = getPropertyNodeValue(node);
    if (pnv == null) {
      throw new ApiException("No such node value: node" + node + "!");
    }
    nodeValues.set(node, null);
  }

  public int getPropertyId() {
    return propertyId;
  }

  public String getPropertyKey() {
    return propertyKey;
  }

  public int getPropertyFileId() {
    return propertyFileId;
  }

  public PropertyFiles getPropertyFile() throws ApiException {
    if (propertyFile == null) {
      propertyFile = PropertyFiles.find(propertyFileId);
    }
    return propertyFile;
  }

  @Override
  public void export(PrintWriter out, boolean prettyPrint, boolean suppressNullValues) throws ApiException {
    out.format("%s=%s%n", propertyKey, (propertyValue == null ? "" : propertyValue.replace("\r","").replaceAll("\n", "\\\\n\\\\\n")));
    for (PropertyNodeValue pnv:getNodeValues()) {
      out.format("%s[%s]=%s%n", propertyKey, pnv.getNodeName(),  (pnv.getPropertyValue() == null ? "" : pnv.getPropertyValue().replace("\r","").replaceAll("\n", "\\\\n\\\\\n")));
    }
  }
  
  @Override
  public Mode getExportMode() {
    return Mode.PROPERTIES;
  }

  @Override
  public String toString() {
    return "Property [propertyKey=" + propertyKey + ", propertyValue=" + propertyValue + ", propertyId=" + propertyId + ", propertyFileId=" + propertyFileId
        + ", createdBy=" + createdBy + ", createdOn=" + createdOn + ", lastUpdatedBy=" + lastUpdatedBy + ", lastUpdatedOn=" + lastUpdatedOn + "]";
  }

  public static Property find(String propertyFilePrefix, String propertyKey) throws ApiException {
    Property result = null;
    try {
      JSONObject json = getJSON(get(PropertyFiles.SVC_NAME, propertyFilePrefix + "/property/?propertyKey=" + propertyKey));
      if (json == null || json.optInt(ERROR_CODE) == 404) {
        LOGGER.log(Level.FINE, "Property {0}/{1} not found.", new Object[] { propertyFilePrefix, propertyKey });
      } else if (json.has(ERROR_CODE)) {
        throw new ApiException(
            "Error getting property " + propertyFilePrefix + "/" + propertyKey + ": " + json.getInt(ERROR_CODE) + "/" + json.get(ERROR_DESCRIPTION) + "!");
      } else {
        result = new Property(json);
        LOGGER.log(Level.FINER, "Found property {0}/{1}: {2}", new Object[] { propertyFilePrefix, propertyKey, result });
      }
    } catch (JSONException e) {
      throw new ApiException(e);
    }
    return result;
  }
}
