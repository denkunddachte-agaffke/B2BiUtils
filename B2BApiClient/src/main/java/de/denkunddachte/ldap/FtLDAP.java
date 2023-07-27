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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import de.denkunddachte.util.ApiConfig;
import de.denkunddachte.exception.ApiException;

public class FtLDAP {
  public enum HASHMODE {
    SHA256, SHA1, MD5, SHA
  }

  public static final String  SSH_PUBLIC_KEY = "sshPublicKey";
  public static final String  USER_PASSWORD  = "userPassword";
  public static final String  GIVEN_NAME     = "givenName";
  public static final String  SN             = "sn";
  public static final String  CN             = "cn";
  public static final String  DN             = "dn";
  public static final String  OBJECT_CLASS   = "objectClass";
  private static final Logger LOGGER         = Logger.getLogger(FtLDAP.class.getName());
  private String ftBase;
  private String ftAdmBase;

  LdapContext                 ctx;

  public FtLDAP(ApiConfig cfg) throws NamingException {
    final Hashtable<String, Object> ldapEnv = new Hashtable<>();
    ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
    ldapEnv.put(Context.SECURITY_PRINCIPAL, cfg.getLdapAdminUser());
    ldapEnv.put(Context.SECURITY_CREDENTIALS, cfg.getLdapAdminPassword());
    ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    ldapEnv.put(Context.PROVIDER_URL, cfg.getLdapUrl());
    if (cfg.isTrustAllCerts() && cfg.getLdapUrl().startsWith("ldaps")) {
      LOGGER.log(Level.FINE, "Set java.naming.ldap.factory.socket=de.denkunddachte.util.CustomSSLSocketFactory to trust all certificates.");
      ldapEnv.put(Context.SECURITY_PROTOCOL, "ssl");
      ldapEnv.put("java.naming.ldap.factory.socket", "de.denkunddachte.util.CustomSSLSocketFactory");
    }
    // ldapEnv.put("java.naming.ldap.attributes.binary", "objectSID");
    // ldapEnv.put("com.sun.jndi.ldap.trace.ber", System.err);
    ctx = new InitialLdapContext(ldapEnv, null);
    ctx.setRequestControls(null);
    this.ftBase = cfg.getLdapBase();
    this.ftAdmBase = cfg.getLdapAdmBase();
  }

  public boolean addUser(LDAPUser ldapuser) throws NamingException {
    boolean result = false;
    if (getUser(ldapuser.getDn()) == null) {
      Attributes attrs = new BasicAttributes();
      attrs.put(CN, ldapuser.getCn());
      if (ldapuser.getGivenName() != null) {
        attrs.put(GIVEN_NAME, ldapuser.getGivenName());
      }
      if (ldapuser.getPassword() != null) {
        attrs.put(USER_PASSWORD, ldapuser.getPassword());
      }
      if (!ldapuser.getSshPublicKeys().isEmpty()) {
        Attribute keys = new BasicAttribute(SSH_PUBLIC_KEY);
        for (String key : ldapuser.getSshPublicKeys()) {
          keys.add(key);
        }
        attrs.put(keys);
      }
      Attribute oc = new BasicAttribute(OBJECT_CLASS);
      oc.add("inetOrgPerson");
      oc.add("ldapPublicKey");
      attrs.put(oc);
      attrs.put(SN, getSnString(ldapuser));
      try {
        ctx.createSubcontext(ldapuser.getDn(), attrs);
        LOGGER.log(Level.FINE, "Created user {0}.", ldapuser);
        result = true;
      } catch (NamingException e) {
        LOGGER.log(Level.SEVERE, e, () -> "Could not add user " + ldapuser.getDn() + "!");
      }
    } else {
      LOGGER.log(Level.WARNING, "User {0} already exists. Use updateUser() to modify.", ldapuser.getDn());
    }
    return result;
  }

  public boolean deleteUser(LDAPUser user) throws NamingException {
    boolean result = false;
    if (getUser(user.getDn()) != null) {
      ctx.destroySubcontext(user.getDn());
      result = true;
    } else {
      LOGGER.log(Level.WARNING, "User DN {0} does not exits!", user.getDn());
    }
    return result;
  }

  public boolean deleteUser(String dn) throws NamingException {
    boolean result = false;
    if (getUser(dn) != null) {
      ctx.destroySubcontext(dn);
      result = true;
    } else {
      LOGGER.log(Level.WARNING, "User DN {0} does not exits!", dn);
    }
    return result;
  }

