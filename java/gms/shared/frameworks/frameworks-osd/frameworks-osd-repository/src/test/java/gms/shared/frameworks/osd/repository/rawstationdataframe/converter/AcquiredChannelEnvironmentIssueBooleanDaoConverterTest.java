package gms.shared.frameworks.osd.repository.rawstationdataframe.converter;

import gms.shared.frameworks.osd.api.rawstationdataframe.AceiUpdates;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueId;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import gms.shared.frameworks.osd.dao.channel.ChannelDao;
import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueBooleanDao;
import gms.shared.frameworks.osd.repository.SohPostgresTest;
import gms.shared.frameworks.osd.repository.rawstationdataframe.AcquiredChannelEnvironmentIssueRepositoryJpa;
import gms.shared.frameworks.osd.repository.station.StationRepositoryJpa;
import gms.shared.frameworks.osd.repository.util.CriteriaQueryUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Stream;

import static gms.shared.frameworks.osd.coi.dataacquisitionstatus.DataAcquisitionStatusTestFixtures.ACQUIRED_CHANNEL_SOH_BOOLEAN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Testcontainers
class AcquiredChannelEnvironmentIssueBooleanDaoConverterTest extends SohPostgresTest {

  private AcquiredChannelEnvironmentIssueBooleanDaoConverter converter;

  @BeforeEach
  void setUp() {
    converter = new AcquiredChannelEnvironmentIssueBooleanDaoConverter();
  }

  @BeforeAll
  static void testCaseSetup() {
    new StationRepositoryJpa(entityManagerFactory)
      .storeStations(List.of(UtilsTestFixtures.STATION));
  }

  @AfterEach
  void cleanUpData() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();

    try {
      entityManager.getTransaction().begin();
      entityManager.createQuery("delete from AcquiredChannelSohBooleanDao").executeUpdate();
      entityManager.getTransaction().commit();
    } catch (Exception ex) {
      entityManager.getTransaction().rollback();
    } finally {
      entityManager.close();
    }
  }

  @AfterAll
  static void tearDown() {
    entityManagerFactory.close();
  }

  @ParameterizedTest
  @MethodSource("fromCoiValidationArguments")
  void testFromCoiValidation(Class<? extends Exception> expectedException,
    AcquiredChannelEnvironmentIssueBoolean sohAnalog, EntityManager entityManager) {

    try {
      assertThrows(expectedException, () -> converter.fromCoi(sohAnalog, entityManager));
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  static Stream<Arguments> fromCoiValidationArguments() {
    return Stream.of(
      arguments(NullPointerException.class, null, entityManagerFactory.createEntityManager()),
      arguments(NullPointerException.class, ACQUIRED_CHANNEL_SOH_BOOLEAN, null));
  }

  @Test
  void testFromCoiNew() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();

    try {
      entityManager.getTransaction().begin();

      AcquiredChannelEnvironmentIssueBooleanDao actual = converter.fromCoi(ACQUIRED_CHANNEL_SOH_BOOLEAN, entityManager);

      assertEquals(ACQUIRED_CHANNEL_SOH_BOOLEAN.getChannelName(), actual.getChannelName());
      assertEquals(ACQUIRED_CHANNEL_SOH_BOOLEAN.getType(), actual.getType());
      assertEquals(ACQUIRED_CHANNEL_SOH_BOOLEAN.getStartTime(), actual.getStartTime());
      assertEquals(ACQUIRED_CHANNEL_SOH_BOOLEAN.getEndTime(), actual.getEndTime());
      assertEquals(ACQUIRED_CHANNEL_SOH_BOOLEAN.getStatus(), actual.isStatus());
    } finally {
      entityManager.getTransaction().rollback();
      entityManager.close();
    }
  }

  @Test
  void testFromCoiExisting() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();

    try {
      AcquiredChannelEnvironmentIssueId acquiredChannelEnvironmentIssueId = AcquiredChannelEnvironmentIssueId.create(
        ACQUIRED_CHANNEL_SOH_BOOLEAN.getChannelName(), ACQUIRED_CHANNEL_SOH_BOOLEAN.getType(),
        ACQUIRED_CHANNEL_SOH_BOOLEAN.getStartTime());
      new AcquiredChannelEnvironmentIssueRepositoryJpa(entityManagerFactory)
        .syncAceiUpdates(AceiUpdates.from(ACQUIRED_CHANNEL_SOH_BOOLEAN));

      AcquiredChannelEnvironmentIssueBooleanDao expected = entityManager.createQuery(
          CriteriaQueryUtil.aceiByCoiIdQuery(entityManager, AcquiredChannelEnvironmentIssueBooleanDao.class,
            acquiredChannelEnvironmentIssueId))
        .getSingleResult();

      entityManager.getTransaction().begin();

      AcquiredChannelEnvironmentIssueBooleanDao actual = converter.fromCoi(ACQUIRED_CHANNEL_SOH_BOOLEAN, entityManager);

      assertEquals(expected, actual);
    } finally {
      entityManager.getTransaction().rollback();
      entityManager.close();
    }
  }

  @Test
  void toCoi() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();

    try {
      AcquiredChannelEnvironmentIssueBooleanDao expected = new AcquiredChannelEnvironmentIssueBooleanDao();
      expected.setChannel(entityManager.find(ChannelDao.class, ACQUIRED_CHANNEL_SOH_BOOLEAN.getChannelName()));
      expected.setChannelName(ACQUIRED_CHANNEL_SOH_BOOLEAN.getChannelName());
      expected.setType(ACQUIRED_CHANNEL_SOH_BOOLEAN.getType());
      expected.setStartTime(ACQUIRED_CHANNEL_SOH_BOOLEAN.getStartTime());
      expected.setEndTime(ACQUIRED_CHANNEL_SOH_BOOLEAN.getEndTime());
      expected.setStatus(ACQUIRED_CHANNEL_SOH_BOOLEAN.getStatus());

      AcquiredChannelEnvironmentIssueBoolean actual = converter.toCoi(expected);

      assertEquals(expected.getChannelName(), actual.getChannelName());
      assertEquals(expected.getType(), actual.getType());
      assertEquals(expected.getStartTime(), actual.getStartTime());
      assertEquals(expected.getEndTime(), actual.getEndTime());
      assertEquals(expected.isStatus(), actual.getStatus());
    } finally {
      entityManager.close();
    }
  }
}