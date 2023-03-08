package gms.core.dataacquisition.receiver;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChannelLookupConfigurationFileTest {

  @Test
  void testSerialization() throws IOException {
    ChannelLookupConfigurationFile expected = ChannelLookupConfigurationFile
      .from(Map.of("foo", "bar"));

    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

    assertEquals(expected, objectMapper.readValue(objectMapper.writeValueAsString(expected),
      ChannelLookupConfigurationFile.class));
  }
}