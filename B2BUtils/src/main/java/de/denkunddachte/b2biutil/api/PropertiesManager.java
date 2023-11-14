package de.denkunddachte.b2biutil.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.denkunddachte.b2biutil.AbstractConsoleApp;
import de.denkunddachte.enums.ExecState;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.sfgapi.ApiClient;
import de.denkunddachte.sfgapi.Property;
import de.denkunddachte.sfgapi.PropertyFiles;
import de.denkunddachte.sfgapi.PropertyNodeValue;
import de.denkunddachte.sfgapi.Workflow;
import de.denkunddachte.sfgapi.WorkflowDefinition;
import de.denkunddachte.utils.CommandLineParser.CommandLineException;
import de.denkunddachte.utils.CommandLineParser.ParsedCommandLine;
import de.denkunddachte.utils.Config;
import de.denkunddachte.utils.StringUtils;
import de.denkunddachte.utils.WordUtil;

public class PropertiesManager extends AbstractConsoleApp {
  private static final String PREFIX_DESCRIPTION = "prefixDescription";
  // private static final Logger LOG = Logger.getLogger(PropertiesManager.class.getName());

  static {
    OPTIONS.setProgramName(PropertiesManager.class.getName());
    OPTIONS.setHasOptionalArgs(true);
    OPTIONS.setProgramDescription("Manage B2Bi properties and extensions.");

    // Common options
    OPTIONS.section("General options");
    OPTIONS.add(Props.PROP_PREFIX + "|P=s", "Properties prefix");
    OPTIONS.add(Props.PROP_CUST_OVERRIDES + "|O", "Manage customer_overrides properties");

    // List
    OPTIONS.section("List/get properties");
    OPTIONS.add(Props.PROP_LIST_FILES + "|l", "List property files in DB");
    OPTIONS.add(Props.PROP_LIST + "|L:s", "List properties (optional: glob pattern)");
    OPTIONS.add(Props.PROP_EXPORT + "|E:s", "Export properties to file");
    // Import
    OPTIONS.add(Props.PROP_IMPORT + "|I=s", "Import properties from file");
    OPTIONS.add(Props.PROP_REPLACE, "Replace with contents of file");

    // Get, set, create
    OPTIONS.add(Props.PROP_CREATE + "|c=s", "Create new property prefix");
    OPTIONS.add(Props.PROP_DESCRIPTION + "=s", "Description for new property prefix", PREFIX_DESCRIPTION);
    OPTIONS.add(Props.PROP_GET + "|g=s", "Get property");
    OPTIONS.add(Props.PROP_SET + "|s=s@", "Set property (specify key=value pairs");
    OPTIONS.add(Props.PROP_NODE + "|n=i", "Get/set/delete property node value (node 1..n)");

    // Delete
    OPTIONS.add(Props.PROP_DELETE + "|d=s", "Delete properties matching pattern");
    OPTIONS.add(Props.PROP_DELETE_PREFIX + "=s", "Delete properties file/prefix");

    OPTIONS.add(Props.PROP_REFRESH_PROPERTIES + "|R", "Refresh properties");

    OPTIONS.addProgramHelp("Examples:");
    OPTIONS.addProgramHelp("  PropertiesManager -l");
    OPTIONS.addProgramHelp("  PropertiesManager -c myprops --description \"My application props\"");
    OPTIONS.addProgramHelp("  PropertiesManager -P myprops -s \"prop1=some value\" -s \"prop2=some other value\"");
    OPTIONS.addProgramHelp("  PropertiesManager -P myprops -s \"prop1=some value for node 1\" -n 1");
    OPTIONS.addProgramHelp("  PropertiesManager -P myprops -L <globPattern>");
    OPTIONS.addProgramHelp("  PropertiesManager -I test.properties");
    OPTIONS.addProgramHelp("  PropertiesManager -P myprops -d deleteMe*");
    OPTIONS.addProgramHelp("  PropertiesManager --deletePrefix myprops");
  }

