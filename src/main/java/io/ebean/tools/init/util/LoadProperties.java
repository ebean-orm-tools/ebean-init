package io.ebean.tools.init.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper to load configuration properties.
 */
class LoadProperties {

  private static final Logger log = LoggerFactory.getLogger(LoadProperties.class);

//  static Properties load() throws IOException {
//    return new LoadProperties().readProperties(log);
//  }
//
//  private Properties readProperties(Log log) throws IOException {
//
//    Properties properties = new Properties();
//
//    File configFile = getConfigFile();
//    if (configFile != null && configFile.exists()) {
//      log.info("loading task properties from " + configFile.getAbsolutePath());
//      FileInputStream is = new FileInputStream(configFile);
//      properties.load(is);
//    }
//    return properties;
//  }
//
//  private File getConfigFile() {
//    String config = System.getProperty("config");
//    if (config != null) {
//      File configFile = new File(config);
//      if (!configFile.exists()) {
//        throw new IllegalArgumentException("config file " + config + " not found?");
//      }
//      return configFile;
//    }
//
//    File configFile = new File("config/ebean-codegen.properties");
//    if (!configFile.exists()) {
//      configFile = new File("ebean-codegen.properties");
//      if (!configFile.exists()) {
//        return null;
//      }
//    }
//
//    return configFile;
//  }

}
