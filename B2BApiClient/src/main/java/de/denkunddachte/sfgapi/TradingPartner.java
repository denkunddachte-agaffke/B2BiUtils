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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.denkunddachte.enums.AuthType;
import de.denkunddachte.enums.PartnerRegion;
import de.denkunddachte.enums.PartnerTimeZone;
import de.denkunddachte.exception.ApiException;

public class TradingPartner extends ApiClient {
  private static final Logger            LOGGER           = Logger.getLogger(TradingPartner.class.getName());
  protected static final String          SVC_NAME         = "tradingpartners";
  private static final String            ID_PROPERTY      = "partnerName";
  public static final PartnerRegion      DEFAULT_REGION   = PartnerRegion.DE;
  public static final PartnerTimeZone    DEFAULT_TIMEZONE = PartnerTimeZone.E01; // (GMT+01:00)

  private String                         partnerName; // ID
  private boolean                        asciiArmor;
  private String                         addressLine1;
  private String                         addressLine2;
  private String                         authenticationHost;
  private AuthType                       authenticationType;
  private String                         authorizedUserKeyName;
  private String                         city;
  private String                         code;
  private Community                      community;
  private PartnerRegion                  countryOrRegion;
  private boolean                        doesRequireCompressedData;
  private boolean                        doesRequireEncryptedData;
  private boolean                        doesRequireSignedData;
  private boolean                        doesUseSSH;
  private String                         emailAddress;
  private boolean                        useGlobalMailbox;
  private String                         givenName;
  private boolean                        keyEnabled;
  private String                         passwordPolicy;
  private String                         postalCode;
  private int                            sessionTimeout;
  private String                         stateOrProvince;
  private String                         surname;
  private boolean                        textMode;
  private PartnerTimeZone                timeZone;
  private String                         username;
  private String                         phone;
  private CdConfiguration                consumerCdConfiguration;
  private CdConfiguration                producerCdConfiguration;
  private FtpConfiguration               consumerFtpConfiguration;
  private FtpConfiguration               producerFtpConfiguration;
  private FtpsConfiguration              consumerFtpsConfiguration;
  private FtpsConfiguration              producerFtpsConfiguration;
  private ConsumerWebSphereConfiguration consumerWsConfiguration;
  private SshConfiguration               consumerSshConfiguration;
  private SshConfiguration               producerSshConfiguration;
  private boolean                        isInitiatingConsumer;
  private boolean                        isInitiatingProducer;
  private boolean                        isListeningConsumer;
  private boolean                        isListeningProducer;
  private String                         remoteFilePattern;
  private String                         mailbox;
  private int                            pollingInterval;
  private String                         publicKeyID;
  private String                         customProtocolName;
  private String                         customProtocolExtensions;
  private boolean                        appendSuffixToUsername;
  private String                         password;
  private String                         communityName;
  private String                         targetFileEncoding;

  public TradingPartner(String partnerName, AuthType authenticationType, String eMail, String givenName, String surname, String username, Community community,
      String phone) {
    super();
    this.partnerName = partnerName;
    this.authenticationType = authenticationType;
    this.emailAddress = eMail;
    this.givenName = givenName;
    this.surname = surname;
    this.username = username;
    setCommunity(community);
    this.phone = phone;

    this.countryOrRegion = DEFAULT_REGION;
    this.timeZone = DEFAULT_TIMEZONE;
  }

  private TradingPartner(JSONObject json) throws JSONException {
    super();
    this.readJSON(json);
  }

