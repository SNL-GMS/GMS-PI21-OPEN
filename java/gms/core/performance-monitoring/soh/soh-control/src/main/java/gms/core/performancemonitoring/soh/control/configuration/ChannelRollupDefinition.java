package gms.core.performancemonitoring.soh.control.configuration;

import com.google.auto.value.AutoValue;

/**
 * Describes a capability rollup calculation for a single raw Channel in the context of a single
 * CapabilitySohRollupDefinition.
 */
@AutoValue
public abstract class ChannelRollupDefinition {

  /**
   * @return the RollupOperator used to compute the Channel's SohStatus rollup from Channel's
   * SohMonitorValueAndStatus objects.
   */
  public abstract RollupOperator getSohMonitorsToChannelRollupOperator();

  /**
   * Create a new ChannelRollupDefinition object.
   *
   * @param sohMonitorsToChannelRollupOperator the sohMonitorsToChannelRollupOperator.
   * @return a ChannelRollupDefinition object.
   */
  public static ChannelRollupDefinition from(
    RollupOperator sohMonitorsToChannelRollupOperator
  ) {
    return new AutoValue_ChannelRollupDefinition(sohMonitorsToChannelRollupOperator);
  }
}
