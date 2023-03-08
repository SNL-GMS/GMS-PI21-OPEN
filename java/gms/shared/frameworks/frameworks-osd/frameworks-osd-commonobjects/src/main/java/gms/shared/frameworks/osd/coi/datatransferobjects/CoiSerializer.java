package gms.shared.frameworks.osd.coi.datatransferobjects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public class CoiSerializer<T> implements Serializer<T> {

  private static final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  @Override
  public byte[] serialize(String topic, T data) {
    try {
      return objectMapper.writeValueAsBytes(data);
    } catch (JsonProcessingException e) {
      throw new SerializationException("Error when serializing object to JSON byte[]", e);
    }
  }
}

