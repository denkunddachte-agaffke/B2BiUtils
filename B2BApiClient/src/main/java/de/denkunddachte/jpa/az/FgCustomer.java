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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.persistence.annotations.CascadeOnDelete;

import de.denkunddachte.enums.FTPartnerType;
import de.denkunddachte.enums.MigrationPath;
import de.denkunddachte.ft.Host;
import de.denkunddachte.jpa.AbstractSfgObject;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

/**
 * The persistent class for the AZ_FG_CUSTOMER database table.
 * 
 */
@Entity
@Table(name = "AZ_FG_CUSTOMER")
// @EntityListeners({TrackChangesListener.class})
@NamedQuery(name = "FgCustomer.findAll", query = "SELECT f FROM FgCustomer f")
@NamedQuery(name = "FgCustomer.find", query = "SELECT f FROM FgCustomer f WHERE f.customerId LIKE :customerId")
@NamedQuery(name = "FgCustomer.findIgnoreCase", query = "SELECT f FROM FgCustomer f WHERE upper(f.customerId) LIKE upper(:customerId)")
@NamedEntityGraph(name = "graph.cust.transfers",
    attributeNodes = { @NamedAttributeNode(value = "fgTransfers", subgraph = "fgTransfers"), @NamedAttributeNode(value = "fgFetchTransfers") },
    subgraphs = @NamedSubgraph(name = "fgTransfers", attributeNodes = @NamedAttributeNode("fgDeliveries")))
