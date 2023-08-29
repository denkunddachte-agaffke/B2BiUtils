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
 * The persistent class for the AZ_FG_DELIVERY_AWSS3 database table.
 * 
 */
@Entity
@Table(name = "AZ_FG_DELIVERY_AWSS3")
@NamedQuery(name = "FgDeliveryAwsS3.findAll", query = "SELECT f FROM FgDeliveryAwsS3 f")
public class FgDeliveryAwsS3 extends AbstractSfgObject implements Serializable {
  private static final long      serialVersionUID = 1L;
  private static final Logger    LOGGER           = Logger.getLogger(FgDeliveryAwsS3.class.getName());
  public static final FTProtocol PROTOCOL         = FTProtocol.AWSS3;

  @Column(name = "BUCKETNAME", nullable = false, length = 63)
  private String                 bucketName;

  @Column(name = "ACCESSKEY", nullable = false, length = 128)
  private String                 accessKey;

  @Column(name = "SECRETKEY", length = 128)
  private String                 secretKey;

  @Column(name = "IAM_USERNAME", length = 32)
  private String                 iamUserName;

  @Column(name = "REGION", length = 30)
  private String                 region;

  @Column(name = "ENDPOINT", length = 100)
  private String                 endpoint;

  @Column(name = "USE_EXTERNAL_CMD", precision = 1)
  private boolean                useExternalCmd;

  @Column(name = "EXTERNAL_CMD", length = 8000)
  private String                 externalCmd;

  // bi-directional one-to-one association to FgDelivery
  // CONSTRAINT AZ_FG_TR_SFTP_FK1 FOREIGN KEY (FG_DELIVERY_ID) REFERENCES
  // AZ_FG_DELIVERY(FG_DELIVERY_ID) ON DELETE CASCADE
  @Id
  @OneToOne
  @JoinColumn(name = "FG_DELIVERY_ID")
  private FgDelivery             fgDelivery;

  public FgDeliveryAwsS3() {
    super();
  }

  public FgDeliveryAwsS3(FgDelivery fgd) {
    this();
    this.fgDelivery = fgd;
  }

  public FTProtocol getProtocol() {
    return FTProtocol.AWSS3;
  }

  public String getBucketName() {
    return bucketName;
  }

  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getIamUserName() {
    return iamUserName;
  }

  public void setIamUserName(String iamUserName) {
    this.iamUserName = iamUserName;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
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
    if (!(obj instanceof FgDeliveryAwsS3))
      return false;
    FgDeliveryAwsS3 other = (FgDeliveryAwsS3) obj;
    return Objects.equals(accessKey, other.accessKey) && Objects.equals(bucketName, other.bucketName) && Objects.equals(secretKey, other.secretKey);
  }

  @Override
  public FgDeliveryAwsS3 createCopy() {
    FgDeliveryAwsS3 copy = new FgDeliveryAwsS3();
    copy.bucketName = this.bucketName;
    copy.accessKey = this.accessKey;
    copy.secretKey = this.secretKey;
    copy.iamUserName = this.iamUserName;
    copy.region = this.region;
    copy.endpoint = this.endpoint;
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
    return "FgDeliveryAwsS3 [fgDeliveryId=" + fgDelivery.getFgDeliveryId() + ", bucketName=" + bucketName + ", accessKey=" + accessKey + ", secretKey="
        + (secretKey == null ? "no" : "yes") + ", iamUserName=" + iamUserName + ", region=" + region + ", endpoint=" + endpoint + ", useExternalCmd=" + useExternalCmd + ", externalCmd="
        + (useExternalCmd ? ", externalCmd=" + externalCmd : "") + super.toString() + "]";
  }
}
