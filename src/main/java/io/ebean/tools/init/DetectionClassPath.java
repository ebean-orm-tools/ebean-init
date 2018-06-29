package io.ebean.tools.init;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

class DetectionClassPath {

  private final Map<String,String> dbMap = new LinkedHashMap<>();

  private final Set<String> targetDatabases = new HashSet<>();

  private boolean kotlin;

  private boolean ebeanDockerRun;

  private boolean ebeanQueryBeans;

  private boolean ebeanElastic;

  private boolean queryBeanGenerator;

  DetectionClassPath() {
    dbMap.put("postgres", "postgres");
    dbMap.put("oracle", "oracle");
    dbMap.put("mysql", "mysql");
  }

  public String toString() {
    return "dbs:" + targetDatabases + " kotlin:" + kotlin + " dockerRun:" + ebeanDockerRun + " qb:" + ebeanQueryBeans + " qbg:" + queryBeanGenerator;
  }

  public Set<String> getTargetDatabases() {
    return targetDatabases;
  }

  public boolean isKotlin() {
    return kotlin;
  }

  public boolean isEbeanElastic() {
    return ebeanElastic;
  }

  public boolean isEbeanDockerRun() {
    return ebeanDockerRun;
  }

  public boolean isEbeanQueryBeans() {
    return ebeanQueryBeans;
  }

  public boolean isQueryBeanGenerator() {
    return queryBeanGenerator;
  }

  public void check(String entry) {

    if (!kotlin) {
      kotlin = entry.contains("kotlin-stdlib");
    }
    if (!ebeanDockerRun) {
      ebeanDockerRun = entry.contains("ebean-docker-run");
    }
    if (!ebeanQueryBeans) {
      ebeanQueryBeans = entry.contains("ebean-querybean");
    }
    if (!queryBeanGenerator) {
      queryBeanGenerator = entry.contains("querybean-generator");
    }
    if (!ebeanElastic) {
      ebeanElastic = entry.contains("ebean-elastic");
    }

    for (Map.Entry<String, String> db : dbMap.entrySet()) {
      if (entry.contains(db.getValue())) {
        targetDatabases.add(db.getKey());
      }
    }
  }
}
