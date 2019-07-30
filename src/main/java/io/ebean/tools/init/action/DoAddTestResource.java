package io.ebean.tools.init.action;

import io.ebean.tools.init.Detection;
import io.ebean.tools.init.InteractionHelp;
import io.ebean.tools.init.LoggingFile;
import io.ebean.tools.init.LoggingFileWriter;
import io.ebean.tools.init.util.FileCopy;

import java.io.File;
import java.io.IOException;

public class DoAddTestResource {

  private final Detection detection;

  private final InteractionHelp help;

  public DoAddTestResource(InteractionHelp help) {
    this.help = help;
    this.detection = help.detection();
  }

  public void addApplicationTestYml() {
    if (add("application-test.yaml", "/tp-application-test.yaml")) {
      detection.addedTestProperties();
    }
  }

  public void addLogbackTest() {
    try {
      final LoggingFile loggingFile = detection.getTestLoggingFile();
      if (loggingFile != null) {
        new LoggingFileWriter(loggingFile).writeToFile();
      } else {
        if (add("logback-test.xml", "/tp-logback-test.xml")) {
          detection.addedTestLogging();
        }
      }
    } catch (IOException e) {
      help.ackErr("... failed to update logging test file. Error: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private boolean add(String destination, String sourceResource) {
    File testResource = detection.getMeta().getTestResource();
    if (doesNotExist(testResource)) {
      String yesNo = help.askYesNo("src/test/resources does not exist, can we create it?");
      if (yesNo.equalsIgnoreCase("Yes")) {
        if (!detection.getMeta().createSrcTestResources()) {
          help.ackErr("... failed to create src/test/java directory");
        }
      }
      testResource = detection.getMeta().getTestResource();
    }

    if (doesNotExist(testResource)) {
      help.ackErr("Unsuccessful - could not determine the test resources directory, maybe it does not exist yet?");
      return false;
    }
    File file = copyTestProperties(testResource, destination, sourceResource);
    if (file != null) {
      help.ackDone("... added " + destination);
      return true;
    }

    return false;
  }

  private boolean doesNotExist(File testResource) {
    return testResource == null || !testResource.exists();
  }

  private File copyTestProperties(File testResourceDir, String destination, String sourceResource) {

    if (!testResourceDir.exists() && !testResourceDir.mkdirs()) {
      throw new RuntimeException("Could not create src test resources ?");

    } else {
      try {
        File testProps = new File(testResourceDir, destination);
        if (testProps.exists()) {
          throw new RuntimeException(testProps.getAbsolutePath() + " already exists? leaving as is.");

        } else {
          FileCopy.copy(testProps, sourceResource);
          return testProps;
        }
      } catch (IOException e) {
        throw new RuntimeException("Failed to copy " + destination, e);
      }
    }
  }

}
