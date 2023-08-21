package de.denkunddachte.b2biutil.loader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import de.denkunddachte.b2biutil.loader.model.CDEndpoint;
import de.denkunddachte.b2biutil.loader.model.DeliveryRule;
import de.denkunddachte.b2biutil.loader.model.Endpoint;
import de.denkunddachte.b2biutil.loader.model.LoadAction;
import de.denkunddachte.b2biutil.loader.model.MboxEndpoint;
import de.denkunddachte.b2biutil.loader.model.OftpEndpoint;
import de.denkunddachte.b2biutil.loader.model.SftpEndpoint;
import de.denkunddachte.b2biutil.loader.model.TransferRule;
import de.denkunddachte.enums.FTProtocol;
import de.denkunddachte.enums.FileDisposition;
import de.denkunddachte.enums.OSType;
import de.denkunddachte.enums.RecordFormat;

/**
 * SFT Full loader input implementation for "DATAU Meldesatz" format.
 * 
 * Header:
 *<pre>
 * Idx Field       Type        Value            Description
 * 0   RECTYPE     CHAR (1)    INIT  ('V')      V=Vorsatz
 * 1   DATE        CHAR (8)    INIT  (' ')      YYYYMMDD
 * 2   REFNUM      CHAR (8)    INIT  (' ')      sequence no., left padded with 0
 * 3   VERSION     CHAR (4)    INIT  ('1.0')    Version
 *</pre>
 *
 * Outgoing to external partner:
 *<pre>
 * Idx Field       Type        Value            Description
 * 0   RECTYPE     CHAR (1)    INIT  ('M')      M=Meldesatz
 * 1   CHANGETYPE  CHAR (1)    INIT  (' ')      N=New, U=Update, D=Deletion
 * 2   RICHTUNG    CHAR (1)    INIT  (' ')      A=Ausgang
 * 3   INT_PARTNER CHAR (4)    INIT  ('AZS_')   Constant 'AZS_'
 * 4   INT_PARTNER CHAR (6)    INIT  ('GDA0  ') Currently: GDA0 only
 * 5   EXT_PARTNER CHAR (10)   INIT  (' ')      External partner id (length=20 if record length is 201 or 226)
 * 6   TNIDOM      CHAR (6)    INIT  (' ')      
 * 7   MEDART      CHAR (3)    INIT  (' ')      SFA=SFTP, OFA=OFTP
 * 8   EMPFNAME    CHAR (44)   INIT  (' ')      receive pattern
 * 9   SENDNAME    CHAR (44)   INIT  (' ')      send filename
 * 10  PFAD        CHAR (40)   INIT  (' ')      remote path
 * 11  KOMPRIM     CHAR (5)    INIT  (' ')      FLAM, ZIP
 * 12  VERSCHL     CHAR (5)    INIT  (' ')      PGP, FLAM
 * 13  KONVERT     CHAR (5)    INIT  (' ')      empty
 * 14  RECFM       CHAR (3)    INIT  (' ')      DCB
 * 15  LRECL       PIC'ZZZZ9'  INIT  (0)        DCB
 * 16  BLKSIZE     PIC'ZZZZ9'  INIT  (0)        DCB
 * 17  ARBGEB      CHAR (3)    INIT  (' ')      
 * 18  AUFTRAGG    CHAR (15)   INIT  (' ')      length=0 if record length is 191 or 201
 * 19  KOSTST      CHAR (25)   INIT  (' ')      length=0 if record length is 191 or 201
 *</pre>
 *
 * Incoming to external partner:
 *<pre>
 * Idx Field       Type        Value            Description
 * 0   RECTYPE     CHAR (1)    INIT  ('M')      M=Meldesatz
 * 1   CHANGETYPE  CHAR (1)    INIT  (' ')      N=New, U=Update, D=Deletion
 * 2   RICHTUNG    CHAR (1)    INIT  (' ')      E=Eingang
 * 3   INT_PARTNER CHAR (4)    INIT  ('AZS_')   Constant 'AZS_'
 * 4   INT_PARTNER CHAR (6)    INIT  ('GDA0  ') Currently: GDA0 only
 * 5   EXT_PARTNER CHAR (10)   INIT  (' ')      External partner id (length=20 if record length is 201 or 226)
 * 6   TNIDOM      CHAR (6)    INIT  (' ')      
 * 7   MEDART      CHAR (3)    INIT  (' ')      SFE=SFTP, OFE=OFTP
 * 8   EMPFNAME    CHAR (44)   INIT  (' ')      receive pattern
 * 9   SENDNAME    CHAR (44)   INIT  (' ')      send filename
 * 10  PFAD        CHAR (40)   INIT  (' ')      remote path
 * 11  KOMPRIM     CHAR (5)    INIT  (' ')      FLAM, ZIP
 * 12  VERSCHL     CHAR (5)    INIT  (' ')      PGP, FLAM
 * 13  KONVERT     CHAR (5)    INIT  (' ')      empty
 * 14  RECFM       CHAR (3)    INIT  (' ')      DCB
 * 15  LRECL       PIC'ZZZZ9'  INIT  (0)        DCB
 * 16  BLKSIZE     PIC'ZZZZ9'  INIT  (0)        DCB
 * 17  ARBGEB      CHAR (3)    INIT  (' ')      
 * 18  AUFTRAGG    CHAR (15)   INIT  (' ')      length=0 if record length is 191 or 201
 * 19  KOSTST      CHAR (25)   INIT  (' ')      length=0 if record length is 191 or 201
 *</pre>
 * Trailer record:
 *<pre>
 * Idx Field       Type        Value            Description
 * 0   RECTYPE     CHAR (1)    INIT  ('V')      E=Endsatz
 * 1   RECCOUNT    CHAR (8)    INIT  (' ')      number of records N, U, D, left padded with 0
 *</pre>

 * @author chef
 *
 */