  @Override
  public String getId() {
    return getGeneratedId() == null ? partnerName : getGeneratedId();
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
    // FIXME: put does not add key if value is null. If B2BApi needs any of these keys, then we have to put the string "null"
    // (or something more useful) into these values...
    if (addressLine1 != null)
      json.put("addressLine1", addressLine1);
    if (addressLine2 != null)
      json.put("addressLine2", addressLine2);
    json.put("appendSuffixToUsername", appendSuffixToUsername);
    json.put("asciiArmor", asciiArmor);
    json.put("authenticationHost", authenticationHost);
    json.put("authenticationType", authenticationType);
    json.put("authorizedUserKeyName", authorizedUserKeyName);
    if (city != null)
      json.put("city", city);
    if (code != null)
      json.put("code", code);
    json.put("community", communityName);
    json.put("consumerCdConfiguration", (consumerCdConfiguration != null ? consumerCdConfiguration.toJSON() : null));
    json.put("consumerFtpConfiguration", (consumerFtpConfiguration != null ? consumerFtpConfiguration.toJSON() : null));
    json.put("consumerFtpsConfiguration", (consumerFtpsConfiguration != null ? consumerFtpsConfiguration.toJSON() : null));
    json.put("consumerSshConfiguration", (consumerSshConfiguration != null ? consumerSshConfiguration.toJSON() : null));
    json.put("consumerWsConfiguration", (consumerWsConfiguration != null ? consumerWsConfiguration.toJSON() : null));
    json.put("countryOrRegion", countryOrRegion.name());
    json.put("customProtocolExtensions", customProtocolExtensions);
    json.put("customProtocolName", customProtocolName);
    json.put("doesRequireCompressedData", doesRequireCompressedData);
    json.put("doesRequireEncryptedData", doesRequireEncryptedData);
    json.put("doesRequireSignedData", doesRequireSignedData);
    json.put("doesUseSSH", doesUseSSH);
    json.put("emailAddress", emailAddress);
    json.put("givenName", givenName);
    json.put("isInitiatingConsumer", isInitiatingConsumer);
    json.put("isInitiatingProducer", isInitiatingProducer);
    json.put("isListeningConsumer", isListeningConsumer);
    json.put("isListeningProducer", isListeningProducer);
    json.put("keyEnabled", keyEnabled);
    json.put("mailbox", mailbox);
    json.put("partnerName", partnerName);
    json.put("password", password);
    json.put("passwordPolicy", passwordPolicy);
    json.put("phone", phone);
    json.put("pollingInterval", pollingInterval);
    json.put("postalCode", postalCode);
    json.put("producerCdConfiguration", (producerCdConfiguration != null ? producerCdConfiguration.toJSON() : null));
    json.put("producerFtpConfiguration", (producerFtpConfiguration != null ? producerFtpConfiguration.toJSON() : null));
    json.put("producerFtpsConfiguration", (producerFtpsConfiguration != null ? producerFtpsConfiguration.toJSON() : null));
    json.put("producerSshConfiguration", (producerSshConfiguration != null ? producerSshConfiguration.toJSON() : null));
    if (publicKeyID != null)
      json.put("publicKeyID", publicKeyID);
    json.put("remoteFilePattern", remoteFilePattern);
    json.put("sessionTimeout", sessionTimeout);
    if (stateOrProvince != null)
      json.put("stateOrProvince", stateOrProvince);
    json.put("surname", surname);
    json.put("textMode", textMode);
    json.put("timeZone", timeZone.getCode());
    json.put("useGlobalMailbox", useGlobalMailbox);
    json.put("username", username);
    json.put("targetFileEncoding", targetFileEncoding);

    return json;
  }

