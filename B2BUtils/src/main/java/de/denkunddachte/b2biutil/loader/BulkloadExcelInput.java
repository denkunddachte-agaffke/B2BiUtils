package de.denkunddachte.b2biutil.loader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import de.denkunddachte.b2biutil.loader.model.AWSS3Endpoint;
import de.denkunddachte.b2biutil.loader.model.BubaEndpoint;
import de.denkunddachte.b2biutil.loader.model.CDEndpoint;
import de.denkunddachte.b2biutil.loader.model.DeliveryRule;
import de.denkunddachte.b2biutil.loader.model.Endpoint;
import de.denkunddachte.b2biutil.loader.model.FetchRule;
import de.denkunddachte.b2biutil.loader.model.LoadAction;
import de.denkunddachte.b2biutil.loader.model.MboxEndpoint;
import de.denkunddachte.b2biutil.loader.model.OftpEndpoint;
import de.denkunddachte.b2biutil.loader.model.Partner;
import de.denkunddachte.b2biutil.loader.model.SftpEndpoint;
import de.denkunddachte.b2biutil.loader.model.TransferRule;
import de.denkunddachte.enums.FTProtocol;
import de.denkunddachte.enums.FileDisposition;
import de.denkunddachte.enums.OSType;
import de.denkunddachte.utils.StringUtils;

public class BulkloadExcelInput extends LoaderInput {
  private static final Logger               LOGGER                   = Logger.getLogger(BulkloadExcelInput.class.getName());
  // Template header fields
  // Partner
  public static final String                XL_WSNAME_PARTNER        = "Partner";
  public static final String                XL_HDR_REC_TYPE          = "RecType";
  public static final String                XL_HDR_PARTNER_ID        = "PartnerId";
  public static final String                XL_HDR_TYPE              = "Type";
  public static final String                XL_HDR_CUST_NAME         = "Customer name";
  public static final String                XL_HDR_STREET            = "Street";
  public static final String                XL_HDR_POSTCODE          = "Postcode";
  public static final String                XL_HDR_CITY              = "City";
  public static final String                XL_HDR_COUNTRY           = "Country|Origin";
  public static final String                XL_HDR_CONTACT           = "Contact";
  public static final String                XL_HDR_PHONE             = "Phone";
  public static final String                XL_HDR_EMAIL             = "eMail";
  public static final String                XL_HDR_ROLE              = "Role";
  public static final String                XL_HDR_AGL_TENANT        = "AgL ?Tenant";
  public static final String                XL_HDR_COMMENT           = "Comment";

  // Rules
  public static final String                XL_WSNAME_RULES          = "Rules";
  public static final String                XL_HDR_PRODUCER          = "Producer";
  public static final String                XL_HDR_PRODUCEROS        = "ProducerOS";
  public static final String                XL_HDR_FILEPATTERN       = "FilePattern";
  public static final String                XL_HDR_DATAU             = "DATAU";
  public static final String                XL_HDR_CONSUMER          = "Consumer";
  public static final String                XL_HDR_CONSUMEROS        = "ConsumerOS";
  public static final String                XL_HDR_SENDNAME          = "Sendname";
  public static final String                XL_HDR_TEMPNAME          = "Tempname";
  public static final String                XL_HDR_ALLOWPATHS        = "AllowPaths";
  public static final String                XL_HDR_FILETYPE          = "Filetype";
  public static final String                XL_HDR_POSTPROCCMD       = "PostprocCmd";
  public static final String                XL_HDR_REMOTEDISP        = "RemoteDisp";
  public static final String                XL_HDR_PROTOCOL          = "Protocol";

  public static final String                XL_HDR_SFTP_HOST         = "SFTP_Host";
  public static final String                XL_HDR_SFTP_PORT         = "SFTP_Port";
  public static final String                XL_HDR_SFTP_USER         = "SFTP_User";
  public static final String                XL_HDR_SFTP_PRIVKEY      = "SFTP_PrivKey";
  public static final String                XL_HDR_SFTP_USERKEY      = "SFTP_UserKey";
  public static final String                XL_HDR_SFTP_PASSWORD     = "SFTP_Password";

