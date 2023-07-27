package de.denkunddachte.sftp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Properties;

import com.jcraft.jsch.Identity;
import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

import de.denkunddachte.utils.Password;
import de.denkunddachte.utils.Password.CryptException;

final class BuiltinSshKey implements Identity {

  private static BuiltinSshKey key;
  private KeyPair             kpair;
  private String              identity;
  private String passphrase;
  private String algName;

  static BuiltinSshKey instance(JSch jsch) throws JSchException {
    if (key == null) {
     key = newInstance(jsch);
    }
    return key;
  }

  private static BuiltinSshKey newInstance(JSch jsch) throws JSchException {
    Properties p = new Properties();
    BuiltinSshKey instance = null;
    try(InputStream is = BuiltinSshKey.class.getClassLoader().getResourceAsStream("de/denkunddachte/sftp/builtinkey.properties")) {
      p.load(is);
      Decoder b64 = Base64.getDecoder();
      KeyPair kpair = KeyPair.load(jsch, b64.decode(p.getProperty("private")), b64.decode(p.getProperty("public.ossh")));
      instance = new BuiltinSshKey(p.getProperty("name"), kpair);
      instance.setPassphrase(p.getProperty("passphrase"));
      instance.setAlgName(p.getProperty("type"));
    } catch (IOException e) {
      throw new JSchException("Could not read builtin key!", e);
    }
    return instance;
  }

  private BuiltinSshKey(String name, KeyPair kpair) throws JSchException {
    this.identity = name;
    this.kpair = kpair;
  }

  /**
   * Decrypts this identity with the specified pass-phrase.
   * @param passphrase the pass-phrase for this identity.
   * @return <tt>true</tt> if the decryption is succeeded
   * or this identity is not cyphered.
   */
  @Override
  public boolean setPassphrase(byte[] passphrase) throws JSchException {
    try {
      return kpair.decrypt(Password.getCleartext(this.passphrase));
    } catch (CryptException e) {
      throw new JSchException("Could not decrypt passphrase!", e);
    }
  }

  private void setPassphrase(String passphrase) {
    this.passphrase = passphrase;
  }

  /**
   * Returns the public-key blob.
   * @return the public-key blob
   */
  @Override
  public byte[] getPublicKeyBlob() {
    return kpair.getPublicKeyBlob();
  }

  /**
   * Signs on data with this identity, and returns the result.
   * @param data data to be signed
   * @return the signature
   */
  @Override
  public byte[] getSignature(byte[] data) {
    return kpair.getSignature(data);
  }

  /**
   * @deprecated This method should not be invoked.
   * @see #setPassphrase(byte[] passphrase)
   */
  @Deprecated
  @Override
  public boolean decrypt() {
    throw new RuntimeException("not implemented");
  }

  /**
   * Returns the name of the key algorithm.
   * @return "ssh-rsa" or "ssh-dss"
   */
  @Override
  public String getAlgName() {
    return algName;
  }

  private void setAlgName(String algName) {
    this.algName = algName;
  }
  
  /**
   * Returns the name of this identity. 
   * It will be useful to identify this object in the {@link IdentityRepository}.
   */
  @Override
  public String getName() {
    return identity;
  }

  /**
   * Returns <tt>true</tt> if this identity is cyphered.
   * @return <tt>true</tt> if this identity is cyphered.
   */
  @Override
  public boolean isEncrypted() {
    return kpair.isEncrypted();
  }

  /**
   * Disposes internally allocated data, like byte array for the private key.
   */
  @Override
  public void clear() {
    kpair.dispose();
    kpair = null;
  }

  /**
   * Returns an instance of {@link KeyPair} used in this {@link Identity}.
   * @return an instance of {@link KeyPair} used in this {@link Identity}.
   */
  public KeyPair getKeyPair() {
    return kpair;
  }

  private static String getKeyProperty(String property, boolean decode) {
    String result = null;
    Properties p = new Properties();
    try(InputStream is = BuiltinSshKey.class.getClassLoader().getResourceAsStream("de/denkunddachte/sftp/builtinkey.properties")) {
      p.load(is);
      if (decode) {
        result = new String(Base64.getDecoder().decode(p.getProperty(property)));
      } else {
        result = p.getProperty(property);
      }
    } catch (IOException e) {
      System.err.println("Could not read property " + property + ": " + e.getMessage());
    }
    return result;
  }

  public static String getSecShPublicKey() {
    return getKeyProperty("public.ssh2", true);
  }

  public static String getOsshPublicKey() {
    return getKeyProperty("public.ossh", true);
  }

  public static String getPrivateKey() {
    return getKeyProperty("private", true);
  }

  public static String getEncryptedPassphrase() {
    return getKeyProperty("passphrase", false);
  }
}
