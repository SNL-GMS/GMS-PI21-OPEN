package gms.shared.frameworks.osd.repository.performancemonitoring;

import gms.shared.frameworks.osd.api.performancemonitoring.SohStatusChangeRepositoryInterface;
import gms.shared.frameworks.osd.coi.SohTestFixtures;
import gms.shared.frameworks.osd.coi.soh.quieting.UnacknowledgedSohStatusChange;
import gms.shared.frameworks.osd.coi.station.StationTestFixtures;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import gms.shared.frameworks.osd.repository.SohPostgresTest;
import gms.shared.frameworks.osd.repository.station.StationGroupRepositoryJpa;
import gms.shared.frameworks.osd.repository.station.StationRepositoryJpa;
import gms.shared.utilities.db.test.utils.TestFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.STATION;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SohStatusChangeRepositoryJpaTests extends SohPostgresTest {
  public static final UnacknowledgedSohStatusChange STATION_CLEAR =
    UnacknowledgedSohStatusChange.from(STATION.getName(), emptySet());
  private static SohStatusChangeRepositoryInterface statusChangePersistence;

  @BeforeAll
  static void testSuiteSetup() {
    statusChangePersistence = new SohStatusChangeRepositoryJpa(entityManagerFactory);

    new StationRepositoryJpa(entityManagerFactory).storeStations(
      List.of(UtilsTestFixtures.STATION, TestFixtures.station));
    new StationGroupRepositoryJpa(entityManagerFactory).storeStationGroups(
      List.of(UtilsTestFixtures.STATION_GROUP, StationTestFixtures.getStationGroup()));
  }

  @AfterEach
  void testCaseTearDown() {
    statusChangePersistence.storeUnacknowledgedSohStatusChange(List.of(STATION_CLEAR));
  }

  @Test
  void testStoreUnacknowledgedSohStatusChange_UpdatedStatusChanges() {
    statusChangePersistence.storeUnacknowledgedSohStatusChange(List.of(SohTestFixtures.UNACK_CHANGE_1));

    assertDoesNotThrow(
      () -> statusChangePersistence.storeUnacknowledgedSohStatusChange(List.of(SohTestFixtures.UNACK_CHANGE_2)));

    assertThat(
      statusChangePersistence.retrieveUnacknowledgedSohStatusChanges(List.of(UtilsTestFixtures.STATION.getName())))
      .containsExactlyInAnyOrder(SohTestFixtures.UNACK_CHANGE_2);
  }

  @Test
  void testStoreUnacknowledgedSohStatusChange_UpdatedStatusDeletes() {
    statusChangePersistence.storeUnacknowledgedSohStatusChange(List.of(SohTestFixtures.UNACK_CHANGE_1));

    assertDoesNotThrow(() -> statusChangePersistence.storeUnacknowledgedSohStatusChange(List.of(
      STATION_CLEAR)));

    assertThat(
      statusChangePersistence.retrieveUnacknowledgedSohStatusChanges(List.of(UtilsTestFixtures.STATION.getName())))
      .isEmpty();
  }
}