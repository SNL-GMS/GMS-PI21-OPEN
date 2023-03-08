package gms.shared.event.repository.converter;

import com.google.common.base.Preconditions;
import gms.shared.event.coi.EventLocation;
import gms.shared.event.coi.LocationBehavior;
import gms.shared.event.coi.LocationSolution;
import gms.shared.event.coi.featureprediction.FeaturePrediction;
import gms.shared.event.coi.featureprediction.FeaturePredictionComponent;
import gms.shared.event.coi.featureprediction.FeaturePredictionComponentType;
import gms.shared.event.coi.featureprediction.FeaturePredictionContainer;
import gms.shared.event.coi.featureprediction.type.FeaturePredictionType;
import gms.shared.event.coi.featureprediction.value.ArrivalTimeFeaturePredictionValue;
import gms.shared.event.coi.featureprediction.value.FeaturePredictionValue;
import gms.shared.event.coi.featureprediction.value.NumericFeaturePredictionValue;
import gms.shared.event.dao.ArInfoDao;
import gms.shared.event.repository.BridgedSdhInformation;
import gms.shared.signaldetection.coi.detection.FeatureMeasurement;
import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.signaldetection.coi.values.ArrivalTimeMeasurementValue;
import gms.shared.signaldetection.coi.values.DurationValue;
import gms.shared.signaldetection.coi.values.InstantValue;
import gms.shared.signaldetection.coi.values.NumericMeasurementValue;
import gms.shared.signaldetection.coi.values.PhaseTypeMeasurementValue;
import gms.shared.signaldetection.dao.css.AssocDao;
import gms.shared.signaldetection.dao.css.converter.DurationToDoubleConverter;
import gms.shared.signaldetection.dao.css.enums.DefiningFlag;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.stationdefinition.coi.utils.Units;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.util.Optionals;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gms.shared.signaldetection.dao.css.enums.DefiningFlag.isDefining;
import static gms.shared.stationdefinition.coi.channel.Channel.createVersionReference;

/**
 * Utility class the help create {@link PredictionsAndBehaviors} objects
 */
public class PredictionsAndBehaviorsConverterUtility {

  private static final DurationToDoubleConverter durationToDoubleConverter = new DurationToDoubleConverter();

  private PredictionsAndBehaviorsConverterUtility() {
    //private utility class constructor
  }

  /**
   * Returns all the {@link LocationBehavior}s and the {@link PredictionsAndBehaviors}s used in creating a
   * {@link LocationSolution}
   *
   * @param sdhInfo Bridged Signal Detection Hypothesis dao and coi information
   * @param eventLocation the eventLocation associated with the signalDetectionHypothesis
   * @return LocationBehaviors and FeaturePredictions for a LocationSolution
   */
  static PredictionsAndBehaviors fromLegacyToPredictionsAndBehaviors(BridgedSdhInformation sdhInfo,
    EventLocation eventLocation) {
    Preconditions.checkNotNull(sdhInfo, "BridgedSdhInformation cannot be null");
    Preconditions.checkNotNull(eventLocation, "EventLocation cannot be null");
    var signalDetectionHypothesis = sdhInfo.getSignalDetectionHypothesis();
    Preconditions.checkArgument(signalDetectionHypothesis.isPresent(), "Cannot create PredictionsAndBehaviors from empty SignalDetectionHypothesis");

    var sdhData = signalDetectionHypothesis
      .get()
      .getData()
      .orElseThrow(() -> new IllegalArgumentException("SignalDetectionHypothesis must contain data!"));
    var phase = sdhData
      .getFeatureMeasurement(FeatureMeasurementTypes.PHASE)
      .map(FeatureMeasurement::getMeasurementValue)
      .map(PhaseTypeMeasurementValue::getValue)
      .orElseThrow(() -> new IllegalArgumentException("SignalDetectionHypothesis must contain a PHASE type FeatureMeasurementType"));
    var arInfoDaoOpt = sdhInfo.getArInfoDao();
    var assocDao = sdhInfo.getAssocDao();
    var featurePredictionResolver = new FeaturePredictionResolver(eventLocation, phase);
    var locationBehaviors = new HashSet<LocationBehavior>();

    var arrivalTimeMeasurement = sdhData.getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME);
    var arrivalTimePrediction = arInfoDaoOpt.flatMap(arInfoDao ->
      arrivalTimeMeasurement
        .map(measurement ->
          featurePredictionResolver.resolveFeaturePrediction(
            buildArrivalTimePredictedValue(
              arInfoDao,
              eventLocation,
              durationToDoubleConverter.convertToEntityAttribute(arInfoDao.getTotalTravelTime()),
              buildArrivalTimePredictionComponents(arInfoDao)), FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE,
            measurement)));
    // if arrivalTimeMeasurement is empty, arrivalTimeBehavior is empty
    var arrivalTimeBehavior = arrivalTimeMeasurement.flatMap(measurement ->
      // if arrivalTimePrediction is present (meaning arInfoDao is also present)
      arrivalTimePrediction.map(prediction -> {
        var weight = arInfoDaoOpt.get().getTravelTimeImport() == -1.0 || !isDefining(assocDao.getTimeDefining()) ?
          Optional.<Double>empty() :
          Optional.of(arInfoDaoOpt.get().getTravelTimeImport());
        return LocationBehavior.from(
          Optional.of(assocDao.getTimeResidual()),
          weight,
          isDefining(assocDao.getTimeDefining()),
          Optional.of(prediction),
          removeChannelInfo(measurement)
        );
      }).or(() -> Optional.of(
        // if arrivalTimePrediction is not present (meaning arInfoDao is not present)
        LocationBehavior.from(
          Optional.empty(),
          Optional.empty(),
          assocDao.getTimeDefining().equals(DefiningFlag.DEFAULT_DEFINING),
          Optional.empty(),
          removeChannelInfo(measurement)
        )
      ))
    );
    arrivalTimeBehavior.ifPresent(locationBehaviors::add);

