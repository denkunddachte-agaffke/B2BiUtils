package de.denkunddachte.b2biutil.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.denkunddachte.b2biutil.AbstractConsoleApp;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.sfgapi.ApiClient;
import de.denkunddachte.sfgapi.Mailbox;
import de.denkunddachte.sfgapi.Mailbox.CreateParent;
import de.denkunddachte.sfgapi.MailboxItem;
import de.denkunddachte.sfgapi.UserAccount;
import de.denkunddachte.sfgapi.UserGroup;
import de.denkunddachte.utils.CommandLineParser.CommandLineException;
import de.denkunddachte.utils.CommandLineParser.ParsedCommandLine;
import de.denkunddachte.utils.Config;
import de.denkunddachte.utils.StringUtils;
import de.denkunddachte.utils.WordUtil;

public class MailboxUtil extends AbstractConsoleApp {
  private static final Logger LOG = Logger.getLogger(MailboxUtil.class.getName());

  static {
    OPTIONS.setProgramName(MailboxUtil.class.getName());
    OPTIONS.setHasOptionalArgs(true);
    OPTIONS.setProgramDescription("B2Bi mailbox management utilitiy.");

    // List
    OPTIONS.section("List mailboxes and permissions");
    OPTIONS.add(Props.PROP_LIST + "|L:s", "List mailboxes (optional: starting with path)");
    OPTIONS.add(Props.PROP_CASE_SENSITIVE + "!b", "Mailbox paths are case sensitive", Props.PROP_CASE_SENSITIVE, "false");
    OPTIONS.add(Props.PROP_SHOW_DETAILS + "|v", "List also users and group permissions");
    OPTIONS.add(Props.PROP_SHOW_DESCRIPTION + "|s", "Show description");
    OPTIONS.add(Props.PROP_RECURSE + "|r!b", "Recurse", Props.PROP_RECURSE, "false");

    // Create
    OPTIONS.section("Create mailboxes");
    OPTIONS.add(Props.PROP_CREATE + "|c=s", "Create mailboxes (comma separated list of paths)");
    OPTIONS.add(Props.PROP_DESCRIPTION + "=s", "Description", Props.PROP_DESCRIPTION);
    OPTIONS.add(Props.PROP_CREATE_PARENTS + "|p!b", "Create parent mailboxes if they don't exist", Props.PROP_CREATE_PARENTS, "false");
    OPTIONS.add(Props.PROP_USERS + "|u=s", "Add user permissions (comma separated list of users)");
    OPTIONS.add(Props.PROP_GROUPS + "|g=s", "Add group permissions (comma separated list of groups)");
    OPTIONS.add(Props.PROP_INHERIT_FROM_PARENT + "!b", "Inherit user and group permissions from first existing parent", Props.PROP_INHERIT_FROM_PARENT, "true");

    // modify (add/remove users and/or groups)
    OPTIONS.section("Modify mailbox permissions (users and groups)");
    OPTIONS.add(Props.PROP_MODIFY + "=s", "Modify list of mailboxes");
    OPTIONS.add(Props.PROP_SET_USERS + "=s", "Set permitted users on path(s).");
    OPTIONS.add(Props.PROP_SET_GROUPS + "=s", "Set permitted groups on path(s).");
    OPTIONS.add(Props.PROP_ADD_USERS + "=s", "Add user permissions to path(s).");
    OPTIONS.add(Props.PROP_ADD_GROUPS + "=s", "Add group permissions to path(s).");
    OPTIONS.add(Props.PROP_DEL_USERS + "=s", "Remove user permissions to path(s).");
    OPTIONS.add(Props.PROP_DEL_GROUPS + "=s", "Remove group permissions to path(s).");

    // delete
    OPTIONS.section("Delete mailboxes");
    OPTIONS.add(Props.PROP_DELETE + "|d:s", "Delete mailboxes (comma separated list of paths). By default, only empty mailboxes will be removed.");
    OPTIONS.add(Props.PROP_FORCE + "|f", "force deletion of non-empty mailboxes (use --recurse to include submailboxes");

    OPTIONS.addProgramHelp("Examples:");
    OPTIONS.addProgramHelp("  MailboxUtil -L /MFT/ -r -v");
    OPTIONS.addProgramHelp("  MailboxUtil -c /MFT/Test/newmbx,/MFT/Test/other --description 'New mailbox' ");
    OPTIONS.addProgramHelp("  MailboxUtil --modify /MFT/Test/newmbx,/MFT/Test/other --removeUsers A0_TESTAG01I,A0_TEST01 --addUsers Testuser01");
  }

