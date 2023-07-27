# B2BApiClient

The `B2BApiClient` jar contains
 
* frontend classes to access (selected) SFG/B2Bi REST APIs providing static lookup methods to retrieve objects from the server, constructory to create new object on the server and update/delete methods to modify and delete objects.
* JPA classes to retrieve, create, modify and delete data in custom tables
* client classes to access Sterling Secure Proxy configuration (currently limited to manipulating Connect:Direct netmaps)
* client to retrieve, create, modify and delete LDAP users on the FT LDAP server, especially methods to manipulate (add/remove) user's SSH authentication keys used in conjunction with Sterling Secure Proxy and Sterling External Authentication Server (SEAS)
* classes to read and create B2Bi `SIExport` resource files (WFD and XSLT types only)
* a `DD_API_WS` API helper business process to be deployed on a HTTP Server Adapter. This BP provide some helper functions to retrieve bulk data directly from the database where the REST APIs are not very performant, provide methods to execute workflows, run resource import and export etc.

# B2B Integrator / Sterling File Gateway APIs

## B2Bi REST APIs

| API | Path | Impl | Want | Classname | Comment |
|-|-|-|-|-|-|
| AS2Organization Services | as2organizations | N | N |  |  |
| AS2TradingPartner Services | as2tradingpartners | N | N |  |
| AS2TradingRelationship Services | as2tradingrelationships | N | N |  |
| CA Digital Certificate Services | cadigitalcertificates | Y | Y | CADigitalCertificate |
| CodeList Services | codelists | N | N |  |
| CodeListCode Services | codelistcodes | N | N |  |
| Community Services | communities | Y | Y | Community |
| CustomJar Services | customjars | N | N |  |
| CustomProtocol Services | customprotocols | N | N |  |
| CustomService Services | customservices | N | N |  |
| CustomSFGExtensions Services | customsfgextensions | N | N |  |
| Digital Certificate Duplicate Check Services | digitalcertificateduplicatechecks | N | N |  |
| Document Services | documents | N | Y |  |
| External User Services | externalusers | N | N |  |
| FgArrivedFile Services | fgarrivedfiles | N | Y |  |
| FgDelivery Services | fgdeliveries | N | Y |  |
| FgRoute Services | fgroutes | N | Y |  |
| Generated Password Services | generatedpasswords | N | N |  |
| HttpClientAdapter Services | httpclientadapters | N | N |  |
| Identity Services | identities | N | N |  |
| JDBC Service Tracking Services | jdbcservicetrackings | N | N |  |
| Mailbox Services | mailboxes | Y | Y | Mailbox | WS api implemented |
| Mailbox Content Services | mailboxcontents | N | Y |  |
| Mailbox Message Services | mailboxmessages | N | Y |  |
| Message Batch Services | messagebatches | N | N |  |
| Partner Group Services | partnergroups | Y | Y | FGPartnerGroup |
| Permission Services | permissions | Y | Y | Permission |
| PGPKey Services | pgpkeys | N | N |  |
| PGP Server Profile Services | pgpserverprofiles | N | N |  |
| Property Services | properties | Y | Y | Property |
| PropertyFile Services | propertyfiles | Y | Y | PropertyFiles |
| PropertyNodeValue Services | propertynodevalues | Y | Y | Property |
| Routing Channel Services | routingchannels | Y | Y | RoutingChannel |
| Routing Channel Duplicate Check Services | routingchannelduplicatechecks | N | N |  |
| Routing Rule Services | routingrules | N | N |  |
| Schedule Services | schedules | N | Y |  |
| ServiceDefinition Services | servicedefinitions | N | Y |  |
| ServiceInstance Services | serviceinstances | N | Y |  |
| SSH Authorized User Key Services | sshauthorizeduserkeys | Y | Y | SshAuthorizedUserKey | WS api implemented |
| SSH Duplicate Check Services | sshduplicatechecks | N | N |  |
| SSH Host Identity Key Grabber Services | sshhostidentitykeygrabbers | N | N | SshKnownHostKey |
| SSH Known Host Key Services | sshknownhostkeys | Y | Y | SshKnownHostKey | WS api implemented (not in BP) |
| SSH Remote Profile Services | sshremoteprofiles | N | N |  |
| SSH User Identity Key Services | sshuseridentitykeys | Y | Y | SshUserIdentityKey |
| Sterling Connect Direct Netmap Services | sterlingconnectdirectnetmaps | Y | Y | SterlingConnectDirectNetmap |
| Sterling Connect Direct Netmap Xref Services | sterlingconnectdirectnetmapxrefs | Y | Y | SterlingConnectDirectNetmapXref |
| Sterling Connect Direct Node Services | sterlingconnectdirectnodes | Y | Y | SterlingConnectDirectNode |
| Sterling Connect Direct Node Duplicate Check Services | sterlingconnectdirectnodeduplicatechecks | N | N |  |
| Sterling Connect Direct XREF Duplicate Check Services | sterlingconnectdirectxrefduplicatechecks | N | N |  |
| System Digital Certificate Services | systemdigitalcertificates | Y | Y | SystemDigitalCertificate |
| TestSFGDeliveryStatus Services | testsfgdeliverystatus | N | N |  |
| Test Trading Partner Services | testtradingpartners | N | N |  |
| Trading Partner Services | tradingpartners | Y | Y | TradingPartner |
| Trusted Digital Certificate Services | trusteddigitalcertificates | Y | Y | TrustedDigitalCertificate |
| UIBranding Services | uibrandings | N | N |  |
| User Account Services | useraccounts | Y | Y | UserAccount |
| UserExit Services | userexits | N | N |  |
| User Group Services | usergroups | Y | Y | UserGroup |
| UserVirtualRoot Services | uservirtualroots | Y | Y | UserVirtualRoot |
| Workflow Services | workflows | Y | Y | Workflow | Workflow definition API |
| WorkFlowMonitor Services | workflowmonitors | Y | Y | Workflow + WorkFlowMonitor | Workflow describes a workflow instance, WorkFlowMonitor describes a workflow step |
----------------------------------------

