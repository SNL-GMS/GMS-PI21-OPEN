package gms.shared.frameworks.osd.coi.systemmessages.util;

import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageTagNames;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;


public class ChannelMonitorTypeQuietedBuilder {

  private final String stationName;
  private final String channelName;
  private final SohMonitorType sohMonitorType;
  private final Duration timeInterval;
  private final String userName;
  private final String comment;

  public ChannelMonitorTypeQuietedBuilder(String stationName, String channelName,
    SohMonitorType sohMonitorType, Duration timeInterval, String userName, String comment) {

    Objects.requireNonNull(stationName, "stationName may not be null");
    Objects.requireNonNull(channelName, "channelName may not be null");
    Objects.requireNonNull(sohMonitorType, "sohMonitorType may not be null");
    Objects.requireNonNull(timeInterval, "timeInterval may not be null");
    Objects.requireNonNull(userName, "userName may not be null");

    this.stationName = stationName;
    this.channelName = channelName;
    this.sohMonitorType = sohMonitorType;
    this.timeInterval = timeInterval;
    this.userName = userName;
    this.comment = comment;
  }

  public SystemMessage build() {
    SystemMessageType type = SystemMessageType.CHANNEL_MONITOR_TYPE_QUIETED;

    String[] msgs = type.getMessageTemplate().split("\\|");

    String msg = "";

    String timeIntervalFormatted = UISystemMessageUtils.convertDurationToHumanReadable(timeInterval);

    String prettyMonitorType = UISystemMessageUtils.prettyPrintMonitorType(sohMonitorType);

    if (Objects.isNull(comment) || comment.isBlank() || comment.isEmpty()) {
      msg = String
        .format(msgs[0], stationName, channelName, prettyMonitorType, timeIntervalFormatted, userName);
    } else {
      msg = String
        .format(msgs[1], stationName, channelName, prettyMonitorType, timeIntervalFormatted, userName,
          comment);
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
