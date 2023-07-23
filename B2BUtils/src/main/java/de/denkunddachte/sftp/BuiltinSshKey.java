package de.denkunddachte.sftp;

import com.jcraft.jsch.Identity;
import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

import de.denkunddachte.utils.Password;
import de.denkunddachte.utils.Password.CryptException;

final class BuiltinSshKey implements Identity {

  // @formatter:off
  private static final String KEY_NAME="sftpclient-builtin";
  private static final String KEY_TYPE_NAME = "ssh-rsa";
  private static final String PRIVATEKEY="-----BEGIN RSA PRIVATE KEY-----\n"
      + "Proc-Type: 4,ENCRYPTED\n"
      + "DEK-Info: DES-EDE3-CBC,BC101F4CCB2FD1AF\n"
      + "\n"
      + "SQ9j+1N9EZv1Ov3nECJkxb9TtiIL8/t6D8yBu/roQOHTrgAY3p8OQw9BxPPDWA09\n"
      + "4eiUY7ArASCHXhZrC0/8BwDmK1RHvwG+ot9SzYACskem9PAec70oX14TUvYTYSeB\n"
      + "FPPsb0e7FqZ1P3PnUa+m35gJ992kxh4JPk+pNNNBdZ7GwYvX5pqUSpoft++ld702\n"
      + "atzSwvOLe5NUQ+ySn9Qxuaygsb4LJ+cy4htfiNPZ+3H0Q2mu9ZRUUE3f5eWcC85N\n"
      + "2/o+fWCxGtMrGx4VBkjXL9/TsDW0yBYY1rnkJebUGg4isBwwOnqdySYAWrQgiDr8\n"
      + "XXjiEQqdwcpGjzEsk/aBvAAc3wcM4RWpLBwOvO7lf8/usTJKc1MJfPBA7I8ug8On\n"
      + "vWEepV35w+5hwRLdSOERox/S/Cb1D9S7/VYiAQ+VmOmflJOOdj4N7QCo0hes1mlu\n"
      + "h3moc3kclt9bLm7aN4EIvlylXCRLKTBoz+a9zoilZN7Dy2TsEgOuU4DAwj6QHAMh\n"
      + "9XGMVfdfjONqGOiSrO/23nBq01we9WJvadWwqf0p08mqGf/FSBgx0neLQwptB1QF\n"
      + "ORy7hbYpl18Ry4xgWw/pzEuN8YRMAGgjukrsY/50+ClhX1/AuhzIVCsE2+w+OrAA\n"
      + "Tiyr0l4y7iCzd4TpzDvfFPtC8uutDjhAUdPdq5fvEa7j7u7/mGBzx6saZO6harmK\n"
      + "cC24cFnoDTPNA9EOWdegYW3G99o04iLdXZnKOdD+6ZhQdPRz4KPwTN3SlAxNKn/2\n"
      + "/YBJmrW1D8U7ZFhQW11o6JjOClmXRvXLlbjKjWSC8EivQs9dyLFi4kLX95ehCART\n"
      + "6IBamS4pkAkV1wXaMBGwaGCa1UUus39Qbbl/UCGCuUg2IAATUuv9VkH58kyW7CC8\n"
      + "XWC+1Plz4av9C4X+QbEnZNoUWz7TAUgs3e3rO+nUVSMp/Dj8talVcBZBJu4MbNzq\n"
      + "HowWgdobcQFjHTIGa5S/hVBGnYB+Z7/lcJYGaw+WTYa7LfvrLDuXOh9jNHlwu7sV\n"
      + "ol3J8eQvkBWz6AfYHvHK2YIzx0c0VAZu8W1soRHcx0wuv031F4Ud+Dd/A/SkcMK1\n"
      + "RmrKvWyRITYR1n8eLz3iK3EU22Da5efRC+8dYukWDWlGz3TtgybewLCknDo9iK4a\n"
      + "iZuOYhP85odlRb8sJKgtER2NF8UKkrYalvHDrU08tBxh2sia1+8QzO64MRFazF0o\n"
      + "6KxHmQLotbvGWtK7NSuXjJ5SQ7dO10MGV+/oF6x0cXXHn5GAGQwaOc/zM1z/RFM+\n"
      + "77hzY8Q5J6cS4rM5IG5S0cpjKVaehjfhYb5e130pmpRAEc7TONnwXy7JejLFOz0l\n"
      + "VwZ3nQsBSW0PoyyZ5AEpuRVSFHqZcBBn6l+dgQvxstEKZgVrHo6FrytvCYRTKiki\n"
      + "avWYWzRRXpVXq8gsQa4z1QOFm2Vk6Bxput9Qp7vZkRv/FzNppZ2Jkr6ZhAx1cEcM\n"
      + "vP8GtPNKUQjSqWxCPOP3CF90mHjhX7wv5/thqHv4VDgsLK3GANJF6QXWP8u5uG8S\n"
      + "H0V2i5YfvhDaKjASGtnKM90oe34jJBATyIyuS7KcOUPQuRrUr7qWfNn6iJhrbjMZ\n"
      + "ayTF6XyKUEeAFHjJwEcK0pCgSw46V3JbnIcFJzYq9F9hBBsXK+Bt35ogjq4scyWw\n"
      + "306FCPQBYjJ7UJ7VVxnfbpNBx3Gorrf/yXRtKYXmNFU52K2QoXu4zmhShiqgfZ4F\n"
      + "wbciYLbCl3Mos8QkT/KrYgb6u12tbdiY9TkweRVwybfucuNdlIsXBjiq+aAY4zhH\n"
      + "BeGmfClzQOoovLNAR9KT2bDRY+DcMscP+jfqIhAMP0Oi3Ow1M36rgth7OJI+wzGK\n"
      + "xYvLI1MFN9D8cnImqHQP6VutdGCE5O0sMc4Y4dIEeD1bnnlSKNs/QAcY+cSFxneJ\n"
      + "JBFEXW9lxcgRnj1rL4d+u1wV9tLS2Yoznloa+wdUhzI/6nhAEbTV1YSjUtkD4RHD\n"
      + "gX0UmFvE+lacu7Tp6h9qrcFTQpPNVq3W3uo9Tva+qqAil8VOAbnqBNXM1G+r2QSa\n"
      + "ANel3qv4ISaZosDonJyUkFzTmsz/56R0IvgyZYA3MSYLiOUwBeFR0TWdeNbgl+UT\n"
      + "F+XZqambtLfDz8kTkX40MsfHj0he/SDArJTzIRSfG115q6xjmgwvMIJr7bLQJlKp\n"
      + "CvELjAfbkIze+wyO4Fq1WzixiqgKKASgzIZbtCDkj/F6SVmAimRRVl6ahvRsfzxv\n"
      + "PREbl4XuT9jD9GsIVKDBQVXUejeIOCKOohfspx0ZklxUoO91jFn3qjGiNx3PFrH2\n"
      + "rFzgfmQv98/gJxd5s/FLAsXZ6iViwf+VWbYuPDarPcToqRzWuhQOfJ+CE6WoHUqs\n"
      + "rnsxI+hLmldX4rK45F0DzPoP3UAVWKqW4vyiRNJ55SVEg6q6LiTrj46WuwhP9T4S\n"
      + "zQOZnGqoPZan6EvWeQVEgMETA/gBejyHjuQ8swhN1DNL98eJuyK6T6ciO7ubbk0e\n"
      + "CZdVvyMWRHe9xq8L6TSYriNIktahxqWxGIrzZzsSPCJeySmgye98nkNL22Pvu1L4\n"
      + "jVDWnlXmcNurnBereB1vSFZU90jBciNHaWU1w8dxVrKZxWaNhjROMKDyNy1bbNv9\n"
      + "pJY8GsT5voC+3YRdfZMLpiiS8u7wlqhUhIgodGwVintG2B/RV1kMfTvnEByNPK0c\n"
      + "9nNcMD0gL/m9ePUrliGDwNf3qp8jit+J/sVkg5LH3qvnvXUA0LJqnyPjpolr5j+e\n"
      + "kBSqwmHk7TN8Dwd7pB8wxJJ8w5GpOAZ5xYRkq608REwfEXc1Z7QrcH8VJVtLd4po\n"
      + "3BVUloHJMXycNSCVWz1iZo4ztg2Pzh6GlcKnjNhJX0P1y20dFUMIAqW9QU4od7H0\n"
      + "DowLrv9GzicUnLQTEHCPNDobv9BBB1/VrLu4yWlLODbrowYgkjrbFNf+4Pd2vzZk\n"
      + "aKMno/uK6Q6BwIntnzrNvjj/GP8zRGPJtDLm1XjCeydVoWfxMzdb94M8s6YHQE8S\n"
      + "++kLLcuPltCWiHrPLfxP/g2bq0gelWoaPZQt+v6qzPwb8HYhYFmWR+mmfozknoAD\n"
      + "7dTPqV0wmKI2cOXombbIeH6MB3EgDmkqS/UGa+jbnM/1pRrfxeA0DwKJjhLeyDra\n"
      + "-----END RSA PRIVATE KEY-----\n";

