package gms.shared.frameworks.configuration.repository.client;

import gms.shared.frameworks.configuration.Configuration;
import gms.shared.frameworks.configuration.ConfigurationRepository;

import java.util.HashMap;
import java.util.Map;

public class UpdateConfigurationClientUtility {

  private UpdateConfigurationClientUtility() {
  }

  public static void store(ConfigurationRepository configurationRepository, Configuration configuration) {

    // TODO: verify inputs

    final Map<String, Configuration> keyValues = new HashMap<>();

    // Extract each Configuration
    keyValues.put(configuration.getName(), configuration);

    // TODO: some sort of exception if couldn't prepare the configurations for storage
    keyValues.values().forEach(configurationRepository::put);

    // TODO: exception if configuration could not be stored?
  }
}
