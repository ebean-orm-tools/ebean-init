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
      case "J":
        sourceMode(SourceMode.JAVA);
        break;
      case "K":
        sourceMode(SourceMode.KOTLIN);
        break;
      case "E":
        executeExtraOptions();
        break;
      case "1":
        loggerDebugOn();
        break;
      case "2":
        loggerDebugOff();
        break;
      case "3":
        startWatcher();
        break;
      case "4":
        stopWatcher();
        break;
    }
  }

  private void sourceMode(SourceMode sourceMode) {
    detection.setSourceMode(sourceMode);
    help.outputSourceMode();
  }


  private void stopWatcher() {
    fileWatcher.stop();
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
      options.add("A", "Application properties","Add application.yml to configure Ebean");
    }
    if (!detection.isTestPropertiesFound()) {
      options.add("P", "Test properties","Add application-test.yml to configure Ebean when running tests (recommended)");
    }
    if (!detection.isTestLoggingEntry()) {
      options.add("L", "Logging","Add test logging entry to log SQL when running tests (recommended)");
    }
    if (!detection.isDbMigration()) {
      options.add("G", "Generate migrations","Add GenerateDbMigration for generating DB migration scripts (recommended)");
    }

    options.add("F", "Finders","Generate finders");
    options.add("T", "Type safe query beans","Generate query beans (rather than via APT/KAPT)");

    if (detection.isSourceModeKotlin()) {
      options.add("J", "Java source mode",null);
    } else {
      options.add("K", "Kotlin source mode",null);
    }

    if (detection.isExtraOptions()) {
      options.add("0", "Output debug",null);
      options.add("1", "Turn debug On",null);
      options.add("2", "Turn debug Off",null);

      if (!fileWatcher.isRunning()) {
        options.add("3", "Start background query bean generation","Experimental background generation of query beans");
      } else {
        options.add("4", "Stop background query bean generation",null);
      }
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
