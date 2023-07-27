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
package de.denkunddachte.ft;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.denkunddachte.enums.CipherSuite;
import de.denkunddachte.enums.TlsVersion;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.sfgapi.CADigitalCertificate;
import de.denkunddachte.sfgapi.SystemDigitalCertificate;
import de.denkunddachte.util.ApiConfig;

public class CDNode {

  public enum LogLevel {
    NONE, DEBUG, INFO, WARN, ERROR
  }

  private String                                  nodeName;
  private String                                  tcpAddress;
  private static CDNode                           defaultNode;
  private int                                     port                        = 1364;
  private int                                     stRetryAttempts             = 3;
  private int                                     stRetryInterval             = 30;
  private int                                     ltRetryAttempts             = 0;
  private int                                     ltRetryInterval             = 10 * 60;
  private int                                     maxPnodeSessions            = 25;
  private int                                     maxSnodeSessions            = 25;
  private String                                  altCommInfo;

  // Secure+
  private boolean                                 securePlus                  = false;
  private TlsVersion                              securityProtocol            = TlsVersion.TLS_V12;
  private final Map<String, CADigitalCertificate> caCertificates              = new LinkedHashMap<>();
  private String                                  systemCertificateName;
  private SystemDigitalCertificate                systemCertificate;
  private final Set<CipherSuite>                  cipherSuites                = new LinkedHashSet<>();
  private String                                  certificateCommonName;
  private boolean                                 requireClientAuthentication = false;

  // for SSP
  private String                                  description;
  private String                                  netmapPolicyName;
  private LogLevel                                logLevel                    = LogLevel.NONE;
  private String                                  sspTruststoreName;
  private String                                  sspKeyStoreName;
  private int                                     tcpTimeout;

  public CDNode(String nodeName, String hostname, int port, boolean securePlusEnabled) throws ApiException {
    this(nodeName);
    setTcpAddress(hostname);
    if (port > 0)
      setPort(port);
    this.securePlus = securePlusEnabled;
  }

  public CDNode(String nodeName) throws ApiException {
    ApiConfig apicfg = ApiConfig.getInstance();
    this.nodeName = nodeName.toUpperCase();
    this.logLevel = apicfg.getSspCdNodeLogLevel();
    this.sspTruststoreName = apicfg.getSspCdTruststoreName();
    this.sspKeyStoreName = apicfg.getSspCdKeystoreName();
    this.netmapPolicyName = apicfg.getSspCdDefaultPolicy();
    this.tcpTimeout = apicfg.getSspCdTcpTimeout();
    this.systemCertificateName = apicfg.getCdSecplusSystemCert();
    this.securityProtocol = apicfg.getCdSecplusProtocol();
    this.requireClientAuthentication = apicfg.isCdSecplusClientauth();
    for (String cacert : apicfg.getCdSecplusCACerts()) {
      this.caCertificates.put(cacert, null);
    }
    this.cipherSuites.addAll(apicfg.getCdSecplusCipherSuites());
    if ("tcp.ip.default".equals(nodeName)) {
      CDNode.defaultNode = this;
    }
    if (CDNode.defaultNode != null) {
      this.port = CDNode.defaultNode.getPort();
      this.stRetryAttempts = CDNode.defaultNode.getStRetryAttempts();
      this.stRetryInterval = CDNode.defaultNode.getStRetryInterval();
      this.ltRetryAttempts = CDNode.defaultNode.getLtRetryAttempts();
      this.ltRetryInterval = CDNode.defaultNode.getLtRetryInterval();
      this.maxPnodeSessions = CDNode.defaultNode.getMaxPnodeSessions();
      this.maxSnodeSessions = CDNode.defaultNode.getMaxSnodeSessions();
    }
  }

  public String getTcpAddress() {
    return tcpAddress;
  }

