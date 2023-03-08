package gms.core.performancemonitoring.soh.control.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;

import java.util.Set;


/**
 * Contains configuration values for either a {@link gms.shared.frameworks.osd.coi.signaldetection.Station}
 * or {@link gms.shared.frameworks.osd.coi.channel.Channel}'s sohMonitorTypesForRollup configuration
 * value.
 */
@AutoValue
public abstract class SohMonitorTypesForRollupConfigurationOption {


  public abstract Set<SohMonitorType> getSohMonitorTypesForRollup();


  @JsonCreator
  public static SohMonitorTypesForRollupConfigurationOption create(
    @JsonProperty("sohMonitorTypesForRollup") Set<SohMonitorType> sohMonitorTypesForRollup) {

    return new AutoValue_SohMonitorTypesForRollupConfigurationOption(sohMonitorTypesForRollup);
  }
}
