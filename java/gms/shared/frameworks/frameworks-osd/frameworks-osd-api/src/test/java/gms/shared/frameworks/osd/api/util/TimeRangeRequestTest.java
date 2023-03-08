package gms.shared.frameworks.osd.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeRangeRequestTest {

  @Test
  void testSerialization() throws IOException {
    TimeRangeRequest request = TimeRangeRequest.create(Instant.EPOCH, Instant.EPOCH.plusSeconds(3));
    ObjectMapper mapper = CoiObjectMapperFactory.getJsonObjectMapper();
    assertEquals(request, mapper.readValue(mapper.writeValueAsString(request), TimeRangeRequest.class));
  }

}