package gms.shared.frameworks.osd.coi.systemmessages.util;

import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageTagNames;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Record changes in pure Station SOH rollup status for every Station.
 */
public class StationGroupCapabilityStatusChangedBuilder {

  private String stationGroupName;
  private SohStatus previousStatus;
  private SohStatus currentStatus;

  public StationGroupCapabilityStatusChangedBuilder(String stationGroupName,
    SohStatus previousStatus, SohStatus currentStatus) {

    Objects.requireNonNull(stationGroupName, "stationGroupName may not be null");
    Objects.requireNonNull(previousStatus, "previousStatus may not be null");
    Objects.requireNonNull(currentStatus, "currentState may not be null");

    this.stationGroupName = stationGroupName;
    this.previousStatus = previousStatus;
    this.currentStatus = currentStatus;
  }


  public SystemMessage build() {

    SystemMessageType type = SystemMessageType.STATION_GROUP_CAPABILITY_STATUS_CHANGED;

    String msg = String
      .format(type.getMessageTemplate(), stationGroupName, previousStatus, currentStatus);

    Map<String, Object> tags = Map.of(
      SystemMessageTagNames.STATION_GROUP.getTagName(), stationGroupName
    );

    return SystemMessage.create(Instant.now(), msg, type, type.getSeverity(), type.getCategory(),
      type.getSubCategory(), tags);
  }
}
