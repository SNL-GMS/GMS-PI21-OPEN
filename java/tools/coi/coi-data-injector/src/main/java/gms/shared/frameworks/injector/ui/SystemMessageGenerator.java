package gms.shared.frameworks.injector.ui;

import gms.core.performancemonitoring.ssam.control.datapublisher.SystemEvent;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.soh.PercentSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType;
import gms.shared.frameworks.osd.coi.systemmessages.util.ChannelMonitorTypeQuietPeriodCanceledBuilder;
import gms.shared.frameworks.osd.coi.systemmessages.util.ChannelMonitorTypeQuietPeriodExpiredBuilder;
import gms.shared.frameworks.osd.coi.systemmessages.util.ChannelMonitorTypeQuietedBuilder;
import gms.shared.frameworks.osd.coi.systemmessages.util.ChannelMonitorTypeStatusChangeAcknowledgedBuilder;
import gms.shared.frameworks.osd.coi.systemmessages.util.ChannelMonitorTypeStatusChangedBuilder;
import gms.shared.frameworks.osd.coi.systemmessages.util.StationCapabilityStatusChangedBuilder;
import gms.shared.frameworks.osd.coi.systemmessages.util.StationGroupCapabilityStatusChangedBuilder;
import gms.shared.frameworks.osd.coi.systemmessages.util.StationNeedsAttentionBuilder;
import gms.shared.frameworks.osd.coi.systemmessages.util.StationSohStatusChangedBuilder;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;

/**
 * SystemMessageGenerator class to randomly generate a system message to be sent to the UI.
 * The class is initialized with a station so there is a consistency in the message.
 */
class SystemMessageGenerator {
  private Station station;
  private static final SecureRandom random = new SecureRandom();

  SystemMessageGenerator(Station s) {
    this.station = s;
  }

  /**
   * Randomly generate a UiSystem message using the COI SystemMessage utilities. Converts the
   * System from the utility to UiSystemMessage
   *
   * @return SystemEvent wrapped UiSystemMessage
   */
  SystemEvent<SystemMessage> getSystemMessage() {
    SystemMessageType messageType = getRandomMessageType();

    return SystemEvent.from("system-message", buildSystemMessageOfType(messageType));
  }

  private static <T extends Enum<?>> T randomEnum(Class<T> clazz) {
    var x = random.nextInt(clazz.getEnumConstants().length);
    return clazz.getEnumConstants()[x];
  }

  private static SystemMessageType getRandomMessageType() {
    return randomEnum(SystemMessageType.class);
  }

  private String getMockChannelName() {
    return List.copyOf(station.getChannels()).get(random.nextInt(this.station.getChannels().size()))
      .getName();
  }

  private static SohStatus getRandomStatus() {
    return randomEnum(SohStatus.class);
  }

  private SystemMessage buildSystemMessageOfType(SystemMessageType messageType) {
    switch (messageType) {
      case STATION_NEEDS_ATTENTION:
        return this.getStationNeedsAttentionMessage();
      case STATION_SOH_STATUS_CHANGED:
        return this.getStationStatusChangedMessage();
      case STATION_CAPABILITY_STATUS_CHANGED:
        return this.getStationCapabilityStatusChangedMessage();
      case STATION_GROUP_CAPABILITY_STATUS_CHANGED:
        return this.getStationGroupCapabilityStatusChangedMessage();
      case CHANNEL_MONITOR_TYPE_STATUS_CHANGED:
        return this.getChannelMonitorTypeStatusChangedMessage();
      case CHANNEL_MONITOR_TYPE_STATUS_CHANGE_ACKNOWLEDGED:
        return this.getChannelMonitorTypeStatusChangeAcknowledgedMessage();
      case CHANNEL_MONITOR_TYPE_QUIETED:
        return this.getChannelMonitorTypeQuietedMessage();
      case CHANNEL_MONITOR_TYPE_QUIET_PERIOD_CANCELED:
        return this.getChannelMonitorTypeQuietPeriodCanceledMessage();
      case CHANNEL_MONITOR_TYPE_QUIET_PERIOD_EXPIRED:
        return this.getChannelMonitorTypeQuietPeriodExpiredMessage();
    }
    throw new IllegalStateException("System Message Type not mapped in mock SystemMessageGenerator");
  }

