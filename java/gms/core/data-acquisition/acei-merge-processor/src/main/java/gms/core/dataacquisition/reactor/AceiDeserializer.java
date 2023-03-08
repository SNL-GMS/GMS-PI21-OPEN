package gms.core.dataacquisition.reactor;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Convenience Kafka deserializer for receiving {@link AcquiredChannelEnvironmentIssue}s
 */
public class AceiDeserializer implements Deserializer<AcquiredChannelEnvironmentIssue<?>> {

  private static final Logger logger = LoggerFactory.getLogger(AceiDeserializer.class);
  private static final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  @Override
  public AcquiredChannelEnvironmentIssue<?> deserialize(String topic, byte[] data) {
    try {
      return objectMapper.readValue(data, AcquiredChannelEnvironmentIssue.class);
    } catch (IOException e) {
      logger.error("Could not deserialize ACEI: {}", data, e);
      return null;
    }
  }
}
