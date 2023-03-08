package gms.core.performancemonitoring.uimaterializedview.utils;

import gms.core.performancemonitoring.soh.control.configuration.ChannelSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.DurationSohMonitorStatusThresholdDefinition;
import gms.core.performancemonitoring.soh.control.configuration.PercentSohMonitorStatusThresholdDefinition;
import gms.core.performancemonitoring.soh.control.configuration.SohMonitorStatusThresholdDefinition;
import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.TimeWindowDefinition;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringDefinition;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringDisplayParameters;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringUiClientParameters;
import gms.core.performancemonitoring.uimaterializedview.AcknowledgedSohStatusChange;
import gms.core.performancemonitoring.uimaterializedview.QuietedSohStatusChangeUpdate;
import gms.core.performancemonitoring.uimaterializedview.UiChannelSoh;
import gms.core.performancemonitoring.uimaterializedview.UiSohContributor;
import gms.core.performancemonitoring.uimaterializedview.UiSohMonitorValueAndStatus;
import gms.core.performancemonitoring.uimaterializedview.UiSohStatus;
import gms.core.performancemonitoring.uimaterializedview.UiStationAndStationGroups;
import gms.core.performancemonitoring.uimaterializedview.UiStationGroupSoh;
import gms.core.performancemonitoring.uimaterializedview.UiStationSoh;
import gms.core.performancemonitoring.uimaterializedview.UiStationSohCapabilityStatus;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.MARGINAL_LAG_SOH_MONITOR_VALUE_AND_STATUS;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.MARGINAL_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.MARGINAL_STATION_SOH;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.soh.quieting.SohStatusChange;
import gms.shared.frameworks.osd.coi.soh.quieting.UnacknowledgedSohStatusChange;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.CHANNEL;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.CHANNEL_TWO;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.GROUP_NAME;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.STATION;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


public class MaterializedViewTestFixtures {

  private static final Set<SohMonitorType> MONITOR_TYPES = Set.of(SohMonitorType.ENV_GAP,
    SohMonitorType.MISSING,
    SohMonitorType.ENV_CLIPPED);

  private static final Map<SohMonitorType, TimeWindowDefinition> TIME_WINDOW_DEFINITION_MAP =
    MONITOR_TYPES.stream().map(item ->
        new AbstractMap.SimpleEntry<>(item,
          TimeWindowDefinition.create(Duration.ofMinutes(5), Duration.ofSeconds(30))))
      .collect(
        Collectors.toMap(Entry::getKey, Entry::getValue));

  private static final Set<String> CHANNEL_NAMES = UtilsTestFixtures.STATION.getChannels()
    .stream()
    .map(Channel::getName)
    .collect(Collectors.toSet());


  private static final PercentSohMonitorStatusThresholdDefinition GAP_DEFINITION =
    PercentSohMonitorStatusThresholdDefinition.create(0.2, 0.5);

  private static final PercentSohMonitorStatusThresholdDefinition MISSING_DEFINITION =
    PercentSohMonitorStatusThresholdDefinition.create(0.2, 0.6);

  private static final PercentSohMonitorStatusThresholdDefinition CLIPPED_DEFINITION =
    PercentSohMonitorStatusThresholdDefinition.create(0.1, 0.3);

  private static final DurationSohMonitorStatusThresholdDefinition LATENCY_DEFINITION =
    DurationSohMonitorStatusThresholdDefinition
      .create(Duration.ofMinutes(30), Duration.ofMinutes(65));

  private static final PercentSohMonitorStatusThresholdDefinition SEAL_DEFINITION =
    PercentSohMonitorStatusThresholdDefinition.create(10.0, 50.0);

