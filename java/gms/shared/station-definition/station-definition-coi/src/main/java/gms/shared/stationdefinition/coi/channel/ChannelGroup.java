package gms.shared.stationdefinition.coi.channel;

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
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.coi.utils.StationDefinitionObject;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;

import static gms.shared.stationdefinition.coi.utils.StationDefinitionCoiUtils.CHANNEL_COMPARATOR;
import static gms.shared.stationdefinition.coi.utils.StationDefinitionCoiUtils.CHANNEL_GROUP_COMPARATOR;

@AutoValue
@JsonDeserialize(builder = AutoValue_ChannelGroup.Builder.class)
@JsonPropertyOrder(alphabetic = true)
public abstract class ChannelGroup implements StationDefinitionObject, Comparable<ChannelGroup> {

  public enum ChannelGroupType {
    PROCESSING_GROUP,
    PHYSICAL_SITE
  }

  public static Builder builder() {
    return new AutoValue_ChannelGroup.Builder();
  }

  public abstract Builder toBuilder();

  public StationDefinitionObject setEffectiveAt(Instant effectiveAt) {
    return this.toBuilder().setEffectiveAt(effectiveAt)
      .build();
  }

  public StationDefinitionObject setEffectiveUntil(Instant effectiveUntil) {
    return this.toBuilder().setData(
        this.getData().orElseThrow().toBuilder().setEffectiveUntil(effectiveUntil).build())
      .build();
  }

  public StationDefinitionObject setEffectiveAtUpdatedByResponse(Boolean effectiveAtUpdatedByResponse){
    return this.toBuilder().setData(
        this.getData().orElseThrow().toBuilder().setEffectiveAtUpdatedByResponse(
          Optional.of(effectiveAtUpdatedByResponse)).build())
      .build();
  }
  public StationDefinitionObject setEffectiveUntilUpdatedByResponse(Boolean effectiveUntilUpdatedByResponse){
    return this.toBuilder().setData(
        this.getData().orElseThrow().toBuilder().setEffectiveUntilUpdatedByResponse(
          Optional.of(effectiveUntilUpdatedByResponse)).build())
      .build();
  }
  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    ChannelGroup.Builder setName(String name);

    String getName();

    default ChannelGroup.Builder setEffectiveAt(Instant effectiveAt) {
      return setEffectiveAt(Optional.ofNullable(effectiveAt));
    }

    ChannelGroup.Builder setEffectiveAt(Optional<Instant> effectiveAt);

    @JsonUnwrapped
    default ChannelGroup.Builder setData(ChannelGroup.Data data) {
      return setData(Optional.ofNullable(data));
    }

    ChannelGroup.Builder setData(Optional<ChannelGroup.Data> data);

    ChannelGroup autoBuild();

