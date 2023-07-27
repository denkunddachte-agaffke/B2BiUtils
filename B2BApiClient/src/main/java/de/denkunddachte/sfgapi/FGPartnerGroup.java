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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.denkunddachte.exception.ApiException;

public class FGPartnerGroup extends ApiClient {
	private final static Logger		LOGGER		= Logger.getLogger(FGPartnerGroup.class.getName());
	protected static final String	SVC_NAME	= "partnergroups";
	protected static final String	ID_PROPERTY	= "groupName";
	private String					groupName;
	private final Set<String>		members		= new HashSet<String>();;

	public FGPartnerGroup() {
		super();
	}

	public FGPartnerGroup(String name) {
		super();
		this.groupName = name;
	}

	private FGPartnerGroup(JSONObject json) throws JSONException {
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
		if (!members.isEmpty()) {
			json.put("groupMembers", String.join(",", members));
		}
		return json;
	}

	@Override
	protected FGPartnerGroup readJSON(JSONObject json) throws JSONException {
		super.init(json);
		this.groupName = json.getString("groupName");
		if (json.has("groupMembers"))
			setMembers(json.getString("groupMembers"));
		return this;
	}

	public String getGroupName() {
		return groupName;
	}

	public Collection<String> getMembers() {
		Collection<String> result = new HashSet<String>(members);
		return result;
	}

	public void setMembers(String members) {
		setMembers(Arrays.asList((members == null ? "" : members).split("\\s*,\\s*")));
	}

	public void setMembers(Collection<String> members) {
		this.members.clear();
		this.members.addAll(members);
	}

	public boolean addMember(String memberName) {
		return members.add(memberName);
	}

	public boolean removeMember(String memberName) {
		return members.remove(memberName);
	}

	// static lookup methods:
	public static List<FGPartnerGroup> findAll() throws ApiException {
		return findAll(null);
	}

	public static List<FGPartnerGroup> findAll(String globPattern, String... includeFields) throws ApiException {
		List<FGPartnerGroup> result = new ArrayList<FGPartnerGroup>();
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("offset", 0);
			params.put("includeFields", includeFields);
			params.put("searchFor", (globPattern != null ? globPattern.replace('*', '%') : null));
			while (true) {
				JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
				for (int i = 0; i < jsonObjects.length(); i++) {
					result.add(new FGPartnerGroup(jsonObjects.getJSONObject(i)));
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
	public static FGPartnerGroup find(String groupName) throws ApiException {
		FGPartnerGroup result = null;
		JSONObject json = findByKey(SVC_NAME, groupName);
		try {
			if (json.has("errorCode")) {
				LOGGER.finer("FGPartnerGroup " + groupName + " not found: errorCode=" + json.getInt("errorCode")
						+ ", errorDescription=" + json.get("errorDescription") + ".");
			} else {
				result = new FGPartnerGroup(json);
				LOGGER.finer("Found FGPartnerGroup " + groupName + ": " + result);
			}
		} catch (JSONException e) {
			throw new ApiException(e);
		}
		return result;
	}

	public static boolean exists(FGPartnerGroup group) throws ApiException {
		return exists(group.getId());
	}

	public static boolean exists(String groupName) throws ApiException {
		JSONObject json = findByKey(SVC_NAME, groupName);
		return json.has(ID_PROPERTY);
	}

	@Override
	public String toString() {
		return "FGPartnerGroup [groupName=" + groupName + ", members=" + members + "]";
	}
}
