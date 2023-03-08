package gms.testtools.simulators.bridgeddatasourcesimulator.api;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.util.SourceInterval;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;

class SourceIntervalTest {

  @Test
  void testSerialization() throws IOException {

    var sourceInterval = SourceInterval.getBuilder()
      .setIntervalIdentifier(1234)
      .setAuthor("Author C. Clarke")
      .setEndTime(99.0)
      .setLastModificationDate(Instant.now())
      .setLoadDate(Instant.now())
      .setName("Name Nameson")
      .setPercentAvailable(50.0)
      .setProcessEndDate(Instant.now())
      .setProcessStartDate(Instant.now())
      .setState("My State")
      .setTime(99.0)
      .setType("Type")
      .build();

    TestUtilities.testSerialization(sourceInterval, SourceInterval.class);
  }
}
