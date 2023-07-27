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
@Deprecated
@Entity
@Table(name = "FG_ROUTCHAN_TMPL")
@NamedQuery(name = "SfgRoutingChannelTemplate.findAll",	query = "SELECT f FROM SfgRoutingChannelTemplate f")
@NamedQuery(name = "SfgRoutingChannelTemplate.findByName",query = "SELECT f FROM SfgRoutingChannelTemplate f WHERE f.tmplName = :tmplName")
@ReadOnly
public class SfgRoutingChannelTemplate implements Serializable {

	private static final long	serialVersionUID	= 1L;

	@Id
	@Column(name = "ROUTCHAN_TMPL_KEY", unique = true, nullable = false, length = 24)
	private String				routchanTmplKey;

	@Column(name = "TMPL_NAME", nullable = false, length = 255)
	private String				tmplName;

	@Column(name = "PV_MBX_PATTERN", nullable = true, length = 255)
	private String				pvMbxPattern;

	@Column(name = "CONSID_TYPE", nullable = false, length = 10)
	private String				considType			= "STATIC";

	@Column(name = "BP_NAME", nullable = true, length = 255)
	private String				bpName;

	@Column(name = "BP_CONS_NAME_XPATH", nullable = true, length = 255)
	private String				bpConsNameXpath;

	@Column(name = "SUBST_MODE", nullable = true, length = 255)
	private String				substMode;

	@Column(name = "SUBST_FROM", nullable = true, length = 255)
	private String				substFrom;

	@Column(name = "SUBST_TO", nullable = true, length = 255)
	private String				substTo;

	@Column(name = "LOCKID", nullable = false)
	private int					lockid;

	@Column(name = "CREATETS", nullable = false)
	@Temporal(TemporalType.DATE)
	private Date				createts;

	@Column(name = "MODIFYTS", nullable = false)
	@Temporal(TemporalType.DATE)
	private Date				modifyts;

	@Column(name = "CREATEUSERID", nullable = false, length = 40)
	private String				createuserid;

	@Column(name = "MODIFYUSERID", nullable = false, length = 40)
	private String				modifyuserid;

	@Column(name = "CREATEPROGID", nullable = false, length = 40)
	private String				createprogid;
	
	@Column(name = "MODIFYPROGID", nullable = false, length = 40)
	private String				modifyprogid;

	public SfgRoutingChannelTemplate() {
	}

	public String getRoutchanTmplKey() {
		return routchanTmplKey;
	}

	public String getTmplName() {
		return tmplName;
	}

	public String getPvMbxPattern() {
		return pvMbxPattern;
	}

	public String getConsidType() {
		return considType;
	}

	public String getBpName() {
		return bpName;
	}

	public String getBpConsNameXpath() {
		return bpConsNameXpath;
	}

	public String getSubstMode() {
		return substMode;
	}

	public String getSubstFrom() {
		return substFrom;
	}

	public String getSubstTo() {
		return substTo;
	}

	public int getLockid() {
		return lockid;
	}

	public Date getCreatets() {
		return createts;
	}

	public Date getModifyts() {
		return modifyts;
	}

	public String getCreateuserid() {
		return createuserid;
	}

	public String getModifyuserid() {
		return modifyuserid;
	}

	public String getCreateprogid() {
		return createprogid;
	}

	public String getModifyprogid() {
		return modifyprogid;
	}

	@Override
	public String toString() {
		return "SfgRoutingChannelTemplate [routchanTmplKey=" + routchanTmplKey + ", tmplName=" + tmplName + ", pvMbxPattern="
				+ pvMbxPattern + ", createts=" + createts + ", createuserid=" + createuserid + ", modifyts=" + modifyts
				+ ", modifyuserid=" + modifyuserid + "]";
	}
}
