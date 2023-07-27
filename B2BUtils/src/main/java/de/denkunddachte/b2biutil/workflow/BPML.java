package de.denkunddachte.b2biutil.workflow;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import de.denkunddachte.exception.ApiException;

public class BPML {
  public enum ActivityType {
    INIT, CHOICE, OPERATION, ASSIGN, ONFAULT
  }

  private final List<Activity> activities = new ArrayList<>();
  private String               name;

  public BPML(String bpName, int depth, String location) {
    super();
    this.name = bpName;
    activities.add(new Activity(ActivityType.INIT, bpName, depth, location));
  }

  public void addActivity(Activity currentAct) {
    currentAct.id = activities.size();
    activities.add(currentAct);
  }

  public String getName() {
    return this.name;
  }

  public List<Activity> getActivities() {
    return this.activities;
  }

  public Activity getActivity(int id) throws ApiException {
    if (id < 0 || id >= activities.size()) {
      throw new ApiException("No such activity " + id + " in WFD " + name + "!");
    }
    return this.activities.get(id);
  }

  public String getActivityDescription(int id) throws ApiException {
    Activity a = getActivity(id);
    switch (a.getType()) {
    case INIT:
      return "INIT";
    case ONFAULT:
      return "onFault" + (a.getName() != null ? " code=" + a.getName() : "");
    case CHOICE:
      return "Choice \"" + a.getName() + "\"";
    case OPERATION:
      return a.getName() + " [" + a.getOptVal() + "]";
    case ASSIGN:
      return "Assign to \"" + a.getName() + "\"";
    default:
      break;
    }
    return a.getName() + (a.getOptVal() == null ? "" : " [" + a.getOptVal() + "]");
  }

  public String getActivityLocation(int id, String main) throws ApiException {
    Activity a = getActivity(id);
    return a.getLocation() + (main == null || !main.equalsIgnoreCase(name) ? " " + name : "");
  }

  @Override
  public String toString() {
    return "BPML [name=" + name + ", activities=" + activities.size() + "]";
  }

  public static class Activity {
    private ActivityType type;
    private int          id;
    private int          depth;
    private String       name;
    private String       location;
    private String       optVal;

    public Activity(ActivityType type, String name, int depth, String location) {
      this.type = type;
      this.name = name;
      this.depth = depth;
      this.location = location;
    }

    public ActivityType getType() {
      return type;
    }

    public String getName() {
      return name;
    }

    public String getLocation() {
      return location;
    }

    public int getId() {
      return id;
    }

    public int getDepth() {
      return depth;
    }

    public String getOptVal() {
      return optVal;
    }

    public void setOptVal(String optVal) {
      this.optVal = optVal;
    }

    @Override
    public String toString() {
      return "Activity " + id + " [type=" + type + ", depth=" + depth + ", name=" + name + (optVal != null ? " [" + optVal + "]" : "") + ", location="
          + location + "]";
    }
  }

  public static BPML parse(File infile) throws ApiException {
    BPML bpml = null;
    try (InputStream is = new FileInputStream(infile)) {
      bpml = BPML.parse(is);
    } catch (IOException e) {
      throw new ApiException(e);
    }
    return bpml;
  }

  public static BPML parse(String bpml) throws ApiException {
    return BPML.parse(new ByteArrayInputStream(bpml.getBytes()));
  }

  public static BPML parse(InputStream is) throws ApiException {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    try {
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      SAXParser sax = factory.newSAXParser();
      sax.getXMLReader().setFeature("http://xml.org/sax/features/external-general-entities", false);
      BPMLHandler bpmlHandler = new BPMLHandler();
      sax.parse(is, bpmlHandler);
      return bpmlHandler.getBPML();
    } catch (SAXException | ParserConfigurationException | IOException e) {
      throw new ApiException(e);
    }
  }
}