  public static final String                XL_HDR_CD_NODE           = "CD_Node";
  public static final String                XL_HDR_CD_HOST           = "CD_Host";
  public static final String                XL_HDR_CD_PORT           = "CD_Port";
  public static final String                XL_HDR_CD_SECPLUS        = "CD_Secure\\+";
  public static final String                XL_HDR_CD_SPOE           = "CD_Spoe";
  public static final String                XL_HDR_CD_USER           = "CD_User";
  public static final String                XL_HDR_CD_PASSWORD       = "CD_Password";
  public static final String                XL_HDR_CD_DCB            = "CD_DCB";
  public static final String                XL_HDR_CD_SYSOPTS        = "CD_SysOpts";
  public static final String                XL_HDR_CD_BINMODE        = "CD_Binmode";
  public static final String                XL_HDR_CD_LOCALXLATE     = "CD_LocalXlate";
  public static final String                XL_HDR_CD_XLATETABLE     = "CD_XlateTable";
  public static final String                XL_HDR_CD_RUNTASK        = "CD_RunTask";
  public static final String                XL_HDR_CD_RUNJOB         = "CD_RunJob";
  public static final String                XL_HDR_CD_DSNMELD        = "CD_DSNMELD";

  public static final String                XL_HDR_OFTP_LPC          = "OFTP_LPC";

  public static final String                XL_HDR_BUBA_HOST         = "BUBA_Host";
  public static final String                XL_HDR_BUBA_PORT         = "BUBA_Port";
  public static final String                XL_HDR_BUBA_USER         = "BUBA_User";
  public static final String                XL_HDR_BUBA_PASSWORD     = "BUBA_Password";
  public static final String                XL_HDR_BUBA_BASEPATH     = "BUBA_BasePath";
  public static final String                XL_HDR_BUBA_LOGINPATH    = "BUBA_LoginPath";
  public static final String                XL_HDR_BUBA_FTPOAREC     = "BUBA_FTPOARec";

  public static final String                XL_HDR_AWSS3_ISREGEX     = "AWSS3_isRegex";
  public static final String                XL_HDR_AWSS3_BUCKET      = "AWSS3_Bucket";
  public static final String                XL_HDR_AWSS3_ACCESSKEY   = "AWSS3_AccessKey";
  public static final String                XL_HDR_AWSS3_SECRETKEY   = "AWSS3_SecretKey";
  public static final String                XL_HDR_AWSS3_IAMUSER     = "AWSS3_IAMUser";
  public static final String                XL_HDR_AWSS3_REGION      = "AWSS3_Region";
  public static final String                XL_HDR_AWSS3_ENDPOINTURI = "AWSS3_EndpointURI";

  // Fetch rules
  public static final String                XL_WSNAME_FETCHRULES     = "Fetch rules";
  public static final String                XL_HDR_SCHEDULE          = "Schedule";

