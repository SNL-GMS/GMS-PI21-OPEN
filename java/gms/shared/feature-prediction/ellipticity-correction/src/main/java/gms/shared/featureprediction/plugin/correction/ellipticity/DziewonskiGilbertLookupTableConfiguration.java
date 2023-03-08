package gms.shared.featureprediction.plugin.correction.ellipticity;

import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.systemconfig.SystemConfig;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Handles retrieving {@link DziewonskiGilbertLookupTableDefinition}. from
 * configuration.
 */
@Configuration
public class DziewonskiGilbertLookupTableConfiguration {

  private static final String D_G_LOOKUP_TABLE_CONFIG = "feature-prediction-service.dziewonski-gilbert-lookup-table";

  private final ConfigurationConsumerUtility configurationConsumerUtility;
  private final SystemConfig systemConfig;

  
  /**
   * Creates a new DziewonskiGilbertLookupTableConfiguration instance.
   *
   * @param configurationConsumerUtility Used to retrieve
   * DziewonskiGilbertLookupTableDefinitions from configuration.
   * @param systemConfig Used to read MinIO bucket name from configuration.
   */
  @Autowired
  public DziewonskiGilbertLookupTableConfiguration(ConfigurationConsumerUtility configurationConsumerUtility, SystemConfig systemConfig) {
    this.configurationConsumerUtility = configurationConsumerUtility;
    this.systemConfig = systemConfig;
  }

  /**
   * Returns the default DziewonskiGilbertLookupTableDefinition from
   * configuration.
   *
   * @return The default DziewonskiGilbertLookupTableDefinition from
   * configuration.
   */
  @Bean
  public DziewonskiGilbertLookupTableDefinition getCurrentDiezwonskiGilbertLookupTableDefinition() {
    return configurationConsumerUtility.resolve(D_G_LOOKUP_TABLE_CONFIG, new ArrayList<>(), DziewonskiGilbertLookupTableDefinition.class);
  }
  
  /**
   * Returns the name of the MinIO bucket to retrieve {@link DziewonskiGilbertLookupTableView}s from.
   * @return Name of the MinIO bucket to retrieve DziewonskiGilbertLookupTableView from.
   */
  public String minIoBucketName() {
    return systemConfig.getValue("minio-bucket-name");
  }

}
