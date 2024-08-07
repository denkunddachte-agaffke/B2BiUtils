/*
  Copyright 2021 denk & dachte Software GmbH

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
package de.denkunddachte.b2biutils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * JavaTask tester. Use to debug JavaTask code with mocked ProcessData and PrimaryDocuments.
 *
 * Implement as static method with name beginning with "test". Then method must have signature:
 * <pre>
 * public static String test<Name>(String... args);
 * </pre>
 *
 * Add required imports as comments at to of method and uncomment when deploying in BP/service.
 *
 * NOTE 1: code must be Java 1.6 or earlier compatible! So, no try with resources etc. :-(
 * NOTE 2: avoid auto boxing/unboxing!
 *
 * @author A. Gaffke
 *
 */
public final class JavaTask {
  // If you want to use JDBCService, provide info, if not, leave JDBC_URL empty.
  private static final String        JDBC_POOL     = "mssqlPool";
  private static final String        JDBC_DRIVER   = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
  private static final String        JDBC_URL      = "jdbc:sqlserver://dbhost:1433;databaseName=SFGDEV1DB;SelectMethod=cursor;sslProtocol=TLSv1.2;trustServerCertificate=true";
  private static final String        JDBC_USER     = "sfgdev1user";
  private static final String        JDBC_PASSWORD = "GehHeim!";
  private static final MockWFContext wfc           = new JavaTask.MockWFContext();
  private static final MockXLogger   log           = new JavaTask.MockXLogger();
  private static final MockManager   Manager       = new MockManager(); // Manager is static class in B2Bi
  private static String              OUTFILE;

