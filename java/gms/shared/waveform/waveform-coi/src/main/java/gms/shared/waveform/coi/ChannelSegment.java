package gms.shared.waveform.coi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.Range;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.waveform.coi.util.TimeseriesUtility;
import org.apache.commons.lang3.Validate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents a segment of  data from a Channel.
 */
@AutoValue
@JsonSerialize(as = ChannelSegment.class)
@JsonDeserialize(builder = AutoValue_ChannelSegment.Builder.class)
public abstract class ChannelSegment<T extends Timeseries> implements Comparable<ChannelSegment<T>> {

  /**
   * Creates a ChannelSegment from all params.  NOTE: This method is only here to support Jackson's
   * JSON deserialization and the passed in timeseriesType value is not used, but rather this value
   * is derived from the actual class type of the timeseries.
   *
   * @param <T> The type of the data stored by the ChannelSegment
   * @param channel the id of the processing channel the segment is from.
   * @param units the units of the channel segment
   * @param series The data of the ChannelSegment.
   * @return the newly constructed ChannelSegment
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static <T extends Timeseries> ChannelSegment<T> from(
    Channel channel,
    Units units,
    Collection<T> series,
    Instant creationTime) {

    Validate.notEmpty(series, "ChannelSegment requires at least one timeseries");
    Validate.notNull(channel, "Channel is required to be non-null");
    Validate.notNull(creationTime, "CreationTime is required to be non-null");

    var timeseriesType = series.iterator().next().getType();

    Validate.notNull(timeseriesType, "Unsupported Timeseries type: "
      + series.iterator().next().getClass());
    List<T> sortedSeries = new ArrayList<>(series);
    Collections.sort(sortedSeries);
    final Range<Instant> timeRange = TimeseriesUtility.computeSpan(sortedSeries);

    return ChannelSegment.<T>builder()
      .setId(
        ChannelSegmentDescriptor.from(
          channel,
          timeRange.lowerEndpoint(),
          timeRange.upperEndpoint(),
          creationTime))
      .setData(ChannelSegment.Data.<T>builder()
        .setUnits(units)
        .setTimeseriesType(timeseriesType)
        .setTimeseries(sortedSeries)
        .build())
      .build();
  }

  /**
   * Creates a channel segment using the provided channel segment descriptor, units, and timeseries
   *
   * @param <T> The type of the data stored by the ChannelSegment
   * @param channelSegmentDescriptor the channel segment descriptor corresponding to the channel segment
   * @param units the units of the channel segment
   * @param series The data of the ChannelSegment.
   * @return the newly constructed ChannelSegment
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if timeseries is empty
   */
  public static <T extends Timeseries> ChannelSegment<T> from(
    ChannelSegmentDescriptor channelSegmentDescriptor,
    Units units,
    Collection<T> series) {

    Validate.notEmpty(series, "ChannelSegment requires at least one timeseries");
    Validate.notNull(channelSegmentDescriptor, "Channel segment descriptor is required to be non-null");

    var timeseriesType = series.iterator().next().getType();

    Validate.notNull(timeseriesType, "Unsupported Timeseries type: "
      + series.iterator().next().getClass());
    List<T> sortedSeries = new ArrayList<>(series);
    Collections.sort(sortedSeries);

    return ChannelSegment.<T>builder()
      .setId(channelSegmentDescriptor)
      .setData(ChannelSegment.Data.<T>builder()
        .setUnits(units)
        .setTimeseriesType(timeseriesType)
        .setTimeseries(sortedSeries)
        .build())
      .build();
  }

  /**
   * Gets the ChannelSegmentDescriptor which acts as a unique id for ChannelSegments
   * Returned in unwrapped form to support UI integration
   *
   * @return the id
   */
  public abstract ChannelSegmentDescriptor getId();

  /**
   * Gets the type of this segment
   *
   * @return the type of the segment
   */
  @JsonIgnore
  public Timeseries.Type getTimeseriesType() {
    return getDataOrThrow().getTimeseriesType();
  }

