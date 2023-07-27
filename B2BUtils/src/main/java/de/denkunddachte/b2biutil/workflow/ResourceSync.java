package de.denkunddachte.b2biutil.workflow;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.w3c.dom.Document;

import de.denkunddachte.exception.ApiException;
import de.denkunddachte.siresource.SIArtifact;
import de.denkunddachte.siresource.SIArtifact.TYPE;
import de.denkunddachte.siresource.SIExport;
import de.denkunddachte.utils.FileFind;

public class ResourceSync {
  private static final Logger LOG   = Logger.getLogger(ResourceSync.class.getName());

  SIExport                    si    = new SIExport();
  Pattern                     ignoreFilenames;

  public ResourceSync(String xmlData) throws ApiException {
    super();
    si.parse(xmlData);
  }

  public ResourceSync(File exportFile) throws ApiException {
    super();
    si.parse(exportFile);
  }

  public ResourceSync(Document doc) throws ApiException {
    super();
    si.parse(doc);
  }

  public SIArtifact getArtifact(TYPE type, String name) {
    return si.getArtifact(type, name);
  }
  
  public Map<String, Result> sync(String rootDir) throws IOException {
    Map<String, Result> filemap = new HashMap<>();
    XmlDiff diff = new XmlDiff();
    for (Path p : FileFind.find(rootDir, "glob:*.[bx][ps][ml][lt]", true, ignoreFilenames)) {
      SIArtifact artifact = new SIArtifact(getArtifactTypeFromExt(p), getBasename(p).replaceAll("-\\d+(?:-\\[default\\])?$", ""));
      Result r = filemap.get(artifact.key());
      if (r == null) {
        r = new Result(artifact.getType(), artifact.getName());
        filemap.put(artifact.key(), r);
      }
      r.addPath(p);
      SIArtifact remote = si.getArtifact(artifact.getType(), artifact.getName());
      if (remote == null) {
        r.setResult(SyncResult.LOCAL_ONLY);
      } else if (diff.compare(p.toFile(), remote.getStringData())) {
        r.setResult(SyncResult.MODIFIED);
      } else {
        r.setResult(SyncResult.UNMODIFIED);
      }
    }

    for (SIArtifact remote : si.getArtifacts()) {
      if (filemap.containsKey(remote.key())) {
        if (filemap.get(remote.key()).getResult() == SyncResult.UNDEF) {
          LOG.log(Level.WARNING, "Unexpected UNDEF state for artifact: {0}", remote);
        }
      } else {
        filemap.put(remote.key(), new Result(remote.getType(), remote.getName(), SyncResult.REMOTE_ONLY));
      }
    }
    return filemap;
  }

  private TYPE getArtifactTypeFromExt(Path path) {
    String ext = getExtension(path);
    if (ext != null) {
      switch (ext.toUpperCase()) {
      case "BPML":
        return TYPE.WFD;
      case "XSLT":
        return TYPE.XSLT;
      }
    }
    return null;
  }

  private String getBasename(Path path) {
    final String name = path.getFileName().toString();
    if (name.indexOf('.') > 0) {
      return name.substring(0, name.lastIndexOf('.'));
    } else {
      return name;
    }
  }

  private String getExtension(Path path) {
    final String name = path.getFileName().toString();
    if (name.indexOf('.') > 0) {
      return name.substring(name.lastIndexOf('.') + 1);
    } else {
      return null;
    }
  }

  public Pattern getIgnoreFilenames() {
    return ignoreFilenames;
  }

  public void setIgnoreFilenames(Pattern ignoreFilenames) {
    this.ignoreFilenames = ignoreFilenames;
  }

  public enum SyncResult {
    UNDEF, UNMODIFIED, MODIFIED, LOCAL_ONLY, REMOTE_ONLY
  }

  public static final class Result {
    private Set<Path>  paths  = new LinkedHashSet<>(1);
    private TYPE       artifactType;
    private String     artifactName;
    private SyncResult result = SyncResult.UNDEF;

    public Result(TYPE type, String name) {
      this.artifactType = type;
      this.artifactName = name;
    }

    public Result(TYPE type, String name, SyncResult result) {
      this.artifactType = type;
      this.artifactName = name;
      this.result = result;
    }

    public SyncResult getResult() {
      return result;
    }

    public void setResult(SyncResult result) {
      this.result = result;
    }

    public Set<Path> getPaths() {
      return Collections.unmodifiableSet(paths);
    }

    public TYPE getArtifactType() {
      return artifactType;
    }

    public String getArtifactName() {
      return artifactName;
    }

    public void addPath(Path path) {
      paths.add(path);
    }

    @Override
    public String toString() {
      return "Result [artifact=" + artifactType + ":" + artifactName + ", paths=" + paths.size() + ": " + result + "]";
    }
  }
}
