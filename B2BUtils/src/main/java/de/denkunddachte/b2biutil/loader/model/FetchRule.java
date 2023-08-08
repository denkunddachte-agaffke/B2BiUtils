package de.denkunddachte.b2biutil.loader.model;

import java.util.Objects;
import java.util.stream.Stream;

import de.denkunddachte.b2biutil.loader.LoaderResult.Artifact;
import de.denkunddachte.enums.FTProtocol;

public class FetchRule extends AbstractLoadRecord {
  private String   producer;
  private String   filePattern;
  private String   schedule;
  private boolean  isRegex = false;
  private FTProtocol protocol;
  private Endpoint endpoint;

  public FetchRule(LoadAction action, int line, String producer, String rcvPattern, String scheduleName) {
    super(action, line);
    this.producer = producer;
    this.filePattern = rcvPattern;
    this.schedule = scheduleName;
    this.protocol = FTProtocol.MBOX;
  }

  public String getProducer() {
    return producer;
  }

  public String getFilePattern() {
    return filePattern;
  }

  public void setRegex(boolean isRegex) {
    this.isRegex = isRegex;
  }

  public boolean isRegex() {
    return isRegex;
  }

  public FTProtocol getProtocol() {
    return protocol;
  }

  public String getSchedule() {
    return schedule;
  }

  public Endpoint getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(Endpoint endpoint) {
    this.endpoint = endpoint;
  }

  @Override
  public boolean isValid(boolean full) {
    if (full) {
      return Stream.of(action, producer, filePattern, schedule, endpoint).allMatch(Objects::nonNull) && endpoint.isValid(full);
    } else {
      return Stream.of(action, producer, filePattern, schedule).allMatch(Objects::nonNull);
    }
  }

  @Override
  public Artifact getArtifact() {
    return Artifact.FetchRule;
  }

  @Override
  public int hashCode() {
    return Objects.hash(filePattern, producer, schedule, protocol);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof FetchRule))
      return false;
    FetchRule other = (FetchRule) obj;
    return Objects.equals(filePattern, other.filePattern) && Objects.equals(producer, other.producer) && protocol == other.protocol
        && Objects.equals(schedule, other.schedule);
  }

  @Override
  public String getId() {
    return "[" + producer + "]" + filePattern + " (" + schedule + ")";
  }

  @Override
  public String toString() {
    return "FetchRule [producer=" + producer + ", filePattern=" + filePattern + ", schedule=" + schedule + ", endpoint=" + endpoint + "]";
  }

}
