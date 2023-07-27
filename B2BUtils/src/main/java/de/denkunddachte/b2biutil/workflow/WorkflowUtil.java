package de.denkunddachte.b2biutil.workflow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.xmlunit.diff.Difference;

import de.denkunddachte.util.ApiConfig;
import de.denkunddachte.b2biutil.AbstractConsoleApp;
import de.denkunddachte.b2biutil.Common;
import de.denkunddachte.b2biutil.workflow.ResourceSync.Result;
import de.denkunddachte.b2biutil.workflow.ResourceSync.SyncResult;
import de.denkunddachte.enums.ExecState;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.sfgapi.ApiClient;
import de.denkunddachte.sfgapi.Resources;
import de.denkunddachte.sfgapi.Resources.TYPE;
import de.denkunddachte.sfgapi.WorkFlowMonitor;
import de.denkunddachte.sfgapi.Workflow;
import de.denkunddachte.sfgapi.WorkflowDefinition;
import de.denkunddachte.sfgapi.WorkflowDefinition.VERSIONS;
import de.denkunddachte.sfgapi.XSLTDefinition;
import de.denkunddachte.siresource.SIArtifact;
import de.denkunddachte.utils.CommandLineParser;
import de.denkunddachte.utils.CommandLineParser.CommandLineException;
import de.denkunddachte.utils.CommandLineParser.ParsedCommandLine;
import de.denkunddachte.utils.Config;
import de.denkunddachte.utils.StringUtils;

public class WorkflowUtil extends AbstractConsoleApp {
  private static final Logger LOG    = Logger.getLogger(WorkflowUtil.class.getName());

  private List<String>        getProcessDataForSteps;
  private File                outdir = null;
  private Map<String, BPML>   bpmlSrc;

  static {
    OPTIONS.setProgramName(WorkflowUtil.class.getName());
    OPTIONS.setHasOptionalArgs(true);
    OPTIONS.setProgramDescription("Utility for deploying and testing B2Bi business processes ");

    // Common options
    OPTIONS.section("General options");
    OPTIONS.add(Props.PROP_XSLT + "|T", "Manage XLSTs");

    OPTIONS.section("Get WFDs/XSLTs from server");
    OPTIONS.add(Props.PROP_LIST_WFD + "|L:s", "List WFDs/XSLTs (optional filtered by WFD name)");
    OPTIONS.add(Props.PROP_GET + "|g=s", "Get WFD/XSLT with name");
    OPTIONS.add(Props.PROP_FILE + "|f=s", "BPML/XSLT filename");
    OPTIONS.add(Props.PROP_ALL_VERSIONS + "|a", "Get all versions of WFD/XSLT");
    OPTIONS.add(Common.PROP_VERSION + "|v=i", "Get/execute WFD/XSLT version (use 0 to get default version");
    OPTIONS.add(Props.PROP_GETALL + ":s", "Get all WFDs/XSLTs matching pattern (regex)");
    OPTIONS.add(Props.PROP_OUTDIR + "=s", "Output directory for --getall and --get");
    OPTIONS.add(Props.PROP_PARSE_WFD, "Show parsed WFD (for step trace)");

    // sync/compare
    OPTIONS.section("Sync/compare WFDs and XSLTs with local workspace");
    OPTIONS.add(Props.PROP_COMPARE + "|c=s",
        "Compare WFD/XSLT. Option must either point to a BPML/XSLT file or must contain the WFD/XSLT name (in this case the file must be specified with --file option).");
    OPTIONS.add(Props.PROP_VERBOSE, "Verbose (show differences)");
    OPTIONS.add(Props.PROP_EXPORT + "|e=s", "Export resources to local file (BPs and XSLTs). Use --force to overwrite existing export file.");
    OPTIONS.add(Props.PROP_IMPORT + "|i=s", "Import resources file (BPs and XSLTs)");
    OPTIONS.add("include=s", "Export include pattern (see doc for export service)", Props.PROP_INCLUDE);
    OPTIONS.add("exclude=s", "Export exclude pattern (see doc for export service)", Props.PROP_EXCLUDE);
    OPTIONS.add(Props.PROP_SYNC + "|s=s", "Sync WFD, XSLT, properties with local dir");
    OPTIONS.add("ignorePaths=s", "Regex to ignore local paths from sync", Props.PROP_IGNORE_PATHS);

    OPTIONS.add(Props.PROP_EXTRACT + "|x:s",
        "Extract resources differing local files (use --force to overwrite all). Optional: specify subdirs for resource types (e.g. WFD=BP:XSLT=XSLT)");

    // put
    OPTIONS.section("Update WFD/XSLT on server");
    OPTIONS.add(Props.PROP_PUT + "|p=s",
        "Update/create WFD/XSLT. Option must either point to a BPML/XSLT file or must contain the WFD/XSLT name (in this case the file must be specified with --file option).");
    OPTIONS.add(Props.PROP_COMMITMSG + "|m=s", "Commit message (description)");
    OPTIONS.add(Props.PROP_SET_VERSIONINFO + ":s", "Add/modify version info comment in BPML to force change recognition (optional arg: add additional message)",
        Props.PROP_SET_VERSIONINFO, "");
    OPTIONS.add(Props.PROP_SETASDEFAULT + "!b", "Set new version as default", Props.PROP_SETASDEFAULT, "true");
    OPTIONS.add(Props.PROP_FORCE, "Force update even if WFD are same");

    // delete, deleteAll
    OPTIONS.section("Delete WFDs on server");
    OPTIONS.add(Props.PROP_DELETE + "|d=s", "Delete WFD with name (requires --version)");
    OPTIONS.add(Props.PROP_DELETE_ALL + "=s", "Try to delete all versions of WFD");
    OPTIONS.add(Props.PROP_INCLUDE_DEFAULT, "Include default version with --deleteAll");

    // list + show workflow executions:
    OPTIONS.section("List/show workflow executions");
    OPTIONS.add(Props.PROP_LIST_BP + "|l:s", "List workflow executions (optional filtered by WFD name)");
    OPTIONS.add(Props.PROP_ALL, "Include system workflows (workflows with WFD.TYPE > 1)");
    OPTIONS.add(Props.PROP_STARTTIME + "=s", "List workflows with start time during last <n>[hm] or in range <yyyyMMddHHmmss>-<yyyyMMddHHmmss>",
        Props.PROP_STARTTIME, "1h");
    OPTIONS.add(Props.PROP_FAILED, "Show only unsuccessful workflows");
    OPTIONS.add(Common.PROP_NOUTF8, "Avoid UTF-8 output");
    OPTIONS.add(Props.PROP_SHOW + "|S=n", "Show workflow with ID <n>");
    OPTIONS.add(Props.PROP_TRACE_WFD + "|X", "Trace WFD execution");

    // execute
    OPTIONS.section("Execute BP");
    OPTIONS.add(Props.PROP_EXECUTE + "|E=s", "Execute workflow name");
    OPTIONS.add(Props.PROP_PRIMARY_DOCUMENT + "|P=s", "Use file as primary document.");
    OPTIONS.add(Props.PROP_DATA + "=s", "Use string data as primary document.");
    OPTIONS.add(Props.PROP_FILENAME + "|n=s", "Set filename for primary document.");
    OPTIONS.add(Props.PROP_PROCESSDATA + "=s",
        "Get processdata for steps (comma separated list, if empty, get PD for all steps). With --outdir, PD ist written to file(s).");

    OPTIONS.addProgramHelp("Some help... ");
  }

