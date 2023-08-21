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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import de.denkunddachte.enums.FTProtocol;
import de.denkunddachte.ft.Host;
import de.denkunddachte.jpa.AbstractSfgObject;

/**
 * The persistent class for the AZ_FG_DELIVERY_SFTP database table.
 * 
 */
@Entity
@Table(name = "AZ_FG_FETCH_SFTP")
@NamedQuery(name = "FgFetchSftp.findAll", query = "SELECT f FROM FgFetchSftp f")
@NamedQuery(name = "FgFetchSftp.findByProducer", query = "SELECT f FROM FgFetchSftp f WHERE f.producer.customerId = :producer")
public class FgFetchSftp extends AbstractSfgObject implements Serializable {
  private static final long      serialVersionUID = 1L;
  private static final Logger    LOGGER           = Logger.getLogger(FgFetchSftp.class.getName());
  public static final FTProtocol PROTOCOL         = FTProtocol.SFTP;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  // @GeneratedValue(generator = "FgFetchId")
  // @SequenceGenerator(name = "FgFetchId", sequenceName = "AZ_SEQ_FG_FETCHID", allocationSize = 1)
  @Column(name = "FG_FETCH_ID", unique = true, nullable = false)
  private long                   fgFetchId;

  @Column(name = "FILEPATTERN", nullable = false, length = 240)
  private String                 filepattern;

  @Column(name = "SCHEDULE_NAME", nullable = false, length = 100)
  private String                 scheduleName;

  @Column(name = "CLIENT_ADAPTER_NAME", nullable = false, length = 100)
  private String                 clientAdapterName;

  @Column(name = "CONN_RETRIES", nullable = false)
  private int                    connRetries;

  @Column(name = "CONN_RETRY_INTERVAL", nullable = false)
  private int                    connRetryInterval;

  @Column(name = "CONN_TIMEOUT", nullable = false)
  private int                    connTimeout;

  @Column(name = "ENABLED", precision = 1)
  private boolean                enabled;

  @Column(name = "HOSTNAME", nullable = false, length = 50)
  private String                 hostname;

  @Column(name = "PASSWORD", length = 30)
  private String                 password;

  @Column(name = "PORT", nullable = false)
  private int                    port;

  @Column(name = "SFG_KNOWNHOSTKEY_ID", nullable = false, length = 100)
  private String                 sfgKnownhostkeyId;

  @Column(name = "SFG_PRIVKEY_ID", length = 100)
  private String                 sfgPrivkeyId;

  @Column(name = "USERNAME", nullable = false, length = 30)
  private String                 username;