  private static final String PASSPHRASE_ENCYRPTED = "{PBKDF2}AQAAC3yprgPlU650FFwnAACcQAEAABAUCI6WwSgmkfLQqOAlT2PzABAKqdtHT8MDFnsbcNau3ZaN";
  private static final String PUBLICKEY=KEY_TYPE_NAME + " AAAAB3NzaC1yc2EAAAABJQAAAgEAgsyuOt+HOqCCa0qHkXD2h1KIsCMQGbBZy3Z/"
      + "+Wd7pt1UsKO5qXXsZTM5tRDPGyn3brDON8Y4f6uPUyQUSZwEIEAOulrtfXq/oJRYHJwPxBmuh+xbTnReGa5WlzVd7JgX3T5OdQHud6lF"
      + "WVi3tWr9OU7mrR7hESu8L5iR3fbImdVwFId6kv4XlYnXFpUMUVomZlNUAVsQ0qAcGtmyPFtl7OC6Mb3iwKjFkbt8Br28RfPVfvIpUv3W"
      + "mquSVgsHb5jcXXioZQEH7ZcWUN9lt1unZiES7GCxj8uhZhWecC6jJm7dfmgnGMuhmHUcy2h2dx7fmcP/3PKALWYsClvhcK2j49WkSIAy"
      + "uHTRY6w0jS4tWyWCEtz5o/g/3Bv0V5NrCrDQ76CMxJ4de25rYTsXw4UMVl2WXP6ZQmFGR3b2nRBda+ZO+91lsCgUnpwxIOMhFihrW/nc"
      + "BOnjtDBFFtWMeKgUgJw1YuUX/z1MEXOq0fTk5HlVRpjjW/i7TPgRydF/+gyVLujenQxHgNAjT9D4dtAbQo5cseOzxuqrbx18sTuYs67M"
      + "FhbJgOK3trCh+aA0fB9k9Mf3wAiusbhwc0lErL7akIWb+gqTdVR7p5kOR1b3viiC3RmWaaSCpg6V8PEDWpq2Pa/8NIFb1f2oKSSSxPJC"
      + "A1cM78U2IJaJM/FsDRdnKmk= " + KEY_NAME;
  
