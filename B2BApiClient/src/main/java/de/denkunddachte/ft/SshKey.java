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
package de.denkunddachte.ft;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// requires JDK >= 15
//import java.security.interfaces.EdECPublicKey;

public class SshKey {
  private static final int     VALUE_LENGTH     = 4;
  private static final Pattern SSH_KEY_PATTERN  = Pattern.compile("(ssh-rsa|ssh-dss|ecdsa-sha2-\\S+|ssh-ed\\d+|rsa-sha2-\\d+)\\s+([A-Za-z0-9/+]+=*)\\s*(.*)[\\r\\n]*");
  private static final byte[]  ED25519_ASN1_HDR = new byte[] { 0x30, 0x2a, 0x30, 0x05, 0x06, 0x03, 0x2b, 0x65, 0x70, 0x03, 0x21, 0x00 };
  private String               keyType;
  private String               keyData;
  private String               keyComment;
  private String               keyId;
  private PublicKey            pubKey;

  public SshKey(File keyFile) throws InvalidKeyException {
    char[] keyData = new char[(int) keyFile.length()];
    try (Reader rd = new FileReader(keyFile)) {
      rd.read(keyData);
    } catch (IOException e) {
      throw new InvalidKeyException("Could not read key file " + keyFile + "!", e);
    }
    String keyString = new String(keyData);
    if (keyString.startsWith("ssh") || keyString.startsWith("ecdsa")) {
      readSshKey(keyString.substring(0, (keyString.indexOf('\n') > 6 ? keyString.indexOf('\n') : keyString.length())));
    } else {
      readSshKey(keyString);
    }
  }

  public SshKey(String keyString) throws InvalidKeyException {
    readSshKey(keyString);
  }

  public SshKey(String type, String keyData) {
    this(type, keyData, null);
  }

  public SshKey(String type, String keyData, String keyComment) {
    this.keyType = type;
    this.keyData = keyData;
    this.keyComment = keyComment;
  }

  protected void readSshKey(String sshKey) throws InvalidKeyException {
    String encodedKeyData = null;
    if (sshKey.matches("(?si)\\s*----+\\s*BEGIN .*?\\s*PUBLIC KEY.*")) {
      try (BufferedReader br = new BufferedReader(new StringReader(sshKey))) {
        String line;
        StringBuilder sb = new StringBuilder(1000);
        boolean hasContinuation = false;
        while ((line = br.readLine()) != null) {
          line = line.trim();
          if (hasContinuation) {
            hasContinuation = false;
            continue;
          }
          if (line.endsWith("\\"))
            hasContinuation = true;

          if (line.isEmpty() || line.matches("(?i)----+\\s*BEGIN .+")) {
          } else if (line.startsWith("--")) {
            break;
          } else if (line.contains(":")) {
            if (line.startsWith("Comment:") && line.length() > 9) {
              this.keyComment = line.substring(line.indexOf(':') + 1).trim().replaceAll("^[\"']?(.+?)[\"']?$", "$1");
            }
            // ignore other headers
          } else {
            sb.append(line);
          }
        }
        encodedKeyData = sb.toString();
      } catch (IOException e) {
        throw new InvalidKeyException("Failed to read SSH2 public key.", e);
      }
    } else {
      Matcher matcher = SSH_KEY_PATTERN.matcher(sshKey);
      if (matcher.matches()) {
        this.keyType = matcher.group(1);
        encodedKeyData = matcher.group(2);
        this.keyComment = matcher.group(3);
      } else {
        encodedKeyData = sshKey;
      }
    }
    decodeKeyString(encodedKeyData);
  }

