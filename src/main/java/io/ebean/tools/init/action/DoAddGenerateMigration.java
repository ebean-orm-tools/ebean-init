package io.ebean.tools.init.action;

import io.ebean.tools.init.Detection;
import io.ebean.tools.init.DetectionMeta;
import io.ebean.tools.init.InteractionHelp;
import io.ebean.tools.init.util.FileCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class DoAddGenerateMigration {

  private static final Logger log = LoggerFactory.getLogger(DoAddGenerateMigration.class);

  private final Detection detection;

  private final InteractionHelp help;

  public DoAddGenerateMigration(Detection detection, InteractionHelp help) {
    this.detection = detection;
    this.help = help;
  }

  public void run() {

    DetectionMeta meta = detection.getMeta();

    if (detection.isSourceModeKotlin()) {
      File ktSrc = meta.getSourceTestKotlin();
      if (ktSrc != null && ktSrc.exists()) {
        addKotlinGeneration(main(ktSrc));
        return;
      } else {
        help.acknowledge("  WARNING - can not find src/test/kotlin ?");
      }
    }

    File javaSrc = meta.getSourceTestJava();
    if (javaSrc == null || !javaSrc.exists()) {
      help.acknowledge("  Unsuccessful - can not determine test source root");
      return;
    }

    addJavaGenerator(main(javaSrc));
  }

  private File main(File src) {
    File testMain = new File(src, "main");
    if (!testMain.mkdirs()) {
      log.error("Error creating src/test directory");
    }
    return testMain;
  }

  private void addJavaGenerator(File srcMain) {
    try {
      File mig = new File(srcMain, "GenerateDbMigration.java");
      FileCopy.copy(mig, "/tp-GenerateDbMigration.java");
      help.ackDone("  ... added " + mig.getAbsolutePath());
      detection.addedGenerateMigration("GenerateDbMigration.java");

    } catch (IOException e) {
      throw new RuntimeException("Failed to copy GenerateDbMigration.java", e);
    }
  }

  private void addKotlinGeneration(File srcMain) {
    try {
      File mig = new File(srcMain, "GenerateDbMigration.kt");
      FileCopy.copy(mig, "/tp-GenerateDbMigration.kt");
      help.ackDone("  ... added " + mig.getAbsolutePath());
      detection.addedGenerateMigration("GenerateDbMigration.kt");

    } catch (IOException e) {
      throw new RuntimeException("Failed to copy GenerateDbMigration.kt", e);
    }
  }
}