  public WorkflowUtil(String[] args) throws CommandLineException, ApiException {
    super(args);
  }

  @Override
  protected CommandLineParser getCommandLineConfig() {
    return OPTIONS;
  }

  @Override
  protected void init(ParsedCommandLine cmdline) throws CommandLineException {
    if (cmdline.isSet(Props.PROP_EXTRACT)) {
      if (!StringUtils.isNullOrWhiteSpace(cfg.getString(Props.PROP_EXTRACT))) {
        cfg.setProperty(Props.PROP_EXTRACT_PATHS, cfg.getString(Props.PROP_EXTRACT));
      }
      cfg.setProperty(Props.PROP_EXTRACT, true);
    }
    if (cfg.hasProperty(Props.PROP_PUT)) {
      if (!(new File(cfg.getString(Props.PROP_PUT))).canRead()
          && (!cfg.hasProperty(Props.PROP_FILE) || !(new File(cfg.getString(Props.PROP_FILE))).canRead())) {
        throw new CommandLineException("Option --" + Props.PROP_FILE + " not set or not readable!");
      }
    } else if (cfg.hasProperty(Props.PROP_GETALL)) {
      if (!cfg.hasProperty(Props.PROP_OUTDIR)) {
        throw new CommandLineException("Option --" + Props.PROP_OUTDIR + " not set!");
      }
    } else if (cfg.hasProperty(Props.PROP_GET)) {
    } else if (cfg.hasProperty(Props.PROP_DELETE)) {
      if (cfg.getInt(Common.PROP_VERSION, 0) == 0) {
        throw new CommandLineException("Specify WFD version with --version option!");
      }
    } else if (cfg.hasProperty(Props.PROP_DELETE_ALL)) {
    } else if (cfg.hasProperty(Props.PROP_LIST_BP)) {
    } else if (cfg.hasProperty(Props.PROP_LIST_WFD)) {
    } else if (cfg.hasProperty(Props.PROP_SHOW)) {
      if (cfg.getLong(Props.PROP_SHOW) == 0) {
        throw new CommandLineException("Specify workflow ID with --show option!");
      }
    } else if (cfg.hasProperty(Props.PROP_EXECUTE)) {
    } else if (cfg.hasProperty(Props.PROP_COMPARE)) {
      if (!(new File(cfg.getString(Props.PROP_COMPARE))).canRead()
          && (!cfg.hasProperty(Props.PROP_FILE) || !(new File(cfg.getString(Props.PROP_FILE))).canRead())) {
        throw new CommandLineException("Option --" + Props.PROP_FILE + " not set or not readable!");
      }
    } else if (cfg.hasProperty(Props.PROP_EXPORT)) {
      if ((new File(cfg.getString(Props.PROP_EXPORT)).exists())) {
        System.err.format("NOTE: export file %s exists. Use --force to overwrite.", cfg.getString(Props.PROP_EXPORT));
      }
    } else if (cfg.hasProperty(Props.PROP_IMPORT)) {

      if (!(new File(cfg.getString(Props.PROP_IMPORT))).exists()) {
        throw new CommandLineException("Option --" + Props.PROP_IMPORT + " not set or not readable!");
      }
    } else if (cfg.hasProperty(Props.PROP_SYNC)) {
      if (!cfg.hasProperty(Props.PROP_EXPORT)) {
        cfg.setProperty(Props.PROP_EXPORT, "tmpexport.xml");
      }
      if (!(new File(cfg.getString(Props.PROP_SYNC))).isDirectory()) {
        throw new CommandLineException("Local sync folder" + cfg.getString(Props.PROP_SYNC) + " does not exist!");
      }
    } else {
      throw new CommandLineException(
          "No operation specified! Use --list-wfd, --get, --getall, --put, --delete, --deleteAll, --execute, --list-bp, --show, --compare, --export, --import, --sync to specify operation.");
    }

    if (cfg.getBoolean(Props.PROP_TRACE_WFD) || cfg.getBoolean(Props.PROP_PARSE_WFD)) {
      bpmlSrc = new HashMap<>();
      cfg.setProperty(Props.PROP_SHOW_DETAILS, true);
    }
    getProcessDataForSteps = new ArrayList<>();
    if (cfg.hasProperty(Props.PROP_PROCESSDATA) && !cfg.getString(Props.PROP_PROCESSDATA).isEmpty()) {
      getProcessDataForSteps.addAll(Arrays.asList(cfg.getString(Props.PROP_PROCESSDATA).split(",")));
      cfg.setProperty(Props.PROP_SHOW_DETAILS, true);
    }
    if (cfg.hasProperty(Props.PROP_EXECUTE)) {
      if (cfg.hasProperty(Props.PROP_PRIMARY_DOCUMENT) && !(new File(cfg.getString(Props.PROP_PRIMARY_DOCUMENT))).canRead()) {
        throw new CommandLineException("PrimaryDocument file " + cfg.getString(Props.PROP_PRIMARY_DOCUMENT) + " not readable!");
      }
    }

    if (cfg.hasProperty(Props.PROP_OUTDIR)) {
      outdir = new File(cfg.getString(Props.PROP_OUTDIR));
      if (!outdir.isDirectory() && !outdir.mkdirs()) {
        throw new CommandLineException("Could not create output directory " + outdir + "!");
      }
    }
  }

