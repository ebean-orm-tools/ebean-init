package io.ebean.tools.init.action;

import io.ebean.tools.init.InteractionHelp;

import java.io.File;

public class DoLocalDevelopment {

  public void run(InteractionHelp help) {

    File ignoreFile = getIgnoreFile();
    if (!ignoreFile.exists()) {
      if (!ignoreFile.getParentFile().exists() && !ignoreFile.getParentFile().mkdirs()) {
        System.err.println("Failed to create directory " + ignoreFile.getParentFile().getAbsolutePath());
      }
      try {
        if (ignoreFile.createNewFile()) {
          help.ackDone("  ... added " + ignoreFile.getAbsolutePath());
        }
      } catch (Exception e) {
        System.err.println("Failed to create file " + ignoreFile.getAbsolutePath());
        e.printStackTrace();
      }
    }
  }

  public static boolean exists() {
    return getIgnoreFile().exists();
  }

  private static File getIgnoreFile() {
    File homeDir = new File(System.getProperty("user.home"));
    return new File(homeDir, ".ebean/ignore-docker-shutdown");
  }

}