  static {
    if (JDBC_URL != null && !JDBC_URL.isEmpty()) {
      JDBCService.getInstance(JDBC_POOL, JDBC_DRIVER, JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    }
  }

  private JavaTask() {
    // static only!
  }

  /**
   * Define your test code in public static test<name>(String... args) methods here:
   */

  /**
   * IBM's sample JavaTask (see <a href="https://www.ibm.com/support/pages/how-work-ibm-sterling-b2b-integrator-javatask-service">...</a>)
   * @return String ("OK")
   * @throws Exception
   */
  public static String testJavaTaskSample(final String... args) throws Exception {
    // Embedded ProcessData to use for this test (alternatively use -p <file> to provide your own):
    // <ProcessData>
    // <InputNumberValueOne>1</InputNumberValueOne>
    // <InputNumberValueTwo>2</InputNumberValueTwo>
    // <InputStringValueOne>String 1</InputStringValueOne>
    // <InputStringValueTwo>String 2</InputStringValueTwo>
    // <nodeOne>
    // <child>one</child>
    // <child>two</child>
    // <child>three</child>
    // </nodeOne>
    // </ProcessData>
    //
    // Set a (dummy) primary doc for test (alternatively use -i <file> to provide primary doc file):
    // PrimaryDocumentName: Document1.txt
    // PrimaryDocumentData: Testdoc data
    // more data
    // ...
    // EOF

    // Add imports as comments here and uncomment when pasting into JavaTask service:
    // import java.util.Random;
    // import org.w3c.dom.Node; import org.w3c.dom.NodeList;import org.w3c.dom.Element;
    // import java.io.InputStream; import java.io.OutputStream;
    // import com.sterlingcommerce.woodstock.workflow.Document;
    // import com.sterlingcommerce.woodstock.util.frame.Manager;

    // Setup dummy property:
    Manager.setProperty("myprops", "test", "Test value.");
    //

    String test = Manager.getProperty("myprops", "test");
    log.log("Run JavaTaskSample... test=" + test);
    Random randomGenerator = new Random();
    int    randomInt       = randomGenerator.nextInt(6) + 1;
    String s               = Integer.toString(randomInt);
    wfc.addWFContent("Random", s);

    String numberOne       = (String) wfc.getWFContent("InputNumberValueOne");
    String numberTwo       = (String) wfc.getWFContent("InputNumberValueTwo");
    int    firstNumber     = Integer.parseInt(numberOne);
    int    secondNumber    = Integer.parseInt(numberTwo);
    int    product         = firstNumber * secondNumber;
    String productAsString = Integer.toString(product);
    wfc.addWFContent("ProductOfMultiplication", productAsString);

    String stringOne    = (String) wfc.getWFContent("InputStringValueOne");
    String stringTwo    = (String) wfc.getWFContent("InputStringValueTwo");
    String resultString = stringOne.concat(stringTwo).toUpperCase();
    wfc.addWFContent("ResultOfConcat", resultString);

    Node     node             = (Node) wfc.getWFContent("nodeOne");
    NodeList nl               = node.getChildNodes();
    int      children         = nl.getLength();
    String   childrenAsString = Integer.toString(children);
    wfc.addWFContent("NumberOfChildren", childrenAsString);

    Node   childOne           = nl.item(1).getFirstChild();
    String lookupElementValue = childOne.getNodeValue();
    wfc.addWFContent("ValueOfChild", lookupElementValue);

    Element newElement = node.getOwnerDocument().createElement("ElementThree");
    newElement.setTextContent("ValueThree");
    node.appendChild(newElement);

    Document    doc               = wfc.getPrimaryDocument();
    String      bodyName          = doc.getBodyName();
    InputStream is                = doc.getBodyInputStream();
    int         numberOfBytesRead = 0;
    while (is.read() != -1) {
      numberOfBytesRead++;
    }
    is.close();
    String bytesReadAsString = Integer.toString(numberOfBytesRead);
    wfc.addWFContent("DocumentSize", bytesReadAsString);
    wfc.addWFContent("DocumentName", bodyName);

    String       newContent  = "This is the content of the new Primary Document";
    Document     newDocument = new Document();
    OutputStream os          = newDocument.getOutputStream();
    os.write(newContent.getBytes());
    os.flush();
    os.close();
    wfc.putPrimaryDocument(newDocument);

    return "OK";
  }

  /**
   * Base64 encode primary document and set as new primary doc
   */
  public static String testEncodeBase64(String... args) throws Exception {
    // import com.sterlingcommerce.woodstock.workflow.Document;
    // import java.util.Base64; import java.util.Base64.Encoder;
    // import java.io.InputStream; import java.io.OutputStream;

    // PrimaryDocumentData: Hello World!

    Encoder      enc    = Base64.getEncoder();
    Document     doc    = wfc.getPrimaryDocument();
    Document     newDoc = new Document();
    InputStream  is     = doc.getBodyInputStream();
    OutputStream os     = enc.wrap(newDoc.getOutputStream());
    byte[]       buf    = new byte[1024];
    int          b;
    while ((b = is.read(buf)) > -1) {
      os.write(buf, 0, b);
    }
    is.close();
    os.close();
    wfc.putPrimaryDocument(newDoc);
    return "OK";
  }

  public static String testXML2JSON(String... args) throws Exception {
    // import java.io.InputStream; import java.io.OutputStream;
    // import com.sterlingcommerce.woodstock.workflow.Document;
    // import org.json.JSONObject; import org.json.XML;
    JSONObject   json = XML.toJSONObject(new String(wfc.getPrimaryDocument().getBody()));
    Document     doc  = wfc.newDocument();
    OutputStream os   = doc.getOutputStream();
    os.write(json.toString().getBytes());
    os.flush();
    os.close();
    wfc.putPrimaryDocument(doc);
    return "JSON";
  }

  /**
   * Internal:
   */
  public static void main(final String[] args) {
    // WorkFlowContext asiWfc = new WorkFlowContext(171753);

    if (args.length == 0)
      usage(1);

    try {
      boolean userPd  = false;
      boolean userDoc = false;
      int     p       = 0;
      while (p < args.length) {
        String arg = args[p++];
        switch (arg) {
        case "-h":
          usage(0);
          break;
        case "-u":
          System.out.println(unescapeString(args[p++]));
          break;
        case "-e":
          String esc = methodToEscapedString(args[p++]);
          System.out.println(esc);
          System.out.println("UNESCAPED:");
          System.out.println(unescapeString(esc));
          System.out.println("LENGTH=" + unescapeString(esc).length());
          break;
        case "-p":
          StringBuilder sb = new StringBuilder();
          try (BufferedReader rd = new BufferedReader(new FileReader(args[p] == null ? "ProcessData.xml" : args[p++]))) {
            String line;
            while ((line = rd.readLine()) != null) {
              sb.append(line);
            }
          }
          wfc.setPdDocument(sb.toString());
          userPd = true;
          break;
        case "-i":
          wfc.setInputPrimaryDocument(args[p++]);
          userDoc = true;
          break;
        case "-o":
          wfc.setOutputPrimaryDocument(args[p++]);
          break;
        default:
          String methodToExecute = arg;
          Object[] argsToMethod = new String[] {};
          if (p < args.length) {
            argsToMethod = Arrays.copyOfRange(args, p, args.length);
          }

          boolean found = false;
          int rc = 0;
          for (Method method : JavaTask.class.getMethods()) {
            if (method.getName().toLowerCase().startsWith(methodToExecute.toLowerCase())
                || method.getName().toLowerCase().replaceFirst("test", "").startsWith(methodToExecute.toLowerCase())) {
              found = true;
              System.out.println("Execute TEST method: " + method.getName() + "(" + String.join(", ", (String[]) argsToMethod) + ")...");
              if (!userPd) {
                wfc.setPdDocument(JavaTask.getEmbeddedProcessData(method.getName()));
              }
              if (!userDoc) {
                wfc.setInputPrimaryDocument(JavaTask.getEmbeddedPrimaryDocument(method.getName()));
              }
              System.out.println("ProcessData at start:");
              System.out.println(wfc);
              System.out.println();
              try {
                String result = (String) method.invoke(null, (Object) argsToMethod);
                System.out.println("RESULT: " + result);
              } catch (InvocationTargetException e) {
                rc = 2;
                System.out.println("EXCEPTION during execution:");
                e.getTargetException().printStackTrace(System.out);
              }
              System.out.println();
              System.out.println("ProcessData after execute:");
              System.out.println(wfc);
              break;
            }
          }
          if (!found) {
            usage(1);
          }
          System.exit(rc);
          break;
        }
      }
    } catch (Exception e) {
      System.err.println("Caught exception: " + e.getMessage());
      try {
        System.err.println("FN/ERRTYPE=" + wfc.getWFContent("FN/ERRTYPE"));
        System.err.println("FN/ERR=" + wfc.getWFContent("FN/ERR"));
      } catch (Exception e1) {
        // NOOP
      }
      e.printStackTrace();
    }
  }

  public static void usage(int exit) {
    System.out.println("usage: JavaTask [-h] [-p <pd file>] [-i <infile>] [-o <outfile>] [-u <escapedString>] [-e <method>] <method>");
    System.out.println();
    System.out.println("Options:");
    System.out.println("  -p <file>       Load process data from file (default: empty ProcessData document)");
    System.out.println("  -i <infile>     Set input PrimaryDocument (for wfc.getPrimaryDocument())");
    System.out.println("  -o <outfile>    Set output file (for wfc.setOutputPrimaryDocument(), default: JavaTask-out.dat)");
    System.out.println("  -e <method>     prints escaped and compacted JavaTask code");
    System.out.println("  -u <string>     pretty prints escaped and compacted JavaTask code");
    System.out.println("  -h              print this help=");
    System.out.println();
    System.out.println("Available TEST JavaTasks (methods named test*):");
    System.out.println(Arrays.asList(JavaTask.class.getMethods()).stream().filter(m -> m.getName().startsWith("test")).map(s -> " - " + s.getName())
        .collect(Collectors.joining(System.lineSeparator())));
    System.out.println();

    System.exit(exit);
  }

  private static String unescapeString(String in) {
    return in.replaceAll("&quot;", "\"").replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&");
  }

  private static void append(StringBuilder sb, Object val, boolean skip) {
    if (!skip)
      sb.append(val);
  }

  private static File getSourceFile() {
    File         f  = new File(JavaTask.class.getProtectionDomain().getCodeSource().getLocation().getPath(),
        JavaTask.class.getName().replace(".", "/") + ".java");
    // If running in eclipse, look for source file in src folder (assuming default output mapping src/main/java -> bin/main)
    final String LF = (new File(".")).separator;
    if (!f.exists()) {
      f = new File(f.getAbsolutePath().replace("/bin/main/".replace("/", LF), "/src/main/java/".replace("/", LF)));
    }
    return f;
  }

  private static String getEmbeddedProcessData(String methodName) throws IOException {
    StringBuilder sb = null;
    try (BufferedReader rd = new BufferedReader(new FileReader(getSourceFile()))) {
      String  line;
      boolean lookForMethod = true;
      while ((line = rd.readLine()) != null) {
        if (lookForMethod) {
          if (line.contains(methodName + "(")) {
            lookForMethod = false;
          } else {
            continue;
          }
        } else {
          if (line.matches("\\s*(?:private|public|protected).+?\\S+\\(.+")) {
            break;
          }
          if (sb != null) {
            sb.append(line.replaceAll("^\\s*//\\s*", ""));
          } else if (line.contains("<ProcessData>")) {
            sb = new StringBuilder(line);
          } else if (line.contains("ProcessDataFile:")) {
            File f = new File(line.replaceAll(".*ProcessDataFile:\\s*(.+)\\s*$", "$1"));
            return new String(Files.readAllBytes(Paths.get(f.toURI())), Charset.forName("UTF-8"));
          }
          if (line.contains("</ProcessData>")) {
            break;
          }
        }
      }
    }

    if (sb != null) {
      return sb.substring(sb.indexOf("<ProcessData>"), sb.indexOf("</ProcessData>") + 14);
    } else {
      return null;
    }
  }

  private static Document getEmbeddedPrimaryDocument(String methodName) throws IOException {
    StringBuilder sb   = null;
    String        name = null;
    try (BufferedReader rd = new BufferedReader(new FileReader(getSourceFile()))) {
      String  line;
      boolean lookForMethod = true;
      while ((line = rd.readLine()) != null) {
        if (lookForMethod) {
          if (line.contains(methodName + "(")) {
            lookForMethod = false;
          } else {
            continue;
          }
        } else {
          if (line.matches("\\s*(?:private|public|protected).+?\\S+\\(.+")) {
            break;
          }
          if (line.contains("PrimaryDocumentName:")) {
            name = line.replaceAll(".*PrimaryDocumentName:\\s*(.+)\\s*$", "$1");
          } else if (line.contains("PrimaryDocumentData:")) {
            sb = new StringBuilder(line.replaceAll(".*PrimaryDocumentData:\\s*(.+)\\s*$", "$1"));
            if (sb.length() > 0) {
              sb.append(System.lineSeparator());
            }
          } else if (line.contains("PrimaryDocumentFile:")) {
            File f = new File(line.replaceAll(".*PrimaryDocumentFile:\\s*(.+)\\s*$", "$1"));
            if (f.exists()) {
              return new Document(f);
            }
          } else {
            if (sb != null) {
              if (!line.contains("//") || line.trim().replaceAll("^//\\s*", "").equals("EOF")) {
                break;
              }
              sb.append(line.replaceAll("^\\s*//\\s*", "")).append(System.lineSeparator());
            }
          }
        }
      }
    }
    if (sb != null && sb.length() > 0 && name == null) {
      name = "document.txt";
    }
    if (sb == null && name != null) {
      sb = new StringBuilder("Default sample data...");
    }
    if (name != null) {
      return new Document(name, (sb != null ? sb.toString() : null));
    } else {
      return null;
    }
  }

  private static String methodToEscapedString(String methodName) throws IOException {
    final File    srcFile = getSourceFile();
    String        src     = null;
    StringBuilder o       = new StringBuilder(2048);
    try (InputStream is = new FileInputStream(srcFile)) {
      byte[] b = new byte[(int) srcFile.length()];
      is.read(b);
      src = (new String(b, "UTF-8")).replaceAll("//\\s*import\\s+", "import ");
    }
    int     p          = src.indexOf('{', src.indexOf(" " + methodName + "(", 0));
    boolean comment    = false;
    boolean quotetd    = false;
    int     bracedepth = 0;
    while (p < src.length() && bracedepth >= 0) {
      char c = src.charAt(p++);
      switch (c) {
      case '{':
        if (bracedepth++ > 0)
          append(o, c, comment);
        break;
      case '}':
        if (--bracedepth > 0)
          append(o, c, comment);
        if (bracedepth == 0)
          bracedepth--;
        break;
      case '"':
        quotetd = !quotetd;
        append(o, "&quot;", comment);
        break;
      case '/':
        if (!quotetd && src.charAt(p) == '/') {
          p++;
          comment = true;
        } else {
          append(o, c, comment);
        }
        break;
      case '\r':
      case '\n':
        comment = false;
        break;
      case ' ':
      case '\t':
        char last = (o.length() > 0 ? o.charAt(o.length() - 1) : 0);
        if (quotetd || (last >= 'a' && last <= 'z') || (last >= 'A' && last <= 'Z'))
          append(o, c, comment);
        break;
      case '&':
        append(o, "&amp;", comment);
        break;
      case '<':
        append(o, "&lt;", comment);
        break;
      case '>':
        append(o, "&gt;", comment);
        break;
      default:
        append(o, c, comment);
        break;
      }
    }
    return o.toString().replace('\t', ' ').replaceAll("\\s+([+/*&=({'\"%-])", "$1");
  }

  /**
   * Mock WFContext class mimicking com.sterlingcommerce.woodstock.workflow.WorkFlowContext class.
   * Currently, only read/write operations on ProcessData and get/set PrimaryDocument operations are implemented!
   * @author A. Gaffke
   *
   */
  static class MockWFContext {
    // @formatter:off
		public static final String	STRIP_SPACE_XSL	= 
				"<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>"
				+ "<xsl:output method='xml' omit-xml-declaration='yes'/><xsl:strip-space elements='*'/>"
				+ "<xsl:template match='@*|node()'><xsl:copy><xsl:apply-templates select='@*|node()'/></xsl:copy></xsl:template>" 
				+ "</xsl:stylesheet>";
		// @formatter:on

    public final static String   PD_ROOT         = "ProcessData";
    private Document             inputPrimaryDocument;
    private String               outputPrimaryDocument;
    private String               advStatus;
    private int                  basicStatus;
    private boolean              errorAdd;
    private org.w3c.dom.Document pdDoc;
    Element                      pdRoot;

    public MockWFContext() {
      super();
      try {
        setPdDocument(null);
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(1);
      }
    }

    @SuppressWarnings("unchecked")
    public Object getWFContent(String key, @SuppressWarnings("rawtypes") List v, boolean elem_only, boolean enableXPath) throws Exception {
      if (!elem_only) {
        throw new IllegalArgumentException("elem_only = false not implemented in MockWFContext (yet)!");
      }
      if (v == null) {
        return getNodes(key, enableXPath, false);
      } else {
        Object o = getNodes(key, enableXPath, false);
        if (o != null && o instanceof NodeList) {
          NodeList nl = (NodeList) o;
          for (int i = 0; i < nl.getLength(); i++) {
            v.add(nl.item(i));
          }
          return v;
        }
      }
      return null;
    }

    public org.w3c.dom.Document toDOM() {
      return pdDoc;
    }

    public Object getWFContent(String key) throws Exception {
      return getWFContent(key, false);
    }

    public Object getWFContent(String key, boolean enableXPath) throws Exception {
      Object o = getNodes(key, enableXPath, false);
      if (o == null) {
        return null;
      }
      // if (enableXPath) {
      // if (o instanceof NodeList) {
      // return o;
      // }
      // }

      Node n = (Node) o;
      if (!n.hasChildNodes()) {
        return n.getTextContent();
      }
      if (n.getChildNodes().getLength() == 1) {
        Node c = n.getChildNodes().item(0);
        if (c.getNodeType() == NodeType.TEXT_NODE.ordinal() || c.getNodeType() == NodeType.CDATA_SECTION_NODE.ordinal()) {
          return c.getTextContent();
        }
      }
      return o;
    }

    private Object getNodes(String path, boolean createIfNotExists) throws Exception {
      return getNodes(path, false, createIfNotExists);
    }

    private Object getNodes(String path, boolean returnList, boolean createIfNotExists) throws Exception {
      String simplePath = path.replaceAll("\\[.+?\\]", "").replaceAll("^[a-z0-9_-]+\\((.+)\\)", "$1").replaceAll("[a-z0-9_-]+\\(.*\\)", "").replaceAll("[/*]+$",
          "");
      XPath  xp         = XPathFactory.newInstance().newXPath();
      Object result;
      if (!simplePath.startsWith("/") && !simplePath.startsWith("//")
          && !simplePath.substring(0, (simplePath.indexOf('/') > 0 ? simplePath.indexOf('/') : simplePath.length() - 1)).equals(PD_ROOT)) {
        result = xp.compile(path).evaluate(pdDoc.getDocumentElement(), (returnList ? XPathConstants.NODESET : XPathConstants.NODE));
      } else {
        result = xp.compile(path).evaluate(pdDoc, (returnList ? XPathConstants.NODESET : XPathConstants.NODE));
      }
      if (createIfNotExists) {
        NodeList nl = (NodeList) result;
        if ((nl == null || nl.getLength() == 0) && !simplePath.matches("^(?://.+|/ProcessData|/)$")) {
          if (!simplePath.startsWith("/") && !simplePath.matches("^/" + PD_ROOT + "(/.*|)$")) {
            simplePath = "/" + PD_ROOT + "/" + simplePath;
          }
          if (!simplePath.matches("^/" + PD_ROOT + "(/.*|)$")) {
            throw new Exception("Invalid path: " + path);
          }

          String   parentPath = simplePath.substring(0, simplePath.lastIndexOf('/'));
          NodeList pl         = (NodeList) getNodes(parentPath, createIfNotExists);

          if (pl != null) {
            Node parent = pl.getLength() == 0 ? (Node) pl : pl.item(pl.getLength() - 1).getParentNode();
            parent.appendChild(pdDoc.createElement(simplePath.substring(simplePath.lastIndexOf('/') + 1)));
            result = getNodes(simplePath, false);
          }
        }
      }
      return result;
    }

    public void addWFContent(String key, Object val) throws Exception {
      setWFContent(key, val, true);
    }

    public void setWFContent(String key, Object val) throws Exception {
      setWFContent(key, val, false);
    }

    public void setWFContent(String key, Object val, boolean append) throws Exception {
      Node n = (Node) getNodes(key, true);
      if (n == null) {
        throw new Exception("Node " + key + " does not exist!");
      }
      if (val instanceof Node) {
        n.appendChild((Node) val);
        return;
      }
      String textVal = (val instanceof String ? (String) val : (val != null ? val.toString() : null));
      if (!n.hasChildNodes()) {
        setTextContent(n, textVal);
      } else {
        if (append) {
          setTextContent(n.getParentNode().appendChild(pdDoc.createElement(n.getNodeName())), textVal);
        } else {
          setTextContent(n, textVal);
        }
      }
    }

    private void setTextContent(Node n, String val) {
      if (val != null && val.indexOf('\n') > -1) {
        n.appendChild(pdDoc.createCDATASection(val));
      } else {
        n.setTextContent(val);
      }
    }

    public void setInputPrimaryDocument(String inputPrimaryDocument) {
      this.inputPrimaryDocument = new Document(inputPrimaryDocument);
    }

    public void setInputPrimaryDocument(Document doc) {
      this.inputPrimaryDocument = doc;
    }

    public String getOutputPrimaryDocument() {
      return outputPrimaryDocument;
    }

    public void setOutputPrimaryDocument(String outputPrimaryDocument) {
      JavaTask.OUTFILE = outputPrimaryDocument;
      this.outputPrimaryDocument = outputPrimaryDocument;
    }

    public Document getPrimaryDocument() {
      return inputPrimaryDocument;
    }

    public void putPrimaryDocument(Document doc) {
      System.out.println("WFC: putPrimaryDocument: " + doc.getBodyName());
    }

    public Document getDocument(String key) throws Exception {
      File f = new File((String) getWFContent(key));
      if (f.exists()) {
        return new Document(f);
      } else {
        throw new Exception("No file found for document key " + key + "!");
      }
    }

    public void putDocument(Document doc) {
      System.out.println("WFC: putDocument: " + doc.getBodyName());
    }

    public void putDocument(String key, Document doc) {
      System.out.println("WFC: putDocument[" + key + "]: " + doc.getBodyName());
    }

    public Document newDocument() {
      return new Document();
    }

    public String getAdvancedStatus() {
      return advStatus;
    }

    public void setAdvancedStatus(String status) {
      this.advStatus = status;
    }

    public int getBasicStatus() {
      return basicStatus;
    }

    public void setBasicStatus(int code) {
      this.basicStatus = code;
    }

    public boolean isErrorAdd() {
      return errorAdd;
    }

    public void setErrorAdd(boolean errorAdd) {
      this.errorAdd = errorAdd;
    }

    public org.w3c.dom.Document getPdDocument() {
      return this.pdDoc;
    }

    public void setPdDocument(String processData) throws ParserConfigurationException, SAXException, IOException, TransformerException {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      if (processData == null || processData.trim().isEmpty()) {
        pdDoc = dbf.newDocumentBuilder().newDocument();
        // create empty ProcessData with one element
        pdDoc.appendChild(pdDoc.createElement("ProcessData")).appendChild(pdDoc.createElement("DUMMY")).setTextContent("Dummy process data...");
      } else {
        // parse prepared ProcessData and "un-prettyprint" to avoid whitespace text nodes:
        DOMResult result = new DOMResult();
        TransformerFactory.newInstance().newTransformer(new StreamSource(new ByteArrayInputStream(STRIP_SPACE_XSL.getBytes())))
            .transform(new DOMSource(dbf.newDocumentBuilder().parse(new ByteArrayInputStream(processData.getBytes()))), result);;
        pdDoc = (org.w3c.dom.Document) result.getNode();
      }
    }

    public void toXML(OutputStream os, boolean indent, int indentAmount) throws TransformerException {
      TransformerFactory tf = TransformerFactory.newInstance();
      tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
      Transformer t = tf.newTransformer();
      t.setOutputProperty(OutputKeys.METHOD, "xml");
      t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      if (indent) {
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(indentAmount));
      }
      pdDoc.setXmlStandalone(true);
      DOMSource src = new DOMSource(pdDoc);
      t.transform(src, new StreamResult(os));
    }

    @Override
    public String toString() {
      try {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        toXML(bos, true, 2);
        return bos.toString();
      } catch (TransformerException e) {
        e.printStackTrace();
        return "ERROR: " + e.getMessage();
      }
    }
  }

