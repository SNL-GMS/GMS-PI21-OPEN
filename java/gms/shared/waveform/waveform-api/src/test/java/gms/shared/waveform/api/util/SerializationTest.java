package gms.shared.waveform.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;
import gms.shared.waveform.testfixture.WaveformRequestTestFixtures;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class SerializationTest {

  @ParameterizedTest
  @MethodSource("getSerializationArguments")
  @SuppressWarnings("unchecked")
  void testSerialization(Object request, Class clazz) throws IOException {
    ObjectMapper mapper = ObjectMapperFactory.getJsonObjectMapper();
    String requestJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
    var rebuilt = mapper.readValue(requestJson, clazz);
    assertEquals(request, rebuilt);
  }

  static Stream<Arguments> getSerializationArguments() {
    return Stream.of(
      arguments(WaveformRequestTestFixtures.channelTimeRangeRequest, ChannelTimeRangeRequest.class),
      arguments(WaveformRequestTestFixtures.facetedChannelTimeRangeRequest, ChannelTimeRangeRequest.class),
      arguments(WaveformRequestTestFixtures.channelSegmentDescriptorRequest, ChannelSegmentDescriptorRequest.class),
      arguments(WaveformRequestTestFixtures.facetedChannelSegmentDescriptorRequest, ChannelSegmentDescriptorRequest.class));
  }
}
