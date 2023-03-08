package gms.core.performancemonitoring.soh.control.configuration;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ChannelRollupConfigurationOption implements CapabilitySohRollupOption {

  public abstract RollupOperator getSohMonitorsToChannelRollupOperator();

  public static ChannelRollupConfigurationOption create(
    RollupOperator sohMonitorsToChannelRollupOperator
  ) {

    return new AutoValue_ChannelRollupConfigurationOption(
      sohMonitorsToChannelRollupOperator
    );
  }

  @Override
  public RollupOperator rollupOperator() {
    return getSohMonitorsToChannelRollupOperator();
  }
}
