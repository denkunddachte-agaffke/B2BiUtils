/*
  Copyright 2023 denk & dachte Software GmbH

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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class FileFind {

  private FileFind() {
    // static
  }

  public static Collection<Path> find(String rootDir, String fileNamePattern) throws IOException {
    return find(rootDir, fileNamePattern, false, null);
  }

  public static Collection<Path> find(String rootDir, String fileNamePattern, boolean ignoreCase) throws IOException {
    return find(rootDir, fileNamePattern, ignoreCase, null);
  }

  public static Collection<Path> find(String rootDir, String fileNamePattern, boolean ignoreCase, Pattern ignorePattern) throws IOException {
    FilenameFilterVisitor list = new FilenameFilterVisitor(fileNamePattern, ignoreCase);
    list.setIgnorePattern(ignorePattern);
    Files.walkFileTree(Paths.get(rootDir), list);
    return list.getResult();
  }

  private static final class FilenameFilterVisitor extends SimpleFileVisitor<Path> {
    private PathMatcher      matcher;
    private boolean          ignoreCase;
    private final List<Path> result = new ArrayList<>();
    private Pattern          ignorePattern;

    public FilenameFilterVisitor(String glob, boolean ignoreCase) {
      if (glob != null) {
        this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + (ignoreCase ? glob.toLowerCase() : glob));
        this.ignoreCase = ignoreCase;
      }
    }

    public void setIgnorePattern(Pattern ignoreFilenames) {
      this.ignorePattern = ignoreFilenames;
    }

    public List<Path> getResult() {
      return this.result;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      Path name = file.getFileName();
      if (name != null) {
        if (ignorePattern != null && ignorePattern.matcher(file.toString().replace('\\', '/')).matches()) {
          // ignore
        } else {
          if (ignoreCase) {
            name = Paths.get(name.toString().toLowerCase());
          }
          if (matcher.matches(name)) {
            result.add(file);
          }
        }
      }
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
      return FileVisitResult.CONTINUE;
    }
  }

}
