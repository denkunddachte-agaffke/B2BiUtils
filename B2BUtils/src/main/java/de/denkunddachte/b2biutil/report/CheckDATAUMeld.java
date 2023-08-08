package de.denkunddachte.b2biutil.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import de.denkunddachte.util.ApiConfig;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.utils.DataSourcePools;
import de.denkunddachte.utils.FileUtil;
import de.denkunddachte.utils.StringUtils;

//@formatter:off
/**
0	3 RECTYPE  CHAR (1)  INIT  ('M'),    
1	3 CHANGET  CHAR (1)  INIT  (' '),    LEER
2	3 RICHT    CHAR (1)  INIT  (' '),    A=Ausgang, E=Eingang
3   4 AZS_
4	3 INT_PART CHAR (10) INIT  (' '),    AZS_GDA0 -> GDA0
5	3 EXT_PART CHAR (10) INIT  (' '),    KPK005
6	3 TNIDOM   CHAR (6)  INIT  (' '),    
7	3 MEDART   CHAR (3)  INIT  (' '),    SFA = SFTP ausg
8	3 EMPFNAME CHAR (44) INIT  (' '),    
9	3 SENDNAME CHAR (44) INIT  (' '),    
10	3 PFAD     CHAR (40) INIT  (' '),    
11	3 KOMPRI   CHAR (5)  INIT  (' '),    
12	3 VERSCHL  CHAR (5)  INIT  (' '),    
13	3 KONVERT  CHAR (5)  INIT  (' '),    
14	3 RECFM    CHAR (3)  INIT  (' '),    
15	3 LRECL    PIC'ZZZZ9'INIT  (0),      
16	3 BLKSIZE  PIC'ZZZZ9'INIT  (0),      
17	3 ARBGEB   CHAR (3)  INIT  (' '),    
18	3 AUFTRAGG CHAR (15) INIT  (' '),    
19	3 KOSTST   CHAR (25) INIT  (' ');    

M AAZS_GDA0  KPK005    KPK005SFAP.EDIGD.AKPK005.PABSPAEN.DRECOM             ABS_Adress_PROD_[TS]                                                                               VB  4056    0EDI               1079910419
 * @author izse364 &lt;EXTERN.GAFFKE_ANDREAS@allianz.de&gt;
 *
 */
//@formatter:on
public class CheckDATAUMeld {

  private static final String                       SQL_SEL_RULE        = "SELECT FG_TRANS_ID, FG_DELIVERY_ID, CONSUMER, RCV_FILEPATTERN, SND_FILENAME, PROTOCOL, "
      + "HOSTNAME, USERNAME, DISPOSITION, DATAU_ACK, CONVERT(NVARCHAR(MAX), T_LAST_ACTIVE, 21) T_LAST_ACTIVE, FILETYPE "
      + "FROM AZ_FG_ALL_TRANSFERS_V WHERE PRODUCER=? AND (RCV_FILEPATTERN=? OR RCV_FILEPATTERN=?)";
  private static final String                       SQL_SEL_DPARAM      = "SELECT * FROM AZ_FG_DELIVERY_CD P WHERE P.FG_DELIVERY_ID=?";
  private static final String                       SQL_SEL_DATAU_RULES = "SELECT PRODUCER, RCV_FILEPATTERN, CONSUMER, SND_FILENAME, CONVERT(NVARCHAR(MAX), "
      + "T_LAST_ACTIVE, 21) T_LAST_ACTIVE FROM AZ_FG_ALL_TRANSFERS_V WHERE DATAU_ACK=1";
  private static final Map<String, TransferPattern> DATAU_PATTERNS      = new HashMap<>(250);

  static String createDatauFilename14(String filename) {
    String[] q = filename.split("\\.");
    String q2 = "  ";
    String q3 = "  ";
    if (q.length > 3)
      q3 = q[3];
    if (q.length > 2)
      q2 = q[2];
    return String.format("%-6.6s%s", q2.substring(1, (q2.length() >= 7 ? 7 : q2.length())) + "______", q3.substring(0, (q3.length() >= 8 ? 8 : q3.length())))
        .trim();
  }

