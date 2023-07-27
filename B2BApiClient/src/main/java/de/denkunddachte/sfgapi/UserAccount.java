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
package de.denkunddachte.sfgapi;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.denkunddachte.enums.AuthType;
import de.denkunddachte.enums.UserLanguage;
import de.denkunddachte.exception.ApiException;

public class UserAccount extends ApiClient {
  private static final Logger                     LOGGER               = Logger.getLogger(UserAccount.class.getName());
  protected static final String                   SVC_NAME             = "useraccounts";

  // JSON fields
  private static final String                     USER_IDENTITY        = "userIdentity";
  private static final String                     USER_ID              = "userId";
  private static final String                     SURNAME              = "surname";
  private static final String                     SESSION_TIMEOUT      = "sessionTimeout";
  private static final String                     PREFERRED_LANGUAGE   = "preferredLanguage";
  private static final String                     POLICY               = "policy";
  private static final String                     PERMISSIONS          = "permissions";
  private static final String                     PASSWORD             = "password";
  private static final String                     PAGER                = "pager";
  private static final String                     MANAGER_ID           = "managerId";
  private static final String                     GROUPS               = "groups";
  private static final String                     GIVEN_NAME           = "givenName";
  private static final String                     EMAIL                = "email";
  private static final String                     AUTHORIZED_USER_KEYS = "authorizedUserKeys";
  private static final String                     NAME                 = "name";
  private static final String                     AUTHENTICATION_TYPE  = "authenticationType";
  private static final String                     AUTHENTICATION_HOST  = "authenticationHost";

  protected static final String                   ID_PROPERTY          = USER_ID;

  // API fields
  private String                                  authenticationHost;
  private AuthType                                authenticationType;
  private final Map<String, SshAuthorizedUserKey> authorizedUserKeys   = new LinkedHashMap<>();
  private String                                  usereMail;
  private String                                  givenName;
  private final Map<String, UserGroup>            assignedGroups       = new LinkedHashMap<>();
  private String                                  managerId;
  private String                                  userPager;
  private final Map<String, Permission>           assignedPermissions  = new LinkedHashMap<>();
  private UserLanguage                            preferredLanguage;
  private String                                  userPolicy;
  private int                                     sessionTimeout;
  private String                                  userSurname;
  private String                                  userId;
  private String                                  userIdentity;
  private String                                  userPassword;

  public UserAccount() {
    super();
  }

  public UserAccount(String userId, String surname, String givenName, AuthType authenticationType) {
    super();
    this.userId = userId;
    this.userSurname = surname;
    this.givenName = givenName;
    this.authenticationType = authenticationType;
    this.preferredLanguage = UserLanguage.EN;
  }

  private UserAccount(JSONObject json) throws JSONException {
    super();
    this.readJSON(json);
  }

  @Override
  public String getId() {
    return getGeneratedId() == null ? userId : getGeneratedId();
  }

  @Override
  public String getIdProperty() {
    return ID_PROPERTY;
  }

  @Override
  public String getServiceName() {
    return SVC_NAME;
  }

  @Override
  public JSONObject toJSON() throws JSONException {
    JSONObject json = new JSONObject();
    json.put(AUTHENTICATION_HOST, authenticationHost);
    json.put(AUTHENTICATION_TYPE, authenticationType.toString());
    if (!authorizedUserKeys.isEmpty()) {
      JSONArray a = new JSONArray();
      for (String keyName : authorizedUserKeys.keySet()) {
        a.put((new JSONObject()).put(NAME, keyName));
      }
      json.put(AUTHORIZED_USER_KEYS, a);
    }
    json.put(EMAIL, usereMail);
    json.put(GIVEN_NAME, givenName);
    if (!assignedGroups.isEmpty()) {
      JSONArray a = new JSONArray();
      for (String g : assignedGroups.keySet()) {
        a.put((new JSONObject()).put(NAME, g));
      }
      json.put(GROUPS, a);
    }
    json.put(MANAGER_ID, managerId);
    json.put(PAGER, userPager);
    json.put(PASSWORD, userPassword);
    if (!assignedPermissions.isEmpty()) {
      JSONArray a = new JSONArray();
      for (String p : assignedPermissions.keySet()) {
        a.put((new JSONObject()).put(NAME, p));
      }
      json.put(PERMISSIONS, a);
    }

    json.put(POLICY, userPolicy);
    json.put(PREFERRED_LANGUAGE, preferredLanguage.getCode());
    json.put(SESSION_TIMEOUT, (sessionTimeout > 0 ? sessionTimeout : null));
    json.put(SURNAME, userSurname);
    json.put(USER_ID, userId);
    json.put(USER_IDENTITY, userIdentity);
    return json;
  }