    var emergenceAngleMeasurement = sdhData.getFeatureMeasurement(FeatureMeasurementTypes.EMERGENCE_ANGLE);
    var emergenceAnglePrediction = emergenceAngleMeasurement
      .map(measurement -> featurePredictionResolver.resolveFeaturePrediction(
        buildEmergenceAnglePredictedValue(assocDao, measurement),
        FeaturePredictionType.EMERGENCE_ANGLE_PREDICTION_TYPE, measurement));
    var emergenceAngleBehavior = Optionals.mapIfAllPresent(emergenceAngleMeasurement, emergenceAnglePrediction,
      (measurement, prediction) -> LocationBehavior.from(
        Optional.of(assocDao.getEmergenceAngleResidual()),
        Optional.empty(),
        false,
        Optional.of(prediction),
        removeChannelInfo(measurement)));
    emergenceAngleBehavior.ifPresent(locationBehaviors::add);

    var rToSAzimuthMeasurement = sdhData.getFeatureMeasurement(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH);
    var rToSAzimuthPrediction = arInfoDaoOpt.flatMap(arInfoDao ->
      rToSAzimuthMeasurement
        .map(measurement -> featurePredictionResolver.resolveFeaturePrediction(
          buildReceiverToSourceAzimuthPrediction(assocDao, arInfoDao), FeaturePredictionType.RECEIVER_TO_SOURCE_AZIMUTH_PREDICTION_TYPE,
          measurement)));
    var rToSAzimuthBehavior = rToSAzimuthMeasurement.flatMap(measurement ->
      rToSAzimuthPrediction.map(prediction -> {
        var weight = arInfoDaoOpt.get().getAzimuthImport() == -1.0 || !isDefining(assocDao.getAzimuthDefining()) ?
          Optional.<Double>empty() :
          Optional.of(arInfoDaoOpt.get().getAzimuthImport());
        return LocationBehavior.from(
          Optional.of(assocDao.getAzimuthResidual()),
          weight,
          assocDao.getAzimuthDefining().equals(DefiningFlag.DEFAULT_DEFINING),
          Optional.of(prediction),
          removeChannelInfo(measurement)
        );
      }).or(() -> Optional.of(
        LocationBehavior.from(
          Optional.empty(),
          Optional.empty(),
          assocDao.getAzimuthDefining().equals(DefiningFlag.DEFAULT_DEFINING),
          Optional.empty(),
          removeChannelInfo(measurement)
        )
      ))
    );
    rToSAzimuthBehavior.ifPresent(locationBehaviors::add);