  public boolean deleteUser(String cn, String base) throws NamingException {
    return deleteUser(CN + "=" + cn + "," + base);
  }

  public LDAPUser getUser(String dn) throws NamingException {
    SearchResult r = getUserItem(dn);
    if (r == null)
      return null;
    return mkUser(r);
  }

  public LDAPUser getFTUser(String userId) throws NamingException {
    return getUser(CN + "=" + userId + "," + ftBase);
  }

  public LDAPUser getAdminUser(String userId) throws NamingException {
    return getUser(CN + "=" + userId + "," + ftAdmBase);
  }

  public LDAPUser getUser(String cn, String base) throws NamingException {
    return getUser(CN + "=" + cn + "," + base);
  }

  public Collection<LDAPUser> getUsers(String base) throws ApiException {
    return getUsers(base, null, false);
  }

  public Collection<LDAPUser> getUsers(String base, String regexPattern, boolean caseSensitive) throws ApiException {
    Collection<LDAPUser> result = new ArrayList<>();
    Pattern p = null;
    if (regexPattern != null && !regexPattern.isEmpty()) {
      p = Pattern.compile(caseSensitive ? "cn=" + regexPattern : "(?i)cn=" + regexPattern);
    }
    try {
      NamingEnumeration<?> e = ctx.search(base, "(objectClass=inetOrgPerson)", getSimpleSearchControls());
      while (e.hasMoreElements()) {
        SearchResult sr = (SearchResult) e.nextElement();
        if (p != null && !p.matcher(sr.getName()).matches()) {
          LOGGER.log(Level.FINER, "Ignore {0} because it does not match pattern {1}.", new Object[] { sr.getName(), regexPattern });
        } else {
          LOGGER.log(Level.FINER, "Found {0}.", sr.getName());
          result.add(mkUser(sr));
        }
      }
    } catch (NamingException e) {
      throw new ApiException("Could not get userlist for " + base + "!", e);
    }
    return result;
  }

  public boolean updateUser(LDAPUser ldapuser) throws NamingException {
    boolean result = false;
    SearchResult r = getUserItem(ldapuser.getDn());
    if (r == null) {
      LOGGER.log(Level.WARNING, "User {0} does not exist!", ldapuser.getDn());
      return result;
    }

    List<ModificationItem> modifications = new ArrayList<>();
    Attributes attrs = r.getAttributes();
    ModificationItem mi;
    if ((mi = getAttrModification(attrs.get(SN), SN, getSnString(ldapuser))) != null) {
      modifications.add(mi);
    }
    if ((mi = getAttrModification(attrs.get(GIVEN_NAME), GIVEN_NAME, ldapuser.getGivenName())) != null) {
      modifications.add(mi);
    }
    if ((mi = getAttrModification(attrs.get(USER_PASSWORD), USER_PASSWORD, ldapuser.getPassword())) != null) {
      modifications.add(mi);
    }

    if ((mi = getSshKeysModification(attrs.get(SSH_PUBLIC_KEY), ldapuser.getSshPublicKeys())) != null) {
      modifications.add(mi);
    }

    if (!modifications.isEmpty()) {
      ModificationItem[] mods = new ModificationItem[modifications.size()];
      for (int i = 0; i < modifications.size(); i++) {
        mods[i] = modifications.get(i);
      }
      LOGGER.log(Level.FINER, "modifications={0}", modifications);
      ctx.modifyAttributes(ldapuser.getDn(), mods);
      LOGGER.log(Level.FINE, "Updated {0} attributes in {1}.", new Object[] { modifications.size(), ldapuser.getDn() });
      result = true;
    } else {
      LOGGER.log(Level.FINE, "Skip update of user {0} (unchanged)", ldapuser);
    }
    return result;
  }
  
  private String getSnString(LDAPUser user) {
    StringBuilder sb = new StringBuilder();
    if (user.getSn() != null && !user.getSn().trim().isEmpty()) {
      int p = user.getSn().indexOf("1:");
      sb.append(p > -1 ? user.getSn().substring(0, p) : user.getSn()).append(' ');
    }
    sb.append(user.getKeyNames());
    return sb.toString().trim();
  }

