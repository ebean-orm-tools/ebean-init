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
  private final DetectionMeta meta;

  public DoAddGenerateMigration(InteractionHelp help) {
    this.help = help;
    this.detection = help.detection();
    this.meta = detection.getMeta();
  }

  public void run() {
    if (detection.isSourceModeKotlin()) {
      addKotlinGenerateMigration();
    } else {
      addJavaGenerateMigration();
    }
  }

  private void addJavaGenerateMigration() {
    File javaSrc = meta.getSourceTestJava();
    if (javaSrc == null || !javaSrc.exists()) {
      String yesNo = help.askYesNo("src/test/java does not exist, can we create it?");
      if (yesNo.equalsIgnoreCase("Yes")) {
        if (!meta.createSourceTestJava()) {
          help.ackErr("... failed to create src/test/java directory");
          return;
        }
      }
    }

    addJavaGenerator(main(meta.getSourceTestJava()));
  }

  private void addKotlinGenerateMigration() {
    File ktSrc = meta.getSourceTestKotlin();
    if (ktSrc == null || !ktSrc.exists()) {
      String yesNo = help.askYesNo("src/test/kotlin does not exist, can we create it?");
      if (yesNo.equalsIgnoreCase("Yes")) {
        if (!meta.createSourceTestKotlin()) {
          help.ackErr("... failed to create src/test/kotlin directory");
          return;
        }
      }
    }
    addKotlinGeneration(main(meta.getSourceTestKotlin()));
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
      help.ackDone("... added GenerateDbMigration.java");
      detection.addedGenerateMigration("GenerateDbMigration.java");

    } catch (IOException e) {
      throw new RuntimeException("Failed to copy GenerateDbMigration.java", e);
    }
  }

  private void addKotlinGeneration(File srcMain) {
    try {
      File mig = new File(srcMain, "GenerateDbMigration.kt");
      FileCopy.copy(mig, "/tp-GenerateDbMigration.kt");
      help.ackDone("... added GenerateDbMigration.kt");
      detection.addedGenerateMigration("GenerateDbMigration.kt");

    } catch (IOException e) {
      throw new RuntimeException("Failed to copy GenerateDbMigration.kt", e);
    }
  }
}
