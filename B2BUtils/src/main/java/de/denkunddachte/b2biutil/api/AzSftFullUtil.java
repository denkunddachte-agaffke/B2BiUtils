package de.denkunddachte.b2biutil.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.naming.NamingException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;

import de.denkunddachte.b2biutil.AbstractConsoleApp;
import de.denkunddachte.b2biutil.loader.B2BiBulkLoader;
import de.denkunddachte.b2biutil.loader.CDNetmapsHandler;
import de.denkunddachte.b2biutil.loader.ChangeReport;
import de.denkunddachte.b2biutil.loader.DatauMeldesatzInput;
import de.denkunddachte.b2biutil.loader.FieldAction;
import de.denkunddachte.b2biutil.loader.LoaderInput;
import de.denkunddachte.b2biutil.loader.LoaderResult;
import de.denkunddachte.b2biutil.loader.SFTFullDatauLoader;
import de.denkunddachte.b2biutil.loader.SFTFullLoader;
import de.denkunddachte.b2biutil.loader.TemplateException;
import de.denkunddachte.b2biutil.loader.model.AbstractLoadRecord;
import de.denkunddachte.b2biutil.loader.model.DeliveryRule;
import de.denkunddachte.b2biutil.loader.model.Endpoint;
import de.denkunddachte.b2biutil.loader.model.FetchRule;
import de.denkunddachte.b2biutil.loader.model.Partner;
import de.denkunddachte.b2biutil.loader.model.TransferRule;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.exception.B2BLoadException;
import de.denkunddachte.jpa.SfgEntityManager;
import de.denkunddachte.jpa.az.FgCustomer;
import de.denkunddachte.jpa.az.FgFetchRule;
import de.denkunddachte.jpa.az.FgRules;
import de.denkunddachte.jpa.az.FgTransfer;
import de.denkunddachte.ldap.FtLDAP;
import de.denkunddachte.ldap.LDAPUser;
import de.denkunddachte.sfgapi.RoutingChannel;
import de.denkunddachte.sfgapi.TradingPartner;
import de.denkunddachte.util.ApiConfig;
import de.denkunddachte.utils.CommandLineParser;
import de.denkunddachte.utils.CommandLineParser.CommandLineException;
import de.denkunddachte.utils.CommandLineParser.ParsedCommandLine;
import de.denkunddachte.utils.Config;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityManager;

// Run with -javaagent:/path/to/org.eclipse.persistence.jpa-3.0.3.jar
public class AzSftFullUtil extends AbstractConsoleApp {
  private static final Logger LOG = Logger.getLogger(AzSftFullUtil.class.getName());

  static {
    OPTIONS.setProgramName(AzSftFullUtil.class.getName());
    OPTIONS.setHasOptionalArgs(true);
    OPTIONS.setProgramDescription("Allianz SFT Full utilities.");

    // Common options
    OPTIONS.section("General options");
    OPTIONS.add(Props.PROP_CASE_SENSITIVE + "!b", "Partner/user ids are case sensitive.", Props.PROP_CASE_SENSITIVE, "true");

    // List
    OPTIONS.section("List partners/rules etc.");
    OPTIONS.add(Props.PROP_LIST_PARTNER + "|L:s", "List partners (optional: pattern)");
    OPTIONS.add(Props.PROP_LIST_RULES + "|l:s", "List rules (optional: producer pattern)");
    OPTIONS.add(Props.PROP_LIST_FETCHRULES + "|g:s", "List fetch rules (optional: producer pattern)");
    OPTIONS.add(Props.PROP_SHOW_DETAILS + "|v", "List also users and group permissions");
    OPTIONS.add(Props.PROP_EXPORT + "=s", "Export (argument: file: export to single file, directory: export for per artifact file)");

    // Create

    // Modify (add/remove users and/or groups)

    // Delete

    // Load
    OPTIONS.section("Loader options");
    OPTIONS.add(Props.PROP_VALIDATE + "=s", "Validate import file");
    OPTIONS.add(Props.PROP_FORMAT + "=s", "Override import file format (default: determine by extension)");
    OPTIONS.add(Props.PROP_IMPORT + "|i=s", "Import master data from file (Excel, YAML or DATAU)");
    OPTIONS.add(Props.PROP_FORCE + "|f", "Try to continue after error.");
    OPTIONS.add(Props.PROP_DRY_RUN + "|n", "Dry-run");
    OPTIONS.add(Props.PROP_REPORT + "|R:s", "Create changelog (no value: output to console, string containing @: send mail else: write to file)");

    OPTIONS.addProgramHelp("Examples:");
    OPTIONS.addProgramHelp("  Load DATAU (report): AzSftFullUtil -n -i dat/add.datau -R user@example.com");
  }

