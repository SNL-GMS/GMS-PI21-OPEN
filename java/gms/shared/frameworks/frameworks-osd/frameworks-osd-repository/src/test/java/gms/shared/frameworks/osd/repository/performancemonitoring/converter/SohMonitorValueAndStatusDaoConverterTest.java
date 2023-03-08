package gms.shared.frameworks.osd.repository.performancemonitoring.converter;

import gms.shared.frameworks.osd.coi.soh.DurationSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.PercentSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType.SohValueType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorValueAndStatus;
import gms.shared.frameworks.osd.dao.soh.SohMonitorValueAndStatusDao;
import gms.shared.frameworks.osd.repository.SohPostgresTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import java.time.Duration;
import java.util.Objects;
import java.util.stream.Stream;

import static gms.shared.frameworks.osd.coi.SohTestFixtures.MARGINAL_LAG_SOH_MONITOR_VALUE_AND_STATUS;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.MARGINAL_MISSING_EMPTY_SOH_MONITOR_VALUE_AND_STATUS;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.MARGINAL_MISSING_SOH_MONITOR_VALUE_AND_STATUS;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.MARGINAL_NULL_LAG_SOH_MONITOR_VALUE_AND_STATUS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Testcontainers
class SohMonitorValueAndStatusDaoConverterTest extends SohPostgresTest {

  @ParameterizedTest
  @MethodSource("getFromCoiArguments")
  void testFromCoiValidation(Class<? extends Exception> expectedException,
    SohMonitorValueAndStatus coi,
    EntityManager entityManager) {
    try {
      assertThrows(expectedException,
        () -> new StationSohMonitorValueAndStatusDaoConverter().fromCoi(coi, entityManager));
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  static Stream<Arguments> getFromCoiArguments() {
    return Stream.of(
      arguments(NullPointerException.class, null, entityManagerFactory.createEntityManager()),
      arguments(NullPointerException.class, MARGINAL_MISSING_SOH_MONITOR_VALUE_AND_STATUS, null),
      arguments(IllegalStateException.class, MARGINAL_MISSING_SOH_MONITOR_VALUE_AND_STATUS, entityManagerFactory
        .createEntityManager())
    );
  }

  @Test
  void testFromCoiPercentNew() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      entityManager.getTransaction().begin();
      SohMonitorValueAndStatusDao dao = new StationSohMonitorValueAndStatusDaoConverter()
        .fromCoi(MARGINAL_MISSING_SOH_MONITOR_VALUE_AND_STATUS, entityManager);
      assertNotNull(dao);
      assertSame(SohValueType.PERCENT, dao.getMonitorType().getSohValueType());

      assertEquals(MARGINAL_MISSING_SOH_MONITOR_VALUE_AND_STATUS.getMonitorType(),
        dao.getMonitorType());
      assertEquals(MARGINAL_MISSING_SOH_MONITOR_VALUE_AND_STATUS.getStatus(),
        dao.getStatus());
      assertEquals(Objects.requireNonNull(MARGINAL_MISSING_SOH_MONITOR_VALUE_AND_STATUS.getValue()).get(),
        dao.getPercent(), 0.00001);
    } finally {
      entityManager.getTransaction().rollback();
      entityManager.close();
    }
  }

  @Test
  void testFromCoiDurationNew() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();

