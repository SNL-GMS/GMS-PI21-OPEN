package gms.shared.stationdefinition.api.channel.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import gms.shared.stationdefinition.api.util.Request;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;
import org.apache.commons.lang3.Validate;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

@AutoValue
@JsonSerialize(as = ChannelGroupsTimeFacetRequest.class)
@JsonDeserialize(builder = AutoValue_ChannelGroupsTimeFacetRequest.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ChannelGroupsTimeFacetRequest implements Request {
  @Override
  @JsonIgnore
  public ImmutableList<String> getNames() {
    return getChannelGroupNames();
  }

  public abstract ImmutableList<String> getChannelGroupNames();

  public abstract Optional<Instant> getEffectiveTime();

  public abstract Optional<FacetingDefinition> getFacetingDefinition();

  public static ChannelGroupsTimeFacetRequest.Builder builder() {
    return new AutoValue_ChannelGroupsTimeFacetRequest.Builder();
  }

  public abstract ChannelGroupsTimeFacetRequest.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    ChannelGroupsTimeFacetRequest.Builder setChannelGroupNames(ImmutableList<String> channelGroupNames);

    ChannelGroupsTimeFacetRequest.Builder setEffectiveTime(Optional<Instant> effectiveTime);

    ChannelGroupsTimeFacetRequest.Builder setFacetingDefinition(Optional<FacetingDefinition> facetingDefinition);

    default ChannelGroupsTimeFacetRequest.Builder setChannelGroupNames(Collection<String> channelGroupNames) {
      return setChannelGroupNames(ImmutableList.copyOf(channelGroupNames));
    }

    ImmutableList.Builder<String> channelGroupNamesBuilder();

    default ChannelGroupsTimeFacetRequest.Builder addChannelGroupName(String channelGroupName) {
      channelGroupNamesBuilder().add(channelGroupName);
      return this;
    }

    default ChannelGroupsTimeFacetRequest.Builder setEffectiveTime(Instant effectiveTime) {
      return setEffectiveTime(Optional.ofNullable(effectiveTime));
    }

    default ChannelGroupsTimeFacetRequest.Builder setFacetingDefinition(FacetingDefinition facetingDefinition) {
      return setFacetingDefinition(Optional.ofNullable(facetingDefinition));
    }

    ChannelGroupsTimeFacetRequest autoBuild();

    default ChannelGroupsTimeFacetRequest build() {
      ChannelGroupsTimeFacetRequest channelGroupsTimeFacetRequest = autoBuild();
      Validate.notEmpty(channelGroupsTimeFacetRequest.getChannelGroupNames(),
        "Channel groups time facet request must be provided a list of channel group names");
      channelGroupsTimeFacetRequest.getEffectiveTime().ifPresent(data ->
        Preconditions.checkState(channelGroupsTimeFacetRequest.getEffectiveTime().isPresent()));
      channelGroupsTimeFacetRequest.getFacetingDefinition().ifPresent(data ->
        Preconditions.checkState(channelGroupsTimeFacetRequest.getFacetingDefinition().isPresent()));
      return channelGroupsTimeFacetRequest;
    }
  }
}
