package gms.shared.frameworks.osd.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HistoricalStationSohRequestTests {

  @Test
  void testSerialization() throws IOException {
    HistoricalStationSohRequest request = HistoricalStationSohRequest.create("TEST",
      Instant.EPOCH, Instant.EPOCH.plusSeconds(5),
      SohMonitorType.MISSING);
    ObjectMapper mapper = CoiObjectMapperFactory.getJsonObjectMapper();
    assertEquals(request,
      mapper.readValue(mapper.writeValueAsString(request), HistoricalStationSohRequest.class));
  }
}