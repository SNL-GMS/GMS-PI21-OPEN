package gms.shared.frameworks.configuration.repository.dao.converter;

import gms.shared.frameworks.configuration.Configuration;
import gms.shared.frameworks.configuration.ConfigurationOption;
import gms.shared.frameworks.configuration.Constraint;
import gms.shared.frameworks.configuration.constraints.BooleanConstraint;
import gms.shared.frameworks.configuration.constraints.DefaultConstraint;
import gms.shared.frameworks.configuration.repository.dao.ConfigurationDao;
import gms.shared.frameworks.configuration.repository.dao.ConfigurationOptionDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationDaoConverterTest {

  private Set<ConfigurationOptionDao> configurationOptionDaos;
  private Configuration configuration;

  @BeforeEach
  public void init() throws IOException {
    this.configurationOptionDaos = new LinkedHashSet<>();
    List<ConfigurationOption> configurationOptions = new ArrayList<>();

    List<Constraint> constraints = new ArrayList<>();
    BooleanConstraint bc = BooleanConstraint.from("test-criterion", true, 1);
    constraints.add(bc);
    DefaultConstraint dc = DefaultConstraint.from();
    constraints.add(dc);
    Map<String, Object> parameters = new LinkedHashMap<>();
    ConfigurationOption co = ConfigurationOption.from("config-option-test", constraints, parameters);
    ConfigurationOptionDaoConverter codConverter = new ConfigurationOptionDaoConverter(null);
    ConfigurationOptionDao cod = codConverter.fromCoi(co);
    this.configurationOptionDaos.add(cod);
    configurationOptions.add(co);
    this.configuration = Configuration.from("test-config", configurationOptions);
  }

  @Test
  void toCoi() {
    ConfigurationDaoConverter cdc = new ConfigurationDaoConverter();
    ConfigurationDao cd = new ConfigurationDao();
    cd.setName("test-config");
    cd.setConfigurationOptionDaos(this.configurationOptionDaos);
    Configuration cfg = cdc.toCoi(cd);
    assertEquals("test-config", cfg.getName(), "Configuration has incorrect name");
    assertEquals(1, cfg.getConfigurationOptions().size(), "ConfigurationOption list is wrong size");
    cfg.getConfigurationOptions().forEach(cgOption -> {
      assertEquals(2, cgOption.getConstraints().size(), "Constraint list is wrong size");
    });
  }

  @Test
  void fromCoi() {
    ConfigurationDaoConverter cdc = new ConfigurationDaoConverter();
    ConfigurationDao configurationDao = cdc.fromCoi(this.configuration);

    assertEquals("test-config", configurationDao.getName(), "ConfigurationDao has incorrect name");
    assertEquals(1, configurationDao.getConfigurationOptionDaos().size(), "ConfigurationOptionDao list is wrong size");
    configurationDao.getConfigurationOptionDaos().forEach(cgOption -> {
      assertEquals(2, cgOption.getConstraintDaos().size(), "ConstraintDao list is wrong size");
    });
  }
}