  public static void main(String[] args) throws IOException {
    try (WorkflowUtil wfu = new WorkflowUtil(args)) {
      Config cfg = Config.getConfig();

      LOG.log(Level.FINER, "Populate ApiConfig from Config: {0}", cfg.getLoadedResources());
      ApiConfig apicfg = ApiConfig.getInstance(cfg.getStringMap(null));
      LOG.log(Level.FINE, "ApiConfig: {0}", apicfg.getConfigFiles());

      if (cfg.hasProperty(Props.PROP_LIST_WFD)) {
        VERSIONS listVersions = VERSIONS.LAST;
        if (cfg.getBoolean(Props.PROP_ALL_VERSIONS)) {
          listVersions = VERSIONS.ALL;
        } else if (cfg.hasProperty(Common.PROP_VERSION) && cfg.getInt(Common.PROP_VERSION) == 0) {
          listVersions = VERSIONS.DEFAULT;
        }
        if (cfg.getBoolean(Props.PROP_XSLT)) {
          wfu.listXslt(cfg.getProperty(Props.PROP_LIST_WFD), listVersions);
        } else {
          wfu.listWfd(cfg.getProperty(Props.PROP_LIST_WFD), listVersions);
        }
      } else if (cfg.hasProperty(Props.PROP_GET)) {
        File out = null;
        if (cfg.hasProperty(Props.PROP_FILE)) {
          out = new File(cfg.getString(Props.PROP_FILE));
        } else if (cfg.hasProperty(Props.PROP_OUTDIR)) {
          out = new File(cfg.getString(Props.PROP_OUTDIR));
        }
        if (cfg.getBoolean(Props.PROP_XSLT)) {
          wfu.getXslt(cfg.getString(Props.PROP_GET), cfg.getInt(Common.PROP_VERSION, 0), out, cfg.getBoolean(Props.PROP_ALL_VERSIONS));
        } else {
          wfu.getWfd(cfg.getString(Props.PROP_GET), cfg.getInt(Common.PROP_VERSION, 0), out, cfg.getBoolean(Props.PROP_ALL_VERSIONS));
        }
      } else if (cfg.hasProperty(Props.PROP_GETALL)) {
        File outdir = new File(cfg.getString(Props.PROP_OUTDIR));
        if (!outdir.isDirectory() && !outdir.mkdirs()) {
          throw new ApiException("Could not create output directory " + outdir + "!");
        }
        Pattern p = null;
        if (!StringUtils.isNullOrEmpty(cfg.getString(Props.PROP_GETALL))) {
          p = Pattern.compile(cfg.getString(Props.PROP_GETALL));
        }

        if (cfg.getBoolean(Props.PROP_XSLT)) {
          wfu.getAllXslt(p, outdir, cfg.getBoolean(Props.PROP_ALL_VERSIONS));
        } else {
          wfu.getAllWfd(p, outdir, cfg.getBoolean(Props.PROP_ALL_VERSIONS));
        }
      } else if (cfg.hasProperty(Props.PROP_PUT)) {
        String name   = null;
        File   infile = new File(cfg.getString(Props.PROP_PUT));
        if (!infile.exists()) {
          name = cfg.getString(Props.PROP_PUT);
          infile = new File(cfg.getString(Props.PROP_FILE));
        }
        String msg = cfg.hasProperty(Props.PROP_COMMITMSG) ? cfg.getString(Props.PROP_COMMITMSG) : "WorkflowUtil - " + System.getProperty("user.name");
        wfu.put(name, msg, infile, cfg.getBoolean(Props.PROP_SETASDEFAULT), cfg.getProperty(Props.PROP_SET_VERSIONINFO));
      } else if (cfg.hasProperty(Props.PROP_LIST_BP)) {
        wfu.listBp(cfg.getString(Props.PROP_LIST_BP), cfg.getProperty(Props.PROP_STARTTIME), cfg.getBoolean(Props.PROP_FAILED), cfg.getBoolean(Props.PROP_ALL));
      } else if (cfg.hasProperty(Props.PROP_SHOW)) {
        wfu.show(cfg.getLong(Props.PROP_SHOW), cfg.getBoolean(Props.PROP_SHOW_DETAILS));
      } else if (cfg.hasProperty(Props.PROP_DELETE)) {
        wfu.delete(cfg.getString(Props.PROP_DELETE), cfg.getInt(Common.PROP_VERSION), false, false);
      } else if (cfg.hasProperty(Props.PROP_DELETE_ALL)) {
        wfu.delete(cfg.getString(Props.PROP_DELETE_ALL), 0, true, cfg.getBoolean(Props.PROP_INCLUDE_DEFAULT));
      } else if (cfg.hasProperty(Props.PROP_COMPARE)) {
        String name   = null;
        File   infile = new File(cfg.getString(Props.PROP_COMPARE));
        if (!infile.exists()) {
          name = cfg.getString(Props.PROP_COMPARE);
          infile = new File(cfg.getString(Props.PROP_FILE));
        }
        wfu.compare(name, infile);
      } else if (cfg.hasAny(Props.PROP_EXPORT, Props.PROP_SYNC)) {
        File exportFile = new File(cfg.getString(Props.PROP_EXPORT));
        if (!exportFile.exists() || cfg.getBoolean(Props.PROP_FORCE)) {
          wfu.export(exportFile);
          if ("tmpexport.xml".equals(exportFile.getName())) {
            exportFile.deleteOnExit();
          }
        }
        if (cfg.hasProperty(Props.PROP_SYNC)) {
          wfu.sync(exportFile, cfg.getString(Props.PROP_SYNC));
        }
      }

      // execute can be combined with any other command
      if (cfg.hasProperty(Props.PROP_EXECUTE)) {
        File   primDocFile = null;
        String filename    = cfg.getProperty(Props.PROP_FILENAME);
        if (cfg.hasProperty(Props.PROP_PRIMARY_DOCUMENT)) {
          primDocFile = new File(cfg.getString(Props.PROP_PRIMARY_DOCUMENT));
          wfu.execute(cfg.getString(Props.PROP_EXECUTE), cfg.getInt(Common.PROP_VERSION), primDocFile, filename, cfg.getBoolean(Props.PROP_SHOW_DETAILS));
        } else {
          byte[] data = null;
          if (cfg.hasProperty(Props.PROP_DATA)) {
            data = cfg.getString(Props.PROP_DATA).getBytes();
          }
          wfu.execute(cfg.getString(Props.PROP_EXECUTE), cfg.getInt(Common.PROP_VERSION), data, filename, cfg.getBoolean(Props.PROP_SHOW_DETAILS));
        }
      }
      System.exit(wfu.getRc());
    } catch (CommandLineException e) {
      e.printStackTrace(System.err);
      System.exit(3);
    } catch (ApiException e) {
      e.printStackTrace(System.err);
      System.exit(2);
    }
  }

  private void writeToFile(ApiClient wfd, File outfile) throws ApiException {
    if (wfd.isRefreshRequired()) {
      LOG.log(Level.FINE, "Refresh WFD object: {0}", wfd);
      wfd.refresh();
    }
    String name    = null;
    int    version = 0;
    try (Writer wr = new FileWriter(outfile)) {
      if (wfd instanceof WorkflowDefinition) {
        WorkflowDefinition o = (WorkflowDefinition) wfd;
        wr.append(o.getBusinessProcess());
        name = o.getName();
        version = o.getWfdVersion();
      } else if (wfd instanceof XSLTDefinition) {
        XSLTDefinition o = (XSLTDefinition) wfd;
        wr.append(o.getXsltData());
        name = o.getName();
        version = o.getVersion();
      }
    } catch (IOException e) {
      throw new ApiException(e);
    }
    System.out.format("Wrote %s/%d to %s%n", name, version, outfile.getAbsolutePath());
  }

