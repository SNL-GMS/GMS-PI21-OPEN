package gms.shared.frameworks.osd.coi.systemmessages.util;

import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageTagNames;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Record changes in Station SOH rollup status for every Station used in every capability rollup.
 */
public class StationCapabilityStatusChangedBuilder {

  private String stationName;
  private String stationGroupName;
  private SohStatus previousStatus;
  private SohStatus currentStatus;

  public StationCapabilityStatusChangedBuilder(String stationName, String stationGroupName,
    SohStatus previousStatus, SohStatus currentStatus) {

    Objects.requireNonNull(stationName, "stationName may not be null");
    Objects.requireNonNull(stationGroupName, "stationGroupName may not be null");
    Objects.requireNonNull(previousStatus, "previousStatus may not be null");
    Objects.requireNonNull(currentStatus, "currentStatus may not be null");

    this.stationName = stationName;
    this.stationGroupName = stationGroupName;
    this.previousStatus = previousStatus;
    this.currentStatus = currentStatus;
  }


  public SystemMessage build() {

    SystemMessageType type = SystemMessageType.STATION_CAPABILITY_STATUS_CHANGED;

    String msg = String.format(type.getMessageTemplate(),
      stationName, stationGroupName, previousStatus, currentStatus);

    Map<String, Object> tags = Map.of(
      SystemMessageTagNames.STATION.getTagName(), stationName,
      SystemMessageTagNames.STATION_GROUP.getTagName(), stationGroupName
    );

    return SystemMessage.create(Instant.now(), msg, type, type.getSeverity(), type.getCategory(),
      type.getSubCategory(), tags);
  }
}
