package gms.shared.frameworks.osd.coi.channel;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import gms.shared.frameworks.osd.coi.util.TestUtilities;
import gms.shared.frameworks.osd.coi.waveforms.Timeseries;
import gms.shared.frameworks.osd.coi.waveforms.Waveform;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link Waveform} creation and usage semantics Created by trsault on 8/25/17.
 */
class ChannelSegmentTests {

  private final Channel channel = ChannelFactory
    .rawFromReferenceChannel(UtilsTestFixtures.referenceChannel, "STA", "GROUP");

  private final UUID segmentId = UUID.fromString("8952f988-ff83-4f3d-a832-a82a04022539");

  private final ChannelSegment.Type type = ChannelSegment.Type.ACQUIRED;
  private final Timeseries.Type seriestype = Timeseries.Type.WAVEFORM;
  private final Instant start = Instant.EPOCH;
  private final Waveform earlierWaveform = Waveform.from(
    start, 1.0, new double[5]);
  private final Waveform laterWaveform = Waveform.from(
    start.plusSeconds(30), 1.0, new double[10]);
  private final List<Waveform> wfs = List
    .of(laterWaveform, earlierWaveform);  // purposely in reverse time order

  @Test
  void testChannelSegmentCreateNullArguments() throws Exception {
    // Test analog SOH
    TestUtilities.checkStaticMethodValidatesNullArguments(
      ChannelSegment.class, "create",
      channel, "NAME", type, wfs);
  }

  @Test
  void testChannelSegmentFromNullArguments() throws Exception {
    // Test analog SOH
    TestUtilities.checkStaticMethodValidatesNullArguments(
      ChannelSegment.class, "from", segmentId,
      channel, "NAME", type, wfs);
  }

  @Test
  void testCreate() {
    final ChannelSegment<Waveform> segment = ChannelSegment
      .create(channel, "NAME", type, wfs);
    assertEquals("NAME", segment.getName());
    assertEquals(channel, segment.getChannel());
    assertEquals(type, segment.getType());
    assertEquals(seriestype, segment.getTimeseriesType());
    assertEquals(earlierWaveform.getStartTime(), segment.getStartTime());
    assertEquals(laterWaveform.getEndTime(), segment.getEndTime());
    // below: using set so order doesn't matter.  channel segment sorts it's series.
    assertEquals(new HashSet<>(wfs), new HashSet<>(segment.getTimeseries()));
  }

  @Test
  void testFrom() {
    final ChannelSegment<Waveform> segment = ChannelSegment
      .from(segmentId, channel, "NAME", type, wfs);
    assertEquals("NAME", segment.getName());
    assertEquals(segmentId, segment.getId());
    assertEquals(channel, segment.getChannel());
    assertEquals(type, segment.getType());
    assertEquals(seriestype, segment.getTimeseriesType());
    assertEquals(earlierWaveform.getStartTime(), segment.getStartTime());
    assertEquals(laterWaveform.getEndTime(), segment.getEndTime());
    // below: using set so order doesn't matter.  channel segment sorts it's series.
    assertEquals(new HashSet<>(wfs), new HashSet<>(segment.getTimeseries()));
  }

  @Test
  void testEmptySeriesExpectIllegalArgumentException() {

    Throwable exception = Assertions.assertThrows(IllegalArgumentException.class,
      () -> ChannelSegment.create(channel, "NAME", type, Collections.EMPTY_LIST));

    Assertions.assertEquals("ChannelSegment requires at least one timeseries",
      exception.getMessage());
  }

  @Test
  void testBlankNameExpectIllegalArgumentException() {

    Throwable exception = Assertions.assertThrows(IllegalArgumentException.class,
      () -> ChannelSegment.create(channel, "", type, wfs));

    Assertions.assertEquals("ChannelSegment requires a non-blank name", exception.getMessage());
  }

  @Test
  void testOverlappingSeriesIllegalArgumentException() {

    Collection<Waveform> overlappingWaveforms = List.of(
      Waveform.from(start, 5.0, new double[50]),
      Waveform.from(start.plusSeconds(1), 100.0, new double[50]));

    Throwable exception = Assertions.assertThrows(IllegalArgumentException.class,
      () -> ChannelSegment.create(channel, "NAME", type,
        overlappingWaveforms));

    Assertions.assertEquals("ChannelSegment cannot have overlapping timeseries",
      exception.getMessage());
  }

  @Test
  void testCompareExpectNegative() {
    final ChannelSegment<Waveform> segment1 = ChannelSegment
      .create(channel, "NAME", type, wfs);

    final List<Waveform> wfs2 = List.of(Waveform.from(
      start.plusMillis(1), 1.0, new double[1]));
    final ChannelSegment<Waveform> segment2 = ChannelSegment
      .create(channel, "NAME", type, wfs2);

    assertTrue(segment1.compareTo(segment2) < 0);
  }

  @Test
  void testCompareExpectPositive() {
    final List<Waveform> wfs2 = List.of(Waveform.from(
      start.plusMillis(1), 1.0, new double[1]));
    final ChannelSegment<Waveform> segment1 = ChannelSegment
      .create(channel, "NAME", type, wfs2);

    final ChannelSegment<Waveform> segment2 = ChannelSegment
      .create(channel, "NAME", type, wfs);

    assertTrue(segment1.compareTo(segment2) > 0);
  }

  @Test
  void testCompareEqualExpectPositive() {
    final ChannelSegment<Waveform> segment1 = ChannelSegment
      .create(channel, "NAME", type, wfs);

    final ChannelSegment<Waveform> segment2 = ChannelSegment
      .create(channel, "NAME", type, wfs);

    assertTrue(segment1.compareTo(segment2) > 0);
  }

  @Test
  void testSerialization() throws IOException {
    ChannelSegment<Waveform> segment = ChannelSegment
      .create(channel, "NAME", type, wfs);

    ObjectMapper mapper = CoiObjectMapperFactory.getJsonObjectMapper();
    JavaType channelSegmentType =
      mapper.getTypeFactory().constructParametricType(ChannelSegment.class, Waveform.class);
    assertEquals(segment, mapper.readValue(mapper.writeValueAsString(segment), channelSegmentType));
  }
}