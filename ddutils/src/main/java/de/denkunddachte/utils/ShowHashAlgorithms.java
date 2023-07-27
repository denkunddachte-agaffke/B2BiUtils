/*
  Copyright 2018 denk & dachte Software GmbH

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

import java.security.MessageDigest;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ShowHashAlgorithms {

  private static final void showHashAlgorithms(Provider prov, Class<?> typeClass) {
    String type = typeClass.getSimpleName();

    List<Service> algos = new ArrayList<>();

    Set<Service> services = prov.getServices();
    for (Service service : services) {
      if (service.getType().equalsIgnoreCase(type)) {
        algos.add(service);
      }
    }

    if (!algos.isEmpty()) {
      System.out.printf(" --- Provider %s, version %.2f --- %n", prov.getName(), prov.getVersion());
      for (Service service : algos) {
        String algo = service.getAlgorithm();
        System.out.printf("Algorithm name: \"%s\"%n", algo);

      }
    }

    // --- find aliases (inefficiently)
    Set<Object> keys = prov.keySet();
    for (Object key : keys) {
      final String prefix = "Alg.Alias." + type + ".";
      if (key.toString().startsWith(prefix)) {
        String value = prov.get(key.toString()).toString();
        System.out.printf("Alias: \"%s\" -> \"%s\"%n", key.toString().substring(prefix.length()), value);
      }
    }
  }

  public static void main(String[] args) {
    Provider[] providers = Security.getProviders();
    for (Provider provider : providers) {
      showHashAlgorithms(provider, MessageDigest.class);
    }
  }
}
