package de.denkunddachte.b2biutil.loader.model;

public enum LoadAction {
  //@formatter:off
  NEW("N"), 
  UPDATE("U"), 
  DELETE("D"), 
  SKIP("S"), 
  ADD_CONSUMER("AC"), 
  UPDATE_CONSUMER("UC"), 
  DELETE_CONSUMER("DC"), 
  FAILED("F");
  //@formatter:on

  public final String code;

  LoadAction(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public static LoadAction byCode(String code) {
    for (LoadAction e : values()) {
      if (e.code.equals(code)) {
        return e;
      }
    }
    throw new IllegalArgumentException("Invalid action code: " + code + "!");
  }

  public static boolean isActive(LoadAction loadAction) {
    return loadAction != SKIP && loadAction != FAILED;
  }
}
