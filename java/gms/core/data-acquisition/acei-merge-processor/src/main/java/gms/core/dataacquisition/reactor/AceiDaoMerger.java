package gms.core.dataacquisition.reactor;

import com.google.common.annotations.VisibleForTesting;
import gms.core.dataacquisition.reactor.util.AceiDaoMergeChecker;
import gms.core.dataacquisition.reactor.util.MergeChecker;
import gms.core.dataacquisition.reactor.util.ToleranceResolver;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueBooleanDao;
import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueDao;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

/**
 * Helper class that conducts merging of {@link AcquiredChannelEnvironmentIssueBooleanDao}s together
 */
public class AceiDaoMerger {

  private final MergeChecker<AcquiredChannelEnvironmentIssueBooleanDao> mergeChecker;

  private AceiDaoMerger(MergeChecker<AcquiredChannelEnvironmentIssueBooleanDao> mergeChecker) {
    this.mergeChecker = mergeChecker;
  }

  public static AceiDaoMerger create(
    MergeChecker<AcquiredChannelEnvironmentIssueBooleanDao> aceiMergeChecker) {
    return new AceiDaoMerger(checkNotNull(aceiMergeChecker));
  }

  public static AceiDaoMerger create(ConfigurationConsumerUtility config) {
    return new AceiDaoMerger(AceiDaoMergeChecker.create(checkNotNull(config)));
  }

  public static AceiDaoMerger create(ToleranceResolver toleranceResolver) {
    return new AceiDaoMerger(AceiDaoMergeChecker.create(checkNotNull(toleranceResolver)));
  }

  /**
   * Merge together all provided boolean DAOs, being aware of managed entities associated with the provided {@link EntityManager}
   * @param aceiDaos ACEI DAOs to merge together. Can consist of both managed and unmanaged entities
   * @param entityManager Entity manager used to identify, update, and delete managed entities to be merged
   * @return Resulting merged DAOs from the merging process, both managed and unmanaged. All "redundant" managed DAOs
   * from the merging process are deleted through the provided {@link EntityManager}
   */
  public Set<AcquiredChannelEnvironmentIssueBooleanDao> mergeAll(Collection<AcquiredChannelEnvironmentIssueBooleanDao> aceiDaos, EntityManager entityManager) {
    Map<String, List<AcquiredChannelEnvironmentIssueBooleanDao>> aceisByChannelAndType = aceiDaos.stream()
      .collect(groupingBy(dao -> AceiKeyBuilder.buildKey(dao.getChannelName(), dao.getType())));

    return aceisByChannelAndType.values().stream()
      .map(channelTypeAceiDaos -> mergeRelated(channelTypeAceiDaos, entityManager))
      .flatMap(List::stream)
      .collect(toSet());
  }

  private List<AcquiredChannelEnvironmentIssueBooleanDao> mergeRelated(
    List<AcquiredChannelEnvironmentIssueBooleanDao> toBeMerged, EntityManager entityManager) {
    if (toBeMerged.size() < 2) {
      return toBeMerged;
    }

    List<AcquiredChannelEnvironmentIssueBooleanDao> merged = new ArrayList<>();
    var mergeIterator = toBeMerged.stream()
      .sorted(Comparator.comparing(AcquiredChannelEnvironmentIssueDao::getStartTime)).iterator();
    AcquiredChannelEnvironmentIssueBooleanDao current = mergeIterator.next();
    while (mergeIterator.hasNext()) {
      AcquiredChannelEnvironmentIssueBooleanDao next = mergeIterator.next();
      if (mergeChecker.canMerge(current, next)) {
        current = merge(current, next, entityManager);
      } else {
        merged.add(current);
        current = next;
      }
    }
    merged.add(current);

    return merged;
  }

  /**
   * Merge the provided {@link AcquiredChannelEnvironmentIssueBooleanDao} into a single entity, order independent
   * @param left An ACEI to be merged, order independent
   * @param right An ACEI to be merged, order independent
   * @param entityManager Entity manager with which to verify if either
   * {@link AcquiredChannelEnvironmentIssueBooleanDao} is a managed entity, and handle accordingly
   * @return The result of merging the provided {@link AcquiredChannelEnvironmentIssueBooleanDao}s together
   */
  @VisibleForTesting
  AcquiredChannelEnvironmentIssueBooleanDao merge(
    AcquiredChannelEnvironmentIssueBooleanDao left,
    AcquiredChannelEnvironmentIssueBooleanDao right, EntityManager entityManager) {
    if (left.equals(right)) {
      return entityManager.contains(left) ? left : right;
    }

    if (entityManager.contains(left)) {
      return mergeManaged(left, right, entityManager);
    } else if (entityManager.contains(right)) {
      return mergeManaged(right, left, entityManager);
    } else {
      left.setStartTime(left.getStartTime().isBefore(right.getStartTime()) ?
        left.getStartTime() :
        right.getStartTime());
      left.setEndTime(right.getEndTime().isAfter(left.getEndTime()) ?
        right.getEndTime() :
        left.getEndTime());
      return left;
    }
  }

  private AcquiredChannelEnvironmentIssueBooleanDao mergeManaged(AcquiredChannelEnvironmentIssueBooleanDao managed,
    AcquiredChannelEnvironmentIssueBooleanDao maybeManaged, EntityManager entityManager) {
    managed.setStartTime(managed.getStartTime().isBefore(maybeManaged.getStartTime()) ?
      managed.getStartTime() :
      maybeManaged.getStartTime());
    managed.setEndTime(maybeManaged.getEndTime().isAfter(managed.getEndTime()) ?
      maybeManaged.getEndTime() :
      managed.getEndTime());

    if (entityManager.contains(maybeManaged)) {
      entityManager.remove(maybeManaged);
    }
    return managed;
  }
}
