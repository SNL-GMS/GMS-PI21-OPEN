package gms.core.dataacquisition;

import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueId;
import gms.shared.frameworks.osd.coi.station.StationTestFixtures;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueAnalogDao;
import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueBooleanDao;
import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueDao;
import gms.shared.frameworks.osd.repository.rawstationdataframe.converter.AcquiredChannelEnvironmentIssueAnalogDaoConverter;
import gms.shared.frameworks.osd.repository.rawstationdataframe.converter.AcquiredChannelEnvironmentIssueBooleanDaoConverter;
import gms.shared.frameworks.osd.repository.station.StationGroupRepositoryJpa;
import gms.shared.frameworks.osd.repository.station.StationRepositoryJpa;
import gms.shared.utilities.db.test.utils.PostgresTest;
import gms.shared.utilities.db.test.utils.TestFixtures;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gms.shared.frameworks.osd.coi.dataacquisitionstatus.DataAcquisitionStatusTestFixtures.ACQUIRED_CHANNEL_SOH_ANALOG;
import static gms.shared.frameworks.osd.coi.dataacquisitionstatus.DataAcquisitionStatusTestFixtures.ACQUIRED_CHANNEL_SOH_BOOLEAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class AceiMergeRepositoryTest extends PostgresTest {
  public static final Duration MERGE_TOLERANCE = Duration.ofSeconds(1);
  private static EntityManagerFactory entityManagerFactory;

  private AcquiredChannelEnvironmentIssueAnalogDaoConverter analogDaoConverter;
  private AcquiredChannelEnvironmentIssueBooleanDaoConverter booleanDaoConverter;

  private AceiMergeRepository aceiMergeRepository;

  @BeforeAll
  static void setUpPersistence() {

    final var jdbcUrl = container.getJdbcUrl() + "&reWriteBatchedInserts=true";
    Map<String, String> props = new HashMap<>(
      Map.ofEntries(
        Map.entry("hibernate.connection.driver_class", "org.postgresql.Driver"),
        Map.entry("hibernate.connection.url", jdbcUrl),
        Map.entry("hibernate.connection.username", GMS_DB_USER),
        Map.entry("hibernate.connection.password", GMS_DB_PASSFAKE),
        Map.entry("hibernate.default_schema", "gms_soh"),
        Map.entry("hibernate.dialect", "org.hibernate.dialect.PostgreSQL95Dialect"),
        Map.entry("hibernate.hbm2ddl.auto", "validate"),
        Map.entry("hibernate.flushMode", "FLUSH_AUTO"),
        Map.entry("hibernate.jdbc.batch_size", "50"),
        Map.entry("hibernate.order_inserts", "true"),
        Map.entry("hibernate.order_updates", "true"),
        Map.entry("hibernate.jdbc.batch_versioned_data", "true")
      ));

    try {
      entityManagerFactory = Persistence.createEntityManagerFactory("gms", props);
    } catch (PersistenceException e) {
      throw new IllegalArgumentException("Could not create persistence unit " + "gms", e);
    }

    new StationRepositoryJpa(entityManagerFactory).storeStations(
      List.of(UtilsTestFixtures.STATION, TestFixtures.station));
    new StationGroupRepositoryJpa(entityManagerFactory).storeStationGroups(
      List.of(UtilsTestFixtures.STATION_GROUP, StationTestFixtures.getStationGroup()));
  }

  @AfterAll
  static void tearDownPersistence() {
    entityManagerFactory.close();
  }

  @BeforeEach
  void setUp() {
    aceiMergeRepository = new AceiMergeRepository(entityManagerFactory, channelName -> MERGE_TOLERANCE);
    analogDaoConverter = new AcquiredChannelEnvironmentIssueAnalogDaoConverter();
    booleanDaoConverter = new AcquiredChannelEnvironmentIssueBooleanDaoConverter();
  }

  @AfterEach
  void testCaseTeardown() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      entityManager.getTransaction().begin();
      Query query = entityManager.createNativeQuery("delete from gms_soh.channel_env_issue_analog");
      query.executeUpdate();
      query = entityManager.createNativeQuery("delete from gms_soh.channel_env_issue_boolean");
      query.executeUpdate();
      entityManager.getTransaction().commit();
    } finally {
      entityManager.close();
    }
  }

  @Test
  void testFindMergeable() {
    var entityManager = entityManagerFactory.createEntityManager();
    try {
      var inputAcei = ACQUIRED_CHANNEL_SOH_BOOLEAN;
      var firstNeighborNoGap = AcquiredChannelEnvironmentIssueBoolean.from(
        inputAcei.getChannelName(),
        inputAcei.getType(),
        inputAcei.getStartTime().minusSeconds(300),
        inputAcei.getStartTime(),
        inputAcei.getStatus()
      );
      var secondNeighborGapInTolerance = AcquiredChannelEnvironmentIssueBoolean.from(
        inputAcei.getChannelName(),
        inputAcei.getType(),
        inputAcei.getEndTime().plus(MERGE_TOLERANCE),
        inputAcei.getEndTime().plusSeconds(500),
        inputAcei.getStatus()
      );

      var aceiToPersist = Stream.of(firstNeighborNoGap, secondNeighborGapInTolerance)
        .map(coi -> booleanDaoConverter.fromCoi(coi, entityManager))
        .collect(Collectors.toList());

      entityManager.getTransaction().begin();
      aceiToPersist.forEach(entityManager::persist);
      entityManager.flush();
      entityManager.getTransaction().commit();

      assertThat(aceiMergeRepository.findMergeable(inputAcei.getChannelName(),
        Set.of(booleanDaoConverter.fromCoi(inputAcei, entityManager)), entityManager)).containsExactlyInAnyOrderElementsOf(aceiToPersist);
    } finally {
      entityManager.close();
    }
  }

  @Test
  void testStore() {

    var analogIdOne = ACQUIRED_CHANNEL_SOH_ANALOG.getId();
    var booleanIdOne = ACQUIRED_CHANNEL_SOH_BOOLEAN.getId();

    var entityManager = entityManagerFactory.createEntityManager();
    try {
      assertTrue(entityManager
        .createQuery(
          aceiByCoiIdQuery(entityManager, AcquiredChannelEnvironmentIssueAnalogDao.class, analogIdOne))
        .getResultList().isEmpty());
      assertTrue(entityManager
        .createQuery(aceiByCoiIdQuery(entityManager, AcquiredChannelEnvironmentIssueBooleanDao.class,
          booleanIdOne))
        .getResultList().isEmpty());
    } finally {
      entityManager.close();
    }

    aceiMergeRepository.store(List.of(ACQUIRED_CHANNEL_SOH_ANALOG, ACQUIRED_CHANNEL_SOH_BOOLEAN));

    entityManager = entityManagerFactory.createEntityManager();
    try {
      var analogOneResult = entityManager
        .createQuery(aceiByCoiIdQuery(entityManager, AcquiredChannelEnvironmentIssueAnalogDao.class,
          analogIdOne))
        .getSingleResult();
      assertEquals(analogDaoConverter.fromCoi(ACQUIRED_CHANNEL_SOH_ANALOG, entityManager), analogOneResult);

      var booleanOneResult = entityManager
        .createQuery(aceiByCoiIdQuery(entityManager, AcquiredChannelEnvironmentIssueBooleanDao.class,
          booleanIdOne))
        .getSingleResult();
      assertEquals(booleanDaoConverter.fromCoi(ACQUIRED_CHANNEL_SOH_BOOLEAN, entityManager), booleanOneResult);
    } finally {
      entityManager.close();
    }
  }

  @Test
  void testRepeatStore() {
    var instertAcei = ACQUIRED_CHANNEL_SOH_BOOLEAN;

    aceiMergeRepository.store(List.of(instertAcei));
    aceiMergeRepository.store(List.of(instertAcei));

    var entityManager = entityManagerFactory.createEntityManager();
    try {
      var stored = findByChannelTimeRange(entityManager,
        entityManager.getCriteriaBuilder(),
        List.of(instertAcei.getChannelName()), instertAcei.getStartTime(), instertAcei.getEndTime());
      assertEquals(1, stored.size());
      assertEquals(instertAcei, booleanDaoConverter.toCoi(stored.get(0)));
    } finally {
      entityManager.close();
    }
  }

  @Test
  void testStoreMergeables() {
    var mergeableAcei = ACQUIRED_CHANNEL_SOH_BOOLEAN;
    var firstNeighborNoGap = AcquiredChannelEnvironmentIssueBoolean.from(
      mergeableAcei.getChannelName(),
      mergeableAcei.getType(),
      mergeableAcei.getStartTime().minusSeconds(300),
      mergeableAcei.getStartTime(),
      mergeableAcei.getStatus()
    );
    var secondNeighborGapInTolerance = AcquiredChannelEnvironmentIssueBoolean.from(
      mergeableAcei.getChannelName(),
      mergeableAcei.getType(),
      mergeableAcei.getEndTime().plus(MERGE_TOLERANCE),
      mergeableAcei.getEndTime().plusSeconds(500),
      mergeableAcei.getStatus()
    );
    var unmergeableAcei = AcquiredChannelEnvironmentIssueBoolean.from(
      mergeableAcei.getChannelName(),
      mergeableAcei.getType(),
      mergeableAcei.getEndTime().plus(1, ChronoUnit.DAYS),
      mergeableAcei.getEndTime().plus(1, ChronoUnit.DAYS).plusSeconds(10),
      mergeableAcei.getStatus()
    );

    var entityManager = entityManagerFactory.createEntityManager();
    try {
      entityManager.getTransaction().begin();
      EntityManager finalPersistEntityManager = entityManager;
      Stream.of(firstNeighborNoGap, secondNeighborGapInTolerance)
        .map(coi -> booleanDaoConverter.fromCoi(coi, finalPersistEntityManager))
        .forEach(entityManager::persist);
      entityManager.flush();
      entityManager.getTransaction().commit();
    } finally {
      entityManager.close();
    }

    List<AcquiredChannelEnvironmentIssueBooleanDao> persisted;

    entityManager = entityManagerFactory.createEntityManager();
    try {
      persisted = findByChannelTimeRange(entityManager, entityManager.getCriteriaBuilder(),
        List.of(firstNeighborNoGap.getChannelName()), firstNeighborNoGap.getStartTime(),
        secondNeighborGapInTolerance.getEndTime());
      assertEquals(2, persisted.size());
      assertThat(persisted.stream().map(booleanDaoConverter::toCoi).collect(Collectors.toList()))
        .containsExactlyInAnyOrderElementsOf(List.of(firstNeighborNoGap, secondNeighborGapInTolerance));
    } finally {
      entityManager.close();
    }

    aceiMergeRepository.store(List.of(mergeableAcei, unmergeableAcei));

    entityManager = entityManagerFactory.createEntityManager();
    try {
      var merged = findByChannelTimeRange(entityManager,
        entityManager.getCriteriaBuilder(),
        List.of(mergeableAcei.getChannelName()), firstNeighborNoGap.getStartTime(), unmergeableAcei.getEndTime());
      assertEquals(2, merged.size());

      var mergedDao = merged.get(0);

      assertEquals(persisted.get(0).getId(), mergedDao.getId());
      assertNotEquals(persisted.get(1).getId(), merged.get(1).getId());
      assertEquals(unmergeableAcei, booleanDaoConverter.toCoi(merged.get(1)));
    } finally {
      entityManager.close();
    }
  }

  private List<AcquiredChannelEnvironmentIssueBooleanDao> findByChannelTimeRange(EntityManager entityManager,
    CriteriaBuilder builder, List<String> channelNames, Instant startTime, Instant endTime) {
    CriteriaQuery<AcquiredChannelEnvironmentIssueBooleanDao> query = builder.createQuery(AcquiredChannelEnvironmentIssueBooleanDao.class);
    Root<AcquiredChannelEnvironmentIssueBooleanDao> fromEntity = query.from(AcquiredChannelEnvironmentIssueBooleanDao.class);
    query.select(fromEntity);

    query.where(builder.and(
        builder.lessThanOrEqualTo(fromEntity.get("startTime"), endTime),
        builder.greaterThanOrEqualTo(fromEntity.get("endTime"), startTime),
        fromEntity.get("channelName").in(channelNames)))
      .orderBy(
        builder.asc(fromEntity.get("channelName")),
        builder.asc(fromEntity.get("startTime")));

    TypedQuery<AcquiredChannelEnvironmentIssueBooleanDao> findDaos = entityManager.createQuery(query);

    return findDaos.getResultList();
  }

  static <E extends AcquiredChannelEnvironmentIssueDao> CriteriaQuery<E> aceiByCoiIdQuery(
    EntityManager entityManager, Class<E> entityType, AcquiredChannelEnvironmentIssueId coiId) {
    var builder = entityManager.getCriteriaBuilder();

    CriteriaQuery<E> aceiQuery =
      builder.createQuery(entityType);
    Root<E> fromAcei = aceiQuery.from(entityType);

    aceiQuery.where(builder.and(builder.equal(fromAcei.get("channelName"), coiId.getChannelName()),
      builder.equal(fromAcei.get("type"), coiId.getType()),
      builder.equal(fromAcei.get("startTime"), coiId.getStartTime())));

    return aceiQuery;
  }
}