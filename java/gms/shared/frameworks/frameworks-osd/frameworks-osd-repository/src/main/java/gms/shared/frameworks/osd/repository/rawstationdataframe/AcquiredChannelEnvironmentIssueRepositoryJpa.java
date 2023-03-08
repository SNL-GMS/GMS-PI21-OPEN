package gms.shared.frameworks.osd.repository.rawstationdataframe;

import com.google.common.collect.Lists;
import gms.shared.frameworks.osd.api.rawstationdataframe.AceiUpdates;
import gms.shared.frameworks.osd.api.rawstationdataframe.AcquiredChannelEnvironmentIssueRepositoryInterface;
import gms.shared.frameworks.osd.api.util.ChannelTimeRangeRequest;
import gms.shared.frameworks.osd.api.util.ChannelTimeRangeSohTypeRequest;
import gms.shared.frameworks.osd.api.util.ChannelsTimeRangeRequest;
import gms.shared.frameworks.osd.api.util.RepositoryExceptionUtils;
import gms.shared.frameworks.osd.api.util.TimeRangeRequest;
import gms.shared.frameworks.osd.coi.ParameterValidation;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueAnalog;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueId;
import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueAnalogDao;
import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueBooleanDao;
import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueDao;
import gms.shared.frameworks.osd.repository.rawstationdataframe.converter.AcquiredChannelEnvironmentIssueAnalogDaoConverter;
import gms.shared.frameworks.osd.repository.rawstationdataframe.converter.AcquiredChannelEnvironmentIssueBooleanDaoConverter;
import gms.shared.frameworks.utilities.jpa.EntityConverter;
import gms.shared.metrics.CustomMetric;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

/**
 * Implement interface for storing and retrieving objects related to State of Health (SOH) from the
 * relational database.
 */
public class AcquiredChannelEnvironmentIssueRepositoryJpa implements AcquiredChannelEnvironmentIssueRepositoryInterface {

  private static final Logger logger = LoggerFactory.getLogger(AcquiredChannelEnvironmentIssueRepositoryJpa.class);

  private static final String CHANNEL_NAME = "channelName";
  private static final String START_TIME = "startTime";
  private static final String END_TIME = "endTime";
  private static final String TYPE = "type";
  private static final String STATUS = "status";
  private static final Comparator<AcquiredChannelEnvironmentIssueBoolean> ACEI_BOOLEAN_COMPARATOR = Comparator.<AcquiredChannelEnvironmentIssueBoolean, Instant>comparing(
    AcquiredChannelEnvironmentIssue::getStartTime).thenComparing(AcquiredChannelEnvironmentIssue::getEndTime);

  private final EntityManagerFactory entityManagerFactory;
  private final int batchSize;

