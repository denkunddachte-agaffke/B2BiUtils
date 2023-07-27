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
package de.denkunddachte.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

import de.denkunddachte.util.Password.CryptException;
import de.denkunddachte.enums.CipherSuite;
import de.denkunddachte.enums.DocumentStorage;
import de.denkunddachte.enums.NodePreference;
import de.denkunddachte.enums.PersistenceLevel;
import de.denkunddachte.enums.Queue;
import de.denkunddachte.enums.RecoveryLevel;
import de.denkunddachte.enums.RemovalMethod;
import de.denkunddachte.enums.ReportingLevel;
import de.denkunddachte.enums.TlsVersion;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.ft.CDNode;
import de.denkunddachte.ft.CDNode.LogLevel;

public class ApiConfig {

  public static final String          WSAPILIST                         = "sfgapi.wsapilist";
  public static final String          CACHEEXPIRY                       = "sfgapi.cacheexpiry";
  public static final String          CACHEDIR                          = "sfgapi.cachedir";
  public static final String          USECACHE                          = "sfgapi.usecache";
  public static final String          TRUST_ALL_CERTS                   = "sfgapi.trustAllCerts";
  public static final String          TRUSTSTORE                        = "sfgapi.trustStore";
  public static final String          TRUSTSTORE_PASS                   = "sfgapi.trustStorePass";
  public static final String          PASSWORD                          = "sfgapi.password";
  public static final String          API_USER                          = "sfgapi.user";
  public static final String          CREDENTIALS                       = "sfgapi.credentials";
  public static final String          WSURI                             = "sfgapi.wsuri";
  public static final String          WS_BP_NAME                        = "sfgapi.wsbp";
  public static final String          BASEURI                           = "sfgapi.baseuri";
  public static final String          CONFIG                            = "sfgapi.config";
  public static final String          API_RANGE_SIZE                    = "sfgapi.rangeSize";
  public static final String          EXECBP_CMD                        = "sfgapi.executebp.cmd";
  public static final String          EXECBP_COPYCMD                    = "sfgapi.executebp.copycmd";
  public static final String          EXECBP_WORKDIR                    = "sfgapi.executebp.workdir";
  public static final String          API_REQ_RETRIES                   = "sfgapi.retries";
  public static final String          USER                              = "user";

  // B2Bi and custom table JPA config (EclipseLink)
  public static final String          DB_DRIVER                         = "sft.db.driver";
  public static final String          DB_URL                            = "sft.db.url";
  public static final String          DB_USER                           = "sft.db.user";
  public static final String          DB_PASSWORD                       = "sft.db.password";
  public static final String          SFT_USERID                        = "sft.userid";
  public static final String          SFT_PROGRAMID                     = "sft.programid";
  public static final String          SFT_EMAIL                         = "sft.email";
  public static final String          SFT_PHONE                         = "sft.phone";
  // see https://wiki.eclipse.org/EclipseLink/Examples/JPA/Logging
  public static final String          ECLIPSELINK_LOGGER                = "eclipselink.logging.logger";
  public static final String          ECLIPSELINK_LOGLEVEL              = "eclipselink.logging.level";
  public static final String          ECLIPSELINK_LOGLEVEL_SQL          = "eclipselink.logging.level.sql";
  public static final String          ECLIPSELINK_LOG_PARAMS            = "eclipselink.logging.parameters";
  public static final String          ECLIPSELINK_EVENTLISTENER         = "eclipselink.session-event-listener";

  // LDAP
  public static final String          LDAP_URL                          = "ldap.url";
  public static final String          LDAP_BASE                         = "ldap.base";
  public static final String          LDAP_ADMBASE                      = "ldap.admbase";
  public static final String          LDAP_ADMIN_USER                   = "ldap.admin.user";
  public static final String          LDAP_ADMIN_PASSWORD               = "ldap.admin.password";
  public static final String          LDAP_PASSWORD_DIGEST              = "ldap.passwordDigest";

