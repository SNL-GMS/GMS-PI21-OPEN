package gms.shared.signaldetection.database.connector;

import gms.shared.signaldetection.dao.css.AmplitudeDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class AmplitudeDatabaseConnectorTest extends SignalDetectionDbTest<AmplitudeDatabaseConnector> {

  @Override
  protected AmplitudeDatabaseConnector getRepository(EntityManagerFactory entityManagerFactory) {
    return new AmplitudeDatabaseConnector(entityManagerFactory);
  }

  @Test
  void testFindAmplitudesbyAridsValidation() {
    NullPointerException ex = assertThrows(NullPointerException.class,
      () -> repository.findAmplitudesByArids(null));
    assertEquals("Arids cannot be null", ex.getMessage());

  }

  @ParameterizedTest
  @MethodSource("getFindAmplitudesbyAridsArguments")
  void testFindAmplitudesByArids(List<Long> arids, long expectedResultSize) {
    List<AmplitudeDao> amplitudeDaos = repository.findAmplitudesByArids(arids);
    assertEquals(expectedResultSize, amplitudeDaos.size());
  }

  static Stream<Arguments> getFindAmplitudesbyAridsArguments() {
    return Stream.of(arguments(List.of(), 0),
      arguments(List.of(2, 3), 2),
      arguments(List.of(3, 5), 1));
  }

}