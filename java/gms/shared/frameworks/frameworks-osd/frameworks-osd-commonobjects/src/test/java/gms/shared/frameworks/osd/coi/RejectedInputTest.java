package gms.shared.frameworks.osd.coi;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RejectedInputTest {

  @Test
  void testSerialization() throws IOException {
    RejectedInput<InstantValue> expectedInput = RejectedInput.create(
      InstantValue.from(Instant.EPOCH, Duration.ofSeconds(1)),
      "Test");
    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    JavaType rejectedInputType = objectMapper.getTypeFactory()
      .constructParametricType(RejectedInput.class, InstantValue.class);
    assertEquals(expectedInput, objectMapper.readValue(
      objectMapper.writeValueAsString(expectedInput), rejectedInputType));
  }

}