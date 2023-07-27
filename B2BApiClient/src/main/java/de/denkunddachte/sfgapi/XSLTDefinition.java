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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.CharBuffer;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.input.XmlStreamReader;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.denkunddachte.util.ApiConfig;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.exception.NotImplementedException;
import de.denkunddachte.sfgapi.WorkflowDefinition.VERSIONS;
import de.denkunddachte.siresource.SIArtifact;
import de.denkunddachte.siresource.SIArtifact.TYPE;
import de.denkunddachte.siresource.SIExport;

/**
 * @author chef
 *
 */
public class XSLTDefinition extends ApiClient {
  private static final Logger   LOGGER                  = Logger.getLogger(XSLTDefinition.class.getName());
  protected static final String SVC_NAME                = "xslt";
  protected static final String IMPORT_SVC_NAME         = "import";

  private static final String   CREATE_DATE             = "CREATE_DATE";
  private static final String   USERNAME                = "USERNAME";
  private static final String   STATUS                  = "STATUS";
  private static final String   XSLT_TEMPLATE           = "XSLT_TEMPLATE";
  private static final String   COMMENTS                = "COMMENTS";
  private static final String   DESCRIPTION             = "DESCRIPTION";
  private static final String   LATEST_VERSION          = "LATEST_VERSION";
  private static final String   DEFAULT_VERSION         = "DEFAULT_VERSION";
  private static final String   TEMPLATE_VERSION        = "TEMPLATE_VERSION";
  private static final String   TEMPLATE_NAME           = "TEMPLATE_NAME";

  private static final String   NAME                    = "name";
  protected static final String ID_PROPERTY             = NAME;

  // API fields
  String                        name;
  String                        encodedXsltData; // b64 encoded GZipped data!
  int                           version;
  private int                   defaultVersion;
  private int                   maxVersion;
  private String                comment;
  private String                modifiedBy;
  private OffsetDateTime        modifyTime;
  private boolean               enabled;
  private String                encoding;
  private String                description;
  private List<Integer>         versions;
  private boolean               setThisVersionAsDefault = true;

  // Constructors
  public XSLTDefinition() {
    super();
  }

  // Constructor NEW
  public XSLTDefinition(String name, String xsltData) throws ApiException {
    this(name, xsltData, null);
  }

  public XSLTDefinition(String name, File xsltFile) throws ApiException {
    this(name, xsltFile, null);
  }

  public XSLTDefinition(String name, String xsltData, String description) throws ApiException {
    super();
    this.name = name;
    this.description = description;
    setXsltData(xsltData);
  }

  public XSLTDefinition(String name, File xsltFile, String description) throws ApiException {
    super();
    this.name = name;
    this.description = description;
    setXsltData(xsltFile);
  }

  // Constructor from export
  public XSLTDefinition(SIArtifact artifact) throws UnsupportedEncodingException, ApiException {
    super();
    if (artifact.getType() != TYPE.XSLT) {
      throw new IllegalArgumentException("Invalid artifact type: " + artifact.getType());
    }
    this.name = artifact.getName();
    this.version = artifact.getVersion();
    setXsltData(artifact.getStringData());
    this.comment = artifact.getComment();
    this.defaultVersion = (artifact.isDefaultVersion() ? artifact.getVersion() : this.version);
    this.enabled = artifact.isEnabled();
    this.modifiedBy = artifact.getModifiedBy();
    this.modifyTime = artifact.getModifyTime();
    this.encoding = artifact.getEncoding();
  }

  private XSLTDefinition(JSONObject json) {
    super();
    readJSON(json);
  }

  @Override
  public String getId() {
    return version > 0 ? name + "/" + version : name;
  }

  @Override
  public String getIdProperty() {
    return ID_PROPERTY;
  }

  @Override
  public String getServiceName() {
    return SVC_NAME;
  }

  /**
   * Create JSON for CREATE, UPDATE
   */
  @Override
  public JSONObject toJSON() throws JSONException {
    JSONObject json = new JSONObject();
    if (isNew()) {
      throw new NotImplementedException("XSLTDefinition.toJSON() is obsolete because there are no create/update APIs!");
    } else {
      json.put(TEMPLATE_NAME, name);
      json.put(DESCRIPTION, description);
      if (encodedXsltData != null) {
        json.put(XSLT_TEMPLATE, encodedXsltData);
      }
      json.put(STATUS, isEnabled() ? 1 : 0);
    }
    return json;
  }