  private static final Map<SohMonitorType, SohMonitorStatusThresholdDefinition<?>> DEFINITIONS_BY_TYPE =
    Map.of(SohMonitorType.ENV_GAP, GAP_DEFINITION,
      SohMonitorType.MISSING, MISSING_DEFINITION,
      SohMonitorType.ENV_CLIPPED, CLIPPED_DEFINITION,
      SohMonitorType.LAG, LATENCY_DEFINITION,
      SohMonitorType.ENV_AUTHENTICATION_SEAL_BROKEN, SEAL_DEFINITION);

  public static final ChannelSohDefinition CHANNEL_DEFINITION =
    ChannelSohDefinition.create(UtilsTestFixtures.CHANNEL.getName(),
      MONITOR_TYPES,
      DEFINITIONS_BY_TYPE, CHANNEL.getNominalSampleRateHz());

  private static final ChannelSohDefinition CHANNEL_TWO_DEFINITION =
    ChannelSohDefinition.create(CHANNEL_TWO.getName(),
      MONITOR_TYPES,
      DEFINITIONS_BY_TYPE, CHANNEL.getNominalSampleRateHz());

  private static final StationSohDefinition STATION_SOH_DEFINITION =
    StationSohDefinition.create(STATION.getName(),
      MONITOR_TYPES,
      Map.of(SohMonitorType.ENV_GAP, CHANNEL_NAMES,
        SohMonitorType.MISSING, CHANNEL_NAMES,
        SohMonitorType.ENV_CLIPPED, CHANNEL_NAMES),
      Set.of(CHANNEL_DEFINITION, CHANNEL_TWO_DEFINITION), TIME_WINDOW_DEFINITION_MAP);

  private static final StationSohDefinition ALT_STATION_SOH_DEFINITION =
    StationSohDefinition.create("ALT-" + STATION.getName(),
      MONITOR_TYPES,
      Map.of(SohMonitorType.ENV_GAP, CHANNEL_NAMES,
        SohMonitorType.MISSING, CHANNEL_NAMES,
        SohMonitorType.ENV_CLIPPED, CHANNEL_NAMES),
      Set.of(CHANNEL_DEFINITION, CHANNEL_TWO_DEFINITION), TIME_WINDOW_DEFINITION_MAP);

  private static final StationSohMonitoringDefinition MONITORING_DEFINITION =
    StationSohMonitoringDefinition.from(Duration.ofSeconds(30),
      List.of(GROUP_NAME),
      Duration.ofSeconds(10),
      Set.of(STATION_SOH_DEFINITION, ALT_STATION_SOH_DEFINITION));

  private static final StationSohMonitoringDisplayParameters MONITORING_DISPLAY_PARAMETERS =
    StationSohMonitoringDisplayParameters.from(Duration.ofSeconds(10),
      50000,
      1000000,
      Duration.ofSeconds(5),
      List.of(Duration.ofMinutes(1), Duration.ofMinutes(5)),
      Duration.ofMinutes(10),
      List.of(Duration.ofMinutes(1), Duration.ofMinutes(5)));

  public static final StationSohMonitoringUiClientParameters STATION_SOH_PARAMETERS =
    StationSohMonitoringUiClientParameters.from(MONITORING_DEFINITION,
      MONITORING_DISPLAY_PARAMETERS);

  public static final SohStatusChange CLIPPED_CHANGE =
    SohStatusChange.from(Instant.EPOCH,
      SohMonitorType.ENV_CLIPPED,
      UtilsTestFixtures.CHANNEL.getName());

  public static final SohStatusChange GAP_CHANGE =
    SohStatusChange.from(Instant.EPOCH,
      SohMonitorType.ENV_GAP,
      UtilsTestFixtures.CHANNEL.getName());

  public static final SohStatusChange ALT_CLIPPED_CHANGE =
    SohStatusChange.from(Instant.EPOCH,
      SohMonitorType.ENV_CLIPPED,
      "ALT-" + UtilsTestFixtures.CHANNEL.getName());

  public static final SohStatusChange ALT_GAP_CHANGE =
    SohStatusChange.from(Instant.EPOCH,
      SohMonitorType.ENV_GAP,
      "ALT-" + UtilsTestFixtures.CHANNEL.getName());

