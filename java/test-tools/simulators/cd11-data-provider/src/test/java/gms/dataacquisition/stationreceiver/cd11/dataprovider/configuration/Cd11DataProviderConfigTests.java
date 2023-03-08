package gms.dataacquisition.stationreceiver.cd11.dataprovider.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class Cd11DataProviderConfigTests {

  private static final String CONFIG_PATH = "./src/test/resources/config/";

  private static ObjectMapper mapper = CoiObjectMapperFactory.getJsonObjectMapper();

  @Test
  void testFileConfig() {
    File config = new File(CONFIG_PATH + "1for30min-config.json");

    assertDoesNotThrow(
      () -> mapper.writeValueAsString(mapper.readValue(config, Cd11DataProviderConfig.class)));
  }

  @Test
  void testKafkaConfig() {
    File config = new File(CONFIG_PATH + "repeater-config.json");

    assertDoesNotThrow(
      () -> mapper.writeValueAsString(mapper.readValue(config, Cd11DataProviderConfig.class)));
  }
}