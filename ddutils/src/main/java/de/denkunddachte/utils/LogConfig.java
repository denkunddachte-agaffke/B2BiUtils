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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

public class LogConfig {
  protected static final String    FMT_DATETIME    = "yyyy-MM-dd HH:mm:ss.SSS";
  protected static final String    FMT_TIME        = "HH:mm:ss.SSS";
  protected static final String    LF              = System.getProperty("line.separator");
  public static final String       PROP_LOG_STDOUT = "log.stdout";
  public static final String       PROP_LOG_STDERR = "log.stderr";
  public static final String       PROP_LOG_IDS    = "log.ids";
  private static final Logger      rootLogger      = LogManager.getLogManager().getLogger("");
  private static final Set<String> IDS             = new HashSet<>();

  private LogConfig() {
    super();
  }

  public static boolean initConfig() {
    return initConfig(Config.getConfig());
  }

  public static boolean initConfig(Config config) {
    for (String id : config.getString(PROP_LOG_IDS, "de.denkunddachte").split(",")) {
      IDS.add(id);
    }

    try {
      for (Handler h : rootLogger.getHandlers()) {
        h.close();
        rootLogger.removeHandler(h);
      }
      rootLogger.setLevel(Level.INFO);
      setLogLevel(IDS, config.getProperty("log.globallevel", "INFO"));

      if (config.hasProperty("log.file")) {
        initLogFile(StringUtils.expandVariables(config.getProperty("log.file")), config.getProperty("log.level", "INFO"), config.getInt("log.size", 1000000),
            config.getInt("log.count", 1), config.getBoolean("log.append", true));
      }
      if (config.hasProperty("trace.file")) {
        initTraceFile(StringUtils.expandVariables(config.getProperty("trace.file")), config.getProperty("trace.level", "INFO"),
            config.getInt("trace.size", 1000000), config.getInt("trace.count", 1), config.getBoolean("trace.append", false));
      }

      if (parseLevel(config.getProperty(PROP_LOG_STDOUT)) != Level.OFF || parseLevel(config.getProperty(PROP_LOG_STDERR)) != Level.OFF) {
        initConsole(config.getProperty(PROP_LOG_STDOUT), config.getProperty(PROP_LOG_STDERR), config.getString("log.console.formatter"));
      }
    } catch (Exception e) {
      System.err.println("Error during initConfig(): " + e.getMessage());
      return false;
    }
    return true;
  }

  private static void setLogLevel(Set<String> loggerNames, String level) {
    for (String loggerName : loggerNames) {
      setLogLevel(loggerName, level);
    }
  }

  public static void setLogLevel(String loggerName, Level lvl) {
    Logger.getLogger(loggerName).setLevel(lvl);
  }

  public static void setLogLevel(String loggerName, String level) {
    Logger.getLogger(loggerName).setLevel(parseLevel(level));
  }

  private static Level parseLevel(String level) {
    if (level == null || level.equals("") || level.equalsIgnoreCase("OFF"))
      return Level.OFF;
    if (level.equalsIgnoreCase("ALL"))
      return Level.ALL;
    if (level.equalsIgnoreCase("INFO"))
      return Level.INFO;
    if (level.equalsIgnoreCase("WARNING"))
      return Level.WARNING;
    if (level.equalsIgnoreCase("SEVERE"))
      return Level.SEVERE;
    if (level.equalsIgnoreCase("FINE"))
      return Level.FINE;
    if (level.equalsIgnoreCase("FINER"))
      return Level.FINER;
    if (level.equalsIgnoreCase("FINEST"))
      return Level.FINEST;
    if (level.equalsIgnoreCase("CONFIG"))
      return Level.CONFIG;

    return Level.OFF;
  }

  private static void addHandler(Handler handler) {
    rootLogger.addHandler(handler);
    for (String loggerName : IDS) {
      if (Logger.getLogger(loggerName).getLevel() == null || (handler.getLevel().intValue() < Logger.getLogger(loggerName).getLevel().intValue())) {
        Logger.getLogger(loggerName).setLevel(handler.getLevel());
      }
    }
  }

