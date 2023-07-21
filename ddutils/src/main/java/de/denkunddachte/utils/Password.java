/*
  Copyright 2022 denk & dachte Software GmbH

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
package de.denkunddachte.utils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Password {
  public static final byte                 VERSION            = 0x01;
  public static final String               MASTER_PW_PROPERTY = "ddutil.passphrase";
  public static final String               MASTER_PW_ENV      = "DDUTIL_PASSPHRASE";
  public static final String               KEY_ALGORITHM      = "PBKDF2WithHmacSHA512";
  public static final String               KEY_ENC_ALGORITHM  = "AES";
  public static final String               CIPHER             = "AES/CBC/PKCS5Padding";
  public static final String               PREFIX             = "{PBKDF2}";
  public static final Pattern              B64_PATTERN        = Pattern.compile("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$");
  private static final Map<String, byte[]> LEGACY_AES_KEYS    = new HashMap<>();
  public static final int                  ITERATION_COUNT    = 40000;
  public static final short                KEY_LENGTH         = 256;
  private static byte                      masterPasswordSrc;
  private static char[]                    masterPassword;
  static {
    String mpw = System.getProperty(MASTER_PW_PROPERTY, System.getenv(MASTER_PW_ENV));
    if (mpw == null) {
      masterPasswordSrc = 0x00;
      masterPassword = "mv%v^1v24eVJ".toCharArray();
    } else {
      masterPassword = mpw.toCharArray();
      masterPasswordSrc = 0x01;
    }
    LEGACY_AES_KEYS.put("DEFAULT", new byte[] { 7, -60, -48, 106, 20, 25, -105, 46, 1, 106, -65, -40, 93, 69, -121, 55 });
    if (System.getProperty("_AES_KEY_") != null) {
      LEGACY_AES_KEYS.put("DEFAULT", Base64.getDecoder().decode(System.getProperty("_AES_KEY_")));
    } 
    LEGACY_AES_KEYS.put("AES", LEGACY_AES_KEYS.get("DEFAULT"));
  }

  private Password() {
  }

  public static String getCleartext(String password) throws CryptException {
    if (password.startsWith(PREFIX)) {
      return decrypt(password);
    }
    if (password.indexOf('{') == 0 && password.indexOf('}') > 3) {
      String aesKeyId = password.substring(1, password.indexOf('}'));
      if (!LEGACY_AES_KEYS.containsKey(aesKeyId)) {
        throw new CryptException("No legacy AES key \"" + aesKeyId + "\"!");
      }
      return decryptBase64String(LEGACY_AES_KEYS.get(aesKeyId), password.substring(password.indexOf('}') + 1), StandardCharsets.US_ASCII);
    }
    if (B64_PATTERN.matcher(password).matches()) {
      try {
        return decryptBase64String(LEGACY_AES_KEYS.get("DEFAULT"), password, StandardCharsets.US_ASCII);
      } catch (CryptException e) {
        // password was not encrypted.
        return password;
      }
    }
    return password;
  }

  public static String getEncrypted(String password) throws CryptException {
    if (password.startsWith(PREFIX) || (password.indexOf('{') == 0 && password.indexOf('}') > 3)) {
      return password;
    }
    return encrypt(password);
  }

  public static String encrypt(String password) throws CryptException {
    try {
      Cipher cipher = Cipher.getInstance(CIPHER);
      int saltlen = ThreadLocalRandom.current().nextInt(6, 12 + 1);
      byte[] salt = new byte[saltlen];
      SecureRandom.getInstanceStrong().nextBytes(salt);
      cipher.init(Cipher.ENCRYPT_MODE, createSecretKey(ITERATION_COUNT, KEY_LENGTH, salt));
      AlgorithmParameters parameters = cipher.getParameters();
      IvParameterSpec ivParameterSpec = parameters.getParameterSpec(IvParameterSpec.class);
      byte[] crypted = cipher.doFinal(password.getBytes("UTF-8"));
      byte[] iv = ivParameterSpec.getIV();
      ByteBuffer b = ByteBuffer.allocate(14 + saltlen + iv.length + crypted.length);
      b.put(VERSION).put(masterPasswordSrc).putShort((short) saltlen).put(salt).putInt(ITERATION_COUNT).putShort(KEY_LENGTH).putShort((short) iv.length).put(iv)
          .putShort((short) crypted.length).put(crypted);
      return PREFIX + Base64.getEncoder().encodeToString(b.array());
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidKeySpecException | InvalidParameterSpecException
        | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
      throw new CryptException(e);
    }
  }

  public static String decrypt(String b64EncodedPassword) throws CryptException {
    ByteBuffer buf = ByteBuffer.wrap(Base64.getDecoder().decode(b64EncodedPassword.replace(PREFIX, "")));
    byte b;
    if ((b = buf.get()) != VERSION) {
      throw new CryptException(String.format("String was encrypted with version %x, this class supports vesrion %x!", b, VERSION));
    }
    if (buf.get() == 0x01) {
      throw new CryptException(
          String.format("Master password required! Set with property -D%s=password or set environment %s.", MASTER_PW_PROPERTY, MASTER_PW_ENV));
    }

    byte[] salt = new byte[buf.getShort()];
    buf.get(salt);
    SecretKeySpec key;
    try {
      key = createSecretKey(buf.getInt(), buf.getShort(), salt);
      byte[] iv = new byte[buf.getShort()];
      buf.get(iv);
      byte[] crypted = new byte[buf.getShort()];
      buf.get(crypted);

      Cipher pbeCipher = Cipher.getInstance(CIPHER);
      pbeCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
      return new String(pbeCipher.doFinal(crypted));
    } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException
        | IllegalBlockSizeException | BadPaddingException e) {
      throw new CryptException(e);
    }

  }

  private static SecretKeySpec createSecretKey(int iterationCount, short keyLength, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
    PBEKeySpec keySpec = new PBEKeySpec(masterPassword, salt, iterationCount, keyLength);
    SecretKey keyTmp = keyFactory.generateSecret(keySpec);
    return new SecretKeySpec(keyTmp.getEncoded(), KEY_ENC_ALGORITHM);
  }

  public static String decryptBase64String(byte[] key, String base64String, Charset charset) throws CryptException {
    return new String(decryptAES(key, Base64.getDecoder().decode(base64String)), charset);
  }

  public static byte[] decryptAES(byte[] key, byte[] encrypted) throws CryptException {
    if (key.length != 16) {
      throw new IllegalArgumentException("Invalid key size.");
    }
    SecretKeySpec skeySpec = new SecretKeySpec(key, KEY_ENC_ALGORITHM);
    Cipher cipher;
    try {
      cipher = Cipher.getInstance(CIPHER);
      cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(new byte[16]));
      return cipher.doFinal(encrypted);
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException
        | BadPaddingException e) {
      throw new CryptException(e);
    }
  }

  public static void main(String[] args) throws CryptException {
    if (args.length > 2 || (args.length == 1 && args[0].contains("help"))) {
      System.err.println("usage: Password <string>");
      System.err.println();
      System.err.println("  Encrypt passwords used in tools.");
      System.exit(1);
    }
    if (args.length == 2 && "-d".equals(args[0])) {
      System.out.println(Password.getCleartext(args[1]));
      System.exit(0);
    }

    String password = null;
    if (args.length == 1) {
      password = args[0];
    } else if (System.console() != null) {
      char[] c = System.console().readPassword("Enter password: ");
      if (c != null) {
        password = new String(c);
      }
    } else {
      System.err.println("Console not available! Provide password as argument.");
      System.exit(1);
    }
    if (password == null || password.trim().isEmpty()) {
      System.err.println("Password is empty!");
      System.exit(1);
    }
    System.out.println(Password.getEncrypted(password));
  }

  public static class CryptException extends Exception {
    private static final long serialVersionUID = -7598605719217129232L;

    public CryptException(String msg) {
      super(msg);
    }

    public CryptException(String msg, Throwable e) {
      super(msg, e);
    }

    public CryptException(Throwable e) {
      super(e);
    }
  }
}