    var slownessMeasurement = sdhData.getFeatureMeasurement(FeatureMeasurementTypes.SLOWNESS);
    var slownessPrediction = arInfoDaoOpt.flatMap(arInfoDao ->
      slownessMeasurement
        .map(measurement -> featurePredictionResolver.resolveFeaturePrediction(
          buildSlownessPrediction(assocDao, arInfoDao, measurement), FeaturePredictionType.SLOWNESS_PREDICTION_TYPE,
          measurement)));
    var slownessBehavior = slownessMeasurement.flatMap(measurement ->
      slownessPrediction.map(prediction -> {
        var weight = arInfoDaoOpt.get().getSlownessImport() == -1.0 || !isDefining(assocDao.getSlownessDefining()) ?
          Optional.<Double>empty() :
          Optional.of(arInfoDaoOpt.get().getSlownessImport());
        return LocationBehavior.from(
          Optional.of(assocDao.getSlownessResidual()),
          weight,
          assocDao.getSlownessDefining().equals(DefiningFlag.DEFAULT_DEFINING),
          Optional.of(prediction),
          removeChannelInfo(measurement)
        );
      }).or(() -> Optional.of(
        LocationBehavior.from(
          Optional.empty(),
          Optional.empty(),
          assocDao.getSlownessDefining().equals(DefiningFlag.DEFAULT_DEFINING),
          Optional.empty(),
          removeChannelInfo(measurement)
        )
      )));
    slownessBehavior.ifPresent(locationBehaviors::add);

    //these two below are only for the LocationSolutions featurePredictions
    var sToRAzimuthMeasurement = sdhData.getFeatureMeasurement(FeatureMeasurementTypes.SOURCE_TO_RECEIVER_AZIMUTH);
    var sToRAzimuthPrediction = sToRAzimuthMeasurement
      .map(measurement -> featurePredictionResolver.resolveFeaturePrediction(
        buildSourceToReceiverAzimuthPrediction(assocDao), FeaturePredictionType.SOURCE_TO_RECEIVER_AZIMUTH_PREDICTION_TYPE,
        measurement
      ));

    var sToRDistanceMeasurement = sdhData.getFeatureMeasurement(FeatureMeasurementTypes.SOURCE_TO_RECEIVER_DISTANCE);
    var sToRDistancePrediction = sToRDistanceMeasurement
      .map(measurement -> featurePredictionResolver.resolveFeaturePrediction(
        buildSourceToReceiverDistancePrediction(assocDao), FeaturePredictionType.SOURCE_TO_RECEIVER_DISTANCE_PREDICTION_TYPE,
        measurement
      ));

    var predictions =
      Optionals.toStream(sToRDistancePrediction,
          sToRAzimuthPrediction,
          slownessPrediction,
          rToSAzimuthPrediction,
          emergenceAnglePrediction,
          arrivalTimePrediction)
        .collect(Collectors.toList());

