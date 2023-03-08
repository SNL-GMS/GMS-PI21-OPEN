package gms.shared.frameworks.osd.coi.signaldetection;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


/**
 * Tests {@link QcMaskVersionDescriptor} creation and usage semantics
 */
class QcMaskVersionDescriptorTests {

  private final UUID qcMaskId = new UUID(0L, 0L);
  private final long qcMaskVersionId = 0L;

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(
      SignalDetectionTestFixtures.QC_MASK_VERSION_DESCRIPTOR,
      QcMaskVersionDescriptor.class);
  }

  @Test
  void testFromNullParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(QcMaskVersionDescriptor.class,
      "from", qcMaskId, qcMaskVersionId);
  }

  @Test
  void testParentIdExpectEqualsIdValue() {
    final UUID id1 = UUID
      .fromString("b38ae749-2833-4197-a8cb-4609ddd4342f");
    final UUID id2 = UUID
      .fromString("b38ae749-2833-4197-a8cb-4609ddd4342f");
    final UUID id3 = UUID
      .fromString("b38ae749-2833-4197-a8cb-4609ddd4342f");

    QcMaskVersionDescriptor qcMaskVersionDescriptor1 = QcMaskVersionDescriptor
      .from(id1, qcMaskVersionId);
    QcMaskVersionDescriptor qcMaskVersionDescriptor2 = QcMaskVersionDescriptor
      .from(id2, qcMaskVersionId);
    QcMaskVersionDescriptor qcMaskVersionDescriptor3 = QcMaskVersionDescriptor
      .from(id3, qcMaskVersionId);

    assertEquals(qcMaskVersionDescriptor1.getQcMaskId(),
      qcMaskVersionDescriptor2.getQcMaskId());
    assertEquals(qcMaskVersionDescriptor2.getQcMaskId(),
      qcMaskVersionDescriptor3.getQcMaskId());
  }

  @Test
  void testParentIdExpectNotEqualsIdValue() {
    final UUID id1 = UUID
      .fromString("04e7d88d-13ef-4e06-ab63-f81c6a170784");
    final UUID id2 = UUID
      .fromString("f66fbfc7-98a1-4e11-826b-968d80ef36eb");

    QcMaskVersionDescriptor qcMaskVersionDescriptor1 = QcMaskVersionDescriptor
      .from(id1, qcMaskVersionId);
    QcMaskVersionDescriptor qcMaskVersionDescriptor2 = QcMaskVersionDescriptor
      .from(id2, qcMaskVersionId);

    assertNotEquals(qcMaskVersionDescriptor1.getQcMaskId(),
      qcMaskVersionDescriptor2.getQcMaskId());
  }

  @Test
  void testParentVersionExpectEqualsIdValue() {
    final long id1 = 3;
    final long id2 = 3;
    final long id3 = 3;

    QcMaskVersionDescriptor qcMaskVersionDescriptor1 = QcMaskVersionDescriptor.from(qcMaskId, id1);
    QcMaskVersionDescriptor qcMaskVersionDescriptor2 = QcMaskVersionDescriptor.from(qcMaskId, id2);
    QcMaskVersionDescriptor qcMaskVersionDescriptor3 = QcMaskVersionDescriptor.from(qcMaskId, id3);

    assertEquals(qcMaskVersionDescriptor1.getQcMaskVersionId(),
      qcMaskVersionDescriptor2.getQcMaskVersionId());
    assertEquals(qcMaskVersionDescriptor2.getQcMaskVersionId(),
      qcMaskVersionDescriptor3.getQcMaskVersionId());
  }

  @Test
  void testParentVersionExpectNotEqualsIdValue() {
    final long id1 = 3;
    final long id2 = 4;

    QcMaskVersionDescriptor qcMaskVersionDescriptor1 = QcMaskVersionDescriptor.from(qcMaskId, id1);
    QcMaskVersionDescriptor qcMaskVersionDescriptor2 = QcMaskVersionDescriptor.from(qcMaskId, id2);

    assertNotEquals(qcMaskVersionDescriptor1.getQcMaskVersionId(),
      qcMaskVersionDescriptor2.getQcMaskVersionId());
  }

}
