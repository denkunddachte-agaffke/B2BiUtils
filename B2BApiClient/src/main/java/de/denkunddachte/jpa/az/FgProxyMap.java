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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import de.denkunddachte.ft.Host;
import de.denkunddachte.jpa.AbstractSfgObject;

/**
 * The persistent class for the AZ_FG_FILETYPE database table.
 * 
 */
@Deprecated
@Entity
@Table(name = "AZ_DCS_PROXYMAP")
@NamedQuery(name = "FgProxyMap.findAll", query = "SELECT m FROM FgProxyMap m ORDER BY m.id")
public class FgProxyMap extends AbstractSfgObject implements Serializable {
  private static final long   serialVersionUID = 1L;
  private static final Logger LOGGER           = Logger.getLogger(FgProxyMap.class.getName());

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  // @GeneratedValue(generator = "FgTypeId")
  // @SequenceGenerator(name = "FgTypeId", sequenceName = "AZ_SEQ_FG_FILETYPEID", allocationSize = 1)
  @Column(name = "ID", unique = true, nullable = false)
  private long                id;

  @Column(name = "HOSTNAME", nullable = false, length = 50)
  private String              hostname;

  @Column(name = "PORT", nullable = false)
  private int                 port;

  @Column(name = "PROXY_HOST", nullable = false, length = 50)
  private String              proxyHost;

  @Column(name = "PROXY_PORT")
  private Integer             proxyPort;

  @Column(name = "PROXY_ENABLED", precision = 1)
  private boolean             proxyEnabled;

  @Column(name = "FETCH_SUPPLY_ENABLED", precision = 1)
  private boolean             fetchSupplyEnabled;

  @Column(name = "PROXY_STATUS", precision = 1)
  private boolean             proxyStatus;

  // bi-directional many-to-one association to FgDelivery
  // CONSTRAINT AZ_DCS_PROXYMAP_FK1 FOREIGN KEY (FG_CUST_ID) REFERENCES
  // AZ_FG_CUSTOMER(FG_CUST_ID) ON DELETE CASCADE
  @ManyToOne
  @JoinColumn(name = "FG_CUST_ID", nullable = false)
  private FgCustomer          fgCustomer;

  public FgProxyMap() {
    this("MBOX", 0);
    this.proxyHost = "172.20.158.18";
    this.proxyPort = null;
  }

  public FgProxyMap(Host host) {
    this(host.getHostname(), host.getPort());
    this.proxyHost = "172.20.158.18";
    this.proxyPort = null;
  }

  public FgProxyMap(String hostname, int port) {
    this.hostname = hostname;
    this.port = port;
    this.proxyHost = "172.20.158.18";
    this.proxyPort = null;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
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

  public String getProxyHost() {
    return proxyHost;
  }

  public void setProxyHost(String proxyHost) {
    this.proxyHost = proxyHost;
  }

  public boolean isProxyEnabled() {
    return proxyEnabled;
  }

  public void setProxyEnabled(boolean proxyEnabled) {
    this.proxyEnabled = proxyEnabled;
  }

  public boolean isFetchSupplyEnabled() {
    return fetchSupplyEnabled;
  }

  public void setFetchSupplyEnabled(boolean fetchSupplyEnabled) {
    this.fetchSupplyEnabled = fetchSupplyEnabled;
  }

  public FgCustomer getFgCustomer() {
    return fgCustomer;
  }

  public void setFgCustomer(FgCustomer fgCustomer) {
    this.fgCustomer = fgCustomer;
  }

  public Integer getProxyPort() {
    return proxyPort;
  }

  public boolean isProxyStatus() {
    return proxyStatus;
  }

  @Override
  public String toString() {
    return "FgProxyMap [id=" + id + ", hostname=" + hostname + ", port=" + port + ", proxyHost=" + proxyHost + ", proxyPort=" + proxyPort + ", proxyEnabled="
        + proxyEnabled + ", fetchSupplyEnabled=" + fetchSupplyEnabled + ", proxyStatus=" + proxyStatus + ", fgCustomer=" + fgCustomer.getCustomerId() + "]";
  }

  @Override
  public Map<String, Object> getIdentityFields() {
    final Map<String, Object> idmap = new HashMap<>();
    idmap.put("hostname", hostname);
    idmap.put("port", port);
    return idmap;
  }

  @Override
  public String getShortId() {
    return "[" + hostname + ":" + port + " -> " + proxyHost + ":" + proxyPort + "]";
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  public String getKey() {
    return id == 0 ? null : Long.toString(id);
  }

  @Override
  public boolean pointsToSame(AbstractSfgObject obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof FgProxyMap))
      return false;
    FgProxyMap other = (FgProxyMap) obj;
    return fgCustomer.pointsToSame(other.fgCustomer) && Objects.equals(hostname, other.hostname) && port == other.port;
  }

}
