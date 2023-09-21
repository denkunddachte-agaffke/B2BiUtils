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
package de.denkunddachte.jpa.az;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.denkunddachte.jpa.AbstractSfgObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.TypedQuery;

/**
 * The persistent class for the AZ_FG_DELIVERY_MBOX database table.
 * 
 */
@Entity(name = "FgSshKeys")
@Table(name = "AZ_FG_SSH_KEYS")
@NamedQuery(name = "FgSshKeys.findAll", query = "SELECT f FROM FgSshKeys f")
@NamedQuery(name = "FgSshKeys.find", query = "SELECT f FROM FgSshKeys f WHERE f.keyId.keyName LIKE :keyName")
public class FgSshKeys extends AbstractSfgObject implements Serializable {
  private static final Logger LOGGER           = Logger.getLogger(FgSshKeys.class.getName());
  private static final long   serialVersionUID = -6422112132541943162L;

  @EmbeddedId
  private FgSshKeyId          keyId;

  @Column(name = "KEY_DATA", nullable = false)
  private String              keyData;

  public FgSshKeys() {
  }

  public FgSshKeys(String keyName, String keyFormat, String keyUsage) {
    super();
    this.keyId = new FgSshKeyId(keyName, keyFormat, keyUsage);
  }

  public FgSshKeyId getId() {
    return this.keyId;
  }

  public void setId(FgSshKeyId id) {
    this.keyId = id;
  }

  public String getKeyName() {
    return keyId.getKeyName();
  }

  public String getKeyFormat() {
    return keyId.getKeyFormat();
  }

  public String getKeyUsage() {
    return keyId.getKeyUsage();
  }

  public String getKeyData() {
    return keyData;
  }

  public void setKeyData(String keyData) {
    this.keyData = keyData;
  }

  public static List<FgSshKeys> findAll(String globPattern, EntityManager em) {
    TypedQuery<FgSshKeys> q = em.createNamedQuery("FgSshKeys.find", FgSshKeys.class);
    q.setParameter("keyName", globPattern.replace('*', '%').replace('?', '_'));
    List<FgSshKeys> result = q.getResultList();
    if (!result.isEmpty()) {
      LOGGER.log(Level.FINEST, "pattern={0}, result.size={1}", new Object[] { globPattern, result.size() });
    } else {
      LOGGER.log(Level.FINEST, "pattern={0} not found.", globPattern);
    }
    return result;
  }

  @Override
  public Map<String, Object> getIdentityFields() {
    final Map<String, Object> idmap = new HashMap<>();
    idmap.put("keyName", keyId.getKeyName());
    return idmap;
  }

  @Override
  public String getShortId() {
    return "[" + keyId.getKeyName() + "]";
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  public String toString() {
    return "FgSshKeys [keyName=" + keyId.getKeyName() + ", keyFormat=" + keyId.getKeyFormat() + ", keyUsage=" + keyId.getKeyUsage() + "]";
  }

  @Embeddable
  public static class FgSshKeyId implements Serializable {
    private static final long serialVersionUID = -8426567278777522740L;
    @Column(name = "KEY_NAME", nullable = false)
    String                    keyName;
    @Column(name = "KEY_FORMAT", nullable = false)
    String                    keyFormat;
    @Column(name = "KEY_USAGE", nullable = false)
    String                    keyUsage;

    public FgSshKeyId() {
    }

    public FgSshKeyId(String keyName, String keyFormat, String keyUsage) {
      super();
      this.keyName = keyName;
      this.keyFormat = keyFormat;
      this.keyUsage = keyUsage;
    }

    public String getKeyName() {
      return keyName;
    }

    public String getKeyFormat() {
      return keyFormat;
    }

    public String getKeyUsage() {
      return keyUsage;
    }

    public Map<String, Object> getIdentityFields() {
      final Map<String, Object> idmap = new HashMap<>();
      idmap.put("keyName", keyName);
      idmap.put("keyFormat", keyFormat);
      idmap.put("keyUsage", keyUsage);
      return idmap;
    }

    @Override
    public int hashCode() {
      return Objects.hash(keyFormat, keyName, keyUsage);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (!(obj instanceof FgSshKeyId))
        return false;
      FgSshKeyId other = (FgSshKeyId) obj;
      return Objects.equals(keyFormat, other.keyFormat) && Objects.equals(keyName, other.keyName) && Objects.equals(keyUsage, other.keyUsage);
    }
  }

  @Override
  public boolean pointsToSame(AbstractSfgObject obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof FgSshKeys))
      return false;
    FgSshKeys other = (FgSshKeys) obj;
    return Objects.equals(keyData, other.keyData);
  }

  @Override
  public String getKey() {
    if (keyId == null) {
      return null;
    } else {
      return keyId.keyName + "/" + keyId.keyFormat + "/" + keyId.keyUsage;
    }
  }
}
