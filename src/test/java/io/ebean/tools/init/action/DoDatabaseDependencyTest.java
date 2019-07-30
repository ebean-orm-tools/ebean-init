package io.ebean.tools.init.action;

import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DoDatabaseDependencyTest {

  @Test
  public void obtainDrivers() {

    DoDatabaseDependency databaseDependency = new DoDatabaseDependency(null);

    final Map<String, String> drivers = databaseDependency.obtainDrivers();

    assertThat(drivers.get("postgres")).startsWith("org.postgresql:postgresql:");
    assertThat(drivers.get("mysql")).isNotNull();
    assertThat(drivers.get("sqlserver")).isNotNull();
    assertThat(drivers.get("oracle")).isNotNull();
    assertThat(drivers.get("clickhouse")).isNotNull();
    assertThat(drivers.get("hana")).isNotNull();
  }
}
