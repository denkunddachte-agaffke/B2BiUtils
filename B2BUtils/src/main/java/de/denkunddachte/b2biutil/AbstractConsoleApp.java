package de.denkunddachte.b2biutil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.output.FileWriterWithEncoding;

import de.denkunddachte.b2biutil.api.Props;
import de.denkunddachte.b2biutil.workflow.WorkflowUtil;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.ft.ExportOutput;
import de.denkunddachte.ft.Exportable;
import de.denkunddachte.sfgapi.ApiClient;
import de.denkunddachte.util.ApiConfig;
import de.denkunddachte.utils.CommandLineParser;
import de.denkunddachte.utils.CommandLineParser.CommandLineException;
import de.denkunddachte.utils.CommandLineParser.CommandLineOption;
import de.denkunddachte.utils.CommandLineParser.ParsedCommandLine;
import de.denkunddachte.utils.Config;
import de.denkunddachte.utils.LogConfig;
import de.denkunddachte.utils.StringUtils;

public abstract class AbstractConsoleApp implements AutoCloseable {
  private static final Logger              LOG               = Logger.getLogger(AbstractConsoleApp.class.getName());
  protected Config                         cfg;
  protected static String                  LF                = System.lineSeparator();
  protected static String                  OK                = Common.ANSI_GREEN + "\u2714 " + Common.ANSI_RESET;
  protected static String                  NOK               = Common.ANSI_RED + "\u2716 " + Common.ANSI_RESET;
  protected static final DateFormat        FMT_HHMMSS        = new SimpleDateFormat("HH:mm:ss");
  protected static final DateFormat        FMT_ISO_TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
  protected static final DateFormat        FMT_ISO_DATETIME  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  protected static final DateTimeFormatter FMT_DTTM          = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");
  protected static String                  MARK              = Common.ANSI_GREEN + "\u2714 " + Common.ANSI_RESET;
  protected static String                  UPDATE_WS_API     = "updateWsApi";
  private int                              rc                = 0;
  protected static final CommandLineParser OPTIONS           = new CommandLineParser(false);
  protected ExportOutput                   exporter          = null;
  // implement general site prefix mapping? see issue #31
  // protected Map<String,String> sitePrefixMap = new LinkedHashMap<>();
  protected Map<String, String>            svcNameMap        = new LinkedHashMap<>();

  public AbstractConsoleApp(String[] args) throws CommandLineException, ApiException {
    LogConfig.initConfig();
    commoninit(args);
  }