  private void listWfd(String wfdNamePattern, VERSIONS listVersions) throws ApiException {
    LOG.log(Level.FINEST, "Enter listWfd(): wfdNamePattern={0}, listVersions={1}", new Object[] { wfdNamePattern, listVersions });

    List<WorkflowDefinition> wfdlist = WorkflowDefinition.findAll(wfdNamePattern, listVersions, false, "wfdID", "name", "wfdVersion", "defaultVersion",
        "enableBusinessProcess", "timestamp", "modifiedBy", "description");

    if (wfdlist.isEmpty()) {
      System.out.println("No matching workflow definitions found!");
      return;
    }
    Collections.sort(wfdlist, new Comparator<WorkflowDefinition>() {
      @Override
      public int compare(WorkflowDefinition o1, WorkflowDefinition o2) {
        return o1.getName().compareTo(o2.getName()) == 0 ? Integer.compare(o1.getWfdVersion(), o2.getWfdVersion()) * -1 : o1.getName().compareTo(o2.getName());
      }
    });
    System.out.format("Found %d workflow definitions.%n%n", wfdlist.size());
    System.out.format("%2s %-5s %-45s %-3s %2s %-17s %-13s %s%n", "En", "ID", "WFD name", "Ver", "Df", "Modified", "User", "Comment");
    System.out.println("------------------------------------------------------------------------------------------------------------------------");
    for (WorkflowDefinition wfd : wfdlist) {
      int version = wfd.getWfdVersion();
      System.out.format("%-2s %-5d %-45.45s %-3d %-2s %-17s %-13.13s %s%n", (wfd.isEnableBusinessProcess() ? OK : NOK), wfd.getWfdId(), wfd.getName(), version,
          (wfd.isDefaultVersion() ? MARK : "  "), (wfd.getTimestamp() == null ? "-" : wfd.getTimestamp().format(FMT_DTTM)), wfd.getModifiedBy(),
          wfd.getDescription());
    }
  }

  private void listXslt(String xsltNamePattern, VERSIONS listVersions) throws ApiException {
    LOG.log(Level.FINEST, "Enter listxslt(): xlstNamePattern={0}, listVersions={1}", new Object[] { xsltNamePattern, listVersions });

    List<XSLTDefinition> xsltlist = XSLTDefinition.findAll(xsltNamePattern, listVersions, false);

    if (xsltlist.isEmpty()) {
      System.out.println("No matching XSLTs found!");
      return;
    }
    Collections.sort(xsltlist, new Comparator<XSLTDefinition>() {
      @Override
      public int compare(XSLTDefinition o1, XSLTDefinition o2) {
        return o1.getName().compareTo(o2.getName()) == 0 ? Integer.compare(o1.getVersion(), o2.getVersion()) * -1 : o1.getName().compareTo(o2.getName());
      }
    });
    System.out.format("Found %d XSLTs.%n%n", xsltlist.size());
    System.out.format("%2s %-45s %-3s %2s %-17s %-20s %s%n", "En", "Name", "Ver", "Df", "Modified", "User", "Comment");
    System.out.println("------------------------------------------------------------------------------------------------------------------------");
    for (XSLTDefinition xslt : xsltlist) {
      int version = xslt.getVersion();
      System.out.format("%-2s %-45.45s %-3d %-2s %-17s %-20.20s %s%n", (xslt.isEnabled() ? OK : NOK), xslt.getName(), version,
          (xslt.isDefaultVersion() ? MARK : "  "), (xslt.getModifyTime() == null ? "-" : xslt.getModifyTime().format(FMT_DTTM)), xslt.getModifiedBy(),
          xslt.getDescription());
    }
  }

  private void parseWfd(WorkflowDefinition wfd) throws ApiException {
    if (!cfg.hasProperty(Props.PROP_PARSE_WFD)) {
      return;
    }
    LOG.log(Level.FINEST, "Enter parseWfd(): wfd={0}", wfd.getId());
    BPML src = bpmlSrc.get(wfd.getId());
    if (src == null) {
      System.err.println("No parsed source for " + wfd.getId() + "!");
      return;
    }
    System.out.format("WFD: %s%n", wfd.getId());
    for (int i = 0; i < src.getActivities().size(); i++) {
      if (i == 0) {
        System.out.format("%-5s %-12s %-10s %s%n", "ID", "Location", "Type", "Activity");
        System.out.println(separator('-', 100));
      }
      System.out.format("%-5d %-12s %-10s %s%n", i, src.getActivityLocation(i, wfd.getName()), src.getActivity(i).getType(), src.getActivityDescription(i));
    }
  }

  private void getWfd(String wfdName, int wfdVersion, File output, boolean getAllVersions) throws ApiException {
    LOG.log(Level.FINEST, "Enter get(): wfdName={0}, wfdVersion={1}", new Object[] { wfdName, wfdVersion });
    WorkflowDefinition wfd = WorkflowDefinition.find(wfdName, wfdVersion);
    if (bpmlSrc != null) {
      bpmlSrc.put(wfd.getId(), BPML.parse(wfd.getBusinessProcess()));
      parseWfd(wfd);
    }

    if (wfd != null) {
      LOG.log(Level.FINE, "Found WFD: {0}, refresh required: {1}", new Object[] { wfd, wfd.isRefreshRequired() });
      System.out.format("%-16s: %s%n", "WFD name", wfd.getName());
      System.out.format("%-16s: %s [WFD ID %s, committed %s by %s]%n", "WFD version", wfd.getWfdVersion(), wfd.getWfdId(), wfd.getTimestamp(),
          wfd.getModifiedBy());
      System.out.format("%-16s: %s%n", "is default?", wfd.isDefaultVersion());
      if (output != null) {
        if (getAllVersions) {
          getAllVersions(wfd, output);
        } else {
          writeToFile(wfd, (output.isDirectory() ? new File(output, wfd.getName() + ".bpml") : output));
        }
      } else {
        if (getAllVersions) {
          System.out.format("%-16s: %s%n", "Versions", wfd.getWfdVersions());
          System.out.format("%-16s: %s%n", "Default version", wfd.getDefaultVersion());
        } else {
          System.out.println("BP data:");
          System.out.println(wfd.getBusinessProcess());
        }
      }
    } else {
      System.out.format("WFD %s (version: %s) not found!%n", wfdName, (wfdVersion > 0 ? wfdVersion : "default"));
    }
  }

  private void getAllVersions(WorkflowDefinition wfd, File outdir) throws ApiException {
    for (int v : wfd.getWfdVersions()) {
      WorkflowDefinition wfdv = WorkflowDefinition.find(wfd.getName(), v);
      if (wfdv != null) {
        writeToFile(wfdv, new File(outdir, String.format("%s-%03d%s.bpml", wfdv.getName(), wfdv.getWfdVersion(), (wfdv.isDefaultVersion() ? "-default" : ""))));
      } else {
        System.err.format("Could not get WFD %s/%s!%n", wfd.getName(), v);
      }
    }
  }

