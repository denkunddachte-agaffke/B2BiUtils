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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * FtpsConfiguration is referenced by TradingPartner API.
 * @author chef
 *
 */
public class FtpsConfiguration extends FtpConfiguration {
	public enum EncryptionStrength {
		ALL, STRONG, WEAK
	}

	private EncryptionStrength	encryptionStrength;
	private boolean				useCcc;
	private boolean				useImplicitSsl;
	private final Set<String> caCertificateNames = new LinkedHashSet<String>();
	private final Set<String> assignedCertificateNames = new LinkedHashSet<String>();

	public FtpsConfiguration(Role role, ConnectionType type) {
		super(role, type);
	}

	public FtpsConfiguration(Role role, JSONObject json) throws JSONException {
		super(role, json);
		this.encryptionStrength = getEncryptionStrength(json.getJSONObject("encryptionStrength").getString("code"));
		this.useCcc = json.getJSONObject("useCcc").getBoolean("code");
		this.useImplicitSsl = json.getJSONObject("useImplicitSsl").getBoolean("code");
		
		if (role == Role.PRODUCER && json.has("caCertificates")) {
			setCaCertificateNames(json.getString("caCertificates"));
		}
		if (role == Role.CONSUMER && json.has("assignedCertificates")) {
			setAssignedCertificateNames(json.getString("assignedCertificates"));
		}
	}

	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put("encryptionStrength", encryptionStrength.toString());
		json.put("useCcc", useCcc);
		json.put("useImplicitSsl", useImplicitSsl);
		if (role == Role.PRODUCER && !caCertificateNames.isEmpty())
			json.put("caCertificates", String.join(",", caCertificateNames));
		if (role == Role.CONSUMER && !assignedCertificateNames.isEmpty())
			json.put("assignedCertificates", String.join(",", assignedCertificateNames));

		return json;
	}

	public EncryptionStrength getEncryptionStrength() {
		return encryptionStrength;
	}

	public void setEncryptionStrength(EncryptionStrength encryptionStrength) {
		this.encryptionStrength = encryptionStrength;
	}

	public EncryptionStrength getEncryptionStrength(String stringVal) {
		EncryptionStrength result = null;
		for (EncryptionStrength val : EncryptionStrength.values()) {
			if (val.toString().equalsIgnoreCase(stringVal)) {
				result = val;
				break;
			}
		}
		return result;
	}

	public boolean isUseCcc() {
		return useCcc;
	}

	public void setUseCcc(boolean useCcc) {
		this.useCcc = useCcc;
	}

	public boolean isUseImplicitSsl() {
		return useImplicitSsl;
	}

	public void setUseImplicitSsl(boolean useImplicitSsl) {
		this.useImplicitSsl = useImplicitSsl;
	}

	public Collection<String> getCaCertificateNames() {
		Collection<String> result = new ArrayList<String>(caCertificateNames.size());
		result.addAll(caCertificateNames);
		return result;
	}

	public void setCaCertificateNames(String certNames) {
		setCaCertificateNames(Arrays.asList(certNames.split("\\s*,\\s*")));
	}

	public void setCaCertificateNames(Collection<String> certNames) {
		caCertificateNames.clear();
		caCertificateNames.addAll(certNames);
	}

	public Collection<String> getAssignedCertificateNames() {
		Collection<String> result = new ArrayList<String>(assignedCertificateNames.size());
		result.addAll(assignedCertificateNames);
		return result;
	}
	
	public void setAssignedCertificateNames(String certNames) {
		setAssignedCertificateNames(Arrays.asList(certNames.split("\\s*,\\s*")));
	}

	public void setAssignedCertificateNames(Collection<String> certNames) {
		assignedCertificateNames.clear();
		assignedCertificateNames.addAll(certNames);
	}

}
