package gms.shared.frameworks.osd.coi.stationreference;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReferenceDigitizerMembershipTest {

  private final UUID id = UUID.fromString("1712f988-ff83-4f3d-a832-a82a040221d9");
  private final UUID channelId = UUID.fromString("6712f988-ff83-4f3d-a832-a82a04022123");
  private final UUID digitizerId = UUID.fromString("9812f988-ff83-4f3d-a832-a82a04022154");
  private final String comment = "Question everything.";
  private final Instant actualTime = Instant.now().minusSeconds(100);
  private final Instant systemTime = Instant.now().minusNanos(1);
  private final StatusType status = StatusType.ACTIVE;

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(StationReferenceTestFixtures.REFERENCE_DIGITIZER_MEMBERSHIP,
      ReferenceDigitizerMembership.class);
  }

  @Test
  void testReferenceDigitizerMembCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
      ReferenceDigitizerMembership.class, "create", comment, actualTime,
      systemTime, digitizerId, channelId, status);
  }

  @Test
  void testReferenceDigitizerMembFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
      ReferenceDigitizerMembership.class, "from", id, comment, actualTime, systemTime,
      digitizerId, channelId, status);

  }

  /**
   * Test that arguments are saved correctly.
   */
  @Test
  void testReferenceDigitizerMembershipCreate() {
    ReferenceDigitizerMembership m = ReferenceDigitizerMembership.create(comment, actualTime,
      systemTime, digitizerId, channelId, status);
    final UUID expectedId = UUID.nameUUIDFromBytes(
      (m.getChannelId().toString() + m.getDigitizerId()
        + m.getStatus() + m.getActualChangeTime()).getBytes(StandardCharsets.UTF_16LE));
    assertEquals(expectedId, m.getId());
    assertEquals(comment, m.getComment());
    assertEquals(actualTime, m.getActualChangeTime());
    assertEquals(systemTime, m.getSystemChangeTime());
    assertEquals(channelId, m.getChannelId());
    assertEquals(digitizerId, m.getDigitizerId());
  }


  /**
   * Test that arguments are saved correctly.  We check that the name was converted to uppercase.
   */
  @Test
  void testReferenceDigitizerMembFrom() {
    ReferenceDigitizerMembership alias = ReferenceDigitizerMembership.from(id, comment, actualTime,
      systemTime, digitizerId, channelId, status);
    assertEquals(id, alias.getId());
    assertEquals(comment, alias.getComment());
    assertEquals(actualTime, alias.getActualChangeTime());
    assertEquals(systemTime, alias.getSystemChangeTime());
    assertEquals(digitizerId, alias.getDigitizerId());
    assertEquals(channelId, alias.getChannelId());
    assertEquals(status, alias.getStatus());
  }

}