  private void getXslt(String xsltName, int version, File output, boolean getAllVersions) throws ApiException {
    LOG.log(Level.FINEST, "Enter get(): xsltName={0}, version={1}, output={2}, getAllVersions={3}", new Object[] { xsltName, version, output, getAllVersions });
    XSLTDefinition xslt = XSLTDefinition.find(xsltName, version);

    if (xslt != null) {
      LOG.log(Level.FINE, "Found XSLT: {0}, refresh required: {1}", new Object[] { xslt, xslt.isRefreshRequired() });
      System.out.format("%-16s: %s%n", "XSLT name", xslt.getName());
      System.out.format("%-16s: %s [committed %s by %s]%n", "XSLT version", xslt.getVersion(), xslt.getModifyTime(), xslt.getModifiedBy());
      System.out.format("%-16s: %s%n", "is default?", xslt.isDefaultVersion());
      if (output != null) {
        if (getAllVersions) {
          getAllVersions(xslt, output);
        } else {
          writeToFile(xslt, (output.isDirectory() ? new File(output, xslt.getName() + ".xslt") : output));
        }
      } else {
        if (getAllVersions) {
          System.out.format("%-16s: %s%n", "Versions", xslt.getVersions());
          System.out.format("%-16s: %s%n", "Default version", xslt.getDefaultVersion());
        } else {
          System.out.println("XSLT data:");
          System.out.println(xslt.getXsltData());
        }
      }
    } else {
      System.out.format("XSLT %s (version: %s) not found!%n", xsltName, (version > 0 ? version : "default"));
    }
  }

  private void getAllVersions(XSLTDefinition xslt, File outdir) throws ApiException {
    for (int v : xslt.getVersions()) {
      XSLTDefinition xlsv = XSLTDefinition.find(xslt.getName(), v);
      if (xlsv != null) {
        writeToFile(xlsv, new File(outdir, String.format("%s-%03d%s.bpml", xlsv.getName(), xlsv.getVersion(), (xlsv.isDefaultVersion() ? "-default" : ""))));
      } else {
        System.err.format("Could not get XSLT %s/%s!%n", xslt.getName(), v);
      }
    }
  }

  private void getAllWfd(Pattern pattern, File outdir, boolean getAllVersions) throws ApiException {
    LOG.log(Level.FINEST, "Enter getAllWfd(): pattern={0}, outdir={1}", new Object[] { pattern, outdir });
    String filter = null;
    if (pattern != null && pattern.pattern().matches("^[A-Za-z0-9_-].*")) {
      filter = pattern.pattern().replaceFirst("^([A-Za-z0-9_-]+).*", "$1");
    }
    List<WorkflowDefinition> wfds = WorkflowDefinition.findAll(filter, VERSIONS.DEFAULT, false);

    for (WorkflowDefinition wfd : wfds) {
      if (pattern != null) {
        Matcher m = pattern.matcher(wfd.getName());
        if (!m.matches()) {
          LOG.log(Level.FINER, "Ignore WFD {0} (does not match pattern {1}).", new Object[] { wfd.getName(), pattern.pattern() });
          continue;
        }
      }
      if (getAllVersions) {
        getAllVersions(wfd, outdir);
      } else {
        writeToFile(wfd, new File(outdir, wfd.getName() + ".bpml"));
      }
    }
  }

  private void getAllXslt(Pattern pattern, File outdir, boolean getAllVersions) throws ApiException {
    LOG.log(Level.FINEST, "Enter getAllXslt(): pattern={0}, outdir={1}", new Object[] { pattern, outdir });
    String filter = null;
    if (pattern != null && pattern.pattern().matches("^[A-Za-z0-9_-].*")) {
      filter = pattern.pattern().replaceFirst("^([A-Za-z0-9_-]+).*", "$1%");
    }
    List<XSLTDefinition> wfds = XSLTDefinition.findAll(filter, VERSIONS.DEFAULT, false);

    for (XSLTDefinition xslt : wfds) {
      if (pattern != null) {
        Matcher m = pattern.matcher(xslt.getName());
        if (!m.matches()) {
          LOG.log(Level.FINER, "Ignore XSLT {0} (does not match pattern {1}).", new Object[] { xslt.getName(), pattern.pattern() });
          continue;
        }
      }
      if (getAllVersions) {
        getAllVersions(xslt, outdir);
      } else {
        writeToFile(xslt, new File(outdir, xslt.getName() + ".xslt"));
      }
    }
  }

  private void delete(String wfdName, int wfdVersion, boolean deleteAllVersions, boolean includeDefaultVersion) throws ApiException {
    LOG.log(Level.FINEST, "Enter delete(): wfdName={0}, wfdVersion={1}, deleteAllVersions={2}, includeDefaultVersion={3}",
        new Object[] { wfdName, wfdVersion, deleteAllVersions, includeDefaultVersion });

    WorkflowDefinition wfd = null;
    if (wfdVersion == 0) {
      wfd = WorkflowDefinition.find(wfdName);
    } else {
      wfd = WorkflowDefinition.find(wfdName, wfdVersion);
    }

    if (wfd == null) {
      System.out.format("WFD %s (version: %s) not found!%n", wfdName, (wfdVersion > 0 ? wfdVersion : "default"));
      return;
    }

    LOG.log(Level.FINE, "Found WFD: {0}, version: {1}", new Object[] { wfd, wfd.getWfdVersion() });
    if (deleteAllVersions) {
      List<Integer> deletedVersions = wfd.deleteAll(includeDefaultVersion);
      if (deletedVersions.isEmpty()) {
        System.err.format("No versions of WFD %s deleted!%n", wfd.getName());
      } else {
        System.out.format("Deleted WFD %s versions: %s%n", wfd.getName(), deletedVersions);
      }
    } else {
      if (wfd.delete()) {
        System.out.format("Deleted WFD %s (version: %s).%n", wfd.getName(), wfd.getWfdVersion());
      } else {
        System.err.format("Could not delete WFD %s (version: %s): %s%n", wfd.getName(), wfd.getWfdVersion(), ApiClient.getApiErrorMsg());
      }
    }
  }

  private ApiClient put(String name, String commitMsg, File infile, boolean setAsDefault, String versionInfoMsg) throws ApiException {
    if (getType(infile.getName()) == TYPE.XSLT) {
      return putXSLT(name, commitMsg, infile, setAsDefault);
    } else { 
      return putWfd(name, commitMsg, infile, setAsDefault, versionInfoMsg);
    }
  }

