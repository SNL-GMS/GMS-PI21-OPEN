package gms.shared.signaldetection.converter.detection;

import com.google.common.base.Preconditions;
import gms.shared.signaldetection.coi.detection.FeatureMeasurement;
import gms.shared.signaldetection.coi.types.FeatureMeasurementType;
import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.converter.measurementvalue.specs.AmplitudeMeasurementValueSpecAcceptor;
import gms.shared.signaldetection.converter.measurementvalue.specs.ArrivalTimeMeasurementValueSpecAcceptor;
import gms.shared.signaldetection.converter.measurementvalue.specs.EmergenceAngleMeasurementValueSpecAcceptor;
import gms.shared.signaldetection.converter.measurementvalue.specs.FirstMotionMeasurementValueSpecAcceptor;
import gms.shared.signaldetection.converter.measurementvalue.specs.MeasurementValueSpec;
import gms.shared.signaldetection.converter.measurementvalue.specs.MeasurementValueSpecAcceptorInterface;
import gms.shared.signaldetection.converter.measurementvalue.specs.MeasurementValueSpecVisitor;
import gms.shared.signaldetection.converter.measurementvalue.specs.PhaseTypeMeasurementValueSpecAcceptor;
import gms.shared.signaldetection.converter.measurementvalue.specs.ReceiverToSourceAzimuthMeasurementValueSpecAcceptor;
import gms.shared.signaldetection.converter.measurementvalue.specs.RectilinearityMeasurementValueSpecAcceptor;
import gms.shared.signaldetection.converter.measurementvalue.specs.SlownessMeasurementValueSpecAcceptor;
import gms.shared.signaldetection.converter.measurementvalues.AmplitudeMeasurementValueConverter;
import gms.shared.signaldetection.converter.measurementvalues.ArrivalTimeMeasurementValueConverter;
import gms.shared.signaldetection.converter.measurementvalues.EmergenceAngleMeasurementValueConverter;
import gms.shared.signaldetection.converter.measurementvalues.FirstMotionMeasurementValueConverter;
import gms.shared.signaldetection.converter.measurementvalues.MeasurementValueConverter;
import gms.shared.signaldetection.converter.measurementvalues.PhaseTypeMeasurementValueConverter;
import gms.shared.signaldetection.converter.measurementvalues.ReceiverToSourceAzimuthMeasurementValueConverter;
import gms.shared.signaldetection.converter.measurementvalues.RectilinearityMeasurementValueConverter;
import gms.shared.signaldetection.converter.measurementvalues.SlownessMeasurementValueConverter;
import gms.shared.signaldetection.dao.css.AmplitudeDao;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.dao.css.AssocDao;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.waveform.coi.ChannelSegment;
import gms.shared.waveform.coi.Timeseries;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Component
public class FeatureMeasurementConverter implements FeatureMeasurementConverterInterface {

  private static final String CONVERTER_NOT_FOUND_MESSAGE = "Converter not found for measurement type: %s";

  // initialize the maps from measurement types to value specs and signal detection converter functions
  private static final Map<FeatureMeasurementType<?>,
    Supplier<? extends MeasurementValueConverter<?>>> featureMeasurementFunctionMap = Map.of(
    FeatureMeasurementTypes.ARRIVAL_TIME, ArrivalTimeMeasurementValueConverter::create,
    FeatureMeasurementTypes.PHASE, PhaseTypeMeasurementValueConverter::create,
    FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH, ReceiverToSourceAzimuthMeasurementValueConverter::create,
    FeatureMeasurementTypes.SLOWNESS, SlownessMeasurementValueConverter::create,
    FeatureMeasurementTypes.EMERGENCE_ANGLE, EmergenceAngleMeasurementValueConverter::create,
    FeatureMeasurementTypes.RECTILINEARITY, RectilinearityMeasurementValueConverter::create,
    FeatureMeasurementTypes.SHORT_PERIOD_FIRST_MOTION, FirstMotionMeasurementValueConverter::create,
    FeatureMeasurementTypes.LONG_PERIOD_FIRST_MOTION, FirstMotionMeasurementValueConverter::create,
    FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2, AmplitudeMeasurementValueConverter::create);

  private static final Map<FeatureMeasurementType<?>,
    Supplier<? extends MeasurementValueSpecAcceptorInterface<?>>> featureMeasurementSpecMap = Map.of(
    FeatureMeasurementTypes.ARRIVAL_TIME, ArrivalTimeMeasurementValueSpecAcceptor::create,
    FeatureMeasurementTypes.PHASE, PhaseTypeMeasurementValueSpecAcceptor::create,
    FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH, ReceiverToSourceAzimuthMeasurementValueSpecAcceptor::create,
    FeatureMeasurementTypes.SLOWNESS, SlownessMeasurementValueSpecAcceptor::create,
    FeatureMeasurementTypes.EMERGENCE_ANGLE, EmergenceAngleMeasurementValueSpecAcceptor::create,
    FeatureMeasurementTypes.RECTILINEARITY, RectilinearityMeasurementValueSpecAcceptor::create,
    FeatureMeasurementTypes.SHORT_PERIOD_FIRST_MOTION, FirstMotionMeasurementValueSpecAcceptor::create,
    FeatureMeasurementTypes.LONG_PERIOD_FIRST_MOTION, FirstMotionMeasurementValueSpecAcceptor::create,
    FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2, AmplitudeMeasurementValueSpecAcceptor::create);

  @Override
  public <V> Stream<MeasurementValueSpec<V>> createMeasurementValueSpec(
    FeatureMeasurementType<V> featureMeasurementType,
    ArrivalDao arrivalDao,
    Optional<AssocDao> assocDao,
    Optional<AmplitudeDao> amplitudeDao) {

    Objects.requireNonNull(featureMeasurementType);
    Objects.requireNonNull(arrivalDao);
    Objects.requireNonNull(assocDao);
    Preconditions.checkState(featureMeasurementSpecMap.containsKey(featureMeasurementType),
      CONVERTER_NOT_FOUND_MESSAGE, featureMeasurementType);

    MeasurementValueSpecAcceptorInterface<V> converterSpecAcceptor = (MeasurementValueSpecAcceptorInterface<V>)
      featureMeasurementSpecMap.get(featureMeasurementType).get();
    return converterSpecAcceptor.accept(MeasurementValueSpecVisitor.create(),
      featureMeasurementType, arrivalDao, assocDao, amplitudeDao);
  }

  @Override
  public <V> Optional<FeatureMeasurement<V>> convert(MeasurementValueSpec<V> measurementValueSpec,
    Channel channel,
    ChannelSegment<? extends Timeseries> channelSegment,
    Optional<DoubleValue> snrOptional) {

    Objects.requireNonNull(measurementValueSpec);
    Objects.requireNonNull(channel);
    Objects.requireNonNull(channelSegment);

    FeatureMeasurementType<V> featureMeasurementType = measurementValueSpec.getFeatureMeasurementType();
    Preconditions.checkState(featureMeasurementSpecMap.containsKey(featureMeasurementType),
      CONVERTER_NOT_FOUND_MESSAGE, featureMeasurementType);

    // searches the map for the measurement value spec
    MeasurementValueConverter<V> converter = (MeasurementValueConverter<V>)
      featureMeasurementFunctionMap.get(featureMeasurementType).get();

    Optional<V> valueOpt = converter.convert(measurementValueSpec);
    return valueOpt.map(value ->
      FeatureMeasurement.from(
        channel, channelSegment, featureMeasurementType, value, snrOptional));
  }
}
