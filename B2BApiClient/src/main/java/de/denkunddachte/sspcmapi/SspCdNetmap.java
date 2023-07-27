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
package de.denkunddachte.sspcmapi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpStatus;

import de.denkunddachte.enums.CipherSuite;
import de.denkunddachte.enums.TlsVersion;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.ft.CDNetMap;
import de.denkunddachte.ft.CDNode;
import de.denkunddachte.sspcmapi.cd.CdObjectFactory;
import de.denkunddachte.sspcmapi.cd.InboundNodes;
import de.denkunddachte.sspcmapi.cd.InboundNodes.InboundNodeDef;
import de.denkunddachte.sspcmapi.cd.InboundNodes.InboundNodeDef.SslInfo;
import de.denkunddachte.sspcmapi.cd.NetmapDef;

public class SspCdNetmap extends AbstractSspApiClient implements CDNetMap {
  private static final Logger   LOGGER                    = Logger.getLogger(SspCdNetmap.class.getName());
  private static final String   SVC                       = "netmap";
  private String                netmapName;
  private NetmapDef             cdNetmapObj;
  public static final String    DEFAULT_CD_NODE_LOG_LEVEL = "ERROR";
  public static final int       DEFAULT_CD_TCP_TIMEOUT    = 90;
  private final CdObjectFactory cdObjectFactory           = new CdObjectFactory();

  public SspCdNetmap() throws ApiException {
    this(null);
  }

  public SspCdNetmap(String netmapName) throws ApiException {
    super();
    if (netmapName != null) {
      load(netmapName);
    }
  }

  @Override
  public boolean isSSPNetmap() {
    return true;
  }

  @Override
  String getServiceName() {
    return SVC;
  }

  @Override
  public String getName() {
    return netmapName;
  }

  public NetmapDef getCdNetmapObj() {
    return cdNetmapObj;
  }

  private void load(String netmapName) throws ApiException {
    XmlResponse xr = doGet("getNetmap", netmapName);
    LOGGER.log(Level.FINER, "Load netmap {0}: {1}/{2}", new Object[] { netmapName, xr.getHttpCode(), xr.getHttpStatus() });
    switch (xr.getHttpCode()) {
    case HttpStatus.SC_OK:
      LOGGER.log(Level.FINEST, "NetmapDef raw XML: {0}", xr.getResults().get(RESULTSKEY));
      NetmapDef cdnm = (NetmapDef) unmarshalResult(xr, NetmapDef.class);
      LOGGER.log(Level.FINER, "Loaded NetmapDef: {0}", cdnm);
      if (!"CD".equalsIgnoreCase(cdnm.getProtocol())) {
        throw new ApiException("API returned wrong protocol: " + cdnm.getProtocol());
      }
      this.netmapName = cdnm.getName();
      this.cdNetmapObj = cdnm;
      break;
    case HttpStatus.SC_NO_CONTENT:
      return;
    default:
      throw new ApiException("Could not load netmap " + netmapName + ". API returns " + xr.getHttpCode() + "/" + xr.getHttpStatus());
    }
  }

  public List<InboundNodeDef> getNodes() {
    return cdNetmapObj.getInboundNodes().getInboundNodeDef();
  }

  @Override
  public Set<String> getNodeNames() {
    final Set<String> result = new HashSet<>(getNodes().size());
    for (InboundNodeDef nd : getNodes()) {
      result.add(nd.getName());
    }
    return result;
  }

  @Override
  public boolean hasNode(String nodeName) {
    return getNodeNames().contains(nodeName);
  }

  @Override
  public boolean hasNode(CDNode node) {
    return getNodeNames().contains(node.getNodeName());
  }

  @Override
  public CDNode getCDNode(String nodeName) throws ApiException {
    CDNode cdnd = null;
    InboundNodeDef nd = getNode(nodeName);
    if (nd != null) {
      cdnd = new CDNode(nd.getName(), nd.getServerAddress(), nd.getPort(), nd.getSecureConnection());
      cdnd.setDescription(nd.getDescription());
      cdnd.setNetmapPolicyName(nd.getPolicyId());
      cdnd.setLogLevel(CDNode.LogLevel.valueOf(nd.getLogLevel()));
      cdnd.setTcpTimeout(nd.getTcpTimeout());
      if (nd.getSecureConnection()) {
        SslInfo ssl = nd.getSslInfo();
        cdnd.setSecurityProtocol(TlsVersion.getByVersionString(ssl.getProtocol()));
        cdnd.setCertificateCommonName(ssl.getCertificateCommonName());
        cdnd.setRequireClientAuthentication(ssl.getClientAuthenticationCD());
        cdnd.setSystemCertificateName(ssl.getKeyCertName());
        cdnd.setSspKeyStoreName(ssl.getKeyStoreName());
        cdnd.setSspTruststoreName(ssl.getTrustStoreName());
        for (String caCert : ssl.getTrustedCertNames().getTrustedCertName()) {
          cdnd.addCaCertificate(caCert, null);
        }
        for (String cs : ssl.getCipherSuites().getCipherSuite()) {
          cdnd.addCipherSuite(CipherSuite.byCode(cs));
        }
      }
    }
    return cdnd;
  }

  public InboundNodeDef getNode(String nodeName) {
    InboundNodeDef result = null;
    for (InboundNodeDef nd : getNodes()) {
      if (nd.getName().equalsIgnoreCase(nodeName)) {
        result = nd;
        break;
      }
    }
    return result;
  }

