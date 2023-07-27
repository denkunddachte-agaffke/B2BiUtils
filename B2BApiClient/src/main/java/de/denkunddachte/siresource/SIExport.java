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
package de.denkunddachte.siresource;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.denkunddachte.util.ApiConfig;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.exception.NotImplementedException;
import de.denkunddachte.siresource.SIArtifact.TYPE;

public class SIExport {
  private static final int      XSLT_TYPE            = 4;
  private static final int      XSLT_DOWNLOADABLE    = 1;
  private static final int      XSLT_DEFAULT_VERSION = -1;
  private static final boolean  XSLT_DATA            = true;

  private final Set<SIArtifact> artifacts            = new LinkedHashSet<>();

  public SIExport() {
    super();
  }

  public Collection<SIArtifact> getArtifacts() {
    return Collections.unmodifiableCollection(artifacts);
  }

  public Collection<SIArtifact> getArtifacts(final TYPE type) {
    return Collections.unmodifiableCollection(artifacts.stream().filter(a -> a.getType() == type).collect(Collectors.toSet()));
  }

  public SIArtifact getArtifact(final TYPE type, final String name) {
    Optional<SIArtifact> v = artifacts.stream().filter(a -> a.getType() == type && a.getName().equalsIgnoreCase(name) && a.isDefaultVersion()).findFirst();
    return v.isPresent() ? v.get() : null;
  }

  public SIArtifact getArtifact(final TYPE type, final String name, final int version) {
    Optional<SIArtifact> v = artifacts.stream()
        .filter(a -> a.getType() == type && a.getName().equalsIgnoreCase(name) && (version < 1 || a.getVersion() == version)).findFirst();
    return v.isPresent() ? v.get() : null;
  }

  public boolean putArtifact(SIArtifact artifact) {
    return artifacts.add(artifact);
  }

  public boolean removeArtifact(SIArtifact artifact) {
    return artifacts.remove(artifact);
  }

  public void clearArtifacts() {
    artifacts.clear();
  }

  public SIArtifact getArtifact(SIArtifact artifact) {
    return getArtifact(artifact.getType(), artifact.getName(), artifact.getVersion());
  }

  public boolean hasArtifact(SIArtifact artifact) {
    if (artifact.getVersion() == -1) {
      return getArtifact(artifact) != null;
    } else {
      return artifacts.contains(artifact);
    }
  }

  public boolean isEmpty() {
    return artifacts.isEmpty();
  }

  public int size() {
    return artifacts.size();
  }