  protected void decodeKeyString(String keyData) throws InvalidKeyException {
    if (keyData == null || keyData.isEmpty()) {
      throw new InvalidKeyException("Key string null or empty!");
    }

    this.keyData = keyData;
    try {
      try (ByteArrayInputStream is = new ByteArrayInputStream(Base64.getDecoder().decode(keyData))) {
        this.keyType = getString(is);
        if ("ssh-rsa".equals(keyType)) {
          BigInteger e = getBigInt(is);
          BigInteger m = getBigInt(is);
          this.pubKey = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(m, e));
        } else if ("ssh-dss".equals(keyType)) {
          BigInteger p = getBigInt(is);
          BigInteger q = getBigInt(is);
          BigInteger g = getBigInt(is);
          BigInteger y = getBigInt(is);
          this.pubKey = KeyFactory.getInstance("DSA").generatePublic(new DSAPublicKeySpec(y, p, q, g));
        } else if (keyType.startsWith("ecdsa-sha2")) {
          ECParameterSpec ecparams = getECParameterSpec(getString(is));
          this.pubKey = KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(decodePoint(getArray(is), ecparams.getCurve()), ecparams));
        } else if (keyType.startsWith("ssh-ed25519")) {
          byte[] pk = getArray(is);
          byte[] asnEncoded = new byte[ED25519_ASN1_HDR.length + pk.length];
          System.arraycopy(ED25519_ASN1_HDR, 0, asnEncoded, 0, ED25519_ASN1_HDR.length);
          System.arraycopy(pk, 0, asnEncoded, ED25519_ASN1_HDR.length, pk.length);
          KeyFactory kf = KeyFactory.getInstance("Ed25519");
          EncodedKeySpec keySpec = new X509EncodedKeySpec(asnEncoded);
          this.pubKey = kf.generatePublic(keySpec);
          if (!this.pubKey.getAlgorithm().equals("EdDSA")) {
            throw new InvalidKeyException("Key type is " + this.pubKey.getAlgorithm() + ", should be EdDSA.");
          }
        } else {
          throw new InvalidKeyException("Key type " + keyType + " is not supported!");
        }
      }
    } catch (IllegalArgumentException | InvalidParameterSpecException iae) {
      throw new InvalidKeyException("Failed to decode SSH key string: " + keyData, iae);
    } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
      throw new InvalidKeyException("Failed to read SSH certificate from key.", e);
    }
  }

  private ECPoint decodePoint(byte[] data, EllipticCurve curve) throws IOException {
    if ((data.length == 0) || (data[0] != 4)) {
      throw new IOException("Only uncompressed point format supported");
    }
    int n = (curve.getField().getFieldSize() + 7) >> 3;
    if (data.length != (n * 2) + 1) {
      throw new IOException("Point does not match field size");
    }
    byte[] xb = new byte[n];
    byte[] yb = new byte[n];
    System.arraycopy(data, 1, xb, 0, n);
    System.arraycopy(data, n + 1, yb, 0, n);
    return new ECPoint(new BigInteger(1, xb), new BigInteger(1, yb));
  }

  private ECParameterSpec getECParameterSpec(String curve) throws NoSuchAlgorithmException, InvalidParameterSpecException {
    AlgorithmParameters params = AlgorithmParameters.getInstance("EC");
    String name = curve.replace("nist", "sec") + "r1";
    params.init(new ECGenParameterSpec(name));
    return params.getParameterSpec(ECParameterSpec.class);
  }

  private static int getFieldLength(InputStream is) throws IOException {
    byte[] lenBuff = new byte[VALUE_LENGTH];
    if (VALUE_LENGTH != is.read(lenBuff)) {
      throw new InvalidParameterException("Unable to read value length.");
    }
    return ByteBuffer.wrap(lenBuff).getInt();
  }

  private static BigInteger getBigInt(InputStream is) throws IOException {
    return new BigInteger(getArray(is));
  }

  private static String getString(InputStream is) throws IOException {
    return new String(getArray(is));
  }

  private static byte[] getArray(InputStream is) throws IOException {
    int len = getFieldLength(is);
    byte[] valueArray = new byte[len];
    if (len != is.read(valueArray)) {
      throw new InvalidParameterException("Unable to read value.");
    }
    return valueArray;
  }

  public String getType() {
    return keyType;
  }

  public String getKeyData() {
    return keyData;
  }

  public String getKeyString() {
    if (keyComment != null) {
      return keyType + " " + keyData + " " + keyComment;
    }
    return keyType + " " + keyData;
  }

  public String getBase64Encoded() {
    return Base64.getEncoder().encodeToString(getKeyString().getBytes(StandardCharsets.UTF_8));
  }

  public String getKeyComment() {
    return keyComment;
  }

  public String getKeyId() {
    return keyId;
  }

  public void setKeyId(String keyId) {
    this.keyId = keyId;
  }

  public String getKeyAlgorithm() {
    if (pubKey == null)
      return null;
    switch (pubKey.getAlgorithm()) {
    case "EdDSA":
      return "Ed25519";
    case "EC":
      return "ECDSA";
    default:
      return pubKey.getAlgorithm();
    }
  }

  public int getKeySize() {
    if (pubKey instanceof DSAPublicKey) {
      return ((DSAPublicKey) pubKey).getParams().getP().bitLength();
    } else if (pubKey instanceof RSAPublicKey) {
      return ((RSAPublicKey) pubKey).getModulus().bitLength();
    } else if (pubKey instanceof ECPublicKey) {
      return ((ECPublicKey) pubKey).getParams().getCurve().getField().getFieldSize();
    } else if ("Ed25519".equalsIgnoreCase(getKeyAlgorithm())) {
      // ED25519 keys are always 256 bits
      return 256;
    } else {
      return 0;
    }
  }

  public PublicKey getPublicKey() {
    return pubKey;
  }

  public String getDigest() {
    try {
      return getBase64KeyDigest("SHA-256");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public String getBase64KeyDigest(String hashAlg) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance(hashAlg);
    return Base64.getEncoder().encodeToString(digest.digest(Base64.getDecoder().decode(keyData)));
  }

  public String getMD5Digest() throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("MD5");
    return byteArrayToHex(digest.digest(Base64.getDecoder().decode(keyData)), ":");
  }

  public String getKeyDigestInfo(String hashAlg) {
    try {
      if (hashAlg.equals("MD5")) {
        return getKeySize() + " MD5:" + getMD5Digest() + (keyComment != null ? " " + keyComment : "") + " (" + getKeyAlgorithm() + ")";
      } else {
        return getKeySize() + " " + hashAlg + ":" + getBase64KeyDigest(hashAlg) + (keyComment != null ? " " + keyComment : "")
            + " (" + getKeyAlgorithm() + ")";
      }
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return null;
  }

  private String byteArrayToHex(byte[] in, String delimeter) {
    final int len = in.length;
    final StringBuilder out = new StringBuilder(len * 2);
    for (int i = 0; i < len; i++) {
      if (i > 0 && delimeter != null)
        out.append(delimeter);
      out.append(Character.forDigit((in[i] >> 4) & 0xF, 16)).append(Character.forDigit(in[i] & 0xF, 16));
    }
    return out.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((keyData == null) ? 0 : getDigest().hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SshKey other = (SshKey) obj;
    if (keyData == null) {
      if (other.keyData != null)
        return false;
    } else if (!getDigest().equals(other.getDigest()))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "SshKey [" + getKeyDigestInfo("SHA-256") + "]";
  }

}
