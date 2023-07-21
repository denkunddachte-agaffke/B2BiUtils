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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import de.denkunddachte.utils.CommandLineParser.CommandLineOption;
import de.denkunddachte.utils.CommandLineParser.ParsedCommandLine;

public class Config {

  private static final String       INTERNAL_PROPPERTIES = "config.properties";
  public static final String        PROP_CONFIG_FILE     = "configfile";
  public static final String        PROP_INSTALLDIR      = "installdir";
  public static final String        PROP_HELP            = "help";
  public static final String        PROP_VERSION         = "version";
  public static final String        CMDLINE              = "_cmdline_";
  private static Config             instance             = null;
  private Properties                internalprops;
  private final Map<String, Object> props                = new HashMap<>();
  private static final Pattern      NUM_PATTERN          = Pattern.compile("^\\s*([+-]?\\s*\\d+)\\s*(?:([KMGT])(B|))?", Pattern.CASE_INSENSITIVE);
  private final List<String>        loadedResources      = new ArrayList<>();
  private static final boolean      DEBUG                = Boolean.parseBoolean(System.getProperty("ddutils.debug", "false"));

  // TODO: allow list of config files (e.g. $HOME/.config;${installdir}/config) in
  // INTERNAL_PROPPERTIES
  static {
    if (DEBUG)
      System.out.format("Config.static: installdir=%s%n", System.getProperty(Config.PROP_INSTALLDIR));
    if (System.getProperty(Config.PROP_INSTALLDIR) == null) {
      try {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        if (trace.length > 0) {
          String p          = Paths.get(Class.forName(trace[trace.length - 1].getClassName()).getProtectionDomain().getCodeSource().getLocation().toURI())
              .toString().replace('\\', '/');
          String installdir = null;
          if (DEBUG)
            System.out.format("Config.static: found install path=%s", p);
          if (p.contains("/target/")) {
            installdir = p.substring(0, p.indexOf("/target/"));
          } else if (p.contains("/lib/")) {
            installdir = p.substring(0, p.indexOf("/lib/"));
          } else if (p.endsWith(".jar")) {
            installdir = p.substring(0, p.lastIndexOf('/'));
          } else {
            installdir = p;
          }
          if (DEBUG)
            System.out.format(" --> %s%n", installdir);
          File f = new File(installdir);
          if (f.isDirectory()) {
            if (DEBUG)
              System.out.format("Config.static: set installdir=%s%n", installdir);
            System.setProperty(Config.PROP_INSTALLDIR, installdir);
          }
        }
      } catch (ClassNotFoundException | URISyntaxException e) {
        System.err
            .println("Could not determine installation directory. Set system property " + Config.PROP_INSTALLDIR + "!");
        System.exit(1);
      }
    }
  }

  /**
   * Constructors
   */
  private Config() {
  }

  public static synchronized Config getConfig() {
    if (instance == null) {
      instance = new Config();
      if (!instance.load(INTERNAL_PROPPERTIES)) {
        System.err.println("Error reading builtin properties: " + INTERNAL_PROPPERTIES);
      }
      if (instance.hasProperty(PROP_CONFIG_FILE)) {
        instance.setConfig(instance.getString(PROP_CONFIG_FILE), false);
      }
    }
    return instance;
  }

  public static synchronized Config getConfig(String resourceFile) {
    if (instance == null) {
      instance = new Config();
      if (!instance.setConfig(resourceFile, false)) {
        System.err.println("Error reading base properties: " + resourceFile);
      }
    }
    if (instance.hasProperty(PROP_CONFIG_FILE)) {
      instance.setConfig(instance.getString(PROP_CONFIG_FILE), false);
    }
    return instance;
  }

  public boolean setConfig(String pathlist) {
    return setConfig(pathlist, true);
  }

  public boolean setConfig(String pathlist, boolean replace) {
    boolean result = false;
    if (replace) {
      loadedResources.clear();
      props.clear();
    }
    for (String path : pathlist.split(",")) {
      if (load(path)) {
        result = true;
        break;
      }
    }
    return result;
  }

  private File getFileForPattern(String pattern) {
    final String normalizedPattern = pattern.replace('\\', '/').replaceAll("^\\./", "");
    File         f                 = new File(pattern);
    if (f.exists())
      return f;

    final Path   cwd  = Paths.get(normalizedPattern.lastIndexOf('/') > -1 ? normalizedPattern.substring(normalizedPattern.lastIndexOf('/') + 1) : ".");
    final String glob = StringUtils.globToRegexp(normalizedPattern);
    try (Stream<Path> results = Files.find(cwd, Integer.MAX_VALUE, (p, fa) -> p.toFile().isFile() && p.toFile().getName().matches(glob))) {
      Optional<Path> f1 = results.findFirst();
      if (f1.isPresent()) {
        return f1.get().toFile();
      } else {
        return null;
      }
    } catch (IOException e) {
      // ignore
    }

    return null;
  }

