package io.ebean.tools.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger log = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {

    DetectionMeta meta = new ProjectDetection().run();

    try {
      Detection detection = new Detection(meta);
      detection.run();

      new Interaction(detection).run();

    } catch (Exception e) {
      log.error("Error running detection on project", e);
    }

  }
}
