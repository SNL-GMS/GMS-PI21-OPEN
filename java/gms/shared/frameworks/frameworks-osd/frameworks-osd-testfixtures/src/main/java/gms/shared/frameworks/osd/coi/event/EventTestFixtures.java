package gms.shared.frameworks.osd.coi.event;

import gms.shared.frameworks.osd.coi.DoubleValue;
import gms.shared.frameworks.osd.coi.InstantValue;
import gms.shared.frameworks.osd.coi.PhaseType;
import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.signaldetection.AmplitudeMeasurementValue;
import gms.shared.frameworks.osd.coi.signaldetection.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.frameworks.osd.coi.signaldetection.FeatureMeasurement;
import gms.shared.frameworks.osd.coi.signaldetection.FeatureMeasurementTypes;
import gms.shared.frameworks.osd.coi.signaldetection.Location;
import gms.shared.frameworks.osd.coi.signaldetection.NumericMeasurementValue;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static gms.shared.frameworks.osd.coi.signaldetection.SignalDetectionTestFixtures.SIGNAL_DETECTION_HYPOTHESIS;

/**
 * @deprecated As of PI 17.5, these test fixtures are actively being migrated into the event-coi package.
 * All usage of this COI outside the Frameworks area should be avoided and the alternative in event-coi used instead,
 * with unmigrated constants migrated to the new test fixture area as needed.
 */
@Deprecated(since = "17.5", forRemoval = true)
public class EventTestFixtures {

  public static final double LAT = 23.9;
  public static final double LON = -89.0;
  public static final double DEPTH = 0.06;
  public static final Instant TIME = Instant.EPOCH;
  private static final Duration PERIOD = Duration.ofSeconds(2);
  private static final double ZERO_DEPTH = 0.0;

  public static final double RESIDUAL = 2.1;
  public static final double WEIGHT = 0.87;
  public static final boolean IS_DEFINING = false;

  public static final InstantValue ARRIVAL_TIME_MEASUREMENT = InstantValue.from(
    TIME, Duration.ofMillis(1));
  private static final PhaseTypeMeasurementValue PHASE_MEASUREMENT = PhaseTypeMeasurementValue.from(
    PhaseType.P, 0.5);
  private static final AmplitudeMeasurementValue AMPLITUDE_MEASUREMENT =
    AmplitudeMeasurementValue.from(
      TIME,
      PERIOD,
      DoubleValue.from(1.0, Double.NaN, Units.NANOMETERS));

  public static final FeatureMeasurement<InstantValue> ARRIVAL_TIME_FEATURE_MEASUREMENT
    = FeatureMeasurement
    .from(UtilsTestFixtures.CHANNEL, UtilsTestFixtures.DESCRIPTOR,
      FeatureMeasurementTypes.ARRIVAL_TIME, ARRIVAL_TIME_MEASUREMENT);
  public static final FeatureMeasurement<PhaseTypeMeasurementValue> PHASE_FEATURE_MEASUREMENT
    = FeatureMeasurement
    .from(UtilsTestFixtures.CHANNEL, UtilsTestFixtures.DESCRIPTOR,
      FeatureMeasurementTypes.PHASE, PHASE_MEASUREMENT);
  public static final FeatureMeasurement<AmplitudeMeasurementValue> AMPLITUDE_FEATURE_MEASUREMENT
    = FeatureMeasurement.from(UtilsTestFixtures.CHANNEL,
    UtilsTestFixtures.DESCRIPTOR,
    FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2,
    AMPLITUDE_MEASUREMENT);

  public static final int ARRAY_LEN = 1;

  //Create a LocationUncertainty with dummy values.
  public static final double XX = 0.0;
  public static final double XY = 0.0;
  public static final double XZ = 0.0;
  public static final double XT = 0.0;
  public static final double YY = 0.0;
  public static final double YZ = 0.0;
  public static final double YT = 0.0;
  public static final double ZZ = 0.0;
  public static final double ZT = 0.0;
  public static final double TT = 0.0;
  public static final double ST_DEV_ONE_OBSERVATION = 0.0;

