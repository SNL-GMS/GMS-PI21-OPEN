package gms.shared.frameworks.osd.repository.rawstationdataframe;

import gms.shared.frameworks.osd.api.rawstationdataframe.AceiUpdates;
import gms.shared.frameworks.osd.api.util.ChannelTimeRangeRequest;
import gms.shared.frameworks.osd.api.util.ChannelTimeRangeSohTypeRequest;
import gms.shared.frameworks.osd.api.util.ChannelsTimeRangeRequest;
import gms.shared.frameworks.osd.api.util.TimeRangeRequest;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueAnalog;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueId;
import gms.shared.frameworks.osd.coi.station.StationTestFixtures;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueAnalogDao;
import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueBooleanDao;
import gms.shared.frameworks.osd.repository.SohPostgresTest;
import gms.shared.frameworks.osd.repository.station.StationGroupRepositoryJpa;
import gms.shared.frameworks.osd.repository.station.StationRepositoryJpa;
import gms.shared.frameworks.osd.repository.util.CriteriaQueryUtil;
import gms.shared.utilities.db.test.utils.TestFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static gms.shared.frameworks.osd.coi.dataacquisitionstatus.DataAcquisitionStatusTestFixtures.ACQUIRED_CHANNEL_SOH_ANALOG;
import static gms.shared.frameworks.osd.coi.dataacquisitionstatus.DataAcquisitionStatusTestFixtures.ACQUIRED_CHANNEL_SOH_ANALOG_TWO;
import static gms.shared.frameworks.osd.coi.dataacquisitionstatus.DataAcquisitionStatusTestFixtures.ACQUIRED_CHANNEL_SOH_BOOLEAN;
import static gms.shared.frameworks.osd.coi.dataacquisitionstatus.DataAcquisitionStatusTestFixtures.ACQUIRED_CHANNEL_SOH_BOOLEAN_ISSUES;
import static gms.shared.frameworks.osd.coi.dataacquisitionstatus.DataAcquisitionStatusTestFixtures.ACQUIRED_CHANNEL_SOH_BOOLEAN_TWO;
import static gms.shared.frameworks.osd.coi.dataacquisitionstatus.DataAcquisitionStatusTestFixtures.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class AcquiredChannelEnvironmentIssueRepositoryJpaTest extends SohPostgresTest {

  private AcquiredChannelEnvironmentIssueRepositoryJpa aceiRepository;

  @BeforeAll
  static void beforeAll() {
    new StationRepositoryJpa(entityManagerFactory).storeStations(
      List.of(UtilsTestFixtures.STATION, TestFixtures.station));
    new StationGroupRepositoryJpa(entityManagerFactory).storeStationGroups(
      List.of(UtilsTestFixtures.STATION_GROUP, StationTestFixtures.getStationGroup()));
  }

  @BeforeEach
  void setUp() {
    aceiRepository = new AcquiredChannelEnvironmentIssueRepositoryJpa(entityManagerFactory);
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
  void testSyncAceiUpdatesValidation() {
    assertThrows(NullPointerException.class, () -> aceiRepository.syncAceiUpdates(null));
  }

  @Test
  void testFindAnalogAceiByChannelAndTimeRangeValidation() {
    assertThrows(NullPointerException.class, () -> aceiRepository.findAnalogAceiByChannelAndTimeRange(null));
  }

  @Test
  void testFindBooleanByChannelAndTimeRangeValidation() {
    assertThrows(NullPointerException.class, () -> aceiRepository.findBooleanAceiByChannelAndTimeRange(null));
  }

  @Test
  void testRetrieveAcquiredChannelSohAnalogByChannelTimeRangeAndTypeValidation() {
    assertThrows(NullPointerException.class, () -> aceiRepository.findAnalogAceiByChannelTimeRangeAndType(null));
  }

  @Test
  void testRetrieveAcquiredChannelSohBooleanByChannelTimeRangeAndTypeValidation() {
    assertThrows(NullPointerException.class, () -> aceiRepository.findBooleanAceiByChannelTimeRangeAndType(null));
  }

  @Test
  void testRetrieveAcquiredChannelEnvironmentalIssueAnalogByTimeValidation() {
    assertThrows(NullPointerException.class, () -> aceiRepository.findAnalogAceiByTime(null));
  }

  @Test
  void testRetrieveAcquiredChannelEnvironmentalIssueBooleanByTimeValidation() {
    assertThrows(NullPointerException.class, () -> aceiRepository.findBooleanAceiByTime(null));
  }

  @Test
  void testFindAnalogAceiByChannelAndTimeRange() {
    aceiRepository.syncAceiUpdates(AceiUpdates.from(ACQUIRED_CHANNEL_SOH_ANALOG));

    ChannelTimeRangeRequest request =
      ChannelTimeRangeRequest.create(UtilsTestFixtures.CHANNEL.getName(),
        NOW.minusSeconds(700),
        NOW);
    List<AcquiredChannelEnvironmentIssueAnalog> result = aceiRepository
      .findAnalogAceiByChannelAndTimeRange(request);
    assertEquals(1, result.size());
    assertTrue(result.contains(ACQUIRED_CHANNEL_SOH_ANALOG));
  }

  @Test
  void testFindBooleanAceiByChannelAndTimeRange() {
    aceiRepository.syncAceiUpdates(AceiUpdates.from(ACQUIRED_CHANNEL_SOH_BOOLEAN));
    ChannelTimeRangeRequest request = ChannelTimeRangeRequest.create(UtilsTestFixtures.CHANNEL.getName(),
      NOW.minusSeconds(700), NOW);
    List<AcquiredChannelEnvironmentIssueBoolean> result = aceiRepository.findBooleanAceiByChannelAndTimeRange(request);
    assertEquals(1, result.size());
    assertTrue(result.contains(ACQUIRED_CHANNEL_SOH_BOOLEAN));
  }

  @Test
  void testFindBooleanAceiByChannelsAndTimeRange() {
    aceiRepository.syncAceiUpdates(
      AceiUpdates.builder().setBooleanInserts(ACQUIRED_CHANNEL_SOH_BOOLEAN_ISSUES).build());

    ChannelsTimeRangeRequest request = ChannelsTimeRangeRequest.create(
      List.of(UtilsTestFixtures.CHANNEL.getName(), UtilsTestFixtures.CHANNEL_TWO.getName()),
      NOW.minusSeconds(700), NOW);
    List<AcquiredChannelEnvironmentIssueBoolean> result = aceiRepository
      .findBooleanAceiByChannelsAndTimeRange(request);
    assertEquals(2, result.size());
    assertTrue(result.contains(ACQUIRED_CHANNEL_SOH_BOOLEAN));
    assertTrue(result.contains(ACQUIRED_CHANNEL_SOH_BOOLEAN_TWO));
  }

  @Test
  void testFindAnalogAceiByIdEmpty() {
    Optional<AcquiredChannelEnvironmentIssueAnalog> result = aceiRepository
      .findAnalogAceiById(AcquiredChannelEnvironmentIssueId.create("bad channel name",
        AcquiredChannelEnvironmentIssueType.MEAN_AMPLITUDE, Instant.EPOCH));

    assertTrue(result.isEmpty());
  }

  @Test
  void testFindAnalogAceiByIdPresent() {
    aceiRepository.syncAceiUpdates(AceiUpdates.from(ACQUIRED_CHANNEL_SOH_ANALOG));

    Optional<AcquiredChannelEnvironmentIssueAnalog> result = aceiRepository
      .findAnalogAceiById(ACQUIRED_CHANNEL_SOH_ANALOG.getId());

    assertTrue(result.isPresent());
    assertEquals(ACQUIRED_CHANNEL_SOH_ANALOG, result.get());
  }

  @Test
  void testFindBooleanAceiByIdEmpty() {
    Optional<AcquiredChannelEnvironmentIssueBoolean> result = aceiRepository.findBooleanAceiById(
      AcquiredChannelEnvironmentIssueId.create("bad channel name",
        AcquiredChannelEnvironmentIssueType.MEAN_AMPLITUDE, Instant.EPOCH));

    assertTrue(result.isEmpty());
  }

  @Test
  void testFindBooleanAceiByIdPresent() {
    aceiRepository.syncAceiUpdates(AceiUpdates.from(ACQUIRED_CHANNEL_SOH_BOOLEAN));

    Optional<AcquiredChannelEnvironmentIssueBoolean> result = aceiRepository
      .findBooleanAceiById(ACQUIRED_CHANNEL_SOH_BOOLEAN.getId());

    assertTrue(result.isPresent());
    assertEquals(ACQUIRED_CHANNEL_SOH_BOOLEAN, result.get());
  }

  @Test
  void testFindAnalogAceiByChannelTimeRangeAndType() {
    aceiRepository.syncAceiUpdates(AceiUpdates.from(ACQUIRED_CHANNEL_SOH_ANALOG));

    ChannelTimeRangeSohTypeRequest request = ChannelTimeRangeSohTypeRequest.create(
      UtilsTestFixtures.CHANNEL.getName(),
      NOW.minusSeconds(700), NOW,
      AcquiredChannelEnvironmentIssueType.CLIPPED);

    List<AcquiredChannelEnvironmentIssueAnalog> result = aceiRepository.findAnalogAceiByChannelTimeRangeAndType(
      request);

    assertEquals(1, result.size());
    assertTrue(result.contains(ACQUIRED_CHANNEL_SOH_ANALOG));
  }

  @Test
  void testFindBooleanAceiByChannelTimeRangeAndType() {
    aceiRepository.syncAceiUpdates(AceiUpdates.from(ACQUIRED_CHANNEL_SOH_BOOLEAN));

    ChannelTimeRangeSohTypeRequest request = ChannelTimeRangeSohTypeRequest.create(
      UtilsTestFixtures.CHANNEL.getName(),
      NOW.minusSeconds(700),
      NOW,
      AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED);

    List<AcquiredChannelEnvironmentIssueBoolean> result =
      aceiRepository.findBooleanAceiByChannelTimeRangeAndType(request);

    assertEquals(1, result.size());
    assertTrue(result.contains(ACQUIRED_CHANNEL_SOH_BOOLEAN));
  }

  @Test
  void testFindAnalogAceiByTime() {
    aceiRepository.syncAceiUpdates(AceiUpdates.from(ACQUIRED_CHANNEL_SOH_ANALOG));

    TimeRangeRequest request = TimeRangeRequest.create(NOW.minusSeconds(700), NOW);
    List<AcquiredChannelEnvironmentIssueAnalog> result = aceiRepository.findAnalogAceiByTime(request);

    assertEquals(1, result.size());
    assertTrue(result.contains(ACQUIRED_CHANNEL_SOH_ANALOG));
  }

  @Test
  void testFindBooleanAceiByTime() {
    aceiRepository.syncAceiUpdates(AceiUpdates.from(ACQUIRED_CHANNEL_SOH_BOOLEAN));

    TimeRangeRequest request = TimeRangeRequest.create(NOW.minusSeconds(700), NOW);
    List<AcquiredChannelEnvironmentIssueBoolean> result = aceiRepository.findBooleanAceiByTime(request);

    assertEquals(1, result.size());
    assertTrue(result.contains(ACQUIRED_CHANNEL_SOH_BOOLEAN));
  }

  @Test
  void testFindMergeable() {
    var tolerance = Duration.ofSeconds(1);
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
      inputAcei.getEndTime().plus(tolerance),
      inputAcei.getEndTime().plusSeconds(500),
      inputAcei.getStatus()
    );

    aceiRepository.syncAceiUpdates(AceiUpdates.from(Set.of(firstNeighborNoGap, secondNeighborGapInTolerance)));
    assertThat(aceiRepository.findMergeable(Set.of(inputAcei), Duration.ZERO)).containsOnly(firstNeighborNoGap);
    assertThat(aceiRepository.findMergeable(Set.of(inputAcei), tolerance)).containsOnly(firstNeighborNoGap,
      secondNeighborGapInTolerance);
  }

  @Test
  void testSyncAceiUpdates() {
    EntityManager entityManager;

    var analogIdOne = ACQUIRED_CHANNEL_SOH_ANALOG.getId();
    var analogIdTwo = ACQUIRED_CHANNEL_SOH_ANALOG_TWO.getId();
    var booleanIdOne = ACQUIRED_CHANNEL_SOH_BOOLEAN.getId();
    var booleanIdTwo = ACQUIRED_CHANNEL_SOH_BOOLEAN_TWO.getId();

    entityManager = entityManagerFactory.createEntityManager();
    assertTrue(entityManager
      .createQuery(
        CriteriaQueryUtil.aceiByCoiIdQuery(entityManager, AcquiredChannelEnvironmentIssueAnalogDao.class, analogIdOne))
      .getResultList().isEmpty());
    assertTrue(entityManager
      .createQuery(CriteriaQueryUtil.aceiByCoiIdQuery(entityManager, AcquiredChannelEnvironmentIssueBooleanDao.class,
        booleanIdOne))
      .getResultList().isEmpty());
    entityManager.close();

    AceiUpdates justInsert = AceiUpdates.builder()
      .addAnalogInsert(ACQUIRED_CHANNEL_SOH_ANALOG)
      .addBooleanInsert(ACQUIRED_CHANNEL_SOH_BOOLEAN)
      .build();
    aceiRepository.syncAceiUpdates(justInsert);

    entityManager = entityManagerFactory.createEntityManager();
    try {
      AcquiredChannelEnvironmentIssueAnalogDao analogOneResult = entityManager
        .createQuery(CriteriaQueryUtil.aceiByCoiIdQuery(entityManager, AcquiredChannelEnvironmentIssueAnalogDao.class,
          analogIdOne))
        .getSingleResult();
      assertEquivalent(ACQUIRED_CHANNEL_SOH_ANALOG, analogOneResult);

      AcquiredChannelEnvironmentIssueBooleanDao booleanOneResult = entityManager
        .createQuery(CriteriaQueryUtil.aceiByCoiIdQuery(entityManager, AcquiredChannelEnvironmentIssueBooleanDao.class,
          booleanIdOne))
        .getSingleResult();
      assertEquivalent(ACQUIRED_CHANNEL_SOH_BOOLEAN, booleanOneResult);
    } finally {
      entityManager.close();
    }

    AceiUpdates removeAndInsert = AceiUpdates.builder()
      .addAnalogInsert(ACQUIRED_CHANNEL_SOH_ANALOG_TWO)
      .addAnalogDelete(ACQUIRED_CHANNEL_SOH_ANALOG)
      .addBooleanInsert(ACQUIRED_CHANNEL_SOH_BOOLEAN_TWO)
      .addBooleanDelete(ACQUIRED_CHANNEL_SOH_BOOLEAN)
      .build();
    aceiRepository.syncAceiUpdates(removeAndInsert);

    entityManager = entityManagerFactory.createEntityManager();
    assertTrue(entityManager
      .createQuery(
        CriteriaQueryUtil.aceiByCoiIdQuery(entityManager, AcquiredChannelEnvironmentIssueAnalogDao.class, analogIdOne))
      .getResultList().isEmpty());
    assertTrue(entityManager
      .createQuery(CriteriaQueryUtil.aceiByCoiIdQuery(entityManager, AcquiredChannelEnvironmentIssueBooleanDao.class,
        booleanIdOne))
      .getResultList().isEmpty());
    entityManager.close();

    entityManager = entityManagerFactory.createEntityManager();
    try {
      AcquiredChannelEnvironmentIssueAnalogDao analogTwoResult = entityManager
        .createQuery(CriteriaQueryUtil.aceiByCoiIdQuery(entityManager, AcquiredChannelEnvironmentIssueAnalogDao.class,
          analogIdTwo))
        .getSingleResult();
      assertEquivalent(ACQUIRED_CHANNEL_SOH_ANALOG_TWO, analogTwoResult);

      AcquiredChannelEnvironmentIssueBooleanDao booleanTwoResult = entityManager
        .createQuery(CriteriaQueryUtil.aceiByCoiIdQuery(entityManager, AcquiredChannelEnvironmentIssueBooleanDao.class,
          booleanIdTwo))
        .getSingleResult();
      assertEquivalent(ACQUIRED_CHANNEL_SOH_BOOLEAN_TWO, booleanTwoResult);
    } finally {
      entityManager.close();
    }

  }

  @Test
  void testAceiEntityMutationPreservesPrimaryKey() {
    var entityManager = entityManagerFactory.createEntityManager();
    var analogCoiId = ACQUIRED_CHANNEL_SOH_ANALOG.getId();

    try {
      assertTrue(entityManager
        .createQuery(CriteriaQueryUtil.aceiByCoiIdQuery(entityManager, AcquiredChannelEnvironmentIssueAnalogDao.class,
          analogCoiId))
        .getResultList().isEmpty());

      AceiUpdates initialState = AceiUpdates.builder()
        .addAnalogInsert(ACQUIRED_CHANNEL_SOH_ANALOG)
        .addAnalogInsert(ACQUIRED_CHANNEL_SOH_ANALOG_TWO)
        .build();
      aceiRepository.syncAceiUpdates(initialState);

      AcquiredChannelEnvironmentIssueAnalogDao managedAcei = entityManager.createQuery(
          CriteriaQueryUtil.aceiByCoiIdQuery(entityManager, AcquiredChannelEnvironmentIssueAnalogDao.class,
            analogCoiId))
        .getSingleResult();

      managedAcei.setStartTime(Instant.EPOCH);

      entityManager.getTransaction().begin();
      entityManager.merge(managedAcei);
      entityManager.flush();
      entityManager.getTransaction().commit();
      entityManager.clear();

      AcquiredChannelEnvironmentIssueId managedId = AcquiredChannelEnvironmentIssueId.create(
        managedAcei.getChannelName(), managedAcei.getType(), managedAcei.getStartTime()
      );

      AcquiredChannelEnvironmentIssueAnalogDao managedUpdatedAcei = entityManager.createQuery(
          CriteriaQueryUtil.aceiByCoiIdQuery(entityManager, AcquiredChannelEnvironmentIssueAnalogDao.class,
            managedId))
        .getSingleResult();

      assertEquals(managedAcei, managedUpdatedAcei);
      assertEquals(managedAcei.getId(), managedUpdatedAcei.getId());
    } finally {
      entityManager.close();
    }
  }

  private void assertEquivalent(AcquiredChannelEnvironmentIssueAnalog expectedCoi,
    AcquiredChannelEnvironmentIssueAnalogDao actualDao) {
    assertEquals(expectedCoi.getChannelName(), actualDao.getChannelName());
    assertEquals(expectedCoi.getType(), actualDao.getType());
    assertEquals(expectedCoi.getStartTime(), actualDao.getStartTime());
    assertEquals(expectedCoi.getEndTime(), actualDao.getEndTime());
    assertEquals(expectedCoi.getStatus(), actualDao.getStatus(), 0.0001);
  }

  private void assertEquivalent(AcquiredChannelEnvironmentIssueBoolean expectedCoi,
    AcquiredChannelEnvironmentIssueBooleanDao actualDao) {
    assertEquals(expectedCoi.getChannelName(), actualDao.getChannelName());
    assertEquals(expectedCoi.getType(), actualDao.getType());
    assertEquals(expectedCoi.getStartTime(), actualDao.getStartTime());
    assertEquals(expectedCoi.getEndTime(), actualDao.getEndTime());
    assertEquals(expectedCoi.getStatus(), actualDao.isStatus());
  }

}
