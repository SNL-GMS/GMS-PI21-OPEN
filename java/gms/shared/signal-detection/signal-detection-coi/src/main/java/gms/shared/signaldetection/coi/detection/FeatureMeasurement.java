package gms.shared.signaldetection.coi.detection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.google.auto.value.AutoValue;
import gms.shared.signaldetection.coi.types.FeatureMeasurementType;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.waveform.coi.ChannelSegment;
import gms.shared.waveform.coi.Timeseries;

import java.io.Serializable;
import java.util.Optional;

/**
 * Represents a measure of some kind of feature.
 * <p>
 * A Signal Detection Hypothesis typically will have many measurements associated with it, captured
 * with the Feature Measurement class. Feature Measurement has been made generic to accommodate any
 * new types of measurement that may be added in the future. Each Feature Measurement has a type
 * indicated with the feature measurement type attribute, a value, and a reference to the Channel
 * Segment on which it was calculated. As shown in the association above, each Signal Detection
 * Hypothesis is required to have at least an arrival time Feature Measurement. The additional
 * Feature Measurements are a "zero to many" relationship, because they are not required by the
 * system.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXISTING_PROPERTY, property =
  "featureMeasurementType", visible = true)
@JsonTypeIdResolver(FeatureMeasurementIdResolver.class)
@AutoValue
public abstract class FeatureMeasurement<V> implements Serializable {

  /**
   * The {@link Channel} on which the measurement was made
   *
   * @return {@link Channel}
   */
  public abstract Channel getChannel();

  /**
   * The {@link ChannelSegment} measured data from the Channel
   *
   * @return {@link ChannelSegment}
   */
  public abstract ChannelSegment<? extends Timeseries> getMeasuredChannelSegment();

  /**
   * Type of the measurement.  Matches up to getMeasurementValue().
   *
   * @return type
   */
  public abstract FeatureMeasurementType<V> getFeatureMeasurementType();

  /**
   * Value of the measurement.  Matches up to getFeatureMeasurementType().
   *
   * @return the value of the measurement
   */
  public abstract V getMeasurementValue();

  public abstract Optional<DoubleValue> getSnr();

  /**
   * Recreates a FeatureMeasurement with all args.
   *
   * @param channel {@link Channel}
   * @param measuredChannelSegment {@link ChannelSegment}
   * @param type feature measurement type
   * @param measurementValue feature mesaurement value
   * @return Feature measurement
   */
  @JsonCreator
  public static <V> FeatureMeasurement<V> from(
    @JsonProperty("channel") Channel channel,
    @JsonProperty("measuredChannelSegment") ChannelSegment<? extends Timeseries> measuredChannelSegment,
    @JsonProperty("featureMeasurementType") FeatureMeasurementType<V> type,
    @JsonProperty("measurementValue") V measurementValue,
    @JsonProperty("snr") Optional<DoubleValue> snr) {

    return new AutoValue_FeatureMeasurement<>(channel, measuredChannelSegment, type, measurementValue, snr);
  }
}

