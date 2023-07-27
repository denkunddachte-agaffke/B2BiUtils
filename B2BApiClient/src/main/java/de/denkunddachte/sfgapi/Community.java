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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.denkunddachte.exception.ApiException;

public class Community extends ApiClient {
	private final static Logger			LOGGER			= Logger.getLogger(Community.class.getName());

	protected static final String		SVC_NAME		= "communities";
	protected static final String		ID_PROPERTY		= "name";
	private String						name;
	private boolean						partnersInitiateConnections;
	private boolean						partnersListenForConnections;
	private boolean						ftpListening;
	private boolean						cdListening;
	private boolean						sshListening;
	private boolean						wsListening;
	private boolean						partnerNotificationsEnabled;
	private final Collection<String>	customProtocols	= new ArrayList<String>();

	public Community() {
		super();
	}

	public Community(String name, boolean partnersInitiateConnections, boolean partnersListenForConnections) {
		super();
		this.name = name;
		this.partnersInitiateConnections = partnersInitiateConnections;
		this.partnersListenForConnections = partnersListenForConnections;

	}

	private Community(JSONObject json) throws JSONException {
		super();
		this.readJSON(json);
	}

	@Override
	public String getId() {
		return getGeneratedId() == null ? name : getGeneratedId();
	}

	@Override
	public String getIdProperty() {
		return ID_PROPERTY;
	}

	@Override
	public String getServiceName() {
		return SVC_NAME;
	}

	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("name", name);
		json.put("partnersInitiateConnections", partnersInitiateConnections);
		json.put("partnersListenForConnections", partnersListenForConnections);
		json.put("ftpListening", ftpListening);
		json.put("cdListening", cdListening);
		json.put("sshListening", sshListening);
		json.put("wsListening", wsListening);
		json.put("partnerNotificationsEnabled", partnerNotificationsEnabled);
		if (!customProtocols.isEmpty()) {
			json.put("customProtocols", String.join(",", customProtocols));
		}
		return json;
	}

	@Override
	protected Community readJSON(JSONObject json) throws JSONException {
		super.init(json);
		this.name = json.getString("name");
		this.partnersInitiateConnections = json.getJSONObject("partnersInitiateConnections").getBoolean("code");
		this.partnersListenForConnections = json.getJSONObject("partnersListenForConnections").getBoolean("code");
		this.ftpListening = json.getJSONObject("ftpListening").getBoolean("code");
		this.cdListening = json.getJSONObject("cdListening").getBoolean("code");
		this.sshListening = json.getJSONObject("sshListening").getBoolean("code");
		this.wsListening = json.getJSONObject("wsListening").getBoolean("code");
		this.partnerNotificationsEnabled = json.getJSONObject("partnerNotificationsEnabled").getBoolean("code");
		if (json.has("customProtocols"))
			setCustomProtocols(json.getString("customProtocols"));
		return this;
	}

	public String getName() {
		return name;
	}

	public boolean isPartnersInitiateConnections() {
		return partnersInitiateConnections;
	}

	public void setPartnersInitiateConnections(boolean partnersInitiateConnections) {
		this.partnersInitiateConnections = partnersInitiateConnections;
	}

	public boolean isPartnersListenForConnections() {
		return partnersListenForConnections;
	}

	public void setPartnersListenForConnections(boolean partnersListenForConnections) {
		this.partnersListenForConnections = partnersListenForConnections;
	}

	public boolean isFtpListening() {
		return ftpListening;
	}

	public void setFtpListening(boolean ftpListening) {
		this.ftpListening = ftpListening;
	}

	public boolean isCdListening() {
		return cdListening;
	}

	public void setCdListening(boolean cdListening) {
		this.cdListening = cdListening;
	}

	public boolean isSshListening() {
		return sshListening;
	}

	public void setSshListening(boolean sshListening) {
		this.sshListening = sshListening;
	}

	public boolean isWsListening() {
		return wsListening;
	}

	public void setWsListening(boolean wsListening) {
		this.wsListening = wsListening;
	}

	public boolean isPartnerNotificationsEnabled() {
		return partnerNotificationsEnabled;
	}

	public void setPartnerNotificationsEnabled(boolean partnerNotificationsEnabled) {
		this.partnerNotificationsEnabled = partnerNotificationsEnabled;
	}

	public Collection<String> getCustomProtocols() {
		Collection<String> result = new ArrayList<String>(customProtocols);
		return result;
	}

	public void setCustomProtocols(String customProtocols) {
		setCustomProtocols(Arrays.asList((customProtocols == null ? "" : customProtocols).split("\\s*,\\s*")));
	}

	public void setCustomProtocols(Collection<String> customProtocols) {
		this.customProtocols.clear();
		this.customProtocols.addAll(customProtocols);
	}

	@Override
	public String toString() {
		return "Community [name=" + name + ", partnersInitiateConnections=" + partnersInitiateConnections
				+ ", partnersListenForConnections=" + partnersListenForConnections + ", ftpListening=" + ftpListening
				+ ", cdListening=" + cdListening + ", sshListening=" + sshListening + ", wsListening=" + wsListening
				+ ", partnerNotificationsEnabled=" + partnerNotificationsEnabled + "]";
	}

	public static List<Community> findAll() throws ApiException {
		return findAll(null);
	}

	public static List<Community> findAll(String globPattern, String... includeFields) throws ApiException {
		List<Community> result = new ArrayList<Community>();
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("offset", 0);
			params.put("includeFields", includeFields);
			params.put("commName", (globPattern != null ? globPattern.replace('*', '%') : null));
			while (true) {
				JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
				for (int i = 0; i < jsonObjects.length(); i++) {
					result.add(new Community(jsonObjects.getJSONObject(i)));
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
	public static Community find(String commName) throws ApiException {
		Community result = null;
		JSONObject json = findByKey(SVC_NAME, commName);
		try {
			if (json.has("errorCode")) {
				LOGGER.finer("Community " + commName + " not found: errorCode=" + json.getInt("errorCode") + ", errorDescription="
						+ json.get("errorDescription") + ".");
			} else {
				result = new Community(json);
				LOGGER.finer("Found Community " + commName + ": " + result);
			}
		} catch (JSONException e) {
			throw new ApiException(e);
		}
		return result;
	}

	public static boolean exists(Community community) throws ApiException {
		return exists(community.getId());
	}

	public static boolean exists(String commName) throws ApiException {
		JSONObject json = findByKey(SVC_NAME, commName);
		return json.has(ID_PROPERTY);
	}
}
