package gms.core.dataacquisition.reactor;

import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueBooleanDao;
import gms.shared.frameworks.osd.dao.channelsoh.AcquiredChannelEnvironmentIssueDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gms.core.dataacquisition.TestFixture.AceiDaoSets.aardvarkClipped46;
import static gms.core.dataacquisition.TestFixture.AceiDaoSets.aardvarkClocklock02;
import static gms.core.dataacquisition.TestFixture.AceiDaoSets.aardvarkClocklock04;
import static gms.core.dataacquisition.TestFixture.AceiDaoSets.aardvarkClocklock06;
import static gms.core.dataacquisition.TestFixture.AceiDaoSets.aardvarkClocklock24;
import static gms.core.dataacquisition.TestFixture.AceiDaoSets.aardvarkClocklock46;
import static gms.core.dataacquisition.TestFixture.AceiDaoSets.aardvarkClocklock56;
import static gms.core.dataacquisition.TestFixture.AceiDaoSets.badgerClocklock46;
import static gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType.CLIPPED;
import static gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType.CLOCK_LOCKED;
import static java.util.stream.Collectors.groupingBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AceiDaoMergerTest {

  @Mock
  EntityManager entityManager;

  private final Consumer<Stream<AcquiredChannelEnvironmentIssueBooleanDao>> SET_MANAGED = managedDaos -> managedDaos
    .forEach(dao -> doReturn(true).when(entityManager).contains(dao));

  private final Consumer<Stream<AcquiredChannelEnvironmentIssueBooleanDao>> SET_UNMANAGED = managedDaos -> managedDaos
    .forEach(dao -> doReturn(false).when(entityManager).contains(dao));

  private AceiDaoMerger merger;

  @BeforeEach
  void setUp() {
    merger = AceiDaoMerger.create(channel -> Duration.ofMillis(500));
  }

  @Test
  void testMergeManaged() {
    var managed1 = aardvarkClocklock02();
    var managed2 = aardvarkClocklock24();

    SET_MANAGED.accept(Stream.of(managed1, managed2));

    var merge = merger.merge(managed1, managed2, entityManager);
    assertEquals(aardvarkClocklock04(), merge);
    assertSame(managed1, merge);
    verify(entityManager).remove(managed2);
  }

  @Test
  void testMergeUnmanaged() {
    var unmanaged1 = aardvarkClocklock02();
    var unmanaged2 = aardvarkClocklock24();

    SET_UNMANAGED.accept(Stream.of(unmanaged1, unmanaged2));

    var merge = merger.merge(unmanaged1, unmanaged2, entityManager);
    assertEquals(aardvarkClocklock04(), merge);
    assertSame(unmanaged1, merge);
  }

  @Test
  void testMergeManagedUnmanaged() {
    var managed = aardvarkClocklock02();
    var unmanaged = aardvarkClocklock24();

    SET_MANAGED.accept(Stream.of(managed));
    SET_UNMANAGED.accept(Stream.of(unmanaged));

    var merge = merger.merge(managed, unmanaged, entityManager);
    assertEquals(aardvarkClocklock04(), merge);
    assertSame(managed, merge);

    merge = merger.merge(unmanaged, managed, entityManager);
    assertEquals(aardvarkClocklock04(), merge);
    assertSame(managed, merge);
  }

  @Test
  void testMergeSame() {
    var sameDao1 = aardvarkClocklock02();
    var sameDao2 = aardvarkClocklock02();

    doReturn(true)
      .when(entityManager).contains(sameDao1);

    var merge = merger.merge(sameDao1, sameDao2, entityManager);
    assertEquals(sameDao1, merge);
    assertSame(sameDao1, merge);

    doReturn(false)
      .when(entityManager).contains(sameDao1);

    merge = merger.merge(sameDao1, sameDao2, entityManager);
    assertEquals(sameDao1, merge);
    assertSame(sameDao2, merge);
  }

  @Test
  void testMergeAllUnmanagedSandwich() {
    var unmanaged1 = aardvarkClocklock02();
    var managed = aardvarkClocklock24();
    var unmanaged2 = aardvarkClocklock46();

    SET_MANAGED.accept(Stream.of(managed));
    SET_UNMANAGED.accept(Stream.of(unmanaged1, unmanaged2));

    var mergeSet = merger.mergeAll(List.of(managed, unmanaged1, unmanaged2), entityManager);
    assertEquals(1, mergeSet.size());
    var merged = mergeSet.toArray()[0];
    assertEquals(aardvarkClocklock06(), merged);
    assertSame(managed, merged);
  }

  @Test
  void testMergeAllManagedUnmanagedFillGap() {
    var managed1 = aardvarkClocklock02();
    var unmanaged = aardvarkClocklock24();
    var managed2 = aardvarkClocklock46();

    SET_MANAGED.accept(Stream.of(managed1, managed2));
    SET_UNMANAGED.accept(Stream.of(unmanaged));

    var mergeSet = merger.mergeAll(List.of(managed1, unmanaged, managed2), entityManager);
    assertEquals(1, mergeSet.size());
    var merged = mergeSet.toArray()[0];
    assertEquals(aardvarkClocklock06(), merged);
    assertSame(managed1, merged);
    verify(entityManager).remove(managed2);
  }

  @Test
  void testMergeAllUnmergeableSegments() {
    var aclock1 = aardvarkClocklock02();
    var aclock2 = aardvarkClocklock24();
    var aclock3 = aardvarkClocklock56();

    SET_UNMANAGED.accept(Stream.of(aclock1, aclock2));

    var mergeSorted = merger.mergeAll(List.of(aclock1, aclock2, aclock3), entityManager)
      .stream().sorted(Comparator.comparing(AcquiredChannelEnvironmentIssueDao::getStartTime))
      .collect(Collectors.toList());
    assertEquals(aardvarkClocklock04(), mergeSorted.get(0));
    assertEquals(aardvarkClocklock56(), mergeSorted.get(1));
  }

  @Test
  void testMergeAllDifferentTypes() {
    var aclock1 = aardvarkClocklock02();
    var aclock2 = aardvarkClocklock24();
    var aclip = aardvarkClipped46();

    SET_UNMANAGED.accept(Stream.of(aclock1, aclock2));

    var mergeSet = merger.mergeAll(List.of(aclock1, aclip, aclock2), entityManager);
    var groupedDaos = mergeSet.stream()
      .collect(groupingBy(AcquiredChannelEnvironmentIssueDao::getType));

    assertEquals(2, groupedDaos.size());
    assertEquals(1, groupedDaos.get(CLOCK_LOCKED).size());
    assertEquals(1, groupedDaos.get(CLIPPED).size());

    assertEquals(aardvarkClocklock04(), groupedDaos.get(CLOCK_LOCKED).get(0));
    assertEquals(aardvarkClipped46(), groupedDaos.get(CLIPPED).get(0));
  }

  @Test
  void testMergeAllDifferentChannels() {
    var aclock1 = aardvarkClocklock02();
    var aclock2 = aardvarkClocklock24();
    var bclock = badgerClocklock46();

    SET_UNMANAGED.accept(Stream.of(aclock1, aclock2));

    var mergeSet = merger.mergeAll(List.of(aclock1, bclock, aclock2), entityManager);
    var groupedDaos = mergeSet.stream()
      .collect(groupingBy(AcquiredChannelEnvironmentIssueDao::getChannelName));

    assertEquals(2, groupedDaos.size());
    assertEquals(1, groupedDaos.get("aardvark").size());
    assertEquals(1, groupedDaos.get("badger").size());

    assertEquals(aardvarkClocklock04(), groupedDaos.get("aardvark").get(0));
    assertEquals(badgerClocklock46(), groupedDaos.get("badger").get(0));
  }
}