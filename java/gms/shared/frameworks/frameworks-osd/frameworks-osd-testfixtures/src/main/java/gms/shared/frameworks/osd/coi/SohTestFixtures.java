package gms.shared.frameworks.osd.coi;

import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.soh.ChannelSoh;
import gms.shared.frameworks.osd.coi.soh.DurationSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.DurationStationAggregate;
import gms.shared.frameworks.osd.coi.soh.PercentSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.PercentStationAggregate;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.soh.StationAggregateType;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import gms.shared.frameworks.osd.coi.soh.quieting.SohStatusChange;
import gms.shared.frameworks.osd.coi.soh.quieting.UnacknowledgedSohStatusChange;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.GROUP_NAME;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.STATION;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SohTestFixtures {

  private SohTestFixtures() {
  }

  public static final String ALTERNATE_GROUP_NAME = "ALTERNATE_GROUP";

  public static final Instant NOW = Instant.now();

  public static final PercentSohMonitorValueAndStatus MARGINAL_MISSING_SOH_MONITOR_VALUE_AND_STATUS =
    PercentSohMonitorValueAndStatus.from(99.0, SohStatus.MARGINAL, SohMonitorType.MISSING);

  public static final PercentSohMonitorValueAndStatus MARGINAL_MISSING_EMPTY_SOH_MONITOR_VALUE_AND_STATUS =
    PercentSohMonitorValueAndStatus.from(null, SohStatus.MARGINAL, SohMonitorType.MISSING);

  public static final PercentSohMonitorValueAndStatus GOOD_MISSING_SOH_MONITOR_VALUE_AND_STATUS =
    PercentSohMonitorValueAndStatus.from(
      100.0,
      SohStatus.GOOD,
      SohMonitorType.MISSING);

  public static final PercentSohMonitorValueAndStatus BAD_MISSING_SOH_MONITOR_VALUE_AND_STATUS =
    PercentSohMonitorValueAndStatus.from(
      100.0,
      SohStatus.BAD,
      SohMonitorType.MISSING);

  public static final PercentSohMonitorValueAndStatus BAD_SEAL_BROKEN_SOH_MONITOR_VALUE_AND_STATUS =
    PercentSohMonitorValueAndStatus.from(
      99.0,
      SohStatus.BAD,
      SohMonitorType.ENV_AUTHENTICATION_SEAL_BROKEN);

  public static final DurationSohMonitorValueAndStatus MARGINAL_NULL_LAG_SOH_MONITOR_VALUE_AND_STATUS =
    DurationSohMonitorValueAndStatus.from(null, SohStatus.MARGINAL,
      SohMonitorType.LAG);

  public static final DurationSohMonitorValueAndStatus MARGINAL_LAG_SOH_MONITOR_VALUE_AND_STATUS =
    DurationSohMonitorValueAndStatus.from(Duration.ofHours(1),
      SohStatus.MARGINAL,
      SohMonitorType.LAG);

  public static final DurationSohMonitorValueAndStatus MARGINAL_TIMELINESS_SOH_MONITOR_VALUE_AND_STATUS =
    DurationSohMonitorValueAndStatus.from(Duration.ofHours(1),
      SohStatus.MARGINAL,
      SohMonitorType.TIMELINESS);

  public static final PercentStationAggregate MISSING_STATION_AGGREGATE =
    PercentStationAggregate.from(
      100.0,
      StationAggregateType.MISSING);

  public static final PercentStationAggregate SEAL_BROKEN_STATION_AGGREGATE =
    PercentStationAggregate.from(
      99.0,
      StationAggregateType.ENVIRONMENTAL_ISSUES);

  public static final DurationStationAggregate NULL_LAG_STATION_AGGREGATE =
    DurationStationAggregate.from(null, StationAggregateType.LAG);

  public static final DurationStationAggregate LAG_STATION_AGGREGATE =
    DurationStationAggregate.from(Duration.ofHours(1),
      StationAggregateType.LAG);

  public static final ChannelSoh BAD_LAG_MISSING_CHANNEL_SOH = ChannelSoh.from(
    UtilsTestFixtures.CHANNEL.getName(),
    SohStatus.BAD,
    Set.of(MARGINAL_MISSING_SOH_MONITOR_VALUE_AND_STATUS));

  public static final ChannelSoh MARGINAL_LAG_MISSING_CHANNEL_SOH = ChannelSoh.from(
    UtilsTestFixtures.CHANNEL.getName(),
    SohStatus.MARGINAL,
    Set.of(BAD_MISSING_SOH_MONITOR_VALUE_AND_STATUS));

  public static final ChannelSoh ALT_MARGINAL_LAG_MISSING_CHANNEL_SOH = ChannelSoh.from(
    "ALT-" + UtilsTestFixtures.CHANNEL.getName(),
    SohStatus.MARGINAL,
    Set.of(BAD_MISSING_SOH_MONITOR_VALUE_AND_STATUS));

  public static final ChannelSoh BAD_LAG_MISSING_CHANNEL_TWO_SOH = ChannelSoh.from(
    UtilsTestFixtures.CHANNEL_TWO.getName(),
    SohStatus.BAD,
    Set.of(MARGINAL_LAG_SOH_MONITOR_VALUE_AND_STATUS,
      MARGINAL_MISSING_SOH_MONITOR_VALUE_AND_STATUS));

  public static final ChannelSoh BAD_MISSING_LAG_SEAL_CHANNEL_SOH = ChannelSoh.from(
    UtilsTestFixtures.CHANNEL.getName(),
    SohStatus.BAD,
    Set.of(MARGINAL_LAG_SOH_MONITOR_VALUE_AND_STATUS,
      MARGINAL_MISSING_SOH_MONITOR_VALUE_AND_STATUS,
      BAD_SEAL_BROKEN_SOH_MONITOR_VALUE_AND_STATUS));

  public static final StationSoh MARGINAL_STATION_SOH = StationSoh.create(
    NOW,
    STATION.getName(),
    Set.of(
      MARGINAL_LAG_SOH_MONITOR_VALUE_AND_STATUS,
      BAD_SEAL_BROKEN_SOH_MONITOR_VALUE_AND_STATUS,
      BAD_MISSING_SOH_MONITOR_VALUE_AND_STATUS),
    SohStatus.MARGINAL,
    Set.of(BAD_MISSING_LAG_SEAL_CHANNEL_SOH,
      BAD_LAG_MISSING_CHANNEL_TWO_SOH),
    Set.of(MISSING_STATION_AGGREGATE)
  );

  public static final StationSoh BAD_STATION_SOH = StationSoh.create(
    NOW.minusSeconds(10L * 60),
    STATION.getName(),
    Set.of(MARGINAL_MISSING_SOH_MONITOR_VALUE_AND_STATUS),
    SohStatus.BAD,
    Set.of(BAD_LAG_MISSING_CHANNEL_SOH),
    Set.of(MISSING_STATION_AGGREGATE));

  public static final StationSoh SIMPLE_MARGINAL_STATION_SOH = StationSoh.create(
    NOW.minusSeconds(10L * 60),
    STATION.getName(),
    Set.of(MARGINAL_MISSING_SOH_MONITOR_VALUE_AND_STATUS),
    SohStatus.BAD,
    Set.of(MARGINAL_LAG_MISSING_CHANNEL_SOH),
    Set.of(MISSING_STATION_AGGREGATE));

  public static final StationSoh ALT_SIMPLE_MARGINAL_STATION_SOH = StationSoh.create(
    NOW.minusSeconds(10L * 60),
    "ALT-" + STATION.getName(),
    Set.of(MARGINAL_MISSING_SOH_MONITOR_VALUE_AND_STATUS),
    SohStatus.BAD,
    Set.of(ALT_MARGINAL_LAG_MISSING_CHANNEL_SOH),
    Set.of(MISSING_STATION_AGGREGATE));

  public static final CapabilitySohRollup MARGINAL_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP =
    CapabilitySohRollup.create(UUID.randomUUID(),
      NOW,
      SohStatus.MARGINAL,
      GROUP_NAME,
      Set.of(MARGINAL_STATION_SOH.getId()),
      Map.of(STATION.getName(), SohStatus.MARGINAL));

  public static final CapabilitySohRollup BAD_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP =
    CapabilitySohRollup.create(UUID.randomUUID(),
      NOW,
      SohStatus.BAD,
      GROUP_NAME,
      Set.of(BAD_STATION_SOH.getId()),
      Map.of(STATION.getName(), SohStatus.BAD));

  public static final CapabilitySohRollup MARGINAL_STATION_GROUP_BAD_STATION_CAPABILITY_ROLLUP =
    CapabilitySohRollup.create(UUID.randomUUID(),
      NOW,
      SohStatus.MARGINAL,
      GROUP_NAME,
      Set.of(MARGINAL_STATION_SOH.getId()),
      Map.of(STATION.getName(), SohStatus.BAD));

  public static final CapabilitySohRollup ALT_MARGINAL_STATION_GROUP_GOOD_STATION_CAPABILITY_ROLLUP =
    CapabilitySohRollup.create(UUID.randomUUID(),
      NOW,
      SohStatus.MARGINAL,
      ALTERNATE_GROUP_NAME,
      Set.of(MARGINAL_STATION_SOH.getId()),
      Map.of(STATION.getName(), SohStatus.GOOD));

  public static final CapabilitySohRollup ALT_MARGINAL_STATION_GROUP_BAD_STATION_CAPABILITY_ROLLUP =
    CapabilitySohRollup.create(UUID.randomUUID(),
      NOW,
      SohStatus.MARGINAL,
      ALTERNATE_GROUP_NAME,
      Set.of(MARGINAL_STATION_SOH.getId()),
      Map.of(STATION.getName(), SohStatus.BAD));

  public static final SohStatusChange CLIPPED_CHANGE =
    SohStatusChange.from(Instant.EPOCH,
      SohMonitorType.ENV_CLIPPED,
      UtilsTestFixtures.CHANNEL.getName());

  public static final SohStatusChange GAP_CHANGE =
    SohStatusChange.from(Instant.EPOCH,
      SohMonitorType.ENV_GAP,
      UtilsTestFixtures.CHANNEL.getName());

  public static final SohStatusChange SEAL_CHANGE =
    SohStatusChange.from(Instant.EPOCH,
      SohMonitorType.ENV_AUTHENTICATION_SEAL_BROKEN,
      UtilsTestFixtures.CHANNEL.getName());

  public static final UnacknowledgedSohStatusChange UNACK_CHANGE_1 =
    UnacknowledgedSohStatusChange.from(STATION.getName(), Set.of(CLIPPED_CHANGE, GAP_CHANGE));

  public static final UnacknowledgedSohStatusChange UNACK_CHANGE_2 =
    UnacknowledgedSohStatusChange.from(STATION.getName(), Set.of(CLIPPED_CHANGE, SEAL_CHANGE));

  public static final String STATION_NEEDS_ATTENTION_MESSAGE_TEMPLATE = SystemMessageType.STATION_NEEDS_ATTENTION
    .getMessageTemplate();
  public static final String STATION_SOH_STATUS_CHANGED_MESSAGE_TEMPLATE =
    SystemMessageType.STATION_SOH_STATUS_CHANGED
      .getMessageTemplate();
  public static final String STATION_CAPABILITY_STATUS_CHANGED_MESSAGE_TEMPLATE =
    SystemMessageType.STATION_CAPABILITY_STATUS_CHANGED
      .getMessageTemplate();
  public static final String STATION_GROUP_CAPABILITY_STATUS_CHANGED_MESSAGE_TEMPLATE =
    SystemMessageType.STATION_GROUP_CAPABILITY_STATUS_CHANGED
      .getMessageTemplate();
  public static final String CHANNEL_MONITOR_TYPE_STATUS_CHANGED_MESSAGE_TEMPLATE =
    SystemMessageType.CHANNEL_MONITOR_TYPE_STATUS_CHANGED
      .getMessageTemplate();
  public static final String CHANNEL_MONITOR_TYPE_STATUS_CHANGE_ACKNOWLEDGED_MESSAGE_TEMPLATE =
    SystemMessageType.CHANNEL_MONITOR_TYPE_STATUS_CHANGE_ACKNOWLEDGED
      .getMessageTemplate();
  public static final String CHANNEL_MONITOR_TYPE_QUIETED_MESSAGE_TEMPLATE =
    SystemMessageType.CHANNEL_MONITOR_TYPE_QUIETED
      .getMessageTemplate();
  public static final String CHANNEL_MONITOR_TYPE_QUIET_PERIOD_CANCELED_MESSAGE_TEMPLATE =
    SystemMessageType.CHANNEL_MONITOR_TYPE_QUIET_PERIOD_CANCELED
      .getMessageTemplate();
  public static final String CHANNEL_MONITOR_TYPE_QUIET_PERIOD_EXPIRED_MESSAGE_TEMPLATE =
    SystemMessageType.CHANNEL_MONITOR_TYPE_QUIET_PERIOD_EXPIRED
      .getMessageTemplate();
}
