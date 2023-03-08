package gms.dataacquisition.stationreceiver.cd11.common.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@AutoValue
@JsonSerialize(as = Cd11DataConsumerParameters.class)
@JsonDeserialize(builder = AutoValue_Cd11DataConsumerParameters.Builder.class)
public abstract class Cd11DataConsumerParameters {

  public abstract String getStationName();

  public abstract int getPort();

  public abstract boolean isAcquired();

  public abstract boolean isFrameProcessingDisabled();

  public static Builder builder() {
    return new AutoValue_Cd11DataConsumerParameters.Builder();
  }

  public abstract Builder toBuilder();

  public static Cd11DataConsumerParameters create(Cd11DataConsumerParametersTemplate template, int basePort) {
    return Cd11DataConsumerParameters.builder()
      .setStationName(template.getStationName())
      .setPort(basePort + template.getPortOffset())
      .setAcquired(template.isAcquired())
      .setFrameProcessingDisabled(template.isFrameProcessingDisabled())
      .build();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    private static final int MIN_PORT = 8050;

    public abstract Builder setStationName(String stationName);

    public abstract Builder setPort(int port);

    public abstract Builder setAcquired(boolean isAcquired);

    public abstract Builder setFrameProcessingDisabled(boolean isFrameProcessingDisabled);

    public abstract Cd11DataConsumerParameters autoBuild();

    public Cd11DataConsumerParameters build() {
      var cd11DataConsumerParameters = autoBuild();

      checkArgument(isNotEmpty(cd11DataConsumerParameters.getStationName()),
        "Cd11StationParameters requires non-null, non-empty name");

      checkArgument(cd11DataConsumerParameters.getPort() > MIN_PORT,
        "Cd11StationParameters requires a port greater than %d", MIN_PORT);

      return cd11DataConsumerParameters;
    }
  }
}
