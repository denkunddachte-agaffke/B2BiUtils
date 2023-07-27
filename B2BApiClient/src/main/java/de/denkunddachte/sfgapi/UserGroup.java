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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.denkunddachte.exception.ApiException;

public class UserGroup extends ApiClient {
	private final static Logger				LOGGER		= Logger.getLogger(UserGroup.class.getName());
	protected static final String			SVC_NAME	= "usergroups";
	protected static final String			ID_PROPERTY	= "groupName";
	private String							groupName;
	private String							groupID;
	private String							owner;
	private String							entityId;

	private final Map<String, UserGroup>	subgroups	= new LinkedHashMap<String, UserGroup>();
	private final Map<String, Permission>	permissions	= new LinkedHashMap<String, Permission>();

	public UserGroup() {
		super();
	}

	public UserGroup(String groupName, String groupID) {
		super();
		this.groupName = groupName;
		this.groupID = groupID;
	}

	private UserGroup(JSONObject json) throws JSONException {
		super();
		this.readJSON(json);
	}

	@Override
	public String getId() {
    return getGeneratedId() == null ? groupName : getGeneratedId();
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
		json.put("groupName", groupName);
		json.put("groupID", groupID);
		json.put("owner", owner);
		if (entityId != null) {
			json.put("entityId", entityId);
		}
		if (!permissions.isEmpty()) {
			JSONArray a = new JSONArray();
			for (String p : permissions.keySet()) {
				a.put((new JSONObject()).put("name", p));
			}
			json.put("permissions", a);
		}
		if (!subgroups.isEmpty()) {
			JSONArray a = new JSONArray();
			for (String g : subgroups.keySet()) {
				a.put((new JSONObject()).put("name", g));
			}
			json.put("subgroups", a);
		}
		return json;
	}

	@Override
	protected UserGroup readJSON(JSONObject json) throws JSONException {
		super.init(json);
		this.groupName = json.getString("groupName");
		this.groupID = json.optString("groupID");

		this.owner = json.optString("owner");
		this.entityId = json.optString("entityId");
		if (this.entityId != null && this.entityId.trim().isEmpty()) {
			this.entityId = null;
		}
		if (json.has("permissions")) {
			JSONArray perms = json.getJSONArray("permissions");
			for (int i = 0; i < perms.length(); i++) {
				permissions.put(perms.getJSONObject(i).getString("name"), null);
			}
		}

		if (json.has("subgroups")) {
			JSONArray a = json.getJSONArray("subgroups");
			for (int i = 0; i < a.length(); i++) {
				subgroups.put(a.getJSONObject(i).getString("name"), null);
			}
		}

		return this;
	}

	public String getGroupName() {
		return groupName;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public Collection<String> getSubGroupNames() {
		return subgroups.keySet();
	}

	public UserGroup getSubGroup(String groupname) {
		if (subgroups.get(groupname) == null) {
			try {
				subgroups.put(groupname, UserGroup.find(groupname));
			} catch (ApiException e) {
				LOGGER.warning("Could not find subgroup " + groupname + "!");
			}
		}
		return subgroups.get(groupname);
	}

	public Collection<UserGroup> getSubGroups() {
		Collection<UserGroup> result = new ArrayList<UserGroup>(subgroups.size());
		for (String group : subgroups.keySet()) {
			result.add(getSubGroup(group));
		}
		return result;
	}

	public UserGroup addSubGroup(UserGroup group) {
		return subgroups.put(group.getGroupName(), group);
	}

	public Collection<String> getPermissionNames() {
		return permissions.keySet();
	}

	public Permission getPermission(String permname) {
		if (permissions.get(permname) == null) {
			try {
				permissions.put(permname, Permission.find(permname));
			} catch (ApiException e) {
				LOGGER.warning("Could not find permission " + permname + "!");
			}
		}
		return permissions.get(permname);
	}

	public Collection<Permission> getPermission() {
		Collection<Permission> result = new ArrayList<Permission>(permissions.size());
		for (String perm : permissions.keySet()) {
			result.add(getPermission(perm));
		}
		return result;
	}

	public Permission addPermission(Permission perm) {
		return permissions.put(perm.getPermissionName(), perm);
	}

	@Override
	public String toString() {
		return "UserGroup [groupName=" + groupName + "]";
	}

	// static lookup methods:
	public static List<UserGroup> findAll() throws ApiException {
		return findAll(null);
	}

	public static List<UserGroup> findAll(String globPattern, String... includeFields) throws ApiException {
		List<UserGroup> result = new ArrayList<UserGroup>();
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("offset", 0);
			params.put("includeFields", includeFields);
			params.put("searchFor", (globPattern != null ? globPattern.replace('*', '%') : null));
			while (true) {
				JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
				for (int i = 0; i < jsonObjects.length(); i++) {
					result.add(new UserGroup(jsonObjects.getJSONObject(i)));
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
	public static UserGroup find(String groupName) throws ApiException {
		UserGroup result = null;
		JSONObject json = findByKey(SVC_NAME, groupName);
		try {
			if (json.has("errorCode")) {
				LOGGER.finer("UserGroup " + groupName + " not found: errorCode=" + json.getInt("errorCode")
						+ ", errorDescription=" + json.get("errorDescription") + ".");
			} else {
				result = new UserGroup(json);
				LOGGER.finer("Found UserGroup " + groupName + ": " + result);
			}
		} catch (JSONException e) {
			throw new ApiException(e);
		}
		return result;
	}

	public static boolean exists(UserGroup userGroup) throws ApiException {
		return exists(userGroup.getId());
	}

	public static boolean exists(String groupName) throws ApiException {
		JSONObject json = findByKey(SVC_NAME, groupName);
		return json.has(ID_PROPERTY);
	}
}
