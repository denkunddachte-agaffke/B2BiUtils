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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

import de.denkunddachte.exception.ApiException;
import de.denkunddachte.sfgapi.MailboxItem.Type;

public class Mailbox extends ApiClient {
  private static final String            PERMISSION            = "permission";
  private static final String            USERS                 = "users";
  private static final String            GROUPS                = "groups";
  private static final String            PATH                  = "path";
  private static final String            MAILBOX_TYPE          = "mailboxType";
  private static final String            LINKED_TO_MAILBOX     = "linkedToMailbox";
  private static final String            DESCRIPTION           = "description";
  private static final String            CREATE_PARENT_MAILBOX = "createParentMailbox";
  private static final Logger            LOGGER                = Logger.getLogger(Mailbox.class.getName());
  public static final String             TYPE_REGULAR          = "R";
  public static final String             TYPE_SHARED           = "S";
  public static final String             TYPE_LINKED           = "L";
  protected static final String          SVC_NAME              = "mailboxes";
  protected static final String          MBXCONTENTS_SVC       = "mailboxcontents";
  protected static final String          ID_PROPERTY           = "_id";

  private String                         path;
  private String                         description;
  private String                         permissionName;
  private String                         mailboxType;
  private int                            linkedToMailboxId;
  private Mailbox                        parentMailbox;
  private Mailbox                        linkedToMailbox;
  private final Map<String, UserGroup>   groups                = new LinkedHashMap<>();
  private final Map<String, UserAccount> users                 = new LinkedHashMap<>();

  private CreateParent                   createParentMailbox   = CreateParent.NO;
  private int                            mailboxId;
  private Collection<MailboxItem>        contents;

  public enum CreateParent {
    NO, INHERIT_NONE, INHERIT_FROM_CURRENT, INHERIT_FROM_PARENT
  }

  public Mailbox() {
    super();
  }

  public Mailbox(String path, String description) {
    this(path, description, CreateParent.NO);
  }

  public Mailbox(String path, String description, CreateParent createParentMailbox) {
    super();
    setPath(path);
    this.description = description;
    this.createParentMailbox = createParentMailbox;
    this.mailboxType = TYPE_REGULAR;
  }

  private Mailbox(JSONObject json) throws JSONException {
    super();
    this.readJSON(json);
  }

  @Override
  public String getId() {
    return getGeneratedId() == null ? String.valueOf(mailboxId) : getGeneratedId();
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
    json.put(CREATE_PARENT_MAILBOX, createParentMailbox == CreateParent.INHERIT_NONE);
    json.put(DESCRIPTION, description);
    if (linkedToMailboxId > 0) {
      json.put(LINKED_TO_MAILBOX, linkedToMailboxId);
    }
    json.put(MAILBOX_TYPE, mailboxType);
    json.put(PATH, path);
    if (!groups.isEmpty()) {
      json.put(GROUPS, String.join(",", groups.keySet()));
    }
    if (!users.isEmpty()) {
      json.put(USERS, String.join(",", users.keySet()));
    }
    return json;
  }

