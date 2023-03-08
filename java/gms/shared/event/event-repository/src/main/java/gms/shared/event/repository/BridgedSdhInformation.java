package gms.shared.event.repository;

import com.google.auto.value.AutoValue;
import gms.shared.event.coi.EventHypothesis;
import gms.shared.event.dao.ArInfoDao;
import gms.shared.event.dao.StaMagDao;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesis;
import gms.shared.signaldetection.dao.css.AssocDao;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;

/**
 * A collection of correlated Signal Detection Hypothesis Dao and COI information used to help completely bridge an
 * {@link EventHypothesis} from legacy data.
 */
@AutoValue
public abstract class BridgedSdhInformation {

  public abstract Optional<SignalDetectionHypothesis> getSignalDetectionHypothesis();

  public abstract AssocDao getAssocDao();

  public abstract Optional<ArInfoDao> getArInfoDao();

  public abstract Set<StaMagDao> getStaMagDaos();

  public Stream<StaMagDao> staMagDaos() {
    return getStaMagDaos().stream();
  }

  public static Builder builder() {
    return new AutoValue_BridgedSdhInformation.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setSignalDetectionHypothesis(SignalDetectionHypothesis signalDetectionHypothesis);

    public abstract Builder setAssocDao(AssocDao assocDao);

    public abstract Builder setArInfoDao(@Nullable ArInfoDao arInfoDao);

    public abstract Builder setStaMagDaos(Collection<StaMagDao> staMagDaos);

    abstract BridgedSdhInformation autoBuild();

    public BridgedSdhInformation build() {
      var sdhInfo = autoBuild();

      var arInfoOpt = sdhInfo.getArInfoDao();
      arInfoOpt.ifPresent(arInfoDao -> checkState(sdhInfo.getAssocDao().getId().getArrivalId() == arInfoDao.getArrivalId(),
        "ArInfoDao's arid must match AssocDao's arid"));

      checkState(sdhInfo.staMagDaos().allMatch(staMagDao -> sdhInfo.getAssocDao().getId().getArrivalId() == staMagDao.getArrivalId()),
        "All StaMagDaos arid must match AssocDao's arid");
      return sdhInfo;
    }


  }
}
