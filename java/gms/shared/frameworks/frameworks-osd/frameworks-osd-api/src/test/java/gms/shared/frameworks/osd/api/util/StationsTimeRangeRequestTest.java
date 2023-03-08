package gms.shared.frameworks.osd.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StationsTimeRangeRequestTest {

  @Test
  void testSerialization() throws IOException {
    StationsTimeRangeRequest request = StationsTimeRangeRequest.create(List.of("test1", "test2"),
      Instant.EPOCH, Instant.EPOCH.plusSeconds(5));
    ObjectMapper mapper = CoiObjectMapperFactory.getJsonObjectMapper();
    assertEquals(request, mapper.readValue(mapper.writeValueAsString(request), StationsTimeRangeRequest.class));
  }

}