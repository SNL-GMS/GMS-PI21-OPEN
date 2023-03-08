package gms.shared.frameworks.osd.coi.systemmessages.util;

import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageTagNames;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Record changes in pure Station SOH rollup status for every Station.
 */
public class StationNeedsAttentionBuilder {

  private String stationName;

  public StationNeedsAttentionBuilder(String stationName) {

    Objects.requireNonNull(stationName, "stationName may not be null");

    this.stationName = stationName;
  }

  public SystemMessage build() {

    SystemMessageType type = SystemMessageType.STATION_NEEDS_ATTENTION;

    String msg = String.format(type.getMessageTemplate(), stationName);

    Map<String, Object> tags = Map.of(SystemMessageTagNames.STATION.getTagName(), stationName);

    return SystemMessage.create(Instant.now(), msg, type, type.getSeverity(), type.getCategory(),
      type.getSubCategory(), tags);
  }
}