  private ModificationItem compareSshKeys(Attribute attribute, Collection<String> userKeys) throws NamingException {
    ModificationItem mi = null;
    NamingEnumeration<?> e = ((Attribute) attribute.clone()).getAll();
    boolean modified = false;
    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      if (!userKeys.contains(key)) {
        attribute.remove(key);
        modified = true;
      }
    }
    for (String key : userKeys) {
      if (!attribute.contains(key)) {
        attribute.add(key);
        modified = true;
      }
    }
    if (modified) {
      mi = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attribute);
    }
    return mi;
  }

  private int compareStringAttr(Attribute currentAttr, String newVal) throws NamingException {
    int result = 0;
    String currentVal = stringValue(currentAttr);
    if (currentVal == null) {
      if (newVal != null) {
        result = DirContext.ADD_ATTRIBUTE;
      }
    } else {
      if (newVal == null) {
        result = DirContext.REMOVE_ATTRIBUTE;
      } else if (!currentVal.equals(newVal)) {
        result = DirContext.REPLACE_ATTRIBUTE;
      }
    }
    return result;
  }

  private ModificationItem getAttrModification(Attribute attr, String name, String value) throws NamingException {
    ModificationItem mi = null;
    int type = compareStringAttr(attr, value);
    if (type != 0) {
      if (attr == null) {
        attr = new BasicAttribute(name);
      }
      attr.set(0, value);
      mi = new ModificationItem(type, attr);
    }
    return mi;
  }

  private SearchControls getSimpleSearchControls() {
    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    searchControls.setTimeLimit(30000);
    return searchControls;
  }

  private ModificationItem getSshKeysModification(Attribute attribute, Collection<String> userKeys) throws NamingException {
    ModificationItem mi = null;
    if (attribute != null && attribute.size() > 0) {
      if (userKeys.isEmpty()) {
        mi = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attribute);
      } else {
        mi = compareSshKeys(attribute, userKeys);
      }
    } else if (!userKeys.isEmpty()) {
      attribute = new BasicAttribute(SSH_PUBLIC_KEY);
      for (String key : userKeys) {
        attribute.add(key);
      }
      mi = new ModificationItem(DirContext.ADD_ATTRIBUTE, attribute);
    }
    return mi;
  }

  private SearchResult getUserItem(String dn) throws NamingException {
    SearchResult result = null;
    try {
      NamingEnumeration<?> e = ctx.search(dn, "(objectclass=inetOrgPerson)", getSimpleSearchControls());
      while (e.hasMoreElements()) {
        if (result != null) {
          LOGGER.log(Level.SEVERE, "Found multiple DN: {0}", dn);
          break;
        }
        result = (SearchResult) e.next();
      }
    } catch (NameNotFoundException nfe) {
      LOGGER.log(Level.FINE, "User DN {0} not found.", dn);
    }
    return result;
  }

  private LDAPUser mkUser(SearchResult sr) throws NamingException {
    LDAPUser user = null;
    Attributes attrs = sr.getAttributes();
    user = new LDAPUser(sr.getNameInNamespace());
    user.setCn(stringValue(attrs, CN));
    user.setGivenName(stringValue(attrs, GIVEN_NAME));
    String pw = stringValue(attrs, USER_PASSWORD);
    if (pw != null && !pw.isEmpty()) {
      if (pw.charAt(0) == '{' && pw.indexOf('}', 3) > -1) {
        try {
          user.setPassword(pw.substring(pw.indexOf('}', 3) + 1));
          user.setHashMode(pw.substring(1, pw.indexOf('}', 3)));
        } catch (IllegalArgumentException ex) {
          LOGGER.log(Level.WARNING, ex, () -> "Unknown password hashing method in: " + pw);
        }
      } else {
        user.setPassword(pw);
      }
    }

    Attribute keys = attrs.get(SSH_PUBLIC_KEY);
    if (keys != null) {
      for (int i = 0; i < keys.size(); i++) {
        if (keys.get(i) != null)
          user.addSshPublicKey(((String)keys.get(i)).trim());
      }
    }
    keys = attrs.get(OBJECT_CLASS);
    if (keys != null) {
      for (int i = 0; i < keys.size(); i++) {
        if (keys.get(i) != null)
          user.addObjectclass(((String)keys.get(i)).trim());
      }
    }
    user.setSn(stringValue(attrs, SN));
    return user;
  }

  private String stringValue(Attribute attr) throws NamingException {
    if (attr != null) {
      if (attr.get() instanceof String) {
        return (String)attr.get();
      }
      if (attr.get() instanceof byte[]) {
        return new String((byte[]) attr.get(), StandardCharsets.UTF_8);
      }
    }
    return null;
  }

  private String stringValue(Attributes attrs, String id) throws NamingException {
    return stringValue(attrs.get(id));
  }
}
