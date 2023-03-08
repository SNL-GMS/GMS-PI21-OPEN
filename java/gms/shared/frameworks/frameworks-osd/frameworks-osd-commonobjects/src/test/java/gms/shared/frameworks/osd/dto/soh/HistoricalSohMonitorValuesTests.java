package gms.shared.frameworks.osd.dto.soh;


import com.fasterxml.jackson.core.JsonProcessingException;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HistoricalSohMonitorValuesTests {

  @Test
  void testCreate() {
    assertDoesNotThrow(() ->
      HistoricalSohMonitorValues
        .create("MKAR.MK01.SHZ",
          Map.of(SohMonitorType.MISSING, PercentSohMonitorValues.create(new double[]{1.0}))));

    assertDoesNotThrow(() ->
      HistoricalSohMonitorValues
        .create("MKAR.MK01.SHZ",
          Map.of(SohMonitorType.LAG,
            DurationSohMonitorValues.create(new long[]{1}))));
  }

  @Test
  void testCreateEnsureValuesMatchType() {
    assertThrows(IllegalArgumentException.class, () ->
      HistoricalSohMonitorValues.create("MKAR.MK01.SHZ",
        Map.of(SohMonitorType.LAG,
          PercentSohMonitorValues.create(new double[]{1.0}))));
  }

  @Test
  void testJsonSerDes() throws JsonProcessingException {
    HistoricalSohMonitorValues values = HistoricalSohMonitorValues
      .create("MKAR.MK01.SHZ",
        Map.of(SohMonitorType.MISSING, PercentSohMonitorValues.create(new double[]{1.0})));

    String json = CoiObjectMapperFactory.getJsonObjectMapper()
      .writeValueAsString(values);

    assertEquals(values, CoiObjectMapperFactory.getJsonObjectMapper()
      .readValue(json, HistoricalSohMonitorValues.class));
  }
}