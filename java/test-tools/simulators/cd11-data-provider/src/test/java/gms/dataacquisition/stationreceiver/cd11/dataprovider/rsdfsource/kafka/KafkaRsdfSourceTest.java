package gms.dataacquisition.stationreceiver.cd11.dataprovider.rsdfsource.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import org.apache.commons.lang3.RandomUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

class KafkaRsdfSourceTest {

  private AutoCloseable openMocks;
  private static final Logger logger = LoggerFactory.getLogger(KafkaRsdfSourceTest.class);

  private static final String DATA_SET_BOSA = "./src/test/resources/dataprovider/rsdfsource/kafka/BOSA/rsdf-sta-BOSA-id-000.json";
  private static final String DATA_SET_KMBO = "./src/test/resources/dataprovider/rsdfsource/kafka/KMBO/rsdf-sta-KMBO-id-000.json";
  private static final String DATA_SET_PDAR = "./src/test/resources/dataprovider/rsdfsource/kafka/PDAR/rsdf-sta-PDAR-id-000.json";
  private int partition;

  private static Stream<Arguments> getSeedSetConstraints() {

    return Stream.of(
      arguments(DATA_SET_BOSA),
      arguments(DATA_SET_KMBO),
      arguments(DATA_SET_PDAR),
      arguments(DATA_SET_BOSA),
      arguments(DATA_SET_KMBO),
      arguments(DATA_SET_PDAR),
      arguments(DATA_SET_BOSA),
      arguments(DATA_SET_KMBO),
      arguments(DATA_SET_PDAR)
    );
  }

  @Mock
  private KafkaReceiver<String, String> kafkaReceiver;
  private final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  @BeforeEach
  void initTest() {
    openMocks = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  void endTest() throws Exception {
    openMocks.close();
  }

  @ParameterizedTest
  @MethodSource("getSeedSetConstraints")
  void testGetRsdfFlux(final String dataFileLocation) throws IOException {

    final File seedDataFile = new File(dataFileLocation);
    final List<RawStationDataFrame> seedRsdfs = objectMapper
      .readValue(seedDataFile, objectMapper.getTypeFactory()
        .constructCollectionType(List.class, RawStationDataFrame.class));

    partition = RandomUtils.nextInt();
    AtomicLong offset = new AtomicLong(0);

    final var consumerRecords = Flux.fromIterable(seedRsdfs)
      .map(this::getSeedJson)
      .map(seedJson -> new ConsumerRecord<>("some topic", partition, offset.getAndIncrement(),
        UUID.randomUUID().toString(), seedJson));

    when(kafkaReceiver.receiveAtmostOnce()).thenReturn(consumerRecords);

    final int expectedEmittedItemCount = seedRsdfs.size();
    final KafkaRsdfSource kafkaRsdfSource = new KafkaRsdfSource(kafkaReceiver);
    final List<RawStationDataFrame> rsdfs = new ArrayList<>();

    final var rsdfFlux = kafkaRsdfSource.getRsdfFlux()
      .take(expectedEmittedItemCount)
      .doOnNext(r -> {
        rsdfs.add(r);
        logger.info("\tStation: {}"
            + "\tReception:  {}"
            + "\tStart Time:  {}"
            + "\tEnd Time:  {}"
            + "\tTotal Items: {}",
          r.getMetadata().getStationName(),
          r.getMetadata().getReceptionTime(),
          r.getMetadata().getPayloadStartTime(),
          r.getMetadata().getPayloadEndTime(),
          rsdfs.size()
        );
      });

    StepVerifier.create(rsdfFlux)
      .expectNextSequence(seedRsdfs)
      .verifyComplete();

    assertNotNull(rsdfs);
    assertFalse(rsdfs.isEmpty());
    assertEquals(expectedEmittedItemCount, rsdfs.size());
    seedRsdfs.forEach(seedRsdf -> {
      final var actualRsdf = rsdfs.stream()
        .filter(r -> r.getId().equals(seedRsdf.getId()))
        .findFirst()
        .orElseThrow();
      verifyRsdf(seedRsdf, actualRsdf);
    });
  }

  private String getSeedJson(RawStationDataFrame seedRsdf) {
    try {
      return objectMapper.writeValueAsString(seedRsdf);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize data to JSON", e);
    }
  }

  private void verifyRsdf(RawStationDataFrame seedRsdf, RawStationDataFrame actualRsdf) {
    assertEquals(seedRsdf, actualRsdf);
    assertEquals(seedRsdf.getId(), actualRsdf.getId());
    assertEquals(seedRsdf.getMetadata().getPayloadStartTime(),
      actualRsdf.getMetadata().getPayloadStartTime());
    assertEquals(seedRsdf.getMetadata().getPayloadEndTime(),
      actualRsdf.getMetadata().getPayloadEndTime());
    assertEquals(seedRsdf.getMetadata().getReceptionTime(),
      actualRsdf.getMetadata().getReceptionTime());
  }

}