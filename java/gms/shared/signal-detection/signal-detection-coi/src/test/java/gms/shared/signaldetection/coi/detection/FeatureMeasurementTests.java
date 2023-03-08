package gms.shared.signaldetection.coi.detection;

import gms.shared.signaldetection.coi.types.ArrivalTimeMeasurementType;
import gms.shared.signaldetection.coi.types.FeatureMeasurementType;
import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.coi.values.AmplitudeMeasurementValue;
import gms.shared.signaldetection.coi.values.ArrivalTimeMeasurementValue;
import gms.shared.signaldetection.coi.values.FirstMotionMeasurementValue;
import gms.shared.signaldetection.coi.values.InstantValue;
import gms.shared.signaldetection.coi.values.PhaseTypeMeasurementValue;
import gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.stationdefinition.testfixtures.UtilsTestFixtures;
import gms.shared.utilities.test.TestUtilities;
import gms.shared.waveform.coi.ChannelSegment;
import gms.shared.waveform.coi.Waveform;
import gms.shared.waveform.testfixture.WaveformTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link FeatureMeasurement} factory creation
 */
class FeatureMeasurementTests {

  /**
   * Tests that all combinations of malformed arguments passed to {@link
   * FeatureMeasurement#from(Channel, ChannelSegment, FeatureMeasurementType, Object, Optional)} result
   * in the correct exceptions being thrown.
   */
  @ParameterizedTest
  @MethodSource("testMalformedArgumentsJsonCreatorProvider")
  <V extends Serializable> void testMalformedArgumentsJsonCreator(
    Channel channel,
    ChannelSegment<Waveform> channelSegment,
    FeatureMeasurementType<V> featureMeasurementType,
    V measurementValue,
    Optional<DoubleValue> snr,
    Class<Throwable> expectedExceptionClass) {

    assertThrows(expectedExceptionClass, () -> {
      FeatureMeasurement.from(
        channel,
        channelSegment,
        featureMeasurementType,
        measurementValue,
        snr
      );
    });
  }

  private static Stream<Arguments> testMalformedArgumentsJsonCreatorProvider() {
    ArrivalTimeMeasurementType featureMeasurementType = FeatureMeasurementTypes.ARRIVAL_TIME;
    InstantValue instantValue = InstantValue.from(
      Instant.EPOCH,
      Duration.ofMillis(2)
    );

    Optional<DoubleValue> snr = Optional.of(DoubleValue.from(1.0, Optional.of(0.1), Units.DEGREES));

    return Stream.of(
      Arguments.arguments(
        null,
        WaveformTestFixtures.singleStationEpochStart100RandomSamples(),
        featureMeasurementType,
        instantValue,
        snr,
        NullPointerException.class),
      Arguments.arguments(
        UtilsTestFixtures.CHANNEL,
        null,
        featureMeasurementType,
        instantValue,
        snr,
        NullPointerException.class),
      Arguments.arguments(
        UtilsTestFixtures.CHANNEL,
        WaveformTestFixtures.singleStationEpochStart100RandomSamples(),
        null,
        instantValue,
        snr,
        NullPointerException.class),
      Arguments.arguments(
        UtilsTestFixtures.CHANNEL,
        WaveformTestFixtures.singleStationEpochStart100RandomSamples(),
        featureMeasurementType,
        null,
        snr,
        NullPointerException.class)
    );
  }

  @Test
  void testSerializationPhaseMeasurement() {
    TestUtilities.assertSerializes(SignalDetectionTestFixtures.PHASE_FEATURE_MEASUREMENT,
      FeatureMeasurement.class);
  }

  @Test
  void testSerializationFirstMotionMeasurement() {
    TestUtilities.assertSerializes(SignalDetectionTestFixtures.LONG_PERIOD_FIRST_MOTION_FEATURE_MEASUREMENT,
      FeatureMeasurement.class);
  }

  @Test
  void testSerializationNumericalMeasurement() {
    TestUtilities.assertSerializes(SignalDetectionTestFixtures.ARRIVAL_TIME_FEATURE_MEASUREMENT,
      FeatureMeasurement.class);
  }

  @Test
  void testSerializationAmplitudeMeasurement() {
    TestUtilities.assertSerializes(SignalDetectionTestFixtures.AMPLITUDE_FEATURE_MEASUREMENT,
      FeatureMeasurement.class);
  }

  @Test
  void testSerializationInstantMeasurement() {
    TestUtilities.assertSerializes(SignalDetectionTestFixtures.INSTANT_FEATURE_MEASUREMENT,
      FeatureMeasurement.class);
  }

  @Test
  void testSerializationBaseMeasurementValue() {
    TestUtilities.assertSerializes(SignalDetectionTestFixtures.standardDoubleValue,
      DoubleValue.class);
    TestUtilities.assertSerializes(SignalDetectionTestFixtures.ARRIVAL_TIME_MEASUREMENT,
      ArrivalTimeMeasurementValue.class);
    TestUtilities.assertSerializes(SignalDetectionTestFixtures.PHASE_MEASUREMENT,
      PhaseTypeMeasurementValue.class);
    TestUtilities.assertSerializes(SignalDetectionTestFixtures.firstMotionMeasurement,
      FirstMotionMeasurementValue.class);
    TestUtilities.assertSerializes(SignalDetectionTestFixtures.amplitudeMeasurement,
      AmplitudeMeasurementValue.class);
    TestUtilities.assertSerializes(SignalDetectionTestFixtures.instantMeasurement,
      InstantValue.class);
  }
}
