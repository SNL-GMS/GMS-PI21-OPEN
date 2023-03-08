package gms.shared.waveform.coi;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;
import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL;
import static gms.shared.waveform.testfixture.WaveformTestFixtures.randomSamples0To1;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Tests {@link Waveform} creation and usage semantics Created by trsault on 8/25/17.
 */
class ChannelSegmentTests {

  private final Timeseries.Type seriestype = Timeseries.Type.WAVEFORM;
  private static final Instant START = Instant.EPOCH;
  private static final Instant CREATION_TIME = START.minus(1, ChronoUnit.MINUTES);
  private static final Waveform EARLIER_WAVEFORM = Waveform.create(
    START, 1.0, new double[5]);
  private static final Waveform LATER_WAVEFORM = Waveform.create(
    START.plusSeconds(30), 1.0, new double[10]);
  private static final List<Waveform> WFS = List
    .of(LATER_WAVEFORM, EARLIER_WAVEFORM);  // purposely in reverse time order

  @Test
  void testFrom() {
    final ChannelSegment<Waveform> segment = ChannelSegment
      .from(CHANNEL, CHANNEL.getUnits(), WFS, CREATION_TIME);
    assertEquals(CHANNEL, segment.getId().getChannel());
    assertEquals(seriestype, segment.getTimeseriesType());
    assertEquals(EARLIER_WAVEFORM.getStartTime(), segment.getId().getStartTime());
    assertEquals(LATER_WAVEFORM.getEndTime(), segment.getId().getEndTime());
    // below: using set so order doesn't matter.  channel segment sorts it's series.
    assertEquals(new HashSet<>(WFS), new HashSet<>(segment.getTimeseries()));
  }

  @Test
  void testFromChannelSegmentDescriptor() {
    final ChannelSegmentDescriptor channelSegmentDescriptor = ChannelSegmentDescriptor
      .from(CHANNEL, EARLIER_WAVEFORM.getStartTime(), LATER_WAVEFORM.getEndTime(), CREATION_TIME);
    final ChannelSegment<Waveform> segment = ChannelSegment.from(channelSegmentDescriptor, CHANNEL.getUnits(), WFS);
    assertEquals(CHANNEL, segment.getId().getChannel());
    assertEquals(seriestype, segment.getTimeseriesType());
    assertEquals(EARLIER_WAVEFORM.getStartTime(), segment.getId().getStartTime());
    assertEquals(LATER_WAVEFORM.getEndTime(), segment.getId().getEndTime());
    // below: using set so order doesn't matter.  channel segment sorts it's series.
    assertEquals(new HashSet<>(WFS), new HashSet<>(segment.getTimeseries()));
  }

  @ParameterizedTest
  @MethodSource("getFromArguments")
  void testFromValidation(Class<? extends Exception> expectedException,
    Channel channel,
    List<Waveform> waveforms,
    Instant creationTime) {
    assertThrows(expectedException, () ->
      ChannelSegment.from(channel, CHANNEL.getUnits(), waveforms, creationTime));
  }

  static Stream<Arguments> getFromArguments() {
    return Stream.of(
      arguments(IllegalArgumentException.class, CHANNEL, List.of(), CREATION_TIME),
      arguments(NullPointerException.class, null,
        List.of(Waveform.create(START, 5.0, new double[50]),
          Waveform.create(START.plusSeconds(1), 100.0, new double[50])), CREATION_TIME),
      arguments(NullPointerException.class, CHANNEL,
        List.of(Waveform.create(START, 5.0, new double[50]),
          Waveform.create(START.plusSeconds(1), 100.0, new double[50])), null));
  }

  @ParameterizedTest
  @MethodSource("getFromChannelSegmentDescriptorArguments")
  void testFromChannelSegmentDescriptorValidation(Class<? extends Exception> expectedException,
    ChannelSegmentDescriptor channelSegmentDescriptor,
    List<Waveform> waveforms) {
    assertThrows(expectedException, () ->
      ChannelSegment.from(channelSegmentDescriptor, CHANNEL.getUnits(), waveforms));
  }