  // SSPCM
  public static final String          SSP_BASE_URI                      = "ssp.api.baseuri";
  public static final String          SSP_USER                          = "ssp.api.user";
  public static final String          SSP_PASSWORD                      = "ssp.api.password";
  public static final String          SSP_CD_KEYSTORE_NAME              = "ssp.cd.keyStoreName";
  public static final String          SSP_CD_NODE_LOGLEVEL              = "ssp.cd.defaultLogLevel";
  public static final String          SSP_CD_TCP_TIMEOUT                = "ssp.cd.defaultTcpTimeout";
  public static final String          SSP_CD_DEFAULT_POLICY             = "ssp.cd.defaultPolicy";
  public static final String          SSP_CD_TRUSTSTORE_NAME            = "ssp.cd.trustStoreName";
  public static final String          CD_SECPLUS_PROTOCOL               = "cd.secplus.protocol";
  public static final String          CD_SECPLUS_CACERTS                = "cd.secplus.cacertlist";
  public static final String          CD_SECPLUS_SYSTEM_CERT            = "cd.secplus.systemcert";
  public static final String          CD_SECPLUS_CIPHERSUITES           = "cd.secplus.ciphersuitelist";
  public static final String          CD_SECPLUS_CLIENTAUTH             = "cd.secplus.reqclientauth";

  // WFD defaults
  public static final String          WFD_DOCUMENTTRACKING              = "wfd.documentTracking";
  public static final String          WFD_ONFAULTPROCESSING             = "wfd.onfaultProcessing";
  public static final String          WFD_QUEUE                         = "wfd.queue";
  public static final String          WFD_USEBPQUEUING                  = "wfd.useBPQueuing";
  public static final String          WFD_ENABLETRANSACTION             = "wfd.enableTransaction";
  public static final String          WFD_COMMITSTEPSUPONERROR          = "wfd.commitStepsUponError";
  public static final String          WFD_PERSISTENCELEVEL              = "wfd.persistenceLevel";
  public static final String          WFD_EVENTREPORTINGLEVEL           = "wfd.eventReportingLevel";
  public static final String          WFD_RECOVERYLEVEL                 = "wfd.recoveryLevel";
  public static final String          WFD_SOFTSTOPRECOVERYLEVEL         = "wfd.softstopRecoveryLevel";
  public static final String          WFD_DOCUMENTSTORAGE               = "wfd.documentStorage";
  public static final String          WFD_NODEPREFERENCE                = "wfd.nodePreference";
  public static final String          WFD_SETCUSTOMDEADLINE             = "wfd.setCustomDeadline";
  public static final String          WFD_SETCUSTOMLIFESPAN             = "wfd.setCustomLifespan";
  public static final String          WFD_REMOVALMETHOD                 = "wfd.removalMethod";
  public static final String          WFD_ENABLEBUSINESSPROCESS         = "wfd.enableBusinessProcess";
  public static final String          WFD_SETTHISVERSIONASDEFAULT       = "wfd.setThisVersionAsDefault";
  public static final String          WFD_USE_API_TO_SET_DEFAULT        = "wfd.useApiToSetDefault";
  public static final String          WFD_TOGGLE_USING_WS_API           = "wfd.toggleUsingWsApi";
  public static final String          WFD_REFRESH_WFD_CACHE             = "wfd.refreshWfdCache";

  public static final String          DEFAULT_CONFIG                    = "apiconfig.properties";
  protected static final String       INTERNAL_CONFIG                   = "de/denkunddachte/sfgapi/apiconfig.properties";

  private String                      apiBaseURI;
  private String                      wsApiBaseURI;
  private String                      apiUser;
  private String                      apiCredentials;
  private HttpClientConnectionManager httpClientConnectionManager;
  private int                         apiRangeSize                      = 1000;
  private String                      wsApiBpName                       = "DD_API_WS";
  private final Set<String>           wsApiList                         = new HashSet<>();
  private boolean                     cacheResults                      = false;
  private File                        cacheDir                          = new File("apicache");
  private long                        cacheExpiryMillis                 = 300 * 1000L;
  private String                      sfgExecBpCmd;
  private String                      sfgExecBpCopycmd;
  private String                      sfgExecBpDir;
  private int                         apiRequestRetries                 = 3;

