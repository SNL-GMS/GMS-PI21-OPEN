package gms.shared.frameworks.osd.dto.soh;

import com.fasterxml.jackson.core.JsonProcessingException;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DurationSohMonitorValuesTests {

  @Test
  void testJsonSerDes() throws JsonProcessingException {
    DurationSohMonitorValues values = DurationSohMonitorValues
      .create(new long[]{Instant.now().toEpochMilli()});

    String json = CoiObjectMapperFactory.getJsonObjectMapper()
      .writeValueAsString(values);

    assertEquals(values, CoiObjectMapperFactory.getJsonObjectMapper()
      .readValue(json, SohMonitorValues.class));
  }
}