  public PropertiesManager(String[] args) throws CommandLineException, ApiException {
    super(args);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void init(ParsedCommandLine cmdline) throws CommandLineException {
    if (cfg.hasProperty(Props.PROP_CUST_OVERRIDES)) {
      cfg.setProperty(Props.PROP_PREFIX, "customer_overrides");
    }
    if (cfg.hasProperty(Props.PROP_LIST_FILES)) {
    } else if (cfg.hasProperty(Props.PROP_LIST) || cfg.hasProperty(Props.PROP_EXPORT) || cfg.hasProperty(Props.PROP_GET)
        || cfg.hasProperty(Props.PROP_DELETE)) {
      if (!cfg.hasProperty(Props.PROP_PREFIX)) {
        throw new CommandLineException("Specifiy property file!");
      }
      if (cfg.hasProperty(Props.PROP_EXPORT) && cfg.getString(Props.PROP_EXPORT).isEmpty()) {
        cfg.setProperty(Props.PROP_EXPORT, cfg.getString(Props.PROP_PREFIX) + ".properties");
      }
    } else if (cfg.hasProperty(Props.PROP_IMPORT)) {
      File f = new File(cfg.getString(Props.PROP_IMPORT));
      if (!f.canRead()) {
        throw new CommandLineException("Input file " + f.getAbsolutePath() + " not readable!");
      }
      if (!cfg.hasProperty(Props.PROP_PREFIX)) {
        cfg.setProperty(Props.PROP_PREFIX, f.getName().replace(".properties", ""));
      }
    } else if (cfg.getObject(Props.PROP_SET) instanceof List<?>) {
      if (!cfg.hasProperty(Props.PROP_PREFIX)) {
        throw new CommandLineException("Specifiy property file!");
      }
      for (String v : ((List<String>) cfg.getObject(Props.PROP_SET))) {
        if (v == null || v.indexOf('=') < 1) {
          throw new CommandLineException("Invalid argument for --set: " + v + "! Specify properties in key=value format.");
        }
      }
    } else if (cfg.hasProperty(Props.PROP_REFRESH_PROPERTIES) || cfg.hasProperty(Props.PROP_DELETE_PREFIX) || cfg.hasProperty(Props.PROP_CREATE)) {
    } else {
      throw new CommandLineException("No operation specified!");
    }
  }

  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    int rc = 1;
    try (PropertiesManager api = new PropertiesManager(args)) {
      Config cfg = Config.getConfig();
      if (cfg.hasProperty(Props.PROP_LIST_FILES)) {
        api.listFiles();
      } else if (cfg.hasProperty(Props.PROP_LIST) || cfg.hasProperty(Props.PROP_EXPORT)) {
        api.listProperties(cfg.getString(Props.PROP_PREFIX), cfg.getProperty(Props.PROP_LIST));
      } else if (cfg.hasProperty(Props.PROP_GET)) {
        api.getProperty(cfg.getString(Props.PROP_PREFIX), cfg.getString(Props.PROP_GET), cfg.getInt(Props.PROP_NODE, 0));
      } else if (cfg.hasProperty(Props.PROP_DELETE)) {
        api.deleteProperties(cfg.getString(Props.PROP_PREFIX), cfg.getString(Props.PROP_DELETE), cfg.getInt(Props.PROP_NODE, 0));
      } else if (cfg.hasProperty(Props.PROP_DELETE_PREFIX)) {
        api.deletePrefix(cfg.getString(Props.PROP_DELETE_PREFIX));
      } else if (cfg.hasProperty(Props.PROP_SET)) {
        for (String v : ((List<String>) cfg.getObject(Props.PROP_SET))) {
          api.setProperty(cfg.getString(Props.PROP_PREFIX), v.substring(0, v.indexOf('=')), cfg.getInt(Props.PROP_NODE, 0), v.substring(v.indexOf('=') + 1));
        }
      } else if (cfg.hasProperty(Props.PROP_IMPORT)) {
        api.importFile(cfg.getString(Props.PROP_PREFIX), new File(cfg.getString(Props.PROP_IMPORT)), cfg.getProperty(PREFIX_DESCRIPTION),
            cfg.getBoolean(Props.PROP_REPLACE));
      } else if (cfg.hasProperty(Props.PROP_CREATE)) {
        api.createPrefix(cfg.getString(Props.PROP_CREATE), cfg.getProperty(PREFIX_DESCRIPTION));
      }

      if (api.getRc() == 0 && cfg.hasProperty(Props.PROP_REFRESH_PROPERTIES)) {
        api.refresh(cfg.getProperty(Props.PROP_PREFIX));
      }
      rc = api.getRc();
    } catch (CommandLineException e) {
      System.exit(3);
    } catch (ApiException e) {
      e.printStackTrace(System.err);
      System.exit(2);
    }
    System.exit(rc);
  }

