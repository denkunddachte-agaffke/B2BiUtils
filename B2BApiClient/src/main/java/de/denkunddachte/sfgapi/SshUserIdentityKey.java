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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.denkunddachte.exception.ApiException;

public class SshUserIdentityKey extends AbstractSfgKey {
  protected static final String SVC_NAME = "sshuseridentitykeys";

  private String                passPhrase;
  private String                privateKeyData;
  private String                base64CodedPrivKey;

  public SshUserIdentityKey() {
    super();
  }

  public SshUserIdentityKey(String keyName, String privateKeyData, String passPhrase, boolean keyStatusEnabled) throws InvalidKeyException {
    super();
    this.keyName = keyName;
    this.keyStatusEnabled = keyStatusEnabled;
    this.passPhrase = passPhrase;
    this.privateKeyData = privateKeyData;
  }

  public SshUserIdentityKey(String keyName, File sshPrivateKeyFile, String passPhrase, boolean keyStatusEnabled) throws IOException, InvalidKeyException {
    super();
    this.keyName = keyName;
    this.keyStatusEnabled = keyStatusEnabled;
    try (InputStream is = new FileInputStream(sshPrivateKeyFile)) {
      byte[] data = new byte[(int) sshPrivateKeyFile.length()];
      is.read(data);
      this.base64CodedPrivKey = Base64.getEncoder().encodeToString(data);
    }
    this.passPhrase = passPhrase;
  }

  private SshUserIdentityKey(JSONObject json) throws JSONException, ApiException {
    super();
    this.readJSON(json);
  }

  @Override
  public String getServiceName() {
    return SVC_NAME;
  }

  @Override
  public JSONObject toJSON() throws JSONException {
    JSONObject json = super.toJSON();
    if (base64CodedPrivKey == null) {
      throw new IllegalStateException("SSH key file data required for creating key entry.");
    }
    json.put("privateKeyData", base64CodedPrivKey);
    json.put("passPhrase", passPhrase);
    return json;
  }

  @Override
  protected SshUserIdentityKey readJSON(JSONObject json) throws JSONException, ApiException {
    super.readJSON(json);
    this.passPhrase = json.optString("passPhrase");
    this.privateKeyData = json.getString("privateKeyData");
    try {
      setSshKey(new String(Base64.getDecoder().decode(json.getString("publicKeyData"))));
    } catch (InvalidKeyException ike) {
      throw new ApiException("Could not decode identity key " + this.getKeyName() + ": " + ike.getMessage(), ike);
    }
    return this;
  }

  public String getPassPhrase() {
    return passPhrase;
  }

  public void setPassPhrase(String passPhrase) {
    this.passPhrase = passPhrase;
  }

  public String getPrivateKeyData() {
    return privateKeyData;
  }

  public void setPrivateKeyData(String privateKeyData) {
    this.privateKeyData = privateKeyData;
  }

  @Override
  public String toString() {
    return "SshUserIdentityKey [passPhrase=" + passPhrase + ", privateKeyData=" + privateKeyData + ", toString()=" + super.toString() + "]";
  }

  // static lookup methods:
  public static List<SshUserIdentityKey> findAll() throws ApiException {
    return findAll(null);
  }

  public static List<SshUserIdentityKey> findAll(String globPattern, String... includeFields) throws ApiException {
    List<SshUserIdentityKey> result = new ArrayList<SshUserIdentityKey>();
    try {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("offset", 0);
      params.put("includeFields", includeFields);
      params.put("searchFor", (globPattern != null ? globPattern.replace('*', '%') : null));
      while (true) {
        JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
        for (int i = 0; i < jsonObjects.length(); i++) {
          result.add(new SshUserIdentityKey(jsonObjects.getJSONObject(i)));
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
  public static SshUserIdentityKey find(String keyName) throws ApiException {
    SshUserIdentityKey result = null;
    JSONObject         json   = findByKey(SVC_NAME, keyName);
    try {
      if (!json.has("errorCode")) {

        result = new SshUserIdentityKey(json);
      }
    } catch (JSONException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public static boolean exists(SshUserIdentityKey key) throws ApiException {
    return exists(key.getId());
  }

  public static boolean exists(String keyName) throws ApiException {
    JSONObject json = findByKey(SVC_NAME, keyName);
    return json.has(ID_PROPERTY);
  }
}
