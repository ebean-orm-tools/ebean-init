package io.ebean.tools.init.action;

import io.ebean.tools.init.Detection;
import io.ebean.tools.init.InteractionHelp;
import io.ebean.tools.init.util.FileCopy;

import java.io.File;
import java.io.IOException;

public class DoAddMainProperties {

  private final Detection detection;

  private final InteractionHelp help;

  public DoAddMainProperties(Detection detection, InteractionHelp help) {
    this.detection = detection;
    this.help = help;
  }

  public void run() {
    File mainResource = detection.getMeta().getMainResource();
    if (mainResource == null || !mainResource.exists()) {
      help.acknowledge("  Unsuccessful - could not determine the main resources directory");

    } else {
      File file = copyProperties(mainResource);
      if (file != null) {
        help.ackDone("  ... added " + file.getAbsolutePath());
        detection.addedMainProperties();
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
