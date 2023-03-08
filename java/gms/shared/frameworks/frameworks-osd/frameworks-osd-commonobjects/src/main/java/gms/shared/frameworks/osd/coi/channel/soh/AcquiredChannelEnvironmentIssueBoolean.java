package gms.shared.frameworks.osd.coi.channel.soh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.time.Instant;

/**
 * Define a class to represent a boolean State-of-Health reading.
 */
@AutoValue
public abstract class AcquiredChannelEnvironmentIssueBoolean implements
  AcquiredChannelEnvironmentIssue<Boolean> {

  @Override
  public String getValueType() {
    return "boolean";
  }

  /**
   * Creates an AcquiredChannelSohBoolean from all params.
   *
   * @param channelName identifier referencing the ProcessingChannel this SOH is for.
   * @param type The state of health type that will be represented by all of the times and statuses
   * held by this class.
   * @param startTime the start time for the status
   * @param endTime the end time for the status
   * @param status the Status of the State-Of-Health (e.g. a boolean or a float or something)
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  @JsonCreator
  public static AcquiredChannelEnvironmentIssueBoolean from(
    @JsonProperty("channelName") String channelName,
    @JsonProperty("type") AcquiredChannelEnvironmentIssueType type,
    @JsonProperty("startTime") Instant startTime,
    @JsonProperty("endTime") Instant endTime,
    @JsonProperty("status") boolean status) {

    return new AutoValue_AcquiredChannelEnvironmentIssueBoolean(channelName,
      type, startTime, endTime, status);
  }
}
