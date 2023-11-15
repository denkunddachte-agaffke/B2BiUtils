package de.denkunddachte.b2biutil.workflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import de.denkunddachte.b2biutil.AbstractConsoleApp;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.siresource.SIArtifact;
import de.denkunddachte.siresource.SIArtifact.TYPE;
import de.denkunddachte.siresource.SIExport;
import de.denkunddachte.utils.CommandLineParser.CommandLineException;
import de.denkunddachte.utils.CommandLineParser.ParsedCommandLine;
import de.denkunddachte.utils.Config;
import de.denkunddachte.utils.FileFind;

public class SIResourceUtil extends AbstractConsoleApp {
  // private static final Logger LOG = Logger.getLogger(SIResourceUtil.class.getName());
  private static final DateTimeFormatter FMT_ISO_DTTM = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  static {
    OPTIONS.setProgramName(SIResourceUtil.class.getName());
    OPTIONS.setHasOptionalArgs(true);
    OPTIONS.setProgramDescription("SI XML resource file utility.");

    // Common options
    OPTIONS.section("General options");
    OPTIONS.add(Props.PROP_FILE + "|f=s", "Resource file, list or folder", true);

    // List
    OPTIONS.section("Parse resource files");
    OPTIONS.add(Props.PROP_LIST + "|L", "List resource file contents");
    OPTIONS.add(Props.PROP_VALIDATE + "|v", "Validate resource or package file against directory");
    OPTIONS.add(Props.PROP_EXTRACT + "|e:s", "Extract resource file to resource folder (default: package folder)");
    OPTIONS.add(Props.PROP_ALL_VERSIONS, "Extract all resource versions.");

    // Create
    OPTIONS.section("Create resource files");
    OPTIONS.add(Props.PROP_CREATE + "|c", "Create resources file from list or folder");

    OPTIONS.addProgramHelp("Examples:");
  }

  private String packageName;
  private File   packageFile;
  private File   packageDir;

  public SIResourceUtil(String[] args) throws CommandLineException, ApiException {
    super(args);
  }

  @Override
  protected void init(ParsedCommandLine cmdline) throws CommandLineException, ApiException {
    // validate
    packageFile = new File(cfg.getString(Props.PROP_FILE));
    if (!cfg.hasAny(Props.PROP_LIST, Props.PROP_EXTRACT, Props.PROP_CREATE, Props.PROP_VALIDATE)) {
      throw new CommandLineException("No action specifed. Use --list, --validate, --extract or --create.");
    }
    if (!cfg.hasProperty(Props.PROP_LIST) && (packageFile.isDirectory() || !packageFile.exists())) {
      if (packageFile.getName().equals(".")) {
        packageDir = packageFile.getAbsoluteFile().getParentFile();
      } else {
        packageDir = packageFile;
      }
      packageFile = new File(packageDir, packageDir.getName() + ".xml");
    }
    packageDir = packageFile.getAbsoluteFile().getParentFile();
    packageName = packageFile.getName().replaceAll("(?i)\\.(?:xml|sipkg)$", "");
  }

  public static void main(String[] args) {
    int rc = 1;
    try (SIResourceUtil api = new SIResourceUtil(args)) {
      Config cfg = Config.getConfig();
      if (cfg.hasProperty(Props.PROP_LIST)) {
        api.listPackage();
      } else if (cfg.hasProperty(Props.PROP_VALIDATE)) {
        rc = (api.validate() ? 0 : 1);
      } else if (cfg.hasProperty(Props.PROP_EXTRACT)) {
        api.extract();
      } else if (cfg.hasProperty(Props.PROP_CREATE)) {
        api.create();
      }
    } catch (CommandLineException e) {
      e.printStackTrace(System.err);
      System.exit(3);
    } catch (ApiException e) {
      e.printStackTrace(System.err);
      System.exit(2);
    }
    System.exit(rc);
  }

  private void create() throws ApiException {
    SIExport si;
    File pkgFile = new File(packageDir, packageName + ".xml");
    File lstFile = new File(packageDir, packageName + ".sipkg");
    if (lstFile.exists()) {
      si = readFile(lstFile);
    } else {
      si = readPackageDir(packageDir);
    }
    if (si.isEmpty()) {
      System.err.println("No artifatcts found in " + (lstFile.exists() ? lstFile : packageDir) + "!");
      return;
    }
    try (OutputStream os = new FileOutputStream(pkgFile); OutputStream lst = new FileOutputStream(lstFile)) {
      si.createImport(os);
      System.out.println("Created package " + pkgFile);
      si.listPackage(lst, true);
      System.out.println("Created package manifest " + pkgFile);
    } catch (IOException e) {
      throw new ApiException(e);
    } finally {
      for (File f : new File[] { pkgFile, lstFile }) {
        if (f.isFile() && f.length() == 0)
          f.delete();
      }
    }
  }