  private SystemMessage getChannelMonitorTypeStatusChangedMessage() {
    String channelName = getMockChannelName();
    double previousValue = random.nextDouble() * 100;
    double currentValue = random.nextDouble() * 100;
    SohMonitorType monitorType = SohMonitorType.MISSING;
    SohMonitorValueAndStatus<Double> previousStatus = PercentSohMonitorValueAndStatus
      .from(previousValue, getRandomStatus(), monitorType);
    SohMonitorValueAndStatus<Double> currentStatus = PercentSohMonitorValueAndStatus
      .from(currentValue, getRandomStatus(), monitorType);

    return new ChannelMonitorTypeStatusChangedBuilder(this.station.getName(), channelName,
      monitorType, previousStatus, currentStatus)
      .build();
  }

  private SystemMessage getChannelMonitorTypeStatusChangeAcknowledgedMessage() {
    var channelName = getMockChannelName();
    var sohMonitorType = SohMonitorType.ENV_AMPLIFIER_SATURATION_DETECTED;
    var userName = "mockUser";

    return new ChannelMonitorTypeStatusChangeAcknowledgedBuilder(this.station.getName(),
      channelName, sohMonitorType, userName, null)
      .build();
  }

  private SystemMessage getChannelMonitorTypeQuietedMessage() {
    var channelName = getMockChannelName();
    var sohMonitorType = SohMonitorType.MISSING;
    var timeInterval = Duration.ofMinutes(5);
    var userName = "gms";

    return new ChannelMonitorTypeQuietedBuilder(this.station.getName(), channelName,
      sohMonitorType, timeInterval, userName, null)
      .build();
  }

  private SystemMessage getChannelMonitorTypeQuietPeriodExpiredMessage() {
    String channelName = getMockChannelName();
    SohMonitorType monitorType = SohMonitorType.ENV_AMPLIFIER_SATURATION_DETECTED;

    return new ChannelMonitorTypeQuietPeriodExpiredBuilder(this.station.getName(), channelName,
      monitorType)
      .build();
  }

  private SystemMessage getChannelMonitorTypeQuietPeriodCanceledMessage() {
    var channelName = getMockChannelName();
    var monitorType = SohMonitorType.ENV_AMPLIFIER_SATURATION_DETECTED;
    var userName = "mockUser";

    return new ChannelMonitorTypeQuietPeriodCanceledBuilder(this.station.getName(), channelName,
      monitorType, userName)
      .build();
  }

  private SystemMessage getStationNeedsAttentionMessage() {
    return new StationNeedsAttentionBuilder(this.station.getName()).build();
  }

  private SystemMessage getStationStatusChangedMessage() {
    SohStatus previousStatus = getRandomStatus();
    SohStatus currentStatus = getRandomStatus();

    return new StationSohStatusChangedBuilder(this.station.getName(), previousStatus,
      currentStatus).build();
  }

  private SystemMessage getStationGroupCapabilityStatusChangedMessage() {
    var stationGroupName = "Mock Group";
    SohStatus previousStatus = getRandomStatus();
    SohStatus currentStatus = getRandomStatus();

    return new StationGroupCapabilityStatusChangedBuilder(stationGroupName,
      previousStatus, currentStatus).build();
  }

  private SystemMessage getStationCapabilityStatusChangedMessage() {
    var stationGroupName = "Mock Group";
    SohStatus previousStatus = getRandomStatus();
    SohStatus currentStatus = getRandomStatus();

    return new StationCapabilityStatusChangedBuilder(this.station.getName(), stationGroupName,
      previousStatus, currentStatus).build();
  }
}