## Custom table persistence APIs

| Table | Impl | Want | Classname | Comment |
|-|-|-|-|-|
| DD_SAMPLE_CUST_TABLE | Y | Y | DdSampleCustomTable |  |
----------------------------------------

## Other client APIs
### LDAP(s)
FtLDAP is used to connect to LDAP. LDAPUser describes a user account in LDAP.

### SSPCM client APIs
| API | Path | Impl | Want | Classname | Comment |
|-|-|-|-|-|-|
| C:D netmap | netmap | Y | Y | de.denkunddachte.ft.sspcmapi.SspCdNetmap | Edit SSP Connect:Direct netmaps |

### Custom WebService APIs (DD_WS_API)
For performance reasons, some APIs were realized in a business process `DD_WS_API`. The `DD_WS_API` must be deployed on a HTTP Server Adapter with

* User Authentication Required = Yes
* Use SSL = Must
* Restrict to group = Yes
* URI = `/ws_api`:
  * Launch BP `DD_WS_API`
  * Send Raw Messages = No
  * Run BP in sync mode = No

The GET APIs require a `Lightweight JDBC Adapter` with name `DD_JDBC_ADAPTER_01`. 
**NOTE:** `DD_WS_API` currently supports only MSSQL databases!

The `executebp` requires a generic `JavaTask` service called `DD_JavaTaskService` and a command line adapter `DD_CLA_LOCAL` to launch `workflowLauncher.sh`.

Implemented APIs:

| API | Request | Used in class | Comment |
|-|-|-|-|
| mailboxes | GET | de.denkunddachte.sfgapi.Mailbox | List mailbox ID, path, permission name, description |
| useraccounts | GET | de.denkunddachte.sfgapi.UserAccount | List login ID, email, first name, surname, user group(s) (comma separated) |
| sshauthorizeduserkeys | GET | de.denkunddachte.sfgapi.SshAuthorizedUserKey | List key name, fingerprint, key data |
| uservirtualroots | GET | de.denkunddachte.sfgapi.UserVirtualRoot | List login ID, mailbox path |
| executebp | GET, POST | de.denkunddachte.sfgapi.WorkflowDefinition | execute workflow |
| workflows | GET | de.denkunddachte.sfgapi.Workflow | Get list of executed workflows |
| wfd | GET | de.denkunddachte.sfgapi.WorkflowDefinition | Get list of WFD versions |
| processdata | GET | de.denkunddachte.sfgapi.WorkflowMonitor | Get process data for workflow step |
| togglewfd | GET | de.denkunddachte.sfgapi.WorkflowDefinition | Toggle WFD enabled flag (workaround for buggy REST API) |

