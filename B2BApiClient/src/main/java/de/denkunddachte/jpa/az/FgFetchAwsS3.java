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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import de.denkunddachte.enums.FTProtocol;
import de.denkunddachte.jpa.AbstractSfgObject;

/**
 * The persistent class for the AZ_FG_DELIVERY_AWSS3 database table.
 * 
 */
@Entity
@Table(name = "AZ_FG_FETCH_AWSS3")
@NamedQuery(name = "FgFetchAwsS3.findAll", query = "SELECT f FROM FgFetchAwsS3 f")
@NamedQuery(name = "FgFetchAwsS3.findByProducer", query = "SELECT f FROM FgFetchAwsS3 f WHERE f.producer.customerId = :producer")
public class FgFetchAwsS3 extends AbstractSfgObject implements Serializable {
  private static final long      serialVersionUID = 1L;
  private static final Logger    LOGGER           = Logger.getLogger(FgFetchAwsS3.class.getName());
  public static final FTProtocol PROTOCOL         = FTProtocol.AWSS3;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  // @GeneratedValue(generator = "FgFetchId")
  // @SequenceGenerator(name = "FgFetchId", sequenceName = "AZ_SEQ_FG_FETCHID", allocationSize = 1)
  @Column(name = "FG_FETCH_ID", unique = true, nullable = false)
  private long                   fgFetchId;

  @Column(name = "FILEPATTERN", nullable = false, length = 120)
  private String                 filepattern;

  @Column(name = "IS_REGEX", precision = 1)
  private boolean                isRegex;

  @Column(name = "SCHEDULE_NAME", nullable = false, length = 50)
  private String                 scheduleName;

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

  @Column(name = "MAX_TRANS_TIME", nullable = false)
  private int                    maxTransferTimeMinutes;

  @Column(name = "PRIORITY", nullable = true)
  private int                    priority;

  @Column(name = "KEEP_FILE", precision = 1)
  private boolean                keepFile;

  @Column(name = "ENABLED", precision = 1)
  private boolean                enabled;

