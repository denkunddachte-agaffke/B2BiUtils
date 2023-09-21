package de.denkunddachte.b2biutil.loader.model;

import java.util.Objects;
import java.util.stream.Stream;

import de.denkunddachte.b2biutil.loader.LoaderResult.Artifact;

public class Partner extends AbstractLoadRecord {

  public enum Type {
    INTERNAL, EXTERNAL
  }

  private String  partnerId;
  private Type    type;
  private String  customerName;
  private String  street;
  private String  postcode;
  private String  city;
  private String  country;
  private String  contact;
  private String  phone;
  private String  eMail;
  private boolean isProducer;
  private boolean isConsumer;
  private String  aglTenant;
  private String  comment;
  private Endpoint endpoint;

  public Partner(LoadAction action, int line, String partnerId, Type type, String customerName) {
    super(action, line);
    this.partnerId = partnerId;
    this.type = type;
    this.customerName = customerName;
    this.isConsumer = true;
    this.isProducer = true;
    this.endpoint = new MboxEndpoint();
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getPostcode() {
    return postcode;
  }

  public void setPostcode(String postcode) {
    this.postcode = postcode;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getContact() {
    return contact;
  }

  public void setContact(String contact) {
    this.contact = contact;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String geteMail() {
    return eMail;
  }

  public void seteMail(String eMail) {
    this.eMail = eMail;
  }

  public boolean isProducer() {
    return isProducer;
  }

  public void setProducer(boolean isProducer) {
    this.isProducer = isProducer;
  }

  public boolean isConsumer() {
    return isConsumer;
  }

  public void setConsumer(boolean isConsumer) {
    this.isConsumer = isConsumer;
  }

  public String getAglTenant() {
    return aglTenant;
  }

  public void setAglTenant(String aglTenant) {
    this.aglTenant = aglTenant;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getPartnerId() {
    return partnerId;
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
      return Stream.of(action, partnerId, type, customerName, endpoint).allMatch(Objects::nonNull);
    } else {
      return Stream.of(action, partnerId).allMatch(Objects::nonNull);
    }
  }

  @Override
  public Artifact getArtifact() {
    return Artifact.FGPartner;
  }

  @Override
  public int hashCode() {
    return Objects.hash(partnerId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof Partner))
      return false;
    Partner other = (Partner) obj;
    return Objects.equals(partnerId, other.partnerId);
  }

  @Override
  public String getId() {
    return partnerId;
  }

  @Override
  public String toString() {
    return "Partner [id=" + partnerId + ", type=" + type + ", customerName=" + customerName + ", isProducer=" + isProducer + ", isConsumer="
        + isConsumer + ", endpoint=" + endpoint + "]";
  }
}