  static Stream<Arguments> getFromChannelSegmentDescriptorArguments() {
    final ChannelSegmentDescriptor channelSegmentDescriptor = ChannelSegmentDescriptor
      .from(CHANNEL, EARLIER_WAVEFORM.getStartTime(), LATER_WAVEFORM.getEndTime(), CREATION_TIME);
    return Stream.of(
      arguments(NullPointerException.class, channelSegmentDescriptor, null),
      arguments(IllegalArgumentException.class, channelSegmentDescriptor, List.of()),
      arguments(NullPointerException.class, null,
        List.of(Waveform.create(START, 5.0, new double[50]),
          Waveform.create(START.plusSeconds(1), 100.0, new double[50]))));
  }

  @Test
  void testCompareExpectNegative() {
    final ChannelSegment<Waveform> segment1 = ChannelSegment
      .from(CHANNEL, CHANNEL.getUnits(), WFS, CREATION_TIME);

    final List<Waveform> wfs2 = List.of(Waveform.create(
      START.plusMillis(1), 1.0, new double[1]));
    final ChannelSegment<Waveform> segment2 = ChannelSegment
      .from(CHANNEL, CHANNEL.getUnits(), wfs2, CREATION_TIME);

    assertTrue(segment1.compareTo(segment2) < 0);
  }

  @Test
  void testCompareExpectPositive() {
    final List<Waveform> wfs2 = List.of(Waveform.create(
      START.plusMillis(1), 1.0, new double[1]));
    final ChannelSegment<Waveform> segment1 = ChannelSegment
      .from(CHANNEL, CHANNEL.getUnits(), wfs2, CREATION_TIME);

    final ChannelSegment<Waveform> segment2 = ChannelSegment
      .from(CHANNEL, CHANNEL.getUnits(), WFS, CREATION_TIME);

    assertTrue(segment1.compareTo(segment2) > 0);
  }

  @Test
  void testCompareEqualExpectEqual() {
    final ChannelSegment<Waveform> segment1 = ChannelSegment
      .from(CHANNEL, CHANNEL.getUnits(), WFS, CREATION_TIME);

    final ChannelSegment<Waveform> segment2 = ChannelSegment
      .from(CHANNEL, CHANNEL.getUnits(), WFS, CREATION_TIME);

    assertEquals(segment1, segment2);
  }

  @ParameterizedTest
  @MethodSource("getCompareEndTimeArguments")
  void testCompareToEndtime(int expected, ChannelSegment<Waveform> base, ChannelSegment<Waveform> other) {
    assertEquals(expected, base.compareTo(other));
  }

  static Stream<Arguments> getCompareEndTimeArguments() {
    Instant end1 = Instant.EPOCH.plusSeconds(10);
    Instant end2 = Instant.EPOCH.plusSeconds(15);
    ChannelSegment<Waveform> channelSegment1 = ChannelSegment.from(CHANNEL,
      CHANNEL.getUnits(),
      List.of(randomSamples0To1(Instant.EPOCH, end1, 2)), CREATION_TIME);
    ChannelSegment<Waveform> channelSegment2 = ChannelSegment.from(CHANNEL,
      CHANNEL.getUnits(),
      List.of(randomSamples0To1(Instant.EPOCH, end2, 2)), CREATION_TIME);
    return Stream.of(arguments(end1.compareTo(end2), channelSegment1, channelSegment2),
      arguments(end2.compareTo(end1), channelSegment2, channelSegment1, CREATION_TIME));
  }

  @Test
  void testSerialization() throws IOException {
    ChannelSegment<Waveform> segment = ChannelSegment
      .from(CHANNEL, CHANNEL.getUnits(), WFS, CREATION_TIME);

    ObjectMapper mapper = ObjectMapperFactory.getJsonObjectMapper();
    JavaType channelSegmentType =
      mapper.getTypeFactory().constructParametricType(ChannelSegment.class, Waveform.class);

    ChannelSegment<Waveform> segment2 = mapper.readValue(mapper.writeValueAsString(segment), channelSegmentType);
    assertEquals(segment, segment2);
  }

  @Test
  void testSerializationIdOnly() {
    var channelSegment = ChannelSegment.builder()
      .setId(
        ChannelSegmentDescriptor.from(
          Channel.createEntityReference("ChannelName"),
          Instant.EPOCH,
          Instant.ofEpochSecond(1),
          Instant.EPOCH
        )
      )
      .build();

    TestUtilities.assertSerializes(channelSegment, ChannelSegment.class);
  }
}