package gms.shared.event.repository.connector;

import gms.shared.event.coi.type.DepthMethod;
import gms.shared.event.dao.LatLonDepthTimeKey;
import gms.shared.event.dao.OriginDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class OriginDatabaseConnectorTest extends DatabaseConnectorTest<OriginDatabaseConnector> {

  @Override
  protected OriginDatabaseConnector getDatabaseConnector(EntityManager entityManager) {
    return new OriginDatabaseConnector(entityManager);
  }

  @Test
  void testFindByEventIds() {

    //Test we throw an exception for invalid input
    assertThrows(Exception.class, () -> databaseConnector.findByEventIds(null));
    assertThrows(Exception.class, () -> databaseConnector.findByEventIds(List.of()));

    //Test an originId that does not exist
    assertTrue(databaseConnector.findByEventIds(List.of(Long.valueOf("23423"))).isEmpty());

    //makes sure we get both origin records back
    assertEquals(2,
      databaseConnector.findByEventIds(List.of(Long.valueOf("5234223423"), Long.valueOf("5234223422"))).size());

    // test try/catch
    var entityManagerFactory = Mockito.mock(EntityManagerFactory.class);
    var entityManager = Mockito.mock(EntityManager.class);
    var criteriaBuilder = Mockito.mock(CriteriaBuilder.class);
    var criteriaQuery = Mockito.mock(CriteriaQuery.class);
    var fromBase = Mockito.mock(Root.class);
    var predicate = Mockito.mock(Predicate.class);
    when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
    when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
    when(criteriaBuilder.createQuery(OriginDao.class)).thenReturn(criteriaQuery);
    when(criteriaQuery.from(OriginDao.class)).thenReturn(fromBase);
    when(criteriaBuilder.and(any(), any(), any())).thenReturn(predicate);
    when(criteriaBuilder.or(any())).thenReturn(predicate);
    when(criteriaQuery.select(any())).thenReturn(criteriaQuery);
    var originDatabaseConnector = new OriginDatabaseConnector(entityManager);
    var exception = new IllegalArgumentException("Test exception");
    when(entityManager.createQuery(criteriaQuery)).thenThrow(exception);
    assertEquals(Collections.emptyList(), originDatabaseConnector.findByEventIds(List.of(Long.valueOf("5234223423"))));

  }

  @ParameterizedTest
  @MethodSource("findByTimeProvider")
  void testFindByTime(Instant queryStartTime, Instant queryEndTime) {

    var expectedOriginDaos = List.of(
      new OriginDao.Builder()
        .withLatLonDepthTimeKey(
          new LatLonDepthTimeKey.Builder()
            .withLatitude(1)
            .withLongitude(1)
            .withDepth(2)
            .withTime(5.0)
            .build()
        )
        .withOriginId(42342342341L)
        .withEventId(5234223422L)
        .withJulianDate(1)
        .withNumAssociatedArrivals(1)
        .withNumTimeDefiningPhases(1)
        .withNumDepthPhases(1)
        .withGeographicRegionNumber(1)
        .withSeismicRegionNumber(1)
        .withEventType("etype")
        .withEstimatedDepth(4)
        .withDepthMethod(DepthMethod.A)
        .withBodyWaveMag(23)
        .withBodyWaveMagId(3423)
        .withSurfaceWaveMag(23)
        .withSurfaceWaveMagId(23434)
        .withLocalMag(32)
        .withLocalMagId(23434)
        .withLocationAlgorithm("algorithm")
        .withAuthor("auth")
        .withCommentId(234234242)
        .withLoadDate(Instant.ofEpochMilli(1619185740000L))
        .build()
    );

    var queriedOriginDaos = databaseConnector.findByTime(queryStartTime, queryEndTime);
    assertEquals(expectedOriginDaos, queriedOriginDaos);
    assertEquals(expectedOriginDaos.toString(), queriedOriginDaos.toString());
    assertEquals(expectedOriginDaos.hashCode(), queriedOriginDaos.hashCode());
  }

  private static Stream<Arguments> findByTimeProvider() {

    return Stream.of(
      // Test Event has time=5, stime=1
      // Event range is 4s - 6s

      // EventTime +- sTime lies entirely within query range
      Arguments.arguments(
        Instant.ofEpochSecond(3),
        Instant.ofEpochSecond(7)
      ),
      // EventTime - sTime lies on left bound of query range
      Arguments.arguments(
        Instant.ofEpochSecond(4),
        Instant.ofEpochSecond(7)
      ),
      // EventTime + sTime lies on right bound of query range
      Arguments.arguments(
        Instant.ofEpochSecond(3),
        Instant.ofEpochSecond(7)
      ),
      // query range is entirely inside EventTime +- sTime
      Arguments.arguments(
        Instant.ofEpochMilli(4500),
        Instant.ofEpochMilli(5500)
      ),
      // EventTime lies inside query range, EventTime - sTime lies outside the query range
      Arguments.arguments(
        Instant.ofEpochMilli(4500),
        Instant.ofEpochSecond(7)
      ),
      // EventTime lies inside query range, EventTime + sTime lies outside the query range
      Arguments.arguments(
        Instant.ofEpochSecond(4),
        Instant.ofEpochMilli(5500)
      )
    );
  }

  @ParameterizedTest
  @MethodSource("findByTimeEventNotFoundProvider")
  void testFindByTimeEventNotFound(Instant queryStartTime, Instant queryEndTime) {

    assertTrue(databaseConnector.findByTime(queryStartTime, queryEndTime).isEmpty());
  }

  private static Stream<Arguments> findByTimeEventNotFoundProvider() {
    return Stream.of(
      // Test Event has time=5, stime=1
      // Event range is 4s - 6s

      // Event +- sTime lies outside query range to the left
      Arguments.arguments(
        Instant.ofEpochSecond(8),
        Instant.ofEpochSecond(9)
      ),
      // EventTime +- sTime lies outside the query range to the right
      Arguments.arguments(
        Instant.ofEpochSecond(1),
        Instant.ofEpochSecond(2)
      )
    );
  }

  @Test
  void testFindByTimeInvalidInput() {

    assertThrows(Exception.class, () -> databaseConnector.findByTime(null, null));

    var time = Instant.now();
    assertThrows(Exception.class, () ->
      databaseConnector.findByTime(time, time.minus(5, ChronoUnit.HOURS)));
  }

  @Test
  void testFindById() {
    var originDaoOpt = assertDoesNotThrow(() -> databaseConnector.findById(42342342341L));
    assertTrue(originDaoOpt.isPresent());
  }
}
