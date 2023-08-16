package de.denkunddachte.b2biutil.loader;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.denkunddachte.jpa.AbstractSfgObject;
import de.denkunddachte.jpa.SfgEntityManager;
import de.denkunddachte.jpa.az.FgCustomer;
import de.denkunddachte.jpa.az.FgDelivery;
import de.denkunddachte.jpa.az.FgDeliveryAwsS3;
import de.denkunddachte.jpa.az.FgDeliveryBuba;
import de.denkunddachte.jpa.az.FgDeliveryBuba.TLS;
import de.denkunddachte.jpa.az.FgDeliveryCd;
import de.denkunddachte.jpa.az.FgDeliveryMbox;
import de.denkunddachte.jpa.az.FgDeliveryOftp;
import de.denkunddachte.jpa.az.FgDeliverySftp;
import de.denkunddachte.jpa.az.FgFetchAwsS3;
import de.denkunddachte.jpa.az.FgFetchSftp;
import de.denkunddachte.jpa.az.FgFiletype;
import de.denkunddachte.jpa.az.FgTransfer;
import de.denkunddachte.b2biutil.api.Props;
import de.denkunddachte.b2biutil.loader.LoaderResult.Outcome;
import de.denkunddachte.b2biutil.loader.model.AWSS3Endpoint;
import de.denkunddachte.b2biutil.loader.model.BubaEndpoint;
import de.denkunddachte.b2biutil.loader.model.CDEndpoint;
import de.denkunddachte.b2biutil.loader.model.DeliveryRule;
import de.denkunddachte.b2biutil.loader.model.FetchRule;
import de.denkunddachte.b2biutil.loader.model.LoadAction;
import de.denkunddachte.b2biutil.loader.model.MboxEndpoint;
import de.denkunddachte.b2biutil.loader.model.OftpEndpoint;
import de.denkunddachte.b2biutil.loader.model.Partner;
import de.denkunddachte.b2biutil.loader.model.SftpEndpoint;
import de.denkunddachte.b2biutil.loader.model.TransferRule;
import de.denkunddachte.enums.CDBinaryMode;
import de.denkunddachte.enums.FTPartnerType;
import de.denkunddachte.enums.FTProtocol;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.exception.B2BLoadException;
import de.denkunddachte.exception.NotImplementedException;
import de.denkunddachte.ft.CDNode;
import de.denkunddachte.sfgapi.SshKnownHostKey;
import de.denkunddachte.sfgapi.SshUserIdentityKey;
import de.denkunddachte.utils.Config;
import de.denkunddachte.utils.StringUtils;

public class SFTFullLoader extends B2BiBulkLoader {
  public static final String DELIVERY_TEMPLATE_PATTERN = "DELIVERY_TEMPLATE";
  public static final String FETCH_TEMPLATE_PATTERN    = "FETCH_TEMPLATE";

  static final Logger        LOGGER                    = Logger.getLogger(SFTFullLoader.class.getName());
  protected boolean          closeEm;
  protected SfgEntityManager sfgEm;
  protected final Config     cfg;

  public SFTFullLoader(LoaderInput loaderInput) {
    super(loaderInput);
    this.cfg = Config.getConfig();
    this.closeEm = false;
  }

  @Override
  public LoaderResult load() throws B2BLoadException, ApiException {
    LOGGER.log(Level.FINEST, "Start loading from input {0}...", loaderInput);

    if (!validate()) {
      throw new B2BLoadException("Validation failed!");
    }

    this.sfgEm = SfgEntityManager.instance();
    this.closeEm = true;
    sfgEm.startTransaction();
    for (Partner p : loaderInput.getActivePartners()) {
      loadPartner(p);
    }
    commitOrRollBack("load partners");

    sfgEm.startTransaction();
    for (TransferRule t : loaderInput.getActiveRules()) {
      loadTransferRule(t);
    }
    commitOrRollBack("load rules");

    sfgEm.startTransaction();
    for (FetchRule f : loaderInput.getActiveFetchRules()) {
      loadFetchRule(f);
    }
    commitOrRollBack("load fetch rules");
    return getResult();
  }

