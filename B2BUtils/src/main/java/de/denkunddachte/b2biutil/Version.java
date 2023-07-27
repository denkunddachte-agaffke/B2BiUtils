package de.denkunddachte.b2biutil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Version {
  static {
    try (InputStream is = Version.class.getResourceAsStream("/version.info")) {
      Properties p = new Properties();
      p.load(is);
      VERSION = p.getProperty("version");
      BUILD = p.getProperty("build");
      COPYRIGHT = p.getProperty("copyright");
      PRODUCT = p.getProperty("product");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  public static final String VERSION;
  public static final String BUILD;
  public static final String COPYRIGHT;
  public static final String PRODUCT;

  private Version() {
  }
}
