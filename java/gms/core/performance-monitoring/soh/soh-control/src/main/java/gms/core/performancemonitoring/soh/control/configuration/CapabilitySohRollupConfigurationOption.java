package gms.core.performancemonitoring.soh.control.configuration;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class CapabilitySohRollupConfigurationOption implements CapabilitySohRollupOption {

  public abstract RollupOperator getStationsToGroupRollupOperator();

  public static CapabilitySohRollupConfigurationOption create(
    RollupOperator stationsToGroupRollupOperator
  ) {

    return new AutoValue_CapabilitySohRollupConfigurationOption(
      stationsToGroupRollupOperator
    );

  }

  @Override
  public RollupOperator rollupOperator() {
    return getStationsToGroupRollupOperator();
  }
}