  @Override
  public boolean validate() throws B2BLoadException, ApiException {
    boolean valid = true;
    for (Partner p : loaderInput.getActivePartners()) {
      if (!p.isValid())
        valid = false;
      if (!p.getEndpoint().isValid()) {
        valid = false;
        error(p, "Producer connection parameters (" + p.getEndpoint().getProtocol() + ") are incomplete! Choose MBOX to use existsting producer connection.");
      }
    }
    for (TransferRule t : loaderInput.getActiveRules()) {
      if (!t.isValid())
        valid = false;
      for (DeliveryRule dr : t.getActiveDeliveryRules()) {
        if (!dr.isValid())
          valid = false;
        if (!dr.getEndpoint().isValid(dr.getLoadAction()) && duplicateMissingDeliveryParams && duplicateDeliveryParamsFor(dr) == null) {
          valid = false;
          error(dr, "Delivery parameters " + dr.getId() + " are incomplete!");
        }
      }
    }
    for (FetchRule fr : loaderInput.getActiveFetchRules()) {
      if (!fr.isValid())
        valid = false;
      if (!fr.getEndpoint().isValid()) {
        valid = false;
        error(fr, "Fetch parameters " + fr.getId() + " are incomplete!");
      }
    }
    return valid;
  }

  private FgDelivery duplicateDeliveryParamsFor(DeliveryRule dr) throws ApiException, B2BLoadException {
    return duplicateDeliveryParamsFor(dr, false);
  }

  private FgDelivery duplicateDeliveryParamsFor(DeliveryRule dr, boolean allProducers) throws ApiException, B2BLoadException {
    AbstractSfgObject dparam             = null;
    FgDelivery        clonedDeliveryRule = null;
    this.sfgEm = SfgEntityManager.instance();

    FgCustomer consumer = FgCustomer.find(dr.getConsumer(), casesensitive, sfgEm.getEntityManager());
    if (consumer == null) {
      error(dr, "Consumer " + dr.getConsumer() + " does not exist!");
      return null;
    }
    // Initial run: look for any transfer rule with pattern "DELIVERY_TEMPLATE" and use this as source for new delivery params.
    // This will allow for a different protocol (e.g. AWSS3) to be used without DATAU having a specific "MEDART" parameter.
    // This template rule is also required when NEW DATAU consumers are added to SFG (because DATAU does not supply connection details).
    if (!allProducers) {
      for (FgDelivery fgd : consumer.getFgDeliveries()) {
        if (DELIVERY_TEMPLATE_PATTERN.equals(fgd.getFgTransfer().getRcvFilepattern())) {
          LOGGER.log(Level.FINE, "Found {2} rule for {0}: {1}", new Object[] { dr.getConsumer(), fgd, DELIVERY_TEMPLATE_PATTERN });
          dr.setProtocol(fgd.getProtocol());
          dr.getEndpoint().setCloneable(true);
          return fgd;
        }
      }
    }
    for (FgDelivery fgd : consumer.getFgDeliveries()) {
      if ((!allProducers && isSamePartner(fgd.getFgTransfer().getProducer().getCustomerId(), dr.getTransferRule().getProducer()))
          && (fgd.getProtocol() == dr.getProtocol() || (dr.getProtocol() == FTProtocol.SFTP && fgd.getProtocol() == FTProtocol.MBOX))) {
        if (dparam == null) {
          dparam = fgd.getDeliveryParams();
          clonedDeliveryRule = fgd;
        } else if (!dparam.pointsToSame(fgd.getDeliveryParams())) {
          LOGGER.log(Level.WARNING, "Could not find unique set of {0} delivery parameters for transfer {1} -> {2}!",
              new Object[] { dr.getProtocol(), dr.getTransferRule().getProducer(), dr.getConsumer() });
          LOGGER.log(Level.WARNING, "Found parameters {0}", dparam);
          LOGGER.log(Level.WARNING, "...and differing {0}", fgd.getDeliveryParams());
          dparam = null;
          break;
        }
      }
    }

    if (dparam == null) {
      if (allProducers) {
        error(dr, "No (unique) delivery parameter template found for " + dr.getId() + "!");
        return null;
      } else {
        return duplicateDeliveryParamsFor(dr, true);
      }
    }
    dr.getEndpoint().setCloneable(true);
    return clonedDeliveryRule;
  }

