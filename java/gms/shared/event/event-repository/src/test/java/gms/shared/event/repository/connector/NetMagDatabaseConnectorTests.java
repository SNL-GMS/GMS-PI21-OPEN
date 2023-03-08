package gms.shared.event.repository.connector;

import gms.shared.event.dao.NetMagDao;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class NetMagDatabaseConnectorTests extends DatabaseConnectorTest<NetMagDatabaseConnector> {

  private static final int ORIGIN_ID = 1111;

  private static final NetMagDao.Builder EXPECTED_NETMAG_DAO_BUILDER = new NetMagDao.Builder()
    .withOriginId(ORIGIN_ID)
    .withMagnitudeType("bb")
    .withNumberOfStations(10)
    .withMagnitudeUncertainty(1)
    .withAuthor("AUTH")
    .withCommentId(1234)
    .withLoadDate(Instant.parse("1980-04-23T13:49:00.00Z"));

  @Override
  protected NetMagDatabaseConnector getDatabaseConnector(EntityManager entityManager) {
    return new NetMagDatabaseConnector(entityManager);
  }

  @Test
  void testFindByOrid() {
    assertThat(databaseConnector.findNetMagByOrid(ORIGIN_ID)).containsExactly(
      EXPECTED_NETMAG_DAO_BUILDER
        .withMagnitudeId(1)
        .withNetwork("AA")
        .withEventId(2222)
        .withMagnitudeType("BB")
        .withMagnitude(1.0)
        .build(),
      EXPECTED_NETMAG_DAO_BUILDER
        .withMagnitudeId(2)
        .withNetwork("BB")
        .withEventId(1111)
        .withMagnitudeType("AA")
        .withMagnitude(2.0)
        .build());
  }

  @Test
  void testFindByOridMissing() {
    assertThat(databaseConnector.findNetMagByOrid(99999999)).isEmpty();
  }
}