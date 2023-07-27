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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.denkunddachte.enums.ExecState;
import de.denkunddachte.enums.PersistenceLevel;
import de.denkunddachte.enums.ReportingLevel;
import de.denkunddachte.exception.ApiException;

/**
 * Template for new API 
 * @author chef
 *
 */
public class WorkFlowMonitor extends ApiClient {
  private static final Logger       LOGGER             = Logger.getLogger(WorkFlowMonitor.class.getName());
  protected static final String     SVC_NAME           = "workflowmonitors";
  protected static final String     PD_WS_API           = "processdata";
  

  // JSON fields
  protected static final String     ACTIVITY_INFO_ID   = "activityInfoId";
  protected static final String     ADV_STATUS         = "advStatus";
  protected static final String     ARCHIVE_DATE       = "archiveDate";
  protected static final String     ARCHIVE_FLAG       = "archiveFlag";
  protected static final String     BASIC_STATUS       = "basicStatus";
  protected static final String     BRANCH_ID          = "branchId";
  protected static final String     CONTENT            = "content";
  protected static final String     CONTRACT_ID        = "contractId";
  protected static final String     DEADLINE           = "deadline";
  protected static final String     DOC_ID             = "docId";
  protected static final String     END_TIME           = "endTime";
  protected static final String     ENTERQ             = "enterq";
  protected static final String     EVENT_LEVEL        = "eventLevel";
  protected static final String     EXITQ              = "exitq";
  protected static final String     LIFESPAN           = "lifespan";
  protected static final String     NEXT_AI_ID         = "nextAiId";
  protected static final String     NODE_EXECUTED      = "nodeExecuted";
  protected static final String     ORIG_WFC_ID        = "origWfcId";
  protected static final String     PARENT_WFD_ID      = "parentWfdId";
  protected static final String     PARENT_WFD_VERSION = "parentWfdVersion";
  protected static final String     PERSISTENCE_LEVEL  = "persistenceLevel";
  protected static final String     PREV_WFC_ID        = "prevWfcId";
  protected static final String     SERVICE_NAME       = "serviceName";
  protected static final String     START_TIME         = "startTime";
  protected static final String     STATUS_RPT         = "statusRpt";
  protected static final String     STEP_ID            = "stepId";
  protected static final String     LAST_STEP_ID       = "lastStepId";
  protected static final String     SVC_PARM_VER       = "svcParmVer";
  protected static final String     WFC_ID             = "wfcId";
  protected static final String     WFD_ID             = "wfdId";
  protected static final String     WFD_VERSION        = "wfdVersion";
  protected static final String     WFE_STATUS         = "wfeStatus";
  protected static final String     WFE_STATUS_RPT     = "wfeStatusRpt";
  protected static final String     WORKFLOW_ID        = "workFlowId";
  protected static final String     EXE_STATE          = "exeState";
  protected static final String     WFD_NAME           = "wfdName";
  protected static final String     FMT_PD             = "FMT_PD";
  protected static final String     PROCESS_DATA       = "PROCESS_DATA";
  protected static final String     ID_PROPERTY        = ID;

  // API fields
  private int                       activityInfoId;
  private String                    advStatus;
  private Timestamp                 archiveDate;
  private int                       archiveFlag;
  private int                       basicStatus;
  private String                    branchId;
  private String                    content;
  private String                    contractId;
  private OffsetDateTime            deadline;
  private String                    docId;
  private Timestamp                 endTime;
  private String                    enterq;
  private ReportingLevel            eventLevel;
  private String                    exitq;
  private Long                      lifespan;
  private Integer                   nextAiId;
  private String                    nodeExecuted;
  private String                    origWfcId;
  private Integer                   parentWfdId;
  private Integer                   parentWfdVersion;
  private PersistenceLevel          persistenceLevel;
  private String                    prevWfcId;
  private String                    serviceName;
  private Timestamp                 startTime;
  private String                    statusRpt;
  private Integer                   stepId;
  private Integer                   svcParmVer;
  private String                    wfcId;
  private Integer                   wfdId;
  private Integer                   wfdVersion;
  private Integer                   wfeStatus;
  private String                    wfeStatusRpt;
  private Long                      workFlowId;
  private ExecState                 exeState;
  private String                    wfdName;
  private Hashtable<String, String> stepData;
  private Hashtable<String, String> statusRptData;

  // Constructors
  // Constructor NEW: not implemented because read/action only API

  // Constructor from server
  protected WorkFlowMonitor(JSONObject json) throws JSONException {
    super();
    this.readJSON(json);
  }

  @Override
  public String getId() {
    return getGeneratedId();
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
    throw new JSONException("WorkFlowMonitor cannot be created");
  }

