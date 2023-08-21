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
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import de.denkunddachte.enums.CDBinaryMode;
import de.denkunddachte.enums.FTProtocol;
import de.denkunddachte.enums.FileDisposition;
import de.denkunddachte.jpa.AbstractSfgObject;

/**
 * The persistent class for the AZ_FG_DELIVERY_CD database table.
 * 
 */
@Entity
@Table(name = "AZ_FG_DELIVERY_CD")
@NamedQuery(name = "FgDeliveryCd.findAll", query = "SELECT f FROM FgDeliveryCd f")
public class FgDeliveryCd extends AbstractSfgObject implements Serializable {
  private static final long      serialVersionUID = 1L;
  private static final Logger    LOGGER           = Logger.getLogger(FgDeliveryCd.class.getName());
  public static final FTProtocol PROTOCOL         = FTProtocol.CD;

  @Enumerated(EnumType.STRING)
  @Column(name = "BINARY_MODE", nullable = false, length = 3)
  private CDBinaryMode           binaryMode;

  @Column(name = "CLIENT_ADAPTER_NAME", nullable = false, length = 100)
  private String                 clientAdapterName;

  @Column(name = "SEQUENTIAL_PROCESSING")
  private int                    sequentialProcessingWaitSeconds;

  @Column(name = "CONN_RETRIES", nullable = false)
  private int                    connRetries;

  @Column(name = "CONN_RETRY_INTERVAL", nullable = false)
  private int                    connRetryInterval;

  @Column(name = "DCBOPTS", length = 100)
  private String                 dcbopts;

  @Enumerated(EnumType.STRING)
  @Column(name = "DISPOSITION", nullable = false, length = 20)
  private FileDisposition        disposition;

  @Column(name = "LT_CONN_RETRIES", nullable = false)
  private int                    ltConnRetries;

  @Column(name = "LT_CONN_RETRY_INTERVAL", nullable = false)
  private int                    ltConnRetryInterval;

  @Column(name = "PASSWORD", length = 30)
  private String                 password;

  @Column(name = "REMOTE_NODE", nullable = false, length = 50)
  private String                 remoteNode;

  @Column(name = "RUNJOB", length = 240)
  private String                 runjob;

  @Column(name = "RUNTASK", length = 240)
  private String                 runtask;

  @Column(name = "MAX_RUNTIME_SECONDS")
  private int                    maxRuntimeSeconds;

  @Column(name = "SYSOPTS", length = 2000)
  private String                 sysopts;

  @Column(name = "UC4TRIGGER", length = 8)
  private String                 uc4trigger;

  @Column(name = "USE_PROXY", nullable = false, precision = 1)
  private boolean                useProxy;

  @Column(name = "USERNAME", nullable = false, length = 30)
  private String                 username;

  @Column(name = "LOCAL_XLATE", nullable = false, precision = 1)
  private boolean                localXlate;

  @Column(name = "XLATE_TABLE", length = 100)
  private String                 localXlateTable;

  // bi-directional one-to-one association to FgDelivery
  // CONSTRAINT AZ_FG_TR_CD_FK1 FOREIGN KEY (FG_DELIVERY_ID) REFERENCES
  // AZ_FG_DELIVERY(FG_DELIVERY_ID) ON DELETE CASCADE
  @Id
  @OneToOne
  @JoinColumn(name = "FG_DELIVERY_ID")
  private FgDelivery             fgDelivery;

  public FgDeliveryCd() {
    this.useProxy = false;
    this.disposition = FileDisposition.NEW;
    this.binaryMode = CDBinaryMode.Yes;
    this.localXlate = false;
    this.maxRuntimeSeconds = 1800;
    this.connRetries = 3;
    this.connRetryInterval = 30;
    this.ltConnRetries = 6;
    this.ltConnRetryInterval = 10;
  }

  public FgDeliveryCd(FgDelivery fgd) {
    this();
    this.fgDelivery = fgd;
  }

  public FTProtocol getProtocol() {
    return FTProtocol.CD;
  }

  public CDBinaryMode getBinaryMode() {
    return this.binaryMode;
  }

  public void setBinaryMode(CDBinaryMode binaryMode) {
    if (this.binaryMode != binaryMode)
      this.binaryMode = binaryMode;
  }

  public String getClientAdapterName() {
    return this.clientAdapterName;
  }

  public void setClientAdapterName(String clientAdapterName) {
    this.clientAdapterName = clientAdapterName;
  }

  public int getConnRetries() {
    return this.connRetries;
  }

  public void setConnRetries(int connRetries) {
    this.connRetries = connRetries;
  }

  public int getConnRetryInterval() {
    return this.connRetryInterval;
  }

  public void setConnRetryInterval(int connRetryInterval) {
    this.connRetryInterval = connRetryInterval;
  }

  public String getDcbopts() {
    return this.dcbopts;
  }

  public void setDcbopts(String dcbopts) {
    this.dcbopts = dcbopts;
  }

  public FileDisposition getDisposition() {
    return this.disposition;
  }

  public void setDisposition(FileDisposition disposition) {
    this.disposition = disposition;
  }

  public int getLtConnRetries() {
    return this.ltConnRetries;
  }

  public void setLtConnRetries(int ltConnRetries) {
    this.ltConnRetries = ltConnRetries;
  }