  public static AcknowledgedSohStatusChange ACK_CHANGE = AcknowledgedSohStatusChange.from(
    UUID.randomUUID(),
    "AckBy",
    Instant.now(),
    Optional.empty(),
    List.of(SohStatusChange.from(Instant.now(),
      SohMonitorType.MISSING,
      "Channel")),
    "Station");

  public static final UnacknowledgedSohStatusChange UNACK_CHANGE_1 =
    UnacknowledgedSohStatusChange.from(STATION.getName(), Set.of(CLIPPED_CHANGE, GAP_CHANGE));

  public static final UnacknowledgedSohStatusChange ENV_GAP_UNACK_CHANGE_1 =
    UnacknowledgedSohStatusChange.from(STATION.getName(), Set.of(GAP_CHANGE));

  public static final UnacknowledgedSohStatusChange ALT_UNACK_CHANGE_1 =
    UnacknowledgedSohStatusChange.from("ALT-" + STATION.getName(),
      Set.of(ALT_CLIPPED_CHANGE, ALT_GAP_CHANGE));

  public static final QuietedSohStatusChangeUpdate QUIETED_CHANGE_1 =
    QuietedSohStatusChangeUpdate.create(
      Instant.EPOCH.plus(10, ChronoUnit.MINUTES),
      Duration.ofMinutes(10),
      SohMonitorType.ENV_GAP,
      UtilsTestFixtures.CHANNEL.getName(),
      Optional.empty(),
      UtilsTestFixtures.STATION.getName(),
      "gms");

  public static final UiSohContributor CONTRIBUTOR = UiSohContributor.from(1.0,
    true,
    SohStatus.BAD,
    true,
    SohMonitorType.ENV_CLIPPED);

  private static final UiSohContributor MARGINAL_MISSING_CONTRIBUTOR =
    UiSohContributor.from(99.0, true, SohStatus.MARGINAL, true, SohMonitorType.MISSING);

  private static final UiSohContributor MARGINAL_LAG_CONTRIBUTOR =
    UiSohContributor.from(3600.0, true, SohStatus.MARGINAL, true, SohMonitorType.LAG);

  private static final UiSohContributor BAD_SEAL_BROKEN_CONTRIBUTOR =
    UiSohContributor.from(99.0,
      true,
      SohStatus.BAD,
      false,
      SohMonitorType.ENV_AUTHENTICATION_SEAL_BROKEN);

  private static final UiSohContributor BAD_MISSING_CONTRIBUTOR =
    UiSohContributor.from(100.0, true, SohStatus.BAD, true, SohMonitorType.MISSING);

  public static final UiStationSohCapabilityStatus CAPABILITY_STATUS =
    UiStationSohCapabilityStatus.from("test", "test 2", UiSohStatus.BAD);

  public static final UiSohMonitorValueAndStatus MONITOR_VALUE_STATUS =
    UiSohMonitorValueAndStatus.create(
      1.0,
      true,
      SohStatus.BAD,
      SohMonitorType.ENV_GAP,
      true,
      2.0,
      1.0,
      1234,
      2345,
      false);

  public static final UiChannelSoh CHANNEL_SOH = UiChannelSoh.from("Channel",
    SohStatus.BAD,
    Set.of(MONITOR_VALUE_STATUS));

  private static final UiSohMonitorValueAndStatus MARGINAL_LATENCY_SMVS =
    UiSohMonitorValueAndStatus.create(MARGINAL_LAG_SOH_MONITOR_VALUE_AND_STATUS.getValue()
        .map(duration -> Math.round(100.0 * duration.toMillis() / 1000.0) / 100.0).orElse(0.0),
      true,
      SohStatus.MARGINAL,
      SohMonitorType.LAG,
      false,
      Math.round(100.0 * LATENCY_DEFINITION.getGoodThreshold().toMillis() / 1000.0) / 100.0,
      Math.round(100.0 * LATENCY_DEFINITION.getMarginalThreshold().toMillis() / 1000.0) / 100.0,
      -1,
      0,
      false);

