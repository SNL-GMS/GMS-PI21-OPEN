package gms.core.performancemonitoring.smds.application;

import gms.core.performancemonitoring.smds.service.SystemMessageDefinitionService;
import gms.shared.frameworks.service.ServiceGenerator;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemMessageDefinitionServiceApplication {
  private static final Logger logger = LoggerFactory.getLogger(SystemMessageDefinitionServiceApplication.class);

  public static void main(String[] args) {
    logger.info("Starting SystemMessageDefinitionServiceApplication");
    final SystemConfig config = SystemConfig.create("smds");
    ServiceGenerator.runService(
      SystemMessageDefinitionService.create(),
      config);
  }
}
