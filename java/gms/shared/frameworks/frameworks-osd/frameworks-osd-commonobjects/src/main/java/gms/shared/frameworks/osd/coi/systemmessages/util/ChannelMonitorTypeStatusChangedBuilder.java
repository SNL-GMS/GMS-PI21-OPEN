package gms.shared.frameworks.osd.coi.systemmessages.util;

import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType.SohValueType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageTagNames;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Record changes in pure Station SOH rollup status for every Station.
 */
public class ChannelMonitorTypeStatusChangedBuilder {

  private final String stationName;
  private final String channelName;
  private final SohMonitorType sohMonitorType;
  private final SohMonitorValueAndStatus<?> previousStatus;
  private final SohMonitorValueAndStatus<?> currentStatus;

  public ChannelMonitorTypeStatusChangedBuilder(String stationName, String channelName,
    SohMonitorType sohMonitorType, SohMonitorValueAndStatus<?> previousStatus,
    SohMonitorValueAndStatus<?> currentStatus) {

    Objects.requireNonNull(stationName, "stationName may not be null");
    Objects.requireNonNull(channelName, "channelName may not be null");
    Objects.requireNonNull(sohMonitorType, "sohMonitorType may not be null");
    Objects.requireNonNull(previousStatus, "previousStatus may not be null");
    Objects.requireNonNull(currentStatus, "currentState may not be null");

    this.stationName = stationName;
    this.channelName = channelName;
    this.sohMonitorType = sohMonitorType;
    this.previousStatus = previousStatus;
    this.currentStatus = currentStatus;
  }

  public SystemMessage build() {

    SystemMessageType type = SystemMessageType.CHANNEL_MONITOR_TYPE_STATUS_CHANGED;

    String previousValue = UISystemMessageUtils.convertValueToString(previousStatus.getValue());
    String currentValue = UISystemMessageUtils.convertValueToString(currentStatus.getValue());

    if (sohMonitorType.getSohValueType() == SohValueType.PERCENT) {
      previousValue += "%";
      currentValue += "%";
    } else if (sohMonitorType.getSohValueType() != SohValueType.DURATION) {
      throw new UnsupportedOperationException(
        "Unsupported SohValueType, " + sohMonitorType.getSohValueType());
    }

    String prettyMonitorType = UISystemMessageUtils.prettyPrintMonitorType(sohMonitorType);

    String msg = String
      .format(type.getMessageTemplate(), stationName, channelName, prettyMonitorType,
        previousValue, previousStatus.getStatus(), currentValue, currentStatus.getStatus());

    Map<String, Object> tags = Map.of(
      SystemMessageTagNames.STATION.getTagName(), stationName,
      SystemMessageTagNames.CHANNEL.getTagName(), channelName,
      SystemMessageTagNames.MONITOR_TYPE.getTagName(), sohMonitorType
    );

    return SystemMessage.create(Instant.now(), msg, type, type.getSeverity(), type.getCategory(),
      type.getSubCategory(), tags);
  }
}
