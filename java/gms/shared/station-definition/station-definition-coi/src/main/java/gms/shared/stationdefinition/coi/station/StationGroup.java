package gms.shared.stationdefinition.coi.station;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import gms.shared.stationdefinition.coi.id.VersionId;
import org.apache.commons.lang3.Validate;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;

import static gms.shared.stationdefinition.coi.utils.StationDefinitionCoiUtils.STATION_COMPARATOR;
import static gms.shared.stationdefinition.coi.utils.StationDefinitionCoiUtils.STATION_GROUP_COMPARATOR;

@AutoValue
@JsonDeserialize(builder = AutoValue_StationGroup.Builder.class)
@JsonPropertyOrder(alphabetic = true)
public abstract class StationGroup implements Comparable<StationGroup> {

  public static Builder builder() {
    return new AutoValue_StationGroup.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    StationGroup.Builder setName(String name);

    String getName();

    default StationGroup.Builder setEffectiveAt(Instant effectiveAt) {
      return setEffectiveAt(Optional.ofNullable(effectiveAt));
    }

    StationGroup.Builder setEffectiveAt(Optional<Instant> effectiveAt);

    default StationGroup.Builder setEffectiveUntil(Instant effectiveUntil) {
      return setEffectiveUntil(Optional.ofNullable(effectiveUntil));
    }

    StationGroup.Builder setEffectiveUntil(Optional<Instant> effectiveUntil);

    @JsonUnwrapped
    default StationGroup.Builder setData(StationGroup.Data data) {
      setData(Optional.ofNullable(data));
      return this;
    }

    StationGroup.Builder setData(Optional<StationGroup.Data> data);

    StationGroup autoBuild();

    default StationGroup build() {
      var stationGroup = autoBuild();
      Validate.notEmpty(stationGroup.getName(), "Station group must be provided a name");
      stationGroup.getData().ifPresent(data -> Preconditions.checkState(stationGroup.getEffectiveAt().isPresent()));
      return autoBuild();
    }
  }

  public static StationGroup createEntityReference(String name) {
    return StationGroup.builder()
      .setName(name)
      .build();
  }

  public StationGroup toEntityReference() {
    return new AutoValue_StationGroup.Builder()
      .setName(getName())
      .build();
  }

  public static StationGroup createVersionReference(String name, Instant effectiveAt) {
    Objects.requireNonNull(effectiveAt);
    return StationGroup.builder()
      .setName(name)
      .setEffectiveAt(effectiveAt)
      .build();
  }

  public abstract String getName();

  public abstract Optional<Instant> getEffectiveAt();

  public abstract Optional<Instant> getEffectiveUntil();

  @JsonIgnore
  public String getDescription() {
    return getDataOrThrow().getDescription();
  }

  @JsonIgnore
  public NavigableSet<Station> getStations() {
    return getDataOrThrow().getStations();
  }

  @JsonIgnore
  public boolean isPresent() {
    return getData().isPresent();
  }

  @JsonUnwrapped
  @JsonProperty(access = Access.READ_ONLY)
  public abstract Optional<StationGroup.Data> getData();

  private Data getDataOrThrow() {
    return getData().orElseThrow(() -> new IllegalStateException("Only contains ID facet"));
  }

  @AutoValue
  @JsonDeserialize(builder = AutoValue_StationGroup_Data.Builder.class)
  @JsonPropertyOrder(alphabetic = true)
  public abstract static class Data {

    public static Builder builder() {
      return new AutoValue_StationGroup_Data.Builder();
    }

    public abstract StationGroup.Data.Builder toBuilder();

    @AutoValue.Builder
    @JsonPOJOBuilder(withPrefix = "set")
    public interface Builder {

      StationGroup.Data.Builder setDescription(String description);

      Optional<String> getDescription();

      default StationGroup.Data.Builder setStations(Collection<Station> stations) {
        final var stationSet = new TreeSet<>(STATION_COMPARATOR);
        stationSet.addAll(stations);
        return setStations(stationSet);
      }

      StationGroup.Data.Builder setStations(NavigableSet<Station> stations);

      Optional<NavigableSet<Station>> getStations();

      StationGroup.Data autoBuild();

      default StationGroup.Data build() {

        final List<Optional<?>> allFields = List
          .of(getDescription(), getStations());
        final long numPresentFields = allFields.stream().filter(Optional::isPresent).count();

        if (0 == numPresentFields) {
          return null;
        } else if (allFields.size() == numPresentFields) {
          Validate.notEmpty(getDescription().orElseThrow(),
            "Station group must be provided a description");
          Validate.notEmpty(getStations().orElseThrow(),
            "Station group must have a non-empty list of stations");

          return autoBuild();
        }

        throw new IllegalStateException(
          "Either all FacetedDataClass fields must be populated or none of them can be populated");
      }
    }

    public abstract String getDescription();

    public abstract NavigableSet<Station> getStations();
  }

  @Override
  public int compareTo(StationGroup otherStationGroup) {
    return STATION_GROUP_COMPARATOR.compare(this, otherStationGroup);
  }

  @Override
  public abstract int hashCode();

  @Override
  public abstract boolean equals(Object obj);

  @JsonIgnore
  public VersionId toVersionId() {
    return VersionId.builder()
      .setEntityId(getName())
      .setEffectiveAt(getEffectiveAt()
        .orElseThrow(() -> new IllegalStateException("Cannot create version id from entity instantiation")))
      .build();
  }
}