  private boolean isSamePartner(String customerId, String producer) {
    if (customerId == null) {
      return false;
    }
    if (casesensitive) {
      return customerId.equals(producer);
    } else {
      return customerId.equalsIgnoreCase(producer);
    }
  }

  private void loadPartner(Partner p) throws B2BLoadException {
    throw new B2BLoadException("Loading partners is not implemented yet!");
    // TODO: Implement partner load
  }

  protected void loadTransferRule(TransferRule t) throws B2BLoadException {
    LOGGER.log(Level.FINEST, "Start loading transfer rule {0}...", t);
    FgCustomer producer = FgCustomer.find(t.getProducer(), casesensitive, true, sfgEm.getEntityManager());
    LOGGER.log(Level.FINER, "Producer: {0}", producer);
    if (producer == null) {
      if (t.getLoadAction() == LoadAction.SKIP) {
        result.addResult(t, Outcome.SKIPPED);
      } else {
        error(t, "Producer " + t.getProducer() + " does not exist!");
      }
      return;
    }
    changeset.add(producer);

    FgTransfer fgt = producer.getFgTransfer(t.getFilePattern());
    LOGGER.log(Level.FINER, "Transfer rule from DB: {0}", fgt);

    switch (t.getLoadAction()) {
    case SKIP:
      LOGGER.log(Level.FINE, "Skip rule: {0}", t);
      result.addResult(t, Outcome.SKIPPED);
      break;
    case NEW:
      if (fgt != null) {
        error(t, "Pattern exists: " + fgt.getShortId());
      } else {
        fgt = createTransferRule(t);
        producer.addFgTransfer(fgt);
        changeset.update(fgt);
      }
      break;
    case DELETE:
      if (fgt == null) {
        error(t, "Pattern does not exist exist: " + t.getId());
      } else {
        removeItem(fgt, changeset);
        producer.removeFgTransfer(fgt);
        sfgEm.remove(fgt);
        result.addResult(t, Outcome.DELETED);
      }
      break;
    case UPDATE:
    case ADD_CONSUMER:
    case DELETE_CONSUMER:
    case UPDATE_CONSUMER:
      if (fgt == null) {
        error(t, "Pattern does not exist exist: " + t.getId());
      } else {
        updateTransferRule(fgt, t);
      }
      break;
    default:
      error(t, "Invalid load action: " + t.getLoadAction() + "!");
      break;
    }
    changeset.update(producer);
  }

  protected void removeItem(AbstractSfgObject item, ChangeSet changeset) throws B2BLoadException {
    switch (item.getClass().getName()) {
    case "de.denkunddachte.jpa.az.FgCustomer":
      for (FgTransfer t : ((FgCustomer) item).getFgTransfers()) {
        removeItem(t, changeset);
      }
      for (FgFetchSftp f : ((FgCustomer) item).getFgFetchTransfers()) {
        removeItem(f, changeset);
      }
      for (FgFetchAwsS3 f : ((FgCustomer) item).getFgAwsS3FetchTransfers()) {
        removeItem(f, changeset);
      }
      break;
    case "de.denkunddachte.jpa.az.FgTransfer":
      for (FgDelivery d : ((FgTransfer) item).getFgDeliveries()) {
        removeItem(d, changeset);
      }
      break;
    case "de.denkunddachte.jpa.az.FgDelivery":
      removeItem(((FgDelivery) item).getDeliveryParams(), changeset);
      break;
    default:
    }
    changeset.remove(item);
  }

