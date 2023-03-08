package gms.core.performancemonitoring.soh.control.configuration;

import com.google.auto.value.AutoValue;

import java.util.Map;

/**
 * Describes a capability rollup calculation for a single Station in the context of a single
 * CapabilitySohRollupDefinition.
 */
@AutoValue
public abstract class StationRollupDefinition {

  /**
   * @return the RollupOperator used to compute the Station's SohStatus rollup from Channel
   * SohStatus rollups.
   */
  public abstract RollupOperator getChannelsToStationRollupOperator();

  /**
   * @return a collection of ChannelRollupDefinition objects which describe the calculations used to
   * produce the Channel rollup SohStatus objects that are input to the Station rollup calculation.
   */
  public abstract Map<String, ChannelRollupDefinition> getChannelRollupDefinitionsByChannel();

  /**
   * Create a new StationRollupDefinition object.
   *
   * @param channelsToStationRollupOperator the channelsToStationRollupOperator.
   * @param channelRollupDefinitionsByChannel the channelRollupDefinitionsByChannel.
   * @return a StationRollupDefinition object.
   */
  public static StationRollupDefinition from(
    RollupOperator channelsToStationRollupOperator,
    Map<String, ChannelRollupDefinition> channelRollupDefinitionsByChannel
  ) {
    return new AutoValue_StationRollupDefinition(channelsToStationRollupOperator,
      channelRollupDefinitionsByChannel);
  }
}
