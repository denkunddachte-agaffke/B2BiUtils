package de.denkunddachte.b2biutil.loader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.denkunddachte.b2biutil.api.Props;
import de.denkunddachte.b2biutil.loader.model.LoadAction;
import de.denkunddachte.b2biutil.loader.model.TransferRule;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.exception.B2BLoadException;
import de.denkunddachte.jpa.SfgEntityManager;
import de.denkunddachte.jpa.az.FgCustomer;
import de.denkunddachte.jpa.az.FgFetchSftp;
import de.denkunddachte.jpa.az.FgParam;
import de.denkunddachte.utils.FileUtil;
import de.denkunddachte.utils.StringUtils;

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
      loadFetchRuleForDATAURule(t);
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

  private boolean matchFetchPattern(TransferRule t, FgFetchSftp f) {
    if (StringUtils.isNullOrWhiteSpace(t.getRcvPath())) {
      return FileUtil.basename(f.getFilepattern()).equals(t.getFilePattern());
    } else {
      return f.getFilepattern().equals(t.getRcvPath().replaceAll("[/]*$", "/") + t.getFilePattern());
    }
  }

  /*
   * Try to determine if producer is active or passive (SFTP) and create a SFTP fetch rule, if required.
   * Criteria:
   * - incoming transfer rule provides a path component (field 9 "Empfangspfad")
   * - producer has other fetch configurations
   * 
   */
  private void loadFetchRuleForDATAURule(TransferRule t) throws B2BLoadException {
    String     fetchPath      = t.getRcvPath();
    FgCustomer producer       = FgCustomer.find(t.getProducer(), true, sfgEm.getEntityManager());
    boolean    activeProducer = StringUtils.isNullOrWhiteSpace(fetchPath);
    if (activeProducer) {
      activeProducer = producer.getFgFetchTransfers().isEmpty();
    }

    if (activeProducer)
      return;

    if (t.getLoadAction() == LoadAction.DELETE) {
      for (FgFetchSftp f : producer.getFgFetchTransfers().stream().filter(f -> matchFetchPattern(t, f)).collect(Collectors.toList()) ) {
        removeItem(f, changeset);
        producer.removeFgFetchTransfer(f);
        sfgEm.remove(f);
      }
      changeset.update(producer);
      return;
    }
    
    FgFetchSftp sftpFetch = null;
    for (FgFetchSftp f : producer.getFgFetchTransfers()) {
      String filename = StringUtils.isNullOrWhiteSpace(fetchPath) ? FileUtil.basename(f.getFilepattern()) : f.getFilepattern();
      String p        = StringUtils.globToRegexp(filename);
      if (t.getFilePattern().matches(p)) {
        LOGGER.log(Level.FINE, "Found matching fetch rule for {0}: {1}", new Object[] { t, f });
        return;
      }
      if (SFTFullLoader.FETCH_TEMPLATE_PATTERN.equals(FileUtil.basename(f.getFilepattern()))) {
        fetchPath = FileUtil.dirname(f.getFilepattern());
        sftpFetch = f.createCopy();
        break;
      }
      if (StringUtils.isNullOrWhiteSpace(fetchPath)) {
        fetchPath = FileUtil.dirname(f.getFilepattern());
      } else if (!fetchPath.equals(FileUtil.dirname(f.getFilepattern()))) {
        LOGGER.log(Level.WARNING, "Producer {0} uses additional source path ({1}): use {2} for pattern {3}.",
            new Object[] { t.getProducer(), FileUtil.dirname(f.getFilepattern()), fetchPath, t.getFilePattern() });
      }
      if (sftpFetch == null) {
        sftpFetch = f.createCopy();
      }
    }
    
    if (sftpFetch == null) {
      throw new B2BLoadException("Could not create SFTP fetch configuration for producer " + t.getProducer() + " because not match template was found!");
    }
    StringBuilder fetchPattern = new StringBuilder();
    if (!StringUtils.isNullOrWhiteSpace(fetchPath)) {
      fetchPattern.append(fetchPath);
      if (!fetchPath.endsWith("/")) {
        fetchPattern.append('/');
      }
    }
    fetchPattern.append(t.getFilePattern());
    sftpFetch.setFilepattern(fetchPattern.toString());
    sftpFetch.setEnabled(true);
    if (cfg.getProperty(Props.PROP_DEFAULT_FETCH_SCHEDULE) != null) {
      sftpFetch.setScheduleName(cfg.getString(Props.PROP_DEFAULT_FETCH_SCHEDULE));
    }
    LOGGER.log(Level.FINE, "Add SFTP fetch config to producer {0}: {1}", new Object[] { t.getProducer(), sftpFetch });
    producer.addFgFetchTransfer(sftpFetch);
    changeset.add(sftpFetch);
    changeset.update(producer);
  }

  @Override
  public boolean validate() throws B2BLoadException, ApiException {
    setDuplicateMissingDeliveryParams(true);
    boolean result = super.validate();

    if (result) {
      this.sfgEm = SfgEntityManager.instance();
      Long   lastRefNum    = FgParam.findOpt(PARAM_REFNUM, sfgEm.getEntityManager()).getLong();
      String lastBatchDate = FgParam.findOpt(PARAM_BATCHDATE, sfgEm.getEntityManager()).getString();
      String lastLoadTime  = FgParam.findOpt(PARAM_LOADDATE, sfgEm.getEntityManager()).getString();
      changeset.log(String.format("Last \"Meldesatz\" batch: refNum=%d, batchDate=%s, loadTime=%s", lastRefNum, lastBatchDate, lastLoadTime));
      changeset.log(String.format("Load \"Meldesatz\" batch: refNum=%d, batchDate=%s from file=%s", refNum, fmtDate.format(batchDate),
          loaderInput.inputFile.getAbsoluteFile()));

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