  private FgTransfer createTransferRule(TransferRule t) throws B2BLoadException {
    LOGGER.log(Level.FINEST, "Create transfer rule {0}...", t);
    FgTransfer fgt = new FgTransfer(t.getFilePattern());
    updateTransferRule(fgt, t);
    return fgt;
  }

  private void updateTransferRule(FgTransfer fgt, TransferRule t) throws B2BLoadException {
    LOGGER.log(Level.FINEST, "Update transfer rule {0}...", t);
    changeset.add(fgt);
    fgt.setDescription(setInfoField(fillDescription, fgt.getDescription(), t.getComment()));
    fgt.setProducerOs(t.getProducerOs());
    fgt.setDatauAck(t.isDatauAck());
    fgt.setAdditionalInfo(setInfoField(fillAdditionalInfo, fgt.getAdditionalInfo(), t.getAdditionalInfo()));
    for (DeliveryRule dr : t.getDeliveryRules()) {
      try {
        FgDelivery fgd = fgt.getFgDeliveryFor(dr.getConsumer(), !casesensitive);
        LOGGER.log(Level.FINER, "Delivery rule {0} (current={1})", new Object[] { dr.getId(), fgd });
        changeset.add(fgd);
        switch (dr.getLoadAction()) {
        case SKIP:
          LOGGER.log(Level.FINE, "Skip delivery rule: {0}", dr);
          result.addResult(dr, Outcome.SKIPPED);
          break;
        case DELETE:
        case DELETE_CONSUMER:
          if (fgd == null) {
            error(dr, "Delivery rule does not exist: " + dr.getId());
          } else {
            LOGGER.log(Level.FINE, "Delete delivery rule {0}", fgd.getShortId());
            removeItem(fgd, changeset);
            fgt.removeFgDelivery(fgd);
            fgd = null;
            result.addResult(dr, Outcome.DELETED);
          }
          break;
        case NEW:
        case ADD_CONSUMER:
          if (fgd != null) {
            error(dr, "Delivery rule exists: " + fgt.getShortId());
          } else {
            changeset.add(createFgDelivery(fgt, dr));
            result.addResult(dr, Outcome.ADDED);
          }
          break;
        case UPDATE:
        case UPDATE_CONSUMER:
          if (fgd == null) {
            error(dr, "Delivery rule does not exist: " + dr.getId());
          } else {
            LOGGER.log(Level.FINE, "Update delivery rule {0}", fgd.getShortId());
            updateFgDelivery(fgd, dr);
            result.addResult(dr, Outcome.MODIFIED);
          }
          break;
        default:
          break;
        }
        changeset.update(fgd);
      } catch (ApiException e) {
        error(dr, e);
      }
    }
    changeset.update(fgt);
  }

  private String setInfoField(FieldAction fillAction, String currentVal, String newVal) {
    switch (fillAction) {
    case KEEP:
      return StringUtils.isNullOrWhiteSpace(currentVal) ? newVal : currentVal;
    case OVERWRITE:
      return StringUtils.isNullOrWhiteSpace(newVal) ? currentVal : newVal;
    case APPEND:
      if (StringUtils.isNullOrWhiteSpace(currentVal)) {
        return newVal;
      } else if (currentVal.equals(newVal) || currentVal.endsWith(". \n" + newVal)) {
        return currentVal;
      } else {
        return currentVal.replaceAll("\\.\\s*$", "") + ". \n" + newVal;
      }
    case PREPEND:
      if (StringUtils.isNullOrWhiteSpace(currentVal)) {
        return newVal;
      } else if (currentVal.equals(newVal) || currentVal.startsWith(newVal + ". \n")) {
        return currentVal;
      } else {
        return newVal.replaceAll("\\.\\s*$", "") + ". \n" + currentVal;
      }
    }
    return null;
  }

