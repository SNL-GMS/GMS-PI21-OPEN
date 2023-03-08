package gms.shared.frameworks.osd.coi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

class StationProcessingIntervalTest {

  @Test
  void testFromValidation() {
    UUID defaultId = new UUID(0, 0);
    Instant defaultStart = Instant.EPOCH;
    Instant defaultEnd = defaultStart.plusSeconds(30);
    List<UUID> idList = List.of(defaultId);
    Assertions.assertAll("From method validation",
      () -> Assertions.assertThrows(IllegalArgumentException.class,
        () -> StationProcessingInterval
          .from(defaultId, defaultId, Collections.EMPTY_LIST, defaultStart, defaultEnd),
        "Expected validation of non-empty processingIds"),
      () -> Assertions.assertThrows(IllegalArgumentException.class,
        () -> StationProcessingInterval.from(defaultId, defaultId, idList,
          defaultEnd, defaultStart),
        "Expected validation of start time before end time"));
  }
}
