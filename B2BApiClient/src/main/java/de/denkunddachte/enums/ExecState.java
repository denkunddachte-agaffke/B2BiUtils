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

public enum ExecState {
  SUCCESS("Success"), ERROR("Error"), WAITING_FOR_IO("Waiting_on_IO"), SVC_CFG_ERROR("Service configuration error"), INTERRUPTED("Interrupted"), SYSTEM_ERROR(
      "System error"), TERMINATED("terminated"), WARNING("Warning"), WAITING("Waiting"), WFE_HALTED("WFE Halted"), WFE_SYSERROR(
          "WFE System Error"), CFG_ERROR("Configuration Error"), INTERRUPTED_AUTO("Interrupted (auto)"), INTERRUPTED_MAN("Interrupted_Man");

  private final String display;

  private ExecState(String display) {
    this.display = display;
  }

  public String getDisplay() {
    return display;
  }

  public static ExecState getState(String val) {
    for (ExecState v : ExecState.values()) {
      if (v.display.equalsIgnoreCase(val))
        return v;
    }
    throw new IllegalArgumentException("No ExecState \"" + val + "\"!");
  }
}
