package gms.shared.frameworks.configuration.repository.dao.converter;

import gms.shared.frameworks.configuration.Configuration;
import gms.shared.frameworks.configuration.ConfigurationOption;
import gms.shared.frameworks.configuration.Constraint;
import gms.shared.frameworks.configuration.constraints.BooleanConstraint;
import gms.shared.frameworks.configuration.constraints.DefaultConstraint;
import gms.shared.frameworks.configuration.repository.dao.ConfigurationOptionDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationOptionDaoConverterTest {

  private Configuration configuration;
  private ConfigurationOption co;

  @BeforeEach
  public void init() throws IOException {
    Collection<ConfigurationOption> configurationOptions = new ArrayList<>();
    List<Constraint> constraints = new ArrayList<>();
    BooleanConstraint bc = BooleanConstraint.from("test-criterion", true, 1);
    constraints.add(bc);
    DefaultConstraint dc = DefaultConstraint.from();
    constraints.add(dc);
    Map<String, Object> parameters = new LinkedHashMap<>();
    this.co = ConfigurationOption.from("config-option-test", constraints, parameters);
    configurationOptions.add(this.co);

    this.configuration = Configuration.from(
      "test-config", configurationOptions
    );
  }

  @Test
  void toCoi() {
    ConfigurationDaoConverter cdConverter = new ConfigurationDaoConverter();
    ConfigurationOptionDaoConverter codConverter = new ConfigurationOptionDaoConverter(cdConverter.fromCoi(this.configuration));
    ConfigurationOptionDao configurationOptionDao = codConverter.fromCoi(this.co);
    ConfigurationOption cfgOption = codConverter.toCoi(configurationOptionDao);

    assertEquals("config-option-test", cfgOption.getName(), "ConfigurationOption has wrong name");
    assertEquals(2, cfgOption.getConstraints().size(), "Constraint list is wrong size");
  }

  @Test
  void fromCoi() {
    ConfigurationDaoConverter cdConverter = new ConfigurationDaoConverter();
    ConfigurationOptionDaoConverter codConverter = new ConfigurationOptionDaoConverter(cdConverter.fromCoi(this.configuration));
    ConfigurationOptionDao configurationOptionDao = codConverter.fromCoi(this.co);
    ConfigurationOption cfgOption = codConverter.toCoi(configurationOptionDao);

    assertEquals("config-option-test", cfgOption.getName(), "cfgOption has the wrong name");
    assertEquals(2, cfgOption.getConstraints().size(), "Size of Constraint list should be 2");
  }
}