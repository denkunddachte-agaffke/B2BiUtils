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
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.denkunddachte.enums.FTPartnerType;
import de.denkunddachte.enums.FTProtocol;
import de.denkunddachte.enums.MigrationPath;
import de.denkunddachte.jpa.PreventAnyUpdate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.TypedQuery;

/**
 * The persistent class for the AZ_FG_ALL_TRANSFERS_V database table.
 * 
 */
@Entity
@EntityListeners(PreventAnyUpdate.class)
@Table(name = "AZ_FG_ALL_TRANSFERS_V")
@NamedQuery(name = "FgRules.findAll", query = "SELECT a FROM FgRules a")
@NamedQuery(name = "FgRules.findPattern",
    query = "SELECT a FROM FgRules a WHERE a.producerId LIKE :producerId AND a.rcvFilepattern LIKE :rcvFilepattern")
@NamedQuery(name = "FgRules.findByConsumer", query = "SELECT a FROM FgRules a WHERE a.consumerId LIKE :consumerId")
@NamedQuery(name = "FgRules.findPatternIgnoreCase",
    query = "SELECT a FROM FgRules a WHERE upper(a.producerId) LIKE upper(:producerId) AND upper(a.rcvFilepattern) LIKE upper(:rcvFilepattern)")
@NamedQuery(name = "FgRules.findByConsumerIgnoreCase", query = "SELECT a FROM FgRules a WHERE upper(a.consumerId) LIKE upper(:consumerId)")
public class FgRules implements Serializable {
  private static final Logger LOGGER           = Logger.getLogger(FgRules.class.getName());
  private static final long   serialVersionUID = 1L;

  @Column(name = "ANTIVIR_CHECK")
  private boolean             antivirCheck;

  @Column(name = "C_CREATE_TIME")
  private Timestamp           consumerCreateTime;

  @Column(name = "C_CREATED_BY")
  private String              consumerCreatedBy;

  @Column(name = "C_ENABLED")
  private boolean             consumerEnabled;

  @Column(name = "C_LAST_ACTIVE")
  private Timestamp           consumerLastActive;

  @Deprecated
  @Enumerated(EnumType.STRING)
  @Column(name = "C_MIGRATION_PATH", length = 10)
  private MigrationPath       consumerMigrationPath;

  @Column(name = "C_MODIFIED_BY")
  private String              consumerModifiedBy;

  @Column(name = "C_MODIFY_TIME")
  private Timestamp           consumerModifyTime;

  @Column(name = "C_NAME")
  private String              consumerName;

  @Column(name = "C_ORIGIN")
  private String              consumerOrigin;

  @Enumerated(EnumType.STRING)
  @Column(name = "C_TYPE", length = 10)
  private FTPartnerType       consumerType;

  @Column(name = "CALC_PRIO")
  private int                 calculatedPriority;

  @Column(name = "CLIENT_ADAPTER_NAME")
  private String              clientAdapterName;

  @Column(name = "CONN_RETRIES")
  private String              connectionRetries;

  @Column(name = "CONN_RETRY_INTERVAL")
  private String              connectionRetryInterval;

  @Column(name = "CONN_TIMEOUT")
  private String              connectionTimeout;

  @Column(name = "CONSUMER")
  private String              consumerId;

  @Column(name = "CONSUMER_OS")
  private String              consumerOs;

  @Column(name = "CSLM_LOG_TENANT")
  private String              cslmLogTenant;

  @Column(name = "D_CREATE_TIME")
  private Timestamp           deliveryCreateTime;

  @Column(name = "D_CREATED_BY")
  private String              deliveryCreatedBy;

  @Column(name = "D_ENABLED")
  private boolean             deliveryEnabled;

  @Column(name = "D_LAST_ACTIVE")
  private Timestamp           deliveryLastActive;

  @Column(name = "D_MODIFIED_BY")
  private String              deliveryModifiedBy;

  @Column(name = "D_MODIFY_TIME")
  private Timestamp           deliveryModifyTime;

  @Column(name = "DATAU_ACK")
  private boolean             datauAck;

  @Column(name = "DISPOSITION")
  private String              disposition;

  @Column(name = "DP_CREATE_TIME")
  private Timestamp           deliveryParmCreateTime;

  @Column(name = "DP_CREATED_BY")
  private String              deliveryParmCreatedBy;

  @Column(name = "DP_MODIFIED_BY")
  private String              deliveryParmModifiedBy;

  @Column(name = "DP_MODIFY_TIME")
  private Timestamp           deliveryParmModifyTime;

  @Column(name = "FG_DELIVERY_ID")
  private long                fgDeliveryId;

  @Column(name = "FG_TRANS_ID")
  private long                fgTransId;

  @Column(name = "FILETYPE")
  private String              filetype;

  @Column(name = "HOSTNAME")
  private String              hostname;