  private FgDelivery createFgDelivery(FgTransfer transfer, DeliveryRule dr) throws B2BLoadException, ApiException {
    LOGGER.log(Level.FINEST, "Create delivery rule {0}...", dr);
    FgCustomer consumer = FgCustomer.find(dr.getConsumer(), casesensitive, true, sfgEm.getEntityManager());
    LOGGER.log(Level.FINER, "Consumer: {0}", consumer);
    if (consumer == null) {
      error(dr, "Could not create delivery rule " + dr.getId() + " (consumer " + dr.getConsumer() + " does not exist)!");
      return null;
    }
    FgDelivery fgd = new FgDelivery(transfer, consumer, dr.getDestName(), dr.getProtocol());
    transfer.addFgDelivery(fgd);
    LOGGER.log(Level.FINE, "Added delivery rule {0}", dr.getId());

    updateFgDelivery(fgd, dr);
    return fgd;
  }

  private void updateFgDelivery(FgDelivery fgd, DeliveryRule dr) throws B2BLoadException, ApiException {
    LOGGER.log(Level.FINEST, "Update delivery rule {0}...", fgd);
    fgd.setConsumerOs(dr.getConsumerOs());
    fgd.setCslmLogTenant(dr.getAglTenant());
    if (dr.getFiletype() != null) {
      FgFiletype type = FgFiletype.find(dr.getFiletype(), sfgEm.getEntityManager());
      if (type == null) {
        throw new B2BLoadException("Invalid file type: " + dr.getFiletype() + "!");
      }
      fgd.setFgFiletype(type);
    }
    fgd.setPostProcessCmd(dr.getPostCommand());
    fgd.setResolvePaths(dr.isAllowCodedPaths());
    fgd.setSndFilename(dr.getDestName());
    fgd.setTmpFilename(dr.getTempName());

    AbstractSfgObject dparam = fgd.getDeliveryParams();
    changeset.add(dparam);
    if (fgd.getProtocol() != dr.getProtocol() && dparam != null) {
      LOGGER.log(Level.FINE, "Delivery protocol changed from {0} -> {1}. Delete old delivery parameters: {2}.",
          new Object[] { fgd.getProtocol(), dr.getProtocol(), dparam });
      removeItem(fgd.getDeliveryParams(), changeset);
      sfgEm.remove(fgd.getDeliveryParams());
    }

    if (dr.getEndpoint().isCloneable()) {
      if (dparam == null) {
        FgDelivery cloneFrom = null;
        if (duplicateMissingDeliveryParams) {
          cloneFrom = duplicateDeliveryParamsFor(dr);
        }
        if (cloneFrom != null && cloneFrom.getDeliveryParams() != null) {
          if (dr.getProtocol() == FTProtocol.SFTP && cloneFrom.getProtocol() == FTProtocol.MBOX) {
            LOGGER.log(Level.INFO, "{0}: replace empty SFTP endpoint with MBOX (passive SFTP) because consumer {1} has other MBOX deliveries.",
                new Object[] { dr.getId(), dr.getConsumer() });
            dr.setEndpoint(new MboxEndpoint());
            fgd.setFgDeliveryMbox(configMboxDelivery(fgd, dr));
          } else {
            LOGGER.log(Level.INFO, "{0}: duplicate missing delivery parameters from existing rule {1}", new Object[] { dr.getId(), cloneFrom.getShortId() });
            fgd.setDeliveryParams(cloneFrom.getDeliveryParams().createCopy());
          }
        } else {
          error(dr, "Delivery parameters " + dr.getId() + " are incomplete!");
        }
        changeset.add(fgd.getDeliveryParams());
      } else {
        LOGGER.log(Level.FINE, "{0} rule {1}: keep existing delivery params: {2}", new Object[] { dr.getLoadAction(), dr, dparam });
      }
    } else {
      if (dparam != null && fgd.getProtocol() != dr.getProtocol()) {
        removeItem(dparam, changeset);
      }
      switch (dr.getProtocol()) {
      case MBOX:
        fgd.setFgDeliveryMbox(configMboxDelivery(fgd, dr));
        break;
      case SFTP:
        fgd.setFgDeliverySftp(configSftpDelivery(fgd, dr));
        break;
      case CD:
        fgd.setFgDeliveryCd(configCdDelivery(fgd, dr));
        break;
      case OFTP:
        fgd.setFgDeliveryOftp(configOftpDelivery(fgd, dr));
        break;
      case BUBA:
        fgd.setFgDeliveryBuba(configBubaDelivery(fgd, dr));
        break;
      case AWSS3:
        fgd.setFgDeliveryAwsS3(configAwsS3Delivery(fgd, dr));
        break;
      default:
        throw new NotImplementedException("Not implemented: protcol=" + dr.getProtocol());
      }
      if (fgd.getDeliveryParams().isNew()) {
        changeset.add(fgd.getDeliveryParams());
      } else {
        changeset.update(fgd.getDeliveryParams());
      }
    }
  }