  public void parse(Document doc) throws ApiException {
    XPathFactory xPathFactory = XPathFactory.newInstance();
    XPath        xpath        = xPathFactory.newXPath();
    // add more types...
    try {
      XPathExpression expr = xpath.compile("/SI_RESOURCES/BPDEFS/BPDEF|/SI_RESOURCES/XSLTS/XSLT");
      NodeList        nl   = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

      for (int i = 0; i < nl.getLength(); i++) {
        Node       n        = nl.item(i);
        SIArtifact artifact = null;
        String     v;
        if (n.getNodeName().equalsIgnoreCase("XSLT")) {
          artifact = new SIArtifact(SIArtifact.TYPE.XSLT, (String) xpath.evaluate("./METADATA/NAME", n, XPathConstants.STRING));
          v = (String) xpath.evaluate("./METADATA/VERSION", n, XPathConstants.STRING);
          if (v != null && !v.isEmpty()) {
            artifact.setVersion(Integer.parseInt(v));
          }
          artifact.setData((String) xpath.evaluate("./TemplateData/SIBinaryFile", n, XPathConstants.STRING));
          artifact.setComment((String) xpath.evaluate("./METADATA/COMMENTS", n, XPathConstants.STRING));
          artifact.setModifiedBy((String) xpath.evaluate("./METADATA/USERNAME", n, XPathConstants.STRING));
          v = (String) xpath.evaluate("./METADATA/CREATE_DATE", n, XPathConstants.STRING);

          artifact.setModifyTime(OffsetDateTime.ofInstant(Instant.ofEpochMilli(Long.valueOf(v)), ZoneId.systemDefault()));
          artifact.setDefaultVersion(Boolean.parseBoolean((String) xpath.evaluate("./METADATA/SIResourceDefaultVersion", n, XPathConstants.STRING)));
          artifact.setEnabled(Boolean.parseBoolean((String) xpath.evaluate("./METADATA/STATUS", n, XPathConstants.STRING)));
        } else if (n.getNodeName().equalsIgnoreCase("BPDEF")) {
          artifact = new SIArtifact(SIArtifact.TYPE.WFD, (String) xpath.evaluate("./ConfigResource/ConfProcessName", n, XPathConstants.STRING));
          v = (String) xpath.evaluate("./ConfigResource/OBJECT_VERSION", n, XPathConstants.STRING);
          if (v != null && !v.isEmpty()) {
            artifact.setVersion(Integer.parseInt(v));
          }
          artifact.setData((String) xpath.evaluate("./LangResource", n, XPathConstants.STRING));
          artifact.setComment((String) xpath.evaluate("./ConfigResource/ConfDescription", n, XPathConstants.STRING));
          artifact.setModifiedBy((String) xpath.evaluate("./ConfigResource/ConfLastUsed", n, XPathConstants.STRING));
          artifact.setDefaultVersion(Boolean.parseBoolean((String) xpath.evaluate("./ConfigResource/SIResourceDefaultVersion", n, XPathConstants.STRING)));
          artifact.setEnabled("1".equals((String) xpath.evaluate("./ConfigResource/ConfStatus", n, XPathConstants.STRING)));
        } else {
          throw new NotImplementedException("SI export artifact type " + n.getNodeName() + " not implemented yet!");
        }
        artifacts.add(artifact);
      }
    } catch (UnsupportedEncodingException | XPathExpressionException e) {
      throw new ApiException("Failed to parse document!", e);
    }
  }