  private WorkflowDefinition putWfd(String wfdName, String commitMsg, File infile, boolean setAsDefault, String versionInfoMsg) throws ApiException {
    LOG.log(Level.FINEST, "Enter put(): bpName={0}, commitMsg={1}, infile={2}, setAsDefault={3}, setVersionInfo={4}",
        new Object[] { wfdName, commitMsg, infile, setAsDefault, versionInfoMsg });
    WorkflowDefinition wfd;
    if (wfdName == null) {
      wfd = new WorkflowDefinition(infile, commitMsg);
      wfdName = wfd.getName();
    }
    wfd = WorkflowDefinition.find(wfdName);

    boolean setVersionInfo = versionInfoMsg != null;
    versionInfoMsg = "true".equalsIgnoreCase(versionInfoMsg) ? null : versionInfoMsg;
    if (wfd == null) {
      LOG.log(Level.FINE, "WFD {0} not found. Create new...", wfdName);
      wfd = new WorkflowDefinition(infile, commitMsg);
      if (setVersionInfo)
        wfd.setVersionInfo(versionInfoMsg);
      if (!wfd.create()) {
        throw new ApiException("Could not create WFD \"" + wfdName + "\" from file " + infile + "!");
      }
      wfd.refresh();
      System.out.format("Created new WFD %s/%d with ID=%d.%n", wfd.getName(), wfd.getWfdVersion(), wfd.getWfdId());
    } else {
      int prevVersion = wfd.getWfdVersion();
      if (!cfg.getBoolean(Props.PROP_FORCE)) {
        XmlDiff diff = compareSrc(infile, wfd);
        if (!diff.differs()) {
          System.out.format("Skip upload of WFD %s: File %s is same as server version %d (use --force to update anyway).%n", wfd.getName(), infile.getPath(),
              prevVersion);
          return wfd;
        }
      }
      wfd.setBusinessProcess(infile);
      wfd.setDescription(commitMsg);
      if (setVersionInfo)
        wfd.setVersionInfo(versionInfoMsg);
      wfd.setSetThisVersionAsDefault(setAsDefault);
      if (!wfd.update()) {
        throw new ApiException("Could not update WFD \"" + wfdName + "\" from file " + infile + "!");
      }
      // if (!ApiConfig.getInstance().isWfdUseApiToSetDefault()) {
      // wfd.refresh();
      // }
      // API BUG: even with setThisVersionAsDefault=1, SI will still use the previous version internally
      // although dashboard shows new version as default.
      // Calling "changeDefaultVersion" API also does not work. The only way to force SI to use the updated
      // BP code is to disable the previous version.
      if (setAsDefault && cfg.getBoolean(Props.PROP_DISABLE_WFD_VERSION_AFTER_UPDATE)) {
        if (WorkflowDefinition.toggleEnabledWorkflow(wfd.getName(), prevVersion, false)) {
          System.out.format("Disabled WFD %s/%d (workaround for API bug).%n", wfd.getName(), prevVersion);
        } else {
          throw new ApiException("Could disable WFD \"" + wfd.getName() + "/" + prevVersion + "!");
        }
      }
      System.out.format("Updated WFD %s/%d (isDefault: %s).%n", wfd.getName(), wfd.getWfdVersion(), wfd.isDefaultVersion());
    }
    return wfd;
  }

  private XSLTDefinition putXSLT(String xsltName, String commitMsg, File infile, boolean setAsDefault) throws ApiException {
    LOG.log(Level.FINEST, "Enter put(): xsltName={0}, commitMsg={1}, infile={2}, setAsDefault={3}", new Object[] { xsltName, commitMsg, infile, setAsDefault });
    xsltName = getName(xsltName, infile);

    XSLTDefinition xslt = XSLTDefinition.find(xsltName);

    if (xslt == null) {
      LOG.log(Level.FINE, "XSLT {0} not found. Create new...", xsltName);
      xslt = new XSLTDefinition(xsltName, infile, commitMsg);
      xslt.setComment(commitMsg);
      if (!xslt.create()) {
        throw new ApiException("Could not create XSLT \"" + xsltName + "\" from file " + infile + "!");
      }
      xslt.refresh();
      System.out.format("Created new XSLT %s/%d.%n", xslt.getName(), xslt.getVersion());
    } else {
      int prevVersion = xslt.getVersion();
      if (!cfg.getBoolean(Props.PROP_FORCE)) {
        XmlDiff diff = compareSrc(infile, xslt);
        if (!diff.differs()) {
          System.out.format("Skip upload of XSLT %s: File %s is same as server version %d (use --force to update anyway).%n", xslt.getName(), infile.getPath(),  prevVersion);
          return xslt;
        }
      }
      xslt.setXsltData(infile);
      xslt.setComment(commitMsg);
      xslt.setDescription("Workflowutil description...");
      xslt.setSetThisVersionAsDefault(setAsDefault);
      if (!xslt.update()) {
        throw new ApiException("Could not update XSLT \"" + xsltName + "\" from file " + infile + "!");
      }
      xslt.refresh();
      System.out.format("Updated XSLT %s/%d (isDefault: %s).%n", xslt.getName(), xslt.getVersion(), xslt.isDefaultVersion());
    }
    return xslt;
  }

  private void listBp(String bpName, String startTime, boolean showOnlyFailed, boolean showSystemWorkflows) throws ApiException {
    LOG.log(Level.FINEST, "Enter listBp(): bpName={0}, startTime={1}, showSystemWorkflows={2}", new Object[] { bpName, startTime, showSystemWorkflows });
    List<Workflow> wflist = Workflow.findAll(bpName, startTime, showOnlyFailed, showSystemWorkflows);

    if (wflist.isEmpty()) {
      System.out.println("No matching workflow executions found!");
      return;
    }
    System.out.format("Found %d workflows.%n%n", wflist.size());
    System.out.format("%-2s %-12s %-8s %-8s %-5s %-50s %-3s%n", OK, "Workflow ID", "Start", "End", "Steps", "WFD name", "Version");
    System.out.println("---------------------------------------------------------------------------------------------");
    for (Workflow wf : wflist) {
      System.out.format("%-2s %-12d %8s %8s %-5d %-50.50s %-3d%n", (wf.getExeState() == ExecState.SUCCESS ? OK : NOK), wf.getWorkFlowId(),
          FMT_HHMMSS.format(wf.getStartTime()), FMT_HHMMSS.format(wf.getEndTime()), wf.getLastStepId(), wf.getWfdName(), wf.getWfdVersion());
    }
  }

  private void show(long workflowId, boolean detail) throws ApiException {
    LOG.log(Level.FINEST, "Enter show(): workflowId={0}, detail={1}", new Object[] { workflowId, detail });
    Workflow wf = Workflow.find(workflowId, detail);

    if (wf == null) {
      System.out.format("Workflow %s not found!%n", workflowId);
      return;
    }
    System.out.format("Found workflow %s.%n%n", workflowId);
    show(wf, detail);
  }

  private void show(Workflow wf, boolean detail) throws ApiException {
    showWorkflow(wf, detail);
    for (Workflow cwf : wf.getChildren()) {
      System.out.format("%nChild workflow: %s [%s]%n", cwf.getWorkFlowId(), cwf.getWfdName());
      showWorkflow(cwf, detail);
    }
  }

