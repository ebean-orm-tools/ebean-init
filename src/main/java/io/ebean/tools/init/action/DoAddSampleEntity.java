package io.ebean.tools.init.action;

import io.ebean.tools.init.DetectionMeta;
import io.ebean.tools.init.InteractionHelp;
import io.ebean.tools.init.SourceMode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DoAddSampleEntity {

  private static final String NEWLINE = "\n";

  private final InteractionHelp help;
  private final DetectionMeta meta;
  private final boolean java;

  private String entityPackage;

  DoAddSampleEntity(InteractionHelp help) {
    this.help = help;
    this.meta = help.meta();
    this.java = (meta.getSourceMode() == SourceMode.JAVA);
  }

  public void run() {

    entityPackage = help.ask("Enter a package that will contain the entity beans");

    help.setEntityBeanPackage(entityPackage);
    addEntitySample();
  }

  private void addEntitySample() {

    final File mainDir = makeMainPackageDir();
    final File testDir = makeTestPackageDir();
    try {
      String suffix = suffix();
      final HttpGet.Response baseModel = HttpGet.get("BaseModel" + suffix);
      final HttpGet.Response customer = HttpGet.get("Customer" + suffix);
      final HttpGet.Response customerTest = HttpGet.get("CustomerTest" + suffix);

      if (baseModel.ok() && customer.ok() && customerTest.ok()) {
        writeSampleCode("BaseModel", baseModel, mainDir);
        writeSampleCode("Customer", customer, mainDir);
        writeSampleCode("CustomerTest", customerTest, testDir, true);
        help.ackDone("...added sample code - BaseModel" + suffix + ", Customer" + suffix + " and CustomerTest" + suffix);

      } else {
        help.ackDone("Failed to obtain sample code from https://ebean.io :(");
      }

    } catch (Exception e) {
      help.ackDone("Error obtaining sample code - " + e.getMessage());
    }
  }
  private void writeSampleCode(String baseModel, HttpGet.Response res, File dir) throws IOException {
    writeSampleCode(baseModel, res, dir, false);
  }

  private void writeSampleCode(String baseModel, HttpGet.Response res, File dir, boolean test) throws IOException {

    File out = new File(dir, baseModel + suffix());

    try (FileWriter writer = new FileWriter(out)) {
      String end = java ? ";" : "";
      writer.append("package ").append(entityPackage).append(end).append(NEWLINE);
      if (test) {
        writer.append(NEWLINE);
        writer.append("import ").append(entityPackage).append(".query.QCustomer").append(end).append(NEWLINE);
      }
      writer.append(res.getContent());
    }
  }

  private String suffix() {
    return java ? ".java" : ".kt";
  }

  private File baseSourceMainDir() {
    return java ? meta.getSourceJava() : meta.getSourceKotlin();
  }

  private File baseSourceTestDir() {
    return java ? meta.getSourceTestJava() : meta.getSourceTestKotlin();
  }

  private File makeMainPackageDir() {
    return makePackageDir(baseSourceMainDir());
  }

  private File makeTestPackageDir() {
    return makePackageDir(baseSourceTestDir());
  }

  private File makePackageDir(File baseDir) {

    final String dir = entityPackage.replace(".", File.separator);

    File packageDir = new File(baseDir, dir);
    if (!packageDir.exists()) {
      if (!packageDir.mkdirs()) {
        help.ackErr("Failed to create package directory - " + packageDir.getAbsolutePath());
      }
    }

    return packageDir;
  }
}
