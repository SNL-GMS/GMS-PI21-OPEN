package gms.shared.signaldetection.coi.values;

import gms.shared.signaldetection.coi.types.FirstMotionType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirstMotionMeasurementValueTests {

  private static final String CONFIDENCE_ERR = "FirstMotionMeasurementValue did not check for confidence out of range";
  private static final String CREATE_ERR = "There was a problem creating FirstMotionMeasurementValue";

  private final FirstMotionType value = FirstMotionType.DILATION;
  private final Optional<Double> confidence = Optional.of(1.0);
  private final Optional<Double> illegalConfidence = Optional.of(2.0);
  private final Instant referenceTime = Instant.EPOCH;

  @Test
  void checkIllegalConfidence_measurement() {
    assertThrows(IllegalStateException.class,
      () -> FirstMotionMeasurementValue.fromFeatureMeasurement(value,
        illegalConfidence, referenceTime),
      CONFIDENCE_ERR);
  }

  @Test
  void createFirstMotionMeasurementValue_measurement() {
    assertNotNull(FirstMotionMeasurementValue.fromFeatureMeasurement(value,
      confidence, referenceTime), CREATE_ERR);
  }

  @Test
  void checkIllegalConfidence_prediction() {

    assertThrows(IllegalStateException.class,
      () -> {
        FirstMotionMeasurementValue.fromFeaturePrediction(value, illegalConfidence);
      },
      CONFIDENCE_ERR);
  }

  @Test
  void createFirstMotionMeasurementValue_prediction() {
    assertNotNull(FirstMotionMeasurementValue.fromFeaturePrediction(value, confidence), CREATE_ERR);
  }
}
