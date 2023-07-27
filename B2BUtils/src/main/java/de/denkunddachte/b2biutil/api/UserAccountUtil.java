package de.denkunddachte.b2biutil.api;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.naming.NamingException;

import de.denkunddachte.util.ApiConfig;
import de.denkunddachte.b2biutil.AbstractConsoleApp;
import de.denkunddachte.b2biutil.api.SshKeyMap.KeyInfo;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.ft.SshKey;
import de.denkunddachte.ldap.FtLDAP;
import de.denkunddachte.ldap.LDAPUser;
import de.denkunddachte.sfgapi.SshAuthorizedUserKey;
import de.denkunddachte.sfgapi.UserAccount;
import de.denkunddachte.utils.CommandLineParser;
import de.denkunddachte.utils.CommandLineParser.CommandLineException;
import de.denkunddachte.utils.CommandLineParser.ParsedCommandLine;
import de.denkunddachte.utils.Config;
import de.denkunddachte.utils.WordUtil;

public class UserAccountUtil extends AbstractConsoleApp {
  private static final Logger LOG = Logger.getLogger(MailboxUtil.class.getName());
  private FtLDAP              ldap;
  private SshKeyMap           sshKeyMap;

  static {
    OPTIONS.setProgramName(MailboxUtil.class.getName());
    OPTIONS.setHasOptionalArgs(true);
    OPTIONS.setProgramDescription("Utility ...");

    // Common options
    OPTIONS.add(Config.PROP_CONFIG_FILE + "|C=s", "Path to API config file.");

    // List
    OPTIONS.add(Props.PROP_LIST + "|L:s", "List users (optional: glob to filter results)");
    OPTIONS.add(Props.PROP_SSHKEYS + "|k", "List users with his SSH keys");
    OPTIONS.add(SshKeyMap.PROP_SSH_KEYMAP_FILE, "SSH key map file");
    OPTIONS.add(Props.PROP_PERMISSIONS + "|p", "List users with permissions");

    OPTIONS.add(Props.PROP_SHOW + "|S=s", "Show user");

    OPTIONS.addProgramHelp("Some help... ");
  }

  public UserAccountUtil(String[] args) throws CommandLineException, ApiException {
    super(args);
  }

  @Override
  protected CommandLineParser getCommandLineConfig() {
    return OPTIONS;
  }

  @Override
  protected void init(ParsedCommandLine cmdline) throws CommandLineException {
    if (cfg.hasProperty(Props.PROP_LIST)) {
    } else if (cfg.hasProperty(Props.PROP_SHOW)) {
    } else if (cfg.hasProperty(Props.PROP_DELETE)) {
    } else if (cfg.hasProperty(Props.PROP_MODIFY)) {
    } else if (cfg.hasProperty(Props.PROP_ADD_USERS) || cfg.hasProperty(Props.PROP_ADD_GROUPS) || cfg.hasProperty(Props.PROP_DEL_USERS)
        || cfg.hasProperty(Props.PROP_DEL_GROUPS)) {
    } else {
      throw new CommandLineException("No operation specified! Use --list, --show, --create, --delete.");
    }

    if (cfg.getBoolean(Props.PROP_SSHKEYS)) {
      try {
        ldap = new FtLDAP(ApiConfig.getInstance());
        sshKeyMap = SshKeyMap.getInstance();
      } catch (NamingException | ApiException | IOException e) {
        throw new CommandLineException(e);
      }
    }
  }