  private static final Map<String, Integer> xlPHdr                   = new HashMap<>(21);
  private static final Map<String, Integer> xlRHdr                   = new HashMap<>(50);
  private static final Map<String, Integer> xlFHdr                   = new HashMap<>(17);
  static {
    Arrays.asList(XL_HDR_REC_TYPE, XL_HDR_PARTNER_ID, XL_HDR_TYPE, XL_HDR_CUST_NAME, XL_HDR_STREET, XL_HDR_POSTCODE, XL_HDR_CITY, XL_HDR_COUNTRY,
        XL_HDR_CONTACT, XL_HDR_PHONE, XL_HDR_EMAIL, XL_HDR_ROLE, XL_HDR_AGL_TENANT, XL_HDR_COMMENT, XL_HDR_PROTOCOL, XL_HDR_SFTP_USERKEY, XL_HDR_CD_NODE,
        XL_HDR_CD_HOST, XL_HDR_CD_PORT, XL_HDR_CD_SECPLUS, XL_HDR_OFTP_LPC).forEach(s -> xlPHdr.put(s, null));
    Arrays.asList(XL_HDR_REC_TYPE, XL_HDR_PRODUCER, XL_HDR_PRODUCEROS, XL_HDR_FILEPATTERN, XL_HDR_DATAU, XL_HDR_COMMENT, XL_HDR_CONSUMER, XL_HDR_CONSUMEROS,
        XL_HDR_SENDNAME, XL_HDR_TEMPNAME, XL_HDR_ALLOWPATHS, XL_HDR_FILETYPE, XL_HDR_POSTPROCCMD, XL_HDR_AGL_TENANT, XL_HDR_REMOTEDISP, XL_HDR_PROTOCOL,
        XL_HDR_SFTP_HOST, XL_HDR_SFTP_PORT, XL_HDR_SFTP_USER, XL_HDR_SFTP_PRIVKEY, XL_HDR_SFTP_PASSWORD, XL_HDR_CD_NODE, XL_HDR_CD_HOST, XL_HDR_CD_PORT,
        XL_HDR_CD_SECPLUS, XL_HDR_CD_SPOE, XL_HDR_CD_USER, XL_HDR_CD_PASSWORD, XL_HDR_CD_DCB, XL_HDR_CD_SYSOPTS, XL_HDR_CD_BINMODE, XL_HDR_CD_LOCALXLATE,
        XL_HDR_CD_XLATETABLE, XL_HDR_CD_RUNTASK, XL_HDR_CD_RUNJOB, XL_HDR_CD_DSNMELD, XL_HDR_OFTP_LPC, XL_HDR_BUBA_HOST, XL_HDR_BUBA_PORT, XL_HDR_BUBA_USER,
        XL_HDR_BUBA_PASSWORD, XL_HDR_BUBA_BASEPATH, XL_HDR_BUBA_LOGINPATH, XL_HDR_BUBA_FTPOAREC, XL_HDR_AWSS3_BUCKET, XL_HDR_AWSS3_ACCESSKEY,
        XL_HDR_AWSS3_SECRETKEY, XL_HDR_AWSS3_IAMUSER, XL_HDR_AWSS3_REGION, XL_HDR_AWSS3_ENDPOINTURI).forEach(s -> xlRHdr.put(s, null));
    Arrays.asList(XL_HDR_REC_TYPE, XL_HDR_PRODUCER, XL_HDR_FILEPATTERN, XL_HDR_SCHEDULE, XL_HDR_PROTOCOL, XL_HDR_SFTP_HOST, XL_HDR_SFTP_PORT, XL_HDR_SFTP_USER,
        XL_HDR_SFTP_PRIVKEY, XL_HDR_SFTP_PASSWORD, XL_HDR_AWSS3_ISREGEX, XL_HDR_AWSS3_BUCKET, XL_HDR_AWSS3_ACCESSKEY, XL_HDR_AWSS3_SECRETKEY,
        XL_HDR_AWSS3_IAMUSER, XL_HDR_AWSS3_REGION, XL_HDR_AWSS3_ENDPOINTURI).forEach(s -> xlFHdr.put(s, null));
  }

  private Workbook      workbook;
  private Sheet         currentSheet;
  private DataFormatter df;

  public BulkloadExcelInput() {
    super();
  }

  @Override
  public void read() throws TemplateException {
    try (InputStream is = new FileInputStream(inputFile)) {
      this.workbook = WorkbookFactory.create(is);
    } catch (IOException | EncryptedDocumentException e) {
      throw new TemplateException("Could not open file " + inputFile + "!", e);
    }
    this.df = new DataFormatter();
    if (workbook.getSheet(XL_WSNAME_PARTNER) != null) {
      readPartnerSheet(workbook.getSheet(XL_WSNAME_PARTNER));
    }
    if (workbook.getSheet(XL_WSNAME_RULES) != null) {
      readRulesSheet(workbook.getSheet(XL_WSNAME_RULES));
    }
    if (workbook.getSheet(XL_WSNAME_FETCHRULES) != null) {
      readFetchRulesSheet(workbook.getSheet(XL_WSNAME_FETCHRULES));
    }
  }

  @Override
  protected void error(String src, int row, Exception e, String... hints) throws TemplateException {
    super.error(src, row + 1, e, hints);
  }