  private FgDeliveryMbox configMboxDelivery(FgDelivery fgd, DeliveryRule dr) throws ApiException {
    FgDeliveryMbox dp = fgd.getFgDeliveryMbox();
    LOGGER.log(Level.FINEST, "dp={0}", dp);
    if (dp == null) {
      LOGGER.log(Level.FINEST, "Create new FgDeliveryMbox...");
      dp = new FgDeliveryMbox(fgd);
    }
    MboxEndpoint ep = (MboxEndpoint) dr.getEndpoint();
    dp.setExtractabilityCount(ep.getExtractabilityCount());
    dp.setDisposition(dr.getRemoteDisp());
    return dp;
  }

  private FgDeliverySftp configSftpDelivery(FgDelivery fgd, DeliveryRule dr) throws ApiException, B2BLoadException {
    FgDeliverySftp dp = fgd.getFgDeliverySftp();
    LOGGER.log(Level.FINEST, "dp={0}", dp);
    if (dp == null) {
      LOGGER.log(Level.FINEST, "Create new FgDeliverySftp...");
      dp = new FgDeliverySftp(fgd);
    }
    SftpEndpoint ep = (SftpEndpoint) dr.getEndpoint();
    dp.setHostname(ep.getHost());
    dp.setPort(ep.getPort());
    dp.setUsername(ep.getUsername());
    if (ep.getPrivKeyName() != null) {
      SshUserIdentityKey idkey = SshUserIdentityKey.find(ep.getPrivKeyName());
      if (idkey == null) {
        error(dr, "User identity key " + ep.getPrivKeyName() + " not found!");
      } else {
        dp.setSfgPrivkeyId(idkey.getKeyId());
      }
    } else {
      dp.setPassword(ep.getPassword());
    }
    boolean isExternal = fgd.getConsumer().getPartnerType() == FTPartnerType.EXTERNAL;
    if (isExternal) {
      dp.setClientAdapterName(cfg.getString(Props.PROP_SFTP_CLIENT_ADAPTER_EXT));
    } else {
      dp.setClientAdapterName(cfg.getString(Props.PROP_SFTP_CLIENT_ADAPTER_INT));
    }
    dp.setOverwriteDest(dr.getRemoteDisp());

    SshKnownHostKey hostKey = SshKnownHostKey.find(ep.getHost(), ep.getPort(), cfg.getBoolean(Props.PROP_GRAB_HOST_KEYS), isExternal, !dryrun);
    if (hostKey == null) {
      error(dr, "Could not get host key for " + ep.getHost() + ":" + ep.getPort() + "!");
    } else {
      dp.setSfgKnownhostkeyId(hostKey.getKeyId());
    }
    return dp;
  }

