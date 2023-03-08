package gms.testtools.simulators.bridgeddatasourcesimulator.api.coi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.stationdefinition.coi.utils.CoiObjectMapperFactory;
import gms.shared.stationdefinition.dao.css.enums.StaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SiteTest {

  private Site.Builder builder;

  @BeforeEach
  void testSetup() {
    builder = Site.builder();
  }

  @Test
  void testSerializationDeserialization() throws JsonProcessingException {
    ObjectMapper coiObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    Site site = builder
      .setStationCode("TST")
      .setOnDate(Instant.EPOCH)
      .setOffDate(Instant.MAX)
      .setLatitude(0)
      .setLongitude(1)
      .setElevation(2)
      .setStationName("TEST_STA")
      .setStationType(StaType.SINGLE_STATION)
      .setDegreesNorth(56)
      .setDegreesEast(72)
      .setReferenceStation("MRTST")
      .setLoadDate(Instant.EPOCH)
      .build();

    assertEquals(site, coiObjectMapper.readValue(coiObjectMapper.writeValueAsString(site), Site.class));
  }
}
