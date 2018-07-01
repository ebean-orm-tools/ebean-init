package io.ebean.tools.init.addprops;

import io.ebean.tools.init.Detection;
import io.ebean.tools.init.InteractionHelp;
import io.ebean.tools.init.util.FileCopy;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DoAddMainProperties {

  private final Detection detection;

  private final InteractionHelp help;

  public DoAddMainProperties(Detection detection, InteractionHelp help) {
    this.detection = detection;
    this.help = help;
  }

  public void run() {
    List<String> mainResourceDirs = detection.getMeta().getMainResources();
    if (mainResourceDirs.isEmpty()) {
      help.acknowledge("  Unsuccessful - could not determine the resources directory");

    } else {
      File mainRes = new File(mainResourceDirs.get(0));
      if (!mainRes.exists() && mainRes.isDirectory()) {
        throw new IllegalStateException("Expected resource directory at " + mainRes.getAbsolutePath());
      }
      File file = copyProperties(mainRes);
      if (file != null) {
        help.acknowledge("  ... added " + file.getAbsolutePath());
        detection.addedTestProperties();
      }
    }
  }

  private File copyProperties(File mainRes) {

    if (!mainRes.exists() && !mainRes.mkdirs()) {
      throw new RuntimeException("Could not create src main resources ?");

    } else {
      try {
        File props = new File(mainRes, "application.yml");
        if (props.exists()) {
          throw new RuntimeException(props.getAbsolutePath() + " already exists? leaving as is.");

        } else {
          FileCopy.copy(props, "/tp-application.yml");
          return props;
        }
      } catch (IOException e) {
        throw new RuntimeException("Failed to copy application.yml", e);
      }
    }
  }

}
