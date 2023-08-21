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
import de.denkunddachte.jpa.AbstractSfgObject;

/**
 * The persistent class for the AZ_FG_DELIVERY_OFTP database table.
 * 
 */
@Entity
@Table(name = "AZ_FG_DELIVERY_OFTP")
@NamedQuery(name = "FgDeliveryOftp.findAll", query = "SELECT f FROM FgDeliveryOftp f")
public class FgDeliveryOftp extends AbstractSfgObject implements Serializable {
  private static final long      serialVersionUID = 1L;
  private static final Logger    LOGGER           = Logger.getLogger(FgDeliveryOftp.class.getName());
  public static final FTProtocol PROTOCOL         = FTProtocol.OFTP;

  @Column(name = "CLIENT_ADAPTER_NAME", nullable = false, length = 100)
  private String                 clientAdapterName;

  @Column(name = "LOG_PARTNER_CONTRACT", nullable = false, length = 100)
  private String                 logPartnerContract;

  @Column(name = "SEQUENTIAL_PROCESSING")
  private int                    sequentialProcessingWaitSeconds;

  // bi-directional one-to-one association to FgDelivery
  // CONSTRAINT AZ_FG_TR_OFTP_FK1 FOREIGN KEY (FG_DELIVERY_ID) REFERENCES AZ_FG_DELIVERY(FG_DELIVERY_ID) ON DELETE CASCADE
  @Id
  @OneToOne
  @JoinColumn(name = "FG_DELIVERY_ID")
  private FgDelivery             fgDelivery;

  public FgDeliveryOftp() {
  }

  public FgDeliveryOftp(FgDelivery fgd) {
    this();
    this.fgDelivery = fgd;
  }

  public FTProtocol getProtocol() {
    return FTProtocol.OFTP;
  }

  public String getClientAdapterName() {
    return this.clientAdapterName;
  }

  public void setClientAdapterName(String clientAdapterName) {
    this.clientAdapterName = clientAdapterName;
  }

  public String getLogPartnerContract() {
    return this.logPartnerContract;
  }

  public void setLogPartnerContract(String logPartnerContract) {
    this.logPartnerContract = logPartnerContract;
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
    if (!(obj instanceof FgDeliveryOftp))
      return false;
    FgDeliveryOftp other = (FgDeliveryOftp) obj;
    return Objects.equals(logPartnerContract, other.logPartnerContract) && Objects.equals(clientAdapterName, other.clientAdapterName);
  }

  @Override
  public FgDeliveryOftp createCopy() {
    FgDeliveryOftp copy = new FgDeliveryOftp();
    copy.clientAdapterName = this.clientAdapterName;
    copy.logPartnerContract = this.logPartnerContract;
    copy.sequentialProcessingWaitSeconds = this.sequentialProcessingWaitSeconds;
    return copy;
  }

  @Override
  public String getKey() {
    return fgDelivery == null ? null : fgDelivery.getKey();
  }

  @Override
  public String toString() {
    return "FgDeliveryOftp [fgDeliveryId=" + fgDelivery.getFgDeliveryId() + ", clientAdapterName=" + clientAdapterName + ", logPartnerContract="
        + logPartnerContract + ", sequentialProcessingWaitSeconds=" + sequentialProcessingWaitSeconds + super.toString() + "]";
  }

}
