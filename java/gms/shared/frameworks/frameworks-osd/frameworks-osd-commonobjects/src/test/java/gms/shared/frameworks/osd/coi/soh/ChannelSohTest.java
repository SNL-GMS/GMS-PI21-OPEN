package gms.shared.frameworks.osd.coi.soh;

import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.CHANNEL;

class ChannelSohTest {

  @Test
  void testChannelSohValidation() {

    Set<SohMonitorValueAndStatus<?>> invalidSetOfSmvs = Set.of(
      PercentSohMonitorValueAndStatus.from(
        100.0,
        SohStatus.GOOD,
        SohMonitorType.MISSING),
      PercentSohMonitorValueAndStatus.from(
        90.0,
        SohStatus.BAD,
        SohMonitorType.MISSING)
    );

    var channelName = CHANNEL.getName();

    Assertions.assertThrows(IllegalArgumentException.class, () -> 
      ChannelSoh.from(
        channelName,
        SohStatus.BAD,
        invalidSetOfSmvs
      ));
  }

}
