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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "",
    propOrder = { "httpCode", "httpStatus", "action", "message", "messageLevel", "configurationObjects", "validationErrors", "objectsList", "results",
        "validationErrorsList" })
@XmlRootElement(name = "XmlResponse")
public class XmlResponse {
  private int                 httpCode;
  private String              httpStatus;
  private String              action;
  private String              message;
  private String              messageLevel;
  private String              configurationObjects;
  private String              validationErrors;
  private List<String>        objectsList;
  private Map<String, String> resultMap = new HashMap<>();
  protected List<String>      validationErrorsList;

  public XmlResponse() {
  }

  XmlResponse(int httpCode, String httpStatus, boolean dryrun) {
    this.httpCode = httpCode;
    this.httpStatus = httpStatus;
    this.resultMap = new HashMap<>();
    if (dryrun) {
      this.objectsList = new ArrayList<>();
      this.objectsList.add("{\"dryrun\": true}");
      resultMap.put("XML", "<result><dryrun>true</dryrun></result>");
    }
  }

  XmlResponse(int httpCode, String httpStatus, List<String> objectsList, Map<String, String> resultMap) {
    this.httpCode = httpCode;
    this.httpStatus = httpStatus;
    this.objectsList = objectsList;
    this.resultMap = resultMap;
  }

  @XmlElement(required = true)
  public Map<String, String> getResults() {
    return resultMap;
  }

  public void setResults(Map<String, String> results) {
    this.resultMap = results;
  }

  public int getHttpCode() {
    return httpCode;
  }

  public void setHttpCode(int httpCode) {
    this.httpCode = httpCode;
  }

  @XmlElement(nillable = true)
  public String getHttpStatus() {
    return httpStatus;
  }

  public void setHttpStatus(String httpStatus) {
    this.httpStatus = httpStatus;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getMessageLevel() {
    return messageLevel;
  }

  public void setMessageLevel(String messageLevel) {
    this.messageLevel = messageLevel;
  }

  public String getConfigurationObjects() {
    return configurationObjects;
  }

  public void setConfigurationObjects(String configurationObjects) {
    this.configurationObjects = configurationObjects;
  }

  public String getValidationErrors() {
    return validationErrors;
  }

  public void setValidationErrors(String validationErrors) {
    this.validationErrors = validationErrors;
  }

  @XmlElement(nillable = true)
  public List<String> getObjectsList() {
    return objectsList;
  }

  public void setObjectsList(List<String> objectsList) {
    this.objectsList = objectsList;
  }

  @XmlElement(nillable = true)
  public List<String> getValidationErrorsList() {
    return validationErrorsList;
  }

  public void setValidationErrorsList(List<String> validationErrorsList) {
    this.validationErrorsList = validationErrorsList;
  }

  @Override
  public String toString() {
    return "TestMap [httpCode=" + httpCode + ", httpStatus=" + httpStatus + ", action=" + action + ", message=" + message + ", messageLevel=" + messageLevel
        + ", configurationObjects=" + configurationObjects + ", validationErrors=" + validationErrors + ", objectsList=" + objectsList + ", resultMap="
        + resultMap + ", validationErrorsList=" + validationErrorsList + "]";
  }
}
