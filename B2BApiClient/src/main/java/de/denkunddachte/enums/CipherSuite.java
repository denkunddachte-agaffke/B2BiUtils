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
package de.denkunddachte.enums;

// @formatter:off
public enum CipherSuite {
	DHE_DSS_WITH_DES_CBC_SHA("DHE_DSS_WITH_DES_CBC_SHA"),
	ECDHE_RSA_WITH_3DES_EDE_CBC_SHA("ECDHE_RSA_WITH_3DES_EDE_CBC_SHA"),
	ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA("ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA"),
	ECDHE_ECDSA_WITH_AES_128_CBC_SHA256("ECDHE_ECDSA_WITH_AES_128_CBC_SHA256"),
	RSA_WITH_AES_128_CBC_SHA("RSA_WITH_AES_128_CBC_SHA"),
	ECDHE_ECDSA_WITH_AES_128_CBC_SHA("ECDHE_ECDSA_WITH_AES_128_CBC_SHA"),
	ECDHE_RSA_WITH_AES_256_GCM_SHA384("ECDHE_RSA_WITH_AES_256_GCM_SHA384*"),
	ECDHE_RSA_WITH_AES_128_CBC_SHA256("ECDHE_RSA_WITH_AES_128_CBC_SHA256"),
	ECDHE_RSA_WITH_AES_256_CBC_SHA384("ECDHE_RSA_WITH_AES_256_CBC_SHA384*"),
	RSA_WITH_AES_256_GCM_SHA384("RSA_WITH_AES_256_GCM_SHA384*"),
	ECDHE_RSA_WITH_AES_128_GCM_SHA256("ECDHE_RSA_WITH_AES_128_GCM_SHA256"),
	DHE_RSA_WITH_DES_CBC_SHA("DHE_RSA_WITH_DES_CBC_SHA"),
	RSA_WITH_AES_256_CBC_SHA("RSA_WITH_AES_256_CBC_SHA"),
	RSA_WITH_AES_128_CBC_SHA256("RSA_WITH_AES_128_CBC_SHA256"),
	RSA_WITH_AES_128_GCM_SHA256("RSA_WITH_AES_128_GCM_SHA256"),
	ECDHE_RSA_WITH_AES_128_CBC_SHA("ECDHE_RSA_WITH_AES_128_CBC_SHA"),
	RSA_WITH_3DES_EDE_CBC_SHA("RSA_WITH_3DES_EDE_CBC_SHA"),
	ECDHE_RSA_WITH_AES_256_CBC_SHA("ECDHE_RSA_WITH_AES_256_CBC_SHA"),
	ECDHE_ECDSA_WITH_AES_256_CBC_SHA("ECDHE_ECDSA_WITH_AES_256_CBC_SHA"),
	DHE_RSA_WITH_3DES_EDE_CBC_SHA("DHE_RSA_WITH_3DES_EDE_CBC_SHA"),
	ECDHE_ECDSA_WITH_AES_128_GCM_SHA256("ECDHE_ECDSA_WITH_AES_128_GCM_SHA256"),
	RSA_WITH_DES_CBC_SHA("RSA_WITH_DES_CBC_SHA"),
	RSA_WITH_AES_256_CBC_SHA256("RSA_WITH_AES_256_CBC_SHA256"),
	ECDHE_ECDSA_WITH_AES_256_GCM_SHA384("ECDHE_ECDSA_WITH_AES_256_GCM_SHA384*"),
	ECDHE_ECDSA_WITH_AES_256_CBC_SHA384("ECDHE_ECDSA_WITH_AES_256_CBC_SHA384*");
//@formatter("formatter"):on("on")
	
	private String b2biCode;
  private CipherSuite(String code) {
    this.b2biCode = code;
  }
  
  public String b2biCode() {
    return this.b2biCode;
  }

  public String sspCode() {
    // SSP codes start with TLS_ and don't have * markers:
    return "TLS_" + this.name();
  }

  public static CipherSuite byCode(String code) {
    for (CipherSuite c : CipherSuite.values()) {
      if (code.equals(c.b2biCode) || code.equals(c.name()) || code.equals("TLS_" + c.name())) {
        return c;
      }
    }
    
    throw new IllegalArgumentException("No CipherSuite for code=" + code + "!");
  }
}
