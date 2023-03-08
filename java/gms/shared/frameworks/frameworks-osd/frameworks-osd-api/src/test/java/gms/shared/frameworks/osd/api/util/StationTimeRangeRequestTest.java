package gms.shared.frameworks.osd.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StationTimeRangeRequestTest {

  @Test
  void testSerialization() throws IOException {
    StationTimeRangeRequest request = StationTimeRangeRequest.create("test", Instant.EPOCH,
      Instant.EPOCH.plusSeconds(3));
    ObjectMapper mapper = CoiObjectMapperFactory.getJsonObjectMapper();
    assertEquals(request, mapper.readValue(mapper.writeValueAsString(request), StationTimeRangeRequest.class));
  }

}