  static void reportMissingPattern(PrintStream out, TransferPattern tp, String msg) {
    out.println("###### DATAU file pattern missing #####");
    if (msg != null && !msg.isEmpty()) {
      out.println(msg);
      out.println("Please create file pattern or edit existing pattern for short filename:");
    } else {
      out.println("Please create file pattern:");
    }
    out.print(tp.toLongString());
    out.println("#######################################");
    out.println();
  }

  static void reportMismatch(PrintStream out, TransferPattern tp, String msg, String lastActive) {
    out.println("###### DATAU file pattern mismatch #####");
    out.println(msg);
    out.println("Please check file pattern and modify only after confirmation of producer AND consumer:");
    out.print(tp.toLongString());
    out.println(String.format("%-30s: %s", "Last active", lastActive));
    out.println("#######################################");
    out.println();
  }

  static void reportDeletion(PrintStream out, String producer, String rcvPattern, String consumer, String sndFilename, String lastActive) {
    out.println("###### DATAU file pattern deleted #####");
    out.println(
        "File pattern on SFG is not found in DATAU. Please check file pattern and delete or remove DATAU flag only after confirmation of producer AND consumer:");
    out.println(String.format("%-30s: %s", "Producer", producer));
    out.println(String.format("%-30s: %s", "Receive pattern", rcvPattern));
    out.println(String.format("%-30s: %s", "Consumer", consumer));
    out.println(String.format("%-30s: %s", "Send filename", sndFilename));
    out.println(String.format("%-30s: %s", "Last active", lastActive));
    out.println("#######################################");
    out.println();

  }

