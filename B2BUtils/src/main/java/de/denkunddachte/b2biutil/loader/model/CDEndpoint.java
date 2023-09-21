package de.denkunddachte.b2biutil.loader.model;

import java.util.Objects;
import java.util.stream.Stream;

import de.denkunddachte.enums.FTProtocol;

public class CDEndpoint extends Endpoint {
  private String  nodeName;
  private boolean useSpoe;
  private String  username;
  private String  password;
  private String  dcbOpts;
  private String  sysOpts;
  private boolean binaryMode;
  private boolean localXlate;
  private String  xlateTable;
  private String  runTask;
  private String  runJob;
  private String  uc4Trigger;
  private boolean useSecurePlus;
  private String  host;
  private int     port;

  public CDEndpoint(String nodeName, String username, boolean useSpoe) {
    super(FTProtocol.CD);
    this.nodeName = nodeName;
    this.username = username;
    this.useSpoe = useSpoe;
    this.port = 1364;
    this.useSecurePlus = false;
    this.host = nodeName;
  }

  public CDEndpoint() {
    super(FTProtocol.CD);
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getDcbOpts() {
    return dcbOpts;
  }

  public void setDcbOpts(String dcbOpts) {
    this.dcbOpts = dcbOpts;
  }

  public String getSysOpts() {
    return sysOpts;
  }

  public void setSysOpts(String sysOpts) {
    this.sysOpts = sysOpts;
  }

  public boolean isBinaryMode() {
    return binaryMode;
  }

  public void setBinaryMode(boolean binaryMode) {
    this.binaryMode = binaryMode;
  }

  public boolean isLocalXlate() {
    return localXlate;
  }

  public void setLocalXlate(boolean localXlate) {
    this.localXlate = localXlate;
  }

  public String getXlateTable() {
    return xlateTable;
  }

  public void setXlateTable(String xlateTable) {
    this.xlateTable = xlateTable;
  }

  public String getRunTask() {
    return runTask;
  }

  public void setRunTask(String runTask) {
    this.runTask = runTask;
  }

  public String getRunJob() {
    return runJob;
  }

  public void setRunJob(String runJob) {
    this.runJob = runJob;
  }

  public String getUc4Trigger() {
    return uc4Trigger;
  }

  public void setUc4Trigger(String uc4Trigger) {
    this.uc4Trigger = uc4Trigger;
  }

  public String getNodeName() {
    return nodeName;
  }

  public boolean isUseSpoe() {
    return useSpoe;
  }

  public boolean isUseSecurePlus() {
    return useSecurePlus;
  }

  public void setUseSecurePlus(boolean useSecurePlus) {
    this.useSecurePlus = useSecurePlus;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  @Override
  public boolean isValid(boolean full) {
    if (isProducerConnection()) {
      if (full) {
        return Stream.of(nodeName, host).allMatch(Objects::nonNull);
      } else {
        return Stream.of(nodeName).allMatch(Objects::nonNull);
      }
    } else {
      if (full) {
        return Stream.of(nodeName, username, host).allMatch(Objects::nonNull);
      } else {
        return Stream.of(nodeName, username).allMatch(Objects::nonNull);
      }
    }
  }

  @Override
  public String toString() {
    return "CDEndpoint [nodeName=" + nodeName + ", useSpoe=" + useSpoe + ", username=" + username + ", password=" + (password == null ? "yes" : "no")
        + ", dcbOpts=" + dcbOpts + ", sysOpts=" + sysOpts + ", binaryMode=" + binaryMode + ", localXlate=" + localXlate + ", xlateTable=" + xlateTable
        + ", runTask=" + runTask + ", runJob=" + runJob + ", uc4Trigger=" + uc4Trigger + ", useSecurePlus=" + useSecurePlus + ", host=" + host + ", port="
        + port + "]";
  }

}