  /**
   * Default constructor.
   */
  public AcquiredChannelEnvironmentIssueRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    this.entityManagerFactory = entityManagerFactory;
    var batchSizeProp = entityManagerFactory.getProperties()
      .getOrDefault("hibernate.jdbc.batch_size", "50")
      .toString();
    this.batchSize = Integer.parseInt(batchSizeProp);
  }

  /**
   * Synchronizes ACEI described within {@link AceiUpdates}' to be deleted and stored.
   *
   * @param aceiUpdates an object describing the ACEI to be deleted and stored.
   */
  @Override
  public void syncAceiUpdates(AceiUpdates aceiUpdates) {
    Validate.notNull(aceiUpdates, "AceiUpdates may not be null");
    logger.debug("Syncing updates: analog[inserts:{} deletes:{}] boolean[inserts:{} deletes:{}]",
      aceiUpdates.getAnalogInserts().size(), aceiUpdates.getAnalogDeletes().size(),
      aceiUpdates.getBooleanInserts().size(), aceiUpdates.getBooleanDeletes().size());

    var entityManager = entityManagerFactory.createEntityManager();

    measureRunnable(() -> {
      entityManager.getTransaction().begin();

      try {
        deleteAceis(aceiUpdates.getAnalogDeletes(), AcquiredChannelEnvironmentIssueAnalogDao.class, entityManager);
        deleteAceis(aceiUpdates.getBooleanDeletes(), AcquiredChannelEnvironmentIssueBooleanDao.class, entityManager);
        storeAceis(aceiUpdates.getAnalogInserts(), new AcquiredChannelEnvironmentIssueAnalogDaoConverter(),
          entityManager);
        storeAceis(aceiUpdates.getBooleanInserts(), new AcquiredChannelEnvironmentIssueBooleanDaoConverter(),
          entityManager);
        entityManager.flush();
        entityManager.getTransaction().commit();
      } catch (PersistenceException e) {
        entityManager.getTransaction().rollback();
        throw RepositoryExceptionUtils.wrap(e);
      } finally {
        entityManager.close();
      }
    }, AceiRepositoryMetrics.sohSyncACEI, AceiRepositoryMetrics.sohSyncACEIDuration)
      .run();
  }

  private <T extends AcquiredChannelEnvironmentIssue<?>, D extends AcquiredChannelEnvironmentIssueDao> void storeAceis(
    Collection<T> aceis, EntityConverter<D, T> aceiConverter, EntityManager entityManager) {
    var channelNames = getChannelNames(entityManager);
    //remove aceis with invalid channel FK and convert to our daos
    var aceiDaos = aceis.stream()
      .filter(acei -> channelNames.contains(acei.getChannelName()))
      .map(acei -> aceiConverter.fromCoi(acei, entityManager))
      .collect(Collectors.toList());

    for (List<D> batch : Lists.partition(aceiDaos, batchSize)) {
      for (D aceiDao : batch) {
        entityManager.merge(aceiDao);
      }
    }
  }

  private <T extends AcquiredChannelEnvironmentIssue<?>, D extends AcquiredChannelEnvironmentIssueDao> void deleteAceis(
    Collection<T> aceis, Class<D> entityType, EntityManager entityManager) {
    //get UUIDs for aceis
    List<AcquiredChannelEnvironmentIssueId> compositeIds = aceis.stream()
      .map(acei -> AcquiredChannelEnvironmentIssueId.create(
          acei.getChannelName(),
          acei.getType(),
          acei.getStartTime()
        )
      )
      .collect(Collectors.toList());

    var builder = entityManager.getCriteriaBuilder();
    CriteriaDelete<D> delete = builder.createCriteriaDelete(entityType);
    Root<D> fromEntity = delete.from(entityType);
    delete.where(fromEntity.get(CHANNEL_NAME).in(compositeIds.stream()
        .map(AcquiredChannelEnvironmentIssueId::getChannelName)
        .collect(Collectors.toSet())),
      fromEntity.get(TYPE).in(compositeIds.stream()
        .map(AcquiredChannelEnvironmentIssueId::getType)
        .collect(Collectors.toSet())),
      fromEntity.get(START_TIME).in(compositeIds.stream()
        .map(AcquiredChannelEnvironmentIssueId::getStartTime)
        .collect(Collectors.toSet())));

    entityManager.createQuery(delete).executeUpdate();
  }

  private Runnable measureRunnable(Runnable runnable,
    CustomMetric<AcquiredChannelEnvironmentIssueRepositoryJpa, Long> metric,
    CustomMetric<Long, Long> duration) {
    return () -> {
      metric.updateMetric(this);
      var start = Instant.now();
      runnable.run();
      var finish = Instant.now();
      long timeElapsed = Duration.between(start, finish).toMillis();
      duration.updateMetric(timeElapsed);
    };
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

  /**
   * Retrieves all {@link AcquiredChannelEnvironmentIssueAnalog} objects for the provided channel
   * created within the provided time range.
   *
   * @param request The collection of channel names and time range that will bound the {@link
   * AcquiredChannelEnvironmentIssueAnalog}s retrieved.
   * @return All SOH analog objects that meet the query criteria.
   */
  @Override
  public List<AcquiredChannelEnvironmentIssueAnalog> findAnalogAceiByChannelAndTimeRange(
    ChannelTimeRangeRequest request) {
    Objects.requireNonNull(request);
    return measureSupplier(() -> findByChannelsAndTimeRangeInternal(AcquiredChannelEnvironmentIssueAnalogDao.class,
        AcquiredChannelEnvironmentIssueAnalogDao::toCoi, Set.of(request.getChannelName()),
        request.getTimeRange().getStartTime(), request.getTimeRange().getEndTime()),
      AceiRepositoryMetrics.sohRetrieveAnalog,
      AceiRepositoryMetrics.sohRetrieveAnalogDuration)
      .get();
  }

  /**
   * Retrieves all {@link AcquiredChannelEnvironmentIssueBoolean} objects for the provided channel
   * created within the provided time range.
   *
   * @param request The channel name and time range that will bound the {@link
   * AcquiredChannelEnvironmentIssueBoolean}s retrieved.
   * @return All SOH boolean objects that meet the query criteria.
   */
  @Override
  public List<AcquiredChannelEnvironmentIssueBoolean> findBooleanAceiByChannelAndTimeRange(
    ChannelTimeRangeRequest request) {
    Objects.requireNonNull(request);

    return measureSupplier(() -> findByChannelsAndTimeRangeInternal(AcquiredChannelEnvironmentIssueBooleanDao.class,
        AcquiredChannelEnvironmentIssueBooleanDao::toCoi, Set.of(request.getChannelName()),
        request.getTimeRange().getStartTime(), request.getTimeRange().getEndTime()),
      AceiRepositoryMetrics.sohRetrieveBoolean,
      AceiRepositoryMetrics.sohRetrieveBooleanDuration)
      .get();
  }

  /**
   * Retrieves all {@link AcquiredChannelEnvironmentIssueBoolean} objects for the provided channel
   * created within the provided time range.
   *
   * @param request The collection of channel names and time range that will bound the {@link
   * AcquiredChannelEnvironmentIssueBoolean}s retrieved.
   * @return All SOH boolean objects that meet the query criteria.
   */
  @Override
  public List<AcquiredChannelEnvironmentIssueBoolean> findBooleanAceiByChannelsAndTimeRange(
    ChannelsTimeRangeRequest request) {
    Objects.requireNonNull(request);
    return measureSupplier(() -> findByChannelsAndTimeRangeInternal(AcquiredChannelEnvironmentIssueBooleanDao.class,
        AcquiredChannelEnvironmentIssueBooleanDao::toCoi, new HashSet<>(request.getChannelNames()),
        request.getTimeRange().getStartTime(), request.getTimeRange().getEndTime()),
      AceiRepositoryMetrics.sohRetrieveBoolean,
      AceiRepositoryMetrics.sohRetrieveBooleanDuration)
      .get();
  }

  /**
   * Queries for JPA entities of type J from a particular channel within a time interval
   *
   * @param entityType JPA entity type (e.g. Class J), not null
   * @param converter converts from a JPA entity type J to the business object type B
   * @param channelNames channel names the SOH was measured on.
   * @param startTime Inclusive start from time range for the query.
   * @param endTime Inclusive end from time range for the query.
   * @param <E> type of acquired channel SOH JPA entity (either {@link
   * AcquiredChannelEnvironmentIssueBooleanDao} or {@link
   * AcquiredChannelEnvironmentIssueAnalogDao})
   * @param <C> type of acquired channel SOH business object (either {@link
   * AcquiredChannelEnvironmentIssueBoolean} or {@link AcquiredChannelEnvironmentIssueAnalog})
   * @return All SOH objects that meet the query criteria.
   */
  private <E extends AcquiredChannelEnvironmentIssueDao, C extends AcquiredChannelEnvironmentIssue<?>> List<C>
  findByChannelsAndTimeRangeInternal(Class<E> entityType, EntityConverter<E, C> converter, Set<String> channelNames,
    Instant startTime, Instant endTime) {
    Objects.requireNonNull(channelNames, "Cannot run query with null channel name set");
    Objects.requireNonNull(startTime, "Cannot run query with null start time");
    Objects.requireNonNull(endTime, "Cannot run query with null end time");

    //this allows startTime == endTime
    ParameterValidation.requireFalse(Instant::isAfter, startTime, endTime,
      "Cannot run query with start time greater than end time");
    var entityManager = entityManagerFactory.createEntityManager();
    try {
      var builder = entityManager.getCriteriaBuilder();
      CriteriaQuery<E> query = builder.createQuery(entityType);
      Root<E> fromEntity = query.from(entityType);
      query.select(fromEntity);

      query.where(builder.and(
          builder.lessThanOrEqualTo(fromEntity.get(START_TIME), endTime),
          builder.greaterThanOrEqualTo(fromEntity.get(END_TIME), startTime),
          fromEntity.get(CHANNEL_NAME).in(channelNames)))
        .orderBy(
          builder.asc(fromEntity.get(CHANNEL_NAME)),
          builder.asc(fromEntity.get(START_TIME)));

      TypedQuery<E> findDaos = entityManager.createQuery(query);

      return findDaos.getResultList()
        .stream()
        .map(converter::toCoi)
        .collect(Collectors.toList());
    } catch (Exception ex) {
      throw new IllegalStateException("Error retrieving frames, ", ex);
    } finally {
      entityManager.close();
    }
  }

  /**
   * Retrieve the {@link AcquiredChannelEnvironmentIssueAnalog} with the provided id.  Returns an
   * empty {@link Optional} if no AcquiredChannelSohAnalog has that id.
   *
   * @param id defining the id for the AcquiredChannelSohAnalog, not null
   * @return Optional AcquiredChannelSohAnalog object with the provided id, not null
   */
  @Override
  public Optional<AcquiredChannelEnvironmentIssueAnalog> findAnalogAceiById(AcquiredChannelEnvironmentIssueId id) {
    return measureSupplier(() -> findByIdInternal(AcquiredChannelEnvironmentIssueAnalogDao.class,
        AcquiredChannelEnvironmentIssueAnalogDao::toCoi, id),
      AceiRepositoryMetrics.sohRetrieveACEIAnalogId,
      AceiRepositoryMetrics.sohRetrieveACEIAnalogIdDuration)
      .get();
  }

  /**
   * Retrieve the {@link AcquiredChannelEnvironmentIssueBoolean} with the provided id.  Returns an
   * empty {@link Optional} if no AcquiredChannelSohBoolean has that id.
   *
   * @param id defining the id for the AcquiredChannelSohBoolean, not null
   * @return Optional AcquiredChannelSohBoolean object with the provided id, not null
   */
  @Override
  public Optional<AcquiredChannelEnvironmentIssueBoolean> findBooleanAceiById(AcquiredChannelEnvironmentIssueId id) {
    return measureSupplier(() -> findByIdInternal(AcquiredChannelEnvironmentIssueBooleanDao.class,
        AcquiredChannelEnvironmentIssueBooleanDao::toCoi, id),
      AceiRepositoryMetrics.sohRetrieveACEIBooleanId,
      AceiRepositoryMetrics.sohRetrieveACEIBooleanIdDuration)
      .get();
  }

  @Override
  public List<AcquiredChannelEnvironmentIssueAnalog> findAnalogAceiByChannelTimeRangeAndType(
    ChannelTimeRangeSohTypeRequest request) {
    Objects.requireNonNull(request);

    return measureSupplier(() -> findByChannelTimeRangeAndTypeInternal(AcquiredChannelEnvironmentIssueAnalogDao.class,
        new AcquiredChannelEnvironmentIssueAnalogDaoConverter(), request.getChannelName(),
        request.getTimeRange().getStartTime(), request.getTimeRange().getEndTime(), request.getType()),
      AceiRepositoryMetrics.sohRetrieveACEIAnalogTimeType,
      AceiRepositoryMetrics.sohRetrieveACEIAnalogTimeTypeDuration)
      .get();
  }

  @Override
  public List<AcquiredChannelEnvironmentIssueBoolean> findBooleanAceiByChannelTimeRangeAndType(
    ChannelTimeRangeSohTypeRequest request) {
    Objects.requireNonNull(request);

    return measureSupplier(() -> findByChannelTimeRangeAndTypeInternal(AcquiredChannelEnvironmentIssueBooleanDao.class,
        new AcquiredChannelEnvironmentIssueBooleanDaoConverter(), request.getChannelName(),
        request.getTimeRange().getStartTime(), request.getTimeRange().getEndTime(), request.getType()),
      AceiRepositoryMetrics.sohRetrieveACEIBooleanTimeType,
      AceiRepositoryMetrics.sohRetrieveACEIBooleanTimeTypeDuration)
      .get();
  }

  private <E extends AcquiredChannelEnvironmentIssueDao, C> List<C> findByChannelTimeRangeAndTypeInternal(
    Class<E> entityType,
    EntityConverter<E, C> converter,
    String channelName,
    Instant startTime,
    Instant endTime,
    AcquiredChannelEnvironmentIssueType type) {

    Objects.requireNonNull(entityType);
    Objects.requireNonNull(converter);
    Objects.requireNonNull(channelName);
    Objects.requireNonNull(startTime);
    Objects.requireNonNull(endTime);
    Objects.requireNonNull(type);

    ParameterValidation.requireFalse(Instant::isAfter, startTime, endTime,
      "Cannot run query when start time is after end time");

    var entityManager = entityManagerFactory.createEntityManager();

    try {
      var builder = entityManager.getCriteriaBuilder();
      CriteriaQuery<E> sohQuery = builder.createQuery(entityType);
      Root<E> fromSoh = sohQuery.from(entityType);
      sohQuery.select(fromSoh);

      List<Predicate> conjunctions = new ArrayList<>();

      conjunctions.add(builder.equal(fromSoh.get(CHANNEL_NAME), channelName));
      conjunctions
        .add(builder.greaterThanOrEqualTo(fromSoh.get(START_TIME), startTime));
      conjunctions.add(builder.lessThanOrEqualTo(fromSoh.get(END_TIME), endTime));
      conjunctions.add(builder.equal(fromSoh.get(TYPE), type));

      sohQuery.where(builder.and(conjunctions.toArray(new Predicate[0])));

      return entityManager.createQuery(sohQuery)
        .getResultStream()
        .map(converter::toCoi)
        .collect(Collectors.toList());
    } finally {
      entityManager.close();
    }
  }

  /**
   * Queries the JPA entity of type J for an {@link AcquiredChannelEnvironmentIssue} object with the
   * provided identity. Uses the converter to convert from an instance of J to an
   * AcquiredChannelSoh. Output {@link Optional} is empty when the query does not find an entity.
   *
   * @param <E> JPA entity type
   * @param entityType JPA entity type (e.g. {@link AcquiredChannelEnvironmentIssueBooleanDao}, not
   * null
   * @param converter converts from an entityType object to an AcquiredChannelSoh, not null
   * @param id {@link AcquiredChannelEnvironmentIssueId} of the desired AcquiredChannelSoh,
   * not null
   * @return Optional AcquiredChannelSoh, not null
   */
  private <E extends AcquiredChannelEnvironmentIssueDao, C> Optional<C> findByIdInternal(Class<E> entityType,
    EntityConverter<E, C> converter, AcquiredChannelEnvironmentIssueId id) {
    var entityManager = entityManagerFactory.createEntityManager();
    var builder = entityManager.getCriteriaBuilder();

    try {
      CriteriaQuery<E> aceiQuery =
        builder.createQuery(entityType);
      Root<E> fromAcei = aceiQuery.from(entityType);

      aceiQuery.where(builder.equal(fromAcei.get(CHANNEL_NAME), id.getChannelName()),
        builder.equal(fromAcei.get(TYPE), id.getType()),
        builder.equal(fromAcei.get(START_TIME), id.getStartTime()));
      return entityManager.createQuery(aceiQuery).getResultStream().findFirst().map(converter::toCoi);
    } finally {
      entityManager.close();
    }
  }

  /**
   * Retrieve the {@link AcquiredChannelEnvironmentIssueAnalog} with the provided id.  Returns an
   * empty {@link Optional} if no AcquiredChannelSohAnalog has that id.
   *
   * @param request time range request to find AcquiredChannelEnvironmentIssueAnalogs by, not null
   * @return Optional AcquiredChannelSohAnalog object with the provided id, not null
   */
  @Override
  public List<AcquiredChannelEnvironmentIssueAnalog> findAnalogAceiByTime(TimeRangeRequest request) {
    Objects.requireNonNull(request);
    return measureSupplier(() -> findByTimeRangeInternal(AcquiredChannelEnvironmentIssueAnalogDao.class,
        new AcquiredChannelEnvironmentIssueAnalogDaoConverter(), request.getStartTime(), request.getEndTime()),
      AceiRepositoryMetrics.sohRetrieveACEIAnalogTime,
      AceiRepositoryMetrics.sohRetrieveACEIAnalogTimeDuration)
      .get();
  }

  /**
   * Retrieve the {@link AcquiredChannelEnvironmentIssueBoolean} with the provided id.  Returns an
   * empty {@link Optional} if no AcquiredChannelSohBoolean has that id.
   *
   * @param request time range for the AcquiredChannelSohBoolean, not null
   * @return Optional AcquiredChannelSohBoolean object with the provided id, not null
   */
  @Override
  public List<AcquiredChannelEnvironmentIssueBoolean> findBooleanAceiByTime(TimeRangeRequest request) {
    Objects.requireNonNull(request);
    return measureSupplier(() -> findByTimeRangeInternal(AcquiredChannelEnvironmentIssueBooleanDao.class,
        new AcquiredChannelEnvironmentIssueBooleanDaoConverter(), request.getStartTime(), request.getEndTime()),
      AceiRepositoryMetrics.sohRetrieveACEIBooleanTime,
      AceiRepositoryMetrics.sohRetrieveACEIBooleanTimeDuration
    ).get();
  }

  /**
   * Find all mergeable acei for the input acei, within the given time tolerance.
   *
   * @param aceis {@link AcquiredChannelEnvironmentIssueBoolean}s to find merge-able neighbors for
   * @param tolerance time tolerance where merging is still allowed regardless of gap
   * @return a Map of channel names to ACEIs.
   */
  @Override
  public Set<AcquiredChannelEnvironmentIssueBoolean> findMergeable(
    Collection<AcquiredChannelEnvironmentIssueBoolean> aceis, Duration tolerance) {
    var sortedAceis = new ArrayList<>(aceis);
    sortedAceis.sort(ACEI_BOOLEAN_COMPARATOR);

    var maxPartitionSize = 10;
    return Lists.partition(sortedAceis, maxPartitionSize).stream()
      .flatMap(partition -> findMergeableInternal(partition, tolerance).stream())
      .collect(Collectors.toSet());
  }

  private Set<AcquiredChannelEnvironmentIssueBoolean> findMergeableInternal(
    Collection<AcquiredChannelEnvironmentIssueBoolean> aceis, Duration tolerance) {
    var converter = new AcquiredChannelEnvironmentIssueBooleanDaoConverter();
    var entityManager = entityManagerFactory.createEntityManager();
    var cb = entityManager.getCriteriaBuilder();

    try {
      // Create parent query
      var aceiQuery = cb.createQuery(AcquiredChannelEnvironmentIssueBooleanDao.class);
      var fromAcei = aceiQuery.from(AcquiredChannelEnvironmentIssueBooleanDao.class);

      var anyMergeable = cb.or(aceis.stream()
        .map(acei -> mergeable(acei, tolerance, cb, fromAcei))
        .toArray(Predicate[]::new));

      aceiQuery.select(fromAcei).where(anyMergeable);

      return entityManager.createQuery(aceiQuery).getResultStream()
        .map(converter::toCoi)
        .collect(toSet());
    } finally {
      entityManager.close();
    }
  }

  private Predicate mergeable(AcquiredChannelEnvironmentIssueBoolean acei, Duration tolerance, CriteriaBuilder cb,
    Root<AcquiredChannelEnvironmentIssueBooleanDao> fromAcei) {
    var sameChannel = cb.equal(fromAcei.get(CHANNEL_NAME), acei.getChannelName());
    var sameType = cb.equal(fromAcei.get(TYPE), acei.getType());
    var sameValue = cb.equal(fromAcei.get(STATUS), acei.getStatus());
    var connectedWithinTolerance = cb.and(
      cb.lessThanOrEqualTo(fromAcei.get(START_TIME), acei.getEndTime().plus(tolerance)),
      cb.greaterThanOrEqualTo(fromAcei.get(END_TIME), acei.getStartTime().minus(tolerance)));

    return cb.and(sameChannel, sameType, sameValue, connectedWithinTolerance);
  }

  private <E, C> List<C> findByTimeRangeInternal(Class<E> entityType, EntityConverter<E, C> converter,
    Instant startTime, Instant endTime) {
    Objects.requireNonNull(entityType);
    Objects.requireNonNull(converter);
    Objects.requireNonNull(startTime);
    Objects.requireNonNull(endTime);

    ParameterValidation.requireFalse(Instant::isAfter, startTime, endTime,
      "Cannot run query when start time is after end time");

    var entityManager = entityManagerFactory.createEntityManager();
    try {
      var builder = entityManager.getCriteriaBuilder();
      CriteriaQuery<E> sohQuery = builder.createQuery(entityType);
      Root<E> fromSoh = sohQuery.from(entityType);
      sohQuery.select(fromSoh);

      List<Predicate> conjunctions = new ArrayList<>();
      conjunctions
        .add(builder.greaterThanOrEqualTo(fromSoh.get(START_TIME), startTime));
      conjunctions.add(builder.lessThanOrEqualTo(fromSoh.get(END_TIME), endTime));

      sohQuery.where(builder.and(conjunctions.toArray(new Predicate[0])));

      return entityManager.createQuery(sohQuery)
        .getResultStream()
        .map(converter::toCoi)
        .collect(Collectors.toList());
    } finally {
      entityManager.close();
    }
  }

  private <V> Supplier<V> measureSupplier(Supplier<V> supplier,
    CustomMetric<AcquiredChannelEnvironmentIssueRepositoryJpa, Long> metric,
    CustomMetric<Long, Long> duration) {
    return () -> {
      metric.updateMetric(this);
      var start = Instant.now();
      var value = supplier.get();
      var finish = Instant.now();
      long timeElapsed = Duration.between(start, finish).toMillis();
      duration.updateMetric(timeElapsed);

      return value;
    };
  }
}