  public static void main(String[] args) {
    int rc = 1;
    try (UserAccountUtil api = new UserAccountUtil(args)) {
      Config cfg = Config.getConfig();
      LOG.fine("Populate ApiConfig from Config " + cfg.getLoadedResources());
      LOG.finer("Map: " + cfg.getStringMap(null));
      ApiConfig apicfg = ApiConfig.getInstance(cfg.getStringMap(null));
      LOG.fine("ApiConfig: " + apicfg.getConfigFiles());
      if (cfg.hasProperty(Props.PROP_LIST)) {
        api.list(cfg.getString(Props.PROP_LIST));
      } else if (cfg.hasProperty(Props.PROP_CREATE)) {
        for (String mbxPath : cfg.getString(Props.PROP_CREATE).split(",")) {
          // util.create(mbxPath, StringUtils.expandVariables(cfg.getProperty(Props.PROP_DESCRIPTION)), cfg.getBoolean(Props.PROP_CASE_SENSITIVE),
          // cfg.getBoolean(Props.PROP_CREATE_PARENTS), cfg.getBoolean(Props.PROP_INHERIT_FROM_PARENT), cfg.getProperty(Props.PROP_GROUPS),
          // cfg.getProperty(Props.PROP_USERS));
        }
      } else if (cfg.hasProperty(Props.PROP_MODIFY)) {
        Set<String> groups, users;
        boolean     replace = false;
        if (cfg.hasProperty(Props.PROP_SET_USERS) || cfg.hasProperty(Props.PROP_SET_GROUPS)) {
          replace = true;
          users = toSet(cfg.getString(Props.PROP_SET_USERS));
          groups = toSet(cfg.getString(Props.PROP_SET_GROUPS));
        } else {
          users = toSet(cfg.getString(Props.PROP_ADD_USERS));
          groups = toSet(cfg.getString(Props.PROP_ADD_GROUPS));
        }
        for (String mbxPath : cfg.getString(Props.PROP_MODIFY).split(",")) {
          // util.modify(mbxPath, cfg.getBoolean(Props.PROP_CASE_SENSITIVE), cfg.getBoolean(Props.PROP_RECURSE), users, groups, replace,
          // toSet(cfg.getString(Props.PROP_DEL_USERS)), toSet(cfg.getString(Props.PROP_DEL_USERS)));
        }
      } else if (cfg.hasProperty(Props.PROP_DELETE)) {
        // for (String mbxPath : cfg.getString(Props.PROP_DELETE).split(",")) {
        // util.delete(mbxPath, cfg.getBoolean(Props.PROP_CASE_SENSITIVE), cfg.getBoolean(Props.PROP_RECURSE), cfg.getBoolean(Props.PROP_FORCE));
        // }
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

  private String listHeader() {
    if (cfg.getBoolean(Props.PROP_SSHKEYS)) {
      return String.format("%-20s %1s %-2s %-4s %-40s %s%n%s", "ID", "A", "PW", "Type", "Key name", "Key hash", separator('-', 152));
    } else if (cfg.getBoolean(Props.PROP_PERMISSIONS)) {
      return String.format("%-20s %-35s %s%n%s", "ID", "Groups", "Permissions", separator('-', 152));
    } else {
      return String.format("%-20s %1s %-40s %-50s %-5s %-30s%n%s", "UserId", "A", "Name", "eMail", "Lang", "Groups", separator('-', 152));
    }
  }

  private void listUserWithKeys(StringBuilder sb, UserAccount ua) throws ApiException {
    if (ua.isRefreshRequired()) {
      ua.refresh();
    }
    Map<String, SshKey> keys = new LinkedHashMap<>();
    for (SshAuthorizedUserKey key : ua.getAuthorizedUserKeys()) {
      keys.put("!" + key.getKeyName(), key.getSshKey());
    }
    LDAPUser ldapuser = null;
    try {
      ldapuser = ldap.getFTUser(ua.getUserId());
      if (ldapuser != null) {
        int i = 0;
        for (String keystring : ldapuser.getSshPublicKeys()) {
          i++;
          SshKey  key = new SshKey(keystring);
          KeyInfo ki  = sshKeyMap.getKeyInfo(key);
          if (ki == null) {
            keys.put("@" + "LDAP key " + i, key);
          } else {
            keys.put("@" + ki.getKeyName(), key);
          }
        }
      }
    } catch (NamingException e) {
      System.err.println("Could not get LDAP user " + ua.getUserId() + ": " + e.getMessage());
    } catch (InvalidKeyException e) {
      System.err.println("Invalid key: " + e.getMessage());
    }
    String           pw  = (ldapuser != null && ldapuser.getPassword() != null ? "X" : "-");
    Iterator<String> it  = keys.keySet().iterator();
    String           idx = null;
    if (!it.hasNext()) {
      sb.append(String.format("%-20s %1s %-2s %-4s %-40s %s", ua.getUserId(), ua.getAuthenticationType().name().charAt(0), pw, "-", "-", "-"));
    } else {
      idx = it.next();
      sb.append(String.format("%-20s %1s %-2s %-4s %-40s %s", ua.getUserId(), ua.getAuthenticationType().name().charAt(0), pw,
          (idx.charAt(0) == '!' ? "Int." : "LDAP"), idx.substring(1), keys.get(idx).getKeyDigestInfo("SHA-256")));
    }
    while (it.hasNext()) {
      idx = it.next();
      sb.append(
          String.format("%n%25s %-4s %-40s %s", " ", (idx.charAt(0) == '!' ? "B2Bi" : "LDAP"), idx.substring(1), keys.get(idx).getKeyDigestInfo("SHA-256")));
    }
  }

  private void listUserWithPermissions(StringBuilder sb, UserAccount ua) throws ApiException {
    if (ua.isRefreshRequired()) {
      ua.refresh();
    }
    Iterator<String> git   = ua.getGroupNames().iterator();
    List<String>     perms = new ArrayList<>(ua.getPermissionNames());
    Collections.sort(perms);
    Iterator<String> pit = perms.iterator();
    int              i   = 0;
    while (git.hasNext() || pit.hasNext()) {
      if (i++ == 0) {
        sb.append(String.format("%-20s %-35s %s", ua.getUserId(), (git.hasNext() ? git.next() : ""), (pit.hasNext() ? pit.next() : "")));
      } else {
        sb.append(String.format("%n%-20s %-35s %s", "", (git.hasNext() ? git.next() : ""), (pit.hasNext() ? pit.next() : "")));
      }
    }
  }

  private String listUser(UserAccount ua) throws ApiException {
    StringBuilder sb = new StringBuilder();
    if (cfg.getBoolean(Props.PROP_SSHKEYS)) {
      listUserWithKeys(sb, ua);
    } else if (cfg.getBoolean(Props.PROP_PERMISSIONS)) {
      listUserWithPermissions(sb, ua);
    } else {
      String[] p1 = WordUtil.wrap(String.join(", ", ua.getGroupNames()), 30, "$1" + LF, true, "( |,)").split(LF);
      sb.append(String.format("%-20s %1s %-40s %-50s %-5s %-30s", ua.getUserId(), ua.getAuthenticationType().name().charAt(0),
          ua.getGivenName() + " " + ua.getSurname(), ua.getEmail(), ua.getPreferredLanguage().getCode(), (p1.length > 0 ? p1[0] : "")));
      for (int i = 1; i < p1.length; i++) {
        sb.append(String.format("%n%120s %-30s", " ", p1[i]));
      }
    }
    return sb.toString();
  }

  private void list(String globPattern) throws ApiException {
    List<UserAccount> userlist = UserAccount.findAll(globPattern);

    if (userlist.isEmpty()) {
      System.out.format("No users found%s!%n", (globPattern == null || globPattern.isEmpty() ? "" : " matching pattern " + globPattern));
      rc = 1;
      return;
    }
    System.out.println(listHeader());
    int cnt = 0;
    for (UserAccount ua : userlist) {
      System.out.println(listUser(ua));
      cnt++;
    }
    System.out.format("%nFound %d users%s", cnt, (globPattern == null ? "." : " matching pattern " + globPattern + "."));
  }

  private static Set<String> toSet(String list) {
    Set<String> result = new HashSet<>();
    if (list != null) {
      result.addAll(Arrays.asList(list.split(",")));
    }
    return result;
  }
}
