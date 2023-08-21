package de.denkunddachte.b2biutil.loader;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.denkunddachte.b2biutil.loader.model.FetchRule;
import de.denkunddachte.b2biutil.loader.model.LoadAction;
import de.denkunddachte.b2biutil.loader.model.Partner;
import de.denkunddachte.b2biutil.loader.model.TransferRule;

public abstract class LoaderInput implements AutoCloseable {
  public enum FORMAT {
    XLSX, YAML, DATAU
  }

  private Logger              logger;
  protected File              inputFile;
  protected boolean           valid;
  protected boolean           raiseError;
  protected Set<Partner>      partners   = new LinkedHashSet<>();
  protected Set<TransferRule> rules      = new LinkedHashSet<>();
  protected Set<FetchRule>    fetchRules = new LinkedHashSet<>();

  protected LoaderInput() {
    logger = getLogger();
    valid = true;
  }

  public void readInput(String inputFile) throws TemplateException {
    readInput(inputFile, true);
  }

  public void readInput(String inputFile, boolean raiseError) throws TemplateException {
    readInput(new File(inputFile), raiseError);
  }

  public void readInput(File inputFile) throws TemplateException {
    readInput(inputFile, true);
  }

  public void readInput(File inputFile, boolean raiseError) throws TemplateException {
    this.raiseError = raiseError;
    this.inputFile = inputFile;
    read();
  }

  protected abstract void read() throws TemplateException;

  public abstract Logger getLogger();

  public File getInputfile() {
    return inputFile;
  }

  public boolean isRaiseError() {
    return raiseError;
  }

  public void setRaiseError(boolean raiseError) {
    this.raiseError = raiseError;
  }

  public Partner getPartner(String id) {
    return partners.stream().filter(p -> p.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
  }

  public Partner getPartner(Partner partner) throws TemplateException {
    return partners.stream().filter(p -> p.equals(partner)).findFirst().orElseThrow(() -> new TemplateException("No such partner: " + partner + "!"));
  }

  public Set<Partner> getActivePartners() {
    Set<Partner> result = new LinkedHashSet<>(partners.size());
    result.addAll(partners.stream().filter(e -> LoadAction.isActive(e.getLoadAction())).collect(Collectors.toSet()));
    return result;
  }

  public Set<Partner> getPartners() {
    Set<Partner> result = new LinkedHashSet<>(partners.size());
    result.addAll(partners);
    return result;
  }

  public TransferRule getTransferRule(TransferRule rule) throws TemplateException {
    return rules.stream().filter(r -> r.equals(rule)).findFirst().orElseThrow(() -> new TemplateException("No such rule: " + rule + "!"));
  }

  public Set<TransferRule> getActiveRules() {
    Set<TransferRule> result = new LinkedHashSet<>(rules.size());
    result.addAll(rules.stream().filter(e -> LoadAction.isActive(e.getLoadAction())).collect(Collectors.toSet()));
    return result;
  }

  public Set<TransferRule> getRules() {
    Set<TransferRule> result = new LinkedHashSet<>(rules.size());
    result.addAll(rules);
    return result;
  }

  public FetchRule getFetchRule(FetchRule rule) throws TemplateException {
    return fetchRules.stream().filter(r -> r.equals(rule)).findFirst().orElseThrow(() -> new TemplateException("No such fetch rule: " + rule + "!"));
  }

  public Set<FetchRule> getActiveFetchRules() {
    Set<FetchRule> result = new LinkedHashSet<>(fetchRules.size());
    result.addAll(fetchRules.stream().filter(e -> LoadAction.isActive(e.getLoadAction())).collect(Collectors.toSet()));
    return result;
  }

  public Set<FetchRule> getFetchRules() {
    Set<FetchRule> result = new LinkedHashSet<>(fetchRules.size());
    result.addAll(fetchRules);
    return result;
  }

  public boolean isValid() {
    return valid;
  }

  protected void error(String src, int row, Exception e, String... hints) throws TemplateException {
    valid = false;
    StringBuilder msg = new StringBuilder();
    if (src != null) {
      msg.append('[').append(src).append("] ");
    }
    if (row > 0) {
      msg.append(" Row ").append(row).append(": ");
    }
    msg.append(e.getMessage());
    logger.log(Level.SEVERE, "{0}", msg);
    if (e.getCause() != null && e.getCause().getCause() != null && !msg.toString().equals(e.getCause().getCause().getMessage())) {
      logger.log(Level.SEVERE, "Cause: {0}", e.getCause().getCause().getMessage());
    }
    if (hints != null) {
      for (String hint : hints) {
        logger.log(Level.SEVERE, hint);
      }
    }

    if (raiseError) {
      if (e instanceof TemplateException) {
        throw (TemplateException) e;
      } else {
        throw new TemplateException(e);
      }
    }
  }

  public static FORMAT getLoaderFormatFor(File infile) throws TemplateException {
    final String ext = infile.getName().indexOf('.') > 1 ? infile.getName().toLowerCase().substring(infile.getName().lastIndexOf('.') + 1) : "";
    switch (ext) {
    case "xlsx":
      return FORMAT.XLSX;
    case "yaml":
      return FORMAT.YAML;
    case "drecom":
    case "datau":
    case "meldesatz":
      return FORMAT.DATAU;
    default:
      throw new TemplateException(
          "Format could not be determined or is not supported for file " + infile + "! Use createLoaderInput(File infile, FORMAT format) to force format.");
    }
  }

  public static LoaderInput createLoaderInput(File infile) throws TemplateException {
    return createLoaderInput(getLoaderFormatFor(infile));
  }

  public static LoaderInput createLoaderInput(FORMAT format) throws TemplateException {
    switch (format) {
    case XLSX:
      return new BulkloadExcelInput();
    case DATAU:
      return new DatauMeldesatzInput();
    case YAML:
    default:
      throw new TemplateException("Loader format " + format + " not implemented yet.");
    }
  }

}