  @Override
  protected TradingPartner readJSON(JSONObject json) throws JSONException {
    super.init(json);
    LOGGER.log(Level.FINEST, "json={0}", json);
    this.partnerName = json.getString("partnerName");
    this.authenticationType = AuthType.valueOf(json.getJSONObject("authenticationType").getString("code"));
    this.emailAddress = json.getString("emailAddress");
    this.givenName = json.optString("givenName");
    this.surname = json.optString("surname");
    this.username = json.optString("username");
    this.setCommunityName(json.getString("community"));
    this.phone = json.getString("phone");
    this.asciiArmor = json.getJSONObject("asciiArmor").getBoolean("code");
    this.addressLine1 = json.getString("addressLine1");
    this.addressLine2 = json.getString("addressLine2");
    this.city = json.getString("city");
    this.code = json.getString("code");
    this.countryOrRegion = PartnerRegion.valueOf(json.getJSONObject("countryOrRegion").getString("code"));
    this.doesRequireCompressedData = json.getJSONObject("doesRequireCompressedData").getBoolean("code");
    this.doesRequireEncryptedData = json.getJSONObject("doesRequireEncryptedData").getBoolean("code");
    this.doesRequireSignedData = json.getJSONObject("doesRequireSignedData").getBoolean("code");
    this.doesUseSSH = json.getJSONObject("doesUseSSH").getBoolean("code");
    this.useGlobalMailbox = json.getJSONObject("useGlobalMailbox").getBoolean("code");
    this.keyEnabled = json.getJSONObject("keyEnabled").getBoolean("code");
    this.sessionTimeout = json.optInt("sessionTimeout");
    this.stateOrProvince = json.getString("stateOrProvince");
    this.textMode = json.getJSONObject("textMode").getBoolean("code");
    if (json.has("timeZone")) {
      this.timeZone = PartnerTimeZone.getByCode(json.getJSONObject("timeZone").getString("code"));
    } else {
      this.timeZone = DEFAULT_TIMEZONE;
    }
    this.isInitiatingConsumer = json.getJSONObject("isInitiatingConsumer").getBoolean("code");
    this.isInitiatingProducer = json.getJSONObject("isInitiatingProducer").getBoolean("code");
    this.isListeningConsumer = json.getJSONObject("isListeningConsumer").getBoolean("code");
    this.isListeningProducer = json.getJSONObject("isListeningProducer").getBoolean("code");
    this.publicKeyID = json.getString("publicKeyID");

    if (authenticationType == AuthType.External) {
      this.authenticationHost = json.getString("authenticationHost");
    } else {
      this.passwordPolicy = json.optString("passwordPolicy");
      this.password = json.optString("password");
    }

    if (json.has("consumerCdConfiguration")) {
      JSONObject j = json.getJSONObject("consumerCdConfiguration");
      if (j.keys().hasNext()) {
        this.consumerCdConfiguration = new CdConfiguration(CdConfiguration.Role.CONSUMER, j);
      }
    }
    if (json.has("producerCdConfiguration")) {
      JSONObject j = json.getJSONObject("producerCdConfiguration");
      if (j.keys().hasNext()) {
        this.producerCdConfiguration = new CdConfiguration(CdConfiguration.Role.PRODUCER, j);
      }
    }
    if (json.has("consumerSshConfiguration")) {
      JSONObject j = json.getJSONObject("consumerSshConfiguration");
      if (j.keys().hasNext()) {
        this.consumerSshConfiguration = new SshConfiguration(j);
      }
    }
    if (json.has("producerSshConfiguration")) {
      JSONObject j = json.getJSONObject("producerSshConfiguration");
      if (j.keys().hasNext()) {
        this.producerSshConfiguration = new SshConfiguration(j);
      }
    }
    if (json.has("consumerFtpConfiguration")) {
      JSONObject j = json.getJSONObject("consumerFtpConfiguration");
      if (j.keys().hasNext()) {
        this.consumerFtpConfiguration = new FtpConfiguration(FtpConfiguration.Role.CONSUMER, j);
      }
    }
    if (json.has("producerFtpConfiguration")) {
      JSONObject j = json.getJSONObject("producerFtpConfiguration");
      if (j.keys().hasNext()) {
        this.producerFtpConfiguration = new FtpConfiguration(FtpConfiguration.Role.PRODUCER, j);
      }
    }
    if (json.has("consumerFtpsConfiguration")) {
      JSONObject j = json.getJSONObject("consumerFtpsConfiguration");
      if (j.keys().hasNext()) {
        this.consumerFtpsConfiguration = new FtpsConfiguration(FtpConfiguration.Role.CONSUMER, j);
      }
    }
    if (json.has("producerFtpsConfiguration")) {
      JSONObject j = json.getJSONObject("producerFtpsConfiguration");
      if (j.keys().hasNext()) {
        this.producerFtpsConfiguration = new FtpsConfiguration(FtpConfiguration.Role.PRODUCER, j);
      }
    }

    if (json.has("customProtocolName")) {
      this.customProtocolName = json.getString("customProtocolName");
    }

    if (json.has("customProtocolExtensions")) {
      this.customProtocolExtensions = json.getString("customProtocolExtensions");
    }

    this.targetFileEncoding = json.optString("targetFileEncoding");
    return this;
  }

  public String getPartnerName() {
    return partnerName;
  }

  public boolean isAsciiArmor() {
    return asciiArmor;
  }

  public void setAsciiArmor(boolean asciiArmor) {
    this.asciiArmor = asciiArmor;
  }

