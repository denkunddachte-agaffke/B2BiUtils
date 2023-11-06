package de.denkunddachte.b2biutil.api;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;

import de.denkunddachte.b2biutil.AbstractConsoleApp;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.ft.SshKey;
import de.denkunddachte.ldap.FtLDAP;
import de.denkunddachte.ldap.LDAPUser;
import de.denkunddachte.util.ApiConfig;
import de.denkunddachte.utils.CommandLineParser.CommandLineException;
import de.denkunddachte.utils.CommandLineParser.ParsedCommandLine;
import de.denkunddachte.utils.Config;
import de.denkunddachte.utils.StringUtils;
import de.denkunddachte.utils.WordUtil;

public class LdapAdmin extends AbstractConsoleApp {
  private static final Logger LOG = Logger.getLogger(LdapAdmin.class.getName());
  private String              keyHashMode;
  private FtLDAP              ldap;
  private String              dnBase;
  private SshKeyMap           keymap;

  static {
    OPTIONS.setProgramName(LdapAdmin.class.getName());
    OPTIONS.setHasOptionalArgs(true);
    OPTIONS.setProgramDescription("FT LDAP admin utility.");

    // Common options
    OPTIONS.section("General options");
    OPTIONS.add(Props.PROP_LDAP_ADMIN + "|A", "List/edit FT admin users");

    // List
    OPTIONS.section("List LDAP users and keys");
    OPTIONS.add(Props.PROP_LIST + "|L:s", "List users (optional: pattern)");
    OPTIONS.add(Props.PROP_SHOW_DETAILS + "|v", "List also users and group permissions");
    OPTIONS.add(Props.PROP_MD5, "Display SSH key hashes as MD5 instead of SHA-256");
    OPTIONS.add(Props.PROP_CASE_SENSITIVE + "!b", "User ids are case sensitive", Props.PROP_CASE_SENSITIVE, "false");
    OPTIONS.add(Props.PROP_EXPORT + "=s", "Export data to file/directory (if argument is a directory, LDIF files will be created per user)");

    // Create
    OPTIONS.section("Create LDAP users");
    OPTIONS.add(Props.PROP_CREATE + "|c=s", "Create user with CN. Use --password and/or --addKey to set password and/or SSH keys(s)");
    OPTIONS.add(Props.PROP_NAME + "|n=s", "Set user givenName (required for admin users, defaults to CN for FT users)");

    // Modify
    OPTIONS.section("Modify LDAP (add/remove SSH keys, set password)");
    OPTIONS.add(Props.PROP_USER + "|u=s", "Modify user CN (use --admin to edit FT admin users)");
    OPTIONS.add(Props.PROP_ADD_KEY + "|a=s@", "Add SSH public key(s) (takes filename or string with openSSH or SSH2 formatted key)");
    OPTIONS.add(Props.PROP_DEL_KEY + "|d=s@", "Delete SSH key (takes key index as shown with -L -v or key string)");
    OPTIONS.add(Props.PROP_DEL_KEYS, "Delete all SSH keys");
    OPTIONS.add(Props.PROP_DEL_KEYS_EXCEPT + "=s", "Delete all SSH keys except one specified (takes key index as shown with -L -v or key string)");
    OPTIONS.add(Props.PROP_PASSWORD + ":s", "Change password (use empty value to clear password)");
    OPTIONS.add(Props.PROP_DIGEST + "=s", "Hash password if not done by LDAP server with algorithm (e.g. SHA, MD5, PLAIN, NONE)",
        ApiConfig.LDAP_PASSWORD_DIGEST);

    // delete
    OPTIONS.section("Delete LDAP users");
    OPTIONS.add(Props.PROP_DELETE + "=s", "Delete user");

    OPTIONS.addProgramHelp("Examples:");
    OPTIONS.addProgramHelp("  LdapAdmin -L Testuser.* -v");
    OPTIONS.addProgramHelp("  LdapAdmin -A -c userid -n 'New Adminuser' --password");
    OPTIONS.addProgramHelp("  LdapAdmin -c Testuser01 --addKey /path/to/pubkey --addkey 'key string'");
    OPTIONS.addProgramHelp("  LdapAdmin -d Testuser01");
  }

