package io.ebean.tools.init.action;

import io.ebean.tools.init.InteractionHelp;
import io.ebean.tools.init.util.QuestionOptions;

import java.util.Map;

public class DoDatabaseDependency {

  private final InteractionHelp help;

  DoDatabaseDependency(InteractionHelp help) {
    this.help = help;
  }

  public void run() {

    QuestionOptions options = new QuestionOptions();
    options.add("0", "No, I'll add it myself", null);
    options.add("P", "Postgres", null);
    options.add("M", "MySql or MariaDB", null);
    options.add("N", "NuoDB", null);
    options.add("S", "SQL Server", null);
    options.add("O", "Oracle", null);
    options.add("H", "Hana", null);
    options.add("C", "Clickhouse", null);
    options.add("R", "Cockroach", null);
    options.add("L", "Sqlite", null);

    help.question("Add JDBC driver dependency?");
    help.outOps(options);
    String answer = help.askKey("Select an option:", options);
    if (!answer.equals("")) {
      QuestionOptions.Option fullAnswer = options.selected(answer);
      if (fullAnswer != null && !fullAnswer.key.equals("0")) {
        final String driver = obtainDriverDependency(fullAnswer.key);
        if (driver == null) {
          help.error("Sorry, Failed to obtain driver. Please add the dependency manually");
        } else {
          help.detection().setDatabaseDependency(driver);
        }
      }
    }
  }

  private String obtainDriverDependency(String key) {
    final Map<String, String> drivers = obtainDrivers();
    if (drivers == null) {
      return null;
    }
    switch (key) {
      case "P":
        return drivers.get("postgres");
      case "M":
        return drivers.get("mysql");
      case "N":
        return drivers.get("nuodb");
      case "S":
        return drivers.get("sqlserver");
      case "O":
        return drivers.get("oracle");
      case "H":
        return drivers.get("hana");
      case "C":
        return drivers.get("clickhouse");
      case "R":
        return drivers.get("cockroach");
      case "L":
        return drivers.get("sqlite");
      default:
        return null;
    }
  }

  Map<String, String> obtainDrivers() {
    try {
      final HttpGet.Response response = HttpGet.get("db-drivers.properties");
      if (response.ok()) {
        return HttpGet.asMap(response);
      }
    } catch (Exception e) {
      help.error("Error fetching DB drivers from https://ebean.io " + e.getMessage());
    }
    return null;
  }
}
