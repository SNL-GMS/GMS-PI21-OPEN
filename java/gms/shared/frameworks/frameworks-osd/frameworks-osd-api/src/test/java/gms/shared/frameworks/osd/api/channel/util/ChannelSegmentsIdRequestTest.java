package gms.shared.frameworks.osd.api.channel.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChannelSegmentsIdRequestTest {

  @Test
  void testSerialization() throws IOException {
    ChannelSegmentsIdRequest request = ChannelSegmentsIdRequest.create(
      List.of(UUID.randomUUID(), UUID.randomUUID()),
      true);
    ObjectMapper mapper = CoiObjectMapperFactory.getJsonObjectMapper();
    assertEquals(request, mapper.readValue(mapper.writeValueAsString(request), ChannelSegmentsIdRequest.class));
  }
}