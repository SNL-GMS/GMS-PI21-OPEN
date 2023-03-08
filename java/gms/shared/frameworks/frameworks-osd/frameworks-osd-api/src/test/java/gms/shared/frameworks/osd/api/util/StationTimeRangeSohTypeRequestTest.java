package gms.shared.frameworks.osd.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StationTimeRangeSohTypeRequestTest {

  @Test
  void testSerialization() throws IOException {
    StationTimeRangeSohTypeRequest expected = StationTimeRangeSohTypeRequest.builder()
      .setStationName("test station name")
      .setTimeRange(TimeRangeRequest.create(Instant.EPOCH, Instant.EPOCH.plusSeconds(200000)))
      .setType(AcquiredChannelEnvironmentIssueType.AMPLIFIER_SATURATION_DETECTED)
      .build();

    ObjectMapper mapper = CoiObjectMapperFactory.getJsonObjectMapper();
    assertEquals(expected,
      mapper.readValue(mapper.writeValueAsString(expected),
        StationTimeRangeSohTypeRequest.class));
  }

}
