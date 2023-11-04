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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import de.denkunddachte.exception.ApiException;
import de.denkunddachte.exception.NotImplementedException;
import de.denkunddachte.siresource.SIExport;

public class Resources extends ApiClient {
  private static final Logger   LOGGER          = Logger.getLogger(Resources.class.getName());
  protected static final String EXPORT_SVC_NAME = "export";
  protected static final String IMPORT_SVC_NAME = "import";
  protected static final String DUMMY           = "DUMMY";

  public enum TYPE {
    WFD, XSLT
  }

  private String    filename;
  private String    data;
  private String    includePattern;
  private String    excludePattern;
  private String    securityContext;
  private String    securityId;
  private Set<TYPE> resourceTypes = new HashSet<>();
  private boolean   allVersions;
  private int       artifactCount;
  private File      dataFile;
  private String importResult;

  public Resources() {
    resourceTypes.add(TYPE.WFD);
    resourceTypes.add(TYPE.XSLT);
    allVersions = false;
  }

  // Constructors
  public Resources(String filename, Set<TYPE> resourceTypes, String includePattern, String excludePattern, boolean allVersions) {
    super();
    this.filename = filename;
    this.resourceTypes.addAll(resourceTypes);
    this.includePattern = includePattern;
    this.excludePattern = excludePattern;
    this.allVersions = allVersions;
  }

  @Override
  public String getId() {
    return filename;
  }

  @Override
  public String getIdProperty() {
    return "filename";
  }

  @Override
  public String getServiceName() {
    return EXPORT_SVC_NAME;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public File getDataFile() {
    return dataFile;
  }

  public void setDataFile(File resourceFile) {
    this.dataFile = resourceFile;
    this.filename = resourceFile.getName();
  }

  public String getIncludePattern() {
    return includePattern;
  }

  public void setIncludePattern(String includePattern) {
    this.includePattern = includePattern;
  }

  public String getExcludePattern() {
    return excludePattern;
  }

  public void setExcludePattern(String excludePattern) {
    this.excludePattern = excludePattern;
  }

  public String getSecurityContext() {
    return securityContext;
  }

  public void setSecurityContext(String securityContext) {
    this.securityContext = securityContext;
  }

  public String getSecurityId() {
    return securityId;
  }

  public void setSecurityId(String securityId) {
    this.securityId = securityId;
  }

  public Set<TYPE> getResourceTypes() {
    return Collections.unmodifiableSet(resourceTypes);
  }

  public void setResourceTypes(Set<TYPE> resourceTypes) {
    this.resourceTypes.clear();
    this.resourceTypes.addAll(resourceTypes);
  }

  public boolean isAllVersions() {
    return allVersions;
  }

  public void setAllVersions(boolean allVersions) {
    this.allVersions = allVersions;
  }

  public int getArtifactCount() {
    return artifactCount;
  }

  public String getImportResult() {
    return importResult;
  }

  /**
   * Create JSON for CREATE, UPDATE
   */
  @Override
  public JSONObject toJSON() throws JSONException {
    throw new NotImplementedException("Resources service does not have instance data!");
  }

  @Override
  protected ApiClient readJSON(JSONObject json) throws JSONException {
    throw new NotImplementedException("Resources service does not have instance data!");
  }

  @Override
  public boolean delete() throws ApiException {
    throw new NotImplementedException("Deletion of resources is not supported with WS API!");
  }

  @Override
  public boolean update() throws ApiException {
    throw new NotImplementedException("Update of resources is not supported with WS API!");
  }

  @Override
  public boolean create() throws ApiException {
    throw new NotImplementedException("Creation of resources is not supported with WS API!");
  }

  @Override
  public String toString() {
    return "Resources [name" + filename + "]";
  }

  private void appendCommonParams(StringBuilder sb) throws UnsupportedEncodingException {
    if (includePattern != null) {
      sb.append("&include=");
      if (!includePattern.startsWith("^") && !includePattern.endsWith("$")) {
        sb.append(urlEncode("^"));
      }
      sb.append(urlEncode(includePattern));
    }
    if (excludePattern != null) {
      sb.append("&exclude=");
      if (!excludePattern.startsWith("^") && !excludePattern.endsWith("$")) {
        sb.append(urlEncode("^"));
      }
      sb.append(urlEncode(excludePattern));
    }
    if (securityContext != null) {
      sb.append("?secContext=").append(securityContext);
      sb.append("?secId=").append(securityId);
    }
  }

  public boolean importResources() throws ApiException {
    if (!useWsApi(IMPORT_SVC_NAME)) {
      throw new NotImplementedException("API \"" + IMPORT_SVC_NAME + "\" not enabled!");
    }
    HttpEntity entity = null;
    JSONObject json   = null;
    try {
      if (dataFile != null) {
        entity = new FileEntity(dataFile);
      } else if (data != null) {
        entity = new StringEntity(data);
      } else {
        throw new IllegalStateException("Data is empty!");
      }
      StringBuilder params = new StringBuilder(getWsApiBaseURI());
      params.append("?api=").append(IMPORT_SVC_NAME).append("&json=1");
      appendCommonParams(params);
      HttpRequestBase httpRequest = createRequest(RequestType.POST, new URI(params.toString()), null);

      ((HttpPost) httpRequest).setEntity(entity);
      try (CloseableHttpResponse response = executeRequest(httpRequest)) {
        if (response.getStatusLine().getStatusCode() != 200) {
          throw new ApiException("POST request " + params + " failed with RC=" + response.getStatusLine());
        }
        json = new JSONObject(getJSONResponse(response));
        this.importResult = json.getString("ImportResult").replace('~', '\n');
        LOGGER.log(Level.FINE, "Import resources returns:\n{0}", importResult);
      }
    } catch (URISyntaxException | IOException e) {
      throw new ApiException(e);
    }
    return importResult.contains("Successfully imported");
  }

  public boolean export() throws ApiException {
    if (!useWsApi(EXPORT_SVC_NAME)) {
      throw new NotImplementedException("WS API \"" + EXPORT_SVC_NAME + "\" not enabled!");
    }
    boolean result = false;
    try {
      StringBuilder params = new StringBuilder("&exportAll=").append(allVersions ? "1" : "0");
      appendCommonParams(params);
      resourceTypes.forEach(t -> params.append("&export").append(t.name()).append("=1"));
      data = getFromWsApi(EXPORT_SVC_NAME, params.toString());
      try {
        SIExport se = new SIExport();
        se.parse(data);
        if (se.getArtifacts().isEmpty()) {
          LOGGER.log(Level.FINE, "Resource export is empty.");
        } else {
          artifactCount = se.getArtifacts().size();
          LOGGER.log(Level.FINE, "Exported resources: {0}.", artifactCount);
          result = true;
        }
      } catch (ApiException e) {
        LOGGER.log(Level.WARNING, "Could not parse resource export: {0}", e.getMessage());
      }
    } catch (UnsupportedEncodingException e) {
      throw new ApiException(e);
    }
    return result;
  }
}
