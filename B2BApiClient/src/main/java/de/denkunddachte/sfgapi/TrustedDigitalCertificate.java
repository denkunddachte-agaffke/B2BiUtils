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
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.denkunddachte.exception.ApiException;

public class TrustedDigitalCertificate extends AbstractSfgCertificate {
	private final static Logger		LOGGER		= Logger.getLogger(TrustedDigitalCertificate.class.getName());
	protected static final String	SVC_NAME	= "trusteddigitalcertificates";

	public TrustedDigitalCertificate() {
		super();
	}

	public TrustedDigitalCertificate(String certName, String certData) {
		super(certName, certData);
	}

	private TrustedDigitalCertificate(JSONObject json) throws JSONException {
		super();
		this.readJSON(json);
	}

	@Override
	public String getServiceName() {
		return SVC_NAME;
	}

	// static lookup methods:
	public static List<TrustedDigitalCertificate> findAll() throws ApiException {
		return findAll(null);
	}

	public static List<TrustedDigitalCertificate> findAll(String globPattern, String... includeFields) throws ApiException {
		List<TrustedDigitalCertificate> result = new ArrayList<TrustedDigitalCertificate>();
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("offset", 0);
			params.put("includeFields", includeFields);
			params.put("searchFor", (globPattern != null ? globPattern.replace('*', '%') : null));
			while (true) {
				JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
				for (int i = 0; i < jsonObjects.length(); i++) {
					result.add(new TrustedDigitalCertificate(jsonObjects.getJSONObject(i)));
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
	public static TrustedDigitalCertificate find(String certName) throws ApiException {
		TrustedDigitalCertificate result = null;
		JSONObject json = findByKey(SVC_NAME, certName);
		try {
			if (json.has("errorCode")) {
				LOGGER.finer("TrustedDigitalCertificate " + certName + " not found: errorCode=" + json.getInt("errorCode")
						+ ", errorDescription=" + json.get("errorDescription") + ".");
			} else {
				result = new TrustedDigitalCertificate(json);
				LOGGER.finer("Found TrustedDigitalCertificate " + certName + ": " + result);
			}
		} catch (JSONException e) {
			throw new ApiException(e);
		}
		return result;
	}

	public static boolean exists(TrustedDigitalCertificate certificate) throws ApiException {
		return exists(certificate.getId());
	}

	public static boolean exists(String certName) throws ApiException {
		JSONObject json = findByKey(SVC_NAME, certName);
		return json.has(ID_PROPERTY);
	}
}
