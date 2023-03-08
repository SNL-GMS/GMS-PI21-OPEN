package gms.shared.event.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.value.AutoValue;
import gms.shared.event.coi.EventLocation;
import gms.shared.event.coi.featureprediction.FeaturePrediction;
import gms.shared.event.coi.featureprediction.FeaturePredictionContainer;
import gms.shared.event.coi.featureprediction.type.FeaturePredictionType;
import gms.shared.event.coi.featureprediction.value.ArrivalTimeFeaturePredictionValue;
import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.signaldetection.coi.values.ArrivalTimeMeasurementValue;
import gms.shared.signaldetection.coi.values.DurationValue;
import gms.shared.signaldetection.coi.values.InstantValue;
import gms.shared.stationdefinition.coi.channel.Location;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;
import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@AutoValue
@JsonIgnoreProperties(ignoreUnknown = true)
class FeaturePredictionsByReceiverNameTest {
  private static final ObjectMapper MAPPER = ObjectMapperFactory.getJsonObjectMapper();

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

  @Test
  void testSerializationEmpty() throws IOException {
    var receiverMap = new HashMap<String, FeaturePredictionContainer>();
    var request = FeaturePredictionsByReceiverName.from(receiverMap);
    TestUtilities.assertSerializes(request, FeaturePredictionsByReceiverName.class);
  }

  @Test
  void testSerialization() {
    var container = FeaturePredictionContainer.of(fp1a);
    var request = FeaturePredictionsByReceiverName.from(Map.of("TestReceiver", container));
    TestUtilities.assertSerializes(request, FeaturePredictionsByReceiverName.class);
  }

  @Test
  @Disabled("Disabled so it doesn't run in the pipeline. Re-enable locally to generate dump")
  void dumpFeaturePredictionsByReceiverName() throws IOException {
    var container = FeaturePredictionContainer.of(fp1a);
    var request = FeaturePredictionsByReceiverName.from(Map.of("TestReceiver", container, "TestReceiver2", container));
    try (FileOutputStream outputStream = new FileOutputStream(
      "build/mock-feature-predictions-by-receiver-name-response.json")) {
      assertDoesNotThrow(() -> outputStream.write(MAPPER
        .writerWithDefaultPrettyPrinter()
        .writeValueAsBytes(request)));
    }
  }
}
