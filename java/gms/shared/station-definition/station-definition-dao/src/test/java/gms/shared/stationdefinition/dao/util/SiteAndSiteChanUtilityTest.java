package gms.shared.stationdefinition.dao.util;

import gms.shared.stationdefinition.dao.css.SiteAndSurroundingDates;
import gms.shared.stationdefinition.dao.css.SiteChanAndSurroundingDates;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteDao;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_CHAN_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.getTestSiteAndSurroundingDates;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.getTestSiteChanAndSurroundingDates;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SiteAndSiteChanUtilityTest {

  /**
   * Test of updateSiteDaoOnAndOffDates method, of class SiteAndSiteChanUtility.
   */
  @Test
  void testUpdateSiteDaoOnAndOffDates() {
    SiteAndSurroundingDates siteAndSurroundingDates
      = getTestSiteAndSurroundingDates().get(0);
    var currOnDate = SITE_DAO_1.getId().getOnDate();
    SiteDao expectedSiteDao = SITE_DAO_1;
    expectedSiteDao.getId().setOnDate(currOnDate.plus(12, ChronoUnit.HOURS));

    SiteDao result = SiteAndSiteChanUtility.updateSiteDaoOnAndOffDates(siteAndSurroundingDates);
    assertEquals(expectedSiteDao, result);
  }

  /**
   * Test of updateSiteChanDaoOnAndOffDates method, of class
   * SiteAndSiteChanUtility.
   */
  @Test
  void testUpdateSiteChanDaoOnAndOffDates() {
    SiteChanAndSurroundingDates siteChanAndSurroundingDates
      = getTestSiteChanAndSurroundingDates().get(0);
    var currOnDate = SITE_CHAN_DAO_1.getId().getOnDate();
    SiteChanDao expectedSiteChanDao = SITE_CHAN_DAO_1;
    expectedSiteChanDao.getId().setOnDate(currOnDate.plus(12, ChronoUnit.HOURS));
    SiteChanDao result = SiteAndSiteChanUtility.updateSiteChanDaoOnAndOffDates(siteChanAndSurroundingDates);
    assertEquals(expectedSiteChanDao, result);
  }
}
