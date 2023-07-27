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

import de.denkunddachte.enums.PermType;
import de.denkunddachte.exception.ApiException;

public class Permission extends ApiClient {
	private final static Logger		LOGGER		= Logger.getLogger(Permission.class.getName());
	protected static final String	SVC_NAME	= "permissions";
	protected static final String	ID_PROPERTY	= "permissionName";

	private String					permissionId;
	private String					permissionName;
	private PermType				permissionType;

	public Permission() {
		super();
	}

	public Permission(String permissionId, String permissionName, PermType permissionType) {
		super();
		this.permissionId = permissionId;
		this.permissionName = permissionName;
		this.permissionType = permissionType;
	}

	private Permission(JSONObject json) throws JSONException {
		super();
		this.readJSON(json);
	}

	@Override
	public String getId() {
    return getGeneratedId() == null ? permissionName : getGeneratedId();
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
		json.put("permissionName", permissionName);
		json.put("permissionId", permissionId);
		json.put("permissionType", permissionType.getPermTypeCode());
		return json;
	}

	@Override
	protected ApiClient readJSON(JSONObject json) throws JSONException {
		super.init(json);
		this.permissionId = json.getString("permissionId");
		this.permissionName = json.getString("permissionName");
		return this;
	}

	public String getPermissionId() {
		return permissionId;
	}

	public void setPermissionId(String permissionId) {
		this.permissionId = permissionId;
	}

	public String getPermissionName() {
		return permissionName;
	}

	public PermType getPermissionType() {
		return permissionType;
	}

	public void setPermissionType(PermType permissionType) {
		this.permissionType = permissionType;
	}

	@Override
	public String toString() {
		return "Permission [permissionId=" + permissionId + ", permissionName=" + permissionName + "]";
	}

	// static lookup methods:
	public static List<Permission> findAll() throws ApiException {
		return findAll(null);
	}

	public static List<Permission> findAll(String globPattern, String... includeFields) throws ApiException {
		List<Permission> result = new ArrayList<Permission>();
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("offset", 0);
			params.put("includeFields", includeFields);
			params.put("searchFor", (globPattern != null ? globPattern.replace('*', '%') : null));
			while (true) {
				JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
				for (int i = 0; i < jsonObjects.length(); i++) {
					result.add(new Permission(jsonObjects.getJSONObject(i)));
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
	public static Permission find(String permissionName) throws ApiException {
		Permission result = null;
		JSONObject json = findByKey(SVC_NAME, permissionName);
		try {
			if (json.has("errorCode")) {
				LOGGER.finer("Permission " + permissionName + " not found: errorCode=" + json.getInt("errorCode")
						+ ", errorDescription=" + json.get("errorDescription") + ".");
			} else {
				result = new Permission(json);
				LOGGER.finer("Found Permission " + permissionName + ": " + result);
			}
		} catch (JSONException e) {
			throw new ApiException(e);
		}
		return result;
	}

	public static boolean exists(Permission permission) throws ApiException {
		return exists(permission.getId());
	}

	public static boolean exists(String permissionName) throws ApiException {
		JSONObject json = findByKey(SVC_NAME, permissionName);
		return json.has(ID_PROPERTY);
	}
}
