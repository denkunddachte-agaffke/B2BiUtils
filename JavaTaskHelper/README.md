# JavaTask debugging helper

This subproject contains the JavaTask helper class used for developing and debugging JavaTasks.

## `de.denkunddachte.b2biutils.JavaTask`
A wrapper class to test java code that is executed in B2Bi JavaTask service. Contains mock classes for `WorkFlowContext`, `XLogger` and simple `JDBCService`.

### Usage
Create static method with signature `public static String testXXX(String... args)` method and add required imports as comment (these will have to be 
uncommented when putting code into JavaTask.

Run JavaTest::main():

```
usage: JavaTask [-h] [-p <pd file>] [-i <infile>] [-o <outfile>] [-u <escapedString>] [-e <method>] <method>

Options:
  -p <file>       Load process data from file (default: empty ProcessData document)
  -i <infile>     Set input PrimaryDocument (for wfc.getPrimaryDocument())
  -o <outfile>    Set output file (for wfc.setOutputPrimaryDocument(), default: JavaTask-out.dat)
  -e <method>     prints escaped and compacted JavaTask code
  -u <string>     pretty prints escaped and compacted JavaTask code
  -h              print this help=

Available TEST JavaTasks (methods named test*):
 - testParseBpExecOutput
 - testDrecomFilename
 - testFixXML
 - testUuid
 - testDecodeBase64
 - testMbxList
 - testSplitEnv
 - testHttp
 - testJavaTask
 - testRefresh
 - testEncodeBase64
 - testJavaTaskSample
 - testRand
 - testWFC
 - testDecodeMIME
 - testFixXML2
```

To test your method, run `JavaTask textXXX`.
