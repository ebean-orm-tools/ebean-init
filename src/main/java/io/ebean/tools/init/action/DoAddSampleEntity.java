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

    final File dir = makePackageDir();
    try {
      String suffix = suffix();
      final HttpGet.Response baseModel = HttpGet.get("BaseModel" + suffix);
      final HttpGet.Response customer = HttpGet.get("Customer" + suffix);
      final HttpGet.Response customerTest = HttpGet.get("CustomerTest" + suffix);

      if (baseModel.ok() && customer.ok() && customerTest.ok()) {
        writeSampleCode("BaseModel", baseModel, dir);
        writeSampleCode("Customer", customer, dir);
        writeSampleCode("CustomerTest", customerTest, dir);
        help.ackDone("...added sample code - BaseModel" + suffix + ", Customer" + suffix + " and CustomerTest" + suffix);

      } else {
        help.ackDone("Failed to obtain sample code from https://ebean.io :(");
      }

    } catch (Exception e) {
      help.ackDone("Error obtaining sample code - " + e.getMessage());
    }
  }

  private void writeSampleCode(String baseModel, HttpGet.Response res, File dir) throws IOException {

    File out = new File(dir, baseModel + suffix());

    try (FileWriter writer = new FileWriter(out)) {
      writer.append("package ").append(entityPackage).append(";").append(NEWLINE);
      writer.append(res.getContent());
    }
  }

  private String suffix() {
    return java ? ".java" : ".kt";
  }

  private File baseSourceDir() {
    return java ? meta.getSourceJava() : meta.getSourceKotlin();
  }

  private File makePackageDir() {

    final File baseDir = baseSourceDir();

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