  public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, IllegalArgumentException, ApiException {
    if (args.length < 2) {
      System.err.println("usage: <DATAU meld file> <report file>");
      System.exit(1);
    }
    File in = new File(args[0]);
    File report = new File(args[1]);
    ApiConfig cfg = ApiConfig.getInstance();
    int records = 0;
    int foundRule = 0;
    int shortnameonly = 0;
    int mismatch = 0;
    int missing = 0;
    int delete = 0;
    int lineno = 0;
    System.out.format("Process file %s [%s]%n", in, (new Date()).toString());
    try (RandomAccessFile raf = new RandomAccessFile(in, "r"); PrintStream out = new PrintStream(new FileOutputStream(report, false))) {
      DataSource ds = DataSourcePools.getPooledDataSource(cfg.getDbDriver(), cfg.getDbUrl(), cfg.getDbUser(), cfg.getDbPassword(), "sfgdb");
      try (Connection con = ds.getConnection();
          PreparedStatement pstmt = con.prepareStatement(SQL_SEL_RULE, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
          PreparedStatement pstmt2 = con.prepareStatement(SQL_SEL_DPARAM);
          PreparedStatement pstmt3 = con.prepareStatement(SQL_SEL_DATAU_RULES)) {
        String line;
        while ((line = raf.readLine()) != null) {
          lineno++;
          if (line.length() < 8 || line.charAt(0) != 'M' || line.charAt(1) == 'D')
            continue;
          records++;
          TransferPattern tp = new TransferPattern(lineno, line);
          DATAU_PATTERNS.put((tp.getProducer() + ":" + tp.getRcvPattern()).toUpperCase(), tp);
          pstmt.setString(1, tp.getProducer());
          pstmt.setString(2, tp.getRcvPattern());
          pstmt.setString(3, tp.getShortName());
          try (ResultSet rs = pstmt.executeQuery()) {
            String consumer = null;
            String sndFilename = null;
            boolean found = false;
            String msg = "NO RULE FOUND IN SFG!";
            while (rs.next()) {
              consumer = rs.getString("CONSUMER");
              sndFilename = FileUtil.basename(rs.getString("SND_FILENAME")).replaceAll("\\(\\+\\d\\)$", "");
              System.out.format("Found [%-10s] %-44s: [%-10s] %-84s%n", tp.getProducer(), rs.getString("RCV_FILEPATTERN"), consumer,
                  rs.getString("SND_FILENAME"));
              if (tp.getRcvPattern().equals(rs.getString("RCV_FILEPATTERN"))) {
                foundRule++;
                found = true;
                msg = "";
                break;
              } else {
                msg = "HAVE RULE FOR SHORTNAME (" + tp.getShortName() + ") ONLY!";
              }
              // preserve rs:
              if (rs.isLast())
                break;
            }
            if (!found) {
              if (consumer != null)
                shortnameonly++;
              else
                missing++;
              reportMissingPattern(out, tp, msg);

            } else {
              StringBuilder sb = new StringBuilder();
              if (tp.isOutgoing() && !rs.getBoolean("DATAU_ACK")) {
                sb.append(String.format("DATAU flag MISSING!%n"));
              }
              if (!tp.getConsumer().equalsIgnoreCase(consumer)) {
                sb.append(String.format("CONSUMER MISMATCH: GOT: %s, expect: %s!%n", consumer, tp.getConsumer()));
              }
              if (!tp.getSndFilename().equals(sndFilename)) {
                sb.append(String.format("SEND FILENAME MISMATCH: GOT: %s, expect: %s!%n", sndFilename, tp.getSndFilename()));
              }
              if (!StringUtils.isNullOrWhiteSpace(tp.getSndPath())) {
                String path = FileUtil.dirname(rs.getString("SND_FILENAME"));
                if (!path.equals(tp.getSndPath()) && !path.equals("/" + tp.getSndPath())) {
                  sb.append(String.format("SEND PATH MISMATCH: GOT: %s, expect: %s!%n", path, tp.getSndPath()));
                }
              }
              if ("ZIP".equals(tp.getCompress())) {
                if (tp.isOutgoing() && !"gzip".equals(rs.getString("FILETYPE")) && !"zip".equals(rs.getString("FILETYPE"))) {
                  sb.append(String.format("COMPRESSION %s REQUIRES FILETYPE %s: GOT: %s!%n", tp.getCompress(), "gzip or zip", rs.getString("FILETYPE")));
                }
                if (!tp.isOutgoing() && !"ungzip".equals(rs.getString("FILETYPE")) && !"entzip".equals(rs.getString("FILETYPE"))) {
                  sb.append(String.format("COMPRESSION %s REQUIRES FILETYPE %s: GOT: %s!%n", tp.getCompress(), "ungzip or entzip", rs.getString("FILETYPE")));
                }

              }
              if (!tp.isOutgoing()) {
                pstmt2.setInt(1, rs.getInt("FG_DELIVERY_ID"));
                found = false;
                try (ResultSet rs2 = pstmt2.executeQuery()) {
                  String dcbOpts = "";
                  if (rs2.next()) {
                    found = true;
                    dcbOpts = rs2.getString("DCBOPTS");
                  }
                  if (!found) {
                    sb.append(String.format("NO DELIVERY PARAMS!%n"));
                  } else {
                    if (!tp.getRecFm().isEmpty() && !dcbOpts.contains("RECFM=" + tp.getRecFm())) {
                      sb.append(String.format("DCBOPTS RECFM MISMATCH: DCBOPTS: %s, expect: RECFM=%s!%n", dcbOpts, tp.getRecFm()));
                    }
                    if (tp.getRecSize() > 0 && !dcbOpts.contains("LRECL=" + tp.getRecSize())) {
                      sb.append(String.format("DCBOPTS LRECL MISMATCH: DCBOPTS: %s, expect: LRECL=%s!%n", dcbOpts, tp.getRecSize()));
                    }
                    if (tp.getBlockSize() > 0 && !dcbOpts.contains("BLKSIZE=" + tp.getBlockSize())) {
                      sb.append(String.format("DCBOPTS BLKSIZE MISMATCH: DCBOPTS: %s, expect: BLKSIZE=%s!%n", dcbOpts, tp.getBlockSize()));
                    }
                  }
                }
              }
              msg = sb.toString();
              if (!msg.isEmpty()) {
                mismatch++;
                reportMismatch(out, tp, msg, rs.getString("T_LAST_ACTIVE"));
              }
            }
            if (!msg.isEmpty()) {
              System.err.format("Line %04d: [%-8s] %-35s -> [%-8s] %-35s: NOT OK:%n%s%n", lineno, tp.getProducer(), tp.getRcvPattern(), tp.getConsumer(),
                  tp.getSndFilename(), msg);
            }
          }

        }// while read
        System.out.println();
        System.out.println("Find SFG patterns not in DATAU...");
        try (ResultSet rs3 = pstmt3.executeQuery()) {
          while (rs3.next()) {
            String key = rs3.getString("PRODUCER") + ":" + rs3.getString("RCV_FILEPATTERN");
            System.out.format("Found [%-10s] %-44s: [%-10s] %-60s: ", rs3.getString("PRODUCER"), rs3.getString("RCV_FILEPATTERN"), rs3.getString("CONSUMER"),
                rs3.getString("SND_FILENAME"));
            if (DATAU_PATTERNS.containsKey(key.toUpperCase())) {
              TransferPattern p = DATAU_PATTERNS.get(key.toUpperCase());
              if (key.equals(p.getProducer() + ":" + p.getRcvPattern())) {
                System.out.println("OK.");
              } else {
                System.out.format("CASE MISMATCH: [%-10s] %s%n", p.getProducer(), p.getRcvPattern());
              }
            } else {
              System.out.println("NOT FOUND IN DATAU!");
              reportDeletion(out, rs3.getString("PRODUCER"), rs3.getString("RCV_FILEPATTERN"), rs3.getString("CONSUMER"), rs3.getString("SND_FILENAME"),
                  rs3.getString("T_LAST_ACTIVE"));
              delete++;
            }
          }
        }
      }
    } catch (Exception e) {
      System.err.println("Caught exception: " + e);
      e.printStackTrace();
      System.exit(1);
    }
    System.out.format("Done: %d total, %d rules found, %d missing rules, %d short name rule only, %d with mismatch(es), %d deletions.%n", records, foundRule,
        missing, shortnameonly, mismatch, delete);
  }

  static class TransferPattern {
    private static final int[] FIELD_WIDTH = new int[] { 1, 1, 1, 4, 6, 10, 6, 3, 44, 44, 40, 5, 5, 5, 3, 5, 5, 3, 15, 10 };//
    private static int         LINE_WIDTH  = 0;
    static {
      for (int i = 0; i < FIELD_WIDTH.length; i++) {
        LINE_WIDTH += FIELD_WIDTH[i];
      }
    }

    String         producer;
    String         consumer;
    String         rcvPattern;
    String         sndFilename;
    String         sndPath;
    String         compress;
    String         encrypt;
    String         recFm;
    Integer        recSize;
    Integer        blockSize;
    boolean        outgoing;
    private String shortname;
    private int    lineno;

    public TransferPattern(String direction, String producer, String consumer, String rcvPattern, String sndFilename, String sndPath, String compress,
        String encrypt, String recFm, Integer recSize, Integer blockSize) {
      super();
      this.outgoing = "A".equals(direction);
      this.producer = producer;
      this.consumer = consumer;
      this.rcvPattern = rcvPattern;
      this.sndFilename = sndFilename;
      this.sndPath = sndPath;
      this.compress = compress;
      this.encrypt = encrypt;
      this.recFm = recFm;
      this.recSize = recSize;
      this.blockSize = blockSize;
      this.shortname = createDatauFilename14(this.rcvPattern);
    }

    public TransferPattern(int lineno, String meldSatz) throws IllegalArgumentException {
      this.lineno = lineno;
      String[] d = splitLine(meldSatz);
      this.outgoing = "A".equals(d[2]);
      if (d[2].equals("A")) {
        this.producer = d[4];
        this.consumer = d[5];
      } else {
        this.producer = d[5];
        this.consumer = d[4];
      }
      this.rcvPattern = d[8];
      this.sndFilename = d[9];
      this.sndPath = d[10];
      this.compress = d[11];
      this.encrypt = d[12];
      this.recFm = d[14];
      this.recSize = (d[15].isEmpty() ? null : Integer.valueOf(d[15]));
      this.blockSize = (d[16].isEmpty() ? null : Integer.valueOf(d[16]));
      this.shortname = createDatauFilename14(this.rcvPattern);
    }

    public String getProducer() {
      return producer;
    }

    public void setProducer(String producer) {
      this.producer = producer;
    }

    public String getConsumer() {
      return consumer;
    }

    public void setConsumer(String consumer) {
      this.consumer = consumer;
    }

    public String getRcvPattern() {
      return rcvPattern;
    }

    public void setRcvPattern(String rcvPattern) {
      this.rcvPattern = rcvPattern;
    }

    public String getSndFilename() {
      return sndFilename;
    }

    public void setSndFilename(String sndFilename) {
      this.sndFilename = sndFilename;
    }

    public String getSndPath() {
      if (sndPath != null && sndPath.endsWith("/")) {
        return sndPath.substring(0, sndPath.length() - 1);
      }
      return sndPath;
    }

    public void setSndPath(String sndPath) {
      this.sndPath = sndPath;
    }

    public String getCompress() {
      return compress;
    }

    public void setCompress(String compress) {
      this.compress = compress;
    }

    public String getEncrypt() {
      return encrypt;
    }

    public void setEncrypt(String encrypt) {
      this.encrypt = encrypt;
    }

    public String getRecFm() {
      return recFm;
    }

    public void setRecFm(String recFm) {
      this.recFm = recFm;
    }

    public Integer getRecSize() {
      return recSize;
    }

    public void setRecSize(Integer recSize) {
      this.recSize = recSize;
    }

    public Integer getBlockSize() {
      return blockSize;
    }

    public void setBlockSize(Integer blockSize) {
      this.blockSize = blockSize;
    }

    public boolean isOutgoing() {
      return outgoing;
    }

    private String[] splitLine(String line) throws IllegalArgumentException {
      int[] fieldLen = FIELD_WIDTH.clone();
      int len = LINE_WIDTH;
      if (line.length() == 226) {
        len = 226;
        fieldLen[5] += 10;
      } else if (line.length() == 191 || line.length() == 201) {
        len = line.length();
        fieldLen[5] += (len == 191 ? 0 : 10);
        fieldLen[18] = 0;
        fieldLen[19] = 0;
      }

      String[] result = new String[fieldLen.length + 1];
      int offset = 0;
      if (line.length() < len && line.length() < len - fieldLen[fieldLen.length - 1]) {
        throw new IllegalArgumentException("Line: " + lineno + " invalid line length: " + line.length() + ", expect: " + len + "!");
      }
      for (int i = 0; i < fieldLen.length && line.length() > (offset + fieldLen[i]); i++) {
        if (fieldLen[i] > 0) {
          result[i] = line.substring(offset, offset + fieldLen[i]).trim();
          offset += fieldLen[i];
        } else {
          result[i] = "";
        }
      }
      result[fieldLen.length] = line.substring(offset);
      return result;
    }

    public String toLongString() {
      StringBuilder sb = new StringBuilder();
      sb.append(String.format("%-30s: %s%n", "Line", lineno));
      sb.append(String.format("%-30s: %s%n", "Producer", getProducer()));
      sb.append(String.format("%-30s: %s%n", "Receive pattern", getRcvPattern()));
      if (isOutgoing()) {
        // sb.append(String.format("%-30s: %s %n", "Short filename", getShortName()));
        sb.append(String.format("%-30s: %s%n", "DATAU flag", "YES"));
      }
      sb.append(String.format("%-30s: %s (Check if consumer name has changed on SFG)%n", "Consumer", getConsumer()));
      sb.append(String.format("%-30s: %s%n", "Send filename", getSndFilename()));
      sb.append(String.format("%-30s: %s (check other outgoing transfers to %s for path!)%n", "Send path", getSndPath(), getConsumer()));
      sb.append(String.format("%-30s: %s (check other outgoing transfers to %s if partner uses other protocol like MBOX)%n", "Protocol",
          (isOutgoing() ? (getConsumer().startsWith("RVS") ? "OFTP" : (getConsumer().startsWith("BUBA") ? "BUBA" : "SFTP")) : "C:D"), getConsumer()));
      sb.append(String.format("%-30s: %s%n", "Processing", (getCompress().contains("ZIP") ? (isOutgoing() ? "zip" : "unzip") : "")));
      // if (!isOutgoing()) {
      StringBuilder sb2 = new StringBuilder("DSORG=PS");
      if (getRecSize() != null && getRecSize() > 0) {
        sb2.append(",LRECL=").append(getRecSize());
      }
      if (getBlockSize() != null && getBlockSize() > 0) {
        sb2.append(",BLKSIZE=").append(getBlockSize());
      }
      if (getRecFm() != null && !getRecFm().isEmpty()) {
        sb2.append(",RECFM=").append(getRecFm());
      }
      sb.append(String.format("%-30s: %s%n", "DCBOPTS", sb2.toString()));

      // }
      return sb.toString();
    }

    private String getShortName() {
      return this.shortname;
    }
  }
}
