package gms.shared.frameworks.osd.dto.soh;

import com.fasterxml.jackson.core.JsonProcessingException;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PercentSohMonitorValuesTests {

  @Test
  void testJsonSerDes() throws JsonProcessingException {
    PercentSohMonitorValues values = PercentSohMonitorValues.create(new double[]{1.0});

    String json = CoiObjectMapperFactory.getJsonObjectMapper()
      .writeValueAsString(values);

    assertEquals(values, CoiObjectMapperFactory.getJsonObjectMapper()
      .readValue(json, SohMonitorValues.class));
  }
}