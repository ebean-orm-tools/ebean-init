package io.ebean.tools.init;

import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class LoggingFileTest {

  @Test
  public void basic() {

    File example = new File("src/test/resources/logging/logback-one.xml");
    LoggingFile loggingFile = new LoggingFile("logging", example);

    assertThat(loggingFile.containsEbeanEntry()).isFalse();
    assertThat(loggingFile.getType()).isEqualTo("logging");
  }

  @Test
  public void withEbean() {

    File example = new File("src/test/resources/logging/logback-withEbean.xml");
    LoggingFile loggingFile = new LoggingFile("logging", example);

    assertThat(loggingFile.containsEbeanEntry()).isTrue();
    assertThat(loggingFile.getType()).isEqualTo("logging");
  }
}