public class FgCustomer extends AbstractSfgObject implements Serializable {
  private static final long   serialVersionUID = 1L;
  private static final Logger LOGGER           = Logger.getLogger(FgCustomer.class.getName());

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "FG_CUST_ID", unique = true, nullable = false)
  private long                fgCustId;

  @Column(name = "CUSTOMER_ID", nullable = false, length = 50)
  private String              customerId;

  @Column(name = "CUSTOMER_NAME", length = 100)
  private String              customerName;

  @Column(name = "STREET", length = 50)
  private String              street;

  @Column(name = "POSTCODE", length = 10)
  private String              postcode;

  @Column(name = "CITY", length = 50)
  private String              city;

  @Column(name = "COUNTRY", length = 50)
  private String              country;

  @Column(name = "CONTACT", length = 100)
  private String              contact;

  @Column(name = "PHONE", length = 16)
  private String              phone;

  @Column(name = "EMAIL", length = 50)
  private String              email;

  @Column(name = "ISCONSUMER", nullable = false, precision = 1)
  private boolean             isconsumer;

  @Column(name = "ISPRODUCER", nullable = false, precision = 1)
  private boolean             isproducer;

  @Enumerated(EnumType.STRING)
  @Column(name = "CUSTOMER_TYPE", length = 10)
  private FTPartnerType       partnerType;

  @Column(name = "CSLM_LOG_TENANT", length = 10)
  private String              cslmLogTenant;

  @Deprecated
  @Column(name = "DRECOM_PARTNER", length = 20)
  private String              drecomPartner;

  @Deprecated
  @Enumerated(EnumType.STRING)
  @Column(name = "MIGRATION_PATH", length = 10)
  private MigrationPath       migrationPath;

  @Column(name = "ADDITIONAL_INFO_1", length = 2000)
  private String              additionalInfo1;

  @Column(name = "ADDITIONAL_INFO_2", length = 2000)
  private String              additionalInfo2;

  @Column(name = "ENABLED", precision = 1)
  private boolean             enabled;

  @Column(name = "LAST_ACTIVE", updatable = false, insertable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date                lastActive;

  // bi-directional many-to-one association to FgDelivery
  // CONSTRAINT AZ_FG_DELIVERY_FK2 FOREIGN KEY (CONSUMER_ID) REFERENCES
  // AZ_FG_CUSTOMER(FG_CUST_ID)
  @OneToMany(mappedBy = "consumer", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
  @CascadeOnDelete
  private List<FgDelivery>    fgDeliveries;

  // bi-directional many-to-one association to FgTransfer
  // CONSTRAINT AZ_FG_TRANSFER_FK1 FOREIGN KEY (PRODUCER_ID) REFERENCES
  // AZ_FG_CUSTOMER(FG_CUST_ID) ON DELETE CASCADE
  @OneToMany(mappedBy = "producer", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
  @CascadeOnDelete
  private List<FgTransfer>    fgTransfers;

  // bi-directional many-to-one association to FgFetchSftp
  // CONSTRAINT AZ_FG_FTCH_SFTP_FK1 FOREIGN KEY (PRODUCER_ID) REFERENCES
  // AZ_FG_CUSTOMER(FG_CUST_ID) ON DELETE CASCADE
  @OneToMany(mappedBy = "producer", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
  @CascadeOnDelete
  private List<FgFetchSftp>   fgFetchTransfers;

  // bi-directional many-to-one association to FgFetchSftp
  // CONSTRAINT AZ_FG_FTCH_AWSS3_FK1 FOREIGN KEY (PRODUCER_ID) REFERENCES
  // AZ_FG_CUSTOMER(FG_CUST_ID) ON DELETE CASCADE
  @OneToMany(mappedBy = "producer", cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
  @CascadeOnDelete
  private List<FgFetchAwsS3>  fgAwsS3FetchTransfers;

  // bi-directional many-to-one association to FgProxyMap
  // CONSTRAINT AZ_DCS_PROXYMAP_FK1 FOREIGN KEY (FG_CUST_ID) REFERENCES
  // AZ_FG_CUSTOMER(FG_CUST_ID) ON DELETE CASCADE
  @Deprecated
  @OneToMany(mappedBy = "fgCustomer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @CascadeOnDelete
  private List<FgProxyMap>    fgProxyMaps;

  public FgCustomer() {
    super();
  }

  public FgCustomer(String customerId) {
    this(customerId, true, true);
  }

  public FgCustomer(String customerId, boolean isProducer, boolean isConsumer) {
    super();
    this.customerId = customerId;
    this.isproducer = isProducer;
    this.isconsumer = isConsumer;
    this.partnerType = FTPartnerType.UNDEF;
    this.enabled = true;
    this.migrationPath = MigrationPath.BOTH;
  }

  public long getFgCustId() {
    return this.fgCustId;
  }

  public void setFgCustId(long fgCustId) {
    this.fgCustId = fgCustId;
  }

  public String getAdditionalInfo1() {
    return this.additionalInfo1;
  }

  public void setAdditionalInfo1(String additionalInfo1) {
    this.additionalInfo1 = additionalInfo1;
  }

  public String getAdditionalInfo2() {
    return this.additionalInfo2;
  }

  public void setAdditionalInfo2(String additionalInfo2) {
    this.additionalInfo2 = additionalInfo2;
  }

  public String getCity() {
    return this.city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getCountry() {
    return this.country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getContact() {
    return this.contact;
  }

  public void setContact(String contact) {
    this.contact = contact;
  }

  public String getCustomerId() {
    return this.customerId;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  public String getCustomerName() {
    return this.customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public FTPartnerType getPartnerType() {
    return partnerType;
  }

  public void setPartnerType(FTPartnerType partnerType) {
    this.partnerType = partnerType;
  }

  public String getCslmLogTenant() {
    return cslmLogTenant;
  }

  public void setCslmLogTenant(String cslmLogTenant) {
    this.cslmLogTenant = cslmLogTenant;
  }

  @Deprecated
  public String getDrecomPartner() {
    if (this.drecomPartner == null) {
      return this.customerId;
    } else {
      return this.drecomPartner;
    }
  }

  @Deprecated
  public void setDrecomPartner(String drecomPartner) {
    this.drecomPartner = drecomPartner;
  }

  public String getEmail() {
    return this.email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public boolean isEnabled() {
    return enabled;
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

  public String getPhone() {
    return this.phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getPostcode() {
    return this.postcode;
  }

  public void setPostcode(String postcode) {
    this.postcode = postcode;
  }

  public String getStreet() {
    return this.street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public boolean isConsumer() {
    return isconsumer;
  }

  public void setIsConsumer(boolean isconsumer) {
    this.isconsumer = isconsumer;
  }

  public boolean isProducer() {
    return isproducer;
  }

  public void setIsProducer(boolean isproducer) {
    this.isproducer = isproducer;
  }

  @Deprecated
  public MigrationPath getMigrationPath() {
    return migrationPath;
  }

  @Deprecated
  public void setMigrationPath(MigrationPath migrationPath) {
    this.migrationPath = migrationPath;
  }

  public List<FgDelivery> getFgDeliveries() {
    fgDeliveries.size(); // trigger LAZY fetch
    List<FgDelivery> result = new ArrayList<>(fgDeliveries);
    return result;
  }

  public void setFgDeliveries(List<FgDelivery> fgDeliveries) {
    this.fgDeliveries.clear();
    for (FgDelivery fgd : fgDeliveries) {
      addFgDelivery(fgd);
    }
  }

  public FgDelivery addFgDelivery(FgDelivery fgDelivery) {
    fgDeliveries.add(fgDelivery);
    fgDelivery.setConsumer(this);
    return fgDelivery;
  }

  public boolean removeFgDelivery(FgDelivery fgDelivery) {
    return fgDeliveries.remove(fgDelivery);
    // fgDelivery.setConsumer(null);
  }

  public List<FgTransfer> getFgTransfers() {
    List<FgTransfer> result = new ArrayList<>(fgTransfers);
    return result;
  }

  public void setFgTransfers(List<FgTransfer> fgTransfers) {
    this.fgTransfers.clear();
    for (FgTransfer fgt : fgTransfers) {
      addFgTransfer(fgt);
    }
  }

  public FgTransfer addFgTransfer(FgTransfer fgTransfer) {
    fgTransfers.add(fgTransfer);
    fgTransfer.setProducer(this);
    return fgTransfer;
  }

  public boolean removeFgTransfer(FgTransfer fgTransfer) {
    return fgTransfers.remove(fgTransfer);
    // fgTransfer.setProducer(null);
  }

  public FgTransfer getFgTransfer(String receiveFilePattern) {
    // initiate LAZY fetch, if not already done:
    if (fgTransfers.isEmpty()) {
      return null;
    }
    FgTransfer result = null;
    for (FgTransfer fgt : getFgTransfers()) {
      if (receiveFilePattern.equals(fgt.getRcvFilepattern())) {
        result = fgt;
        break;
      }
    }
    return result;
  }

  public boolean hasFgTransferFor(String receiveFilePattern) {
    return getFgTransfer(receiveFilePattern) != null;
  }

  public List<FgFetchSftp> getFgFetchTransfers() {
    List<FgFetchSftp> result = new ArrayList<>(fgFetchTransfers.size());
    result.addAll(fgFetchTransfers);
    return result;
  }

  public void setFgFetchTransfers(List<FgFetchSftp> fgFetchTransfers) {
    this.fgFetchTransfers.clear();
    for (FgFetchSftp ft : fgFetchTransfers) {
      addFgFetchTransfer(ft);
    }
  }

  public FgFetchSftp addFgFetchTransfer(FgFetchSftp fgFetchTransfer) {
    fgFetchTransfers.add(fgFetchTransfer);
    fgFetchTransfer.setProducer(this);

    return fgFetchTransfer;
  }

  public boolean removeFgFetchTransfer(FgFetchSftp fgFetchTransfer) {
    return fgFetchTransfers.remove(fgFetchTransfer);
  }

  public FgFetchSftp getFgFetchTransfer(String hostname, String scheduleName, String filePattern) {
    // initiate LAZY fetch, if not already done:
    if (getFgFetchTransfers().isEmpty()) {
      return null;
    }
    FgFetchSftp result = null;
    for (FgFetchSftp fgt : getFgFetchTransfers()) {
      if (hostname.equalsIgnoreCase(fgt.getHostname()) && scheduleName.equals(fgt.getScheduleName()) && filePattern.equals(fgt.getFilepattern())) {
        result = fgt;
        break;
      }
    }
    return result;
  }

  public List<FgFetchAwsS3> getFgAwsS3FetchTransfers() {
    List<FgFetchAwsS3> result = new ArrayList<>(fgAwsS3FetchTransfers.size());
    result.addAll(fgAwsS3FetchTransfers);
    return result;
  }

  public void setFgAwsS3FetchTransfers(List<FgFetchAwsS3> fgAwsS3FetchTransfers) {
    this.fgAwsS3FetchTransfers.clear();
    for (FgFetchAwsS3 ft : fgAwsS3FetchTransfers) {
      addFgAwsS3FetchTransfer(ft);
    }
  }

  public FgFetchAwsS3 addFgAwsS3FetchTransfer(FgFetchAwsS3 fgAwsS3FetchTransfer) {
    fgAwsS3FetchTransfers.add(fgAwsS3FetchTransfer);
    fgAwsS3FetchTransfer.setProducer(this);

    return fgAwsS3FetchTransfer;
  }

  public boolean removeFgAwsS3FetchTransfer(FgFetchAwsS3 fgFetchTransfer) {
    return fgAwsS3FetchTransfers.remove(fgFetchTransfer);
  }

  public FgFetchAwsS3 getFgAwsS3FetchTransfer(String bucketName, String scheduleName, String filePattern) {
    // initiate LAZY fetch, if not already done:
    if (getFgFetchTransfers().isEmpty()) {
      return null;
    }
    FgFetchAwsS3 result = null;
    for (FgFetchAwsS3 fgt : getFgAwsS3FetchTransfers()) {
      if (bucketName.equalsIgnoreCase(fgt.getBucketName()) && scheduleName.equals(fgt.getScheduleName()) && filePattern.equals(fgt.getFilepattern())) {
        result = fgt;
        break;
      }
    }
    return result;
  }

  @Deprecated
  public List<FgProxyMap> getFgProxyMaps() {
    List<FgProxyMap> result = new ArrayList<>(fgProxyMaps);
    return result;
  }

  @Deprecated
  public void setFgProxyMaps(List<FgProxyMap> fgProxyMaps) {
    this.fgProxyMaps.clear();
    for (FgProxyMap map : fgProxyMaps) {
      addFgProxyMap(map);
    }
  }

  @Deprecated
  public FgProxyMap addFgProxyMap(FgProxyMap map) {
    fgProxyMaps.add(map);
    map.setFgCustomer(this);
    return map;
  }

  @Deprecated
  public boolean removeFgProxyMap(FgProxyMap map) {
    return fgProxyMaps.remove(map);
  }

  @Deprecated
  public FgProxyMap getFgProxyMap(Host host) {
    // initiate LAZY fetch, if not already done:
    if (fgProxyMaps.isEmpty()) {
      return null;
    }
    FgProxyMap result = null;
    for (FgProxyMap map : getFgProxyMaps()) {
      if (host.getHostname().equals(map.getHostname())) {
        if (host.getPort() <= 0 || host.getPort() == map.getPort()) {
          result = map;
          break;
        }
      }
    }
    return result;
  }

  @Deprecated
  public boolean hasFgProxyMapFor(Host host) {
    return getFgProxyMap(host) != null;
  }

  @Deprecated
  public boolean hasFgProxyMapFor(String host) {
    return getFgProxyMap(new Host(host, 0)) != null;
  }

  public static FgCustomer find(String customerId, boolean ignoreCase, EntityManager em) {
    return find(customerId, ignoreCase, false, em);
  }

  public static FgCustomer find(String customerId, boolean ignoreCase, boolean fetchTransfers, EntityManager em) {
    List<FgCustomer> result = findAll(customerId, ignoreCase, fetchTransfers, em);
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }

  public static List<FgCustomer> findAll(EntityManager em) {
    return findAll("%", false, false, em);
  }
  
  public static List<FgCustomer> findAll(String globPattern, boolean ignoreCase, boolean fetchTransfers, EntityManager em) {
    TypedQuery<FgCustomer> q = em.createNamedQuery((ignoreCase ? "FgCustomer.findIgnoreCase" : "FgCustomer.find"), FgCustomer.class);
    if (globPattern == null || globPattern.isEmpty())
      globPattern = "%";
    q.setParameter("customerId", globPattern.replace('*', '%').replace('?', '_'));
    if (fetchTransfers) {
      @SuppressWarnings("unchecked")
      EntityGraph<FgCustomer> graph = (EntityGraph<FgCustomer>) em.getEntityGraph("graph.cust.transfers");
      q.setHint("jakarta.persistence.fetchgraph", graph);
    }
    List<FgCustomer> result = q.getResultList();
    if (!result.isEmpty()) {
      LOGGER.log(Level.FINEST, "pattern={0}, result.size={1}", new Object[] { globPattern, result.size() });
    } else {
      LOGGER.log(Level.FINEST, "pattern={0} not found.", globPattern);
    }
    return result;
  }

  @Override
  public Map<String, Object> getIdentityFields() {
    final Map<String, Object> idmap = new HashMap<>();
    idmap.put("customerId", customerId);
    return idmap;
  }

  @Override
  public String getShortId() {
    return customerId + " [ID=" + fgCustId + "]";
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  public boolean pointsToSame(AbstractSfgObject obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof FgCustomer))
      return false;
    FgCustomer other = (FgCustomer) obj;
    return Objects.equals(customerId, other.customerId);
  }

  @Override
  public String getKey() {
    return fgCustId == 0 ? null : Long.toString(fgCustId);
  }

  @Override
  public String toString() {
    return "FgCustomer [fgCustId=" + fgCustId + ", customerId=" + customerId + ", customerName=" + customerName + ", partnerType=" + partnerType
        + ", isconsumer=" + isconsumer + ", isproducer=" + isproducer + ", street=" + street + ", postcode=" + postcode + ", city=" + city + ", country="
        + country + ", contact=" + contact + ", email=" + email + ", phone=" + phone + ", cslmLogTenant=" + cslmLogTenant + super.toString() + "]";
  }
}
