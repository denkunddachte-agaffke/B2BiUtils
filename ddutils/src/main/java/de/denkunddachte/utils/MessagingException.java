package de.denkunddachte.utils;

public class MessagingException extends Exception {

  private static final long serialVersionUID = -3353811530791915864L;

  public MessagingException() {
    super();
  }

  public MessagingException(String arg0) {
    super(arg0);
  }

  public MessagingException(Throwable arg0) {
    super(arg0);
  }

  public MessagingException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  public MessagingException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
    super(arg0, arg1, arg2, arg3);
  }
}