  private DocumentBuilder getDocBuilder() throws ApiException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try {
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      factory.setXIncludeAware(false);
      factory.setExpandEntityReferences(false);
      return factory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new ApiException(e);
    }
  }

  public void parse(File file) throws ApiException {
    try {
      parse(getDocBuilder().parse(file));
    } catch (ApiException | SAXException | IOException e) {
      throw new ApiException("Failed to parse file!", e);
    }
  }

  public void parse(String xmldata) throws ApiException {
    try {
      parse(getDocBuilder().parse(new InputSource(new StringReader(xmldata))));
    } catch (ApiException | SAXException | IOException e) {
      throw new ApiException("Failed to parse data!", e);
    }
  }

  private Document createImportData() throws ApiException {
    Document doc = getDocBuilder().newDocument();
    try {
      Element root = doc.createElement("SI_RESOURCES");
      root.setAttribute("xmlns", "http://www.stercomm.com/SI/SI_IE_Resources");
      root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
      root.setAttribute("GISVersion", "6100");
      root.setAttribute("FrameworkVersion", "2");
      doc.appendChild(root);
      if (!getArtifacts(TYPE.WFD).isEmpty()) {
        Node nl = doc.createElement("BPDEFS");
        root.appendChild(nl);
        throw new NotImplementedException("Creating import structures for WFDs not implemented yet!");
      }
      if (!getArtifacts(TYPE.XSLT).isEmpty()) {
        Node nl = doc.createElement("XSLTS");
        root.appendChild(nl);
        for (SIArtifact artifact : getArtifacts(TYPE.XSLT)) {
          nl.appendChild(createXSLTNode(artifact, doc));
        }
      }
      return doc;
    } catch (DOMException e) {
      throw new ApiException("Failed to build XML document!", e);
    }
  }

  private Element createXSLTNode(SIArtifact artifact, Document doc) throws ApiException {
    Element xslt = doc.createElement("XSLT");
    Element md   = doc.createElement("METADATA");
    xslt.appendChild(md);
    md.appendChild(doc.createElement("NAME")).appendChild(doc.createTextNode(artifact.getName()));
    md.appendChild(doc.createElement("TYPE")).appendChild(doc.createTextNode(String.valueOf(XSLT_TYPE)));
    md.appendChild(doc.createElement("VERSION")).appendChild(doc.createTextNode(String.valueOf(artifact.getVersion())));
    OffsetDateTime d = artifact.getModifyTime() == null ? OffsetDateTime.now() : artifact.getModifyTime();
    md.appendChild(doc.createElement("CREATE_DATE")).appendChild(doc.createTextNode(String.valueOf(d.toInstant().toEpochMilli())));
    String comment = artifact.getComment() == null ? "Created/updated via SIExport" : artifact.getComment();
    md.appendChild(doc.createElement("COMMENTS")).appendChild(doc.createTextNode(comment));
    md.appendChild(doc.createElement("BASE_HREF")).appendChild(doc.createTextNode(""));
    md.appendChild(doc.createElement("DOC_ENCODING")).appendChild(doc.createTextNode(""));
    md.appendChild(doc.createElement("USERNAME")).appendChild(doc.createTextNode(ApiConfig.getInstance().getUser()));
    md.appendChild(doc.createElement("DESCRIPTION")).appendChild(doc.createTextNode(artifact.getDescription() == null ? comment : artifact.getDescription()));
    md.appendChild(doc.createElement("STATUS")).appendChild(doc.createTextNode(String.valueOf(artifact.isEnabled())));
    md.appendChild(doc.createElement("DOWNLOADABLE")).appendChild(doc.createTextNode(String.valueOf(XSLT_DOWNLOADABLE)));
    md.appendChild(doc.createElement("DEFAULT_VERSION")).appendChild(doc.createTextNode(String.valueOf(XSLT_DEFAULT_VERSION)));
    md.appendChild(doc.createElement("OBJECT_VERSION")).appendChild(doc.createTextNode(String.valueOf(artifact.getVersion())));
    if (artifact.isDefaultVersion()) {
      md.appendChild(doc.createElement("SIResourceDefaultVersion")).appendChild(doc.createTextNode("true"));
    }
    md.appendChild(doc.createElement("LATEST_VERSION")).appendChild(doc.createTextNode(String.valueOf(artifact.getVersion())));
    md.appendChild(doc.createElement("DATA")).appendChild(doc.createTextNode(String.valueOf(XSLT_DATA)));
    Element templdata = doc.createElement("TemplateData");
    xslt.appendChild(templdata);
    Element sidata = doc.createElement("SIBinaryFile");
    templdata.appendChild(sidata);
    sidata.setAttribute("filename", artifact.getName());
    sidata.appendChild(doc.createTextNode(artifact.getEncodedData(false)));

    return xslt;
  }

  public void createImport(OutputStream os) throws ApiException {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer        transformer;
    try {
      transformer = transformerFactory.newTransformer();
      DOMSource    source = new DOMSource(createImportData());
      StreamResult result = new StreamResult(os);
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.transform(source, result);
    } catch (TransformerException e) {
      throw new ApiException("Failed to build XML document!", e);
    }
  }

  public static void main(String[] args) throws ApiException {
    if (args.length < 2) {
      System.err.println("usage: SIExportFile list <export file>");
      System.exit(1);
    }
    SIExport si = new SIExport();
    if ("list".equals(args[0])) {
      si.parse(new File(args[1]));
      if (!si.isEmpty()) {
        System.out.format("%-5s %-50s %-3s%n", "Type", "Name", "Ver");
        System.out.println("--------------------------------------------------------------------------");
      }
      for (SIArtifact a : si.getArtifacts()) {
        System.out.format("%-5s %-50s %-3d%n", a.getType(), a.getName(), a.getVersion());
      }
      si.createImport(System.out);
    }
  }
}
