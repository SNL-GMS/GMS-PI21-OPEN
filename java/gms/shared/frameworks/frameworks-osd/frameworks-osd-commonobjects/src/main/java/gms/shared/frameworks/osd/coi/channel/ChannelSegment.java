package gms.shared.frameworks.osd.coi.channel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.Range;
import gms.shared.frameworks.osd.coi.waveforms.Timeseries;
import gms.shared.frameworks.osd.coi.waveforms.util.TimeseriesUtility;
import org.apache.commons.lang3.Validate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


/**
 * Represents a segment of  data from a Channel.
 */
@AutoValue
public abstract class ChannelSegment<T extends Timeseries> implements Comparable<ChannelSegment<T>> {

  /**
   * The type of the channel segment.
   */
  public enum Type {
    ACQUIRED, RAW, DETECTION_BEAM, FK_BEAM, FK_SPECTRA, FILTER
  }

  /**
   * Creates a ChannelSegment anew.
   *
   * @param <T> The type of the data stored by the ChannelSegment
   * @param channel the processing channel the segment is from.
   * @param name Name of the ChannelSegment.
   * @param type The type of the ChannelSegment, e.g. Type.RAW.
   * @param series The the data of the ChannelSegment.
   * @return the newly constructed ChannelSegment
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static <T extends Timeseries> ChannelSegment<T> create(
    Channel channel, String name, Type type,
    Collection<T> series) {

    return ChannelSegment.from(UUID.randomUUID(), channel, name,
      type, series);
  }

  /**
   * Creates a ChannelSegment for a collection of {@link Timeseries}.
   *
   * @param <T> The type of the data stored by the ChannelSegment
   * @param id the identifier for this segment
   * @param channel the processing channel the segment is from.
   * @param name Name of the ChannelSegment.
   * @param type The type of the ChannelSegment, e.g. Type.RAW.
   * @param series The data of the ChannelSegment.
   * @return the newly constructed ChannelSegment
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static <T extends Timeseries> ChannelSegment<T> from(
    UUID id, Channel channel, String name, Type type,
    Collection<T> series) {

    Validate.notEmpty(series, "ChannelSegment requires at least one timeseries");

    Timeseries.Type timeseriesType = series.iterator().next().getType();
    Validate.notNull(timeseriesType, "Unsupported Timeseries type: "
      + series.iterator().next().getClass());

    return ChannelSegment.from(id, channel, name,
      type, timeseriesType, series);
  }

  /**
   * Creates a ChannelSegment from all params.  NOTE: This method is only here to support Jackson's
   * JSON deserialization and the passed in timeseriesType value is not used, but rather this value
   * is derived from the actual class type of the timeseries.
   *
   * @param <T> The type of the data stored by the ChannelSegment
   * @param id the identifier for this segment
   * @param channel the id of the processing channel the segment is from.
   * @param name Name of the ChannelSegment.
   * @param type The type of the ChannelSegment, e.g. ChannelSegment.Type.RAW.
   * @param timeseriesType The type of the timeseries data
   * @param series The data of the ChannelSegment.
   * @return the newly constructed ChannelSegment
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  @JsonCreator
  public static <T extends Timeseries> ChannelSegment<T> from(
    @JsonProperty("id") UUID id,
    @JsonProperty("channel") Channel channel,
    @JsonProperty("name") String name,
    @JsonProperty("type") Type type,
    @JsonProperty("timeseriesType") Timeseries.Type timeseriesType,
    @JsonProperty("timeseries") Collection<T> series) {

    Validate.notBlank(name, "ChannelSegment requires a non-blank name");
    Validate.notEmpty(series, "ChannelSegment requires at least one timeseries");

    List<T> sortedSeries = new ArrayList<>(series);
    Collections.sort(sortedSeries);
    Validate.isTrue(TimeseriesUtility.noneOverlapped(sortedSeries),
      "ChannelSegment cannot have overlapping timeseries");
    final Range<Instant> timeRange = TimeseriesUtility.computeSpan(sortedSeries);

    return new AutoValue_ChannelSegment<>(id, channel, name,
      type, timeseriesType, timeRange.lowerEndpoint(), timeRange.upperEndpoint(), sortedSeries);
  }

  /**
   * Gets the id
   *
   * @return the id
   */
  public abstract UUID getId();

  /**
   * Gets the processing channel the segment is from
   *
   * @return the channel
   */
  public abstract Channel getChannel();

  /**
   * Gets the name of this segment
   *
   * @return the name
   */
  public abstract String getName();

  /**
   * Gets the type of this segment
   *
   * @return the type of the segment
   */
  public abstract Type getType();

  /**
   * Gets the type of this segment
   *
   * @return the type of the segment
   */
  public abstract Timeseries.Type getTimeseriesType();

  /**
   * Gets the start time of the segment
   *
   * @return the start time
   */
  public abstract Instant getStartTime();

  /**
   * Gets the end time of this segment
   *
   * @return the end time
   */
  public abstract Instant getEndTime();

  /**
   * Gets the timeseries that this segment contains. The returned list is sorted and immutable.
   *
   * @return {@link List} of T, not null
   */
  public abstract List<T> getTimeseries();

  /**
   * Compares the state of this object against another.
   *
   * @param otherSegment the object to compare against
   * @return true if this object and the provided one have the same state, i.e. their values are
   * equal except for entity ID.  False otherwise.
   */
  public final boolean hasSameState(ChannelSegment otherSegment) {
    return otherSegment != null &&
      Objects.equals(this.getChannel(), otherSegment.getChannel()) &&
      Objects.equals(this.getName(), otherSegment.getName()) &&
      Objects.equals(this.getType(), otherSegment.getType()) &&
      Objects.equals(this.getTimeseriesType(), otherSegment.getTimeseriesType()) &&
      Objects.equals(this.getStartTime(), otherSegment.getStartTime()) &&
      Objects.equals(this.getEndTime(), otherSegment.getEndTime()) &&
      Objects.equals(this.getTimeseries(), otherSegment.getTimeseries());

  }

  /**
   * Compares two ChannelSegments by their start time. However, if their times are equal than 1 is
   * returned. This is done to avoid weird behavior in collections such as SortedSet.
   *
   * @param cs the segment to compare this one to
   * @return int
   */
  @Override
  public int compareTo(ChannelSegment<T> cs) {
    if (!getStartTime().equals(cs.getStartTime())) {
      return getStartTime().compareTo(cs.getStartTime());
    }
    if (!getEndTime().equals(cs.getEndTime())) {
      return getEndTime().compareTo(cs.getEndTime());
    }
    return this.equals(cs) ? 0 : 1;
  }
}
