package gms.shared.frameworks.configuration.repository.dao.converter;

import gms.shared.frameworks.configuration.Configuration;
import gms.shared.frameworks.configuration.repository.dao.ConfigurationDao;
import gms.shared.frameworks.utilities.jpa.EntityConverter;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConfigurationDaoConverter implements EntityConverter<ConfigurationDao, Configuration> {

  @Override
  public ConfigurationDao fromCoi(Configuration configuration) {
    Objects.requireNonNull(configuration);
    var cd = new ConfigurationDao();
    cd.setName(configuration.getName());
    cd.setConfigurationOptionDaos(new LinkedHashSet<>());

    configuration.getConfigurationOptions().forEach(cfgOption -> {
      var cfgOptionConverter = new ConfigurationOptionDaoConverter(cd);
      cd.getConfigurationOptionDaos().add(cfgOptionConverter.fromCoi(cfgOption));
    });
    return cd;
  }

  @Override
  public Configuration toCoi(ConfigurationDao configurationDao) {
    Objects.requireNonNull(configurationDao);

    return Configuration.from(
      configurationDao.getName(),
      configurationDao.getConfigurationOptionDaos().stream().map(
        cfgOptionDao -> new ConfigurationOptionDaoConverter(configurationDao).toCoi(cfgOptionDao)).collect(
        Collectors.toList()));
  }

}
