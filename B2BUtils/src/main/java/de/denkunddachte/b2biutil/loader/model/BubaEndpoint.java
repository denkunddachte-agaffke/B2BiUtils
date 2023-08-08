package de.denkunddachte.b2biutil.loader.model;

import java.util.Objects;
import java.util.stream.Stream;

import de.denkunddachte.enums.FTProtocol;

public class BubaEndpoint extends Endpoint {
  private String host;
  private int    port      = 443;
  private String username;
  private String password;
  private String basePath  = "/FT/";
  private String loginPath = "/pkmslogin.form";
  private String ftpoaRecipient;

  public BubaEndpoint(String host, int port, String username, String password, String ftpoaRecipient) {
    super(FTProtocol.BUBA);
    this.host = host;
    if (port > 0) {
      this.port = port;
    }
    this.username = username;
    this.password = password;
    this.ftpoaRecipient = ftpoaRecipient;
  }

  public BubaEndpoint() {
    super(FTProtocol.BUBA);
  }

  public String getBasePath() {
    return basePath;
  }

  public void setBasePath(String basePath) {
    this.basePath = basePath;
  }

  public String getLoginPath() {
    return loginPath;
  }

  public void setLoginPath(String loginPath) {
    this.loginPath = loginPath;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getFtpoaRecipient() {
    return ftpoaRecipient;
  }

  @Override
  public boolean isValid(boolean full) {
    if (full) {
      return Stream.of(host, username, password, ftpoaRecipient).allMatch(Objects::nonNull);
    } else {
      return Stream.of(host, username, ftpoaRecipient).allMatch(Objects::nonNull);
    }
  }

  @Override
  public String toString() {
    return "BubaEndpoint [host=" + host + ", port=" + port + ", username=" + username + ", password=" + (password == null ? "no" : "yes") + ", basePath="
        + basePath + ", loginPath=" + loginPath + ", ftpoaRecipient=" + ftpoaRecipient + "]";
  }

}
