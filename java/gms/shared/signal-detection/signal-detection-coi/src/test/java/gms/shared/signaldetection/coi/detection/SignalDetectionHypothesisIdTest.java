package gms.shared.signaldetection.coi.detection;

import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

class SignalDetectionHypothesisIdTest {

  @Test
  void testSerialization() throws IOException {
    SignalDetectionHypothesisId id = SignalDetectionHypothesisId.from(UUID.randomUUID(), UUID.randomUUID());
    TestUtilities.assertSerializes(id, SignalDetectionHypothesisId.class);
  }

}