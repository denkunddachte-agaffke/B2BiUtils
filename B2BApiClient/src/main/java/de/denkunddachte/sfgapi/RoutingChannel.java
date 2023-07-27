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
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.denkunddachte.exception.ApiException;
import de.denkunddachte.exception.NotImplementedException;

public class RoutingChannel extends ApiClient {
  private final static Logger       LOGGER            = Logger.getLogger(RoutingChannel.class.getName());
  protected static final String     SVC_NAME          = "routingchannels";
  protected static final String     ID_PROPERTY       = "routingChannelKey";
  private String                    templateName;
  private String                    producer;
  private String                    consumer;
  private final Map<String, String> provisioningFacts = new HashMap<>();
  private String                    routingChannelKey;
  private String                    producerMailboxPath;

  public RoutingChannel() {
    super();
  }

  public RoutingChannel(String templateName, String producer) {
    this(templateName, producer, null);
  }

  public RoutingChannel(String templateName, String producer, String consumer) {
    super();
    this.templateName = templateName;
    this.producer = producer;
    this.consumer = consumer;
  }

  private RoutingChannel(JSONObject json) throws JSONException {
    super();
    this.readJSON(json);
  }

  @Override
  public String getServiceName() {
    return SVC_NAME;
  }

  @Override
  public String getIdProperty() {
    return ID_PROPERTY;
  }

  @Override
  public JSONObject toJSON() throws JSONException {
    JSONObject json = new JSONObject();

    json.put("templateName", templateName);
    json.put("producer", producer);
    json.put("consumer", consumer);
    if (!provisioningFacts.isEmpty()) {
      JSONArray pf = new JSONArray();
      pf.put(provisioningFacts);
      json.put("provisioningFacts", pf);
    }
    return json;
  }

  @Override
  public String getId() {
    return getGeneratedId() == null ? routingChannelKey : getGeneratedId();
  }

  public String getTemplateName() {
    return templateName;
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

  public String getProducer() {
    return producer;
  }

  public void setProducer(String producer) {
    this.producer = producer;
  }

  public String getConsumer() {
    return consumer;
  }

  public void setConsumer(String consumer) {
    this.consumer = consumer;
  }

  public Map<String, String> getProvisioningFacts() {
    Map<String, String> result = new HashMap<String, String>(provisioningFacts);
    return result;
  }

  public void setProvisioningFacts(Map<String, String> provisioningFacts) {
    this.provisioningFacts.clear();
    this.provisioningFacts.putAll(provisioningFacts);
  }

  public void clearProvisioningFacts() {
    provisioningFacts.clear();
  }

  public String putProvisioningFact(String provFactName, String provFactValue) {
    return provisioningFacts.put(provFactName, provFactValue);
  }

  public String getProvisioningFact(String provFactName) {
    return provisioningFacts.get(provFactName);
  }

  public String getRoutingChannelKey() {
    return routingChannelKey;
  }

  public void setRoutingChannelKey(String routingChannelKey) {
    this.routingChannelKey = routingChannelKey;
  }

  public String getProducerMailboxPath() {
    return producerMailboxPath;
  }

  public void setProducerMailboxPath(String producerMailboxPath) {
    this.producerMailboxPath = producerMailboxPath;
  }

  @Override
  protected RoutingChannel readJSON(JSONObject json) throws JSONException {
    super.init(json);
    this.templateName = json.getString("templateName");
    this.producer = json.getString("producer");
    if (json.has("consumer"))
      this.consumer = json.getString("consumer");
    if (json.has("provisioningFacts")) {
      JSONArray a = json.getJSONArray("provisioningFacts");
      for (int i = 0; i < a.length(); i++) {
        provisioningFacts.put(a.getJSONObject(i).getString("provFactName"), a.getJSONObject(i).getString("provFactValue"));
      }
    }
    if (json.has("routingChannelKey")) {
      routingChannelKey = json.getString("routingChannelKey");
    }
    if (json.has("producerMailboxPath")) {
      producerMailboxPath = json.getString("producerMailboxPath");
    }
    return this;
  }

  @Override
  public boolean update() {
    throw new NotImplementedException("Update is not supported for RoutingChannel objects!");
  }

  @Override
  public String toString() {
    return "RoutingChannel [routingChannelKey=" + routingChannelKey + ", templateName=" + templateName + ", producer=" + producer + ", consumer=" + consumer
        + ", provisioningFacts=" + provisioningFacts + ", producerMailboxPath=" + producerMailboxPath + "]";
  }

  // static lookup methods:
  public static List<RoutingChannel> findAll() throws ApiException {
    return findAll(null, null, null);

  }

  public static List<RoutingChannel> findAll(String template) throws ApiException {
    return findAll(template, null, null);
  }

  public static List<RoutingChannel> findByProducer(String producer) throws ApiException {
    return findAll(null, producer, null);
  }

  public static List<RoutingChannel> findAll(String templateName, String producer, String consumer, String... includeFields) throws ApiException {
    List<RoutingChannel> result = new ArrayList<>();
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("offset", 0);
      params.put("includeFields", includeFields);
      if (templateName != null)
        params.put("searchByTemplate", (templateName != null ? templateName.replace('*', '%') : null));
      if (producer != null)
        params.put("searchByProducer", (producer != null ? producer.replace('*', '%') : null));
      if (consumer != null)
        params.put("searchByConsumer", (consumer != null ? consumer.replace('*', '%') : null));
      while (true) {
        JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
        for (int i = 0; i < jsonObjects.length(); i++) {
          result.add(new RoutingChannel(jsonObjects.getJSONObject(i)));
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
  public static RoutingChannel find(String routingChannelKey) throws ApiException {
    RoutingChannel result = null;
    JSONObject     json   = findByKey(SVC_NAME, routingChannelKey);
    try {
      if (json.has("errorCode")) {
        LOGGER.finer("RoutingChannel " + routingChannelKey + " not found: errorCode=" + json.getInt("errorCode") + ", errorDescription="
            + json.get("errorDescription") + ".");
      } else {
        result = new RoutingChannel(json);
        LOGGER.finer("Found RoutingChannel " + routingChannelKey + ": " + result);
      }
    } catch (JSONException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public static boolean exists(RoutingChannel routingChannel) throws ApiException {
    return exists(routingChannel.getId());
  }

  public static boolean exists(String routingChannelKey) throws ApiException {
    JSONObject json = findByKey(SVC_NAME, routingChannelKey);
    return json.has(ID_PROPERTY);
  }

  public static RoutingChannel find(String templateName, String producer) throws ApiException {
    return find(templateName, producer, null);
  }

  public static RoutingChannel find(String templateName, String producer, String consumer) throws ApiException {
    RoutingChannel result          = null;
    int            requiredmatches = 0;
    if (templateName != null)
      requiredmatches++;
    if (producer != null)
      requiredmatches++;
    if (consumer != null)
      requiredmatches++;
    int matches = 0;
    for (RoutingChannel rc : findAll(templateName, producer, consumer)) {
      if (templateName != null && templateName.equals(rc.getTemplateName()))
        matches++;
      if (producer != null && producer.equals(rc.getProducer()))
        matches++;
      if (consumer != null && consumer.equals(rc.getConsumer()))
        matches++;
      if (matches == requiredmatches) {
        result = rc;
        break;
      }
    }
    return result;
  }

  public static boolean exists(String templateName, String producer) throws ApiException {
    return (find(templateName, producer) != null);
  }

  public static boolean exists(String templateName, String producer, String consumer) throws ApiException {
    return (find(templateName, producer, consumer) != null);
  }
}