  public static void disableConsoleLogging() {
    for (Handler h : rootLogger.getHandlers()) {
      if (h instanceof DualConsoleHandler) {
        DualConsoleHandler dch = (DualConsoleHandler) h;
        dch.setLevel(Level.OFF);
        dch.setStderrLevel(Level.WARNING);
      } else if (h instanceof ConsoleHandler) {
        h.setLevel(Level.WARNING);
      }
    }
  }

  public static void enableConsoleLogging() {
    for (Handler h : rootLogger.getHandlers()) {
      if (h instanceof DualConsoleHandler) {
        DualConsoleHandler dch = (DualConsoleHandler) h;
        dch.setLevel(Level.INFO);
        dch.setStderrLevel(Level.WARNING);
      } else if (h instanceof ConsoleHandler) {
        h.setLevel(Level.INFO);
      }
    }
  }

  private static void initLogFile(String pattern, String level, int size, int count, boolean append) throws SecurityException, IOException {
    Level lvl = parseLevel(level);
    if (lvl != Level.OFF) {
      pattern = FileUtil.getAbsolutePath(pattern);
      mkPath(pattern);
      Handler logFileHandler = new FileHandler(pattern, size, count, append);
      logFileHandler.setFormatter(new LogFormatter());
      logFileHandler.setLevel(lvl);
      addHandler(logFileHandler);
    }
  }

  private static void initTraceFile(String pattern, String level, int size, int count, boolean append) throws SecurityException, IOException {
    Level lvl = parseLevel(level);
    if (lvl != Level.OFF) {
      pattern = FileUtil.getAbsolutePath(pattern);
      mkPath(pattern);
      Handler traceFileHandler = new FileHandler(pattern, size, count, append);
      traceFileHandler.setFormatter(new TraceFormatter());
      traceFileHandler.setLevel(lvl);
      addHandler(traceFileHandler);
    }
  }

  private static void initConsole(String stdoutLevel, String stderrLevel, String fmtClass) throws SecurityException {
    Level     stdoutLvl = parseLevel(stdoutLevel);
    Level     stderrLvl = parseLevel(stderrLevel);
    Formatter logfmt    = null;
    Formatter trcfmt    = null;
    if ("TraceFormatter".equals(fmtClass)) {
      logfmt = new TraceFormatter();
      trcfmt = new TraceFormatter();
    } else if ("LogFormatter".equals(fmtClass)) {
      logfmt = new LogFormatter();
      trcfmt = new LogFormatter();
    } else {
      logfmt = new ConsoleFormatter();
      trcfmt = new ConsoleFormatter();
    }

    addHandler(new DualConsoleHandler(stdoutLvl, stderrLvl, logfmt, trcfmt));
    for (String loggerName : IDS) {
      if (stderrLvl.intValue() < Logger.getLogger(loggerName).getLevel().intValue()) {
        Logger.getLogger(loggerName).setLevel(stderrLvl);
      }
    }
  }

  private static void mkPath(String abstractPath) {
    File f = new File(abstractPath).getAbsoluteFile().getParentFile();
    if (!f.isDirectory()) {
      f.mkdirs();
    }
  }

  public static class LogFormatter extends Formatter {
    protected final DateFormat fmtDateTime = new SimpleDateFormat(FMT_DATETIME);
    protected final DateFormat fmtTime     = new SimpleDateFormat(FMT_TIME);

    @Override
    public String format(LogRecord logrecord) {
      //
      // Formatiere Zeitstempel
      String        timeStamp = fmtDateTime.format(new Date(logrecord.getMillis()));
      //
      // Message zusammenbauen
      StringBuilder buffer    = new StringBuilder();
      // Header
      buffer.append(timeStamp);
      buffer.append(String.format("[%-7s] ", logrecord.getLevel()));
      if (logrecord.getSourceClassName() != null) {
        buffer.append(logrecord.getSourceClassName());
      } else {
        buffer.append('-');
      }
      if (logrecord.getSourceMethodName() != null) {
        buffer.append('.').append(logrecord.getSourceMethodName());
      }
      buffer.append(' ');

      // Text
      String msg = formatMessage(logrecord);
      if (msg != null) {
        String message = formatMessage(logrecord).replaceAll(LF, LF + "\t");
        buffer.append(message).append(LF);
      }
      //
      // Exception-Daten falls erforderlich
      if (logrecord.getThrown() != null) {
        Throwable tr = logrecord.getThrown();
        buffer.append(tr.getClass().getName()).append(' ').append(tr.getMessage()).append(LF);
        StackTraceElement[] st = tr.getStackTrace();
        for (int i = 0; i < st.length; i++) {
          buffer.append('\t').append(st[i].toString()).append(LF);
        }
      }
      return buffer.toString();
    }
  }

