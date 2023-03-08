package gms.shared.featureprediction.plugin.lookuptable.traveltime;

import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IaspeiTravelTimeLookupTableConfiguration extends TravelTimeLookupTableConfiguration {

  @Autowired
  public IaspeiTravelTimeLookupTableConfiguration(
    ConfigurationConsumerUtility configurationConsumerUtility,
    SystemConfig systemConfig) {
    super(configurationConsumerUtility, systemConfig);
  }

  @Override
  public TravelTimeLookupTableDefinition getTravelTimeLookupTableDefinition() {
    return super.getTravelTimeLookupTableDefinition("iaspei-travel-time-lookup-table");
  }
}
