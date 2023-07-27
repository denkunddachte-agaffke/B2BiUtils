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

import java.util.ArrayList;
import java.util.List;

public class FTProcessChain {
  private String             name;
  private final List<String> processingCommands = new ArrayList<>();

  public FTProcessChain(String name) {
    this(name, null, null);
  }

  public FTProcessChain(String name, List<String> commands, String path) {
    this.name = name;
    if (commands != null) {
      for (String cmd : commands) {
        if (cmd != null && !cmd.trim().isEmpty()) {
          cmd = cmd.trim();
          if (path != null && !cmd.toUpperCase().matches("^(?:[A-Z]:|)[\\\\/].+$")) {
            cmd = path + "/" + cmd;
          }
          processingCommands.add(cmd);
        }
      }
    }
  }

  public String getName() {
    if (name != null) {
      return name;
    } else {
      return getProcessingCommandShortName(0);
    }
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getProcessingCommands() {
    return new ArrayList<>(processingCommands);
  }

  public String getProcessingCommand(int i) {
    return processingCommands.get(i);
  }

  public String getProcessingCommandShortName(int i) {
    if (processingCommands.get(i) == null) {
      return null;
    }
    String result = processingCommands.get(i);
    int pos = result.lastIndexOf('/');
    if (pos > -1) {
      result = result.substring(pos + 1, (result.indexOf(' ') > pos + 1 ? result.indexOf(' ') : result.length()));
    }
    return result;
  }

  public boolean addProcessingCommand(String command) {
    if (command != null && !command.trim().isEmpty()) {
      return this.processingCommands.add(command);
    }
    return false;
  }

  public static String basename(String path) {
    if (path == null)
      return null;
    int p = path.lastIndexOf('/');
    if (p > -1) {
      return path.substring(p);
    }
    p = path.lastIndexOf('\\');
    if (p > -1) {
      return path.substring(p);
    }
    return path;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    for (int i = 0; i < processingCommands.size(); i++) {
      result = prime * result + (i * prime) + ((processingCommands.get(i) == null) ? 0 : basename(processingCommands.get(i)).hashCode());
    }
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FTProcessChain other = (FTProcessChain) obj;
    if (processingCommands.size() != other.processingCommands.size())
      return false;

    for (int i = 0; i < processingCommands.size(); i++) {
      if (!basename(processingCommands.get(i)).equals(basename(other.processingCommands.get(i))))
        return false;
    }

    return true;
  }

  @Override
  public String toString() {
    return "FTProcessChain [name=" + name + ", processingCommands=" + processingCommands + "]";
  }
}
