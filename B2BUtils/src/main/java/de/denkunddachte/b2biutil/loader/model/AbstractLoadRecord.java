package de.denkunddachte.b2biutil.loader.model;

import de.denkunddachte.b2biutil.loader.LoaderResult.Artifact;

public abstract class AbstractLoadRecord {
  protected LoadAction action;
  protected int line;
  
  protected AbstractLoadRecord(LoadAction action, int line) {
    this.action = action;
    this.line = line;
  }
  
  public LoadAction getLoadAction() {
    return this.action;
  }
  
  public boolean isValid() {
    return isValid(action);
  }

  public boolean isValid(LoadAction action) {
    switch (action) {
    case FAILED:
      return false;
    case NEW:
    case UPDATE:
    case ADD_CONSUMER:
    case UPDATE_CONSUMER:
      return isValid(true);
    case DELETE:
    case DELETE_CONSUMER:
    case SKIP:
      return isValid(false);
    default:
      return isValid(false);
    }
  }
  
  public int getLine() {
    return this.line;
  }

  public abstract boolean isValid(boolean full);
  public abstract Artifact getArtifact();
  public abstract String getId();
}
