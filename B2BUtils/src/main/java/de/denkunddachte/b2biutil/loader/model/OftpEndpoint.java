package de.denkunddachte.b2biutil.loader.model;

import java.util.Objects;
import java.util.stream.Stream;

import de.denkunddachte.enums.FTProtocol;

public class OftpEndpoint extends Endpoint {
  private String logPartnerContract;

  public OftpEndpoint(String logPartnerContract) {
    super(FTProtocol.OFTP);
    this.logPartnerContract = logPartnerContract;
  }

  public OftpEndpoint() {
    super(FTProtocol.OFTP);
  }

  public String getLogPartnerContract() {
    return logPartnerContract;
  }

  @Override
  public boolean isValid(boolean full) {
      return Stream.of(logPartnerContract).allMatch(Objects::nonNull);
  }

  @Override
  public String toString() {
    return "OftpEndpoint [logPartnerContract=" + logPartnerContract + "]";
  }
  
}