  @Override
  protected ApiClient readJSON(JSONObject json) throws JSONException {
    super.init(json);
    this.mailboxId = json.getInt(ID);
    this.path = json.getString(PATH);
    this.description = json.optString(DESCRIPTION);
    this.permissionName = json.optString(PERMISSION);
    if (json.has(MAILBOX_TYPE))
      this.mailboxType = getStringCode(json, MAILBOX_TYPE);
    this.linkedToMailboxId = json.optInt(LINKED_TO_MAILBOX);
    if (json.has(GROUPS) && !json.getString(GROUPS).isEmpty()) {
      for (String g : json.getString(GROUPS).split(",")) {
        if (!g.isEmpty())
          groups.put(g, null);
      }
    }
    if (json.has(USERS) && !json.getString(USERS).isEmpty()) {
      for (String u : json.getString(USERS).split(",")) {
        if (!u.isEmpty())
          users.put(u, null);
      }
    }
    return this;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    if (path == null || !path.startsWith("/")) {
      throw new IllegalArgumentException("Path must be provided as absolute path!");
    }
    this.path = path;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPermissionName() {
    if (permissionName == null) {
      return path + " Mailbox";
    }
    return permissionName;
  }

  private void setPermissionName(String permissionName) {
    this.permissionName = permissionName;
  }

  public String getMailboxType() {
    return mailboxType;
  }

  public void setMailboxType(String mailboxType) {
    this.mailboxType = mailboxType;
  }

  public int getLinkedToMailboxId() {
    return linkedToMailboxId;
  }

  public Mailbox getLinkedToMailbox() throws ApiException {
    if (linkedToMailbox == null && linkedToMailboxId > 0) {
      linkedToMailbox = Mailbox.find(linkedToMailboxId);
    }
    return linkedToMailbox;
  }

  public void setLinkedToMailbox(Mailbox linkedToMailbox) {
    this.linkedToMailbox = linkedToMailbox;
  }

  public CreateParent getCreateParentMailbox() {
    return createParentMailbox;
  }

  public void setCreateParentMailbox(CreateParent createParentMailbox) {
    this.createParentMailbox = createParentMailbox;
  }

  public int getMailboxId() {
    return mailboxId;
  }

  private void setMailboxId(int mailboxId) {
    this.mailboxId = mailboxId;
  }

  public String getParentPath() {
    int pos = path.lastIndexOf('/');
    if (pos > 1) {
      return path.substring(0, pos);
    }
    return "/";
  }

  public Mailbox getParent() throws ApiException {
    if (parentMailbox == null) {
      parentMailbox = Mailbox.find(getParentPath());
    }
    return parentMailbox;
  }

  public List<Mailbox> getParentMbxsToCreate() throws ApiException {
    List<Mailbox> result = new ArrayList<>();
    Mailbox mbx = this;
    while (mbx.getParent() == null) {
      mbx = new Mailbox(mbx.getParentPath(), getDescription());
      result.add(mbx);
    }
    Collections.reverse(result);
    return result;
  }

  public Mailbox getExistingParent() throws ApiException {
    Mailbox mbx = this;
    while (!mbx.getPath().equals("/")) {
      if (mbx.getParent() != null) {
        return mbx.getParent();
      }
      mbx = new Mailbox(mbx.getParentPath(), "-");
    }
    return null;
  }

  public Collection<String> getGroupNames() {
    return groups.keySet();
  }

  public void setGroupNames(String groupNames) {
    if (groupNames == null || groupNames.isEmpty()) {
      this.groups.clear();
    } else {
      setGroupNames(Arrays.asList(groupNames.split(",")));
    }
  }

  public void setGroupNames(Collection<String> groupNames) {
    if (groupNames == null || groupNames.isEmpty()) {
      this.groups.clear();
      return;
    }

    Iterator<String> iter = groups.keySet().iterator();
    while (iter.hasNext()) {
      String g = iter.next();
      if (!groupNames.contains(g)) {
        groups.remove(g);
      }
    }

    for (String g : groupNames) {
      if (!groups.containsKey(g)) {
        groups.put(g, null);
      }
    }
  }

  public UserGroup getGroup(String groupname) {
    if (groups.containsKey(groupname) && groups.get(groupname) == null) {
      try {
        groups.put(groupname, UserGroup.find(groupname));
      } catch (ApiException e) {
        LOGGER.warning("Could not find group " + groupname + "!");
      }
    }
    return groups.get(groupname);
  }

  public Collection<UserGroup> getGroups() {
    Collection<UserGroup> result = new ArrayList<>(groups.size());
    for (String group : groups.keySet()) {
      result.add(getGroup(group));
    }
    return result;
  }

  public UserGroup addGroup(UserGroup group) {
    return groups.put(group.getGroupName(), group);
  }

  public Collection<String> getUserNames() {
    return users.keySet();
  }

  public void setUserNames(String userNames) {
    if (userNames == null || userNames.isEmpty()) {
      this.users.clear();
    } else {
      setUserNames(Arrays.asList(userNames.split(",")));
    }
  }

  public void setUserNames(Collection<String> userNames) {
    if (userNames == null || userNames.isEmpty()) {
      this.users.clear();
      return;
    }

    Iterator<String> iter = users.keySet().iterator();
    while (iter.hasNext()) {
      String u = iter.next();
      if (!userNames.contains(u)) {
        groups.remove(u);
      }
    }

    for (String u : userNames) {
      if (!users.containsKey(u)) {
        users.put(u, null);
      }
    }
  }

  public UserAccount getUser(String userid) {
    if (users.containsKey(userid) && users.get(userid) == null) {
      try {
        users.put(userid, UserAccount.find(userid));
      } catch (ApiException e) {
        LOGGER.warning("Could not find user " + userid + "!");
      }
    }
    return users.get(userid);
  }

  public Collection<UserAccount> getUsers() {
    Collection<UserAccount> result = new ArrayList<>(users.size());
    for (String userid : users.keySet()) {
      result.add(getUser(userid));
    }
    return result;
  }

  public ApiClient addUser(UserAccount user) {
    return users.put(user.getUserId(), user);
  }

  public Collection<MailboxItem> getContents() throws ApiException {
    if (this.contents == null) {
      contents = new ArrayList<>();
      try {
        Map<String, Object> params = new HashMap<>();
        params.put("offset", 0);
        params.put("detail", "true");
        params.put("parentMailboxId", getMailboxId());
        while (true) {
          JSONArray jsonObjects = getJSONArray(get(MBXCONTENTS_SVC, params));
          for (int i = 0; i < jsonObjects.length(); i++) {
            contents.add(new MailboxItem(jsonObjects.getJSONObject(i)));
          }
          if (jsonObjects.length() < API_RANGESIZE)
            break;
        }
      } catch (JSONException | UnsupportedEncodingException e) {
        throw new ApiException(e);
      }
    }
    return this.contents;
  }

  public Collection<MailboxItem> getSubMailboxList() throws ApiException {
    return getContents().stream().filter(i -> i.getType() == Type.MAILBOX).collect(Collectors.toList());
  }

  public Collection<MailboxItem> getMessageList() throws ApiException {
    return getContents().stream().filter(i -> i.getType() == Type.MESSAGE).collect(Collectors.toList());
  }

  public Collection<Mailbox> getSubMailboxes(boolean recurse) throws ApiException {
    Collection<Mailbox> result = new ArrayList<>();
    for (MailboxItem mi : getSubMailboxList()) {
      Mailbox sub = Mailbox.find(mi.getId());
      if (sub == null) {
        throw new ApiException("Sub mailbox " + mi + " not found!");
      }
      result.add(sub);
      if (recurse) {
        result.addAll(sub.getSubMailboxes(recurse));
      }
    }
    return result;
  }

  @Override
  public boolean create() throws ApiException {
    if (createParentMailbox == CreateParent.NO || createParentMailbox == CreateParent.INHERIT_NONE) {
      return super.create();
    }

    boolean result = true;
    Collection<String> groupNames = null;
    Collection<String> userNames = null;

    if (createParentMailbox == CreateParent.INHERIT_FROM_CURRENT) {
      groupNames = getGroupNames();
      userNames = getUserNames();
    } else if (createParentMailbox == CreateParent.INHERIT_FROM_PARENT) {
      Mailbox p = getExistingParent();
      if (p != null) {
        groupNames = p.getGroupNames();
        userNames = p.getUserNames();
      }
    }

    for (Mailbox mbx : getParentMbxsToCreate()) {
      mbx.setGroupNames(groupNames);
      mbx.setUserNames(userNames);
      mbx.setCreateParentMailbox(CreateParent.NO);
      result = mbx.create();
      if (!result) {
        LOGGER.log(Level.WARNING, "Could not create path element {0}!", mbx.getPath());
        break;
      }
    }
    if (result) {
      result = super.create();
    } else {
      LOGGER.log(Level.WARNING, "Could not create mailbox {0} because parent(s) don't exist and could not be created!", getPath());
    }
    return result;
  }

  @Override
  public String toString() {
    return "Mailbox [mailboxId=" + mailboxId + ", path=" + path + ", description=" + description + ", mailboxType=" + mailboxType + ", linkedToMailbox="
        + linkedToMailbox + "]";
  }

  // static lookup methods:
  public static List<Mailbox> findAll() throws ApiException {
    return findAll(null);
  }

  public static List<Mailbox> findAll(String filter, String... includeFields) throws ApiException {
    return findAll(filter, false, includeFields);
  }

  public static List<Mailbox> findAll(String filter, boolean caseSensitive, String... includeFields) throws ApiException {
    if (useWsApi(SVC_NAME)) {
      return findAllWithWSApi(filter, caseSensitive);
    } else {
      return findAllWithRESTApi(filter, caseSensitive, includeFields);
    }
  }

  private static List<Mailbox> findAllWithWSApi(String filter, boolean caseSensitive) throws ApiException {
    List<Mailbox> result = new ArrayList<>();
    try {
      String param = "";
      if (filter != null) {
        param += "&searchFor=" + urlEncode(filter.replace('*', '%').replace('?', '_'));
      }
      if(caseSensitive) {
        param += "&casesensitive=1";
      }
      Document xmlDoc = getXmlDocumentFromWsApi(SVC_NAME, param);
      XPathFactory xPathFactory = XPathFactory.newInstance();
      XPath xpath = xPathFactory.newXPath();
      XPathExpression expr = xpath.compile("/result/row");
      NodeList nl = (NodeList) expr.evaluate(xmlDoc, XPathConstants.NODESET);

      for (int i = 0; i < nl.getLength(); i++) {
        Node n = nl.item(i);
        Mailbox mbx = new Mailbox((String) xpath.evaluate("./path", n, XPathConstants.STRING),
            (String) xpath.evaluate("./description", n, XPathConstants.STRING), CreateParent.NO);
        mbx.setGeneratedId((String) xpath.evaluate("./id", n, XPathConstants.STRING));
        mbx.setMailboxId(Integer.parseInt(mbx.getId()));
        mbx.setPermissionName((String) xpath.evaluate("./permission", n, XPathConstants.STRING));
        mbx.setMailboxType((String) xpath.evaluate("./mailboxType", n, XPathConstants.STRING));
        mbx.setUserNames((String) xpath.evaluate("./users", n, XPathConstants.STRING));
        mbx.setGroupNames((String) xpath.evaluate("./groups", n, XPathConstants.STRING));
        LOGGER.log(Level.FINER, "Got mailbox: {0}", mbx);
        result.add(mbx);
      }
    } catch (UnsupportedEncodingException | XPathExpressionException e) {
      throw new ApiException(e);
    }
    return result;
  }

  private static List<Mailbox> findAllWithRESTApi(String filter, boolean caseSensitive, String... includeFields) throws ApiException {
    List<Mailbox> result = new ArrayList<>();
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("offset", 0);
      params.put("includeFields", includeFields);
      params.put(PATH, (filter != null ? filter.replace('*', '%') : null));
      while (true) {
        JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
        for (int i = 0; i < jsonObjects.length(); i++) {
          if (caseSensitive) {
            if (!jsonObjects.getJSONObject(i).getString(PATH).matches(filter.replace('%', '*').replace("*", ".*") + ".*")) {
              continue;
            }
          }
          result.add(new Mailbox(jsonObjects.getJSONObject(i)));
        }
        if (jsonObjects.length() < API_RANGESIZE)
          break;
      }
    } catch (JSONException | UnsupportedEncodingException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public static Mailbox find(String path) throws ApiException {
    return find(path, ID_MATCH_CASE_SENSITVE);
  }

  public static Mailbox find(String path, boolean matchCaseSensitive) throws ApiException {
    Mailbox result = null;
    try {
      JSONObject json = getJSON(get(SVC_NAME, "?path=" + urlEncode(path)), PATH, path, matchCaseSensitive);
      if (json.has(ERROR_CODE)) {
        LOGGER.log(Level.FINE, "Mailbox {0} not found: errorCode={1}, errorDescription={2}.",
            new Object[] { path, json.getInt("errorCode"), json.get("errorDescription") });
      } else {
        result = new Mailbox(json);
        LOGGER.log(Level.FINER, "Found Mailbox {0}: {1}", new Object[] { path, result });
      }
    } catch (JSONException | UnsupportedEncodingException e) {
      throw new ApiException(e);
    }

    return result;
  }

  public static Mailbox find(long mailboxId) throws ApiException {
    Mailbox result = null;
    try {
      JSONObject json = findByKey(SVC_NAME, Long.toString(mailboxId));
      if (json.has(ERROR_CODE)) {
        LOGGER.log(Level.FINE, "Mailbox ID {0} not found: errorCode={1}, errorDescription={2}.",
            new Object[] { mailboxId, json.getInt("errorCode"), json.get("errorDescription") });
      } else {
        result = new Mailbox(json);
        LOGGER.log(Level.FINER, "Found Mailbox ID {0}: {1}", new Object[] { mailboxId, result });
      }
    } catch (JSONException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public static boolean exists(Mailbox mailbox) throws ApiException {
    if (mailbox.getMailboxId() > 0) {
      return exists(mailbox.getMailboxId());
    } else {
      return exists(mailbox.getPath(), ID_MATCH_CASE_SENSITVE);
    }
  }

  public static boolean exists(int mailboxId) throws ApiException {
    JSONObject json = findByKey(SVC_NAME, Integer.toString(mailboxId));
    return json.has(ID_PROPERTY);
  }

  public static boolean exists(String path, boolean matchCaseSensitive) throws ApiException {
    boolean result = false;
    JSONObject json;
    try {
      json = getJSON(get(SVC_NAME, "?path=" + urlEncode(path)), PATH, path, matchCaseSensitive);
    } catch (JSONException | UnsupportedEncodingException e) {
      throw new ApiException(e);
    }
    if (json != null && json.has(ID_PROPERTY)) {
      result = true;
    }
    return result;
  }
}
