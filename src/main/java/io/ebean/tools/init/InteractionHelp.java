package io.ebean.tools.init;

import io.ebean.tools.init.action.DoLocalDevelopment;
import io.ebean.tools.init.action.DoProjectSetup;
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

  private static final String INDENT = "  ";
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

//  public void lines(String... lines) {
//    out.println();
//    for (String line : lines) {
//      out.println(Ansi.ansi().fgGreen().a("  "+line));
//    }
//  }

  public boolean setupProject() {

    question("It looks like Ebean is not configured for this project.");
    String yesNo = askYesNo("Do you want to run project setup to add dependencies and configuration ?");
    if (isYes(yesNo)) {
      new DoProjectSetup(this).run();
      return true;
    }
    return false;
  }

  public void localDevelopment() {

    out.println();
    out.print(Ansi.ansi().fgGreen().a(INDENT + "For local development we want to "));
    out.println(Ansi.ansi().bold().fgGreen().a("ignore docker shutdown").boldOff().a(" for test containers"));
    out.println(Ansi.ansi().boldOff().fgGreen().a(INDENT + "This makes running tests locally faster and is recommended").reset());
    out.println();

    String yesNo = askYesNo("create marker file ~/.ebean/ignore-docker-shutdown ?");
    if (isYes(yesNo)) {
      new DoLocalDevelopment().run(this);
    }
  }

  public void checkResourceDirectories() {
    DetectionMeta meta = detection.getMeta();

    File mainResource = meta.getMainResource();
    if (mainResource == null || !mainResource.exists()) {
      String yesNo = askYesNo("src/main/resource does not exist, can we create it?");
      if (isYes(yesNo)) {
        if (!meta.createSrcMainResources()) {
          ackErr("... failed to create src/main/resource directory");
        }
      }
    }

    File testResource = meta.getTestResource();
    if (testResource == null || !testResource.exists()) {
      String yesNo = askYesNo("src/test/resources does not exist, can we create it?");
      if (isYes(yesNo)) {
        if (!meta.createSrcTestResources()) {
          ackErr("... failed to create src/test/resources directory");
        }
      }
    }
  }

  private boolean isYes(String yesNo) {
    return yesNo.equalsIgnoreCase("Yes") || yesNo.equals("");
  }

  public void questionTransactionalPackage() {

    String topPackage = detection.getTopPackage();
    String entityPackage = detection.getEntityPackage();
    if (topPackage != null && topPackage.equals(entityPackage)) {
      int pos = topPackage.lastIndexOf('.');
      if (pos > 0) {
        topPackage = topPackage.substring(0, pos);
      }
    }

    QuestionOptions options = new QuestionOptions();
    if (topPackage != null && !topPackage.isEmpty()) {
      options.add("0", topPackage, null);
    }
    options.add("1", "Other", null);
    options.add("2", "None, I don't want to use @Transactional", null);

    question("Select the top level package @Transactional is used");
    outOps(options);
    String answer = askKey("Select an option:", options);
    if ("".equals(answer)) {
      answer = (topPackage != null) ? "0" : "1";
    }
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

    if (detection.isSourceModeKotlin()) {
      actions.setManifestQueryBeanPackage(actions.getManifestEntityPackage());
    } else {
      String txnPackage = actions.getManifestTransactionalPackage();
      if (!txnPackage.equals("none")) {
        actions.setManifestQueryBeanPackage(txnPackage);
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
      list.add("Other");
      QuestionOptions options = new QuestionOptions();
      options.addAll(list);

      question("Select a package that will contain the entity beans");
      QuestionOptions.Option answer = outOptions(options, "0");

      if ("Other".equalsIgnoreCase(answer.text)) {
        q_enterEntityPackage();
      }
      actions.setManifestEntityPackage(answer.text);
    }
  }

  private void q_enterEntityPackage() {
    String answer = ask("Enter a package that will contain the entity beans (e.g. org.myapp.domain)");
    actions.setManifestEntityPackage(answer);
  }

  private void q_enterTransactionalPackage() {
    String answer = ask("Enter the top level package that will contain @Transactional classes");
    actions.setManifestTransactionalPackage(answer);
  }

  public QuestionOptions.Option outOptions(QuestionOptions options, String defaultKey) {

    outOps(options);
    String answer = askKey("Select an option:", options);
    if ((answer == null || answer.equals("")) && defaultKey != null) {
      answer = defaultKey;
    }
    return options.selected(answer);
  }

  public void outOps(QuestionOptions options) {
    for (Map.Entry<String, QuestionOptions.Option> entry : options.entries()) {
      PrintStream out = AnsiConsole.out();
      out.print(Ansi.ansi().bold().fgBrightDefault().a(INDENT + entry.getKey().toLowerCase()).reset());

      QuestionOptions.Option option = entry.getValue();
      outputKeyDesc(30, option.text, option.description);
    }
  }

  String askSameLine(String ask) {
    out.print(Ansi.ansi().bold().fgBlue().a(ask).reset());
    return actions.readLine();
  }

  public String ask(String ask) {
    out.print(Ansi.ansi().bold().fgBlue().a(INDENT + ask).reset().a(" > "));
    return actions.readLine();
  }

  public String askKey(String ask, QuestionOptions options) {
    return ask(ask, options, true);
  }

  public String ask(String ask, QuestionOptions options) {
    return ask(ask, options, false);
  }

  public String ask(String ask, QuestionOptions options, boolean keyMode) {

    out.print(Ansi.ansi().bold().fgBlue().a(INDENT + ask).reset());

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
      optionKey(key.toLowerCase());
    }
    plain("]");
  }

  public void question(String content) {
    AnsiConsole.out().println(Ansi.ansi().bold().fgBlue().a(INDENT + content).reset());
  }

  void outputSourceMode() {

    String desc = detection.isSourceModeKotlin() ? "Kotlin source mode" : "Java source mode";

    out.println();
    out.println(Ansi.ansi().bold().fgGreen().a(INDENT + desc).reset());
  }

  void outputAllGood(String key, String description) {

    out.print(Ansi.ansi().bold().fgGreen().a("  PASS").reset());
    outputKeyDesc(27, key, description);
  }

  void outputKeyDesc(int padTo, String key, String description ) {

    out.print(Ansi.ansi().a(" - "+key).reset());
    if (description != null && description.trim().length() > 0) {
      int gap = padTo - key.length();
      out.print(Ansi.ansi().a(padding(gap) + "  - " + description).reset());
    }
    out.println();
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

  public void ackErr(String content) {
    out.println(Ansi.ansi().fgRed().a(INDENT + content).reset());
  }

  public void ackDone(String content) {
    out.println(Ansi.ansi().fgDefault().a(INDENT + content).reset());
  }

  void outputHeading() {
    AnsiConsole.out().println(Ansi.ansi().fgMagenta().a("-------------------------------------------------------------").reset());
    AnsiConsole.out().print(Ansi.ansi().bold().fgYellow().a("ebeaninit").reset());
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

  public String askYesNo(String content) {
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

  public DetectionMeta meta() {
    return detection.getMeta();
  }

  public void setEntityBeanPackage(String entityPackage) {
    detection.setEntityBeanPackage(entityPackage);
  }

  public Detection detection() {
    return detection;
  }

  public void error(String content) {
    out.println(Ansi.ansi().fgRed().a(content).reset());
  }

  public InteractionHelp newLine() {
    AnsiConsole.out().println();
    return this;
  }
}
