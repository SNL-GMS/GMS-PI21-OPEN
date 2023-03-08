package gms.shared.frameworks.osd.coi.soh;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Types of issues that can be found by the monitor.
 * The dbId maps to id values in the DB...if you change these, they DB must be updated as well
 */
public enum SohMonitorType {
  LAG((short) 0, false, SohValueType.DURATION),
  TIMELINESS((short) 1, false, SohValueType.DURATION),
  MISSING((short) 2, false, SohValueType.PERCENT),
  ENV_AMPLIFIER_SATURATION_DETECTED((short) 3),
  ENV_AUTHENTICATION_SEAL_BROKEN((short) 4, SohValueType.PERCENT),
  ENV_BACKUP_POWER_UNSTABLE((short) 5, SohValueType.PERCENT),
  ENV_BEGINNING_DATE_OUTAGE((short) 6),
  ENV_BEGINNING_TIME_OUTAGE((short) 7),
  ENV_CALIBRATION_UNDERWAY((short) 8, SohValueType.PERCENT),
  ENV_CLIPPED((short) 9, SohValueType.PERCENT),
  ENV_CLOCK_DIFFERENTIAL_IN_MICROSECONDS((short) 10),
  ENV_CLOCK_DIFFERENTIAL_TOO_LARGE((short) 11, SohValueType.PERCENT),
  ENV_CLOCK_LOCKED((short) 12),
  ENV_DATA_AVAILABILITY_MINIMUM_CHANNELS((short) 13),
  ENV_DATA_AVAILABILITY_GEOPHYSICAL_CHANNELS((short) 14),
  ENV_DATA_AVAILABILITY_GEOPHYSICAL_CHANNELS_UNAUTHENTICATED((short) 15),
  ENV_LAST_GPS_SYNC_TIME((short) 16),
  ENV_DEAD_SENSOR_CHANNEL((short) 17, SohValueType.PERCENT),
  ENV_DIGITAL_FILTER_MAY_BE_CHARGING((short) 18),
  ENV_DIGITIZER_ANALOG_INPUT_SHORTED((short) 19, SohValueType.PERCENT),
  ENV_DIGITIZER_CALIBRATION_LOOP_BACK((short) 20, SohValueType.PERCENT),
  ENV_DIGITIZING_EQUIPMENT_OPEN((short) 21, SohValueType.PERCENT),
  ENV_DURATION_OUTAGE((short) 22),
  ENV_ENDING_DATE_OUTAGE((short) 23),
  ENV_ENDING_TIME_OUTAGE((short) 24),
  ENV_END_TIME_SERIES_BLOCKETTE((short) 25),
  ENV_EQUIPMENT_HOUSING_OPEN((short) 26, SohValueType.PERCENT),
  ENV_EQUIPMENT_MOVED((short) 27, SohValueType.PERCENT),
  ENV_EVENT_IN_PROGRESS((short) 28),
  ENV_GAP((short) 29),
  ENV_GLITCHES_DETECTED((short) 30),
  ENV_GPS_RECEIVER_OFF((short) 31, SohValueType.PERCENT),
  ENV_GPS_RECEIVER_UNLOCKED((short) 32, SohValueType.PERCENT),
  ENV_LONG_DATA_RECORD((short) 33),
  ENV_MAIN_POWER_FAILURE((short) 34, SohValueType.PERCENT),
  ENV_MAXIMUM_DATA_TIME((short) 35),
  ENV_MEAN_AMPLITUDE((short) 36),
  ENV_MISSION_CAPABILITY_STATISTIC((short) 37),
  ENV_NEGATIVE_LEAP_SECOND_DETECTED((short) 38),
  ENV_NUMBER_OF_CONSTANT_VALUES((short) 39),
  ENV_NUMBER_OF_DATA_GAPS((short) 40),
  ENV_NUMBER_OF_SAMPLES((short) 41),
  ENV_OUTAGE_COMMENT((short) 42),
  ENV_PERCENT_AUTHENTICATED_DATA_AVAILABLE((short) 43),
  ENV_PERCENT_DATA_RECEIVED((short) 44),
  ENV_PERCENT_UNAUTHENTICATED_DATA_AVAILABLE((short) 45),
  ENV_PERCENTAGE_GEOPHYSICAL_CHANNEL_RECEIVED((short) 46),
  ENV_POSITIVE_LEAP_SECOND_DETECTED((short) 47),
  ENV_QUESTIONABLE_TIME_TAG((short) 48),
  ENV_ROOT_MEAN_SQUARE_AMPLITUDE((short) 49),
  ENV_SHORT_DATA_RECORD((short) 50),
  ENV_SPIKE_DETECTED((short) 51),
  ENV_START_TIME_SERIES_BLOCKETTE((short) 52),
  ENV_STATION_EVENT_DETRIGGER((short) 53),
  ENV_STATION_EVENT_TRIGGER((short) 54),
  ENV_STATION_VOLUME_PARITY_ERROR_POSSIBLY_PRESENT((short) 56),
  ENV_TELEMETRY_SYNCHRONIZATION_ERROR((short) 57),
  ENV_TIMELY_DATA_AVAILABILITY((short) 58),
  ENV_TIMING_CORRECTION_APPLIED((short) 59),
  ENV_VAULT_DOOR_OPENED((short) 60, SohValueType.PERCENT),
  ENV_ZEROED_DATA((short) 61, SohValueType.PERCENT);

  private static final Set<SohMonitorType> validTypes = Arrays.stream(SohMonitorType.values())
    .filter(SohMonitorType::isValid).collect(Collectors.toSet());

  private final short dbId;
  private final boolean isEnvironmentIssue;
  private final SohValueType sohValueType;

  public short getDbId() {
    return this.dbId;
  }

  public boolean isEnvironmentIssue() {
    return this.isEnvironmentIssue;
  }

  public boolean isValid() {
    return this.sohValueType != SohValueType.INVALID;
  }

  public SohValueType getSohValueType() {
    return this.sohValueType;
  }

  SohMonitorType(short dbId) {
    this(dbId, SohValueType.INVALID);
  }

  SohMonitorType(short dbId, SohValueType sohValueType) {
    this(dbId, true, sohValueType);
  }

  SohMonitorType(short dbId, boolean isEnvironmentIssue, SohValueType sohValueType) {
    this.dbId = dbId;
    this.isEnvironmentIssue = isEnvironmentIssue;
    this.sohValueType = sohValueType;
  }

  /**
   * Will return a set of all current valid SohMonitorTypes
   *
   * @return Set of SohMonitorType sohMonitorTypes
   */
  public static Set<SohMonitorType> validTypes() {
    return validTypes;
  }

  public enum SohValueType {
    DURATION,
    PERCENT,
    // TODO: ditch this when we know what type of values the analog SohMonitorTypes will be
    INVALID
  }
}
