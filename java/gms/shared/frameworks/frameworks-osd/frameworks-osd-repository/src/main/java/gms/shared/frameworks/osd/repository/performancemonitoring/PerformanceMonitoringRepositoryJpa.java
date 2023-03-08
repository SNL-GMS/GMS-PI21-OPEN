package gms.shared.frameworks.osd.repository.performancemonitoring;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import gms.shared.frameworks.osd.api.performancemonitoring.PerformanceMonitoringRepositoryInterface;
import gms.shared.frameworks.osd.api.util.HistoricalStationSohRequest;
import gms.shared.frameworks.osd.api.util.RepositoryExceptionUtils;
import gms.shared.frameworks.osd.api.util.StationsTimeRangeRequest;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import gms.shared.frameworks.osd.dao.soh.ChannelSohMonitorValueAndStatusDao;
import gms.shared.frameworks.osd.dao.soh.HistoricalSohMonitorValue;
import gms.shared.frameworks.osd.dao.soh.StationSohDao;
import gms.shared.frameworks.osd.dto.soh.HistoricalStationSoh;
import gms.shared.frameworks.osd.repository.performancemonitoring.converter.StationSohDaoConverter;
import gms.shared.frameworks.osd.repository.performancemonitoring.transform.HistoricalStationSohTransformer;
import gms.shared.metrics.CustomMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;
import javax.persistence.RollbackException;

import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;

