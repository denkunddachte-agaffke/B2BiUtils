package de.denkunddachte.b2biutil.loader;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.denkunddachte.b2biutil.loader.LoaderResult.Outcome;
import de.denkunddachte.b2biutil.loader.model.AbstractLoadRecord;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.exception.B2BLoadException;
import de.denkunddachte.sfgapi.ApiClient;

public abstract class B2BiBulkLoader implements AutoCloseable {
  private Logger            logger;
  protected LoaderInput     loaderInput;
  protected boolean         dryrun;
  protected boolean         raiseerror;
  protected boolean         casesensitive;
  protected LoaderResult    result;
  protected boolean         duplicateMissingDeliveryParams;
  protected FieldAction     fillDescription    = FieldAction.KEEP;
  protected FieldAction     fillAdditionalInfo = FieldAction.KEEP;
  protected final ChangeSet changeset          = new ChangeSet(false);

  protected B2BiBulkLoader(LoaderInput input) {
    this.logger = getLogger();
    this.dryrun = false;
    this.raiseerror = true;
    this.casesensitive = true;
    this.loaderInput = input;
    this.result = new LoaderResult(input);
  }

  public LoaderInput getLoaderInput() {
    return this.loaderInput;
  }

  public void setDryrun(boolean dryrun) {
    this.dryrun = dryrun;
    System.setProperty(ApiClient.API_DRYRUN_PROPERTY, Boolean.toString(dryrun));
  }

  public void setRaiseError(boolean raiseerror) {
    this.raiseerror = raiseerror;
  }

  public void setCaseSensitive(boolean casesensitive) {
    this.casesensitive = casesensitive;
  }

  protected boolean error(AbstractLoadRecord src, String msg) throws B2BLoadException {
    return error(src, new B2BLoadException(msg));
  }

  public abstract LoaderResult load() throws B2BLoadException, ApiException;

  public abstract Logger getLogger();

  protected boolean error(AbstractLoadRecord src, Exception e, String... hints) throws B2BLoadException {
    StringBuilder msg = new StringBuilder();
    if (src == null) {
      result.setResult(false);
    } else {
      result.addResult(src, Outcome.FAILED);
      msg.append('[').append(src.getLoadAction().name()).append(' ').append(src.getArtifact()).append(':').append(src.getLine()).append("]: ");
    }
    msg.append(e.getMessage());
    result.addMessage(msg.toString());
    Level lvl = raiseerror ? Level.SEVERE : Level.WARNING;
    logger.log(lvl, "{0}", msg);
    if (e.getCause() != null && e.getCause().getCause() != null && !msg.toString().equals(e.getCause().getCause().getMessage())) {
      logger.log(lvl, "Cause: {0}", e.getCause().getCause().getMessage());
    }
    if (hints != null) {
      for (String hint : hints) {
        logger.log(lvl, hint);
      }
    }

    if (raiseerror) {
      if (e instanceof B2BLoadException) {
        throw (B2BLoadException) e;
      } else {
        throw new B2BLoadException(e);
      }
    } else {
      return true;
    }
  }

  public abstract boolean validate() throws B2BLoadException, ApiException;

  public boolean isDuplicateMissingDeliveryParams() {
    return duplicateMissingDeliveryParams;
  }

  public void setDuplicateMissingDeliveryParams(boolean duplicateMissingDeliveryParams) {
    this.duplicateMissingDeliveryParams = duplicateMissingDeliveryParams;
  }

  public LoaderResult getResult() {
    return result;
  }

  public void setResult(LoaderResult result) {
    this.result = result;
  }

  public FieldAction getFillDescription() {
    return fillDescription;
  }

  public void setFillDescription(FieldAction fillDescription) {
    this.fillDescription = fillDescription;
  }

  public FieldAction getFillAdditionalInfo() {
    return fillAdditionalInfo;
  }

  public void setFillAdditionalInfo(FieldAction fillAdditionalInfo) {
    this.fillAdditionalInfo = fillAdditionalInfo;
  }

  public ChangeSet getChangeSet() {
    return this.changeset;
  }

  public void collect() {
    this.changeset.setCollect(true);
  }
}