  @Override
  protected ApiClient readJSON(JSONObject json) throws JSONException {
    super.init(json);
    this.userId = json.getString(USER_ID);
    this.userSurname = json.optString(SURNAME);
    this.givenName = json.optString(GIVEN_NAME);
    if (json.has(AUTHENTICATION_TYPE))
      this.authenticationType = AuthType.valueOf(json.getJSONObject(AUTHENTICATION_TYPE).getString(CODE));

    this.usereMail = json.optString(EMAIL);
    if (json.has(GROUPS)) {
      JSONArray a = json.getJSONArray(GROUPS);
      for (int i = 0; i < a.length(); i++) {
        assignedGroups.put(a.getJSONObject(i).getString(NAME), null);
      }
    }
    if (json.has(PERMISSIONS)) {
      JSONArray perms = json.getJSONArray(PERMISSIONS);
      for (int i = 0; i < perms.length(); i++) {
        assignedPermissions.put(perms.getJSONObject(i).getString(NAME), null);
      }
    }
    if (json.has(PREFERRED_LANGUAGE))
      this.preferredLanguage = UserLanguage.getByCode(json.getJSONObject(PREFERRED_LANGUAGE).getString(CODE));
    this.sessionTimeout = json.optInt(SESSION_TIMEOUT);
    this.userIdentity = json.optString(USER_IDENTITY);
    if (json.has(AUTHORIZED_USER_KEYS)) {
      JSONArray a = json.getJSONArray(AUTHORIZED_USER_KEYS);
      for (int i = 0; i < a.length(); i++) {
        authorizedUserKeys.put(a.getJSONObject(i).getString(NAME), null);
      }
    }
    this.authenticationHost = json.optString(AUTHENTICATION_HOST);
    this.managerId = json.optString(MANAGER_ID);
    this.userPager = json.optString(PAGER);
    this.userPolicy = json.optString(POLICY);
    this.userIdentity = json.optString(USER_IDENTITY);
    this.userPassword = json.optString(PASSWORD);

    return this;
  }

  public String getAuthenticationHost() {
    return authenticationHost;
  }

  public void setAuthenticationHost(String authenticationHost) {
    this.authenticationHost = authenticationHost;
  }

  public AuthType getAuthenticationType() {
    return authenticationType;
  }

  public void setAuthenticationType(AuthType authenticationType) {
    this.authenticationType = authenticationType;
  }

  public String getEmail() {
    return usereMail;
  }

  public void setEmail(String email) {
    this.usereMail = email;
  }