  private void readPartnerSheet(Sheet partnerSheet) throws TemplateException {
    this.currentSheet = partnerSheet;
    int row = readHeader(currentSheet, xlPHdr);
    if (row == -1) {
      LOGGER.log(Level.FINE, "Worksheet \"{0}\" is empty. Skip.", currentSheet.getSheetName());
      return;
    }

    LOGGER.log(Level.INFO, "Process worksheet \"{0}\"...", currentSheet.getSheetName());
    // loading stops at first empty cell in first column
    while (getString(row, 0) != null) {
      String val = getString(row, 0, true);
      if (val.startsWith("#")) {
        // skip comment rows
        continue;
      }

      Partner p = new Partner(LoadAction.byCode(val), row + 1, getString(row, xlPHdr.get(XL_HDR_PARTNER_ID)),
          Partner.Type.valueOf(getString(row, xlPHdr.get(XL_HDR_TYPE), "Internal", true)), getString(row, xlPHdr.get(XL_HDR_CUST_NAME)));
      if (partners.contains(p)) {
        error(partnerSheet.getSheetName(), row, new TemplateException("Duplicate partner record: " + p));
      }
      p.setStreet(getString(row, xlPHdr.get(XL_HDR_CUST_NAME)));
      p.setPostcode(getString(row, xlPHdr.get(XL_HDR_POSTCODE)));
      p.setCity(getString(row, xlPHdr.get(XL_HDR_CITY)));
      p.setCountry(getString(row, xlPHdr.get(XL_HDR_COUNTRY)));
      p.setContact(getString(row, xlPHdr.get(XL_HDR_CONTACT)));
      p.setPhone(getString(row, xlPHdr.get(XL_HDR_PHONE)));
      p.seteMail(getString(row, xlPHdr.get(XL_HDR_EMAIL)));
      String role = getString(row, xlPHdr.get(XL_HDR_ROLE), "Both", true);
      switch (role == null ? "BOTH" : role) {
      case "PRODUCER":
        p.setProducer(true);
        break;
      case "CONSUMER":
        p.setConsumer(true);
        break;
      default:
        p.setProducer(true);
        p.setConsumer(true);
        break;
      };
      p.setAglTenant(getString(row, xlPHdr.get(XL_HDR_AGL_TENANT)));
      p.setComment(getString(row, xlPHdr.get(XL_HDR_COMMENT)));
      p.setEndpoint(getEndpoint(row, xlPHdr, true));
      p.getEndpoint().setProducerConnection(true);
      if (!p.isValid()) {
        error(partnerSheet.getSheetName(), row, new TemplateException("Not all mandatory fields are filled!"), "Partner: " + p);
      }
      partners.add(p);
      row++;
    }
  }

  private void readRulesSheet(Sheet rulesSheet) throws TemplateException {
    this.currentSheet = rulesSheet;
    int row = readHeader(currentSheet, xlRHdr);
    if (row == -1) {
      LOGGER.log(Level.FINE, "Worksheet \"{0}\" is empty. Skip.", currentSheet.getSheetName());
      return;
    }

    LOGGER.log(Level.INFO, "Process worksheet \"{0}\"...", currentSheet.getSheetName());
    // loading stops at first empty cell in first column
    while (getString(row, 0) != null) {
      String val = getString(row, 0, true);
      if (val.startsWith("#")) {
        // skip comment rows
        continue;
      }
      TransferRule t = new TransferRule(LoadAction.byCode(val), row + 1, getString(row, xlRHdr.get(XL_HDR_PRODUCER)),
          getString(row, xlRHdr.get(XL_HDR_FILEPATTERN)));
      if (rules.contains(t)) {
        LOGGER.log(Level.FINE, "Rule {0} exists. Use previously defined and add delivery records.", t);
        t = getTransferRule(t);
      } else {
        t.setProducerOs(OSType.valueOf(getString(row, xlRHdr.get(XL_HDR_PRODUCEROS))));
        t.setDatauAck("YES".equalsIgnoreCase(getString(row, xlRHdr.get(XL_HDR_DATAU), "No")));
        t.setComment(getString(row, xlRHdr.get(XL_HDR_COMMENT)));
      }
      String proto = getString(row, xlRHdr.get(XL_HDR_PROTOCOL));
      if (proto != null) {
        FTProtocol protocol = null;
        try {
          protocol = FTProtocol.valueOf(proto);
        } catch (IllegalArgumentException e) {
          error(rulesSheet.getSheetName(), row, new TemplateException("Invalid delivery protocol: " + proto + "!"));
          continue;
        }
        DeliveryRule d = new DeliveryRule(LoadAction.byCode(val), row + 1, t, getString(row, xlRHdr.get(XL_HDR_CONSUMER)),
            getString(row, xlRHdr.get(XL_HDR_SENDNAME)), protocol);
        if (t.getDeliveryRules().contains(d)) {
          error(rulesSheet.getSheetName(), row, new TemplateException("Duplicate delivery rule: " + d));
        } else {
          d.setAglTenant(getString(row, xlRHdr.get(XL_HDR_AGL_TENANT)));
          d.setAllowCodedPaths("YES".equalsIgnoreCase(getString(row, xlRHdr.get(XL_HDR_ALLOWPATHS), "No")));
          d.setConsumerOs(OSType.valueOf(getString(row, xlRHdr.get(XL_HDR_CONSUMEROS), true)));
          d.setFiletype(getString(row, xlRHdr.get(XL_HDR_FILETYPE), "default"));
          d.setPostCommand(getString(row, xlRHdr.get(XL_HDR_POSTPROCCMD)));
          d.setRemoteDisp(FileDisposition.valueOf(getString(row, xlRHdr.get(XL_HDR_REMOTEDISP), "RPL", true)));
          d.setTempName(getString(row, xlRHdr.get(XL_HDR_TEMPNAME)));
          d.setEndpoint(getEndpoint(row, xlRHdr, false));
          if (!d.isValid()) {
            error(rulesSheet.getSheetName(), row, new TemplateException("Not all mandatory fields are filled!"), "Delivery rule: " + d);
          }
          t.addDeliveryRule(d);
        }
      } else {
        error(rulesSheet.getSheetName(), row, new TemplateException("No delivery protocol defined!"));
      }
      if (!rules.contains(t)) {
        if (!t.isValid()) {
          error(rulesSheet.getSheetName(), row, new TemplateException("Not all mandatory fields are filled!"), "Transfer rule: " + t);
        }
        if (t.getLoadAction() != LoadAction.DELETE && t.getDeliveryRules().isEmpty()) {
          error(rulesSheet.getSheetName(), row, new TemplateException("No delivery rules defined!"), "Transfer rule: " + t);
        }
        rules.add(t);
      }
      row++;
    }
  }

