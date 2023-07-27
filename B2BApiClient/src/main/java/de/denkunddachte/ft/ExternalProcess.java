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
package de.denkunddachte.ft;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExternalProcess {

  public enum EnvironmenType {
    UNIX, CYGWIN, WINDOWS
  }

  private static final String  CLASSNAME           = ExternalProcess.class.getName();
  private static final Logger  LOGGER              = Logger.getLogger(CLASSNAME);
  public static final long     DEFAULT_TIMEOUT     = 5 * 60 * 1000L;

  private String               command;
  private String               workDir;
  private StringBuilder        stdout;
  private StringBuilder        stderr;
  private boolean              hasStderrOutput;
  private boolean              hasStdoutOutput;
  private Map<String, String>  environment;
  public static final String   ENV_PROPERTIES      = "env.properties";

  // Regex: (["'])((?:(?!\1)[^\\]|(?:\\\\)*|\\.)*?)\1|(\S+)
  private static final Pattern cmdLineSplitPattern = Pattern.compile("([\"'])((?:(?!\\1)[^\\\\]|(?:\\\\\\\\)*|\\\\.)*?)\\1|(\\S+)");

  public static EnvironmenType ENV_TYPE;
  public static String         SYSTEM_TEMPDIR;

  /**
   * Determine system type (Windows, Windows + Cygwin or UNIX)
   */
  static {
    // Windows system?
    if (System.getProperty("os.name").startsWith("Windows")) {
      // with Cygwin?
      if (System.getenv("CYGWIN_HOME") != null) {
        ENV_TYPE = EnvironmenType.CYGWIN;
        try {
          SYSTEM_TEMPDIR = (new File("/var/tmp")).getCanonicalPath(); // make sure we stay on current drive
        } catch (IOException ignore) {
        }
      } else {
        ENV_TYPE = EnvironmenType.WINDOWS;
        SYSTEM_TEMPDIR = System.getProperty("java.io.tmpdir");
      }
    } else {
      // assume any UNIX type
      ENV_TYPE = EnvironmenType.UNIX;
      SYSTEM_TEMPDIR = System.getProperty("java.io.tmpdir");
    }
  }

  /**
   * Creates an ExternalProcess for command with default environment from
   * env.properties. The environment can be manipulated
   * through {@link #getEnvironment()} / {@link #setEnvironment(Map)} before
   * running the process.
   * STDOUT and STDERR are collected and can be retrieved with
   * {@link #getStderr()} and {@link #getStdout()}.
   * The work directory is set to the java.io.tmpdir system property except in a
   * CYGWIN environment, where it is set to the
   * /var/tmp directory on the current windows drive.
   * The process can be started using one of the {@link #execute()} methods.
   * 
   * @param command
   */
  public ExternalProcess(String command) {
    this(command, SYSTEM_TEMPDIR, new StringBuilder(), new StringBuilder(), false);
  }

  public ExternalProcess(String command, boolean readEnvironment) {
    this(command, SYSTEM_TEMPDIR, new StringBuilder(), new StringBuilder(), readEnvironment);
  }

  public ExternalProcess(String command, Map<String, String> environment) {
    this(command, SYSTEM_TEMPDIR, new StringBuilder(), new StringBuilder(), environment);
  }

  /**
   * Creates an ExternalProcess handler for command.
   * 
   * @param command         Commmand to run (on windows, commands that don't end
   *                        with .exe are started with CMD /C command [args])
   * @param workDir         set working directory
   * @param stdout          StringBuilder that STDOUT output is appended to
   * @param stderr          StringBuilder that STDERR output is appended to
   * @param readEnvironment if true, the env.proerties is read into the
   *                        environment map
   */
  public ExternalProcess(String command, String workDir, StringBuilder stdout, StringBuilder stderr, boolean readEnvironment) {
    this.command = command;
    this.workDir = workDir;
    this.stdout = stdout;
    this.stderr = stderr;
    if (readEnvironment) {
      this.environment = readEnvironment();
    } else {
      this.environment = null;
    }
  }

  public ExternalProcess(String command, String workDir, StringBuilder stdout, StringBuilder stderr, Map<String, String> environment) {
    this.command = command;
    this.workDir = workDir;
    this.stdout = stdout;
    this.stderr = stderr;
    this.environment = environment;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public StringBuilder getStdout() {
    return stdout;
  }

  public void setStdout(StringBuilder stdout) {
    this.stdout = stdout;
  }

  public StringBuilder getStderr() {
    return stderr;
  }

  public void setStderr(StringBuilder stderr) {
    this.stderr = stderr;
  }

  public Map<String, String> getEnvironment() {
    return this.environment;
  }

  public void setEnvironment(Map<String, String> environment) {
    this.environment = environment;
  }

  public String getWorkDir() {
    return workDir;
  }

  public void setWorkDir(String workDir) {
    this.workDir = workDir;
  }

  public boolean hasStderrOutput() {
    return this.hasStderrOutput;
  }

  public boolean hasStdoutOutput() {
    return this.hasStdoutOutput;
  }

  /**
   * Runs the external command with default timeout of 300000ms (5min.).
   * 
   * @return exit code of the external process
   * @throws InterruptedException
   * @throws IOException
   */
  public int execute() throws InterruptedException, IOException {
    return execute(DEFAULT_TIMEOUT, false);
  }

  /**
   * Runs the external command with default timeout of 300000ms (5min.). The
   * environment is replaced with {@link #getEnvironment()}.
   * 
   * @return exit code of the external process
   * @throws InterruptedException
   * @throws IOException
   */
  public int execute(boolean clearEnvironment) throws InterruptedException, IOException {
    return execute(DEFAULT_TIMEOUT, clearEnvironment);
  }

  public int execute(long timeout, boolean clearEnvironment) throws InterruptedException, IOException {
    int rc = -1;
    Timer timer = null;
    Process p = null;
    StreamOutputCollector stdoutReader = null;
    StreamOutputCollector stderrReader = null;

    ProcessBuilder pb = ExternalProcess.createProcessBuilder(command, workDir, environment, !clearEnvironment);

    timer = new Timer(true);
    InterruptTimerTask interruptor = new InterruptTimerTask(Thread.currentThread());
    timer.schedule(interruptor, timeout);
    LOGGER.log(Level.FINER, "Start process \"{0}\", in dir={1}, timeout={2}ms, appendEnvironment={3}...",
        new Object[] { pb.command(), workDir, timeout, !clearEnvironment });

    try {
      p = pb.start();
      p.getOutputStream().close();
    } catch (IOException ioe) {
      LOGGER.log(Level.FINE, "Could not run \"{0}\": {1}", new Object[] { command, ioe.getMessage() });
      throw new IOException("Could not run \"" + command + "\": " + ioe.getMessage());
    }

    try {
      if (stderr != null) {
        stderrReader = new StreamOutputCollector(p.getErrorStream(), stderr);
        stderrReader.start();
      }
      if (stdout != null) {
        stdoutReader = new StreamOutputCollector(p.getInputStream(), stdout);
        stdoutReader.start();
      }
      rc = p.waitFor();

      if (stderrReader != null) {
        try {
          stderrReader.join(10000L);
        } catch (InterruptedException ie) {
          LOGGER.log(Level.FINE, "STDERR handler interrupted while waiting.");
        } finally {
          this.hasStderrOutput = stderrReader.hasOutput();
          if (stderrReader.isAlive()) {
            stderrReader.interrupt();
          }
          stderrReader = null;
        }
      }

      if (stdoutReader != null) {
        try {
          stdoutReader.join(10000L);
        } catch (InterruptedException ie) {
          LOGGER.log(Level.FINE, "STDOUT handler interrupted while waiting.");
        } finally {
          this.hasStdoutOutput = stdoutReader.hasOutput();
          if (stdoutReader.isAlive()) {
            stdoutReader.interrupt();
          }
          stdoutReader = null;
        }
      }

      LOGGER.log(Level.FINE, "Process \"{0}\" ends with rc={1}", new Object[] { command, rc });

    } catch (InterruptedException e) {
      LOGGER.log(Level.FINE, "Command \"{0}\" timed out after {1}ms.", new Object[] { command, timeout });
      p.destroy();
      throw new InterruptedException("Command \"" + command + "\" timed out after " + timeout + "ms.");

    } finally {
      LOGGER.log(Level.FINEST, "Cleanup...");
      timer.cancel();
      Thread.interrupted();
      try {
        p.getInputStream().close();
        p.getErrorStream().close();
      } catch (IOException e1) {
        LOGGER.log(Level.WARNING, "Error closing streams: {0}", e1.getMessage());
      }

    }

    LOGGER.log(Level.FINEST, "Normal exit.");
    return rc;
  }

  /**
   * Reads file env.properties and puts its contents into Map.
   * 
   * @return environment
   */
  public static Map<String, String> readEnvironment() {
    if (System.getProperty("env.properties") != null) {
      return readEnvironment(new File(System.getProperty("env.properties")));
    } else {
      return readEnvironment(ENV_PROPERTIES);
    }
  }

  public static Map<String, String> readEnvironment(File propertyFile) {
    Map<String, String> env = new HashMap<>();
    if (!propertyFile.exists()) {
      LOGGER.log(Level.WARNING, "Environment properties file {0} does not exits!", propertyFile.getAbsolutePath());
      return env;
    }

    try (InputStream in = new FileInputStream(propertyFile)) {
      env = readEnvironment(in);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e, () -> "Could not read properties file " + propertyFile);
    }
    return env;
  }

  public static Map<String, String> readEnvironment(String resourceName) {
    Map<String, String> env = new HashMap<>();

    try (InputStream in = ExternalProcess.class.getClassLoader().getResourceAsStream(resourceName)) {
      if (in != null) {
        env = readEnvironment(in);
      } else {
        LOGGER.log(Level.WARNING, "Environment properties resource {0} could not be found!", resourceName);
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e, () -> "Could not read properties " + resourceName);
    }
    return env;
  }

  private static Map<String, String> readEnvironment(InputStream in) throws IOException {
    Map<String, String> env = new HashMap<>();

    Properties p = new Properties();
    p.load(in);
    if (!p.isEmpty()) {
      for (Object key : p.keySet()) {
        // LOGGER.log(Level.DEBUG_DUMP, "Add property: " + key + ", value: " +
        // p.get(key));
        env.put((String) key, (String) p.get(key));
      }

    }
    return env;
  }

  public static String getDefaultTempDirPath() {
    String path = System.getProperty("java.io.tmpdir");
    if (path == null || path.isEmpty()) {
      if (ENV_TYPE == EnvironmenType.UNIX || ENV_TYPE == EnvironmenType.CYGWIN) {
        path = "/tmp";
      } else {
        path = ".";
      }
    }
    return path;
  }

  public static File getDefaultTempDir() {
    return new File(getDefaultTempDirPath());
  }

  /**
   * Splits string as command line (separated by whitespace except when enclosed
   * in " ").
   * 
   * @param cmd
   *            String to split.
   * @return List<>
   */
  public static List<String> splitCmdLine(String cmd) {
    Matcher m = cmdLineSplitPattern.matcher(cmd);
    List<String> ret = new ArrayList<String>();
    while (m.find()) {
      if (m.group(3) != null) {
        ret.add(m.group(3)); // unquoted
      } else if (m.group(2) != null) {
        ret.add(m.group(2)); // quoted
      } else {
        ret.add(m.group()); // should not happed
      }
    }
    return ret;
  }

  /*
   * Creates ProcessBuilder instance from command line cmd
   */
  public static ProcessBuilder createProcessBuilder(String cmd, String directory, boolean readExternalEnvironment, boolean appendenvironment) {
    Map<String, String> environment = null;
    if (readExternalEnvironment) {
      environment = readEnvironment();
    }
    return createProcessBuilder(splitCmdLine(cmd), new File(directory), environment, appendenvironment);
  }

  public static ProcessBuilder createProcessBuilder(String cmd, String directory, Map<String, String> environment, boolean appendenvironment) {

    return createProcessBuilder(splitCmdLine(cmd), new File(directory), environment, appendenvironment);
  }

  public static ProcessBuilder createProcessBuilder(List<String> cmd, File directory, Map<String, String> environment, boolean appendenvironment) {
    ArrayList<String> localcmd = new ArrayList<>();
    if (ENV_TYPE != EnvironmenType.UNIX && !cmd.get(0).toLowerCase().endsWith(".exe")) {
      localcmd.add("cmd");
      localcmd.add("/c");
    }
    // else if (environment != null && environment.containsKey("RUNSH")) {
    // localcmd.add(environment.get("RUNSH"));
    // }
    localcmd.addAll(cmd);
    ProcessBuilder pb = new ProcessBuilder(localcmd);
    if (directory.exists() && directory.isDirectory()) {
      pb.directory(directory);
    } else {
      try {
        LOGGER.log(Level.WARNING, "Working directory {0} for ProcessBuilder does not exist. Use default.", directory.getCanonicalPath());
      } catch (IOException e) {
        // ignore
        e.printStackTrace();
      }
    }
    if (environment != null && !environment.isEmpty()) {
      // TODO: fix paths to platform...
      if (!appendenvironment) {
        pb.environment().clear();
      }
      pb.environment().putAll(environment);
    }
    return pb;
  }

  /**
   * Helper class to interrupt a running thread by Timer.
   * 
   * @author agaffke
   * 
   */
  private class InterruptTimerTask extends TimerTask {
    private Thread thread;

    public InterruptTimerTask(Thread thread) {
      this.thread = thread;
      thread.setName("extproc-timer-" + thread.getId());
    }

    @Override
    public void run() {
      thread.interrupt();
    }
  }

}
