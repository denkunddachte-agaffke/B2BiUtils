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
import java.util.HashMap;
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

import de.denkunddachte.exception.ApiException;

public class UserVirtualRoot extends ApiClient {
	private final static Logger		LOGGER		= Logger.getLogger(UserVirtualRoot.class.getName());
	protected static final String	SVC_NAME	= "uservirtualroots";
	protected static final String	ID_PROPERTY	= "userName";

	private String					userName;
	private String					mailboxPath;

	public UserVirtualRoot() {
		super();
	}

	public UserVirtualRoot(String userName, String mailboxPath) {
		super();
		this.userName = userName;
		this.mailboxPath = mailboxPath;
	}

	private UserVirtualRoot(JSONObject json) throws JSONException {
		super();
		this.readJSON(json);
	}

	@Override
	public String getId() {
    return getGeneratedId() == null ? userName : getGeneratedId();
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
		json.put("userName", userName);
		json.put("mailboxPath", mailboxPath);
		return json;
	}

	@Override
	protected ApiClient readJSON(JSONObject json) throws JSONException {
		super.init(json);
		this.userName = json.getString("userName");
		this.mailboxPath = json.getString("mailboxPath");
		return this;
	}

	public String getUserName() {
		return userName;
	}

	public String getMailboxPath() {
		return mailboxPath;
	}

	public void setMailboxPath(String mailboxPath) {
		this.mailboxPath = mailboxPath;
	}

	@Override
	public String toString() {
		return "UserVirtualRoot [userName=" + userName + ", mailboxPath=" + mailboxPath + "]";
	}

	// static lookup methods:
	public static List<UserVirtualRoot> findAll() throws ApiException {
		return findAll(null);
	}

	public static List<UserVirtualRoot> findAll(String filter, String... includeFields) throws ApiException {
		if (useWsApi(SVC_NAME)) {
			return findAllWithWSApi(filter);
		} else {
			return findAllWithRESTApi(filter, includeFields);
		}
	}

	private static List<UserVirtualRoot> findAllWithWSApi(String filter, String... includeFields) throws ApiException {
		List<UserVirtualRoot> result = new ArrayList<>();
		try {
			Document xmlDoc = getXmlDocumentFromWsApi(SVC_NAME,
					(filter != null ? "&searchFor=" + urlEncode(filter.replace('*', '%')) : null));
			XPathFactory xPathFactory = XPathFactory.newInstance();
			XPath xpath = xPathFactory.newXPath();
			XPathExpression expr = xpath.compile("/result/row");
			NodeList nl = (NodeList) expr.evaluate(xmlDoc, XPathConstants.NODESET);

			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				UserVirtualRoot vr = new UserVirtualRoot((String) xpath.evaluate("./userName", n, XPathConstants.STRING), (String) xpath.evaluate("./mailboxPath", n, XPathConstants.STRING));
				vr.setGeneratedId(vr.getId());
				LOGGER.log(Level.FINER, "Got UserVirtualRoot: {0}", vr);
				result.add(vr);
			}
		} catch (UnsupportedEncodingException | XPathExpressionException e) {
			throw new ApiException(e);
		}
		return result;
	}

	private static List<UserVirtualRoot> findAllWithRESTApi(String globPattern, String... includeFields) throws ApiException {
		List<UserVirtualRoot> result = new ArrayList<>();
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("offset", 0);
			params.put("includeFields", includeFields);
			params.put("userName", (globPattern != null ? globPattern.replace('*', '%') : null));
			while (true) {
				JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
				for (int i = 0; i < jsonObjects.length(); i++) {
					result.add(new UserVirtualRoot(jsonObjects.getJSONObject(i)));
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
	public static UserVirtualRoot find(String userName) throws ApiException {
		UserVirtualRoot result = null;
		JSONObject json = findByKey(SVC_NAME, userName);
		try {
			if (json.has("errorCode")) {
				LOGGER.finer("UserVirtualRoot " + userName + " not found: errorCode=" + json.getInt("errorCode")
						+ ", errorDescription=" + json.get("errorDescription") + ".");
			} else {
				result = new UserVirtualRoot(json);
				LOGGER.finer("Found UserVirtualRoot " + userName + ": " + result);
			}
		} catch (JSONException e) {
			throw new ApiException(e);
		}
		return result;
	}

	public static boolean exists(UserVirtualRoot userVirtualRoot) throws ApiException {
		return exists(userVirtualRoot.getId());
	}

	public static boolean exists(String userName) throws ApiException {
		JSONObject json = findByKey(SVC_NAME, userName);
		return json.has(ID_PROPERTY);
	}
}
