package de.denkunddachte.b2biutil.loader.model;

import de.denkunddachte.enums.FTProtocol;

public class MboxEndpoint extends Endpoint {
  private int extractabilityCount;

  public MboxEndpoint() {
    super(FTProtocol.MBOX);
    this.setExtractabilityCount(999);
  }

  public int getExtractabilityCount() {
    return extractabilityCount;
  }

  public void setExtractabilityCount(int extractabilityCount) {
    this.extractabilityCount = extractabilityCount;
  }

  @Override
  public boolean isValid(boolean full) {
    return true;
  }

  @Override
  public String toString() {
    return "MboxEndpoint [extractabilityCount=" + extractabilityCount + "]";
  }

}
