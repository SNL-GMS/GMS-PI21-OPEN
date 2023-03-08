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
public class StationSohStatusChangedBuilder {

  private final String stationName;
  private final SohStatus previousStatus;
  private final SohStatus currentStatus;

  public StationSohStatusChangedBuilder(String stationName, SohStatus previousStatus,
    SohStatus currentStatus) {

    Objects.requireNonNull(stationName, "stationName may not be null");
    Objects.requireNonNull(previousStatus, "previousStatus may not be null");
    Objects.requireNonNull(currentStatus, "currentStatus may not be null");

    this.stationName = stationName;
    this.previousStatus = previousStatus;
    this.currentStatus = currentStatus;
  }

  public SystemMessage build() {
    Objects.requireNonNull(stationName, "stationName may not be null");
    Objects.requireNonNull(previousStatus, "previousStatus may not be null");
    Objects.requireNonNull(currentStatus, "currentStatus may not be null");

    SystemMessageType type = SystemMessageType.STATION_SOH_STATUS_CHANGED;

    String msg = String
      .format(type.getMessageTemplate(), stationName, previousStatus, currentStatus);

    Map<String, Object> tags = Map.of(SystemMessageTagNames.STATION.getTagName(), stationName);

    return SystemMessage.create(Instant.now(), msg, type, type.getSeverity(), type.getCategory(),
      type.getSubCategory(), tags);
  }
}
