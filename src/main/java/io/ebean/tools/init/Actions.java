package io.ebean.tools.init;

public class Actions {

  private boolean addEbeanManifest;

  private String manifestEntityPackage;

  private boolean addTestProperties;

  private boolean quitState;
  private String manifestTransactionalPackage;
  private String manifestQueryBeanPackage;

  public String checkState(String answer) {
    if ("Q".equalsIgnoreCase(answer)) {
      quitState = true;
    }
    return answer;
  }

  public void setAddEbeanManifest(boolean addEbeanManifest) {
    this.addEbeanManifest = addEbeanManifest;
  }

  public boolean isAddEbeanManifest() {
    return addEbeanManifest;
  }

  public void setManifestEntityPackage(String answer) {
    this.manifestEntityPackage = checkState(answer);
  }

  public String getManifestEntityPackage() {
    return manifestEntityPackage;
  }

  public void setAddTestProperties(boolean addTestProperties) {
    this.addTestProperties = addTestProperties;
  }

  public boolean isAddTestProperties() {
    return addTestProperties;
  }

  public boolean continueState() {
    return !quitState;
  }

  public String readLine() {
    String answer = System.console().readLine();
    answer = answer.trim();
    return checkState(answer);
  }

  public void setManifestTransactionalPackage(String manifestTransactionalPackage) {
    this.manifestTransactionalPackage = manifestTransactionalPackage;
  }

  public String getManifestTransactionalPackage() {
    return manifestTransactionalPackage;
  }

  public void setManifestQueryBeanPackage(String manifestQueryBeanPackage) {
    this.manifestQueryBeanPackage = manifestQueryBeanPackage;
  }

  public String getManifestQueryBeanPackage() {
    return manifestQueryBeanPackage;
  }
}