  public boolean addNode(InboundNodeDef nodedef) throws ApiException {
    InboundNodes in = cdObjectFactory.createInboundNodes();
    in.getInboundNodeDef().add(nodedef);
    XmlResponse xr = doPost("addNetmapNodes", netmapName, marshalObject(in, InboundNodes.class));
    if (xr.getHttpCode() == HttpStatus.SC_OK) {
      LOGGER.log(Level.FINE, "Added node {0} to netmap {1}.", new Object[] { nodedef, netmapName });
      return cdNetmapObj.getInboundNodes().getInboundNodeDef().add(nodedef);
    } else {
      throw new ApiException("Could not add node " + nodedef + " to netmap " + netmapName + ": " + getApiErrorMsg());
    }
  }

  public boolean updateNode(InboundNodeDef nodedef) throws ApiException {
    if (!hasNode(nodedef.getName())) {
      LOGGER.log(Level.WARNING, "Netmap {0} does not contain {1}!", new Object[] { netmapName, nodedef.getName() });
      return false;
    }
    InboundNodes in = cdObjectFactory.createInboundNodes();
    in.getInboundNodeDef().add(nodedef);
    XmlResponse xr = doPut("modifyNetmapNodes", netmapName, marshalObject(in, InboundNodes.class));
    if (xr.getHttpCode() == HttpStatus.SC_OK) {
      LOGGER.log(Level.FINE, "Modified node {0} in netmap {1}.", new Object[] { nodedef, netmapName });
      return true;
    } else {
      throw new ApiException("Could not update node " + nodedef + " in netmap " + netmapName + ": " + getApiErrorMsg());
    }
  }

  @Override
  public boolean removeNode(String name) throws ApiException {
    boolean deleted = false;
    InboundNodes.InboundNodeDef nd = getNode(name);
    if (nd != null) {
      XmlResponse xr = doDelete("deleteNetmapNodes", netmapName + "/" + name);
      if (xr.getHttpCode() == HttpStatus.SC_OK) {
        LOGGER.log(Level.FINE, "Deleted node {0} in netmap {1}.", new Object[] { name, netmapName });
        cdNetmapObj.getInboundNodes().getInboundNodeDef().remove(nd);
        deleted = true;
      } else {
        LOGGER.log(Level.WARNING, "Could not remove node {0} from netmap {1}: {2}", new Object[] { name, netmapName, getApiErrorMsg() });
      }
    } else {
      LOGGER.log(Level.WARNING, "Node {0} not registered in netmap {1}!", new Object[] { name, netmapName });
    }
    return deleted;
  }

  @Override
  public boolean removeNode(CDNode node) throws ApiException {
    return removeNode(node.getNodeName());
  }

  @Override
  public boolean addNewNode(String name, String address, int port, boolean securePlusEnabled) throws ApiException {
    return addNewNode(new CDNode(name, address, port, securePlusEnabled));
  }

  @Override
  public boolean addNewNode(CDNode node) throws ApiException {
    return addOrUpdateNode(node, false);
  }

  @Override
  public boolean addOrUpdateNode(CDNode node, boolean modifyExisting) throws ApiException {
    boolean modified = false;
    boolean created = false;
    final String name = node.getNodeName();
    final String address = node.getTcpAddress();
    final int port = node.getPort();

    InboundNodes.InboundNodeDef nd = getNode(name);
    InboundNodes.InboundNodeDef.Addresses.Address na;
    if (nd == null) {
      nd = cdObjectFactory.createInboundNodesInboundNodeDef(node);
      created = true;
    }
    na = nd.getAddresses().getAddress().get(0);
    if (!name.equals(na.getNodeName())) {
      na.setNodeName(name);
      modified = true;
    }
    if (!address.equals(na.getHost())) {
      na.setHost(address);
      modified = true;
    }
    if (port != na.getPort()) {
      na.setPort(port);
      modified = true;
    }
    if (!address.equals(nd.getServerAddress())) {
      nd.setServerAddress(address);
      modified = true;
    }
    if (!address.equals(nd.getPeerAddressPattern())) {
      nd.setPeerAddressPattern(address);
      modified = true;
    }
    if (port != nd.getPort()) {
      nd.setPort(port);
      modified = true;
    }
    if (!nd.getPolicyId().equals(node.getNetmapPolicyName())) {
      nd.setPolicyId(node.getNetmapPolicyName());
      modified = true;
    }
    if (created) {
      return addNode(nd);
    } else if (modifyExisting && modified) {
      nd.setDescription(node.getDescription());
      return updateNode(nd);
    } else {
      LOGGER.log(Level.FINE, "Skip update node {0} [{1}:{2}, {3}] in netmap {4} (unchanged).",
          new Object[] { name, address, port, node.getNetmapPolicyName(), netmapName });
    }
    return modified;
  }

  @Override
  public String toString() {
    return "SspCdNetmap [netmapName=" + netmapName + ", cdNetmapObj=" + cdNetmapObj + "]";
  }

  public static List<String> getAllNetmaps() throws ApiException {
    List<String> result = new ArrayList<>();
    try (SspCdNetmap nm = new SspCdNetmap()) {
      XmlResponse xr = nm.doGet("getAllNetmaps", null);
      if (xr.getHttpCode() == HttpStatus.SC_OK) {
        result.addAll(convertToList(xr.getObjectsList().get(0)));
      } else if (xr.getHttpCode() != HttpStatus.SC_NO_CONTENT) {
        throw new ApiException("API returns " + xr.getHttpCode() + "/" + xr.getHttpStatus() + "!");
      }
      return result;
    }
  }
}
