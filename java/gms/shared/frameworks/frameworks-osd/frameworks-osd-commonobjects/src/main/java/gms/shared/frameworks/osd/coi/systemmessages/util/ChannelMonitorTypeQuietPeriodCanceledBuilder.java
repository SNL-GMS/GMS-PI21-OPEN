package gms.shared.frameworks.osd.coi.systemmessages.util;

import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageTagNames;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Record changes in pure Station SOH rollup status for every Station.
 */
public class ChannelMonitorTypeQuietPeriodCanceledBuilder {

  private final String stationName;
  private final String channelName;
  private final SohMonitorType sohMonitorType;
  private final String userName;

  public ChannelMonitorTypeQuietPeriodCanceledBuilder(String stationName,
    String channelName, SohMonitorType sohMonitorType, String userName) {

    Objects.requireNonNull(stationName, "stationName may not be null");
    Objects.requireNonNull(channelName, "channelName may not be null");
    Objects.requireNonNull(sohMonitorType, "monitorType may not be null");
    Objects.requireNonNull(userName, "userName may not be null");

    this.stationName = stationName;
    this.channelName = channelName;
    this.sohMonitorType = sohMonitorType;
    this.userName = userName;
  }

  public SystemMessage build() {
    SystemMessageType type = SystemMessageType.CHANNEL_MONITOR_TYPE_QUIET_PERIOD_CANCELED;

    String prettyMonitorType = UISystemMessageUtils.prettyPrintMonitorType(sohMonitorType);

    String msg = String
      .format(type.getMessageTemplate(), stationName, channelName, prettyMonitorType, userName);

    Map<String, Object> tags = Map.of(
      SystemMessageTagNames.STATION.getTagName(), stationName,
      SystemMessageTagNames.CHANNEL.getTagName(), channelName,
      SystemMessageTagNames.MONITOR_TYPE.getTagName(), sohMonitorType
    );

    return SystemMessage.create(Instant.now(), msg, type, type.getSeverity(), type.getCategory(),
      type.getSubCategory(), tags);
  }
}