  public AzSftFullUtil(String[] args) throws CommandLineException, ApiException {
    super(args);
  }

  @Override
  protected CommandLineParser getCommandLineConfig() {
    return OPTIONS;
  }

  @Override
  protected void init(ParsedCommandLine cmdline) throws CommandLineException {
    if (cfg.hasProperty(Props.PROP_LIST_PARTNER)) {
    } else if (cfg.hasProperty(Props.PROP_LIST_RULES)) {
    } else if (cfg.hasProperty(Props.PROP_IMPORT)) {
      File f = new File(cfg.getString(Props.PROP_IMPORT));
      if (!f.canRead()) {
        throw new CommandLineException("Input file " + f.getAbsolutePath() + " not readable!");
      }
    } else if (cfg.hasProperty(Props.PROP_VALIDATE)) {
      File f = new File(cfg.getString(Props.PROP_VALIDATE));
      if (!f.canRead()) {
        throw new CommandLineException("Input file " + f.getAbsolutePath() + " not readable!");
      }
    } else {
      throw new CommandLineException("No operation specified! Use --listPartner, --listRules, --listFetchRules, --import or --validate");
    }
  }

  public static void main(String[] args) {
    int rc = 1;
    try (AzSftFullUtil api = new AzSftFullUtil(args)) {
      Config cfg = Config.getConfig();

      if (cfg.hasProperty(Props.PROP_LIST_PARTNER)) {
        api.listPartners(cfg.getProperty(Props.PROP_LIST_PARTNER), cfg.getBoolean(Props.PROP_CASE_SENSITIVE), cfg.getBoolean(Props.PROP_SHOW_DETAILS));
      } else if (cfg.hasProperty(Props.PROP_LIST_RULES)) {
        api.listRules(cfg.getString(Props.PROP_LIST_RULES), cfg.getBoolean(Props.PROP_CASE_SENSITIVE), cfg.getBoolean(Props.PROP_SHOW_DETAILS));
      } else if (cfg.hasProperty(Props.PROP_IMPORT)) {
        api.importFile(cfg.getString(Props.PROP_IMPORT), cfg.getProperty(Props.PROP_FORMAT), cfg.getBoolean(Props.PROP_CASE_SENSITIVE),
            cfg.getBoolean(Props.PROP_DRY_RUN), cfg.getBoolean(Props.PROP_FORCE));
      } else if (cfg.hasProperty(Props.PROP_VALIDATE)) {
        api.validateImport(cfg.getString(Props.PROP_VALIDATE), cfg.getProperty(Props.PROP_FORMAT));
      }
      rc = api.getRc();
    } catch (CommandLineException e) {
      e.printStackTrace(System.err);
      System.exit(3);
    } catch (ApiException | B2BLoadException | TemplateException | NamingException e) {
      e.printStackTrace(System.err);
      System.exit(2);
    }
    System.exit(rc);
  }

