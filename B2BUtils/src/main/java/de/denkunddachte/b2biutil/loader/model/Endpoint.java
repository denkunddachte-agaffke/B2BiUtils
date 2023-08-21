package de.denkunddachte.b2biutil.loader.model;

import de.denkunddachte.enums.FTProtocol;

public abstract class Endpoint {
  private FTProtocol protocol;
  private boolean  producerConnection;
  private boolean cloneable;

  protected Endpoint(FTProtocol protocol) {
    this.protocol = protocol;
    this.setProducerConnection(false);
  }

  public FTProtocol getProtocol() {
    return protocol;
  }

  public boolean isProducerConnection() {
    return producerConnection;
  }

  public void setProducerConnection(boolean producerConnection) {
    this.producerConnection = producerConnection;
  }

  public boolean isCloneable() {
    return cloneable;
  }

  public void setCloneable(boolean cloneable) {
    this.cloneable = cloneable;
  }

  public boolean isValid() {
    return cloneable || isValid(false);
  }

  public boolean isValid(LoadAction action) {
    if(cloneable)
      return true;
    
    switch (action) {
    case NEW:
    case UPDATE:
    case ADD_CONSUMER:
    case UPDATE_CONSUMER:
      return isValid(true);

    case DELETE:
    case DELETE_CONSUMER:
      // for DELETE operations, no endpoint details are required
      return true;
    default:
      return isValid(false);
    }
  }

  public abstract boolean isValid(boolean full);

  @Override
  public String toString() {
    return "Endpoint [protocol=" + protocol + ", producerConnection=" + producerConnection + "]";
  }
}
