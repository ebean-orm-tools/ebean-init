package io.ebean.tools.init;

import ch.qos.logback.classic.Level;
import io.ebean.tools.init.action.DoAddGenerateMigration;
import io.ebean.tools.init.action.DoAddMainProperties;
import io.ebean.tools.init.action.DoAddManifest;
import io.ebean.tools.init.action.DoAddTestResource;
import io.ebean.tools.init.action.DoGenerate;
import io.ebean.tools.init.util.QuestionOptions;
import io.ebean.tools.init.watch.FileWatcher;
import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

class Interaction {

  private static final Logger log = LoggerFactory.getLogger(Interaction.class);

  private final Detection detection;

  private final InteractionHelp help;

  private final FileWatcher fileWatcher;

  private boolean debugOutput;

  private boolean watching;

  Interaction(Detection detection) {
    this.detection = detection;
    this.help = new InteractionHelp(detection, new Actions());
    this.fileWatcher = new FileWatcher(detection, help);
  }

  void run() {
    try {
      help.outputHeading();
      help.outputAllGoodBits();
      if (help.unexpectedLocation()) {
        return;
      }

      help.outputSourceMode();
      help.checkResourceDirectories();

      boolean quit = false;
      while (!quit) {
        QuestionOptions options = createOptions();
        help.newCommand();
        help.question("Commands:");
        help.outOps(options);
        String answer = help.askKey("Select an command:", options);
        quit = isQuit(answer);
        if (quit) {
          stopWatcher();
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
      case "J":
        sourceMode(SourceMode.JAVA);
        break;
      case "K":
        sourceMode(SourceMode.KOTLIN);
        break;
      case "E":
        executeExtraOptions();
        break;
      case "0":
        loggerDebugToggle();
        break;
      case "1":
        watcherToggle();
        break;
      case "2":
        executeGenerateQueryBeans();
        break;
    }
  }

  private void sourceMode(SourceMode sourceMode) {
    detection.setSourceMode(sourceMode);
    help.outputSourceMode();
  }


  private void watcherToggle() {
    if (watching) {
      stopWatcher();
    } else {
      startWatcher();
    }
    watching = fileWatcher.isRunning();
  }

  private void stopWatcher() {
    try {
      fileWatcher.stop();
    } catch (Exception e) {
      help.yell("Error stopping watcher: " + e.getMessage());
      log.error("Error stopping watcher", e);
    }
  }

  private void startWatcher() {
    File mainOut = detection.getMeta().getMainOutput();
    if (mainOut == null || !mainOut.exists()) {
      help.yell("main output dir does not exist? " + mainOut);
      return;
    }

    String entityPackage = detection.getEntityPackage();
    String entityPath = entityPackage.replace('.', '/');

    File fullPath = new File(mainOut, entityPath);
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
      options.add("M", "Manifest","add ebean.mf to control enhancement (recommended)");
    }
    if (!detection.isMainProperties()) {
      options.add("A", "Application properties","Add application.yaml to configure Ebean");
    }
    if (!detection.isTestPropertiesFound()) {
      options.add("P", "Test properties","Add application-test.yaml to configure Ebean when running tests (recommended)");
    }
    if (!detection.isTestLoggingEntry()) {
      options.add("L", "Logging","Add test logging entry to log SQL when running tests (recommended)");
    }
    if (!detection.isDbMigration()) {
      options.add("G", "Generate migrations","Add GenerateDbMigration for generating DB migration scripts (recommended)");
    }

    options.add("F", "Finders","Generate finders");

    if (detection.isSourceModeKotlin()) {
      options.add("J", "Java source mode",null);
    } else {
      options.add("K", "Kotlin source mode",null);
    }

    if (detection.isExtraOptions()) {
      String debugDesc = debugOutput ? "Turn debug Off" : "Turn debug On";
      options.add("0", debugDesc,null);

      String watchDesc = watching ? "Stop background query bean generation" : "Start background query bean generation";
      options.add("1", watchDesc,null);

      options.add("2", "Type safe query beans","Generate query beans (rather than via APT/KAPT)");

    } else {
      options.add("E", "Experimental options","Show experimentation options for query bean generation");
    }

    options.add("Q", "Quit",null);
    return options;
  }

  private void executeAddDbMigration() {
    new DoAddGenerateMigration(detection, help).run();
  }

  private void executeAddTestLogging() {
    new DoAddTestResource(detection, help).addLogbackTest();
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
    new DoAddTestResource(detection, help).addApplicationTestYml();
  }

  private void executeAddMainProperties() {
    new DoAddMainProperties(detection, help).run();
  }

  private void executeExtraOptions() {
    detection.setExtraOptions(true);
  }

  private void loggerDebugToggle() {

    debugOutput = !debugOutput;
    String msg = debugOutput ? "... logging debug ON" : "... logging debug ON";
    Level level = debugOutput ? Level.TRACE : Level.WARN;
    help.acknowledge(msg);
    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger("io");
    root.setLevel(level);
  }

}
