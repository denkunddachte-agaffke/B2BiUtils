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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import de.denkunddachte.exception.ApiException;
import de.denkunddachte.ft.Exportable.Mode;

public class ExportOutput implements AutoCloseable {
  private PrintWriter writer             = null;
  private Mode        mode               = Mode.NONE;
  private int         itemCount          = 0;
  private File        destination;
  private boolean     prettyPrint        = false;
  private boolean     suppressNullValues = false;

  public ExportOutput(File destination, Mode mode) throws ApiException {
    this(destination, false, false);
  }

  public ExportOutput(File destination, boolean prettyPrint, boolean suppressNullValues) throws ApiException {
    this.destination = destination;
    this.prettyPrint = prettyPrint;
    this.suppressNullValues = suppressNullValues;
    if (!destination.isDirectory()) {
      try {
        writer = new PrintWriter(new FileOutputStream(destination, false));
      } catch (FileNotFoundException e) {
        throw new ApiException(e);
      }
    }
  }

  public void exportArtifact(Exportable artifact) throws ApiException {
    mode = artifact.getExportMode();
    if (artifact == null || mode == Mode.NONE)
      return;

    if (destination.isDirectory()) {
      try (PrintWriter out = new PrintWriter(new FileOutputStream(new File(destination, artifact.getBasename() + "." + mode.name().toLowerCase())), false)) {
        artifact.export(out, prettyPrint, suppressNullValues);
      } catch (FileNotFoundException e) {
        throw new ApiException(e);
      }
    } else {
      appendArtifact(artifact);
    }
  }

  private void appendArtifact(Exportable artifact) throws ApiException {
    if (artifact == null || mode == Mode.NONE)
      return;
    itemCount++;
    switch (mode) {
    case JSON:
      if (itemCount == 1) {
        writer.append('[');
      } else {
        writer.append(',');
      }
      break;
    case LDIF:
      if (itemCount > 1)
        writer.println();
      break;
    default:
      break;
    }
    artifact.export(writer, prettyPrint, suppressNullValues);
  }

  public boolean isPrettyPrint() {
    return prettyPrint;
  }

  public void setPrettyPrint(boolean prettyPrint) {
    this.prettyPrint = prettyPrint;
  }

  public boolean isSuppressNullValues() {
    return suppressNullValues;
  }

  public void setSuppressNullValues(boolean suppressNullValues) {
    this.suppressNullValues = suppressNullValues;
  }

  public PrintWriter getWriter() {
    return writer;
  }

  public Mode getMode() {
    return mode;
  }

  public int getItemCount() {
    return itemCount;
  }

  public File getDestination() {
    return destination;
  }

  @Override
  public void close() throws ApiException {
    if (writer != null) {
      if (mode == Mode.JSON) {
        writer.append(']');
      }
      writer.close();
    }
    if (itemCount == 0 && destination.isFile()) {
      destination.delete();
    }
  }
}