  private File getFileFor(String filename) {
    if (StringUtils.isNullOrWhiteSpace(filename)) {
      return null;
    }
    String name = filename.replace("${installdir}", System.getProperty("installdir", ".")).replace("${workdir}", System.getProperty("workdir", "."))
        .replaceAll("\\$\\{(?:user.home|HOME)\\}", System.getProperty("user.home", "."));
    File   f    = null;
    if (name.startsWith("**/") || filename.startsWith("**\\")) {
      name = name.substring(3);
      f = getFileForPattern(name);
    } else {
      f = new File(StringUtils.expandVariables(name));
    }

    if (f != null && f.exists()) {
      return f;
    } // found

    if (name.startsWith("/") && name.matches("^[A-Za-z]:[\\\\/]")) {
      return null;
    } // name is point to abs. path

    f = new File(new File(System.getProperty("user.home", ".")), name);
    if (f.exists()) {
      return f;
    }
    // name found in user home

    File jar = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
    f = new File(jar.getParentFile(), name);
    if (jar.exists() && f.exists()) {
      return f;
    }
    // name found in application dir
    return null;
  }

  private boolean load(String name) {
    InputStream is = null;
    boolean     ok = false;
    try {
      File f = getFileFor(name);
      if (f != null) {
        is = new FileInputStream(f);
        if (DEBUG)
          System.out.format("Config.load(): load file %s (%s)%n", f.getAbsolutePath(), is);
        props.clear();
        internalprops.forEach((k, v) -> props.put(k.toString(), v));
        Properties p = new Properties();
        p.load(is);
        p.forEach((k, v) -> props.put(k.toString(), v));
        ok = true;
        loadedResources.add(f.getAbsolutePath());
      } else {
        is = getClass().getClassLoader().getResourceAsStream(name);
        if (DEBUG)
          System.out.format("Config.load(): load resource %s (%s)%n", getClass().getClassLoader().getResource(name),
              is);
        if (is != null) {
          internalprops = new Properties();
          internalprops.load(is);
          props.clear();
          internalprops.forEach((k, v) -> props.put(k.toString(), v));
          ok = true;
          loadedResources.add("[" + getClass().getClassLoader().getResource(name) + "]");
        }
      }
    } catch (IOException e) {
      System.err.println("Error reading properties from " + name + ". Exception: " + e.getMessage());
    } finally {
      // @formatter:off
			if (is != null) try { is.close(); } catch (IOException e1) { e1.printStackTrace(); }
			// @formatter:on
    }
    return ok;
  }

  public Object getObject(String key) {
    return props.get(key);
  }

  public String getProperty(String key) {
    return (String) props.get(key);
  }

  public String getProperty(String key, String defaultValue) {
    if (props.get(key) != null) {
      return (String) props.get(key);
    }
    return defaultValue;
  }

  public Object getProperty(String key, String defaultValue, Object... params) {
    String txt = getProperty(key, defaultValue);
    return String.format(txt, params);
  }

  public String getString(String key) {
    if (props.get(key) != null) {
      return (String) props.get(key);
    }
    return "";
  }

  public String getString(String key, String defaultValue) {
    return getProperty(key, defaultValue);
  }

  public boolean getBoolean(String key) {
    return getBoolean(key, false);
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    if (props.containsKey(key)) {
      if (props.get(key) instanceof Boolean) {
        return (boolean) props.get(key);
      } else {
        return Boolean.parseBoolean(getString(key));
      }
    } else {
      return defaultValue;
    }
  }

  public long getLong(String key) {
    return getLong(key, 0);
  }

  public long getLong(String key, long defaultValue) {
    if (props.containsKey(key)) {
      return parseNum(getString(key));
    } else {
      return defaultValue;
    }
  }

  public int getInt(String key) {
    return getInt(key, 0);
  }

  public int getInt(String key, int defaultValue) {
    if (props.containsKey(key)) {
      return (int) parseNum(getString(key));
    } else {
      return defaultValue;
    }
  }

  public double getDouble(String key) {
    return getDouble(key, 0);
  }

  public double getDouble(String key, int defaultValue) {
    if (props.containsKey(key)) {
      return Double.parseDouble(getString(key));
    } else {
      return defaultValue;
    }
  }

  public synchronized void setObject(String key, Object value) {
    props.put(key, value);
  }

  public synchronized void setProperty(String key, String value) {
    props.put(key, value);
    if (value != null && PROP_CONFIG_FILE.equals(key)) {
      setConfig(value, true);
    }
  }