  private void showWorkflow(Workflow wf, boolean detail) throws ApiException {
    System.out.format("%-16s: %s%n", "Workflow ID", wf.getWorkFlowId());
    System.out.format("%-16s: %s (version: %d)%n", "WFD name", wf.getWfdName(), wf.getWfdVersion());
    System.out.format("%-16s: %s%n", "Started", FMT_ISO_TIMESTAMP.format(wf.getStartTime()));
    System.out.format("%-16s: %s%n", "Ended", FMT_ISO_TIMESTAMP.format(wf.getEndTime()));
    System.out.format("%-16s: first: %d, last: %d, count: %d%n%n", "Steps", wf.getStepId(), wf.getLastStepId(), wf.getStepsCounted());
    if (wf.getParent() != null) {
      System.out.format("%-16s: %s%n", "Parent ID", wf.getParent().getWorkFlowId());
    }
    if (!wf.getChildren().isEmpty()) {
      System.out.format("%-16s: %s%n", "Children", wf.getChildren().stream().map(c -> c.getWorkFlowId() + "/" + c.getWfdName()).collect(Collectors.toList()));
    }
    int errorCount = 0;
    if (!detail)
      return;
    int width = 138;
    if (bpmlSrc == null) {
      System.out.format("%-2s %-4s %-3s %-50.50s %-8s %-8s %-12.12s %-3s %s%n", OK, "Step", "AId", "Service name", "Start", "End", "exeState", "Sts",
          "Adv. status");
    } else {
      System.out.format("%-2s %-4s %-8s %-8s %-12.12s %-3s %-50.50s %-30.30s %-3s %s%n", OK, "Step", "Start", "End", "exeState", "AId", "Activity", "Location",
          "Sts", "Adv. status");
      width = 160;
    }
    System.out.println(separator('-', width));
    for (int stepId = 0; stepId < wf.getNumberOfSteps(); stepId++) {
      WorkFlowMonitor s = wf.getWfStep(stepId);
      if (bpmlSrc == null) {
        System.out.format("%-2s %-4s %-3s %-50.50s %-8s %-8s %-12.12s %-3s %s%n", (s.getExeState() == ExecState.SUCCESS ? OK : NOK), s.getStepId(),
            s.getActivityInfoId(), s.getStepName(), FMT_HHMMSS.format(s.getStartTime()), FMT_HHMMSS.format(s.getEndTime()), s.getExeState(), s.getBasicStatus(),
            s.getAdvStatus());
      } else {
        BPML bpml = bpmlSrc.get(s.getWfdKey());
        if (bpml == null) {
          WorkflowDefinition wfd = WorkflowDefinition.find(s.getWfdName(), s.getWfdVersion());
          bpml = BPML.parse(wfd.getBusinessProcess());
        }
        System.out.format("%-2s %-4s %-8s %-8s %-12.12s %-3s %-50.50s %-30.30s %-3s %s%n", (s.getExeState() == ExecState.SUCCESS ? OK : NOK), s.getStepId(),
            FMT_HHMMSS.format(s.getStartTime()), FMT_HHMMSS.format(s.getEndTime()), s.getExeState(), s.getActivityInfoId(),
            bpml.getActivityDescription(s.getActivityInfoId()), bpml.getActivityLocation(s.getActivityInfoId(), wf.getWfdName()), s.getBasicStatus(),
            s.getAdvStatus());
      }

      if (!getProcessDataForSteps.isEmpty()) {
        checkProcessDataFor(s, wf);
      }
      if (s.getExeState() != ExecState.SUCCESS)
        errorCount++;
    }
    System.out.println(separator('-', width));
    System.out.format("Result: %s [WF_ID: %d, WFD: %s/%d, %d steps executed, %d errors.%n", wf.getExeState(), wf.getWorkFlowId(), wf.getWfdName(),
        wf.getWfdVersion(), wf.getStepsCounted(), errorCount);
  }

  private void checkProcessDataFor(WorkFlowMonitor s, Workflow wf) throws ApiException {
    String spec = null;
    if (getProcessDataForSteps.contains("*") || getProcessDataForSteps.contains(String.valueOf(s.getStepId()))) {
      getProcessDataFor(s);
    } else if (s.getExeState() != ExecState.SUCCESS) {
      spec = getProcessDataForSteps.stream().filter(e -> e.startsWith("!")).map(e -> "!".equals(e) ? "-1" : e.substring(1)).findFirst().orElse(null);
    } else if (s.getStepId() == wf.getLastStepId()) {
      spec = getProcessDataForSteps.stream().filter(e -> e.startsWith("-")).map(e -> "-".equals(e) ? "-1" : e).findFirst().orElse(null);
    }
    if (spec != null) {
      int o = Integer.parseInt(spec);
      for (int i = (o > 0 ? s.getStepId() : s.getStepId() + o + 1); i <= (o > 0 ? Math.max(s.getStepId() + o, wf.getLastStepId()) : s.getStepId()); i++) {
        getProcessDataFor(wf.getWfStep(i));
      }
    }
  }

  private void getProcessDataFor(WorkFlowMonitor s) throws ApiException {
    if (s == null)
      return;
    if (outdir == null) {
      if (s.getProcessData() != null) {
        System.out.format("----- ProcessData step %d: -----%n%s%n----- END ProcessData step %d -----%n", s.getStepId(), s.getProcessData(), s.getStepId());
      }
      if (s.getStatusRpt() != null) {
        System.out.format("----- Status_Report step %d: -----%n%s%n----- END Status_Report step %d -----%n", s.getStepId(), s.getStatusRpt(), s.getStepId());
      }
    } else {
      File outfile = new File(outdir, String.format("ProcessData-%d.xml", s.getStepId()));
      try (OutputStream os = new FileOutputStream(outfile)) {
        os.write(s.getProcessData().getBytes());
      } catch (IOException e) {
        throw new ApiException(e);
      }
      if (s.getStatusRpt() != null) {
        outfile = new File(outdir, String.format("StatusRpt-%d.xml", s.getStepId()));
        try (OutputStream os = new FileOutputStream(outfile)) {
          os.write(s.getStatusRpt().getBytes());
        } catch (IOException e) {
          throw new ApiException(e);
        }
      }
    }
  }

  private void execute(String wfdName, int wfdVersion, File primDocFile, String filename, boolean showDetails) throws ApiException {
    _execute(wfdName, wfdVersion, primDocFile, null, filename, showDetails);
  }

  private void execute(String wfdName, int wfdVersion, byte[] data, String filename, boolean showDetails) throws ApiException {
    _execute(wfdName, wfdVersion, null, data, filename, showDetails);
  }

  private void _execute(String wfdName, int wfdVersion, File primDocFile, byte[] data, String filename, boolean showDetails) throws ApiException {
    LOG.log(Level.FINEST, "Enter execute(): wfdName={0}, wfdVersion={1}, primDocFile={2}, data.length={3}, filename{4}, showDetails={5}",
        new Object[] { wfdName, wfdVersion, primDocFile, (data == null ? "-" : data.length), filename, showDetails });
    WorkflowDefinition wfd = WorkflowDefinition.find(wfdName, wfdVersion);

    if (wfd == null) {
      System.out.format("WFD %s (version: %s) not found!%n", wfdName, (wfdVersion > 0 ? wfdVersion : "default"));
      return;
    }

    System.out.format("Execute %s (version: %s)...", wfdName, (wfdVersion > 0 ? wfdVersion : "default"));
    Workflow wf = primDocFile == null ? wfd.execute(data, filename) : wfd.execute(primDocFile, filename);
    System.out.format(" workflow ID %s:%n%n", wf.getWorkFlowId());
    if (bpmlSrc != null) {
      bpmlSrc.put(wfd.getId(), BPML.parse(wfd.getBusinessProcess()));
      parseWfd(wfd);
    }

    show(wf, showDetails);
  }