  @Override
  protected ApiClient readJSON(JSONObject json) throws JSONException {
    super.init(json);
    this.activityInfoId = json.getInt(ACTIVITY_INFO_ID);
    this.advStatus = json.getString(ADV_STATUS);
    if (json.has(ARCHIVE_DATE)) {
      this.archiveDate = Timestamp.valueOf(json.getString(ARCHIVE_DATE));
    }
    this.archiveFlag = json.getInt(ARCHIVE_FLAG);
    this.basicStatus = json.getInt(BASIC_STATUS);
    this.branchId = json.getString(BRANCH_ID);
    this.content = json.optString(CONTENT);
    this.contractId = json.optString(CONTRACT_ID);
    this.deadline = toOffsetDateTime(json.getString(DEADLINE));
    this.docId = json.optString(DOC_ID);
    this.endTime = Timestamp.valueOf(json.getString(END_TIME));
    this.enterq = json.optString(ENTERQ);
    this.eventLevel = ReportingLevel.getByCode(json.getInt(EVENT_LEVEL));
    this.exitq = json.optString(EXITQ);
    this.lifespan = json.getLong(LIFESPAN);
    this.nextAiId = json.optInt(NEXT_AI_ID);
    this.nodeExecuted = json.optString(NODE_EXECUTED);
    this.origWfcId = json.optString(ORIG_WFC_ID);
    this.parentWfdId = json.optInt(PARENT_WFD_ID);
    this.parentWfdVersion = json.optInt(PARENT_WFD_VERSION);
    if (json.has(PERSISTENCE_LEVEL)) {
      this.persistenceLevel = PersistenceLevel.getByCode(json.getInt(PERSISTENCE_LEVEL));
    }
    this.prevWfcId = json.optString(PREV_WFC_ID);
    this.serviceName = json.getString(SERVICE_NAME);
    this.startTime = Timestamp.valueOf(json.getString(START_TIME));
    this.statusRpt = json.optString(STATUS_RPT);
    this.stepId = json.getInt(STEP_ID);
    this.svcParmVer = json.getInt(SVC_PARM_VER);
    this.wfcId = json.getString(WFC_ID);
    this.wfdId = json.getInt(WFD_ID);
    this.wfdVersion = json.optInt(WFD_VERSION);
    this.wfeStatus = json.optInt(WFE_STATUS);
    this.wfeStatusRpt = json.optString(WFE_STATUS_RPT);
    this.workFlowId = json.getLong(WORKFLOW_ID);
    this.exeState = ExecState.getState(json.getString(EXE_STATE));
    this.wfdName = json.getString(WFD_NAME);
    return this;
  }

  // Getters and setters
  public int getWfdVersion() {
    return wfdVersion;
  }

  public int getWfdId() {
    return wfdId;
  }

  public String getWfdKey() {
    return wfdName + "/" + wfdVersion;
  }

  public int getActivityInfoId() {
    return activityInfoId;
  }

  public String getAdvStatus() {
    return advStatus;
  }

  public Timestamp getArchiveDate() {
    return archiveDate;
  }

  public Integer getArchiveFlag() {
    return archiveFlag;
  }

  public Integer getBasicStatus() {
    return basicStatus;
  }

  public String getBranchId() {
    return branchId;
  }

  public String getContent() {
    return content;
  }

  public String getContractId() {
    return contractId;
  }

  public OffsetDateTime getDeadline() {
    return deadline;
  }

  public String getDocId() {
    return docId;
  }

  public Timestamp getEndTime() {
    return endTime;
  }

  public String getEnterq() {
    return enterq;
  }

  public ReportingLevel getEventLevel() {
    return eventLevel;
  }

  public String getExitq() {
    return exitq;
  }

  public Long getLifespan() {
    return lifespan;
  }

  public Integer getNextAiId() {
    return nextAiId;
  }

  public String getNodeExecuted() {
    return nodeExecuted;
  }

  public String getOrigWfcId() {
    return origWfcId;
  }

  public Integer getParentWfdId() {
    return parentWfdId;
  }

  public Integer getParentWfdVersion() {
    return parentWfdVersion;
  }

  public PersistenceLevel getPersistenceLevel() {
    return persistenceLevel;
  }

  public String getPrevWfcId() {
    return prevWfcId;
  }

  public Timestamp getStartTime() {
    return startTime;
  }

  public String getStatusRpt() throws ApiException {
    getStepData();
    if (statusRptData != null) {
      return statusRptData.get("Status_Report");
    }
    return null;
  }

  public Integer getStepId() {
    return stepId;
  }

  public Integer getSvcParmVer() {
    return svcParmVer;
  }

  public String getWfcId() {
    return wfcId;
  }

  public Integer getWfeStatus() {
    return wfeStatus;
  }

  public String getWfeStatusRpt() {
    return wfeStatusRpt;
  }

  public Long getWorkFlowId() {
    return workFlowId;
  }

  public ExecState getExeState() {
    return exeState;
  }

  public String getWfdName() {
    return wfdName;
  }

  public String getStepName() {
    return this.serviceName;
  }

  public String getProcessData() throws ApiException {
    getStepData();

    if (stepData.containsKey(PROCESS_DATA) && !stepData.containsKey(FMT_PD)) {
      stepData.put(PROCESS_DATA, xmlToString(parseXml(stepData.get(PROCESS_DATA)), 2));
      stepData.put(FMT_PD, "0");
    }
    return stepData.get(PROCESS_DATA);
  }