  /**
   * Mock logger class.
   * @author A. Gaffke
   *
   */
  static class MockXLogger {
    public MockXLogger() {

    }

    public void log(String msg) {
      System.out.println("XLOG: " + msg);
    }
  }

  /**
   * Mock com.sterlingcommerce.woodstock.workflow.Document class.
   *
   * @author A. Gaffke
   *
   */
  static class Document {
    private String bodyName;
    private File   docfile;
    private byte[] body;

    public Document() {
      this((String) null);
    }

    public Document(String bodyName) {
      if (bodyName != null) {
        File f = new File(bodyName);
        if (f.canRead()) {
          this.docfile = f;
          this.bodyName = f.getName();
        } else {
          this.bodyName = bodyName;
        }
      }
    }

    public Document(String bodyName, String content) {
      this.bodyName = bodyName;
      if (content != null) {
        this.body = content.getBytes();
      }
    }

    public Document(File f) {
      this.docfile = f;
      this.bodyName = f.getName();
    }

    public OutputStream getOutputStream() throws FileNotFoundException {
      String filename;
      if (JavaTask.OUTFILE != null) {
        filename = JavaTask.OUTFILE;
      } else if (bodyName == null) {
        filename = "JavaTask-out.dat";
      } else {
        int pos = bodyName.lastIndexOf('.');
        if (pos == -1) {
          filename = bodyName + ".out";
        } else {
          filename = bodyName.substring(0, pos) + "-out" + bodyName.substring(pos);
        }
      }
      bodyName = filename;
      File f = new File(filename);
      System.out.format("Write PrimaryDocument \"%s\" to %s%n", bodyName, f.getAbsolutePath());
      return new FileOutputStream(f);
    }