  private String                      dbDriver;
  private String                      dbUrl;
  private String                      dbUser;
  private String                      dbPassword;
  private String                      eclipseLinkLogger                 = "JavaLogger";
  private Level                       eclipseLinkLogLevel               = Level.OFF;
  private Level                       eclipseLinkLogLevelSql            = Level.OFF;
  private String                      eclipseLinkLogParams;
  private String                      eclipseLinkSessionEventListener;
  private String                      sftUserId                         = System.getProperty("user.name");
  private String                      sftProgramId                      = "api";
  private String                      sftEmail                          = "api@example.com";
  private String                      sftPhone                          = "+123";
  private String                      ldapUrl;
  private String                      ldapBase;
  private String                      ldapAdmBase;
  private String                      ldapAdminUser;
  private String                      ldapAdminPassword;
  private String                      ldapPasswordDigestAlg;
  private String                      sspBaseURI;
  private String                      sspUser;
  private String                      sspPassword;
  private String                      sspCdKeystoreName                 = "dfltKeyStore";
  private CDNode.LogLevel             sspCdNodeLogLevel                 = LogLevel.NONE;
  private int                         sspCdTcpTimeout                   = 90;
  private String                      sspCdDefaultPolicy;
  private String                      sspCdTruststoreName               = "dfltTrustStore";
  private TlsVersion                  cdSecplusProtocol                 = TlsVersion.TLS_V12;
  private Set<String>                 cdSecplusCACerts;
  private String                      cdSecplusSystemCert               = "sspDefaultKeyCert";
  private Set<CipherSuite>            cdSecplusCipherSuites;
  private boolean                     cdSecplusClientauth               = true;

  private String                      configFiles;
  private boolean                     trustAllCerts;

  // WFD defaults:
  private boolean                     wfdDefaultDocumentTracking        = false;
  private boolean                     wfdDefaultOnfaultProcessing       = false;
  private Queue                       wfdDefaultQueue                   = Queue.Q4;
  private boolean                     wfdDefaultUseBPQueuing            = true;
  private boolean                     wfdDefaultEnableTransaction       = false;
  private boolean                     wfdDefaultCommitStepsUponError    = true;
  private PersistenceLevel            wfdDefaultPersistenceLevel        = PersistenceLevel.SYSTEM_DEFAULT;
  private ReportingLevel              wfdDefaultEventReportingLevel     = ReportingLevel.NONE;
  private RecoveryLevel               wfdDefaultRecoveryLevel           = RecoveryLevel.MANUAL;
  private RecoveryLevel               wfdDefaultSoftstopRecoveryLevel   = RecoveryLevel.MANUAL;
  private DocumentStorage             wfdDefaultDocumentStorage         = DocumentStorage.SYSTEM_DEFAULT;
  private NodePreference              wfdDefaultNodePreference          = NodePreference.NO_PREFERENCE;
  private boolean                     wfdDefaultSetCustomDeadline       = false;
  private boolean                     wfdDefaultSetCustomLifespan       = false;
  private RemovalMethod               wfdDefaultRemovalMethod           = RemovalMethod.PURGE;
  private boolean                     wfdDefaultEnableBusinessProcess   = true;
  private boolean                     wfdDefaultSetThisVersionAsDefault = true;
  private boolean                     wfdToggleUsingWsApi               = false;
  private boolean                     wfdUseApiToSetDefault             = false;
  private boolean                     wfdRefreshWfdCache                = false;
  private String                      user                              = System.getProperty("user.name");

  private static ApiConfig            instance;

  private ApiConfig(String cfg) throws ApiException {
    this.init(cfg);
  }

  private void init(String cfg) throws ApiException {
    if (cfg == null && (new File(DEFAULT_CONFIG)).canRead()) {
      cfg = DEFAULT_CONFIG;
    }
    try (InputStream is = (cfg == null ? this.getClass().getClassLoader().getResourceAsStream(INTERNAL_CONFIG) : new FileInputStream(cfg))) {
      Properties p = new Properties();
      p.load(is);
      loadMap(p);
    } catch (IOException e) {
      throw new ApiException(e);
    }
    configFiles = (cfg == null ? "INTERNAL" : (new File(cfg)).getAbsolutePath());
    if (System.getProperty(CONFIG) != null) {
      File cfgFile = new File(System.getProperty(CONFIG));
      try (InputStream is = new FileInputStream(cfgFile)) {
        Properties p = new Properties();
        p.load(is);
        loadMap(p);
        configFiles += "," + cfgFile.getAbsolutePath();
      } catch (IOException e) {
        throw new ApiException(e);
      }
    }
    loadMap(System.getProperties());

    if (cacheResults) {
      cacheResults = cacheDir.isDirectory() || cacheDir.mkdirs();
    }
  }

