package gms.shared.frameworks.processing.configuration.service;

import gms.shared.frameworks.configuration.repository.JpaConfigurationRepository;
import gms.shared.frameworks.service.ServiceGenerator;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProcessingConfigurationServiceApplication {

  private static final Logger logger = LoggerFactory.getLogger(ProcessingConfigurationServiceApplication.class);

  public static void main(String[] args) {
    final SystemConfig config = SystemConfig.create("processing-cfg");

    try {
      var configRepository = new JpaConfigurationRepository(config);
      ServiceGenerator.runService(configRepository, config);
    } catch (Exception e) {
      logger.error("Processing Configuration Service encountered an unrecoverable exception: ", e);
      System.exit(1);
    }
  }
}