    public String getBodyName() {
      return bodyName;
    }

    public InputStream getBodyInputStream() throws FileNotFoundException {
      if (docfile != null) {
        return new FileInputStream(docfile);
      } else {
        if (body == null) {
          return new ByteArrayInputStream(body);
        }
        return new ByteArrayInputStream(body);
      }
    }

    public void setBodyName(String bodyName) {
      this.bodyName = bodyName;
    }

    public byte[] getBody() {
      return body;
    }

    public void setBody(byte[] body) {
      this.body = body;
    }

    public long getSize() {
      return this.docfile.length();
    }
  }

  /**
   * org.w3c.dom.Node types.
   * @author A. Gaffke
   *
   */
  public enum NodeType {
    // @formatter:off
		UNDEF, 
		ELEMENT_NODE, 
		ATTRIBUTE_NODE, 
		TEXT_NODE, 
		CDATA_SECTION_NODE, 
		ENTITY_REFERENCE_NODE, 
		ENTITY_NODE, 
		PROCESSING_INSTRUCTION_NODE, 
		COMMENT_NODE, 
		DOCUMENT_NODE, 
		DOCUMENT_TYPE_NODE, 
		DOCUMENT_FRAGMENT_NODE, 
		NOTATION_NODE;
		// @formatter:on

