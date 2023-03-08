package gms.shared.frameworks.osd.coi.stationreference;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ReferenceAliasTest {

  private final UUID id = UUID.fromString("8052f988-ff83-4f3d-a832-a82a04022119");
  private final String name = "FOO";
  private final String comment = "This is a comment.";
  private final StatusType status = StatusType.INACTIVE;
  private final Instant actualTime = Instant.now().minusSeconds(10);
  private final Instant systemTime = Instant.now();

  @Test
  void testSerialization() throws Exception {
    TestUtilities
      .testSerialization(StationReferenceTestFixtures.STATION_ALIAS, ReferenceAlias.class);
  }

  @Test
  void testReferenceStationAliasCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
      ReferenceAlias.class, "create",
      name, status, comment, actualTime, systemTime);
  }

  @Test
  void testReferenceStationAliasFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
      ReferenceAlias.class, "from", id, name,
      status, comment, actualTime, systemTime);
  }

  @Test
  void testReferenceStationAliasCreate() {
    ReferenceAlias alias = ReferenceAlias.create(name, status, comment,
      actualTime, systemTime);
    assertNotEquals(id, alias.getId());
    assertEquals(name, alias.getName());
    assertEquals(status, alias.getStatus());
    assertEquals(comment, alias.getComment());
    assertEquals(actualTime, alias.getActualChangeTime());
    assertEquals(systemTime, alias.getSystemChangeTime());
  }

  @Test
  void testReferenceStationAliasFrom() {
    ReferenceAlias alias = ReferenceAlias.from(id, name, status, comment,
      actualTime, systemTime);
    assertEquals(id, alias.getId());
    assertEquals(name, alias.getName());
    assertEquals(status, alias.getStatus());
    assertEquals(comment, alias.getComment());
    assertEquals(actualTime, alias.getActualChangeTime());
    assertEquals(systemTime, alias.getSystemChangeTime());
  }
}