  @Id
  @Column(name = "ID")
  private String              pseudoId;

  @Column(name = "P_CREATE_TIME")
  private Timestamp           producerCreateTime;

  @Column(name = "P_CREATED_BY")
  private String              producerCreatedBy;

  @Column(name = "P_ENABLED")
  private boolean             producerEnabled;

  @Column(name = "P_LAST_ACTIVE")
  private Timestamp           producerLastActive;

  @Column(name = "P_MODIFIED_BY")
  private String              producerModifiedBy;

  @Column(name = "P_MODIFY_TIME")
  private Timestamp           producerModifyTime;

  @Column(name = "P_NAME")
  private String              producerName;

  @Column(name = "P_ORIGIN")
  private String              producerOrigin;

  @Enumerated(EnumType.STRING)
  @Column(name = "P_TYPE", length = 10)
  private FTPartnerType       producerType;

  @Column(name = "POST_PROCESS_CMD")
  private String              postProcessCmd;

  @Column(name = "PRODUCER")
  private String              producerId;

  @Column(name = "PRODUCER_OS")
  private String              producerOs;

  @Enumerated(EnumType.STRING)
  @Column(name = "PROTOCOL")
  private FTProtocol          protocol;

  @Column(name = "PROTOCOL_OPTIONS")
  private String              protocolOptions;

  @Column(name = "RCV_FILEPATTERN")
  private String              rcvFilepattern;

  @Column(name = "SND_FILENAME")
  private String              sndFilename;

  @Column(name = "T_CREATE_TIME")
  private Timestamp           transferCreateTime;

  @Column(name = "T_CREATED_BY")
  private String              transferCreatedBy;

  @Column(name = "T_ENABLED")
  private boolean             transferEnabled;

  @Column(name = "T_LAST_ACTIVE")
  private Timestamp           transferLastActive;

  @Column(name = "T_MODIFIED_BY")
  private String              transferModifiedBy;

  @Column(name = "T_MODIFY_TIME")
  private Timestamp           transferModifyTime;

  @Column(name = "TMP_FILENAME")
  private String              tmpFilename;

  @Column(name = "USERNAME")
  private String              username;

  public FgRules() {
  }

  public boolean isAntivirCheck() {
    return antivirCheck;
  }

  public Timestamp getConsumerCreateTime() {
    return consumerCreateTime;
  }

  public String getConsumerCreatedBy() {
    return consumerCreatedBy;
  }

  public boolean isConsumerEnabled() {
    return consumerEnabled;
  }

  public Timestamp getConsumerLastActive() {
    return consumerLastActive;
  }

  public MigrationPath getConsumerMigrationPath() {
    return consumerMigrationPath;
  }

  public String getConsumerModifiedBy() {
    return consumerModifiedBy;
  }

  public Timestamp getConsumerModifyTime() {
    return consumerModifyTime;
  }

  public String getConsumerName() {
    return consumerName;
  }

  public String getConsumerOrigin() {
    return consumerOrigin;
  }

  public FTPartnerType getConsumerType() {
    return consumerType;
  }

  public int getCalculatedPriority() {
    return calculatedPriority;
  }

  public String getClientAdapterName() {
    return clientAdapterName;
  }

  public String getConnectionRetries() {
    return connectionRetries;
  }

  public String getConnectionRetryInterval() {
    return connectionRetryInterval;
  }

  public String getConnectionTimeout() {
    return connectionTimeout;
  }

  public String getConsumerId() {
    return consumerId;
  }

  public String getConsumerOs() {
    return consumerOs;
  }

  public String getCslmLogTenant() {
    return cslmLogTenant;
  }

  public Timestamp getDeliveryCreateTime() {
    return deliveryCreateTime;
  }

  public String getDeliveryCreatedBy() {
    return deliveryCreatedBy;
  }

  public boolean isDeliveryEnabled() {
    return deliveryEnabled;
  }

  public Timestamp getDeliveryLastActive() {
    return deliveryLastActive;
  }

  public String getDeliveryModifiedBy() {
    return deliveryModifiedBy;
  }

  public Timestamp getDeliveryModifyTime() {
    return deliveryModifyTime;
  }

  public boolean isDatauAck() {
    return datauAck;
  }

  public String getDisposition() {
    return disposition;
  }

  public Timestamp getDeliveryParmCreateTime() {
    return deliveryParmCreateTime;
  }

  public String getDeliveryParmCreatedBy() {
    return deliveryParmCreatedBy;
  }

  public String getDeliveryParmModifiedBy() {
    return deliveryParmModifiedBy;
  }

  public Timestamp getDeliveryParmModifyTime() {
    return deliveryParmModifyTime;
  }

  public long getFgDeliveryId() {
    return fgDeliveryId;
  }

  public long getFgTransId() {
    return fgTransId;
  }

  public String getFiletype() {
    return filetype;
  }

