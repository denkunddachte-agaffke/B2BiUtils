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

import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * CdConfiguration is referenced by TradingPartner API.
 * @author chef
 *
 */
public class CdConfiguration {
	private static final Logger LOGGER = Logger.getLogger(CdConfiguration.class.getName());

	public enum Role {
		PRODUCER, CONSUMER
	}

	private Role	role;
	private int		checkpointInterval;
	private String	localNodeName;
	private String	localUserId;
	private String	remoteDisposition;
	private String	remoteFileName;
	private String	remoteNodeName;
	private String	remoteSysopts;
	private String	remoteUserId;
	private String	operatingSystem;

	public CdConfiguration(Role role) {
		super();
		this.role = role;
	}

	public CdConfiguration(Role role, JSONObject json) throws JSONException {
		this(role);
		LOGGER.finest("New CdConfiguration, role=" + role + ", json=" + json);
		this.checkpointInterval = json.optInt("checkpointInterval");
		this.localNodeName = json.getString("localNodeName");
		this.localUserId = json.optString("localUserId");
		this.remoteNodeName = json.getString("remoteNodeName");
		this.remoteSysopts = json.optString("remoteSysopts");
		this.remoteUserId = json.optString("remoteUserId");

		if (role == Role.CONSUMER) {
			this.remoteDisposition = json.optString("remoteDisposition");
			this.remoteFileName = json.optString("remoteFileName");
		} 
		if (role == Role.PRODUCER) {
			this.operatingSystem = json.getString("operatingSystem");
		} 
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject result = new JSONObject();
		result.put("checkpointInterval", checkpointInterval);
		result.put("localNodeName", localNodeName);
		result.put("localUserId", localUserId);
		result.put("remoteNodeName", remoteNodeName);
		result.put("remoteSysopts", remoteSysopts);
		result.put("remoteUserId", remoteUserId);

		if (role == Role.PRODUCER) {
			result.put("remoteDisposition", remoteDisposition);
			result.put("remoteFileName", remoteFileName);
		}
		if (role == Role.PRODUCER) {
			result.put("operatingSystem", operatingSystem);
		}
		return result;
	}

	public Role getRole() {
		return role;
	}

	public int getCheckpointInterval() {
		return checkpointInterval;
	}

	public void setCheckpointInterval(int checkpointInterval) {
		this.checkpointInterval = checkpointInterval;
	}

	public String getLocalNodeName() {
		return localNodeName;
	}

	public void setLocalNodeName(String localNodeName) {
		this.localNodeName = localNodeName;
	}

	public String getLocalUserId() {
		return localUserId;
	}

	public void setLocalUserId(String localUserId) {
		this.localUserId = localUserId;
	}

	public String getRemoteDisposition() {
		return remoteDisposition;
	}

	public void setRemoteDisposition(String remoteDisposition) {
		this.remoteDisposition = remoteDisposition;
	}

	public String getRemoteFileName() {
		return remoteFileName;
	}

	public void setRemoteFileName(String remoteFileName) {
		this.remoteFileName = remoteFileName;
	}

	public String getRemoteNodeName() {
		return remoteNodeName;
	}

	public void setRemoteNodeName(String remoteNodeName) {
		this.remoteNodeName = remoteNodeName;
	}

	public String getRemoteSysopts() {
		return remoteSysopts;
	}

	public void setRemoteSysopts(String remoteSysopts) {
		this.remoteSysopts = remoteSysopts;
	}

	public String getRemoteUserId() {
		return remoteUserId;
	}

	public void setRemoteUserId(String remoteUserId) {
		this.remoteUserId = remoteUserId;
	}

	public String getOperatingSystem() {
		return operatingSystem;
	}

	public void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}

	@Override
	public String toString() {
		return "CdConfiguration [role=" + role + ", checkpointInterval=" + checkpointInterval + ", localNodeName=" + localNodeName
				+ ", localUserId=" + localUserId + ", remoteDisposition=" + remoteDisposition + ", remoteFileName="
				+ remoteFileName + ", remoteNodeName=" + remoteNodeName + ", remoteSysopts=" + remoteSysopts + ", remoteUserId="
				+ remoteUserId + ", operatingSystem=" + operatingSystem + "]";
	}
}
