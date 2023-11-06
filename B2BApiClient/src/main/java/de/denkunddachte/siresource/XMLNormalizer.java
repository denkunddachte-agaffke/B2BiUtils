package de.denkunddachte.siresource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import de.denkunddachte.exception.ApiException;

public class XMLNormalizer {

  private DocumentBuilder docBuilder;
  private Transformer     transformer;
  private static XMLNormalizer instance;

  public XMLNormalizer() throws ApiException {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setIgnoringComments(true);
      docBuilder = dbf.newDocumentBuilder();
      TransformerFactory tf = TransformerFactory.newInstance();
      tf.setAttribute("indent-number", 0);
      transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty(OutputKeys.INDENT, "no");
    } catch (ParserConfigurationException | TransformerConfigurationException e) {
      throw new ApiException(e);
    }
  }
  
  public static XMLNormalizer instance() throws ApiException {
    if (instance == null) {
      instance = new XMLNormalizer();
    }
    return instance;
  }

  private void removeWhiteSpaceNodes(Element e) {
    NodeList cl = e.getChildNodes();
    for (int i = cl.getLength() - 1; i >= 0; i--) {
      Node c = cl.item(i);
      if (c instanceof Text && ((Text) c).getData().trim().isEmpty()) {
        e.removeChild(c);
      } else if (c instanceof Element) {
        removeWhiteSpaceNodes((Element) c);
      }
    }
  }

  public String normalizeString(byte[] xmlData) throws ApiException {
    try (InputStream is = new ByteArrayInputStream(xmlData)) {
      org.w3c.dom.Document doc = docBuilder.parse(is);
      removeWhiteSpaceNodes(doc.getDocumentElement());
      OutputStream os = new ByteArrayOutputStream();
      transformer.transform(new DOMSource(doc), new StreamResult(os));
      return os.toString();
    } catch (SAXException | IOException | TransformerException e) {
      throw new ApiException(e);
    }
  }

  public String createHash(byte[] xmlData) throws ApiException {
    MessageDigest digest = null;
    try {
      digest = MessageDigest.getInstance("SHA-256");
      
    } catch (NoSuchAlgorithmException nae) {
      throw new ApiException(nae);
    }
    OutputStream nos = new OutputStream() {
      @Override
      public void write(int b) throws IOException {
      }

      @Override
      public void write(byte[] b, int off, int len) throws IOException {
      }
    };
    try (InputStream is = new ByteArrayInputStream(xmlData); DigestOutputStream dos = new DigestOutputStream(nos, digest)) {
      org.w3c.dom.Document doc = docBuilder.parse(is);
      removeWhiteSpaceNodes(doc.getDocumentElement());
      transformer.transform(new DOMSource(doc), new StreamResult(dos));
    } catch (SAXException | IOException | TransformerException e) {
      throw new ApiException(e);
    }
    return Base64.getEncoder().encodeToString(digest.digest());
  }
}
