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

public class SshConfiguration {
	private String remoteProfile;

	public SshConfiguration(String remoteProfile) {
		this.setRemoteProfile(remoteProfile);

	}

	protected SshConfiguration(JSONObject json) throws JSONException {
		this.remoteProfile = json.getString("remoteProfile");
	}

	public String getRemoteProfile() {
		return remoteProfile;
	}

	public void setRemoteProfile(String remoteProfile) {
		this.remoteProfile = remoteProfile;
	}

	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("remoteProfile", remoteProfile);
		return json;
	}

	@Override
	public String toString() {
		return "SshConfiguration [remoteProfile=" + remoteProfile + "]";
	}

}
