package io.ebean.tools.init.action;

import io.ebean.tools.init.Detection;
import io.ebean.tools.init.util.FileCopy;
import io.ebean.tools.init.InteractionHelp;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DoAddTestResource {

  private final Detection detection;

  private final InteractionHelp help;

  public DoAddTestResource(Detection detection, InteractionHelp help) {
    this.detection = detection;
    this.help = help;
  }

  public void addApplicationTestYml() {
    if (add("application-test.yml", "/tp-application-test.yml")) {
      detection.addedTestProperties();
    }
  }

  public void addLogbackTest() {
    if (add("logback-test.xml", "/tp-logback-test.xml")) {
      detection.addedTestLogging();
    }
  }

  private boolean add(String destination, String sourceResource) {
    List<String> testResourceDirs = detection.getMeta().getTestResources();
    if (testResourceDirs.isEmpty()) {
      help.acknowledge("  Unsuccessful - could not determine the test resources directory, maybe it does not exist yet?");

    } else {
      File testRes = new File(testResourceDirs.get(0));
      if (!testRes.exists() && testRes.isDirectory()) {
        throw new IllegalStateException("Expected test resource directory at " + testRes.getAbsolutePath());
      }
      File file = copyTestProperties(testRes, destination, sourceResource);
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
