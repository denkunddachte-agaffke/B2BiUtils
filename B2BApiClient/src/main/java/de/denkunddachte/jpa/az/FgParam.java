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
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.denkunddachte.jpa.AbstractSfgObject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.TypedQuery;

/**
 * The persistent class for the AZ_FG_PARAM database table.
 * 
 */
@Entity
@Table(name = "AZ_FG_PARAM")
@NamedQuery(name = "FgParam.findAll", query = "SELECT f FROM FgParam f ORDER BY f.paramName")
@NamedQuery(name = "FgParam.findByName", query = "SELECT f FROM FgParam f WHERE UPPER(f.paramName) = UPPER(:name)")
public class FgParam extends AbstractSfgObject implements Serializable {
  private static final long   serialVersionUID = 1L;
  private static final Logger LOGGER           = Logger.getLogger(FgParam.class.getName());

  @Id
  @Column(name = "PARAM_NAME", unique = true, nullable = false)
  private String              paramName;

  @Column(name = "PARAM_VALUE", length = 100, nullable = true)
  private String              paramValue;

  public FgParam() {
  }

  public FgParam(String name, String value) {
    this.paramName = name;
    this.paramValue = value;
  }

  public String getParamName() {
    return paramName;
  }

  public void setParamName(String paramName) {
    this.paramName = paramName;
  }

  public String getParamValue() {
    return paramValue;
  }

  public void setParamValue(String paramValue) {
    this.paramValue = paramValue;
  }

  public void setInt(Integer val) {
    if (val != null) {
      this.paramValue = Integer.toString(val);
    } else {
      this.paramName = null;
    }
  }

  public Integer getInt() {
    if (this.paramValue != null) {
      return Integer.valueOf(this.paramValue);
    }
    return null;
  }

  public void setLong(Long val) {
    if (val != null) {
      this.paramValue = Long.toString(val);
    } else {
      this.paramName = null;
    }
  }

  public Long getLong() {
    if (this.paramValue != null) {
      return Long.valueOf(this.paramValue);
    }
    return null;
  }

  public void setDate(Date val) {
    if (val != null) {
      this.paramValue = fmtDate.format(val);
    } else {
      this.paramName = null;
    }
  }

  public Date getDate() throws ParseException {
    if (this.paramValue != null) {
      return fmtDate.parse(this.paramValue);
    }
    return null;
  }

  public void setDateTime(Date val) {
    if (val != null) {
      this.paramValue = fmtDateTime.format(val);
    } else {
      this.paramName = null;
    }
  }

  public Date getDateTime() throws ParseException {
    if (this.paramValue != null) {
      return fmtDateTime.parse(this.paramValue);
    }
    return null;
  }

  public void setTimestamp(Timestamp val) {
    if (val != null) {
      this.paramValue = fmtTimestamp.format(val);
    } else {
      this.paramName = null;
    }
  }

  public Timestamp getTimestamp() {
    if (this.paramValue != null) {
      return Timestamp.valueOf(this.paramValue);
    }
    return null;
  }

  public void setString(String val) {
    this.paramValue = val;
  }

  public String getString() {
    return this.paramValue;
  }

  public static FgParam find(String paramName, EntityManager em) {
    FgParam fgp = null;
    TypedQuery<FgParam> q = em.createNamedQuery("FgParam.findByName", FgParam.class);
    q.setParameter("name", paramName);
    List<FgParam> result = q.getResultList();
    if (!result.isEmpty()) {
      fgp = result.get(0);
      LOGGER.log(Level.FINEST, "FgParam name={0}, fgt={1}", new Object[] { paramName, fgp });
    } else {
      LOGGER.log(Level.FINEST, "FgParam name={0} not found.", paramName);
    }
    return fgp;
  }

  public static FgParam findOpt(String paramName, EntityManager em) {
    FgParam fgp = find(paramName, em);
    return fgp == null ? new FgParam(paramName, null) : fgp;
  }

  public static List<FgParam> findAll(EntityManager em) {
    TypedQuery<FgParam> q = em.createNamedQuery("FgParam.findAll", FgParam.class);
    return q.getResultList();
  }

  @Override
  public Map<String, Object> getIdentityFields() {
    final Map<String, Object> idmap = new HashMap<>();
    idmap.put("paramName", paramName);
    return idmap;
  }

  @Override
  public String getShortId() {
    return paramName + "=" + paramValue;
  }

  @Override
  public boolean pointsToSame(AbstractSfgObject obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof FgParam))
      return false;
    FgParam other = (FgParam) obj;
    return Objects.equals(paramName, other.paramName);
  }

  @Override
  public String getKey() {
    return paramName;
  }

  @Override
  public String toString() {
    return "FgParam [paramName=" + paramName + ", paramValue=" + paramValue + "]";
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

}
