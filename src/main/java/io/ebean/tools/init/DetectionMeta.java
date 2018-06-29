package io.ebean.tools.init;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DetectionMeta {

  private Set<String> testClasspath = new LinkedHashSet<>();
  private Set<String> runtimeClasspath = new LinkedHashSet<>();

  private List<String> mainResources = new ArrayList<>();
  private List<String> testResources = new ArrayList<>();

  private File sourceJava;
  private File sourceKotlin;
  private List<String> mainSource;

  private File sourceTestJava;
  private File sourceTestKotlin;
  private List<String> testSource;

  private String mainOutput;
  private String testOutput;

  public List<String> getMainSource() {
    return mainSource;
  }

  public List<String> getTestSource() {
    return testSource;
  }

  public void setTestSource(List<String> testSource, File sourceTestJava, File sourceTestKotlin) {
    this.testSource = testSource;
    this.sourceTestJava = sourceTestJava;
    this.sourceTestKotlin = sourceTestKotlin;
  }

  public void setMainSource(List<String> mainSource, File sourceJava, File sourceKotlin) {
    this.mainSource = mainSource;
    this.sourceJava = sourceJava;
    this.sourceKotlin = sourceKotlin;
  }

  public void addResourceDirectory(String directory) {
    mainResources.add(directory);
  }

  public void addTestResourceDirectory(String directory) {
    testResources.add(directory);
  }

  public void addTestClassPath(List<String> classpathElements) {
    testClasspath.addAll(classpathElements);
  }

  public void addRuntimeClassPath(List<String> classpathElements) {
    runtimeClasspath.addAll(classpathElements);
  }

  public void setMainOutput(String mainOutput) {
    this.mainOutput = mainOutput;
  }

  public String getMainOutput() {
    return mainOutput;
  }

  public void setTestOutput(String testOutput) {
    this.testOutput = testOutput;
  }

  public String getTestOutput() {
    return testOutput;
  }

  public Set<String> getRuntimeClasspath() {
    return runtimeClasspath;
  }

  public List<String> getTestResources() {
    return testResources;
  }

  public List<String> getMainResources() {
    return mainResources;
  }

  public File getSourceJava() {
    return sourceJava;
  }

  public File getSourceKotlin() {
    return sourceKotlin;
  }

  public File getSourceTestJava() {
    return sourceTestJava;
  }

  public File getSourceTestKotlin() {
    return sourceTestKotlin;
  }
}
