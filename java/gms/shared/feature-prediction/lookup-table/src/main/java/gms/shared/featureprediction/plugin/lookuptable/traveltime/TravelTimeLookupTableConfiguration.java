package gms.shared.featureprediction.plugin.lookuptable.traveltime;

import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.utilities.filestore.FileDescriptor;
import java.util.List;

public abstract class TravelTimeLookupTableConfiguration {

  private final ConfigurationConsumerUtility configurationConsumerUtility;
  private final SystemConfig systemConfig;

  protected TravelTimeLookupTableConfiguration(
    ConfigurationConsumerUtility configurationConsumerUtility,
    SystemConfig systemConfig) {
    this.configurationConsumerUtility = configurationConsumerUtility;
    this.systemConfig = systemConfig;
  }

  /**
   * Gets the FeaturePredictorDefinition that represents configuration
   *
   * @return FeaturePredictorDefinition
   */
  public TravelTimeLookupTableDefinition getTravelTimeLookupTableDefinition(
    String configurationName
  ) {

    String minioKeyPrefix = (String) configurationConsumerUtility.resolve(
      configurationName, List.of()
    ).get("minio_key_prefix");

    String minioBucket = systemConfig.getValue("minio-bucket-name");

    var fileDescriptor = FileDescriptor.create(minioBucket, minioKeyPrefix);

    return TravelTimeLookupTableDefinition.builder()
      .setFileDescriptor(fileDescriptor)
      .build();
  }

  public abstract TravelTimeLookupTableDefinition getTravelTimeLookupTableDefinition();
}