  // START list partner
  private void listPartners(String globPattern, boolean caseSensitive, boolean showDetails) throws ApiException, NamingException {
    final Map<String, JSONObject> ids = new HashMap<>(500);
    EntityManager                 em  = SfgEntityManager.instance().getEntityManager();
    int                           cnt = 0;
    for (FgCustomer fc : FgCustomer.findAll(globPattern, !caseSensitive, false, em)) {
      JSONObject o = new JSONObject();
      cnt++;
      o.put("id", fc.getCustomerId());
      o.put("fgCustomer", true);
      o.put("fgCustName", fc.getCustomerName());
      o.put("fgEnabled", fc.isEnabled());
      if (fc.isProducer()) {
        o.put("fgProdRules", FgRules.findPatterns(fc.getCustomerId(), null, false, em).size());
        List<FgFetchRule> fetchrules = FgFetchRule.findByProducer(fc.getFgCustId(), em);
        // fetchrules.forEach(r -> em.detach(r));
        o.put("fgFetchAWS", fetchrules.stream().filter(fr -> "AWSS3".equals(fr.getFgFetchRuleId().getType())).count());
        o.put("fgFetchSFTP", fetchrules.stream().filter(fr -> "SFTP".equals(fr.getFgFetchRuleId().getType())).count());
        o.put("fgFetchDrmOe",
            fetchrules.stream().filter(fr -> "DRM".equals(fr.getFgFetchRuleId().getType()) || "OE".equals(fr.getFgFetchRuleId().getType())).count());
      }
      if (fc.isConsumer()) {
        List<FgRules> cr = FgRules.findByConsumer(fc.getCustomerId(), false, em);
        // cr.forEach(r -> em.detach(r));
        o.put("fgConsRules", cr.size());
        o.put("fgConsProto", cr.stream().map(FgRules::getProtocol).collect(Collectors.toSet()));
      }
      // em.detach(fc);
      ids.put(fc.getCustomerId(), o);
      LOG.log(Level.FINE, "Found FG customer {0}.", fc.getCustomerId());
      exportArtifact(fc);
    }
    System.out.println("Found " + cnt + " FG customers.");
    cnt = 0;
    for (TradingPartner tp : TradingPartner.findAll(globPattern)) {
      cnt++;
      JSONObject o = ids.get(tp.getId());
      if (o == null) {
        o = new JSONObject();
        o.put("id", tp.getId());
        ids.put(tp.getId(), o);
      }
      o.put("sfgPartner", true);
      o.put("sfgCommunity", tp.getCommunityName());
      o.put("sfgAuthMode", tp.getAuthenticationType().ordinal());
      o.put("sfgProducer", tp.isListeningProducer() ? "passive" : (tp.isInitiatingProducer() ? "active" : "no"));
      o.put("sfgConsumer", tp.isListeningConsumer() ? "passive" : (tp.isInitiatingConsumer() ? "active" : "no"));
      Set<String> rcNames = new HashSet<>();
      for (RoutingChannel rc : RoutingChannel.findByProducer(tp.getId())) {
        rcNames.add(rc.getTemplateName());
      }
      LOG.log(Level.FINE, "Found SFG partner {0}.", tp.getId());
      o.put("sfgRoutingChannels", rcNames);
    }
    System.out.println("Found " + cnt + " SFG trading partners.");
    cnt = 0;
    ApiConfig apicfg = ApiConfig.getInstance();
    FtLDAP    ldap   = new FtLDAP(apicfg);
    for (LDAPUser u : ldap.getUsers(apicfg.getLdapBase(), globPattern, caseSensitive)) {
      cnt++;
      JSONObject o = ids.get(u.getCn());
      LOG.log(Level.FINE, "Found LDAP user {0}.", u.getCn());
      if (o == null) {
        o = new JSONObject();
        o.put("id", u.getCn());
        ids.put(u.getCn(), o);
      }
      o.put("ldapUser", true);
      o.put("ldapPassword", u.getPassword() != null);
      o.put("ldapSshKeys", u.getSshPublicKeys().size());
    }
    CDNetmapsHandler nmh = CDNetmapsHandler.getInstance();
    for (String nm : nmh.getAllNetmaps()) {
      System.out.println("NM: " + nm);
    }

    System.out.println("Total IDs: " + ids.size());
  }

  private String listPartner(FgCustomer fc, boolean details) throws ApiException {
    if (details) {
      StringBuilder sb = new StringBuilder();
      return sb.toString();
    } else {
      return String.format("%-4s %s %s", fc.getFgCustId(), fc.getCustomerId(), fc.getCustomerName());
    }
  }