  /**
   * Gets the units of this segment
   *
   * @return the units
   */
  @JsonIgnore
  public Units getUnits() {
    return getDataOrThrow().getUnits();
  }

  /**
   * Gets the timeseries that this segment contains. The returned list is sorted and immutable.
   *
   * @return {@link List} of T, not null
   */
  @JsonIgnore
  public List<T> getTimeseries() {
    return getDataOrThrow().getTimeseries();
  }

  @JsonIgnore
  public boolean isPresent() {
    return getData().isPresent();
  }

  /**
   * Compares two ChannelSegments by their start time. However, if their times are equal then they
   * are compared based on the .equals method.
   *
   * @param cs the segment to compare this one to
   * @return int
   */
  @Override
  public int compareTo(ChannelSegment<T> cs) {
    if (!getId().getStartTime().equals(cs.getId().getStartTime())) {
      return getId().getStartTime().compareTo(cs.getId().getStartTime());
    }
    if (!getId().getEndTime().equals(cs.getId().getEndTime())) {
      return getId().getEndTime().compareTo(cs.getId().getEndTime());
    }
    return this.equals(cs) ? 0 : 1;
  }

  @Override
  public abstract boolean equals(Object obj);

  @Override
  public abstract int hashCode();

  public static <D extends Timeseries> ChannelSegment.Builder<D> builder() {
    return new AutoValue_ChannelSegment.Builder<>();
  }

  public abstract Builder<T> toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder<T extends Timeseries> {

    Builder<T> setId(ChannelSegmentDescriptor channelSegmentDescriptor);

    @JsonUnwrapped
    default ChannelSegment.Builder<T> setData(ChannelSegment.Data<T> data) {
      return setData(Optional.ofNullable(data));
    }

    ChannelSegment.Builder<T> setData(Optional<ChannelSegment.Data<T>> data);

    ChannelSegment<T> build();
  }

  @JsonUnwrapped
  @JsonProperty(access = Access.READ_ONLY)
  public abstract Optional<ChannelSegment.Data<T>> getData();

  private ChannelSegment.Data<T> getDataOrThrow() {
    return getData().orElseThrow(() -> new IllegalStateException("Only contains ID facet"));
  }

  @AutoValue
  @JsonSerialize(as = ChannelSegment.Data.class)
  @JsonDeserialize(builder = AutoValue_ChannelSegment_Data.Builder.class)
  public abstract static class Data<T extends Timeseries> {

    public static <D extends Timeseries> ChannelSegment.Data.Builder<D> builder() {
      return new AutoValue_ChannelSegment_Data.Builder<>();
    }

    public abstract ChannelSegment.Data.Builder<T> toBuilder();

    public abstract Units getUnits();

    public abstract Timeseries.Type getTimeseriesType();

    public abstract List<T> getTimeseries();

    @AutoValue.Builder
    @JsonPOJOBuilder(withPrefix = "set", buildMethodName = "deserializeBuild")
    public interface Builder<T extends Timeseries> {
      ChannelSegment.Data.Builder<T> setUnits(Units units);

      Optional<Units> getUnits();

      ChannelSegment.Data.Builder<T> setTimeseriesType(Timeseries.Type timeseriesType);

      Optional<Timeseries.Type> getTimeseriesType();

      ChannelSegment.Data.Builder<T> setTimeseries(List<T> timeseries);

      Optional<List<T>> getTimeseries();

      ChannelSegment.Data<T> build();

      /**
       * Used to allow the deserializer to create a ChannelSegment instance with empty Data
       *
       * @return ChannelSegment with possibly empty data
       */
      default ChannelSegment.Data<T> deserializeBuild() {
        //This check is for allowing deserialization of a faceted Event to be created
        //where the data is empty. This build must return null in this case
        List<Optional<?>> allFields = List.of(getUnits(), getTimeseriesType(), getTimeseries());
        var numPresentFields = allFields.stream()
          .filter(Optional::isPresent)
          .count();
        if (numPresentFields == 0) {
          return null;
        }

        return build();
      }
    }
  }
}
