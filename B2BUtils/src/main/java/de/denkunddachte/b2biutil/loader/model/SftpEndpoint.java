package de.denkunddachte.b2biutil.loader.model;

import java.util.Objects;
import java.util.stream.Stream;

import de.denkunddachte.enums.FTProtocol;

public class SftpEndpoint extends Endpoint {
  private String host;
  private int    port;
  private String username;
  private String privKeyName;
  private String password;
  private String publicKey;

  public SftpEndpoint(String host, int port, String username) {
    super(FTProtocol.SFTP);
    this.host = host;
    this.port = port > 0 ? port : 22;
    this.username = username;
  }

  public SftpEndpoint() {
    super(FTProtocol.SFTP);
  }

  public String getPrivKeyName() {
    return privKeyName;
  }

  public void setPrivKeyName(String privKeyName) {
    this.privKeyName = privKeyName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
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

  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }

  @Override
  public boolean isValid(boolean full) {
    if (isProducerConnection()) {
      if (full) {
        return Stream.of(username, publicKey).allMatch(Objects::nonNull);
      } else {
        return Stream.of(username).allMatch(Objects::nonNull);
      }

    } else {
      if (full) {
        return Stream.of(host, port, username, (privKeyName == null ? password : privKeyName)).allMatch(Objects::nonNull);
      } else {
        return Stream.of(host, port, username).allMatch(Objects::nonNull);
      }
    }
  }

  @Override
  public String toString() {
    return "SftpEndpoint [host=" + host + ", port=" + port + ", username=" + username + ", privKeyName=" + privKeyName + ", password="
        + (password == null ? "yes" : "no") + ", publicKey=" + publicKey + "]";
  }

}
