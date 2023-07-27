package de.denkunddachte.sftp;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import de.denkunddachte.b2biutil.Version;
import de.denkunddachte.utils.CommandLineParser;
import de.denkunddachte.utils.CommandLineParser.CommandLineException;
import de.denkunddachte.utils.CommandLineParser.CommandLineOption;
import de.denkunddachte.utils.CommandLineParser.ParsedCommandLine;
import de.denkunddachte.utils.Config;
import de.denkunddachte.utils.FileUtil;
import de.denkunddachte.utils.LogConfig;
import de.denkunddachte.utils.Password;
import de.denkunddachte.utils.Password.CryptException;
import de.denkunddachte.utils.StringUtils;

public class SFTPClient {
  private static final Logger            LOGGER           = Logger.getLogger(SFTPClient.class.getName());
  private static final CommandLineParser cmdLine;
  private static final Config            CFG              = Config.getConfig("de/denkunddachte/sftp/SFTPClient.properties");
  private Properties                     counterprops;
  private boolean                        saveCounterprops = false;
  private Random                         rand             = new Random();
  static {
    if (System.getProperty(Config.PROP_INSTALLDIR) == null) {
      System.setProperty(Config.PROP_INSTALLDIR, System.getProperty("user.dir"));
    }
    cmdLine = new CommandLineParser(false);
    cmdLine.setProgramName(SFTPClient.class.getName());
    cmdLine.setHasOptionalArgs(true);
    cmdLine.setProgramDescription("Simple SFTPClient client for testing SFG.");
    cmdLine.add(Config.PROP_CONFIG_FILE + "=s", "Path to config file.", Config.PROP_CONFIG_FILE, "${installdir}/sftp.properties");
    cmdLine.add("localfile|f=s", "Local file");
    cmdLine.add("eicartest", "Send EICAR AV test string");
    cmdLine.add("put|p=s", "PUT file (%<n>d patterns are replaced by formatted counter)");
    cmdLine.add("host|H=s", "Host [<user>@]<host>[:<port>]");
    cmdLine.add("port|P=i", "Port", "port", "22");
    cmdLine.add("user|u=s", "User", "user", "${user.name}");
    cmdLine.add("size|s=i", "File size to upload");
    cmdLine.add("count|n=i", "Number of files to upload");
    cmdLine.add("password|K=s", "Password");
    cmdLine.add("identity|i=s", "Path to private key file (Default: use builtin unless password is used)");
    cmdLine.add("passphrase|k=s", "Passphrase for private key file.");
    cmdLine.add("knownhosts=s", "Path to known hosts file", "knownhosts", "${user.home}/.ssh/known_hosts");
    cmdLine.add("nostricthostkeycheck|N", "No strict host key checking");
    cmdLine.add("print-key:s", "Print builtin public key (optinal: format ossh or secsh)");
    cmdLine.add("debug|D=s", "Set debug to stdout to level (use java.util.logging level)", LogConfig.PROP_LOG_STDOUT);
    cmdLine.add(Config.PROP_VERSION, "Display version and exit.");
    cmdLine.add(Config.PROP_HELP, "Show this help.");
  }

  public SFTPClient(String[] args) {
    init(args);
  }

