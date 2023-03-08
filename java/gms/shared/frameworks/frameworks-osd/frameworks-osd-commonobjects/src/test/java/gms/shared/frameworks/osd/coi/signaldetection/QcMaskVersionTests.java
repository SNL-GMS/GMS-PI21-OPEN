package gms.shared.frameworks.osd.coi.signaldetection;


import gms.shared.frameworks.osd.coi.signaldetection.QcMaskVersion.Builder;
import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Tests {@link QcMaskVersion} factory creation
 * <p>
 * Created by jrhipp on 9/13/17.
 */
class QcMaskVersionTests {

  private final long qcMaskVersionId = 6L;
  private final UUID qcMaskVersionParentId = UUID
    .fromString("b38ae749-2833-4197-a8cb-4609ddd4342f");
  private final long qcMaskVersionParentVersion = 5L;

  private final UUID channelSegmentId1 = UUID.randomUUID();
  private final UUID channelSegmentId2 = UUID.randomUUID();

  private final QcMaskType qcMaskType = QcMaskType.LONG_GAP;
  private final QcMaskCategory qcMaskCategory = QcMaskCategory.WAVEFORM_QUALITY;
  private final String rationale = "Rationale";
  private final Instant startTime = Instant.parse("2007-12-03T10:15:30.00Z");
  private final Instant endTime = Instant.parse("2007-12-03T11:15:30.00Z");

  private QcMaskVersion qcMaskVersion;

  @BeforeEach
  void setUp() {
    qcMaskVersion = QcMaskVersion.builder()
      .setVersion(qcMaskVersionId)
      .addParentQcMask(qcMaskVersionParentId, qcMaskVersionParentVersion)
      .addChannelSegmentId(channelSegmentId1)
      .addChannelSegmentId(channelSegmentId2)
      .setType(qcMaskType)
      .setCategory(qcMaskCategory)
      .setRationale(rationale)
      .setStartTime(startTime)
      .setEndTime(endTime)
      .build();
  }

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.qcMaskVersion,
      QcMaskVersion.class);
  }

  @Test
  void testBuildRejectedOptionalsValidated() {
    Supplier<Builder> rejectedVersionSupplier = () -> QcMaskVersion.builder()
      .setVersion(6L)
      .addParentQcMask(qcMaskVersionParentId, qcMaskVersionParentVersion)
      .addChannelSegmentId(channelSegmentId1)
      .addChannelSegmentId(channelSegmentId2)
      .setCategory(QcMaskCategory.REJECTED)
      .setRationale(rationale);

    //permute the cases, only when none of the three are set should we successfully validate
    assertDoesNotThrow(rejectedVersionSupplier.get()::build);
    QcMaskVersion.Builder qcMaskBuilder = rejectedVersionSupplier.get().setType(qcMaskType);

    assertThrows(IllegalStateException.class, () -> qcMaskBuilder.build());

    qcMaskBuilder.setStartTime(startTime);
    assertThrows(IllegalStateException.class, () -> qcMaskBuilder.build());

    qcMaskBuilder.setEndTime(endTime);
    assertThrows(IllegalStateException.class, () -> qcMaskBuilder.build());

    qcMaskBuilder.setType(qcMaskType)
      .setStartTime(startTime);
    assertThrows(IllegalStateException.class, () -> qcMaskBuilder.build());

    qcMaskBuilder.setStartTime(startTime)
      .setEndTime(endTime);
    assertThrows(IllegalStateException.class, () -> qcMaskBuilder.build());

    qcMaskBuilder.setType(qcMaskType).setEndTime(endTime);
    assertThrows(IllegalStateException.class, () -> qcMaskBuilder.build());
  }

  @Test
  void testBuildNotRejectedCheckOptionalsValidated() {

    Supplier<QcMaskVersion.Builder> notRejectedVersionSupplier =
      () -> QcMaskVersion.builder()
        .setVersion(6L)
        .addParentQcMask(qcMaskVersionParentId, qcMaskVersionParentVersion)
        .addChannelSegmentId(channelSegmentId1)
        .addChannelSegmentId(channelSegmentId2)
        .setCategory(qcMaskCategory)
        .setRationale(rationale);

    //permute the cases, only when all three are set should we successfully validate
    assertThrows(IllegalStateException.class,
      notRejectedVersionSupplier.get()::build);

    QcMaskVersion.Builder qcBuilder = notRejectedVersionSupplier.get().setType(qcMaskType);
    assertThrows(IllegalStateException.class, () -> qcBuilder.build());

    QcMaskVersion.Builder qcBuilder2 = notRejectedVersionSupplier.get().setStartTime(startTime);
    assertThrows(IllegalStateException.class, () -> qcBuilder2.build());

    QcMaskVersion.Builder qcBuilder3 = notRejectedVersionSupplier.get().setEndTime(endTime);
    assertThrows(IllegalStateException.class, () -> qcBuilder3.build());

    QcMaskVersion.Builder qcBuilder4 = notRejectedVersionSupplier.get().setType(qcMaskType).setStartTime(startTime);
    assertThrows(IllegalStateException.class, () -> qcBuilder4.build());

    QcMaskVersion.Builder qcBuilder5 = notRejectedVersionSupplier.get().setStartTime(startTime)
      .setEndTime(endTime);
    assertThrows(IllegalStateException.class, () -> qcBuilder5.build());

    QcMaskVersion.Builder qcBuilder6 = notRejectedVersionSupplier.get().setType(qcMaskType).setEndTime(endTime);
    assertThrows(IllegalStateException.class, () -> qcBuilder6.build());

    QcMaskVersion.Builder qcBuilder7 = notRejectedVersionSupplier.get().setType(qcMaskType)
      .setStartTime(startTime).setEndTime(endTime);
    assertDoesNotThrow(() -> qcBuilder7.build());

  }

  @Test
  void testBuildStartTimeAfterEndTimeExpectIllegalStateException() {
    QcMaskVersion.Builder qcBuilder = qcMaskVersion.toBuilder().setStartTime(endTime.plusSeconds(10));
    assertThrows(IllegalStateException.class, () -> qcBuilder.build());
  }

  @Test
  void testBuildDuplicateParentQcMasksExpectIllegalStateException() {
    QcMaskVersion.Builder qcBuilder = qcMaskVersion.toBuilder()
      .addParentQcMask(qcMaskVersionParentId, qcMaskVersionParentVersion);
    assertThrows(IllegalStateException.class, () -> qcBuilder.build());
  }

  @Test
  void testHasParent() {
    assertTrue(qcMaskVersion.hasParent());
    assertFalse(qcMaskVersion.toBuilder().setParentQcMasks(List.of()).build().hasParent());
  }

  @Test
  void testTypeCategoryValidationExpectIllegalStateException() {
    // throws error ... SENSOR_PROBLEM type is not a WAVEFORM_QUALITY category.
    QcMaskVersion.Builder qcBuilder = qcMaskVersion.toBuilder().setType(QcMaskType.SENSOR_PROBLEM);
    assertThrows(IllegalStateException.class, () -> qcBuilder.build());
  }

}
