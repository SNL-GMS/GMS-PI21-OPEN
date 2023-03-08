package gms.shared.stationdefinition.api.channel.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import gms.shared.stationdefinition.api.util.Request;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

@AutoValue
@JsonSerialize(as = ChannelsTimeFacetRequest.class)
@JsonDeserialize(builder = AutoValue_ChannelsTimeFacetRequest.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ChannelsTimeFacetRequest implements Request {
  @Override
  @JsonIgnore
  public ImmutableList<String> getNames() {
    return getChannelNames();
  }

  public abstract ImmutableList<String> getChannelNames();

  public abstract Optional<Instant> getEffectiveTime();

  public abstract Optional<FacetingDefinition> getFacetingDefinition();

  public static ChannelsTimeFacetRequest.Builder builder() {
    return new AutoValue_ChannelsTimeFacetRequest.Builder();
  }

  public abstract ChannelsTimeFacetRequest.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract ChannelsTimeFacetRequest.Builder setChannelNames(ImmutableList<String> channelNames);

    public ChannelsTimeFacetRequest.Builder setChannelNames(Collection<String> channelNames) {
      return setChannelNames(ImmutableList.copyOf(channelNames));
    }

    protected abstract ImmutableList.Builder<String> channelNamesBuilder();

    public ChannelsTimeFacetRequest.Builder addChannelGroupName(String channelName) {
      channelNamesBuilder().add(channelName);
      return this;
    }

    public ChannelsTimeFacetRequest.Builder setEffectiveTime(Instant effectiveTime) {
      return setEffectiveTime(Optional.ofNullable(effectiveTime));
    }

    public abstract ChannelsTimeFacetRequest.Builder setEffectiveTime(Optional<Instant> effectiveTime);

    public ChannelsTimeFacetRequest.Builder setFacetingDefinition(FacetingDefinition facetingDefinition) {
      return setFacetingDefinition(Optional.ofNullable(facetingDefinition));
    }

    public abstract ChannelsTimeFacetRequest.Builder setFacetingDefinition(
      Optional<FacetingDefinition> facetingDefinition);

    public abstract ChannelsTimeFacetRequest build();
  }
}
