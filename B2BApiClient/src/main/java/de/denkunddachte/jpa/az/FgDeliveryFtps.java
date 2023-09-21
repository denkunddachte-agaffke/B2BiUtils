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
import de.denkunddachte.jpa.AbstractSfgObject;
import de.denkunddachte.jpa.az.FgDeliverySftp.Overwrite;
import de.denkunddachte.enums.FTProtocol;
import de.denkunddachte.enums.FileDisposition;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.ft.Host;

/**
 * The persistent class for the AZ_FG_DELIVERY_FTPS database table.
 * 
 */
@Entity
@Table(name = "AZ_FG_DELIVERY_FTPS")
@NamedQuery(name = "FgDeliveryFtps.findAll", query = "SELECT f FROM FgDeliveryFtps f")
public class FgDeliveryFtps extends AbstractSfgObject implements Serializable {
  private static final long      serialVersionUID = 1L;
  private static final Logger    LOGGER           = Logger.getLogger(FgDeliveryFtps.class.getName());
  public static final FTProtocol PROTOCOL         = FTProtocol.FTPS;

  @Column(name = "CLIENT_ADAPTER_NAME", nullable = false, length = 100)
  private String                 clientAdapterName;

  @Column(name = "CONN_RETRIES", nullable = false)
  private int                    connRetries;

  @Column(name = "CONN_RETRY_INTERVAL", nullable = false)
  private int                    connRetryInterval;

  @Column(name = "CONN_TIMEOUT", nullable = false)
  private int                    connTimeout;

  @Column(name = "CONN_TYPE", nullable = false)
  private String                 connType;

  @Column(name = "HOSTNAME", nullable = false, length = 50)
  private String                 hostname;

  @Column(name = "PASSWORD", length = 30)
  private String                 password;

  @Column(name = "PORT", nullable = false)
  private int                    port;

  @Column(name = "CACERTIFICATEID", nullable = true, length = 100)
  private String                 caCertificateId;

  @Column(name = "SYSTEMCERTIFICATEID", nullable = true, length = 100)
  private String                 systemCertificateId;

  @Column(name = "REPRESENTATIONTYPE", nullable = false, length = 10)
  private String                 representationType;

  @Column(name = "USERNAME", nullable = false, length = 30)
  private String                 username;

  @Column(name = "USE_SSL", nullable = false, length = 30)
  private String                 useSSL;

  @Column(name = "USE_EXTERNAL_CMD", precision = 1)
  private boolean                useExternalCmd;

  @Column(name = "EXTERNAL_CMD", length = 8000)
  private String                 externalCmd;

  @Enumerated(EnumType.STRING)
  @Column(name = "OVERWRITE_DEST", length = 20)
  private Overwrite              overwriteDest;

  // bi-directional one-to-one association to FgDelivery
  // CONSTRAINT AZ_FG_TR_FTPS_FK1 FOREIGN KEY (FG_DELIVERY_ID) REFERENCES
  // AZ_FG_DELIVERY(FG_DELIVERY_ID) ON DELETE CASCADE
  @Id
  @OneToOne
  @JoinColumn(name = "FG_DELIVERY_ID")
  private FgDelivery             fgDelivery;

  public FgDeliveryFtps() {
    this.port = 21;
    this.useSSL = "SSL_NONE";
    this.representationType = "BINARY";
    this.connType = "PASSIVE";
    this.connRetries = 3;
    this.connRetryInterval = 10;
    this.connTimeout = 120;
    this.overwriteDest = Overwrite.YES;
  }

  public FgDeliveryFtps(FgDelivery fgd) {
    this();
    this.fgDelivery = fgd;
  }

  public FTProtocol getProtocol() {
    return FTProtocol.FTPS;
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

  public String getUsername() {
    return this.username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getConnType() {
    return connType;
  }

  public void setConnType(String connType) {
    this.connType = connType;
  }

  public String getCaCertificateId() {
    return caCertificateId;
  }

  public void setCaCertificateId(String caCertificateId) {
    this.caCertificateId = caCertificateId;
  }

  public String getSystemCertificateId() {
    return systemCertificateId;
  }

  public void setSystemCertificateId(String systemCertificateId) {
    this.systemCertificateId = systemCertificateId;
  }

  public String getRepresentationType() {
    return representationType;
  }

  public void setRepresentationType(String representationType) {
    this.representationType = representationType;
  }

  public String getUseSSL() {
    return useSSL;
  }

  public void setUseSSL(String useSSL) {
    this.useSSL = useSSL;
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
      throw new ApiException("Invalid remote disposition for FTP(S): " + disposition);
    }
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
    if (!(obj instanceof FgDeliveryFtps))
      return false;
    FgDeliveryFtps other = (FgDeliveryFtps) obj;
    return Objects.equals(connType, other.connType) && Objects.equals(hostname, other.hostname) && Objects.equals(password, other.password)
        && port == other.port && Objects.equals(useSSL, other.useSSL) && Objects.equals(username, other.username)
        && Objects.equals(clientAdapterName, other.clientAdapterName);
  }

  @Override
  public FgDeliveryFtps createCopy() {
    FgDeliveryFtps copy = new FgDeliveryFtps();
    copy.clientAdapterName = this.clientAdapterName;
    copy.connRetries = this.connRetries;
    copy.connRetryInterval = this.connRetryInterval;
    copy.connTimeout = this.connTimeout;
    copy.connType = this.connType;
    copy.hostname = this.hostname;
    copy.password = this.password;
    copy.port = this.port;
    copy.caCertificateId = this.caCertificateId;
    copy.systemCertificateId = this.systemCertificateId;
    copy.representationType = this.representationType;
    copy.username = this.username;
    copy.useSSL = this.useSSL;
    copy.useExternalCmd = this.useExternalCmd;
    copy.externalCmd = this.externalCmd;
    copy.overwriteDest = this.overwriteDest;
    return copy;
  }

  @Override
  public String getKey() {
    return fgDelivery == null ? null : fgDelivery.getKey();
  }

  @Override
  public String toString() {
    return "FgDeliveryFtps [fgDelivery=" + fgDelivery.getFgDeliveryId() + ", clientAdapterName=" + clientAdapterName + ", connRetries=" + connRetries
        + ", connRetryInterval=" + connRetryInterval + ", connTimeout=" + connTimeout + ", connType=" + connType + ", hostname=" + hostname + ", password="
        + (password == null ? "no" : "yes") + ", port=" + port + ", caCertificateId=" + caCertificateId + ", systemCertificateId=" + systemCertificateId
        + ", representationType=" + representationType + ", username=" + username + ", useSSL=" + useSSL + "overwriteDest=" + overwriteDest
        + (useExternalCmd ? ", externalCmd=" + externalCmd : "") + super.toString() + "]";
  }
}
