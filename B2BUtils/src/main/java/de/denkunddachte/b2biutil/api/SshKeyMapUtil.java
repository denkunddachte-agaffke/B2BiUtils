package de.denkunddachte.b2biutil.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;

import de.denkunddachte.b2biutil.AbstractConsoleApp;
import de.denkunddachte.b2biutil.api.SshKeyMap.KeyInfo;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.ft.SshKey;
import de.denkunddachte.ldap.FtLDAP;
import de.denkunddachte.ldap.LDAPUser;
import de.denkunddachte.sfgapi.SshAuthorizedUserKey;
import de.denkunddachte.sfgapi.SshKnownHostKey;
import de.denkunddachte.util.ApiConfig;
import de.denkunddachte.utils.CommandLineParser.CommandLineException;
import de.denkunddachte.utils.CommandLineParser.ParsedCommandLine;
import de.denkunddachte.utils.Config;
import de.denkunddachte.utils.StringUtils;

public class SshKeyMapUtil extends AbstractConsoleApp {
  private static final Logger            LOG = Logger.getLogger(SshKeyMapUtil.class.getName());
  private SshKeyMap                      map;
  private static Collection<String>      files;
  private Pattern                        ignoreKeyPattern;

  static {
    OPTIONS.setProgramName(SshKeyMapUtil.class.getName());
    OPTIONS.setHasOptionalArgs(true);
    OPTIONS.setProgramDescription("SSH key mapping utility.");

    // map
    OPTIONS.add(SshKeyMap.PROP_SSH_KEYMAP_FILE + "|f=s", "Path to map file.");
    OPTIONS.add(Props.PROP_MAP_B2BI_KEYS, "Map B2Bi SSH keys");
    OPTIONS.add(Props.PROP_INCL_HOSTKEYS, "Also include host keys");
    OPTIONS.add(Props.PROP_MAP_FILES, "Map key from file(s) (provide files as optional args)");
    OPTIONS.add(Props.PROP_MAP_LDAP_KEYS, "Map key from LDAP");

    OPTIONS.add(Props.PROP_IGNORE_KEYNAMES + "=s", "Ignore keys with name/comment matching regex (e.g. \"pseudo-keys\" in SFG for UI entry)");

    OPTIONS.addProgramHelp("Map SSH key hashes to key names/comments to help identify keys (e.g. for LdapAdmin utility).");
    OPTIONS.addProgramHelp("Reads files either from B2B Integrator with REST API or from openSSH/SSH2 files (public key files, authorized_keys files).");
  }

  public SshKeyMapUtil(String[] args) throws CommandLineException, ApiException {
    super(args);
  }

  @Override
  protected void init(ParsedCommandLine cmdline) throws CommandLineException, ApiException {

    if (cfg.getBoolean(Props.PROP_MAP_FILES)) {
      if (!cmdline.hasCmdLineParameters()) {
        throw new CommandLineException("No files to parse!");
      }
      files = cmdline.getcmdLineParameters();
    } else if (cfg.getBoolean(Props.PROP_MAP_B2BI_KEYS)) {
    } else if (cfg.getBoolean(Props.PROP_MAP_LDAP_KEYS)) {
    } else {
      throw new CommandLineException("No operation specified! Use --mapB2BiKeys, --mapFiles, --mapLdapKeys");
    }

    LOG.log(Level.FINER, "Populate ApiConfig from Config: {0}", cfg.getLoadedResources());
    LOG.log(Level.FINEST, "Map: {0}", cfg.getStringMap(null));
    ApiConfig apicfg = ApiConfig.getInstance(cfg.getStringMap(null));
    LOG.log(Level.FINER, "ApiConfig: {0}", apicfg.getConfigFiles());

    try {
      map = SshKeyMap.getInstance(StringUtils.expandVariables(cfg.getProperty(SshKeyMap.PROP_SSH_KEYMAP_FILE)));
    } catch (IOException e) {
      throw new CommandLineException("Error reading map file " + cfg.getProperty(SshKeyMap.PROP_SSH_KEYMAP_FILE) + "!", e);
    }
    if (!cfg.getString(Props.PROP_IGNORE_KEYNAMES).isEmpty()) {
      ignoreKeyPattern = Pattern.compile(cfg.getString(Props.PROP_IGNORE_KEYNAMES));
    }
  }

  public static void main(String[] args) {
    int rc = 1;
    try (SshKeyMapUtil api = new SshKeyMapUtil(args)) {
      Config cfg = Config.getConfig();
      if (cfg.getBoolean(Props.PROP_MAP_B2BI_KEYS)) {
        api.mapFromApi(cfg.getBoolean(Props.PROP_INCL_HOSTKEYS));
      }
      if (cfg.getBoolean(Props.PROP_MAP_FILES)) {
        api.mapFromFiles();
      }
      if (cfg.getBoolean(Props.PROP_MAP_LDAP_KEYS) && ApiConfig.getInstance().getLdapUrl() != null) {
        api.mapFromLdap();
      }
      rc = api.getRc();
    } catch (CommandLineException e) {
      e.printStackTrace(System.err);
      System.exit(3);
    } catch (ApiException e) {
      e.printStackTrace(System.err);
      System.exit(2);
    }
    System.exit(rc);
  }

