package de.denkunddachte.exception;

public class B2BLoadException extends Exception {
  private static final long serialVersionUID = -7477979402519704854L;

  public B2BLoadException(String msg) {
    super(msg);
  }

  public B2BLoadException(String msg, Throwable e) {
    super(msg, e);
  }

  public B2BLoadException(Exception e) {
    super(e);
  }
}