  public int getLtConnRetryInterval() {
    return this.ltConnRetryInterval;
  }

  public void setLtConnRetryInterval(int ltConnRetryInterval) {
    this.ltConnRetryInterval = ltConnRetryInterval;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getRemoteNode() {
    return this.remoteNode;
  }

  public void setRemoteNode(String remoteNode) {
    this.remoteNode = remoteNode;
  }

  public String getRunjob() {
    return this.runjob;
  }

  public void setRunjob(String runjob) {
    this.runjob = runjob;
  }

  public String getRuntask() {
    return this.runtask;
  }

  public void setRuntask(String runtask) {
    this.runtask = runtask;
  }

  public int getSequentialProcessingWaitSeconds() {
    return sequentialProcessingWaitSeconds;
  }

  public void setSequentialProcessingWaitSeconds(int sequentialProcessingWaitSeconds) {
    this.sequentialProcessingWaitSeconds = sequentialProcessingWaitSeconds;
  }

  public int getMaxRuntimeSeconds() {
    return maxRuntimeSeconds;
  }

  public void setMaxRuntimeSeconds(int maxRuntimeSeconds) {
    this.maxRuntimeSeconds = maxRuntimeSeconds;
  }

  public String getSysopts() {
    return this.sysopts;
  }

  public void setSysopts(String sysopts) {
    this.sysopts = sysopts;
  }

  public String getUc4trigger() {
    return uc4trigger;
  }

  public void setUc4trigger(String uc4trigger) {
    this.uc4trigger = uc4trigger;
  }

  public boolean getUseProxy() {
    return this.useProxy;
  }

  public void setUseProxy(boolean useProxy) {
    this.useProxy = useProxy;
  }

  public String getUsername() {
    return this.username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public boolean isLocalXlate() {
    return localXlate;
  }

  public void setLocalXlate(boolean localXlate) {
    this.localXlate = localXlate;
  }

  public String getLocalXlateTable() {
    return localXlateTable;
  }

  public void setLocalXlateTable(String localXlateTable) {
    this.localXlateTable = localXlateTable;
  }

  public FgDelivery getFgDelivery() {
    return this.fgDelivery;
  }

  public void setFgDelivery(FgDelivery fgDelivery) {
    this.fgDelivery = fgDelivery;
  }

  @Override
  public Map<String, Object> getIdentityFields() {
    final Map<String, Object> idmap = new HashMap<>();
    idmap.put("fgDelivery", fgDelivery.getIdentityFields());
    return idmap;
  }

  @Override
  public String getShortId() {
    return "[" + fgDelivery.getShortId() + "]";
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  public boolean pointsToSame(AbstractSfgObject obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof FgDeliveryCd))
      return false;
    FgDeliveryCd other = (FgDeliveryCd) obj;
    return Objects.equals(password, other.password) && Objects.equals(remoteNode, other.remoteNode) && useProxy == other.useProxy
        && Objects.equals(username, other.username) && Objects.equals(clientAdapterName, other.clientAdapterName);
  }

  @Override
  public FgDeliveryCd createCopy() {
    FgDeliveryCd copy = new FgDeliveryCd();
    copy.binaryMode = this.binaryMode;
    copy.clientAdapterName = this.clientAdapterName;
    copy.sequentialProcessingWaitSeconds = this.sequentialProcessingWaitSeconds;
    copy.connRetries = this.connRetries;
    copy.connRetryInterval = this.connRetryInterval;
    copy.dcbopts = this.dcbopts;
    copy.disposition = this.disposition;
    copy.ltConnRetries = this.ltConnRetries;
    copy.ltConnRetryInterval = this.ltConnRetryInterval;
    copy.password = this.password;
    copy.remoteNode = this.remoteNode;
    copy.runjob = this.runjob;
    copy.runtask = this.runtask;
    copy.maxRuntimeSeconds = this.maxRuntimeSeconds;
    copy.sysopts = this.sysopts;
    copy.uc4trigger = this.uc4trigger;
    copy.useProxy = this.useProxy;
    copy.username = this.username;
    copy.localXlate = this.localXlate;
    copy.localXlateTable = this.localXlateTable;
    return copy;
  }

  @Override
  public String getKey() {
    return fgDelivery == null ? null : fgDelivery.getKey();
  }

  @Override
  public String toString() {
    return "FgDeliveryCd [fgDeliveryId=" + fgDelivery.getFgDeliveryId() + ", binaryMode=" + binaryMode + ", clientAdapterName=" + clientAdapterName
        + ", connRetries=" + connRetries + ", connRetryInterval=" + connRetryInterval + ", dcbopts=" + dcbopts + ", disposition=" + disposition
        + ", ltConnRetries=" + ltConnRetries + ", ltConnRetryInterval=" + ltConnRetryInterval + ", password=" + password + ", remoteNode=" + remoteNode
        + ", runjob=" + runjob + ", runtask=" + runtask + ", maxRuntimeSeconds=" + maxRuntimeSeconds + ", sysopts=" + sysopts + ", useProxy=" + useProxy
        + ", username=" + username + ", localXlate=" + localXlate + ", localXlateTable=" + localXlateTable + ", sequentialProcessingWaitSeconds="
        + sequentialProcessingWaitSeconds + super.toString() + "]";
  }

}