  public String getHostname() {
    return hostname;
  }

  public String getPseudoId() {
    return pseudoId;
  }

  public Timestamp getProducerCreateTime() {
    return producerCreateTime;
  }

  public String getProducerCreatedBy() {
    return producerCreatedBy;
  }

  public boolean isProducerEnabled() {
    return producerEnabled;
  }

  public Timestamp getProducerLastActive() {
    return producerLastActive;
  }

  public String getProducerModifiedBy() {
    return producerModifiedBy;
  }

  public Timestamp getProducerModifyTime() {
    return producerModifyTime;
  }

  public String getProducerName() {
    return producerName;
  }

  public String getProducerOrigin() {
    return producerOrigin;
  }

  public FTPartnerType getProducerType() {
    return producerType;
  }

  public String getPostProcessCmd() {
    return postProcessCmd;
  }

  public String getProducerId() {
    return producerId;
  }

  public String getProducerOs() {
    return producerOs;
  }

  public FTProtocol getProtocol() {
    return protocol;
  }

  public String getProtocolOptions() {
    return protocolOptions;
  }

  public String getRcvFilepattern() {
    return rcvFilepattern;
  }

  public String getSndFilename() {
    return sndFilename;
  }

  public Timestamp getTransferCreateTime() {
    return transferCreateTime;
  }

  public String getTransferCreatedBy() {
    return transferCreatedBy;
  }

  public boolean isTransferEnabled() {
    return transferEnabled;
  }

  public Timestamp getTransferLastActive() {
    return transferLastActive;
  }

  public String getTransferModifiedBy() {
    return transferModifiedBy;
  }

  public Timestamp getTransferModifyTime() {
    return transferModifyTime;
  }

  public String getTmpFilename() {
    return tmpFilename;
  }

  public String getUsername() {
    return username;
  }

  @Override
  public String toString() {
    return "FgRules [producerId=" + producerId + ", rcvFilepattern=" + rcvFilepattern + ", consumerId=" + consumerId + ", protocol=" + protocol + "]";
  }

  // static lookup
  public static List<FgRules> findAll(EntityManager em) {
    TypedQuery<FgRules> q      = em.createNamedQuery("FgRules.findAll", FgRules.class);
    List<FgRules>       result = q.getResultList();
    if (!result.isEmpty()) {
      LOGGER.log(Level.FINEST, "Result.size={0}", result.size());
    } else {
      LOGGER.log(Level.FINEST, "No patterns found.");
    }
    return result;
  }

  public static List<FgRules> findPatterns(String producer, String rcvFilename, boolean ignoreCase, EntityManager em) {
    TypedQuery<FgRules> q = em.createNamedQuery((ignoreCase ? "FgRules.findPatternIgnoreCase" : "FgRules.findPatternIgnoreCase"),
        FgRules.class);
    if (producer == null || producer.isEmpty()) {
      q.setParameter("producerId", "%");
    } else {
      q.setParameter("producerId", producer.replace('*', '%').replace('?', '_'));
    }
    if (rcvFilename == null || rcvFilename.isEmpty()) {
      q.setParameter("rcvFilepattern", "%");
    } else {
      q.setParameter("rcvFilepattern", rcvFilename.replace('*', '%').replace('?', '_'));
    }
    List<FgRules> result = q.getResultList();
    if (!result.isEmpty()) {
      LOGGER.log(Level.FINEST, "producer={0}, rcvFilename={1}: result.size={2}", new Object[] { producer, rcvFilename, result.size() });
    } else {
      LOGGER.log(Level.FINEST, "producer={0}, rcvFilename={1} not found.", new Object[] { producer, rcvFilename });
    }
    return result;
  }

  public static List<FgRules> findByConsumer(String consumer, boolean ignoreCase, EntityManager em) {
    TypedQuery<FgRules> q = em
        .createNamedQuery((ignoreCase ? "FgRules.findByConsumerIgnoreCase" : "FgRules.findByConsumerIgnoreCase"), FgRules.class);
    if (consumer == null || consumer.isEmpty()) {
      q.setParameter("consumerId", "%");
    } else {
      q.setParameter("consumerId", consumer.replace('*', '%').replace('?', '_'));
    }
    List<FgRules> result = q.getResultList();
    if (!result.isEmpty()) {
      LOGGER.log(Level.FINEST, "consumer={0}: result.size={1}", new Object[] { consumer, result.size() });
    } else {
      LOGGER.log(Level.FINEST, "consumer={0} not found.", consumer);
    }
    return result;
  }

  public static FgRules find(String producerId, String rcvFilepattern, boolean ignoreCase, EntityManager em) {
    List<FgRules> result = findPatterns(producerId, rcvFilepattern, ignoreCase, em);
    if (result.isEmpty()) {
      return null;
    } else {
      return result.get(0);
    }
  }
}