  @Column(name = "LAST_ATTEMPT", updatable = false, insertable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date                   lastAttempt;

  @Column(name = "LAST_ACTIVE", updatable = false, insertable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date                   lastActive;

  @Column(name = "ERROR_COUNT", nullable = false, updatable = false, insertable = false)
  private int                    errorCount;

  @Column(name = "LAST_ERROR", nullable = true, updatable = false, insertable = false)
  private String                 lastError;

  @Column(name = "USE_EXTERNAL_CMD", precision = 1)
  private boolean                useExternalCmd;

  @Column(name = "EXTERNAL_CMD", length = 8000)
  private String                 externalCmd;

  // bi-directional many-to-one association to FgCustomer
  // CONSTRAINT AZ_FG_FTCH_SFTP_FK1 FOREIGN KEY (PRODUCER_ID) REFERENCES
  // AZ_FG_CUSTOMER(FG_CUST_ID) ON DELETE CASCADE
  @ManyToOne
  @JoinColumn(name = "PRODUCER_ID", nullable = false)
  private FgCustomer             producer;

  public FgFetchAwsS3() {
    this.maxTransferTimeMinutes = 240;
    this.priority = 100;
    this.enabled = true;
    this.keepFile = false;
  }

  public FgFetchAwsS3(String bucketName, String accessKey, String secretKey, String filepattern, String scheduleName) {
    this();
    this.bucketName = bucketName;
    this.accessKey = accessKey;
    this.secretKey = secretKey;
    this.filepattern = filepattern;
    this.scheduleName = scheduleName;
  }

  public long getFgFetchId() {
    return this.fgFetchId;
  }

  public void setFgFetchId(long fgFetchId) {
    this.fgFetchId = fgFetchId;
  }

  public String getFilepattern() {
    return filepattern;
  }

  public void setFilepattern(String filepattern) {
    this.filepattern = filepattern;
  }

  public String getScheduleName() {
    return scheduleName;
  }

  public void setScheduleName(String scheduleName) {
    this.scheduleName = scheduleName;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public int getMaxTransferTimeMinutes() {
    return maxTransferTimeMinutes;
  }

  public void setMaxTransferTimeMinutes(int maxTransferTimeMinutes) {
    this.maxTransferTimeMinutes = maxTransferTimeMinutes;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public Date getLastActive() {
    return lastActive;
  }

  public void setLastActive(Date lastActiveTime) {
    lastActive = lastActiveTime;
  }

  public FgCustomer getProducer() {
    return this.producer;
  }

  public void setProducer(FgCustomer producer) {
    this.producer = producer;
  }

  public boolean isKeepFile() {
    return keepFile;
  }

  public void setKeepFile(boolean keepFile) {
    this.keepFile = keepFile;
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

  public boolean isRegex() {
    return isRegex;
  }

  public void setRegex(boolean isRegex) {
    this.isRegex = isRegex;
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

  public Date getLastAttempt() {
    return lastAttempt;
  }

  public int getErrorCount() {
    return errorCount;
  }

  public String getLastError() {
    return lastError;
  }

  @Override
  public Map<String, Object> getIdentityFields() {
    final Map<String, Object> idmap = new HashMap<>();
    idmap.put("producer", producer.getIdentityFields());
    idmap.put("filepattern", filepattern);
    return idmap;
  }

  @Override
  public String getShortId() {
    return "[" + getProducer().getCustomerId() + "]" + filepattern + " (" + scheduleName + ") [ID=" + fgFetchId + "]";
  }

  @Override
  public boolean pointsToSame(AbstractSfgObject obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof FgFetchAwsS3))
      return false;
    FgFetchAwsS3 other = (FgFetchAwsS3) obj;
    return Objects.equals(accessKey, other.accessKey) && Objects.equals(bucketName, other.bucketName) && Objects.equals(filepattern, other.filepattern)
        && isRegex == other.isRegex && producer.pointsToSame(other.producer) && Objects.equals(secretKey, other.secretKey);
  }

  @Override
  public FgFetchAwsS3 createCopy() {
    FgFetchAwsS3 copy = new FgFetchAwsS3();
    copy.bucketName = this.bucketName;
    copy.accessKey = this.accessKey;
    copy.secretKey = this.secretKey;
    copy.iamUserName = this.iamUserName;
    copy.region = this.region;
    copy.endpoint = this.endpoint;
    copy.maxTransferTimeMinutes = this.maxTransferTimeMinutes;
    copy.priority = this.priority;
    copy.keepFile = this.keepFile;
    copy.enabled = this.enabled;
    copy.lastAttempt = this.lastAttempt;
    copy.lastActive = this.lastActive;
    copy.errorCount = this.errorCount;
    copy.lastError = this.lastError;
    copy.useExternalCmd = this.useExternalCmd;
    copy.externalCmd = this.externalCmd;
    return copy;
  }

  @Override
  public String getKey() {
    return fgFetchId == 0 ? null : Long.toString(fgFetchId);
  }

  @Override
  public String toString() {
    return "FgFetchAwsS3 [fgFetchId=" + fgFetchId + ", producer=" + (producer != null ? producer.getCustomerId() : null) + ", filepattern=" + filepattern
        + ", isRegex=" + isRegex + ", scheduleName=" + scheduleName + ", bucketName=" + bucketName + ", accessKey=" + accessKey + ", secretKey="
        + (secretKey == null ? "no" : "yes") + ", iamUserName=" + iamUserName + ", region=" + region + ", endpoint=" + endpoint + ", maxTransferTimeMinutes="
        + maxTransferTimeMinutes + ", priority=" + priority + ", keepFile=" + keepFile + ", enabled=" + enabled + ", lastAttempt=" + lastAttempt
        + ", lastActive=" + lastActive + ", errorCount=" + errorCount + ", lastError=" + lastError + ", useExternalCmd=" + useExternalCmd + ", externalCmd="
        + (useExternalCmd ? ", externalCmd=" + externalCmd : "") + "]";
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

}
