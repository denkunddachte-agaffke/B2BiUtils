package de.denkunddachte.utils;

import java.util.Properties;
import java.util.regex.Pattern;

import de.denkunddachte.utils.Password.CryptException;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class MailUtil {
  final private Properties mailprops = new Properties();
  private Session          session;
  private Config           cfg;

  public MailUtil() throws CryptException {
    this(Config.getConfig());
  }

  public MailUtil(Config cfg) throws CryptException {
    this.cfg = cfg;
    mailprops.put("mail.smtp.auth", cfg.getProperty("mail.smtp.auth", "false"));
    mailprops.put("mail.smtp.starttls.enable", cfg.getProperty("mail.smtp.starttls.enable", "false"));
    mailprops.put("mail.smtp.host", cfg.getProperty("mail.smtp.host", "localhost"));
    mailprops.put("mail.smtp.port", cfg.getProperty("mail.smtp.port", "25"));
    Authenticator authenticator = null;
    if (cfg.getBoolean("mail.smtp.auth")) {
      final String user     = cfg.getProperty("mail.smtp.auth.user");
      final String password = Password.getCleartext(cfg.getProperty("mail.smtp.auth.password"));
      authenticator = new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(user, password);
        }
      };
    }
    session = Session.getInstance(mailprops, authenticator);
  }

  public void sendMail(String subject, String body) throws MessagingException {
    sendMail(cfg.getString("mail.to"), cfg.getProperty("mail.cc"), cfg.getString("mail.from", "noreply@localhost"), subject, body, true);
  }

  public void sendMail(String mailto, String mailcc, String from, String subject, String body, boolean textMode) throws MessagingException {
    try {
      sendMail(InternetAddress.parse(mailto), (mailcc != null ? InternetAddress.parse(mailcc) : null), new InternetAddress(from), subject, body, textMode);
    } catch (AddressException e) {
      throw new MessagingException(e);
    }
  }

  public void sendMail(InternetAddress[] mailto, InternetAddress[] mailcc, InternetAddress from, String subject, String body, boolean textMode)
      throws MessagingException {
    Message message = new MimeMessage(session);
    try {
      message.setFrom(from);
      message.setRecipients(Message.RecipientType.TO, mailto);
      if (mailcc != null)
        message.setRecipients(Message.RecipientType.CC, mailcc);
      message.setSubject(subject);
      if (textMode) {
        message.setContent("<pre>" + body + "</pre>", "text/html");
      } else {
        message.setContent(body, "text/plain");
      }
      Transport.send(message);
    } catch (jakarta.mail.MessagingException me) {
      throw new MessagingException(me);
    }
  }

  public static boolean validateAddress(String address) {
    boolean result = true;
    if (address == null || address.trim().isEmpty())
      return false;

    final Pattern p = Pattern.compile("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$");
    for (String a : address.split(",")) {
      if (a.isEmpty() || !p.matcher(a).matches()) {
        result = false;
      }
    }
    return result;
  }
}
