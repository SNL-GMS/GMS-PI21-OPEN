package gms.shared.stationdefinition.dao.css;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_CHAN_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.OFFDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SiteChanAndSurroundingDatesTest {
  private static SiteChanAndSurroundingDates siteChanAndSurroundingDates;

  @BeforeEach
  void setUp() {
    siteChanAndSurroundingDates = new SiteChanAndSurroundingDates(SITE_CHAN_DAO_1, OFFDATE, ONDATE);
  }

  /**
   * Test of getSiteChanDao method, of class SiteChanAndSurroundingDates.
   */
  @Test
  void testGetSiteChanDao() {
    SiteChanDao result = siteChanAndSurroundingDates.getSiteChanDao();
    assertEquals(SITE_CHAN_DAO_1, result);
  }

  /**
   * Test of getPreviousOffDate method, of class SiteChanAndSurroundingDates.
   */
  @Test
  void testGetPreviousOffDate() {
    Optional<Instant> result = siteChanAndSurroundingDates.getPreviousOffDate();
    assertTrue(result.isPresent());
    assertEquals(OFFDATE, result.get());
  }

  /**
   * Test of getNextOnDate method, of class SiteChanAndSurroundingDates.
   */
  @Test
  void testGetNextOnDate() {
    Optional<Instant> result = siteChanAndSurroundingDates.getNextOnDate();
    assertTrue(result.isPresent());
    assertEquals(ONDATE, result.get());
  }
}
