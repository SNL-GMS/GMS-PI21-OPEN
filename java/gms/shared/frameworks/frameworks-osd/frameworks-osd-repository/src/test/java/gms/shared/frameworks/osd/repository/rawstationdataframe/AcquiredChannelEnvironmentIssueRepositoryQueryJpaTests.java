package gms.shared.frameworks.osd.repository.rawstationdataframe;

import gms.shared.frameworks.osd.api.rawstationdataframe.AceiUpdates;
import gms.shared.frameworks.osd.api.rawstationdataframe.AcquiredChannelEnvironmentIssueRepositoryQueryInterface;
import gms.shared.frameworks.osd.api.station.StationRepositoryInterface;
import gms.shared.frameworks.osd.api.util.StationTimeRangeSohTypeRequest;
import gms.shared.frameworks.osd.api.util.TimeRangeRequest;
import gms.shared.frameworks.osd.coi.station.StationTestFixtures;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import gms.shared.frameworks.osd.dto.soh.DataPoint;
import gms.shared.frameworks.osd.dto.soh.DoubleOrInteger;
import gms.shared.frameworks.osd.dto.soh.LineSegment;
import gms.shared.frameworks.osd.repository.SohPostgresTest;
import gms.shared.frameworks.osd.repository.station.StationGroupRepositoryJpa;
import gms.shared.frameworks.osd.repository.station.StationRepositoryJpa;
import gms.shared.utilities.db.test.utils.TestFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static gms.shared.frameworks.osd.coi.dataacquisitionstatus.DataAcquisitionStatusTestFixtures.ACQUIRED_CHANNEL_SOH_ANALOG;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.EXAMPLE_STATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class AcquiredChannelEnvironmentIssueRepositoryQueryJpaTests extends SohPostgresTest {

  private AcquiredChannelEnvironmentIssueRepositoryJpa aceiRepository;
  private AcquiredChannelEnvironmentIssueRepositoryQueryInterface aceiQueryRepository;

  @BeforeAll
  static void beforeAll() {
    new StationRepositoryJpa(entityManagerFactory).storeStations(
      List.of(UtilsTestFixtures.STATION, TestFixtures.station));
    new StationGroupRepositoryJpa(entityManagerFactory).storeStationGroups(
      List.of(UtilsTestFixtures.STATION_GROUP, StationTestFixtures.getStationGroup()));
  }

  @BeforeEach
  void setUp() {
    StationRepositoryInterface stationRepository = new StationRepositoryJpa(entityManagerFactory);
    aceiRepository = new AcquiredChannelEnvironmentIssueRepositoryJpa(entityManagerFactory);
    aceiQueryRepository =
      new AcquiredChannelEnvironmentIssueRepositoryQueryJpa(entityManagerFactory, stationRepository);
  }

  @AfterEach
  void testCaseTeardown() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      entityManager.getTransaction().begin();
      Query query = entityManager.createNativeQuery("delete from gms_soh.channel_env_issue_analog");
      query.executeUpdate();
      query = entityManager.createNativeQuery("delete from gms_soh.channel_env_issue_boolean");
      query.executeUpdate();
      entityManager.getTransaction().commit();
    } finally {
      entityManager.close();
    }
  }

  @Test
  void testRetrieveAcquiredChannelEnvironmentIssuesByStationTimeRangeAndType() {
    aceiRepository.syncAceiUpdates(AceiUpdates.from(ACQUIRED_CHANNEL_SOH_ANALOG));

    StationTimeRangeSohTypeRequest request = StationTimeRangeSohTypeRequest.builder()
      .setStationName(EXAMPLE_STATION)
      .setTimeRange(TimeRangeRequest.create(ACQUIRED_CHANNEL_SOH_ANALOG.getStartTime().minusSeconds(1),
        ACQUIRED_CHANNEL_SOH_ANALOG.getEndTime().plusSeconds(1)))
      .setType(ACQUIRED_CHANNEL_SOH_ANALOG.getType()).build();

    var
      historicalACEIs =
      aceiQueryRepository.retrieveAcquiredChannelEnvironmentIssuesByStationTimeRangeAndType(request);

    assertEquals(1, historicalACEIs.size());

    var historicalACEI = historicalACEIs.get(0);

    assertEquals(ACQUIRED_CHANNEL_SOH_ANALOG.getChannelName(), historicalACEI.getChannelName());
    assertEquals(ACQUIRED_CHANNEL_SOH_ANALOG.getType().toString().toUpperCase(), historicalACEI.getMonitorType().toUpperCase());

    var dataPoints = historicalACEI.getTrendLine().stream()
      .map(LineSegment::getDataPoints)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());

    var statuses = dataPoints.stream()
      .map(DataPoint::getStatus)
      .distinct()
      .collect(Collectors.toList());

    assertEquals(1, statuses.size());

    var status = statuses.get(0);
    assertTrue(status instanceof DoubleOrInteger.DoubleValue);
    var dvStatus = (DoubleOrInteger.DoubleValue) status;
    assertEquals(ACQUIRED_CHANNEL_SOH_ANALOG.getStatus(), dvStatus.getValue());

    var timeStamps = dataPoints.stream()
      .map(DataPoint::getTimeStamp)
      .map(Instant::ofEpochMilli)
      .collect(Collectors.toList());

    var startTime = timeStamps.stream()
      .min(Instant::compareTo).orElseThrow();

    var endTime = timeStamps.stream()
      .max(Instant::compareTo).orElseThrow();

    assertEquals(ACQUIRED_CHANNEL_SOH_ANALOG.getStartTime().toEpochMilli(), startTime.toEpochMilli());
    assertEquals(ACQUIRED_CHANNEL_SOH_ANALOG.getEndTime().toEpochMilli(), endTime.toEpochMilli());
  }
}