  public void setTcpAddress(String tcpAddress) {
    this.tcpAddress = tcpAddress;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public int getStRetryAttempts() {
    return stRetryAttempts;
  }

  public void setStRetryAttempts(int stRetryAttempts) {
    this.stRetryAttempts = stRetryAttempts;
  }

  public int getStRetryInterval() {
    return stRetryInterval;
  }

  public void setStRetryInterval(int stRetryInterval) {
    this.stRetryInterval = stRetryInterval;
  }

  public int getLtRetryAttempts() {
    return ltRetryAttempts;
  }

  public void setLtRetryAttempts(int ltRetryAttempts) {
    this.ltRetryAttempts = ltRetryAttempts;
  }

  public int getLtRetryInterval() {
    return ltRetryInterval;
  }

  public void setLtRetryInterval(int ltRetryInterval) {
    this.ltRetryInterval = ltRetryInterval;
  }

  public int getMaxPnodeSessions() {
    return maxPnodeSessions;
  }

  public void setMaxPnodeSessions(int maxPnodeSessions) {
    this.maxPnodeSessions = maxPnodeSessions;
  }

  public int getMaxSnodeSessions() {
    return maxSnodeSessions;
  }

  public void setMaxSnodeSessions(int maxSnodeSessions) {
    this.maxSnodeSessions = maxSnodeSessions;
  }

  public String getNodeName() {
    return nodeName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isSecurePlus() {
    return securePlus;
  }

  public void setSecurePlus(boolean securePlus) {
    this.securePlus = securePlus;
  }

  public TlsVersion getSecurityProtocol() {
    return securityProtocol;
  }

  public void setSecurityProtocol(TlsVersion securityProtocol) {
    this.securityProtocol = securityProtocol;
  }

  public String getSystemCertificateName() {
    return systemCertificateName;
  }

  public void setSystemCertificateName(String systemCertificateName) {
    this.systemCertificateName = systemCertificateName;
  }

  public SystemDigitalCertificate getSystemCertificate() {
    return systemCertificate;
  }

  public void setSystemCertificate(SystemDigitalCertificate systemCertificate) {
    this.systemCertificate = systemCertificate;
  }

  public boolean isRequireClientAuthentication() {
    return requireClientAuthentication;
  }

  public void setRequireClientAuthentication(boolean requireClientAuthentication) {
    this.requireClientAuthentication = requireClientAuthentication;
  }

  public String getCertificateCommonName() {
    return certificateCommonName;
  }

  public void setCertificateCommonName(String certificateCommonName) {
    this.certificateCommonName = certificateCommonName;
  }

  public Map<String, CADigitalCertificate> getCaCertificates() {
    return caCertificates;
  }

  public void addCaCertificate(String certName, CADigitalCertificate cert) {
    caCertificates.put(certName, cert);
  }

  public Set<CipherSuite> getCipherSuites() {
    return cipherSuites;
  }

  public boolean addCipherSuite(CipherSuite suite) {
    return cipherSuites.add(suite);
  }

  public boolean addCipherSuites(Set<CipherSuite> suites) {
    return cipherSuites.addAll(suites);
  }

  public void setAltCommInfo(String altCommInfo) {
    this.altCommInfo = altCommInfo;
  }

  public String getAltCommInfo() {
    return altCommInfo;
  }

  public String getNetmapPolicyName() {
    return netmapPolicyName;
  }

  public void setNetmapPolicyName(String netmapPolicyName) {
    this.netmapPolicyName = netmapPolicyName;
  }

  public LogLevel getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(LogLevel logLevel) {
    this.logLevel = logLevel;
  }

  public String getSspTruststoreName() {
    return sspTruststoreName;
  }

  public void setSspTruststoreName(String sspTruststoreName) {
    this.sspTruststoreName = sspTruststoreName;
  }

  public String getSspKeyStoreName() {
    return sspKeyStoreName;
  }

  public void setSspKeyStoreName(String sspKeyStoreName) {
    this.sspKeyStoreName = sspKeyStoreName;
  }

  public int getTcpTimeout() {
    return tcpTimeout;
  }

  public void setTcpTimeout(int tcpTimeout) {
    this.tcpTimeout = tcpTimeout;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((nodeName == null) ? 0 : nodeName.hashCode());
    result = prime * result + port;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CDNode other = (CDNode) obj;
    if (nodeName == null) {
      if (other.nodeName != null)
        return false;
    } else if (!nodeName.equals(other.nodeName) || port != other.port)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "CDNode [nodeName=" + nodeName + ", tcpAddress=" + tcpAddress + ", port=" + port + ", stRetryAttempts=" + stRetryAttempts + ", stRetryInterval="
        + stRetryInterval + ", ltRetryAttempts=" + ltRetryAttempts + ", ltRetryInterval=" + ltRetryInterval + ", maxPnodeSessions=" + maxPnodeSessions
        + ", maxSnodeSessions=" + maxSnodeSessions + "]";
  }

}
