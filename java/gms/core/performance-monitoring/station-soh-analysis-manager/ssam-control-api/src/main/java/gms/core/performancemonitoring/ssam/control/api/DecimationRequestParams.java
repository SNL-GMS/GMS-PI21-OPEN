package gms.core.performancemonitoring.ssam.control.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import org.apache.commons.lang3.Validate;

import java.time.Instant;

@AutoValue
public abstract class DecimationRequestParams {

  public abstract Instant getStartTime();

  public abstract Instant getEndTime();

  public abstract int getSamplesPerChannel();

  public abstract String getStationName();

  public abstract SohMonitorType getSohMonitorType();

  @JsonCreator
  public static DecimationRequestParams create(
    @JsonProperty("startTime") Instant startTime,
    @JsonProperty("endTime") Instant endTime,
    @JsonProperty("samplesPerChannel") int samplesPerChannel,
    @JsonProperty("stationName") String stationName,
    @JsonProperty("sohMonitorType") SohMonitorType sohMonitorType) {

    Validate.isTrue(endTime.isAfter(startTime), "startTime must be before endTime");
    Validate.isTrue(samplesPerChannel >= 1,
      "samplesPerChannel must be greater than or equal to one");
    Validate.isTrue(!stationName.isEmpty() && !stationName.isBlank(),
      "stationName must not be empty or blank");

    Validate.isTrue(!sohMonitorType.isEnvironmentIssue(), String
      .format("SohMonitorType %s must be %s, %s, or %s", sohMonitorType, SohMonitorType.LAG,
        SohMonitorType.TIMELINESS, SohMonitorType.MISSING));

    return new AutoValue_DecimationRequestParams(startTime, endTime, samplesPerChannel, stationName,
      sohMonitorType);
  }
}
