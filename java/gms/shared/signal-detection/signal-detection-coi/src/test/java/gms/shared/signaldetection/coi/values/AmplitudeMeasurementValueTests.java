package gms.shared.signaldetection.coi.values;

import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.stationdefinition.coi.utils.Units;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AmplitudeMeasurementValueTests {

  private static final String ZERO_PERIOD_ERR = "AmplitudeMeasurementValue period should be non-zero";
  private static final String CREATE_ERR = "There was a problem creating AmplitudeMeasurementValue";

  private final DoubleValue amplitude = DoubleValue.from(0.0, Optional.of(0.0), Units.NANOMETERS);
  private final Duration period = Duration.ofSeconds(1);
  private final Instant measurementTime = Instant.EPOCH;
  private final Instant measurementWindowStart = Instant.EPOCH;
  private final Duration measurementWindowDuration = Duration.ofSeconds(1);
  private final Boolean isClipped = true;

  @Test
  void checkZeroPeriod_measurement() {
    assertThrows(IllegalStateException.class,
      () -> AmplitudeMeasurementValue.fromFeatureMeasurement(amplitude, Duration.ZERO,
        measurementTime, measurementWindowStart, measurementWindowDuration, isClipped),
      ZERO_PERIOD_ERR);
  }

  @Test
  void createAmplitudeMeasurementValue_measurement() {
    assertNotNull(AmplitudeMeasurementValue.fromFeatureMeasurement(amplitude, period,
        measurementTime, measurementWindowStart, measurementWindowDuration, isClipped),
      CREATE_ERR);
  }

  @Test
  void checkZeroPeriod_prediction() {
    assertThrows(IllegalStateException.class,
      () -> AmplitudeMeasurementValue.fromFeaturePrediction(amplitude, Duration.ZERO),
      ZERO_PERIOD_ERR);
  }

  @Test
  void createAmplitudeMeasurementValue_prediction() {
    assertNotNull(AmplitudeMeasurementValue.fromFeaturePrediction(amplitude, period),
      CREATE_ERR);
  }
}