  private void getStepData() throws ApiException {
    if (this.stepData == null) {
      if (!useWsApi(PD_WS_API)) {
        throw new ApiException("The " + PD_WS_API + " API is not implemented or not configured in ApiConfig!");
      }
      try {
        JSONObject json = getJSON(getJSONFromWsApi(PD_WS_API, "&wfcid=" + wfcId, false));
        this.stepData = deserializeObject(json.getString(PROCESS_DATA));
        this.statusRptData = deserializeObject(json.optString("STATUS_RPT"));
      } catch (JSONException e) {
        throw new ApiException(e);
      }
    }
    if (this.stepData == null) {
      this.stepData = new Hashtable<>();
    }
  }

  @SuppressWarnings("unchecked")
  private Hashtable<String, String> deserializeObject(String base64Data) throws ApiException {
    if (isNullOrEmpty(base64Data)) {
      return null;
    }
    final ByteArrayOutputStream os = new ByteArrayOutputStream();
    Object o = null;
    try {
      if (base64Data.startsWith("H4s")) {
        byte[] buf = new byte[8096];
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(base64Data)))) {
          int i;
          while ((i = gis.read(buf)) != -1) {
            os.write(buf, 0, i);
          }
        }
      } else {
        os.write(Base64.getDecoder().decode(base64Data.getBytes()));
      }
      try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(os.toByteArray()))) {
        o = ois.readObject();
      }
    } catch (IOException | ClassNotFoundException e) {
      throw new ApiException(e);
    }
    if (o instanceof Hashtable<?, ?>) {
      return (Hashtable<String, String>) o;
    } else {
      throw new ApiException("Unexpected object type: " + o.getClass().getName() + "! Expect Hashtable<?,?>.");
    }
  }

  public String toDumpString() {
    return "WorkFlowMonitor [activityInfoId=" + activityInfoId + ", advStatus=" + advStatus + ", archiveDate=" + archiveDate + ", archiveFlag=" + archiveFlag
        + ", basicStatus=" + basicStatus + ", branchId=" + branchId + ", content=" + content + ", contractId=" + contractId + ", deadline=" + deadline
        + ", docId=" + docId + ", endTime=" + endTime + ", enterq=" + enterq + ", eventLevel=" + eventLevel + ", exitq=" + exitq + ", lifespan=" + lifespan
        + ", nextAiId=" + nextAiId + ", nodeExecuted=" + nodeExecuted + ", origWfcId=" + origWfcId + ", parentWfdId=" + parentWfdId + ", parentWfdVersion="
        + parentWfdVersion + ", persistenceLevel=" + persistenceLevel + ", prevWfcId=" + prevWfcId + ", serviceName=" + serviceName + ", startTime=" + startTime
        + ", statusRpt=" + statusRpt + ", stepId=" + stepId + ", svcParmVer=" + svcParmVer + ", wfcId=" + wfcId + ", wfdId=" + wfdId + ", wfdVersion="
        + wfdVersion + ", wfeStatus=" + wfeStatus + ", wfeStatusRpt=" + wfeStatusRpt + ", workFlowId=" + workFlowId + ", exeState=" + exeState + ", wfdName="
        + wfdName + "]";
  }

  @Override
  public String toString() {
    return "Step [Id=" + stepId + ", serviceName=" + serviceName + ", basicStatus=" + basicStatus + ", advStatus=" + advStatus + ", startTime=" + startTime
        + ", endTime=" + endTime + ", exeState=" + exeState + "]";
  }

  public static List<WorkFlowMonitor> find(Long workflowId, boolean withDetails) throws ApiException {
    List<WorkFlowMonitor> result = new ArrayList<>();
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("offset", 0);
      params.put("fieldList", (withDetails ? "Full" : "Brief"));
      params.put(WORKFLOW_ID, String.valueOf(workflowId));
      while (true) {
        JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
        for (int i = 0; i < jsonObjects.length(); i++) {
          result.add(new WorkFlowMonitor(jsonObjects.getJSONObject(i)));
        }
        if (jsonObjects.length() < API_RANGESIZE)
          break;
      }
    } catch (JSONException | UnsupportedEncodingException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public static WorkFlowMonitor find(String stepId) throws ApiException {
    WorkFlowMonitor result = null;
    JSONObject json = findByKey(SVC_NAME, stepId);
    try {
      if (json.has(ERROR_CODE)) {
        LOGGER.log(Level.FINER, "WorkFlowMonitor {0} not found: errorCode={1}, errorDescription={2}.",
            new Object[] { stepId, json.getInt(ERROR_CODE), json.get(ERROR_DESCRIPTION) });
      } else {
        result = new WorkFlowMonitor(json);
        LOGGER.log(Level.FINER, "Found WorkFlowMonitor {0}: {1}", new Object[] { stepId, result });
      }
    } catch (JSONException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public static boolean exists(WorkFlowMonitor wfStep) throws ApiException {
    return exists(wfStep.getId());
  }

  public static boolean exists(String stepId) throws ApiException {
    JSONObject json = findByKey(SVC_NAME, stepId);
    return json.has(ID_PROPERTY);
  }
}