    default ChannelGroup build() {
      var channelGroup = autoBuild();
      Validate.notEmpty(channelGroup.getName(), "Channel group must be provided a name");
      channelGroup.getData().ifPresent(data -> Preconditions.checkState(channelGroup.getEffectiveAt().isPresent()));
      return channelGroup;
    }

  }

  public static ChannelGroup createEntityReference(String name) {
    return new AutoValue_ChannelGroup.Builder()
      .setName(name)
      .build();
  }

  public ChannelGroup toEntityReference() {
    return new AutoValue_ChannelGroup.Builder()
      .setName(getName())
      .build();
  }
  public static ChannelGroup createEntityReference(ChannelGroup channelGroup) {
    Objects.requireNonNull(channelGroup);
    return new AutoValue_ChannelGroup.Builder()
      .setName(channelGroup.getName())
      .build();
  }

  public static ChannelGroup createVersionReference(ChannelGroup channelGroup) {
    Objects.requireNonNull(channelGroup);
    return new AutoValue_ChannelGroup.Builder()
      .setName(channelGroup.getName())
      .setEffectiveAt(channelGroup.getEffectiveAt())
      .build();
  }
  public static ChannelGroup createVersionReference(String name, Instant effectiveAt) {
    Objects.requireNonNull(effectiveAt);
    return new AutoValue_ChannelGroup.Builder()
      .setName(name)
      .setEffectiveAt(effectiveAt)
      .build();
  }

  public abstract String getName();

  @JsonIgnore
  public String getDescription() {
    return getDataOrThrow().getDescription();
  }


  @JsonIgnore
  public Optional<Location> getLocation() {
    final var location = getDataOrThrow().getLocation();
    return Optional.ofNullable(location);
  }

  @JsonIgnore
  public Optional<Station> getStation() {
    return Optional.ofNullable(getDataOrThrow().getStation());
  }

  @JsonIgnore
  public Optional<Instant> getEffectiveUntil() {
    return getDataOrThrow().getEffectiveUntil();
  }

  @JsonIgnore
  public Optional<Boolean> getEffectiveAtUpdatedByResponse() {
    return getDataOrThrow().getEffectiveAtUpdatedByResponse();
  }
  @JsonIgnore
  public Optional<Boolean> getEffectiveUntilUpdatedByResponse() {
    return getDataOrThrow().getEffectiveUntilUpdatedByResponse();
  }
  @JsonIgnore
  public ChannelGroupType getType() {
    return getDataOrThrow().getType();
  }

  @JsonIgnore
  public NavigableSet<Channel> getChannels() {
    return getDataOrThrow().getChannels();
  }

  @JsonIgnore
  public boolean isPresent() {
    return getData().isPresent();
  }

  @JsonUnwrapped
  @JsonProperty(access = Access.READ_ONLY)
  public abstract Optional<ChannelGroup.Data> getData();

  private Data getDataOrThrow() {
    return getData().orElseThrow(() -> new IllegalStateException("Only contains ID facet"));
  }

  @AutoValue
  @JsonDeserialize(builder = AutoValue_ChannelGroup_Data.Builder.class)
  @JsonPropertyOrder(alphabetic = true)
  public abstract static class Data {

    public static Builder builder() {
      return new AutoValue_ChannelGroup_Data.Builder();
    }

    public abstract ChannelGroup.Data.Builder toBuilder();

    @AutoValue.Builder
    @JsonPOJOBuilder(withPrefix = "set")
    public interface Builder {

      ChannelGroup.Data.Builder setDescription(String description);

      Optional<String> getDescription();

      ChannelGroup.Data.Builder setLocation(Location location);

      Location getLocation();

      ChannelGroup.Data.Builder setStation(Station station);

      Station getStation();

      ChannelGroup.Data.Builder setEffectiveUntil(Optional<Instant> effectiveUntil);

      default ChannelGroup.Data.Builder setEffectiveUntil(Instant effectiveUntil) {
        return setEffectiveUntil(Optional.ofNullable(effectiveUntil));
      }

      Optional<Instant> getEffectiveUntil();

      Optional<Boolean> getEffectiveAtUpdatedByResponse();

      ChannelGroup.Data.Builder setEffectiveAtUpdatedByResponse(Optional<Boolean> effectiveAtUpdatedByResponse);

      Optional<Boolean> getEffectiveUntilUpdatedByResponse();

      ChannelGroup.Data.Builder setEffectiveUntilUpdatedByResponse(Optional<Boolean> effectiveUntilUpdatedByResponse);

      ChannelGroup.Data.Builder setType(ChannelGroupType type);

      Optional<ChannelGroupType> getType();

      default ChannelGroup.Data.Builder setChannels(Collection<Channel> channels) {
        final var channelSet = new TreeSet<>(CHANNEL_COMPARATOR);
        channelSet.addAll(channels);
        setChannels(channelSet);
        return this;
      }

      ChannelGroup.Data.Builder setChannels(NavigableSet<Channel> channels);

      Optional<NavigableSet<Channel>> getChannels();

      ChannelGroup.Data autoBuild();

      default ChannelGroup.Data build() {

        final List<Optional<?>> allFields = List.of(getDescription(), getType(), getChannels());

        final long numPresentFields = allFields.stream().filter(Optional::isPresent).count();

        if (0 == numPresentFields && getLocation() == null) {
          return null;
        } else if (allFields.size() == numPresentFields) {
          Validate.notEmpty(getChannels().orElseThrow(),
            "ChannelGroup must have a non-zero number of channels");

          return autoBuild();
        }

        throw new IllegalStateException(
          "Either all FacetedDataClass fields must be populated or none of them can be populated");
      }
    }

    public abstract String getDescription();

    @Nullable
    public abstract Location getLocation();

    @Nullable
    @JsonIgnore
    public abstract Station getStation();

    public abstract Optional<Instant> getEffectiveUntil();

    @JsonIgnore
    public abstract Optional<Boolean> getEffectiveAtUpdatedByResponse();

    @JsonIgnore
    public abstract Optional<Boolean> getEffectiveUntilUpdatedByResponse();

    public abstract ChannelGroupType getType();

    public abstract NavigableSet<Channel> getChannels();
  }

  @Override
  public int compareTo(ChannelGroup otherChannelGroup) {
    return CHANNEL_GROUP_COMPARATOR.compare(this, otherChannelGroup);
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
