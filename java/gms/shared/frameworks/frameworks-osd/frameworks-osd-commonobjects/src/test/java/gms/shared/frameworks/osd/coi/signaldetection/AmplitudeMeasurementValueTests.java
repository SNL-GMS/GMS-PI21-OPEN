package gms.shared.frameworks.osd.coi.signaldetection;

import gms.shared.frameworks.osd.coi.DoubleValue;
import gms.shared.frameworks.osd.coi.Units;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AmplitudeMeasurementValueTests {

  private final Duration duration = Duration.ofSeconds(1);
  private final Instant instant = Instant.EPOCH;
  private final DoubleValue doubleValue = DoubleValue.from(0.0, 0.0, Units.NANOMETERS);

  @Test
  void checkZeroPeriod() {
    assertThrows(IllegalStateException.class,
      () -> AmplitudeMeasurementValue.from(instant, Duration.ZERO, doubleValue),
      "AmplitudeMeasurementValue did not check for a zero period");
  }

  @Test
  void createAmplitudeMeasurementValue() {
    assertNotNull(AmplitudeMeasurementValue.from(instant, duration, doubleValue),
      "There was a problem creating AmplitudeMeasurementValue");
  }

}