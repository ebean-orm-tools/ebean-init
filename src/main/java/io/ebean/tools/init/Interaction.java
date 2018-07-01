package io.ebean.tools.init;

import ch.qos.logback.classic.Level;
import io.ebean.tools.init.addfinders.DoGenerate;
import io.ebean.tools.init.addmanifest.DoAddManifest;
import io.ebean.tools.init.addmigration.DoAddGenerateMigration;
import io.ebean.tools.init.addprops.DoAddMainProperties;
import io.ebean.tools.init.addprops.DoAddTestProperties;
import io.ebean.tools.init.other.DoDebug;
import io.ebean.tools.init.util.QuestionOptions;
import io.ebean.tools.init.watch.FileWatcher;
import org.fusesource.jansi.AnsiConsole;

import java.io.File;

class Interaction {

  private final Detection detection;

  private final InteractionHelp help;

  private final FileWatcher fileWatcher;


  Interaction(Detection detection) {
    this.detection = detection;
    this.help = new InteractionHelp(detection, new Actions());
    this.fileWatcher = new FileWatcher(detection, help);
  }

  void run() {
    try {
      help.outputHeading();
      help.outputAllGoodBits();

      boolean quit = false;
      while (!quit) {
        QuestionOptions options = createOptions();
        help.question("Commands:");
        help.outOps(options);
        String answer = help.askKey("Select an command:", options);
        quit = isQuit(answer);
        if (quit) {
          help.acknowledge("  done.");
          help.acknowledge(" ");
          help.acknowledge(" ");
        } else {
          executeCommand(answer);
        }
      }

    } finally {
      AnsiConsole.systemUninstall();
    }
  }

  private void executeCommand(String answer) {

    answer = answer.toUpperCase();
    switch (answer) {
      case "M":
        executeManifest();
        break;
      case "A":
        executeAddMainProperties();
        break;
      case "P":
        executeAddTestProperties();
        break;
      case "L":
        executeAddTestLogging();
        break;
      case "G":
        executeAddDbMigration();
        break;
      case "F":
        executeGenerateFinders();
        break;
      case "T":
        executeGenerateQueryBeans();
        break;
      case "0":
        executeDebug();
        break;
      case "1":
        startWatcher();
        break;
      case "2":
        stopWatcher();
        break;
      case "8":
        loggerDebugOff();
        break;
      case "9":
        loggerDebugOn();
        break;
    }
  }


  private void stopWatcher() {
    fileWatcher.stop();
  }

  private void startWatcher() {
    String mainOut = detection.getMeta().getMainOutput();
    File mainOutDir = new File(mainOut);
    if (!mainOutDir.exists()) {
      help.yell("main output dir does not exist? " + mainOut);
      return;
    }

    String entityPackage = detection.getEntityPackage();
    String entityPath = entityPackage.replace('.', '/');

    File fullPath = new File(mainOutDir, entityPath);
    if (!fullPath.exists()) {
      help.yell("entity bean output dir does not exist? " + fullPath.getAbsolutePath());
      return;
    }

    fileWatcher.start(fullPath.toPath());
  }


  private boolean isQuit(String answer) {
    answer = answer.trim();
    return "Q".equalsIgnoreCase(answer) || "X".equalsIgnoreCase(answer);
  }

  private QuestionOptions createOptions() {
    QuestionOptions options = new QuestionOptions();
    if (!detection.isEbeanManifestFound()) {
      options.add("M", "Manifest - add ebean.mf to control enhancement (recommended)");
    }
    if (!detection.isMainProperties()) {
      options.add("A", "Application properties - Add application.yml to configure Ebean");
    }
    if (!detection.isTestPropertiesFound()) {
      options.add("P", "Test properties - Add application-test.yml to configure Ebean when running tests (recommended)");
    }
    if (!detection.isTestLoggingEntry()) {
      options.add("L", "Logging - Add test logging entry to log SQL when running tests (recommended)");
    }
    if (!detection.isDbMigration()) {
      options.add("G", "Generate migrations - Add GenerateDbMigration for generating DB migration scripts (recommended)");
    }

    options.add("F", "Finders - generate finders");
    options.add("T", "Type safe query beans - manually generate them (rather than via APT/KAPT)");
    options.add("Q", "Quit");

    return options;
  }

  private void executeAddDbMigration() {
    new DoAddGenerateMigration(detection, help).run();
  }

  private void executeAddTestLogging() {
    help.acknowledge("  Refer to https://ebean-orm.github.io/docs/logging");
  }

  private void executeGenerateFinders() {
    new DoGenerate(detection, help).generateFinders();
  }

  private void executeGenerateQueryBeans() {
    new DoGenerate(detection, help).generateQueryBeans();
  }

  private void executeManifest() {
    new DoAddManifest(detection, help).run();
  }

  private void executeAddTestProperties() {
    new DoAddTestProperties(detection, help).run();
  }

  private void executeAddMainProperties() {
    new DoAddMainProperties(detection, help).run();
  }

  private void executeDebug() {
    new DoDebug(detection, help).run();
  }

  private void loggerDebugOn() {
    help.acknowledge("... logging debug ON");
    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("io");
    root.setLevel(Level.DEBUG);
  }

  private void loggerDebugOff() {
    help.acknowledge("... logging debug OFF");
    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("io");
    root.setLevel(Level.WARN);
  }

}
