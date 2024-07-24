package de.denkunddachte.sfgapi;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.denkunddachte.exception.ApiException;
import de.denkunddachte.util.ApiConfig;

public abstract class Validator {
  private static final Logger  LOGGER                  = Logger.getLogger(Validator.class.getName());

  private static final Pattern EMAIL_PATTERN           = Pattern.compile("[a-zA-Z0-9]+([._-][0-9a-zA-Z]+)*@[a-zA-Z0-9]+([.-][0-9a-zA-Z]+)*\\.[a-zA-Z]{2,}");
  private static final Pattern API000175_INVALID_CHARS = Pattern.compile("[!@#%^*()+?,<>{}\\[\\]|;\"'/\\\\.]");
  private static ApiConfig     apicfg;

  static {
    try {
      apicfg = ApiConfig.getInstance();
    } catch (ApiException e) {
      e.printStackTrace();
    }
  }

  private Validator() {
  }

  public static String validateEmail(String email) throws ApiException {
    if (email != null && !email.trim().isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
      final String template = apicfg.getAutofixEmailTemplate();
      if (apicfg.isAutofix()) {
        LOGGER.log(Level.WARNING, "AUTOFIX: replace invalid email address {0} with {1}.", new Object[] { email, template });
        return template;
      }
      throw new ApiException("Invalid email address: " + email + "!");
    }
    return email;
  }

  public static String validateName(String str) throws ApiException {
    if (str == null || str.trim().isEmpty())
      return str;

    Matcher      m        = API000175_INVALID_CHARS.matcher(str);
    StringBuffer sb       = new StringBuffer();
    boolean      modified = false;
    while (m.find()) {
      if (!apicfg.isAutofix()) {
        throw new ApiException("Name string \"" + str + "\" contain invalid characters " + API000175_INVALID_CHARS.pattern() + "!");
      }
      m.appendReplacement(sb, " ");
      modified = true;
    }

    if (modified) {
      m.appendTail(sb);
      LOGGER.log(Level.WARNING, "AUTOFIX: replace invalid name string \"{0}\" with \"{1}\".", new Object[] { str, sb });
      return sb.toString();
    } else {
      return str;
    }
  }

}