  public MailboxUtil(String[] args) throws CommandLineException, ApiException {
    super(args);
  }

  @Override
  protected void init(ParsedCommandLine cmdline) throws CommandLineException {
    if (cfg.hasProperty(Props.PROP_LIST)) {
    } else if (cfg.hasProperty(Props.PROP_CREATE)) {
      if (!cfg.getString(Props.PROP_CREATE).startsWith("/"))
        throw new CommandLineException("--create requires list of absolute mailbox paths!");
    } else if (cfg.hasProperty(Props.PROP_DELETE)) {
      if (!cfg.getString(Props.PROP_DELETE).startsWith("/"))
        throw new CommandLineException("--delete requires list of absolute mailbox paths!");
    } else if (cfg.hasProperty(Props.PROP_MODIFY)) {
      if (!cfg.getString(Props.PROP_MODIFY).startsWith("/"))
        throw new CommandLineException("--modify requires list of absolute mailbox paths!");
      if (cfg.hasProperty(Props.PROP_SET_USERS) || cfg.hasProperty(Props.PROP_SET_GROUPS)) {
        if (cfg.hasProperty(Props.PROP_ADD_USERS) || cfg.hasProperty(Props.PROP_ADD_GROUPS) || cfg.hasProperty(Props.PROP_DEL_USERS)
            || cfg.hasProperty(Props.PROP_DEL_GROUPS)) {
          throw new CommandLineException(
              "Options --addUsers, --removeUsers, --addGroups or --removeGroups cannot be used together with --setUsers and/or --setGroups!");
        }
      } else if (cfg.hasProperty(Props.PROP_ADD_USERS) || cfg.hasProperty(Props.PROP_ADD_GROUPS) || cfg.hasProperty(Props.PROP_DEL_USERS)
          || cfg.hasProperty(Props.PROP_DEL_GROUPS)) {
        // ok
      } else {
        throw new CommandLineException("--modify requires --addUsers, --removeUsers, --addGroups or --removeGroups!");
      }
    } else {
      throw new CommandLineException("No operation specified! Use --list, --create, --delete, --addUsers, --addGroups");
    }
  }

