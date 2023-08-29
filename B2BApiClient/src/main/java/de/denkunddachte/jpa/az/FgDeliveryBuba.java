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
import de.denkunddachte.ft.Host;
import de.denkunddachte.jpa.AbstractSfgObject;

/**
 * The persistent class for the AZ_FG_DELIVERY_BUBA database table.
 * 
 */
@Entity
@Table(name = "AZ_FG_DELIVERY_BUBA")
@NamedQuery(name = "FgDeliveryBuba.findAll", query = "SELECT f FROM FgDeliveryBuba f")
public class FgDeliveryBuba extends AbstractSfgObject implements Serializable {
  private static final long      serialVersionUID = 1L;
  private static final Logger    LOGGER           = Logger.getLogger(FgDeliveryBuba.class.getName());

  public enum TLS {
    Must, None
  };

  @Column(name = "CA_CERTIFICATE_ID", length = 20)
  private String     caCertificateId;

  @Column(name = "CLIENT_ADAPTER_NAME", nullable = false, length = 100)
  private String     clientAdapterName;

  @Column(name = "CONN_RETRIES", nullable = false)
  private int        connRetries;

  @Column(name = "CONN_RETRY_INTERVAL", nullable = false)
  private int        connRetryInterval;

  @Column(name = "CONN_TIMEOUT", nullable = false)
  private int        connTimeout;

  @Column(name = "PASSWORD", nullable = false, length = 30)
  private String     password;

  @Column(name = "HOSTNAME", nullable = false, length = 50)
  private String     remoteHost;

  @Column(name = "PORT", nullable = false)
  private int        remotePort;

  @Column(name = "TLS", nullable = false, precision = 1)
  @Enumerated(EnumType.STRING)
  private TLS        tls;

  @Column(name = "FTPOA_RECIPIENT", nullable = false, length = 20)
  private String     ftpoaRecipient;

  @Column(name = "BASE_PATH", nullable = false, length = 255)
  private String     basePath;

  @Column(name = "LOGIN_PATH", nullable = false, length = 255)
  private String     loginPath;

  @Column(name = "USERNAME", nullable = false, length = 30)
  private String     username;

  @Column(name = "USE_EXTERNAL_CMD", precision = 1)
  private boolean    useExternalCmd;

  @Column(name = "EXTERNAL_CMD", length = 8000)
  private String     externalCmd;

  // bi-directional one-to-one association to FgDelivery
  // CONSTRAINT AZ_FG_TR_BUBA_FK1 FOREIGN KEY (FG_DELIVERY_ID) REFERENCES
  // AZ_FG_DELIVERY(FG_DELIVERY_ID) ON DELETE CASCADE
  @Id
  @OneToOne
  @JoinColumn(name = "FG_DELIVERY_ID")
  private FgDelivery fgDelivery;

  public FgDeliveryBuba() {
    this.remotePort = 443;
    this.tls = TLS.Must;
    this.basePath = "/FT/";
    this.loginPath = "/pkmslogin.form";
    this.connRetries = 5;
    this.connRetryInterval = 10;
    this.connTimeout = 120;
  }

  public FgDeliveryBuba(FgDelivery fgd) {
    this();
    this.fgDelivery = fgd;
  }
  
  public FTProtocol getProtocol() {
    return FTProtocol.BUBA;
  }

  public String getCaCertificateId() {
    return this.caCertificateId;
  }

  public void setCaCertificateId(String caCertificateId) {
    this.caCertificateId = caCertificateId;
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

  public String getLoginPath() {
    return this.loginPath;
  }

  public void setLoginPath(String path) {
    this.loginPath = path;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getRemoteHost() {
    return this.remoteHost;
  }

  public void setRemoteHost(String remoteHost) {
    this.remoteHost = remoteHost;
  }

  public int getRemotePort() {
    return this.remotePort;
  }

  public void setRemotePort(int remotePort) {
    this.remotePort = remotePort;
  }

  public Host getHost() {
    return new Host(this.remoteHost, this.remotePort);
  }

  public void setHost(Host host) {
    this.remoteHost = host.getHostname();
    this.remotePort = host.getPort();
  }

  public TLS getTls() {
    return this.tls;
  }

  public void setTls(TLS tls) {
    this.tls = tls;
  }

  public String getFtpoaRecipient() {
    return this.ftpoaRecipient;
  }

  public void setFtpoaRecipient(String val) {
    this.ftpoaRecipient = val;
  }

  public String getBasePath() {
    return this.basePath;
  }

  public void setBasePath(String path) {
    this.basePath = path;
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
    if (!(obj instanceof FgDeliveryBuba))
      return false;
    FgDeliveryBuba other = (FgDeliveryBuba) obj;
    return Objects.equals(basePath, other.basePath) && Objects.equals(ftpoaRecipient, other.ftpoaRecipient) && Objects.equals(loginPath, other.loginPath)
        && Objects.equals(password, other.password) && Objects.equals(remoteHost, other.remoteHost) && remotePort == other.remotePort
        && Objects.equals(username, other.username) && Objects.equals(clientAdapterName, other.clientAdapterName);
  }

  @Override
  public FgDeliveryBuba createCopy() {
    FgDeliveryBuba copy = new FgDeliveryBuba();
    copy.caCertificateId = this.caCertificateId;
    copy.clientAdapterName = this.clientAdapterName;
    copy.connRetries = this.connRetries;
    copy.connRetryInterval = this.connRetryInterval;
    copy.connTimeout = this.connTimeout;
    copy.password = this.password;
    copy.remoteHost = this.remoteHost;
    copy.remotePort = this.remotePort;
    copy.tls = this.tls;
    copy.ftpoaRecipient = this.ftpoaRecipient;
    copy.basePath = this.basePath;
    copy.loginPath = this.loginPath;
    copy.username = this.username;
    copy.useExternalCmd = this.useExternalCmd;
    copy.externalCmd = this.externalCmd;
    return copy;
  }

  @Override
  public String getKey() {
    return fgDelivery == null ? null : fgDelivery.getKey();
  }

  @Override
  public String toString() {
    return "FgDeliveryBuba [fgDeliveryId=" + fgDelivery.getFgDeliveryId() + ", caCertificateId=" + caCertificateId + ", clientAdapterName=" + clientAdapterName
        + ", connRetries=" + connRetries + ", connRetryInterval=" + connRetryInterval + ", connTimeout=" + connTimeout + ", loginPath=" + loginPath
        + ", password=" + (password == null ? "no" : "yes") + ", remoteHost=" + remoteHost + ", remotePort=" + remotePort + ", tls=" + tls + ", ftpoaRecipient="
        + ftpoaRecipient + ", basePath=" + basePath + ", username=" + username + (useExternalCmd ? ", externalCmd=" + externalCmd : "") + super.toString()
        + "]";
  }

}
