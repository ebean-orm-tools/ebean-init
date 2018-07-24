package io.ebean.tools.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Version {
  private Version() {

  }
  private static final Logger logger = LoggerFactory.getLogger(Version.class);

  private static String version = "unknown";
  static {
    try {
      Properties prop = new Properties();
      try (InputStream in = Main.class.getResourceAsStream("/META-INF/maven/io.ebean.tools/ebean-init/pom.properties")) {
        if (in != null) {
          prop.load(in);
          in.close();
          version = prop.getProperty("version");
        }
      }
    } catch (IOException e) {
      logger.warn("Could not determine version: {}", e.getMessage());
    }
  }

  public static String getVersion() {
    return version;
  }

}
