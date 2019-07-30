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

  public DoAddManifest(InteractionHelp help) {
    this.help = help;
    this.detection = help.detection();
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
      help.ackErr("Unsuccessful - could not determine the resources directory?");

    } else {
      try {
        File file = new File(resourceDir, "ebean.mf");
        FileWriter writer = new FileWriter(file);

        Actions actions = help.actions();
        String entityPkg = actions.getManifestEntityPackage();
        String transPkg = actions.getManifestTransactionalPackage();
        String queryPkg = actions.getManifestQueryBeanPackage();

        if (hasValue(entityPkg)) {
          writer.append("entity-packages: ").append(entityPkg).append("\n");
        }
        if (hasValue(transPkg)) {
          writer.append("transactional-packages: ").append(transPkg).append("\n");
        }
        if (hasValue(queryPkg)) {
          writer.append("querybean-packages: ").append(queryPkg).append("\n");
        }
        writer.append("profile-location: true").append("\n");
        writer.append("\n");
        writer.flush();
        writer.close();

        help.ackDone("... added ebean.mf");
        detection.addedEbeanManifest();

      } catch (IOException e) {
        throw new RuntimeException("Failed to write ebean.mf", e);
      }
    }
  }

  private boolean hasValue(String queryPkg) {
    return queryPkg != null && !queryPkg.trim().isEmpty();
  }
}
