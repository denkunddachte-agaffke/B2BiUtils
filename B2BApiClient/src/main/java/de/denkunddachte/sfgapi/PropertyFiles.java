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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.denkunddachte.exception.ApiException;
import de.denkunddachte.exception.NotImplementedException;

public class PropertyFiles extends ApiClient {
  protected static final String PROPERTY_FILE_ID = "propertyFileId";
  private final static Logger   LOGGER           = Logger.getLogger(PropertyFiles.class.getName());
  protected static final String SVC_NAME         = "propertyfiles";
  protected static final String WFD_WS_API       = "refresh";
  protected static final String ID_PROPERTY      = PROPERTY_FILE_ID;
  private int                   propertyFileId;

  private boolean               componentEditable;
  private String                createdBy;
  private Date                  createdOn;
  private String                lastUpdatedBy;
  private Date                  lastUpdatedOn;
  private String                description;
  private String                propertyFileContent;
  private String                propertyFilePrefix;
  private boolean               replaceExistingPropertySet;
  private boolean               systemDefined;
  private boolean               doUpdate         = false;

  SortedMap<String, Property>   properties;
  Properties                    nodeValues       = null;

  public PropertyFiles() {
    super();
  }

  public PropertyFiles(String propertyFilePrefix) {
    super();
    this.propertyFilePrefix = propertyFilePrefix;
  }

  private PropertyFiles(JSONObject json) throws JSONException {
    super();
    this.readJSON(json);
  }

  public PropertyFiles(String propertyFilePrefix, File propertyFile, String description, boolean replaceExisting) throws ApiException {
    this(propertyFilePrefix);
    this.description = description;
    this.replaceExistingPropertySet = replaceExisting;
    readFile(propertyFile);
  }

  @Override
  public String getId() {
    return getGeneratedId() == null ? Integer.toString(propertyFileId) : getGeneratedId();
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
    boolean rc = super.update();
    addPropertyNodeValues();
    return rc;
  }

  @Override
  public boolean create() throws ApiException {
    doUpdate = false;
    boolean rc = super.create();
    addPropertyNodeValues();
    return rc;
  }

  private void addPropertyNodeValues() throws ApiException {
    if (nodeValues != null) {
      Pattern nodeValuePattern = Pattern.compile("(\\S+)\\[node(\\d+)\\]");
      for (Object k : nodeValues.keySet()) {
        Matcher  m = nodeValuePattern.matcher((String) k);
        Property p = getProperty(m.group(1));
        p.setNodeValue(Integer.parseInt(m.group(2)), nodeValues.getProperty((String) k));
        p.update();
      }
      nodeValues = null;
    }
  }

  @Override
  public JSONObject toJSON() throws JSONException {
    JSONObject json = new JSONObject();
    if (doUpdate) {
      json.put("replaceExistingPropertySet", replaceExistingPropertySet);
    } else {
      json.put("propertyFilePrefix", propertyFilePrefix);
    }
    if (description != null)
      json.put("description", description);
    if (propertyFileContent != null) {
      json.put("propertyFileContent", Base64.getEncoder().encodeToString(propertyFileContent.getBytes()));
    }
    return json;
  }