  @Override
  protected ApiClient readJSON(JSONObject json) throws JSONException {
    super.init(json);
    this.name = json.getString(TEMPLATE_NAME);
    this.version = json.getInt(TEMPLATE_VERSION);
    this.defaultVersion = json.getInt(DEFAULT_VERSION);
    this.maxVersion = json.getInt(LATEST_VERSION);
    this.description = json.getString(DESCRIPTION);
    this.setGeneratedId(this.name + "/" + this.version);
    if (!json.optString(COMMENTS).isEmpty()) {
      this.comment = json.getString(COMMENTS).substring(1);
    }
    if (json.has(XSLT_TEMPLATE)) {
      this.encodedXsltData = json.getString(XSLT_TEMPLATE);
      setRefreshRequired(false);
    }
    this.enabled = json.getInt(STATUS) == 1;
    this.modifiedBy = json.getString(USERNAME);
    DateTimeFormatter dtf = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd HH:mm:ss").appendFraction(ChronoField.MILLI_OF_SECOND, 0, 3, true)
        .toFormatter();
    this.modifyTime = OffsetDateTime.of(LocalDateTime.parse(json.getString(CREATE_DATE), dtf), ZoneOffset.ofHours(0));
    return this;
  }

  private SIArtifact toSIArtifact() throws UnsupportedEncodingException, ApiException {
    SIArtifact result = new SIArtifact(TYPE.XSLT, name);
    result.setVersion(version);
    result.setData(getXsltData());
    result.setDefaultVersion(setThisVersionAsDefault);
    result.setEnabled(enabled);
    result.setModifiedBy(ApiConfig.getInstance().getUser());
    result.setModifyTime(OffsetDateTime.now());
    result.setComment(comment);
    result.setDescription(description);
    return result;
  }

