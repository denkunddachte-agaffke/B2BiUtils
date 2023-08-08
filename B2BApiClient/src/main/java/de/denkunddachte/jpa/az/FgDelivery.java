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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.eclipse.persistence.annotations.CascadeOnDelete;

import de.denkunddachte.enums.FTProtocol;
import de.denkunddachte.enums.OSType;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.jpa.AbstractSfgObject;

/**
 * The persistent class for the AZ_FG_DELIVERY database table.
 * 
 */
@Entity
@Table(name = "AZ_FG_DELIVERY")
@NamedQuery(name = "FgDelivery.findAll", query = "SELECT f FROM FgDelivery f")
public class FgDelivery extends AbstractSfgObject implements Serializable {
  private static final long   serialVersionUID = 1L;
  private static final Logger LOGGER           = Logger.getLogger(FgDelivery.class.getName());

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "FG_DELIVERY_ID", unique = true, nullable = false)
  private long                fgDeliveryId;

  @Enumerated(EnumType.STRING)
  @Column(name = "CONSUMER_OS", length = 20)
  private OSType              consumerOs;

  @Column(name = "POST_PROCESS_CMD", length = 1000)
  private String              postProcessCmd;

  @Enumerated(EnumType.STRING)
  @Column(name = "PROTOCOL", nullable = false, length = 8)
  private FTProtocol          protocol;

  @Column(name = "SND_FILENAME", nullable = false, length = 240)
  private String              sndFilename;

  @Column(name = "TMP_FILENAME", nullable = true, length = 240)
  private String              tmpFilename;

  @Column(name = "DELIVERY_SCHEDULE", length = 100)
  private String              deliverySchedule;

  @Column(name = "ENABLED", precision = 1)
  private boolean             enabled;

  @Column(name = "LAST_ACTIVE", updatable = false, insertable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date                lastActive;

  @Column(name = "CSLM_LOG_TENANT", length = 10)
  private String              cslmLogTenant;

  @Column(name = "RESOLVE_PATHS", precision = 1)
  private boolean             resolvePaths;

  @Column(name = "MAX_RUNTIME_SECONDS")
  private int                 maxRuntimeSeconds;

  @Column(name = "DELAY_SECONDS")
  private int                 delaySeconds;

  // bi-directional many-to-one association to FgCustomer
  // CONSTRAINT AZ_FG_DELIVERY_FK2 FOREIGN KEY (CONSUMER_ID) REFERENCES
  // AZ_FG_CUSTOMER(FG_CUST_ID)
  @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
  @JoinColumn(name = "CONSUMER_ID", nullable = false)
  private FgCustomer          consumer;

  // bi-directional many-to-one association to FgFiletype
  // CONSTRAINT AZ_FG_DELIVERY_FK3 FOREIGN KEY (FG_FILETYPE_ID) REFERENCES
  // AZ_FG_FILETYPE(FG_FILETYPE_ID)
  @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.EAGER)
  @JoinColumn(name = "FG_FILETYPE_ID")
  private FgFiletype          fgFiletype;

  // bi-directional many-to-one association to FgTransfer
  // CONSTRAINT AZ_FG_DELIVERY_FK1 FOREIGN KEY (FG_TRANS_ID) REFERENCES
  // AZ_FG_TRANSFER(FG_TRANS_ID) ON DELETE CASCADE
  @ManyToOne
  @JoinColumn(name = "FG_TRANS_ID", nullable = false)
  private FgTransfer          fgTransfer;

  /*************************************************************************
   * Delivery params
   *************************************************************************/
  // bi-directional one-to-one association to FgDeliveryBuba
  // CONSTRAINT AZ_FG_TR_BUBA_FK1 FOREIGN KEY (FG_DELIVERY_ID) REFERENCES
  // AZ_FG_DELIVERY(FG_DELIVERY_ID) ON DELETE CASCADE
  @OneToOne(mappedBy = "fgDelivery", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @CascadeOnDelete
  private FgDeliveryBuba      fgDeliveryBuba;

  // bi-directional one-to-one association to FgDeliveryCd
  // CONSTRAINT AZ_FG_TR_CD_FK1 FOREIGN KEY (FG_DELIVERY_ID) REFERENCES
  // AZ_FG_DELIVERY(FG_DELIVERY_ID) ON DELETE CASCADE
  @OneToOne(mappedBy = "fgDelivery", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @CascadeOnDelete
  private FgDeliveryCd        fgDeliveryCd;

  // bi-directional one-to-one association to FgDeliveryMbox
  // CONSTRAINT AZ_FG_TR_MBOX_FK1 FOREIGN KEY (FG_DELIVERY_ID) REFERENCES
  // AZ_FG_DELIVERY(FG_DELIVERY_ID) ON DELETE CASCADE
  @OneToOne(mappedBy = "fgDelivery", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @CascadeOnDelete
  private FgDeliveryMbox      fgDeliveryMbox;

  // bi-directional one-to-one association to FgDeliveryOftp
  // CONSTRAINT AZ_FG_TR_OFTP_FK1 FOREIGN KEY (FG_DELIVERY_ID) REFERENCES
  // AZ_FG_DELIVERY(FG_DELIVERY_ID) ON DELETE CASCADE
  @OneToOne(mappedBy = "fgDelivery", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @CascadeOnDelete
  private FgDeliveryOftp      fgDeliveryOftp;

  // bi-directional one-to-one association to FgDeliverySftp
  // CONSTRAINT AZ_FG_TR_SFTP_FK1 FOREIGN KEY (FG_DELIVERY_ID) REFERENCES
  // AZ_FG_DELIVERY(FG_DELIVERY_ID) ON DELETE CASCADE
  @OneToOne(mappedBy = "fgDelivery", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @CascadeOnDelete
  private FgDeliverySftp      fgDeliverySftp;

  // bi-directional one-to-one association to FgDeliverySsh
  // CONSTRAINT AZ_FG_TR_SSH_FK1 FOREIGN KEY (FG_DELIVERY_ID) REFERENCES
  // AZ_FG_DELIVERY(FG_DELIVERY_ID) ON DELETE CASCADE
  @OneToOne(mappedBy = "fgDelivery", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @CascadeOnDelete
  private FgDeliverySsh       fgDeliverySsh;

  // bi-directional one-to-one association to FgDeliveryExt
  // CONSTRAINT AZ_FG_TR_EXT_FK1 FOREIGN KEY (FG_DELIVERY_ID) REFERENCES
  // AZ_FG_DELIVERY(FG_DELIVERY_ID) ON DELETE CASCADE
  @OneToOne(mappedBy = "fgDelivery", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @CascadeOnDelete
  private FgDeliveryExt       fgDeliveryExt;

  // bi-directional one-to-one association to FgDeliveryFtps
  // CONSTRAINT AZ_FG_TR_FTPS_FK1 FOREIGN KEY (FG_DELIVERY_ID) REFERENCES
  // AZ_FG_DELIVERY(FG_DELIVERY_ID) ON DELETE CASCADE
  @OneToOne(mappedBy = "fgDelivery", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @CascadeOnDelete
  private FgDeliveryFtps      fgDeliveryFtps;

  // bi-directional one-to-one association to FgDeliveryAwsS3
  // CONSTRAINT AZ_FG_TR_AWSS3_FK1 FOREIGN KEY (FG_DELIVERY_ID) REFERENCES
  // AZ_FG_DELIVERY(FG_DELIVERY_ID) ON DELETE CASCADE
  @OneToOne(mappedBy = "fgDelivery", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @CascadeOnDelete
  private FgDeliveryAwsS3     fgDeliveryAwsS3;

  public FgDelivery() {
    super();
  }

  public FgDelivery(FgTransfer transfer, FgCustomer consumer, String sndFilename, FTProtocol protocol) {
    super();
    this.fgTransfer = transfer;
    this.consumer = consumer;
    this.sndFilename = sndFilename;
    this.protocol = protocol;
    this.enabled = true;
    this.maxRuntimeSeconds = 1800;
    this.resolvePaths = false;
    this.delaySeconds = 0;
  }

  public long getFgDeliveryId() {
    return this.fgDeliveryId;
  }

  public void setFgDeliveryId(long fgDeliveryId) {
    this.fgDeliveryId = fgDeliveryId;
  }

  public OSType getConsumerOs() {
    return this.consumerOs;
  }

  public void setConsumerOs(OSType consumerOs) {
    this.consumerOs = consumerOs;
  }

  public String getPostProcessCmd() {
    return this.postProcessCmd;
  }

  public void setPostProcessCmd(String postProcessCmd) {
    this.postProcessCmd = postProcessCmd;
  }

  public FTProtocol getProtocol() {
    return this.protocol;
  }

  private void setProtocol(FTProtocol protocol) {
    if (this.protocol != protocol) {
      setFgDeliveryBuba(null);
      setFgDeliveryCd(null);
      setFgDeliveryMbox(null);
      setFgDeliveryOftp(null);
      setFgDeliverySftp(null);
      setFgDeliverySsh(null);
      setFgDeliveryExt(null);
      setFgDeliveryAwsS3(null);
      setFgDeliveryFtps(null);
      this.protocol = protocol;
    }
  }

  public String getSndFilename() {
    return this.sndFilename;
  }

  public void setSndFilename(String sndFilename) {
    this.sndFilename = sndFilename;
  }

  public String getTmpFilename() {
    return tmpFilename;
  }

  public void setTmpFilename(String tmpFilename) {
    this.tmpFilename = tmpFilename;
  }

  public FgCustomer getConsumer() {
    return this.consumer;
  }

  public void setConsumer(FgCustomer consumer) {
    this.consumer = consumer;
  }

  public FgFiletype getFgFiletype() {
    return this.fgFiletype;
  }

  public void setFgFiletype(FgFiletype fgFiletype) {
    this.fgFiletype = fgFiletype;
  }

  public FgTransfer getFgTransfer() {
    return this.fgTransfer;
  }

  public void setFgTransfer(FgTransfer fgTransfer) {
    this.fgTransfer = fgTransfer;
  }

  public AbstractSfgObject getDeliveryParams() {
    switch (protocol) {
    case CD:
      return this.fgDeliveryCd;
    case SFTP:
      return this.fgDeliverySftp;
    case SSH:
      return this.fgDeliverySsh;
    case MBOX:
      return this.fgDeliveryMbox;
    case OFTP:
      return this.fgDeliveryOftp;
    case BUBA:
      return this.fgDeliveryBuba;
    case EXT:
      return this.fgDeliveryExt;
    case FTPS:
      return this.fgDeliveryFtps;
    case AWSS3:
      return this.fgDeliveryAwsS3;
    default:
      return null;
    }
  }

  public void setDeliveryParams(AbstractSfgObject dparam) throws ApiException {
    if (dparam instanceof FgDeliveryCd) {
      setFgDeliveryCd((FgDeliveryCd) dparam);
    } else if (dparam instanceof FgDeliverySftp) {
      setFgDeliverySftp((FgDeliverySftp) dparam);
    } else if (dparam instanceof FgDeliveryMbox) {
      setFgDeliveryMbox((FgDeliveryMbox) dparam);
    } else if (dparam instanceof FgDeliveryAwsS3) {
      setFgDeliveryAwsS3((FgDeliveryAwsS3) dparam);
    } else if (dparam instanceof FgDeliveryOftp) {
      setFgDeliveryOftp((FgDeliveryOftp) dparam);
    } else if (dparam instanceof FgDeliveryBuba) {
      setFgDeliveryBuba((FgDeliveryBuba) dparam);
    } else if (dparam instanceof FgDeliveryExt) {
      setFgDeliveryExt((FgDeliveryExt) dparam);
    } else if (dparam instanceof FgDeliverySsh) {
      setFgDeliverySsh((FgDeliverySsh) dparam);
    } else if (dparam instanceof FgDeliveryFtps) {
      setFgDeliveryFtps((FgDeliveryFtps) dparam);
    } else {
      throw new ApiException("Invalid delivery parameters object: " + dparam.getClass().getName());
    }
  }

  public FgDeliveryBuba getFgDeliveryBuba() {
    return this.fgDeliveryBuba;
  }

  public void setFgDeliveryBuba(FgDeliveryBuba dparam) {
    if (dparam != null) {
      setProtocol(dparam.getProtocol());
      dparam.setFgDelivery(this);
    }
    this.fgDeliveryBuba = dparam;
  }

  public FgDeliveryCd getFgDeliveryCd() {
    return this.fgDeliveryCd;
  }

  public void setFgDeliveryCd(FgDeliveryCd dparam) {
    if (dparam != null) {
      setProtocol(dparam.getProtocol());
      dparam.setFgDelivery(this);
    }
    this.fgDeliveryCd = dparam;
  }

  public FgDeliveryMbox getFgDeliveryMbox() {
    return this.fgDeliveryMbox;
  }

  public void setFgDeliveryMbox(FgDeliveryMbox dparam) {
    if (dparam != null) {
      setProtocol(dparam.getProtocol());
      dparam.setFgDelivery(this);
    }
    this.fgDeliveryMbox = dparam;
  }

  public FgDeliveryOftp getFgDeliveryOftp() {
    return this.fgDeliveryOftp;
  }

  public void setFgDeliveryOftp(FgDeliveryOftp dparam) {
    if (dparam != null) {
      setProtocol(dparam.getProtocol());
      dparam.setFgDelivery(this);
    }
    this.fgDeliveryOftp = dparam;
  }

  public FgDeliverySftp getFgDeliverySftp() {
    return this.fgDeliverySftp;
  }

  public void setFgDeliverySftp(FgDeliverySftp dparam) {
    if (dparam != null) {
      setProtocol(dparam.getProtocol());
      dparam.setFgDelivery(this);
    }
    this.fgDeliverySftp = dparam;
  }

  public FgDeliverySsh getFgDeliverySsh() {
    return this.fgDeliverySsh;
  }

  public void setFgDeliverySsh(FgDeliverySsh dparam) {
    if (dparam != null) {
      setProtocol(dparam.getProtocol());
      dparam.setFgDelivery(this);
    }
    this.fgDeliverySsh = dparam;
  }

  public FgDeliveryExt getFgDeliveryExt() {
    return this.fgDeliveryExt;
  }

  public void setFgDeliveryExt(FgDeliveryExt dparam) {
    if (dparam != null) {
      setProtocol(dparam.getProtocol());
      dparam.setFgDelivery(this);
    }
    this.fgDeliveryExt = dparam;
  }

  public FgDeliveryAwsS3 getFgDeliveryAwsS3() {
    return this.fgDeliveryAwsS3;
  }

  public void setFgDeliveryAwsS3(FgDeliveryAwsS3 dparam) {
    if (dparam != null) {
      setProtocol(dparam.getProtocol());
      dparam.setFgDelivery(this);
    }
    this.fgDeliveryAwsS3 = dparam;
  }

  public FgDeliveryFtps getFgDeliveryFtps() {
    return this.fgDeliveryFtps;
  }

  public void setFgDeliveryFtps(FgDeliveryFtps dparam) {
    if (dparam != null) {
      setProtocol(dparam.getProtocol());
      dparam.setFgDelivery(this);
    }
    this.fgDeliveryFtps = dparam;
  }

  public String getDeliverySchedule() {
    return this.deliverySchedule;
  }

  public void setDeliverySchedule(String deliverySchedule) {
    this.deliverySchedule = deliverySchedule;
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

  public String getCslmLogTenant() {
    return cslmLogTenant;
  }

  public void setCslmLogTenant(String cslmLogTenant) {
    this.cslmLogTenant = cslmLogTenant;
  }

  public boolean isResolvePaths() {
    return resolvePaths;
  }

  public void setResolvePaths(boolean resolvePaths) {
    this.resolvePaths = resolvePaths;
  }

  public int getMaxRuntimeSeconds() {
    return maxRuntimeSeconds;
  }

  public void setMaxRuntimeSeconds(int maxRuntimeSeconds) {
    this.maxRuntimeSeconds = maxRuntimeSeconds;
  }

  public int getDelaySeconds() {
    return delaySeconds;
  }

  public void setDelaySeconds(int delaySeconds) {
    this.delaySeconds = delaySeconds;
  }

  @Override
  public boolean pointsToSame(AbstractSfgObject obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof FgDelivery))
      return false;
    FgDelivery other = (FgDelivery) obj;
    return consumer.pointsToSame(other.consumer) && fgFiletype.pointsToSame(other.fgFiletype) && fgTransfer.pointsToSame(other.fgTransfer)
        && protocol == other.protocol && Objects.equals(sndFilename, other.sndFilename);
  }

  @Override
  public Map<String, Object> getIdentityFields() {
    final Map<String, Object> idmap = new HashMap<>();
    idmap.put("fgTransfer", fgTransfer.getIdentityFields());
    idmap.put("consumer", consumer.getIdentityFields());
    return idmap;
  }

  @Override
  public String getShortId() {
    return getFgTransfer().getShortId() + " -> [" + consumer.getCustomerId() + "]" + sndFilename + "(" + protocol + ") [ID=" + fgDeliveryId + "]";
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  public String getKey() {
    return fgDeliveryId == 0 ? null : Long.toString(fgDeliveryId);
  }

  @Override
  public String toString() {
    return "FgDelivery [fgDeliveryId=" + fgDeliveryId + ", consumer=" + (consumer != null ? consumer.getCustomerId() : null) + ", sndFilename=" + sndFilename
        + ", resolvePaths=" + resolvePaths + ", protocol=" + protocol + ", cslmLogTenant=" + cslmLogTenant + ", delaySeconds=" + delaySeconds
        + ", deliverySchedule=" + deliverySchedule + ", consumerOs=" + consumerOs + ", postProcessCmd=" + postProcessCmd + ", maxRuntimeSeconds="
        + maxRuntimeSeconds + ", fgFiletype=" + (fgFiletype != null ? fgFiletype.getFiletype() : null) + ", fgDeliveryBuba=" + fgDeliveryBuba
        + ", fgDeliveryCd=" + fgDeliveryCd + ", fgDeliveryMbox=" + fgDeliveryMbox + ", fgDeliveryOftp=" + fgDeliveryOftp + ", fgDeliverySftp=" + fgDeliverySftp
        + ", fgDeliverySsh=" + fgDeliverySsh + ", fgDeliveryExt=" + fgDeliveryExt + ", fgDeliveryAwsS3=" + fgDeliveryAwsS3 + ", fgDeliveryFtps="
        + fgDeliveryFtps + super.toString() + "]";
  }
}
