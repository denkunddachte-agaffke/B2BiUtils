# B2BiUtils

A collection of libraries and tools to help with business process development&debugging and management of IBM Sterling B2B Integraton / IBM Sterling Filegateway artifacts.

The project contains

* [B2BApiClients](B2BApiClients/README.md): client libraries for B2Bi REST APIs, SEAS LDAP servers, SSP REST APIs
* [B2BApiClients](B2BUtils/README.md): Command line tools (WorkflowUtil, LdapAdmin, MailboxMgr etc.)
* [ddutils](ddutils/README.md): Utlilities
* [JavaTaskHelper](JavaTaskHelper/README.md): Wrapper class to help developing and debugging JavaTask code.

## Releases

Binary releases are available on [Github](https://github.com/denkunddachte-agaffke/B2BiUtils/releases).
## Building

The project is built with gradle. To create a distribution package, run

```bash
[B2BiUtils] $ ./gradlew clean distTar
```

or 

```bash
[B2BiUtils] $ ./gradlew clean distZip
```

Packages are built in `[B2BiUtils]/B2BUtils/build/distributions`.

## Installation

Extract binary package to destination dir. Make sure, a compatible JDK is installed. `java` binary is searched in

* JRE_HOME/bin
* JAVA_HOME/bin
* PATH

Min. version in JDK-8. If you are handling SSH keys using elliptic curve algorithms (e.g. `Ed25519`), a JDK >= 17 is required.

### `DD_API_WS` web service

The package comes with a helper business process `DD_API_WS` (see [DD_API_WS.bpml](B2BApiClient/src/main/resources/DD_API_WS.bpml)) that implements some functionality not provided by B2Bi (e.g. XSLT management, execute BPs in containers etc.) and more performant alternatives to some REST GET APIs. To use this:

1. add WFDs `DD_API_WS` and `DD_API_WS_RESPONSE` from `B2BApiClient/src/main
/resources/`
2. implement a HTTP Server Adapter with authentication and expose a URI (e.g. `/ws_api`) to run the `DD_API_WS`
3. Create a LightWeight JDBC Adapter `DD_JDBC_ADAPTER_01` (for GET APIs)
4. Create a Command Line Adapter 2 `DD_CLA_LOCAL` to run local on ASI server/pod (for execute BP)
5. Create an empty JavaTask service `DD_JavaTaskService` with inline source (hint: put an "x" in source code to overcome dashboard validation)



## Configuration

Create configuration file `apiconfig.properties` (use `[installdir]/apiconfig-sample.properties` as template). The applications will look for a configuration file in the following locations:

* file given by `--configfile` or `-C` option
* `${user.home}/.apiconfig.properties`
* `${user.home}/apiconfig.properties`
* `${installdir}/apiconfig.properties`

Depending on what you want to use, configure sections

* `sfgapi.*`: REST APIs and/or WS API. User requires permission `APIUser` to access REST API.
* `sfgapi.wsapilist`: leave empty, if `DD_API_WS` is not installed. Remove `executebp` from list, if you want to execute `workflowLauncher.sh` via ssh and/or sudo.
* `sfgapi.executebp.*`: if you can execute `workflowLauncher.sh` via ssh and/or sudo
* `sfg.db.*`: required for JPA (manage custom tables)
* `ldap.*`: if you want to manage LDAP users/ssh keys (with SEAS)
* `ssp.*` and `cd.*`: If you want to access SSP REST APIs (currently only C:D netmap)
 

# Bugs/issues
Please report issues, ideas, change requests etc. in [issues](https://github.com/denkunddachte-agaffke/B2BiUtils/issues).
x
