package gms.shared.event.coi.featureprediction;

import gms.shared.event.coi.EventLocation;
import gms.shared.event.coi.featureprediction.type.FeaturePredictionType;
import gms.shared.event.coi.featureprediction.value.ArrivalTimeFeaturePredictionValue;
import gms.shared.event.coi.featureprediction.value.NumericFeaturePredictionValue;
import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.signaldetection.coi.values.ArrivalTimeMeasurementValue;
import gms.shared.signaldetection.coi.values.DurationValue;
import gms.shared.signaldetection.coi.values.InstantValue;
import gms.shared.signaldetection.coi.values.NumericMeasurementValue;
import gms.shared.stationdefinition.coi.channel.Location;
import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

class FeaturePredictionContainerTests {

  private static final FeaturePrediction<ArrivalTimeFeaturePredictionValue> fp1a = FeaturePrediction.<ArrivalTimeFeaturePredictionValue>builder()
    .setPredictionValue(
      ArrivalTimeFeaturePredictionValue.from(
        FeatureMeasurementTypes.ARRIVAL_TIME,
        ArrivalTimeMeasurementValue.from(
          InstantValue.from(Instant.ofEpochSecond(1), Duration.ofHours(1)),
          Optional.of(DurationValue.from(Duration.ofDays(1), Duration.ZERO))
        ),
        Map.of(),
        Set.of()
      )
    )
    .setPredictionType(FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE)
    .setPhase(PhaseType.P)
    .setExtrapolated(false)
    .setSourceLocation(EventLocation.from(1, 1, 1, Instant.EPOCH))
    .setReceiverLocation(Location.from(1.0, 1.0, 1.0, 1.0))
    .setChannel(Optional.empty())
    .setPredictionChannelSegment(Optional.empty())
    .build();

  private static final FeaturePrediction<ArrivalTimeFeaturePredictionValue> fp1b = FeaturePrediction.<ArrivalTimeFeaturePredictionValue>builder()
    .setPredictionValue(
      ArrivalTimeFeaturePredictionValue.from(
        FeatureMeasurementTypes.ARRIVAL_TIME,
        ArrivalTimeMeasurementValue.from(
          InstantValue.from(Instant.ofEpochSecond(10), Duration.ofHours(10)),
          Optional.of(DurationValue.from(Duration.ofDays(10), Duration.ZERO))
        ),
        Map.of(),
        Set.of()
      )
    )
    .setPredictionType(FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE)
    .setPhase(PhaseType.P)
    .setExtrapolated(false)
    .setSourceLocation(EventLocation.from(10, 1, 1, Instant.EPOCH))
    .setReceiverLocation(Location.from(1.0, 1.0, 1.0, 1.0))
    .setChannel(Optional.empty())
    .setPredictionChannelSegment(Optional.empty())
    .build();

  private static final FeaturePrediction<NumericFeaturePredictionValue> fp2 = FeaturePrediction.<NumericFeaturePredictionValue>builder()
    .setPredictionValue(
      NumericFeaturePredictionValue.from(
        FeatureMeasurementTypes.SLOWNESS,
        NumericMeasurementValue.from(
          Optional.of(Instant.ofEpochSecond(1)),
          DoubleValue.from(
            1.0, Optional.of(1.0), Units.SECONDS_PER_DEGREE
          )
        ),
        Map.of(),
        Set.of()
      )
    )
    .setPredictionType(FeaturePredictionType.SLOWNESS_PREDICTION_TYPE)
    .setPhase(PhaseType.P)
    .setExtrapolated(false)
    .setSourceLocation(EventLocation.from(1, 1, 1, Instant.EPOCH))
    .setReceiverLocation(Location.from(1.0, 1.0, 1.0, 1.0))
    .setChannel(Optional.empty())
    .setPredictionChannelSegment(Optional.empty())
    .build();

  @Test
  void testRetrieval() {

    var container = FeaturePredictionContainer.of(fp1a, fp1b, fp2);

    testArrivalTimeFpCollectionEquality(
      Set.of(fp1a, fp1b),
      container.getFeaturePredictionsForType(FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE)
    );

    testSlownessFpCollectionEquality(
      Set.of(fp2),
      container.getFeaturePredictionsForType(FeaturePredictionType.SLOWNESS_PREDICTION_TYPE)
    );
  }

  private void testArrivalTimeFpCollectionEquality(
    Collection<FeaturePrediction<ArrivalTimeFeaturePredictionValue>> expected,
    Collection<FeaturePrediction<ArrivalTimeFeaturePredictionValue>> actual) {

    Assertions.assertEquals(expected, actual);
  }

  private void testSlownessFpCollectionEquality(
    Collection<FeaturePrediction<NumericFeaturePredictionValue>> expected,
    Collection<FeaturePrediction<NumericFeaturePredictionValue>> actual) {

    Assertions.assertEquals(expected, actual);
  }

  @Test
  void testSerialization() {

    var container = FeaturePredictionContainer.of(fp1a, fp1b, fp2);

    TestUtilities.assertSerializes(container, FeaturePredictionContainer.class);
  }

  @Test
  void testContains() {

    var container = FeaturePredictionContainer.of(fp1a, fp1b, fp2);

    Assertions.assertTrue(container.contains(fp1a));
    Assertions.assertTrue(container.contains(fp1b));
    Assertions.assertTrue(container.contains(fp2));

  }

  @Test
  void testMap() {

    var container1 = FeaturePredictionContainer.of(fp1a, fp2);

    Assertions.assertEquals(2, container1.map(Function.identity()).count());
  }

  @Test
  void testEqualsHashcode() {

    var container1 = FeaturePredictionContainer.of(fp1a, fp2);
    var container2 = FeaturePredictionContainer.of(fp2, fp1a);

    Assertions.assertEquals(container1, container1);
    Assertions.assertEquals(container1, container2);
    Assertions.assertEquals(container2, container1);
    Assertions.assertEquals(container1.hashCode(), container2.hashCode());
  }

  @Test
  void testNotEquals() {
    var container1 = FeaturePredictionContainer.of(fp1a, fp2);
    var container2 = FeaturePredictionContainer.of(fp1a);

    Assertions.assertNotEquals(container1, container2);
    Assertions.assertNotEquals(container2, container1);

    // test instanceof path
    Assertions.assertNotEquals(100, container1);
  }
}
