package io.ebean.tools.init;

import io.ebean.tools.init.util.QuestionOptions;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.Console;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InteractionHelp {

  private final Detection detection;

  private final Actions actions;

  private final PrintStream out;

//  class Answer {
//    final String key;
//    final String value;
//
//    Answer(String key, String value) {
//      this.key = key;
//      this.value = value;
//    }
//  }

  InteractionHelp(Detection detection, Actions actions) {
    this.detection = detection;
    this.actions = actions;

    AnsiConsole.systemInstall();
    this.out = AnsiConsole.out();
  }

  public boolean isContinue() {
    return actions.continueState();
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
      options.add("0", topPackage);
    }
    options.add("1", "Other");
    options.add("2", "None - I don't want to use @Transactional");

    question("Select the top level package @Transactional is used");
    outOps(options);
    String answer = askKey("Select an option:", options);
    String fullAnswer = options.selected(answer);
    System.out.println("---- answer key:"+answer+" pkg:"+fullAnswer);

    if (isContinue()) {
      if ("1".equalsIgnoreCase(answer)) {
        q_enterTransactionalPackage();
      } else if ("2".equalsIgnoreCase(answer)) {
        actions.setManifestTransactionalPackage("none");
      } else {
        actions.setManifestTransactionalPackage(fullAnswer);
      }
    }
  }

  public void questionQueryBeanPackage() {

    actions.setManifestQueryBeanPackage("none");

    if (!detection.isQueryBeanInClassPath()) {
      acknowledge("  ebean-querybean does not appear to be in the classpath");
      String yesNo = askYesNo("  Do you plan to use Query Beans");
      if (!yesNo.equalsIgnoreCase("Yes")) {
        return;
      }
    }
    if (isContinue()) {
      if (detection.isKotlinInClassPath()) {
        acknowledge("  ... for kotlin query beans using the entity package of " + actions.getManifestEntityPackage());
        actions.setManifestQueryBeanPackage(actions.getManifestEntityPackage());
      } else {
        acknowledge("  ... for java query beans using the top level package of " + actions.getManifestTransactionalPackage());
        actions.setManifestQueryBeanPackage(actions.getManifestTransactionalPackage());
      }
    }
  }

  public void questionEntityBeanPackage() {

    List<String> list = detection.getDetectedPackages();
    if (list.isEmpty()) {
      q_enterEntityPackage();

    } else {
      QuestionOptions options = new QuestionOptions();
      options.add("0", "Other");
      options.addAll(list);

      question("Select a package that will contain the entity beans");
      String answer = outOptions(options);

      System.out.println("---- answer: "+answer);
      if ("Other".equalsIgnoreCase(answer)) {
        q_enterEntityPackage();
      }
      actions.setManifestEntityPackage(answer);
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

  String outOptions(QuestionOptions options) {

    outOps(options);
    String answer = ask("select one of the options above");
    return options.selected(answer);
  }

  void outOps(QuestionOptions options) {
    Set<Map.Entry<String, String>> entries = options.entries();
    for (Map.Entry<String, String> entry : entries) {
      PrintStream out = AnsiConsole.out();
      out.print(Ansi.ansi().bold().fgBrightBlue().a("  " + entry.getKey()).reset());
      out.println(Ansi.ansi().fgBlue().a(" - " + entry.getValue()).reset());
    }
  }

  String ask(String ask) {
    out.println(Ansi.ansi().bold().fgBrightBlue().a(ask).reset());
    return actions.readLine();
  }

  String askKey(String ask, QuestionOptions options) {
    return ask(ask, options, true);
  }

  String ask(String ask, QuestionOptions options) {
    return ask(ask, options, false);
  }

  String ask(String ask, QuestionOptions options, boolean keyMode) {

    out.print(Ansi.ansi().bold().fgBrightBlue().a("  " + ask).reset());

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
      answer = options.selected(answer);
    }
    return answer;//new Answer(line, description);
  }

  private void outOptionKeys(QuestionOptions options) {

    plain(" [");
    Set<String> keys = options.keys();
    int i = 0;
    for (String key : keys) {
      if (i++ != 0) {
        plain("/");
      }
      bYell(key);
    }
    plain("]");
  }

  void question(String content) {
    PrintStream out = AnsiConsole.out();
    out.println(Ansi.ansi().bold().fgBrightBlue().a(content).reset());
  }

  void outputAllGood(String key, String description) {

    int gap = 40 - key.length();

    out.print(Ansi.ansi().bold().fgGreen().a("  PASS").reset());
    out.print(Ansi.ansi().bold().a(" - ").reset());
    out.print(Ansi.ansi().bold().fgBrightBlue().a(key).reset());
    out.println(Ansi.ansi().a(padding(gap).toString() + "  - " + description).reset());
  }

  public void outputDebug(String key, String description) {

    int gap = 25 - key.length();

    out.print(Ansi.ansi().bold().fgBrightBlue().a(key).reset());
    out.println(Ansi.ansi().a(padding(gap) + "  - " + description).reset());
  }

  private String padding(int gap) {
    StringBuilder sbGap = new StringBuilder(gap);
    if (gap > 0) {
      for (int i = 0; i < gap; i++) {
        sbGap.append(' ');
      }
    }
    return sbGap.toString();
  }

//  void outputManifest() {
//
//    String pad = "          ";
//
//    int padOut = 40;
//    plain(pad);
//    blueBold(detection.getEntityPackages().toString(), padOut);
//    plain("  ... enhancement for entity beans\n");
//
//    plain(pad);
//    blueBold(detection.getTransactionalPackages().toString(), padOut);
//    plain("  ... enhancement for @Transactional\n");
//
//    plain(pad);
//    blueBold(detection.getQueryBeanPackages().toString(), padOut);
//    plain("  ... query bean caller enhancement\n");
//  }

  void plain(String content) {
    out.print(Ansi.ansi().a(content).reset());
  }

  void plainln(String content) {
    out.println(Ansi.ansi().a(content).reset());
  }

  void blueBold(String content, int padTo) {
    int pad = padTo - content.length();
    out.print(Ansi.ansi().bold().fgBlue().a(content).reset());
    padOut(pad);
  }

  void yell(String content, int padTo) {
    int pad = padTo - content.length();
    yell(content);
    padOut(pad);
  }

  void yell(String content) {
    out.print(Ansi.ansi().bold().fgYellow().a(content).reset());
  }

  void bYell(String content) {
    out.print(Ansi.ansi().bold().fgBrightYellow().a(content).reset());
  }

  public void acknowledge(String content) {
    out.println(Ansi.ansi().fgRed().a(content).reset());
  }


  void padOut(int len) {
    plain(padding(len));
  }

  void outputHeading() {
    AnsiConsole.out().println(Ansi.ansi().fgBrightMagenta().a("---------------------------------------------").reset());
    AnsiConsole.out().print(Ansi.ansi().bold().fgYellow().a("EBEAN : INIT").reset());
    AnsiConsole.out().println(Ansi.ansi().fgBrightMagenta().a("  - interactive ebean initialiser").reset());
    AnsiConsole.out().println(Ansi.ansi().fgBrightMagenta().a("---------------------------------------------").reset());
  }


  void outputAllGoodBits() {

    if (detection.isEbeanManifestFound()) {
      outputAllGood("ebean.mf", "controlling packages that are enhanced");
      //outputManifest();
    }
    if (detection.isTestEbeanProperties()) {
      outputAllGood("test-ebean.properties", "configuration when running tests");
    }
    if (detection.isTestLoggingEntry()) {
      outputAllGood(detection.getLoggerType(), "entry for logging SQL when running tests");
    }
    if (detection.isDbMigration()) {
      outputAllGood(detection.getDbMigrationFile(), "for generation of DB migrations");
    }

    plainln(" ");
    plainln(" ");
  }

  void desc(String content) {
    plainln("  " + content);
  }

  String askYesNo(String content) {
    QuestionOptions yn = new QuestionOptions();
    yn.add("Y", "Yes");
    yn.add("N", "No");
    return actions.checkState(ask(content, yn));
  }

  public Actions actions() {
    return actions;
  }
}
