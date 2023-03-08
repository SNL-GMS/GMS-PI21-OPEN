package gms.shared.stationdefinition.dao.css;

import java.time.Instant;
import java.util.Optional;

/**
 * @author agonza5
 */
public class SiteChanAndSurroundingDates {
  private final SiteChanDao siteChanDao;
  private final Optional<Instant> previousOffDate;
  private final Optional<Instant> nextOnDate;

  public SiteChanAndSurroundingDates(SiteChanDao siteChanDao,
    Instant previousOffDate, Instant nextOnDate) {
    this.siteChanDao = siteChanDao;
    this.previousOffDate = Optional.ofNullable(previousOffDate);
    this.nextOnDate = Optional.ofNullable(nextOnDate);
  }

  public SiteChanDao getSiteChanDao() {
    return siteChanDao;
  }

  public Optional<Instant> getPreviousOffDate() {
    return previousOffDate;
  }

  public Optional<Instant> getNextOnDate() {
    return nextOnDate;
  }
}
