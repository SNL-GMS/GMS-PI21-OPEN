package gms.shared.frameworks.osd.coi.waveforms;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Triple;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

/**
 * Data class that represents a Waveform which is more generally known as a timeseries.
 */
@AutoValue
public abstract class Waveform extends Timeseries {

  /**
   * Creates a waveform by providing all arguments, except endTime which is computed in the base
   * class.
   */
  @JsonCreator
  public static Waveform from(@JsonProperty("startTime") Instant startTime,
    @JsonProperty("sampleRate") double sampleRate,
    @JsonProperty("values") double[] values) {
    Preconditions.checkArgument(sampleRate > 0.0,
      "Cannot create Waveform with negative sample rate");
    Objects.requireNonNull(values, "Cannot create waveform with null values");
    Preconditions.checkArgument(values.length > 0,
      "Cannot create waveform with empty values array");
    return new AutoValue_Waveform(Type.WAVEFORM, startTime, sampleRate, values);
  }

  /**
   * The time at which the Waveform begins.
   */
  @Override
  public abstract Instant getStartTime();

  /**
   * The sample rate (a measurement of how many data points there are per unit * time)
   */
  @Override
  public abstract double getSampleRate();

  /**
   * The number of samples in this waveform.
   */
  @Override
  public int getSampleCount() {
    return getValues().length;
  }

  /**
   * The data points of this Waveform.
   */
  public abstract double[] getValues();

  /**
   * Gets the first value
   *
   * @return double
   */
  public double getFirstSample() {
    return getValues()[0];
  }

  /**
   * Gets the last value
   *
   * @return double
   */
  public double getLastSample() {
    return getValues()[getSampleCount() - 1];
  }

  /**
   * Trims out data from this waveform and removes a new one that only contains the data within the
   * specified time bounds.
   *
   * @param start the start of the time bound
   * @param end the end of the time bound
   * @return A new waveform with data only within the given time range
   * @throws NullPointerException if start or end are null
   * @throws IllegalArgumentException if start is not before end, or if the requested time range is
   * completely outside the range of data this waveform has.
   */
  public Waveform trim(Instant start, Instant end) {
    Validate.notNull(start);
    Validate.notNull(end);
    Validate.isTrue(this.intersects(start, end),
      String.format("Cannot trim waveform to time range it doesn't have; current range is "
          + "[%s, %s], requested is [%s, %s]", this.getStartTime(), this.getEndTime(),
        start, end));
    if (getStartTime().equals(start) && getEndTime().equals(end)) {
      return this;  // waveform is already exactly trimmed
    }
    Instant newStart = getStartTime().isAfter(start) ? getStartTime() : start;
    Instant newEnd = getEndTime().isBefore(end) ? getEndTime() : end;
    return window(newStart, newEnd);
  }

  /**
   * Windows this waveform to be within the specified time bounds.
   *
   * @param start the start of the time bound
   * @param end the end of the time bound
   * @return A few possibilities: 1.) A new Waveform that contains the narrowed (windowed) set of
   * data from this Waveform. It will have updated start/end times and sample count.  Note that the
   * start/end times of the new Waveform may not equal the requested start/end times, as they
   * reflect where data actually begins and ends. 2.) This exact Waveform if this ones' start/end
   * times are equal to requested range. 3.) A new Waveform with empty points (sampleRate=0,
   * sampleCount=0, values=[]) if the requested range is completely outside the range of this
   * Waveform.
   * @throws NullPointerException if start or end are null
   * @throws IllegalArgumentException if start is not before end.
   */
  public Waveform window(Instant start, Instant end) {
    Validate.notNull(start);
    Validate.notNull(end);
    Validate.isTrue(!start.isBefore(this.getStartTime()),
      "new start is before startTime of this waveform");
    Validate.isTrue(!end.isAfter(this.getEndTime()),
      "new end is after endTime of this waveform");
    Validate.isTrue(!start.isAfter(end), "new start must be <= new end");

    // Already exactly windowed?  Great, just return this!
    if (this.getStartTime().equals(start) && this.getEndTime().equals(end)) {
      return this;
    }

    final Triple<Integer, Integer, Integer> newIndicesAndSampleCount = this
      .computeIndicesAndSampleCount(start, end);
    final Instant newStart = computeSampleTime(newIndicesAndSampleCount.getLeft());

    // copy values into new array; adding one to upper index because Arrays.copyOfRange is end-exclusive
    // but the upper index is to be included.
    final double[] newValues = Arrays.copyOfRange(this.getValues(),
      newIndicesAndSampleCount.getLeft(), newIndicesAndSampleCount.getMiddle() + 1);
    return Waveform.from(newStart, getSampleRate(), newValues);
  }

  /**
   * Computes the new lower/upper indices (zero-indexed) of the Waveform and new sample count given
   * a new start and end time range.
   *
   * @param newStart the new start time
   * @param newEnd the new end time
   * @return a 3-tuple of (lowerIndex, upperIndex, sampleCount).  lowerIndex/upperIndex are
   * zero-indexed (array indices), sampleCount is one-indexed.
   */
  private Triple<Integer, Integer, Integer> computeIndicesAndSampleCount(Instant newStart,
    Instant newEnd) {
    Validate.isTrue(!newStart.isBefore(this.getStartTime()),
      "newStart is before startTime");
    Validate.isTrue(!newEnd.isAfter(this.getEndTime()),
      "newEnd is after endTime");
    Validate.isTrue(!newStart.isAfter(newEnd), "newStart must be <= newEnd");

    final double sampleRateNanos = getSampleRate() / 1.0e9;
    final var fromStartToNewStart = Duration.between(this.getStartTime(), newStart);
    final var fromNewEndToEnd = Duration.between(newEnd, this.getEndTime());
    final int lowerIndex = (int) Math.ceil(fromStartToNewStart.toNanos() * sampleRateNanos);
    final int samplesRemovedRight = (int) Math.ceil(fromNewEndToEnd.toNanos() * sampleRateNanos);
    final int upperIndex = getSampleCount() - 1 - samplesRemovedRight;
    Validate.isTrue(lowerIndex >= 0 && lowerIndex < getSampleCount(),
      String.format("Lower index must in range[0, %d) but was %d",
        getSampleCount(), lowerIndex));
    Validate.isTrue(upperIndex >= 0 && upperIndex < getSampleCount(),
      String.format("Upper index must be in range [0, %d) but was %d",
        getSampleCount(), upperIndex));
    Validate.isTrue(lowerIndex <= upperIndex,
      "lower index must be less than upper index");
    final int newSampleCount = getSampleCount() - (lowerIndex + samplesRemovedRight);
    return Triple.of(lowerIndex, upperIndex, newSampleCount);
  }

  private boolean intersects(Instant start, Instant end) {
    return (getEndTime().equals(start) || getEndTime().isAfter(start)) &&
      (getStartTime().equals(end) || getStartTime().isBefore(end));
  }

}
