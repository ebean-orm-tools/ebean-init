package io.ebean.tools.init;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DetectionMeta {

  private Set<String> testClasspath = new LinkedHashSet<>();
  private Set<String> runtimeClasspath = new LinkedHashSet<>();

  private List<String> mainResources = new ArrayList<>();
  private List<String> testResources = new ArrayList<>();

  private List<String> mainSource;
  private List<String> testSource;

  private String mainOutput;
  private String testOutput;

  public List<String> getMainSource() {
    return mainSource;
  }

  public List<String> getTestSource() {
    return testSource;
  }

  public void setTestSource(List<String> testSource) {
    this.testSource = testSource;
  }

  public void setMainSource(List<String> mainSource) {
    this.mainSource = mainSource;
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
}