public class DatauMeldesatzInput extends LoaderInput {
  private static final Logger LOGGER      = Logger.getLogger(DatauMeldesatzInput.class.getName());
  private final DateFormat    fmtYYYYMMDD = new SimpleDateFormat("yyyyMMdd");
  private final DateFormat    fmtIsoDate  = new SimpleDateFormat("yyyy-MM-dd");

  public enum RecordType {
    V, M, E
  }

  private Date   batchDate;
  private long   refNum;
  private String version;

  // field parser state:
  private int    offset;
  String         r;
  String         f;

  public DatauMeldesatzInput() {
    super();
  }

  @Override
  public void read() throws TemplateException {
    valid = true;
    Integer records = null;
    try (BufferedReader rd = new BufferedReader(new FileReader(inputFile))) {
      int line = 0;
      while ((r = rd.readLine()) != null) {
        offset = 0;
        try {
          RecordType recType = RecordType.valueOf(getNext(1));
          if (++line == 1 && recType != RecordType.V) {
            throw new TemplateException("Invalid file: file must begin with a 'V' record!");
          }
          switch (recType) {
          case V:
            batchDate = fmtYYYYMMDD.parse(getNext(8));
            refNum = Long.parseLong(getNext(8));
            version = getRest();
            break;
          case M:
            rules.add(parseRule(line));
            break;
          case E:
            records = Integer.parseInt(getNext(8));
            if (records != rules.size()) {
              error(line, "Number of records mismatch! Expect " + records + ", got " + rules.size() + " records.");
            }
            break;
          }
        } catch (ParseException | IllegalArgumentException e) {
          valid = false;
          error(line, "Could not parse field '" + f + "': " + e.getMessage());
        }
      }
      if (records == null) {
        error(line, "No 'E' record found!");
      }
    } catch (IOException e) {
      throw new TemplateException("Could not open file " + inputFile + "!", e);
    }
  }