  private void extract() throws ApiException {
    SIExport si          = new SIExport(packageFile);
    boolean  allVersions = cfg.getBoolean(Props.PROP_ALL_VERSIONS);
    if (!packageDir.isDirectory()) {
      packageDir.mkdirs();
    }
    try (OutputStream lst = new FileOutputStream(new File(packageDir, packageName + ".sipkg"))) {
      for (SIArtifact a : si.getArtifacts()) {
        File   destDir  = new File(packageDir, a.getType() == TYPE.WFD ? "BP" : "XSLT");
        String fileName = a.getName() + (a.getType() == TYPE.WFD ? ".bpml" : "." + a.getType().name().toLowerCase());
        if (allVersions) {
          fileName = a.getName() + (allVersions ? "-" + a.getVersion() : "") + (a.getType() == TYPE.WFD ? ".bpml" : "." + a.getType().name().toLowerCase());
        } else if (!a.isDefaultVersion()) {
          continue;
        }
        Path p        = FileFind.getPath(packageDir.getAbsolutePath(), fileName, true);
        File destFile = null;
        if (p == null) {
          if (!destDir.isDirectory()) {
            destDir.mkdir();
          }
          destFile = new File(destDir, fileName);
        } else {
          destFile = p.toFile();
        }
        System.out.format("Extract %s %s to %s%n", a.getType(), a.getName(), destFile);
        try (OutputStream os = new FileOutputStream(destFile)) {
          os.write(a.getData());
        }
      }
      si.writeManifest(lst, !allVersions);
    } catch (IOException e) {
      throw new ApiException(e);
    }
  }

  private SIExport readFile(File packageFile) throws ApiException {
    SIExport si = null;
    if (packageFile.isDirectory()) {
      for (File f : new File[] { new File(packageFile, packageName + ".sipkg"), new File(packageFile, packageName + ".xml") }) {
        if (f.exists()) {
          packageFile = f;
          return readFile(packageFile);
        }
      }
      return readPackageDir(packageFile);
    }
    if (packageFile.exists()) {
      if (packageFile.getName().matches("(?i).+\\.sipkg$")) {
        si = readPackageList(packageFile);
      } else {
        si = new SIExport(packageFile);
      }
    } else {
      throw new ApiException("File " + packageFile + " does not exist!");
    }
    return si;
  }

  private SIExport readPackageList(File pkgList) throws ApiException {
    SIExport si = new SIExport();
    try (BufferedReader rd = new BufferedReader(new FileReader(pkgList))) {
      String line;
      while ((line = rd.readLine()) != null) {
        if (!line.matches("^(WFD|XSLT);.+")) {
          continue;
        }
        String[]   d        = line.split(";");
        SIArtifact a        = new SIArtifact(SIArtifact.TYPE.valueOf(d[0]), d[1]);
        String     fileName = a.getName() + (a.getType() == TYPE.WFD ? ".bpml" : "." + a.getType().name().toLowerCase());
        Path       p        = FileFind.getPath(packageDir.getAbsolutePath(), fileName, true);
        if (p != null) {
          a.setData(p.toFile());
          if (d.length >= 7) {
            a.setVersion(Integer.parseInt(d[3]));
            a.setModifyTime(OffsetDateTime.from(FMT_ISO_DTTM.parse(d[4])));
            a.setModifiedBy(d[5]);
            a.setComment(d[6]);
          }
          si.putArtifact(a);
        } else {
          System.err.format("No file found for artifact %s in %s. Ignore...%n", a.key(), packageDir);
        }
      }
    } catch (IOException e) {
      throw new ApiException(e);
    }
    return si;
  }

  private SIExport readPackageDir(File dir) throws ApiException {
    SIExport si = new SIExport();
    try {
      for (Path p : FileFind.find(dir.getAbsolutePath(), "(?i).+\\.(xslt|bpml)$", null)) {
        String     fn = p.getFileName().toString();
        SIArtifact a  = new SIArtifact((fn.toLowerCase().endsWith(".bpml") ? TYPE.WFD : TYPE.XSLT), fn.replaceAll("(?i)\\.(bpml|xslt)$", ""));
        a.setData(p.toFile());
        si.putArtifact(a);
      }
    } catch (IOException e) {
      throw new ApiException(e);
    }
    return si;
  }

