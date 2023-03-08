package gms.shared.frameworks.osd.coi.channel;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.referenceChannel;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReferenceChannelTests {

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(referenceChannel, ReferenceChannel.class);
  }

  @Test
  void testEmptyNameThrowsException() {
    ReferenceChannel.Builder refChanBuilder = referenceChannel.toBuilder().setName("");
    Exception exception = assertThrows(IllegalArgumentException.class,
      () -> refChanBuilder.build());
    assertEquals("name should not be an empty field", exception.getMessage());

    refChanBuilder.setName(" ");
    exception = assertThrows(IllegalArgumentException.class,
      () -> refChanBuilder.build());
    assertEquals("name should not be an empty field", exception.getMessage());
  }

  @Test
  void testOrientationCodeIsWhitespaceThrowsException() {
    ReferenceChannel.Builder refChanBuilder = referenceChannel.toBuilder()
      .setOrientationType(ChannelOrientationType.UNKNOWN)
      .setOrientationCode(' ');
    final Exception exception = assertThrows(IllegalArgumentException.class,
      () -> refChanBuilder.build());

    assertEquals("orientationCode cannot be whitespace", exception.getMessage());
  }

  @Test
  void testOrientationTypeCodeDoesNotMatchOrientationCodeThrowsException() {
    ReferenceChannel.Builder refChanBuilder = referenceChannel.toBuilder()
      .setOrientationType(ChannelOrientationType.VERTICAL)
      .setOrientationCode('N');
    final Exception exception = assertThrows(IllegalArgumentException.class,
      () -> refChanBuilder.build());

    assertEquals(
      "orientationType.code must match orientationCode when orientationType is not 'UNKNOWN'",
      exception.getMessage());
  }

  @Test
  void testOrientationTypeUnknownCodeDoesNotNeedToMatchOrientationCode() {
    assertDoesNotThrow(
      () -> referenceChannel.toBuilder()
        .setOrientationType(ChannelOrientationType.UNKNOWN)
        .setOrientationCode('U').build()
    );
  }
}