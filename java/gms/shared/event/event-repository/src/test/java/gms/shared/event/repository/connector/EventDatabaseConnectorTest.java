package gms.shared.event.repository.connector;

import gms.shared.event.dao.EventDao;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventDatabaseConnectorTest extends DatabaseConnectorTest<EventDatabaseConnector> {

  @Override
  protected EventDatabaseConnector getDatabaseConnector(EntityManager entityManager) {
    return new EventDatabaseConnector(entityManager);
  }

  @Test
  void testFindEventById() {

    var expectedEventDao = new EventDao.Builder()
      .withEventId(1)
      .withEventName("EventOne")
      .withPreferredOrigin(1)
      .withAuthor("Something")
      .withCommentId(1)
      .withLoadDate(Instant.ofEpochMilli(1619185740000L))
      .build();

    var queriedEventDaoOpt = databaseConnector.findEventById(1);

    assertTrue(queriedEventDaoOpt.isPresent());
    assertEquals(expectedEventDao, queriedEventDaoOpt.get());
    assertEquals(expectedEventDao.toString(), queriedEventDaoOpt.get().toString());
    assertEquals(expectedEventDao.hashCode(), queriedEventDaoOpt.get().hashCode());
  }

  @Test
  void testFindEventByIdNoEvent() {

    assertFalse(databaseConnector.findEventById(99).isPresent());
  }

  @Test
  void testFindEventsByTime() {

    var queriedEventDaos = databaseConnector.findEventsByTime(Instant.ofEpochSecond(10001),
      Instant.ofEpochSecond(10002));
    assertEquals(2, queriedEventDaos.size());

    //test inclusive bounds
    queriedEventDaos = databaseConnector.findEventsByTime(Instant.ofEpochSecond(10005),
      Instant.ofEpochSecond(10003));

    assertEquals(1, queriedEventDaos.size());
    assertEquals(5, queriedEventDaos.get(0).getEventId());
  }

  @Test
  void testFindEventIdsByArids() {
    var arids = List.of(2L, 1L);
    var queriedEventIds = databaseConnector.findEventIdsByArids(arids);
    assertEquals(2, queriedEventIds.size());

    assertEquals(1111, queriedEventIds.get(0));
  }

}
