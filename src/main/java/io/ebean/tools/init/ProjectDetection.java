package io.ebean.tools.init;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectDetection {

  private boolean maven;
  private boolean gradle;

  private List<String> sourceDirs = new ArrayList();
  private List<String> sourceTestDirs = new ArrayList();

  private File sourceJava;
  private File sourceKotlin;

  private File outputDir;

  private File sourceTestJava;
  private File sourceTestKotlin;

  public DetectionMeta run() {

    File pom = new File("pom.xml");
    if (pom.exists()) {
      maven = true;
    }
    File gradleBuild = new File("build.gradle");
    if (gradleBuild.exists()) {
      gradle = true;
    }

    sourceJava = addPath("src/main/java", sourceDirs);
    sourceKotlin = addPath("src/main/kotlin", sourceDirs);

    sourceTestJava = addPath("src/test/java", sourceTestDirs);
    sourceTestKotlin = addPath("src/test/kotlin", sourceTestDirs);

    File mainResourcesDir = null;
    File testResourcesDir = null;

    File mainRes = new File("src/main/resources");
    if (mainRes.exists()) {
      mainResourcesDir = mainRes;
    }

    File testRes = new File("src/test/resources");
    if (testRes.exists()) {
      testResourcesDir = testRes;
    }

//    File testKotlin = new File("src/test/kotlin");
//    if (testKotlin.exists()) {
//      testSourceDir = testKotlin;
//    }

    File targetClass = new File("target/classes");
    if (targetClass.exists()) {
      outputDir = targetClass;
    }

    File outClass = new File("out/production/classes");
    if (outClass.exists()) {
      outputDir = outClass;
    }

    System.out.println("outputDir: "+ outputDir + " main: " + mainResourcesDir);

    System.out.println("sourceTestDirs: " + sourceTestDirs);


    DetectionMeta meta = new DetectionMeta();
    meta.setMainSource(sourceDirs, sourceJava, sourceKotlin);
    meta.setMainOutput(outputDir.getAbsolutePath());

    meta.setTestSource(sourceTestDirs, sourceTestJava, sourceTestKotlin);
    meta.setTestOutput(meta.getMainOutput());

    if (mainResourcesDir != null) {
      meta.addResourceDirectory(mainResourcesDir.getAbsolutePath());
    }

    if (testResourcesDir != null) {
      meta.addTestResourceDirectory(testResourcesDir.getAbsolutePath());
    }

    return meta;
  }

  private File addPath(String path, List<String> dirs) {
    File dir = new File(path);
    if (dir.exists()) {
      dirs.add(dir.getAbsolutePath());
      return dir;
    }
    return null;
  }

  private boolean add(String path, List<File> dirs) {
    File dir = new File(path);
    if (dir.exists()) {
      dirs.add(dir);
      return true;
    }
    return false;
  }

  private boolean fileExists(String path) {
    return new File(path).exists();
  }
}
