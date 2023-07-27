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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.denkunddachte.exception.ApiException;

public class CADigitalCertificate extends AbstractSfgCertificate {
	private final static Logger		LOGGER		= Logger.getLogger(CADigitalCertificate.class.getName());
	protected static final String	SVC_NAME	= "cadigitalcertificates";
	private Set<String>				certGroups	= new HashSet<String>();

	public CADigitalCertificate() {
		super();
	}

	public CADigitalCertificate(String certName, String certData) {
		super(certName, certData);
	}

	private CADigitalCertificate(JSONObject json) throws JSONException {
		super();
		this.readJSON(json);
	}

	@Override
	public String getServiceName() {
		return SVC_NAME;
	}

	public Set<String> getCertGroups() {
		return new HashSet<>(certGroups);
	}

	public boolean addCertGroup(String groupName) {
		return certGroups.add(groupName);
	}

	public void clearCertGroups() {
		certGroups.clear();
	}

	@Override
	protected CADigitalCertificate readJSON(JSONObject json) throws JSONException {
		super.readJSON(json);
		certGroups.clear();
		if (json.has("certGroups")) {
			JSONArray a = json.getJSONArray("certGroups");
			for (int i = 0; i < a.length(); i++) {
				certGroups.add(a.getJSONObject(i).getString("certGroupName"));
			}
		}
		return this;
	}

	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		if (!certGroups.isEmpty()) {
			JSONArray a = new JSONArray();
			for (String groupName : certGroups) {
				a.put((new JSONObject()).put("certGroupName", groupName));
			}
		}
		return json;
	}

	@Override
	public String toString() {
		return "CADigitalCertificate [certGroups=" + certGroups + ", toString()=" + super.toString() + "]";
	}

	// static lookup methods:
	public static List<CADigitalCertificate> findAll() throws ApiException {
		return findAll(null);
	}

	public static List<CADigitalCertificate> findAll(String globPattern, String... includeFields) throws ApiException {
		List<CADigitalCertificate> result = new ArrayList<>();
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("offset", 0);
			params.put("includeFields", includeFields);
			params.put("searchFor", (globPattern != null ? globPattern.replace('*', '%') : null));
			while (true) {
				JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
				for (int i = 0; i < jsonObjects.length(); i++) {
					result.add(new CADigitalCertificate(jsonObjects.getJSONObject(i)));
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
	public static CADigitalCertificate find(String certName) throws ApiException {
		CADigitalCertificate result = null;
		JSONObject json = findByKey(SVC_NAME, certName);
		try {
			if (json.has("errorCode")) {
				LOGGER.finer("CADigitalCertificate " + certName + " not found: errorCode=" + json.getInt("errorCode")
						+ ", errorDescription=" + json.get("errorDescription") + ".");
			} else {
				result = new CADigitalCertificate(json);
				LOGGER.finer("Found CADigitalCertificate " + certName + ": " + result);
			}
		} catch (JSONException e) {
			throw new ApiException(e);
		}
		return result;
	}

	public static boolean exists(CADigitalCertificate certificate) throws ApiException {
		return exists(certificate.getId());
	}

	public static boolean exists(String certName) throws ApiException {
		JSONObject json = findByKey(SVC_NAME, certName);
		return json.has(ID_PROPERTY);
	}
}