  // START list files
  private void listFiles() throws ApiException {
    int i = 0;
    for (PropertyFiles f : PropertyFiles.findAll()) {
      if (i++ == 0) {
        System.out.format("%-2s %-30.30s %-2s %-5s %-2s %-19s %-12s %-19s %-12s%n", "ID", "Property file prefix", "Sys", "Props", "Ed", "Created", "By",
            "Modified", "By");
        System.out.println(separator('-', 120));
      } ;
      System.out.format("%2s %-30.30s %-3s %-5s %-2s %-19s %-12s %-19s %-12s%n", f.getId(), f.getPropertyFilePrefix(), (f.isSystemDefined() ? "*" : ""),
          f.getProperties().size(), (f.isComponentEditable() ? OK : ""), (f.getCreatedOn() == null ? "" : FMT_ISO_DATETIME.format(f.getCreatedOn())),
          (f.getCreatedBy() == null ? "" : f.getCreatedBy()), (f.getLastUpdatedOn() == null ? "" : FMT_ISO_DATETIME.format(f.getLastUpdatedOn())),
          (f.getLastUpdatedBy() == null ? "" : f.getLastUpdatedBy()));
    }
    System.out.println("Total IDs: " + i);
  }
  // END list partner

  // START list properties
  private void listProperties(String prefix, String globPattern) throws ApiException {
    PropertyFiles pf = PropertyFiles.find(prefix);
    if (pf == null) {
      System.err.format("No such property prefix: %s%n", prefix);
      return;
    }
    int     i       = 0;
    Pattern pattern = null;
    if (globPattern != null) {
      pattern = Pattern.compile(StringUtils.globToRegexp(globPattern));
    }
    for (Property p : pf.getProperties().values()) {
      if (!propertyMatch(pattern, p)) {
        continue;
      }
      if (i++ == 0) {
        System.out.format("%-4s %s %-40.40s %-70.70s %-19s %-12s %-19s %-12s%n", "ID", "S", "Key/node", "Value(s)", "Created", "By", "Modified", "By");
        System.out.println(separator('-', 190));
      }
      String v[] = null;
      if (p.getPropertyValue() == null) {
        v = new String[] { null };
      } else {
        v = WordUtil.wrap(p.getPropertyValue(), 70, null, true).split(System.lineSeparator());
      }
      for (int j = 0; j < v.length; j++) {
        if (j == 0) {
          System.out.format("%-4s %s %-40.40s %-70.70s %-19s %-12s %-19s %-12s%n", p.getId(), (p.isSystemDefined() ? "*" : " "), p.getPropertyKey(), v[j],
              (p.getCreatedOn() == null ? "" : FMT_ISO_DATETIME.format(p.getCreatedOn())), (p.getCreatedBy() == null ? "" : p.getCreatedBy()),
              (p.getLastUpdatedOn() == null ? "" : FMT_ISO_DATETIME.format(p.getLastUpdatedOn())), (p.getLastUpdatedBy() == null ? "" : p.getLastUpdatedBy()));
        } else {
          System.out.format("%47s %s%n", " ", v[j]);
        }
      }
      for (PropertyNodeValue pnv : p.getNodeValues()) {
        if (pnv.getPropertyValue() == null) {
          v = new String[] { null };
        } else {
          v = WordUtil.wrap(pnv.getPropertyValue(), 70, null, true).split(System.lineSeparator());
        }
        for (int j = 0; j < v.length; j++) {
          if (j == 0) {
            System.out.format("%6s %40.40s %-70.70s%n", " ", "[" + pnv.getNodeName() + "]", v[j]);
          } else {
            System.out.format("%47s %s%n", " ", v[j]);
          }
        }
      }
      exportArtifact(p);
    }
    System.out.println("Properties: " + i);
  }

