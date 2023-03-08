package gms.core.dataacquisition;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import gms.core.dataacquisition.reactor.AceiDaoMerger;
import gms.core.dataacquisition.reactor.util.ToleranceResolver;
import gms.shared.frameworks.osd.api.util.RepositoryExceptionUtils;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueAnalog;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueBooleanDao;
import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueDao;
import gms.shared.frameworks.osd.repository.rawstationdataframe.converter.AcquiredChannelEnvironmentIssueAnalogDaoConverter;
import gms.shared.frameworks.osd.repository.rawstationdataframe.converter.AcquiredChannelEnvironmentIssueBooleanDaoConverter;
import org.apache.commons.lang3.tuple.Pair;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Repository for conducting the merge and storage of {@link AcquiredChannelEnvironmentIssue}s
 */
public class AceiMergeRepository {

  private static final String CHANNEL_NAME = "channelName";
  private static final String START_TIME = "startTime";
  private static final String END_TIME = "endTime";
  private static final String TYPE = "type";
  private static final String STATUS = "status";
  private static final Comparator<AcquiredChannelEnvironmentIssueBooleanDao> ACEI_BOOLEAN_DAO_COMPARATOR =
    Comparator.<AcquiredChannelEnvironmentIssueBooleanDao, Instant>comparing(AcquiredChannelEnvironmentIssueDao::getStartTime)
      .thenComparing(AcquiredChannelEnvironmentIssueDao::getEndTime);
  private static final AcquiredChannelEnvironmentIssueAnalogDaoConverter analogConverter = new AcquiredChannelEnvironmentIssueAnalogDaoConverter();
  private static final AcquiredChannelEnvironmentIssueBooleanDaoConverter booleanConverter = new AcquiredChannelEnvironmentIssueBooleanDaoConverter();

  private final EntityManagerFactory entityManagerFactory;
  private final ToleranceResolver toleranceResolver;
  private final AceiDaoMerger daoMerger;
  private final int batchSize;

  /**
   * Repository for conducting the merge and storage of {@link AcquiredChannelEnvironmentIssue}s
   * @param entityManagerFactory Factory to resolve {@link EntityManager}s to conduct database interactions
   * @param toleranceResolver Resolver for the tolerance value used to determine whether two ACEIs can be merged
   */
  public AceiMergeRepository(EntityManagerFactory entityManagerFactory, ToleranceResolver toleranceResolver) {
    this.entityManagerFactory = entityManagerFactory;
    var batchSizeProp = entityManagerFactory.getProperties()
      .getOrDefault("hibernate.jdbc.batch_size", "50")
      .toString();
    this.batchSize = Integer.parseInt(batchSizeProp);

    this.toleranceResolver = toleranceResolver;
    this.daoMerger = AceiDaoMerger.create(toleranceResolver);
  }

  /**
   * Store the provided {@link AcquiredChannelEnvironmentIssue}s, merging with the underlying database records when appropriate
   * @param aceiToStore Collection of ACEI to store/merge with the underlying database
   */
  public void store(Collection<AcquiredChannelEnvironmentIssue<?>> aceiToStore) {
    Preconditions.checkNotNull(aceiToStore);

    var entityManager = entityManagerFactory.createEntityManager();

    var groupedAcei = aceiToStore.stream()
      .collect(collectingAndThen(
        toList(),
        list -> Pair.of(
          list.stream().filter(AcquiredChannelEnvironmentIssueAnalog.class::isInstance)
            .map(AcquiredChannelEnvironmentIssueAnalog.class::cast).collect(toList()),
          list.stream().filter(AcquiredChannelEnvironmentIssueBoolean.class::isInstance)
            .map(AcquiredChannelEnvironmentIssueBoolean.class::cast).collect(toList()))));

    entityManager.getTransaction().begin();

    try {
      var channelNames = getChannelNames(entityManager);
      storeAnalog(groupedAcei.getLeft(), entityManager, channelNames);
      syncBoolean(groupedAcei.getRight(), entityManager, channelNames);
      entityManager.flush();
      entityManager.getTransaction().commit();
    } catch (PersistenceException e) {
      entityManager.getTransaction().rollback();
      throw RepositoryExceptionUtils.wrap(e);
    } finally {
      entityManager.close();
    }
  }

  private void storeAnalog(Collection<AcquiredChannelEnvironmentIssueAnalog> analogInserts,
    EntityManager entityManager, List<String> validChannelNames) {

    var filteredDaos = analogInserts.stream()
      .filter(acei -> validChannelNames.contains(acei.getChannelName()))
      .map(acei -> analogConverter.fromCoi(acei, entityManager))
      .collect(toList());

    for (var batch : Lists.partition(filteredDaos, batchSize)) {
      for (var aceiDao : batch) {
        entityManager.merge(aceiDao);
      }
    }
  }

