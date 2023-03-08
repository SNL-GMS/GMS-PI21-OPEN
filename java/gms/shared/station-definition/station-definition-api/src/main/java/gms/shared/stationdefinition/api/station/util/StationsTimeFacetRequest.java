package gms.shared.stationdefinition.api.station.util;

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
@JsonSerialize(as = StationsTimeFacetRequest.class)
@JsonDeserialize(builder = AutoValue_StationsTimeFacetRequest.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class StationsTimeFacetRequest implements Request {

  @Override
  @JsonIgnore
  public ImmutableList<String> getNames() {
    return getStationNames();
  }

  public abstract ImmutableList<String> getStationNames();

  public abstract Optional<Instant> getEffectiveTime();

  public abstract Optional<FacetingDefinition> getFacetingDefinition();

  public static StationsTimeFacetRequest.Builder builder() {
    return new AutoValue_StationsTimeFacetRequest.Builder();
  }

  public abstract StationsTimeFacetRequest.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    StationsTimeFacetRequest.Builder setStationNames(ImmutableList<String> stationNames);

    default StationsTimeFacetRequest.Builder setStationNames(Collection<String> stationNames) {
      return setStationNames(ImmutableList.copyOf(stationNames));
    }

    ImmutableList.Builder<String> stationNamesBuilder();

    default StationsTimeFacetRequest.Builder addStationName(String stationName) {
      stationNamesBuilder().add(stationName);
      return this;
    }

    StationsTimeFacetRequest.Builder setEffectiveTime(Optional<Instant> effectiveTime);

    StationsTimeFacetRequest.Builder setFacetingDefinition(Optional<FacetingDefinition> facetingDefinition);

    default StationsTimeFacetRequest.Builder setEffectiveTime(Instant effectiveTime) {
      return setEffectiveTime(Optional.ofNullable(effectiveTime));
    }

    default StationsTimeFacetRequest.Builder setFacetingDefinition(FacetingDefinition facetingDefinition) {
      return setFacetingDefinition(Optional.ofNullable(facetingDefinition));
    }

    StationsTimeFacetRequest autoBuild();

    default StationsTimeFacetRequest build() {
      StationsTimeFacetRequest stationsTimeFacetRequest = autoBuild();
      Validate.notEmpty(stationsTimeFacetRequest.getStationNames(),
        "Stations time facet request must be provided a list of station names");
      stationsTimeFacetRequest.getEffectiveTime().ifPresent(data ->
        Preconditions.checkState(stationsTimeFacetRequest.getEffectiveTime().isPresent()));
      stationsTimeFacetRequest.getFacetingDefinition().ifPresent(data ->
        Preconditions.checkState(stationsTimeFacetRequest.getFacetingDefinition().isPresent()));
      return stationsTimeFacetRequest;
    }
  }
}