  private boolean propertyMatch(Pattern pattern, Property p) throws ApiException {
    if (pattern == null) {
      return true;
    }
    // property matches if key or value matches
    if ((p.getPropertyKey() != null && pattern.matcher(p.getPropertyKey()).matches())
        || (p.getPropertyValue() != null && pattern.matcher(p.getPropertyValue()).matches())) {
      return true;
    }
    
    for (PropertyNodeValue pnv : p.getNodeValues()) {
      if (pnv.getPropertyValue() != null && pattern.matcher(pnv.getPropertyValue()).matches()) {
        return true;
      }
    }
    return false;
  }

  // END list properties

  private void getProperty(String prefix, String key, int node) throws ApiException {
    Property p = Property.find(prefix, key);
    if (p == null || (node > 0 && p.getNodeValue(node) == null)) {
      System.err.format("No such property [%s]%s%s!%n", prefix, key, (node > 0 ? " (node" + node + ")" : ""));
    } else {
      if (node > 0) {
        System.out.println(p.getNodeValue(node));
      } else {
        System.out.println(p.getPropertyValue());
      }
    }
  }

  private void setProperty(String prefix, String key, int node, String value) throws ApiException {
    Property p = Property.find(prefix, key);

    if (p == null) {
      PropertyFiles pf = PropertyFiles.find(prefix);
      if (pf == null) {
        throw new ApiException("No property file with prefix " + prefix + " found!");
      }
      p = pf.addProperty(key, (node == 0 ? value : null));
      if (node > 0) {
        p.setNodeValue(node, value);
        if (!p.update()) {
          throw new ApiException("Failed to create node value [" + prefix + "]" + key + " (node " + node + "): " + ApiClient.getApiErrorMsg());
        }
      }
      System.out.format("Created property [%s]%s=%s%s%n", prefix, key, value, (node > 0 ? " (node" + node + ")" : ""));
    } else {
      if (node > 0) {
        p.setNodeValue(node, value);
      } else {
        p.setPropertyValue(value);
      }
      if (p.update()) {
        System.out.format("Updated property [%s]%s=%s%s%n", prefix, key, value, (node > 0 ? " (node" + node + ")" : ""));
      } else {
        throw new ApiException("Failed to set property [" + prefix + "]" + key + (node > 0 ? " (node " + node + ")" : "") + ": " + ApiClient.getApiErrorMsg());
      }
    }
  }

  private void importFile(String prefix, File file, String description, boolean clearBefore) throws ApiException {
    PropertyFiles pf = getPropertyFile(prefix, (description == null ? "Imported from " + file.getName() : description), true);
    if (pf == null) {
      System.err.format("Failed to import prefix %s!%n", prefix);
      return;
    }
    Properties props = new Properties();
    try (InputStream is = new FileInputStream(file)) {
      props.load(is);
    } catch (IOException e) {
      throw new ApiException(e);
    }
    System.out.format("Found %d properties in file %s.%n", props.size(), file);
    if (props.isEmpty()) {
      System.err.println("Properties file is empty!");
      return;
    }

    if (clearBefore) {
      System.out.format("Delete existing properties with prefix %s.%n", prefix);
      deleteProperties(prefix, "*", 0);
    }
    final List<String> keys = new ArrayList<>(props.size());
    for (Object k : props.keySet()) {
      keys.add((String) k);
    }
    Collections.sort(keys);
    Pattern p = Pattern.compile("(\\S+)\\[node(\\d+)\\]");
    for (String key : keys) {
      Matcher m = p.matcher(key);
      if (m.matches()) {
        setProperty(prefix, m.group(1), Integer.parseInt(m.group(2)), props.getProperty(key));
      } else {
        setProperty(prefix, key, 0, props.getProperty(key));
      }
    }
  }

