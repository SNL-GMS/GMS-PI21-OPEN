package gms.shared.frameworks.osd.repository.rawstationdataframe;

import gms.shared.frameworks.osd.api.rawstationdataframe.RawStationDataFrameRepositoryInterface;
import gms.shared.frameworks.osd.api.util.StationTimeRangeRequest;
import gms.shared.frameworks.osd.api.util.TimeRangeRequest;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import gms.shared.frameworks.osd.dao.transferredfile.RawStationDataFrameDao;
import gms.shared.frameworks.osd.repository.SohPostgresTest;
import gms.shared.frameworks.osd.repository.util.TestFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RawStationDataFrameRepositoryJpaTests extends SohPostgresTest {

  private static RawStationDataFrameRepositoryInterface dataFramePersistence;

  @BeforeAll
  static void testSuiteSetup() {
    dataFramePersistence = new RawStationDataFrameRepositoryJpa(entityManagerFactory);
  }

  @BeforeEach
  void testCaseSetUp() {
    // store 2 frames for further testing
    dataFramePersistence.storeRawStationDataFrames(List.of(TestFixtures.frame1,
      TestFixtures.frame2));
  }

  @AfterEach
  void testCaseTearDown() {
    EntityManager em = entityManagerFactory.createEntityManager();
    List<RawStationDataFrameDao> daos = Stream.of(TestFixtures.frame1.getId(),
        TestFixtures.frame2.getId())
      .map(id -> em.find(RawStationDataFrameDao.class, id))
      .collect(Collectors.toList());
    em.getTransaction().begin();
    for (var dao : daos) {
      em.remove(dao);
    }
    em.getTransaction().commit();
  }

  @Test
  void testStoreFrame1Again() {
    assertDoesNotThrow(() -> dataFramePersistence.storeRawStationDataFrames(List.of(TestFixtures.frame1)));
  }

  @Test
  void retrieveByStationIdTest() {
    // Query for frame1's id with time range [frame1.start, frame2.end], should only find frame1
    // since the id of frame2 is different.
    List<RawStationDataFrame> results =
      dataFramePersistence.retrieveRawStationDataFramesByStationAndTime(
        StationTimeRangeRequest.create(TestFixtures.frame1.getMetadata().getStationName(),
          TestFixtures.SEGMENT_START, TestFixtures.SEGMENT_END2));
    assertEquals(Stream.of(TestFixtures.frame1).map(RawStationDataFrame::getMetadata).collect(Collectors.toList()),
      results.stream().map(RawStationDataFrame::getMetadata).collect(Collectors.toList()));

    // Query for frame2's id with time range [frame1.start, frame2.end], should only find frame2
    // since the id of frame1 is different.
    results = dataFramePersistence.retrieveRawStationDataFramesByStationAndTime(
      StationTimeRangeRequest.create(TestFixtures.frame2.getMetadata().getStationName(),
        TestFixtures.SEGMENT_START, TestFixtures.SEGMENT_END2));
    assertEquals(Stream.of(TestFixtures.frame2).map(RawStationDataFrame::getMetadata).collect(Collectors.toList()),
      results.stream().map(RawStationDataFrame::getMetadata).collect(Collectors.toList()));

    // query for frame1's id with time range [frame1.start - 1, frame2.end],
    // finds frame 1 since it has some data for the first second of the range
    // (and frame2's id differs)
    results = dataFramePersistence.retrieveRawStationDataFramesByStationAndTime(
      StationTimeRangeRequest.create(TestFixtures.frame1.getMetadata().getStationName(),
        TestFixtures.SEGMENT1_END.minusSeconds(1), TestFixtures.SEGMENT_END2));
    assertEquals(Stream.of(TestFixtures.frame1).map(RawStationDataFrame::getMetadata).collect(Collectors.toList()),
      results.stream().map(RawStationDataFrame::getMetadata).collect(Collectors.toList()));

    // query for a fake name, with time range [frame1.start, frame2.end],
    // finds nothing because the id is no good.
    results = dataFramePersistence.retrieveRawStationDataFramesByStationAndTime(
      StationTimeRangeRequest.create(UUID.randomUUID().toString(),
        TestFixtures.SEGMENT_START, TestFixtures.SEGMENT_END2));
    assertEquals(List.of(), results);

    // query for frame1's id in [EPOCH, frame1.start], only find frame 1.
    results = dataFramePersistence.retrieveRawStationDataFramesByStationAndTime(
      StationTimeRangeRequest.create(TestFixtures.frame1.getMetadata().getStationName(),
        Instant.EPOCH, TestFixtures.SEGMENT_START));
    assertEquals(Stream.of(TestFixtures.frame1).map(RawStationDataFrame::getMetadata).collect(Collectors.toList()),
      results.stream().map(RawStationDataFrame::getMetadata).collect(Collectors.toList()));

    // query for frame1's name in [EPOCH, frame1.start - 1], find nothing because time range
    // is before frame1.start.
    results = dataFramePersistence.retrieveRawStationDataFramesByStationAndTime(
      StationTimeRangeRequest.create(TestFixtures.frame1.getMetadata().getStationName(),
        Instant.EPOCH, TestFixtures.SEGMENT_START.minusSeconds(1)));
    assertEquals(List.of(), results);

    // query for frame2's id in [frame2.start, frame2.end], only finds frame2.
    results = dataFramePersistence.retrieveRawStationDataFramesByStationAndTime(
      StationTimeRangeRequest.create(TestFixtures.frame2.getMetadata().getStationName(),
        TestFixtures.SEGMENT_START2, TestFixtures.SEGMENT_END2));
    assertEquals(Stream.of(TestFixtures.frame2).map(RawStationDataFrame::getMetadata).collect(Collectors.toList()),
      results.stream().map(RawStationDataFrame::getMetadata).collect(Collectors.toList()));

    // query for frame2's id in [frame1.start, frame2.start - 1], finds nothing.
    results = dataFramePersistence.retrieveRawStationDataFramesByStationAndTime(
      StationTimeRangeRequest.create(TestFixtures.frame2.getMetadata().getStationName(),
        TestFixtures.SEGMENT_START, TestFixtures.SEGMENT_START2.minusSeconds(1)));
    assertEquals(List.of(), results);
  }

  @Test
  void retrieveByTimeTest() {
    // Retrieve frames, giving exact start/end times of the two known frames.
    // Ensure they were retrieved.
    List<RawStationDataFrame> results = dataFramePersistence.retrieveRawStationDataFramesByTime(
      TimeRangeRequest.create(TestFixtures.SEGMENT_START, TestFixtures.SEGMENT_END2));
    assertEquals(TestFixtures.allFrames.stream().map(RawStationDataFrame::getMetadata).collect(Collectors.toList()),
      results.stream().map(RawStationDataFrame::getMetadata).collect(Collectors.toList()));
    // query in [frame1.start - 1, frame2.start + 1],
    // since the time range (barely) touches both frames they should both be returned.
    results = dataFramePersistence.retrieveRawStationDataFramesByTime(
      TimeRangeRequest.create(TestFixtures.SEGMENT1_END.minusSeconds(1),
        TestFixtures.SEGMENT_START2.plusSeconds(1)));
    assertEquals(TestFixtures.allFrames.stream().map(RawStationDataFrame::getMetadata).collect(Collectors.toList()),
      results.stream().map(RawStationDataFrame::getMetadata).collect(Collectors.toList()));
    // query in [frame1.start - 1, frame2.end + 1], should find both frames.
    results = dataFramePersistence.retrieveRawStationDataFramesByTime(
      TimeRangeRequest.create(TestFixtures.SEGMENT_START.minusSeconds(1),
        TestFixtures.SEGMENT_END2.plusSeconds(1)));
    assertEquals(TestFixtures.allFrames.stream().map(RawStationDataFrame::getMetadata).collect(Collectors.toList()),
      results.stream().map(RawStationDataFrame::getMetadata).collect(Collectors.toList()));
    // query in [EPOCH, frame1.start], finds the first frame only.
    results = dataFramePersistence.retrieveRawStationDataFramesByTime(
      TimeRangeRequest.create(Instant.EPOCH, TestFixtures.SEGMENT_START));
    assertEquals(Stream.of(TestFixtures.frame1).map(RawStationDataFrame::getMetadata).collect(Collectors.toList()),
      results.stream().map(RawStationDataFrame::getMetadata).collect(Collectors.toList()));
    // query in [EPOCH, frame1.start - 1], finds nothing.
    results = dataFramePersistence.retrieveRawStationDataFramesByTime(
      TimeRangeRequest.create(Instant.EPOCH, TestFixtures.SEGMENT_START.minusSeconds(1)));
    assertEquals(List.of(), results);
    // query in [frame2.start, frame2.end], only finds the 2nd frame.
    results = dataFramePersistence.retrieveRawStationDataFramesByTime(
      TimeRangeRequest.create(TestFixtures.SEGMENT_START2, TestFixtures.SEGMENT_END2));
    assertEquals(Stream.of(TestFixtures.frame2).map(RawStationDataFrame::getMetadata).collect(Collectors.toList()),
      results.stream().map(RawStationDataFrame::getMetadata).collect(Collectors.toList()));
    // query in [frame2.end + 1, frame2.end + 61], finds nothing.
    results = dataFramePersistence.retrieveRawStationDataFramesByTime(
      TimeRangeRequest.create(TestFixtures.SEGMENT_END2.plusSeconds(1),
        TestFixtures.SEGMENT_END2.plusSeconds(61)));
    assertEquals(List.of(), results);
  }

  @Test
  void storeNullFrameTest() {
    assertThrows(Exception.class, () ->
      dataFramePersistence.storeRawStationDataFrames(null));
  }
}
