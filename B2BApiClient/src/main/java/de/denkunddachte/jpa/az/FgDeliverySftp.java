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

import de.denkunddachte.enums.FTProtocol;
import de.denkunddachte.enums.FileDisposition;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.ft.Host;
import de.denkunddachte.jpa.AbstractSfgObject;

/**
 * The persistent class for the AZ_FG_DELIVERY_SFTP database table.
 * 
 */
@Entity
@Table(name = "AZ_FG_DELIVERY_SFTP")
@NamedQuery(name = "FgDeliverySftp.findAll", query = "SELECT f FROM FgDeliverySftp f")
public class FgDeliverySftp extends AbstractSfgObject implements Serializable {
  private static final long      serialVersionUID = 1L;
  private static final Logger    LOGGER           = Logger.getLogger(FgDeliverySftp.class.getName());
  public static final FTProtocol PROTOCOL         = FTProtocol.SFTP;

  public enum Overwrite {
    YES, NO, APPENDTS
  }

  @Column(name = "CLIENT_ADAPTER_NAME", nullable = false, length = 100)
  private String     clientAdapterName;

  @Column(name = "CONN_RETRIES", nullable = false)
  private int        connRetries;

  @Column(name = "CONN_RETRY_INTERVAL", nullable = false)
  private int        connRetryInterval;

  @Column(name = "CONN_TIMEOUT", nullable = false)
  private int        connTimeout;

  @Column(name = "HOSTNAME", nullable = false, length = 50)
  private String     hostname;

  @Column(name = "PASSWORD", length = 30)
  private String     password;

  @Column(name = "PORT", nullable = false)
  private int        port;

  @Column(name = "SFG_KNOWNHOSTKEY_ID", nullable = false, length = 100)
  private String     sfgKnownhostkeyId;

  @Column(name = "SFG_PRIVKEY_ID", length = 100)
  private String     sfgPrivkeyId;

  @Column(name = "USERNAME", nullable = false, length = 30)
  private String     username;

  @Column(name = "USE_EXTERNAL_CMD", precision = 1)
  private boolean    useExternalCmd;

  @Column(name = "EXTERNAL_CMD", length = 8000)
  private String     externalCmd;

  @Enumerated(EnumType.STRING)
  @Column(name = "OVERWRITE_DEST", length = 20)
  private Overwrite  overwriteDest;

  @Column(name = "SEQUENTIAL_PROCESSING")
  private int        sequentialProcessingWaitSeconds;

  // bi-directional one-to-one association to FgDelivery
  // CONSTRAINT AZ_FG_TR_SFTP_FK1 FOREIGN KEY (FG_DELIVERY_ID) REFERENCES
  // AZ_FG_DELIVERY(FG_DELIVERY_ID) ON DELETE CASCADE
  @Id
  @OneToOne
  @JoinColumn(name = "FG_DELIVERY_ID")
  private FgDelivery fgDelivery;

  public FgDeliverySftp() {
    this.port = 22;
    this.connRetries = 3;
    this.connRetryInterval = 10;
    this.connTimeout = 120;
    this.overwriteDest = Overwrite.YES;
  }

  public FgDeliverySftp(FgDelivery fgd) {
    this();
    this.fgDelivery = fgd;
  }

  public FTProtocol getProtocol() {
    return FTProtocol.SFTP;
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

  public String getHostname() {
    return this.hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
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

  public Host getHost() {
    return new Host(this.hostname, this.port);
  }

  public void setHost(Host host) {
    this.hostname = host.getHostname();
    this.port = host.getPort();
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

  public Overwrite getOverwriteDest() {
    return overwriteDest;
  }

  public void setOverwriteDest(FileDisposition disposition) throws ApiException {
    switch (disposition) {
    case APPEND:
      this.overwriteDest = Overwrite.APPENDTS;
      break;
    case NEW:
      this.overwriteDest = Overwrite.NO;
      break;
    case RPL:
      this.overwriteDest = Overwrite.YES;
      break;
    default:
      throw new ApiException("Invalid remote disposition for SFTP: " + disposition);
    }
  }

  public int getSequentialProcessingWaitSeconds() {
    return sequentialProcessingWaitSeconds;
  }

  public void setSequentialProcessingWaitSeconds(int sequentialProcessingWaitSeconds) {
    this.sequentialProcessingWaitSeconds = sequentialProcessingWaitSeconds;
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
    if (!(obj instanceof FgDeliverySftp))
      return false;
    FgDeliverySftp other = (FgDeliverySftp) obj;
    return Objects.equals(clientAdapterName, other.clientAdapterName) && Objects.equals(hostname, other.hostname) && Objects.equals(password, other.password)
        && port == other.port && Objects.equals(sfgPrivkeyId, other.sfgPrivkeyId) && Objects.equals(username, other.username);
  }

  @Override
  public FgDeliverySftp createCopy() {
    FgDeliverySftp copy = new FgDeliverySftp();
    copy.clientAdapterName = this.clientAdapterName;
    copy.connRetries = this.connRetries;
    copy.connRetryInterval = this.connRetryInterval;
    copy.connTimeout = this.connTimeout;
    copy.hostname = this.hostname;
    copy.password = this.password;
    copy.port = this.port;
    copy.sfgKnownhostkeyId = this.sfgKnownhostkeyId;
    copy.sfgPrivkeyId = this.sfgPrivkeyId;
    copy.username = this.username;
    copy.useExternalCmd = this.useExternalCmd;
    copy.externalCmd = this.externalCmd;
    copy.overwriteDest = this.overwriteDest;
    copy.sequentialProcessingWaitSeconds = this.sequentialProcessingWaitSeconds;
    return copy;
  }

  @Override
  public String getKey() {
    return fgDelivery == null ? null : fgDelivery.getKey();
  }

  @Override
  public String toString() {
    return "FgDeliverySftp [fgDeliveryId=" + fgDelivery.getFgDeliveryId() + ", clientAdapterName=" + clientAdapterName + ", connRetries=" + connRetries
        + ", connRetryInterval=" + connRetryInterval + ", connTimeout=" + connTimeout + ", hostname=" + hostname + ", password="
        + (password == null ? "no" : "yes") + ", port=" + port + ", sfgKnownhostkeyId=" + sfgKnownhostkeyId + ", sfgPrivkeyId=" + sfgPrivkeyId + ", username="
        + username + ", overwriteDest=" + overwriteDest + ", sequentialProcessingWaitSeconds=" + sequentialProcessingWaitSeconds
        + (useExternalCmd ? ", externalCmd=" + externalCmd : "") + super.toString() + "]";
  }

}
