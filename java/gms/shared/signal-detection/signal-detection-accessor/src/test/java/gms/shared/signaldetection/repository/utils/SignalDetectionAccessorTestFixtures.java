package gms.shared.signaldetection.repository.utils;

import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.coi.values.AmplitudeMeasurementValue;
import gms.shared.signaldetection.coi.values.ArrivalTimeMeasurementValue;
import gms.shared.signaldetection.coi.values.FirstMotionMeasurementValue;
import gms.shared.signaldetection.coi.values.NumericMeasurementValue;
import gms.shared.signaldetection.coi.values.PhaseTypeMeasurementValue;
import gms.shared.signaldetection.converter.measurementvalue.specs.MeasurementValueSpec;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.stationdefinition.coi.utils.Units;

import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.AMPLITUDE_DAO_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_2;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_3;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ASSOC_DAO_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ASSOC_DAO_2;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.WORKFLOW_DEFINITION_ID1;

public class SignalDetectionAccessorTestFixtures {
  // ------- ArrivalDao MeasurementValueSpecs --------
  public static final MeasurementValueSpec<ArrivalTimeMeasurementValue> ARRIVAL_MEASUREMENT_SPEC =
    MeasurementValueSpec.<ArrivalTimeMeasurementValue>builder()
      .setArrivalDao(ARRIVAL_1)
      .setFeatureMeasurementType(FeatureMeasurementTypes.ARRIVAL_TIME)
      .build();
  public static final MeasurementValueSpec<PhaseTypeMeasurementValue> PHASE_MEASUREMENT_SPEC =
    MeasurementValueSpec.<PhaseTypeMeasurementValue>builder()
      .setArrivalDao(ARRIVAL_1)
      .setFeatureMeasurementType(FeatureMeasurementTypes.PHASE)
      .build();
  public static final MeasurementValueSpec<NumericMeasurementValue> RECEIVER_AZIMUTH_MEASUREMENT_SPEC =
    MeasurementValueSpec.<NumericMeasurementValue>builder()
      .setArrivalDao(ARRIVAL_2)
      .setFeatureMeasurementType(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH)
      .setMeasuredValueExtractor(ArrivalDao::getAzimuth)
      .setUncertaintyValueExtractor(ArrivalDao::getAzimuthUncertainty)
      .setUnits(Units.DEGREES)
      .build();
  public static final MeasurementValueSpec<NumericMeasurementValue> SLOWNESS_MEASUREMENT_SPEC =
    MeasurementValueSpec.<NumericMeasurementValue>builder()
      .setArrivalDao(ARRIVAL_1)
      .setFeatureMeasurementType(FeatureMeasurementTypes.SLOWNESS)
      .setMeasuredValueExtractor(ArrivalDao::getSlowness)
      .setUnits(Units.SECONDS_PER_DEGREE)
      .build();
  public static final MeasurementValueSpec<NumericMeasurementValue> EMERGENCE_ANGLE_MEASUREMENT_SPEC =
    MeasurementValueSpec.<NumericMeasurementValue>builder()
      .setArrivalDao(ARRIVAL_1)
      .setFeatureMeasurementType(FeatureMeasurementTypes.EMERGENCE_ANGLE)
      .setMeasuredValueExtractor(ArrivalDao::getEmergenceAngle)
      .setUnits(Units.DEGREES)
      .build();
  public static final MeasurementValueSpec<NumericMeasurementValue> RECTILINEARITY_MEASUREMENT_SPEC =
    MeasurementValueSpec.<NumericMeasurementValue>builder()
      .setArrivalDao(ARRIVAL_1)
      .setFeatureMeasurementType(FeatureMeasurementTypes.RECTILINEARITY)
      .setMeasuredValueExtractor(ArrivalDao::getRectilinearity)
      .setUnits(Units.UNITLESS)
      .build();
  public static final MeasurementValueSpec<FirstMotionMeasurementValue> SHORT_PERIOD_MEASUREMENT_SPEC =
    MeasurementValueSpec.<FirstMotionMeasurementValue>builder()
      .setArrivalDao(ARRIVAL_1)
      .setFeatureMeasurementType(FeatureMeasurementTypes.SHORT_PERIOD_FIRST_MOTION)
      .setFeatureMeasurementTypeCode("c")
      .build();
  public static final MeasurementValueSpec<FirstMotionMeasurementValue> LONG_PERIOD_MEASUREMENT_SPEC =
    MeasurementValueSpec.<FirstMotionMeasurementValue>builder()
      .setArrivalDao(ARRIVAL_3)
      .setFeatureMeasurementType(FeatureMeasurementTypes.LONG_PERIOD_FIRST_MOTION)
      .setFeatureMeasurementTypeCode("r")
      .build();

  // ------- AssocDao MeasurementValueSpecs --------
  public static final MeasurementValueSpec<ArrivalTimeMeasurementValue> ARRIVAL_MEASUREMENT_SPEC_2 =
    MeasurementValueSpec.<ArrivalTimeMeasurementValue>builder()
      .setArrivalDao(ARRIVAL_1)
      .setAssocDao(ASSOC_DAO_1)
      .setFeatureMeasurementType(FeatureMeasurementTypes.ARRIVAL_TIME)
      .build();
  public static final MeasurementValueSpec<PhaseTypeMeasurementValue> PHASE_MEASUREMENT_SPEC_2 =
    MeasurementValueSpec.<PhaseTypeMeasurementValue>builder()
      .setArrivalDao(ARRIVAL_2)
      .setAssocDao(ASSOC_DAO_2)
      .setFeatureMeasurementType(FeatureMeasurementTypes.PHASE)
      .build();

  public static final MeasurementValueSpec<AmplitudeMeasurementValue> AMPLITUDE_MEASURMENT_SPEC =
    MeasurementValueSpec.<AmplitudeMeasurementValue>builder()
      .setAmplitudeDao(AMPLITUDE_DAO_1)
      .setArrivalDao(ARRIVAL_1)
      .setFeatureMeasurementType(FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2)
      .build();

  public static final SignalDetectionHypothesisArrivalIdComponents SIGNAL_DETECTION_HYPOTHESIS_ARRIVAL_ID_COMPONENTS_1 =
    SignalDetectionHypothesisArrivalIdComponents.create(WORKFLOW_DEFINITION_ID1.getName(), ARRIVAL_1.getId());
  public static final SignalDetectionHypothesisArrivalIdComponents SIGNAL_DETECTION_HYPOTHESIS_ARRIVAL_ID_COMPONENTS_3 =
    SignalDetectionHypothesisArrivalIdComponents.create(WORKFLOW_DEFINITION_ID1.getName(), ARRIVAL_3.getId());

}