public class PerformanceMonitoringRepositoryJpa implements
  PerformanceMonitoringRepositoryInterface {

  public static final String STATION_SOH = "stationSoh";
  public static final String MONITOR_TYPE = "monitorType";

  public static final String COI_ID_ATTRIBUTE = "coiId";
  public static final String STATION_ATTRIBUTE = "station";
  public static final String STATION_NAME_ATTRIBUTE = "stationName";
  public static final String CREATION_TIME_ATTRIBUTE = "creationTime";
  public static final String NAME_ATTRIBUTE = "name";
  public static final String CHANNEL_SOH = "channelSoh";
  public static final String CHANNEL_NAME = "channelName";

  private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringRepositoryJpa.class);

  private final EntityManagerFactory entityManagerFactory;

  private static final CustomMetric<PerformanceMonitoringRepositoryJpa, Long> performanceMonitoringRetrieveStationId =
    CustomMetric.create(CustomMetric::incrementer,
      "performance_monitoring_retrieve_station_id_hits:type=Counter", 0L);

  private static final CustomMetric<PerformanceMonitoringRepositoryJpa, Long> performanceMonitoringRetrieveStationTime =
    CustomMetric.create(CustomMetric::incrementer,
      "performanceMonitoringRetrieveStationTime:type=Counter", 0L);

  private static final CustomMetric<Long, Long> performanceMonitoringRetrieveStationIdDuration =
    CustomMetric.create(CustomMetric::updateTimingData,
      "performance_monitoring_retrieve_station_id_duration:type=Value", 0L);

  private static final CustomMetric<Long, Long> performanceMonitoringRetrieveStationTimeDuration =
    CustomMetric.create(CustomMetric::updateTimingData,
      "performance_monitoring_retrieve_station_time_duration:type=Value", 0L);


  /**
   * Constructor taking in the EntityManagerFactory
   *
   * @param entityManagerFactory {@link EntityManagerFactory}
   */
  public PerformanceMonitoringRepositoryJpa(EntityManagerFactory entityManagerFactory) {
    Objects.requireNonNull(entityManagerFactory,
      "Cannot instantiate PerformanceMonitoringRepositoryJpa with null EntityManager");
    this.entityManagerFactory = entityManagerFactory;
  }

  /**
   * Retrieve the latest {@link StationSoh} by station group ids. There are multiple {@link
   * StationSoh} objects per station group id; this method returns the "latest" {@link StationSoh}
   * object for a given station group id where "latest" equals the {@link StationSoh} object for
   * that station group id with the max end time.
   *
   * @return a List of {@link StationSoh}, or an empty list if none found.
   */

  @Override
  public List<StationSoh> retrieveByStationId(List<String> stationNames) {
    Objects.requireNonNull(stationNames);
    Preconditions.checkState(!stationNames.isEmpty());
    var em = entityManagerFactory.createEntityManager();

    performanceMonitoringRetrieveStationId.updateMetric(this);
    var start = Instant.now();

    try {
      var builder = em.getCriteriaBuilder();
      CriteriaQuery<StationSohDao> stationSohQuery = builder.createQuery(StationSohDao.class);
      Root<StationSohDao> fromStationSoh = stationSohQuery.from(StationSohDao.class);
      stationSohQuery.select(fromStationSoh);

      Expression<String> stationName = fromStationSoh.get(STATION_NAME_ATTRIBUTE);

      Expression<Instant> creationTime = fromStationSoh.get(CREATION_TIME_ATTRIBUTE);

      Subquery<Instant> subquery = stationSohQuery.subquery(Instant.class);
      Root<StationSohDao> subRoot = subquery.from(StationSohDao.class);

      Expression<String> subStationName = subRoot.get(STATION_NAME_ATTRIBUTE);
      Expression<Instant> subCreationTime = subRoot.get(CREATION_TIME_ATTRIBUTE);

      subquery
        .where(
          subStationName.in(stationNames),
          builder.equal(subStationName, stationName)
        )
        .groupBy(subStationName)
        .select(builder.greatest(subCreationTime));

      stationSohQuery.where(builder.equal(creationTime, subquery));

      var converter = new StationSohDaoConverter();
      return em.createQuery(stationSohQuery)
        .getResultStream()
        .map(converter::toCoi)
        .collect(toList());
    } catch (Exception ex) {
      throw new IllegalStateException("Error retrieving station group SOH status: {}", ex);
    } finally {
      em.close();

      var finish = Instant.now();
      long timeElapsed = Duration.between(start, finish).toMillis();
      performanceMonitoringRetrieveStationIdDuration.updateMetric(timeElapsed);
    }
  }

  /**
   * Retrieve the {@link StationSoh}(s) within a time range (inclusive) that are currently stored in
   * the database. If a start and end time is not provided, retrieve the most recently stored {@link
   * StationSoh}.
   *
   * @param request Request containing station names and a time range
   * @return a List of {@link StationSoh}(s)
   */
  @Override
  public List<StationSoh> retrieveByStationsAndTimeRange(StationsTimeRangeRequest request) {
    Objects.requireNonNull(request);

    performanceMonitoringRetrieveStationTime.updateMetric(this);
    var start = Instant.now();

    var entityManager = entityManagerFactory.createEntityManager();
    try {
      var builder = entityManager.getCriteriaBuilder();
      CriteriaQuery<StationSohDao> stationSohQuery =
        builder.createQuery(StationSohDao.class);
      Root<StationSohDao> fromStationSoh = stationSohQuery.from(StationSohDao.class);

      Expression<String> stationName = fromStationSoh.get(STATION_NAME_ATTRIBUTE);

      stationSohQuery.select(fromStationSoh)
        .where(builder.and(builder.greaterThanOrEqualTo(fromStationSoh.get(
            CREATION_TIME_ATTRIBUTE), request.getTimeRange().getStartTime()),
          builder.lessThanOrEqualTo(fromStationSoh.get(CREATION_TIME_ATTRIBUTE),
            request.getTimeRange().getEndTime()),
          stationName.in(request.getStationNames())));

      return entityManager.createQuery(stationSohQuery)
        .getResultStream()
        .map(new StationSohDaoConverter()::toCoi)
        .collect(toList());
    } finally {
      entityManager.close();

      var finish = Instant.now();
      long timeElapsed = Duration.between(start, finish).toMillis();
      performanceMonitoringRetrieveStationTimeDuration.updateMetric(timeElapsed);
    }
  }

  /**
   * Store the provided {@link StationSoh}(s).
   *
   * @param stationSohs The {@link StationSoh}(s) to store
   *
   * @return A list of UUIDs that correspond to the {@link StationSoh}(s) that
   * were successfully stored.
   */
  @Override
  public List<UUID> storeStationSoh(Collection<StationSoh> stationSohs) {
    Objects.requireNonNull(stationSohs);

    var sohByStaName = stationSohs.stream()
      .collect(groupingBy(StationSoh::getStationName));

    var singletonStationSohBatch = sohByStaName.values()
      .stream()
      .filter(sohs -> sohs.size() <= 1)
      .flatMap(List::stream)
      .collect(Collectors.toList());

    var sohSameStationBatches = sohByStaName.values()
      .stream()
      .filter(sohs -> sohs.size() > 1)
      .flatMap(List::stream)
      .sorted(Comparator.comparing(StationSoh::getTime))
      .collect(Collectors.toList());

    logger.debug("storing {} StationSoh", stationSohs.size());
    var start = Instant.now();
    var entityManager = entityManagerFactory.createEntityManager();
    var singletonStationSohToStore = singletonStationSohBatch;
    var sohSameStationToStore = sohSameStationBatches;
    try {
      storeBatchStationSoh(singletonStationSohToStore, entityManager);
      sohSameStationToStore.stream().forEach(soh -> storeBatchStationSoh(List.of(soh), entityManager));
    }
    catch (RollbackException e) {

      entityManager.getTransaction().rollback();

      logger.warn("Caught a duplicate storing StationSoh, trying again after filtering");
      singletonStationSohToStore = filterStationSohToStore(entityManager, singletonStationSohBatch);
      sohSameStationToStore = filterStationSohToStore(entityManager, sohSameStationBatches);

      try {
        storeBatchStationSoh(singletonStationSohToStore, entityManager);
        sohSameStationToStore.stream().forEach(soh -> storeBatchStationSoh(List.of(soh), entityManager));
      }
      catch (RollbackException e2) {
        entityManager.getTransaction().rollback();
        throw RepositoryExceptionUtils.wrap(e2);
      }

    }
    catch (Exception e3) {
      entityManager.getTransaction().rollback();
      throw RepositoryExceptionUtils.wrap(e3);
    }
    finally {
      entityManager.close();

      var finish = Instant.now();
      long timeElapsed = Duration.between(start, finish).toMillis();
      logger.debug("Storing {} stationSoh took {} ms", stationSohs.size(), timeElapsed);

    }
    return Stream.concat(
      singletonStationSohToStore.stream(),
      sohSameStationToStore.stream())
      .map(StationSoh::getId)
      .collect(toList());
  }

  /**
   * Attempts to store the stationSoh records in batches
   *
   * @param stationSohs the List of StationSoh to store
   * @param entityManager the EntityManger used to store and create the
   * transaction
   */
  void storeBatchStationSoh(List<StationSoh> stationSohs, EntityManager entityManager) {
    var batchSize = 10;
    var converter = new StationSohDaoConverter();
    entityManager.getTransaction().begin();

    for (List<StationSoh> batch : Lists.partition(stationSohs, batchSize)) {
      for (StationSoh stationSoh : batch) {
        StationSohDao dao = converter.fromCoi(stationSoh, entityManager);
        entityManager.persist(dao);
      }
    }
    entityManager.getTransaction().commit();
  }

  private static List<StationSoh> filterStationSohToStore(EntityManager entityManager,
    Collection<StationSoh> stationSohs) {

    var coidIds = stationSohs.stream()
      .map(StationSoh::getId)
      .collect(toList());

    var cb = entityManager.getCriteriaBuilder();
    var query = cb.createQuery(UUID.class);
    var fromStationSoh = query.from(StationSohDao.class);
    Path<UUID> coiId = fromStationSoh.get(COI_ID_ATTRIBUTE);

    query.select(coiId).where(coiId.in(coidIds));

    var previouslyStored = entityManager.createQuery(query)
      .getResultList();

    return stationSohs.stream()
      .filter(stationSoh -> !previouslyStored.contains(stationSoh.getId()))
      .collect(toList());
  }

  /**
   * Retrieves a HistoricalStationSoh DTO object corresponding to the provided Station ID and
   * collection of SohMonitorTypes provided in the request body.
   * <p>
   * The returned HistoricalStationSoh object contains SOH monitor values from StationSoh objects
   * with calculation time attributes in the time range provided (both start and end times are
   * inclusive), and aggregates the HistoricalSohMonitorValue objects by value and all associations
   * to Station and Channel are by identifier.
   *
   * @return A {@link HistoricalStationSoh} object that conforms to the provided parameters
   */
  @Override
  public HistoricalStationSoh retrieveHistoricalStationSoh(HistoricalStationSohRequest request) {
    Preconditions.checkNotNull(request, "Request cannot be null");

    var sohMonitorType = request.getSohMonitorType();

    if (!SohMonitorType.validTypes().contains(sohMonitorType)) {
      logger.warn(
        "Unsupported monitor type provided. No SOH will be provided for {}. "
          + "Supported types are {}.",
        sohMonitorType, SohMonitorType.validTypes());

      return HistoricalStationSoh.create(request.getStationName(), new long[]{}, List.of());
    }

    return queryHistoricalStationSoh(request);
  }

  /**
   * Performs query to DB.  We are using nativeQuery to select only specific columns we need. I
   * wasn't able to do it with CriteriaQuery or JPQL due to inheritance table on SMVS
   *
   * @param request contains request with values to pass into query
   * @return HistoricalStationSoh contains processed results of query
   */
  private HistoricalStationSoh queryHistoricalStationSoh(HistoricalStationSohRequest request) {

    var entityManager = entityManagerFactory.createEntityManager();

    try {
      var builder = entityManager.getCriteriaBuilder();
      CriteriaQuery<Tuple> historicalSohQuery = builder.createQuery(Tuple.class);
      Root<ChannelSohMonitorValueAndStatusDao> fromCsmvs = historicalSohQuery.from(
        ChannelSohMonitorValueAndStatusDao.class);
      historicalSohQuery.multiselect(fromCsmvs.alias("channelSohMonitorValueStatus"),
          fromCsmvs.get(CHANNEL_NAME).alias(CHANNEL_NAME),
          fromCsmvs.get(CREATION_TIME_ATTRIBUTE).alias(CREATION_TIME_ATTRIBUTE))
        .where(builder.and(
          builder.equal(fromCsmvs.get(STATION_NAME_ATTRIBUTE), request.getStationName()),
          builder.equal(fromCsmvs.get(MONITOR_TYPE), request.getSohMonitorType()),
          builder.between(fromCsmvs.get(CREATION_TIME_ATTRIBUTE),
            request.getStartTime(),
            request.getEndTime())))
        .orderBy(builder.asc(fromCsmvs.get(CREATION_TIME_ATTRIBUTE)));

      var results = entityManager.createQuery(historicalSohQuery)
        .getResultStream()
        .map(tuple -> {
          var channelSmvs = tuple.get("channelSohMonitorValueStatus", ChannelSohMonitorValueAndStatusDao.class);
          var historicalSMVS = new HistoricalSohMonitorValue(channelSmvs.getStationName(),
            tuple.get(CHANNEL_NAME, String.class),
            tuple.get(CREATION_TIME_ATTRIBUTE, Instant.class),
            channelSmvs.getMonitorType());
          historicalSMVS.setDuration(channelSmvs.getDuration());
          historicalSMVS.setPercent(channelSmvs.getPercent());
          historicalSMVS.setStatus(channelSmvs.getStatus());
          return historicalSMVS;
        })
        .collect(toList());
      return HistoricalStationSohTransformer.createHistoricalStationSoh(request.getStationName(), results);
    } catch (Exception ex) {
      throw new IllegalStateException("Error retrieving historical SOH: {}", ex);
    } finally {
      entityManager.close();
    }

  }
}