  private static final UiSohMonitorValueAndStatus MARGINAL_MISSING_SMVS =
    UiSohMonitorValueAndStatus.create(99.00,
      true,
      SohStatus.MARGINAL,
      SohMonitorType.MISSING,
      false,
      0.20,
      0.60,
      -1,
      0,
      false);

  private static final UiSohMonitorValueAndStatus BAD_SEAL_SMVS =
    UiSohMonitorValueAndStatus.create(99.00,
      true,
      SohStatus.BAD,
      SohMonitorType.ENV_AUTHENTICATION_SEAL_BROKEN,
      false,
      10.00,
      50.00,
      -1,
      0,
      false);

  public static final UiChannelSoh BAD_MISSING_LATENCY_SEAL_UI_CHANNEL_SOH =
    UiChannelSoh.from(CHANNEL.getName(),
      SohStatus.BAD,
      Set.of(MARGINAL_LATENCY_SMVS,
        MARGINAL_MISSING_SMVS,
        BAD_SEAL_SMVS));

  private static final UiChannelSoh BAD_MISSING_LATENCY_UI_CHANNEL_SOH_2 =
    UiChannelSoh.from(CHANNEL_TWO.getName(),
      SohStatus.BAD,
      Set.of(MARGINAL_LATENCY_SMVS,
        MARGINAL_MISSING_SMVS));

  public static final UiStationSoh UI_STATION_SOH = UiStationSoh.from("Id",
    UUID.randomUUID(),
    SohStatus.BAD,
    true,
    true,
    List.of(CONTRIBUTOR),
    List.of(CAPABILITY_STATUS),
    1234,
    "Station",
    Set.of(CHANNEL_SOH),
    Set.of());

  private static final UiStationSohCapabilityStatus MARGINAL_CAPABILITY_STATUS =
    UiStationSohCapabilityStatus
      .from(MARGINAL_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP.getForStationGroup(),
        STATION.getName(),
        UiSohStatus.MARGINAL);

  public static final UiStationSoh MARGINAL_UI_STATION_SOH =
    UiStationSoh.create(
      MARGINAL_STATION_SOH,
      true,
      true,
      List.of(MARGINAL_LAG_CONTRIBUTOR,
        BAD_SEAL_BROKEN_CONTRIBUTOR,
        BAD_MISSING_CONTRIBUTOR),
      List.of(MARGINAL_CAPABILITY_STATUS),
      Set.of(BAD_MISSING_LATENCY_SEAL_UI_CHANNEL_SOH,
        BAD_MISSING_LATENCY_UI_CHANNEL_SOH_2));

  public static final UiStationGroupSoh STATION_GROUP_SOH =
    UiStationGroupSoh.create(GROUP_NAME,
      GROUP_NAME,
      Instant.EPOCH.toEpochMilli(),
      UiSohStatus.GOOD,
      1);

  public static final UiStationAndStationGroups STATION_AND_STATION_GROUPS =
    UiStationAndStationGroups.create(List.of(STATION_GROUP_SOH),
      List.of(UI_STATION_SOH), false);

  public static final UiStationGroupSoh MARGINAL_STATION_GROUPS =
    UiStationGroupSoh.create(GROUP_NAME,
      GROUP_NAME,
      Instant.now().toEpochMilli(),
      UiSohStatus
        .from(MARGINAL_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP.getGroupRollupSohStatus()),
      1);

  public static final UiStationAndStationGroups MARGINAL_STATION_AND_GROUPS =
    UiStationAndStationGroups.create(List.of(MARGINAL_STATION_GROUPS),
      List.of(MARGINAL_UI_STATION_SOH), false);
}
