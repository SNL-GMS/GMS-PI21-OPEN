package gms.shared.frameworks.osd.dto.soh;

import com.fasterxml.jackson.core.JsonProcessingException;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HistoricalStationSohTests {

  @Test
  void testCreate() {
    assertDoesNotThrow(() ->
      HistoricalStationSoh
        .create("MKAR", new long[]{Instant.now().toEpochMilli()},
          List.of(HistoricalSohMonitorValues.create("MKAR.MK01.SHZ",
            Map.of(SohMonitorType.MISSING,
              PercentSohMonitorValues.create(new double[]{1.0}))))));
  }

  @Test
  void testCreateArraysNotSameLength() {
    assertThrows(IllegalArgumentException.class, () ->
      HistoricalStationSoh
        .create("MKAR", new long[]{Instant.now().toEpochMilli()},
          List.of(HistoricalSohMonitorValues.create(
            "MKAR.MK01.SHZ",
            Map.of(SohMonitorType.MISSING,
              PercentSohMonitorValues.create(new double[]{1.0, 2.0}))))));
  }

  @Test
  void testJsonSerDes() throws JsonProcessingException {

    List<HistoricalSohMonitorValues> monitorValuesList = List.of(
      HistoricalSohMonitorValues.create("MKAR.MK01.SHZ",
        Map.of(SohMonitorType.MISSING, PercentSohMonitorValues.create(new double[]{1.0}),
          SohMonitorType.LAG, DurationSohMonitorValues.create(new long[]{1}))));

    HistoricalStationSoh soh = HistoricalStationSoh
      .create("MKAR", new long[]{Instant.now().toEpochMilli()}, monitorValuesList);

    String json = CoiObjectMapperFactory.getJsonObjectMapper()
      .writeValueAsString(soh);

    assertEquals(soh, CoiObjectMapperFactory.getJsonObjectMapper()
      .readValue(json, HistoricalStationSoh.class));
  }
}