  // Getters and setters
  public String getXsltData() throws ApiException {
    if (encodedXsltData == null) {
      return null;
    }
    String data = null;
    // XSLT_TEMPLATE contains the gzipped XSLT payload, base64 encoded.
    try (InputStream is = new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(encodedXsltData)));
        ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      byte[] buf = new byte[8192];
      int    b;
      while ((b = is.read(buf)) > -1) {
        bos.write(buf, 0, b);
      }
      data = bos.toString("UTF-8");
    } catch (IOException e) {
      throw new ApiException(e);
    }
    return data;
  }

  public void setXsltData(File infile) throws ApiException {
    try (XmlStreamReader x = new XmlStreamReader(infile)) {
      this.encoding = x.getEncoding();
      CharBuffer cb = CharBuffer.allocate((int) infile.length());
      if (x.read(cb) > 0) {
        cb.flip();
        setXsltData(cb.toString());
      }
    } catch (IOException e) {
      throw new ApiException(e);
    }
  }

  public void setXsltData(String xsltData) throws ApiException {
    if (xsltData != null) {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      try (OutputStream os = new GZIPOutputStream(new Base64OutputStream(bos, true, 0, null))) {
        os.write(xsltData.getBytes("UTF-8"));
      } catch (IOException e) {
        throw new ApiException("Could not encode XSLT_TEMPLATE!", e);
      }
      this.encodedXsltData = bos.toString();
    } else {
      this.encodedXsltData = null;
    }
  }

  public String getEncoding() {
    return encoding;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public OffsetDateTime getModifyTime() {
    return modifyTime;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public int getDefaultVersion() {
    return defaultVersion;
  }

  public boolean isDefaultVersion() {
    return defaultVersion == version;
  }

  public boolean isSetThisVersionAsDefault() {
    return setThisVersionAsDefault;
  }

  public void setSetThisVersionAsDefault(boolean setThisVersionAsDefault) {
    this.setThisVersionAsDefault = setThisVersionAsDefault;
  }

  public int getMaxVersion() {
    return maxVersion;
  }

  public List<Integer> getVersions() throws ApiException {
    if (this.versions == null) {
      if (!useWsApi(SVC_NAME)) {
        throw new ApiException("The " + SVC_NAME + " API is not implemented or not configured in ApiConfig!");
      }

      this.versions = new ArrayList<>();
      try {
        JSONArray json = getJSONArray(getJSONFromWsApi(SVC_NAME, "&json=1&name=" + getName(), true));
        for (int i = 0; i < json.length(); i++) {
          versions.add(json.getJSONObject(i).getInt(TEMPLATE_VERSION));
          if (i == 0) {
            this.defaultVersion = json.getJSONObject(i).getInt(DEFAULT_VERSION);
            this.maxVersion = json.getJSONObject(i).getInt(LATEST_VERSION);
          }
        }
      } catch (JSONException e) {
        throw new ApiException(e);
      }
    }
    return this.versions;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public boolean delete() throws ApiException {
    throw new NotImplementedException("Deletion of XSLTs is not supported with WS API!");
  }

  @Override
  public boolean update() throws ApiException {
    if (!useWsApi(IMPORT_SVC_NAME)) {
      throw new NotImplementedException("XSLT WS API \"" + IMPORT_SVC_NAME + "\" not enabled!");
    }
    JSONObject json = null;
    try {
      StringBuilder sb = new StringBuilder(getWsApiBaseURI());
      sb.append("?api=").append(IMPORT_SVC_NAME).append("&json=1");
      HttpRequestBase       httpRequest = createRequest(RequestType.POST, new URI(sb.toString()), null);
      ByteArrayOutputStream bos         = new ByteArrayOutputStream();
      SIExport              si          = new SIExport();
      si.putArtifact(toSIArtifact());
      si.createImport(bos);
      ((HttpPost) httpRequest).setEntity(new ByteArrayEntity(bos.toByteArray()));
      try (CloseableHttpResponse response = executeRequest(httpRequest)) {
        if (response.getStatusLine().getStatusCode() != 200) {
          throw new ApiException("POST request " + sb + " failed with RC=" + response.getStatusLine());
        }
        json = new JSONObject(getJSONResponse(response));
        LOGGER.log(Level.FINE, "Import resources returns:\n{0}", json.getString("ImportResult").replace('~', '\n'));
        version = ++maxVersion;
//        if (setThisVersionAsDefault)
//          defaultVersion = version;
        setRefreshRequired();
      }
    } catch (IOException | URISyntaxException e) {
      throw new ApiException(e);
    }
    return json.getString("ImportResult").contains("Successfully imported");
  }

  @Override
  public boolean create() throws ApiException {
    return update();
  }

  @Override
  public void refresh() {
    try {
      XSLTDefinition xslt = XSLTDefinition.find(name, version);
      if (xslt != null) {
        this.encodedXsltData = xslt.encodedXsltData;
        this.comment = xslt.comment;
        this.defaultVersion = xslt.defaultVersion;
        this.version = xslt.version;
        this.maxVersion = xslt.maxVersion;
        this.description = xslt.description;
        this.enabled = xslt.enabled;
        this._id = xslt._id;
        this.href = xslt.href;
        this.encoding = xslt.encoding;
        this.modifiedBy = xslt.modifiedBy;
        this.modifyTime = xslt.modifyTime;
        this.versions = null;
      }
    } catch (ApiException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String toString() {
    return "XSLTDefinition [name" + name + ", version=" + version + "]";
  }

  // static lookup methods:
  public static List<XSLTDefinition> findAll() throws ApiException {
    return findAll(false);
  }

  public static List<XSLTDefinition> findAll(boolean getTemplates) throws ApiException {
    return findAll(null, 0, VERSIONS.DEFAULT, getTemplates);
  }

  public static List<XSLTDefinition> findAll(String globPattern) throws ApiException {
    return findAll(globPattern, false);
  }

  public static List<XSLTDefinition> findAll(String globPattern, boolean getTemplates) throws ApiException {
    return findAll(globPattern, 0, VERSIONS.DEFAULT, getTemplates);
  }

  public static List<XSLTDefinition> findAll(String globPattern, VERSIONS getVersions, boolean getTemplates) throws ApiException {
    return findAll(globPattern, 0, getVersions, getTemplates);
  }

  private static List<XSLTDefinition> findAll(String globPattern, int version, VERSIONS getVersions, boolean getTemplates) throws ApiException {
    if (!useWsApi(SVC_NAME)) {
      throw new NotImplementedException("XSLT WS API \"" + SVC_NAME + "\" not enabled!");
    }
    List<XSLTDefinition> result = new ArrayList<>();
    try {
      JSONObject    o      = null;
      StringBuilder params = new StringBuilder("&json=1");
      if (globPattern != null) {
        params.append("&name=").append(urlEncode(globPattern.replace('*', '%')));
      }
      if (version > 0) {
        params.append("&version=").append(version);
      } else if (getVersions == VERSIONS.DEFAULT) {
        params.append("&default=1");
      }
      if (getTemplates) {
        params.append("&template=1");
      }
      JSONArray json = new JSONArray(getJSONFromWsApi(SVC_NAME, params.toString(), true));

      for (int i = 0; i < json.length(); i++) {
        switch (getVersions) {
        case FIRST:
          if (o == null || !o.getString(TEMPLATE_NAME).equals(json.getJSONObject(i).getString(TEMPLATE_NAME))) {
            result.add(new XSLTDefinition(o));
            o = null;
          } else {
            o = json.getJSONObject(i);
          }
          break;
        case LAST:
          o = json.getJSONObject(i);
          if (o.getLong(TEMPLATE_VERSION) == o.getLong(LATEST_VERSION)) {
            result.add(new XSLTDefinition(o));
            o = null;
          }
          break;
        case DEFAULT:
        case ALL:
        default:
          result.add(new XSLTDefinition(json.getJSONObject(i)));
          break;
        }
      }
      if (o != null)
        result.add(new XSLTDefinition(o));
    } catch (UnsupportedEncodingException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public static XSLTDefinition find(String name) throws ApiException {
    return find(name, 0);
  }

  public static XSLTDefinition find(String name, int version) throws ApiException {
    XSLTDefinition result = null;

    for (XSLTDefinition xslt : findAll(name, version, (version == 0 ? VERSIONS.DEFAULT : VERSIONS.ALL), true)) {
      if (xslt.getName().equalsIgnoreCase(name) && (version == 0 || version == xslt.getVersion())) {
        result = xslt;
        LOGGER.log(Level.FINER, "Found XSLTDefinition {0}: {1}", new Object[] { name, result });
        break;
      }
    }
    return result;
  }

  public static boolean exists(XSLTDefinition xslt) throws ApiException {
    return exists(xslt.getId());
  }

  public static boolean exists(String name) throws ApiException {
    return find(name) != null;
  }
}
