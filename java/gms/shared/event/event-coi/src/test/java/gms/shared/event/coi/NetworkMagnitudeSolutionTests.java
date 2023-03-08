package gms.shared.event.coi;

import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

class NetworkMagnitudeSolutionTests {
  private static final MagnitudeType TYPE = MagnitudeType.MB;
  private static final DoubleValue MAGNITUDE = DoubleValue.from(1.0, Optional.empty(), Units.MAGNITUDE);

  @Test
  void testSerialize() throws IOException {
    NetworkMagnitudeSolution expected = NetworkMagnitudeSolution.builder()
      .setType(TYPE)
      .setMagnitude(MAGNITUDE)
      .setNetworkMagnitudeBehaviors(Collections.emptyList())
      .build();

    TestUtilities.assertSerializes(expected, NetworkMagnitudeSolution.class);
  }

  @Test
  void testBuildInvalidMagnitude() {
    var nmsBuilder = NetworkMagnitudeSolution.builder()
      .setType(TYPE)
      .setMagnitude(DoubleValue.from(11, Optional.empty(), Units.MAGNITUDE))
      .setNetworkMagnitudeBehaviors(Collections.emptyList());

    assertThrows(IllegalStateException.class, nmsBuilder::build);
  }
}