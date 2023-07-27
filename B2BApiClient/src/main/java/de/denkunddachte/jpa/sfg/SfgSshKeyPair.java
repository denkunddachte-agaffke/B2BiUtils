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
package de.denkunddachte.jpa.sfg;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.eclipse.persistence.annotations.ReadOnly;

/**
 * The persistent class for the SSH_KEY_PAIR database table.
 * 
 */
@Deprecated
@Entity
@Table(name = "SSH_KEY_PAIR")
@NamedQuery(name = "SfgSshKeyPair.findAll", query = "SELECT f FROM SfgSshKeyPair f")
@NamedQuery(name = "SfgSshKeyPair.findByName", query = "SELECT f FROM SfgSshKeyPair f WHERE f.name LIKE :name")
@ReadOnly
public class SfgSshKeyPair implements Serializable {
	private static final long	serialVersionUID	= 1L;

	@Id
	@Column(name = "OBJECT_ID", unique = true, nullable = false, length = 255)
	private String				objectId;

	@Column(name = "NAME", nullable = false, length = 64)
	private String				name;

	@Column(name = "USERNAME", nullable = false, length = 255)
	private String				username;

	@Column(name = "CREATE_DATE", nullable = false)
	@Temporal(TemporalType.DATE)
	private Date				createDate;

	@Column(name = "KEY_TYPE", length = 64)
	private String				keyType;

	@Column(name = "STATUS")
	private int					status;

	@Column(name = "KEY_PASSPHRASE", length = 255)
	private String				keyPassphrase;

	@Column(name = "RAW_STORE", length = 255)
	private String				rawStore;

	@Column(name = "FINGER_PRINT", length = 255)
	private String				fingerPrint;

	@Column(name = "MODIFIED_DATE", nullable = false)
	@Temporal(TemporalType.DATE)
	private Date				modifiedDate;

	@Column(name = "MODIFIED_BY", length = 255)
	private String				modifiedBy;

	@Column(name = "KEY_COMMENT", length = 255)
	private String				keyComment;

	public SfgSshKeyPair() {
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getKeyType() {
		return keyType;
	}

	public void setKeyType(String keyType) {
		this.keyType = keyType;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getKeyPassphrase() {
		return keyPassphrase;
	}

	public void setKeyPassphrase(String keyPassphrase) {
		this.keyPassphrase = keyPassphrase;
	}

	public String getRawStore() {
		return rawStore;
	}

	public void setRawStore(String rawStore) {
		this.rawStore = rawStore;
	}

	public String getFingerPrint() {
		return fingerPrint;
	}

	public void setFingerPrint(String fingerPrint) {
		this.fingerPrint = fingerPrint;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getKeyComment() {
		return keyComment;
	}

	public void setKeyComment(String keyComment) {
		this.keyComment = keyComment;
	}

	@Override
	public String toString() {
		return "SfgSshKeyPair [objectId=" + objectId + ", name=" + name + ", fingerPrint=" + fingerPrint + ", keyComment="
				+ keyComment + "]";
	}

}