  //Create an Ellipse.
  public static final ScalingFactorType scalingFactorType = ScalingFactorType.CONFIDENCE;
  public static final ScalingFactorType scalingFactorType2 = ScalingFactorType.COVERAGE;
  public static final double K_WEIGHT = 0.0;
  public static final double CONFIDENCE_LEVEL = 0.5;
  public static final double MAJOR_AXIS_LENGTH = 0.0;
  public static final double MAJOR_AXIS_TREND = 0.0;
  public static final double MAJOR_AXIS_PLUNGE = 0.0;
  public static final double INTERMEDIATE_AXIS_LENGTH = 0.0;
  public static final double INTERMEDIATE_AXIS_TREND = 0.0;
  public static final double INTERMEDIATE_AXIS_PLUNGE = 0.0;
  public static final double MINOR_AXIS_LENGTH = 0.0;
  public static final double MINOR_AXIS_TREND = 0.0;
  public static final double MINOR_AXIS_PLUNGE = 0.0;
  public static final double DEPTH_UNCERTAINTY = 0.0;
  public static final Duration timeUncertainty = Duration.ofSeconds(5);

  public static final Ellipse ellipse = Ellipse
    .from(scalingFactorType, K_WEIGHT, CONFIDENCE_LEVEL, MAJOR_AXIS_LENGTH, MAJOR_AXIS_TREND,
      MINOR_AXIS_LENGTH, MINOR_AXIS_TREND, DEPTH_UNCERTAINTY, timeUncertainty);
  public static final Ellipsoid ellipsoid = Ellipsoid
    .from(scalingFactorType, K_WEIGHT, CONFIDENCE_LEVEL,
      MAJOR_AXIS_LENGTH, MAJOR_AXIS_TREND, MAJOR_AXIS_PLUNGE,
      INTERMEDIATE_AXIS_LENGTH, INTERMEDIATE_AXIS_TREND,
      INTERMEDIATE_AXIS_PLUNGE, MINOR_AXIS_LENGTH, INTERMEDIATE_AXIS_TREND,
      INTERMEDIATE_AXIS_PLUNGE, timeUncertainty);
  protected static final Set<Ellipse> ellipseSet = Set.of(ellipse);
  protected static final Set<Ellipsoid> ellipsoidSet = Set.of(ellipsoid);

  public static final LocationUncertainty LOCATION_UNCERTAINTY = LocationUncertainty
    .from(XX, XY, XZ, XT, YY, YZ, YT, ZZ, ZT, TT, ST_DEV_ONE_OBSERVATION,
      ellipseSet, ellipsoidSet);

  public static final LocationRestraint LOCATION_RESTRAINT = LocationRestraint.from(
    RestraintType.FIXED,
    LAT,
    RestraintType.FIXED,
    LON,
    DepthRestraintType.FIXED_AT_SURFACE,
    ZERO_DEPTH,
    RestraintType.FIXED,
    TIME
  );

  public static final EventLocation EVENT_LOCATION = EventLocation.from(LAT, LON, DEPTH, TIME);
  public static final Location LOCATION = Location.from(1.0, 2.0, 4.0, 3.0);
  public static final FeaturePrediction<NumericMeasurementValue> FEATURE_PREDICTION =
    FeaturePrediction.<NumericMeasurementValue>builder()
      .setPhase(PhaseType.P)
      .setPredictedValue(NumericMeasurementValue.from(
        Instant.EPOCH,
        DoubleValue.from(1.0, 2.0, Units.SECONDS)))
      .setFeaturePredictionComponents(Set.of(FeaturePredictionComponent.from(DoubleValue.from(3.0, 4.0, Units.SECONDS),
        false,
        FeaturePredictionCorrectionType.BASELINE_PREDICTION)))
      .setExtrapolated(false)
      .setPredictionType(FeatureMeasurementTypes.SLOWNESS)
      .setSourceLocation(EVENT_LOCATION)
      .setReceiverLocation(LOCATION)
      .setChannelName(UtilsTestFixtures.CHANNEL.getName())
      .setFeaturePredictionDerivativeMap(Map.of())
      .build();

  protected static final Set<FeaturePrediction<?>> FEATURE_PREDICTIONS = Set.of(FEATURE_PREDICTION);

  public static final LocationBehavior LOCATION_BEHAVIOR = LocationBehavior
    .from(RESIDUAL, WEIGHT, IS_DEFINING, FEATURE_PREDICTION, ARRIVAL_TIME_FEATURE_MEASUREMENT);