  private ParsedCommandLine commoninit(String[] args) throws CommandLineException, ApiException {
    // add common options:
    if (!OPTIONS.hasOption(Config.PROP_CONFIG_FILE)) {
      OPTIONS.section("Common options", true);
      OPTIONS.add(Config.PROP_CONFIG_FILE + "|C=s", "Path to API config file.");
      OPTIONS.add("yes", "Assume yes in intercative actions.");
      OPTIONS.add(Common.PROP_SHOWVERSION, "Show version information.");
      OPTIONS.add(UPDATE_WS_API, "Install/update WS API WFD on server.");
      OPTIONS.add("debug|D=s", "Set debug to stdout to level (use java.util.logging level)", LogConfig.PROP_LOG_STDERR);
      OPTIONS.add("help", "Show this help.", Config.PROP_HELP);

      OPTIONS.addProgramHelp("");
      OPTIONS.addProgramHelp("Please report bugs, change requests, ideas at https://github.com/denkunddachte-agaffke/B2BiUtils/issues");
    }

    ParsedCommandLine cmdLine = null;
    try {
      cmdLine = getCommandLineConfig().parse(args);
    } catch (CommandLineException ce) {
      if (Arrays.asList(args).contains("--help")) {
        getCommandLineConfig().printHelp();
        System.exit(0);
      } else {
        throw ce;
      }
    }
    cfg = Config.getConfig();
    // copy/overwrite config properties with command line options. Handle pointer to config file
    // first, because the config is reloaded from file when this property is set.
    if (cmdLine.containsKey(Config.PROP_CONFIG_FILE)) {
      String configFile = cmdLine.get(Config.PROP_CONFIG_FILE).getValue();
      File   f          = new File(configFile);
      if (!f.exists()) {
        configFile = "${user.home}/.@,${user.home}/@,${installdir}/@".replace("@", f.getName());
      }
      if (!cfg.setConfig(configFile)) {
        throw new CommandLineException("Could not load config file from " + cmdLine.get(Config.PROP_CONFIG_FILE).getValue() + "!");
      }
    }
    // Optional: set default values for command line options from properties file(s):
    for (CommandLineOption o : getCommandLineConfig().getAllOptions()) {
      if (cfg.hasProperty(o.getPropertyName())) {
        if (Config.PROP_CONFIG_FILE.equals(o.getPropertyName())) {
          o.setDefaultValue(cfg.getLoadedResources().get(cfg.getLoadedResources().size() - 1));
        } else {
          o.setDefaultValue(cfg.getProperty(o.getPropertyName()));
        }
      }
    }
    // now put command line values into config properties:
    for (CommandLineOption o : cmdLine.values()) {
      if (!Config.PROP_CONFIG_FILE.equals(o.getLongName()) && (o.wasInTheCommandLine() || !cfg.hasProperty(o.getPropertyName()))) {
        if (o.isAllowsMultipleValues()) {
          cfg.setObject(o.getPropertyName(), (o.getValues().isEmpty() ? null : o.getValues()));
        } else {
          cfg.setProperty(o.getPropertyName(), o.getValue());
        }
      }
    }

    // reinitialize logger (settings might have changed in config and/or commandline)
    if (cfg.hasProperty("log.file") ) {
      cfg.setProperty("log.level", cfg.getString(LogConfig.PROP_LOG_STDERR));
    }
    LogConfig.initConfig();

    LOG.log(Level.FINE, "Start {0} {1}", new Object[] { OPTIONS.getProgramName(), String.join(" ", args) });

    LOG.log(Level.FINER, "Populate ApiConfig from Config: {0}", cfg.getLoadedResources());
    LOG.log(Level.FINEST, "Map: {0}", cfg.getStringMap(null));
    ApiConfig apicfg = ApiConfig.getInstance(cfg.getStringMap(null));
    LOG.log(Level.FINER, "ApiConfig: {0}", apicfg.getConfigFiles());

    if (cmdLine.containsKey(Config.PROP_HELP)) {
      getCommandLineConfig().printHelp();
      System.exit(0);
    }
    if (cmdLine.containsKey(Common.PROP_SHOWVERSION)) {
      System.out.format("%s version %s (Build %s)%n", this.getClass().getName(), de.denkunddachte.b2biutil.Version.VERSION,
          de.denkunddachte.b2biutil.Version.BUILD);
      if (!StringUtils.isNullOrWhiteSpace(de.denkunddachte.b2biutil.Version.COPYRIGHT)) {
        System.out.format("%s%n", de.denkunddachte.b2biutil.Version.COPYRIGHT);
      }
      System.out.format("B2BApiClient version %s (Build %s)%n", de.denkunddachte.util.Version.VERSION, de.denkunddachte.util.Version.BUILD);
      if (!StringUtils.isNullOrWhiteSpace(de.denkunddachte.util.Version.COPYRIGHT)) {
        System.out.format("%s%n", de.denkunddachte.util.Version.COPYRIGHT);
      }
      if (StringUtils.isNullOrWhiteSpace(apicfg.getWsApiBaseURI()) || apicfg.getWsApiList().isEmpty()) {
        System.out.println("API WS is not installed or deactivated. Some functionalities may not be available or run with reduced performance.");
      } else if (!apicfg.getWsApiList().contains("version")) {
        System.out.format("\"version\" WS API not supported/activated (URL: %s)%n", apicfg.getWsApiBaseURI());
      } else {
        String installedApiVersion = ApiClient.getWsApiVersion();
        System.out.format("API WS version %s (URL: %s)%n", installedApiVersion, apicfg.getWsApiBaseURI());
        if (newApiAvailable(installedApiVersion)) {
          System.out.format("%nA newer version (%s) of %s is available. Use --%s to update on server.%n", getWsApiVersion(), apicfg.getWsApiBpName(),
              UPDATE_WS_API);
        }
      }
      System.exit(0);
    }
    // cfg.setCommandLine(cmdLine);

    // toMap(cfg.getString(Props.PROP_SITE_PREFIX_MAP), sitePrefixMap);
    toMap(cfg.getString(Props.PROP_WSAPI_SVCMAP), svcNameMap);
    if (cmdLine.containsKey(UPDATE_WS_API)) {
      updateWsApi();
      System.exit(0);
    }

    if (cfg.hasProperty(Props.PROP_EXPORT)) {
      exporter = new ExportOutput(new File(cfg.getString(Props.PROP_EXPORT)), cfg.getBoolean(Props.PROP_EXPORT_PRETTYPRINT),
          cfg.getBoolean(Props.PROP_EXPORT_NULL));
    }
    if (cfg.getBoolean(Common.PROP_NOUTF8)) {
      OK = "+";
      NOK = "!";
      MARK = "*";
    }

    try {
      init(cmdLine);
    } catch (CommandLineException ce) {
      System.err.println(ce.getMessage());
      OPTIONS.printHelp(System.err);
      throw ce;
    }

    return cmdLine;
  }

  protected CommandLineParser getCommandLineConfig() {
    return OPTIONS;
  }

  abstract protected void init(ParsedCommandLine cmdline) throws CommandLineException, ApiException;

  public int getRc() {
    return rc;
  }

  protected void setRc(int rc) {
    this.rc = rc;
  }

  protected CharSequence separator(char c, int length) {
    char[] l = new char[length];
    for (int i = 0; i < length; i++) {
      l[i] = c;
    }
    return CharBuffer.wrap(l);
  }