  public static ApiConfig getInstance() throws ApiException {
    return getInstance((String) null);
  }

  public static ApiConfig getInstance(String cfg) throws ApiException {
    if (instance == null) {
      instance = new ApiConfig(cfg);
    }
    return instance;
  }

  public static ApiConfig getInstance(Map<String, String> cfg) throws ApiException {
    if (instance == null) {
      instance = new ApiConfig(null);

      if (cfg != null) {
        instance.loadMap(cfg.entrySet().stream().collect(Collectors.toMap(e -> (String) e.getKey(), e -> (String) e.getValue())));
        instance.configFiles += ",MAP";
      }
    }
    return instance;
  }

  private void loadMap(Map<Object, Object> props) throws ApiException {
    try {
      for (Object key : props.keySet()) {
        switch (key.toString()) {
        case BASEURI:
          apiBaseURI = (String) props.get(key);
          break;
        case WSURI:
          wsApiBaseURI = (String) props.get(key);
          break;
        case CREDENTIALS:
          apiCredentials = (String) props.get(key);
          break;
        case API_USER:
          apiUser = (String) props.get(key);
          setBasicApiCredentials(apiUser, Password.getCleartext((String) props.get(PASSWORD)));
          break;
        case TRUST_ALL_CERTS:
          trustAllCerts = Boolean.parseBoolean((String) props.get(key));
          break;
        case TRUSTSTORE:
          System.setProperty("javax.net.ssl.trustStore", (String) props.get(key));
          break;
        case TRUSTSTORE_PASS:
          System.setProperty("javax.net.ssl.trustStorePassword", Password.getCleartext((String) props.get(key)));
          break;
        case USECACHE:
          cacheResults = Boolean.parseBoolean((String) props.get(key));
          break;
        case CACHEDIR:
          cacheDir = new File((String) props.get(key));
          break;
        case CACHEEXPIRY:
          cacheExpiryMillis = Long.parseLong((String) props.get(key));
          break;
        case WSAPILIST:
          useWsApiFor(Arrays.asList(((String) props.get(key)).split("\\s*,\\s*")));
          break;
        case DB_DRIVER:
          dbDriver = (String) props.get(key);
          break;
        case DB_URL:
          dbUrl = (String) props.get(key);
          break;
        case DB_USER:
          dbUser = (String) props.get(key);
          break;
        case DB_PASSWORD:
          dbPassword = Password.getCleartext((String) props.get(key));
          break;
        case ECLIPSELINK_LOG_PARAMS:
          eclipseLinkLogParams = (String) props.get(key);
          break;
        case ECLIPSELINK_LOGGER:
          eclipseLinkLogger = (String) props.get(key);
          break;
        case ECLIPSELINK_LOGLEVEL:
          eclipseLinkLogLevel = Level.parse((String) props.get(key));
          break;
        case ECLIPSELINK_LOGLEVEL_SQL:
          eclipseLinkLogLevelSql = Level.parse((String) props.get(key));
          break;
        case ECLIPSELINK_EVENTLISTENER:
          eclipseLinkSessionEventListener = (String) props.get(key);
          break;
        case SFT_EMAIL:
          sftEmail = (String) props.get(key);
          break;
        case SFT_USERID:
          sftUserId = (String) props.get(key);
          break;
        case SFT_PROGRAMID:
          sftProgramId = (String) props.get(key);
          break;
        case SFT_PHONE:
          sftPhone = (String) props.get(key);
          break;
        case LDAP_URL:
          ldapUrl = (String) props.get(key);
          break;
        case LDAP_BASE:
          ldapBase = (String) props.get(key);
          break;
        case LDAP_ADMBASE:
          ldapAdmBase = (String) props.get(key);
          break;
        case LDAP_ADMIN_USER:
          ldapAdminUser = (String) props.get(key);
          break;
        case LDAP_ADMIN_PASSWORD:
          ldapAdminPassword = Password.getCleartext((String) props.get(key));
          break;
        case LDAP_PASSWORD_DIGEST:
          ldapPasswordDigestAlg = (String) props.get(key);
          break;
        case SSP_BASE_URI:
          sspBaseURI = (String) props.get(key);
          break;
        case SSP_USER:
          sspUser = (String) props.get(key);
          break;
        case SSP_PASSWORD:
          sspPassword = Password.getCleartext((String) props.get(key));
          break;
        case WFD_DOCUMENTTRACKING:
          wfdDefaultDocumentTracking = Boolean.parseBoolean((String) props.get(key));
          break;
        case WFD_ONFAULTPROCESSING:
          wfdDefaultOnfaultProcessing = Boolean.parseBoolean((String) props.get(key));
          break;
        case WFD_QUEUE:
          wfdDefaultQueue = Queue.valueOf((String) props.get(key));
          break;
        case WFD_USEBPQUEUING:
          wfdDefaultUseBPQueuing = Boolean.parseBoolean((String) props.get(key));
          break;
        case WFD_ENABLETRANSACTION:
          wfdDefaultEnableTransaction = Boolean.parseBoolean((String) props.get(key));
          break;
        case WFD_COMMITSTEPSUPONERROR:
          wfdDefaultCommitStepsUponError = Boolean.parseBoolean((String) props.get(key));
          break;
        case WFD_PERSISTENCELEVEL:
          wfdDefaultPersistenceLevel = PersistenceLevel.valueOf((String) props.get(key));
          break;
        case WFD_EVENTREPORTINGLEVEL:
          wfdDefaultEventReportingLevel = ReportingLevel.valueOf((String) props.get(key));
          break;
        case WFD_RECOVERYLEVEL:
          wfdDefaultRecoveryLevel = RecoveryLevel.valueOf((String) props.get(key));
          break;
        case WFD_SOFTSTOPRECOVERYLEVEL:
          wfdDefaultSoftstopRecoveryLevel = RecoveryLevel.valueOf((String) props.get(key));
          break;
        case WFD_DOCUMENTSTORAGE:
          wfdDefaultDocumentStorage = DocumentStorage.valueOf((String) props.get(key));
          break;
        case WFD_NODEPREFERENCE:
          wfdDefaultNodePreference = NodePreference.valueOf((String) props.get(key));
          break;
        case WFD_SETCUSTOMDEADLINE:
          wfdDefaultSetCustomDeadline = Boolean.parseBoolean((String) props.get(key));
          break;
        case WFD_SETCUSTOMLIFESPAN:
          wfdDefaultSetCustomLifespan = Boolean.parseBoolean((String) props.get(key));
          break;
        case WFD_REMOVALMETHOD:
          wfdDefaultRemovalMethod = RemovalMethod.valueOf((String) props.get(key));
          break;
        case WFD_ENABLEBUSINESSPROCESS:
          wfdDefaultEnableBusinessProcess = Boolean.parseBoolean((String) props.get(key));
          break;
        case WFD_SETTHISVERSIONASDEFAULT:
          wfdDefaultSetThisVersionAsDefault = Boolean.parseBoolean((String) props.get(key));
          break;
        case WFD_TOGGLE_USING_WS_API:
          wfdToggleUsingWsApi = Boolean.parseBoolean((String) props.get(key));
          break;
        case WFD_USE_API_TO_SET_DEFAULT:
          wfdUseApiToSetDefault = Boolean.parseBoolean((String) props.get(key));
          break;
        case WFD_REFRESH_WFD_CACHE:
          wfdRefreshWfdCache = Boolean.parseBoolean((String) props.get(key));
          break;
        case SSP_CD_KEYSTORE_NAME:
          sspCdKeystoreName = (String) props.get(key);
          break;
        case SSP_CD_TRUSTSTORE_NAME:
          sspCdTruststoreName = (String) props.get(key);
          break;
        case SSP_CD_NODE_LOGLEVEL:
          sspCdNodeLogLevel = CDNode.LogLevel.valueOf((String) props.get(key));
          break;
        case SSP_CD_TCP_TIMEOUT:
          sspCdTcpTimeout = Integer.parseInt((String) props.get(key));
          break;
        case SSP_CD_DEFAULT_POLICY:
          sspCdDefaultPolicy = (String) props.get(key);
          break;
        case CD_SECPLUS_PROTOCOL:
          cdSecplusProtocol = TlsVersion.getByVersionString((String) props.get(key));
          break;
        case CD_SECPLUS_CACERTS:
          setCdSecplusCACerts((String) props.get(key));
          break;
        case CD_SECPLUS_SYSTEM_CERT:
          cdSecplusSystemCert = (String) props.get(key);
          break;
        case CD_SECPLUS_CIPHERSUITES:
          setCdSecplusCipherSuites((String) props.get(key));
          break;
        case CD_SECPLUS_CLIENTAUTH:
          cdSecplusClientauth = Boolean.parseBoolean((String) props.get(key));
          break;
        case API_RANGE_SIZE:
          apiRangeSize = Integer.parseInt((String) props.get(key));
          break;
        case EXECBP_CMD:
          sfgExecBpCmd = (String) props.get(key);
          break;
        case EXECBP_COPYCMD:
          sfgExecBpCopycmd = (String) props.get(key);
          break;
        case EXECBP_WORKDIR:
          sfgExecBpDir = (String) props.get(key);
          break;
        case API_REQ_RETRIES:
          apiRequestRetries = Integer.parseInt((String) props.get(key));
          break;
        case USER:
          user = (String) props.get(key);
          break;
        case WS_BP_NAME:
          wsApiBpName = (String) props.get(key);
          break;
        default:
          break;
        }
      }
    } catch (CryptException e) {
      throw new ApiException("Could not decrypt password!", e);
    }
  }

