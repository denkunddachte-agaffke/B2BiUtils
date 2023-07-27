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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import org.eclipse.persistence.annotations.ReadOnly;


/**
 * The persistent class for the SERVICE_IMPL database table.
 * 
 */
@Deprecated
@Entity
@Table(name="SERVICE_IMPL")
@NamedQueries(value={
@NamedQuery(name="SfgServiceImpl.findAllCustomAdapter", query="SELECT f FROM SfgServiceImpl f WHERE f.serviceName LIKE 'A_\\_%' ESCAPE '\\'"),
@NamedQuery(name="SfgServiceImpl.findCDSA", query="SELECT f FROM SfgServiceImpl f WHERE f.serviceName LIKE 'A_\\_CDSA%' ESCAPE '\\'"),
@NamedQuery(name="SfgServiceImpl.findSFTPCA", query="SELECT f FROM SfgServiceImpl f WHERE f.serviceName LIKE 'A_\\_SFTP\\_C%' ESCAPE '\\'"),
@NamedQuery(name="SfgServiceImpl.findByName", query="SELECT f FROM SfgServiceImpl f WHERE f.serviceName = :serviceName")
})
@ReadOnly
public class SfgServiceImpl implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="SERVICE_NAME", unique=true, nullable=false, length=100)
	private String serviceName;

	@Column(name="CRITERIA", nullable=false, length=255)
	private String criteria;

	@Column(name="IMPL_TYPE", nullable=false)
	private int implType;

	@Column(name="LOOKUP_NAME", nullable=false, length=255)
	private String lookupName;


	public SfgServiceImpl() {
	}


	public String getServiceName() {
		return serviceName;
	}


	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}


	public String getCriteria() {
		return criteria;
	}


	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}


	public int getImplType() {
		return implType;
	}


	public void setImplType(int implType) {
		this.implType = implType;
	}


	public String getLookupName() {
		return lookupName;
	}


	public void setLookupName(String lookupName) {
		this.lookupName = lookupName;
	}
}