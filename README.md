# B2BiUtils
Manage todos, docs and bugs for B2Bi API utils.

# Bugs/issues
Please report issues in [issues](https://github.com/denkunddachte-agaffke/B2BiUtils/issues).

# TODOs

## `B2BApiClient` package 

The `B2BApiClient` jar contains  

* frontend classes to access (selected) SFG/B2Bi REST APIs providing static lookup methods to retrieve objects from the server, constructory to create new object on the server and update/delete methods to modify and delete objects.

* JPA classes to retrieve, create, modify and delete data in custom tables

* client classes to access Sterling Secure Proxy configuration (currently limited to manipulating Connect:Direct netmaps)

* client to retrieve, create, modify and delete LDAP users on the FT LDAP server, especially methods to manipulate (add/remove) user's SSH authentication keys used in conjunction with Sterling Secure Proxy and Sterling External Authentication Server (SEAS)

* classes to read and create B2Bi `SIExport` resource files (WFD and XSLT types only)

* a `DD_API_WS` API helper business process to be deployed on a HTTP Server Adapter. This BP provide some helper functions to retrieve bulk data directly from the database where the REST APIs are not very performant, provide methods to execute workflows, run resource import and export etc.

## `ddutils` package

The `ddutils` jar contains common utility classes like

* simple command line parser
* a configuration container 
* passwword encryption


## Utilities

The `B2BiUtils` pacakge provides some command line utilities to help administration and development of business processes.

The tools are "work in progress" and in different states of maturirty.

### `b2bWorkflowMgr`

Utility to help develop BPs. It provides options to upload/download WFDs and XSLTs, execute and (to an extent) trace BPs and manage resources (sync/compare local WFDs and XSLTs with remote).

Status: WFD up-/download, execute/trace stable, XSLT support: alpha...

```bash
Usage: de.denkunddachte.b2biutil.workflow.WorkflowUtil [options] [<args>]

Utility for deploying and testing B2Bi business processes 


General options:
  -T, --xslt                  Manage XLSTs

Get WFDs/XSLTs from server:
  -L, --list-wfd [str]        List WFDs/XSLTs (optional filtered by WFD name)
  -g, --get <str>             Get WFD/XSLT with name
  -f, --file <str>            BPML/XSLT filename
  -a, --allVersions           Get all versions of WFD/XSLT
  -v, --version <n>           Get/execute WFD/XSLT version (use 0 to get default version
  --getall [str]              Get all WFDs/XSLTs matching pattern (regex)
  --outdir <str>              Output directory for --getall and --get
  --parseWfd                  Show parsed WFD (for step trace)

Sync/compare WFDs and XSLTs with local workspace:
  -c, --compare <str>         Compare WFD/XSLT. Option must either point to a BPML/XSLT file or must contain the WFD/XSLT name (in this case the file must be specified with --file option).
  --verbose                   Verbose (show differences)
  -e, --export <str>          Export resources to local file (BPs and XSLTs). Use --force to overwrite existing export file.
  -i, --import <str>          Import resources file (BPs and XSLTs)
  --include <str> (=^AZ_|^A0_|^DD_)
                              Export include pattern (see doc for export service)
  --exclude <str> (=^UHU$)    Export exclude pattern (see doc for export service)
  -s, --sync <str>            Sync WFD, XSLT, properties with local dir
  --ignorePaths <str> (=^.*/BP/all/.+)
                              Regex to ignore local paths from sync
  -x, --extract [str]         Extract resources differing local files (use --force to overwrite all). Optional: specify subdirs for resource types (e.g. WFD=BP:XSLT=XSLT)

Update WFD/XSLT on server:
  -p, --put <str>             Update/create WFD/XSLT. Option must either point to a BPML/XSLT file or must contain the WFD/XSLT name (in this case the file must be specified with --file option).
  -m, --message <str>         Commit message (description)
  --setVersionInfo [str] (=true)
                              Add/modify version info comment in BPML to force change recognition (optional arg: add additional message)
  --setAsDefault, --no-setAsDefault (=true)
                              Set new version as default
  --force                     Force update even if WFD are same

Delete WFDs on server:
  -d, --delete <str>          Delete WFD with name (requires --version)
  --deleteAll <str>           Try to delete all versions of WFD
  --includeDefaultVersion     Include default version with --deleteAll

List/show workflow executions:
  -l, --list-bps [str]        List workflow executions (optional filtered by WFD name)
  --all                       Include system workflows (workflows with WFD.TYPE > 1)
  --startTime <str> (=1h)     List workflows with start time during last <n>[hm] or in range <yyyyMMddHHmmss>-<yyyyMMddHHmmss>
  --failed                    Show only unsuccessful workflows
  --noutf8                    Avoid UTF-8 output
  -S, --show <n>              Show workflow with ID <n>
  -X, --traceWfd              Trace WFD execution

Execute BP:
  -E, --execute <str>         Execute workflow name
  -P, --primaryDocument <str> Use file as primary document.
  --data <str>                Use string data as primary document.
  -n, --filename <str>        Set filename for primary document.
  --pd <str>                  Get processdata for steps (comma separated list, if empty, get PD for all steps). With --outdir, PD ist written to file(s).

Common options:
----------------------------------------------------------------------------------------
  -C, --configfile <str> (=/home/chef/apiconfig.properties)
                              Path to API config file.
  --yes                       Assume yes in intercative actions.
  --showversion               Show version information.
  --updateWsApi               Install/update WS API WFD on server.
  -D, --debug <str> (=OFF)    Set debug to stdout to level (use java.util.logging level)
  --help (=true)              Show this help.

Some help... 

Please report bugs, change requests, ideas at https://github.com/denkunddachte-agaffke/B2BiUtils/issues
```

### `b2bLdapAdmin`

Frontend to manage FT LDAP users (list, create, modify, LDIF export), especially manage SSH keys.

Status: production ready

```bash
Usage: de.denkunddachte.b2biutil.api.LdapAdmin [options] [<args>]

FT LDAP admin utility.


General options:
  -A, --admin                 List/edit FT admin users

List LDAP users and keys:
  -L, --list [str]            List users (optional: pattern)
  -v, --details               List also users and group permissions
  --md5                       Display SSH key hashes as MD5 instead of SHA-256
  --caseSensitive, --no-caseSensitive (=false)
                              User ids are case sensitive
  --export <str>              Export data to file/directory (if argument is a directory, LDIF files will be created per user)

Create LDAP users:
  -c, --create <str>          Create user with CN. Use --password and/or --addKey to set password and/or SSH keys(s)
  -n, --name <str>            Set user givenName (required for admin users, defaults to CN for FT users)

Modify LDAP (add/remove SSH keys, set password):
  -u, --user <str>            Modify user CN (use --admin to edit FT admin users)
  -a, --addKey@ <str>         Add SSH public key(s) (takes filename or string with openSSH or SSH2 formatted key)
  -d, --deleteKey <n>         Delete SSH key (takes key index as shown with -L -v)
  --deleteAllKeys             Delete all SSH keys
  --password [str]            Change password (use empty value to clear password)
  --digest <str> (=SHA)       Hash password if not done by LDAP server with algorithm (e.g. SHA, MD5, PLAIN, NONE)

Delete LDAP users:
  --delete <str>              Delete user

Common options:
----------------------------------------------------------------------------------------
  -C, --configfile <str> (=/home/chef/apiconfig.properties)
                              Path to API config file.
  --yes                       Assume yes in intercative actions.
  --showversion               Show version information.
  -D, --debug <str> (=OFF)    Set debug to stdout to level (use java.util.logging level)
  --help (=true)              Show this help.

Examples:
  LdapAdmin -L DD.* -v
  LdapAdmin -A -c userid -n 'New Adminuser' --password
  LdapAdmin -c TESTUSER01 --addKey /path/to/pubkey --addkey 'key string'
  LdapAdmin -d TESTUSER01

```

### `b2bMailboxMgr`

Tool to (bulk) manage mailboxes (list, create, delete and manage permissions). Allows creation of mailboxes inheriting user/group permissions from parent.

Status: stable

```bash
Usage: de.denkunddachte.b2biutil.api.MailboxUtil [options] [<args>]

B2Bi mailbox management utilitiy.


List mailboxes and permissions:
  -L, --list [str]            List mailboxes (optional: starting with path)
  --caseSensitive, --no-caseSensitive (=false)
                              Mailbox paths are case sensitive
  -v, --details               List also users and group permissions
  -s, --showDescription       Show description
  -r, --recurse, --no-recurse (=false)
                              Recurse

Create mailboxes:
  -c, --create <str>          Create mailboxes (comma separated list of paths)
  --description <str> (=Created ${yyyy-MM-dd HH:mm:ss} by ${user.name})
                              Description
  -p, --createParents, --no-createParents (=false)
                              Create parent mailboxes if they don't exist
  -u, --users <str>           Add user permissions (comma separated list of users)
  -g, --groups <str>          Add group permissions (comma separated list of groups)
  --inheritPerms, --no-inheritPerms (=true)
                              Inherit user and group permissions from first existing parent

Modify mailbox permissions (users and groups):
  --modify <str>              Modify list of mailboxes
  --setUsers <str>            Set permitted users on path(s).
  --setGroups <str>           Set permitted groups on path(s).
  --addUsers <str>            Add user permissions to path(s).
  --addGroups <str>           Add group permissions to path(s).
  --removeUsers <str>         Remove user permissions to path(s).
  --removeGroups <str>        Remove group permissions to path(s).

Delete mailboxes:
  -d, --delete [str]          Delete mailboxes (comma separated list of paths). By default, only empty mailboxes will be removed.
  -f, --force                 force deletion of non-empty mailboxes (use --recurse to include submailboxes

Common options:
----------------------------------------------------------------------------------------
  -C, --configfile <str> (=/home/chef/apiconfig.properties)
                              Path to API config file.
  --yes                       Assume yes in intercative actions.
  --showversion               Show version information.
  --updateWsApi               Install/update WS API WFD on server.
  -D, --debug <str> (=OFF)    Set debug to stdout to level (use java.util.logging level)
  --help (=true)              Show this help.

Examples:
  MailboxUtil -L /Test/ -r -v
  MailboxUtil -c /Test/MFTTest/newmbx --description 'New mailbox' 
  MailboxUtil --modify /Test/MFTTest/newmbx --removeUsers A0_TESTAG01I,A0_TEST01

Please report bugs, change requests, ideas at https://github.com/denkunddachte-agaffke/B2BiUtils/issues
```

### `b2bPropertiesAdmin`

Tool to manage custom properties in DB (customer_overrides, custom prefixes). Supports listing/exporting properties, add, modify, remove operations, import properties from file and refresh properties cache on server.

Status: beta

```bash
Usage: de.denkunddachte.b2biutil.api.PropertiesManager [options] [<args>]

Manage B2Bi properties and extensions.


General options:
  -P, --prefix <str>          Properties prefix
  -O, --customerOverrides     Manage customer_overrides properties

List/get properties:
  -l, --list-files            List property files in DB
  -L, --list [str]            List properties (optional: glob pattern)
  -E, --export [str]          Export properties to file
  -I, --import <str>          Import properties from file
  --replace                   Replace with contents of file
  -c, --create <str>          Create new property prefix
  --description <str>         Description for new property prefix
  -g, --get <str>             Get property
  -s, --set@ <str>            Set property (specify key=value pairs
  -n, --node <n>              Get/set/delete property node value (node 1..n)
  -d, --delete <str>          Delete properties matching pattern
  --deletePrefix <str>        Delete properties file/prefix
  -R, --refresh               Refresh properties

Common options:
----------------------------------------------------------------------------------------
  -C, --configfile <str> (=/home/chef/apiconfig.properties)
                              Path to API config file.
  --yes                       Assume yes in intercative actions.
  --showversion               Show version information.
  --updateWsApi               Install/update WS API WFD on server.
  -D, --debug <str> (=OFF)    Set debug to stdout to level (use java.util.logging level)
  --help (=true)              Show this help.

Examples:
  PropertiesManager -l
  PropertiesManager -c myprops --description "My application props"
  PropertiesManager -P myprops -s "prop1=some value" -s "prop2=some other value"
  PropertiesManager -P myprops -s "prop1=some value for node 1" -n 1
  PropertiesManager -P myprops -L <globPattern>
  PropertiesManager -I test.properties
  PropertiesManager -P myprops -d deleteMe*
  PropertiesManager --deletePrefix myprops

Please report bugs, change requests, ideas at https://github.com/denkunddachte-agaffke/B2BiUtils/issues

```

### `b2bRefreshProperties`

Runs a `REFRESH_PROPERTIES` BP on server (may be replaced by function in `DD_API_WS`...). 

Status: stable

### `b2bPasswordUtil`

Tool to encrypt passwords for use in `apiconfig.properties`.

Status: stable

### `b2bSshKeyMapUtil`

Maintains a (file based) database with SSH keys to help identify keys by their hashes. Intended as helper to `b2bLdapAdmin` because LDAP only contains the raw key string without the comment fields.

Status: beta (better to use a DB based central key repository...)

```bash
Usage: de.denkunddachte.b2biutil.api.SshKeyMapUtil [options] [<args>]

SSH key mapping utility.

  -f, --sshKeyMapFile <str> (=${user.home}/sshkeys.txt)
                              Path to map file.
  --mapB2BiKeys (=true)       Map B2Bi SSH keys
  --includeHostKeys (=false)  Also include host keys
  --mapFiles (=false)         Map key from file(s) (provide files as optional args)
  --mapLdapKeys (=true)       Map key from LDAP
  --ignoreKeyNames <str> (=(?i)(?:-Choose-from-list-below-))
                              Ignore keys with name/comment matching regex (e.g. "pseudo-keys" in SFG for UI entry)

Common options:
----------------------------------------------------------------------------------------
  -C, --configfile <str> (=/home/chef/apiconfig.properties)
                              Path to API config file.
  --yes                       Assume yes in intercative actions.
  --showversion               Show version information.
  -D, --debug <str> (=OFF)    Set debug to stdout to level (use java.util.logging level)
  --help (=true)              Show this help.

Map SSH key hashes to key names/comments to help identify keys (e.g. for LdapAdmin utility).
Reads files either from B2B Integrator with REST API or from openSSH/SSH2 files (public key files, authorized_keys files).
```

### `sftpclient`

The `sftpclient` tool is a Java based SFTP client for testing. It supports

* public key / password authentication
* a builtin SSH key pair for testing to avoid the hassle of copying keys or having to create new keys for testing.
* generation of test files with dummy data and different sizes
* support numeric placeholder in filenames which is incremented per filename pattern (e.g. `TEST-%04d.txt`)
* generation of EICAR test files to test AV scanning on server side
* allow upload of multiple files (currently only single threaded)

```bash
Usage: de.denkunddachte.sftp.SFTPClient [options] [<args>]

Simple SFTPClient client for testing SFG.

  --configfile <str> (=${installdir}/sftp.properties)
                              Path to config file.
  -f, --localfile <str>       Local file
  --eicartest                 Send EICAR AV test string
  -p, --put <str>             PUT file (%<n>d patterns are replaced by formatted counter)
  -H, --host <str>            Host [<user>@]<host>[:<port>]
  -P, --port <n> (=22)        Port
  -u, --user <str> (=${user.name})
                              User
  -s, --size <n>              File size to upload
  -n, --count <n>             Number of files to upload
  -K, --password <str>        Password
  -i, --identity <str>        Path to private key file (Default: use builtin unless password is used)
  -k, --passphrase <str>      Passphrase for private key file.
  --knownhosts <str> (=${user.home}/.ssh/known_hosts)
                              Path to known hosts file
  -N, --nostricthostkeycheck  No strict host key checking
  --print-key [str]           Print builtin public key (optinal: format ossh or secsh)
  -D, --debug <str> (=INFO)   Set debug to stdout to level (use java.util.logging level)
  --version                   Display version and exit.
  --help                      Show this help.
```

## `JavaTask` debug helper

The `B2BiUtils` project also contains a "wrapper class" to help test and debug Java code to be executed in B2Bi's `JavaTask` services. The class provide some mockup classes for variables implicitly provided by the `JavaTask` service:

* `MockWFContext`: mockup of `com.sterlingcommerce.woodstock.workflow.WorkFlowContext`, implicitly provided in the variable `wfd`,
* `Document`: mockup of `com.sterlingcommerce.woodstock.workflow.Document` representing primary documents.
* `MockXLogger`: mockup of `??` logger, implicitly provided in the variable `log`
* `MockManager`: mockup of `com.sterlingcommerce.woodstock.util.frame.Manager`.
* `JDBCService`: mockup of `com.sterlingcommerce.woodstock.util.frame.jdbc.JDBCService class`


The mock classes only implement a small subset of methods/functionality of the mocked classes and the implementation might give different results than the original (MockWFContext when using DOM objects for example). Nevertheless, the `JavaTask` wrapper has proved to be very useful when developing...

Always keep in mind, that `JavaTask` service on provide limited language support for Java:

* compatability level JDK-1.6
* no boxing/unboxing support (implicit conversion between implicit types and the objects link int to/from Integer)
* no try-with-resources (JDK-1.7 feature anyway)

