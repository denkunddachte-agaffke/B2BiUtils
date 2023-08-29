package de.denkunddachte.b2biutil.loader.model;

import java.util.Objects;
import java.util.stream.Stream;

import de.denkunddachte.b2biutil.loader.LoaderResult.Artifact;
import de.denkunddachte.enums.FTProtocol;
import de.denkunddachte.enums.FileDisposition;
import de.denkunddachte.enums.OSType;

public class DeliveryRule extends AbstractLoadRecord {
  private TransferRule    transferRule;
  private String          consumer;
  private String          destName;
  private FTProtocol      protocol;
  private String          tempName;
  private OSType          consumerOs;
  private boolean         allowCodedPaths = false;
  private String          filetype;
  private String          postCommand;
  private String          aglTenant;
  private FileDisposition remoteDisp;
  private Endpoint        endpoint;

  public DeliveryRule(LoadAction action, int line, TransferRule transferRule, String consumer, String destName, Endpoint endpoint) {
    this(action, line, transferRule, consumer, destName, endpoint.getProtocol());
    this.endpoint = endpoint;
  }

  public DeliveryRule(LoadAction action, int line, TransferRule transferRule, String consumer, String destName, FTProtocol protocol) {
    super(action, line);
    this.transferRule = transferRule;
    this.consumer = consumer;
    this.destName = destName;
    this.protocol = protocol;
    this.remoteDisp = FileDisposition.RPL;
    this.filetype = "default";
  }

  public TransferRule getTransferRule() {
    return transferRule;
  }

  public String getTempName() {
    return tempName;
  }

  public void setTempName(String tempName) {
    this.tempName = tempName;
  }

  public OSType getConsumerOs() {
    return consumerOs;
  }

  public void setConsumerOs(OSType consumerOs) {
    this.consumerOs = consumerOs;
  }

  public boolean isAllowCodedPaths() {
    return allowCodedPaths;
  }

  public void setAllowCodedPaths(boolean allowCodedPaths) {
    this.allowCodedPaths = allowCodedPaths;
  }

  public String getFiletype() {
    return filetype;
  }

  public void setFiletype(String filetype) {
    this.filetype = filetype;
  }

  public String getPostCommand() {
    return postCommand;
  }

  public void setPostCommand(String postCommand) {
    this.postCommand = postCommand;
  }

  public String getAglTenant() {
    return aglTenant;
  }

  public void setAglTenant(String aglTenant) {
    this.aglTenant = aglTenant;
  }

  public FileDisposition getRemoteDisp() {
    return remoteDisp;
  }

  public void setRemoteDisp(FileDisposition remoteDisp) {
    this.remoteDisp = remoteDisp;
  }

  public Endpoint getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(Endpoint endpoint) {
    this.endpoint = endpoint;
  }

  public String getConsumer() {
    return consumer;
  }

  public String getDestName() {
    return destName;
  }

  public FTProtocol getProtocol() {
    return protocol;
  }

  public void setProtocol(FTProtocol protocol) {
    this.protocol = protocol;
    if (this.endpoint != null && this.endpoint.getProtocol() != protocol) {
      switch (protocol) {
      case AWSS3:
        this.endpoint = new AWSS3Endpoint();
        break;
      case BUBA:
        this.endpoint = new BubaEndpoint();
        break;
      case CD:
        this.endpoint = new CDEndpoint();
        break;
      case OFTP:
        this.endpoint = new OftpEndpoint();
        break;
      case MBOX:
        this.endpoint = new MboxEndpoint();
        break;
      case SFTP:
        this.endpoint = new SftpEndpoint();
        break;
      default:
        throw new IllegalArgumentException("Invalid protocol: " + protocol);
      }
      this.endpoint = null;
    }
  }

  @Override
  public boolean isValid(boolean full) {
    if (full) {
      return Stream.of(action, transferRule, consumer, destName, endpoint).allMatch(Objects::nonNull);
    } else {
      return Stream.of(action, transferRule, consumer).allMatch(Objects::nonNull);
    }
  }

  @Override
  public Artifact getArtifact() {
    return Artifact.RoutingRule;
  }

  @Override
  public int hashCode() {
    return Objects.hash(consumer, destName, transferRule);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof DeliveryRule))
      return false;
    DeliveryRule other = (DeliveryRule) obj;
    return Objects.equals(consumer, other.consumer) && Objects.equals(destName, other.destName) && Objects.equals(transferRule, other.transferRule);
  }

  @Override
  public String getId() {
    return transferRule.getId() + " -> [" + consumer + "]" + destName + "(" + protocol + ")";
  }

  @Override
  public String toString() {
    return "DeliveryRule [consumer=" + consumer + ", destName=" + destName + ", remoteDisp=" + remoteDisp + ", endpoint=" + endpoint + "]";
  }
}