  public boolean isTrustAllCerts() {
    return trustAllCerts;
  }

  public String getApiBaseURI() {
    return apiBaseURI;
  }

  public String getWsApiBaseURI() {
    return wsApiBaseURI;
  }

  public String getApiUser() {
    return apiUser;
  }

  public String getApiCredentials() {
    return apiCredentials;
  }

  private void setBasicApiCredentials(String apiUser, String password) {
    this.apiCredentials = "Basic " + Base64.getEncoder().encodeToString((apiUser + ":" + password).getBytes());
  }

  public HttpClientConnectionManager getHttpClientConnectionManager() throws ApiException {
    if (httpClientConnectionManager == null) {
      if (trustAllCerts) {
        try {
          TrustStrategy                     acceptingTrustStrategy = new TrustStrategy() {
                                                                     @Override
                                                                     public boolean isTrusted(X509Certificate[] chain, String authType)
                                                                         throws CertificateException {
                                                                       return true;
                                                                     }
                                                                   };
          SSLContext                        sslContext             = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
          SSLConnectionSocketFactory        sslsf                  = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
          Registry<ConnectionSocketFactory> socketFactoryRegistry  = RegistryBuilder.<ConnectionSocketFactory> create().register("https", sslsf)
              .register("http", new PlainConnectionSocketFactory()).build();
          httpClientConnectionManager = new BasicHttpClientConnectionManager(socketFactoryRegistry);
          this.trustAllCerts = true;
        } catch (KeyStoreException | KeyManagementException | NoSuchAlgorithmException e) {
          throw new ApiException("Error creating connection factory: " + e.getMessage(), e);
        }
      } else {
        httpClientConnectionManager = new BasicHttpClientConnectionManager();
        this.trustAllCerts = false;
      }
    }
    return httpClientConnectionManager;
  }