  private String listPartnerHeader(boolean details) {
    if (details) {
      return String.format("%-4s %s %-65s %-7s %-30s%n%s", "ID", "T", "Path", "Items", "Permissions", separator('-', 103));
    } else {
      return String.format("%-4s %s %s%n%s", "ID", "T", "Path", separator('-', 103));
    }
  }
  // END list partner

  // START list rules
  private void listRules(String producerPattern, boolean caseSensitive, boolean showDetails) throws ApiException {
    EntityManager em = SfgEntityManager.instance().getEntityManager();
    for (FgCustomer fc : FgCustomer.findAll(producerPattern, !caseSensitive, false, em)) {
      for (FgTransfer t : fc.getFgTransfers()) {
        listRule(t, showDetails);
        exportArtifact(t);
      }
    }

  }

  private String listRule(FgTransfer t, boolean details) throws ApiException {
    if (details) {
      StringBuilder sb = new StringBuilder();
      return sb.toString();
    } else {
      return String.format("%-4s %s %s", t.getFgTransId(), t.getProducer().getCustomerId(), t.getRcvFilepattern());
    }
  }

  private String listRuleHeader(boolean details) {
    if (details) {
      return String.format("%-4s %s %-65s %-7s %-30s%n%s", "ID", "T", "Path", "Items", "Permissions", separator('-', 103));
    } else {
      return String.format("%-4s %s %s%n%s", "ID", "T", "Path", separator('-', 103));
    }
  }
  // END list rules

  // START Loader
  private void listLoadPartners(Set<Partner> partners) {
    int cnt = 0;
    System.out.println();
    for (Partner p : partners) {
      if (cnt++ == 0) {
        System.out.format("%-4s %1s %-20s %-8s %-35s %1s %1s %-7s %s%n%s%n", "Line", "A", "PartnerID", "Type", "Customer name", "P", "C", "AGL Te.",
            "Producerproto", separator('-', 99));
      }
      System.out.format("%1s%-3s %1s %-20s %-8s %-35s %1s %1s %-7s %s%n", getValidMark(p, p.getEndpoint()), p.getLine(), p.getLoadAction().getCode(), p.getId(),
          p.getType(), p.getCustomerName(), yn(p.isProducer()), yn(p.isConsumer()), optString(p.getAglTenant()),
          p.getEndpoint() == null ? "" : p.getEndpoint().getProtocol());
    }
  }

  private char getValidMark(AbstractLoadRecord r, Endpoint ep) {
    if (!r.isValid())
      return '!';
    if (ep == null) {
      return ' ';
    }
    if (ep.isValid())
      return ep.isCloneable() ? '*' : ' ';
    return '!';
  }

  private void listLoadRules(Set<TransferRule> rules) {
    int cnt = 0;
    System.out.println();
    for (TransferRule t : rules) {
      if (cnt++ == 0) {
        System.out.format("%-4s %1s %-20s %-40s %1s %1s %-20s %-7s %-16s %-7s %-8s %1s %s%n%s%n", "Line", "A", "Producer", "RcvPattern", "D", "n", "Consumer",
            "OS", "Filetype", "AGL Te.", "Protocol", "P", "Sendname", separator('-', 152));
      }
      Iterator<DeliveryRule> dit = t.getDeliveryRules().iterator();
      if (!dit.hasNext()) {
        System.out.format("%1s%-3s %1s %-20s %-40s %s %d %s%n", "!", t.getLine(), t.getLoadAction().getCode(), t.getProducer(), t.getFilePattern(),
            yn(t.isDatauAck()), 0, "No delivery rules!");
      } else {
        int d = 0;
        while (dit.hasNext()) {
          DeliveryRule dr = dit.next();
          if (d++ == 0) {
            System.out.format("%1s%-3s %1s %-20s %-40s %1s %d %-20s %-7s %-16s %-7s %-8s %1s %s%n", (t.isValid() ? getValidMark(dr, dr.getEndpoint()) : "!"),
                t.getLine(), t.getLoadAction().getCode(), t.getProducer(), t.getFilePattern(), yn(t.isDatauAck()), d, dr.getConsumer(), dr.getConsumerOs(),
                optString(dr.getFiletype()), optString(dr.getAglTenant()), dr.getProtocol(), yn(dr.isAllowCodedPaths()), dr.getDestName());
          } else {
            System.out.format("%1s%-3s %1s %63s %d %-20s %-7s %-16s %-7s %-8s %1s %s%n", getValidMark(dr, dr.getEndpoint()), dr.getLine(),
                dr.getLoadAction().getCode(), "", d, dr.getConsumer(), dr.getConsumerOs(), optString(dr.getFiletype()), optString(dr.getAglTenant()),
                dr.getProtocol(), yn(dr.isAllowCodedPaths()), dr.getDestName());
          }
        }
      }
    }
  }

