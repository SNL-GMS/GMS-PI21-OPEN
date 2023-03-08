package gms.shared.signaldetection.coi.values;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DurationMeasurementValueTests {

  private final InstantValue startTime = InstantValue.from(Instant.EPOCH, Duration.ofSeconds(1));
  private final DurationValue duration = DurationValue.from(Duration.ofSeconds(1), Duration.ZERO);

  @Test
  void createDurationMeasurementValue_measurement() {
    assertNotNull(DurationMeasurementValue.from(startTime, duration),
      "There was a problem creating DurationMeasurementValue");
  }
}
