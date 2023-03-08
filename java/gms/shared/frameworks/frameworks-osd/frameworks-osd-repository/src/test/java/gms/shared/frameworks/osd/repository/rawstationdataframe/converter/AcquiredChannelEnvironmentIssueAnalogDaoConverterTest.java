package gms.shared.frameworks.osd.repository.rawstationdataframe.converter;

import gms.shared.frameworks.osd.api.rawstationdataframe.AceiUpdates;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueAnalog;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueId;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import gms.shared.frameworks.osd.dao.channel.ChannelDao;
import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueAnalogDao;
import gms.shared.frameworks.osd.repository.SohPostgresTest;
import gms.shared.frameworks.osd.repository.rawstationdataframe.AcquiredChannelEnvironmentIssueRepositoryJpa;
import gms.shared.frameworks.osd.repository.station.StationRepositoryJpa;
import gms.shared.frameworks.osd.repository.util.CriteriaQueryUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Stream;

import static gms.shared.frameworks.osd.coi.dataacquisitionstatus.DataAcquisitionStatusTestFixtures.ACQUIRED_CHANNEL_SOH_ANALOG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Testcontainers
class AcquiredChannelEnvironmentIssueAnalogDaoConverterTest extends SohPostgresTest {

  @BeforeAll
  static void testSuiteSetup() {
    new StationRepositoryJpa(entityManagerFactory)
      .storeStations(List.of(UtilsTestFixtures.STATION));
  }

  @AfterEach
  void cleanUpData() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();

    try {
      entityManager.getTransaction().begin();
      entityManager.createQuery("delete from AcquiredChannelSohAnalogDao").executeUpdate();
      entityManager.getTransaction().commit();
    } catch (Exception ex) {
      entityManager.getTransaction().rollback();
    } finally {
      entityManager.close();
    }
  }


  @ParameterizedTest
  @MethodSource("getFromCoiArguments")
  void testFromCoiValidation(Class<? extends Exception> expectedException,
    AcquiredChannelEnvironmentIssueAnalog sohAnalog,
    EntityManager entityManager) {

    try {
      assertThrows(expectedException,
        () -> new AcquiredChannelEnvironmentIssueAnalogDaoConverter()
          .fromCoi(sohAnalog, entityManager));
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  static Stream<Arguments> getFromCoiArguments() {
    return Stream.of(
      arguments(NullPointerException.class, null, entityManagerFactory.createEntityManager()),
      arguments(NullPointerException.class, ACQUIRED_CHANNEL_SOH_ANALOG, null));
  }

  @Test
  void testFromCoiNew() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();

    try {
      entityManager.getTransaction().begin();

      AcquiredChannelEnvironmentIssueAnalogDao actual = new AcquiredChannelEnvironmentIssueAnalogDaoConverter()
        .fromCoi(ACQUIRED_CHANNEL_SOH_ANALOG, entityManager);

      assertEquals(ACQUIRED_CHANNEL_SOH_ANALOG.getChannelName(), actual.getChannelName());
      assertEquals(ACQUIRED_CHANNEL_SOH_ANALOG.getType(), actual.getType());
      assertEquals(ACQUIRED_CHANNEL_SOH_ANALOG.getStartTime(), actual.getStartTime());
      assertEquals(ACQUIRED_CHANNEL_SOH_ANALOG.getEndTime(), actual.getEndTime());
      assertEquals(ACQUIRED_CHANNEL_SOH_ANALOG.getStatus(), actual.getStatus(), 0.001);
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
        ACQUIRED_CHANNEL_SOH_ANALOG.getChannelName(), ACQUIRED_CHANNEL_SOH_ANALOG.getType(),
        ACQUIRED_CHANNEL_SOH_ANALOG.getStartTime());
      new AcquiredChannelEnvironmentIssueRepositoryJpa(entityManagerFactory)
        .syncAceiUpdates(AceiUpdates.from(ACQUIRED_CHANNEL_SOH_ANALOG));

      AcquiredChannelEnvironmentIssueAnalogDao expected = entityManager.createQuery(
          CriteriaQueryUtil.aceiByCoiIdQuery(entityManager, AcquiredChannelEnvironmentIssueAnalogDao.class,
            acquiredChannelEnvironmentIssueId))
        .getSingleResult();

      entityManager.getTransaction().begin();

      AcquiredChannelEnvironmentIssueAnalogDao actual = new AcquiredChannelEnvironmentIssueAnalogDaoConverter()
        .fromCoi(ACQUIRED_CHANNEL_SOH_ANALOG, entityManager);

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
      AcquiredChannelEnvironmentIssueAnalogDao expected = new AcquiredChannelEnvironmentIssueAnalogDao();
      expected.setChannel(entityManager.find(ChannelDao.class, ACQUIRED_CHANNEL_SOH_ANALOG.getChannelName()));
      expected.setChannelName(ACQUIRED_CHANNEL_SOH_ANALOG.getChannelName());
      expected.setType(ACQUIRED_CHANNEL_SOH_ANALOG.getType());
      expected.setStartTime(ACQUIRED_CHANNEL_SOH_ANALOG.getStartTime());
      expected.setEndTime(ACQUIRED_CHANNEL_SOH_ANALOG.getEndTime());
      expected.setStatus(ACQUIRED_CHANNEL_SOH_ANALOG.getStatus());

      AcquiredChannelEnvironmentIssueAnalog actual = new AcquiredChannelEnvironmentIssueAnalogDaoConverter()
        .toCoi(expected);

      assertEquals(expected.getChannelName(), actual.getChannelName());
      assertEquals(expected.getType(), actual.getType());
      assertEquals(expected.getStartTime(), actual.getStartTime());
      assertEquals(expected.getEndTime(), actual.getEndTime());
      assertEquals(expected.getStatus(), actual.getStatus(), 0.001);
    } finally {
      entityManager.close();
    }
  }
}