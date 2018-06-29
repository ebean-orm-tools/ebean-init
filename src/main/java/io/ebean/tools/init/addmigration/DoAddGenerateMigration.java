package io.ebean.tools.init.addmigration;

import io.ebean.tools.init.Detection;
import io.ebean.tools.init.util.FileCopy;
import io.ebean.tools.init.InteractionHelp;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DoAddGenerateMigration {

  private final Detection detection;
  private final InteractionHelp help;

  public DoAddGenerateMigration(Detection detection, InteractionHelp help) {
    this.detection = detection;
    this.help = help;
  }

  public void run() {

    List<String> testSourceRoots = detection.getMeta().getTestSource();
    if (testSourceRoots.isEmpty()) {
      help.acknowledge("  Unsuccessful - can not determine test source root");
      return;
    }

    File dir = new File(testSourceRoots.get(0));
    if (!dir.exists()) {
      throw new RuntimeException("test source root does not exist? "+dir.getAbsolutePath());
    }

    File testMain = new File(dir, "main");
    if (!testMain.exists() && !testMain.mkdirs()) {
      throw new RuntimeException("Failed to make test main directory - " + testMain.getAbsolutePath());
    }

    if (detection.isKotlinInClassPath()) {
      try {
        File mig = new File(testMain, "GenerateDbMigration.kt");
        FileCopy.copy(mig, "/tp-GenerateDbMigration.kt");
        help.acknowledge("  ... added " + mig.getAbsolutePath());
        detection.addedGenerateMigration("GenerateDbMigration.kt");

      } catch (IOException e) {
        throw new RuntimeException("Failed to copy GenerateDbMigration.kt", e);
      }
    } else {
      try {
        File mig = new File(testMain, "GenerateDbMigration.java");
        FileCopy.copy(mig, "/tp-GenerateDbMigration.java");
        help.acknowledge("  ... added " + mig.getAbsolutePath());
        detection.addedGenerateMigration("GenerateDbMigration.java");

      } catch (IOException e) {
        throw new RuntimeException("Failed to copy GenerateDbMigration.java", e);
      }
    }
  }
}
