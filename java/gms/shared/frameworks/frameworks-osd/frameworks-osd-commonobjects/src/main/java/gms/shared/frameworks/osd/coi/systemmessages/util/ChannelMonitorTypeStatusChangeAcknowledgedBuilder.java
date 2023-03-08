package gms.shared.frameworks.osd.coi.systemmessages.util;

import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageTagNames;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Record Channel SOH monitor status change acknowledgements.
 */
public class ChannelMonitorTypeStatusChangeAcknowledgedBuilder {

  private final String stationName;
  private final String channelName;
  private final SohMonitorType sohMonitorType;
  private final String userName;
  private final String comment;

  public ChannelMonitorTypeStatusChangeAcknowledgedBuilder(String stationName,
    String channelName, SohMonitorType sohMonitorType, String userName, String comment) {

    Objects.requireNonNull(stationName, "stationName may not be null");
    Objects.requireNonNull(channelName, "channelName may not be null");
    Objects.requireNonNull(sohMonitorType, "sohMonitorType may not be null");
    Objects.requireNonNull(userName, "userName may not be null");

    this.stationName = stationName;
    this.channelName = channelName;
    this.sohMonitorType = sohMonitorType;
    this.userName = userName;
    this.comment = comment;
  }

  public SystemMessage build() {
    SystemMessageType type = SystemMessageType.CHANNEL_MONITOR_TYPE_STATUS_CHANGE_ACKNOWLEDGED;

    String[] msgs = type.getMessageTemplate().split("\\|");

    String msg = "";

    String prettyMonitorType = UISystemMessageUtils.prettyPrintMonitorType(sohMonitorType);

    if (Objects.isNull(comment)) {
      msg = String
        .format(msgs[0], stationName, channelName, prettyMonitorType, userName);
    } else {
      msg = String
        .format(msgs[1], stationName, channelName, prettyMonitorType, userName, comment);
    }

    Map<String, Object> tags = Map.of(
      SystemMessageTagNames.STATION.getTagName(), stationName,
      SystemMessageTagNames.CHANNEL.getTagName(), channelName,
      SystemMessageTagNames.MONITOR_TYPE.getTagName(), sohMonitorType
    );

    return SystemMessage.create(Instant.now(), msg, type, type.getSeverity(), type.getCategory(),
      type.getSubCategory(), tags);
  }
}
