/*
  Copyright 2022 denk & dachte Software GmbH

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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;

public abstract class FileUtil {

  public static final File WORKDIR = new File(System.getProperty("workdir", System.getProperty(Config.PROP_INSTALLDIR, System.getProperty("user.dir"))));
  private static final String DUMMYROOT = "@RooT@";

  private FileUtil() {
  }

  public static File getFile(final String path) {
    if (path == null) {
      return null;
    }

    if (path.startsWith("/") || path.charAt(1) == ':') {
      return new File(path);
    }
    return new File(WORKDIR, path);
  }

  public static String getAbsolutePath(final String path) {
    if (path == null) {
      return null;
    }

    if (path.startsWith("/") || path.charAt(1) == ':') {
      return path;
    }
    int pos = 0;
    if (path.length() > 1 && path.charAt(0) == '.' && (path.charAt(1) == '\\' || path.charAt(1) == '/'))
      pos = 2;
    return WORKDIR.getAbsolutePath() + File.separator + path.substring(pos);
  }

  public static String basename(final String path) {
    if (path == null) {
      return null;
    }
    int end = path.length() - 1;
    for (; path.charAt(end) == '/' || path.charAt(end) == '\\'; end--) {
    }

    int start = path.replace('\\', '/').lastIndexOf('/', end) + 1;
    return path.substring(start, end + 1);
  }

  public static String dirname(String path) {
    return dirname(path, null);
  }

  public static String dirname(String path, String emptyPath) {
    if (path == null) {
      return emptyPath;
    }
    if (path.startsWith("/"))
      emptyPath = "/";
    path = path.replaceAll("([/\\\\])\\1+", "$1").replaceAll("[/\\\\]$", "");
    final int pos = path.replace('\\', '/').lastIndexOf('/');
    if (pos > 0) {
      return path.substring(0, pos);
    } else {
      return emptyPath;
    }
  }

  public static File getWorkdir() {
    return WORKDIR;
  }

  public static String commonAncestor(Collection<String> paths) {
    if (paths == null || paths.size() == 0)
      return null;
    Iterator<String> pi = paths.iterator();
    // prepend a DUMMYROOT directory to avoid interpretation as UNC paths on Windows
    Path path1 = Paths.get(DUMMYROOT, pi.next()).normalize();
    while (pi.hasNext()) {
      Path path2 = Paths.get(DUMMYROOT, pi.next()).normalize();
      if (path1.equals(path2)) {
        continue;
      }
      int minCount = Math.min(path1.getNameCount(), path2.getNameCount());
      for (int i = minCount; i > 0; i--) {
        Path sp1 = path1.subpath(0, i);
        if (sp1.equals(path2.subpath(0, i))) {
          path1 = Paths.get((sp1.toString().startsWith(DUMMYROOT) ? "" : DUMMYROOT), sp1.toString()).normalize();
          break;
        }
      }
    }
    return (path1.getNameCount() > 1 ? path1.toString().substring(DUMMYROOT.length()).replace('\\', '/') : "/");
  }
}

