package gms.shared.frameworks.osd.repository.performancemonitoring.converter;

import gms.shared.frameworks.osd.coi.soh.DurationSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.PercentSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.dao.soh.ChannelSohMonitorValueAndStatusDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChannelSohMonitorValueAndStatusDaoConverterTest {

  private ChannelSohMonitorValueAndStatusDaoConverter converter;

  @BeforeEach
  void setup() {
    converter = new ChannelSohMonitorValueAndStatusDaoConverter();
  }

  @ParameterizedTest
  @MethodSource("getFromCoiValidationArguments")
  void testFromCoiValidation(Class<? extends Exception> expectedException,
    SohMonitorValueAndStatus coi,
    EntityManager entityManager) {
    assertThrows(expectedException, () -> converter.fromCoi(coi, entityManager));
  }

  static Stream<Arguments> getFromCoiValidationArguments() {
    var entityManager = mock(EntityManager.class);
    var transaction = mock(EntityTransaction.class);
    when(entityManager.getTransaction()).thenReturn(transaction);
    when(transaction.isActive()).thenReturn(false);

    var entityManagerActive = mock(EntityManager.class);
    var transactionActive = mock(EntityTransaction.class);
    when(entityManagerActive.getTransaction()).thenReturn(transactionActive);
    when(transactionActive.isActive()).thenReturn(true);
    return Stream.of(arguments(NullPointerException.class, null, mock(EntityManager.class)),
      arguments(NullPointerException.class, mock(SohMonitorValueAndStatus.class), null),
      arguments(IllegalStateException.class, mock(SohMonitorValueAndStatus.class), entityManager),
      arguments(IllegalArgumentException.class, mock(SohMonitorValueAndStatus.class), entityManagerActive));
  }

  @ParameterizedTest
  @MethodSource("getFromCoiArguments")
  void testFromCoi(SohMonitorValueAndStatus coi,
    EntityManager em) {
    var actual = converter.fromCoi(coi, em);

    if (coi instanceof PercentSohMonitorValueAndStatus) {
      ((PercentSohMonitorValueAndStatus) coi).getValue().ifPresentOrElse(val -> assertEquals(val, actual.getPercent().doubleValue(), 0.0001),
        () -> fail());
      assertNull(actual.getDuration());
    } else {
      ((DurationSohMonitorValueAndStatus) coi).getValue()
        .ifPresentOrElse(val -> assertEquals(val, Duration.ofSeconds(actual.getDuration())),
          () -> fail());
      assertNull(actual.getPercent());
    }

    assertEquals(coi.getMonitorType(), actual.getMonitorType());
    assertEquals(coi.getStatus(), actual.getStatus());
  }

  static Stream<Arguments> getFromCoiArguments() {
    var entityManagerActive = mock(EntityManager.class);
    var transactionActive = mock(EntityTransaction.class);
    when(entityManagerActive.getTransaction()).thenReturn(transactionActive);
    when(transactionActive.isActive()).thenReturn(true);

    SohMonitorValueAndStatus<Double> percentSmvs = PercentSohMonitorValueAndStatus.from(0.0,
      SohStatus.GOOD,
      SohMonitorType.MISSING);

    SohMonitorValueAndStatus<Duration> durationSmvs = DurationSohMonitorValueAndStatus.from(Duration.ofSeconds(10),
      SohStatus.BAD,
      SohMonitorType.LAG);

    return Stream.of(arguments(percentSmvs, entityManagerActive),
      arguments(durationSmvs, entityManagerActive));
  }

  @ParameterizedTest
  @MethodSource("getToCoiValidationArguments")
  void testToCoiValidation(Class<? extends Exception> expectedException, ChannelSohMonitorValueAndStatusDao dao) {
    assertThrows(expectedException, () -> converter.toCoi(dao));
  }

  static Stream<Arguments> getToCoiValidationArguments() {
    ChannelSohMonitorValueAndStatusDao invalidDao = new ChannelSohMonitorValueAndStatusDao();
    invalidDao.setMonitorType(SohMonitorType.ENV_TIMELY_DATA_AVAILABILITY);

    return Stream.of(arguments(NullPointerException.class, null),
      arguments(IllegalArgumentException.class, invalidDao));
  }

  @ParameterizedTest
  @MethodSource("getToCoiArguments")
  void testToCoi(ChannelSohMonitorValueAndStatusDao dao) {
    SohMonitorValueAndStatus<?> smvs = converter.toCoi(dao);
    if (dao.getPercent() != null) {
      assertTrue(smvs instanceof PercentSohMonitorValueAndStatus);
      ((PercentSohMonitorValueAndStatus) smvs).getValue()
        .ifPresentOrElse(val -> assertEquals(dao.getPercent().doubleValue(), val, 0.0001), () -> fail());
    } else {
      assertTrue(smvs instanceof DurationSohMonitorValueAndStatus);
      ((DurationSohMonitorValueAndStatus) smvs).getValue()
        .ifPresentOrElse(val -> assertEquals(Duration.ofSeconds(dao.getDuration()), val), () -> fail());
    }

    assertEquals(dao.getStatus(), smvs.getStatus());
    assertEquals(dao.getMonitorType(), smvs.getMonitorType());
  }

  static Stream<Arguments> getToCoiArguments() {
    var percentCsmvs = new ChannelSohMonitorValueAndStatusDao();
    percentCsmvs.setPercent(90.0f);
    percentCsmvs.setMonitorType(SohMonitorType.MISSING);
    percentCsmvs.setStatus(SohStatus.MARGINAL);

    var durationCsmvs = new ChannelSohMonitorValueAndStatusDao();
    durationCsmvs.setDuration(3234);
    durationCsmvs.setMonitorType(SohMonitorType.LAG);
    durationCsmvs.setStatus(SohStatus.GOOD);
    return Stream.of(arguments(percentCsmvs),
      arguments(durationCsmvs));
  }

}