  private FgDeliveryCd configCdDelivery(FgDelivery fgd, DeliveryRule dr) throws ApiException {
    FgDeliveryCd dp = fgd.getFgDeliveryCd();
    LOGGER.log(Level.FINEST, "dp={0}", dp);
    if (dp == null) {
      LOGGER.log(Level.FINEST, "Create new FgDeliveryCd...");
      dp = new FgDeliveryCd(fgd);
    }
    CDEndpoint ep         = (CDEndpoint) dr.getEndpoint();
    boolean    isExternal = fgd.getConsumer().getPartnerType() == FTPartnerType.EXTERNAL;
    if (isExternal) {
      dp.setClientAdapterName(cfg.getString(Props.PROP_CD_CLIENT_ADAPTER_EXT));
    } else {
      dp.setClientAdapterName(cfg.getString(Props.PROP_CD_CLIENT_ADAPTER_INT));
    }
    configureCDNode(ep, false, isExternal);
    dp.setRemoteNode(ep.getNodeName());
    dp.setDisposition(dr.getRemoteDisp());
    dp.setBinaryMode(ep.isBinaryMode() ? CDBinaryMode.Yes : CDBinaryMode.No);
    dp.setLocalXlate(ep.isLocalXlate());
    dp.setLocalXlateTable(ep.getXlateTable() == null ? cfg.getString(Props.PROP_CD_XLATETAB_SND) : ep.getXlateTable());
    dp.setUsername(ep.getUsername());
    if (ep.isUseSpoe()) {
      dp.setUseProxy(true);
    } else {
      dp.setUseProxy(false);
      dp.setPassword(ep.getPassword());
    }
    dp.setSysopts(ep.getSysOpts());
    dp.setDcbopts(ep.getDcbOpts());
    dp.setRuntask(ep.getRunTask());
    dp.setRunjob(ep.getRunJob());
    dp.setUc4trigger(ep.getUc4Trigger());
    return dp;
  }

  private void configureCDNode(CDEndpoint cdep, boolean incoming, boolean isExternal) throws ApiException {
    String sfgNetmap;
    String sspNetmap;
    if (isExternal) {
      sfgNetmap = cfg.getString(Props.PROP_CD_SFGNETMAP_EXT);
      sspNetmap = cfg.getString(Props.PROP_CD_SSPNETMAP_EXT);
    } else {
      sfgNetmap = cfg.getString(Props.PROP_CD_SFGNETMAP_INT);
      sspNetmap = cfg.getString(Props.PROP_CD_SSPNETMAP_INT);
    }
    CDNetmapsHandler nmh  = CDNetmapsHandler.getInstance();
    CDNode           node = new CDNode(cdep.getNodeName(), cdep.getHost(), cdep.getPort(), cdep.isUseSecurePlus());

    nmh.addNodeToSfgNetmap(sfgNetmap, node, false);
    if (incoming) {
      nmh.addNodeToSspNetmap(sspNetmap, node, false);
    }
  }

  private FgDeliveryOftp configOftpDelivery(FgDelivery fgd, DeliveryRule dr) {
    FgDeliveryOftp dp = fgd.getFgDeliveryOftp();
    LOGGER.log(Level.FINEST, "dp={0}", dp);
    if (dp == null) {
      LOGGER.log(Level.FINEST, "Create new FgDeliveryOftp...");
      dp = new FgDeliveryOftp(fgd);
    }
    OftpEndpoint ep         = (OftpEndpoint) dr.getEndpoint();
    boolean      isExternal = fgd.getConsumer().getPartnerType() == FTPartnerType.EXTERNAL;
    if (isExternal) {
      dp.setClientAdapterName(cfg.getString(Props.PROP_OFTP_ADAPTER_EXT));
    } else {
      dp.setClientAdapterName(cfg.getString(Props.PROP_OFTP_ADAPTER_INT));
    }
    dp.setLogPartnerContract(ep.getLogPartnerContract());
    return dp;
  }

