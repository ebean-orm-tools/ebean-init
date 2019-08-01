package io.ebean.tools.init;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class LoggingFileWriter {

  private static final String[] logEntries = {"io.ebean.docker","io.ebean.DDL","io.ebean.SQL","io.ebean.TXN","io.ebean.cache"};

  private final String newLine = "\n";

  private final LoggingFile loggingFile;
  private final FileWriter writer;

  private final boolean logback;
  private final String detectInsertContent;

  public LoggingFileWriter(LoggingFile loggingFile) throws IOException {
    this(loggingFile, loggingFile.getSourceFile());
  }

  LoggingFileWriter(LoggingFile loggingFile, File writer) throws IOException {
    this.loggingFile = loggingFile;
    this.writer = new FileWriter(writer);
    this.logback = "logback".equals(loggingFile.getType());
    this.detectInsertContent = logback ? "</configuration>" : "</Loggers>";
  }

  public void writeToFile() throws IOException {

    final List<String> lines = loggingFile.getLines();
    for (String line : lines) {
      writeLine(line);
    }
    writer.close();
  }

  private void writeLine(String line) throws IOException {

    if (line.contains(detectInsertContent)) {
      insertLoggingEntries();
    }
    writer.append(line).append(newLine);
  }

  private void insertLoggingEntries() throws IOException {
    if (logback) {
      insertLogbackEntries();
    } else {
      insertLog4jEntries();
    }
  }

  private void insertLog4jEntries() throws IOException {

    for (String logEntry : logEntries) {
      writer.append("    <Logger name=\"")
        .append(logEntry).append("\" level=\"TRACE\"/>")
        .append(newLine);
    }
  }

  private void insertLogbackEntries() throws IOException {

    for (String logEntry : logEntries) {
      writer.append("  <logger name=\"")
        .append(logEntry).append("\" level=\"TRACE\"/>")
        .append(newLine);
    }
  }
}