    public static NodeType byCode(short code) {
      for (NodeType t : values()) {
        if (t.ordinal() == code) {
          return t;
        }
      }
      return UNDEF;
    }

    @Override
    public String toString() {
      return this.ordinal() + "[" + this.name() + "]";
    }
  }

  /**
   * Mock com.sterlingcommerce.woodstock.util.frame.jdbc.JDBCService class implementing  getConnection() methods
   * @author A. Gaffke
   *
   */
  static class JDBCService {
    private String                                jdbcDriver;
    private String                                connectionString;
    private String                                username;
    private String                                password;
    private String                                poolName;
    private final static Map<String, JDBCService> instances = new HashMap<>();
    private static String                         defaultPool;

    public JDBCService(String poolName, String jdbcDriver, String connectionString, String username, String password) {
      super();
      this.poolName = poolName;
      this.jdbcDriver = jdbcDriver;
      this.connectionString = connectionString;
      this.username = username;
      this.password = password;
    }

    public static JDBCService getInstance(String poolName, String jdbcDriver, String connectionString, String username, String password) {
      if (instances.get(poolName) == null) {
        if (instances.size() == 0) {
          defaultPool = poolName;
        }
        instances.put(poolName, new JDBCService(poolName, jdbcDriver, connectionString, username, password));

      }
      return instances.get(poolName);
    }

