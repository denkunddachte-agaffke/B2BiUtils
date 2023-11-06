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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.CharBuffer;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.input.XmlStreamReader;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.denkunddachte.util.ApiConfig;
import de.denkunddachte.enums.DocumentStorage;
import de.denkunddachte.enums.NodePreference;
import de.denkunddachte.enums.PersistenceLevel;
import de.denkunddachte.enums.Queue;
import de.denkunddachte.enums.RecoveryLevel;
import de.denkunddachte.enums.RemovalMethod;
import de.denkunddachte.enums.ReportingLevel;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.ft.ExternalProcess;

/**
 * Template for new API 
 * @author chef
 *
 */
public class WorkflowDefinition extends ApiClient {
  private static final Logger LOGGER = Logger.getLogger(WorkflowDefinition.class.getName());

  public enum VERSIONS {
    ALL, DEFAULT, FIRST, LAST
  }

  protected static final String     SVC_NAME                    = "workflows";
  protected static final String     WFD_WS_API                  = "wfd";
  protected static final String     TOGGLE_WFD_WS_API           = "togglewfd";
  protected static final String     REFRESH_WFD_WS_API          = "refreshwfd";

  // JSON fields
  private static final String       NAME                        = "name";
  private static final String       WFD_ID                      = "wfdID";
  private static final String       WFD_VERSION                 = "wfdVersion";
  private static final String       MODIFIED_BY                 = "modifiedBy";
  private static final String       TIMESTAMP                   = "timestamp";
  private static final String       IDENTIFIER                  = ID;
  private static final String       DEFAULT_VERSION             = "defaultVersion";
  private static final String       SET_THIS_VERSION_AS_DEFAULT = "setThisVersionAsDefault";
  private static final String       ENABLE_BUSINESS_PROCESS     = "enableBusinessProcess";
  private static final String       USE_BP_QUEUING              = "useBPQueuing";
  private static final String       SOFTSTOP_RECOVERY_LEVEL     = "softstopRecoveryLevel";
  private static final String       RECOVERY_LEVEL              = "recoveryLevel";
  private static final String       QUEUE                       = "queue";
  private static final String       PERSISTENCE_LEVEL           = "persistenceLevel";
  private static final String       ONFAULT_PROCESSING          = "onfaultProcessing";
  private static final String       NODE                        = "node";
  private static final String       NODE_PREFERENCE             = "nodePreference";
  private static final String       REMOVAL_METHOD              = "removalMethod";
  private static final String       LIFESPAN_HOURS              = "lifespanHours";
  private static final String       LIFESPAN_DAYS               = "lifespanDays";
  private static final String       EVENT_REPORTING_LEVEL       = "eventReportingLevel";
  private static final String       DOCUMENT_TRACKING           = "documentTracking";
  private static final String       DOCUMENT_STORAGE            = "documentStorage";
  private static final String       SECOND_NOTIFICATION_MINUTES = "secondNotificationMinutes";
  private static final String       SECOND_NOTIFICATION_HOURS   = "secondNotificationHours";
  private static final String       FIRST_NOTIFICATION_MINUTES  = "firstNotificationMinutes";
  private static final String       FIRST_NOTIFICATION_HOURS    = "firstNotificationHours";
  private static final String       DEADLINE_MINUTES            = "deadlineMinutes";
  private static final String       DEADLINE_HOURS              = "deadlineHours";
  private static final String       COMMIT_STEPS_UPON_ERROR     = "commitStepsUponError";
  private static final String       ENABLE_TRANSACTION          = "enableTransaction";
  private static final String       CATEGORY                    = "category";
  private static final String       BUSINESS_PROCESS            = "businessProcess";
  private static final String       DESCRIPTION                 = "description";
  private static final String       EXECUTE_WS_API              = "executebp";

  protected static final String     ID_PROPERTY                 = NAME;

  // List of fields returned by DD_API_WS "wfd" api:
  private static final List<String> WFD_WS_API_FIELDS           = Arrays
      .asList(new String[] { WFD_ID, NAME, WFD_VERSION, DEFAULT_VERSION, ENABLE_BUSINESS_PROCESS, TIMESTAMP, MODIFIED_BY, DESCRIPTION });
  // API fields
  private String                    name;
  private String                    description;
  private String                    businessProcess;

  private boolean                   documentTracking;
  private boolean                   onfaultProcessing;
  private Queue                     queue;
  private boolean                   useBPQueuing;
  private boolean                   enableTransaction;
  private boolean                   commitStepsUponError;
  private String                    category;
  private PersistenceLevel          persistenceLevel;
  private ReportingLevel            eventReportingLevel;
  private RecoveryLevel             recoveryLevel;
  private RecoveryLevel             softstopRecoveryLevel;
  private DocumentStorage           documentStorage;

  private NodePreference            nodePreference;
  private String                    node;

  private boolean                   setCustomDeadline;
  private Integer                   deadlineHours;
  private Integer                   deadlineMinutes;
  private Integer                   firstNotificationHours;
  private Integer                   firstNotificationMinutes;
  private Integer                   secondNotificationHours;
  private Integer                   secondNotificationMinutes;

  private boolean                   setCustomLifespan;
  private Integer                   lifespanDays;
  private Integer                   lifespanHours;
  private RemovalMethod             removalMethod;

  private boolean                   enableBusinessProcess;

  private boolean                   setThisVersionAsDefault;
  private int                       wfdVersion;
  private int                       wfdId;

  private int                       defaultVersion;
  private String                    identifier;
  private OffsetDateTime            timestamp;
  private String                    modifiedBy;

  // local
  private List<Integer>             wfdVersions;
  private Integer                   maxVersion;
  private static boolean            toggleUsingWsApi            = false;
  private static boolean            useApiToSetDefault          = false;
  private static boolean            refreshWfdCache             = false;

  // Constructors
  public WorkflowDefinition() {
    super();
  }

  // Constructor NEW
  public WorkflowDefinition(File infile, String description) throws ApiException {
    super();
    this.description = description;
    setBusinessProcess(infile);
    setDefaults();
  }

  public WorkflowDefinition(String name, String description, String businessProcess) throws ApiException {
    super();
    this.name = name;
    this.description = description;
    setBusinessProcess(businessProcess);
    setDefaults();
  }

  // Constructor from server
  private WorkflowDefinition(JSONObject json) throws JSONException, ApiException {
    super();
    this.readJSON(json);
  }

