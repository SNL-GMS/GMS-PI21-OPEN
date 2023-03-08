package gms.shared.signaldetection.converter.detection;

import gms.shared.signaldetection.coi.detection.FeatureMeasurement;
import gms.shared.signaldetection.coi.types.FeatureMeasurementType;
import gms.shared.signaldetection.converter.measurementvalue.specs.MeasurementValueSpec;
import gms.shared.signaldetection.dao.css.AmplitudeDao;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.dao.css.AssocDao;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.waveform.coi.ChannelSegment;
import gms.shared.waveform.coi.Timeseries;

import java.util.Optional;
import java.util.stream.Stream;

public interface FeatureMeasurementConverterInterface {

  /**
   * Create the {@link MeasurementValueSpec} spec from {@link FeatureMeasurementType}, {@link ArrivalDao}
   * and an optional {@link AssocDao}
   *
   * @param featureMeasurementType {@link FeatureMeasurementType} for measurement value
   * @param arrivalDao {@link ArrivalDao}
   * @param assocDao optional {@link AssocDao}
   * @param amplitudeDao optional {@link AmplitudeDao}
   * @param <V> measurement value class
   * @return {@link MeasurementValueSpec}
   */
  <V> Stream<MeasurementValueSpec<V>> createMeasurementValueSpec(FeatureMeasurementType<V> featureMeasurementType,
    ArrivalDao arrivalDao, Optional<AssocDao> assocDao, Optional<AmplitudeDao> amplitudeDao);

  /**
   * Gets the associated converter for the input {@link FeatureMeasurementType} and {@link ArrivalDao}
   */
  <V> Optional<FeatureMeasurement<V>> convert(MeasurementValueSpec<V> measurementValueSpec,
    Channel channel,
    ChannelSegment<? extends Timeseries> channelSegment,
    Optional<DoubleValue> snrOptional);
}