  public int getApiRangeSize() {
    return apiRangeSize;
  }

  public boolean isCacheResults() {
    return cacheResults;
  }

  public File getCacheDir() {
    return cacheDir;
  }

  public long getCacheExpiryMillis() {
    return cacheExpiryMillis;
  }

  public String getWsApiBpName() {
    return wsApiBpName;
  }

  public Set<String> getWsApiList() {
    Set<String> result = new HashSet<String>(wsApiList.size());
    result.addAll(wsApiList);
    return result;
  }

  public void useWsApiFor(Collection<String> apiList) {
    wsApiList.clear();
    wsApiList.addAll(apiList.stream().map(s -> s.toLowerCase()).collect(Collectors.toList()));
  }

  public boolean useWsApi(String svcName) {
    return wsApiList.contains(svcName.toLowerCase());
  }

  public static List<String> getApiProprties() {
    return Arrays.stream(ApiConfig.class.getFields()).map(f -> {
      try {
        return f.get(null).toString();
      } catch (IllegalArgumentException | IllegalAccessException e) {
        e.printStackTrace();
        return null;
      }
    }).collect(Collectors.toList());
  }

  public String getDbDriver() {
    return dbDriver;
  }

  public String getDbUrl() {
    return dbUrl;
  }

  public String getDbUser() {
    return dbUser;
  }

