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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.denkunddachte.enums.ExecState;
import de.denkunddachte.exception.ApiException;

/**
 * Template for new API 
 * @author chef
 *
 */
public class Workflow extends ApiClient {
  private static final Logger                     LOGGER      = Logger.getLogger(Workflow.class.getName());
  protected static final String                   SVC_NAME    = "workflowmonitors";
  protected static final String                   WS_API_NAME = "workflows";

  protected static final String                   ID_PROPERTY = WorkFlowMonitor.WORKFLOW_ID;

  // JSON fields
  private final TreeMap<Integer, WorkFlowMonitor> wfSteps     = new TreeMap<>();

  // API fields
  private String                                  branchId;
  private Timestamp                               endTime;
  private Timestamp                               startTime;
  private Integer                                 stepId;
  private Integer                                 wfdId;
  private Integer                                 wfdVersion;
  private Long                                    workFlowId;
  private ExecState                               exeState;
  private String                                  wfdName;
  private int                                     lastStepId;
  private int                                     stepsCounted;
  private Workflow                                parent;

  // execution

  private final Set<Workflow>                     children    = new LinkedHashSet<>();

  // Constructors
  // Constructor NEW: not implemented because read/action only API
  private Workflow() {
    this.workFlowId = -1L;
  }

  // Constructor from server
  private Workflow(WorkFlowMonitor wfStep) throws JSONException {
    super();
    this.workFlowId = wfStep.getWorkFlowId();
  }

  private Workflow(JSONObject json) throws JSONException {
    super();
    this.lastStepId = -1;
    this.stepsCounted = 0;
    readJSON(json);
  }

  @Override
  public String getId() {
    return getGeneratedId() == null ? String.valueOf(workFlowId) : getGeneratedId();
  }

  @Override
  public String getIdProperty() {
    return ID_PROPERTY;
  }

  @Override
  public String getServiceName() {
    return SVC_NAME;
  }

  // getters, delegates to WorkFlowMonitor
  public int getNumberOfSteps() {
    return this.wfSteps.size();
  }

  public Collection<WorkFlowMonitor> getWfSteps() {
    return wfSteps.values();
  }

  public WorkFlowMonitor getWfStep(int id) throws ApiException {
    if (id < 0 || id >= wfSteps.size()) {
      throw new ApiException("No such workflow step: " + id + "!");
    }
    return wfSteps.get(id);
  }

  public String getBranchId() {
    return branchId;
  }

  public Timestamp getEndTime() {
    return endTime;
  }

  public Timestamp getStartTime() {
    return startTime;
  }

  public Integer getStepId() {
    return stepId;
  }

  public Integer getWfdId() {
    return wfdId;
  }

