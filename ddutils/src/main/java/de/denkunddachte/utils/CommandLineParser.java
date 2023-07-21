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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandLineParser {
  public enum ARGUMENT_TYPE {
    STRING, INTEGER, NUMBER, BOOLEAN, NONE
  }

  private List<CommandLineItem> commandLineItems  = new ArrayList<>();
  protected String              keyValueSeparator = "=";
  protected boolean             hasMandatoryArgs  = false;
  protected boolean             hasOptionalArgs   = false;
  private String                programName;
  private String                programDescription;
  private StringBuilder         programHelp;

  public CommandLineParser() {
  }

  public CommandLineParser(boolean hasMandatoryArgs) {
    this.hasMandatoryArgs = hasMandatoryArgs;
  }

  public CommandLineParser(boolean hasMandatoryArgs, String keyValueSeparator) {
    this.hasMandatoryArgs = hasMandatoryArgs;
    this.keyValueSeparator = keyValueSeparator;
  }

  public String getProgramName() {
    return programName;
  }

  public void setProgramName(String programName) {
    this.programName = programName;
  }

  public String getProgramDescription() {
    return programDescription;
  }

  public void setProgramDescription(String programDescription) {
    this.programDescription = programDescription;
  }

  public String getProgramHelp() {
    if (programHelp != null) {
      return programHelp.toString();
    }
    return null;
  }

  public void addProgramHelp(String line) {
    if (programHelp == null) {
      programHelp = new StringBuilder();
    }
    programHelp.append(line).append(StringUtils.LF);
  }

  public void setHasOptionalArgs(boolean hasOptionalArgs) {
    this.hasOptionalArgs = hasOptionalArgs;
  }

  public List<CommandLineOption> getAllOptions() {
    return commandLineItems.stream().filter(item -> item instanceof CommandLineOption).map(item -> (CommandLineOption) item).collect(Collectors.toList());
  }

  public boolean hasOption(final String longName) {
    return getAllOptions().stream().anyMatch(o -> o.getLongName().equals(longName));
  }

  public CommandLineParser add(CommandLineOption option) {
    if (option.getShortName() != null) {
      for (CommandLineOption o : getAllOptions()) {
        if (option.getShortName().equals(o.getShortName())) {
          throw new IllegalArgumentException("Short name \"" + option.getShortName() + "\" already used by option \"" + o.getLongName() + "\"!");
        }
      }
    }
    commandLineItems.add(option);
    return this;
  }

  public CommandLineParser add(Separator item) {
    commandLineItems.add(item);
    return this;
  }

  public CommandLineParser add(String spec) throws IllegalArgumentException {
    return add(new CommandLineOption(spec));
  }

  public CommandLineParser add(String spec, String helpText) throws IllegalArgumentException {
    return add(new CommandLineOption(spec, helpText));
  }

  public CommandLineParser add(String spec, String helpText, String propertyName) throws IllegalArgumentException {
    return add(new CommandLineOption(spec, helpText, propertyName));
  }

  public CommandLineParser add(String spec, String helpText, boolean mandatory) throws IllegalArgumentException {
    return add(new CommandLineOption(spec, helpText, null, mandatory));
  }

  public CommandLineParser add(String spec, String helpText, String propertyName, String defaultValue) throws IllegalArgumentException {
    return add(new CommandLineOption(spec, helpText, propertyName, defaultValue));
  }

  public CommandLineParser add(String spec, String helpText, String propertyName, boolean mandatory) throws IllegalArgumentException {
    return add(new CommandLineOption(spec, helpText, propertyName, mandatory));
  }

  public CommandLineParser linefeed() {
    return add(new Separator());
  }

  public CommandLineParser line() {
    return add(new Separator(true));
  }

  public CommandLineParser text(String text) {
    return add(new Separator(text));
  }

  public CommandLineParser section(String title) {
    return section(title, false);
  }

  public CommandLineParser section(String title, boolean underline) {
    add(new Separator());
    add(new Separator(title + ":"));
    if (underline) {
      add(new Separator(true));
    }
    return this;
  }

  public ParsedCommandLine parse(String[] args) throws CommandLineException {
    ParsedCommandLine result    = new ParsedCommandLine();

    boolean           parseArgs = true;
    CommandLineOption option    = null;
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if ("--".equals(arg)) {
        parseArgs = false;
        continue;
      }
      if (parseArgs) {
        if (arg.startsWith("-")) {
          if (option != null) {
            if (option.requiresValue()) {
              throw new CommandLineException("Option --" + option.getLongName() + " requires value!");
            }
            if (option.hasOptionalValue && !option.hasValues()) {
              option.addValue(null);
            }
          }
          String value  = null;

          int    sepIdx = arg.indexOf(keyValueSeparator);
          if (sepIdx > 0) {
            value = arg.substring(sepIdx + 1);
            arg = arg.substring(0, sepIdx);
          }
          if (arg.startsWith("--") || arg.length() == 2) {
            option = getOptionFor(arg);
            result.put(option.getLongName(), option);
          } else {
            // bundled options
            for (int n = 1; n < arg.length(); n++) {
              option = getOptionFor("-" + arg.charAt(n));
              if (option.isIncrementable) {
                if (!result.containsKey(option.getLongName())) {
                  result.put(option.getLongName(), option);
                  option.setValue("0");
                }
                int v = Integer.parseInt(option.getValue());
                option.setValue(String.valueOf(++v));

              } else {
                result.put(option.getLongName(), option);
              }
              // all but last option in bundle must not require a value
              if (n == (arg.length() - 1) && option.requiresValue()) {
                throw new CommandLineException("Option --" + option.getLongName() + " requires value!");
              }
            }
          }

          if (value != null) {
            option.addValue(value);
            option = null;
          } else if (!option.takesValue()) {
            option = null;
          }
          continue;
        }
        if (option != null && option.takesValue()) {
          option.addValue(arg);
          option = null;
          continue;
        }
      }
      // if (option != null && option.requiresValue() && !option.hasValues()) { throw
      // new CommandLineException("Option --" +
      // option.getLongName() + " requires value!"); }

      if (hasMandatoryArgs || hasOptionalArgs) {
        result.addCmdLineParameter(arg);
      }
    }
    if (option != null && option.requiresValue() && !option.hasValues()) {
      throw new CommandLineException("Option --" + option.getLongName() + " requires value!");
    }
    if (option != null && option.hasOptionalValue && !option.hasValues()) {
      // last arg takes optional arg, so add null value
      option.addValue(null);
    }
    // check for missing mandatory options and add all options with default values
    // to result
    for (CommandLineOption o : getAllOptions()) {
      if (o.isMandatory() && !result.containsKey(o.getLongName())) {
        throw new CommandLineException("Missing mandatory option --" + o.getLongName() + "!");
      }
      if (!o.wasInTheCommandLine() && o.getDefaultValue() != null) {
        result.put(o.getLongName(), o);
      }
    }

    // check if mandatory arguments were passed in the command line:
    if (hasMandatoryArgs && !result.hasCmdLineParameters()) {
      throw new CommandLineException("Missing mandatory argument(s)!");
    }
    // and vice versa: arguments were passed but program does not allow any...
    if (result.hasCmdLineParameters() && !hasMandatoryArgs && !hasOptionalArgs) {
      throw new CommandLineException("Programm does not take argument(s)!");
    }
    return result;
  }

  private CommandLineOption getOptionFor(String option) throws CommandLineException {
    CommandLineOption foundOption = null;
    String            key;
    if (!option.startsWith("-")) {
      throw new CommandLineException("Options must start with - character!");
    }
    int                  i = 1;
    CommandLineException e = null;
    while (i < option.length() && option.charAt(i) == '-') {
      i++;
    }
    key = option.substring(i);
    for (CommandLineOption o : getAllOptions()) {
      if ((key.length() == 1 && key.equals(o.getShortName())) || (key.equals(o.getLongName()))
          || (o.isNegatable() && key.startsWith("no-") && o.getLongName().equals(key.substring(3)))) {
        foundOption = o;
        e = null;
        break;
      }
      if (o.getLongName().startsWith(key) || (o.isNegatable && key.startsWith("no-") && o.getLongName().startsWith(key.substring(3)))) {
        if (foundOption != null) {
          e = new CommandLineException("Ambigous option: " + option + " (--" + foundOption.getLongName() + ", --" + o.getLongName() + ")!");
        }
        foundOption = o;
        if (!o.takesValue()) {
          o.allValues.add(key.startsWith("no-") ? "false" : "true");
        }
      }
    }
    if (e != null)
      throw e;

    if (foundOption == null) {
      throw new CommandLineException("Invalid option: " + option + "!");
    }
    foundOption.setWasInTheCommandLine(true);
    return foundOption;
  }

  public void printHelp() {
    printHelp(System.out);
  }

  public void printHelp(PrintStream out) {
    if (programName != null) {
      out.print("Usage: " + programName + " [options]");
      if (hasMandatoryArgs) {
        out.println(" <args>");
      } else if (hasOptionalArgs) {
        out.println(" [<args>]");
      } else {
        out.println("");
      }
      out.println("");
    }
    if (programDescription != null) {
      out.println(programDescription);
      out.println("");
    }
    for (CommandLineItem item : commandLineItems) {
      out.println(item.getHelpLine());
    }
    if (programHelp != null) {
      out.println("");
      out.println(programHelp);
      out.println("");
    }
  }

  // here ends the main class CommandLineParser

  public interface CommandLineItem {
    public String getHelpLine();
  }

  public static class Separator implements CommandLineItem {
    private final String        text;
    private final static String SEPARATOR_LINE = "----------------------------------------------------------------------------------------";

    protected Separator(boolean separatorLine) {
      this(SEPARATOR_LINE);
    }

    protected Separator() {
      this(null);
    }

    protected Separator(String text) {
      this.text = text;
    }

    @Override
    public String getHelpLine() {
      if (text == null) {
        return "";
      } else {
        return text;
      }
    }

  }

  /**
   * Command line option container. Uses perl Getopt::Long style option specifications (not all Getopt::Long features are implemented though...)
   * 
   * @author agaffke
   *
   */
  public static class CommandLineOption implements CommandLineItem {
    private static final int     OPTION_OUTPUT_LENGTH = 30;
    private static final Pattern SPEC_PATTERN         = Pattern.compile("^([^:=|+!]+)\\|?([A-Z0-9_-]?)(?:([:=!+])([snib]?)([@]?))?$", Pattern.CASE_INSENSITIVE);

    private List<String>         allValues            = new ArrayList<>();
    private boolean              wasInTheCommandLine;

    private String               shortName;
    private String               longName;
    private String               helpText;
    private String               defaultValue;
    private String               propertyName;
    private boolean              allowsMultipleValues;
    private boolean              mandatory            = false;
    private boolean              hasValue             = false;
    private boolean              hasOptionalValue     = false;
    private boolean              isNegatable          = false;
    private boolean              isIncrementable      = false;
    private ARGUMENT_TYPE        argumentType         = ARGUMENT_TYPE.NONE;

    public CommandLineOption(String spec) throws IllegalArgumentException {
      this(spec, null, null, false);
    }

    public CommandLineOption(String spec, String helpText) throws IllegalArgumentException {
      this(spec, helpText, null, false);
    }

    public CommandLineOption(String spec, String helpText, String propertyName) throws IllegalArgumentException {
      this(spec, helpText, propertyName, false);
    }

    public CommandLineOption(String spec, String helpText, String propertyName, boolean mandatory) throws IllegalArgumentException {
      this(spec, helpText, propertyName, null);
      this.mandatory = mandatory;
    }

    public CommandLineOption(String spec, String helpText, String propertyName, String defaultValue) throws IllegalArgumentException {
      parseOptionSpec(spec);
      this.helpText = helpText;
      this.propertyName = propertyName;
      this.defaultValue = defaultValue;
    }

    public boolean hasValues() {
      return !allValues.isEmpty();
    }

    public String getValue() {
      if (hasValues()) {
        return allValues.get(0);
      } else if (!hasValue && wasInTheCommandLine) {
        switch (getArgumentType()) {
        case BOOLEAN:
          return "true";
        case INTEGER:
        case NUMBER:
          return "0";
        case STRING:
          return "";
        default:
          return "true";
        }
      } else if (defaultValue != null) {
        return defaultValue;
      }
      return null;
    }

    protected void setValue(String val) {
      allValues.set(0, val);
    }

    public List<String> getValues() {
      return new ArrayList<>(allValues);
    }

    public boolean isMandatory() {
      return mandatory;
    }

    public boolean takesValue() {
      return hasValue || hasOptionalValue;
    }

    public boolean requiresValue() {
      return hasValue;
    }

    public ARGUMENT_TYPE getArgumentType() {
      return argumentType;
    }

    public boolean wasInTheCommandLine() {
      return wasInTheCommandLine;
    }

    public void setWasInTheCommandLine(boolean wasInTheCommandLine) {
      this.wasInTheCommandLine = wasInTheCommandLine;
    }

    public String getShortName() {
      return shortName;
    }

    public String getLongName() {
      return longName;
    }

    public boolean isNegatable() {
      return isNegatable;
    }

    public boolean isIncrementable() {
      return isIncrementable;
    }

    public String getHelpText() {
      return helpText;
    }

    public String getPropertyName() {
      if (propertyName != null) {
        return propertyName;
      } else {
        return longName;
      }
    }

    public String getDefaultValue() {
      return defaultValue;
    }

    public void setDefaultValue(String val) {
      defaultValue = val;
    }

    public boolean isAllowsMultipleValues() {
      return allowsMultipleValues;
    }

    public void addValue(String value) throws CommandLineException {
      if (value == null && hasValue) {
        throw new CommandLineException("Option --" + longName + " requires value!");
      }
      if (value != null && !hasValue && !hasOptionalValue) {
        throw new CommandLineException("Option --" + longName + " does not take value(s)!");
      }

      if (allValues.isEmpty() || allowsMultipleValues) {
        allValues.add(value);
      } else {
        throw new CommandLineException("No multiple values allowed for option --" + longName);
      }
    }

    @Override
    public String getHelpLine() {
      StringBuilder result = new StringBuilder("  ");
      if (shortName != null) {
        result.append('-').append(shortName);
        if (longName != null) {
          result.append(", ");
        }
      }

      if (longName != null) {
        result.append("--").append(longName);
        if (isNegatable) {
          result.append(", --no-").append(longName);
        }
      }

      if (allowsMultipleValues) {
        result.append("@");
      }

      if (hasValue) {
        result.append(" <");
      }
      if (hasOptionalValue) {
        result.append(" [");
      }
      switch (argumentType) {
      case BOOLEAN:
        result.append("true|false");
        break;
      case INTEGER:
      case NUMBER:
        result.append("n");
        break;
      case STRING:
        result.append("str");
        break;
      default:
        break;
      }

      if (hasValue) {
        result.append(">");
      }
      if (hasOptionalValue) {
        result.append("]");
      }

      if (defaultValue != null) {
        result.append(" (=").append(defaultValue).append(")");
      } else {
        Config cfg = Config.getConfig();
        if (cfg.hasProperty(getPropertyName())) {
          result.append(" (=").append(StringUtils.expandVariables(cfg.getString(getPropertyName()))).append(")");
        }
      }

      if (helpText != null) {
        int i = (OPTION_OUTPUT_LENGTH - result.length());
        if (i <= 0) {
          result.append(StringUtils.LF);
          i = OPTION_OUTPUT_LENGTH;
        }

        for (; i > 0; i--)
          result.append(' ');
        result.append(helpText);
      }

      return result.toString();
    }

    @Override
    public String toString() {
      return "CommandLineOption [allValues=" + allValues + ", wasInTheCommandLine=" + wasInTheCommandLine + ", shortName=" + shortName + ", longName="
          + longName + ", helpText=" + helpText + ", defaultValue=" + defaultValue + ", allowsMultipleValues=" + allowsMultipleValues + ", mandatory="
          + mandatory + ", hasValue=" + hasValue + ", hasOptionalValue=" + hasOptionalValue + ", argumentType=" + argumentType + "]";
    }

    private void parseOptionSpec(String spec) throws IllegalArgumentException {
      Matcher m = SPEC_PATTERN.matcher(spec);
      if (!m.matches()) {
        throw new IllegalArgumentException("Invalid option spec: " + spec);
      }

      this.longName = m.group(1);
      if (!m.group(2).isEmpty()) {
        this.shortName = m.group(2);
      }
      if ("=".equals(m.group(3))) {
        this.hasValue = true;
      } else if (":".equals(m.group(3))) {
        this.hasOptionalValue = true;
      } else if ("!".equals(m.group(3))) {
        this.isNegatable = true;
      } else if ("+".equals(m.group(3))) {
        this.isIncrementable = true;
      }

      if (hasValue || hasOptionalValue) {
        switch (m.group(4).charAt(0)) {
        case 's':
          this.argumentType = ARGUMENT_TYPE.STRING;
          isIncrementable = false;
          break;
        case 'i':
          this.argumentType = ARGUMENT_TYPE.INTEGER;
          break;
        case 'n':
          this.argumentType = ARGUMENT_TYPE.NUMBER;
          break;
        case 'b':
          this.argumentType = ARGUMENT_TYPE.BOOLEAN;
          isIncrementable = false;
          break;
        default:
          throw new IllegalArgumentException("Invalid argument type spec: " + spec.charAt(0) + " in [" + spec + "]");
        }
        if ("@".equals(m.group(5))) {
          this.allowsMultipleValues = true;
        }
      }
    }
  }

  /**
   * Container for command line options and arguments as hash map.
   * 
   * @author agaffke
   *
   */

  public static class ParsedCommandLine extends LinkedHashMap<String, CommandLineOption> {
    private static final long  serialVersionUID  = 7963583632104893466L;
    private final List<String> cmdLineParameters = new ArrayList<>();

    protected void addCmdLineParameter(String par) {
      if (par != null) {
        cmdLineParameters.add(par);
      }
    }

    public Collection<String> getcmdLineParameters() {
      return Collections.unmodifiableCollection(cmdLineParameters);
    }

    public boolean hasCmdLineParameters() {
      return !cmdLineParameters.isEmpty();
    }

    public boolean isSet(String option) {
      return get(option) != null && get(option).wasInTheCommandLine();
    }
  }

  public static class CommandLineException extends Exception {
    private static final long serialVersionUID = 1162250977171159476L;

    public CommandLineException(String msg) {
      super(msg);
    }

    public CommandLineException(String msg, Throwable e) {
      super(msg, e);
    }

    public CommandLineException(Throwable e) {
      super(e);
    }
  }
}
