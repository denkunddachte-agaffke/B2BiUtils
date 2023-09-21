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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.eclipse.persistence.annotations.CascadeOnDelete;

import de.denkunddachte.enums.OSType;
import de.denkunddachte.jpa.AbstractSfgObject;

/**
 * The persistent class for the AZ_FG_TRANSFER database table.
 * 
 */
@Entity
@Table(name = "AZ_FG_TRANSFER")
@NamedQuery(name = "FgTransfer.findAll", query = "SELECT f FROM FgTransfer f")
@NamedQuery(name = "FgTransfer.findByProducer", query = "SELECT f FROM FgTransfer f WHERE f.producer.customerId = :producer")
public class FgTransfer extends AbstractSfgObject implements Serializable {
  private static final long   serialVersionUID = 1L;
  private static final Logger LOGGER           = Logger.getLogger(FgTransfer.class.getName());

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  // @GeneratedValue(generator = "FgTransferId")
  // @SequenceGenerator(name = "FgTransferId", sequenceName = "AZ_SEQ_FG_TRANSFERID", allocationSize = 1)
  @Column(name = "FG_TRANS_ID", unique = true, nullable = false)
  private long                fgTransId;

  @Column(name = "ANTIVIR_CHECK", precision = 1)
  private boolean             antivirCheck;

  @Column(name = "DATAU_ACK", precision = 1)
  private boolean             datauAck;

  @Column(name = "DESCRIPTION", length = 2000)
  private String              description;

  @Column(name = "ENABLED", precision = 1)
  private boolean             enabled;

  @Column(name = "PRIORITY", nullable = false)
  private int                 priority;

  @Column(name = "ADDITIONAL_INFO", length = 2000)
  private String              additionalInfo;

  @Enumerated(EnumType.STRING)
  @Column(name = "PRODUCER_OS", length = 20)
  private OSType              producerOs;

  @Column(name = "RCV_FILEPATTERN", nullable = false, length = 240)
  private String              rcvFilepattern;

