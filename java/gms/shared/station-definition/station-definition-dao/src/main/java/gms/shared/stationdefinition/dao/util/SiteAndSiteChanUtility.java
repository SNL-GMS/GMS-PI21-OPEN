package gms.shared.stationdefinition.dao.util;

import gms.shared.stationdefinition.dao.css.SiteAndSurroundingDates;
import gms.shared.stationdefinition.dao.css.SiteChanAndSurroundingDates;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteDao;

import java.time.temporal.ChronoUnit;

/**
 * Site and SiteChan utility for adjusting DAO properties
 */
public class SiteAndSiteChanUtility {

  private SiteAndSiteChanUtility() {
  }

  /**
   * Update site dao on and off dates using the {@link SiteAndSurroundingDates}
   *
   * @param siteAndSurroundingDates {@link SiteAndSurroundingDates}
   * @return {@link SiteDao}
   */
  public static SiteDao updateSiteDaoOnAndOffDates(SiteAndSurroundingDates siteAndSurroundingDates) {
    siteAndSurroundingDates.getNextOnDate().ifPresent(nextOnDate -> {
      if (siteAndSurroundingDates.getSiteDao().getOffDate().equals(nextOnDate)) {
        siteAndSurroundingDates.getSiteDao().setOffDate(nextOnDate.plus(12,
          ChronoUnit.HOURS).minus(1, ChronoUnit.MILLIS));
      }
    });

    siteAndSurroundingDates.getPreviousOffDate().ifPresent(previousOffDate -> {
      if (siteAndSurroundingDates.getSiteDao().getId().getOnDate().equals(previousOffDate)) {
        siteAndSurroundingDates.getSiteDao().getId().setOnDate(previousOffDate.plus(12, ChronoUnit.HOURS));
      }
    });

    return siteAndSurroundingDates.getSiteDao();
  }

  /**
   * Update site chan dao on and off dates using
   * {@link SiteChanAndSurroundingDates}
   *
   * @param siteChanAndSurroundingDates {@link SiteChanAndSurroundingDates}
   * @return {@link SiteChanDao}
   */
  public static SiteChanDao updateSiteChanDaoOnAndOffDates(SiteChanAndSurroundingDates siteChanAndSurroundingDates) {
    // set site chan dao off date
    siteChanAndSurroundingDates.getNextOnDate().ifPresent(nextOnDate -> {
      if (siteChanAndSurroundingDates.getSiteChanDao().getOffDate().equals(nextOnDate)) {
        siteChanAndSurroundingDates.getSiteChanDao().setOffDate(nextOnDate.plus(12,
          ChronoUnit.HOURS).minus(1, ChronoUnit.MILLIS));
      }
    });

    // set site chan dao on date
    siteChanAndSurroundingDates.getPreviousOffDate().ifPresent(previousOffDate -> {
      if (siteChanAndSurroundingDates.getSiteChanDao().getId().getOnDate().equals(previousOffDate)) {
        siteChanAndSurroundingDates.getSiteChanDao().getId().setOnDate(previousOffDate.plus(12,
          ChronoUnit.HOURS));
      }
    });

    return siteChanAndSurroundingDates.getSiteChanDao();
  }
}