  public static class TraceFormatter extends Formatter {
    protected final DateFormat fmtDateTime = new SimpleDateFormat(FMT_DATETIME);
    protected final DateFormat fmtTime     = new SimpleDateFormat(FMT_TIME);

    @Override
    public String format(LogRecord logrecord) {
      //
      // Formatiere Zeitstempel
      String        timeStamp = fmtDateTime.format(new Date(logrecord.getMillis()));
      //
      // Message zusammenbauen
      StringBuilder buffer    = new StringBuilder(timeStamp);
      // Header
      buffer.append(String.format("[%-7s] %s ", logrecord.getLevel(), Thread.currentThread().getName()));
      buffer.append(logrecord.getSourceClassName());
      if (logrecord.getSourceMethodName() != null) {
        buffer.append('.').append(logrecord.getSourceMethodName());
      }

      buffer.append(' ');
      // Text
      String msg = formatMessage(logrecord);
      if (msg != null) {
        String message = formatMessage(logrecord).replaceAll(LF, LF + "\t");
        buffer.append(message).append(LF);
      }
      //
      // Exception-Daten falls erforderlich
      if (logrecord.getThrown() != null) {
        try (StringWriter sw = new StringWriter()) {
          PrintWriter pw = new PrintWriter(sw);
          logrecord.getThrown().printStackTrace(pw);
          buffer.append(sw.toString());
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
      return buffer.toString();
    }
  }

  public static class ConsoleFormatter extends Formatter {
    protected final DateFormat fmtDateTime = new SimpleDateFormat(FMT_DATETIME);
    protected final DateFormat fmtTime     = new SimpleDateFormat(FMT_TIME);

    @Override
    public String format(LogRecord logrecord) {
      String msg = formatMessage(logrecord);
      if (msg != null) {
        msg = msg.replaceAll(LF, LF + "\t");

      }
      // Message zusammenbauen
      StringBuilder buffer = new StringBuilder(String.format("%s [%-7s] %s%s", fmtTime.format(new Date(logrecord.getMillis())), logrecord.getLevel(), msg, LF));
      // Exception-Daten falls erforderlich
      if (logrecord.getThrown() != null) {
        try (StringWriter sw = new StringWriter()) {
          PrintWriter pw = new PrintWriter(sw);
          logrecord.getThrown().printStackTrace(pw);
          buffer.append(sw.toString());
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
      return buffer.toString();
    }
  }

  public static class DualConsoleHandler extends StreamHandler {

    private final ConsoleHandler stderrHandler = new ConsoleHandler();
    private Level                stderrLevel   = Level.OFF;

    public DualConsoleHandler(Level stdoutLevel, Level stderrLevel, Formatter logFmt, Formatter trcFmt) {
      super(System.out, logFmt);
      stderrHandler.setFormatter(trcFmt);
      stderrHandler.setLevel(stderrLevel);
      setLevel(stdoutLevel);
      this.stderrLevel = stderrLevel;
    }

    public void setStderrLevel(Level level) {
      this.stderrLevel = level;
    }

    @Override
    public synchronized void publish(LogRecord logrecord) {
      if (logrecord.getLevel().intValue() < stderrLevel.intValue()) {
        super.publish(logrecord);
        super.flush();
      } else {
        stderrHandler.publish(logrecord);
        stderrHandler.flush();
      }
    }

    @Override
    public synchronized void close() {
      // nothing to close
    }
  }
}