  private void setDefaults() throws ApiException {
    // defaults:
    ApiConfig cfg = ApiConfig.getInstance();
    this.documentTracking = cfg.isWfdDefaultDocumentTracking();
    this.onfaultProcessing = cfg.isWfdDefaultOnfaultProcessing();
    this.queue = cfg.getWfdDefaultQueue();
    this.useBPQueuing = cfg.isWfdDefaultUseBPQueuing();
    this.enableTransaction = cfg.isWfdDefaultEnableTransaction();
    this.commitStepsUponError = cfg.isWfdDefaultCommitStepsUponError();
    this.persistenceLevel = cfg.getWfdDefaultPersistenceLevel();
    this.eventReportingLevel = cfg.getWfdDefaultEventReportingLevel();
    this.recoveryLevel = cfg.getWfdDefaultRecoveryLevel();
    this.softstopRecoveryLevel = cfg.getWfdDefaultSoftstopRecoveryLevel();
    this.documentStorage = cfg.getWfdDefaultDocumentStorage();
    this.nodePreference = cfg.getWfdDefaultNodePreference();
    this.setCustomDeadline = cfg.isWfdDefaultSetCustomDeadline();
    this.setCustomLifespan = cfg.isWfdDefaultSetCustomLifespan();
    this.removalMethod = cfg.getWfdDefaultRemovalMethod();
    this.enableBusinessProcess = cfg.isWfdDefaultEnableBusinessProcess();
    this.setThisVersionAsDefault = cfg.isWfdDefaultSetThisVersionAsDefault();
    WorkflowDefinition.toggleUsingWsApi = cfg.isWfdToggleUsingWsApi();
    WorkflowDefinition.useApiToSetDefault = cfg.isWfdUseApiToSetDefault();
    WorkflowDefinition.refreshWfdCache = cfg.isWfdRefreshWfdCache();
  }

  @Override
  public String getId() {
    return getGeneratedId() == null ? (wfdVersion > 0 ? name + "/" + wfdVersion : name) : getGeneratedId();
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
      json.put(NAME, this.name);
    } else {
      if (setThisVersionAsDefault && useApiToSetDefault) {
        LOGGER.log(Level.FINER, "Override setThisVersionAsDefault with \"false\".");
        json.put(SET_THIS_VERSION_AS_DEFAULT, false);
      } else {
        json.put(SET_THIS_VERSION_AS_DEFAULT, setThisVersionAsDefault);
      }
    }
    json.put(DESCRIPTION, description);
    json.put(BUSINESS_PROCESS, businessProcess);
    if (category != null)
      json.put(CATEGORY, category);
    if (enableTransaction) {
      json.put(ENABLE_TRANSACTION, enableTransaction);
      json.put(COMMIT_STEPS_UPON_ERROR, commitStepsUponError);
    }
    if (setCustomDeadline) {
      json.put(DEADLINE_HOURS, deadlineHours);
      json.put(DEADLINE_MINUTES, deadlineMinutes);
      json.put(FIRST_NOTIFICATION_HOURS, firstNotificationHours);
      json.put(FIRST_NOTIFICATION_MINUTES, firstNotificationMinutes);
      json.put(SECOND_NOTIFICATION_HOURS, secondNotificationHours);
      json.put(SECOND_NOTIFICATION_MINUTES, secondNotificationMinutes);
    }
    json.put(DOCUMENT_STORAGE, documentStorage.getCode());
    json.put(DOCUMENT_TRACKING, documentTracking);

    json.put(EVENT_REPORTING_LEVEL, eventReportingLevel.getCode());

    if (setCustomLifespan) {
      json.put(LIFESPAN_DAYS, lifespanDays);
      json.put(LIFESPAN_HOURS, lifespanHours);
    }
    json.put(REMOVAL_METHOD, removalMethod.getCode());

    json.put(NODE_PREFERENCE, nodePreference.getCode());
    json.put(NODE, node);

    json.put(ONFAULT_PROCESSING, onfaultProcessing);
    json.put(PERSISTENCE_LEVEL, persistenceLevel.getCode());
    json.put(QUEUE, queue.getQueueNumber());
    json.put(RECOVERY_LEVEL, recoveryLevel.getCode());
    json.put(SOFTSTOP_RECOVERY_LEVEL, softstopRecoveryLevel.getSoftStopCode());
    json.put(USE_BP_QUEUING, useBPQueuing);
    json.put(ENABLE_BUSINESS_PROCESS, enableBusinessProcess);