  private void init(String[] args) {
    try {
      ParsedCommandLine result = cmdLine.parse(args);
      if (result.containsKey(Config.PROP_CONFIG_FILE) && !CFG.setConfig(result.get(Config.PROP_CONFIG_FILE).getValue())) {
        throw new CommandLineException("Could not load config file from " + result.get(Config.PROP_CONFIG_FILE).getValue() + "!");
      }
      if (result.containsKey(Config.PROP_HELP)) {
        cmdLine.printHelp();
        System.exit(0);
      }
      if (result.containsKey(Config.PROP_VERSION)) {
        System.out.format("%s version %s (Build %s)\n", SFTPClient.class.getName(), Version.VERSION, Version.BUILD);
        if (!StringUtils.isNullOrWhiteSpace(Version.COPYRIGHT)) {
          System.out.format("%s\n", Version.COPYRIGHT);
        }
        System.exit(0);
      }
      for (CommandLineOption o : result.values()) {
        if (!Config.PROP_CONFIG_FILE.equals(o.getLongName())) {
          if (o.wasInTheCommandLine() || !CFG.hasProperty(o.getPropertyName()) && (o.wasInTheCommandLine() || !CFG.hasProperty(o.getPropertyName()))) {
            CFG.setProperty(o.getPropertyName(), o.getValue());
          }
        }
      }
      LogConfig.initConfig(CFG);

      if (CFG.hasProperty("print-key")) {
        
        System.out.println("Builtin private key: ");
        System.out.println(BuiltinSshKey.getPrivateKey());
        System.out.println();
        System.out.println("Passphrase (encrypted):");
        System.out.println(BuiltinSshKey.getEncryptedPassphrase());
        System.out.println();
        System.out.println("Builtin public key: ");
        if ("secsh".equalsIgnoreCase(CFG.getString("print-key"))) {
          System.out.println(BuiltinSshKey.getSecShPublicKey());
        } else {
          System.out.println(BuiltinSshKey.getOsshPublicKey());
        }
        System.exit(0);
      }

      if (CFG.getString("host").isEmpty()) {
        throw new CommandLineException("Required parameter --host missing!");
      }

      String host = CFG.getString("host");
      int pos = host.indexOf(':');
      if (pos > 0) {
        int port = Integer.parseInt(host.substring(pos + 1));
        CFG.setProperty("port", Integer.toString(port));
        host = host.substring(0, pos);
      }
      pos = host.indexOf('@');
      if (pos > 0) {
        CFG.setProperty("user", host.substring(0, pos));
        host = host.substring(pos + 1);
      }
      CFG.setProperty("host", host);
      if (CFG.hasProperty("nostricthostkeycheck")) {
        CFG.setProperty("StrictHostKeyChecking", "no");
      }

      counterprops = new Properties();
      File counter = FileUtil.getFile(StringUtils.expandVariables(CFG.getString("counterFile")));
      if (counter.exists()) {
        try (InputStream is = new FileInputStream(counter)) {
          counterprops.load(is);
        } catch (IOException e) {
          throw new CommandLineException("Could not open counter file: " + counter.getAbsolutePath());
        }
      }

    } catch (CommandLineException e) {
      System.err.println("Caught CommandLineException: " + e.getMessage());
      cmdLine.printHelp();
      System.exit(1);
    }
  }

