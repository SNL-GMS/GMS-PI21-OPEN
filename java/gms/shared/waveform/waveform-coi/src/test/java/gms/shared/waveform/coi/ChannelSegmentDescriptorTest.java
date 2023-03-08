package gms.shared.waveform.coi;

import gms.shared.stationdefinition.coi.channel.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ChannelSegmentDescriptorTest {

  @Test
  void testFrom() {
    Instant now = Instant.now();

    ChannelSegmentDescriptor csd = ChannelSegmentDescriptor.from(
      CHANNEL,
      now,
      now.plus(1, ChronoUnit.MINUTES),
      now.minus(1, ChronoUnit.MINUTES));
    assertEquals(CHANNEL.getName(), csd.getChannel().getName());
    assertEquals(now, csd.getStartTime());
    assertEquals(now.plus(1, ChronoUnit.MINUTES), csd.getEndTime());
    assertEquals(now.minus(1, ChronoUnit.MINUTES), csd.getCreationTime());
  }

  @ParameterizedTest
  @MethodSource("getFromArguments")
  void testFromValidation(Class<? extends Exception> expectedException,
    Channel channel,
    Instant segmentStartTime,
    Instant segmentEndTime,
    Instant segmentCreationTime) {
    assertThrows(expectedException, () -> ChannelSegmentDescriptor.from(
      channel,
      segmentStartTime,
      segmentEndTime,
      segmentCreationTime));
  }

  static Stream<Arguments> getFromArguments() {
    return Stream.of(
      arguments(NullPointerException.class, null, Instant.now(), Instant.now(), Instant.now()),
      arguments(NullPointerException.class, CHANNEL, null, Instant.now(), Instant.now()),
      arguments(NullPointerException.class, CHANNEL, Instant.now(), null, Instant.now()),
      arguments(NullPointerException.class, CHANNEL, Instant.now(), Instant.now(), null));
  }
}