  private void saveMap() throws ApiException {
    LOG.log(Level.FINE, "Save map file {0}...", map.getMapFilePath());
    try (PrintWriter wr = new PrintWriter(map.getMapFilePath())) {
      int cnt = 0;
      for (String h : map.getKeyHashes()) {
        KeyInfo k = map.getKeyInfo(h);
        wr.format("%s;%s;%s%n", h, k.getKeyName(), k.getKeyDigest());
        cnt++;
      }
      LOG.log(Level.INFO, "Wrote {0} keys to {1}.", new Object[] { cnt, map.getMapFilePath() });
    } catch (FileNotFoundException e) {
      throw new ApiException("Could not save file " + map.getMapFilePath() + "!", e);
    }
  }

  private void mapFromApi(boolean includeHostKeys) throws ApiException {
    ApiConfig apicfg = ApiConfig.getInstance();
    LOG.log(Level.INFO, "Map keys from B2B Integrator at {0}", apicfg.getApiBaseURI());
    int cnt = 0;
    for (SshAuthorizedUserKey userkey : SshAuthorizedUserKey.findAll()) {
      if (addKey(userkey.getSshKey(), userkey.getKeyName())) {
        cnt++;
      }
    }

    if (includeHostKeys) {
      for (SshKnownHostKey hostkey : SshKnownHostKey.findAll()) {
        if (addKey(hostkey.getSshKey(), hostkey.getKeyName())) {
          cnt++;
        }
      }
    }
    if (cnt > 0) {
      LOG.log(Level.INFO, "Added {0} keys from API.", cnt);
      saveMap();
    }
  }

  private void mapFromFiles() throws ApiException {
    LOG.log(Level.INFO, "Map keys files...");
    int filesProcessed = 0;
    int keys = 0;
    for (String file : files) {
      File f = new File(file);
      if (f.canRead()) {
        int keysInFile = mapFromFile(f);
        if (keysInFile > 0) {
          filesProcessed++;
          keys += keysInFile;
        }
      } else {
        LOG.log(Level.WARNING, "File {0} does not exist or is not readable!", file);
      }
    }
    if (filesProcessed > 0) {
      LOG.log(Level.INFO, "Added {0} keys from {1} files.", new Object[] { keys, filesProcessed });
      saveMap();
    }
  }

  private void mapFromLdap() throws ApiException {
    ApiConfig apicfg = ApiConfig.getInstance();
    LOG.log(Level.INFO, "Map keys from LDAP instance {0} (Base DN: {1})", new Object[] { apicfg.getLdapUrl(), apicfg.getLdapBase() });
    int cnt = 0;
    FtLDAP ldap;
    try {
      ldap = new FtLDAP(apicfg);
    } catch (NamingException e) {
      throw new ApiException("Could not connect to LDAP server " + apicfg.getLdapUrl() + "!", e);
    }
    for (LDAPUser u : ldap.getUsers(apicfg.getLdapBase())) {
      List<String> keyNames = new ArrayList<>(10);
      if (u.getGivenName() != null && u.getGivenName().toLowerCase().contains("key")) {
        int i = 0;
        Pattern p = Pattern.compile("key\\s*(\\d*)\\s*[:=]\\s*([^,]+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(u.getGivenName());
        while (m.find()) {
          i++;
          int keyIdx = (m.group(1).isEmpty() ? i : Integer.parseInt(m.group(1)));
          for (int j = keyNames.size(); j < keyIdx; j++)
            keyNames.add(j, null);
          keyNames.add(keyIdx, m.group(2));
        }
      }
      int i = 0;
      for (String key : u.getSshPublicKeys()) {
        i++;
        if (addKey(key, (i < keyNames.size() && keyNames.get(i) != null ? keyNames.get(i) : u.getCn() + "/" + i))) {
          cnt++;
        }
      }
    }

    if (cnt > 0) {
      LOG.log(Level.INFO, "Added {0} keys from LDAP.", cnt);
      saveMap();
    }
  }

  private boolean addKey(SshKey key, String name) {
    if (name == null)
      name = key.getKeyComment();
    if (ignoreKeyPattern != null && ignoreKeyPattern.matcher(name).matches()) {
      LOG.log(Level.FINE, "Ignore key name {0} (matches ignore pattern {1})", new Object[] { name, ignoreKeyPattern });
      return false;
    }
    return map.addKey(key, (name == null ? key.getKeyComment() : name));
  }

  private boolean addKey(String keyData) {
    return addKey(keyData, null);
  }

  private boolean addKey(String keyData, String name) {
    try {
      return addKey(new SshKey(keyData.replaceAll("[\\r\\n]", "").trim()), name);
    } catch (InvalidKeyException e) {
      LOG.log(Level.WARNING, e, () -> "Key " + name + ": invalid key string: " + keyData);
    }
    return false;
  }

  private int mapFromFile(File infile) {
    LOG.log(Level.FINE, "Map key file {0}...", infile);
    int keys = 0;
    try (BufferedReader rd = new BufferedReader(new FileReader(infile))) {
      StringBuilder sb = new StringBuilder();
      String line;
      boolean collectLines = false;
      while ((line = rd.readLine()) != null) {
        if (line.trim().isEmpty() || line.startsWith("#")) {
          continue;
        }
        sb.append(line);
        if (line.startsWith("---")) {
          if (line.matches("(?i)----+\\s*BEGIN .+")) {
            collectLines = true;
          } else {
            collectLines = false;
          }
        }
        if (!collectLines) {
          if (addKey(sb.toString()))
            keys++;
          sb.setLength(0);
        }
      }
    } catch (IOException e) {
      LOG.log(Level.WARNING, e, () -> "Could not read file " + infile + "!");
    }
    return keys;
  }
}
