/*
  Copyright 2016 denk & dachte Software GmbH

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

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class StringUtils {
  private static final byte[]       _KEY_                = new byte[] { 7, -60, -48, 106, 20, 25, -105, 46, 1, 106, -65, -40, 93, 69, -121, 55 };

  private static final Pattern      PATTERN_VAR_PROPERTY = Pattern.compile("(?<!\\\\)[$%]\\{\\s*(.+?)\\}");
  private static final Pattern      PATTERN_VAR_DATETIME = Pattern
      .compile("(?<!\\\\)[$%]\\{\\s*([GyYMwWDdFEuaHkKhmsSzZX]+?.*?)\\s*(?:(?!<\\\\)=([<]?)([+-]?\\d+)([HMSdmy]?))?\\}");
  public static final String        LF                   = System.getProperty("line.separator");

  private static final SecureRandom RND                  = new SecureRandom();

  public static boolean isNullOrEmpty(String value) {
    return value == null || value.isEmpty();
  }

  public static boolean isNullOrWhiteSpace(String value) {
    return isNullOrEmpty(value) || value.trim().isEmpty();
  }

  public static String truncateString(String str, int maxlength) {
    return truncateString(str, maxlength, null);
  }

  public static String truncateString(String str, int maxlength, String defaultValue) {
    String result = (str == null ? defaultValue : str);
    if (result.length() > maxlength) {
      return result.substring(0, maxlength - 1);
    } else {
      return result;
    }
  }

  @Deprecated
  public static String encryptToBase64(String plainText) throws GeneralSecurityException {
    return encryptToBase64(plainText, StandardCharsets.US_ASCII);
  }

  @Deprecated
  public static String encryptToBase64(String plainText, Charset charset) throws GeneralSecurityException {
    return encryptToBase64(plainText.getBytes(charset));
  }

  @Deprecated
  public static String encryptToBase64(byte[] plainText) throws GeneralSecurityException {
    return Base64.getEncoder().encodeToString(encryptAES(getAESKey(), plainText));
  }

  @Deprecated
  public static String decryptBase64String(String base64String) throws GeneralSecurityException {
    return decryptBase64String(base64String, StandardCharsets.US_ASCII);
  }

  @Deprecated
  public static String decryptBase64String(String base64String, Charset charset) throws GeneralSecurityException {
    return new String(decryptAES(getAESKey(), Base64.getDecoder().decode(base64String)), charset);
  }

  public static String decodeBase64ToString(String base64String) {
    return decodeBase64ToString(base64String, Charset.defaultCharset());
  }

  public static String decodeBase64ToString(String base64String, Charset charset) {
    return new String(Base64.getDecoder().decode(base64String), charset);
  }

  public static String encodeBase64(String string) {
    return encodeBase64(string, Charset.defaultCharset());
  }

  public static String encodeBase64(String string, Charset charset) {
    return Base64.getEncoder().encodeToString(string.getBytes(charset));
  }

  @Deprecated
  public static byte[] encryptAES(byte[] key, byte[] plainText) throws GeneralSecurityException {

    if (key.length != 16) {
      throw new IllegalArgumentException("Invalid key size.");
    }

    SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    byte[] bytesIV = new byte[16];
    RND.nextBytes(bytesIV);
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(bytesIV));
    return cipher.doFinal(plainText);
  }

  @Deprecated
  public static byte[] decryptAES(byte[] key, byte[] encrypted) throws GeneralSecurityException {

    if (key.length != 16) {
      throw new IllegalArgumentException("Invalid key size.");
    }
    SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    byte[] bytesIV = new byte[16];
    RND.nextBytes(bytesIV);
    cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(bytesIV));
    return cipher.doFinal(encrypted);
  }

  public static String byteArrayToHex(byte[] in) {
    return byteArrayToHex(in, null);
  }

  public static String byteArrayToHex(byte[] in, String delimeter) {
    final int len = in.length;
    final StringBuilder out = new StringBuilder(len * 2);
    for (int i = 0; i < len; i++) {
      if (i > 0 && delimeter != null)
        out.append(delimeter);
      out.append(Character.forDigit((in[i] >> 4) & 0xF, 16)).append(Character.forDigit(in[i] & 0xF, 16));
    }
    return out.toString();
  }

  public static byte[] hexToByteArray(String in) {
    final int len = in.length();
    final byte[] out = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      out[i / 2] = (byte) ((Character.digit(in.charAt(i), 16) << 4) + Character.digit(in.charAt(i + 1), 16));
    }
    return out;
  }

  public static String globToRegexp(String glob) {
    if (isNullOrWhiteSpace(glob)) {
      return null;
    }

    glob = glob.trim();
    StringBuilder sb = new StringBuilder(glob.length());
    boolean escaping = false;
    int inCurlies = 0;
    for (char currentChar : glob.toCharArray()) {
      switch (currentChar) {
      case '*':
        if (escaping)
          sb.append("\\*");
        else
          sb.append(".*");
        escaping = false;
        break;
      case '?':
        if (escaping)
          sb.append("\\?");
        else
          sb.append('.');
        escaping = false;
        break;
      case '.':
      case '(':
      case ')':
      case '+':
      case '|':
      case '^':
      case '$':
      case '@':
      case '%':
        sb.append('\\');
        sb.append(currentChar);
        escaping = false;
        break;
      case '\\':
        if (escaping) {
          sb.append("\\\\");
          escaping = false;
        } else
          escaping = true;
        break;
      case '{':
        if (escaping) {
          sb.append("\\{");
        } else {
          sb.append('(');
          inCurlies++;
        }
        escaping = false;
        break;
      case '}':
        if (inCurlies > 0 && !escaping) {
          sb.append(')');
          inCurlies--;
        } else if (escaping)
          sb.append("\\}");
        else
          sb.append("}");
        escaping = false;
        break;
      case ',':
        if (inCurlies > 0 && !escaping) {
          sb.append('|');
        } else if (escaping)
          sb.append("\\,");
        else
          sb.append(",");
        break;
      default:
        escaping = false;
        sb.append(currentChar);
      }
    }
    return sb.toString();
  }

  public static String expandVariables(String in) {
    if (isNullOrWhiteSpace(in)) {
      return in;
    }
    if ((in.indexOf('$') + in.indexOf('%')) == -2) {
      return in;
    }
    Matcher m = PATTERN_VAR_PROPERTY.matcher(in);
    Config cfg = Config.getConfig();
    while (m.find()) {
      String val = cfg.getString(m.group(1), System.getProperty(m.group(1), System.getenv(m.group(1))));
      if (!isNullOrEmpty(val)) {
        in = in.replace(m.group(0), val);
      }
    }

    m = PATTERN_VAR_DATETIME.matcher(in);
    Date now = new Date();
    while (m.find()) {
      try {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(now);
        if ("<".equals(m.group(2))) {
          cal.set(Calendar.HOUR, 0);
          cal.set(Calendar.MINUTE, 0);
          cal.set(Calendar.SECOND, 0);
          cal.set(Calendar.MILLISECOND, 0);
        }

        if (!StringUtils.isNullOrEmpty(m.group(3))) {
          int offset = Integer.parseInt(m.group(3));
          char metric = 'd';
          if (!StringUtils.isNullOrEmpty(m.group(4))) {
            metric = m.group(4).charAt(0);
          }
          switch (metric) {
          case 'H':
            cal.add(Calendar.HOUR_OF_DAY, offset);
            break;
          case 'M':
            cal.add(Calendar.MINUTE, offset);
            break;
          case 'S':
            cal.add(Calendar.SECOND, offset);
            break;
          case 'y':
            cal.add(Calendar.YEAR, offset);
            break;
          case 'm':
            cal.add(Calendar.MONTH, offset);
            break;
          case 'd':
            cal.add(Calendar.DAY_OF_MONTH, offset);
            break;
          default:
            break;
          }
        }

        SimpleDateFormat sdf = new SimpleDateFormat(m.group(1));
        in = in.replace(m.group(0), sdf.format(cal.getTime()));
      } catch (NumberFormatException nfe) {
        Logger.getLogger(StringUtils.class.getName()).log(Level.WARNING, "NumberFormatException parsing {0} in variable $\\{{1}\\}: {2}",
            new Object[] { m.group(3), m.group(0), nfe.getMessage() });
      } catch (IllegalArgumentException iae) {
        Logger.getLogger(StringUtils.class.getName()).log(Level.FINER, "IllegalArgumentException in variable $\\{{0}\\} ({1}). Ignore.",
            new Object[] { m.group(0), iae.getMessage() });
      }
    }
    return in;
  }

  public static Set<Integer> expandRange(String range) {
    Set<Integer> list = new LinkedHashSet<>();
    StringBuilder sb = new StringBuilder();
    int start = -1;
    int num = 0;
    try {
      range += ",";
      for (char c : range.toCharArray()) {
        switch (c) {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          sb.append(c);
          break;
        case ' ':
          break;
        case '-':
        case ',':
          num = Integer.parseInt(sb.toString());
          sb.setLength(0);
          if (start > -1) {
            for (int i = start; i <= num; i++) {
              list.add(i);
            }
            start = -1;
          } else {
            list.add(num);
          }
          if (c == '-') {
            start = num;
          }
          break;
        default:
          break;
        }
      }
    } catch (NumberFormatException e) {
      list.clear();
    }
    return list;
  }

  public static String listToRange(Collection<Integer> list) {
    StringBuilder sb = new StringBuilder();

    Integer[] values = list.toArray(new Integer[0]);
    Arrays.sort(values);

    for (int i = 0; i < values.length; i++) {
      if (i > 0) {
        if (values[i] == values[i - 1] + 1) {
          if (i < (values.length - 1) && values[i + 1] == (values[i] + 1)) {
            continue;
          } else {
            sb.append('-');
          }
        } else {
          sb.append(',');
        }
      }
      sb.append(values[i]);
    }
    return sb.toString();
  }

  public static String getTimestamp(Date date) {
    SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    return fmt.format(date);
  }

  public static String getTimestamp() {
    return getTimestamp(new Date());
  }

  public static String getFileExt(final String fileName) {
    String result = "";
    if (fileName.length() > 0) {
      int fileExtPos = fileName.lastIndexOf('.');
      if (fileExtPos >= 0) {
        int backslashPos = fileName.lastIndexOf('\\');
        int slashPos = fileName.lastIndexOf('/');
        if (fileExtPos > backslashPos && fileExtPos > slashPos) {
          result = fileName.substring(fileExtPos + 1);
          if (result.matches("(?i)^(gz|gzip|bzip|bzip2)$")) {
            String ext = getFileExt(fileName.substring(0, fileExtPos));
            if (ext.length() > 0 && ext.length() <= 4) {
              result = ext + "." + result;
            }
          }
        }
      }
    }

    return result;
  }

  public static String changeFileExt(String fileName, String newExt) {
    String result = fileName;
    final String fileExt = getFileExt(fileName);
    if (fileExt.length() > 0) {
      result = result.substring(0, result.length() - fileExt.length() - 1) + newExt;
    } else {
      result += newExt;
    }
    return result;
  }

  public static String addTimestampToFilename(final String fileName) {
    return changeFileExt(fileName, "-" + getTimestamp() + "." + getFileExt(fileName));
  }

  public static Timestamp parseTimestamp(String ts) throws ParseException {
    Timestamp result = null;
    Pattern p = Pattern.compile("^(\\d{2,4})-(\\d{1,2})-(\\d{1,2})(?:(?:\\s+|T)(\\d{1,2}):(\\d{1,2}):?(\\d{0,2})\\.?(\\d{0,9}))?");
    Matcher m = p.matcher(ts);
    if (m.matches()) {
      Calendar cal = toCalendar(m.group(1), m.group(2), m.group(3), m.group(4), m.group(5), m.group(6));
      result = new Timestamp(cal.getTimeInMillis());
      result.setNanos(toNanos(m.group(7)));
      return result;
    }
    p = Pattern.compile("^(\\d{1,2}).(\\d{1,2}).(\\d{2,4})(?:(?:\\s+|T)(\\d{1,2}):(\\d{1,2}):?(\\d{0,2})\\.?(\\d{0,9}))?");
    m = p.matcher(ts);
    if (m.matches()) {
      Calendar cal = toCalendar(m.group(3), m.group(2), m.group(1), m.group(4), m.group(5), m.group(6));
      result = new Timestamp(cal.getTimeInMillis());
      result.setNanos(toNanos(m.group(7)));
      return result;
    }
    throw new ParseException("Could not parse timestamp: " + ts, 0);
  }

  public static Date parseDateTime(String dt) throws ParseException {
    return new Date(parseTimestamp(dt).getTime());
  }

  private static Calendar toCalendar(String year, String month, String dayOfMonth, String hourOfDay, String minute, String second) {
    Calendar cal = new GregorianCalendar();

    cal.set(Calendar.YEAR, Integer.valueOf(year));
    if (cal.get(Calendar.YEAR) < 1000)
      cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 2000);
    cal.set(Calendar.MONTH, Integer.valueOf(month) - 1);
    cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(dayOfMonth));
    cal.set(Calendar.HOUR_OF_DAY, (isNullOrWhiteSpace(hourOfDay) ? 0 : Integer.valueOf(hourOfDay)));
    cal.set(Calendar.MINUTE, (isNullOrWhiteSpace(minute) ? 0 : Integer.valueOf(minute)));
    cal.set(Calendar.SECOND, (isNullOrWhiteSpace(second) ? 0 : Integer.valueOf(second)));
    cal.set(Calendar.MILLISECOND, 0);

    return cal;
  }

  private static int toNanos(String val) {
    if (!isNullOrWhiteSpace(val)) {
      return (int) (Integer.valueOf(val) * Math.pow(10, (9 - val.length())));
    } else {
      return 0;
    }
  }

  public static BigDecimal parseDecimal(String val) throws ParseException {
    int comma = val.indexOf(',');
    int point = val.indexOf('.');
    char decimalsep = '.';
    char thousandssep = ',';
    if (comma > -1 && point > -1) {
      if (comma > point) {
        decimalsep = ',';
        thousandssep = '.';
      }
    } else if (val.lastIndexOf('.') > point || (val.length() - comma) < 4) {
      decimalsep = ',';
      thousandssep = '.';
    }
    return parseDecimal(val, decimalsep, thousandssep);
  }

  public static BigDecimal parseDecimal(String val, char decimalChar, char thousandsSep) throws ParseException {
    BigDecimal result = null;
    try {
      result = new BigDecimal(val.replace(new String(new char[] { thousandsSep }), "").replace(decimalChar, '.'));
    } catch (NumberFormatException nfe) {
      throw new ParseException("Could not parse decimal: " + val, 0);
    }
    return result;
  }

  @Deprecated
  public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
    keyGen.init(RND);
    return keyGen.generateKey();
  }

  @Deprecated
  private static byte[] getAESKey() {
    Config cfg = Config.getConfig();
    if (cfg.hasProperty("_AES_KEY_")) {
      return Base64.getDecoder().decode(cfg.getString("_AES_KEY_"));
    } else {
      return _KEY_;
    }
  }

  public static String normalizeString(String string) {
    if (string == null)
      return null;
    StringBuilder sb = new StringBuilder(string.length());
    boolean space = true;
    for (int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);
      switch (c) {
      case '\t':
      case '\r':
      case '\n':
      case ' ':
        if (!space) {
          sb.append(' ');
          space = true;
        }
        break;
      default:
        sb.append(c);
        space = false;
        break;
      }
    }
    return sb.substring(0, (sb.charAt(sb.length() - 1) == ' ' ? sb.length() - 1 : sb.length()));
  }
}