  @Column(name = "LAST_ACTIVE", updatable = false, insertable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date                   lastActive;

  @Column(name = "MAX_TRANS_TIME", nullable = false)
  private int                    maxTransferTimeMinutes;

  @Column(name = "PRIORITY", nullable = true)
  private int                    priority;

  @Column(name = "KEEP_FILE", precision = 1)
  private boolean                keepFile;

  @Column(name = "USE_EXTERNAL_CMD", precision = 1)
  private boolean                useExternalCmd;

  @Column(name = "EXTERNAL_CMD", length = 8000)
  private String                 externalCmd;

  // bi-directional many-to-one association to FgCustomer
  // CONSTRAINT AZ_FG_FTCH_SFTP_FK1 FOREIGN KEY (PRODUCER_ID) REFERENCES
  // AZ_FG_CUSTOMER(FG_CUST_ID) ON DELETE CASCADE
  @ManyToOne
  @JoinColumn(name = "PRODUCER_ID", nullable = false)
  private FgCustomer             producer;

  public FgFetchSftp() {
    this.port = 22;
    this.connRetries = 3;
    this.connRetryInterval = 10;
    this.connTimeout = 120;
    this.maxTransferTimeMinutes = 240;
    this.priority = 100;
    this.enabled = true;
  }

  public FgFetchSftp(String scheduleName, String filepattern) {
    this();
    this.scheduleName = scheduleName;
    this.filepattern = filepattern;
  }

  public long getFgFetchId() {
    return this.fgFetchId;
  }

  public void setFgFetchId(long fgFetchId) {
    this.fgFetchId = fgFetchId;
  }

  public String getFilepattern() {
    return filepattern;
  }

  public void setFilepattern(String filepattern) {
    this.filepattern = filepattern;
  }

  public String getScheduleName() {
    return scheduleName;
  }

  public void setScheduleName(String scheduleName) {
    this.scheduleName = scheduleName;
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

  public int getConnTimeout() {
    return this.connTimeout;
  }

  public void setConnTimeout(int connTimeout) {
    this.connTimeout = connTimeout;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public int getMaxTransferTimeMinutes() {
    return maxTransferTimeMinutes;
  }

  public void setMaxTransferTimeMinutes(int maxTransferTimeMinutes) {
    this.maxTransferTimeMinutes = maxTransferTimeMinutes;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public Date getLastActive() {
    return lastActive;
  }

  public void setLastActive(Date lastActiveTime) {
    lastActive = lastActiveTime;
  }

  public String getHostname() {
    return this.hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public Host getHost() {
    return new Host(this.hostname, this.port);
  }

  public void setHost(Host host) {
    this.hostname = host.getHostname();
    this.port = host.getPort();
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public int getPort() {
    return this.port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getSfgKnownhostkeyId() {
    return this.sfgKnownhostkeyId;
  }

  public void setSfgKnownhostkeyId(String sfgKnownhostkeyId) {
    this.sfgKnownhostkeyId = sfgKnownhostkeyId;
  }

  public String getSfgPrivkeyId() {
    return this.sfgPrivkeyId;
  }

  public void setSfgPrivkeyId(String sfgPrivkeyId) {
    this.sfgPrivkeyId = sfgPrivkeyId;
  }

  public String getUsername() {
    return this.username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public FgCustomer getProducer() {
    return this.producer;
  }

  public void setProducer(FgCustomer producer) {
    this.producer = producer;
  }

  public boolean isKeepFile() {
    return keepFile;
  }

  public void setKeepFile(boolean keepFile) {
    this.keepFile = keepFile;
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

  @Override
  public Map<String, Object> getIdentityFields() {
    final Map<String, Object> idmap = new HashMap<>();
    idmap.put("producer", producer.getIdentityFields());
    idmap.put("filepattern", filepattern);
    return idmap;
  }

  @Override
  public String getShortId() {
    return "[" + getProducer().getCustomerId() + "]" + filepattern + " (" + scheduleName + ") [ID=" + fgFetchId + "]";
  }

  @Override
  public boolean pointsToSame(AbstractSfgObject obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof FgFetchSftp))
      return false;
    FgFetchSftp other = (FgFetchSftp) obj;
    return Objects.equals(clientAdapterName, other.clientAdapterName) && Objects.equals(filepattern, other.filepattern)
        && Objects.equals(hostname, other.hostname) && Objects.equals(password, other.password) && port == other.port && producer.pointsToSame(other.producer)
        && Objects.equals(sfgPrivkeyId, other.sfgPrivkeyId) && Objects.equals(username, other.username);
  }

  @Override
  public FgFetchSftp createCopy() {
    FgFetchSftp copy = new FgFetchSftp();
    copy.clientAdapterName = this.clientAdapterName;
    copy.connRetries = this.connRetries;
    copy.connRetryInterval = this.connRetryInterval;
    copy.connTimeout = this.connTimeout;
    copy.enabled = this.enabled;
    copy.hostname = this.hostname;
    copy.password = this.password;
    copy.port = this.port;
    copy.sfgKnownhostkeyId = this.sfgKnownhostkeyId;
    copy.sfgPrivkeyId = this.sfgPrivkeyId;
    copy.username = this.username;
    copy.lastActive = this.lastActive;
    copy.maxTransferTimeMinutes = this.maxTransferTimeMinutes;
    copy.priority = this.priority;
    copy.keepFile = this.keepFile;
    copy.useExternalCmd = this.useExternalCmd;
    copy.externalCmd = this.externalCmd;
    return copy;
  }

  @Override
  public String getKey() {
    return fgFetchId == 0 ? null : Long.toString(fgFetchId);
  }

  @Override
  public String toString() {
    return "FgFetchSftp [fgFetchId=" + fgFetchId + ", producer=" + (producer != null ? producer.getCustomerId() : null) + ", filepattern=" + filepattern
        + ", scheduleName=" + scheduleName + ", clientAdapterName=" + clientAdapterName + ", connRetries=" + connRetries + ", connRetryInterval="
        + connRetryInterval + ", connTimeout=" + connTimeout + ", hostname=" + hostname + ", password=" + (password == null ? "no" : "yes") + ", port=" + port
        + ", sfgKnownhostkeyId=" + sfgKnownhostkeyId + ", sfgPrivkeyId=" + sfgPrivkeyId + ", username=" + username + ", keepFile=" + keepFile
        + (useExternalCmd ? ", externalCmd=" + externalCmd : "") + super.toString() + "]";
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

}