  private void readFetchRulesSheet(Sheet fetchRulesSheet) throws TemplateException {
    this.currentSheet = fetchRulesSheet;
    int row = readHeader(currentSheet, xlFHdr);
    if (row == -1) {
      LOGGER.log(Level.FINE, "Worksheet \"{0}\" is empty. Skip.", currentSheet.getSheetName());
      return;
    }

    LOGGER.log(Level.INFO, "Process worksheet \"{0}\"...", currentSheet.getSheetName());
    // loading stops at first empty cell in first column
    while (getString(row, 0) != null) {
      String val = getString(row, 0, true);
      if (val.startsWith("#")) {
        // skip comment rows
        continue;
      }

      FetchRule r = new FetchRule(LoadAction.byCode(val), row + 1, getString(row, xlFHdr.get(XL_HDR_PRODUCER)), getString(row, xlFHdr.get(XL_HDR_FILEPATTERN)),
          getString(row, xlFHdr.get(XL_HDR_SCHEDULE)));
      if (fetchRules.contains(r)) {
        error(fetchRulesSheet.getSheetName(), row, new TemplateException("Duplicate fetch rule: " + r));
      }
      r.setRegex("YES".equalsIgnoreCase(getString(row, xlFHdr.get(XL_HDR_AWSS3_ISREGEX), "No")));
      r.setEndpoint(getEndpoint(row, xlFHdr, false));
      if (!r.isValid()) {
        error(fetchRulesSheet.getSheetName(), row, new TemplateException("Not all mandatory fields are filled!"), "Fetch rule: " + r);
      }
      fetchRules.add(r);
      row++;
    }
  }

  private Endpoint getEndpoint(int row, Map<String, Integer> hdrMap, boolean producerConnection) throws TemplateException {
    FTProtocol proto = FTProtocol.valueOf(getString(row, hdrMap.get(XL_HDR_PROTOCOL), true));
    switch (proto) {
    case SFTP:
      return getSFTPEndpoint(row, hdrMap, producerConnection);
    case CD:
      return getCDEndpoint(row, hdrMap, producerConnection);
    case MBOX:
      return new MboxEndpoint();
    case AWSS3:
      return getAWSS3Endpoint(row, hdrMap, producerConnection);
    case OFTP:
      return getOFTPEndpoint(row, hdrMap, producerConnection);
    case BUBA:
      return getBUBAEndpoint(row, hdrMap, producerConnection);
    default:
      throw new TemplateException("Unsupported protocol: " + proto);
    }
  }

