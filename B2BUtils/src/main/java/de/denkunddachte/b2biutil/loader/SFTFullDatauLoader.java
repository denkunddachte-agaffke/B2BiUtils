package de.denkunddachte.b2biutil.loader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.denkunddachte.jpa.SfgEntityManager;
import de.denkunddachte.jpa.az.FgParam;
import de.denkunddachte.b2biutil.api.Props;
import de.denkunddachte.b2biutil.loader.model.TransferRule;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.exception.B2BLoadException;

public class SFTFullDatauLoader extends SFTFullLoader {
  private static final Logger LOGGER          = Logger.getLogger(SFTFullDatauLoader.class.getName());
  private static final String PARAM_REFNUM    = "DATAU_REFNUM";
  private static final String PARAM_BATCHDATE = "DATAU_BATCHDATE";
  private static final String PARAM_LOADDATE  = "DATAU_LOADDATE";

  private long                refNum;
  private Date                batchDate;
  private final DateFormat    fmtDate         = new SimpleDateFormat("yyyy-MM-dd");

  public SFTFullDatauLoader(LoaderInput loaderInput) {
    super(loaderInput);
    refNum = ((DatauMeldesatzInput) loaderInput).getRefNum();
    batchDate = ((DatauMeldesatzInput) loaderInput).getBatchDate();
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  public LoaderResult load() throws B2BLoadException, ApiException {
    LOGGER.log(Level.FINEST, "Start loading from input {0}... Set duplicate delivery parameters to true.", loaderInput);
    setDuplicateMissingDeliveryParams(true);

    if (!validate()) {
      throw new B2BLoadException("Validation failed!");
    }

    this.sfgEm = SfgEntityManager.instance();
    this.closeEm = true;

    sfgEm.startTransaction();
    for (TransferRule t : loaderInput.getActiveRules()) {
      loadTransferRule(t);
    }
    commitOrRollBack("load rules");
    
    sfgEm.startTransaction();
    FgParam par = FgParam.findOpt(PARAM_REFNUM, sfgEm.getEntityManager());
    par.setLong(refNum);
    if (!sfgEm.getEntityManager().contains(par)) {
      sfgEm.persist(par);
    }
    par = FgParam.findOpt(PARAM_BATCHDATE, sfgEm.getEntityManager());
    par.setDate(batchDate);
    if (!sfgEm.getEntityManager().contains(par)) {
      sfgEm.persist(par);
    }
    par = FgParam.findOpt(PARAM_LOADDATE, sfgEm.getEntityManager());
    par.setDateTime(new Date());
    if (!sfgEm.getEntityManager().contains(par)) {
      sfgEm.persist(par);
    }
    if (dryrun && !cfg.hasProperty(Props.PROP_REPORT)) {
      sfgEm.rollback();
    } else {
      sfgEm.commit();  
    }
    return getResult();
  }

  @Override
  public boolean validate() throws B2BLoadException, ApiException {
    setDuplicateMissingDeliveryParams(true);
    boolean result = super.validate();

    if (result) {
      this.sfgEm = SfgEntityManager.instance();
      Long lastRefNum = FgParam.findOpt(PARAM_REFNUM, sfgEm.getEntityManager()).getLong();
      String lastBatchDate = FgParam.findOpt(PARAM_BATCHDATE, sfgEm.getEntityManager()).getString();
      String lastLoadTime = FgParam.findOpt(PARAM_LOADDATE, sfgEm.getEntityManager()).getString();
      changeset.log(String.format("Last \"Meldesatz\" batch: refNum=%d, batchDate=%s, loadTime=%s", lastRefNum, lastBatchDate, lastLoadTime));
      changeset.log(
          String.format("Load \"Meldesatz\" batch: refNum=%d, batchDate=%s from file=%s", refNum, fmtDate.format(batchDate), loaderInput.inputFile.getAbsoluteFile()));

      LOGGER.log(Level.FINE, "Current Meldesatz file: {0} (refNum: {1}, date: {2}). Last refnum: {3}, date: {4}, loaded: {5} ",
          new Object[] { loaderInput.inputFile.getName(), refNum, fmtDate.format(batchDate), lastRefNum, lastBatchDate, lastLoadTime });
      if (lastRefNum != null && lastRefNum >= refNum) {
        error(null, String.format("DATAU Meldesatz refNum not in sequence (last: %d/%s loaded %s, current input: %d/%s)", lastRefNum, lastBatchDate,
            lastLoadTime, refNum, fmtDate.format(batchDate)));
        result = !raiseerror;
      }
    }
    return result;
  }
}