  public String getDbPassword() {
    return dbPassword;
  }

  public String getEclipseLinkLogger() {
    return eclipseLinkLogger;
  }

  public Level getEclipseLinkLogLevel() {
    return eclipseLinkLogLevel;
  }

  public Level getEclipseLinkLogLevelSql() {
    return eclipseLinkLogLevelSql;
  }

  public String getEclipseLinkLogParams() {
    return eclipseLinkLogParams;
  }

  public String getEclipseLinkSessionEventListener() {
    return eclipseLinkSessionEventListener;
  }

  public String getSftUserId() {
    return sftUserId;
  }

  public String getSftProgramId() {
    return sftProgramId;
  }

  public String getSftEmail() {
    return sftEmail;
  }

  public String getSftPhone() {
    return sftPhone;
  }

  public String getLdapUrl() {
    return ldapUrl;
  }

  public String getLdapBase() {
    return ldapBase;
  }

  public String getLdapAdmBase() {
    return ldapAdmBase;
  }

  public String getLdapAdminUser() {
    return ldapAdminUser;
  }

  public String getLdapAdminPassword() {
    return ldapAdminPassword;
  }

  public String getLdapPasswordDigestAlg() {
    return ldapPasswordDigestAlg;
  }

  public String getSspBaseURI() {
    return sspBaseURI;
  }

  public String getSspUser() {
    return sspUser;
  }

  public String getSspPassword() {
    return sspPassword;
  }

  public String getSspCdKeystoreName() {
    return sspCdKeystoreName;
  }

  public CDNode.LogLevel getSspCdNodeLogLevel() {
    return sspCdNodeLogLevel;
  }

  public int getSspCdTcpTimeout() {
    return sspCdTcpTimeout;
  }

  public String getSspCdDefaultPolicy() {
    return sspCdDefaultPolicy;
  }

  public String getSspCdTruststoreName() {
    return sspCdTruststoreName;
  }

  public TlsVersion getCdSecplusProtocol() {
    return cdSecplusProtocol;
  }

  public Set<String> getCdSecplusCACerts() {
    if (cdSecplusCACerts == null) {
      cdSecplusCACerts = new HashSet<>();
      cdSecplusCACerts.add("sspDefaultTrustedCert");
    }
    return cdSecplusCACerts;
  }

  private void setCdSecplusCACerts(String caCertList) {
    cdSecplusCACerts = new HashSet<>();
    cdSecplusCACerts.addAll(Arrays.asList(caCertList.split("\\s*,\\s*")));
  }

  public String getCdSecplusSystemCert() {
    return cdSecplusSystemCert;
  }

  public Set<CipherSuite> getCdSecplusCipherSuites() {
    if (cdSecplusCipherSuites == null) {
      cdSecplusCipherSuites = new HashSet<>();
      cdSecplusCipherSuites.add(CipherSuite.ECDHE_ECDSA_WITH_AES_256_GCM_SHA384);
      cdSecplusCipherSuites.add(CipherSuite.ECDHE_ECDSA_WITH_AES_256_CBC_SHA384);
      cdSecplusCipherSuites.add(CipherSuite.ECDHE_ECDSA_WITH_AES_128_GCM_SHA256);
      cdSecplusCipherSuites.add(CipherSuite.ECDHE_ECDSA_WITH_AES_128_CBC_SHA256);
      cdSecplusCipherSuites.add(CipherSuite.ECDHE_RSA_WITH_AES_256_GCM_SHA384);
      cdSecplusCipherSuites.add(CipherSuite.ECDHE_RSA_WITH_AES_256_CBC_SHA384);
      cdSecplusCipherSuites.add(CipherSuite.ECDHE_RSA_WITH_AES_128_GCM_SHA256);
      cdSecplusCipherSuites.add(CipherSuite.ECDHE_RSA_WITH_AES_128_CBC_SHA256);
      cdSecplusCipherSuites.add(CipherSuite.RSA_WITH_AES_256_GCM_SHA384);
      cdSecplusCipherSuites.add(CipherSuite.RSA_WITH_AES_128_GCM_SHA256);
      cdSecplusCipherSuites.add(CipherSuite.RSA_WITH_AES_256_CBC_SHA256);
      cdSecplusCipherSuites.add(CipherSuite.RSA_WITH_AES_128_CBC_SHA256);
      cdSecplusCipherSuites.add(CipherSuite.ECDHE_ECDSA_WITH_AES_256_CBC_SHA);
      cdSecplusCipherSuites.add(CipherSuite.ECDHE_ECDSA_WITH_AES_128_CBC_SHA);
      cdSecplusCipherSuites.add(CipherSuite.ECDHE_RSA_WITH_AES_256_CBC_SHA);
      cdSecplusCipherSuites.add(CipherSuite.ECDHE_RSA_WITH_AES_128_CBC_SHA);
      cdSecplusCipherSuites.add(CipherSuite.RSA_WITH_AES_256_CBC_SHA);
      cdSecplusCipherSuites.add(CipherSuite.RSA_WITH_AES_128_CBC_SHA);
    }
    return cdSecplusCipherSuites;
  }

