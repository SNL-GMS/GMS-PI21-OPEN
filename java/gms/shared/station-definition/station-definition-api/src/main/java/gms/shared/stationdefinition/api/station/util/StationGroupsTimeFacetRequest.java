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
@JsonSerialize(as = StationGroupsTimeFacetRequest.class)
@JsonDeserialize(builder = AutoValue_StationGroupsTimeFacetRequest.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class StationGroupsTimeFacetRequest implements Request {

  @Override
  @JsonIgnore
  public ImmutableList<String> getNames() {
    return getStationGroupNames();
  }

  public abstract ImmutableList<String> getStationGroupNames();

  public abstract Optional<Instant> getEffectiveTime();

  public abstract Optional<FacetingDefinition> getFacetingDefinition();

  public static StationGroupsTimeFacetRequest.Builder builder() {
    return new AutoValue_StationGroupsTimeFacetRequest.Builder();
  }

  public abstract StationGroupsTimeFacetRequest.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    StationGroupsTimeFacetRequest.Builder setStationGroupNames(ImmutableList<String> stationGroupNames);

    default StationGroupsTimeFacetRequest.Builder setStationGroupNames(Collection<String> stationGroupNames) {
      return setStationGroupNames(ImmutableList.copyOf(stationGroupNames));
    }

    ImmutableList.Builder<String> stationGroupNamesBuilder();

    default StationGroupsTimeFacetRequest.Builder addStationGroupName(String stationGroupName) {
      stationGroupNamesBuilder().add(stationGroupName);
      return this;
    }

    StationGroupsTimeFacetRequest.Builder setEffectiveTime(Optional<Instant> effectiveTime);

    StationGroupsTimeFacetRequest.Builder setFacetingDefinition(Optional<FacetingDefinition> facetingDefinition);

    default StationGroupsTimeFacetRequest.Builder setEffectiveTime(Instant effectiveTime) {
      return setEffectiveTime(Optional.ofNullable(effectiveTime));
    }

    default StationGroupsTimeFacetRequest.Builder setFacetingDefinition(FacetingDefinition facetingDefinition) {
      return setFacetingDefinition(Optional.ofNullable(facetingDefinition));
    }

    StationGroupsTimeFacetRequest autoBuild();

    default StationGroupsTimeFacetRequest build() {
      StationGroupsTimeFacetRequest stationGroupsTimeFacetRequest = autoBuild();
      Validate.notEmpty(stationGroupsTimeFacetRequest.getStationGroupNames(),
        "Station groups time facet request must be provided a list of station group names");
      stationGroupsTimeFacetRequest.getEffectiveTime().ifPresent(data ->
        Preconditions.checkState(stationGroupsTimeFacetRequest.getEffectiveTime().isPresent()));
      stationGroupsTimeFacetRequest.getFacetingDefinition().ifPresent(data ->
        Preconditions.checkState(stationGroupsTimeFacetRequest.getFacetingDefinition().isPresent()));
      return stationGroupsTimeFacetRequest;
    }
  }
}
