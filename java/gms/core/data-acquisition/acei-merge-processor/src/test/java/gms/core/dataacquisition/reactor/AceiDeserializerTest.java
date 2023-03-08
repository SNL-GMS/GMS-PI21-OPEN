package gms.core.dataacquisition.reactor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.core.dataacquisition.TestFixture.AcquiredChannelEnvironmentalIssuesSets;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AceiDeserializerTest {

  static final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  @Test
  void testDeserializeErrorReturnsNull() {
    AcquiredChannelEnvironmentIssue<?> actualAcei = assertDoesNotThrow(
      () -> new AceiDeserializer().deserialize("any", new byte[0]));
    assertNull(actualAcei);
  }

  @Test
  void testDeserialize() throws JsonProcessingException {
    AcquiredChannelEnvironmentIssue<?> inputAcei = AcquiredChannelEnvironmentalIssuesSets.AARDVARK_CLIPPED_4_6;
    byte[] inputJsonBytes = objectMapper.writeValueAsBytes(inputAcei);

    AcquiredChannelEnvironmentIssue<?> actualAcei = new AceiDeserializer()
      .deserialize("any", inputJsonBytes);
    assertEquals(inputAcei, actualAcei);
  }
}