  private Endpoint getSFTPEndpoint(int row, Map<String, Integer> hdrMap, boolean producerConnection) {
    SftpEndpoint ep;
    if (producerConnection) {
      ep = new SftpEndpoint(null, 0, getString(row, hdrMap.get(XL_HDR_PARTNER_ID)));
      ep.setPublicKey(getString(row, hdrMap.get(XL_HDR_SFTP_USERKEY)));
      ep.setProducerConnection(true);
    } else {
      ep = new SftpEndpoint(getString(row, hdrMap.get(XL_HDR_SFTP_HOST)), Integer.valueOf(getString(row, hdrMap.get(XL_HDR_SFTP_PORT), "22")),
          getString(row, hdrMap.get(XL_HDR_SFTP_USER)));
      ep.setPassword(getString(row, hdrMap.get(XL_HDR_SFTP_PASSWORD)));
      ep.setPrivKeyName(getString(row, hdrMap.get(XL_HDR_SFTP_PRIVKEY)));
    }
    return ep;
  }

  private Endpoint getCDEndpoint(int row, Map<String, Integer> hdrMap, boolean producerConnection) {
    CDEndpoint ep;
    if (producerConnection) {
      ep = new CDEndpoint(getString(row, hdrMap.get(XL_HDR_CD_NODE)), getString(row, hdrMap.get(XL_HDR_CD_USER)), true);
      ep.setProducerConnection(true);
    } else {
      ep = new CDEndpoint(getString(row, hdrMap.get(XL_HDR_CD_NODE)), getString(row, hdrMap.get(XL_HDR_CD_USER)),
          "YES".equalsIgnoreCase(getString(row, hdrMap.get(XL_HDR_CD_SPOE), "Yes")));
      if (!ep.isUseSpoe()) {
        ep.setPassword(getString(row, hdrMap.get(XL_HDR_CD_PASSWORD)));
      }
      ep.setBinaryMode("YES".equalsIgnoreCase(getString(row, hdrMap.get(XL_HDR_CD_BINMODE), "Yes")));
      ep.setDcbOpts(getString(row, hdrMap.get(XL_HDR_CD_DCB)));
      ep.setLocalXlate("YES".equalsIgnoreCase(getString(row, hdrMap.get(XL_HDR_CD_LOCALXLATE), "No")));
      ep.setRunJob(getString(row, hdrMap.get(XL_HDR_CD_RUNJOB)));
      ep.setRunTask(getString(row, hdrMap.get(XL_HDR_CD_RUNTASK)));
      ep.setSysOpts(getString(row, hdrMap.get(XL_HDR_CD_SYSOPTS)));
      ep.setUc4Trigger(getString(row, hdrMap.get(XL_HDR_CD_DSNMELD)));
      ep.setXlateTable(getString(row, hdrMap.get(XL_HDR_CD_XLATETABLE)));
    }
    ep.setHost(getString(row, hdrMap.get(XL_HDR_CD_HOST)));
    ep.setPort(Integer.valueOf(getString(row, hdrMap.get(XL_HDR_CD_PORT), "1364")));
    ep.setUseSecurePlus("YES".equalsIgnoreCase(getString(row, hdrMap.get(XL_HDR_CD_SECPLUS), "No", true)));
    return ep;
  }

  private Endpoint getAWSS3Endpoint(int row, Map<String, Integer> hdrMap, boolean producerConnection) throws TemplateException {
    if (producerConnection) {
      throw new TemplateException("AWSS3 is not a valid producer connection protocol!");
    }
    AWSS3Endpoint ep = new AWSS3Endpoint(getString(row, hdrMap.get(XL_HDR_AWSS3_BUCKET)), getString(row, hdrMap.get(XL_HDR_AWSS3_ACCESSKEY)),
        getString(row, hdrMap.get(XL_HDR_AWSS3_SECRETKEY)));
    ep.setIamUser(getString(row, hdrMap.get(XL_HDR_AWSS3_IAMUSER)));
    String awsUri = getString(row, hdrMap.get(XL_HDR_AWSS3_ENDPOINTURI));
    if (awsUri != null) {
      try {
        ep.setS3endpoint(new URI(awsUri));
      } catch (URISyntaxException e) {
        throw new TemplateException(e);
      }
    }
    ep.setS3region(getString(row, hdrMap.get(XL_HDR_AWSS3_REGION)));
    return ep;
  }