  private void listLoadFetchRules(Set<FetchRule> rules) {
    int cnt = 0;
    System.out.println();
    for (FetchRule f : rules) {
      if (cnt++ == 0) {
        System.out.format("%-4s %s %-20s %-40s %-12s %s%n%s%n", "Line", "A", "Producer", "FilePattern", "Schedule", "Protocol", separator('-', 104));
      }
      System.out.format("%1s%-3s %s %-20s %-40s %-12s %s%n", f.isValid() ? "" : "!", f.getLine(), f.getLoadAction().getCode(), f.getProducer(),
          f.getFilePattern(), f.getSchedule(), f.getProtocol());
    }
  }

  private B2BiBulkLoader getLoader(String file, String format, boolean raiseError) throws TemplateException {
    File               infile      = new File(file);
    LoaderInput.FORMAT inputFormat = null;

    if (format != null) {
      try {
        inputFormat = LoaderInput.FORMAT.valueOf(format.toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new TemplateException("Invalid format: " + format);
      }
    } else {
      inputFormat = LoaderInput.getLoaderFormatFor(infile);
    }
    LoaderInput li = null;
    try (LoaderInput input = LoaderInput.createLoaderInput(inputFormat)) {
      input.setRaiseError(raiseError);
      input.readInput(infile);
      li = input;
    } catch (TemplateException te) {
      throw te;
    } catch (Exception e) {
      // ignore exception during close()
    }
    if (li instanceof DatauMeldesatzInput) {
      return new SFTFullDatauLoader(li);
    } else {
      return new SFTFullLoader(li);
    }
  }

  private void importFile(String filePath, String format, boolean caseSensitive, boolean dryrun, boolean force)
      throws TemplateException, ApiException, B2BLoadException {
    LOG.log(Level.INFO, "Import file {0}...", filePath);
    try (B2BiBulkLoader loader = getLoader(filePath, format, true)) {
      loader.setDryrun(dryrun);
      if (cfg.hasProperty(Props.PROP_REPORT)) {
        loader.collect();
      }
      loader.setRaiseError(!force);
      loader.setCaseSensitive(caseSensitive);
      loader.setDuplicateMissingDeliveryParams(cfg.getBoolean(Props.PROP_DUP_DELIVERYPARMS));
      loader.setFillDescription(FieldAction.valueOf(cfg.getString(Props.PROP_FILL_DESCRIPTION, "PREPEND").toUpperCase()));
      loader.setFillAdditionalInfo(FieldAction.valueOf(cfg.getString(Props.PROP_FILL_ADDINFO, "PREPEND").toUpperCase()));
      LoaderResult result = loader.load();
      createChangeReport(loader, cfg.getProperty(Props.PROP_REPORT));
      result.printResult(System.out);
    } catch (TemplateException | ApiException | B2BLoadException e) {
      if (cfg.hasProperty(Props.PROP_REPORT)) {
        StringBuilder sb = new StringBuilder("Dear team,\n\nthe processing of DATAU Meldesatz file failed:\n\n");
        sb.append("File      : ").append(filePath).append('\n');
        sb.append("Exception : ").append(e.getMessage()).append('\n');
        sb.append("Stacktrace:\n").append(ExceptionUtils.getStackTrace(e)).append('\n');
        sendMail(cfg.getString(Props.PROP_REPORT), "DATAU Meldesatz load FAILED!", sb.toString());
      }
      throw e;
    } catch (Exception e) {
      throw new B2BLoadException(e);
    }
  }

  private void createChangeReport(B2BiBulkLoader loader, String output) {
    if (!loader.getChangeSet().isCollect()) {
      return;
    }
    ChangeReport          rpt = new ChangeReport(loader.getChangeSet());

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintStream           ps  = new PrintStream(bos);
    ps.format("Change report for: %s%n", loader.getLoaderInput().getInputfile());
    ps.format("Created: %s%n", (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()));
    ps.println("--------------------------------------------------------------------");
    if (cfg.getBoolean(Props.PROP_DRY_RUN)) {
      ps.println();
      ps.println("### DRY-RUN MODE (no changes where made to the database) ###");
      ps.println();
    }
    rpt.printReport(ps);
    ps.println();
    loader.getResult().printResult(ps);
    ps.flush();

    if (output == null) {
      System.out.println(bos.toString());
    } else if (output.contains("@")) {
      String subj = String.format("Changereport for DATAU Meldesatz file %s%s", loader.getLoaderInput().getInputfile().getName(),
          (cfg.getBoolean(Props.PROP_DRY_RUN) ? " [DRY-RUN]" : ""));
      sendMail(output, subj, bos.toString());
    } else {
      try (OutputStream os = new FileOutputStream(output)) {
        os.write(bos.toByteArray());
      } catch (IOException e) {
        System.err.format("Could not write report file %s!%n", output);
        e.printStackTrace();
      }
    }
  }

  private void sendMail(String mailto, String subject, String body) {
    Properties mailprops = new Properties();
    mailprops.put("mail.smtp.auth", cfg.getProperty("mail.smtp.auth", "false"));
    mailprops.put("mail.smtp.starttls.enable", cfg.getProperty("mail.smtp.starttls.enable", "false"));
    mailprops.put("mail.smtp.host", cfg.getProperty("mail.smtp.host", "localhost"));
    mailprops.put("mail.smtp.port", cfg.getProperty("mail.smtp.port", "25"));
    Authenticator authenticator = null;
    if (cfg.getBoolean("mail.smtp.auth")) {
      authenticator = new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(cfg.getProperty("mail.smtp.auth.user"), cfg.getProperty("mail.smtp.auth.password"));
        }
      };
    }
    Session session = Session.getInstance(mailprops, authenticator);
    try {
      Message message = new MimeMessage(session);
      message.setFrom(new InternetAddress(cfg.getProperty("mail.from", "noreply@localhost")));
      message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailto));
      message.setSubject(subject);

      message.setContent("<pre>" + body + "</pre>", "text/html");
      Transport.send(message);
      System.out.println("Email Message Sent Successfully");
    } catch (MessagingException e) {
      throw new RuntimeException(e);
    }
  }

  private void validateImport(String filePath, String format) throws TemplateException {
    LOG.log(Level.INFO, "Validate import file {0}...", filePath);
    try (B2BiBulkLoader loader = getLoader(filePath, format, false)) {
      loader.setDuplicateMissingDeliveryParams(cfg.getBoolean(Props.PROP_DUP_DELIVERYPARMS));
      loader.setDryrun(true);
      loader.setRaiseError(false);
      boolean     valid       = loader.validate();
      LoaderInput loaderInput = loader.getLoaderInput();
      if (!loaderInput.getPartners().isEmpty()) {
        listLoadPartners(loaderInput.getPartners());
      }
      if (!loaderInput.getRules().isEmpty()) {
        listLoadRules(loaderInput.getRules());
      }
      if (!loaderInput.getFetchRules().isEmpty()) {
        listLoadFetchRules(loaderInput.getFetchRules());
      }
      if (valid) {
        System.out.format("%nFile %s is valid.%n", loaderInput.getInputfile().getAbsolutePath());
      } else {
        System.out.flush();
        System.err.format("%n!!! File %s is NOT valid !!!%n", loaderInput.getInputfile().getAbsolutePath());
        rc = 2;
      }
    } catch (TemplateException te) {
      throw te;
    } catch (Exception e) {
      e.printStackTrace();
      throw new TemplateException(e);
    }
  }
  // END loader
}