  private FgDeliveryBuba configBubaDelivery(FgDelivery fgd, DeliveryRule dr) {
    FgDeliveryBuba dp = fgd.getFgDeliveryBuba();
    LOGGER.log(Level.FINEST, "dp={0}", dp);
    if (dp == null) {
      LOGGER.log(Level.FINEST, "Create new FgDeliveryBuba...");
      dp = new FgDeliveryBuba(fgd);
    }
    BubaEndpoint ep         = (BubaEndpoint) dr.getEndpoint();
    boolean      isExternal = fgd.getConsumer().getPartnerType() == FTPartnerType.EXTERNAL;
    if (isExternal) {
      dp.setClientAdapterName(cfg.getString(Props.PROP_HTTP_CLIENT_ADAPTER_EXT));
    } else {
      dp.setClientAdapterName(cfg.getString(Props.PROP_HTTP_CLIENT_ADAPTER_INT));
    }
    dp.setRemoteHost(ep.getHost());
    dp.setRemotePort(ep.getPort());
    dp.setUsername(ep.getUsername());
    dp.setPassword(ep.getPassword());
    dp.setBasePath(ep.getBasePath());
    dp.setLoginPath(ep.getLoginPath());
    dp.setFtpoaRecipient(ep.getFtpoaRecipient());
    dp.setTls(TLS.Must);
    return dp;
  }

  private FgDeliveryAwsS3 configAwsS3Delivery(FgDelivery fgd, DeliveryRule dr) {
    FgDeliveryAwsS3 dp = fgd.getFgDeliveryAwsS3();
    LOGGER.log(Level.FINEST, "dp={0}", dp);
    if (dp == null) {
      LOGGER.log(Level.FINEST, "Create new FgDeliveryAwsS3...");
      dp = new FgDeliveryAwsS3(fgd);
    }
    AWSS3Endpoint ep = (AWSS3Endpoint) dr.getEndpoint();
    dp.setBucketName(ep.getBucketName());
    dp.setAccessKey(ep.getAccessKey());
    dp.setSecretKey(ep.getSecretKey());
    dp.setIamUserName(ep.getIamUser());
    dp.setRegion(ep.getS3region());
    dp.setEndpoint(ep.getS3endpoint().toString());
    return dp;
  }

  private void loadFetchRule(FetchRule f) throws B2BLoadException {
    throw new B2BLoadException("Loading fetch rules is not implemented yet!");
    // TODO: Implement fetch rule loading
  }

  protected void commitOrRollBack(String action) {
    if (dryrun) {
      LOGGER.log(Level.INFO, "DRY RUN: rollback {0}.", action);
      sfgEm.rollback();
    } else {
      LOGGER.log(Level.INFO, "Commit {0}.", action);
      sfgEm.commit();
    }
  }

  @Override
  public void close() throws Exception {
    if (closeEm && sfgEm != null) {
      if (sfgEm.getEntityManager().isJoinedToTransaction()) {
        LOGGER.log(Level.INFO, "Rollback current JPA transaction after exception.");
        sfgEm.rollback();
      }
      sfgEm.close();
    }
    if (!dryrun) {
      CDNetmapsHandler nmh = CDNetmapsHandler.getInstance();

      LOGGER.log(Level.INFO, "Update netmaps...");
      nmh.flushSfgNetmaps();
      nmh.flushSspNetmaps();
      // FIXME:
      // LOGGER.log(Level.INFO, "Update PROXY records in {0}.", cfg.getString(Props.PROP_SFG_CD_USERFILE));
      // CDProxyRecords.getInstance().close();
    } else {
      LOGGER.log(Level.FINER, "DRY RUN/validate: discard netmap and proxy record changes.");
    }
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }
}
