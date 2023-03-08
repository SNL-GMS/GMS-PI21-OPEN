package gms.shared.frameworks.osd.repository.systemmessage;

import gms.shared.frameworks.osd.coi.soh.PercentSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.coi.systemmessages.util.ChannelMonitorTypeQuietPeriodCanceledBuilder;
import gms.shared.frameworks.osd.coi.systemmessages.util.ChannelMonitorTypeQuietPeriodExpiredBuilder;
import gms.shared.frameworks.osd.coi.systemmessages.util.ChannelMonitorTypeQuietedBuilder;
import gms.shared.frameworks.osd.coi.systemmessages.util.ChannelMonitorTypeStatusChangeAcknowledgedBuilder;
import gms.shared.frameworks.osd.coi.systemmessages.util.ChannelMonitorTypeStatusChangedBuilder;
import gms.shared.frameworks.osd.coi.systemmessages.util.StationCapabilityStatusChangedBuilder;
import gms.shared.frameworks.osd.coi.systemmessages.util.StationGroupCapabilityStatusChangedBuilder;
import gms.shared.frameworks.osd.coi.systemmessages.util.StationNeedsAttentionBuilder;
import gms.shared.frameworks.osd.coi.systemmessages.util.StationSohStatusChangedBuilder;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

public class TestFixtures {

  public static SystemMessage needsAttention = new StationNeedsAttentionBuilder("stationA")
    .build();

  public static SystemMessage stationStatusChanged = new StationSohStatusChangedBuilder(
    "stationB", SohStatus.MARGINAL, SohStatus.BAD).build();

  public static SystemMessage capabilityStatusChanged = new StationCapabilityStatusChangedBuilder(
    "stationC", "GROUP_A", SohStatus.BAD, SohStatus.GOOD).build();

  public static SystemMessage stationGroupCapabilityStatusChanged = new StationGroupCapabilityStatusChangedBuilder(
    "GROUP_B", SohStatus.GOOD, SohStatus.MARGINAL).build();

  static double previousValue = 12.3;
  static double currentValue = 99.9;
  static SohMonitorType monitorType = SohMonitorType.MISSING;
  static Duration oneDay = Duration.ofDays(1);

  static SohMonitorValueAndStatus<Double> previousStatus = PercentSohMonitorValueAndStatus
    .from(previousValue, SohStatus.MARGINAL, monitorType);

  static SohMonitorValueAndStatus<Double> currentStatus = PercentSohMonitorValueAndStatus
    .from(currentValue, SohStatus.BAD, monitorType);

  public static SystemMessage channelMonitorStatusChanged = new ChannelMonitorTypeStatusChangedBuilder(
    "stationD", "channelA", monitorType, previousStatus, currentStatus).build();

  public static SystemMessage channelMonitorStatusChangedAcknowledgedWithComment = new ChannelMonitorTypeStatusChangeAcknowledgedBuilder(
    "stationE", "channelB", SohMonitorType.ENV_CLOCK_LOCKED, "gms-user-2", "acknowledged")
    .build();

  public static SystemMessage channelMonitorStatusChangedAcknowledgedWithoutComment = new ChannelMonitorTypeStatusChangeAcknowledgedBuilder(
    "stationF", "channelC", SohMonitorType.ENV_CLIPPED, "gms-user-1", null).build();

  public static SystemMessage channelMonitorQuietedWithComment = new ChannelMonitorTypeQuietedBuilder(
    "stationG", "channelD", SohMonitorType.MISSING, oneDay, "gms-user-3", "quieted").build();

  public static SystemMessage channelMonitorQuietedWithoutComment = new ChannelMonitorTypeQuietedBuilder(
    "stationH", "channelE", SohMonitorType.LAG, oneDay, "gms-user-4", null).build();

  public static SystemMessage channelMonitorQuietPeriodCanceled = new ChannelMonitorTypeQuietPeriodCanceledBuilder(
    "stationI", "channelF", SohMonitorType.MISSING, "gms-user-5").build();

  public static SystemMessage channelMonitorQuietPeriodExpired = new ChannelMonitorTypeQuietPeriodExpiredBuilder(
    "stationJ", "channelG", SohMonitorType.LAG).build();

  public static Collection<SystemMessage> msgs = List.of(
    needsAttention, stationStatusChanged, capabilityStatusChanged,
    stationGroupCapabilityStatusChanged, channelMonitorStatusChanged,
    channelMonitorStatusChangedAcknowledgedWithComment,
    channelMonitorStatusChangedAcknowledgedWithoutComment,
    channelMonitorQuietedWithComment, channelMonitorQuietedWithoutComment,
    channelMonitorQuietPeriodCanceled, channelMonitorQuietPeriodExpired
  );
}
