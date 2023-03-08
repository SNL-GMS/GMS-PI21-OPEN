package gms.dataacquisition.stationreceiver.cd11.common.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@AutoValue
@JsonSerialize(as = Cd11DataConsumerParametersTemplate.class)
@JsonDeserialize(builder = AutoValue_Cd11DataConsumerParametersTemplate.Builder.class)
public abstract class Cd11DataConsumerParametersTemplate {

  public abstract String getStationName();

  public abstract int getPortOffset();

  public abstract boolean isAcquired();

  public abstract boolean isFrameProcessingDisabled();

  public static Builder builder() {
    return new AutoValue_Cd11DataConsumerParametersTemplate.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setStationName(String stationName);

    public abstract Builder setPortOffset(int portOffset);

    public abstract Builder setAcquired(boolean isAcquired);

    public abstract Builder setFrameProcessingDisabled(boolean isFrameProcessingDisabled);

    public abstract Cd11DataConsumerParametersTemplate autoBuild();

    public Cd11DataConsumerParametersTemplate build() {
      var cd11DataConsumerParametersTemplate = autoBuild();

      checkArgument(isNotEmpty(cd11DataConsumerParametersTemplate.getStationName()),
        "Cd11StationParametersTemplate requires non-null, non-empty name");

      checkArgument(cd11DataConsumerParametersTemplate.getPortOffset() >= 0,
        "Cd11StationParametersTemplate requires non-negative portOffset");

      return cd11DataConsumerParametersTemplate;
    }
  }
}
