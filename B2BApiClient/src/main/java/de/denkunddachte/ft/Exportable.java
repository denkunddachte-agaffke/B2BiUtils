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

import java.io.PrintWriter;

import de.denkunddachte.exception.ApiException;

public interface Exportable {
  public enum Mode {
    NONE, JSON, LDIF, RAW, PROPERTIES
  }

  public abstract void export(PrintWriter os) throws ApiException;
  public abstract void export(PrintWriter os, boolean prettyPrint, boolean suppressNullValues) throws ApiException;
  public abstract String getBasename();
  public Mode getExportMode();
}