  public synchronized void setProperty(String key, boolean value) {
    props.put(key, value);
  }

  public synchronized Object setGlobal(String key, Object value) {
    if (value == null) {
      return props.remove(key);
    } else {
      return props.put(key, value);
    }
  }

  public Object getGlobal(String key) {
    return props.get(key);
  }

  public boolean isProperty(String key) {
    boolean ret = false;
    if (props.containsKey(key)) {
      ret = getBoolean(key);
    }
    return ret;
  }

  public boolean hasAll(String... keys) {
    for (String key : keys) {
      if (!props.containsKey(key)) {
        return false;
      }
    }
    return true;
  }

  public boolean hasAny(String... keys) {
    for (String key : keys) {
      if (props.containsKey(key)) {
        return true;
      }
    }
    return false;
  }

  public boolean hasProperty(String key) {
    return props.containsKey(key);
  }

  public List<String> getKeys() {
    return getKeys(null);
  }

  public List<String> getKeys(String prefix) {
    List<String> ret = new ArrayList<>(props.size());
    if (prefix != null)
      prefix = prefix + ".";

    for (Object o : props.keySet()) {
      String key = String.valueOf(o);
      if (prefix == null || key.startsWith(prefix)) {
        ret.add(key);
      }
    }
    return ret;
  }

  public Map<String, Object> getMap(String prefix) {
    Map<String, Object> map = new HashMap<>(props.size());
    for (String key : getKeys(prefix)) {
      map.put(key, props.get(key));
    }
    return map;
  }

  public Map<String, String> getStringMap(String prefix) {
    Map<String, String> map = new HashMap<>(props.size());
    for (String key : getKeys(prefix)) {
      map.put(key, String.valueOf(props.get(key)));
    }
    return map;
  }

  public void writeConfig() throws IOException {
    if (hasProperty(PROP_CONFIG_FILE)) {
      writeConfig(getString(PROP_CONFIG_FILE));
    }
  }

  public void writeConfig(String configFile) throws IOException {
    writeConfig(getFileFor(configFile));
  }

  public void writeConfig(File configFile) throws IOException {
    writeConfig(new FileOutputStream(configFile));
  }

  public void writeConfig(OutputStream out) throws IOException {
    for (Entry<String, Object> e : props.entrySet()) {
      out.write(String.format("%s=%s%n", e.getKey(), e.getValue()).getBytes());
    }
  }

  private static long parseNum(String in) {
    Matcher m    = NUM_PATTERN.matcher(in);
    int     mult = 1000;
    long    val;

    if (m.matches()) {
      val = Long.parseLong(m.group(1));
      if ("B".equalsIgnoreCase(m.group(3))) {
        mult = 1024;
      }

      if (m.group(2) != null) {
        switch (m.group(2).charAt(0)) {
          case 'K':
            val *= mult;
            break;
          case 'M':
            val *= mult * mult;
            break;
          case 'G':
            val *= mult * mult * mult;
            break;
          case 'T':
            val *= mult * mult * mult * mult;
            break;
          default:
            break;
        }
      }
    } else {
      throw new NumberFormatException("String " + in + " does not match pattern " + NUM_PATTERN.pattern() + ".");
    }
    return val;
  }

  public Properties getProperties() {
    Properties p = new Properties();
    p.putAll(props);
    return p;
  }

  public List<String> getLoadedResources() {
    return Collections.unmodifiableList(loadedResources);
  }

  public void dumpConfig() {
    if (props == null || props.isEmpty()) {
      System.out.println("Properties have not been initialized!");
      return;
    }
    System.out.println("Config [" + this + "] internal properties:");
    for (Object key : internalprops.keySet()) {
      System.out.format("%-20s : %s%n", key, internalprops.get(key));
    }
    System.out.println();
    System.out.println("Config [" + this + "] effective properties:");
    for (Object key : props.keySet()) {
      System.out.format("%-20s : %s%n", key, props.get(key));
    }
  }
  
  public void setCommandLine(ParsedCommandLine cmdLine) {
    props.put(CMDLINE, cmdLine);
  }

  public ParsedCommandLine getCommandLine() {
    return (ParsedCommandLine)props.get(CMDLINE);
  }

  public CommandLineOption getCommandLineOption(String option) {
    if (getCommandLine() == null) {
      throw new IllegalAccessError("Parsed commandline was not registered with setCommandLine(ParsedCommandLine cmdLine)!");
    }
    return getCommandLine().get(option);
  }

  public boolean wasInCommandLine(String option) {
    return getCommandLineOption(option) != null && getCommandLineOption(option).wasInTheCommandLine();
  }
}
