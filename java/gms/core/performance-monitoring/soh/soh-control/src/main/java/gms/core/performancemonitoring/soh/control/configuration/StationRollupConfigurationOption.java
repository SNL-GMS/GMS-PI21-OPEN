package gms.core.performancemonitoring.soh.control.configuration;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class StationRollupConfigurationOption implements CapabilitySohRollupOption {

  public abstract RollupOperator getChannelsToStationRollupOperator();

  public static StationRollupConfigurationOption create(
    RollupOperator channelsToStationRollupOperator
  ) {

    return new AutoValue_StationRollupConfigurationOption(
      channelsToStationRollupOperator
    );
  }

  @Override
  public RollupOperator rollupOperator() {
    return getChannelsToStationRollupOperator();
  }
}
