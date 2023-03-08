package gms.shared.waveform.api.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

@AutoValue
@JsonSerialize(as = ChannelTimeRangeRequest.class)
@JsonDeserialize(builder = AutoValue_ChannelTimeRangeRequest.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ChannelTimeRangeRequest {

  public abstract Set<Channel> getChannels();

  public abstract Instant getStartTime();

  public abstract Instant getEndTime();

  public abstract Optional<FacetingDefinition> getFacetingDefinition();

  public static ChannelTimeRangeRequest.Builder builder() {
    return new AutoValue_ChannelTimeRangeRequest.Builder();
  }

  public abstract ChannelTimeRangeRequest.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    ChannelTimeRangeRequest.Builder setChannels(Set<Channel> channels);

    Set<Channel> getChannels();

    ChannelTimeRangeRequest.Builder setStartTime(Instant startTime);

    Instant getStartTime();

    ChannelTimeRangeRequest.Builder setEndTime(Instant endTime);

    Instant getEndTime();

    ChannelTimeRangeRequest.Builder setFacetingDefinition(Optional<FacetingDefinition> facetingDefinition);

    ChannelTimeRangeRequest build();
  }
}
