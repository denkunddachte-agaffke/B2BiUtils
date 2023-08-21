package de.denkunddachte.b2biutil.loader.model;

import java.net.URI;
import java.util.Objects;
import java.util.stream.Stream;

import de.denkunddachte.enums.FTProtocol;

public class AWSS3Endpoint extends Endpoint {

  private String bucketName;
  private String accessKey;
  private String secretKey;
  private String iamUser;
  private String s3region;
  private URI    s3endpoint;

  public AWSS3Endpoint() {
    super(FTProtocol.AWSS3);
  }

  public AWSS3Endpoint(String bucketName, String accessKey, String secretKey) {
    super(FTProtocol.AWSS3);
    this.bucketName = bucketName;
    this.accessKey = accessKey;
    this.secretKey = secretKey;
  }

  public String getIamUser() {
    return iamUser;
  }

  public void setIamUser(String iamUser) {
    this.iamUser = iamUser;
  }

  public String getS3region() {
    return s3region;
  }

  public void setS3region(String s3region) {
    this.s3region = s3region;
  }

  public URI getS3endpoint() {
    return s3endpoint;
  }

  public void setS3endpoint(URI s3endpoint) {
    this.s3endpoint = s3endpoint;
  }

  public String getBucketName() {
    return bucketName;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  @Override
  public boolean isValid(boolean full) {
    if (full) {
      return Stream.of(bucketName, accessKey, secretKey).allMatch(Objects::nonNull);
    } else {
      return Stream.of(bucketName).allMatch(Objects::nonNull);
    }
  }

  @Override
  public String toString() {
    return "AWSS3Endpoint [bucketName=" + bucketName + ", accessKey=" + accessKey + ", secretKey=" + (secretKey == null ? "no" : "yes") + ", iamUser=" + iamUser
        + ", s3region=" + s3region + ", s3endpoint=" + s3endpoint + "]";
  }

}