  private void syncBoolean(Collection<AcquiredChannelEnvironmentIssueBoolean> booleanInserts,
    EntityManager entityManager, List<String> validChannelNames) {

    var aceiByChannel = booleanInserts.stream()
      .filter(acei -> validChannelNames.contains(acei.getChannelName()))
      .map(coi -> booleanConverter.fromCoi(coi, entityManager))
      .collect(groupingBy(AcquiredChannelEnvironmentIssueDao::getChannelName,
        collectingAndThen(toSet(), set -> daoMerger.mergeAll(set, entityManager))));

    var managedMergeables = aceiByChannel.entrySet().stream()
      .map(entry -> findMergeable(entry.getKey(), entry.getValue(), entityManager))
      .flatMap(Set::stream)
      .collect(toList());

    var aceiToMerge = Streams.concat(
        aceiByChannel.values().stream()
          .flatMap(Set::stream),
        managedMergeables.stream())
      .collect(toList());

    var mmIds = managedMergeables.stream()
      .map(AcquiredChannelEnvironmentIssueBooleanDao::getId)
      .collect(toList());

    var aceiToStore = daoMerger.mergeAll(aceiToMerge, entityManager)
      .stream()
      .filter(aceiDao -> !mmIds.contains(aceiDao.getId()))
      .collect(toList());

    for (var batch : Lists.partition(aceiToStore, batchSize)) {
      for (var aceiDao : batch) {
        entityManager.persist(aceiDao);
      }
    }
  }

  /**
   * Retrieve the ACEI segments from the database "mergeable" with the provided
   * {@link AcquiredChannelEnvironmentIssueBooleanDao}s
   * @param channelName Name of the channel all provided ACEIs match to
   * @param aceis ACEIs for which mergeable segments are to be found in the database
   * @param entityManager Entity manager associated with the database to be queried against
   * @return All {@link AcquiredChannelEnvironmentIssueBooleanDao}s in the database that are "mergeable" with the
   * provided ACEI within tolerance
   */
  @VisibleForTesting
  Set<AcquiredChannelEnvironmentIssueBooleanDao> findMergeable(
    String channelName, Collection<AcquiredChannelEnvironmentIssueBooleanDao> aceis, EntityManager entityManager) {
    var sortedAceis = new ArrayList<>(aceis);
    sortedAceis.sort(ACEI_BOOLEAN_DAO_COMPARATOR);

    var maxPartitionSize = 10;
    var tolerance = toleranceResolver.resolveTolerance(channelName);
    return Lists.partition(sortedAceis, maxPartitionSize).stream()
      .flatMap(partition -> findMergeableInternal(partition, tolerance, entityManager).stream())
      .collect(toSet());
  }

  private Set<AcquiredChannelEnvironmentIssueBooleanDao> findMergeableInternal(
    Collection<AcquiredChannelEnvironmentIssueBooleanDao> aceis, Duration tolerance, EntityManager entityManager) {
    var cb = entityManager.getCriteriaBuilder();

    // Create parent query
    var aceiQuery = cb.createQuery(AcquiredChannelEnvironmentIssueBooleanDao.class);
    var fromAcei = aceiQuery.from(AcquiredChannelEnvironmentIssueBooleanDao.class);

    var anyMergeable = cb.or(aceis.stream()
      .map(acei -> mergeable(acei, tolerance, cb, fromAcei))
      .toArray(Predicate[]::new));

    aceiQuery.select(fromAcei).where(anyMergeable);

    return entityManager.createQuery(aceiQuery).getResultStream()
      .collect(toSet());
  }

  private Predicate mergeable(AcquiredChannelEnvironmentIssueBooleanDao acei, Duration tolerance, CriteriaBuilder cb,
    Root<AcquiredChannelEnvironmentIssueBooleanDao> fromAcei) {
    var sameChannel = cb.equal(fromAcei.get(CHANNEL_NAME), acei.getChannelName());
    var sameType = cb.equal(fromAcei.get(TYPE), acei.getType());
    var sameValue = cb.equal(fromAcei.get(STATUS), acei.isStatus());
    var connectedWithinTolerance = cb.and(
      cb.lessThanOrEqualTo(fromAcei.get(START_TIME), acei.getEndTime().plus(tolerance)),
      cb.greaterThanOrEqualTo(fromAcei.get(END_TIME), acei.getStartTime().minus(tolerance)));

    return cb.and(sameChannel, sameType, sameValue, connectedWithinTolerance);
  }

  /**
   * If an ACEI references an invalid channel, it can't be stored to the DB and should be removed
   *
   * @return filtered list with aceis that have invalid channel removed
   */
  private List<String> getChannelNames(EntityManager entityManager) {

    TypedQuery<String> query = entityManager
      .createNamedQuery("Channel.getChannelNames", String.class)
      .setHint("org.hibernate.cacheable", true);
    return query.getResultList();
  }
}
