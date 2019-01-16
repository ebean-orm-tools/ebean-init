package io.ebean.tools.init;

import io.ebean.tools.init.util.QuestionOptions;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.Console;
import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InteractionHelp {

  private final Detection detection;

  private final Actions actions;

  private final PrintStream out;

  InteractionHelp(Detection detection, Actions actions) {
    this.detection = detection;
    this.actions = actions;

    AnsiConsole.systemInstall();
    this.out = AnsiConsole.out();
  }

  public boolean isContinue() {
    return actions.continueState();
  }

  public void checkResourceDirectories() {
    DetectionMeta meta = detection.getMeta();

    File mainResource = meta.getMainResource();
    if (mainResource == null || !mainResource.exists()) {
      String yesNo = askYesNo("  src/main/resource does not exist, can we create it?");
      if (yesNo.equalsIgnoreCase("Yes")) {
        if (!meta.createSrcMainResources()) {
          acknowledge("  ... failed to create src/main/resource directory");
        }
      }
    }

    File testResource = meta.getTestResource();
    if (testResource == null || !testResource.exists()) {
      String yesNo = askYesNo("  src/test/resource does not exist, can we create it?");
      if (yesNo.equalsIgnoreCase("Yes")) {
        if (!meta.createSrcTestResources()) {
          acknowledge("  ... failed to create src/test/resource directory");
        }
      }
    }
  }

  public void questionTransactionalPackage() {

    String topPackage = detection.getTopPackage();
    String entityPackage = detection.getEntityPackage();
    if (topPackage != null && entityPackage != null && topPackage.equals(entityPackage)) {
      int pos = topPackage.lastIndexOf('.');
      if (pos > 0) {
        topPackage = topPackage.substring(0, pos);
      }
    }

    QuestionOptions options = new QuestionOptions();
    if (topPackage != null) {
      options.add("0", topPackage, null);
    }
    options.add("1", "Other", null);
    options.add("2", "None","I don't want to use @Transactional");

    question("Select the top level package @Transactional is used");
    outOps(options);
    String answer = askKey("Select an option:", options);
    QuestionOptions.Option fullAnswer = options.selected(answer);

    if (isContinue()) {
      if ("1".equalsIgnoreCase(answer)) {
        q_enterTransactionalPackage();
      } else if ("2".equalsIgnoreCase(answer)) {
        actions.setManifestTransactionalPackage("none");
      } else {
        actions.setManifestTransactionalPackage(fullAnswer.text);
      }
    }
  }

  public void questionQueryBeanPackage() {

    actions.setManifestQueryBeanPackage("none");

    String yesNo = askYesNo("  Do you plan to use Query Beans");
    if (!yesNo.equalsIgnoreCase("Yes")) {
      return;
    }
    if (isContinue()) {
      detection.setUsingQueryBeans();
      if (detection.isSourceModeKotlin()) {
        ackDone("  ... for kotlin query beans using the entity package of " + actions.getManifestEntityPackage());
        actions.setManifestQueryBeanPackage(actions.getManifestEntityPackage());
      } else {
        ackDone("  ... for java query beans using the top level package of " + actions.getManifestTransactionalPackage());
        actions.setManifestQueryBeanPackage(actions.getManifestTransactionalPackage());
      }
    }
  }

  public String questionFinder() {
    return askSameLine("Enter an entity bean name (to generate the finder for): ");
  }

  public void questionEntityBeanPackage() {

    List<String> list = detection.getDetectedPackages();
    if (list.isEmpty()) {
      q_enterEntityPackage();

    } else {
      QuestionOptions options = new QuestionOptions();
      options.add("0", "Other", null);
      options.addAll(list);

      question("Select a package that will contain the entity beans");
      QuestionOptions.Option answer = outOptions(options);

      if ("Other".equalsIgnoreCase(answer.text)) {
        q_enterEntityPackage();
      }
      actions.setManifestEntityPackage(answer.text);
    }
  }

  private void q_enterEntityPackage() {
    String answer = ask("Enter a package that will contain the entity beans: ");
    actions.setManifestEntityPackage(answer);
  }

  private void q_enterTransactionalPackage() {
    String answer = ask("Enter the top level package that will contain @Transactional classes : ");
    actions.setManifestTransactionalPackage(answer);
  }

  QuestionOptions.Option outOptions(QuestionOptions options) {

    outOps(options);
    String answer = ask("select one of the options above");
    return options.selected(answer);
  }

  void outOps(QuestionOptions options) {
    for (Map.Entry<String, QuestionOptions.Option> entry : options.entries()) {
      PrintStream out = AnsiConsole.out();
      out.print(Ansi.ansi().bold().fgBrightDefault().a("  " + entry.getKey()).reset());

      QuestionOptions.Option option = entry.getValue();
      outputKeyDesc(30, option.text, option.description);
    }
  }

  String askSameLine(String ask) {
    out.print(Ansi.ansi().bold().fgBlue().a(ask).reset());
    return actions.readLine();
  }

  String ask(String ask) {
    out.println(Ansi.ansi().bold().fgBlue().a(ask).reset());
    return actions.readLine();
  }

  String askKey(String ask, QuestionOptions options) {
    return ask(ask, options, true);
  }

  String ask(String ask, QuestionOptions options) {
    return ask(ask, options, false);
  }

  String ask(String ask, QuestionOptions options, boolean keyMode) {

    out.print(Ansi.ansi().bold().fgBlue().a("  " + ask).reset());

    if (options != null) {
      outOptionKeys(options);
    }

    plain(" > ");
    AnsiConsole.out().flush();

    Console console = System.console();
    if (console == null) {
      plainln("... no Console - skipping");
      return null;
    }

    String answer = actions.readLine();

    if (options != null && !keyMode) {
      QuestionOptions.Option selected = options.selected(answer);
      if (selected != null) {
        answer = selected.text;
      }
    }
    return answer;
  }

  private void outOptionKeys(QuestionOptions options) {

    plain(" [");
    Set<String> keys = options.keys();
    int i = 0;
    for (String key : keys) {
      if (i++ != 0) {
        plain("/");
      }
      optionKey(key);
    }
    plain("]");
  }

  public void newCommand() {
    AnsiConsole.out().println();
  }

  void question(String content) {
    PrintStream out = AnsiConsole.out();
    out.println(Ansi.ansi().bold().fgBlue().a(content).reset());
  }

  void outputSourceMode() {

    String desc = detection.isSourceModeKotlin() ? "Kotlin source mode" : "Java source mode";

    out.println();
    out.println(Ansi.ansi().bold().fgGreen().a("  " + desc).reset());
  }

  void outputAllGood(String key, String description) {

    out.print(Ansi.ansi().bold().fgGreen().a("  PASS").reset());
    outputKeyDesc(27, key, description);
  }

  void outputKeyDesc(int padTo, String key, String description ) {

    out.print(Ansi.ansi().bold().a(" - ").reset());
    out.print(Ansi.ansi().bold().fgBlue().a(key).reset());
    if (description != null && description.trim().length() > 0) {
      int gap = padTo - key.length();
      out.print(Ansi.ansi().a(padding(gap) + "  - " + description).reset());
    }
    out.println();
  }

  public void outputDebug(String key, String description) {

    int gap = 25 - key.length();

    out.print(Ansi.ansi().bold().fgBlue().a(key).reset());
    out.println(Ansi.ansi().a(padding(gap) + "  - " + description).reset());
  }

  private String padding(int gap) {
    if (gap <= 0) {
      return "";
    }
    StringBuilder sbGap = new StringBuilder(gap);
    for (int i = 0; i < gap; i++) {
      sbGap.append(' ');
    }
    return sbGap.toString();
  }

  void plain(String content) {
    out.print(Ansi.ansi().a(content).reset());
  }

  void plainln(String content) {
    out.println(Ansi.ansi().a(content).reset());
  }

  void yell(String content) {
    out.print(Ansi.ansi().bold().fgYellow().a(content).reset());
  }

  void optionKey(String content) {
    out.print(Ansi.ansi().bold().fgBrightDefault().a(content).reset());
  }

  public void acknowledge(String content) {
    out.println(Ansi.ansi().fgRed().a(content).reset());
  }

  public void ackDone(String content) {
    out.println(Ansi.ansi().fgDefault().a(content).reset());
  }


  void padOut(int len) {
    plain(padding(len));
  }

  void outputHeading() {
    AnsiConsole.out().println(Ansi.ansi().fgMagenta().a("-------------------------------------------------------------").reset());
    AnsiConsole.out().print(Ansi.ansi().bold().fgYellow().a("EBEAN : INIT").reset());
    AnsiConsole.out().println(Ansi.ansi().fgDefault().a("  - interactive ebean initialiser - " + "v" + Version.getVersion()).reset());
    AnsiConsole.out().println(Ansi.ansi().fgMagenta().a("-------------------------------------------------------------").reset());
  }


  void outputAllGoodBits() {

    if (detection.isEbeanManifestFound()) {
      outputAllGood("ebean.mf", "controlling packages that are enhanced");
      //outputManifest();
    }
    if (detection.isTestPropertiesFound()) {
      outputAllGood("test properties/yaml", "configuration when running tests");
    }
    if (detection.isTestLoggingEntry()) {
      outputAllGood(detection.getLoggerType(), "entry for logging SQL when running tests");
    }
    if (detection.isDbMigration()) {
      outputAllGood(detection.getDbMigrationFile(), "for generation of DB migrations");
    }
  }

  String askYesNo(String content) {
    QuestionOptions yn = new QuestionOptions();
    yn.add("Y", "Yes", null);
    yn.add("N", "No", null);
    return actions.checkState(ask(content, yn));
  }

  public Actions actions() {
    return actions;
  }

  public boolean unexpectedLocation() {

    if (detection.unexpectedLocation()) {
      ackDone("The current directory does not appear to be a Java or Kotlin project");
      ackDone("Please run me in a project directory - thanks !!");
      return true;
    }

    return false;
  }
}