  @Override
  protected PropertyFiles readJSON(JSONObject json) throws JSONException {
    super.init(json);
    this.propertyFileId = json.getInt(PROPERTY_FILE_ID);
    this.propertyFilePrefix = json.getString("propertyFilePrefix");
    this.createdBy = json.getString("createdBy");
    this.createdOn = getDate(json.getString("createdOn"));
    if (json.has("lastUpdatedBy")) {
      this.lastUpdatedBy = json.getString("lastUpdatedBy");
      this.lastUpdatedOn = getDate(json.getString("lastUpdatedOn"));
    }
    if (json.has("componentEditable"))
      this.componentEditable = "Y".equalsIgnoreCase(json.getJSONObject("componentEditable").getString("code"));
    this.systemDefined = "Y".equalsIgnoreCase(json.getJSONObject("systemDefined").getString(CODE));
    return this;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isReplaceExistingPropertySet() {
    return replaceExistingPropertySet;
  }

  public void setReplaceExistingPropertySet(boolean replaceExistingPropertySet) {
    this.replaceExistingPropertySet = replaceExistingPropertySet;
  }

  public int getPropertyFileId() {
    return propertyFileId;
  }

  public boolean isComponentEditable() {
    return componentEditable;
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

  public String getPropertyFilePrefix() {
    return propertyFilePrefix;
  }

  public boolean isSystemDefined() {
    return systemDefined;
  }

  public Map<String, Property> getProperties() throws ApiException {
    if (properties == null) {
      try {
        JSONArray jsonObjects = getJSONArray(get(SVC_NAME, getPropertyFilePrefix() + "/property/"));
        properties = new TreeMap<>();
        for (int i = 0; i < jsonObjects.length(); i++) {
          Property p = new Property(jsonObjects.getJSONObject(i));
          properties.put(p.getPropertyKey(), p);
        }
      } catch (JSONException e) {
        throw new ApiException(e);
      }
    }
    return Collections.unmodifiableMap(this.properties);
  }

  public Property getProperty(String propertyKey) throws ApiException {
    if (getProperties().get(propertyKey) == null) {
      try {
        JSONObject json = getJSON(get(SVC_NAME, getPropertyFilePrefix() + "/property/" + propertyKey + "/"));
        properties.put(propertyKey, new Property(json));
      } catch (JSONException e) {
        throw new ApiException(e);
      }
    }
    return getProperties().get(propertyKey);
  }

  public Property addProperty(String propertyKey, String value, String... nodeValues) throws ApiException {
    Property p = getProperties().get(propertyKey);
    if (p != null) {
      throw new ApiException("Property [" + this.propertyFilePrefix + "]" + propertyKey + " exists!");
    } else {
      p = new Property(this, propertyKey, value);
      if (p.create()) {
        p = getProperty(propertyKey);
        LOGGER.log(Level.FINER, "Created property {0}/{1} with value {2}", new Object[] { propertyFilePrefix, propertyKey, value });
      } else {
        throw new ApiException("Could not create property [" + propertyFilePrefix + "]" + propertyKey + "!");
      }
    }
    if (nodeValues != null) {
      for (int i = 0; i < nodeValues.length; i++) {
        if (nodeValues[i] != null) {
          p.setNodeValue(i + 1, nodeValues[i]);
        }
      }
    }
    return p;
  }

  public void setPropertyFileContent(String propertyFileContent) {
    this.propertyFileContent = propertyFileContent;
  }

  @Override
  public void export(PrintWriter out, boolean prettyPrint, boolean suppressNullValues) {
    throw new NotImplementedException("TODO: implemenent proprties export");
  }

  @Override
  public Mode getExportMode() {
    return Mode.PROPERTIES;
  }

  private void readFile(File file) throws ApiException {
    Properties props = new Properties();
    nodeValues = new Properties();
    try (InputStream is = new FileInputStream(file)) {
      props.load(is);
    } catch (IOException e) {
      throw new ApiException(e);
    }
    if (props.isEmpty()) {
      throw new ApiException("Properties file is empty!");
    }

    final List<String> keys = new ArrayList<>(props.size());
    for (Object k : props.keySet()) {
      keys.add((String) k);
    }
    Collections.sort(keys);
    Pattern nodeValuePattern = Pattern.compile("(\\S+)\\[node(\\d+)\\]");
    properties = new TreeMap<>();
    for (String key : keys) {
      Matcher m = nodeValuePattern.matcher(key);
      if (m.matches()) {
        Property p = properties.get(key);
        if (p == null) {
          p = new Property(this, m.group(1), null);
          properties.put(p.getPropertyKey(), p);
        }
        p.setNodeValue(Integer.parseInt(m.group(2)), props.getProperty(key));
        nodeValues.put(key, props.get(key));
        props.remove(key);
      } else {
        properties.put(key, new Property(this, key, props.getProperty(key)));
      }
    }
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      props.store(bos, null);
      this.propertyFileContent = bos.toString();
    } catch (IOException e) {
      throw new ApiException(e);
    }
  }

  public List<String> refreshCache() throws ApiException {
    return PropertyFiles.refreshCache(getPropertyFilePrefix());
  }
  
  @Override
  public String toString() {
    return "PropertyFiles [propertyFileId=" + propertyFileId + ", propertyFilePrefix=" + propertyFilePrefix + ", description=" + description
        + ", systemDefined=" + systemDefined + ", componentEditable=" + componentEditable + ", createdBy=" + createdBy + ", createdOn=" + createdOn
        + ", lastUpdatedBy=" + lastUpdatedBy + ", lastUpdatedOn=" + lastUpdatedOn + "]";
  }

  // static lookup methods:

  public static List<PropertyFiles> findAll() throws ApiException {
    List<PropertyFiles> result = new ArrayList<>();
    try {
      JSONArray jsonObjects = getJSONArray(get(SVC_NAME));
      for (int i = 0; i < jsonObjects.length(); i++) {
        result.add(new PropertyFiles(jsonObjects.getJSONObject(i)));
      }
    } catch (JSONException e) {
      throw new ApiException(e);
    }
    Collections.sort(result, new Comparator<PropertyFiles>() {
      @Override
      public int compare(PropertyFiles o1, PropertyFiles o2) {
        return Integer.compare(o1.propertyFileId, o2.propertyFileId);
      }
    });
    return result;
  }

  public static PropertyFiles find(String propertyFilePrefix) throws ApiException {
    PropertyFiles result = null;
    try {
      JSONObject json = getJSON(get(SVC_NAME, "?propertyFilePrefix=" + propertyFilePrefix));
      if (json != null && json.has(ERROR_CODE)) {
        LOGGER.log(Level.FINE, "PropertyFile with prefix {0} not found: errorCode={1}, errorDescription={2}.",
            new Object[] { propertyFilePrefix, json.getInt(ERROR_CODE), json.get(ERROR_DESCRIPTION) });
      } else {
        result = new PropertyFiles(json);
        LOGGER.log(Level.FINER, "Found property files {0}: {1}", new Object[] { propertyFilePrefix, result });
      }
    } catch (JSONException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public static PropertyFiles find(int propertyFileId) throws ApiException {
    PropertyFiles result = null;
    try {
      JSONObject json = getJSON(get(SVC_NAME, Integer.toString(propertyFileId)));
      if (json != null && json.has(ERROR_CODE)) {
        LOGGER.log(Level.FINE, "PropertyFile with ID {0} not found: errorCode={1}, errorDescription={2}.",
            new Object[] { propertyFileId, json.getInt(ERROR_CODE), json.get(ERROR_DESCRIPTION) });
      } else {
        result = new PropertyFiles(json);
        LOGGER.log(Level.FINER, "Found property files {0}: {1}", new Object[] { propertyFileId, result });
      }
    } catch (JSONException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public static boolean exists(PropertyFiles propertyFile) throws ApiException {
    return exists(propertyFile.getId());
  }

  public static boolean exists(String propertyFilePrefix) throws ApiException {
    boolean    result = false;
    JSONObject json;
    try {
      json = getJSON(get(SVC_NAME, propertyFilePrefix));
    } catch (JSONException e) {
      throw new ApiException(e);
    }
    if (json != null && json.has(ID_PROPERTY)) {
      result = true;
    }
    return result;
  }

  public static boolean canRefresh() {
    return useWsApi(WFD_WS_API);
  }

  public static List<String> refreshCache(String prefix) throws ApiException {
    if (!useWsApi(WFD_WS_API)) {
      throw new ApiException("The " + WFD_WS_API + " API is not implemented or not configured in ApiConfig!");
    }
    if (prefix == null || prefix.isEmpty()) {
      prefix = "%";
    }
    final List<String> props = new ArrayList<>();
    try {
      JSONArray json = getJSONArray(getJSONFromWsApi(WFD_WS_API, "&prefix=" + urlEncode(prefix), true));
      if (ApiClient.getApiReturnCode() != 200) {
        throw new ApiException("WS API returned " + ApiClient.getApiReturnCode() + "/" + ApiClient.getApiErrorMsg());
      }
      for (int i = 0; i < json.length(); i++) {
        props.add(String.format("%s:%s", json.getJSONObject(i).getString("NODE_NAME"), json.getJSONObject(i).getString("PROPERTY_FILE_PREFIX") ));
      }
    } catch (JSONException | UnsupportedEncodingException e) {
      throw new ApiException(e);
    }
    return props;
  }
}
