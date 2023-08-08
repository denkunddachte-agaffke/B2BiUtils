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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TypedQuery;

import de.denkunddachte.ft.FTProcessChain;
import de.denkunddachte.jpa.AbstractSfgObject;

/**
 * The persistent class for the AZ_FG_FILETYPE database table.
 * 
 */
@Entity
@Table(name = "AZ_FG_FILETYPE")
@NamedQuery(name = "FgFiletype.findAll", query = "SELECT f FROM FgFiletype f ORDER BY f.fgFiletypeId")
@NamedQuery(name = "FgFiletype.findByName", query = "SELECT f FROM FgFiletype f WHERE UPPER(f.filetype) LIKE UPPER(:name)")
public class FgFiletype extends AbstractSfgObject implements Serializable {
  private static final long   serialVersionUID = 1L;
  private static final Logger LOGGER           = Logger.getLogger(FgFiletype.class.getName());

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  // @GeneratedValue(generator = "FgTypeId")
  // @SequenceGenerator(name = "FgTypeId", sequenceName = "AZ_SEQ_FG_FILETYPEID", allocationSize = 1)
  @Column(name = "FG_FILETYPE_ID", unique = true, nullable = false)
  private long                fgFiletypeId;

  @Column(name = "DESCRIPTION", length = 2000)
  private String              description;

  @Column(name = "FILETYPE", nullable = false, length = 20)
  private String              filetype;

  @Column(name = "PROCESS_CMD_1", length = 2000)
  private String              processCmd1;

  @Column(name = "PROCESS_CMD_2", length = 2000)
  private String              processCmd2;

  @Column(name = "PROCESS_CMD_3", length = 2000)
  private String              processCmd3;

  @Column(name = "PROCESS_CMD_4", length = 2000)
  private String              processCmd4;

  @Column(name = "MAX_RUNTIME_SECONDS")
  private int                 maxRuntimeSeconds;

  // bi-directional many-to-one association to FgDelivery
  // CONSTRAINT AZ_FG_DELIVERY_FK3 FOREIGN KEY (FG_FILETYPE_ID) REFERENCES
  // AZ_FG_FILETYPE(FG_FILETYPE_ID)
  @OneToMany(mappedBy = "fgFiletype", fetch = FetchType.LAZY)
  private List<FgDelivery>    fgDeliveries;

  public FgFiletype() {
  }

  public FgFiletype(FTProcessChain processChain) {
    this.filetype = processChain.getName();
    int i = processChain.getProcessingCommands().size();
    if (i > 0)
      processCmd1 = processChain.getProcessingCommand(0);
    if (i > 1)
      processCmd2 = processChain.getProcessingCommand(1);
    if (i > 2)
      processCmd3 = processChain.getProcessingCommand(2);
    if (i > 3)
      processCmd4 = processChain.getProcessingCommand(3);
  }

  public long getFgFiletypeId() {
    return this.fgFiletypeId;
  }

  public void setFgFiletypeId(long fgFiletypeId) {
    this.fgFiletypeId = fgFiletypeId;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getFiletype() {
    return this.filetype;
  }

  public void setFiletype(String filetype) {
    this.filetype = filetype;
  }

  public String getProcessCmd1() {
    return this.processCmd1;
  }

  public void setProcessCmd1(String processCmd1) {
    this.processCmd1 = processCmd1;
  }

  public String getProcessCmd2() {
    return this.processCmd2;
  }

  public void setProcessCmd2(String processCmd2) {
    this.processCmd2 = processCmd2;
  }

  public String getProcessCmd3() {
    return this.processCmd3;
  }

  public void setProcessCmd3(String processCmd3) {
    this.processCmd3 = processCmd3;
  }

  public String getProcessCmd4() {
    return this.processCmd4;
  }

  public void setProcessCmd4(String processCmd4) {
    this.processCmd4 = processCmd4;
  }

  public int getMaxRuntimeSeconds() {
    return maxRuntimeSeconds;
  }

  public void setMaxRuntimeSeconds(int maxRuntimeSeconds) {
    this.maxRuntimeSeconds = maxRuntimeSeconds;
  }

  public List<FgDelivery> getFgDeliveries() {
    fgDeliveries.size();
    return new ArrayList<>(fgDeliveries);
  }

  public void setFgDeliveries(List<FgDelivery> fgDeliveries) {
    this.fgDeliveries.clear();
    for (FgDelivery fgd : fgDeliveries) {
      addFgDelivery(fgd);
    }
  }

  public FgDelivery addFgDelivery(FgDelivery fgDelivery) {
    getFgDeliveries().add(fgDelivery);
    fgDelivery.setFgFiletype(this);

    return fgDelivery;
  }

  public boolean removeFgDelivery(FgDelivery fgDelivery) {
    fgDelivery.setFgFiletype(null);
    return getFgDeliveries().remove(fgDelivery);
  }

  public static FgFiletype find(String name, EntityManager em) {
    FgFiletype fgt = null;
    TypedQuery<FgFiletype> q = em.createNamedQuery("FgFiletype.findByName", FgFiletype.class);
    q.setParameter("name", name);
    List<FgFiletype> result = q.getResultList();
    if (!result.isEmpty()) {
      fgt = result.get(0);
      LOGGER.log(Level.FINEST, "FgFiletype name={0}, fgt={1}", new Object[] { name, fgt });
    } else {
      LOGGER.log(Level.FINEST, "FgFiletype name={0} not found.", name);
    }
    return fgt;
  }

  public static List<FgFiletype> findAll(String regexPattern, EntityManager em) {
    TypedQuery<FgFiletype> q = em.createNamedQuery("FgFiletype.findAll", FgFiletype.class);
    List<FgFiletype> result = q.getResultList();
    if (regexPattern != null) {
      return result.stream().filter(fgt -> fgt.getFiletype().matches(regexPattern)).collect(Collectors.toList());
    } else {
      return result;
    }
  }

  @Override
  public Map<String, Object> getIdentityFields() {
    final Map<String, Object> idmap = new HashMap<>();
    idmap.put("filetype", filetype);
    return idmap;
  }

  @Override
  public String getShortId() {
    return filetype + " [ID=" + fgFiletypeId + "]";
  }

  @Override
  public boolean pointsToSame(AbstractSfgObject obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof FgFiletype))
      return false;
    FgFiletype other = (FgFiletype) obj;
    return Objects.equals(filetype, other.filetype);
  }

  @Override
  public String getKey() {
    return fgFiletypeId == 0 ? null : Long.toString(fgFiletypeId);
  }
  
  @Override
  public String toString() {
    return "FgFiletype [fgFiletypeId=" + fgFiletypeId + ", description=" + description + ", filetype=" + filetype + ", processCmd1=" + processCmd1
        + ", processCmd2=" + processCmd2 + ", processCmd3=" + processCmd3 + ", processCmd4=" + processCmd4 + ", maxRuntimeSeconds=" + maxRuntimeSeconds
        + super.toString() + "]";
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

}
