package gms.shared.signaldetection.converter.measurementvalues;

import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.coi.values.AmplitudeMeasurementValue;
import gms.shared.signaldetection.converter.measurementvalue.specs.MeasurementValueSpec;
import gms.shared.signaldetection.dao.css.AmplitudeDao;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.dao.css.enums.AmplitudeUnits;
import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.utilities.bridge.database.enums.ClipFlag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class AmplitudeMeasurementValueConverterTest {

  private AmplitudeMeasurementValueConverter converter;
  public static final ArrivalDao ARRIVAL_DAO = new ArrivalDao();
  private static double AMP_VAL = 23.45;
  private static Instant TIME = Instant.parse("2015-12-07T12:13:14.000Z");
  private static double NANO_SEC = 908_343_342;
  private static double NANO_SECOND_PER_SECOND = 1_000_000_000;
  private static Duration DURATION = Duration.ofNanos(1241235);

  @BeforeEach
  void setup() {
    converter = AmplitudeMeasurementValueConverter.create();
  }

  @Test
  void testCreate() {
    AmplitudeMeasurementValueConverter converter =
      assertDoesNotThrow(AmplitudeMeasurementValueConverter::create);
    assertNotNull(converter);
  }

  @ParameterizedTest
  @MethodSource("getTestConvertArguments")
  void testConvert(Optional<AmplitudeMeasurementValue> expectedValue,
    MeasurementValueSpec<AmplitudeMeasurementValue> measurementValueSpec) {

    final Optional<AmplitudeMeasurementValue> actualValue;
    actualValue = converter.convert(measurementValueSpec);

    assertEquals(expectedValue, actualValue);
  }

  static Stream<Arguments> getTestConvertArguments() {

    MeasurementValueSpec<AmplitudeMeasurementValue> measurementValueSpec1 = MeasurementValueSpec.<AmplitudeMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_DAO)
      .setFeatureMeasurementType(FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2)
      .build();

    var expected1 = Optional.empty();

    AmplitudeDao ampDao1 = new AmplitudeDao();
    ampDao1.setAmplitude(AMP_VAL);

    MeasurementValueSpec<AmplitudeMeasurementValue> measurementValueSpec2 = MeasurementValueSpec.<AmplitudeMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_DAO)
      .setAmplitudeDao(ampDao1)
      .setFeatureMeasurementType(FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2)
      .build();

    AmplitudeDao ampDao2 = new AmplitudeDao();
    ampDao2.setAmplitude(AMP_VAL);
    ampDao2.setUnits(AmplitudeUnits.NA);

    MeasurementValueSpec<AmplitudeMeasurementValue> measurementValueSpec3 = MeasurementValueSpec.<AmplitudeMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_DAO)
      .setAmplitudeDao(ampDao2)
      .setFeatureMeasurementType(FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2)
      .build();

    double nanoSeconds = NANO_SEC / NANO_SECOND_PER_SECOND;

    AmplitudeDao ampDao3 = new AmplitudeDao();
    ampDao3.setAmplitude(AMP_VAL);
    ampDao3.setUnits(AmplitudeUnits.NA);
    ampDao3.setPeriod(nanoSeconds);
    MeasurementValueSpec<AmplitudeMeasurementValue> measurementValueSpec4 = MeasurementValueSpec.<AmplitudeMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_DAO)
      .setAmplitudeDao(ampDao3)
      .setFeatureMeasurementType(FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2)
      .build();

    DoubleValue doubleValue = DoubleValue.from(AMP_VAL, Optional.empty(), Units.UNITLESS);
    AmplitudeMeasurementValue ampMeasurementVal =
      AmplitudeMeasurementValue.from(doubleValue, Duration.ofNanos((long) NANO_SEC), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    Optional<AmplitudeMeasurementValue> expected2 = Optional.of(ampMeasurementVal);

    AmplitudeDao ampDao4 = new AmplitudeDao();
    ampDao4.setAmplitude(AMP_VAL);
    ampDao4.setUnits(AmplitudeUnits.NA);
    ampDao4.setPeriod(nanoSeconds);
    ampDao4.setAmplitudeTime(TIME);

    MeasurementValueSpec<AmplitudeMeasurementValue> measurementValueSpec5 = MeasurementValueSpec.<AmplitudeMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_DAO)
      .setAmplitudeDao(ampDao4)
      .setFeatureMeasurementType(FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2)
      .build();

    AmplitudeMeasurementValue ampMeasurementVal2 =
      AmplitudeMeasurementValue.from(doubleValue, Duration.ofNanos((long) NANO_SEC), Optional.of(TIME),
        Optional.empty(), Optional.empty(), Optional.empty());
    Optional<AmplitudeMeasurementValue> expected3 = Optional.of(ampMeasurementVal2);

    AmplitudeDao ampDao5 = new AmplitudeDao();
    ampDao5.setAmplitude(AMP_VAL);
    ampDao5.setUnits(AmplitudeUnits.NA);
    ampDao5.setPeriod(nanoSeconds);
    ampDao5.setAmplitudeTime(TIME);
    ampDao5.setTime(TIME);

    MeasurementValueSpec<AmplitudeMeasurementValue> measurementValueSpec6 = MeasurementValueSpec.<AmplitudeMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_DAO)
      .setAmplitudeDao(ampDao5)
      .setFeatureMeasurementType(FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2)
      .build();

    AmplitudeMeasurementValue ampMeasurementVal3 =
      AmplitudeMeasurementValue.from(doubleValue, Duration.ofNanos((long) NANO_SEC), Optional.of(TIME),
        Optional.of(TIME), Optional.empty(), Optional.empty());
    Optional<AmplitudeMeasurementValue> expected4 = Optional.of(ampMeasurementVal3);

    AmplitudeDao ampDao6 = new AmplitudeDao();
    ampDao6.setAmplitude(AMP_VAL);
    ampDao6.setUnits(AmplitudeUnits.NA);
    ampDao6.setPeriod(nanoSeconds);
    ampDao6.setAmplitudeTime(TIME);
    ampDao6.setTime(TIME);
    ampDao6.setDuration(DURATION);

    MeasurementValueSpec<AmplitudeMeasurementValue> measurementValueSpec7 = MeasurementValueSpec.<AmplitudeMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_DAO)
      .setAmplitudeDao(ampDao6)
      .setFeatureMeasurementType(FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2)
      .build();

    AmplitudeMeasurementValue ampMeasurementVal4 =
      AmplitudeMeasurementValue.from(doubleValue, Duration.ofNanos((long) NANO_SEC), Optional.of(TIME),
        Optional.of(TIME), Optional.of(DURATION), Optional.empty());
    Optional<AmplitudeMeasurementValue> expected5 = Optional.of(ampMeasurementVal4);

    AmplitudeDao ampDao7 = new AmplitudeDao();
    ampDao7.setAmplitude(AMP_VAL);
    ampDao7.setUnits(AmplitudeUnits.NA);
    ampDao7.setPeriod(nanoSeconds);
    ampDao7.setAmplitudeTime(TIME);
    ampDao7.setTime(TIME);
    ampDao7.setDuration(DURATION);
    ampDao7.setClip(ClipFlag.CLIPPED);

    MeasurementValueSpec<AmplitudeMeasurementValue> measurementValueSpec8 = MeasurementValueSpec.<AmplitudeMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_DAO)
      .setAmplitudeDao(ampDao7)
      .setFeatureMeasurementType(FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2)
      .build();

    AmplitudeMeasurementValue ampMeasurementVal5 =
      AmplitudeMeasurementValue.from(doubleValue, Duration.ofNanos((long) NANO_SEC), Optional.of(TIME),
        Optional.of(TIME), Optional.of(DURATION), Optional.of(Boolean.TRUE));
    Optional<AmplitudeMeasurementValue> expected6 = Optional.of(ampMeasurementVal5);

    return Stream.of(
      arguments(expected1, measurementValueSpec1),
      arguments(expected1, measurementValueSpec2),
      arguments(expected1, measurementValueSpec3),
      arguments(expected2, measurementValueSpec4),
      arguments(expected3, measurementValueSpec5),
      arguments(expected4, measurementValueSpec6),
      arguments(expected5, measurementValueSpec7),
      arguments(expected6, measurementValueSpec8)
    );
  }
}
