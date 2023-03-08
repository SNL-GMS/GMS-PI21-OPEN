package gms.shared.frameworks.osd.coi;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ProcessingResultTest {

  @Test
  void testSerialization() throws IOException {
    DurationValue expectedResultValue = DurationValue.from(Duration.ofSeconds(1), Duration.ZERO);
    ProcessingResult<InstantValue, DurationValue> expectedProcessingResult =
      ProcessingResult.<InstantValue, DurationValue>builder()
        .addRejectedInput(RejectedInput.create(InstantValue.from(Instant.EPOCH, Duration.ofNanos(2534)), "Test 1"))
        .addRejectedInput(RejectedInput.create(InstantValue.from(Instant.now(), Duration.ofMillis(32523)), "Test 2"))
        .setResult(expectedResultValue)
        .build();

    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    TypeFactory typeFactory = objectMapper.getTypeFactory();
    JavaType processingResultType = typeFactory.constructParametricType(ProcessingResult.class, InstantValue.class, DurationValue.class);
    String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(expectedProcessingResult);
    ProcessingResult<InstantValue, DurationValue> actualProcessingResult = objectMapper.readValue(json, processingResultType);

    Optional<DurationValue> actualResultValue = actualProcessingResult.getResult();
    if (actualResultValue.isPresent()) {
      DurationValue value = actualResultValue.get();
      assertEquals(expectedResultValue, value);
      assertEquals(expectedProcessingResult.getRejectedInputs().size(), actualProcessingResult.getRejectedInputs().size());
      assertTrue(expectedProcessingResult.getRejectedInputs().containsAll(actualProcessingResult.getRejectedInputs()));
    } else {
      fail("Not present");
    }
  }

  @Test
  void testSerializationNullResult() throws IOException {
    ProcessingResult<InstantValue, DurationValue> expectedProcessingResult =
      ProcessingResult.<InstantValue, DurationValue>builder()
        .addRejectedInput(RejectedInput.create(InstantValue.from(Instant.EPOCH, Duration.ofNanos(2534)), "Test 1"))
        .addRejectedInput(RejectedInput.create(InstantValue.from(Instant.now(), Duration.ofMillis(32523)), "Test 2"))
        .build();

    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    TypeFactory typeFactory = objectMapper.getTypeFactory();
    JavaType processingResultType = typeFactory.constructParametricType(ProcessingResult.class, InstantValue.class, DurationValue.class);
    String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(expectedProcessingResult);
    ProcessingResult<InstantValue, DurationValue> actualProcessingResult = objectMapper.readValue(json, processingResultType);

    Optional<DurationValue> actualResultValue = actualProcessingResult.getResult();
    assertFalse(actualResultValue.isPresent());
  }
}