package gms.shared.frameworks.osd.coi.channel.soh;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.google.common.collect.Range;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;

import java.time.Instant;

/**
 * Defined in section 2.4.5 of Data Model v2.1 to represent a piece of Station SOH data as received
 * in a packet (such as from the CD-1.1 protocol). The StatusType class parameter is mostly commonly
 * Boolean (i.e. it is a status 'bit'), but is parameterized to support things like Floats (e.g. a
 * measure of the voltage to the station).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = As.EXISTING_PROPERTY,
  property = "valueType")
@JsonSubTypes({
  @JsonSubTypes.Type(value = AcquiredChannelEnvironmentIssueAnalog.class, name = "analog"),
  @JsonSubTypes.Type(value = AcquiredChannelEnvironmentIssueBoolean.class, name = "boolean")
})
public interface AcquiredChannelEnvironmentIssue<T> {

  /**
   * Return the channel name associated with this SoH information.
   *
   * @return the channel name.
   */
  String getChannelName();

  /**
   * See list of enum values in AcquiredChannelSohType
   */
  AcquiredChannelEnvironmentIssueType getType();

  /**
   * Gets the startTime this SOH information is for.
   */
  Instant getStartTime();

  /**
   * @return Gets the startTime this SOH information is for.
   */
  Instant getEndTime();

  /**
   * Gets the StatusType for this SOH information
   *
   * @return status of the SOH
   */
  T getStatus();

  String getValueType();

  @JsonIgnore
  default AcquiredChannelEnvironmentIssueId getId() {
    return AcquiredChannelEnvironmentIssueId.create(getChannelName(), getType(), getStartTime());
  }

  default boolean encloses(AcquiredChannelEnvironmentIssueBoolean other) {
    return getRange().encloses(other.getRange());
  }

  @JsonIgnore
  default Range<Instant> getRange() {
    return Range.closed(getStartTime(), getEndTime());
  }

  /**
   * Enumeration defined in section 2.4.5 of Data Model v2.1
   */
  enum AcquiredChannelEnvironmentIssueType {
    AMPLIFIER_SATURATION_DETECTED(1, "amplifier saturation detected"), // MiniSeed only
    AUTHENTICATION_SEAL_BROKEN(2, "authentication seal broken"),
    BACKUP_POWER_UNSTABLE(3, "backup power unstable"),
    BEGINNING_DATE_OUTAGE(4, "beginning date of outage"), // IMS 2.0: Analog
    BEGINNING_TIME_OUTAGE(5, "beginning time of outage"), // IMS 2.0: Analog
    CALIBRATION_UNDERWAY(6, "calibration underway"), // aka. CALIBRATION
    CLIPPED(7, "clipped"),
    CLOCK_DIFFERENTIAL_IN_MICROSECONDS(8,
      "clock differential in microseconds > threshold"),
    CLOCK_DIFFERENTIAL_TOO_LARGE(9, "clock differential too large"),
    CLOCK_LOCKED(10, "clock locked"), // MiniSeed Addition
    DATA_AVAILABILITY_MINIMUM_CHANNELS(11,
      "data availability of the minimum channels"), // IMS 2.0: Analog
    DATA_AVAILABILITY_GEOPHYSICAL_CHANNELS(12,
      "data availability of the geophysical channels"), // IMS 2.0: Analog
    DATA_AVAILABILITY_GEOPHYSICAL_CHANNELS_UNAUTHENTICATED(13,
      "data availability (unauthenticated) of the geophysical channels"),
    ENV_LAST_GPS_SYNC_TIME(14,
      "time of last gps synchronization"),
    DEAD_SENSOR_CHANNEL(15, "dead sensor channel"),
    DIGITAL_FILTER_MAY_BE_CHARGING(16, "digital filter may be charging"), // MiniSeed Addition
    DIGITIZER_ANALOG_INPUT_SHORTED(17, "digitizer analog input shorted"),
    DIGITIZER_CALIBRATION_LOOP_BACK(18, "digitizer calibration loop back"),
    DIGITIZING_EQUIPMENT_OPEN(19, "digitizing equipment open"),
    DURATION_OUTAGE(20, "duration of outage"), // IMS 2.0: Analog
    ENDING_DATE_OUTAGE(21, "date of first sample after outage"), // IMS 2.0: Analog
    ENDING_TIME_OUTAGE(22, "time of first sample after outage"), // IMS 2.0: Analog
    END_TIME_SERIES_BLOCKETTE(23, "end of time series"),  //MiniSeed Addition
    EQUIPMENT_HOUSING_OPEN(24, "equipment housing open"), // aka. EQUIPMENT_OPEN
    EQUIPMENT_MOVED(25, "equipment moved"),
    EVENT_IN_PROGRESS(26, "event in progress"), // MiniSeed Addition
    GAP(27, "missing/padded data present"),
    GLITCHES_DETECTED(28, "data quality: glitches detected"), // MiniSeed Addition
    GPS_RECEIVER_OFF(29, "GPS receiver off"), // aka. GPS_OFF
    GPS_RECEIVER_UNLOCKED(30, "GPS receiver unlocked"),
    LONG_DATA_RECORD(31, "long record read"), //MiniSeed Addition
    MAIN_POWER_FAILURE(32, "main power failure"),
    MAXIMUM_DATA_TIME(33, "maximum data time possible"), // IMS 2.0: Analog
    MEAN_AMPLITUDE(34, "mean amplitude"), // IMS 2.0: Analog
    MISSION_CAPABILITY_STATISTIC(35, "mission capability of minimum channels"), // IMS 2.0: Analog
    NEGATIVE_LEAP_SECOND_DETECTED(36, "negative leap second happened"), //MiniSeed Addition
    NUMBER_OF_CONSTANT_VALUES(37, "constant"),
    NUMBER_OF_DATA_GAPS(38, "gaps"), // IMS 2.0: Analog
    NUMBER_OF_SAMPLES(39, "samples"), // IMS 2.0: Analog
    OUTAGE_COMMENT(40, "comment"), // IMS 2.0: Analog
    PERCENT_AUTHENTICATED_DATA_AVAILABLE(41, "percent available"), // IMS 2.0: Analog
    PERCENT_DATA_RECEIVED(42, "percent received"), // IMS 2.0: Analog
    PERCENT_UNAUTHENTICATED_DATA_AVAILABLE(43, "percent unauthentic available"), // IMS 2.0: Analog
    PERCENTAGE_GEOPHYSICAL_CHANNEL_RECEIVED(44, "data received percentage of the geophysical channels"), // IMS 2.0: Analog
    POSITIVE_LEAP_SECOND_DETECTED(45, "positive leap second happened"), //MiniSeed Addition
    QUESTIONABLE_TIME_TAG(46, "time tag is questionable"), //MiniSeed Addition
    ROOT_MEAN_SQUARE_AMPLITUDE(47, "RMS"),
    SHORT_DATA_RECORD(48, "short record read (record padded)"), //MiniSeed Addition
    SPIKE_DETECTED(49, "spikes detected"), //MiniSeed Addition
    START_TIME_SERIES_BLOCKETTE(50, "start of time series"), //MiniSeed Addition
    STATION_EVENT_DETRIGGER(51, "end of event, station detrigger"), //MiniSeed Addition
    STATION_EVENT_TRIGGER(52, "beginning of event, station trigger"), //MiniSeed Addition
    STATION_POWER_VOLTAGE(53, "station power voltage"), // analog soh value
    STATION_VOLUME_PARITY_ERROR_POSSIBLY_PRESENT(54, "station volume parity error possibly present"),
    TELEMETRY_SYNCHRONIZATION_ERROR(55, "telemetry synchronization error"),
    TIMELY_DATA_AVAILABILITY(56, "timely data availability of minimum channels"),
    TIMING_CORRECTION_APPLIED(57, "time correction applied"),
    VAULT_DOOR_OPENED(58, "vault door opened"),
    ZEROED_DATA(59, "zeroed data");

    private final int ordinalIndex;
    private final String returnedString;

    AcquiredChannelEnvironmentIssueType(int weight, String description) {
      this.ordinalIndex = weight;
      this.returnedString = description;
    }

    /**
     * Get the order of this SoH Type.
     *
     * @return the ordinal index of the Type.
     */
    public int getWeight() {
      return ordinalIndex;
    }

    /**
     * @return the SohMonitorType that corresponds to this AcquiredChannelEnvironmentIssueType
     */
    public SohMonitorType getMatchingSohMonitorType() {
      return SohMonitorType.valueOf("ENV_" + this.name());
    }

    @Override
    public String toString() {
      return returnedString;
    }
  }

}