  public static void main(String[] args) {
    int rc = 1;
    try (MailboxUtil api = new MailboxUtil(args)) {
      Config cfg = Config.getConfig();
      if (cfg.hasProperty(Props.PROP_LIST)) {
        api.list(cfg.getString(Props.PROP_LIST), cfg.getBoolean(Props.PROP_CASE_SENSITIVE), cfg.getBoolean(Props.PROP_RECURSE),
            cfg.getBoolean(Props.PROP_SHOW_DETAILS), cfg.getBoolean(Props.PROP_SHOW_DESCRIPTION));
      } else if (cfg.hasProperty(Props.PROP_CREATE)) {
        for (String mbxPath : cfg.getString(Props.PROP_CREATE).split(",")) {
          api.create(mbxPath, StringUtils.expandVariables(cfg.getProperty(Props.PROP_DESCRIPTION)), cfg.getBoolean(Props.PROP_CASE_SENSITIVE),
              cfg.getBoolean(Props.PROP_CREATE_PARENTS), cfg.getBoolean(Props.PROP_INHERIT_FROM_PARENT), cfg.getProperty(Props.PROP_GROUPS),
              cfg.getProperty(Props.PROP_USERS));
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
          api.modify(mbxPath, cfg.getBoolean(Props.PROP_CASE_SENSITIVE), cfg.getBoolean(Props.PROP_RECURSE), users, groups, replace,
              toSet(cfg.getString(Props.PROP_DEL_USERS)), toSet(cfg.getString(Props.PROP_DEL_USERS)));
        }
      } else if (cfg.hasProperty(Props.PROP_DELETE)) {
        for (String mbxPath : cfg.getString(Props.PROP_DELETE).split(",")) {
          api.delete(mbxPath, cfg.getBoolean(Props.PROP_CASE_SENSITIVE), cfg.getBoolean(Props.PROP_RECURSE), cfg.getBoolean(Props.PROP_FORCE));
        }
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

  private String listHeader(boolean details, boolean descriptions) {
    if (details) {
      return String.format("%-6s %s %-65s %-7s %s%n%s", "ID", "T", (descriptions ? "Path/Description" : "Path"), "Items", "Permissions", separator('-', 110));
    } else {
      return String.format("%-6s %s %s%n%s", "ID", "T", (descriptions ? "Path/Description" : "Path"), separator('-', 110));
    }
  }

  private String listMailbox(Mailbox mbx, boolean details, boolean description) throws ApiException {
    if (details) {
      StringBuilder sb = new StringBuilder();
      if (!mbx.getGroupNames().isEmpty()) {
        sb.append("Groups: ").append(String.join(",", mbx.getGroupNames()));
      }
      if (!mbx.getUserNames().isEmpty()) {
        if (sb.length() > 0)
          sb.append(LF);
        sb.append("Users: ").append(String.join(",", mbx.getUserNames()));
      }
      String[] p1 = WordUtil.wrap(mbx.getPath(), 65, "$1" + LF, true, "( |/)").split(LF);
      if (description) {
        p1 = Arrays.copyOf(p1, p1.length + 1);
        p1[p1.length - 1] = String.format("%-65.65s", mbx.getDescription());
      }
      String[] p2 = new String[] {};
      if (sb.length() > 0)
        p2 = WordUtil.wrap(sb.toString(), 30, "$1" + LF, true, "( |,)").split(LF);

      sb = new StringBuilder();
      for (int i = 0; i < Math.max(p1.length, p2.length); i++) {
        if (i == 0) {
          sb.append(String.format("%-6s %s %-65s %4d/%2d %s", mbx.getMailboxId(), mbx.getMailboxType(), p1[i], mbx.getMessageList().size(),
              mbx.getSubMailboxList().size(), (p2 != null && i < p2.length ? p2[i] : "")));
        } else {
          sb.append(String.format("%n%-6s %s %-65s %7s %s", " ", " ", (i < p1.length ? p1[i] : " "), " ", (p2 != null && i < p2.length ? p2[i] : " ")));
        }
      }
      return sb.toString();
    } else if (description) {
      return String.format("%-6s %s %s%n%-7s%s", mbx.getMailboxId(), mbx.getMailboxType(), mbx.getPath(), " ", mbx.getDescription());
    } else {
      return String.format("%-6s %s %s", mbx.getMailboxId(), mbx.getMailboxType(), mbx.getPath());
    }
  }

  private void list(String path, boolean caseSensitive, boolean recurse, boolean showDetails, boolean showDescriptions) throws ApiException {
    List<Mailbox> mbxlist;
    if (recurse) {
      mbxlist = Mailbox.findAll(path + (path.endsWith("/") ? "%" : "/%"), caseSensitive);
    } else {
      mbxlist = new ArrayList<>(1);
      Mailbox mbx = Mailbox.find((path.isEmpty() ? "/" : path), caseSensitive);
      if (mbx != null)
        mbxlist.add(mbx);
    }

    if (mbxlist.isEmpty()) {
      System.out.format("No mailboxes found matching path: %s%n", (path == null || path.isEmpty() ? "/" : path));
      setRc(1);
      return;
    }
    int cnt = 0;
    System.out.println(listHeader(showDetails, showDescriptions));
    for (Mailbox mbx : mbxlist) {
      System.out.println(listMailbox(mbx, showDetails, showDescriptions));
      cnt++;
    }
    System.out.format("%nFound %d mailboxes%s%n", cnt, (path == null ? "." : " matching path " + path + "."));

  }

  private void create(String mbxPath, String description, boolean caseSensitive, boolean createParents, boolean inheritFromParent, String groups, String users)
      throws ApiException {
    Mailbox mbx = Mailbox.find(mbxPath, caseSensitive);
    if (mbx != null) {
      System.out.format("Mailbox %s exists: %s%n", mbxPath, mbx);
      return;
    }

    mbx = new Mailbox(mbxPath, description);
    mbx.setGroupNames(groups);
    mbx.setUserNames(users);

    if (createParents) {
      if (users != null || groups != null) {
        mbx.setCreateParentMailbox(CreateParent.INHERIT_FROM_CURRENT);
      } else if (inheritFromParent) {
        mbx.setCreateParentMailbox(CreateParent.INHERIT_FROM_PARENT);
      } else {
        mbx.setCreateParentMailbox(CreateParent.INHERIT_NONE);
      }
    } else if (inheritFromParent && users == null && groups == null) {
      Mailbox pmbx = mbx.getParent();
      if (pmbx != null) {
        mbx.setGroupNames(pmbx.getGroupNames());
        mbx.setUserNames(pmbx.getUserNames());
      } else {
        LOG.log(Level.WARNING, "Parent mailbox {0} does not exist!", mbx.getParentPath());
      }
    }

    if (mbx.create()) {
      System.out.format("Created mailbox %s.%n", mbx.getPath());
    } else {
      setRc(1);
      System.err.format("Mailbox %s not created: error code=%s, error=%s%n", mbx.getPath(), ApiClient.getApiReturnCode(), ApiClient.getApiErrorMsg());
    }
  }

  private void modify(String mbxPath, boolean caseSensitive, boolean recurse, Set<String> addUsers, Set<String> addGroups, boolean replace,
      Set<String> delUsers, Set<String> delGroups) throws ApiException {
    Mailbox mbx = Mailbox.find(mbxPath, caseSensitive);
    if (mbx == null) {
      System.out.format("Mailbox %s does not exist!%n", mbxPath);
      setRc(1);
      return;
    }

    List<Mailbox> mailboxes = new ArrayList<>();
    mailboxes.add(mbx);
    if (recurse) {
      mailboxes.addAll(mbx.getSubMailboxes(recurse));
    }

    for (Mailbox m : mailboxes) {
      boolean update = false;
      if (replace) {
        System.out.format("%s: replace users %s -> %s%n", m.getPath(), m.getUserNames(), addUsers);
        System.out.format("%s: replace groups %s -> %s%n", m.getPath(), m.getGroupNames(), addGroups);
        m.setGroupNames(addGroups);
        m.setUserNames(addUsers);
        update = true;
      } else {
        for (String group : addGroups) {
          UserGroup ug = UserGroup.find(group);
          if (ug == null) {
            throw new ApiException("Group " + group + " does exist!");
          }
          if (!m.getGroupNames().contains(group)) {
            System.out.format("%s: add group %s%n", m.getPath(), group);
            m.addGroup(ug);
            update = true;
          }
        }
        for (String user : addUsers) {
          UserAccount ua = UserAccount.find(user);
          if (ua == null) {
            throw new ApiException("User " + user + " does exist!");
          }
          if (!m.getUserNames().contains(user)) {
            System.out.format("%s: add user %s%n", m.getPath(), user);
            m.addUser(ua);
            update = true;
          }
        }
        Collection<String> groups = m.getGroupNames();
        for (String group : delGroups) {
          if (groups.remove(group)) {
            System.out.format("%s: remove group %s%n", m.getPath(), group);
          }
        }
        if (!update && m.getGroupNames().size() != groups.size()) {
          m.setGroupNames(groups);
          update = true;
        }
        Collection<String> users = m.getUserNames();
        for (String user : delUsers) {
          if (users.remove(user)) {
            System.out.format("%s: remove user %s%n", m.getPath(), user);
          }
        }
        if (!update && m.getUserNames().size() != users.size()) {
          m.setUserNames(users);
          update = true;
        }
      }

      if (update) {
        if (m.update()) {
          System.out.format("Updated mailbox %s%n", m);
        } else {
          System.err.format("Could not update mailbox %s: %s%n", m, ApiClient.getApiErrorMsg());
          setRc(1);
        }
      }
    }
  }

  private void delete(String mbxPath, boolean caseSensitive, boolean recurse, boolean force) throws ApiException {
    Mailbox mbx = Mailbox.find(mbxPath, caseSensitive);
    LOG.log(Level.FINE, "delete(): mbxPath={0}, caseSensitive={1}, recurse={2}, force={3}, mbx={4}",
        new Object[] { mbxPath, caseSensitive, recurse, force, mbx });
    if (mbx == null) {
      System.out.format("Mailbox %s does not exist!%n", mbxPath);
      setRc(1);
      return;
    }
    if ((recurse && force) || mbx.getContents().isEmpty() || (mbx.getSubMailboxList().isEmpty() && force)) {
      if (mbx.delete()) {
        System.out.format("Deleted mailbox(es): %s%n", mbx);
      } else {
        System.err.format("Could not delete mailbox(es) %s: %s%n", mbx.getPath(), ApiClient.getApiErrorMsg());
        setRc(1);
      }
      return;
    }

    if (!mbx.getMessageList().isEmpty() || (!recurse && !mbx.getSubMailboxList().isEmpty())) {
      System.err.format("Mailbox not empty: %s%n", mbx.getPath());
      setRc(1);
      return;
    }
    // mbx contains submbx
    for (MailboxItem mi : mbx.getSubMailboxList()) {
      LOG.log(Level.FINE, "delete(): subMailbox={0}", mi);
      delete(mbx.getPath() + "/" + mi.getName(), caseSensitive, recurse, force);
    }
  }

  private static Set<String> toSet(String list) {
    Set<String> result = new HashSet<>();
    if (list != null && !list.trim().isEmpty()) {
      result.addAll(Arrays.asList(list.split(",")));
    }
    return result;
  }
}
