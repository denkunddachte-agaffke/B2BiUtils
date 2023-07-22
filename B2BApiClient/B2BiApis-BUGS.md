# IBM Sterling B2B Integrator / File Gateway REST API bugs, inconsistencies, weirdness

* Version: 6.1.0.4_1

## API Bugs

### tradingpartners

* GET: `customProtocolName` not returned

### testtradingpartners

* GET: returns 400/java.lang.NullPointerException when selecting consumer with custom protocol
* GET: returns 400/API000161 or 400/API000160 for any other partners

### customprotocols
* GET: 

### sterlingconnectdirectnodes

* GET: `securityProtocol` not returned