  public String getAddressLine1() {
    return addressLine1;
  }

  public void setAddressLine1(String addressLine1) {
    this.addressLine1 = addressLine1;
  }

  public String getAddressLine2() {
    return addressLine2;
  }

  public void setAddressLine2(String addressLine2) {
    this.addressLine2 = addressLine2;
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

  public String getAuthorizedUserKeyName() {
    return authorizedUserKeyName;
  }

  public void setAuthorizedUserKeyName(String authorizedUserKeyName) {
    this.authorizedUserKeyName = authorizedUserKeyName;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Community getCommunity() throws ApiException {
    if (community == null && communityName != null) {
      this.community = Community.find(communityName);
    }
    return community;
  }

  public void setCommunity(Community community) {
    this.community = community;
    this.communityName = community.getName();
  }

  public String getCommunityName() {
    return communityName;
  }

  public void setCommunityName(String communityName) {
    if (communityName == null || !communityName.equals(this.communityName)) {
      this.communityName = communityName;
      this.community = null;
    }
  }

  public PartnerRegion getCountryOrRegion() {
    return countryOrRegion;
  }

  public void setCountryOrRegion(PartnerRegion countryOrRegion) {
    this.countryOrRegion = countryOrRegion;
  }

  public boolean isDoesRequireCompressedData() {
    return doesRequireCompressedData;
  }

  public void setDoesRequireCompressedData(boolean doesRequireCompressedData) {
    this.doesRequireCompressedData = doesRequireCompressedData;
  }

  public boolean isDoesRequireEncryptedData() {
    return doesRequireEncryptedData;
  }

  public void setDoesRequireEncryptedData(boolean doesRequireEncryptedData) {
    this.doesRequireEncryptedData = doesRequireEncryptedData;
  }

  public boolean isDoesRequireSignedData() {
    return doesRequireSignedData;
  }

  public void setDoesRequireSignedData(boolean doesRequireSignedData) {
    this.doesRequireSignedData = doesRequireSignedData;
  }

  public boolean isDoesUseSSH() {
    return doesUseSSH;
  }

  public void setDoesUseSSH(boolean doesUseSSH) {
    this.doesUseSSH = doesUseSSH;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public boolean isUseGlobalMailbox() {
    return useGlobalMailbox;
  }

  public void setUseGlobalMailbox(boolean useGlobalMailbox) {
    this.useGlobalMailbox = useGlobalMailbox;
  }

  public String getGivenName() {
    return givenName;
  }

  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }

  public boolean isKeyEnabled() {
    return keyEnabled;
  }

  public void setKeyEnabled(boolean keyEnabled) {
    this.keyEnabled = keyEnabled;
  }

  public String getPasswordPolicy() {
    return passwordPolicy;
  }

  public void setPasswordPolicy(String passwordPolicy) {
    this.passwordPolicy = passwordPolicy;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public int getSessionTimeout() {
    return sessionTimeout;
  }

  public void setSessionTimeout(int sessionTimeout) {
    this.sessionTimeout = sessionTimeout;
  }

  public String getStateOrProvince() {
    return stateOrProvince;
  }

  public void setStateOrProvince(String stateOrProvince) {
    this.stateOrProvince = stateOrProvince;
  }

  public String getSurname() {
    return surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  public boolean isTextMode() {
    return textMode;
  }

  public void setTextMode(boolean textMode) {
    this.textMode = textMode;
  }

  public PartnerTimeZone getTimeZone() {
    return timeZone;
  }

  public void setTimeZone(PartnerTimeZone timeZone) {
    this.timeZone = timeZone;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public CdConfiguration getConsumerCdConfiguration() {
    return consumerCdConfiguration;
  }

  public void setConsumerCdConfiguration(CdConfiguration consumerCdConfiguration) {
    this.consumerCdConfiguration = consumerCdConfiguration;
  }

  public CdConfiguration getProducerCdConfiguration() {
    return producerCdConfiguration;
  }

  public void setProducerCdConfiguration(CdConfiguration producerCdConfiguration) {
    this.producerCdConfiguration = producerCdConfiguration;
  }

  public FtpConfiguration getConsumerFtpConfiguration() {
    return consumerFtpConfiguration;
  }

  public void setConsumerFtpConfiguration(FtpConfiguration consumerFtpConfiguration) {
    this.consumerFtpConfiguration = consumerFtpConfiguration;
  }

  public FtpConfiguration getProducerFtpConfiguration() {
    return producerFtpConfiguration;
  }

  public void setProducerFtpConfiguration(FtpConfiguration producerFtpConfiguration) {
    this.producerFtpConfiguration = producerFtpConfiguration;
  }

  public FtpsConfiguration getConsumerFtpsConfiguration() {
    return consumerFtpsConfiguration;
  }

  public void setConsumerFtpsConfiguration(FtpsConfiguration consumerFtpsConfiguration) {
    this.consumerFtpsConfiguration = consumerFtpsConfiguration;
  }

  public FtpsConfiguration getProducerFtpsConfiguration() {
    return producerFtpsConfiguration;
  }

  public void setProducerFtpsConfiguration(FtpsConfiguration producerFtpsConfiguration) {
    this.producerFtpsConfiguration = producerFtpsConfiguration;
  }

  public ConsumerWebSphereConfiguration getConsumerWsConfiguration() {
    return consumerWsConfiguration;
  }

  public void setConsumerWsConfiguration(ConsumerWebSphereConfiguration consumerWsConfiguration) {
    this.consumerWsConfiguration = consumerWsConfiguration;
  }

  public SshConfiguration getConsumerSshConfiguration() {
    return consumerSshConfiguration;
  }

  public void setConsumerSshConfiguration(SshConfiguration consumerSshConfiguration) {
    this.consumerSshConfiguration = consumerSshConfiguration;
  }

  public SshConfiguration getProducerSshConfiguration() {
    return producerSshConfiguration;
  }

  public void setProducerSshConfiguration(SshConfiguration producerSshConfiguration) {
    this.producerSshConfiguration = producerSshConfiguration;
  }

  public boolean isInitiatingConsumer() {
    return isInitiatingConsumer;
  }

  public void setInitiatingConsumer(boolean isInitiatingConsumer) {
    this.isInitiatingConsumer = isInitiatingConsumer;
  }

  public boolean isInitiatingProducer() {
    return isInitiatingProducer;
  }

  public void setInitiatingProducer(boolean isInitiatingProducer) {
    this.isInitiatingProducer = isInitiatingProducer;
  }

  public boolean isListeningConsumer() {
    return isListeningConsumer;
  }

  public void setListeningConsumer(boolean isListeningConsumer) {
    this.isListeningConsumer = isListeningConsumer;
  }

  public boolean isListeningProducer() {
    return isListeningProducer;
  }

  public void setListeningProducer(boolean isListeningProducer) {
    this.isListeningProducer = isListeningProducer;
  }

  public String getRemoteFilePattern() {
    return remoteFilePattern;
  }

  public void setRemoteFilePattern(String remoteFilePattern) {
    this.remoteFilePattern = remoteFilePattern;
  }

  public String getMailbox() {
    return mailbox;
  }

  public void setMailbox(String mailbox) {
    this.mailbox = mailbox;
  }

  public int getPollingInterval() {
    return pollingInterval;
  }

  public void setPollingInterval(int pollingInterval) {
    this.pollingInterval = pollingInterval;
  }

  public String getPublicKeyID() {
    return publicKeyID;
  }

  public void setPublicKeyID(String publicKeyID) {
    this.publicKeyID = publicKeyID;
  }

  public String getCustomProtocolName() {
    return customProtocolName;
  }

  public void setCustomProtocolName(String customProtocolName) {
    this.customProtocolName = customProtocolName;
  }

  public String getCustomProtocolExtensions() {
    return customProtocolExtensions;
  }

  public void setCustomProtocolExtensions(String customProtocolExtensions) {
    this.customProtocolExtensions = customProtocolExtensions;
  }

  public boolean isAppendSuffixToUsername() {
    return appendSuffixToUsername;
  }

  public void setAppendSuffixToUsername(boolean appendSuffixToUsername) {
    this.appendSuffixToUsername = appendSuffixToUsername;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getTargetFileEncoding() {
    return targetFileEncoding;
  }

  public void setTargetFileEncoding(String targetFileEncoding) {
    this.targetFileEncoding = targetFileEncoding;
  }

  // static lookup methods:
  public static List<TradingPartner> findAll() throws ApiException {
    return findAll(null);
  }

  public static List<TradingPartner> findAll(String globPattern, String... includeFields) throws ApiException {
    List<TradingPartner> result = new ArrayList<>();
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("offset", 0);
      params.put("includeFields", includeFields);
      params.put("searchFor", (globPattern != null ? globPattern.replace('*', '%') : null));
      while (true) {
        JSONArray jsonObjects = getJSONArray(get(SVC_NAME, params));
        for (int i = 0; i < jsonObjects.length(); i++) {
          result.add(new TradingPartner(jsonObjects.getJSONObject(i)));
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
  public static TradingPartner find(String partner) throws ApiException {
    TradingPartner result = null;
    JSONObject json = findByKey(SVC_NAME, partner);
    try {
      if (json.has(ERROR_CODE)) {
        LOGGER.log(Level.FINER, "TradingPartner {0} not found: errorCode={1}, errorDescription={2}.",
            new Object[] { partner, json.getInt(ERROR_CODE), json.get(ERROR_DESCRIPTION) });
      } else {
        result = new TradingPartner(json);
        LOGGER.log(Level.FINER, "Found TradingPartner {0}: {1}", new Object[] { partner, result });
      }
    } catch (JSONException e) {
      throw new ApiException(e);
    }
    return result;
  }

  public static boolean exists(TradingPartner partner) throws ApiException {
    return exists(partner.getId());
  }

  public static boolean exists(String partner) throws ApiException {
    JSONObject json = findByKey(SVC_NAME, partner);
    return json.has(ID_PROPERTY);
  }

  @Override
  public String toString() {
    return "TradingPartner [partnerName=" + partnerName + ", asciiArmor=" + asciiArmor + ", addressLine1=" + addressLine1 + ", addressLine2=" + addressLine2
        + ", authenticationHost=" + authenticationHost + ", authenticationType=" + authenticationType + ", authorizedUserKeyName=" + authorizedUserKeyName
        + ", city=" + city + ", code=" + code + ", community=" + community + ", countryOrRegion=" + countryOrRegion + ", doesRequireCompressedData="
        + doesRequireCompressedData + ", doesRequireEncryptedData=" + doesRequireEncryptedData + ", doesRequireSignedData=" + doesRequireSignedData
        + ", doesUseSSH=" + doesUseSSH + ", emailAddress=" + emailAddress + ", useGlobalMailbox=" + useGlobalMailbox + ", givenName=" + givenName
        + ", keyEnabled=" + keyEnabled + ", passwordPolicy=" + passwordPolicy + ", postalCode=" + postalCode + ", sessionTimeout=" + sessionTimeout
        + ", stateOrProvince=" + stateOrProvince + ", surname=" + surname + ", textMode=" + textMode + ", timeZone=" + timeZone + ", username=" + username
        + ", phone=" + phone + ", consumerCdConfiguration=" + consumerCdConfiguration + ", producerCdConfiguration=" + producerCdConfiguration
        + ", consumerFtpConfiguration=" + consumerFtpConfiguration + ", producerFtpConfiguration=" + producerFtpConfiguration + ", consumerFtpsConfiguration="
        + consumerFtpsConfiguration + ", producerFtpsConfiguration=" + producerFtpsConfiguration + ", consumerWsConfiguration=" + consumerWsConfiguration
        + ", consumerSshConfiguration=" + consumerSshConfiguration + ", producerSshConfiguration=" + producerSshConfiguration + ", isInitiatingConsumer="
        + isInitiatingConsumer + ", isInitiatingProducer=" + isInitiatingProducer + ", isListeningConsumer=" + isListeningConsumer + ", isListeningProducer="
        + isListeningProducer + ", remoteFilePattern=" + remoteFilePattern + ", mailbox=" + mailbox + ", pollingInterval=" + pollingInterval + ", publicKeyID="
        + publicKeyID + ", customProtocolName=" + customProtocolName + ", customProtocolExtensions=" + customProtocolExtensions + ", appendSuffixToUsername="
        + appendSuffixToUsername + ", password=" + password + "]";
  }
}