  @Column(name = "LAST_ACTIVE", updatable = false, insertable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date                lastActive;

  // OE/DRM specific fields
  @Column(name = "TRIGGER_FILE", length = 255)
  private String              triggerFile;

  @Column(name = "DATA_FILE", length = 255)
  private String              dataFile;

  @Column(name = "SCHEDULE_NAME", nullable = true, length = 100)
  private String              scheduleName;

  @Column(name = "HOSTNAME", nullable = true, length = 50)
  private String              hostname;

  @Column(name = "PORT", nullable = true)
  private int                 port;

  @Column(name = "USERNAME", nullable = true, length = 30)
  private String              username;

  @Column(name = "PASSWORD", length = 30)
  private String              password;

  @Column(name = "SFG_PRIVKEY_ID", length = 100)
  private String              sfgPrivkeyId;

  @Column(name = "SFG_KNOWNHOSTKEY_ID", nullable = true, length = 100)
  private String              sfgKnownhostkeyId;

  @Column(name = "MAX_TRANS_TIME", nullable = true)
  private int                 maxTransferTimeMinutes;

  @Column(name = "CONN_RETRIES", nullable = true)
  private int                 connRetries;

  @Column(name = "CONN_RETRY_INTERVAL", nullable = true)
  private int                 connRetryInterval;

  @Column(name = "CONN_TIMEOUT", nullable = true)
  private int                 connTimeout;

  @Column(name = "CLIENT_ADAPTER_NAME", nullable = true, length = 100)
  private String              clientAdapterName;

  @Column(name = "FETCH_OPTIONS", nullable = true, length = 100)
  private String              fetchOptions;

  @Column(name = "USE_EXTERNAL_CMD", precision = 1)
  private boolean             useExternalCmd;

  @Column(name = "EXTERNAL_CMD", length = 8000)
  private String              externalCmd;

  // bi-directional many-to-one association to FgDelivery
  // CONSTRAINT AZ_FG_DELIVERY_FK1 FOREIGN KEY (FG_TRANS_ID) REFERENCES
  // AZ_FG_TRANSFER(FG_TRANS_ID) ON DELETE CASCADE
  @OneToMany(mappedBy = "fgTransfer", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @CascadeOnDelete
  @OrderBy(value = "fgDeliveryId")
  // @JoinColumn(name = "FG_TRANS_ID", referencedColumnName = "FG_TRANS_ID", nullable = false)
  private List<FgDelivery>    fgDeliveries;

  // bi-directional many-to-one association to FgCustomer
  // CONSTRAINT AZ_FG_TRANSFER_FK1 FOREIGN KEY (PRODUCER_ID) REFERENCES
  // AZ_FG_CUSTOMER(FG_CUST_ID) ON DELETE CASCADE
  @ManyToOne
  @JoinColumn(name = "PRODUCER_ID", nullable = false)
  private FgCustomer          producer;

  public FgTransfer() {
  }

  public FgTransfer(String rcvFilepattern) {
    super();
    this.rcvFilepattern = rcvFilepattern;
    this.fgDeliveries = new ArrayList<>();
    this.priority = 100;
    this.enabled = true;
    this.antivirCheck = true;
    this.datauAck = false;
    this.port = 22;
    this.maxTransferTimeMinutes = 240;
    this.connRetries = 3;
    this.connRetryInterval = 10;
    this.connTimeout = 120;
  }

  public long getFgTransId() {
    return this.fgTransId;
  }

  public void setFgTransId(long fgTransId) {
    this.fgTransId = fgTransId;
  }

  public boolean getAntivirCheck() {
    return this.antivirCheck;
  }

  public void setAntivirCheck(boolean antivirCheck) {
    this.antivirCheck = antivirCheck;
  }

  public boolean getDatauAck() {
    return this.datauAck;
  }

  public void setDatauAck(boolean datauAck) {
    this.datauAck = datauAck;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean getEnabled() {
    return this.enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Date getLastActive() {
    return lastActive;
  }

  public void setLastActive(Date lastActiveTime) {
    lastActive = lastActiveTime;
  }

  public int getPriority() {
    return this.priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public String getAdditionalInfo() {
    return additionalInfo;
  }

  public void setAdditionalInfo(String additionalInfo) {
    this.additionalInfo = additionalInfo;
  }

  public OSType getProducerOs() {
    return this.producerOs;
  }

  public void setProducerOs(OSType producerOs) {
    this.producerOs = producerOs;
  }

  public String getRcvFilepattern() {
    return this.rcvFilepattern;
  }

  public void setRcvFilepattern(String rcvFilepattern) {
    this.rcvFilepattern = rcvFilepattern;
  }

  public String getTriggerFile() {
    return triggerFile;
  }

  public void setTriggerFile(String triggerFile) {
    this.triggerFile = triggerFile;
  }

  public String getDataFile() {
    return dataFile;
  }

  public void setDataFile(String dataFile) {
    this.dataFile = dataFile;
  }

  public String getScheduleName() {
    return scheduleName;
  }

  public void setScheduleName(String scheduleName) {
    this.scheduleName = scheduleName;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isUseExternalCmd() {
    return useExternalCmd;
  }

  public void setUseExternalCmd(boolean useExternalCmd) {
    this.useExternalCmd = useExternalCmd;
  }

  public String getExternalCmd() {
    return externalCmd;
  }

  public void setExternalCmd(String externalCmd) {
    this.externalCmd = externalCmd;
  }

  public String getSfgPrivkeyId() {
    return sfgPrivkeyId;
  }

  public void setSfgPrivkeyId(String sfgPrivkeyId) {
    this.sfgPrivkeyId = sfgPrivkeyId;
  }

  public String getSfgKnownhostkeyId() {
    return sfgKnownhostkeyId;
  }

  public void setSfgKnownhostkeyId(String sfgKnownhostkeyId) {
    this.sfgKnownhostkeyId = sfgKnownhostkeyId;
  }

  public int getMaxTransferTimeMinutes() {
    return maxTransferTimeMinutes;
  }

  public void setMaxTransferTimeMinutes(int maxTransferTimeMinutes) {
    this.maxTransferTimeMinutes = maxTransferTimeMinutes;
  }

  public int getConnRetries() {
    return connRetries;
  }

  public void setConnRetries(int connRetries) {
    this.connRetries = connRetries;
  }

  public int getConnRetryInterval() {
    return connRetryInterval;
  }

  public void setConnRetryInterval(int connRetryInterval) {
    this.connRetryInterval = connRetryInterval;
  }

  public int getConnTimeout() {
    return connTimeout;
  }

  public void setConnTimeout(int connTimeout) {
    this.connTimeout = connTimeout;
  }

  public String getClientAdapterName() {
    return clientAdapterName;
  }

  public void setClientAdapterName(String clientAdapterName) {
    this.clientAdapterName = clientAdapterName;
  }

  public String getFetchOptions() {
    return fetchOptions;
  }

  public void setFetchOptions(String fetchOptions) {
    this.fetchOptions = fetchOptions;
  }

  public List<FgDelivery> getFgDeliveries() {
    List<FgDelivery> result = new ArrayList<>(fgDeliveries);
    return result;
  }

  public FgDelivery getFgDeliveryFor(String consumerId, boolean ignoreCase) {
    FgDelivery fgd = null;
    for (FgDelivery d : fgDeliveries) {
      if (consumerId.equals(d.getConsumer().getCustomerId()) || (ignoreCase && consumerId.equalsIgnoreCase(d.getConsumer().getCustomerId()))) {
        fgd = d;
        break;
      }
    }
    return fgd;
  }

  public void setFgDeliveries(List<FgDelivery> fgDeliveries) {
    this.fgDeliveries.clear();
    for (FgDelivery fgd : fgDeliveries) {
      addFgDelivery(fgd);
    }
  }

  public FgDelivery addFgDelivery(FgDelivery fgDelivery) {
    fgDeliveries.add(fgDelivery);
    fgDelivery.setFgTransfer(this);

    return fgDelivery;
  }

  public boolean removeFgDelivery(FgDelivery fgDelivery) {
    return fgDeliveries.remove(fgDelivery);
    // fgDelivery.setFgTransfer(null);
  }

  public FgCustomer getProducer() {
    return this.producer;
  }

  public void setProducer(FgCustomer producer) {
    this.producer = producer;
  }

  @Override
  public Map<String, Object> getIdentityFields() {
    final Map<String, Object> idmap = new HashMap<>();
    idmap.put("customerId", producer.getIdentityFields());
    idmap.put("rcvFilepattern", rcvFilepattern);
    return idmap;
  }

  @Override
  public String getShortId() {
    return "[" + (producer == null ? "-" : producer.getCustomerId()) + "]" + rcvFilepattern + " [ID=" + fgTransId + "]";
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  public boolean pointsToSame(AbstractSfgObject obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof FgTransfer))
      return false;
    FgTransfer other = (FgTransfer) obj;
    return Objects.equals(clientAdapterName, other.clientAdapterName) && Objects.equals(password, other.password) && port == other.port
        && producer.pointsToSame(other.producer) && Objects.equals(rcvFilepattern, other.rcvFilepattern) && Objects.equals(sfgPrivkeyId, other.sfgPrivkeyId)
        && Objects.equals(username, other.username);
  }

  @Override
  public String getKey() {
    return fgTransId == 0 ? null : Long.toString(fgTransId);
  }
  
  @Override
  public String toString() {
    return "FgTransfer [fgTransId=" + fgTransId + ", producer=" + (producer != null ? producer.getCustomerId() : null) + ", rcvFilepattern=" + rcvFilepattern
        + ", priority=" + priority + ", enabled=" + enabled + ", description=" + description + ", producerOs=" + producerOs + ", antivirCheck=" + antivirCheck
        + ", datauAck=" + datauAck + (useExternalCmd ? ", externalCmd=" + externalCmd : "") + super.toString() + "]";
  }

}
