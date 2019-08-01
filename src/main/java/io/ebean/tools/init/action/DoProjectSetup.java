package io.ebean.tools.init.action;

import io.ebean.tools.init.DetectionMeta;
import io.ebean.tools.init.GradleBuild;
import io.ebean.tools.init.InteractionHelp;
import io.ebean.tools.init.MavenPom;
import io.ebean.tools.init.SourceMode;
import io.ebean.tools.init.util.QuestionOptions;

public class DoProjectSetup {

  private final InteractionHelp help;

  public DoProjectSetup(InteractionHelp help) {
    this.help = help;
  }

  public void run() {

    final DetectionMeta meta = help.meta();

    askJavaOrKotlin();

    final MavenPom mavenPom = meta.getMavenPom();
    if (mavenPom != null) {
      String yesNo = help.newLine().askYesNo("Add dependencies and enhancement plugin to pom?");
      if (yesNo.equalsIgnoreCase("Yes")) {
        new DoMavenPomUpdate(help).run(mavenPom);
      }
    } else {
      final GradleBuild gradleBuild = meta.getGradleBuild();
      if (gradleBuild != null) {
        String yesNo = help.newLine().askYesNo("Add dependencies and enhancement plugin to gradle build?");
        if (yesNo.equalsIgnoreCase("Yes")) {
          new DoGradleBuildUpdate(help).run(gradleBuild);
        }
      }
    }

    String yesNo = help.newLine().askYesNo("Add a sample Entity bean?");
    if (yesNo.equalsIgnoreCase("Yes")) {
      new DoAddSampleEntity(help).run();
    }

    yesNo = help.newLine().askYesNo("Add ebean.mf manifest to control enhancement?");
    if (yesNo.equalsIgnoreCase("Yes")) {
      new DoAddManifest(help).run();
    }

    yesNo = help.newLine().askYesNo("Add application-test.yaml to configure testing?");
    if (yesNo.equalsIgnoreCase("Yes")) {
      new DoAddTestResource(help).addApplicationTestYml();
    }

    yesNo = help.newLine().askYesNo("Add GenerateDbMigration to generate database migrations?");
    if (yesNo.equalsIgnoreCase("Yes")) {
      new DoAddGenerateMigration(help).run();
    }

    if (!help.detection().isTestLoggingEntry()) {
      yesNo = help.newLine().askYesNo("Add logback-test.xml entry for logging SQL, Transactions etc during testing?");
      if (yesNo.equalsIgnoreCase("Yes")) {
        new DoAddTestResource(help).addLogbackTest();
      }
    }

    help.newLine();
    help.newLine();
    help.question("Please re-build the project and then you are all set to go.");
    help.question("You can re-run ebeaninit later to generate finders if you like.");
    help.newLine();
    help.question("Try running CustomerTest ...");
    help.question("Bye");
    help.newLine();
  }

  private void askJavaOrKotlin() {

    QuestionOptions options = new QuestionOptions();
    options.add("J", "Java", null);
    options.add("K", "Kotlin", null);

    String answer = help.newLine().ask("Using Java or Kotlin?", options);
    answer = answer.toLowerCase();

    if (answer.startsWith("j")) {
      help.detection().setSourceMode(SourceMode.JAVA);
    } else if (answer.startsWith("k")) {
      help.detection().setSourceMode(SourceMode.KOTLIN);
    } else {
      help.ackErr("I didn't understand the response - assuming Java");
      help.detection().setSourceMode(SourceMode.JAVA);
    }

  }

}
