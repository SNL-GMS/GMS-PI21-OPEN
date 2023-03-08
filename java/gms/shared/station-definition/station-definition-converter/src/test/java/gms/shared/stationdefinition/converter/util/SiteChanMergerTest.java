package gms.shared.stationdefinition.converter.util;

import gms.shared.stationdefinition.dao.css.SiteChanDao;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.createSiteChanDao;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHAN1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHANID_1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHAN_PARAM_MAP;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.OFFDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.OFFDATE2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.OFFDATE3;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STA1;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SiteChanMergerTest {

  @Test
  void mergeSiteChans() {
    var SITE_CHAN_DAO_1 = createSiteChanDao(STA1, CHAN1, ONDATE, OFFDATE, CHAN_PARAM_MAP,
      CHANID_1);
    var SITE_CHAN_DAO_2 = createSiteChanDao(STA1, CHAN1, OFFDATE, OFFDATE2, CHAN_PARAM_MAP,
      CHANID_1);
    var SITE_CHAN_DAO_3 = createSiteChanDao(STA1, CHAN1, OFFDATE2, OFFDATE3, CHAN_PARAM_MAP,
      CHANID_1);
    var actual = List.of(createSiteChanDao(STA1, CHAN1, ONDATE, OFFDATE3, CHAN_PARAM_MAP,
      CHANID_1));

    List<SiteChanDao> expected = SiteChanMerger.mergeSiteChans(Map.of("",
      List.of(SITE_CHAN_DAO_1, SITE_CHAN_DAO_2, SITE_CHAN_DAO_3)));

    assertEquals(expected.size(), actual.size());
    assertTrue(expected.containsAll(actual));
  }

  @Test
  void getPossibleVersionTimes() {
    var SITE_CHAN_DAO_1 = createSiteChanDao(STA1, CHAN1, ONDATE, OFFDATE, CHAN_PARAM_MAP,
      CHANID_1);
    var SITE_CHAN_DAO_2 = createSiteChanDao(STA1, CHAN1, OFFDATE2, OFFDATE3, CHAN_PARAM_MAP,
      CHANID_1);
    var actual = List.of(ONDATE, OFFDATE, OFFDATE2, OFFDATE3);

    var expected = SiteChanMerger.getPossibleVersionTimes(
      List.of(SITE_CHAN_DAO_1, SITE_CHAN_DAO_2));

    assertEquals(expected.size(), actual.size());
    assertTrue(expected.containsAll(actual));
  }
}