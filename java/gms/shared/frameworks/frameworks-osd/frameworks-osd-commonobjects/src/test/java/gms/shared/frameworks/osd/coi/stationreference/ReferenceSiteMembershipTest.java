package gms.shared.frameworks.osd.coi.stationreference;


import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReferenceSiteMembershipTest {

  private final UUID id = UUID.fromString("1712f988-ff83-4f3d-a832-a82a040221d9");
  private final UUID siteId = UUID.fromString("6712f988-ff83-4f3d-a832-a82a04022123");
  private final String comment = "Question everything.";
  private final Instant actualTime = Instant.now().minusSeconds(100);
  private final Instant systemTime = Instant.now().minusNanos(1);
  private final StatusType status = StatusType.ACTIVE;

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(StationReferenceTestFixtures.REFERENCE_SITE_MEMBERSHIP,
      ReferenceSiteMembership.class);
  }

  @Test
  void testReferenceSiteMembCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
      ReferenceSiteMembership.class, "create", comment, actualTime,
      systemTime, siteId, StationReferenceTestFixtures.CHANNEL_NAME, status);
  }

  @Test
  void testReferenceSiteMembFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
      ReferenceSiteMembership.class, "from", id, comment, actualTime, systemTime,
      siteId, StationReferenceTestFixtures.CHANNEL_NAME, status);

  }

  /**
   * Test that arguments are saved correctly.
   */
  @Test
  void testReferenceSiteMembCreate() {
    ReferenceSiteMembership m = ReferenceSiteMembership.create(comment, actualTime,
      systemTime, siteId, StationReferenceTestFixtures.CHANNEL_NAME, status);
    final UUID expectedId = UUID.nameUUIDFromBytes(
      (m.getSiteId().toString() + m.getChannelName()
        + m.getStatus() + m.getActualChangeTime()).getBytes(StandardCharsets.UTF_16LE));
    assertEquals(expectedId, m.getId());
    assertEquals(comment, m.getComment());
    assertEquals(actualTime, m.getActualChangeTime());
    assertEquals(systemTime, m.getSystemChangeTime());
    assertEquals(siteId, m.getSiteId());
    assertEquals(StationReferenceTestFixtures.CHANNEL_NAME, m.getChannelName());
    assertEquals(status, m.getStatus());
  }


  /**
   * Test that arguments are saved correctly.  We check that the name was converted to uppercase.
   */
  @Test
  void testReferenceSiteMembFrom() {
    ReferenceSiteMembership alias = ReferenceSiteMembership.from(id, comment, actualTime,
      systemTime, siteId, StationReferenceTestFixtures.CHANNEL_NAME, status);
    assertEquals(id, alias.getId());
    assertEquals(comment, alias.getComment());
    assertEquals(actualTime, alias.getActualChangeTime());
    assertEquals(systemTime, alias.getSystemChangeTime());
    assertEquals(siteId, alias.getSiteId());
    assertEquals(StationReferenceTestFixtures.CHANNEL_NAME, alias.getChannelName());
    assertEquals(status, alias.getStatus());
  }


}
