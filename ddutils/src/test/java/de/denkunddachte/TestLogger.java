package de.denkunddachte;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.denkunddachte.utils.Config;
import de.denkunddachte.utils.LogConfig;

public class TestLogger {
  private static final Logger LOGGER = Logger.getLogger(TestLogger.class.getName());
  public static void main(String[] args) {
    Config config = Config.getConfig();
    config.setProperty(LogConfig.PROP_LOG_STDOUT, "FINE");
    config.setProperty(LogConfig.PROP_LOG_STDERR, "WARNING");
    LogConfig.initConfig();
    LOGGER.log(Level.INFO, "Hello....");
    LOGGER.log(Level.FINE, "fine.");
    
  }

}