    return json;
  }

  @Override
  protected ApiClient readJSON(JSONObject json) throws JSONException, ApiException {
    super.init(json);
    this.name = json.getString(NAME);
    this.businessProcess = json.optString(BUSINESS_PROCESS);
    this.category = json.optString(CATEGORY);
    if (json.has(COMMIT_STEPS_UPON_ERROR))
      this.commitStepsUponError = json.getJSONObject(COMMIT_STEPS_UPON_ERROR).getBoolean(CODE);
    if (json.has(DEADLINE_HOURS))
      this.deadlineHours = json.getInt(DEADLINE_HOURS);
    if (json.has(DEADLINE_MINUTES))
      this.deadlineMinutes = json.getInt(DEADLINE_MINUTES);
    this.defaultVersion = json.optInt(DEFAULT_VERSION);
    if (json.has(SET_THIS_VERSION_AS_DEFAULT))
      this.setThisVersionAsDefault = json.getJSONObject(SET_THIS_VERSION_AS_DEFAULT).getBoolean(CODE);
    this.description = json.optString(DESCRIPTION);
    if (json.has(DOCUMENT_STORAGE))
      this.documentStorage = DocumentStorage.getByCode(json.getJSONObject(DOCUMENT_STORAGE).getInt(CODE));
    if (json.has(DOCUMENT_TRACKING))
      this.documentTracking = json.getJSONObject(DOCUMENT_TRACKING).getBoolean(CODE);
    if (json.has(ENABLE_TRANSACTION))
      this.enableTransaction = json.getJSONObject(ENABLE_TRANSACTION).getBoolean(CODE);
    if (json.has(ENABLE_BUSINESS_PROCESS))
      this.enableBusinessProcess = json.getJSONObject(ENABLE_BUSINESS_PROCESS).getBoolean(CODE);
    if (json.has(EVENT_REPORTING_LEVEL))
      this.eventReportingLevel = ReportingLevel.getByCode(json.getJSONObject(EVENT_REPORTING_LEVEL).getInt(CODE));
    if (json.has(FIRST_NOTIFICATION_HOURS))
      this.firstNotificationHours = json.getInt(FIRST_NOTIFICATION_HOURS);
    if (json.has(FIRST_NOTIFICATION_MINUTES))
      this.firstNotificationMinutes = json.getInt(FIRST_NOTIFICATION_MINUTES);
    if (json.has(SECOND_NOTIFICATION_HOURS))
      this.secondNotificationHours = json.getInt(SECOND_NOTIFICATION_HOURS);
    if (json.has(SECOND_NOTIFICATION_MINUTES))
      this.secondNotificationMinutes = json.getInt(SECOND_NOTIFICATION_MINUTES);

    this.identifier = json.getString(IDENTIFIER);
    if (json.has(TIMESTAMP)) {
      this.timestamp = toOffsetDateTime(json.getString(TIMESTAMP));
    }
    if (json.has(LIFESPAN_DAYS))
      this.lifespanDays = json.getInt(LIFESPAN_DAYS);
    if (json.has(LIFESPAN_HOURS))
      this.lifespanHours = json.getInt(LIFESPAN_HOURS);

    this.modifiedBy = json.optString(MODIFIED_BY);
    if (json.has(NODE_PREFERENCE))
      this.nodePreference = NodePreference.getByCode(json.getJSONObject(NODE_PREFERENCE).getInt(CODE));
    if (json.has(ONFAULT_PROCESSING))
      this.onfaultProcessing = json.getJSONObject(ONFAULT_PROCESSING).getBoolean(CODE);
    if (json.has(PERSISTENCE_LEVEL))
      this.persistenceLevel = PersistenceLevel.getByCode(json.getJSONObject(PERSISTENCE_LEVEL).getInt(CODE));
    if (json.has(QUEUE))
      this.queue = Queue.getQueue(json.getJSONObject(QUEUE).getInt(CODE));
    if (json.has(RECOVERY_LEVEL))
      this.recoveryLevel = RecoveryLevel.getByCode(json.getJSONObject(RECOVERY_LEVEL).getInt(CODE));
    if (json.has(REMOVAL_METHOD))
      this.removalMethod = RemovalMethod.getByCode(json.getJSONObject(REMOVAL_METHOD).getInt(CODE));
    if (json.has(SOFTSTOP_RECOVERY_LEVEL))
      this.softstopRecoveryLevel = RecoveryLevel.getByCode(json.getJSONObject(SOFTSTOP_RECOVERY_LEVEL).getInt(CODE));
    if (json.has(USE_BP_QUEUING))
      this.useBPQueuing = json.getJSONObject(USE_BP_QUEUING).getBoolean(CODE);
    this.wfdId = json.optInt(WFD_ID);
    // BUG: REST API returns the requested version number in "wfdVersion" if version does not exist!
    // Therefore, extract version from _id
    this.wfdVersion = json.optInt(WFD_VERSION);
    String id = json.getString("_id");
    if (id.indexOf('/') > -1 && !id.endsWith("/" + wfdVersion)) {
      LOGGER.log(Level.FINEST, "Fix wfdVersion mismatch:_id={0}, returned wfdVersion={1}", new Object[] { id, wfdVersion });
      this.wfdVersion = Integer.parseInt(id.substring(id.indexOf('/') + 1));
    }
    setRefreshRequired(isNullOrEmpty(this.businessProcess));
    setCustomDeadline = hasAny(deadlineHours, deadlineMinutes, firstNotificationHours, firstNotificationMinutes, secondNotificationHours,
        secondNotificationMinutes);
    setCustomLifespan = hasAny(lifespanDays, lifespanHours);
    return this;
  }

  private boolean hasAny(Integer... values) {
    for (Integer v : values) {
      if (v != null && v > 0)
        return true;
    }
    return false;
  }

  // Getters and setters
  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getBusinessProcess() {
    return businessProcess;
  }

  public boolean isDocumentTracking() {
    return documentTracking;
  }

  public void setDocumentTracking(boolean documentTracking) {
    this.documentTracking = documentTracking;
  }

  public boolean isOnfaultProcessing() {
    return onfaultProcessing;
  }

  public void setOnfaultProcessing(boolean onfaultProcessing) {
    this.onfaultProcessing = onfaultProcessing;
  }

  public Queue getQueue() {
    return queue;
  }

  public void setQueue(Queue queue) {
    this.queue = queue;
  }

  public boolean isUseBPQueuing() {
    return useBPQueuing;
  }

  public void setUseBPQueuing(boolean useBPQueuing) {
    this.useBPQueuing = useBPQueuing;
  }

  public boolean isEnableTransaction() {
    return enableTransaction;
  }

  public void setEnableTransaction(boolean enableTransaction) {
    this.enableTransaction = enableTransaction;
  }

  public boolean isCommitStepsUponError() {
    return commitStepsUponError;
  }

  public void setCommitStepsUponError(boolean commitStepsUponError) {
    this.commitStepsUponError = commitStepsUponError;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public PersistenceLevel getPersistenceLevel() {
    return persistenceLevel;
  }

  public void setPersistenceLevel(PersistenceLevel persistenceLevel) {
    this.persistenceLevel = persistenceLevel;
  }

  public ReportingLevel getEventReportingLevel() {
    return eventReportingLevel;
  }

  public void setEventReportingLevel(ReportingLevel eventReportingLevel) {
    this.eventReportingLevel = eventReportingLevel;
  }

  public RecoveryLevel getRecoveryLevel() {
    return recoveryLevel;
  }

  public void setRecoveryLevel(RecoveryLevel recoveryLevel) {
    this.recoveryLevel = recoveryLevel;
  }

  public RecoveryLevel getSoftstopRecoveryLevel() {
    return softstopRecoveryLevel;
  }

  public void setSoftstopRecoveryLevel(RecoveryLevel softstopRecoveryLevel) {
    this.softstopRecoveryLevel = softstopRecoveryLevel;
  }

  public DocumentStorage getDocumentStorage() {
    return documentStorage;
  }

  public void setDocumentStorage(DocumentStorage documentStorage) {
    this.documentStorage = documentStorage;
  }

  public NodePreference getNodePreference() {
    return nodePreference;
  }

  public void setNodePreference(NodePreference nodePreference) {
    this.nodePreference = nodePreference;
  }

  public String getNode() {
    return node;
  }

  public void setNode(String node) {
    this.node = node;
  }

  public boolean isSetCustomDeadline() {
    return setCustomDeadline;
  }

  public Integer getDeadlineHours() {
    return deadlineHours;
  }

  public void setDeadlineHours(Integer deadlineHours) {
    this.deadlineHours = deadlineHours;
  }

  public Integer getDeadlineMinutes() {
    return deadlineMinutes;
  }

  public void setDeadlineMinutes(Integer deadlineMinutes) {
    this.deadlineMinutes = deadlineMinutes;
  }

  public Integer getFirstNotificationHours() {
    return firstNotificationHours;
  }

  public void setFirstNotificationHours(Integer firstNotificationHours) {
    this.firstNotificationHours = firstNotificationHours;
  }

  public Integer getFirstNotificationMinutes() {
    return firstNotificationMinutes;
  }

  public void setFirstNotificationMinutes(Integer firstNotificationMinutes) {
    this.firstNotificationMinutes = firstNotificationMinutes;
  }

  public Integer getSecondNotificationHours() {
    return secondNotificationHours;
  }

  public void setSecondNotificationHours(Integer secondNotificationHours) {
    this.secondNotificationHours = secondNotificationHours;
  }

  public Integer getSecondNotificationMinutes() {
    return secondNotificationMinutes;
  }

  public void setSecondNotificationMinutes(Integer secondNotificationMinutes) {
    this.secondNotificationMinutes = secondNotificationMinutes;
  }

  public boolean isSetCustomLifespan() {
    return setCustomLifespan;
  }

  public Integer getLifespanDays() {
    return lifespanDays;
  }

  public void setLifespanDays(Integer lifespanDays) {
    this.lifespanDays = lifespanDays;
  }

  public Integer getLifespanHours() {
    return lifespanHours;
  }

  public void setLifespanHours(Integer lifespanHours) {
    this.lifespanHours = lifespanHours;
  }

  public RemovalMethod getRemovalMethod() {
    return removalMethod;
  }

  public void setRemovalMethod(RemovalMethod removalMethod) {
    this.removalMethod = removalMethod;
  }

  public boolean isSetThisVersionAsDefault() {
    return setThisVersionAsDefault;
  }

  public void setSetThisVersionAsDefault(boolean setThisVersionAsDefault) {
    this.setThisVersionAsDefault = setThisVersionAsDefault;
  }

  public boolean isDefaultVersion() {
    return wfdVersion == defaultVersion;
  }

  public boolean isEnableBusinessProcess() {
    return enableBusinessProcess;
  }

  public int getWfdVersion() {
    return wfdVersion;
  }

  public int getWfdId() {
    return wfdId;
  }

  public int getDefaultVersion() {
    return defaultVersion;
  }

  public String getIdentifier() {
    if (wfdVersion == 0) {
      return name;
    } else {
      return name + "/" + wfdVersion;
    }
  }

  public OffsetDateTime getTimestamp() {
    return timestamp;
  }

  public String getModifiedBy() {
    return modifiedBy;
  }

  private String getProcessName() throws ApiException {
    Pattern p = Pattern.compile("<process\\s+name\\s*=\\s*[\"'](\\S+?)[\"']\\s*>");
    Matcher m = p.matcher(this.businessProcess);
    if (m.find()) {
      return m.group(1);
    } else {
      throw new ApiException("Could not determine BP name from source!");
    }
  }

  public void setBusinessProcess(String businessProcess) throws ApiException {
    this.businessProcess = businessProcess;
    if (!this.getName().equals(getProcessName())) {
      throw new ApiException("BP source contains wrong name (" + getProcessName() + "). Expect " + name + "!");
    }
  }

  public void setBusinessProcess(File infile) throws ApiException {
    try (XmlStreamReader x = new XmlStreamReader(infile)) {
      CharBuffer cb = CharBuffer.allocate((int) infile.length());
      if (x.read(cb) > 0) {
        cb.flip();
        this.businessProcess = cb.toString();
      }
    } catch (IOException e) {
      throw new ApiException(e);
    }
    this.name = getProcessName();
  }

  public String getVersionInfo() throws ApiException {
    if (this.businessProcess == null) {
      throw new ApiException("Business process source is null!");
    }
    Pattern p = Pattern.compile("<!--(\\s*Versioninfo:.+?\\s*)-->", Pattern.DOTALL);
    Matcher m = p.matcher(this.businessProcess);
    if (m.find()) {
      return m.group(1);
    }
    return null;
  }

  public void setVersionInfo() throws ApiException {
    setVersionInfo(null);
  }

  public void setVersionInfo(String msg) throws ApiException {
    if (this.businessProcess == null) {
      throw new ApiException("Business process source is null!");
    }
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    String           versionInfo;
    if (msg == null || msg.isEmpty()) {
      versionInfo = String.format(" Versioninfo: user=%s, timestamp=%s ", System.getProperty("user.name"), sdf.format(new Date()));
    } else {
      versionInfo = String.format(" Versioninfo: user=%s, timestamp=%s%n %s%n", System.getProperty("user.name"), sdf.format(new Date()), msg);
    }
    String oldVersion = getVersionInfo();
    if (oldVersion != null) {
      this.businessProcess = this.businessProcess.replace(oldVersion, versionInfo);
    } else {
      this.businessProcess = this.businessProcess.replaceFirst("(<process\\s+name\\s*=\\s*['\"]\\S+['\\\"]\\s*>) *(\\r\\n|\\n)?",
          "$1$2  <!--" + versionInfo + "  -->$2");
    }
  }

  public int getMinVersion() throws ApiException {
    getWfdVersions();
    if (!wfdVersions.isEmpty()) {
      return wfdVersions.get(0);
    }
    return -1;
  }

  public int getMaxVersion() throws ApiException {
    getWfdVersions();
    if (!wfdVersions.isEmpty()) {
      return wfdVersions.get(wfdVersions.size() - 1);
    }
    return -1;
  }

  public List<Integer> getWfdVersions() throws ApiException {
    if (this.wfdVersions == null) {
      if (!useWsApi(WFD_WS_API)) {
        throw new ApiException("The " + WFD_WS_API + " API is not implemented or not configured in ApiConfig!");
      }

      this.wfdVersions = new ArrayList<>();
      try {
        JSONArray json = getJSONArray(getJSONFromWsApi(WFD_WS_API, "&bpname=" + getName(), true));
        for (int i = 0; i < json.length(); i++) {
          wfdVersions.add(json.getJSONObject(i).getInt("WFD_VERSION"));
          if (i == 0) {
            this.defaultVersion = json.getJSONObject(i).getInt("DEFAULT_VERSION");
            this.maxVersion = json.getJSONObject(i).getInt("LATEST_VERSION");
          }
        }
        // Collections.sort(wfdVersions);
      } catch (JSONException e) {
        throw new ApiException(e);
      }
    }
    return this.wfdVersions;
  }

  @Override
  public boolean delete() throws ApiException {
    if (Boolean.FALSE.equals(Boolean.valueOf(System.getProperty("de.denkunddachte.sfgapi.Workflowdefinition.enableDelete", "false")))) {
      throw new ApiException("The Workflow DELETE REST api is broken! Attempts to delete a single version causes all versions to be deleted! "
          + "Set system property de.denkunddachte.sfgapi.Workflowdefinition.enableDelete to true to enable api anyway.");
    }
    return super.delete();
  }

  public List<Integer> deleteAll(boolean includeDefault) throws ApiException {
    List<Integer> result = new ArrayList<>(getWfdVersions().size());
    try {
      for (int version : getWfdVersions()) {
        String key = getName() + "/" + version;
        if (!includeDefault && version == getDefaultVersion()) {
          LOGGER.log(Level.FINE, "Skip default WFD version {0}", key);
          continue;
        }
        if (DRYRUN) {
          LOGGER.log(Level.INFO, "DRY RUN: Skip operation={0}, service={1}, params={2}.", new Object[] { "DELETE", SVC_NAME, key });
          continue;
        }
        HttpRequestBase req = createRequest(RequestType.DELETE, getSvcUri(getServiceName(), key), null);
        try (CloseableHttpResponse response = executeRequest(req)) {
          JSONObject json = new JSONObject(getJSONResponse(response));
          if (json.has(ROWS_AFFECTED) && json.getInt(ROWS_AFFECTED) == 1) {
            LOGGER.log(Level.FINE, "Deleted {0}.", key);
            result.add(version);
          } else {
            LOGGER.log(Level.FINE, "Could not delete {0}, errorCode={1}, errorDescription={2}.",
                new Object[] { getId(), json.getInt(ERROR_CODE), json.get(ERROR_DESCRIPTION) });
          }
        }
      }
      clearCache(getServiceName());
      this.wfdVersions = null;
    } catch (IllegalStateException | IOException | URISyntaxException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public String toDumpString() {
    return "Workflow [name=" + name + ", description=" + description + ", businessProcess=" + businessProcess + ", documentTracking=" + documentTracking
        + ", onfaultProcessing=" + onfaultProcessing + ", queue=" + queue + ", useBPQueuing=" + useBPQueuing + ", enableTransaction=" + enableTransaction
        + ", commitStepsUponError=" + commitStepsUponError + ", category=" + category + ", persistenceLevel=" + persistenceLevel + ", eventReportingLevel="
        + eventReportingLevel + ", recoveryLevel=" + recoveryLevel + ", softstopRecoveryLevel=" + softstopRecoveryLevel + ", documentStorage=" + documentStorage
        + ", nodePreference=" + nodePreference + ", node=" + node + ", setCustomDeadline=" + setCustomDeadline + ", deadlineHours=" + deadlineHours
        + ", deadlineMinutes=" + deadlineMinutes + ", firstNotificationHours=" + firstNotificationHours + ", firstNotificationMinutes="
        + firstNotificationMinutes + ", secondNotificationHours=" + secondNotificationHours + ", secondNotificationMinutes=" + secondNotificationMinutes
        + ", setCustomLifespan=" + setCustomLifespan + ", lifespanDays=" + lifespanDays + ", lifespanHours=" + lifespanHours + ", removalMethod="
        + removalMethod + ", enableBusinessProcess=" + enableBusinessProcess + ", setThisVersionAsDefault=" + setThisVersionAsDefault + ", wfdVersion="
        + wfdVersion + ", wfdId=" + wfdId + ", defaultVersion=" + defaultVersion + ", identifier=" + identifier + ", timestamp=" + timestamp + ", modifiedBy="
        + modifiedBy + "]";
  }

  @Override
  public String toString() {
    if (wfdId == 0) {
      return "Workflow [name=" + name + "]";
    } else {
      return "Workflow [name=" + name + ", wfdVersion=" + wfdVersion + ", wfdId=" + wfdId + ", enableBusinessProcess=" + enableBusinessProcess
          + ", setThisVersionAsDefault=" + setThisVersionAsDefault + ", timestamp=" + timestamp + ", modifiedBy=" + modifiedBy + ", description=" + description
          + "]";
    }
  }

  // static lookup methods:
  public static List<WorkflowDefinition> findAll() throws ApiException {
    return findAll(false);
  }

  public static List<WorkflowDefinition> findAll(boolean withDetails) throws ApiException {
    return findAll(null, VERSIONS.LAST, withDetails);
  }

  public static List<WorkflowDefinition> findAll(String globPattern, String... includeFields) throws ApiException {
    return findAll(globPattern, VERSIONS.LAST, false, includeFields);
  }

  public static List<WorkflowDefinition> findAll(String globPattern, VERSIONS getVersions, boolean withDetails, String... includeFields) throws ApiException {
    final List<WorkflowDefinition> result;
    if (useWsApi(WFD_WS_API)) {
      result = findAllWithWSApi(globPattern + "%", getVersions);
    } else {
      result = findAllWithRESTApi(globPattern, includeFields);
    }

    if (withDetails || !WFD_WS_API_FIELDS.containsAll(Arrays.asList(includeFields))) {
      for (WorkflowDefinition wfd : result) {
        if (wfd.isRefreshRequired())
          wfd.refresh();
      }
    }
    return result;
  }

  private static List<WorkflowDefinition> findAllWithRESTApi(String globPattern, String... includeFields) throws ApiException {
    List<WorkflowDefinition> result = new ArrayList<>();
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("offset", 0);
      params.put("fieldList", "brief");
      if (includeFields.length != 0) {
        params.put("includeFields", includeFields);
      }
      if (globPattern != null)
        params.put("searchFor", (globPattern != null ? globPattern.replace('*', '%') : null));
      while (true) {
        JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
        for (int i = 0; i < jsonObjects.length(); i++) {
          result.add(new WorkflowDefinition(jsonObjects.getJSONObject(i)));
        }
        if (jsonObjects.length() < API_RANGESIZE)
          break;
      }
    } catch (JSONException | UnsupportedEncodingException e) {
      throw new ApiException(e);
    }
    return result;
  }

  private static List<WorkflowDefinition> findAllWithWSApi(String globPattern, VERSIONS getVersions) throws ApiException {
    List<WorkflowDefinition> result = new ArrayList<>();
    try {
      JSONObject    o      = null;
      StringBuilder params = new StringBuilder("&json=1");
      if (globPattern != null) {
        params.append("&bpname=").append(urlEncode(globPattern.replace('*', '%')));
      }
      if (getVersions == VERSIONS.DEFAULT) {
        params.append("&default=1");
      }
      JSONArray json = new JSONArray(getJSONFromWsApi(WFD_WS_API, params.toString(), true));

      for (int i = 0; i < json.length(); i++) {
        switch (getVersions) {
        case FIRST:
          if (o == null || o.getLong("WFD_ID") != json.getJSONObject(i).getLong("WFD_ID")) {
            result.add(fromWsApi(o));
            o = null;
          } else {
            o = json.getJSONObject(i);
          }
          break;
        case LAST:
          o = json.getJSONObject(i);
          if (o.getLong("WFD_VERSION") == o.getLong("LATEST_VERSION")) {
            result.add(fromWsApi(o));
            o = null;
          }
          break;
        case DEFAULT:
        case ALL:
        default:
          result.add(fromWsApi(json.getJSONObject(i)));
          break;
        }
      }
      if (o != null)
        result.add(fromWsApi(o));
    } catch (UnsupportedEncodingException e) {
      throw new ApiException(e);
    }
    return result;
  }

  private static WorkflowDefinition fromWsApi(JSONObject o) {
    WorkflowDefinition result = new WorkflowDefinition();
    result.name = o.getString("NAME");
    result.wfdId = o.getInt("WFD_ID");
    result.wfdVersion = o.getInt("WFD_VERSION");
    result.description = o.getString("DESCRIPTION").substring(1);
    result.modifiedBy = o.getString("EDITED_BY");
    DateTimeFormatter dtf = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd HH:mm:ss").appendFraction(ChronoField.MILLI_OF_SECOND, 0, 3, true)
        .toFormatter();
    result.timestamp = OffsetDateTime.of(LocalDateTime.parse(o.getString("MOD_DATE"), dtf), ZoneOffset.ofHours(0));
    result.enableBusinessProcess = o.getInt("STATUS") == 1;
    result.defaultVersion = o.getInt("DEFAULT_VERSION");
    result.maxVersion = o.getInt("LATEST_VERSION");
    result.setGeneratedId(result.name + "/" + result.wfdVersion);
    result.setRefreshRequired(true);
    return result;
  }

  // BUG: REST API does not reliably return the default version when doing lookup by id. Get with searchFor...
  public static WorkflowDefinition find(String name) throws ApiException {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("offset", 0);
      params.put("fieldList", "full");
      params.put("searchFor", name);
      JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
      for (int i = 0; i < jsonObjects.length(); i++) {
        if (jsonObjects.getJSONObject(i).getString("name").equals(name)) {
          return new WorkflowDefinition(jsonObjects.getJSONObject(i));
        }
      }
    } catch (JSONException | UnsupportedEncodingException e) {
      throw new ApiException(e);
    }
    return null;
  }

  public static WorkflowDefinition find(String name, int version) throws ApiException {
    if (version == 0) {
      return find(name);
    }
    WorkflowDefinition result = null;
    String             key    = name + "/" + version;
    JSONObject         json   = findByKey(SVC_NAME, key);
    try {
      if (json.has(ERROR_CODE)) {
        LOGGER.log(Level.FINER, "Workflow {0} not found: errorCode={1}, errorDescription={2}.",
            new Object[] { key, json.getInt(ERROR_CODE), json.get(ERROR_DESCRIPTION) });
      } else {
        result = new WorkflowDefinition(json);
        // BUG: B2Bi REST Api returns default(?) version if requested version does not exist
        if (version > 0 && version != result.getWfdVersion()) {
          LOGGER.log(Level.FINER, "Discard returned WFD version {0}. It is not what was asked for {1}...", new Object[] { result.getWfdVersion(), version });
          result = null;
        } else {
          LOGGER.log(Level.FINER, "Found Workflow {0}: {1}", new Object[] { key, result });
        }
      }
    } catch (JSONException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public static boolean exists(WorkflowDefinition workflow) throws ApiException {
    return exists(workflow.getIdentifier());
  }

  public static boolean exists(String name) throws ApiException {
    return exists(name, 0);
  }

  public static boolean exists(String name, int version) throws ApiException {
    String     key  = (version == 0 ? name : name + "/" + version);
    JSONObject json = findByKey(SVC_NAME, key);
    return json.has(ID_PROPERTY);
  }

  // actions
  @Override
  public boolean update() throws ApiException {
    boolean result = super.update(getName());
    if (result) {
      // assume success and wfdVersion was incremented. Patch stored _id and href so refresh() loads the new version:
      if (maxVersion == null) {
        JSONObject o = new JSONObject(getJSONFromWsApi(WFD_WS_API, "&json=1&default=1&bpname=" + this.getName(), false));
        maxVersion = o.getInt("LATEST_VERSION");
      } else {
        maxVersion++;
      }
      wfdVersion = maxVersion;
      this.href = this.href.replace(this._id, getIdentifier());
      this._id = getIdentifier();
      if (wfdVersions != null) {
        wfdVersions.add(wfdVersion);
      }
      if (setThisVersionAsDefault) {
        if (useApiToSetDefault) {
          setDefaultVersion();
        } else {
          defaultVersion = wfdVersion;
        }
      }
    }
    if (refreshWfdCache) {
      LOGGER.log(Level.FINER, "update(): refresh WFD cache...");
      try {
        JSONObject json = getJSON(getJSONFromWsApi(REFRESH_WFD_WS_API, "&bpname=" + name, false));
        LOGGER.log(Level.FINER, "json: {0}", json);
      } catch (JSONException e) {
        throw new ApiException(e);
      }
    }
    return result;
  }

  @Override
  public void refresh() throws ApiException {
    super.refresh();
    this.maxVersion = null;
    this.wfdVersions = null;
  }

  public boolean setDefaultVersion() throws ApiException {
    boolean result = WorkflowDefinition.changeDefaultVersion(getName(), getWfdVersion());
    if (result) {
      this.defaultVersion = getWfdVersion();
    }
    return result;
  }

  public static boolean changeDefaultVersion(String name, int version) throws ApiException {
    String key = (version == 0 ? name : name + "/" + version);
    LOGGER.log(Level.FINER, "changeDefaultVersion(); key={0}", key);
    if (DRYRUN) {
      LOGGER.log(Level.INFO, "DRY RUN: Skip operation={0}, service={1}, params={2}.", new Object[] { "CREATE", SVC_NAME, key });
      return true;
    }
    boolean result = false;
    try {
      HttpRequestBase req = createRequest(RequestType.POST, getSvcUri(SVC_NAME, key + "/actions/changedefaultversion"), null);
      try (CloseableHttpResponse response = executeRequest(req)) {
        JSONObject json = new JSONObject(getJSONResponse(response));
        result = response.getStatusLine().getStatusCode() == 200;
        if (result) {
          LOGGER.log(Level.FINE, "changeDefaultVersion: {0}", key);
        } else {
          setApiError(json.getInt(ERROR_CODE), json.getString(ERROR_DESCRIPTION));
          LOGGER.log(Level.WARNING, "Could not change default version {0}, errorCode={1}, errorDescription={2}.",
              new Object[] { key, json.getInt(ERROR_CODE), json.get(ERROR_DESCRIPTION) });
        }
      }
    } catch (URISyntaxException | JSONException | IOException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public boolean toggleEnabledWorkflow(boolean enableBusinessProcess) throws ApiException {
    return WorkflowDefinition.toggleEnabledWorkflow(getName(), getWfdVersion(), enableBusinessProcess);
  }

  public static boolean toggleEnabledWorkflow(String name, int version, boolean enableBusinessProcess) throws ApiException {
    if (toggleUsingWsApi && useWsApi(TOGGLE_WFD_WS_API)) {
      return toggleEnabledWorkflowWithWsApi(name, version, enableBusinessProcess);
    } else {
      return toggleEnabledWorkflowWithRestApi(name, version, enableBusinessProcess);
    }
  }

  private static boolean toggleEnabledWorkflowWithWsApi(String name, int version, boolean enableBusinessProcess) throws ApiException {
    LOGGER.log(Level.FINER, "toggleEnabledWorkflowWithWsApi(); name={0}, version={1}, enableBusinessProcess={2}",
        new Object[] { name, version, enableBusinessProcess });
    boolean result = false;
    try {
      JSONObject json = getJSON(getJSONFromWsApi(TOGGLE_WFD_WS_API, "&bpname=" + name + "&bpversion=" + version + "&enable=" + enableBusinessProcess, false));
      result = json.getInt("Rows_Affected") == 1;
    } catch (JSONException e) {
      throw new ApiException(e);
    }
    return result;
  }

  private static boolean toggleEnabledWorkflowWithRestApi(String name, int version, boolean enableBusinessProcess) throws ApiException {
    String key = (version == 0 ? name : name + "/" + version);
    LOGGER.log(Level.FINER, "toggleEnabledWorkflowWithRestApi(); key={0}, enableBusinessProcess={1}", new Object[] { key, enableBusinessProcess });
    if (DRYRUN) {
      LOGGER.log(Level.INFO, "DRY RUN: Skip operation={0}, service={1}, params={2}.",
          new Object[] { "POST", SVC_NAME, key + "/actions/toggleEnabledWorkflow" });
      return true;
    }
    boolean result = false;
    try {
      JSONObject json = new JSONObject();
      json.put(ENABLE_BUSINESS_PROCESS, enableBusinessProcess);
      HttpRequestBase req = createRequest(RequestType.POST, getSvcUri(SVC_NAME, key + "/actions/toggleEnabledWorkflow"), json.toString());
      try (CloseableHttpResponse response = executeRequest(req)) {
        json = new JSONObject(getJSONResponse(response));
        result = response.getStatusLine().getStatusCode() == 200;
        if (result) {
          LOGGER.log(Level.FINE, "toggleEnabledWorkflow {0}: {1}", new Object[] { key, enableBusinessProcess });
        } else {
          setApiError(json.getInt(ERROR_CODE), json.getString(ERROR_DESCRIPTION));
          LOGGER.log(Level.WARNING, "Could not toggle enabled flag on workflow {0} -> {1}, errorCode={2}, errorDescription={3}.",
              new Object[] { key, enableBusinessProcess, json.getInt(ERROR_CODE), json.get(ERROR_DESCRIPTION) });
        }
      }
    } catch (URISyntaxException | JSONException | IOException e) {
      throw new ApiException(e);
    }
    return result;
  }

  // Execute BP:
  public Workflow execute() throws ApiException {
    return execute((byte[]) null, (String) null);
  }

  public Workflow execute(File primaryDoc) throws ApiException {
    return execute(primaryDoc, primaryDoc.getName());
  }

  public Workflow execute(byte[] data, String filename) throws ApiException {
    if (useWsApi(EXECUTE_WS_API)) {
      return executeWs(data, filename);
    } else {
      File primaryDoc = null;

      if (data != null) {
        primaryDoc = new File(ExternalProcess.getDefaultTempDir(), filename + ".tmp");
        try (FileOutputStream os = new FileOutputStream(primaryDoc)) {
          os.write(data);
          primaryDoc.deleteOnExit();
        } catch (IOException e) {
          throw new ApiException("Could not write " + primaryDoc.getAbsolutePath() + "!", e);
        }
      }
      return execute(primaryDoc, filename);
    }
  }

  public Workflow execute(File primaryDoc, String filename) throws ApiException {
    ApiConfig cfg = ApiConfig.getInstance();
    if (primaryDoc != null && (filename == null || filename.isEmpty())) {
      filename = primaryDoc.getName();
    }

    if (useWsApi(EXECUTE_WS_API)) {
      byte[] data = null;
      if (primaryDoc != null) {
        data = new byte[(int) primaryDoc.length()];
        try (InputStream is = new FileInputStream(primaryDoc)) {
          is.read(data);
        } catch (IOException e) {
          throw new ApiException(e);
        }
      }
      return executeWs(data, filename);
    } else {
      if (primaryDoc != null && cfg.getSfgExecCopycmd() != null) {
        final String    copyCmd  = cfg.getSfgExecCopycmd().replace("$filename", filename).replace("$file", primaryDoc.getAbsolutePath());
        ExternalProcess copyProc = new ExternalProcess(copyCmd);
        LOGGER.log(Level.INFO, "Copy primary doc with {0}...", copyCmd);
        int rc;
        try {
          rc = copyProc.execute();
          LOGGER.log(Level.FINEST, "Copy rc={0}, output: ", copyProc.getStdout());
        } catch (InterruptedException | IOException e) {
          rc = -1;
        }
        if (rc != 0) {
          LOGGER.log(Level.SEVERE, "Copy failed (rc={0})!\nSTDOUT:\n{1}\nSTDERR:\n{2} ", new Object[] { rc, copyProc.getStdout(), copyProc.getStderr() });
          throw new ApiException("Copy command \"" + copyCmd + "\" failed with RC=" + rc + "!");
        }
      }
      return executeWorkflowLauncher(filename);
    }
  }

  private Workflow executeWorkflowLauncher(String filename) throws ApiException {
    ApiConfig cfg = ApiConfig.getInstance();
    if (cfg.getSfgExecBpCmd() == null || cfg.getSfgExecBpCmd().isEmpty()) {
      throw new ApiException("Parameter " + ApiConfig.EXECBP_CMD + " is not set in config!");
    }
    StringBuilder args = new StringBuilder(" -n ").append(name).append(" -c -s");
    if (wfdVersion > 0) {
      args.append(" -v ").append(wfdVersion);
    }
    if (filename != null) {
      args.append(" -f ");
      if (cfg.getSfgExecBpDir() != null) {
        args.append(cfg.getSfgExecBpDir()).append('/');
      }
      args.append(filename);
    }
    String cmd = cfg.getSfgExecBpCmd();
    if (cmd.contains("@args")) {
      cmd = cmd.replace("@args", args.toString());
    } else {
      cmd += args;
    }
    ExternalProcess ep = new ExternalProcess(cmd);
    LOGGER.log(Level.INFO, "Execute {0}...", cmd);
    int    rc;
    String launcherOutput = "";
    try {
      rc = ep.execute();
      launcherOutput = ep.getStdout().toString();
      LOGGER.log(Level.FINEST, "Execute rc={0}, output: ", launcherOutput);
    } catch (InterruptedException | IOException e) {
      rc = -1;
    }
    if (!launcherOutput.contains("[WorkFlowLauncher]")) {
      LOGGER.log(Level.SEVERE, "Execute failed (rc={0})!\nSTDOUT:\n{1}\nSTDERR:\n{2} ", new Object[] { rc, launcherOutput, ep.getStderr() });
      throw new ApiException("Execute command \"" + ep + "\" failed with RC=" + rc + "!");
    }
    return parseLauncherOutput(launcherOutput);
  }

  private Workflow parseLauncherOutput(String output) throws NumberFormatException, ApiException {
    // 1: "TotalTime" (ms), 2: numberWaits, 3: WF_ID, 4: WFD_NAME, 5: PARENT_ID, 6: req. state, 7: state, 8: result
    final Pattern       wfResult  = Pattern
        .compile("\\[WorkFlowLauncher\\] TotalTime: (\\d+\\(.?s\\)) " + "numberWaits: (\\d+)\\s+Id\\((\\s*\\d+\\s*)\\) \\((\\S+)\\)"
            + "\\s+(?:parent Id \\(\\s*(\\d+)\\s*\\)|())" + "\\s*Requested state: (\\S+) state: (\\S+) < (\\S+) >");
    Map<Long, Workflow> workflows = new LinkedHashMap<>();
    long                parent    = -1L;
    try (Scanner scanner = new Scanner(output)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (!line.startsWith("[WorkFlowLauncher]"))
          continue;
        Matcher m = wfResult.matcher(line);
        if (m.matches()) {
          Workflow wf = Workflow.find(Long.valueOf(m.group(3)), true);
          parent = (parent > 0 ? parent : wf.getWorkFlowId());
          workflows.put(wf.getWorkFlowId(), wf);
          if (m.group(5) != null) {
            wf.setParent(workflows.get(Long.valueOf(m.group(5))));
            wf.getParent().addChildWorkflow(wf);
          }
        }
      }
    }
    return workflows.get(parent);
  }

  private Workflow executeWs(byte[] data, String fileName) throws ApiException {
    Map<Long, Workflow> workflows = new LinkedHashMap<>();
    long                parent    = -1L;
    if (!useWsApi(EXECUTE_WS_API)) {
      throw new ApiException("The " + EXECUTE_WS_API + " API is not implemented or not configured in ApiConfig!");
    }

    String jsonResult = null;
    if (data == null) {
      jsonResult = getJSONFromWsApi(EXECUTE_WS_API, "&json=1&bpname=" + getName() + "&bpversion=" + getWfdVersion(), false);
    } else {
      try {
        StringBuilder sb = new StringBuilder(getWsApiBaseURI());
        sb.append("?api=").append(EXECUTE_WS_API);
        sb.append("&bpname=").append(getName());
        sb.append("&bpversion=").append(getWfdVersion());
        sb.append("&json=1");
        sb.append("&filename=").append(fileName == null || fileName.isEmpty() ? "in.dat" : fileName);
        HttpRequestBase httpRequest = createRequest(RequestType.POST, new URI(sb.toString()), null);
        ((HttpPost) httpRequest).setEntity(new ByteArrayEntity(data));
        try (CloseableHttpResponse response = executeRequest(httpRequest)) {
          if (response.getStatusLine().getStatusCode() != 200) {
            throw new ApiException("POST request " + sb + " failed with RC=" + response.getStatusLine());
          }
          jsonResult = getJSONResponse(response);
        }
      } catch (URISyntaxException | IOException e) {
        throw new ApiException(e);
      }
    }
    if (jsonResult == null || jsonResult.isEmpty()) {
      throw new ApiException("Execution of workflow " + getName() + " (version: " + getWfdVersion() + ") failed (no document returned)!");
    }

    JSONObject json = new JSONObject(jsonResult);
    if (json.has(ERROR_CODE)) {
      throw new ApiException(MessageFormat.format("Could not execute workflow {0}/{1}: errorCode={2}, errorDescription={3}.", getName(), getWfdVersion(),
          json.getInt(ERROR_CODE), json.get(ERROR_DESCRIPTION)));
    }
    JSONArray bps = null;
    if (json.getJSONObject("LAUNCH").get("BP") instanceof JSONArray) {
      bps = json.getJSONObject("LAUNCH").getJSONArray("BP");
    } else {
      bps = new JSONArray();
      bps.put(json.getJSONObject("LAUNCH").getJSONObject("BP"));
    }
    for (int i = 0; i < bps.length(); i++) {
      JSONObject bp = bps.getJSONObject(i);
      Workflow   wf = Workflow.find(bp.getLong("ID"), true);
      parent = (parent > 0 ? parent : wf.getWorkFlowId());
      workflows.put(wf.getWorkFlowId(), wf);
      if (bp.has("PARENT_ID")) {
        wf.setParent(workflows.get(bp.getLong("PARENT_ID")));
        wf.getParent().addChildWorkflow(wf);
      }
    }
    return workflows.get(parent);
  }
}
