package gms.shared.utilities.reactor;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmitFailureHandlerUtilityTest {

  @Test
  void testOnEmitFailure() {
    assertTrue(EmitFailureHandlerUtility.getInstance().onEmitFailure(SignalType.CANCEL, Sinks.EmitResult.FAIL_NON_SERIALIZED));
    assertFalse(EmitFailureHandlerUtility.getInstance().onEmitFailure(SignalType.CANCEL, Sinks.EmitResult.FAIL_OVERFLOW));
  }
}
