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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

/**
 * Helper thread for reading STDOUT and STDERR from ProcessBuilder (or other).
 * 
 * @author agaffke
 * 
 */
public class StreamOutputCollector extends Thread {
  public static final String LINE_DELIMETER = System.getProperty("line.separator");

  InputStream                is;
  StringBuilder              sb;
  String                     tag;
  Consumer<String>           consumer;
  boolean                    hasOutput;

  /**
   * Default constructor for a collector that aggregates data from InputStream is.
   * 
   * @param is
   */
  public StreamOutputCollector(InputStream is) {
    this(is, new StringBuilder(), null);
  }

  /**
   * Default constructor for a collector that aggregates data from InputStream is
   * and appends it to the given StringBuilder.
   * 
   * @param is
   * @param collector
   */
  public StreamOutputCollector(InputStream is, StringBuilder collector) {
    this(is, collector, null);
  }

  public StreamOutputCollector(InputStream is, Consumer<String> consumer) {
    this.is = is;
    this.consumer = consumer;
  }

  /**
   * Creates collector that inserts <tag>: before every line read, if tag is not
   * null.
   * 
   * @param is
   * @param tag
   */
  public StreamOutputCollector(InputStream is, String tag) {
    this(is, new StringBuilder(), tag);
  }

  public StreamOutputCollector(InputStream is, boolean collect, String tag) {
    this(is, (collect ? new StringBuilder() : null), tag);
  }

  /**
   * Creates collector that appends collected lines to given StringBuilder.
   * 
   * @param is
   * @param collect
   * @param string
   */
  public StreamOutputCollector(InputStream is, StringBuilder collector, String tag) {
    this.is = is;
    this.tag = tag;
    this.hasOutput = false;
    this.sb = collector;
    if (tag == null) {
      this.setName("collector-anon-" + this.getId());
    } else {
      this.setName("collector-" + tag + "-" + this.getId());
    }

  }

  /**
   * Gets collected output as String.
   * 
   * @return
   */
  public String getOutput() {
    if (sb != null)
      return sb.toString();
    return null;
  }

  /**
   * true if data has been read from InputStream (even empty lines)
   * 
   * @return
   */
  public boolean hasOutput() {
    return this.hasOutput;
  }

  @Override
  public void run() {
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    if (consumer != null) {
      br.lines().forEach(consumer);
    } else {
      String buf;
      try {
        while ((buf = br.readLine()) != null) {
          if (sb != null) {
            hasOutput = true;
            // if (tag != null) {
            // sb.append(tag).append(": ");
            // }
            sb.append(buf).append(LINE_DELIMETER);
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
