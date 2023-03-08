package gms.shared.stationdefinition.coi.station;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StationGroupDefinitionTest {

  private static final Logger logger = LoggerFactory.getLogger(StationGroupDefinitionTest.class);

  private final static ObjectMapper mapper = ObjectMapperFactory.getJsonObjectMapper();

  @Test
  void testStation_from_facetedChannelGroups_serializeToAndFrom()
    throws JsonProcessingException {
    final StationGroupDefinition stationGroupDefinition = StationGroupDefinition.from("test", "test description", Instant
      .now(), List.of("station 1", "station 2"));

    final String json = mapper.writeValueAsString(stationGroupDefinition);
    logger.info("json serialized station group definition: {}", json);

    final StationGroupDefinition deserialized = mapper.readValue(json, StationGroupDefinition.class);
    assertEquals(stationGroupDefinition, deserialized);
    assertThat("ChannelGroups in deserialized station group are out of order",
      deserialized.getStationNames(),
      contains(stationGroupDefinition.getStationNames().toArray(new String[]{})));
  }

}