    try {
      entityManager.getTransaction().begin();
      SohMonitorValueAndStatusDao dao = new StationSohMonitorValueAndStatusDaoConverter()
        .fromCoi(MARGINAL_LAG_SOH_MONITOR_VALUE_AND_STATUS, entityManager);
      assertNotNull(dao);
      assertSame(SohValueType.DURATION, dao.getMonitorType().getSohValueType());

      assertEquals(MARGINAL_LAG_SOH_MONITOR_VALUE_AND_STATUS.getMonitorType(),
        dao.getMonitorType());
      assertEquals(MARGINAL_LAG_SOH_MONITOR_VALUE_AND_STATUS.getStatus(),
        dao.getStatus());
      assertEquals(MARGINAL_LAG_SOH_MONITOR_VALUE_AND_STATUS.getValue().orElseThrow(),
        Duration.ofSeconds(dao.getDuration()));
    } finally {
      entityManager.getTransaction().rollback();
      entityManager.close();
    }
  }

  @Test
  void testToCoiValidation() {
    var converter = new StationSohMonitorValueAndStatusDaoConverter();
    assertThrows(NullPointerException.class,
      () -> converter.toCoi(null));
  }

  @Test
  void testToCoiPercent() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    var converter = new StationSohMonitorValueAndStatusDaoConverter();
    try {
      entityManager.getTransaction().begin();
      var expected = converter
        .fromCoi(MARGINAL_MISSING_SOH_MONITOR_VALUE_AND_STATUS, entityManager);

      SohMonitorValueAndStatus coi = converter.toCoi(expected);

      assertNotNull(coi);
      assertTrue(coi instanceof PercentSohMonitorValueAndStatus);

      PercentSohMonitorValueAndStatus percentCoi = (PercentSohMonitorValueAndStatus) coi;
      assertEquals(expected.getMonitorType(), percentCoi.getMonitorType());
      assertEquals(expected.getStatus(), percentCoi.getStatus());
      assertEquals(expected.getPercent(), percentCoi.getValue().get(), 0.0001);
    } finally {
      entityManager.getTransaction().rollback();
      entityManager.close();
    }
  }

  @Test
  void testToCoiDuration() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    var converter = new StationSohMonitorValueAndStatusDaoConverter();
    try {
      entityManager.getTransaction().begin();
      var expected = converter
        .fromCoi(MARGINAL_LAG_SOH_MONITOR_VALUE_AND_STATUS, entityManager);

      SohMonitorValueAndStatus coi = converter.toCoi(expected);

      assertNotNull(coi);
      assertTrue(coi instanceof DurationSohMonitorValueAndStatus);

      DurationSohMonitorValueAndStatus durationCoi = (DurationSohMonitorValueAndStatus) coi;
      assertEquals(expected.getMonitorType(), durationCoi.getMonitorType());
      assertEquals(expected.getStatus(), durationCoi.getStatus());
      assertEquals(Long.valueOf(expected.getDuration()), durationCoi.getValue().orElseThrow().getSeconds());
    } finally {
      entityManager.getTransaction().rollback();
      entityManager.close();
    }
  }

  @Test
  void testToCoiPercentNullValue() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    var converter = new StationSohMonitorValueAndStatusDaoConverter();
    try {
      entityManager.getTransaction().begin();
      var expected = converter
        .fromCoi(MARGINAL_MISSING_EMPTY_SOH_MONITOR_VALUE_AND_STATUS, entityManager);

      SohMonitorValueAndStatus coi = converter.toCoi(expected);

      assertNotNull(coi);
      assertTrue(coi instanceof PercentSohMonitorValueAndStatus);

      PercentSohMonitorValueAndStatus percentCoi = (PercentSohMonitorValueAndStatus) coi;
      assertEquals(expected.getMonitorType(), percentCoi.getMonitorType());
      assertEquals(expected.getStatus(), percentCoi.getStatus());
      assertTrue(percentCoi.getValue().isEmpty());
    } finally {
      entityManager.getTransaction().rollback();
      entityManager.close();
    }

  }

  @Test
  void testToCoiDurationNullValue() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    var converter = new StationSohMonitorValueAndStatusDaoConverter();
    try {
      entityManager.getTransaction().begin();
      var expected = converter
        .fromCoi(MARGINAL_NULL_LAG_SOH_MONITOR_VALUE_AND_STATUS, entityManager);

      SohMonitorValueAndStatus coi = converter.toCoi(expected);

      assertNotNull(coi);
      assertTrue(coi instanceof DurationSohMonitorValueAndStatus);

      DurationSohMonitorValueAndStatus durationCoi = (DurationSohMonitorValueAndStatus) coi;
      assertEquals(expected.getMonitorType(), durationCoi.getMonitorType());
      assertEquals(expected.getStatus(), durationCoi.getStatus());
      assertTrue(durationCoi.getValue().isEmpty());
    } finally {
      entityManager.getTransaction().rollback();
      entityManager.close();
    }
  }

}