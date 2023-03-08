package gms.shared.stationdefinition.dao.css;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.OFFDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SiteAndSurroundingDatesTest {
  private static SiteAndSurroundingDates siteAndSurroundingDates;

  @BeforeEach
  public void setUp() {
    siteAndSurroundingDates = new SiteAndSurroundingDates(SITE_DAO_1, OFFDATE, ONDATE);
  }

  /**
   * Test of getSiteDao method, of class SiteAndSurroundingDates.
   */
  @Test
  void testGetSiteDao() {
    SiteDao result = siteAndSurroundingDates.getSiteDao();
    assertEquals(SITE_DAO_1, result);
  }

  /**
   * Test of getPreviousOffDate method, of class SiteAndSurroundingDates.
   */
  @Test
  void testGetPreviousOffDate() {
    Optional<Instant> result = siteAndSurroundingDates.getPreviousOffDate();
    assertTrue(result.isPresent());
    assertEquals(OFFDATE, result.get());
  }

  /**
   * Test of getNextOnDate method, of class SiteAndSurroundingDates.
   */
  @Test
  void testGetNextOnDate() {
    Optional<Instant> result = siteAndSurroundingDates.getNextOnDate();
    assertTrue(result.isPresent());
    assertEquals(ONDATE, result.get());
  }
}
