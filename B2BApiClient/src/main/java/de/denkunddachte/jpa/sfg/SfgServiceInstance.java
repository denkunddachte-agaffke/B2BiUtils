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
package de.denkunddachte.jpa.sfg;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import org.eclipse.persistence.annotations.ReadOnly;

@Deprecated
@Entity
@Table(name = "SERVICE_INSTANCE")
@NamedQuery(name = "SfgServiceInstance.findAllCustomAdapter",
    query = "SELECT f FROM SfgServiceInstance f WHERE f.serviceName LIKE 'A_\\_%' ESCAPE '\\' OR f.serviceGroupName LIKE 'A_\\_%' ESCAPE '\\'")
@NamedQuery(name = "SfgServiceInstance.findCDSA",
    query = "SELECT f FROM SfgServiceInstance f WHERE f.serviceName LIKE 'A_\\_CDSA%' ESCAPE '\\' OR f.serviceGroupName LIKE 'A_\\_CDSA%' ESCAPE '\\'")
@NamedQuery(name = "SfgServiceInstance.findSFTPCA",
    query = "SELECT f FROM SfgServiceInstance f WHERE f.serviceName LIKE 'A_\\_SFTP\\_C%' ESCAPE '\\' OR f.serviceGroupName LIKE 'A_\\_SFTP\\_C%' ESCAPE '\\'")
@NamedQuery(name = "SfgServiceInstance.findByName",
    query = "SELECT f FROM SfgServiceInstance f WHERE f.serviceName = :serviceName OR f.serviceGroupName = :serviceName")
@ReadOnly
public class SfgServiceInstance implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "SERVICE_ID", unique = true, nullable = false)
  private int               serviceId;

  @Column(name = "SERVICE_NAME", nullable = false, length = 100)
  private String            serviceName;

  @Column(name = "DISPLAY_NAME", nullable = false, length = 100)
  private String            displayName;

  @Column(name = "DEF_ID", nullable = false)
  private int               defId;

  @Column(name = "DEF_NAME", nullable = false, length = 100)
  private String            defName;

  @Column(name = "DESCRIPTION", nullable = true, length = 255)
  private String            description;

  @Column(name = "PARM_VERSION", nullable = false)
  private int               parmVersion;

  @Column(name = "CREATE_DATE", nullable = false, length = 100)
  private String            createDate;

  @Column(name = "LOCK_STATUS", nullable = false)
  private int               lockStatus;

  @Column(name = "ACTIVE_STATUS", nullable = false)
  private int               activeStatus;

  @Column(name = "SYSTEM_SERVICE", nullable = false)
  private int               systemService;

  @Column(name = "TARGET_ENV", nullable = false, length = 255)
  private String            targetEnv;

  @Column(name = "SERVICE_GROUP_NAME", nullable = true, length = 100)
  private String            serviceGroupName;

  @Column(name = "ORGANIZATION_KEY", nullable = false, length = 255)
  private String            orginizationKey;

  public SfgServiceInstance() {
  }

  public String getServiceName() {
    return serviceName;
  }

  public int getServiceId() {
    return serviceId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public int getDefId() {
    return defId;
  }

  public String getDefName() {
    return defName;
  }

  public String getDescription() {
    return description;
  }

  public int getParmVersion() {
    return parmVersion;
  }

  public String getCreateDate() {
    return createDate;
  }

  public int getLockStatus() {
    return lockStatus;
  }

  public int getActiveStatus() {
    return activeStatus;
  }

  public int getSystemService() {
    return systemService;
  }

  public String getTargetEnv() {
    return targetEnv;
  }

  public String getServiceGroupName() {
    return serviceGroupName;
  }

  public String getOrginizationKey() {
    return orginizationKey;
  }

  @Override
  public String toString() {
    return "ID=" + serviceId + ", " + (serviceGroupName != null ? "serviceGroup=" + serviceGroupName : "name=" + serviceName) + ", (" + displayName + ")";
  }

}
