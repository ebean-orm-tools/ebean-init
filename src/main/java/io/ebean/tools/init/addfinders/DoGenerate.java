package io.ebean.tools.init.addfinders;

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

  public DoGenerate(Detection detection, InteractionHelp help) {
    this.detection = detection;
    this.help = help;
  }

  public void generateQueryBeans() {

    GeneratorConfig config = createConfig();
    if (config == null) return;

    Generator generator = new Generator(config);
    try {
      generator.generateQueryBeans();

    } catch (IOException e) {
      help.acknowledge("Error " + e);
      e.printStackTrace();
    }
  }


  public void generateFinders() {

    GeneratorConfig config = createConfig();
    if (config == null) return;

    Generator generator = new Generator(config);
    try {
      generator.generateFinders();
      help.acknowledge("  ... generated finders: " + generator.getFinders());

      generator.modifyEntityBeansAddFinderField();
      help.acknowledge("   ... linked finders: " + generator.getFinderLinks());

    } catch (IOException e) {
      help.acknowledge("Error " + e);
      e.printStackTrace();
    }
  }

  private GeneratorConfig createConfig() {

    DetectionMeta meta = detection.getMeta();

    GeneratorConfig config = new GeneratorConfig();
    config.setClassesDirectory(meta.getMainOutput());

    boolean asKotlin = true;
    File source = meta.getSourceKotlin();

    List<File> ktDomains = detection.kotlinDomainDirs();
    if (ktDomains.isEmpty()) {
      source = meta.getSourceJava();
      asKotlin = false;
    }
    if (asKotlin) {
      config.setLang("kt");
    }

    //FIXME: This does not support multiple packages for entities?
    String entityPackage = detection.getEntityPackage();

    config.setDestDirectory(source.getAbsolutePath());

    help.acknowledge("  settings used - kotlin:" + asKotlin + " package:" + entityPackage);

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

  private List<File> touchedClasses;

  public void setEntityClasses(List<File> touchedClasses) {
    this.touchedClasses = touchedClasses;
  }
}
