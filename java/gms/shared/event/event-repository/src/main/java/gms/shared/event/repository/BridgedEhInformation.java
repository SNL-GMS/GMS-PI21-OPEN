package gms.shared.event.repository;

import com.google.auto.value.AutoValue;
import gms.shared.event.coi.EventHypothesis;
import gms.shared.event.dao.EventControlDao;
import gms.shared.event.dao.GaTagDao;
import gms.shared.event.dao.NetMagDao;
import gms.shared.event.dao.OrigerrDao;
import gms.shared.event.dao.OriginDao;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;

/**
 * A collection of correlated Event Hypothesis Dao and COI information used to help completely bridge an
 * {@link EventHypothesis} from legacy data.
 */
@AutoValue
public abstract class BridgedEhInformation {

  public abstract Optional<EventStages> getEventStages();

  public abstract OriginDao getOriginDao();

  public abstract OrigerrDao getOrigerrDao();

  public abstract Optional<EventControlDao> getEventControlDao();

  public abstract Optional<GaTagDao> getGaTagDao();

  public abstract Set<NetMagDao> getNetMagDaos();

  public Stream<NetMagDao> netMagDaos() {
    return getNetMagDaos().stream();
  }

  public abstract Set<EventHypothesis.Id> getParentEventHypotheses();

  public static Builder builder() {
    return new AutoValue_BridgedEhInformation.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setEventStages(EventStages eventStages);

    public abstract Builder setOriginDao(OriginDao originDao);

    public abstract Builder setOrigerrDao(OrigerrDao origerrDao);

    public abstract Builder setEventControlDao(EventControlDao eventControlDao);

    public abstract Builder setGaTagDao(GaTagDao gaTagDao);

    public abstract Builder setNetMagDaos(Collection<NetMagDao> netMagDaos);

    public abstract Builder setParentEventHypotheses(Collection<EventHypothesis.Id> parentEventHypotheses);

    abstract BridgedEhInformation autoBuild();

    public BridgedEhInformation build() {
      var ehInfo = autoBuild();

      checkState(ehInfo.getOriginDao().getOriginId() == ehInfo.getOrigerrDao().getOriginId(),
        "originDao and origerrDao originId must match");
      ehInfo.getEventControlDao().ifPresent(eventControlDao ->
        checkState(ehInfo.getOriginDao().getOriginId() == eventControlDao.getOriginId(),
          "originDao and eventControlDao originId must match"));
      checkState(ehInfo.netMagDaos().allMatch(netMag -> ehInfo.getOriginDao().getOriginId() == netMag.getOriginId()),
        "originDao and netMagDaos originIds must match");
      checkState(ehInfo.getGaTagDao().stream().allMatch(gaTagDao -> "o".equals(gaTagDao.getObjectType())),
        "Analyst rejected GaTagDao must be of object type 'o'");

      return ehInfo;
    }
  }
}