URL parameters:
| Param | Value | Mandatory | Comment |
|-|-|-|-|
| api | API name | Y |  |
| searchFor | string | N | filter result (mailbox path, user id, key name) |
| casesensitive | 0, 1 | N | search for mailbox paths case sensitive |
| json | 0, 1 | N | Request result in JSON format |
| bpname | BP name | C | required for 'executebp' and 'togglewfd' APIs, optional for 'workflows' API |
| bpversion | BP version | C | specify version of BP to run or update. Required for 'togglewfd' API |
| infile | path | N | input file (must be accessible with ASI server) |
| filename | string | N | If using POST to upload primary document a filename can be set with this |
| starttime | string | N | start time range when selecting workflows (yyyyMMddHHmmss-yyyyMMddHHmmss or <n>[mh]) |
| all | 0, 1 | N | if 1, all workflows (incl. system workflows) are retrieved |
| wfcid | String | C | required for processdata api |
| enable | true,false | C | required for processdata togglewfd' API |

## Examples

### Executing workflows
Example:
```bash
$ curl -u <user>:<password> -X POST -H 'Accept: application/json' -H 'Content-Type: text/plain' --data-binary '@testfile'  "https://garfield:40443/ws_api?api=executebp&bpname=A0_TEST_BP&json=1&filename=testfile.txt"
```
launches BP `A0_TEST_BP` with content of `./testfile` as primary document with document/body name `testfile.txt`. Parameter `json=1` requests the result as JSON:

```json
{
  "LAUNCH" : {
    "RC" : 0,
    "BP" : [ {
      "EDITED_BY" : "chef1",
      "BP_NAME" : "A0_TEST_BP",
      "WFD_VERSION" : 115,
      "NUMBER_WAITS" : 1,
      "TOTAL_TIME" : "500(ms)",
      "STATE" : "COMPLETE",
      "FAILED_STEPS" : "",
      "WFD_ID" : 896,
      "DESCRIPTION" : "testInvoke",
      "REQ_STATE" : "UNKNOWN",
      "ID" : 1273585,
      "RESULT" : "SUCCESS",
      "STEPS" : 17
    }, {
      "EDITED_BY" : "chef1",
      "BP_NAME" : "A0_TEST_BP",
      "WFD_VERSION" : 115,
      "NUMBER_WAITS" : 0,
      "TOTAL_TIME" : "0(ms)",
      "STATE" : "COMPLETE",
      "PARENT_ID" : 1273585,
      "FAILED_STEPS" : "",
      "WFD_ID" : 896,
      "DESCRIPTION" : "testInvoke",
      "REQ_STATE" : "UNKNOWN",
      "ID" : 1273586,
      "RESULT" : "SUCCESS",
      "STEPS" : 17
    }, {
      "EDITED_BY" : "chef1",
      "BP_NAME" : "A0_TEST_BP",
      "WFD_VERSION" : 115,
      "NUMBER_WAITS" : 0,
      "TOTAL_TIME" : "0(ms)",
      "STATE" : "COMPLETE",
      "PARENT_ID" : 1273585,
      "FAILED_STEPS" : "",
      "WFD_ID" : 896,
      "DESCRIPTION" : "testInvoke",
      "REQ_STATE" : "UNKNOWN",
      "ID" : 1273587,
      "RESULT" : "SUCCESS",
      "STEPS" : 17
    }, {
      "EDITED_BY" : "chef1",
      "BP_NAME" : "A0_TEST_BP",
      "WFD_VERSION" : 115,
      "NUMBER_WAITS" : 0,
      "TOTAL_TIME" : "0(ms)",
      "STATE" : "COMPLETE",
      "PARENT_ID" : 1273585,
      "FAILED_STEPS" : "",
      "WFD_ID" : 896,
      "DESCRIPTION" : "testInvoke",
      "REQ_STATE" : "UNKNOWN",
      "ID" : 1273588,
      "RESULT" : "SUCCESS",
      "STEPS" : 14
    } ]
  }
}
```
The returned XML/JSON document contains a list of workflows initiated. If field `PARENT_ID` is present, the workflow was initiated by another BP.






