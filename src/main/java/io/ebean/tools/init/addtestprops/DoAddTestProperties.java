package io.ebean.tools.init.addtestprops;

import io.ebean.tools.init.Detection;
import io.ebean.tools.init.util.FileCopy;
import io.ebean.tools.init.InteractionHelp;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DoAddTestProperties {

  private final Detection detection;

  private final InteractionHelp help;

  public DoAddTestProperties(Detection detection, InteractionHelp help) {
    this.detection = detection;
    this.help = help;
  }

  public void run() {
    List<String> testResourceDirs = detection.getMeta().getTestResources();
    if (testResourceDirs.isEmpty()) {
      help.acknowledge("  Unsuccessful - could not determine the test resources directory");

    } else {
      File testRes = new File(testResourceDirs.get(0));
      if (!testRes.exists() && testRes.isDirectory()) {
        throw new IllegalStateException("Expected test resource directory at " + testRes.getAbsolutePath());
      }
      File file = copyTestProperties(testRes);
      if (file != null) {
        help.acknowledge("  ... added " + file.getAbsolutePath());
        detection.addedTestProperties();
      }
    }
  }


  File copyTestProperties(File testResources) {

    if (!testResources.exists() && !testResources.mkdirs()) {
      throw new RuntimeException("Could not create src test resources ?");

    } else {
      try {
        File testProps = new File(testResources, "test-ebean.properties");
        if (testProps.exists()) {
          throw new RuntimeException(testProps.getAbsolutePath() + " already exists? leaving as is.");

        } else {
          FileCopy.copy(testProps, "/tp-test-ebean.properties");
          return testProps;
        }
      } catch (IOException e) {
        throw new RuntimeException("Failed to copy test-ebean.properties", e);
      }
    }
  }

}
