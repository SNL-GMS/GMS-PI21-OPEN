package gms.shared.frameworks.configuration.repository.dao;

import gms.shared.frameworks.configuration.Configuration;
import gms.shared.frameworks.configuration.ConfigurationOption;
import gms.shared.frameworks.configuration.Constraint;
import gms.shared.frameworks.configuration.repository.dao.converter.ConfigurationDaoConverter;
import gms.shared.frameworks.configuration.repository.dao.converter.ConfigurationOptionDaoConverter;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationOptionDaoTest {

  private ConfigurationOptionDao configurationOptionDao1;
  private ConfigurationOptionDao configurationOptionDao2;
  private List<Constraint> constraints;

  @BeforeEach
  public void init() throws IOException {
    Collection<ConfigurationOption> configurationOptions = new ArrayList<>();
    this.constraints = new ArrayList<>();
    Map<String, Object> parameters = new LinkedHashMap<>();
    parameters.put("some-string", "some-value");
    ConfigurationOption co = ConfigurationOption.from("config-option-test", this.constraints, parameters);
    configurationOptions.add(co);

    Configuration configuration = Configuration.from(
      "test-config", configurationOptions
    );
    ConfigurationDaoConverter cdConverter = new ConfigurationDaoConverter();
    ConfigurationOptionDaoConverter codConverter = new ConfigurationOptionDaoConverter(cdConverter.fromCoi(configuration));

    this.configurationOptionDao1 = codConverter.fromCoi(co);

    this.configurationOptionDao2 = new ConfigurationOptionDao();
    this.configurationOptionDao2.setName("config-option-test");
    this.configurationOptionDao2.setConfigurationDao(cdConverter.fromCoi(configuration));
    parameters = new LinkedHashMap<>();
    parameters.put("some-string", "some-value");
    this.configurationOptionDao2.setParameters(CoiObjectMapperFactory.getJsonObjectMapper().valueToTree(parameters));
  }

  @Test
  void testEquals() {

    assertEquals(this.configurationOptionDao1, this.configurationOptionDao2, "Equals not correct for ConfigurationOptionDao");
  }

  @Test
  void testHashCode() {
    Set<ConfigurationOptionDao> configurationOptionDaoSet = new LinkedHashSet<>();
    configurationOptionDaoSet.add(this.configurationOptionDao1);
    assertTrue(configurationOptionDaoSet.contains(this.configurationOptionDao2), "HashCode not correct for ConfigurationOptionDao");
  }
}