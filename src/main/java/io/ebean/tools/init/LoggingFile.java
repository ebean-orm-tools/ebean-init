package io.ebean.tools.init;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

public class LoggingFile {

  private final File sourceFile;
  private final String type;
  private List<String> lines = new ArrayList<>();
  private boolean containsEbeanEntry;

  LoggingFile(String type, File sourceFile) {
    this.type = type;
    this.sourceFile = sourceFile;
    readLines(sourceFile);
  }

  File getSourceFile() {
    return sourceFile;
  }

  List<String> getLines() {
    return lines;
  }

  String getType() {
    return type;
  }

  boolean containsEbeanEntry() {
    return containsEbeanEntry;
  }

  /**
   * Search logging config file for Ebean logging entry.
   */
  private void readLines(File logback) {

    try (LineNumberReader lineReader = new LineNumberReader(new FileReader(logback))) {
      String logLine;
      while ((logLine = lineReader.readLine()) != null) {
        lines.add(logLine);
        if (containsLoggingEntry(logLine)) {
          containsEbeanEntry = true;
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private boolean containsLoggingEntry(String logLine) {
    return logLine.contains("io.ebean.SQL");
  }

}
