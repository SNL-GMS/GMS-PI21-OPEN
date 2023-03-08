package gms.shared.frameworks.osd.coi.signaldetection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

@AutoValue
@JsonSerialize(as = AmplitudeMeasurementChannelDefinition.class)
@JsonDeserialize(builder = AutoValue_AmplitudeMeasurementChannelDefinition.Builder.class)
public abstract class AmplitudeMeasurementChannelDefinition {

  public abstract Optional<BeamParameters> getBeamParameters();

  public abstract FilterParameters getFilterParameters();

  public abstract ImmutableList<String> getChannelNames();

  public abstract Duration getRawWaveformBufferLead();

  public abstract Duration getRawWaveformBufferLag();

  public static Builder builder() {
    return new AutoValue_AmplitudeMeasurementChannelDefinition.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setBeamParameters(BeamParameters beamParameters);

    @JsonProperty
    public abstract Builder setBeamParameters(Optional<BeamParameters> optionalBeamParameters);

    public abstract Builder setFilterParameters(FilterParameters filterParameters);

    abstract Builder setChannelNames(ImmutableList<String> channelNames);

    public Builder setChannelNames(Collection<String> channels) {
      return setChannelNames(ImmutableList.copyOf(channels));
    }

    abstract ImmutableList.Builder<String> channelNamesBuilder();

    public Builder addChannelName(String channelName) {
      channelNamesBuilder().add(channelName);
      return this;
    }

    public abstract Builder setRawWaveformBufferLead(Duration lead);

    public abstract Builder setRawWaveformBufferLag(Duration lag);

    abstract AmplitudeMeasurementChannelDefinition autoBuild();

    public AmplitudeMeasurementChannelDefinition build() {
      AmplitudeMeasurementChannelDefinition channelDefinition = autoBuild();

      if (!channelDefinition.getBeamParameters().isPresent()) {
        checkState(channelDefinition.getChannelNames().size() == 1,
          "AmplitudeMeasurementChannelDefinition requires the number of channels to be exactly 1 if BeamParameters are not present");
      }

      return channelDefinition;
    }
  }

  @AutoValue
  public abstract static class BeamParameters {

    public abstract String getPluginName();

    public abstract BeamDefinition getBeamDefinition();

    @JsonCreator
    public static BeamParameters from(
      @JsonProperty("pluginName") String pluginName,
      @JsonProperty("beamDefinition") BeamDefinition beamDefinition) {
      return new AutoValue_AmplitudeMeasurementChannelDefinition_BeamParameters(pluginName,
        beamDefinition);
    }
  }

  @AutoValue
  public abstract static class FilterParameters {

    public abstract String getPluginName();

    public abstract FilterDefinition getFilterDefinition();

    public abstract FrequencyAmplitudePhase getFilterResponse();

    @JsonCreator
    public static FilterParameters from(
      @JsonProperty("pluginName") String pluginName,
      @JsonProperty("filterDefinition") FilterDefinition filterDefinition,
      @JsonProperty("filterResponse") FrequencyAmplitudePhase filterResponse) {
      return new AutoValue_AmplitudeMeasurementChannelDefinition_FilterParameters(pluginName,
        filterDefinition, filterResponse);
    }
  }
}
