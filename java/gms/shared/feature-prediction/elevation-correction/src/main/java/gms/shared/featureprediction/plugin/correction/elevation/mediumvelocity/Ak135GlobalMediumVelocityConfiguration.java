package gms.shared.featureprediction.plugin.correction.elevation.mediumvelocity;

import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.systemconfig.SystemConfig;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.Objects.requireNonNull;

@Configuration
public class Ak135GlobalMediumVelocityConfiguration {

  private final ConfigurationConsumerUtility configurationConsumerUtility;
  private final SystemConfig systemConfig;

  @Autowired
  public Ak135GlobalMediumVelocityConfiguration(
    ConfigurationConsumerUtility configurationConsumerUtility,
    SystemConfig systemConfig) {
    this.configurationConsumerUtility = requireNonNull(configurationConsumerUtility);
    this.systemConfig = requireNonNull(systemConfig);
  }

  @Bean
  public Ak135GlobalMediumVelocityDefinition ak135GlobalMediumVelocityDefinition() {
    var rawObject = configurationConsumerUtility.resolve(
      "feature-prediction-service.ak135-global-medium-velocity", List.of()
    ).get("dataDescriptor");

    if (rawObject instanceof String) {
      return Ak135GlobalMediumVelocityDefinition.create((String) rawObject);
    } else {
      throw new IllegalStateException("Ak135GlobalMediumVelocityConfiguration: dataDescriptor is not a String!");
    }
  }

  public String minIoBucketName() {
    return systemConfig.getValue("minio-bucket-name");
  }
}
