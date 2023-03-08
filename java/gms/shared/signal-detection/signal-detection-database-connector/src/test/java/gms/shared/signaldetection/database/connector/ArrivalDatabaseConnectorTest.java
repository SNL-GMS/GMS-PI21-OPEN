package gms.shared.signaldetection.database.connector;

import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.utilities.bridge.database.connector.DatabaseConnectorException;
import gms.shared.utilities.bridge.database.converter.InstantToDoubleConverterNegativeNa;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.persistence.EntityManagerFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ArrivalDatabaseConnectorTest extends SignalDetectionDbTest<ArrivalDatabaseConnector> {
  private static final Instant START = Instant.now();
  private static final Instant END = Instant.now().plusSeconds(30);
  private static final Duration ZERO_DURATION = Duration.ZERO;
  private static final String STATION = "ASAR";
  private static final long ARID = 1L;

  @Override
  protected ArrivalDatabaseConnector getRepository(EntityManagerFactory entityManagerFactory) {
    return new ArrivalDatabaseConnector(entityManagerFactory);
  }

  @Test
  void testFindArrivalByAridPresent() {
    Optional<ArrivalDao> possibleArrival = repository.findArrivalByArid(59210057L);
    possibleArrival.ifPresentOrElse(arrival -> assertEquals(59210057L, arrival.getId()),
      () -> fail());
  }

  @Test
  void testFindArrivalByAridAbsent() {
    Optional<ArrivalDao> possibleArrival = repository.findArrivalByArid(17);
    assertTrue(possibleArrival.isEmpty());
  }

  @Test
  void testFindArrivalsByArids_aridsInput() {
    List<Long> arids = List.of(59210057L, 59210202L, 59210470L);

    final List<ArrivalDao> arrivalDaos = repository
      .findArrivalsByArids(arids);
    arrivalDaos.forEach(dao -> System.out.println(dao.toString()));

    assertThat(arrivalDaos, CoreMatchers.notNullValue());

    assertEquals(arids.size(), arrivalDaos.size());
    arrivalDaos.forEach(arrivalDao ->
      assertTrue(arids.contains(arrivalDao.getId())));
  }

  @Test
  void testFindArrivalsByArids_nullAridsInput_emptyResult() {
    assertErrorThrown(NullPointerException.class,
      ArrivalDatabaseConnector.EMPTY_ARID_LIST_ERROR,
      () -> repository.findArrivalsByArids(null));
  }

  @Test
  void testFindArrivalsByArids_emptyAridsInput_emptyResult() {
    final List<ArrivalDao> arrivalDaos = repository
      .findArrivalsByArids(List.of());

    assertThat(arrivalDaos, CoreMatchers.notNullValue());
    assertTrue(arrivalDaos.isEmpty());
  }

  @Test
  void testFindArrivalsByStationNamesAridsAndTimeRange_stationNamesAridsTimeRangeInput()
    throws DatabaseConnectorException {
    // create time variables for time range query
    final Instant time = new InstantToDoubleConverterNegativeNa()
      .convertToEntityAttribute(1274385713.0);
    Instant offdate = time.plusSeconds(600);
    Duration deltaTime = Duration.ofSeconds(60);
    List<String> stationNames = List.of("AKASG", "ASAR", "MKAR");

    // Test expects to have arids: [59210058, 59210059, 59210061, 59210057]
    List<Long> excludedArids = List.of(59210058L);

    final List<ArrivalDao> arrivalDaos = repository
      .findArrivals(stationNames, excludedArids, time, offdate, deltaTime, deltaTime);

    assertThat(arrivalDaos, CoreMatchers.notNullValue());
    assertEquals(3, arrivalDaos.size());
    arrivalDaos.forEach(arr -> assertTrue(stationNames.contains(arr.getArrivalKey().getStationCode())));
    arrivalDaos.forEach(arr -> assertFalse(excludedArids.contains(arr.getId())));
  }

  @Test
  void testFindArrivalsByStationNamesAridsAndTimeRange_nullStationListInput_emptyResult() {
    assertErrorThrown(NullPointerException.class,
      ArrivalDatabaseConnector.EMPTY_STATION_NAME_LIST_ERROR,
      () -> repository.findArrivals(null, List.of(), START, END, ZERO_DURATION, ZERO_DURATION));
  }

  @Test
  void testFindArrivalsByStationNamesAridsAndTimeRange_emptyStationListInput_emptyResult() {
    final List<ArrivalDao> arrivalDaos = repository
      .findArrivals(List.of(), List.of(), START, END, ZERO_DURATION, ZERO_DURATION);

    assertThat(arrivalDaos, CoreMatchers.notNullValue());
    assertTrue(arrivalDaos.isEmpty());
  }

  @Test
  void testFindArrivalsByStationNamesAridsAndTimeRange_nullAridListInput_emptyResult() {
    assertErrorThrown(NullPointerException.class,
      ArrivalDatabaseConnector.EMPTY_EXCLUDED_ARID_LIST_ERROR,
      () -> repository.findArrivals(List.of(STATION), null, START, END, ZERO_DURATION, ZERO_DURATION));
  }

  @Test
  void testFindArrivalsByStationNamesAridsAndTimeRange_emptyAridListInput_emptyResult() {
    final List<ArrivalDao> arrivalDaos = repository
      .findArrivals(List.of(STATION), List.of(), START, END, ZERO_DURATION, ZERO_DURATION);

    assertThat(arrivalDaos, CoreMatchers.notNullValue());
    assertTrue(arrivalDaos.isEmpty());
  }

  @Test
  void testFindArrivalsByStationNamesAridsAndTimeRange_nullStartTimeInput_emptyResult() {
    assertErrorThrown(NullPointerException.class,
      ArrivalDatabaseConnector.MISSING_START_TIME_ERROR, () -> repository
        .findArrivals(List.of(STATION), List.of(ARID), null, END, ZERO_DURATION, ZERO_DURATION)
    );
  }

  @Test
  void testFindArrivalsByStationNamesAridsAndTimeRange_nullEndTimeInput_emptyResult() {
    assertErrorThrown(NullPointerException.class,
      ArrivalDatabaseConnector.MISSING_END_TIME_ERROR, () -> repository
        .findArrivals(List.of(STATION), List.of(ARID), START, null, ZERO_DURATION, ZERO_DURATION)
    );
  }

  @Test
  void testFindArrivalsByStationNamesAridsAndTimeRange_nullLeadDelta_emptyResult() {
    assertErrorThrown(NullPointerException.class,
      ArrivalDatabaseConnector.MISSING_LEAD_DELTA_ERROR, () -> repository
        .findArrivals(List.of(STATION), List.of(ARID), START, END, null, ZERO_DURATION)
    );
  }

  @Test
  void testFindArrivalsByStationNamesAridsAndTimeRange_nullLagDelta_emptyResult() {
    assertErrorThrown(NullPointerException.class,
      ArrivalDatabaseConnector.MISSING_LAG_DELTA_ERROR, () -> repository
        .findArrivals(List.of(STATION), List.of(ARID), START, END, ZERO_DURATION, null)
    );
  }

  @ParameterizedTest
  @MethodSource("findArrivalsArgumentsSupplier")
  void testFindArrivals_badInputs(Class<Exception> exceptionClass, List<String> stationNames,
    List<Long> excludedArids, Instant startTime, Instant endTime,
    Duration timeDelta, String errorMessage) {
    assertThrows(exceptionClass, () -> repository.findArrivals(stationNames, excludedArids,
      startTime, endTime, timeDelta, timeDelta), errorMessage);
  }

  static Stream<Arguments> findArrivalsArgumentsSupplier() {
    final Instant timeRangeBoundary = Instant.parse("2000-05-20T00:00:00Z");
    final Collection<String> stationNames = List.of(STATION);
    final Collection<Long> arids = List.of(ARID);
    return Stream.of(arguments(NullPointerException.class, stationNames,
        arids, null, timeRangeBoundary, ZERO_DURATION,
        ArrivalDatabaseConnector.MISSING_START_TIME_ERROR),
      arguments(NullPointerException.class, stationNames,
        arids, timeRangeBoundary, null, ZERO_DURATION,
        ArrivalDatabaseConnector.MISSING_END_TIME_ERROR),
      arguments(IllegalArgumentException.class, stationNames,
        arids, timeRangeBoundary.plusSeconds(10),
        timeRangeBoundary, ZERO_DURATION,
        ArrivalDatabaseConnector.START_NOT_BEFORE_END_TIME_ERROR),
      arguments(IllegalArgumentException.class, stationNames,
        arids, timeRangeBoundary,
        timeRangeBoundary.minusSeconds(10), ZERO_DURATION,
        ArrivalDatabaseConnector.START_NOT_BEFORE_END_TIME_ERROR));
  }

  @Test
  void testFindArrivalsByTimeRange_timeRangeInput()
    throws DatabaseConnectorException {
    // create time variables for time range query
    final Instant time = new InstantToDoubleConverterNegativeNa()
      .convertToEntityAttribute(1595389700.025);
    Instant offdate = time.plusSeconds(300);

    final List<ArrivalDao> arrivalDaos = repository
      .findArrivalsByTimeRange(time, offdate);

    assertThat(arrivalDaos, CoreMatchers.notNullValue());
    assertEquals(3, arrivalDaos.size());
  }

  @ParameterizedTest
  @MethodSource("findArrivalsByTimeRangeArgumentsSupplier")
  void testFindArrivalsByTimeRange_badInputs(Class<Exception> exceptionClass,
    Instant startTime, Instant endTime, String errorMessage) {
    assertThrows(exceptionClass, () -> repository.findArrivalsByTimeRange(
      startTime, endTime), errorMessage);
  }

  static Stream<Arguments> findArrivalsByTimeRangeArgumentsSupplier() {
    final Instant timeRangeBoundary = Instant.parse("2000-05-20T00:00:00Z");
    return Stream.of(arguments(NullPointerException.class,
        null, timeRangeBoundary, ArrivalDatabaseConnector.MISSING_START_TIME_ERROR),
      arguments(NullPointerException.class, timeRangeBoundary, null,
        ArrivalDatabaseConnector.MISSING_END_TIME_ERROR),
      arguments(IllegalArgumentException.class,
        timeRangeBoundary.plusSeconds(10),
        timeRangeBoundary,
        ArrivalDatabaseConnector.START_NOT_BEFORE_END_TIME_ERROR),
      arguments(IllegalArgumentException.class,
        timeRangeBoundary,
        timeRangeBoundary.minusSeconds(10),
        ArrivalDatabaseConnector.START_NOT_BEFORE_END_TIME_ERROR));
  }
}
