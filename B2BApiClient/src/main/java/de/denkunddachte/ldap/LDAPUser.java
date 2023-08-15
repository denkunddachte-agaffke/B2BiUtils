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
package de.denkunddachte.ldap;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.denkunddachte.ft.Exportable;

public class LDAPUser implements Exportable {
  private static final Logger LOGGER = Logger.getLogger(LDAPUser.class.getName());
  private String                    dn;
  private String                    cn;
  private String                    sn;
  private String                    givenName;
  private String                    password;
  private String                    hashMode;
  private final Map<String, String> sshPublicKeys = new LinkedHashMap<>();
  private final Set<String>         objectClasses = new LinkedHashSet<>();

  public LDAPUser(String dn) {
    this.dn = dn;
    int p = dn.indexOf("cn=");
    if (p > -1) {
      this.cn = dn.substring(p + 3, (dn.indexOf(',', p + 3) > 0 ? dn.indexOf(',', p + 3) : dn.length()));
    }
  }

  public boolean addObjectclass(String objectClass) {
    return objectClasses.add(objectClass);
  }

  public String addSshPublicKey(String key) {
    return sshPublicKeys.put(key, "?");
  }

  public String addSshPublicKey(String key, String keyName) {
    return sshPublicKeys.put(key, keyName);
  }

  public String getCn() {
    return cn;
  }

  public String getDn() {
    return dn;
  }

  public String getGivenName() {
    return givenName;
  }

  public String getHashMode() {
    return hashMode;
  }

  public Collection<String> getObjectClasses() {
    return new LinkedHashSet<>(objectClasses);
  }

  public Collection<String> getSshPublicKeys() {
    return new LinkedHashSet<>(sshPublicKeys.keySet());
  }

  public boolean hasKey(String key) {
    return sshPublicKeys.containsKey(key);
  }

  public boolean isObjectclass(String objectClass) {
    return objectClasses.contains(objectClass);
  }

  public String removeSshPublicKey(String key) {
    return sshPublicKeys.remove(key);
  }

  public void removeAllSshPublicKeys() {
    sshPublicKeys.clear();
  }

  public void setCn(String cn) {
    if (this.cn != null && !this.cn.equals(cn)) {
      LOGGER.log(Level.WARNING, "Overwriting common name {0} with {1})!", new Object[] {this.cn, cn});
    }
    this.cn = cn;
  }

  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }

  public void setHashMode(String hashMode) {
    this.hashMode = hashMode;
  }

  public void setPassword(String password) {
    if (password == null || password.trim().isEmpty()) {
      this.password = null;
    } else {
      this.password = password.trim();
    }
  }

  public void setPassword(String plainTextPw, String hashMode) throws NoSuchAlgorithmException {
    if (plainTextPw == null || plainTextPw.trim().isEmpty()) {
      this.password = null;
    } else {
      if (hashMode != null) {
        this.password = hashPassword(plainTextPw.trim(), hashMode);
      } else {
        this.password = plainTextPw;
      }
      this.hashMode = hashMode;
    }
  }

  public String getPassword() {
    return this.password;
  }

  public String getKeyNames() {
    if (sshPublicKeys.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    int           i  = 0;
    for (String n : sshPublicKeys.values()) {
      if (sb.length() > 0)
        sb.append(" ");
      sb.append(++i).append(':').append(n).append(';');
    }
    return sb.toString();
  }

  public void setKeyNames(String str) {
    Pattern p = Pattern.compile("(\\d+):(\\S.+?)([;,] ?|$)");
    Matcher m = p.matcher(str);
    while (m.find()) {
      int idx = Integer.parseInt(m.group(1));
      int i   = 0;
      for (Entry<String, String> e : sshPublicKeys.entrySet()) {
        if (++i == idx && "?".equals(e.getValue())) {
          e.setValue(m.group(2));
          break;
        }
      }
    }
  }

  public String getSn() {
    return sn;
  }

  public void setSn(String sn) {
    if (sn != null && sn.contains("1:")) {
      setKeyNames(sn);
    }
    this.sn = sn;
  }

  @Override
  public String toString() {
    return "LDAPUser [dn=" + dn + ", cn=" + cn + ", sn=" + sn + ", givenName=" + givenName + ", pw=" + (password == null ? "no" : "yes") + ", hashMode="
        + hashMode + ", sshPublicKeys=" + sshPublicKeys + ", objectClasses=" + objectClasses + "]";
  }

  private String hashPassword(String plainTextPw, String mode) throws NoSuchAlgorithmException {
    MessageDigest crypt = MessageDigest.getInstance(mode);
    crypt.reset();
    crypt.update(plainTextPw.getBytes(StandardCharsets.UTF_8));
    byte[] hash = crypt.digest();
    return "{" + mode + "}" + Base64.getEncoder().encodeToString(hash);
  }

  @Override
  public void export(PrintWriter os) {
    export(os, false, false);
  }

  @Override
  public void export(PrintWriter out, boolean prettyPrint, boolean suppressNullValues) {
    Encoder b64 = Base64.getEncoder();
    out.format("dn: %s%n", getDn());
    getObjectClasses().forEach(oc -> out.format("objectClass: %s%n", oc));
    out.format("cn: %s%n", getCn());
    if (getSn() != null) {
      out.format("sn: %s%n", getSn());
    }
    if (getGivenName() != null) {
      out.format("givenName: %s%n", getGivenName());
    }
    if (getPassword() != null) {
      out.format("userPassword:: %s%n", b64.encodeToString(getPassword().getBytes(StandardCharsets.UTF_8)));
    }
    getSshPublicKeys().forEach(key -> key2ldif(out, "sshPublicKey:: " + b64.encodeToString(key.getBytes(StandardCharsets.UTF_8))));
  }

  @Override
  public String getBasename() {
    return getCn();
  }

  @Override
  public Mode getExportMode() {
    return Mode.LDIF;
  }

  private void key2ldif(PrintWriter out, String key) {
    out.print(key.charAt(0));
    int o = 1;
    while (o < key.length()) {
      if (o > 1)
        out.print(' ');
      out.println(key.substring(o, ((o + 75) < key.length() ? o + 75 : key.length())));
      o += 75;
    }
  }
}
