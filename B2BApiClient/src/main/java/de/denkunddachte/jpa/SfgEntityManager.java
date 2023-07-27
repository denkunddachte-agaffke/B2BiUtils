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
package de.denkunddachte.jpa;

import static org.eclipse.persistence.config.PersistenceUnitProperties.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.Persistence;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.eclipse.persistence.logging.SessionLog;

import de.denkunddachte.util.ApiConfig;
import de.denkunddachte.exception.ApiException;

public class SfgEntityManager {
  // must match name in persistence.xml:
  public static final String      PERSISTENCE_UNIT = "B2BCustomDb";
  private DataSource              dataSource;
  EntityManagerFactory            emf;
  private EntityManager           em;
  private static SfgEntityManager instance;

  private SfgEntityManager(ApiConfig apiConfig, DataSource ds) throws ApiException {
    super();
    init(apiConfig, ds);
  }

  public static SfgEntityManager instance() throws ApiException {
    if (instance == null) {
      instance = new SfgEntityManager(ApiConfig.getInstance(), null);
    }
    return instance;
  }

  public static SfgEntityManager instance(ApiConfig apiConfig) throws ApiException {
    if (instance == null) {
      instance = new SfgEntityManager(apiConfig, null);
    }
    return instance;
  }

  public static SfgEntityManager instance(ApiConfig apiConfig, DataSource ds) throws ApiException {
    if (instance == null) {
      instance = new SfgEntityManager(apiConfig, ds);
    }
    return instance;
  }

  public DataSource getDataSource() {
    return this.dataSource;
  }

  private void init(ApiConfig apiConfig, DataSource ds) throws ApiException {
    Map<String, Object> props = new HashMap<>();
    if (ds == null) {
      BasicDataSource bds = new BasicDataSource();
      bds.setDriverClassName(apiConfig.getDbDriver());
      bds.setUrl(apiConfig.getDbUrl());
      bds.setUsername(apiConfig.getDbUser());
      bds.setPassword(apiConfig.getDbPassword());
      bds.setInitialSize(5);
      this.dataSource = bds;
      // Test/prepare DB connection
      try (Connection con = dataSource.getConnection()) {
        // OK
      } catch (SQLException e) {
        throw new ApiException("Could not connect to database " + apiConfig.getDbUrl() + " with user " + apiConfig.getDbUser() + "!", e);
      }
    }
    props.put(org.eclipse.persistence.config.PersistenceUnitProperties.NON_JTA_DATASOURCE, dataSource);
    props.put(LOGGING_LOGGER, apiConfig.getEclipseLinkLogger());
    props.put(LOGGING_LEVEL, apiConfig.getEclipseLinkLogLevel().getName());
    props.put(CATEGORY_LOGGING_LEVEL_ + SessionLog.SQL, apiConfig.getEclipseLinkLogLevelSql().getName());
    props.put(LOGGING_PARAMETERS, apiConfig.getEclipseLinkLogParams());
    props.put(SESSION_EVENT_LISTENER_CLASS, apiConfig.getEclipseLinkSessionEventListener());
    emf = Persistence.createEntityManagerFactory(SfgEntityManager.PERSISTENCE_UNIT, props);
  }

  public EntityManager getEntityManager() {
    if (this.em == null || !this.em.isOpen()) {
      this.em = emf.createEntityManager();
      this.em.setFlushMode(FlushModeType.COMMIT);
    }
    return this.em;
  }

  public void startTransaction() {
    getEntityManager().getTransaction().begin();
  }

  public void close() {
    if (em != null) {
      em.close();
    }
    this.em = null;
  }

  public void commit() {
    if (em != null) {
      em.getTransaction().commit();
    }
  }

  public void rollback() {
    if (em != null) {
      em.getTransaction().rollback();
    }
  }

  public void persist(AbstractSfgObject entity) {
    em.persist(entity);
  }

  public <T> T merge(T entity) {
    return em.merge(entity);
  }

  public void remove(AbstractSfgObject entity) {
    em.remove(entity);
  }

  public void flush() {
    em.flush();
  }

}
