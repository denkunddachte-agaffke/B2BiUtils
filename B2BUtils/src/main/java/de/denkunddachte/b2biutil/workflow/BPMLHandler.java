package de.denkunddachte.b2biutil.workflow;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.denkunddachte.b2biutil.workflow.BPML.Activity;
import de.denkunddachte.b2biutil.workflow.BPML.ActivityType;

public class BPMLHandler extends DefaultHandler {
  private static final Logger LOG         = Logger.getLogger(BPMLHandler.class.getName());

  // BPML elements
  private static final String PROCESS     = "process";
  private static final String ASSIGN      = "assign";
  private static final String CHOICE      = "choice";
  private static final String OPERATION   = "operation";
  private static final String PARTICIPANT = "participant";
  private static final String ON_FAULT    = "onFault";
  /*
  private static final String SEQUENCE    = "sequence";
  private static final String RULE        = "rule";
  private static final String CONDITION   = "condition";
  private static final String SELECT      = "select";
  private static final String CASE        = "case";
  private static final String OUTPUT      = "output";
  private static final String INPUT       = "input";
  private static final String REPEAT      = "repeat";
  */
  
  private BPML                bpml;
  private Locator             locator;

  private StringBuilder       value;
  private BPML.Activity       currentAct;
  private int                 depth       = 0;
  private boolean operation = false;

  public BPMLHandler() {
    super();
  }

  @Override
  public void startDocument() throws SAXException {
    super.startDocument();
    LOG.log(Level.FINEST, "Enter startDocument().");
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    LOG.log(Level.FINEST, "Enter characters(). value: {0}", value());
    if (value != null) {
      value.append(ch, start, length);
    }
  }

  @Override
  public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) throws SAXException {
    LOG.log(Level.FINEST, "Enter startElement(): uri={0}, localName={1}, qName={2}, attributes={3}; depth={4}",
        new Object[] { uri, localName, qName, attributes, depth });
    if (++depth <= 0 && bpml == null) {
      throw new SAXException("Root element \"" + PROCESS + "\" not found!");
    }
    switch (qName) {
    case PROCESS:
      bpml = new BPML(attributes.getValue("name"), depth, getLocation());
      break;
    case ASSIGN:
      if (!operation) {
        currentAct = new Activity(ActivityType.ASSIGN, attributes.getValue("to"), depth, getLocation());
        currentAct.setOptVal(attributes.getValue("to"));
      } 
      break;
    case PARTICIPANT:
      if (currentAct != null && currentAct.getType() == ActivityType.OPERATION) {
        currentAct.setOptVal(attributes.getValue("name"));
      } else {
        throw new SAXException("Element \"" + qName + "\" not expected at " + getLocation());
      }
      break;
    case OPERATION:
      operation = true;
    case CHOICE:
      currentAct = new Activity(ActivityType.valueOf(qName.toUpperCase()), attributes.getValue("name"), depth, getLocation());
      break;
    case ON_FAULT:
      bpml.addActivity(new Activity(ActivityType.ONFAULT, attributes.getValue("code"), depth, getLocation()));
      break;
    default:
      LOG.log(Level.FINEST, "Assume text node: {0}", qName);
      break;
    }
    value = new StringBuilder();
  }

  private String getLocation() {
    if (locator != null) {
      return String.format("[%d,%d]", locator.getLineNumber(), locator.getColumnNumber());
    }
    return null;
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    LOG.log(Level.FINEST, "Enter endElement(): uri={0}, localName={1}, qName={2}; depth={3}, value={4}",
        new Object[] { uri, localName, qName, depth, value() });
    if (--depth > 0 && bpml == null) {
      throw new SAXException("Closing element " + qName + " not expected!");
    }

    switch (qName) {
    case ON_FAULT:
      break;
    case OPERATION:
      operation = false;
    default:
      if ( currentAct != null) {
        bpml.addActivity(currentAct);
        currentAct = null;
      }
      break;
    }
    value = null;
  }

  @Override
  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }

  public BPML getBPML() {
    return bpml;
  }

  private String value() {
    if (value == null) {
      return null;
    } else {
      if (value.length() > 100) {
        return value.substring(0, 100) + "...";
      } else {
        return value.toString();
      }
    }
  }

}
