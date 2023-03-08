package gms.shared.stationdefinition.dao.css;

import java.time.Instant;
import java.util.Optional;

public class SiteAndSurroundingDates {
  private final SiteDao siteDao;
  private final Optional<Instant> previousOffDate;
  private final Optional<Instant> nextOnDate;

  public SiteAndSurroundingDates(SiteDao siteDao, Instant previousOffDate, Instant nextOnDate) {
    this.siteDao = siteDao;
    this.previousOffDate = Optional.ofNullable(previousOffDate);
    this.nextOnDate = Optional.ofNullable(nextOnDate);
  }

  public SiteDao getSiteDao() {
    return siteDao;
  }

  public Optional<Instant> getPreviousOffDate() {
    return previousOffDate;
  }

  public Optional<Instant> getNextOnDate() {
    return nextOnDate;
  }
}