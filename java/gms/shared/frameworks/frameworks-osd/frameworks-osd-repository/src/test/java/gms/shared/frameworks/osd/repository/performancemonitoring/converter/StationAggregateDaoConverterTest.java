package gms.shared.frameworks.osd.repository.performancemonitoring.converter;

import gms.shared.frameworks.osd.coi.soh.DurationStationAggregate;
import gms.shared.frameworks.osd.coi.soh.PercentStationAggregate;
import gms.shared.frameworks.osd.coi.soh.StationAggregate;
import gms.shared.frameworks.osd.dao.soh.DurationStationAggregateDao;
import gms.shared.frameworks.osd.dao.soh.PercentStationAggregateDao;
import gms.shared.frameworks.osd.dao.soh.StationAggregateDao;
import gms.shared.frameworks.osd.repository.SohPostgresTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import java.util.Objects;
import java.util.stream.Stream;

import static gms.shared.frameworks.osd.coi.SohTestFixtures.LAG_STATION_AGGREGATE;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.MISSING_STATION_AGGREGATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class StationAggregateDaoConverterTest extends SohPostgresTest {

  @ParameterizedTest
  @MethodSource("getFromCoiArguments")
  void testFromCoiValidation(Class<? extends Exception> expectedException,
    StationAggregate coi,
    EntityManager entityManager) {
    try {
      assertThrows(expectedException,
        () -> new StationAggregateDaoConverter().fromCoi(coi, entityManager));
    } finally {
      if (entityManager != null) {
        entityManager.close();
      }
    }
  }

  static Stream<Arguments> getFromCoiArguments() {
    return Stream.of(
      Arguments.arguments(NullPointerException.class, null, entityManagerFactory.createEntityManager()),
      Arguments.arguments(NullPointerException.class, MISSING_STATION_AGGREGATE, null),
      Arguments
        .arguments(IllegalStateException.class, MISSING_STATION_AGGREGATE, entityManagerFactory.createEntityManager())
    );
  }

  @Test
  void testFromCoiPercentNew() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      entityManager.getTransaction().begin();
      StationAggregateDao dao = new StationAggregateDaoConverter()
        .fromCoi(MISSING_STATION_AGGREGATE, entityManager);
      assertNotNull(dao);
      assertTrue(dao instanceof PercentStationAggregateDao);

      PercentStationAggregateDao percentDao = (PercentStationAggregateDao) dao;
      assertEquals(MISSING_STATION_AGGREGATE.getAggregateType(), percentDao.getAggregateType());
      assertEquals(Objects.requireNonNull(MISSING_STATION_AGGREGATE.getValue()).get(), percentDao.getValue(), 0.00001);
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
      ;
      StationAggregateDao dao = new StationAggregateDaoConverter()
        .fromCoi(LAG_STATION_AGGREGATE, entityManager);
      assertNotNull(dao);
      assertTrue(dao instanceof DurationStationAggregateDao);

      DurationStationAggregateDao durationDao = (DurationStationAggregateDao) dao;
      assertEquals(LAG_STATION_AGGREGATE.getAggregateType(), durationDao.getAggregateType());
      assertEquals(LAG_STATION_AGGREGATE.getValue().orElseThrow(), durationDao.getValue());
    } finally {
      entityManager.getTransaction().rollback();
      entityManager.close();
    }
  }

  @Test
  void testToCoiValidation() {
    assertThrows(NullPointerException.class,
      () -> new StationAggregateDaoConverter().toCoi(null));
  }

  @Test
  void testToCoiPercent() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    StationAggregateDaoConverter converter = new StationAggregateDaoConverter();
    try {
      entityManager.getTransaction().begin();
      PercentStationAggregateDao expected = (PercentStationAggregateDao) converter
        .fromCoi(MISSING_STATION_AGGREGATE, entityManager);
      StationAggregate coi = converter.toCoi(expected);

      assertNotNull(coi);
      assertTrue(coi instanceof PercentStationAggregate);

      PercentStationAggregate percentCoi = (PercentStationAggregate) coi;
      assertEquals(expected.getAggregateType(), percentCoi.getAggregateType());
      assertEquals(expected.getValue(), percentCoi.getValue().get(), 0.0001);
    } finally {
      entityManager.getTransaction().rollback();
      entityManager.close();
    }
  }

  @Test
  void testToCoiDuration() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    StationAggregateDaoConverter converter = new StationAggregateDaoConverter();
    try {
      entityManager.getTransaction().begin();
      DurationStationAggregateDao expected = (DurationStationAggregateDao) converter
        .fromCoi(LAG_STATION_AGGREGATE, entityManager);

      StationAggregate coi = converter.toCoi(expected);

      assertNotNull(coi);
      assertTrue(coi instanceof DurationStationAggregate);

      DurationStationAggregate durationCoi = (DurationStationAggregate) coi;
      assertEquals(expected.getAggregateType(), durationCoi.getAggregateType());
      assertEquals(expected.getValue(), durationCoi.getValue().orElseThrow());
    } finally {
      entityManager.getTransaction().rollback();
      entityManager.close();
    }
  }
}