package gms.shared.stationdefinition.coi.station;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelGroup;
import gms.shared.stationdefinition.coi.channel.Location;
import gms.shared.stationdefinition.coi.channel.RelativePosition;
import gms.shared.stationdefinition.coi.channel.RelativePositionChannelPair;
import gms.shared.stationdefinition.coi.id.VersionId;
import gms.shared.stationdefinition.coi.utils.StationDefinitionObject;
import org.apache.commons.lang3.Validate;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static gms.shared.stationdefinition.coi.utils.StationDefinitionCoiUtils.STATION_COMPARATOR;

@AutoValue
@JsonSerialize(as = AutoValue_Station.class)
@JsonDeserialize(builder = AutoValue_Station.Builder.class)
@JsonPropertyOrder(alphabetic = true)
public abstract class Station implements StationDefinitionObject, Comparable<Station> {
  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    Station.Builder setName(String name);

    String getName();

    default Station.Builder setEffectiveAt(Instant effectiveAt) {
      return setEffectiveAt(Optional.ofNullable(effectiveAt));
    }

    Station.Builder setEffectiveAt(Optional<Instant> effectiveAt);

    @JsonUnwrapped
    default Station.Builder setData(Station.Data data) {
      return setData(Optional.ofNullable(data));
    }

    Station.Builder setData(Optional<Station.Data> data);

    Station autoBuild();