    var featurePredictionContainer = FeaturePredictionContainer.of(predictions.toArray(
      new FeaturePrediction<?>[]{}));
    return PredictionsAndBehaviors.create(featurePredictionContainer, locationBehaviors);
  }

  private static Set<FeaturePredictionComponent<DoubleValue>> buildSourceToReceiverDistancePredictionComponents(
    NumericMeasurementValue numericMeasurementValue) {
    var featureComponent = FeaturePredictionComponent.from(
      DoubleValue.from(numericMeasurementValue.getMeasuredValue().getValue(),
        Optional.empty(), Units.DEGREES),
      false,
      FeaturePredictionComponentType.BASELINE_PREDICTION
    );
    return Set.of(featureComponent);
  }

  private static NumericFeaturePredictionValue buildSourceToReceiverDistancePrediction(AssocDao assocDao) {
    var numericMeasurementValue = NumericMeasurementValue
      .from(Optional.empty(),
        DoubleValue.from(assocDao.getDelta(),
          Optional.empty(),
          Units.DEGREES));

    return NumericFeaturePredictionValue.from(
      FeatureMeasurementTypes.SOURCE_TO_RECEIVER_DISTANCE,
      numericMeasurementValue,
      Map.of(),
      PredictionsAndBehaviorsConverterUtility.buildSourceToReceiverDistancePredictionComponents(numericMeasurementValue));
  }

  private static Set<FeaturePredictionComponent<DoubleValue>> buildSourceToReceiverAzimuthPredictionComponents(
    NumericMeasurementValue numericMeasurementValue) {
    var predictionComponent = FeaturePredictionComponent.from(
      DoubleValue.from(numericMeasurementValue.getMeasuredValue().getValue(),
        Optional.empty(), Units.DEGREES),
      false,
      FeaturePredictionComponentType.BASELINE_PREDICTION
    );
    return Set.of(predictionComponent);
  }

  private static NumericFeaturePredictionValue buildSourceToReceiverAzimuthPrediction(AssocDao assocDao) {
    var numericMeasurementValue = NumericMeasurementValue
      .from(Optional.empty(),
        DoubleValue.from(assocDao.getEventToStationAzimuth(),
          Optional.empty(),
          Units.DEGREES));

    return NumericFeaturePredictionValue.from(
      FeatureMeasurementTypes.SOURCE_TO_RECEIVER_AZIMUTH,
      numericMeasurementValue,
      Map.of(),
      PredictionsAndBehaviorsConverterUtility.buildSourceToReceiverAzimuthPredictionComponents(numericMeasurementValue));
  }

  private static Set<FeaturePredictionComponent<DoubleValue>> buildSlownessPredictionComponents(ArInfoDao arInfoDao,
    NumericMeasurementValue measurementValue) {
    var measuredValue = measurementValue.getMeasuredValue().getValue();
    return Map.of(
        FeaturePredictionComponentType.BASELINE_PREDICTION, measuredValue - arInfoDao.getSlownessSourceSpecificCorrection(),
        FeaturePredictionComponentType.SOURCE_DEPENDENT_CORRECTION, arInfoDao.getSlownessSourceSpecificCorrection())
      .entrySet()
      .stream()
      .map((entry ->
        FeaturePredictionComponent.from(
          DoubleValue.from(entry.getValue(),
            Optional.empty(), Units.SECONDS_PER_DEGREE),
          false,
          entry.getKey()))).collect(Collectors.toSet());
  }

  private static NumericFeaturePredictionValue buildSlownessPrediction(AssocDao assocDao, ArInfoDao arInfoDao,
    FeatureMeasurement<NumericMeasurementValue> measurementValue) {
    var measuredValue = measurementValue.getMeasurementValue().getMeasuredValue().getValue();
    var numericMeasurementValue = NumericMeasurementValue
      .from(Optional.empty(),
        DoubleValue.from(measuredValue - assocDao.getSlownessResidual(),
          Optional.of(arInfoDao.getSlownessModelError()),
          Units.SECONDS_PER_DEGREE));

    return NumericFeaturePredictionValue.from(
      FeatureMeasurementTypes.SLOWNESS,
      numericMeasurementValue,
      Map.of(),
      buildSlownessPredictionComponents(arInfoDao, numericMeasurementValue));
  }

  private static Set<FeaturePredictionComponent<DoubleValue>> buildReceiverToSourceAzimuthPredictionComponents(
    ArInfoDao arInfoDao,
    NumericMeasurementValue measurementValue) {
    var measuredValue = measurementValue.getMeasuredValue().getValue();
    return Stream.of(
        Pair.of(FeaturePredictionComponentType.BASELINE_PREDICTION, measuredValue - arInfoDao.getAzimuthSourceSpecificCorrection()),
        Pair.of(FeaturePredictionComponentType.SOURCE_DEPENDENT_CORRECTION, arInfoDao.getAzimuthSourceSpecificCorrection()))
      .map((pair -> FeaturePredictionComponent.from(
        DoubleValue.from(pair.getValue(), Optional.empty(), Units.DEGREES),
        false,
        pair.getKey())))
      .collect(Collectors.toSet());
  }

  private static NumericFeaturePredictionValue buildReceiverToSourceAzimuthPrediction(AssocDao assocDao,
    ArInfoDao arInfoDao) {
    var numericMeasurementValue = NumericMeasurementValue
      .from(Optional.empty(),
        DoubleValue.from(assocDao.getStationToEventAzimuth(),
          Optional.of(arInfoDao.getAzimuthModelError()),
          Units.DEGREES));

    return NumericFeaturePredictionValue.from(
      FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH,
      numericMeasurementValue,
      Map.of(),
      buildReceiverToSourceAzimuthPredictionComponents(arInfoDao, numericMeasurementValue));
  }

  private static NumericFeaturePredictionValue buildEmergenceAnglePredictedValue(AssocDao assocDao,
    FeatureMeasurement<NumericMeasurementValue> measurementValue) {
    var measuredValue = measurementValue.getMeasurementValue().getMeasuredValue().getValue();

    var numericMeasurementValue = NumericMeasurementValue
      .from(Optional.empty(),
        DoubleValue.from(measuredValue - assocDao.getEmergenceAngleResidual(),
          Optional.empty(),
          Units.DEGREES));

    return NumericFeaturePredictionValue.from(
      FeatureMeasurementTypes.EMERGENCE_ANGLE,
      numericMeasurementValue,
      Map.of(),
      PredictionsAndBehaviorsConverterUtility.buildEmergenceAnglePredictionComponents(numericMeasurementValue));
  }

  private static Set<FeaturePredictionComponent<DoubleValue>> buildEmergenceAnglePredictionComponents(
    NumericMeasurementValue value) {
    var component = FeaturePredictionComponent.from(
      DoubleValue.from(value.getMeasuredValue().getValue(), Optional.empty(), Units.DEGREES),
      false, FeaturePredictionComponentType.BASELINE_PREDICTION);

    return Set.of(component);
  }

  private static ArrivalTimeFeaturePredictionValue buildArrivalTimePredictedValue(ArInfoDao arInfoDao,
    EventLocation eventLocation,
    Duration totalTravelTimeDuration,
    Set<FeaturePredictionComponent<DurationValue>> featurePredictionComponents) {

    var arrivalTimeMeasurementValue = ArrivalTimeMeasurementValue.from(
      InstantValue.from(eventLocation.getTime().plus(totalTravelTimeDuration.toSeconds(), ChronoUnit.SECONDS), null),
      Optional.of(DurationValue.from(totalTravelTimeDuration, durationToDoubleConverter.convertToEntityAttribute(arInfoDao.getTravelTimeModelError()))));

    return ArrivalTimeFeaturePredictionValue.from(
      FeatureMeasurementTypes.ARRIVAL_TIME,
      arrivalTimeMeasurementValue,
      Map.of(),
      featurePredictionComponents
    );

  }

  private static Set<FeaturePredictionComponent<DurationValue>> buildArrivalTimePredictionComponents(
    ArInfoDao arInfoDao) {
    return Map.of(
        FeaturePredictionComponentType.BASELINE_PREDICTION, arInfoDao.getBaseModelTravelTime(),
        FeaturePredictionComponentType.BULK_STATIC_STATION_CORRECTION, arInfoDao.getTravelTimeStaticCorrection(),
        FeaturePredictionComponentType.ELLIPTICITY_CORRECTION, arInfoDao.getTravelTimeEllipticityCorrection(),
        FeaturePredictionComponentType.ELEVATION_CORRECTION, arInfoDao.getTravelTimeElevationCorrection(),
        FeaturePredictionComponentType.SOURCE_DEPENDENT_CORRECTION, arInfoDao.getTravelTimeSourceSpecificCorrection())
      .entrySet()
      .stream()
      .map(entry ->
        FeaturePredictionComponent.from(
          DurationValue.from(durationToDoubleConverter.convertToEntityAttribute(entry.getValue()), null),
          false,
          entry.getKey())
      ).collect(Collectors.toSet());
  }

  private static <V> FeatureMeasurement<V> removeChannelInfo(FeatureMeasurement<V> featureMeasurement) {
    var channel = featureMeasurement.getChannel();
    return FeatureMeasurement.from(
      Channel.createVersionReference(channel.getName(), channel.getEffectiveAt().orElseThrow()),
      featureMeasurement.getMeasuredChannelSegment(),
      featureMeasurement.getFeatureMeasurementType(),
      featureMeasurement.getMeasurementValue(),
      featureMeasurement.getSnr()
    );
  }

  private static class FeaturePredictionResolver {

    private final EventLocation eventLocation;
    private final PhaseType phaseType;

    FeaturePredictionResolver(EventLocation eventLocation, PhaseType phase) {

      this.eventLocation = eventLocation;
      this.phaseType = phase;
    }

    /**
     * @param featurePredictionValue The FeaturePredictionValue
     * @param featurePredictionType The {@link FeaturePredictionType}
     * @param featureMeasurement The {@link FeatureMeasurement}
     * @param <T> Type of the feature measurement value
     * @param <U> The type of the derivatives
     * @param <V> The type of the feature prediction component
     * @param <W> The {@link extends FeaturePredictionValue}
     * @return a newly created {@link FeaturePrediction}
     */
    public <T, U, V, W extends FeaturePredictionValue<T, U, V>> FeaturePrediction<W> resolveFeaturePrediction(
      W featurePredictionValue,
      FeaturePredictionType<W> featurePredictionType,
      FeatureMeasurement<T> featureMeasurement
    ) {
      var channel = featureMeasurement.getChannel();
      return FeaturePrediction.<W>builder()
        .setPredictionValue(
          featurePredictionValue)
        .setChannel(Optional.of(createVersionReference(channel.getName(), channel.getEffectiveAt().orElseThrow())))
        .setSourceLocation(eventLocation)
        .setPhase(phaseType)
        .setPredictionType(featurePredictionType)
        .setPredictionChannelSegment(Optional.empty())
        .setReceiverLocation(channel.getLocation())
        .setExtrapolated(false)
        .build();
    }
  }
}