    public static Connection getConnection() throws Exception {
      return getConnection(defaultPool);
    }

    public static Connection getConnection(String poolName) throws Exception {
      JDBCService instance = instances.get(poolName);
      if (instance == null) {
        throw new IllegalArgumentException("No JDBC pool \"" + poolName + "\" instantiated!");
      }
      Class.forName(instance.getJdbcDriver());
      return DriverManager.getConnection(instance.getConnectionString(), instance.getUsername(), instance.getPassword());
    }

    public String getPoolName() {
      return poolName;
    }

    public String getJdbcDriver() {
      return jdbcDriver;
    }

    public String getConnectionString() {
      return connectionString;
    }

    public String getUsername() {
      return username;
    }

    public String getPassword() {
      return password;
    }

  }

  /**
  * Mock com.sterlingcommerce.woodstock.util.frame.Manager class for reading/writing properties
  * @author A. Gaffke
  *
  */
  @SuppressWarnings("unused")
  static class MockManager {
    static final Map<String, Properties> cache   = new HashMap<>();
    public static final String           DEFAULT = "_DEFAULT_";
    private static String                current;

    static {
      cache.put(DEFAULT, new Properties());
      current = DEFAULT;
    }

    private MockManager() {
    }

    public void setProperty(String propertyFile, String key, String value) {
      if (cache.get(propertyFile) == null) {
        cache.put(propertyFile, new Properties());
      }
      cache.get(propertyFile).setProperty(key, value);
    }

    public String getProperty(String propertyFile, String key) throws Exception {
      return getProperty(propertyFile, key, null);
    }

    public String getProperty(String propertyFile, String key, String defaultVal) throws Exception {
      if (cache.get(propertyFile) == null) {
        throw new Exception("No such property file: " + propertyFile + "!");
      }
      return cache.get(propertyFile).getProperty(key, defaultVal);
    }

    public long getIntProperty(String propertyFile, String key) throws Exception {
      return getIntProperty(propertyFile, key, 0);
    }

    public int getIntProperty(String propertyFile, String key, int defaultVal) throws Exception {
      return Integer.parseInt(getProperty(propertyFile, key, String.valueOf(defaultVal)));
    }

    public long getLongProperty(String propertyFile, String key) throws Exception {
      return getLongProperty(propertyFile, key, 0);
    }

    public long getLongProperty(String propertyFile, String key, long defaultVal) throws Exception {
      return Long.parseLong(getProperty(propertyFile, key, String.valueOf(defaultVal)));
    }

    public String getStringProperty(String propertyFile, String key, String defaultVal) throws Exception {
      String v = getProperty(propertyFile, key);
      return v == null ? defaultVal : v;
    }
  }
}
