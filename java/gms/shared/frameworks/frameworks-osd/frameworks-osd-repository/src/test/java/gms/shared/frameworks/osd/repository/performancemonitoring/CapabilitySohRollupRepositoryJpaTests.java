package gms.shared.frameworks.osd.repository.performancemonitoring;

import gms.shared.frameworks.osd.api.performancemonitoring.CapabilitySohRollupRepositoryInterface;
import gms.shared.frameworks.osd.coi.SohTestFixtures;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.station.StationTestFixtures;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import gms.shared.frameworks.osd.repository.SohPostgresTest;
import gms.shared.frameworks.osd.repository.station.StationGroupRepositoryJpa;
import gms.shared.frameworks.osd.repository.station.StationRepositoryJpa;
import gms.shared.utilities.db.test.utils.TestFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CapabilitySohRollupRepositoryJpaTests extends SohPostgresTest {

  private static CapabilitySohRollupRepositoryInterface rollupPersistence;

  @BeforeAll
  static void testSuiteSetup() {
    rollupPersistence = new CapabilitySohRollupRepositoryJpa(entityManagerFactory);
    new StationRepositoryJpa(entityManagerFactory).storeStations(
      List.of(UtilsTestFixtures.STATION, TestFixtures.station));
    new StationGroupRepositoryJpa(entityManagerFactory).storeStationGroups(
      List.of(UtilsTestFixtures.STATION_GROUP, StationTestFixtures.getStationGroup()));
  }

  @BeforeEach
  void testCaseSetup() {
    rollupPersistence.storeCapabilitySohRollup(
      List.of(SohTestFixtures.BAD_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP));
  }

  @AfterEach
  void testCaseTearDown() {
  }

  @Test
  void testSoreCapabilitySohRollupAgain() {
    assertDoesNotThrow(() -> rollupPersistence.storeCapabilitySohRollup(
      List.of(SohTestFixtures.BAD_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP)));

    List<CapabilitySohRollup> storedRollups = rollupPersistence
      .retrieveCapabilitySohRollupByStationGroup(List.of(UtilsTestFixtures.STATION_GROUP.getName()));

    assertThat(storedRollups).containsExactlyInAnyOrder(
      SohTestFixtures.BAD_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP);
  }
}