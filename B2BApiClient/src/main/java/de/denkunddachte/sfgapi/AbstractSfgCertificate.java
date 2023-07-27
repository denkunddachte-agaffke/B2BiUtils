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

public abstract class AbstractSfgCertificate extends ApiClient {

	public enum CertificateType {
		keyCert, pkcs12
	}

	protected static final String	ID_PROPERTY	= "certName";
	private String					certName;
	private String					certData;
	private String					systemCertId;
	private String					createdOrUpdatedBy;
	private String					creationOrUpdateTime;
	private boolean					verifyValidity;
	private boolean					verifyAuthChain;

	protected AbstractSfgCertificate() {
		super();
	}

	protected AbstractSfgCertificate(String certName, String certData) {
		super();
		this.certName = certName;
		this.certData = certData;
	}

	@Override
	public String getId() {
    return getGeneratedId() == null ? certName : getGeneratedId();
	}

	@Override
	public String getIdProperty() {
		return ID_PROPERTY;
	}

	public String getCertName() {
		return certName;
	}

	public void setCertName(String certName) {
		this.certName = certName;
	}

	public String getCertData() {
		return certData;
	}

	public void setCertData(String certData) {
		this.certData = certData;
	}

	public String getSystemCertId() {
		return systemCertId;
	}

	public String getCreatedOrUpdatedBy() {
		return createdOrUpdatedBy;
	}

	public void setCreatedOrUpdatedBy(String createdOrUpdatedBy) {
		this.createdOrUpdatedBy = createdOrUpdatedBy;
	}

	public String getCreationOrUpdateTime() {
		return creationOrUpdateTime;
	}

	public void setCreationOrUpdateTime(String creationOrUpdateTime) {
		this.creationOrUpdateTime = creationOrUpdateTime;
	}

	public boolean isVerifyValidity() {
		return verifyValidity;
	}

	public void setVerifyValidity(boolean verifyValidity) {
		this.verifyValidity = verifyValidity;
	}

	public boolean isVerifyAuthChain() {
		return verifyAuthChain;
	}

	public void setVerifyAuthChain(boolean verifyAuthChain) {
		this.verifyAuthChain = verifyAuthChain;
	}

	@Override
	protected AbstractSfgCertificate readJSON(JSONObject json) throws JSONException {
		super.init(json);
		this.certName = json.getString("certName");
		this.certData = json.getString("certData");
		this.systemCertId = json.getString("systemCertId");
		this.createdOrUpdatedBy = json.getString("createdOrUpdatedBy");
		this.creationOrUpdateTime = json.getString("creationOrUpdateTime");
		this.verifyValidity = json.getJSONObject("verifyValidity").getBoolean("code");
		this.verifyAuthChain = json.getJSONObject("verifyAuthChain").getBoolean("code");
		return this;
	}

	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("certName", certName);
		json.put("certData", certData);
		json.put("verifyValidity", verifyValidity);
		json.put("verifyAuthChain", verifyAuthChain);
		return json;
	}

	@Override
	public String toString() {
		return "AbstractSfgCertificate [certName=" + certName + ", certData=" + certData + ", createdOrUpdatedBy="
				+ createdOrUpdatedBy + ", creationOrUpdateTime=" + creationOrUpdateTime + ", verifyValidity=" + verifyValidity
				+ ", verifyAuthChain=" + verifyAuthChain + "]";
	}
}
