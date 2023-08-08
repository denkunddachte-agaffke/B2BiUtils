package de.denkunddachte.b2biutil.loader;

public class TemplateException extends Exception {

  private static final long serialVersionUID = -934581007152506419L;

  public TemplateException(String msg) {
    super(msg);
  }

  public TemplateException(Throwable e) {
    super(e);
  }

  public TemplateException(String msg, Throwable e) {
    super(msg, e);
  }

  public TemplateException(String msg, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(msg, cause, enableSuppression, writableStackTrace);
  }

}
