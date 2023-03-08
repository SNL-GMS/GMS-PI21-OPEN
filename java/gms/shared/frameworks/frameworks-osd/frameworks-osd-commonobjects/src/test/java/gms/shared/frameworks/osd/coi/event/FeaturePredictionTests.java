package gms.shared.frameworks.osd.coi.event;

import gms.shared.frameworks.osd.coi.DoubleValue;
import gms.shared.frameworks.osd.coi.InstantValue;
import gms.shared.frameworks.osd.coi.PhaseType;
import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.signaldetection.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.frameworks.osd.coi.signaldetection.FeatureMeasurementTypes;
import gms.shared.frameworks.osd.coi.signaldetection.Location;
import gms.shared.frameworks.osd.coi.signaldetection.NumericMeasurementType;
import gms.shared.frameworks.osd.coi.signaldetection.NumericMeasurementValue;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @deprecated moving to new feature prediction project
 */
@Deprecated(forRemoval = true)
class FeaturePredictionTests {

  private static EventLocation eventLocation = EventLocation.from(80.0, 90.0, 0.0, Instant.MIN);

  private static Location receiverLocation = Location.from(90.0, 90.0, 0.0, 0.0);

  private static String CHANNEL_NAME = UtilsTestFixtures.CHANNEL.getName();

  private static FeaturePrediction<InstantValue> featurePredictionInstant =
    FeaturePrediction.<InstantValue>builder()
      .setPhase(PhaseType.P)
      .setPredictedValue(InstantValue.from(Instant.EPOCH, Duration.ZERO))
      .setFeaturePredictionComponents(Set.of())
      .setExtrapolated(false)
      .setPredictionType(FeatureMeasurementTypes.ARRIVAL_TIME)
      .setSourceLocation(eventLocation)
      .setReceiverLocation(receiverLocation)
      .setChannelName(CHANNEL_NAME)
      .build();

  private static FeaturePrediction<NumericMeasurementValue> featurePredictionNumeric =
    FeaturePrediction.<NumericMeasurementValue>builder()
      .setPhase(PhaseType.P)
      .setPredictedValue(
        NumericMeasurementValue.from(
          Instant.EPOCH,
          DoubleValue.from(1.0, 0.0, Units.UNITLESS)
        )
      )
      .setFeaturePredictionComponents(Set.of())
      .setExtrapolated(false)
      .setPredictionType(FeatureMeasurementTypes.SLOWNESS)
      .setSourceLocation(eventLocation)
      .setReceiverLocation(receiverLocation)
      .setChannelName(CHANNEL_NAME)
      .build();

  private static FeaturePrediction<PhaseTypeMeasurementValue> featurePredictionPhase =
    FeaturePrediction.<PhaseTypeMeasurementValue>builder()
      .setPhase(PhaseType.P)
      .setPredictedValue(PhaseTypeMeasurementValue.from(PhaseType.S, 0.0))
      .setFeaturePredictionComponents(Set.of())
      .setExtrapolated(false)
      .setPredictionType(FeatureMeasurementTypes.PHASE)
      .setSourceLocation(eventLocation)
      .setReceiverLocation(receiverLocation)
      .setChannelName(CHANNEL_NAME)
      .build();

  @ParameterizedTest
  @MethodSource("testFeaturePredictionProvider")
  void testSerialization(FeaturePrediction<?> referenceFeaturePrediction) throws IOException {

    TestUtilities.testSerialization(referenceFeaturePrediction, FeaturePrediction.class);

  }

  static Stream<Arguments> testFeaturePredictionProvider() {
    return Stream.of(

      //NOTE: Right now only Instant and Numeric feature predictions exist.
      Arguments.arguments(featurePredictionInstant),
      Arguments.arguments(featurePredictionNumeric),

      Arguments.arguments(featurePredictionPhase)
    );
  }

  @Test
  void testEqualHashCodes() {
    NumericMeasurementValue predictedValue = NumericMeasurementValue
      .from(Instant.EPOCH, DoubleValue.from(1.0, 1.0, Units.SECONDS));
    NumericMeasurementValue predictedValue2 = NumericMeasurementValue
      .from(Instant.EPOCH, DoubleValue.from(2.0, 2.0, Units.SECONDS));
    Set<FeaturePredictionComponent> featurePredictionComponents = new HashSet<>();
    boolean extrapolated = false;
    NumericMeasurementType predictionType = FeatureMeasurementTypes.SLOWNESS;
    EventLocation sourceLocation = EventLocation.from(
      10.0, 10.0, 10.0, Instant.EPOCH);
    Location receiverLocation = Location.from(10.0, 10.0, 10.0, 10.0);
    Optional<String> channelName = Optional.of(UtilsTestFixtures.CHANNEL.getName());

    UUID fpUuid = UUID.fromString("22111111-1111-1111-1111-111111111111");
    FeaturePrediction<NumericMeasurementValue> fp1 = FeaturePrediction
      .<NumericMeasurementValue>builder()
      .setPhase(PhaseType.P)
      .setPredictedValue(predictedValue)
      .setFeaturePredictionComponents(featurePredictionComponents)
      .setExtrapolated(extrapolated)
      .setPredictionType(predictionType)
      .setSourceLocation(sourceLocation)
      .setReceiverLocation(receiverLocation)
      .setChannelName(channelName)
      .setFeaturePredictionDerivativeMap(Map.of())
      .build();

    FeaturePrediction<NumericMeasurementValue> fp2 = FeaturePrediction
      .<NumericMeasurementValue>builder()
      .setPhase(PhaseType.P)
      .setPredictedValue(predictedValue)
      .setFeaturePredictionComponents(featurePredictionComponents)
      .setExtrapolated(extrapolated)
      .setPredictionType(predictionType)
      .setSourceLocation(sourceLocation)
      .setReceiverLocation(receiverLocation)
      .setChannelName(channelName)
      .setFeaturePredictionDerivativeMap(Map.of())
      .build();

    assertEquals(fp1.hashCode(), fp2.hashCode());
    assertEquals(fp1, fp2);

    FeaturePrediction<NumericMeasurementValue> fp3 = FeaturePrediction
      .<NumericMeasurementValue>builder()
      .setPhase(PhaseType.P)
      .setPredictedValue(predictedValue2)
      .setFeaturePredictionComponents(featurePredictionComponents)
      .setExtrapolated(extrapolated)
      .setPredictionType(predictionType)
      .setSourceLocation(sourceLocation)
      .setReceiverLocation(receiverLocation)
      .setChannelName(channelName)
      .setFeaturePredictionDerivativeMap(Map.of())
      .build();

    assertNotEquals(fp1.hashCode(), fp3.hashCode());
    assertNotEquals(fp1, fp3);
  }
}