  private static final String PUBLICKEY_SSH2="---- BEGIN SSH2 PUBLIC KEY ----\n"
      + "Comment: \"" + KEY_NAME + "\"\n"
      + "AAAAB3NzaC1yc2EAAAABJQAAAgEAgsyuOt+HOqCCa0qHkXD2h1KIsCMQGbBZy3Z/\n"
      + "+Wd7pt1UsKO5qXXsZTM5tRDPGyn3brDON8Y4f6uPUyQUSZwEIEAOulrtfXq/oJRY\n"
      + "HJwPxBmuh+xbTnReGa5WlzVd7JgX3T5OdQHud6lFWVi3tWr9OU7mrR7hESu8L5iR\n"
      + "3fbImdVwFId6kv4XlYnXFpUMUVomZlNUAVsQ0qAcGtmyPFtl7OC6Mb3iwKjFkbt8\n"
      + "Br28RfPVfvIpUv3WmquSVgsHb5jcXXioZQEH7ZcWUN9lt1unZiES7GCxj8uhZhWe\n"
      + "cC6jJm7dfmgnGMuhmHUcy2h2dx7fmcP/3PKALWYsClvhcK2j49WkSIAyuHTRY6w0\n"
      + "jS4tWyWCEtz5o/g/3Bv0V5NrCrDQ76CMxJ4de25rYTsXw4UMVl2WXP6ZQmFGR3b2\n"
      + "nRBda+ZO+91lsCgUnpwxIOMhFihrW/ncBOnjtDBFFtWMeKgUgJw1YuUX/z1MEXOq\n"
      + "0fTk5HlVRpjjW/i7TPgRydF/+gyVLujenQxHgNAjT9D4dtAbQo5cseOzxuqrbx18\n"
      + "sTuYs67MFhbJgOK3trCh+aA0fB9k9Mf3wAiusbhwc0lErL7akIWb+gqTdVR7p5kO\n"
      + "R1b3viiC3RmWaaSCpg6V8PEDWpq2Pa/8NIFb1f2oKSSSxPJCA1cM78U2IJaJM/Fs\n"
      + "DRdnKmk=\n"
      + "---- END SSH2 PUBLIC KEY ----";
    //@formatter:on

  private KeyPair             kpair;
  private String              identity;

  static BuiltinSshKey instance(JSch jsch) throws JSchException {
    return BuiltinSshKey.newInstance(KEY_NAME, PRIVATEKEY.getBytes(), PUBLICKEY.getBytes(), jsch);
  }

  private static BuiltinSshKey newInstance(String name, byte[] prvkey, byte[] pubkey, JSch jsch) throws JSchException {
    KeyPair kpair = KeyPair.load(jsch, prvkey, pubkey);
    return new BuiltinSshKey(name, kpair);
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
      return kpair.decrypt(Password.getCleartext(PASSPHRASE_ENCYRPTED));
    } catch (CryptException e) {
      throw new JSchException("Could not decrypt passphrase!", e);
    }
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
    return KEY_TYPE_NAME;
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

  public static String getSecShPublicKey() {
    return PUBLICKEY_SSH2;
  }

  public static String getOsshPublicKey() {
    return PUBLICKEY;
  }

  public static String getPrivateKey() {
    return PRIVATEKEY;
  }

  public static String getEncryptedPassphrase() {
    return PASSPHRASE_ENCYRPTED;
  }
}