  private void setCdSecplusCipherSuites(String cipherSuiteList) {
    cdSecplusCipherSuites = new HashSet<>();
    for (String cs : cipherSuiteList.split("\\s*,\\s*")) {
      cdSecplusCipherSuites.add(CipherSuite.byCode(cs));
    }
  }

  public boolean isCdSecplusClientauth() {
    return cdSecplusClientauth;
  }

  public String getConfigFiles() {
    return this.configFiles;
  }

  public boolean isWfdDefaultDocumentTracking() {
    return wfdDefaultDocumentTracking;
  }

  public boolean isWfdDefaultOnfaultProcessing() {
    return wfdDefaultOnfaultProcessing;
  }

  public Queue getWfdDefaultQueue() {
    return wfdDefaultQueue;
  }

  public boolean isWfdDefaultUseBPQueuing() {
    return wfdDefaultUseBPQueuing;
  }

  public boolean isWfdDefaultEnableTransaction() {
    return wfdDefaultEnableTransaction;
  }

  public boolean isWfdDefaultCommitStepsUponError() {
    return wfdDefaultCommitStepsUponError;
  }

  public PersistenceLevel getWfdDefaultPersistenceLevel() {
    return wfdDefaultPersistenceLevel;
  }

  public ReportingLevel getWfdDefaultEventReportingLevel() {
    return wfdDefaultEventReportingLevel;
  }

  public RecoveryLevel getWfdDefaultRecoveryLevel() {
    return wfdDefaultRecoveryLevel;
  }

  public RecoveryLevel getWfdDefaultSoftstopRecoveryLevel() {
    return wfdDefaultSoftstopRecoveryLevel;
  }

  public DocumentStorage getWfdDefaultDocumentStorage() {
    return wfdDefaultDocumentStorage;
  }

  public NodePreference getWfdDefaultNodePreference() {
    return wfdDefaultNodePreference;
  }

  public boolean isWfdDefaultSetCustomDeadline() {
    return wfdDefaultSetCustomDeadline;
  }

  public boolean isWfdDefaultSetCustomLifespan() {
    return wfdDefaultSetCustomLifespan;
  }

  public RemovalMethod getWfdDefaultRemovalMethod() {
    return wfdDefaultRemovalMethod;
  }

  public boolean isWfdDefaultEnableBusinessProcess() {
    return wfdDefaultEnableBusinessProcess;
  }

  public boolean isWfdDefaultSetThisVersionAsDefault() {
    return wfdDefaultSetThisVersionAsDefault;
  }

  public boolean isWfdToggleUsingWsApi() {
    return wfdToggleUsingWsApi;
  }

  public boolean isWfdUseApiToSetDefault() {
    return wfdUseApiToSetDefault;
  }

  public boolean isWfdRefreshWfdCache() {
    return wfdRefreshWfdCache;
  }

  public String getSfgExecBpCmd() {
    return sfgExecBpCmd;
  }

  public String getSfgExecCopycmd() {
    return sfgExecBpCopycmd;
  }

  public String getSfgExecBpDir() {
    return sfgExecBpDir;
  }

  public int getApiRequestRetries() {
    return apiRequestRetries;
  }

  public String getUser() {
    return user;
  }
}
