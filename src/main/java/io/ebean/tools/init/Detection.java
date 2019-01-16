package io.ebean.tools.init;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class Detection {

  private final Set<String> domainDirectoryMatch = new HashSet<>();

  private final DetectionMeta meta;

  private boolean ebeanManifestFound;

  private boolean mainProperties;

  private boolean testProperties;

  /**
   * logback or log4j.
   */
  private String loggerType;

  /**
   * True when logging file found.
   */
  private boolean testLoggingFile;

  /**
   * True when the logging file contains an entry for io.ebean.SQL.
   */
  private boolean testLoggingEntry;

  private boolean usingQueryBeans;

  private final Set<String> entityPackages = new HashSet<>();

  private final Set<String> transactionalPackages = new HashSet<>();

  private final Set<String> queryBeanPackages = new HashSet<>();

  private final List<File> javaDomainDirs = new ArrayList<>();
  private final List<File> kotlinDomainDirs = new ArrayList<>();

  private File topJavaPackageDir;
  private File topKotlinPackageDir;

  private String dbMigrationFile;

  private SourceMode sourceMode;

  private boolean extraOptions;

  public Detection(DetectionMeta meta) {
    this.meta = meta;
    Collections.addAll(domainDirectoryMatch, "domain", "model", "entity", "entities");
    sourceMode = meta.getSourceMode();
  }

  public String toString() {
    return "mf:" + ebeanManifestFound + " entityPkgs:" + entityPackages + " txnPkgs:" + transactionalPackages + " qbPkgs:" + queryBeanPackages;
  }

  public boolean isExtraOptions() {
    return extraOptions;
  }

  public void setExtraOptions(boolean extraOptions) {
    this.extraOptions = extraOptions;
  }

  public void setUsingQueryBeans() {
    usingQueryBeans = true;
  }

  public boolean isUsingQueryBeans() {
    return usingQueryBeans;
  }

  public boolean isSourceModeKotlin() {
    return sourceMode == SourceMode.KOTLIN;
  }

  public void setSourceMode(SourceMode sourceMode) {
    this.sourceMode = sourceMode;
  }

  public boolean isEbeanManifestFound() {
    return ebeanManifestFound;
  }

  public boolean isMainProperties() {
    return mainProperties;
  }

  public boolean isTestPropertiesFound() {
    return testProperties;
  }

  public String getLoggerType() {
    return loggerType;
  }

  public boolean isTestLoggingFile() {
    return testLoggingFile;
  }

  public boolean isTestLoggingEntry() {
    return testLoggingEntry;
  }

  public boolean isDbMigration() {
    return dbMigrationFile != null;
  }

  public String getDbMigrationFile() {
    return dbMigrationFile;
  }

  public String getTopPackage() {
    if (topJavaPackageDir != null) {
      return diff(meta.getSourceJava(), topJavaPackageDir);
    }
    if (topKotlinPackageDir != null) {
      return diff(meta.getSourceKotlin(), topKotlinPackageDir);
    }
    return null;
  }

  public List<File> kotlinDomainDirs() {
    return kotlinDomainDirs;
  }

  public String getEntityPackage() {
    if (kotlinDomainDirs.size() == 1) {
      return diff(meta.getSourceKotlin(), kotlinDomainDirs.get(0));
    }
    if (javaDomainDirs.size() == 1) {
      return diff(meta.getSourceJava(), javaDomainDirs.get(0));
    }
    return null;
  }

  public List<String> getDetectedPackages() {
    List<String> list = new ArrayList<>();
    for (File domainDir : javaDomainDirs) {
      list.add(diff(meta.getSourceJava(), domainDir));
    }
    for (File domainDir : kotlinDomainDirs) {
      list.add(diff(meta.getSourceKotlin(), domainDir));
    }
    return list;
  }

  public void run() throws IOException {

    findMainProperties();
    findEbeanManifest();
    findTestProperties();
    // detect Kotlin or Java

    findGenerateDbMigration();
    findLogging();
    findTopLevelPackage();
  }

  private void findGenerateDbMigration() {
    findGenerateDbMigration(meta.getSourceTestJava());
    findGenerateDbMigration(meta.getSourceTestKotlin());
  }

  private void findGenerateDbMigration(File testSrc) {

    if (testSrc == null || !testSrc.exists()) {
      return;
    }

    File testMain = new File(testSrc, "main");
    if (testMain.exists()) {
      File[] files = testMain.listFiles();
      if (files != null) {
        for (File file : files) {
          if (file.getName().contains("DbMigration")) {
            dbMigrationFile = file.getName();
          }
        }
      }
    }
  }

  /**
   * Return the package based on the 2 directories.
   */
  private String diff(File top, File sub) {
    if (top == null || sub == null) {
      return null;
    }
    String relative = sub.getPath().substring(top.getPath().length() + 1);
    return relative.replace(File.separatorChar, '.');
  }

  private void findTopLevelPackage() {

    File sourceJava = meta.getSourceJava();
    if (sourceJava != null && sourceJava.exists()) {
      topJavaPackageDir = findUntilSplit(sourceJava);
      if (topJavaPackageDir != null) {
        findEntityDirs(topJavaPackageDir, javaDomainDirs);
      }
    }

    File sourceKotlin = meta.getSourceKotlin();
    if (sourceKotlin != null && sourceKotlin.exists()) {
      topKotlinPackageDir = findUntilSplit(sourceKotlin);
      if (topKotlinPackageDir != null) {
        findEntityDirs(topKotlinPackageDir, kotlinDomainDirs);
      }
    }
  }

  private void findEntityDirs(File dir, List<File> collect) {
    if (couldBeDomain(dir.getName())) {
      collect.add(dir);
    }
    File[] files = dir.listFiles();
    if (files != null) {
      for (File sub : files) {
        if (sub.isDirectory()) {

          findEntityDirs(sub, collect);
        }
      }
    }
  }

  private boolean couldBeDomain(String directoryName) {
    return domainDirectoryMatch.contains(directoryName);
  }

  private File findUntilSplit(File dir) {

    File[] files = dir.listFiles();
    if (files != null && files.length == 1 && files[0].isDirectory()) {
      return findUntilSplit(files[0]);
    } else {
      return dir;
    }
  }

  private void findTestProperties() {
    testProperties = (findTestResourceAny("application-test.yaml", "application-test.yml", "application-test.properties", "test-ebean.properties") != null);
  }


  private void findLogging() {
    findTestLoggingFor("logback-test.xml", "logback");
    findTestLoggingFor("log4j2.xml", "log4j");
  }

  private void findTestLoggingFor(String loggingFileName, String loggerType) {
    File logFile = findTestResource(loggingFileName);
    if (logFile != null) {
      this.testLoggingFile = true;
      this.loggerType = loggerType;
      this.testLoggingEntry = detectLoggingEntry(logFile);
    }
  }

  /**
   * Search logging config file for Ebean logging entry.
   */
  private boolean detectLoggingEntry(File logback) {
    try (LineNumberReader lineReader = new LineNumberReader(new FileReader(logback))) {
      String logLine;
      while ((logLine = lineReader.readLine()) != null) {
        if (containsLoggingEntry(logLine)) {
          return true;
        }
      }
      return false;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  private boolean containsLoggingEntry(String logLine) {
    return logLine.contains("io.ebean.SQL");
  }

  private File findTestResourceAny(String... names) {

    for (String name : names) {
      File testResource = meta.getTestResource();
      if (testResource != null) {
        File file = new File(testResource, name);
        if (file.exists()) {
          return file;
        }
      }
    }
    return null;
  }

  private File findTestResource(String name) {

    File testResource = meta.getTestResource();
    if (testResource != null) {
      File file = new File(testResource, name);
      if (file.exists()) {
        return file;
      }
    }
    return null;
  }

  private void findEbeanManifest() throws IOException {

    File mainResource = meta.getMainResource();
    if (mainResource != null && mainResource.exists()) {
      if (!loadEbeanManifest(new File(mainResource, "ebean.mf"))) {
        loadEbeanManifest(new File(mainResource, "META-INF/ebean.mf"));
      }
    }
  }

  private void findMainProperties() {
    File mainResource = meta.getMainResource();
    if (mainResource != null && mainResource.exists()) {
      if (exists(mainResource, "application.yaml")
        || exists(mainResource, "application.yml")
        || exists(mainResource, "application.properties")
        || exists(mainResource, "ebean.properties")) {
        mainProperties = true;
      }
    }
  }

  private boolean exists(File dir, String file) {
    return new File(dir, file).exists();
  }

  private boolean loadEbeanManifest(File file) throws IOException {

    if (!file.exists()) {
      return false;
    } else {
      try (FileInputStream is = new FileInputStream(file)) {

        Manifest manifest = new Manifest(is);
        addManifest(manifest);
        ebeanManifestFound = true;
        return true;
      }
    }
  }


  private void addManifest(Manifest manifest) {
    Attributes attributes = manifest.getMainAttributes();
    add(entityPackages, attributes.getValue("packages"));
    add(entityPackages, attributes.getValue("entity-packages"));
    add(transactionalPackages, attributes.getValue("transactional-packages"));
    add(queryBeanPackages, attributes.getValue("querybean-packages"));
    usingQueryBeans = !queryBeanPackages.isEmpty();
  }

  /**
   * Collect each individual package splitting by delimiters.
   */
  private void add(Set<String> addTo, String packages) {
    if (packages != null) {
      String[] split = packages.split(",|;| ");
      for (String aSplit : split) {
        String pkg = aSplit.trim();
        if (!pkg.isEmpty()) {
          addTo.add(pkg);
        }
      }
    }
  }


  public DetectionMeta getMeta() {
    return meta;
  }

  public void addedMainProperties() {
    mainProperties = true;
  }

  /**
   * Set that the ebean.mf file has been set.
   */
  public void addedEbeanManifest() {
    ebeanManifestFound = true;
  }

  /**
   * Set that the test-ebean.properties has been set.
   */
  public void addedTestProperties() {
    testProperties = true;
  }

  public void addedTestLogging() {
    testLoggingEntry = true;
    testLoggingFile = true;
  }

  public void addedGenerateMigration(String name) {
    dbMigrationFile = name;
  }

  public boolean unexpectedLocation() {
    return meta.unexpectedLocation();
  }
}
