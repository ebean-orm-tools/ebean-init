package io.ebean.tools.init;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class LoggingFileWriterTest {

  @Test
  public void writeToFile() throws IOException {

    File example = new File("src/test/resources/logging/logback-one.xml");
    LoggingFile loggingFile = new LoggingFile("logback", example);

    File out = new File("target/logback-one-2.xml");
    LoggingFileWriter writer = new LoggingFileWriter(loggingFile, out);
    writer.writeToFile();

    File compare = new File("src/test/resources/logging/logback-one-2.xml");
    assertThat(out).hasSameContentAs(compare);
  }

}
