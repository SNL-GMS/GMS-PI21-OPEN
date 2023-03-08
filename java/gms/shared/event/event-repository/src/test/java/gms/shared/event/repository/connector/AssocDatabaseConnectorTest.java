package gms.shared.event.repository.connector;

import gms.shared.signaldetection.dao.css.AssocDao;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class AssocDatabaseConnectorTest extends DatabaseConnectorTest<AssocDatabaseConnector> {

  @Override
  protected AssocDatabaseConnector getDatabaseConnector(EntityManager entityManager) {
    return new AssocDatabaseConnector(entityManager);
  }

  @Test
  void testFindAssocsByAridValidation() {
    Exception ex = assertThrows(NullPointerException.class,
      () -> databaseConnector.findAssocsByArids(null));
    assertEquals("Arids cannot be null", ex.getMessage());
  }

  @ParameterizedTest
  @MethodSource("getFindAssocsByAridArguments")
  void testFindAssocsByArid(List<Long> arids,
    long expectedResultSize) {

    List<AssocDao> assocs = assertDoesNotThrow(() -> databaseConnector.findAssocsByArids(arids));
    assertEquals(expectedResultSize, assocs.size());
  }

  static Stream<Arguments> getFindAssocsByAridArguments() {
    return Stream.of(arguments(List.of(), 0),
      arguments(List.of(11, 12), 2),
      arguments(List.of(12, 14), 1));
  }

  @Test
  void testFindAssocsByOrid() {
    //test validation case
    assertThrows(NullPointerException.class, () -> databaseConnector.findAssocsByOrids(null));

    //test empty list
    var foundAssocs = databaseConnector.findAssocsByOrids(List.of());
    assertEquals(Collections.emptyList(), foundAssocs);

    //test we can pull back actual Assoc records (note 4l is not a valid ORID)
    foundAssocs = databaseConnector.findAssocsByOrids(List.of(11L, 12L, 13L, 14L));
    assertEquals(3, foundAssocs.size());
  }

  @Test
  void testFindAssocsByAridsAndOrid() {
    //test validation case
    assertThrows(NullPointerException.class, () -> databaseConnector.findAssocsByAridsAndOrids(null));

    //test empty list
    var foundAssocs = databaseConnector.findAssocsByAridsAndOrids(List.of());
    assertEquals(Collections.emptyList(), foundAssocs);

    //test we can pull back actual Assoc records (note 4l is not a valid ORID)
    foundAssocs = databaseConnector.findAssocsByAridsAndOrids(List.of(Pair.of(11L, 11L),
      Pair.of(12L, 12L),
      Pair.of(13L, 13L),
      Pair.of(14L, 14L)));
    assertEquals(3, foundAssocs.size());
  }
}