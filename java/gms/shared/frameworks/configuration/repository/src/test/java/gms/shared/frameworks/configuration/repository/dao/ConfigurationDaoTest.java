package gms.shared.frameworks.configuration.repository.dao;

import gms.shared.frameworks.configuration.Configuration;
import gms.shared.frameworks.configuration.repository.dao.converter.ConfigurationDaoConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationDaoTest {

  private ConfigurationDao configurationDao1;
  private ConfigurationDao configurationDao2;

  @BeforeEach
  public void init() throws IOException {

    Configuration configuration = Configuration.from(
      "test-config", new ArrayList<>()
    );
    ConfigurationDaoConverter cdConverter = new ConfigurationDaoConverter();

    this.configurationDao1 = cdConverter.fromCoi(configuration);
    this.configurationDao2 = new ConfigurationDao();
    this.configurationDao2.setName("test-config");
  }

  @Test
  void testEquals() {

    assertEquals(this.configurationDao2, this.configurationDao1, "equals not correct for ConfigurationDao");
  }

  @Test
  void testHashCode() {
    Set<ConfigurationDao> configurationDaoSet = new LinkedHashSet<>();
    configurationDaoSet.add(this.configurationDao1);
    assertTrue(configurationDaoSet.contains(this.configurationDao2), "hash code not correct for ConfigurationDao");
  }
}