  private PropertyFiles getPropertyFile(String prefix, String description, boolean create) throws ApiException {
    PropertyFiles pf = PropertyFiles.find(prefix);
    if (pf != null) {
      return pf;
    }
    if (create) {
      pf = new PropertyFiles(prefix);
      pf.setDescription(description);
      if (pf.create()) {
        pf = PropertyFiles.find(prefix);
        System.out.format("Created properties prefix %s.%n", prefix);
      } else {
        System.err.format("Could not create properties prefix %s: %s%n", prefix, ApiClient.getApiErrorMsg());
        pf = null;
      }
    }
    return pf;
  }

  private void createPrefix(String prefix, String description) throws ApiException {
    PropertyFiles pf = PropertyFiles.find(prefix);
    if (pf != null) {
      System.err.format("Prefix %s exists!%n", prefix);
      return;
    }
    getPropertyFile(prefix, description, true);
  }

  private void deletePrefix(String prefix) throws ApiException {
    PropertyFiles pf = PropertyFiles.find(prefix);
    if (pf == null) {
      System.err.format("No such property prefix: %s%n", prefix);
      return;
    }
    if (pf.delete()) {
      System.out.format("Deleted properties prefix %s.%n", prefix);
    } else {
      System.err.format("Could not delete properties prefix %s: %s%n", prefix, ApiClient.getApiErrorMsg());
    }
  }

  private void deleteProperties(String prefix, String globPattern, int node) throws ApiException {
    PropertyFiles pf = PropertyFiles.find(prefix);
    if (pf == null) {
      System.err.format("No such property prefix: %s%n", prefix);
      return;
    }
    int     deleted = 0;
    int     failed  = 0;
    Pattern pattern = null;
    if (globPattern != null) {
      pattern = Pattern.compile(StringUtils.globToRegexp(globPattern));
    }
    for (Property p : pf.getProperties().values()) {
      if (pattern != null && !pattern.matcher(p.getPropertyKey()).matches() && !pattern.matcher(p.getPropertyValue()).matches()) {
        continue;
      }
      if (node == 0) {
        if (p.delete()) {
          System.out.format("Deleted property [%s]%s=%s%n", prefix, p.getPropertyKey(), p.getPropertyValue());
          deleted++;
        } else {
          System.err.format("Failed to deleted property [%s]%s=%s: %s%n", prefix, p.getPropertyKey(), p.getPropertyValue(), ApiClient.getApiErrorMsg());
          failed++;
        }
      } else {
        if (p.hasNodeValue(node)) {
          if (p.delete()) {
            System.out.format("Deleted property node value [%s]%s=%s (node%s)%n", prefix, p.getPropertyKey(), p.getNodeValue(node), node);
            deleted++;
          } else {
            System.err.format("Failed to deleted property node value [%s]%s=%s (node%s): %s%n", prefix, p.getPropertyKey(), p.getNodeValue(node), node,
                ApiClient.getApiErrorMsg());
            failed++;
          }
        }
      }
    }
    System.out.format("Deleted %d properties, %d deletions failed.", deleted, failed);
  }

  private void refresh(String prefix) throws ApiException {
    if (PropertyFiles.canRefresh()) {
      List<String> props = PropertyFiles.refreshCache(prefix);
      if (props.isEmpty()) {
        System.err.println("No matching properties refreshed on server!");
        rc = 1;
      } else {
        System.out.format("Refreshed properties: %s%n", props);
      }
    } else {
      final String       bpname = cfg.getString(Props.PROP_REFRESH_PROPERTIES_BP, "DD_REFRESH_PROPERTIES");
      WorkflowDefinition wfd    = WorkflowDefinition.find(bpname);
      if (wfd != null) {
        Workflow wf = wfd.execute();
        if (wf.getExeState() == ExecState.SUCCESS) {
          System.out.format("Refresh properties (%s) executed successfully (WF_ID %s, %d steps).", bpname, wf.getWorkFlowId(), wf.getNumberOfSteps());
        } else {
          System.err.format("Refresh properties (%s) failed with state %s (WF_ID %s).", bpname, wf.getExeState(), wf.getWorkFlowId());
        }
      } else {
        System.err.format("Refresh properties not available. Install %s or WFD %s.", "DD_API_WS", bpname);
      }
    }
  }
}
