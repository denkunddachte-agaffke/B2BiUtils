package de.denkunddachte.b2biutil.api;

import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import de.denkunddachte.util.Password;
import de.denkunddachte.util.Password.CryptException;

public class PasswordUtil {

  public static void main(String[] args) throws CryptException {

    if (args.length > 2 || (args.length == 1 && args[0].contains("help"))) {
      System.err.println("usage: Password <string>");
      System.err.println();
      System.err.println("  Encrypt passwords used in tools.");
      System.exit(1);
    }
    String password = null;
    if (args.length == 2 && "-d".equals(args[0])) {
      System.out.println(Password.getCleartext(args[1]));
      System.exit(0);
    } else if (args.length == 1 && "list-ciphers".equals(args[0])) {
      PasswordUtil.listCiphers();
      System.exit(0);
    }

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

  public static void listCiphers() {
    for (Provider provider : Security.getProviders()) {
      System.out.format("%nProvider: %s:%n", provider.getName());
      TreeMap<String, List<String>> algs = new TreeMap<>();
      for (Service s: provider.getServices()) {
        if (algs.get(s.getType()) == null) {
          algs.put(s.getType(), new ArrayList<>());
        }
        algs.get(s.getType()).add(s.getAlgorithm());
      }
      
      for(Entry<String, List<String>> e : algs.entrySet()) {
        System.out.format("%n  %s:%n", e.getKey());
        for (String a : e.getValue()) {
          System.out.format("    %s%n", a);
        }
      }
    }
  }
}
