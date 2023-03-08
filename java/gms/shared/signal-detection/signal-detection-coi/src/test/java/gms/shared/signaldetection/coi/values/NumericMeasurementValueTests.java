package gms.shared.signaldetection.coi.values;

import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.stationdefinition.coi.utils.Units;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class NumericMeasurementValueTests {

  private final Instant referenceTime = Instant.EPOCH;
  private final DoubleValue measuredValue = DoubleValue.from(0.0, Optional.of(0.0), Units.NANOMETERS);
  private final DoubleValue predictedValue = DoubleValue.from(0.0, Optional.of(0.0), Units.NANOMETERS);


  @Test
  void createNumericMeasurementValue_measurement() {
    assertNotNull(NumericMeasurementValue.fromFeatureMeasurement(referenceTime, measuredValue),
      "There was a problem creating NumericMeasurementValue");
  }

  @Test
  void createNumericMeasurementValue_prediction() {
    assertNotNull(NumericMeasurementValue.fromFeaturePrediction(predictedValue),
      "There was a problem creating NumericMeasurementValue");
  }
}
