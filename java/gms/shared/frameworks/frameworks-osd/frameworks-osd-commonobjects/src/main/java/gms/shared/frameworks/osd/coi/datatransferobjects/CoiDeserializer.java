package gms.shared.frameworks.osd.coi.datatransferobjects;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

public class CoiDeserializer<T> implements Deserializer<T> {

  private static final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  private final Class<T> coiClass;

  public CoiDeserializer(Class<T> coiClass) {
    this.coiClass = coiClass;
  }

  @Override
  public T deserialize(String topic, byte[] data) {
    try {
      return objectMapper.readValue(data, coiClass);
    } catch (final IOException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
