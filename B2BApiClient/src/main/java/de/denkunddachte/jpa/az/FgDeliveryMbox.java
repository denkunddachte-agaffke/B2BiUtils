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
import de.denkunddachte.jpa.AbstractSfgObject;

/**
 * The persistent class for the AZ_FG_DELIVERY_MBOX database table.
 * 
 */
@Entity
@Table(name = "AZ_FG_DELIVERY_MBOX")
@NamedQuery(name = "FgDeliveryMbox.findAll", query = "SELECT f FROM FgDeliveryMbox f")
public class FgDeliveryMbox extends AbstractSfgObject implements Serializable {
  private static final long      serialVersionUID = 1L;
  private static final Logger    LOGGER           = Logger.getLogger(FgDeliveryMbox.class.getName());
  public static final FTProtocol PROTOCOL         = FTProtocol.MBOX;

  @Enumerated(EnumType.STRING)
  @Column(name = "DISPOSITION", nullable = false, length = 20)
  private FileDisposition        disposition;

  @Column(name = "EXTRACTABILITY_COUNT", nullable = false)
  private int                    extractabilityCount;

  // bi-directional one-to-one association to FgDelivery
  // CONSTRAINT AZ_FG_TR_MBOX_FK1 FOREIGN KEY (FG_DELIVERY_ID) REFERENCES
  // AZ_FG_DELIVERY(FG_DELIVERY_ID) ON DELETE CASCADE
  @Id
  @OneToOne
  @JoinColumn(name = "FG_DELIVERY_ID")
  private FgDelivery             fgDelivery;

  public FgDeliveryMbox() {
    this.extractabilityCount = 999;
    this.disposition = FileDisposition.NEW;
  }

  public FgDeliveryMbox(FgDelivery fgd) {
    this();
    this.fgDelivery = fgd;
  }

  public FTProtocol getProtocol() {
    return FTProtocol.MBOX;
  }

  public FileDisposition getDisposition() {
    return this.disposition;
  }

  public void setDisposition(FileDisposition disposition) throws ApiException {
    switch (disposition) {
    case RPL:
    case NEW:
    case APPEND:
    case TIMESTAMP:
      this.disposition = disposition;
      break;
    default:
      throw new ApiException("Invalid remote disposition for MBOX: " + disposition);
    }
  }

  public int getExtractabilityCount() {
    return this.extractabilityCount;
  }

  public void setExtractabilityCount(int extractabilityCount) {
    if (this.extractabilityCount != extractabilityCount)
      this.extractabilityCount = extractabilityCount;
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
    return obj instanceof FgDeliveryMbox;
  }

  @Override
  public FgDeliveryMbox createCopy() {
    FgDeliveryMbox copy = new FgDeliveryMbox();
    copy.disposition = this.disposition;
    copy.extractabilityCount = this.extractabilityCount;
    return copy;
  }

  @Override
  public String getKey() {
    return fgDelivery == null ? null : fgDelivery.getKey();
  }

  @Override
  public String toString() {
    return "FgDeliveryMbox [fgDeliveryId=" + fgDelivery.getFgDeliveryId() + ", disposition=" + disposition + ", extractabilityCount=" + extractabilityCount
        + super.toString() + "]";
  }

}