  public Integer getWfdVersion() {
    return wfdVersion;
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

  public int getLastStepId() {
    return lastStepId;
  }

  public int getStepsCounted() {
    return stepsCounted;
  }

  public Collection<WorkFlowMonitor> getWorkflowSteps() throws ApiException {
    if (wfSteps.isEmpty()) {
      for (WorkFlowMonitor step : WorkFlowMonitor.find(this.workFlowId, true)) {
        wfSteps.put(step.getStepId(), step);
      }
    }
    return wfSteps.values();
  }

  public Workflow getParent() {
    return parent;
  }

  protected void setParent(Workflow parent) {
    this.parent = parent;
  }

  public Set<Workflow> getChildren() {
    return children;
  }

  protected void addChildWorkflow(Workflow wf) {
    children.add(wf);
  }

  public boolean restart() throws ApiException {
    return Workflow.restartWorkflow(workFlowId);
  }

  public boolean terminate() throws ApiException {
    return Workflow.terminateWorkflow(workFlowId);
  }

  private void addWorkflowStep(WorkFlowMonitor step) {
    this.wfSteps.put(step.getStepId(), step);
    this.lastStepId = step.getStepId();
    this.endTime = step.getEndTime();
    this.exeState = step.getExeState();
    this.stepsCounted++;
  }

  private void addWorkflowStep(Workflow step) {
    this.lastStepId = step.getStepId();
    this.endTime = step.getEndTime();
    this.exeState = step.getExeState();
    this.stepsCounted++;
  }

  private Workflow addWorkflowStep(JSONObject json) {
    Workflow wf;
    if (json.getLong(WorkFlowMonitor.WORKFLOW_ID) != this.workFlowId) {
      wf = new Workflow(json);
    } else {
      wf = this;
    }
    if (json.has(WorkFlowMonitor.SERVICE_NAME)) {
      wf.addWorkflowStep(new WorkFlowMonitor(json));
    } else {
      wf.addWorkflowStep(new Workflow(json));
    }
    return wf;
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
    this.branchId = json.optString(WorkFlowMonitor.BRANCH_ID);
    this.endTime = Timestamp.valueOf(json.getString(WorkFlowMonitor.END_TIME));
    this.startTime = Timestamp.valueOf(json.getString(WorkFlowMonitor.START_TIME));
    this.stepId = json.getInt(WorkFlowMonitor.STEP_ID);
    this.lastStepId = json.optInt(WorkFlowMonitor.LAST_STEP_ID);
    this.wfdId = json.getInt(WorkFlowMonitor.WFD_ID);
    this.wfdVersion = json.optInt(WorkFlowMonitor.WFD_VERSION);
    this.workFlowId = json.getLong(WorkFlowMonitor.WORKFLOW_ID);
    this.exeState = ExecState.getState(json.getString(WorkFlowMonitor.EXE_STATE));
    this.wfdName = json.getString(WorkFlowMonitor.WFD_NAME);
    return this;
  }

  @Override
  public String toString() {
    return "Workflow [wfdName=" + wfdName + ", wfdVersion=" + wfdVersion + ", startTime=" + startTime + ", endTime=" + endTime + ", wfSteps=" + wfSteps
        + ", stepsCounted=" + stepsCounted + ", exeState=" + exeState + "]";
  }

  // static lookup methods:
  // WS (XML) based API supported
  public static List<Workflow> findAll(String bpName, String startTime, boolean showOnlyFailed, boolean includeSystemWfds) throws ApiException {
    if (useWsApi(WS_API_NAME)) {
      return findAllWithWSApi(bpName, startTime, showOnlyFailed, includeSystemWfds);
    } else {
      throw new ApiException("WS API \"workflows\" not configured!");
    }
  }

  private static List<Workflow> findAllWithWSApi(String bpName, String startTime, boolean showOnlyFailed, boolean includeSystemWfds) throws ApiException {
    List<Workflow> result = new ArrayList<>();
    try {
      String param = "";
      if (bpName != null) {
        param += "&bpname=" + urlEncode(bpName.replace('*', '%'));
      }
      if (includeSystemWfds) {
        param += "&all=1";
      }
      if (showOnlyFailed) {
        param += "&failed=1";
      }
      if (startTime != null) {
        if (startTime.matches("(?i)\\d{14}-\\d{14}|\\d+[hm]")) {
          param += "&starttime=" + startTime;
        } else {
          throw new ApiException("Invalid value (" + startTime + ") for starttime! Use \"yyyyMMddHHmmss-yyyyMMddHHmmss\" or \"\\d+[hm]\".");
        }
      }

      JSONArray jsonObjects = getJSONArray(getJSONFromWsApi(WS_API_NAME, (param.isEmpty() ? null : param), true));
      for (int i = 0; i < jsonObjects.length(); i++) {
        Workflow wf = new Workflow(jsonObjects.getJSONObject(i));
        LOGGER.log(Level.FINER, "Got Workflow: {0}", wf);
        result.add(wf);
      }
    } catch (UnsupportedEncodingException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public static List<Workflow> findAll() throws ApiException {
    return findAll(null, false);
  }

  public static Workflow find(long workflowId) throws ApiException {
    return find(workflowId, true);
  }

  public static Workflow find(long workflowId, boolean loadSteps) throws ApiException {
    List<Workflow> list = Workflow.findAll(workflowId, loadSteps);
    if (list.isEmpty()) {
      return null;
    }
    return list.get(0);
  }

  private static List<Workflow> findAll(Long workflowId, boolean loadSteps) throws ApiException {
    List<Workflow> result        = new ArrayList<>();
    final String[] includeFields = new String[] { "endTime", "startTime", "stepId", "wfdId", "wfdVersion", "workFlowId", "exeState", "wfdName" };
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("offset", 0);
      if (loadSteps) {
        params.put("fieldList", "Full");
      } else {
        params.put("includeFields", includeFields);
      }
      params.put("workFlowId", String.valueOf(workflowId));
      Workflow wf = new Workflow();
      while (true) {
        JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
        for (int i = 0; i < jsonObjects.length(); i++) {
          wf = wf.addWorkflowStep(jsonObjects.getJSONObject(i));
          if (result.isEmpty() || result.get(result.size() - 1).getWorkFlowId() != wf.getWorkFlowId()) {
            result.add(wf);
          }
        }
        if (jsonObjects.length() < API_RANGESIZE)
          break;
      }
    } catch (JSONException | UnsupportedEncodingException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public static boolean exists(Workflow workflow) throws ApiException {
    return exists(workflow.getWorkFlowId());
  }

  public static boolean exists(Long workflowId) throws ApiException {
    return !findAll(workflowId, false).isEmpty();
  }

  // actions
  public static boolean restartWorkflow(Long workflowId) throws ApiException {
    LOGGER.log(Level.FINER, "restartWorkflow(); workflowId={0}", workflowId);
    boolean result = false;
    try {
      JSONObject post = new JSONObject();
      post.put(WorkFlowMonitor.WORKFLOW_ID, workflowId);
      HttpRequestBase req = createRequest(RequestType.POST, getSvcUri(SVC_NAME, null), post.toString());
      req.addHeader(ACCEPT, "application/vnd.ibm.tenx.workflowmonitor.restartworkflowmonitor");

      try (CloseableHttpResponse response = executeRequest(req)) {
        JSONObject json = new JSONObject(getJSONResponse(response));
        result = response.getStatusLine().getStatusCode() == 201;
        if (result) {
          LOGGER.log(Level.FINE, "restartWorkflow: {0}", workflowId);
        } else {
          setApiError(json.getInt(ERROR_CODE), json.getString(ERROR_DESCRIPTION));
          LOGGER.log(Level.WARNING, "Could not restart worklow {0}, errorCode={1}, errorDescription={2}.",
              new Object[] { workflowId, json.getInt(ERROR_CODE), json.get(ERROR_DESCRIPTION) });
        }
      }
    } catch (URISyntaxException | JSONException | IOException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public static boolean terminateWorkflow(Long workflowId) throws ApiException {
    LOGGER.log(Level.FINER, "terminateWorkflow(); workflowId={0}", workflowId);
    boolean result = false;
    try {
      JSONObject post = new JSONObject();
      post.put(WorkFlowMonitor.WORKFLOW_ID, workflowId);
      HttpRequestBase req = createRequest(RequestType.POST, getSvcUri(SVC_NAME, null), post.toString());

      try (CloseableHttpResponse response = executeRequest(req)) {
        JSONObject json = new JSONObject(getJSONResponse(response));
        result = response.getStatusLine().getStatusCode() == 201;
        if (result) {
          LOGGER.log(Level.FINE, "terminateWorkflow: {0}", workflowId);
        } else {
          setApiError(json.getInt(ERROR_CODE), json.getString(ERROR_DESCRIPTION));
          LOGGER.log(Level.WARNING, "Could not terminate worklow {0}, errorCode={1}, errorDescription={2}.",
              new Object[] { workflowId, json.getInt(ERROR_CODE), json.get(ERROR_DESCRIPTION) });
        }
      }
    } catch (URISyntaxException | JSONException | IOException e) {
      throw new ApiException(e);
    }
    return result;
  }
}
