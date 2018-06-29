package io.ebean.tools.init.other;

import io.ebean.tools.init.Detection;
import io.ebean.tools.init.DetectionMeta;
import io.ebean.tools.init.InteractionHelp;

public class DoDebug {
  private final Detection detection;
  private final InteractionHelp help;

  public DoDebug(Detection detection, InteractionHelp help) {
    this.detection = detection;
    this.help = help;
  }

  public void run() {

    DetectionMeta meta = detection.getMeta();

    help.outputDebug("main out ", meta.getMainOutput());
    help.outputDebug("main src", meta.getMainSource().toString());
    help.outputDebug("main res", meta.getMainResources().toString());

    help.outputDebug("test src", meta.getTestSource().toString());
    help.outputDebug("test res", meta.getTestResources().toString());
    help.outputDebug("test out", meta.getTestOutput());

  }

}
