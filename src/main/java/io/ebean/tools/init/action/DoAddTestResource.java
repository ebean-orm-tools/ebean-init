package io.ebean.tools.init.action;

import io.ebean.tools.init.Detection;
import io.ebean.tools.init.InteractionHelp;
import io.ebean.tools.init.util.FileCopy;

import java.io.File;
import java.io.IOException;

public class DoAddTestResource {

  private final Detection detection;

  private final InteractionHelp help;

  public DoAddTestResource(Detection detection, InteractionHelp help) {
    this.detection = detection;
    this.help = help;
  }

  public void addApplicationTestYml() {
    if (add("application-test.yaml", "/tp-application-test.yaml")) {
      detection.addedTestProperties();
    }
  }

  public void addLogbackTest() {
    if (add("logback-test.xml", "/tp-logback-test.xml")) {
      detection.addedTestLogging();
    }
  }

  private boolean add(String destination, String sourceResource) {
    File testResource = detection.getMeta().getTestResource();
    if (testResource == null || !testResource.exists()) {
      help.acknowledge("  Unsuccessful - could not determine the test resources directory, maybe it does not exist yet?");

    } else {
      File file = copyTestProperties(testResource, destination, sourceResource);
      if (file != null) {
        help.ackDone("  ... added " + file.getAbsolutePath());
        return true;
      }
    }
    return false;
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
