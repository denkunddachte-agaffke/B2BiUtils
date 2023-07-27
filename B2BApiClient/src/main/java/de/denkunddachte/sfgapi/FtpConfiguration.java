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

/**
 * FtpConfiguration is referenced by TradingPartner API.
 * @author chef
 *
 */
public class FtpConfiguration {
	public enum ConnectionType {
		ACTIVE, PASSIVE
	}

	public enum Role {
		PRODUCER, CONSUMER
	}

	protected Role			role;
	private String			hostname;
	private String			listenPort;
	private String			username;
	private String			password;
	private ConnectionType	connectionType;
	private int				numberOfRetries;
	private int				retryInterval;
	private String			baseDirectory;
	private String			controlPortRange;
	private String			localPortRange;
	private Boolean			uploadFileUnderTemporaryNameFirst;
	private String			directory;
	private String			siteCommand;

	public FtpConfiguration(Role role, ConnectionType type) {
		this.role = role;
		this.connectionType = type;
	}

	public FtpConfiguration(Role role, JSONObject json) throws JSONException {
		this(role, ConnectionType.valueOf(json.getJSONObject("connectionType").getString("code").toUpperCase()));
		this.hostname = json.getString("hostname");
		this.listenPort = json.getString("listenPort");
		this.username = json.getString("username");
		this.numberOfRetries = json.getInt("numberOfRetries");
		this.retryInterval = json.getInt("retryInterval");

		if (role == Role.PRODUCER) {
			this.directory = json.getString("directory");
			this.siteCommand = json.optString("siteCommand");
		}

		if (role == Role.CONSUMER) {
			this.baseDirectory = json.optString("baseDirectory");
			this.controlPortRange = json.optString("controlPortRange");
			this.localPortRange = json.optString("localPortRange");
			this.uploadFileUnderTemporaryNameFirst = json.getBoolean("uploadFileUnderTemporaryNameFirst");
		}
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("connectionType", connectionType.toString().toLowerCase());
		json.put("hostname", hostname);
		json.put("listenPort", listenPort);
		json.put("numberOfRetries", numberOfRetries);
		json.put("retryInterval", retryInterval);
		json.put("username", username);
		json.put("password", password);

		if (role == Role.PRODUCER) {
			if (directory != null)
				json.put("directory", directory);
			if (siteCommand != null)
				json.put("siteCommand", siteCommand);
		}
		if (role == Role.CONSUMER) {
			if (directory != null)
				json.put("directory", directory);
			if (controlPortRange != null)
				json.put("controlPortRange", controlPortRange);
			if (localPortRange != null)
				json.put("localPortRange", localPortRange);
			if (uploadFileUnderTemporaryNameFirst != null)
				json.put("uploadFileUnderTemporaryNameFirst", uploadFileUnderTemporaryNameFirst);
		}
		return json;
	}

	public Role getRole() {
		return role;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getListenPort() {
		return listenPort;
	}

	public void setListenPort(String listenPort) {
		this.listenPort = listenPort;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public ConnectionType getConnectionType() {
		return connectionType;
	}

	public int getNumberOfRetries() {
		return numberOfRetries;
	}

	public void setNumberOfRetries(int numberOfRetries) {
		this.numberOfRetries = numberOfRetries;
	}

	public int getRetryInterval() {
		return retryInterval;
	}

	public void setRetryInterval(int retryInterval) {
		this.retryInterval = retryInterval;
	}

	public String getBaseDirectory() {
		return baseDirectory;
	}

	public void setBaseDirectory(String baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	public String getControlPortRange() {
		return controlPortRange;
	}

	public void setControlPortRange(String controlPortRange) {
		this.controlPortRange = controlPortRange;
	}

	public String getLocalPortRange() {
		return localPortRange;
	}

	public void setLocalPortRange(String localPortRange) {
		this.localPortRange = localPortRange;
	}

	public Boolean getUploadFileUnderTemporaryNameFirst() {
		return uploadFileUnderTemporaryNameFirst;
	}

	public void setUploadFileUnderTemporaryNameFirst(Boolean uploadFileUnderTemporaryNameFirst) {
		this.uploadFileUnderTemporaryNameFirst = uploadFileUnderTemporaryNameFirst;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getSiteCommand() {
		return siteCommand;
	}

	public void setSiteCommand(String siteCommand) {
		this.siteCommand = siteCommand;
	}

	public void setConnectionType(ConnectionType connectionType) {
		this.connectionType = connectionType;
	}
}
