package de.denkunddachte.b2biutil.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.denkunddachte.ft.SshKey;
import de.denkunddachte.utils.Config;
import de.denkunddachte.utils.StringUtils;

public class SshKeyMap {
  private static final Logger        LOGGER               = Logger.getLogger(SshKeyMap.class.getName());
  public static final String         PROP_SSH_KEYMAP_FILE = "sshKeyMapFile";
  private static SshKeyMap           instance;
  private final Map<String, KeyInfo> keymap               = new LinkedHashMap<>();
  private String                     mapFilePath;

  private SshKeyMap(File mapFile) throws IOException {
    readMapFile(mapFile);
  }

  public static SshKeyMap getInstance() throws IOException {
    return getInstance(null);
  }

  public static SshKeyMap getInstance(String mapFilePath) throws IOException {
    if (instance == null) {
      File mapFile = null;
      if (mapFilePath == null) {
        Config cfg = Config.getConfig();
        if (!cfg.hasProperty(PROP_SSH_KEYMAP_FILE)) {
          LOGGER.log(Level.WARNING, "Property {0} not defined. No key/name mapping available!", PROP_SSH_KEYMAP_FILE);
        } else {
          mapFilePath = StringUtils.expandVariables(cfg.getProperty(PROP_SSH_KEYMAP_FILE));
        }
      }
      mapFile = new File(mapFilePath);
      if (!mapFile.canRead() || mapFile.length() == 0) {
        LOGGER.log(Level.CONFIG, "Key map file {0} does not exist or is empty. No key/name mapping available!", mapFile);
        instance = new SshKeyMap(null);
      } else {
        instance = new SshKeyMap(mapFile);
      }
      instance.mapFilePath = mapFilePath;
    }
    return instance;
  }

  /*
   * Map file:
   * <SHA256 hash>;<keyName>;<key digest>
   */
  private void readMapFile(File mapFile) throws IOException {
    if (mapFile != null) {
      LOGGER.log(Level.FINE, "Read file {0}...", mapFile);
      try (BufferedReader br = new BufferedReader(new FileReader(mapFile))) {
        String line;
        while ((line = br.readLine()) != null) {
          if (line.trim().isEmpty() || line.indexOf('#') == 0 || line.indexOf(';') == -1) {
            continue;
          }
          String[] d = line.split(";");
          if (d.length == 3) {
            keymap.put(d[0], new KeyInfo(d[1], d[2]));
          } else {
            LOGGER.log(Level.WARNING, "Ignore invalid mapping: {0}!", line);
          }
        }
      }
    }
  }

  public String getKeyName(String sha256hash, String defaultVal) {
    if (keymap.containsKey(sha256hash)) {
      return keymap.get(sha256hash).getKeyName();
    } else {
      return defaultVal == null ? sha256hash : defaultVal;
    }
  }

  public String getKeyName(String sha256hash) {
    return getKeyName(sha256hash, null);
  }

  public String getKeyName(SshKey key) {
    return getKeyName(key.getDigest(), null);
  }

  public boolean containsKeyHash(String hash) {
    return keymap.containsKey(hash);
  }

  public KeyInfo getKeyInfo(String hash) {
    return keymap.get(hash);
  }

  public KeyInfo getKeyInfo(SshKey key) {
    return keymap.get(key.getDigest());
  }

  public KeyInfo addKey(SshKey key) {
    final String hash = key.getDigest();
    if (!keymap.containsKey(hash)) {
      if (key.getKeyComment() == null) {
        LOGGER.log(Level.WARNING, "Key {0} does have a comment field! Use addKey(SshKey key, String keyName) and provide name!", key);
      } else {
        return keymap.put(hash, new KeyInfo(key.getKeyComment(), key.getKeyDigestInfo("SHA-256")));
      }
    }
    return null;
  }

  public boolean addKey(SshKey key, String keyName) {
    if (!keymap.containsKey(key.getDigest())) {
      keymap.put(key.getDigest(), new KeyInfo(keyName, key.getKeyDigestInfo("SHA-256")));
      return true;
    }
    return false;
    // return keymap.computeIfAbsent(key.getDigest(), k -> new KeyInfo(keyName, key.getKeyDigestInfo("SHA-256")));
  }

  public String getMapFilePath() {
    return mapFilePath;
  }

  public Set<String> getKeyHashes() {
    return keymap.keySet();
  }
  
  @Override
  public String toString() {
    return "SshKeyMap [file=" + mapFilePath + ", mappings=" + keymap.size() + "]";
  }

  class KeyInfo {
    private String keyName;
    private String keyDigest;

    public KeyInfo(String keyName, String keyDigest) {
      this.keyName = keyName;
      this.keyDigest = keyDigest;
    }

    public String getKeyName() {
      return keyName;
    }

    public String getKeyDigest() {
      return keyDigest;
    }
  }
}
