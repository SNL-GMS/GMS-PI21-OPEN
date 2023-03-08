package gms.shared.frameworks.osd.repository.performancemonitoring;

import gms.shared.frameworks.osd.api.util.HistoricalStationSohRequest;
import gms.shared.frameworks.osd.api.util.StationsTimeRangeRequest;
import gms.shared.frameworks.osd.api.util.TimeRangeRequest;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.BAD_STATION_SOH;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.MARGINAL_STATION_SOH;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.NOW;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.soh.ChannelSoh;
import gms.shared.frameworks.osd.coi.soh.DurationSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.PercentSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import gms.shared.frameworks.osd.coi.station.StationTestFixtures;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.STATION;
import gms.shared.frameworks.osd.dao.soh.StationSohDao;
import gms.shared.frameworks.osd.dto.soh.DurationSohMonitorValues;
import gms.shared.frameworks.osd.dto.soh.HistoricalSohMonitorValues;
import gms.shared.frameworks.osd.dto.soh.HistoricalStationSoh;
import gms.shared.frameworks.osd.dto.soh.PercentSohMonitorValues;
import gms.shared.frameworks.osd.dto.soh.SohMonitorValues;
import gms.shared.frameworks.osd.repository.SohPostgresTest;
import gms.shared.frameworks.osd.repository.performancemonitoring.converter.StationSohDaoConverter;
import gms.shared.frameworks.osd.repository.station.StationGroupRepositoryJpa;
import gms.shared.frameworks.osd.repository.station.StationRepositoryJpa;
import gms.shared.utilities.db.test.utils.TestFixtures;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import static org.assertj.core.api.Assertions.assertThat;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class PerformanceMonitoringRepositoryJpaTest extends SohPostgresTest {

  private static final Logger logger = LoggerFactory
    .getLogger(PerformanceMonitoringRepositoryJpaTest.class);
  private static final int NUM_STATIONS_FOR_HISTORICAL_QUERY = 10;

  private static EntityManagerFactory stagedEMF;

  private PerformanceMonitoringRepositoryJpa stagedPMR;

  @BeforeAll
  static void beforeAll() {
    new StationRepositoryJpa(entityManagerFactory).storeStations(
      List.of(UtilsTestFixtures.STATION, TestFixtures.station));
    new StationGroupRepositoryJpa(entityManagerFactory).storeStationGroups(
      List.of(UtilsTestFixtures.STATION_GROUP, StationTestFixtures.getStationGroup()));

    // Initialize staged EMF
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
        Map.entry("hibernate.jdbc.batch_versioned_data", "true"),
        Map.entry("hibernate.physical_naming_strategy", "gms.shared.frameworks.osd.dao.util.StagedPrefixNamingStrategy")
      ));

    try {
      stagedEMF = Persistence.createEntityManagerFactory("gms_staged", props);
    }
    catch (PersistenceException e) {
      throw new IllegalArgumentException("Could not create persistence unit gms", e);
    }
  }

  @AfterEach
  void testCaseTeardown() throws InterruptedException {
    // Tear down staged tables
    EntityManager stagedEM = stagedEMF.createEntityManager();
    stagedEM.getTransaction().begin();
    stagedEM.createQuery("delete from ChannelSohMonitorValueAndStatusDao").executeUpdate();
    stagedEM.createQuery("delete from ChannelSohDao").executeUpdate();
    stagedEM.createQuery("delete from StationSohMonitorValueAndStatusDao").executeUpdate();
    stagedEM.createQuery("delete from StationAggregateDao").executeUpdate();
    stagedEM.createQuery("delete from StationSohDao").executeUpdate();
    stagedEM.getTransaction().commit();
    stagedEM.close();

    // Tear down non-staged tables
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();
    entityManager.createQuery("delete from ChannelSohMonitorValueAndStatusDao").executeUpdate();
    entityManager.createQuery("delete from ChannelSohDao").executeUpdate();
    entityManager.createQuery("delete from StationSohMonitorValueAndStatusDao").executeUpdate();
    entityManager.createQuery("delete from StationAggregateDao").executeUpdate();
    entityManager.createQuery("delete from StationSohDao").executeUpdate();
    entityManager.getTransaction().commit();
    entityManager.close();
  }

  @Test
  void testStore() {
    var storePMR = new PerformanceMonitoringRepositoryJpa(stagedEMF);
    List<UUID> uuids = storePMR
      .storeStationSoh(List.of(MARGINAL_STATION_SOH));

    assertFalse(uuids.isEmpty());
    assertEquals(1, uuids.size());
    assertTrue(uuids.contains(MARGINAL_STATION_SOH.getId()));

    EntityManager queryEM = stagedEMF.createEntityManager();
    try {
      StationSohDao dao = queryEM.unwrap(Session.class)
        .bySimpleNaturalId(StationSohDao.class)
        .load(MARGINAL_STATION_SOH.getId());
      assertNotNull(dao);
      assertEquals(MARGINAL_STATION_SOH, new StationSohDaoConverter().toCoi(dao));
    }
    finally {
      queryEM.close();
    }
  }

  @Test
  void testDuplicateStationSohStoreErrors() {
    stagedPMR = new PerformanceMonitoringRepositoryJpa(stagedEMF);
    stagedPMR.storeStationSoh(List.of(MARGINAL_STATION_SOH));

    List<UUID> results = assertDoesNotThrow(() -> stagedPMR.storeStationSoh(List.of(MARGINAL_STATION_SOH)));
    assertTrue(results.contains(MARGINAL_STATION_SOH.getId()));

    var retrieved = stagedEMF.createEntityManager().unwrap(Session.class)
      .bySimpleNaturalId(StationSohDao.class)
      .load(MARGINAL_STATION_SOH.getId());

    assertNotNull(retrieved);
  }

  @ParameterizedTest
  @MethodSource("getRetrieveByStationGroupIdArgs")
  void testRetrieveByStationGroupIdValidation(Class<? extends Exception> expectedException,
    List<String> stationGroupNames) {
    assertThrows(expectedException,
      () -> new PerformanceMonitoringRepositoryJpa(stagedEMF)
        .retrieveByStationId(stationGroupNames));
  }

  static Stream<Arguments> getRetrieveByStationGroupIdArgs() {
    return Stream.of(
      arguments(NullPointerException.class, null),
      arguments(IllegalStateException.class, List.of()));
  }

  @Test
  void testRetrieveByStationGroupId() throws InterruptedException, IOException {
    stagedPMR = new PerformanceMonitoringRepositoryJpa(stagedEMF);
    stagedPMR.storeStationSoh(List.of(MARGINAL_STATION_SOH, BAD_STATION_SOH));
    List<StationSoh> stationSohs = new PerformanceMonitoringRepositoryJpa(entityManagerFactory)
      .retrieveByStationId(List.of(STATION.getName()));

    assertEquals(1, stationSohs.size());
    var om = CoiObjectMapperFactory.getJsonObjectMapper();
    logger.info("Marginal Station SOH: {}", om.writeValueAsString(MARGINAL_STATION_SOH));
    logger.info("Returned Station SOH: {}", om.writeValueAsString(stationSohs.get(0)));
    assertEquals(MARGINAL_STATION_SOH, stationSohs.get(0));
  }

  @Test
  void testRetrieveByTimeRangeValidation() {
    var queryPMR = new PerformanceMonitoringRepositoryJpa(entityManagerFactory);
    assertThrows(NullPointerException.class,
      () -> queryPMR.retrieveByStationsAndTimeRange(null));
  }

  @Test
  void testRetrieveByTimeRange() {
    stagedPMR = new PerformanceMonitoringRepositoryJpa(stagedEMF);
    stagedPMR.storeStationSoh(List.of(MARGINAL_STATION_SOH, BAD_STATION_SOH));
    StationsTimeRangeRequest request = StationsTimeRangeRequest.create(List.of(STATION.getName()),
      NOW.minusSeconds(60),
      NOW);
    TimeRangeRequest.create(Instant.EPOCH, Instant.EPOCH.plusSeconds(20));

    var queryPMR = new PerformanceMonitoringRepositoryJpa(entityManagerFactory);
    List<StationSoh> timeRangeStatus = queryPMR
      .retrieveByStationsAndTimeRange(request);
    assertEquals(1, timeRangeStatus.size());
    assertTrue(timeRangeStatus.contains(MARGINAL_STATION_SOH));
  }

  @Test
  void testHistoricalStationSohQueryWithInvalidMonitorType() {
    stagedPMR = new PerformanceMonitoringRepositoryJpa(stagedEMF);
    stagedPMR.storeStationSoh(populateHistoricalStationSohData());

    HistoricalStationSohRequest request = HistoricalStationSohRequest.create(
      STATION.getName(), NOW, NOW.plusSeconds(20),
      SohMonitorType.TIMELINESS);

    var queryPMR = new PerformanceMonitoringRepositoryJpa(entityManagerFactory);
    HistoricalStationSoh historicalStationSoh = queryPMR
      .retrieveHistoricalStationSoh(request);

    assertEquals(request.getStationName(), historicalStationSoh.getStationName());
    assertEquals(0, historicalStationSoh.getCalculationTimes().length);
    assertEquals(0, historicalStationSoh.getMonitorValues().size());
  }

  @Test
  void testHistoricalStationSohQuery() {
    stagedPMR = new PerformanceMonitoringRepositoryJpa(stagedEMF);
    stagedPMR.storeStationSoh(populateHistoricalStationSohData());

    Instant start = Instant.now();

    HistoricalStationSohRequest request = HistoricalStationSohRequest.create(
      STATION.getName(), NOW,
      NOW.plusSeconds(20 * NUM_STATIONS_FOR_HISTORICAL_QUERY),
      SohMonitorType.MISSING);

    logger.info("Station Time Range Soh Type Request: {}", request.toString());

    var queryPMR = new PerformanceMonitoringRepositoryJpa(entityManagerFactory);
    HistoricalStationSoh historicalStationSoh = queryPMR
      .retrieveHistoricalStationSoh(request);

    Instant finish = Instant.now();
    long timeElapsed = Duration.between(start, finish).toMillis();

    logger.info("---- SQL Retrieve Historical Station Soh Data: ----");
    logger.info("Elapsed time (ms): {}", timeElapsed);
    assertEquals(NUM_STATIONS_FOR_HISTORICAL_QUERY,
      historicalStationSoh.getCalculationTimes().length);
    for (int i = 0; i < historicalStationSoh.getCalculationTimes().length - 1; i++) {
      assertTrue(historicalStationSoh.getCalculationTimes()[i] < historicalStationSoh
        .getCalculationTimes()[i + 1],
        "CalculationTimes are not sorted correctly");
    }

    for (HistoricalSohMonitorValues hmv : historicalStationSoh.getMonitorValues()) {
      for (Map.Entry<SohMonitorType, SohMonitorValues> es : hmv.getValuesByType().entrySet()) {
        if (es.getValue() instanceof DurationSohMonitorValues) {
          DurationSohMonitorValues smv = (DurationSohMonitorValues) es.getValue();
          for (int i = 0; i < smv.getValues().length - 1; i++) {
            assertTrue(smv.getValues()[i] < smv.getValues()[i + 1],
              "DurationSohMonitorValues are not sorted correctly");
          }
        }
        if (es.getValue() instanceof PercentSohMonitorValues) {
          PercentSohMonitorValues smv = (PercentSohMonitorValues) es.getValue();
          for (int i = 0; i < smv.getValues().length - 1; i++) {
            assertTrue(smv.getValues()[i] < smv.getValues()[i + 1],
              "PercentSohMonitorValues are not sorted correctly");
          }
        }
      }
    }
  }

  @Test
  void testStoreStationSohAgain() {
    stagedPMR = new PerformanceMonitoringRepositoryJpa(stagedEMF);
    var otherStationSoh = MARGINAL_STATION_SOH.toBuilder()
      .setId(UUID.randomUUID()).setTime(MARGINAL_STATION_SOH.getTime().plusSeconds(5)).build();
    stagedPMR.storeStationSoh(List.of(MARGINAL_STATION_SOH, BAD_STATION_SOH));
    stagedPMR.storeStationSoh(List.of(otherStationSoh));
    var stationSohs = List.of(MARGINAL_STATION_SOH);
    assertDoesNotThrow(() -> stagedPMR.storeStationSoh(stationSohs));

    List<StationSoh> storedStationSohs = new PerformanceMonitoringRepositoryJpa(entityManagerFactory)
      .retrieveByStationId(List.of(MARGINAL_STATION_SOH.getStationName()));

    assertThat(storedStationSohs).containsExactlyInAnyOrder(otherStationSoh);
  }

  private Collection<StationSoh> populateHistoricalStationSohData() {
    List<StationSoh> stationSohList = new ArrayList<>();
    StationSoh stationSoh = MARGINAL_STATION_SOH;
    for (int i = 0; i < NUM_STATIONS_FOR_HISTORICAL_QUERY; i++) {
      Set<ChannelSoh> channelSohs = stationSoh.getChannelSohs();
      Set<ChannelSoh> newChannelSohs = new LinkedHashSet<>();

      for (ChannelSoh channelSoh : channelSohs) {
        var latency = PercentSohMonitorValueAndStatus.from((double) i, SohStatus.GOOD, SohMonitorType.MISSING);
        var missing = DurationSohMonitorValueAndStatus.from(
          Duration.ofSeconds(i), SohStatus.GOOD, SohMonitorType.LAG);

        Set<SohMonitorValueAndStatus<?>> allSmvs = new HashSet<>();
        allSmvs.add(latency);
        allSmvs.add(missing);
        for (var smvs : channelSoh.getAllSohMonitorValueAndStatuses()) {
          if (smvs.getMonitorType() != SohMonitorType.MISSING
            && smvs.getMonitorType() != SohMonitorType.LAG) {
            allSmvs.add(smvs);
          }
        }

        newChannelSohs.add(ChannelSoh.from(
          channelSoh.getChannelName(),
          channelSoh.getSohStatusRollup(),
          allSmvs));
      }

      stationSohList.add(
        StationSoh.from(UUID.randomUUID(),
          stationSoh.getTime().plusSeconds(i * 20),
          stationSoh.getStationName(),
          stationSoh.getSohMonitorValueAndStatuses(),
          stationSoh.getSohStatusRollup(),
          newChannelSohs,
          stationSoh.getAllStationAggregates()));
    }

    StationSoh invalidTimeRange = StationSoh.create(
      MARGINAL_STATION_SOH.getTime().plusSeconds(60 * 60 * 24),
      MARGINAL_STATION_SOH.getStationName(),
      MARGINAL_STATION_SOH.getSohMonitorValueAndStatuses(),
      MARGINAL_STATION_SOH.getSohStatusRollup(),
      MARGINAL_STATION_SOH.getChannelSohs(),
      MARGINAL_STATION_SOH.getAllStationAggregates()
    );
    StationSoh invalidStation = StationSoh.create(
      Instant.ofEpochMilli(NOW.toEpochMilli()),
      "ASAR",
      MARGINAL_STATION_SOH.getSohMonitorValueAndStatuses(),
      MARGINAL_STATION_SOH.getSohStatusRollup(),
      MARGINAL_STATION_SOH.getChannelSohs(),
      MARGINAL_STATION_SOH.getAllStationAggregates()
    );
    stationSohList.add(invalidTimeRange);
    stationSohList.add(invalidStation);
    return stationSohList;
  }
}