  public String getGivenName() {
    return givenName;
  }

  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }

  public String getManagerId() {
    return managerId;
  }

  public void setManagerId(String managerId) {
    this.managerId = managerId;
  }

  public String getPager() {
    return userPager;
  }

  public void setPager(String pager) {
    if (pager.length() > 30) {
      LOGGER.log(Level.WARNING, "Value for pager ({0}) exceeds 30 charcters. Value will be truncated.", pager);
      this.userPager = pager.substring(0, 30);
    } else {
      this.userPager = pager;
    }
  }

  public UserLanguage getPreferredLanguage() {
    return preferredLanguage;
  }

  public void setPreferredLanguage(UserLanguage preferredLanguage) {
    this.preferredLanguage = preferredLanguage;
  }

  public String getPolicy() {
    return userPolicy;
  }

  public void setPolicy(String policy) {
    this.userPolicy = policy;
  }

  public int getSessionTimeout() {
    return sessionTimeout;
  }

  public void setSessionTimeout(int sessionTimeout) {
    this.sessionTimeout = sessionTimeout;
  }

  public String getSurname() {
    return userSurname;
  }

  public void setSurname(String surname) {
    this.userSurname = surname;
  }

  public String getUserId() {
    return userId;
  }

  public String getUserIdentity() {
    return userIdentity;
  }

  public void setUserIdentity(String userIdentity) {
    this.userIdentity = userIdentity;
  }

  public Collection<String> getAuthorizedUserKeyNames() {
    return authorizedUserKeys.keySet();
  }

  public Collection<SshAuthorizedUserKey> getAuthorizedUserKeys() {
    Collection<SshAuthorizedUserKey> result = new ArrayList<>(authorizedUserKeys.size());
    for (String keyName : authorizedUserKeys.keySet()) {
      result.add(getAuthorizedUserKey(keyName));
    }
    return result;
  }

  public SshAuthorizedUserKey getAuthorizedUserKey(String keyName) {
    if (authorizedUserKeys.get(keyName) == null) {
      try {
        authorizedUserKeys.put(keyName, SshAuthorizedUserKey.find(keyName));
      } catch (ApiException e) {
        LOGGER.warning("Could not find SshAuthorizedUserKey " + keyName + "!");
      }
    }
    return authorizedUserKeys.get(keyName);
  }

  public SshAuthorizedUserKey addAuthorizedUserKey(SshAuthorizedUserKey key) {
    return authorizedUserKeys.put(key.getKeyName(), key);
  }

  public void addAuthorizedUserKey(String keyName) {
    if (!authorizedUserKeys.containsKey(keyName)) {
      authorizedUserKeys.put(keyName, null);
    }
  }

  public void clearAuthorizedUserKeys() {
    authorizedUserKeys.clear();
  }

  public SshAuthorizedUserKey removeAuthorizedUserKey(SshAuthorizedUserKey key) {
    return authorizedUserKeys.remove(key.getKeyName());
  }

  public SshAuthorizedUserKey removeAuthorizedUserKey(String keyName) {
    return authorizedUserKeys.remove(keyName);
  }

  public Collection<String> getGroupNames() {
    return assignedGroups.keySet();
  }

  public UserGroup getGroup(String groupname) {
    if (assignedGroups.get(groupname) == null) {
      try {
        assignedGroups.put(groupname, UserGroup.find(groupname));
      } catch (ApiException e) {
        LOGGER.warning("Could not find group " + groupname + "!");
      }
    }
    return assignedGroups.get(groupname);
  }

  public Collection<UserGroup> getGroups() {
    Collection<UserGroup> result = new ArrayList<>(assignedGroups.size());
    for (String group : assignedGroups.keySet()) {
      result.add(getGroup(group));
    }
    return result;
  }

  public UserGroup addGroup(UserGroup group) {
    return assignedGroups.put(group.getGroupName(), group);
  }
  
  public void clearGroups() {
    assignedGroups.clear();
  }

  public UserGroup removeGroup(UserGroup group) {
    return assignedGroups.remove(group.getGroupName());
  }

  public UserGroup removeGroup(String groupName) {
    return assignedGroups.remove(groupName);
  }

  public Collection<String> getPermissionNames() {
    return assignedPermissions.keySet();
  }

  public Permission getPermission(String permname) {
    if (assignedPermissions.get(permname) == null) {
      try {
        assignedPermissions.put(permname, Permission.find(permname));
      } catch (ApiException e) {
        LOGGER.warning("Could not find permission " + permname + "!");
      }
    }
    return assignedPermissions.get(permname);
  }

  public Collection<Permission> getPermissions() {
    Collection<Permission> result = new ArrayList<>(assignedPermissions.size());
    for (String perm : assignedPermissions.keySet()) {
      result.add(getPermission(perm));
    }
    return result;
  }

  public void addPermissionName(String permissionName) {
    assignedPermissions.put(permissionName, null);
  }

  public Permission addPermission(Permission perm) {
    return assignedPermissions.put(perm.getPermissionName(), perm);
  }

  public void clearPermissions() {
    assignedPermissions.clear();
  }

  public Permission removePermission(Permission permission) {
    return assignedPermissions.remove(permission.getPermissionName());
  }

  public Permission removePermission(String permissionName) {
    return assignedPermissions.remove(permissionName);
  }

  public void setPassword(String password) {
    this.userPassword = password;
  }

  // static lookup methods:
  public static List<UserAccount> findAll() throws ApiException {
    return findAll(null);
  }

  public static List<UserAccount> findAll(String filter, String... includeFields) throws ApiException {
    if (useWsApi(SVC_NAME)) {
      return findAllWithWSApi(filter);
    } else {
      return findAllWithRESTApi(filter, includeFields);
    }
  }

  private static List<UserAccount> findAllWithWSApi(String filter) throws ApiException {
    List<UserAccount> result = new ArrayList<>();
    try {
      Document xmlDoc = getXmlDocumentFromWsApi(SVC_NAME, (filter != null ? "&searchFor=" + urlEncode(filter.replace('*', '%')) : null));
      XPathFactory xPathFactory = XPathFactory.newInstance();
      XPath xpath = xPathFactory.newXPath();
      XPathExpression expr = xpath.compile("/result/row");
      NodeList nl = (NodeList) expr.evaluate(xmlDoc, XPathConstants.NODESET);

      for (int i = 0; i < nl.getLength(); i++) {
        Node n = nl.item(i);
        UserAccount ua = new UserAccount((String) xpath.evaluate("./id", n, XPathConstants.STRING),
            (String) xpath.evaluate("./surname", n, XPathConstants.STRING), (String) xpath.evaluate("./givenName", n, XPathConstants.STRING), AuthType.Local);
        ua.setGeneratedId(ua.getId());
        ua.setEmail((String) xpath.evaluate("./email", n, XPathConstants.STRING));
        for (String groupName : ((String) xpath.evaluate("./grouplist", n, XPathConstants.STRING)).split("\\s*,\\s*")) {
          ua.assignedGroups.put(groupName, null);
        }
        LOGGER.log(Level.FINER, "Got user: {0}", ua);
        result.add(ua);
      }
    } catch (UnsupportedEncodingException | XPathExpressionException e) {
      throw new ApiException(e);
    }
    return result;
  }

  private static List<UserAccount> findAllWithRESTApi(String globPattern, String... includeFields) throws ApiException {
    List<UserAccount> result = new ArrayList<>();
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("offset", 0);
      params.put("includeFields", includeFields);
      params.put("searchFor", (globPattern != null ? globPattern.replace('*', '%') : null));
      while (true) {
        JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
        for (int i = 0; i < jsonObjects.length(); i++) {
          result.add(new UserAccount(jsonObjects.getJSONObject(i)));
        }
        if (jsonObjects.length() < API_RANGESIZE)
          break;
      }
    } catch (JSONException | UnsupportedEncodingException e) {
      throw new ApiException(e);
    }
    return result;
  }

  // find by key
  public static UserAccount find(String userId) throws ApiException {
    UserAccount result = null;
    JSONObject json = findByKey(SVC_NAME, userId);
    try {
      if (json.has(ERROR_CODE)) {
        LOGGER.log(Level.FINER, "UserAccount {0} not found: errorCode={1}, errorDescription={2}.",
            new Object[] { userId, json.getInt(ERROR_CODE), json.get(ERROR_DESCRIPTION) });
      } else {
        result = new UserAccount(json);
        LOGGER.log(Level.FINER, "Found UserAccount {0}: {1}", new Object[] { userId, result });
      }
    } catch (JSONException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public static boolean exists(ApiClient userAccount) throws ApiException {
    return exists(userAccount.getId());
  }

  public static boolean exists(String userId) throws ApiException {
    JSONObject json = findByKey(SVC_NAME, userId);
    return json.has(ID_PROPERTY);
  }

  @Override
  public String toString() {
    return "UserAccount [userId=" + userId + ", surname=" + userSurname + ", givenName=" + givenName + ", email=" + usereMail + ", authorizedUserKeys="
        + authorizedUserKeys.keySet() + ", groups=" + assignedGroups.keySet() + "]";
  }
}
