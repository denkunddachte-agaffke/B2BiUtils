/*
  Copyright 2016 denk & dachte Software GmbH

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

package de.denkunddachte.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.DataSources;

import de.denkunddachte.utils.Password.CryptException;

public class DataSourcePools {

  private static final Map<String, DataSource> datasources = new HashMap<>();
  private static final Logger                  LOGGER      = Logger.getLogger(DataSourcePools.class.getName());

  static {
    // Disable log4j log output from MLog:
    Properties p = new Properties(System.getProperties());
    p.put("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
    p.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "OFF"); // Off or any other level
    System.setProperties(p);
  }

  private DataSourcePools() {
  }

  public static DataSource getPooledDataSource(String alias) throws ClassNotFoundException, SQLException {
    String prefix;
    if (alias != null) {
      prefix = "db." + alias + ".";
    } else {
      alias = "default";
      prefix = "db.";
    }

    if (!datasources.containsKey(alias)) {
      Config cfg = Config.getConfig();
      if (cfg.hasProperty(prefix + "url")) {
        try {
          return getPooledDataSource(cfg.getString(prefix + "driver"), cfg.getString(prefix + "url"), cfg.getString(prefix + "user"),
              Password.getCleartext(cfg.getString(prefix + "password")), alias);
        } catch (CryptException e) {
          LOGGER.log(Level.SEVERE, e, () -> "Could not decrypt password " + cfg.getString(prefix + "password") + "!");
        }
      }
    }
    return datasources.get(alias);
  }

  public static DataSource getPooledDataSource(String driver, String url, String user, String password, String alias)
      throws ClassNotFoundException, SQLException {
    if (alias == null) {
      alias = user + "@" + url;
    }
    if (alias != null && datasources.containsKey(alias)) {
      return DataSources.pooledDataSource(datasources.get(alias));
    }

    try {
      Class.forName(driver.trim());
      Properties prop = new Properties();
      prop.setProperty("user", user);
      prop.setProperty("password", password);
      if (Config.getConfig().hasProperty("program")) {
        prop.setProperty("v$session.program", Config.getConfig().getProperty("program"));
      }
      DataSource ds_unpooled = DataSources.unpooledDataSource(url, prop);
      datasources.put(alias, ds_unpooled);
    } catch (ClassNotFoundException e) {
      LOGGER.log(Level.SEVERE, "Could not load driver class {0}!", new Object[] { driver });
      throw e;
    } catch (SQLException e) {
      // FIXME: remove password from output
      LOGGER.log(Level.SEVERE, "Could not get DataSource for url={0}, user={1}: {2}!", new Object[] { url, user, e.getMessage(), password });
      throw e;
    }
    return datasources.get(alias);
  }

  public static Connection getConnectionFromPool(String alias) throws ClassNotFoundException, SQLException {
    return getPooledDataSource(alias).getConnection();
  }

}
