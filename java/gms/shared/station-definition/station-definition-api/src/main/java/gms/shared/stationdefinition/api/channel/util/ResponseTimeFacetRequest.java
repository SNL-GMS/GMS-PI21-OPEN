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
import java.util.UUID;
import java.util.stream.Collectors;

@AutoValue
@JsonSerialize(as = ResponseTimeFacetRequest.class)
@JsonDeserialize(builder = AutoValue_ResponseTimeFacetRequest.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ResponseTimeFacetRequest implements Request {

  @Override
  @JsonIgnore
  public ImmutableList<String> getNames() {
    return ImmutableList.copyOf(getResponseIds().stream().map(UUID::toString).collect(Collectors.toList()));
  }

  public abstract ImmutableList<UUID> getResponseIds();

  public abstract Optional<Instant> getEffectiveTime();

  public abstract Optional<FacetingDefinition> getFacetingDefinition();

  public static ResponseTimeFacetRequest.Builder builder() {
    return new AutoValue_ResponseTimeFacetRequest.Builder();
  }

  public abstract ResponseTimeFacetRequest.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    ResponseTimeFacetRequest.Builder setResponseIds(ImmutableList<UUID> responseIds);

    default ResponseTimeFacetRequest.Builder setResponseIds(Collection<UUID> responseIds) {
      return setResponseIds(ImmutableList.copyOf(responseIds));
    }

    ImmutableList.Builder<UUID> responseIdsBuilder();

    default ResponseTimeFacetRequest.Builder addResponseId(UUID responseId) {
      responseIdsBuilder().add(responseId);
      return this;
    }

    ResponseTimeFacetRequest.Builder setEffectiveTime(Optional<Instant> effectiveTime);

    ResponseTimeFacetRequest.Builder setFacetingDefinition(Optional<FacetingDefinition> facetingDefinition);

    ResponseTimeFacetRequest build();
  }
}
