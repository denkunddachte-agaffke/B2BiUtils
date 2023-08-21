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
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import de.denkunddachte.enums.FTProtocol;
import de.denkunddachte.ft.Host;
import de.denkunddachte.jpa.AbstractSfgObject;

/**
 * The persistent class for the AZ_FG_DELIVERY_EXT database table.
 * 
 */
@Entity
@Table(name = "AZ_FG_DELIVERY_EXT")
@NamedQuery(name = "FgDeliveryExt.findAll", query = "SELECT f FROM FgDeliveryExt f")
public class FgDeliveryExt extends AbstractSfgObject implements Serializable {
  private static final long      serialVersionUID = 1L;
  private static final Logger    LOGGER           = Logger.getLogger(FgDeliveryExt.class.getName());
  public static final FTProtocol PROTOCOL         = FTProtocol.EXT;

  @Column(name = "HOSTNAME", nullable = false, length = 50)
  private String                 hostname;

  @Column(name = "PORT", nullable = false)
  private int                    port;

  @Column(name = "USERNAME", nullable = false, length = 30)
  private String                 username;

  @Column(name = "PASSWORD", length = 30)
  private String                 password;

  @Column(name = "SSH_KEY_NAME", length = 100)
  private String                 sshKeyName;

  @Column(name = "CLIENT_ADAPTER_NAME", nullable = false, length = 100)
  private String                 clientAdapterName;

  @Column(name = "COMMANDLINE", nullable = false, length = 8000)
  private String                 commandline;

  @Column(name = "ENVIRONMENT", length = 8000)
  private String                 environment;

  // bi-directional one-to-one association to FgDelivery
  // CONSTRAINT AZ_FG_TR_OTHR_FK1 FOREIGN KEY (FG_DELIVERY_ID) REFERENCES
  // AZ_FG_DELIVERY(FG_DELIVERY_ID) ON DELETE CASCADE
  @Id
  @OneToOne
  @JoinColumn(name = "FG_DELIVERY_ID")
  private FgDelivery             fgDelivery;

  public FgDeliveryExt() {
    this.port = 22;
  }

  public FgDeliveryExt(FgDelivery fgd) {
    this();
    this.fgDelivery = fgd;
  }

  public FTProtocol getProtocol() {
    return FTProtocol.EXT;
  }

  public String getClientAdapterName() {
    return this.clientAdapterName;
  }

  public void setClientAdapterName(String clientAdapterName) {
    this.clientAdapterName = clientAdapterName;
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

  public String getSshKeyName() {
    return sshKeyName;
  }

  public void setSshKeyName(String sshKeyName) {
    this.sshKeyName = sshKeyName;
  }

  public String getCommandline() {
    return commandline;
  }

  public void setCommandline(String commandline) {
    this.commandline = commandline;
  }

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
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
    if (!(obj instanceof FgDeliveryExt))
      return false;
    FgDeliveryExt other = (FgDeliveryExt) obj;
    return Objects.equals(hostname, other.hostname) && Objects.equals(password, other.password) && port == other.port
        && Objects.equals(sshKeyName, other.sshKeyName) && Objects.equals(username, other.username)
        && Objects.equals(clientAdapterName, other.clientAdapterName);
  }

  @Override
  public FgDeliveryExt createCopy() {
    FgDeliveryExt copy = new FgDeliveryExt();
    copy.hostname = this.hostname;
    copy.port = this.port;
    copy.username = this.username;
    copy.password = this.password;
    copy.sshKeyName = this.sshKeyName;
    copy.clientAdapterName = this.clientAdapterName;
    copy.commandline = this.commandline;
    copy.environment = this.environment;
    return copy;
  }

  @Override
  public String getKey() {
    return fgDelivery == null ? null : fgDelivery.getKey();
  }

  @Override
  public String toString() {
    return "FgDeliveryExt [fgDeliveryId=" + fgDelivery.getFgDeliveryId() + ", hostname=" + hostname + ", port=" + port + ", username=" + username
        + ", password=" + (password == null ? "no" : "yes") + ", sshKeyName=" + sshKeyName + ", clientAdapterName=" + clientAdapterName + ", commandline="
        + commandline + ", environment=" + environment + super.toString() + "]";
  }
}
