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

public class SystemDigitalCertificate extends AbstractSfgCertificate {
	private final static Logger		LOGGER		= Logger.getLogger(SystemDigitalCertificate.class.getName());
	protected static final String	SVC_NAME	= "systemdigitalcertificates";
	private boolean					crlCache;
	private CertificateType certType;
	private String privateKeyPassword;
	private String keyStorePassword;

	public SystemDigitalCertificate() {
		super();
	}

	public SystemDigitalCertificate(CertificateType certType, String certName, String certData, String privateKeyPassword) {
		super(certName, certData);
		this.certType = certType;
		this.privateKeyPassword = privateKeyPassword;
	}

	private SystemDigitalCertificate(JSONObject json) throws JSONException {
		super();
		this.readJSON(json);
	}

	@Override
	public String getServiceName() {
		return SVC_NAME;
	}

	public boolean isCrlCache() {
		return crlCache;
	}

	public void setCrlCache(boolean crlCache) {
		this.crlCache = crlCache;
	}

	public CertificateType getCertType() {
		return certType;
	}

	public String getPrivateKeyPassword() {
		return privateKeyPassword;
	}

	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	@Override
	protected SystemDigitalCertificate readJSON(JSONObject json) throws JSONException {
		super.readJSON(json);
		this.crlCache = json.getJSONObject("crlCache").getBoolean("code");
		return this;
	}

	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put("certType", certType);
		json.put("crlCache", crlCache);
		json.put("keyStorePassword", keyStorePassword);
		json.put("privateKeyPassword", privateKeyPassword);
		return json;
	}

	@Override
	public String toString() {
		return "SystemDigitalCertificate [crlCache=" + crlCache + ", toString()=" + super.toString() + "]";
	}

	// static lookup methods:
	public static List<SystemDigitalCertificate> findAll() throws ApiException {
		return findAll(null);
	}

	public static List<SystemDigitalCertificate> findAll(String globPattern, String... includeFields) throws ApiException {
		List<SystemDigitalCertificate> result = new ArrayList<SystemDigitalCertificate>();
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("offset", 0);
			params.put("includeFields", includeFields);
			params.put("searchFor", (globPattern != null ? globPattern.replace('*', '%') : null));
			while (true) {
				JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
				for (int i = 0; i < jsonObjects.length(); i++) {
					result.add(new SystemDigitalCertificate(jsonObjects.getJSONObject(i)));
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
	public static SystemDigitalCertificate find(String certName) throws ApiException {
		SystemDigitalCertificate result = null;
		JSONObject json = findByKey(SVC_NAME, certName);
		try {
			if (json.has("errorCode")) {
				LOGGER.finer("SystemDigitalCertificate " + certName + " not found: errorCode=" + json.getInt("errorCode")
						+ ", errorDescription=" + json.get("errorDescription") + ".");
			} else {
				result = new SystemDigitalCertificate(json);
				LOGGER.finer("Found SystemDigitalCertificate " + certName + ": " + result);
			}
		} catch (JSONException e) {
			throw new ApiException(e);
		}
		return result;
	}

	public static boolean exists(SystemDigitalCertificate certificate) throws ApiException {
		return exists(certificate.getId());
	}

	public static boolean exists(String certName) throws ApiException {
		JSONObject json = findByKey(SVC_NAME, certName);
		return json.has(ID_PROPERTY);
	}
}
