package de.denkunddachte.b2biutil.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRow.Tag;
import com.github.difflib.text.DiffRowGenerator;

import de.denkunddachte.b2biutil.AbstractConsoleApp;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.sfgapi.ApiClient;
import de.denkunddachte.sfgapi.Property;
import de.denkunddachte.sfgapi.PropertyFiles;
import de.denkunddachte.utils.CommandLineParser.CommandLineException;
import de.denkunddachte.utils.CommandLineParser.ParsedCommandLine;
import de.denkunddachte.utils.Config;

// -f "C:/workspace/B2BiUtils/JavaTaskHelper/src/main/java/de/denkunddachte/b2biutils/JavaTask.java" -g QueryJSONArray
public class JavaTaskHelper extends AbstractConsoleApp {

  static {
    OPTIONS.setProgramName(JavaTaskHelper.class.getName());
    OPTIONS.setHasOptionalArgs(true);
    OPTIONS.setProgramDescription("Helper for JavaTask source maintained in B2Bi custom properties.");

    OPTIONS.add(Props.PROP_SRCFILE + "|f=s", "JavaTask source file", true);
    OPTIONS.add(Props.PROP_PREFIX + "|P=s", "Properties prefix", Props.PROP_PREFIX, System.getenv("JAVASRC_PREFIX"));
    OPTIONS.add(Props.PROP_METHOD_PREFIX + "=s", "Method prefix", Props.PROP_METHOD_PREFIX, "test");
    OPTIONS.add(Props.PROP_LIST + "|L", "List properties");
    OPTIONS.add(Props.PROP_GET + "|g=s", "Get JavaTask source from property and creaet/update method");
    OPTIONS.add(Props.PROP_SET + "|s=s", "Set JavaTask source property");
    OPTIONS.add(Props.PROP_COMPARE + "|c=s", "Compare JavaTask source property");
    OPTIONS.add(Props.PROP_DELETE + "|d=s", "Delete JavaTask source property");
    OPTIONS.add("lf", "Force LF as line delimiter.");

    OPTIONS.addProgramHelp("Note: Property names are case sensitive, method names are not!");
    OPTIONS.addProgramHelp("");
    OPTIONS.addProgramHelp("Examples:");
    OPTIONS.addProgramHelp("  JavaTaskHelper -f /path/to/JavaTask.java -P javasrc -L");
    OPTIONS.addProgramHelp("  JavaTaskHelper -f /path/to/JavaTask.java -P javasrc -g testTask");
    OPTIONS.addProgramHelp("  JavaTaskHelper -f /path/to/JavaTask.java -P javasrc -s testTask");
    OPTIONS.addProgramHelp("  JavaTaskHelper -f /path/to/JavaTask.java -P javasrc -c testTask");
    OPTIONS.addProgramHelp("  JavaTaskHelper -f /path/to/JavaTask.java -P javasrc -d testTask");
  }

  private File     srcFile;
  private String   methodPrefix;
  private String   methodName;
  private Pattern  methodSignature;
  private Pattern  methodEnd;
  private int      colWidth;
  private String[] diffHdr = new String[] { null, null };

  public JavaTaskHelper(String[] args) throws CommandLineException, ApiException {
    super(args);
  }

  @Override
  protected void init(ParsedCommandLine cmdline) throws CommandLineException {
    srcFile = new File(cfg.getString(Props.PROP_SRCFILE));
    if (!srcFile.canRead()) {
      throw new CommandLineException("Cannot read source file " + srcFile.getAbsolutePath());
    }

    if (cfg.getString(Props.PROP_PREFIX).isEmpty()) {
      throw new CommandLineException("Property prefix not set!");
    }
    methodPrefix = cfg.getString(Props.PROP_METHOD_PREFIX);

    int columns = 200;
    if (System.getenv("COLUMNS") != null) {
      columns = Integer.parseInt(System.getenv("COLUMNS"));
    }
    colWidth = (columns - 6) / 2;

    methodName = cfg.getString(Props.PROP_GET, cfg.getString(Props.PROP_SET, cfg.getString(Props.PROP_DELETE, cfg.getString(Props.PROP_COMPARE))));
    if (methodName.startsWith(methodPrefix))
      methodName = methodName.substring(methodPrefix.length());
    if (!cfg.hasProperty(Props.PROP_LIST) && methodName.isEmpty())
      throw new CommandLineException("Method/property name is empty!");
    methodSignature = Pattern.compile("(\\s+)public static String ((?:" + methodPrefix + ")?" + methodName + ")\\(String\\.\\.\\. args\\).*",
        Pattern.CASE_INSENSITIVE);
    methodEnd = Pattern.compile("\\s+return \".+?\";\\s*");
    if (cfg.hasProperty("lf"))
      LF = "\n";
  }

