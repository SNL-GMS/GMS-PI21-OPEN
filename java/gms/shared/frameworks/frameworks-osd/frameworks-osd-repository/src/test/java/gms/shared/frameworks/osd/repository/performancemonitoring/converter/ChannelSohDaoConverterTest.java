package gms.shared.frameworks.osd.repository.performancemonitoring.converter;

import gms.shared.frameworks.osd.coi.soh.ChannelSoh;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import gms.shared.frameworks.osd.dao.soh.ChannelSohDao;
import gms.shared.frameworks.osd.repository.SohPostgresTest;
import gms.shared.frameworks.osd.repository.station.StationRepositoryJpa;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gms.shared.frameworks.osd.coi.SohTestFixtures.BAD_MISSING_LAG_SEAL_CHANNEL_SOH;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.BAD_SEAL_BROKEN_SOH_MONITOR_VALUE_AND_STATUS;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.GOOD_MISSING_SOH_MONITOR_VALUE_AND_STATUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Testcontainers
class ChannelSohDaoConverterTest extends SohPostgresTest {

  @BeforeAll
  static void testSuiteSetup() {
    new StationRepositoryJpa(entityManagerFactory).storeStations(List.of(UtilsTestFixtures.STATION));
  }

  @AfterAll
  static void tearDown() {
    entityManagerFactory.close();
  }

  @ParameterizedTest
  @MethodSource("getFromCoiArguments")
  void testFromCoiValidation(Class<? extends Exception> expectedException,
    ChannelSoh channelSoh,
    EntityManager entityManager) {
    try {
      assertThrows(expectedException,
        () -> new ChannelSohDaoConverter().fromCoi(channelSoh, entityManager));
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  static Stream<Arguments> getFromCoiArguments() {
    return Stream.of(
      arguments(NullPointerException.class, null, entityManagerFactory.createEntityManager()),
      arguments(NullPointerException.class, BAD_MISSING_LAG_SEAL_CHANNEL_SOH, null),
      arguments(IllegalStateException.class,
        ChannelSoh.from("Unknown channel name",
          SohStatus.BAD,
          Set.of(GOOD_MISSING_SOH_MONITOR_VALUE_AND_STATUS,
            BAD_SEAL_BROKEN_SOH_MONITOR_VALUE_AND_STATUS)),
        entityManagerFactory.createEntityManager())
    );
  }

  @Test
  void testFromCoiNew() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      entityManager.getTransaction().begin();
      ChannelSoh expected = BAD_MISSING_LAG_SEAL_CHANNEL_SOH;
      ChannelSohDaoConverter converter = new ChannelSohDaoConverter();
      ChannelSohDao dao = converter.fromCoi(BAD_MISSING_LAG_SEAL_CHANNEL_SOH, entityManager);
      assertEquals(expected.getChannelName(), dao.getChannelName());
      assertEquals(expected.getSohStatusRollup(), dao.getSohStatus());
      dao.getAllMonitorValueAndStatuses()
        .forEach(smvsDao -> assertEquals(expected.getChannelName(), smvsDao.getChannelName()));
      Set convertedActualSVMS = dao.getAllMonitorValueAndStatuses()
        .stream()
        .map(svmsDao -> new ChannelSohMonitorValueAndStatusDaoConverter().toCoi(svmsDao))
        .collect(Collectors.toSet());
      assertEquals(expected.getAllSohMonitorValueAndStatuses(), convertedActualSVMS);
    } finally {
      entityManager.getTransaction().rollback();
      entityManager.close();
    }
  }

  @Test
  void testToCoiValidation() {
    var channelSohDaoConverter = new ChannelSohDaoConverter();
    assertThrows(NullPointerException.class, () -> channelSohDaoConverter.toCoi(null));
  }

  @Test
  void testToCoi() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      entityManager.getTransaction().begin();
      ChannelSohDao dao = new ChannelSohDao();
      dao.setSohStatus(BAD_MISSING_LAG_SEAL_CHANNEL_SOH.getSohStatusRollup());
      dao.setChannelName(BAD_MISSING_LAG_SEAL_CHANNEL_SOH.getChannelName());
      dao.setAllMonitorValueAndStatuses(BAD_MISSING_LAG_SEAL_CHANNEL_SOH.getAllSohMonitorValueAndStatuses()
        .stream()
        .map(smvs -> new ChannelSohMonitorValueAndStatusDaoConverter().fromCoi(smvs, entityManager))
        .collect(Collectors.toSet()));

      ChannelSoh actual = new ChannelSohDaoConverter().toCoi(dao);
      assertEquals(BAD_MISSING_LAG_SEAL_CHANNEL_SOH, actual);
    } finally {
      entityManager.getTransaction().rollback();
      entityManager.close();
    }
  }
}