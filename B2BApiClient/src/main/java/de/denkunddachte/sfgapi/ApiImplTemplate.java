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

/**
 * Template for new API 
 * Hints for notepad++
 * * copy field table from from api doc
 * * create list of fields with regex replace ^(\S+)\t.+\t(\S+) -> private $2 $1;
 * * create field name list  with regex replace ^(\S+)\t.+\t(\S+) -> $1 (copy to two buffers)
 * * camel convert in buffer 1 with regex replace ([a-z])([A-Z]) -> $1_$2 and convert all to UPPPER case
 * * append blanks to first line to make it the widest line
 * * created quoted list in buffer 2 with regex replace ^(\S+) -> "$1"
 * * copy quoted list in column mode (ALT+mouse), CTRL-X
 * * place cursor at EOL of line 1 in buffer 1 and paste with CTRL-V
 * * convert to constant list with regex replace ^(\S+)\s+(\S+) -> private static final String $1 = $2; 
 * @author chef
 *
 */
public class ApiImplTemplate extends ApiClient {
	private static final Logger		LOGGER		= Logger.getLogger(ApiImplTemplate.class.getName());
	protected static final String	SVC_NAME	= "apiname"; // as in B2BAPIs/svc/apiname

	// JSON fields
	private static final String		ID_FIELD	= "idField"; // e.g. userId

	protected static final String	ID_PROPERTY	= ID_FIELD;  //

	// API fields
	String							idField;

	// Constructors
	public ApiImplTemplate() {
		super();
	}

	// Constructor NEW
	public ApiImplTemplate(String idField) {
		super();
		this.idField = idField;
	}

	// Constructor from server
	private ApiImplTemplate(JSONObject json) throws JSONException {
		super();
		this.readJSON(json);
	}

	@Override
	public String getId() {
    return getGeneratedId() == null ? idField : getGeneratedId();
	}

	@Override
	public String getIdProperty() {
		return ID_PROPERTY;
	}

	@Override
	public String getServiceName() {
		return SVC_NAME;
	}

	/**
	 * Create JSON for CREATE, UPDATE
	 */
	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put(ID_FIELD, this.idField);
		return json;
	}

	@Override
	protected ApiClient readJSON(JSONObject json) throws JSONException {
		super.init(json);
		this.idField = json.getString(ID_FIELD);
		return this;
	}

	// Getters and setters
	public String getIdField() {
		return idField;
	}

	public void setIdField(String idField) {
		this.idField = idField;
	}

	@Override
	public String toString() {
		return "ApiImplTemplate [idField=" + idField + "]";
	}

	// static lookup methods:
	public static List<ApiImplTemplate> findAll() throws ApiException {
		return findAll(null);
	}

	// WS (XML) based API supported
	// public static List<ApiImplTemplate> findAll(String filter, String... includeFields) throws ApiException {
	// if (useWsApi(SVC_NAME)) {
	// return findAllWithWSApi(filter);
	// } else {
	// return findAllWithRESTApi(filter, includeFields);
	// }
	// }
	// private static List<ApiImplTemplate> findAllWithRESTApi(String filter, String... includeFields) throws ApiException {
	public static List<ApiImplTemplate> findAll(String globPattern, String... includeFields) throws ApiException {
		List<ApiImplTemplate> result = new ArrayList<>();
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("offset", 0);
			params.put("includeFields", includeFields);
			params.put("searchFor", (globPattern != null ? globPattern.replace('*', '%') : null));
			while (true) {
				JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
				for (int i = 0; i < jsonObjects.length(); i++) {
					result.add(new ApiImplTemplate(jsonObjects.getJSONObject(i)));
				}
				if (jsonObjects.length() < API_RANGESIZE)
					break;
			}
		} catch (JSONException | UnsupportedEncodingException e) {
			throw new ApiException(e);
		}
		return result;
	}

	private static List<ApiImplTemplate> findAllWithWSApi(String filter) throws ApiException {
		List<ApiImplTemplate> result = new ArrayList<>();
		try {
			Document xmlDoc = getXmlDocumentFromWsApi(SVC_NAME, (filter != null ? "&searchFor=" + urlEncode(filter.replace('*', '%')) : null));
			XPathFactory xPathFactory = XPathFactory.newInstance();
			XPath xpath = xPathFactory.newXPath();
			XPathExpression expr = xpath.compile("/result/row");
			NodeList nl = (NodeList) expr.evaluate(xmlDoc, XPathConstants.NODESET);

			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				ApiImplTemplate item = new ApiImplTemplate((String) xpath.evaluate("./_id", n, XPathConstants.STRING));
				item.setGeneratedId(item.getIdField());
				LOGGER.log(Level.FINER, "Got item: {0}", item);
				result.add(item);
			}
		} catch (UnsupportedEncodingException | XPathExpressionException e) {
			throw new ApiException(e);
		}
		return result;
	}

	// find() and exists() can be implemented in 2 different ways depending on API
	// 1) use lookup with parameter (e.g. ?path=<path>)
	// 2) REST style with .../svc/<svcname>/<key> (key being e.g. username)
	public static ApiImplTemplate find(String id) throws ApiException {
		ApiImplTemplate result = null;
		JSONObject json = findByKey(SVC_NAME, id);
		try {
			if (json.has(ERROR_CODE)) {
				LOGGER.log(Level.FINER, "ApiImplTemplate {0} not found: errorCode={1}, errorDescription={2}.",
						new Object[] { id, json.getInt(ERROR_CODE), json.get(ERROR_DESCRIPTION) });
			} else {
				result = new ApiImplTemplate(json);
				LOGGER.log(Level.FINER, "Found UserAccount {0}: {1}", new Object[] { id, result });
			}
		} catch (JSONException e) {
			throw new ApiException(e);
		}
		return result;
	}

	public static boolean exists(ApiImplTemplate item) throws ApiException {
		return exists(item.getId());
	}

	public static boolean exists(String id) throws ApiException {
		JSONObject json = findByKey(SVC_NAME, id);
		return json.has(ID_PROPERTY);
	}
}
