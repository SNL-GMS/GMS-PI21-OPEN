package gms.shared.featureprediction.plugin.lookuptable.traveltime;

import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Ak135TravelTimeLookupTableConfiguration extends TravelTimeLookupTableConfiguration {

  @Autowired
  public Ak135TravelTimeLookupTableConfiguration(
    ConfigurationConsumerUtility configurationConsumerUtility,
    SystemConfig systemConfig) {
    super(configurationConsumerUtility, systemConfig);
  }

  @Override
  public TravelTimeLookupTableDefinition getTravelTimeLookupTableDefinition() {
    return super.getTravelTimeLookupTableDefinition("ak135-travel-time-lookup-table");
  }
}
