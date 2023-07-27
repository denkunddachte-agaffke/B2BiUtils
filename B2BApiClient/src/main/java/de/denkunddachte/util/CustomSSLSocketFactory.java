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
package de.denkunddachte.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.SecureRandom;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

public class CustomSSLSocketFactory extends SocketFactory {
  public static String MIN_TLS_VERSION = "TLSv1.2";
  private SSLSocketFactory sf;

  public CustomSSLSocketFactory() {
    try {
      SSLContext ctx = SSLContext.getInstance(MIN_TLS_VERSION);
      ctx.init(null, new X509TrustManager[] { new DummyX509Trustmanager() }, new SecureRandom());
      sf = ctx.getSocketFactory();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public static SocketFactory getDefault() {
    return new CustomSSLSocketFactory();
  }

  @Override
  public Socket createSocket(String host, int port) throws IOException {
    return sf.createSocket(host, port);
  }

  @Override
  public Socket createSocket(InetAddress host, int port) throws IOException {
    return sf.createSocket(host, port);
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
      throws IOException {
    return sf.createSocket(host, port, localHost, localPort);
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
    return sf.createSocket(address, port, localAddress, localPort);
  }

}
