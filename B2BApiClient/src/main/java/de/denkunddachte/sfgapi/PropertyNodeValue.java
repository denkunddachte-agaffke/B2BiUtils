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

import org.json.JSONException;
import org.json.JSONObject;

import de.denkunddachte.exception.ApiException;

public class PropertyNodeValue extends ApiClient {
  protected static final String SVC_NAME       = "propertynodevalues";
  protected static final String NODE_NAME      = "nodeName";
  protected static final String PROPERTY       = "property";

  private int                   propertyId;
  private String                nodeName;
  private String                propertyValue;

  private boolean               doUpdate;

  protected PropertyNodeValue() {
    super();
  }

  protected PropertyNodeValue(Property property, int node, String propertyValue) throws ApiException {
    super();
    this.propertyId = property.getPropertyId();
    this.nodeName = "node" + node;
    this.propertyValue = propertyValue;
  }

  protected PropertyNodeValue(JSONObject json) throws JSONException {
    super();
    this.readJSON(json);
  }

  @Override
  public String getId() {
    return getGeneratedId() == null ? this.nodeName + ":" +  propertyId : getGeneratedId();
  }

  @Override
  public String getIdProperty() {
    return Property.PROPERTY_ID;
  }

  @Override
  public String getServiceName() {
    return SVC_NAME;
  }

  @Override
  public boolean update() throws ApiException {
    doUpdate = true;
    return super.update();
  }

  @Override
  public boolean create() throws ApiException {
    doUpdate = false;
    boolean result = super.create();
    this._id = nodeName + ":" + propertyId;
    return result;
  }

  @Override
  public JSONObject toJSON() throws JSONException {
    if (doUpdate) {
      return toJSON(false);
    } else {
      return toJSON(true);
    }
  }

  protected JSONObject toJSON(boolean full) {
    JSONObject json = new JSONObject();
    if (full) {
      if (getGeneratedId() == null) {
        json.put(PROPERTY, String.valueOf(propertyId));
        json.put(NODE_NAME, nodeName);
      } else {
        json.put(ID, getGeneratedId());
      }
    }
    json.put(Property.PROPERTY_VALUE, propertyValue);
    return json;
  }

  @Override
  protected PropertyNodeValue readJSON(JSONObject json) throws JSONException {
    super.init(json);
    this.propertyId = json.getInt(Property.PROPERTY_ID);
    this.nodeName = json.getString(NODE_NAME);
    this.propertyValue = json.getString(Property.PROPERTY_VALUE);
    return this;
  }

  public String getPropertyValue() {
    return propertyValue;
  }

  public void setPropertyValue(String propertyValue) {
    this.propertyValue = propertyValue;
  }

  public int getPropertyId() {
    return propertyId;
  }

  protected void setProperty(Property property) {
    this.propertyId = property.getPropertyId();
  }

  protected void setPropertyId(int propertyId) {
    this.propertyId = propertyId;
  }

  public String getNodeName() {
    return this.nodeName;
  }

  public int getNode() {
    return Integer.parseInt(nodeName.substring(5));
  }
}
