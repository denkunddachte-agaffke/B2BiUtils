package de.denkunddachte.b2biutil.loader.model;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.denkunddachte.b2biutil.loader.LoaderResult.Artifact;
import de.denkunddachte.enums.OSType;

public class TransferRule extends AbstractLoadRecord {
  private String                  producer;
  private String                  filePattern;
  private boolean                 datauAck;
  private String                  comment;
  private String                  additionalInfo;
  private OSType                  producerOs;
  private final Set<DeliveryRule> deliveryRules = new LinkedHashSet<>();

  public TransferRule(LoadAction action, int line, String producer, String rcvPattern) {
    super(action, line);
    this.producer = producer;
    this.filePattern = rcvPattern;
  }

  public boolean isDatauAck() {
    return datauAck;
  }

  public void setDatauAck(boolean datauAck) {
    this.datauAck = datauAck;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getAdditionalInfo() {
    return additionalInfo;
  }

  public void setAdditionalInfo(String additionalInfo) {
    this.additionalInfo = additionalInfo;
  }

  public OSType getProducerOs() {
    return producerOs;
  }

  public void setProducerOs(OSType producerOs) {
    this.producerOs = producerOs;
  }

  public String getProducer() {
    return producer;
  }

  public String getFilePattern() {
    return filePattern;
  }

  public boolean addDeliveryRule(DeliveryRule rule) {
    return deliveryRules.add(rule);
  }

  public Set<DeliveryRule> getActiveDeliveryRules() {
    Set<DeliveryRule> result = new LinkedHashSet<>(deliveryRules.size());
    result.addAll(deliveryRules.stream().filter(e -> LoadAction.isActive(e.getLoadAction())).collect(Collectors.toSet()));
    return result;
  }

  public Set<DeliveryRule> getDeliveryRules() {
    Set<DeliveryRule> result = new LinkedHashSet<>(deliveryRules.size());
    result.addAll(deliveryRules);
    return result;
  }

  @Override
  public boolean isValid(boolean full) {
    return Stream.of(action, producer, filePattern).allMatch(Objects::nonNull) && !deliveryRules.isEmpty();
  }

  @Override
  public Artifact getArtifact() {
    return Artifact.RoutingRule;
  }

  @Override
  public int hashCode() {
    return Objects.hash(filePattern, producer);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof TransferRule))
      return false;
    TransferRule other = (TransferRule) obj;
    return Objects.equals(filePattern, other.filePattern) && Objects.equals(producer, other.producer);
  }

  @Override
  public String toString() {
    return "TransferRule [producer=" + producer + ", filePattern=" + filePattern + ", deliveryRules=" + deliveryRules.size() + "]";
  }

  @Override
  public String getId() {
    return "[" + producer + "]" + filePattern;
  }
}