  protected static final Set<LocationBehavior> LOCATION_BEHAVIORS = Set.of(LOCATION_BEHAVIOR);

  public static final StationMagnitudeSolution STATION_MAGNITUDE_SOLUTION = StationMagnitudeSolution
    .builder()
    .setType(MagnitudeType.MB)
    .setModel(MagnitudeModel.VEITH_CLAWSON)
    .setStationName(UtilsTestFixtures.STATION.getName())
    .setPhase(PhaseType.P)
    .setMagnitudeUncertainty(1.0)
    .setMagnitude(0.1)
    .setModelCorrection(0.01)
    .setStationCorrection(0.02)
    .setMeasurement(AMPLITUDE_FEATURE_MEASUREMENT)
    .build();

  public static final NetworkMagnitudeBehavior NETWORK_MAGNITUDE_BEHAVIOR = NetworkMagnitudeBehavior
    .builder()
    .setDefining(true)
    .setResidual(1.0)
    .setWeight(0.3)
    .setStationMagnitudeSolution(STATION_MAGNITUDE_SOLUTION)
    .build();

  public static final NetworkMagnitudeSolution NETWORK_MAGNITUDE_SOLUTION = NetworkMagnitudeSolution
    .builder()
    .setNetworkMagnitudeBehaviors(List.of(NETWORK_MAGNITUDE_BEHAVIOR))
    .setMagnitude(5.0)
    .setUncertainty(1.0)
    .setMagnitudeType(MagnitudeType.MB)
    .build();

  protected static final List<NetworkMagnitudeSolution> NETWORK_MAGNITUDE_SOLUTIONS = List
    .of(NETWORK_MAGNITUDE_SOLUTION);

  // Create a LocationSolution
  public static final LocationSolution LOCATION_SOLUTION = LocationSolution.builder()
    .generateId()
    .setLocation(EVENT_LOCATION)
    .setLocationRestraint(LOCATION_RESTRAINT)
    .setLocationUncertainty(Optional.of(LOCATION_UNCERTAINTY))
    .setLocationBehaviors(LOCATION_BEHAVIORS)
    .setFeaturePredictions(FEATURE_PREDICTIONS)
    .setNetworkMagnitudeSolutions(NETWORK_MAGNITUDE_SOLUTIONS)
    .build();

  private static final UUID EVENT_ID = UUID.randomUUID();

  // ------- SignalDetectionEventAssociation -------

  private static final UUID signalDetectionEventAssociationId = UUID.randomUUID();
  public static final UUID EVENT_HYPOTHESIS_ID = UUID.randomUUID();
  private static final boolean IS_REJECTED = false;

  public static final SignalDetectionEventAssociation SIGNAL_DETECTION_EVENT_ASSOCIATION =
    SignalDetectionEventAssociation
      .from(signalDetectionEventAssociationId, EVENT_HYPOTHESIS_ID,
        SIGNAL_DETECTION_HYPOTHESIS.getId(),
        IS_REJECTED);

  public static final EventHypothesis EVENT_HYPOTHESIS = EventHypothesis.builder()
    .setId(EVENT_HYPOTHESIS_ID)
    .setEventId(EVENT_ID)
    .setParentEventHypotheses(List.of(UUID.randomUUID()))
    .setRejected(false)
    .setLocationSolutions(Set.of(LOCATION_SOLUTION))
    .setPreferredLocationSolution(PreferredLocationSolution.from(LOCATION_SOLUTION))
    .setAssociations(List.of(SIGNAL_DETECTION_EVENT_ASSOCIATION))
    .build();

  // Create an Event
  public static final Event EVENT = Event.from(EVENT_ID,
    Set.of(),
    "monitoringOrg",
    Set.of(EVENT_HYPOTHESIS),
    List.of(FinalEventHypothesis.from(EVENT_HYPOTHESIS)),
    List.of(PreferredEventHypothesis.from(UUID.randomUUID(), EVENT_HYPOTHESIS)));

  static {
    // mark event as final
    EVENT.markFinal(EVENT.getHypotheses().iterator().next());
  }

  private EventTestFixtures() {
    // empty private constructor for static test fixtures factory
  }
}
