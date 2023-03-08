package gms.shared.signaldetection.coi.values;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ArrivalTimeMeasurementValueTests {

  private final InstantValue arrivalTime = InstantValue.from(Instant.EPOCH, Duration.ofSeconds(1));
  private final DurationValue travelTime = DurationValue.from(Duration.ofSeconds(1), Duration.ZERO);

  @Test
  void createArrivalTimeMeasurementValue_measurement() {
    assertNotNull(ArrivalTimeMeasurementValue.fromFeatureMeasurement(arrivalTime),
      "There was a problem creating ArrivalTimeMeasurementValue");
  }

  @Test
  void createArrivalTimeMeasurementValue_prediction() {
    assertNotNull(ArrivalTimeMeasurementValue.fromFeaturePrediction(arrivalTime, travelTime),
      "There was a problem creating ArrivalTimeMeasurementValue");
  }
}
