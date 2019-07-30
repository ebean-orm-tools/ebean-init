package io.ebean.tools.init.action;

import io.ebean.tools.init.Detection;
import io.ebean.tools.init.DetectionMeta;
import io.ebean.tools.init.InteractionHelp;
import io.ebean.typequery.generator.Generator;
import io.ebean.typequery.generator.GeneratorConfig;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DoGenerate {

  private final Detection detection;

  private final InteractionHelp help;

  private List<File> touchedClasses;

  public DoGenerate(Detection detection, InteractionHelp help) {
    this.detection = detection;
    this.help = help;
  }

  public void generateQueryBeans() {

    GeneratorConfig config = createConfig();
    if (config == null) {
      return;
    }

    Generator generator = new Generator(config);
    try {
      generator.generateQueryBeans();

    } catch (IOException e) {
      help.ackErr("Error " + e);
      e.printStackTrace();
    }
  }

  public void generateSingleFinder() {
    String entityForFinder = help.questionFinder();
    generateFinders(entityForFinder);
  }

  public void generateFinders() {
    generateFinders(null);
  }

  private void generateFinders(String filter) {

    GeneratorConfig config = createConfig();
    if (config == null) {
      return;
    }

    if (filter != null) {
      config.setEntityNameFilter(filter);
    }

    Generator generator = new Generator(config);
    try {
      generator.generateFinders();
      help.ackDone("... generated finders: " + generator.getFinders());

      generator.modifyEntityBeansAddFinderField();
      help.ackDone("... linked finders: " + generator.getFinderLinks());

    } catch (IOException e) {
      help.ackErr("Error " + e);
      e.printStackTrace();
    }
  }

  private GeneratorConfig createConfig() {

    DetectionMeta meta = detection.getMeta();

    File mainOutput = meta.getMainOutput();
    if (mainOutput == null || !mainOutput.exists()) {
      help.ackErr("Failed - Can not determine main classes output directory?");
      return null;
    }

    GeneratorConfig config = new GeneratorConfig();
    config.setClassesDirectory(mainOutput.getAbsolutePath());

    File source = meta.getSourceJava();

    boolean asKotlin = detection.isSourceModeKotlin();
    if (asKotlin) {
      config.setLang("kt");
      source = meta.getSourceKotlin();
      if (detection.kotlinDomainDirs().isEmpty()) {
        help.ackErr("Failed - Can not determine kotlin domain package?");
        return null;
      }
    }

    //FIXME: This does not support multiple packages for entities?
    String entityPackage = detection.getEntityPackage();

    config.setDestDirectory(source.getAbsolutePath());

    help.ackDone("settings used - kotlin:" + asKotlin + " package:" + entityPackage);

    config.setEntityBeanPackage(entityPackage);
    if (touchedClasses != null) {
      config.setEntityClassFiles(touchedClasses);
    }
    config.setAddFinderWherePublic(true);
    config.setOverwriteExistingFinders(false);
    config.setAddFinderWhereMethod(false);
    config.setAddFinderTextMethod(false);
    return config;
  }

  public void setEntityClasses(List<File> touchedClasses) {
    this.touchedClasses = touchedClasses;
  }
}