  public LdapAdmin(String[] args) throws CommandLineException, ApiException {
    super(args);
  }

  @Override
  protected void init(ParsedCommandLine cmdline) throws CommandLineException, ApiException {
    if (cfg.getString(ApiConfig.LDAP_PASSWORD_DIGEST).matches("(?i)PLAIN|NONE")) {
      cfg.setProperty(ApiConfig.LDAP_PASSWORD_DIGEST, null);
    }
    if (cfg.hasAny(Props.PROP_LIST, Props.PROP_DELETE)) {
    } else if (cfg.hasProperty(Props.PROP_CREATE)) {
      if (!cfg.hasProperty(Props.PROP_ADD_KEY) && !cfg.hasProperty(Props.PROP_PASSWORD)) {
        throw new CommandLineException("--create requires either SSH public key with --addKey and/or password with --password!");
      }
      if (cfg.hasProperty(Props.PROP_LDAP_ADMIN)) {
        if (!cfg.hasProperty(Props.PROP_NAME)) {
          throw new CommandLineException("--create with --admin requires user name --name!");
        }
        if (!cfg.hasProperty(Props.PROP_PASSWORD)) {
          throw new CommandLineException("--create with --admin requires password with --password!");
        }
      }
    } else if (cfg.hasAny(Props.PROP_PASSWORD, Props.PROP_ADD_KEY, Props.PROP_DEL_KEY, Props.PROP_DEL_KEYS, Props.PROP_DEL_KEYS_EXCEPT)) {
      if (!cfg.hasProperty(Props.PROP_USER)) {
        throw new CommandLineException("Specify user with --user=<CN>!");
      }
    } else {
      throw new CommandLineException(
          "No operation specified! Use --list, --password, --addKey, --deleteKey, --deleteAllKeys, --deleteAllKeysExcept, --create or --delete.");
    }

    ApiConfig apicfg = ApiConfig.getInstance(cfg.getStringMap(null));
    if (cfg.getBoolean(Props.PROP_LDAP_ADMIN)) {
      dnBase = apicfg.getLdapAdmBase();
    } else {
      dnBase = apicfg.getLdapBase();
    }
    try {
      ldap = new FtLDAP(apicfg);
    } catch (NamingException e) {
      throw new ApiException(e);
    }
    if (cfg.hasProperty(Props.PROP_MD5)) {
      keyHashMode = "MD5";
    } else {
      keyHashMode = "SHA-256";
    }
    if (cfg.hasProperty(SshKeyMap.PROP_SSH_KEYMAP_FILE)) {
      try {
        keymap = SshKeyMap.getInstance();
      } catch (IOException e) {
        LOG.log(Level.WARNING, e, () -> "Error reading key map files " + cfg.getString(SshKeyMap.PROP_SSH_KEYMAP_FILE) + ". No key names available.");
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    int rc = 1;
    try (LdapAdmin api = new LdapAdmin(args)) {

      Config       cfg     = Config.getConfig();
      List<String> addKeys = null;
      if (cfg.hasProperty(Props.PROP_ADD_KEY) && cfg.getObject(Props.PROP_ADD_KEY) instanceof List<?>) {
        addKeys = (List<String>) cfg.getObject(Props.PROP_ADD_KEY);
      }
      List<String> delKeys = null;
      if (cfg.hasProperty(Props.PROP_DEL_KEY) && cfg.getObject(Props.PROP_DEL_KEY) instanceof List<?>) {
        delKeys = (List<String>) cfg.getObject(Props.PROP_DEL_KEY);
      }
      if (cfg.hasProperty(Props.PROP_LIST)) {
        api.listUsers(cfg.getProperty(Props.PROP_LIST), cfg.getBoolean(Props.PROP_CASE_SENSITIVE), cfg.getBoolean(Props.PROP_SHOW_DETAILS), false);
      } else if (cfg.hasProperty(Props.PROP_DELETE)) {
        api.deleteUser(cfg.getString(Props.PROP_DELETE));
      } else if (cfg.hasProperty(Props.PROP_CREATE)) {
        api.createUser(cfg.getString(Props.PROP_CREATE), cfg.getProperty(Props.PROP_NAME), addKeys, cfg.getProperty(Props.PROP_PASSWORD),
            cfg.getProperty(ApiConfig.LDAP_PASSWORD_DIGEST));
      } else if (cfg.hasProperty(Props.PROP_PASSWORD)) {
        api.changePassword(cfg.getString(Props.PROP_USER), cfg.getProperty(Props.PROP_PASSWORD), cfg.getProperty(ApiConfig.LDAP_PASSWORD_DIGEST));
      } else if (cfg.hasAny(Props.PROP_DEL_KEYS, Props.PROP_DEL_KEY, Props.PROP_ADD_KEY, Props.PROP_DEL_KEYS_EXCEPT)) {
        api.editKeys(cfg.getString(Props.PROP_USER), cfg.hasProperty(Props.PROP_DEL_KEYS), delKeys, addKeys, cfg.getProperty(Props.PROP_DEL_KEYS_EXCEPT));
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

  private String listUserHeader(boolean details) {
    if (details) {
      return String.format("%-20s %-30s %-35s %-6s %-32s%n%s", "CN", "givenName", "SN", "PWHash", "SSH Keys",
          "----------------------------------------------------------------------------------------------------------------------------------------------------------------");
    } else {
      return String.format("%-24s %-30s %-35s %2s %s%n%s", "CN", "givenName", "SN", "PW", "Keys",
          "-------------------------------------------------------------------------------------------------------");
    }
  }

  private String listLdapUser(LDAPUser user, boolean details) {
    if (details) {
      String[] k  = new String[user.getSshPublicKeys().size()];
      String[] sn = new String[] {};
      if (user.getSn() != null) {
        sn = WordUtil.wrap(user.getSn(), 35, null, true).split(System.lineSeparator());
      }
      Iterator<String> sshkeyiter = user.getSshPublicKeys().iterator();
      StringBuilder    sb         = new StringBuilder();
      int              idx        = 0;
      do {
        if (sshkeyiter.hasNext()) {
          try {
            SshKey key    = new SshKey(sshkeyiter.next());
            String digest = key.getDigest();
            if (keymap == null || !keymap.containsKeyHash(digest)) {
              k[idx] = key.getKeyDigestInfo(keyHashMode);
            } else {
              k[idx] = keymap.getKeyName(digest);
            }
          } catch (InvalidKeyException e) {
            k[idx] = "!! INVALID KEY !!";
          }
          idx++;
        }
      } while (sshkeyiter.hasNext());

      for (int i = 0; i < Math.max(k.length, sn.length); i++) {
        if (i == 0) {
          sb.append(String.format("%-20s %-30.30s %-35.35s %-6s %s %s", user.getCn(), user.getGivenName(), sn[i],
              (user.getPassword() == null ? " " : (user.getHashMode() == null ? "PLAIN" : user.getHashMode())), (i < k.length ? i + 1 : ""),
              (i < k.length ? k[i] : "")));
        } else {
          sb.append(
              String.format("%n%-51s %-35.35s %-6s %s %s", "", (i < sn.length ? sn[i] : ""), "", (i < k.length ? i + 1 : ""), (i < k.length ? k[i] : "")));
        }
      }
      return sb.toString();
    } else {
      return String.format("%-24s %-30s %-35.35s %2s %s", user.getCn(), user.getGivenName(), user.getSn(), (user.getPassword() == null ? "N" : "Y"),
          user.getSshPublicKeys().size());
    }
  }

  private void listUsers(String globPattern, boolean caseSensitive, boolean showDetails, boolean quiet) throws ApiException {
    int cnt = 0;
    for (LDAPUser u : ldap.getUsers(dnBase, StringUtils.globToRegexp(globPattern), caseSensitive)) {
      if (cnt++ == 0) {
        System.out.println(listUserHeader(showDetails));
      }
      System.out.println(listLdapUser(u, showDetails));
      exportArtifact(u);
    }
    if (quiet)
      return;

    if (cnt == 0) {
      System.out.format("No LDAP users found with base %s%s%n", dnBase, (globPattern == null ? "." : " matching pattern " + globPattern + "."));
      rc = 1;
    } else {
      System.out.format("%nFound %d LDAP users with base %s%s%n", cnt, dnBase, (globPattern == null ? "." : " matching pattern " + globPattern + "."));
    }
  }

  private String getPassword(String password) throws ApiException {
    if (password == null || password.trim().isEmpty()) {
      if (System.console() == null) {
        throw new ApiException("No console available! Use --password=<value> to specify password or --password= to clear password.");
      }
      char[] pw = System.console().readPassword("Enter password or enter to clear password: ");
      if (pw.length > 0) {
        password = new String(pw);
      }
    }
    return password;
  }

  private SshKey getSshKey(String key) throws InvalidKeyException {
    File   f      = new File(key);
    SshKey sshKey = null;
    if (f.canRead()) {
      sshKey = new SshKey(f);
    } else {
      sshKey = new SshKey(key);
    }
    return sshKey;
  }

  private void changePassword(String username, String newPassword, String digest) throws ApiException {
    LDAPUser user = null;
    rc = 1;
    try {
      user = ldap.getUser(username, dnBase);
      if (user == null) {
        System.err.println("User " + username + "," + dnBase + " does not exist!");
        return;
      }
    } catch (NamingException e) {
      throw new ApiException(e);
    }

    try {
      user.setPassword(getPassword(newPassword), digest);
      if (ldap.updateUser(user)) {
        System.out.println("Password changed for user " + user.getDn());
        rc = 0;
      } else {
        System.err.println("Password not changed for user " + user.getDn());
      }
    } catch (NamingException | NoSuchAlgorithmException e) {
      throw new ApiException(e);
    }
  }

  private void createUser(String cn, String username, List<String> sshKeys, String password, String digest) throws ApiException {
    LDAPUser user = null;
    rc = 1;
    try {
      user = ldap.getUser(cn, dnBase);
      if (user != null) {
        System.err.format("User %s,%s exists!%n", username, dnBase);
        return;
      }
      user = new LDAPUser("cn=" + cn + "," + dnBase);
      user.setCn(cn);
      user.setGivenName(username == null ? cn : username);
      if (sshKeys == null || sshKeys.isEmpty()) {
        user.setPassword(getPassword(password), digest);
        user.setSn(user.getGivenName());
      } else {
        addKeys(user, sshKeys);
      }

      if (ldap.addUser(user)) {
        System.out.format("Added user %s.%n", user.getDn());
        listUsers(user.getCn(), true, true, true);
        rc = 0;
      } else {
        System.err.format("User %s not added!%n", user.getDn());
      }
    } catch (NamingException | NoSuchAlgorithmException e) {
      throw new ApiException(e);
    }

  }

  private void deleteUser(String username) throws ApiException {
    LDAPUser user = null;
    rc = 1;
    try {
      user = ldap.getUser(username, dnBase);
      if (user == null) {
        System.err.format("User %s,%s does not exist!%n", username, dnBase);
        return;
      }
      exportArtifact(user);
      if (ldap.deleteUser(user)) {
        System.out.format("Deleted user %s.%n", user.getDn());
        rc = 0;
      } else {
        System.err.format("User %s not deleted!%n", user.getDn());
      }
    } catch (NamingException e) {
      throw new ApiException(e);
    }
  }

  private void editKeys(String username, boolean deleteAllKeys, List<String> delKeys, List<String> addKeys, String deleteExcept) throws ApiException {
    LDAPUser user = null;
    rc = 1;
    try {
      user = ldap.getUser(username, dnBase);
      boolean update = false;
      if (user == null) {
        System.err.format("User %s,%s does not exist!%n", username, dnBase);
        return;
      }
      List<String> keyList = new ArrayList<>(user.getSshPublicKeys());
      if (deleteAllKeys) {
        user.removeAllSshPublicKeys();
        update = true;
      } else if (deleteExcept != null) {
        String keyData = getKey(keyList, deleteExcept);
        for (String k : user.getSshPublicKeys()) {
          if (!k.equalsIgnoreCase(keyData)) {
            user.removeSshPublicKey(k);
            update = true;
          }
        }
      } else if (delKeys != null) {
        for (String delKey : delKeys) {
          user.removeSshPublicKey(getKey(keyList, delKey));
          update = true;
        }
      }
      if (addKeys != null) {
        addKeys(user, addKeys);
        update = true;
      }

      if (update) {
        if (ldap.updateUser(user)) {
          System.out.format("Updated user %s.%n", user.getDn());
          listUsers(user.getCn(), true, true, true);
          rc = 0;
        } else {
          System.err.format("User %s not updated!%n", user.getDn());
        }
      } else {
        System.out.format("No SSH keys changes for user %s.%n", user.getDn());
        rc = 0;
      }
    } catch (NamingException e) {
      throw new ApiException(e);
    }
  }

  private String getKey(List<String> userKeys, String keyOrHashOrNameOrIndex) throws ApiException {
    if (keyOrHashOrNameOrIndex.matches("\\d+")) {
      int idx = Integer.parseInt(keyOrHashOrNameOrIndex) - 1;
      if (idx >= 0 && idx < userKeys.size()) {
        return userKeys.get(idx);
      } else {
        throw new ApiException("Key index " + (idx + 1) + " out of bounds!");
      }
    } else {
      Pattern p   = Pattern.compile("(?:\\d+ |)([A-Z0-9-]+):(\\S+).*");
      SshKey  key = null;
      try {
        key = getSshKey(keyOrHashOrNameOrIndex);
        keyOrHashOrNameOrIndex = key.getKeyData();
      } catch (InvalidKeyException e) {
        // ignore, keyOrHashOrNameOrIndex could contain key hash, name or invalid key
      }
      for (String keyData : userKeys) {
        if (keyOrHashOrNameOrIndex.equalsIgnoreCase(keyData)) {
          return keyData;
        }
        if (key == null) {
          try {
            SshKey  k = new SshKey(keyData);
            Matcher m = p.matcher(keyOrHashOrNameOrIndex);
            if ((m.matches() && k.getKeyDigestInfo(m.group(1)).toLowerCase().contains(keyOrHashOrNameOrIndex.toLowerCase()))
                || (keymap != null && keyOrHashOrNameOrIndex.equalsIgnoreCase(keymap.getKeyName(k.getDigest())))) {
              return k.getKeyData();
            }
          } catch (InvalidKeyException e) {
            throw new ApiException(e);
          }
        }
      }
    }
    throw new ApiException("No key matching string \"" + keyOrHashOrNameOrIndex + "\"!");
  }

  private void addKeys(LDAPUser user, List<String> sshKeys) throws ApiException {
    for (String sshKey : sshKeys) {
      try {
        SshKey key = getSshKey(sshKey);
        if (user.hasKey(key.getKeyData())) {
          System.err.format("User %s already has key %s assigned. Skip.%n", user.getCn(), key.getKeyDigestInfo("SHA-256"));
        } else {
          user.addSshPublicKey(key.getKeyData(), key.getKeyComment());
        }
      } catch (InvalidKeyException e) {
        throw new ApiException("Invalid key: " + sshKey + "!", e);
      }
    }
  }
}
