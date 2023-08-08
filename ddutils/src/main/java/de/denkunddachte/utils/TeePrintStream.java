/*
  Copyright 2020 denk & dachte Software GmbH

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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class TeePrintStream extends PrintStream {

  protected PrintStream copy;

  public TeePrintStream(PrintStream original, PrintStream copy, String encoding, boolean autoFlush) throws IOException {
    super(original, autoFlush, encoding);
    this.copy = copy;
  }

  public TeePrintStream(PrintStream original, PrintStream copy, boolean autoFlush) throws IOException {
    super(original, autoFlush);
    this.copy = copy;
  }

  public TeePrintStream(PrintStream original, PrintStream copy) throws IOException {
    this(original, copy, false);
  }

  public TeePrintStream(PrintStream original, String fileName, boolean append, String encoding, boolean autoFlush)
      throws IOException {
    this(original, new PrintStream(new FileOutputStream(fileName, append), autoFlush, encoding), encoding, autoFlush);
  }

  public TeePrintStream(PrintStream original, String fileName, boolean append, boolean autoFlush) throws IOException {
    this(original, new PrintStream(new FileOutputStream(fileName, append), autoFlush), autoFlush);
  }

  public TeePrintStream(PrintStream original, String fileName, boolean append) throws IOException {
    this(original, fileName, append, false);
  }

  public TeePrintStream(PrintStream original, String fileName) throws IOException {
    this(original, fileName, false, false);
  }

  @Override
  public boolean checkError() {
    return super.checkError() || copy.checkError();
  }

  @Override
  public void write(int b) {
    copy.write(b);
    super.write(b);
  }

  @Override
  public void write(byte[] buf) throws IOException {
    copy.write(buf);
    super.write(buf);
  }

  @Override
  public void write(byte[] buf, int off, int len) {
    copy.write(buf, off, len);
    super.write(buf, off, len);
  }

  @Override
  public void close() {
    close(copy);
    close(super.out);
  }
  
  private void close(OutputStream os) {
    if (!os.equals(System.out) && !os.equals(System.err)) {
      try {
        os.close();
      } catch (IOException e) {
        // IGNORE
      }
    }
  }
}
