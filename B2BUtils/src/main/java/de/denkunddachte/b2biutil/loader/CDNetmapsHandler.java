package de.denkunddachte.b2biutil.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;

import de.denkunddachte.util.ApiConfig;
import de.denkunddachte.exception.ApiException;
import de.denkunddachte.ft.CDNode;
import de.denkunddachte.sfgapi.SterlingConnectDirectNetmap;
import de.denkunddachte.sfgapi.SterlingConnectDirectNode;
import de.denkunddachte.sspcmapi.SspCdNetmap;

public class CDNetmapsHandler {
  private static final Logger                            LOGGER           = Logger.getLogger(CDNetmapsHandler.class.getName());
  private final Map<String, SterlingConnectDirectNetmap> sfgNetmaps       = new HashMap<>(2);
  private final Map<String, SspCdNetmap>                 sspNetmaps       = new HashMap<>(2);
  private final Map<String, Boolean>                     sfgNmDirty       = new HashMap<>(2);
  private final Map<String, Boolean>                     sspNmDirty       = new HashMap<>(2);
  private final Set<String>                              sfgNodesToDelete = new HashSet<>();
  private static CDNetmapsHandler                        instance;

  private CDNetmapsHandler() {
    // singleton
  }

  public static CDNetmapsHandler getInstance() {
    if (instance == null) {
      instance = new CDNetmapsHandler();
    }
    return instance;
  }

  public SterlingConnectDirectNetmap getSfgNetmap(String name) throws ApiException {
    if (!sfgNetmaps.containsKey(name)) {
      sfgNetmaps.put(name, SterlingConnectDirectNetmap.find(name));
      sfgNmDirty.put(name, false);
    }
    return sfgNetmaps.get(name);
  }

  public SspCdNetmap getSspNetmap(String name) throws ApiException {
    if (!sspNetmaps.containsKey(name)) {
      sspNetmaps.put(name, new SspCdNetmap(name));
      sspNmDirty.put(name, false);
    }
    return sspNetmaps.get(name);
  }

  public void addNodeToSfgNetmap(String netmapName, CDNode node, boolean updateExisting) throws ApiException {
    SterlingConnectDirectNetmap nm = getSfgNetmap(netmapName);
    if (nm != null && (!nm.hasNode(node.getNodeName()) || updateExisting) && (updateExisting ? nm.addOrUpdateNode(node, updateExisting) : nm.addNewNode(node))) {
      sfgNmDirty.put(netmapName, true);
    }
  }

  public void removeNodeFromSfgNetmap(String netmapName, String nodeName, boolean deleteNode) throws ApiException {
    SterlingConnectDirectNetmap nm = getSfgNetmap(netmapName);
    if (nm.hasNode(nodeName)) {
      nm.removeNode(nodeName);
      sfgNmDirty.put(netmapName, true);
      if (deleteNode)
        sfgNodesToDelete.add(nodeName);
    }
  }

  public void addNodeToSspNetmap(String netmapName, CDNode node, boolean updateExisting) throws ApiException {
    SspCdNetmap sspnm = getSspNetmap(netmapName);
    if (sspnm != null && (!sspnm.hasNode(node.getNodeName()) || updateExisting) && sspnm.addNewNode(node))
      sspNmDirty.put(netmapName, true);
  }

  public void removeNodeFromSspNetmap(String netmapName, String nodeName) throws ApiException, JSONException {
    SspCdNetmap sspnm = getSspNetmap(netmapName);
    if (sspnm != null && sspnm.removeNode(nodeName))
      sspNmDirty.put(netmapName, true);
  }

  public boolean flushSfgNetmap(String netmapName) throws ApiException {
    if (Boolean.TRUE.equals(sfgNmDirty.get(netmapName)) && sfgNetmaps.get(netmapName) != null) {
      LOGGER.log(Level.FINE, "Update modified SFG netmap {0}...", netmapName);
      return sfgNetmaps.get(netmapName).update();
    }
    for (String nodeName : sfgNodesToDelete) {
      if (SterlingConnectDirectNode.exists(nodeName)) {
        SterlingConnectDirectNode node = SterlingConnectDirectNode.find(nodeName);
        node.delete();
        LOGGER.log(Level.INFO, "Deleted node {0} from SFG.", nodeName);
      }
    }
    return false;
  }

  public void flushSfgNetmaps() throws ApiException {
    for (String nm : sfgNetmaps.keySet()) {
      flushSfgNetmap(nm);
    }
  }

  public boolean flushSspNetmap(String netmapName) throws ApiException {
    if (Boolean.TRUE.equals(sspNmDirty.get(netmapName)) && sspNetmaps.get(netmapName) != null) {
      LOGGER.log(Level.FINE, "Close SSP netmap {0}...", netmapName);
      sspNetmaps.get(netmapName).close();
      return true;
    }
    return false;
  }

  public void flushSspNetmaps() throws ApiException {
    for (String nm : sspNetmaps.keySet()) {
      flushSspNetmap(nm);
    }
  }

  public List<String> getAllNetmaps() throws ApiException {
    final List<String> result = new ArrayList<>();
    for (SterlingConnectDirectNetmap sfgNm : SterlingConnectDirectNetmap.findAll()) {
      sfgNetmaps.put(sfgNm.getName(), sfgNm);
      result.add("SFG:" + sfgNm.getName());
    }

    if (ApiConfig.getInstance().getSspBaseURI() != null) {
      try {
        for (String sspNm : SspCdNetmap.getAllNetmaps()) {
          sspNetmaps.put(sspNm, new SspCdNetmap(sspNm));
          result.add("SSP:" + sspNm);
        }
      } catch (JSONException e) {
        throw new ApiException("Could not get SSP netmaps!", e);
      }
    }
    return result;
  }
}
