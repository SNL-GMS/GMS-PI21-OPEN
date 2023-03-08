package gms.shared.frameworks.osd.coi.soh;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum StationAggregateType {
  LAG(false, StationValueType.DURATION),
  TIMELINESS(false, StationValueType.DURATION),
  MISSING(false, StationValueType.PERCENT),
  ENVIRONMENTAL_ISSUES(true, StationValueType.PERCENT);

  private static Set<StationAggregateType> validTypes = Arrays.stream(StationAggregateType.values())
    .filter(type -> !type.getStationValueType().equals(StationValueType.INVALID)).collect(
      Collectors.toSet());

  private boolean isEnvironmentIssue;
  private StationValueType stationValueType;

  public boolean isEnvironmentIssue() {
    return this.isEnvironmentIssue;
  }

  public StationValueType getStationValueType() {
    return this.stationValueType;
  }

  StationAggregateType() {
    this(StationValueType.INVALID);
  }

  StationAggregateType(StationValueType stationValueType) {
    this(true, stationValueType);
  }

  StationAggregateType(boolean isEnvironmentIssue, StationValueType stationValueType) {
    this.isEnvironmentIssue = isEnvironmentIssue;
    this.stationValueType = stationValueType;
  }

  /**
   * Return a set of all current valid StationAggregateTypes
   *
   * @return Set of StationAggregateType stationAggregateType
   */
  public static Set<StationAggregateType> validTypes() {
    return validTypes;
  }

  public enum StationValueType {
    DURATION,
    PERCENT,
    INVALID
  }
}