  public static void main(String[] args) {
    int rc = 1;
    try (JavaTaskHelper api = new JavaTaskHelper(args)) {
      Config cfg = Config.getConfig();
      if (cfg.hasProperty(Props.PROP_LIST)) {
        api.listMethods(cfg.getString(Props.PROP_PREFIX));
      } else if (cfg.hasProperty(Props.PROP_COMPARE)) {
        api.compare(cfg.getString(Props.PROP_PREFIX));
      } else if (cfg.hasProperty(Props.PROP_GET)) {
        api.getSource(cfg.getString(Props.PROP_PREFIX));
      } else if (cfg.hasProperty(Props.PROP_DELETE)) {
        api.deleteSource(cfg.getString(Props.PROP_PREFIX));
      } else if (cfg.hasProperty(Props.PROP_SET)) {
        api.setSource(cfg.getString(Props.PROP_PREFIX));
      }
      if (api.getRc() == 0 && (cfg.hasProperty(Props.PROP_SET) || cfg.hasProperty(Props.PROP_DELETE))) {
        api.refresh(cfg.getProperty(Props.PROP_PREFIX));
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

  private void listMethods(String prefix) throws ApiException {
    PropertyFiles pf = PropertyFiles.find(prefix);
    if (pf == null) {
      System.err.format("No such property prefix: %s%n", prefix);
      return;
    }
    int i = 0;
    for (Property p : pf.getProperties().values()) {
      if (i++ == 0) {
        System.out.format("%-4s %-40.40s %-10s %-19s %-12s %-19s %-12s%n", "ID", "Method/Taskname", "Size", "Created", "By", "Modified", "By");
        System.out.println(separator('-', 120));
      }
      long size = (p.getPropertyValue() == null ? -1 : p.getPropertyValue().length());
      System.out.format("%-4s %-40.40s %-10s %-19s %-12s %-19s %-12s%n", p.getId(), p.getPropertyKey(), size,
          (p.getCreatedOn() == null ? "" : FMT_ISO_DATETIME.format(p.getCreatedOn())), (p.getCreatedBy() == null ? "" : p.getCreatedBy()),
          (p.getLastUpdatedOn() == null ? "" : FMT_ISO_DATETIME.format(p.getLastUpdatedOn())), (p.getLastUpdatedBy() == null ? "" : p.getLastUpdatedBy()));
    }
    System.out.println("Properties: " + i);
  }

  private void compare(String prefix) throws ApiException {
    String   localSrc  = null;
    String   remoteSrc = null;
    Property p         = Property.find(prefix, methodName);
    if (p != null) {
      remoteSrc = p.getPropertyValue();
      diffHdr[1] = "Property: " + prefix + "." + p.getPropertyKey();
    }

    StringBuffer sb = getMethodSource();
    if (sb != null) {
      extractHeader(sb, "");
      localSrc = sb.toString();
    }

    if (localSrc == null) {
      if (remoteSrc == null) {
        System.err.format("No source or property for method %s!%n", methodName);
      } else {
        System.out.format("No local source for method %s in %s.%n", methodName, srcFile);
      }
    } else if (remoteSrc == null) {
      System.out.format("No property for method %s.%s.%n", prefix, methodName);
    } else if (diff(localSrc, remoteSrc, true, false, -1)) {
      diff(localSrc, remoteSrc, true, true, -1);
      System.out.format("%nSources for %s differ!", methodName);
      setRc(1);
    } else {
      System.out.format("%nSources for %s unchanged!", methodName);
    }
  }

  private boolean getSource(String prefix) throws ApiException {
    Property p = Property.find(prefix, methodName);
    if (p == null) {
      System.err.format("No such property [%s]%s!%n", prefix, methodName);
      setRc(1);
      return false;
    }
    diffHdr[1] = "Property: " + prefix + "." + p.getPropertyKey();
    StringBuffer methodSrc = getMethodSource();
    String       hdr       = "";
    if (methodSrc != null) {
      hdr = extractHeader(methodSrc, "");
    }

    if (methodSrc != null && !diff(methodSrc.toString(), p.getPropertyValue(), true, false, 0)) {
      System.out.println("Source for method " + methodName + " unchanged.");
      setRc(1);
      return false;
    }

    int  mark    = 1; // 1=look for insert/replace pos, 3=found method (skip write), 2=found return (skip until method close), 0=method written
    File tmpFile = new File(srcFile.getParentFile(), "JavaTaskTmp.java");
    try (BufferedWriter wr = new BufferedWriter(new FileWriter(tmpFile))) {
      List<String> src    = Files.readAllLines(Paths.get(srcFile.toURI()));
      String       indent = "  ";
      for (String line : src) {
        switch (mark) {
        case 1:
          Matcher m = null;
          if (line.contains("public static void main(") || (m = methodSignature.matcher(line)).matches()) {
            if (m != null) {
              indent = m.group(1);
              methodName = m.group(2);
              mark = 3;
              continue;
            } else {
              methodName = methodPrefix + p.getPropertyKey();
              writeSrc(wr, methodName, hdr + p.getPropertyValue(), indent);
              mark = 0;
            }
          }
          break;
        case 2:
          if (line.matches("\\s+\\}\\s*")) {
            // found close of existing method: write
            writeSrc(wr, methodName, hdr + p.getPropertyValue(), indent);
            mark = 0;
          }
          continue;
        case 3:
          if (methodEnd.matcher(line).matches()) {
            mark = 2;
          }
          continue;
        default:
          break;
        }
        wr.write(line);
        wr.write(LF);
      }
    } catch (IOException e) {
      throw new ApiException(e);
    }
    if (mark != 0) {
      tmpFile.delete();
      throw new ApiException("Failed to write source method to file " + srcFile + " [" + mark + "]!");
    } else {
      srcFile.delete();
      tmpFile.renameTo(srcFile);
    }
    return true;
  }

  private void writeSrc(BufferedWriter wr, String methodName, String srcBody, String indent) throws IOException {
    wr.write(indent);
    wr.write("public static String ");
    wr.write(methodName);
    wr.write("(String... args) throws Exception {");
    wr.write(LF);
    for (String line : srcBody.split("\\r?\\n")) {
      wr.write(indent);
      wr.write(indent);
      if (line.startsWith("import "))
        wr.write("// ");
      wr.write(line);
      wr.write(LF);
    }
    wr.write(indent);
    wr.write("}");
    wr.write(LF);
  }

  private void setSource(String prefix) throws ApiException {
    StringBuffer methodSrc = getMethodSource();
    if (methodSrc == null) {
      throw new ApiException("No source method found for " + methodName + " (methodPrefix=" + methodPrefix + ")!");
    }

    extractHeader(methodSrc, "");
    String   src = methodSrc.toString();

    Property p   = Property.find(prefix, methodName);
    if (p == null) {
      PropertyFiles pf = PropertyFiles.find(prefix);
      if (pf == null) {
        throw new ApiException("No property file with prefix " + prefix + " found!");
      }
      pf.addProperty(methodName, src);
      System.out.format("Created source property [%s]%s%n", prefix, methodName);
    } else {
      diffHdr[1] = diffHdr[0];
      diffHdr[0] = "Property: " + prefix + "." + p.getPropertyKey();

      if (!diff(p.getPropertyValue(), src, true, true, 2)) {
        System.out.println("Source for method " + methodName + " unchanged.");
        setRc(1);
        return;
      }
      if (askYN("Update source property " + p.getPropertyKey() + "?", false)) {
        p.setPropertyValue(src);
        if (p.update()) {
          System.out.format("Updated source [%s]%s%n", prefix, methodName);
        } else {
          throw new ApiException("Failed to set property [" + prefix + "]" + methodName + ": " + ApiClient.getApiErrorMsg());
        }
      } else {
        System.out.println("Skipped.");
        setRc(1);
      }
    }
  }

  private StringBuffer getMethodSource() throws ApiException {
    StringBuffer sb     = null;
    String       indent = "";

    try (BufferedReader rd = new BufferedReader(new FileReader(srcFile))) {
      String line;
      int    c = 0;
      while ((line = rd.readLine()) != null) {
        c++;
        if (sb == null) {
          Matcher m = methodSignature.matcher(line);
          if (m.matches()) {
            sb = new StringBuffer();
            // indent is one level deeper than method indent:
            indent = m.group(1) + m.group(1);
            diffHdr[0] = "Local method: " + m.group(2) + " (line " + c + ")";
          }
        } else {
          line = line.replaceFirst("^(\\s+)//\\s*import ", "$1import ");
          sb.append(line.startsWith(indent) ? line.substring(indent.length()) : line).append(LF);
          if (methodEnd.matcher(line).matches()) {
            break;
          }
        }
      }
    } catch (IOException e) {
      throw new ApiException(e);
    }
    return sb;
  }

  private String extractHeader(StringBuffer src, String replacement) {
    String  hdr  = null;
    Pattern pHdr = Pattern.compile("(\\s*//\\s*@formatter:off.+(?:<ProcessData>|PrimaryDocumentName|PrimaryDocumentData).+?@formatter:on\\ *\\r?\\n)",
        Pattern.MULTILINE | Pattern.DOTALL);
    Matcher m    = pHdr.matcher(src.toString());
    src.setLength(0);
    if (m.find()) {
      hdr = m.group(1);
      m.appendReplacement(src, replacement == null ? "" : replacement);
    }
    m.appendTail(src);
    return hdr;
  }

  private void deleteSource(String prefix) throws ApiException {
    Property p = Property.find(prefix, methodName);
    if (p == null) {
      System.err.format("No such property: %s%n", methodName);
      setRc(1);
      return;
    }
    if (p.delete()) {
      System.out.format("Deleted property [%s]%s%n", prefix, p.getPropertyKey());
    } else {
      System.err.format("Failed to delete property [%s]%s: %s%n", prefix, p.getPropertyKey(), ApiClient.getApiErrorMsg());
      setRc(1);
    }
  }

  private void refresh(String prefix) throws ApiException {
    List<String> props = PropertyFiles.refreshCache(prefix);
    if (props.isEmpty()) {
      System.err.println("No matching properties refreshed on server!");
      setRc(1);
    } else {
      System.out.format("Refreshed properties: %s%n", props);
    }
  }

  private boolean diff(String oldSrc, String newSrc, boolean ignoreBlankLines, boolean showDiff, int surround) {
    // @formatter:off
    DiffRowGenerator dg   = DiffRowGenerator.create()
        .showInlineDiffs(true)
        .mergeOriginalRevised(false)
        .inlineDiffByWord(true)
        .oldTag(f -> "~")
        .newTag(f -> "**")
        .build();

    List<DiffRow> rows = dg.generateDiffRows(
        Arrays.asList(oldSrc.split("\\r?\\n")), 
        Arrays.asList(newSrc.split("\\r?\\n"))
      );
    // @formatter:on

    boolean          rc      = false;
    int              rowNum  = 0;
    int              showPos = 0;
    for (DiffRow r : rows) {
      rowNum++;
      if (r.getTag() != Tag.EQUAL && !(ignoreBlankLines
          && ((r.getTag() == Tag.DELETE && r.getOldLine().trim().isEmpty()) || (r.getTag() == Tag.INSERT && r.getNewLine().trim().isEmpty())))) {
        rc = true;
        if (showDiff && surround >= 0) {
          for (int i = (rowNum - surround); i <= (rowNum + surround); i++) {
            if (i > 0 && i > showPos) {
              showDiff(i, rows.get(i - 1));
              showPos = i;
            }
          }
          System.out.println();
        }
      }
      if (showDiff && surround == -1) {
        showDiff(rowNum, r);
      }
    }
    return rc;
  }

  private void showDiff(int rowNum, DiffRow r) {
    char c;
    switch (r.getTag()) {
    case CHANGE:
      c = '~';
      break;
    case DELETE:
      c = '<';
      break;
    case INSERT:
      c = '>';
      break;
    default:
      c = '|';
      break;
    }

    String fmt = String.format("%%-4s|%%-%d.%ds%%s%%-%d.%ds%%n", colWidth, colWidth, colWidth, colWidth);
    if (diffHdr != null) {
      System.out.format(fmt, "Line", diffHdr[0], "|", diffHdr[1]);
      StringBuffer sb = new StringBuffer(colWidth);
      for (int i = 0; i < colWidth; i++)
        sb.append('-');
      System.out.format(fmt, "----", sb, "|", sb);
      diffHdr = null;
    }
    System.out.format(fmt, rowNum, r.getOldLine(), c, r.getNewLine());
  }
}
