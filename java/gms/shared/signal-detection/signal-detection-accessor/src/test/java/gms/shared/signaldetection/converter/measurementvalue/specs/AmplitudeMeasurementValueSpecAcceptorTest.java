package gms.shared.signaldetection.converter.measurementvalue.specs;

import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.coi.values.AmplitudeMeasurementValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.AMPLITUDE_DAO_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_1;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class AmplitudeMeasurementValueSpecAcceptorTest {

  protected AmplitudeMeasurementValueSpecAcceptor specAcceptor;

  @Mock
  private MeasurementValueSpecVisitor<AmplitudeMeasurementValue> specVisitor;

  @BeforeEach
  void setup() {
    specAcceptor = AmplitudeMeasurementValueSpecAcceptor.create();
  }

  @Test
  void testAccept() {

    assertDoesNotThrow(() ->
      specAcceptor.accept(specVisitor, FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2,
        ARRIVAL_1, Optional.empty(), Optional.of(AMPLITUDE_DAO_1)));
  }
}