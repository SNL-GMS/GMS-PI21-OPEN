package gms.shared.stationdefinition.coi.channel;

import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChannelIdTest {
  @Test
  void testEmptyEffectiveTime() {
    ChannelId channelId = ChannelId.builder()
      .setName("some channel")
      .build();
    assertTrue(channelId.getEffectiveAt().isEmpty());
  }

  @Test
  void testEmptyNameException() {
    ChannelId.Builder builder = ChannelId.builder()
      .setEffectiveAt(Instant.EPOCH);

    assertThrows(IllegalStateException.class, builder::build);
  }

  @Test
  void testSerialization() {
    ChannelId channelId = ChannelId.builder()
      .setEffectiveAt(Instant.EPOCH)
      .setName("some channel")
      .build();
    TestUtilities.assertSerializes(channelId, ChannelId.class);
  }

  @Test
  void testCreateEntityReference() {
    ChannelId channelId = ChannelId.createEntityReference("some channel");
    assertEquals("some channel", channelId.getName());
    assertTrue(channelId.getEffectiveAt().isEmpty());
  }

  @Test
  void testCreateVersionReference() {
    ChannelId channelId = ChannelId.createVersionReference("some channel", Instant.EPOCH);
    assertEquals("some channel", channelId.getName());
    assertEquals(Instant.EPOCH, channelId.getEffectiveAt().orElseThrow());
  }

  @Test
  void testToEntityReference() {
    ChannelId channelId = ChannelId.builder()
      .setName("someChannel")
      .build();
    Channel channel = channelId.toChannelEntityReference();

    assertFalse(channel.isPresent());
    assertFalse(channel.getEffectiveAt().isPresent());
  }

  @Test
  void testToVersionReference() {
    ChannelId channelId = ChannelId.builder()
      .setName("someChannel")
      .setEffectiveAt(Instant.EPOCH)
      .build();
    Channel channel = channelId.toChannelVersionReference();

    assertFalse(channel.isPresent());
    assertTrue(channel.getEffectiveAt().isPresent());
  }
}