    default Station build() {
      var station = autoBuild();
      Validate.notEmpty(station.getName(), "Station must be provided a name");
      station.getData().ifPresent(data -> Preconditions.checkState(station.getEffectiveAt().isPresent()));

      return autoBuild();
    }
  }

  public static Station.Builder builder() {
    return new AutoValue_Station.Builder();
  }

  public abstract Station.Builder toBuilder();

  public static Station createEntityReference(String name) {
    return Station.builder()
      .setName(name)
      .build();
  }

  public Station toEntityReference() {
    return new AutoValue_Station.Builder()
      .setName(getName())
      .build();
  }

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

  public static Station createVersionReference(String name, Instant effectiveAt) {
    Objects.requireNonNull(effectiveAt);
    return Station.builder()
      .setName(name)
      .setEffectiveAt(effectiveAt)
      .build();
  }

  public abstract String getName();

  @JsonIgnore
  public StationType getType() {
    return getDataOrThrow().getType();
  }

  @JsonIgnore
  public String getDescription() {
    return getDataOrThrow().getDescription();
  }


  @JsonIgnore
  public Map<Channel, RelativePosition> getRelativePositionsByChannel() {
    return getDataOrThrow().getRelativePositionsByChannel();
  }

  @JsonIgnore
  public List<RelativePositionChannelPair> getRelativePositionChannelPairs() {
    return getDataOrThrow().getRelativePositionChannelPairs();
  }

  @JsonIgnore
  public Location getLocation() {
    return getDataOrThrow().getLocation();
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
  public NavigableSet<ChannelGroup> getChannelGroups() {
    return getDataOrThrow().getChannelGroups();
  }

  @JsonIgnore
  public NavigableSet<Channel> getAllRawChannels() {
    return getDataOrThrow().getAllRawChannels();
  }

  @JsonIgnore
  public boolean isPresent() {
    return getData().isPresent();
  }

  @JsonUnwrapped
  @JsonProperty(access = Access.READ_ONLY)
  public abstract Optional<Station.Data> getData();

  private Station.Data getDataOrThrow() {
    return getData().orElseThrow(() -> new IllegalStateException("Only contains ID facet"));
  }

  @AutoValue
  @JsonSerialize(as = Station.Data.class)
  @JsonDeserialize(builder = AutoValue_Station_Data.Builder.class)
  @JsonPropertyOrder(alphabetic = true)
  public abstract static class Data {

    public abstract Data.Builder toBuilder();

    @AutoValue.Builder
    @JsonPOJOBuilder(withPrefix = "set")
    public interface Builder {

      Station.Data.Builder setType(StationType stationType);

      Optional<StationType> getType();

      Station.Data.Builder setDescription(String description);

      Optional<String> getDescription();

      Station.Data.Builder setRelativePositionChannelPairs(
        List<RelativePositionChannelPair> relativePositionChannelPairList);

      Optional<List<RelativePositionChannelPair>> getRelativePositionChannelPairs();

      Station.Data.Builder setLocation(Location location);

      Optional<Location> getLocation();

      Station.Data.Builder setEffectiveUntil(Optional<Instant> effectiveUntil);

      default Station.Data.Builder setEffectiveUntil(Instant effectiveUntil) {
        return setEffectiveUntil(Optional.ofNullable(effectiveUntil));
      }

      Optional<Instant> getEffectiveUntil();

      Optional<Boolean> getEffectiveAtUpdatedByResponse();

      Station.Data.Builder setEffectiveAtUpdatedByResponse(Optional<Boolean> effectiveAtUpdatedByResponse);

      Optional<Boolean> getEffectiveUntilUpdatedByResponse();

      Station.Data.Builder setEffectiveUntilUpdatedByResponse(Optional<Boolean> effectiveUntilUpdatedByResponse);


      Station.Data.Builder setChannelGroups(NavigableSet<ChannelGroup> channelGroups);

      default Data.Builder setChannelGroups(Collection<ChannelGroup> channelGroups) {
        var treeSet = new TreeSet<ChannelGroup>();
        treeSet.addAll(channelGroups);

        return setChannelGroups(treeSet);
      }

      Optional<NavigableSet<ChannelGroup>> getChannelGroups();

      Station.Data.Builder setAllRawChannels(NavigableSet<Channel> channels);

      default Station.Data.Builder setAllRawChannels(Collection<Channel> channels) {
        var treeSet = new TreeSet<Channel>();
        treeSet.addAll(channels);
        return setAllRawChannels(treeSet);
      }

      Optional<NavigableSet<Channel>> getAllRawChannels();

      Station.Data autoBuild();

      default Station.Data build() {

        final List<Optional<?>> allFields = List
          .of(getType(), getDescription(), getRelativePositionChannelPairs(),
            getLocation(), getChannelGroups(), getAllRawChannels());
        final long numPresentFields = allFields.stream().filter(Optional::isPresent).count();

        if (0 == numPresentFields) {
          return null;
        } else if (allFields.size() == numPresentFields) {

          Validate.notEmpty(getAllRawChannels().orElseThrow(),
            "Station must have a non-empty list of channels");
          Validate
            .isTrue(getDescription().orElseThrow().length() <= 1024,
              "Descriptions can be no longer than 1024 characters");
          Validate.notEmpty(getChannelGroups().orElseThrow(),
            "Station must have a non-empty list of channel groups");
          Validate.notEmpty(getRelativePositionChannelPairs().orElseThrow(),
            "Station being passed an empty or null map of relative positions for channels it manages");
          setRelativePositionChannelPairs(getRelativePositionChannelPairs().orElseThrow().stream()
            .sorted(Comparator.comparing(Functions.compose(Channel::getName, RelativePositionChannelPair::getChannel)))
            .collect(Collectors.toList()));


          Map<Channel, RelativePosition> relativePositionsByChannel = getRelativePositionChannelPairs()
            .orElseThrow()
            .stream()
            .collect(Collectors.toMap(pair -> pair.getChannel().toEntityReference(), RelativePositionChannelPair::getRelativePosition));
          Validate.isTrue(getAllRawChannels().orElseThrow().stream()
              .allMatch(channel -> relativePositionsByChannel.containsKey(channel.toEntityReference())),
            "Station passed in a relative position for a channel it does not manage");

          final var channelNames = getAllRawChannels().orElseThrow().stream().map(Channel::getName)
            .collect(Collectors.toList());

          Validate
            .isTrue(
              getChannelGroups().orElseThrow().stream().noneMatch(ChannelGroup::isPresent) ||

                channelNames.stream().allMatch(getChannelGroups().orElseThrow().stream()
                  .map(ChannelGroup::getChannels)
                  .flatMap(Collection::stream)
                  .map(Channel::getName)
                  .collect(Collectors.toSet())::contains),
              "All raw channels must be present in channel groups.");

          return autoBuild();
        }

        throw new IllegalStateException(
          "Either all FacetedDataClass fields must be populated or none of them can be populated");
      }
    }

    public abstract StationType getType();

    public abstract String getDescription();


    @JsonIgnore
    @Memoized
    public Map<Channel, RelativePosition> getRelativePositionsByChannel() {
      return getRelativePositionChannelPairs().stream()
        .collect(Collectors.toMap(
          Functions.compose(Channel::toEntityReference, RelativePositionChannelPair::getChannel),
          RelativePositionChannelPair::getRelativePosition));
    }

    public abstract List<RelativePositionChannelPair> getRelativePositionChannelPairs();


    public abstract Location getLocation();

    public abstract Optional<Instant> getEffectiveUntil();

    @JsonIgnore
    public abstract Optional<Boolean> getEffectiveAtUpdatedByResponse();

    @JsonIgnore
    public abstract Optional<Boolean> getEffectiveUntilUpdatedByResponse();

    public abstract NavigableSet<ChannelGroup> getChannelGroups();

    public abstract NavigableSet<Channel> getAllRawChannels();

    public static Data.Builder builder() {
      return new AutoValue_Station_Data.Builder();
    }

  }


  @Override
  public int compareTo(Station otherStation) {
    return STATION_COMPARATOR.compare(this, otherStation);
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