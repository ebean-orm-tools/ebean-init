package io.ebean.tools.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitMojo  {

  private static final Logger log = LoggerFactory.getLogger(InitMojo.class);

  public void execute() {


    DetectionMeta meta = new DetectionMeta();

//    meta.addSourceRoots(project.getMainSource());
//    meta.setTestSource(project.getTestCompileSourceRoots());
//
//    //project.getBuild().getTestOutput();
//
//    meta.setMainOutput(project.getBuild().getMainOutput());
//    meta.setTestOutput(project.getBuild().getTestOutput());
//
//    for (Resource resource : project.getResources()) {
//      meta.addResourceDirectory(resource.getDirectory());
//    }
//
//    for (Resource resource : project.getTestResources()) {
//      meta.addTestResourceDirectory(resource.getDirectory());
//    }

    try {
//      meta.addTestClassPath(project.getTestClasspathElements());
//      meta.addRuntimeClassPath(project.getRuntimeClasspathElements());
//      meta.addRuntimeClassPath(project.getCompileClasspathElements());

      Detection detection = new Detection(meta);
      detection.run();

      Interaction interaction = new Interaction(detection);

      interaction.run();

    } catch (Exception e) {
      log.error("Error running detection on project", e);
    }

  }


}