  private TransferRule parseRule(int line) throws TemplateException {
    TransferRule t           = null;
    LoadAction   action      = LoadAction.byCode(getNext(1));
    boolean      outgoing    = "A".equals(getNext(1));
    String       intPartner  = getPartnerId(getNext(10));
    String       extPartner  = getPartnerId(getNext(r.length() == 216 || r.length() == 201 || r.length() == 217 ? 10 : 20));
    String       producer    = (outgoing ? intPartner : extPartner);
    String       consumer    = (outgoing ? extPartner : intPartner);
    String       tnidom      = getNext(6);
    String       medart      = getNext(3);
    String       filepattern = getNext(44);
    String       destname    = getNext(44);
    String       path        = getNext(40);

    t = new TransferRule(action, line, producer, filepattern);
    t.setDatauAck(outgoing); // DATAU ACK only for outgoing transfers
    t.setAdditionalInfo(String.format("DATAU Meldesatz refNum=%d, date=%s, line=%d, TNIDOM=%s.", refNum, fmtIsoDate.format(batchDate), line, tnidom));
    Endpoint ep = null;
    switch (medart) {
    case "SFA":
      ep = new SftpEndpoint(null, 22, null);
      t.setProducerOs(OSType.MVS);
      break;
    case "OFA":
      ep = new OftpEndpoint(null);
      t.setProducerOs(OSType.MVS);
      break;
    case "SFE":
    case "OFE":
      ep = new CDEndpoint(intPartner, "maestro", true);
      break;
    default:
      error(line, "MEDART field " + medart + " not mapped to protocol!");
      ep = new MboxEndpoint();
      break;
    }
    if (!path.isEmpty()) {
      if (medart.charAt(2) == 'E') {
        t.setRcvPath(path);
      } else {
        destname = path + (path.endsWith("/") ? "" : "/") + destname;
      }
    }

    String compression = getNext(5);
    String encryption  = getNext(5);
    getNext(5); // reserve

    RecordFormat recForm = RecordFormat.valueOf(getNext(3, "VB"));
    int          recSize = Integer.parseInt(getNext(5, "0"));
    int          blkSize = Integer.parseInt(getNext(5, "0"));
    if (r.length() < 216) {
      t.setComment(String.format("DATAU: AG=%s", getNext(3)));
    } else {
      t.setComment(String.format("DATAU: AG=%s, OE=%s, Kostenstelle=%s", getNext(3), getNext(15), getRest()));
    }
    if (ep.getProtocol() == FTProtocol.CD && consumer.matches("GD[AB]0")) {
      CDEndpoint cdep = (CDEndpoint) ep;
      cdep.setBinaryMode(true);
      cdep.setDcbOpts(String.format("DSORG=PS,LRECL=%d,BLKSIZE=%d,RECFM=%s", recSize, blkSize, recForm));
      destname += "(+1)";
    }
    DeliveryRule dr = new DeliveryRule(action, line, t, consumer, destname, ep);
    if (ep.getProtocol() == FTProtocol.CD) {
      dr.setConsumerOs(OSType.MVS);
      dr.setRemoteDisp(FileDisposition.NEW);
    } else if (destname.indexOf('\\') > -1) {
      dr.setConsumerOs(OSType.WINDOWS);
    } else {
      dr.setConsumerOs(OSType.UNIX);
    }

    switch ((compression + "/" + encryption).toUpperCase()) {
    case "ZIP/":
      dr.setFiletype("zip");
      break;
    case "FLAM/":
      dr.setFiletype("ebsfix256");
      break;
    case "/":
      dr.setFiletype("default");
      break;
    default:
      error(line, "Compression/encryption combination " + compression + "/" + encryption + " not mapped to filetype!");
      break;
    }
    t.addDeliveryRule(dr);

    return t;
  }

  private String getRest() {
    return r.substring(offset);
  }

  private String getNext(int length) throws TemplateException {
    return getNext(length, null).trim();
  }

  private String getNext(int length, String defaultVal) throws TemplateException {
    if (r == null || offset + length > r.length()) {
      throw new TemplateException(
          String.format("Could not get field [%d,%d] from record! Record too short (%s).", offset, offset + length, (r != null ? r.length() : "null")));
    }
    f = r.substring(offset, offset += length).trim();
    if (f.isEmpty() && defaultVal != null) {
      return defaultVal;
    }
    return f;
  }

  public String getVersion() {
    return version;
  }

  public Date getBatchDate() {
    return batchDate;
  }

  public long getRefNum() {
    return refNum;
  }

  protected void error(int line, String msg, String... hints) throws TemplateException {
    super.error(null, line, new TemplateException(msg), hints);
  }

  private String getPartnerId(String partnerField) {
    if ("AZS_GDA0".equals(partnerField)) {
      return "GDA0";
    } else if ("AZS_GDB0".equals(partnerField)) {
      return "GDB0";
    } else if (partnerField.matches("AZS[_-][A-Z]+.*")) {
      return partnerField.substring(4);
    } else {
      return partnerField;
    }
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

  @Override
  public void close() throws Exception {
    // nothing to do
  }
}
