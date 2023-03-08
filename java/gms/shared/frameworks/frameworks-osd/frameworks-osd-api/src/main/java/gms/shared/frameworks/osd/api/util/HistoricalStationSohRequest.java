package gms.shared.frameworks.osd.api.util;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;

import java.time.Instant;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@AutoValue
@JsonSerialize(as = HistoricalStationSohRequest.class)
@JsonDeserialize(builder = AutoValue_HistoricalStationSohRequest.Builder.class)
public abstract class HistoricalStationSohRequest {

  public abstract String getStationName();

  public abstract Instant getStartTime();

  public abstract Instant getEndTime();

  public abstract SohMonitorType getSohMonitorType();

  public static Builder builder() {
    return new AutoValue_HistoricalStationSohRequest.Builder();
  }

  public abstract Builder toBuilder();

  public static HistoricalStationSohRequest create(
    String stationName,
    Instant startTime,
    Instant endTime,
    SohMonitorType sohMonitorType
  ) {
    return builder()
      .setStationName(stationName)
      .setStartTime(startTime)
      .setEndTime(endTime)
      .setSohMonitorType(sohMonitorType)
      .build();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setStationName(String stationName);

    public abstract Builder setStartTime(Instant startTime);

    public abstract Builder setEndTime(Instant endTime);

    public abstract Builder setSohMonitorType(SohMonitorType sohMonitorType);

    public abstract HistoricalStationSohRequest autoBuild();

    public HistoricalStationSohRequest build() {
      var historicalStationSohRequest = autoBuild();

      checkArgument(isNotEmpty(historicalStationSohRequest.getStationName()),
        "HistoricalStationSohRequest requires non-null, non-empty stationName");

      return historicalStationSohRequest;
    }
  }
}