  protected boolean askYN(String msg, boolean defaultReturn) {
    if (Config.getConfig().getBoolean("yes")) {
      return true;
    }
    if (msg != null) {
      System.out.format("%s (%s): ", msg, (defaultReturn ? "Y/n" : "y/N"));
    } else {
      System.out.format("Enter %s: ", (defaultReturn ? "Y/n" : "y/N"));
    }
    try (Scanner sc = new Scanner(System.in)) {
      while (true) {
        String ans = sc.nextLine().toLowerCase();
        if (ans.startsWith("y")) {
          return true;
        } else if (ans.startsWith("n")) {
          return false;
        } else if (ans.isEmpty()) {
          return defaultReturn;
        } else {
          System.err.println("Enter y or n.");
        }
      }
    }
  }

  protected String yn(Object val) {
    if (val == null) {
      return "";
    }
    if (val instanceof Boolean) {
      return Boolean.TRUE.equals(val) ? "Y" : "N";
    }
    if (val instanceof String) {
      return ((String) val).trim().isEmpty() ? "N" : "Y";
    }
    if (val instanceof Collection<?>) {
      return String.valueOf(((Collection<?>) val).size());
    }
    if (val instanceof Object[]) {
      return String.valueOf(((Object[]) val).length);
    }
    return "?";
  }

  protected String yn(boolean val) {
    return val ? "Y" : "N";
  }

  protected String optString(Object val) {
    return val == null ? "" : val.toString();
  }

  protected void exportArtifact(Exportable artifact) throws ApiException {
    if (exporter != null) {
      exporter.exportArtifact(artifact);
    }
  }

  @Override
  public void close() throws ApiException {
    if (exporter != null)
      exporter.close();

  }

  private boolean newApiAvailable(String installedVersion) throws ApiException {
    return parseVersion(getWsApiVersion()) > parseVersion(installedVersion);
  }

  private int parseVersion(String v) {
    if (v.lastIndexOf('-') > 0) {
      v = v.substring(v.lastIndexOf('-') + 1);
    }
    String[] vi = v.split("\\.");
    return Integer.parseInt(vi[0]) * 100000 + Integer.parseInt(vi[1]) * 1000 + Integer.parseInt(vi[2]);
  }

  private String getWsApiVersion() throws ApiException {
    String result = null;
    try (Scanner s = new Scanner(ApiClient.class.getClassLoader().getResourceAsStream("DD_API_WS.bpml"))) {
      while (s.hasNextLine()) {
        String line = s.nextLine();
        if (line.contains("to=\"API_VERSION\"")) {
          result = line.replaceFirst(".+>([0-9\\.]+)<.+", "$1");
        }
      }
    }
    return result;
  }

  private void updateWsApi() throws ApiException {
    String bp = ApiClient.getWsApiVersion();
    try {
      String bpName  = mapResourceName(bp.substring(0, bp.lastIndexOf('-')));
      File   tmpFile = File.createTempFile(bpName, ".bpml");
      tmpFile.deleteOnExit();
      try (Writer wr = new FileWriterWithEncoding(tmpFile, StandardCharsets.UTF_8);
          BufferedReader rd = new BufferedReader(
              new InputStreamReader(ApiClient.class.getClassLoader().getResourceAsStream("DD_API_WS.bpml"), StandardCharsets.UTF_8))) {
        String line;
        while ((line = rd.readLine()) != null) {
          // wr.write(patchLine(line, (svcNameMap.isEmpty() ? sitePrefixMap : svcNameMap)));
          wr.write(patchLine(line, svcNameMap));
          wr.append('\n');
        }
      }
      WorkflowUtil.main(new String[] { "-p", bpName, "-f", tmpFile.getAbsolutePath() });
      // won't return!
    } catch (IOException e) {
      throw new ApiException(e);
    }
  }

  protected String patchLine(String in, Map<String, String> map) {
    if (map.isEmpty()) {
      return in;
    }
    String out = in;
    for (Entry<String, String> e : map.entrySet()) {
      out = out.replace("\"" + e.getKey(), "\"" + e.getValue());
    }
    return out;
  }

  protected String mapResourceName(String baseName) {
    for (Entry<String, String> e : svcNameMap.entrySet()) {
      if (baseName.startsWith(e.getKey())) {
        return e.getValue() + baseName.substring(e.getKey().length());
      }
    }
    return baseName;
  }

  private void toMap(String mapstr, Map<String, String> map) {
    if (!mapstr.isEmpty()) {
      for (String m : mapstr.split(",")) {
        if (m.indexOf(':') == -1) {
          LOG.log(Level.WARNING, "Ignore invalid mapping {0} in [{1}].", new Object[] { m, mapstr });
        } else {
          map.put(m.substring(0, m.indexOf(':')), m.substring(m.indexOf(':') + 1));
        }
      }
    }
  }
}
