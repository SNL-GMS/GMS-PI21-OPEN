package gms.shared.frameworks.osd.coi.stationreference;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReferenceSiteTest {

  final UUID versionId = UUID.nameUUIDFromBytes(
    (StationReferenceTestFixtures.SITE_NAME + StationReferenceTestFixtures.LATITUDE + StationReferenceTestFixtures.LONGITUDE +
      StationReferenceTestFixtures.ELEVATION + StationReferenceTestFixtures.ACTUAL_TIME)
      .getBytes(StandardCharsets.UTF_16LE));

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(StationReferenceTestFixtures.site, ReferenceSite.class);
  }

  @Test
  void testAddAlias() {
    ReferenceSite site = ReferenceSite.builder()
      .setName(StationReferenceTestFixtures.SITE_NAME)
      .setDescription(StationReferenceTestFixtures.DESCRIPTION)
      .setSource(StationReferenceTestFixtures.INFORMATION_SOURCE)
      .setComment(StationReferenceTestFixtures.COMMENT)
      .setLatitude(StationReferenceTestFixtures.LATITUDE)
      .setLongitude(StationReferenceTestFixtures.LONGITUDE)
      .setElevation(StationReferenceTestFixtures.ELEVATION)
      .setActualChangeTime(StationReferenceTestFixtures.ACTUAL_TIME)
      .setSystemChangeTime(StationReferenceTestFixtures.SYSTEM_TIME)
      .setActive(true)
      .setPosition(StationReferenceTestFixtures.POSITION)
      .setAliases(new ArrayList<>())
      .build();
    site.getAliases().add(StationReferenceTestFixtures.SITE_ALIAS);
    assertEquals(1, site.getAliases().size());
    assertEquals(StationReferenceTestFixtures.SITE_ALIAS, site.getAliases().get(0));
  }
}