  private XmlDiff compareSrc(Object src1, Object src2) throws ApiException {
    XmlDiff diff = new XmlDiff();
    diff.setIgnoreComments(cfg.getBoolean(Props.DIFF_IGNORE_COMMENTS, true));
    diff.setIgnoreWhitespace(cfg.getBoolean(Props.DIFF_IGNORE_WHITESPACE, true));
    diff.compare(getSource(src1), getSource(src2));
    return diff;
  }

  private Object getSource(Object src) throws ApiException {
    if (src instanceof WorkflowDefinition) {
      return ((WorkflowDefinition) src).getBusinessProcess();
    } else if (src instanceof XSLTDefinition) {
      return ((XSLTDefinition) src).getXsltData();
    } else {
      return src;
    }
  }

  private void compare(String name, File infile) throws ApiException, IOException {
    ApiClient      remote = getRemoteFor(infile, name);
    ApiClient      local;
    Resources.TYPE type   = (remote != null && remote instanceof XSLTDefinition ? TYPE.XSLT : TYPE.WFD);

    if (remote == null) {
      System.out.format("%s %s not found on server.%n", type, (name == null ? infile.getName() : name));
    } else {
      if (type == TYPE.XSLT) {
        name = ((XSLTDefinition) remote).getName();
        local = new XSLTDefinition(name, infile, "compare");
      } else {
        name = ((WorkflowDefinition) remote).getName();
        local = new WorkflowDefinition(infile, "compare");
      }
      XmlDiff diff          = compareSrc(local, remote);
      int     remoteVersion = (type == TYPE.XSLT ? ((XSLTDefinition) remote).getVersion() : ((WorkflowDefinition) remote).getWfdVersion());
      if (diff.differs()) {
        System.out.format("%s %s modifified (server version %d): %d comparison(s) failed.%n", type, name, remoteVersion, diff.getCount());
        if (cfg.getBoolean(Props.PROP_VERBOSE)) {
          System.out.println();
          for (Difference d : diff.get().getDifferences()) {
            System.out.format("> %s%n", d.toString());
          }
        }
        if (cfg.getBoolean(Props.PROP_EXTRACT)) {
          writeToFile((type == TYPE.XSLT ? ((XSLTDefinition) remote).getXsltData() : ((WorkflowDefinition) remote).getBusinessProcess()), infile);
        }
      } else {
        System.out.format("%s %s unchanged (server version %d)%n", type, name, remoteVersion);
      }
    }
  }

  private void export(File exportFile) throws ApiException {
    Resources res = new Resources();
    res.setIncludePattern(cfg.getString(Props.PROP_INCLUDE));
    res.setExcludePattern(cfg.getString(Props.PROP_EXCLUDE));
    if (res.export()) {
      try (Writer wr = new FileWriter(exportFile)) {
        wr.write(res.getData());
        wr.flush();
        System.out.format("Wrote export file %s with %d artifacts.%n", exportFile.getAbsoluteFile(), res.getArtifactCount());
      } catch (IOException e) {
        throw new ApiException(e);
      }
    }
  }

  private void sync(File exportFile, String rootDir) throws ApiException, IOException {
    ResourceSync sync = new ResourceSync(exportFile);
    if (cfg.hasProperty(Props.PROP_IGNORE_PATHS))
      sync.setIgnoreFilenames(Pattern.compile(cfg.getString(Props.PROP_IGNORE_PATHS)));
    Map<String, Result> syncresult = sync.sync(rootDir);
    List<String>        keys       = new ArrayList<>(syncresult.size());
    keys.addAll(syncresult.keySet());
    Collections.sort(keys);

    System.out.println();
    System.out.format("%-4s %-4s %-12s %-36s %s%n", "No.", "Type", "Status", "Name", "Path(s)");
    System.out.println("---------------------------------------------------------------------------------------------------------------------------");
    int cnt = 0;
    for (String k : keys) {
      Result r = syncresult.get(k);
      System.out.format("%-4d %-4s %-12s %-36s %s%n", ++cnt, r.getArtifactType(), r.getResult().name(), r.getArtifactName(), r.getPaths());
      if (cfg.getBoolean(Props.PROP_EXTRACT)
          && (cfg.getBoolean(Props.PROP_FORCE) || r.getResult() == SyncResult.MODIFIED || r.getResult() == SyncResult.REMOTE_ONLY)) {
        File outfile = r.getPaths().isEmpty() ? getOutfileFor(sync.getArtifact(r.getArtifactType(), r.getArtifactName()))
            : r.getPaths().iterator().next().toFile();
        writeToFile(sync.getArtifact(r.getArtifactType(), r.getArtifactName()).getStringData(), outfile);
      }
    }
  }

  private File getOutfileFor(SIArtifact artifact) throws IOException {
    String ext;
    switch (artifact.getType()) {
    case WFD:
      ext = "bpml";
      break;
    case XSLT:
      ext = "xslt";
      break;
    default:
      ext = artifact.getType().name().toLowerCase();
      break;
    }
    Matcher m = Pattern.compile(".*" + artifact.getType().name() + "=([^=:]+).*").matcher(cfg.getString(Props.PROP_EXTRACT_PATHS, ""));
    if (m.matches()) {
      return new File(m.group(1), artifact.getName() + "." + ext);
    } else {
      return new File(artifact.getName() + "." + ext);
    }
  }

  private void writeToFile(String content, File outfile) throws IOException {
    if (outfile.exists() && !askYN("File " + outfile + " exists. Overwrite ?", false)) {
      return;
    }
    if (!outfile.getParentFile().isDirectory()) {
      outfile.getParentFile().mkdirs();
    }
    try (Writer wr = new FileWriter(outfile, false)) {
      wr.write(content);
      System.out.println("Wrote " + outfile);
    }
  }

  private String getName(String name, File infile) {
    if (name == null) {
      name = infile.getName();
    }
    return name.replaceAll("(?i)\\.(xslt|xsl|bpml)$", "");
  }

  private Resources.TYPE getType(String filename) {
    if (cfg.getBoolean(Props.PROP_XSLT) || filename.toLowerCase().endsWith(".xslt") || filename.toLowerCase().endsWith(".xsl")) {
      return TYPE.XSLT;
    }
    return TYPE.WFD;
  }

  private ApiClient getRemoteFor(Object src, String name) throws ApiException {
    Resources.TYPE type;

    if (src instanceof XSLTDefinition) {
      type = TYPE.XSLT;
      name = ((XSLTDefinition) src).getName();
    } else if (src instanceof WorkflowDefinition) {
      type = TYPE.WFD;
      name = ((WorkflowDefinition) src).getName();
    } else if (src instanceof File) {
      File f = (File) src;
      type = getType(f.getName());
      name = getName(name, f);
    } else {
      if (name == null)
        name = src.toString();
      type = getType(name);
    }

    if (type == TYPE.XSLT) {
      return XSLTDefinition.find(name);
    } else {
      return WorkflowDefinition.find(name);
    }
  }
}
