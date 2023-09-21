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

import de.denkunddachte.jpa.PreventAnyUpdate;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.TypedQuery;

/**
 * The persistent class for the AZ_FG_ALL_FETCHCFG_V database table.
 * 
 */
@Entity
@EntityListeners(PreventAnyUpdate.class)
@Table(name = "AZ_FG_ALL_FETCHCFG_V")
@NamedQuery(name = "FgFetchRule.findAll", query = "SELECT f FROM FgFetchRule f")
@NamedQuery(name = "FgFetchRule.findByProducerId", query = "SELECT f FROM FgFetchRule f WHERE f.fgProducerId = :fgProducerId")
public class FgFetchRule implements Serializable {
  private static final Logger LOGGER           = Logger.getLogger(FgFetchRule.class.getName());
  private static final long   serialVersionUID = 1L;

  @Column(name = "CLIENT_ADAPTER_NAME")
  private String              clientAdapterName;

  @Column(name = "CREATE_TIME")
  private Timestamp           createTime;

  @Column(name = "CREATED_BY")
  private String              createdBy;

  @Column(name = "ENABLED")
  private boolean             enabled;

  @Column(name = "ERROR_COUNT")
  private int                 errorCount;

  @Column(name = "EXTERNAL_CMD")
  private String              externalCmd;

  @EmbeddedId
  private FgFetchRuleId       fgFetchRuleId;

  @Column(name = "FILEPATTERN")
  private String              filepattern;

  @Column(name = "HOSTNAME")
  private String              hostname;

  @Column(name = "IS_REGEX")
  private boolean             isRegex;

  @Column(name = "KEEP_FILE")
  private boolean             keepFile;

  @Column(name = "LAST_ACTIVE")
  private Timestamp           lastActive;

  @Column(name = "LAST_ATTEMPT")
  private Timestamp           lastAttempt;

  @Column(name = "LAST_ERROR")
  private String              lastError;

  @Column(name = "MAX_TRANS_TIME")
  private int                 maxTransTime;

  @Column(name = "MODIFIED_BY")
  private String              modifiedBy;

  @Column(name = "MODIFY_TIME")
  private Timestamp           modifyTime;

  @Column(name = "PRIORITY")
  private int                 priority;

  @Column(name = "PRODUCER_ID")
  private long                fgProducerId;

  @Column(name = "PROTOCOL_OPTIONS")
  private String              protocolOptions;

  @Column(name = "SCHEDULE_NAME")
  private String              scheduleName;

  @Column(name = "USE_EXTERNAL_CMD")
  private boolean             useExternalCmd;

  @Column(name = "USERNAME")
  private String              username;

  public FgFetchRule() {
  }

  public String getClientAdapterName() {
    return clientAdapterName;
  }

  public Timestamp getCreateTime() {
    return createTime;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public int getErrorCount() {
    return errorCount;
  }

  public String getExternalCmd() {
    return externalCmd;
  }

  public FgFetchRuleId getFgFetchRuleId() {
    return fgFetchRuleId;
  }

  public String getFilepattern() {
    return filepattern;
  }

  public String getHostname() {
    return hostname;
  }

  public boolean isRegex() {
    return isRegex;
  }

  public boolean isKeepFile() {
    return keepFile;
  }

  public Timestamp getLastActive() {
    return lastActive;
  }

  public Timestamp getLastAttempt() {
    return lastAttempt;
  }

  public String getLastError() {
    return lastError;
  }

  public int getMaxTransTime() {
    return maxTransTime;
  }

  public String getModifiedBy() {
    return modifiedBy;
  }

  public Timestamp getModifyTime() {
    return modifyTime;
  }

  public int getPriority() {
    return priority;
  }

  public long getFgProducerId() {
    return fgProducerId;
  }

  public String getProtocolOptions() {
    return protocolOptions;
  }

  public String getScheduleName() {
    return scheduleName;
  }

  public boolean isUseExternalCmd() {
    return useExternalCmd;
  }

  public String getUsername() {
    return username;
  }

  // static lookup
  public static List<FgFetchRule> findAll(EntityManager em) {
    TypedQuery<FgFetchRule> q      = em.createNamedQuery("FgFetchRule.findAll", FgFetchRule.class);
    List<FgFetchRule>       result = q.getResultList();
    if (!result.isEmpty()) {
      LOGGER.log(Level.FINEST, "Result.size={0}", result.size());
    } else {
      LOGGER.log(Level.FINEST, "No patterns found.");
    }
    return result;
  }

  public static List<FgFetchRule> findByProducer(long fgProducerId, EntityManager em) {
    TypedQuery<FgFetchRule> q = em.createNamedQuery("FgFetchRule.findByProducerId", FgFetchRule.class);
    q.setParameter("fgProducerId", fgProducerId);
    List<FgFetchRule> result = q.getResultList();
    if (!result.isEmpty()) {
      LOGGER.log(Level.FINEST, "fgProducerId={0}: result.size={2}", new Object[] { fgProducerId, result.size() });
    } else {
      LOGGER.log(Level.FINEST, "fgProducerId={0} not found.", fgProducerId);
    }
    return result;
  }

  @Embeddable
  public static class FgFetchRuleId implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column(name = "TYPE")
    private String type;
    @Column(name = "FG_FETCH_ID")
    private long   fgFetchId;

    public FgFetchRuleId() {
    }

    public FgFetchRuleId(String type, long fgFetchId) {
      super();
      this.type = type;
      this.fgFetchId = fgFetchId;
    }

    public String getType() {
      return type;
    }

    public long getFgFetchId() {
      return fgFetchId;
    }

    @Override
    public String toString() {
      return "FgFetchRuleId [type=" + type + ", fgFetchId=" + fgFetchId + "]";
    }
  }
}
