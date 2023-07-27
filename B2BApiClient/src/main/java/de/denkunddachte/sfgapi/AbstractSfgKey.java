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

import java.security.InvalidKeyException;
import java.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import de.denkunddachte.ft.SshKey;

public abstract class AbstractSfgKey extends ApiClient {
  protected static final String ID_PROPERTY = "keyName";
  private String                keyFingerPrint;
  private String                keyId;
  private int                   keyLength;
  protected String              keyName;
  protected boolean             keyStatusEnabled;
  private SshKey                sshKey;

  protected AbstractSfgKey() {
    super();
  }

  protected AbstractSfgKey(String keyName, String keyString, boolean keyStatusEnabled) throws InvalidKeyException {
    super();
    this.keyName = keyName;
    this.sshKey = new SshKey(keyString);
    this.keyStatusEnabled = keyStatusEnabled;
  }

  @Override
  public String getId() {
    return getGeneratedId() == null ? keyName : getGeneratedId();
  }

  @Override
  public String getIdProperty() {
    return ID_PROPERTY;
  }

  @Override
  protected AbstractSfgKey readJSON(JSONObject json) throws JSONException {
    super.init(json);
    this.keyName = json.optString("keyName");
    if (json.has("keyData")) {
      try {
        this.sshKey = new SshKey(new String(Base64.getDecoder().decode(json.getString("keyData"))));
      } catch (Exception ike) {
        ike.printStackTrace();
      }
    }
    if (json.has("keyStatusEnabled"))
      this.keyStatusEnabled = json.getJSONObject("keyStatusEnabled").getBoolean("code");
    this.keyId = json.optString("keyId");
    this.keyFingerPrint = json.optString("keyFingerPrint");
    this.keyLength = json.optInt("keyLength");
    return this;
  }

  public SshKey getSshKey() {
    return sshKey;
  }

  public void setSshKey(SshKey sshKey) {
    this.sshKey = sshKey;
  }

  public void setSshKey(String keyString) throws InvalidKeyException {
    this.sshKey = new SshKey(keyString);
  }

  public String getKeyFingerPrint() {
    return keyFingerPrint;
  }

  public String getKeyId() {
    return keyId;
  }

  public int getKeyLength() {
    return keyLength;
  }

  public String getKeyName() {
    return keyName;
  }

  public boolean isKeyStatusEnabled() {
    return keyStatusEnabled;
  }

  public void setKeyStatusEnabled(boolean keyStatusEnabled) {
    this.keyStatusEnabled = keyStatusEnabled;
  }

  @Override
  public JSONObject toJSON() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("keyName", keyName);
    if (sshKey != null) {
      json.put("keyData", sshKey.getBase64Encoded());
    }
    json.put("keyStatusEnabled", keyStringValue(keyStatusEnabled));
    return json;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((sshKey == null) ? 0 : sshKey.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    AbstractSfgKey other = (AbstractSfgKey) obj;
    if (sshKey == null) {
      if (other.sshKey != null)
        return false;
    } else if (!sshKey.equals(other.sshKey))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "AbstractSfgKey [sshKey=" + sshKey + ", keyFingerPrint=" + keyFingerPrint + ", keyId=" + keyId + ", keyLength=" + keyLength + ", keyName=" + keyName
        + ", keyStatusEnabled=" + keyStatusEnabled + "]";
  }
}