  private void listPackage() throws ApiException {
    int c = 0;
    SIExport si = readFile(packageFile);

    System.out.format("Package %s %s:%n%n",
        (packageFile.isDirectory() ? "folder" : (packageFile.getName().toLowerCase().endsWith(".sipkg") ? "manifest" : "resource file")), packageFile);
    if (si.isImported()) {
      System.out.format("%-3s %-4s %-30s %3s %2s %-14s %-19s %-44s %s%n", "No", "Type", "Name", "Ver", "Df", "ModifiedBy", "ModifyTime", "Hash", "Comment");
      System.out.println(separator('-', 160));
      for (SIArtifact a : si.getArtifacts()) {
        System.out.format("%-3s %-4s %-30s %3s %2s %-14s %-19s %-44s %s%n", ++c, a.getType().name(), a.getName(), a.getVersion(),
            (a.isDefaultVersion() ? MARK : ""), a.getModifiedBy(), a.getModifyTime().format(FMT_ISO_DTTM), a.getSha256Hash(), a.getComment());
      }
    } else {
      System.out.format("%-3s %-4s %-30s %-44s%n", "No", "Type", "Name", "Hash");
      System.out.println(separator('-', 80));
      for (SIArtifact a : si.getArtifacts()) {
        System.out.format("%-3s %-4s %-30s %-44s%n", ++c, a.getType().name(), a.getName(), a.getSha256Hash());
      }
    }
  }

  private boolean validateList(File pkgList, SIExport pkg) throws ApiException {
    boolean result  = true;
    String  pkgType = "pkg";
    if (pkg == null) {
      pkg = readPackageDir(packageDir);
      pkgType = "pkg dir";
    }
    Set<String> lstentries = new HashSet<>();
    try (BufferedReader rd = new BufferedReader(new FileReader(pkgList))) {
      String line;
      while ((line = rd.readLine()) != null) {
        if (!line.matches("^(WFD|XSLT);.+")) {
          continue;
        }
        String[]   d = line.split(";");
        SIArtifact a = new SIArtifact(SIArtifact.TYPE.valueOf(d[0]), d[1]);
        lstentries.add(a.key());
        System.out.format("%s %s ... ", a.getType(), a.getName());
        if (pkg.hasArtifact(a)) {
          if (d.length > 2 && !d[2].equals(pkg.getArtifact(a).getSha256Hash())) {
            System.out.format("hash mismatch in %s!%n", pkgType);
            result = false;
          } else {
            System.out.println("OK.");
          }
        } else {
          System.out.format("missing %s!%n", pkgType);
          result = false;
        }
      }
    } catch (IOException e) {
      throw new ApiException(e);
    }
    for (SIArtifact a : pkg.getArtifacts()) {
      if (!lstentries.contains(a.key())) {
        System.out.format("Artifact %s in %s not included in list file!%n", a.key(), pkgType);
        result = false;
      }
    }
    return result;
  }

  private boolean validatePkg(SIExport pkg) throws ApiException {
    boolean  result = true;
    SIExport dir    = readPackageDir(packageDir);
    for (SIArtifact a : pkg.getArtifacts()) {
      System.out.format("%s %s ... ", a.getType(), a.getName());
      if (dir.hasArtifact(a, true)) {
        if (!a.getSha256Hash().equals(dir.getArtifact(a, true).getSha256Hash())) {
          System.out.println("hash mismatch in pkg dir!");
          result = false;
        } else {
          System.out.println("OK.");
        }
      } else {
        System.out.println("missing in pkg dir!");
        result = false;
      }
    }
    for (SIArtifact a : dir.getArtifacts()) {
      if (!pkg.hasArtifact(a, true)) {
        System.out.format("Artifact %s in pkg dir not included in package!%n", a.key());
        result = false;
      }
    }
    return result;
  }

  private boolean validate() throws ApiException {
    File     lstFile = new File(packageDir, packageName + ".sipkg");
    SIExport pkg     = null;
    if (packageFile != null && packageFile.exists() && packageFile.getName().toLowerCase().endsWith(".xml")) {
      pkg = new SIExport(packageFile);
    }
    if (lstFile.exists()) {
      return validateList(lstFile, pkg);
    } else if (pkg != null) {
      return validatePkg(pkg);
    } else {
      System.out.println("Nothing to validate!");
      return false;
    }
  }
}
