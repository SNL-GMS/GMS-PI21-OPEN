package gms.core.performancemonitoring.smds.service;

import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageDefinition;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


/**
 *
 */
public class SystemMessageDefinitionService implements SystemMessageDefinitionInterface {

  private SystemMessageDefinitionService() {
  }

  /**
   * Creates a new SystemMessageDefinitionService
   */
  public static SystemMessageDefinitionService create() {
    return new SystemMessageDefinitionService();
  }

  @Override
  public Set<SystemMessageDefinition> getSystemMessageDefinitions(String placeholder) {
    return Arrays.stream(SystemMessageType.values())
      .map(SystemMessageDefinition::from).collect(Collectors.toSet());
  }

}
