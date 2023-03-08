package gms.shared.frameworks.osd.coi.signaldetection;

import gms.shared.frameworks.osd.coi.DoubleValue;
import gms.shared.frameworks.osd.coi.InstantValue;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.signaldetection.EnumeratedMeasurementValue.FirstMotionMeasurementValue;
import gms.shared.frameworks.osd.coi.signaldetection.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

/**
 * Tests {@link FeatureMeasurement} factory creation
 */
class FeatureMeasurementTests {

  /**
   * Tests that all combinations of malformed arguments passed to {@link
   * FeatureMeasurement#from(Channel, MeasuredChannelSegmentDescriptor, String, Object)} result
   * in the correct exceptions being thrown.
   */
  @ParameterizedTest
  @MethodSource("testMalformedArgumentsJsonCreatorProvider")
  <V extends Serializable> void testMalformedArgumentsJsonCreator(
    Channel channel,
    MeasuredChannelSegmentDescriptor descriptor,
    String stringType,
    V measurementValue,
    Class<Throwable> expectedExceptionClass) {

    Throwable exception = Assertions.assertThrows(expectedExceptionClass, () -> {
      FeatureMeasurement.from(
        channel,
        descriptor,
        stringType,
        measurementValue
      );
    });
  }

  @Test
  void testSerializationPhaseMeasurement() throws IOException {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.PHASE_FEATURE_MEASUREMENT,
      FeatureMeasurement.class);
  }

  @Test
  void testSerializationFirstMotionMeasurement() throws IOException {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.FIRST_MOTION_FEATURE_MEASUREMENT,
      FeatureMeasurement.class);
  }

  @Test
  void testSerializationNumericalMeasurement() throws IOException {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.ARRIVAL_TIME_FEATURE_MEASUREMENT,
      FeatureMeasurement.class);
  }

  @Test
  void testSerializationAmplitudeMeasurement() throws IOException {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.AMPLITUDE_FEATURE_MEASUREMENT,
      FeatureMeasurement.class);
  }

  @Test
  void testSerializationInstantMeasurement() throws IOException {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.INSTANT_FEATURE_MEASUREMENT,
      FeatureMeasurement.class);
  }

  @Test
  void testSerializationBaseMeasurementValue() throws IOException {
    TestUtilities
      .testSerialization(SignalDetectionTestFixtures.standardDoubleValue, DoubleValue.class);
    TestUtilities
      .testSerialization(SignalDetectionTestFixtures.ARRIVAL_TIME_MEASUREMENT, InstantValue.class);
    TestUtilities.testSerialization(SignalDetectionTestFixtures.phaseMeasurement,
      PhaseTypeMeasurementValue.class);
    TestUtilities.testSerialization(SignalDetectionTestFixtures.firstMotionMeasurement,
      FirstMotionMeasurementValue.class);
    TestUtilities.testSerialization(SignalDetectionTestFixtures.amplitudeMeasurement,
      AmplitudeMeasurementValue.class);
    TestUtilities
      .testSerialization(SignalDetectionTestFixtures.instantMeasurement, InstantValue.class);
  }

  /**
   * {@link MethodSource} for
   * {@link FeatureMeasurementTests#testMalformedArgumentsJsonCreator(Channel, MeasuredChannelSegmentDescriptor,
   * String, Serializable, Class)}.
   *
   * @return {@link Stream} of {@link Arguments} containing the arguments for {@link
   * FeatureMeasurementTests#testMalformedArgumentsJsonCreator(Channel, MeasuredChannelSegmentDescriptor, String, Serializable, Class,
   *)}.
   */
  private static Stream<Arguments> testMalformedArgumentsJsonCreatorProvider() {

    String stringType = FeatureMeasurementTypes.ARRIVAL_TIME.getFeatureMeasurementTypeName();
    InstantValue instantValue = InstantValue.from(
      Instant.EPOCH,
      Duration.ofMillis(2)
    );

    return Stream.of(
      Arguments.arguments(
        null,
        UtilsTestFixtures.DESCRIPTOR,
        stringType,
        instantValue,
        NullPointerException.class),
      Arguments.arguments(
        UtilsTestFixtures.CHANNEL,
        null,
        stringType,
        instantValue,
        NullPointerException.class),
      Arguments.arguments(
        UtilsTestFixtures.CHANNEL,
        UtilsTestFixtures.DESCRIPTOR,
        null,
        instantValue,
        NullPointerException.class),
      Arguments.arguments(
        UtilsTestFixtures.CHANNEL,
        UtilsTestFixtures.DESCRIPTOR,
        stringType,
        null,
        NullPointerException.class)
    );
  }
}