  private void closeCounter() {
    if (saveCounterprops) {
      try (OutputStream os = new FileOutputStream(FileUtil.getFile(StringUtils.expandVariables(CFG.getString("counterFile"))))) {
        counterprops.store(os, null);
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, e, () -> "Error saving counter file " + CFG.getString("counterFile") + "!");
      }
    }
  }

  public void put(String sndFilePattern) throws JSchException {
    int count = CFG.getInt("count", 1);
    Channel channel = null;
    ChannelSftp sftp = null;
    try {
      channel = openSftpChannel();
      sftp = (ChannelSftp) channel;
      while (count-- > 0) {
        putFile(sftp, sndFilePattern);
      }
    } catch (JSchException | SftpException | IOException | CryptException e) {
      LOGGER.severe("Error: " + e.getMessage());
      e.printStackTrace();
    } finally {
      if (channel != null) {
        LOGGER.info("Exit SFTPClient.");
        sftp.exit();
        LOGGER.info("Disconnect channel and end session.");
        sftp.disconnect();
        sftp.getSession().disconnect();
      }
    }
    closeCounter();
  }

  public void putFile(ChannelSftp sftp, String sndFilePattern) throws SftpException, IOException {
    LOGGER.log(Level.FINER, "PUT {0}", sndFilePattern);
    String destFile = getDestFile(sndFilePattern);

    if (CFG.hasProperty("localfile")) {
      File localfile = FileUtil.getFile(StringUtils.expandVariables(CFG.getString("localfile")));
      sftp.put(localfile.getAbsolutePath(), destFile);
      LOGGER.log(Level.INFO, "Uploaded local file {0} as {1}.", new Object[] { localfile, destFile });
    } else {
      OutputStream os = sftp.put(destFile);
      if (CFG.getBoolean("eicartest")) {
        os.write("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes());
        LOGGER.log(Level.INFO, "Created EICAR infected testfile {0}.", destFile);
      } else {
        os.write(getData(destFile));
        LOGGER.log(Level.INFO, "Created testfile {0}.", destFile);
      }
      os.close();
    }
  }

  private byte[] getData(String destFile) {
    if (CFG.hasProperty("size")) {
      int size = CFG.getInt("size");
      byte[] data = new byte[size];
      rand.nextBytes(data);
      return data;
    } else {
      return ("This is s SFTP test file " + destFile + "\nCreated " + new Date() + "\n").getBytes();
    }
  }

  private String getDestFile(String sndFile) {
    String result;
    if (sndFile.indexOf('%') > -1) {
      int i = sndFile.lastIndexOf('/');
      String cname = sndFile.substring((i > -1 ? i + 1 : 0));
      if (counterprops.containsKey(cname)) {
        i = Integer.parseInt(counterprops.getProperty(cname));
      } else {
        i = 0;
      }
      counterprops.put(cname, Integer.toString(++i));
      saveCounterprops = true;
      result = String.format(sndFile, i);
    } else {
      return sndFile;
    }
    return result;
  }

  private Channel openSftpChannel() throws JSchException, CryptException {
    Channel ch = null;
    JSch jsch = new JSch();
    jsch.setKnownHosts(StringUtils.expandVariables(CFG.getString("knownhosts")));
    if (!CFG.hasProperty("password")) {
      if (CFG.hasProperty("identity")) {
        String identityFile = StringUtils.expandVariables(CFG.getProperty("identity"));
        File keyFile = FileUtil.getFile(identityFile);

        if (!keyFile.canRead()) {
          throw new RuntimeException("Key file " + identityFile + " not found or not readable!");
        }
        jsch.addIdentity(identityFile, getPassphrase(jsch, identityFile));
      } else {
        jsch.addIdentity(BuiltinSshKey.instance(jsch), Password.getCleartext(BuiltinSshKey.getEncryptedPassphrase()).getBytes());
      }
    }
    Properties sshconfig = new Properties();
    sshconfig.put("StrictHostKeyChecking", CFG.getProperty("StrictHostKeyChecking", "yes"));
    sshconfig.put("compression.s2c", CFG.getProperty("compression.s2c", "zlib,none"));
    sshconfig.put("compression.c2s", CFG.getProperty("compression.s2c", "zlib,none"));
    Session session = jsch.getSession(StringUtils.expandVariables(CFG.getString("user")), CFG.getString("host"), Integer.parseInt(CFG.getString("port")));
    session.setConfig(sshconfig);
    if (CFG.hasProperty("password")) {
      session.setPassword(Password.getCleartext(CFG.getProperty("password")));
    }
    session.setTimeout(10 * 1000);
    session.connect();
    LOGGER.log(Level.INFO, "Connected to host {0}:{1}, user={2} ({3}).",
        new Object[] { session.getHost(), session.getPort(), session.getUserName(), (CFG.hasProperty("password") ? "using password" : "using public key") });
    ch = session.openChannel("sftp");
    ch.connect();
    LOGGER.info("Opened and connected SFTPClient channel.");

    return ch;
  }

  private String getPassphrase(JSch jsch, String identityFile) throws JSchException, CryptException {
    String passphrase = Password.getCleartext(CFG.getProperty("passphrase"));
    KeyPair kpair = KeyPair.load(jsch, identityFile);
    LOGGER.log(Level.FINE, "Attempt to decrypt key: {0} (have passphrase:{1})", new Object[] { identityFile, (passphrase == null ? "no" : "yes") });
    if (!kpair.decrypt(passphrase)) {
      LOGGER.log(Level.INFO, "Identity file {0} requires passphrase.", identityFile);
      Console console = System.console();
      if (console == null) {
        LOGGER.severe("Could not get console instance to enter passhprase. Please provide passphrase as property or command line option!");
        System.exit(1);
      }
      passphrase = new String(console.readPassword("Enter passphrase: "));
      if (!kpair.decrypt(passphrase)) {
        LOGGER.severe("Passphrase invalid!");
        System.exit(1);
      }
    }
    return passphrase;
  }

  public static void main(String[] args) throws JSchException, IOException {
    SFTPClient sftp = new SFTPClient(args);
    if (CFG.hasProperty("put")) {
      sftp.put(StringUtils.expandVariables(CFG.getString("put")));
    }
  }
}
