/*
  Copyright 2018 - 2023 denk & dachte Software GmbH

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package de.denkunddachte.enums;

public enum TlsVersion {
  SSL_V3(-1, "SSL v3", "SSLv3"), TLS_V1(0, "TLS 1.0", "TLSv1"), TLS_V11(1, "TLS 1.1", "TLSv1.1"), TLS_V12(2, "TLS 1.2", "TLSv1.2"), TLS_V13(3, "TLS 1.3",
      "TLSv1.3"), TLS_ONLY(-1, "TLS_ONLY", "TLS_ONLY"), PNODE(-1, "PNODE_CTRL", "PNODE");

  private final String version;
  private final int    code;
  private final String sspProtcolString;

  private TlsVersion(int code, String tlsVersion, String sspProtcolString) {
    this.code = code;
    this.version = tlsVersion;
    this.sspProtcolString = sspProtcolString;
  }

  public String getVersion() {
    return version;
  }

  public String getSspProtcolString() {
    return sspProtcolString;
  }

  public int getCode() {
    return code;
  }

  public static TlsVersion getByCode(int code) {
    for (TlsVersion o : TlsVersion.values()) {
      if (o.code == code)
        return o;
    }
    throw new IllegalArgumentException("No TlsVersion for code=" + code + "!");
  }

  public static TlsVersion getByVersionString(String tlsVersion) {
    for (TlsVersion o : TlsVersion.values()) {
      if (o.version.equalsIgnoreCase(tlsVersion) || o.sspProtcolString.equalsIgnoreCase(tlsVersion))
        return o;
    }
    throw new IllegalArgumentException("No TlsVersion " + tlsVersion + "!");
  }
}
