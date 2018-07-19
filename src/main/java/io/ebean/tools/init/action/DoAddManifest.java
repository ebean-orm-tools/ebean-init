package io.ebean.tools.init.action;

import io.ebean.tools.init.Actions;
import io.ebean.tools.init.Detection;
import io.ebean.tools.init.InteractionHelp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DoAddManifest {

  private final Detection detection;
  private final InteractionHelp help;

  public DoAddManifest(Detection detection, InteractionHelp help) {
    this.detection = detection;
    this.help = help;
  }

  public void run() {
    help.questionEntityBeanPackage();
    if (help.isContinue()) {
      help.questionTransactionalPackage();
    }
    if (help.isContinue()) {
      help.questionQueryBeanPackage();
    }
    if(help.isContinue()) {
      // write the ebean.mf
      writeManifest();
    }
  }

  private void writeManifest() {

    File resourceDir = detection.getMeta().getMainResource();
    if (resourceDir == null || !resourceDir.exists()) {
      help.acknowledge("  Unsuccessful - could not determine the resources directory?");

    } else {
      try {
        File file = new File(resourceDir, "ebean.mf");
        FileWriter writer = new FileWriter(file);

        Actions actions = help.actions();
        String entityPkg = actions.getManifestEntityPackage();
        String transPkg = actions.getManifestTransactionalPackage();
        String queryPkg = actions.getManifestQueryBeanPackage();

        writer.append("entity-packages: ").append(entityPkg).append("\n");
        writer.append("transactional-packages: ").append(transPkg).append("\n");
        writer.append("querybean-packages: ").append(queryPkg).append("\n");
        writer.append("\n");
        writer.flush();
        writer.close();

        help.ackDone("  ... added " + file.getAbsolutePath());
        detection.addedEbeanManifest();

      } catch (IOException e) {
        throw new RuntimeException("Failed to write ebean.mf", e);
      }
    }
  }
}