  private Endpoint getOFTPEndpoint(int row, Map<String, Integer> hdrMap, boolean producerConnection) {
    OftpEndpoint ep = new OftpEndpoint(getString(row, hdrMap.get(XL_HDR_OFTP_LPC)));
    ep.setProducerConnection(producerConnection);
    return ep;
  }

  private Endpoint getBUBAEndpoint(int row, Map<String, Integer> hdrMap, boolean producerConnection) throws TemplateException {
    if (producerConnection) {
      throw new TemplateException("BUBA is not a valid producer connection protocol!");
    }
    BubaEndpoint ep = new BubaEndpoint(getString(row, hdrMap.get(XL_HDR_BUBA_HOST)), Integer.valueOf(getString(row, hdrMap.get(XL_HDR_BUBA_PORT), "443")),
        getString(row, hdrMap.get(XL_HDR_BUBA_USER)), getString(row, hdrMap.get(XL_HDR_BUBA_PASSWORD)), getString(row, hdrMap.get(XL_HDR_BUBA_FTPOAREC)));
    ep.setBasePath(getString(row, hdrMap.get(XL_HDR_BUBA_BASEPATH), "/FT/"));
    ep.setLoginPath(getString(row, hdrMap.get(XL_HDR_BUBA_BASEPATH), "/pkmslogin.form"));
    return ep;
  }

  private String getString(int row, int col) {
    return getString(row, col, null, false);
  }

  private String getString(int row, int col, String defaultVal) {
    return getString(row, col, defaultVal, false);
  }

  private String getString(int row, int col, boolean toUpper) {
    return getString(row, col, null, toUpper);
  }

  private String getString(int row, int col, String defaultVal, boolean toUpper) {
    if (currentSheet == null) {
      throw new IllegalStateException("Current worksheet not set!");
    }
    String val = null;
    if (currentSheet.getRow(row) != null && currentSheet.getRow(row).getCell(col) != null) {
      val = df.formatCellValue(currentSheet.getRow(row).getCell(col));
      val = val.isEmpty() ? defaultVal : val;
    }
    if (val != null) {
      return toUpper ? val.toUpperCase() : val;
    }
    return val;
  }

  private int readHeader(Sheet sheet, Map<String, Integer> xlHdrMap) throws TemplateException {
    int row = sheet.getFirstRowNum();
    int hdrRow = -1;

    // search row with "RecType" in first column
    while (sheet.getRow(row) == null || (sheet.getRow(row).getCell(0) != null && !sheet.getRow(row).getCell(0).getStringCellValue().matches(XL_HDR_REC_TYPE))) {
      if (++row > sheet.getLastRowNum()) {
        throw new TemplateException("Header row not found in sheet " + sheet.getSheetName() + "!");
      }
    }
    hdrRow = row;
    LOGGER.log(Level.FINER, "Header row: {0}", hdrRow);

    // fill header map with column indexes:
    for (Cell c : sheet.getRow(row)) {
      if (StringUtils.isNullOrWhiteSpace(c.toString())) {
        continue;
      }
      String hdr = c.toString().trim();
      for (Entry<String, Integer> e : xlHdrMap.entrySet()) {
        if (hdr.matches("(?i)" + e.getKey())) {
          xlHdrMap.put(e.getKey(), c.getColumnIndex());
          break;
        }
      }
    }

    // check if any header column is missing:
    if (xlHdrMap.values().stream().anyMatch(Objects::isNull)) {
      throw new TemplateException(
          "Columns " + xlHdrMap.entrySet().stream().filter(e -> e.getValue() == null).map(Map.Entry::getKey).collect(Collectors.toList()) + " not found!");
    }

    // skip info rows (rows that have a background color):
    while (sheet.getRow(row) == null
        || (sheet.getRow(row).getCell(0) != null && sheet.getRow(row).getCell(0).getCellStyle().getFillPattern() != FillPatternType.NO_FILL)) {
      if (++row > sheet.getLastRowNum()) {
        row = -1;
        break;
      }
    }
    // check if first data row (first row below info rows) is empty
    if (row > 0 && sheet.getRow(row).getCell(0) == null || sheet.getRow(row).getCell(0).toString().isEmpty())
      row = -1;
    LOGGER.log(Level.FINER, "First data row: {0}", row);
    return row;
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  public void close() throws Exception {
    // if (this.workbook != null)
    // this.workbook.close();
  }
}
