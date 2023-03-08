package gms.core.performancemonitoring.soh.control.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.core.performancemonitoring.soh.control.TestFixture;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.soh.AcquiredStationSohExtract;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrameMetadata;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.TransactionManager;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class ReactorKafkaSohExtractReceiverTests {

  @Test
  void testReceiverReceivesAsset() throws IOException {

    var extracts = TestFixture.loadExtracts();

    var extractsScheduler = VirtualTimeScheduler.getOrSet();

    var mockKafkaReceiver = getKafkaReceiver(
      Flux.fromIterable(extracts)
        .delayElements(Duration.ofMillis(5), extractsScheduler)
    );

    ReactorKafkaSohExtractReceiver kafkaSohExtractReceiver = new ReactorKafkaSohExtractReceiver(
      mockKafkaReceiver,
      createDumbDurationsMap(extracts)
    );

    List<AcquiredStationSohExtract> extractsReceivedList = new ArrayList<>();

    List<ReceiverOffset> receiverOffsets = new ArrayList<>();

    var intervalScheduler = VirtualTimeScheduler.getOrSet();

    // Define these here so that SonarQube does not complain that the arguments to receive,
    // in the assertThrows below, can throw an exception.
    var duration = Duration.ofSeconds(1);
    var emptyCache = List.<AcquiredStationSohExtract>of();

    kafkaSohExtractReceiver.receive(
      duration,
      extractsReceivedList::addAll,
      emptyCache,
      intervalScheduler
    );

    // Make sure we can only start receiving once before stopping the receiver
    Assertions.assertThrows(IllegalStateException.class, () -> kafkaSohExtractReceiver.receive(
      duration,
      p -> {
      },
      emptyCache,
      intervalScheduler
    ));

    Assertions.assertTrue(kafkaSohExtractReceiver.isReceiving());

    extractsScheduler.advanceTime();

    try {

      intervalScheduler.advanceTimeBy(Duration.ofSeconds(1));

      Assertions.assertEquals(
        extracts.size(),
        extractsReceivedList.size()
      );

    } finally {
      kafkaSohExtractReceiver.stop();
      Assertions.assertFalse(kafkaSohExtractReceiver.isReceiving());
      intervalScheduler.dispose();
      extractsScheduler.dispose();
    }

  }

  @Test
    // TODO: Look into rewriting this to use the now package-private cache methods
  void testCacheGetsInitialized() throws IOException {

    var extracts = TestFixture.loadExtracts();
    var extraExtracts = loadAlternateExtracts();

    var mockKafkaReceiver = getKafkaReceiver(
      Flux.fromIterable(extracts)
    );

    var allExtracts = new ArrayList<AcquiredStationSohExtract>();
    allExtracts.addAll(extracts);
    allExtracts.addAll(extraExtracts);

    ReactorKafkaSohExtractReceiver kafkaSohExtractReceiver = new ReactorKafkaSohExtractReceiver(
      mockKafkaReceiver,
      createDumbDurationsMap(allExtracts)
    );

    List<AcquiredStationSohExtract> extractsReceivedList = new ArrayList<>();

    List<ReceiverOffset> receiverOffsets = new ArrayList<>();

    var intervalScheduler = VirtualTimeScheduler.getOrSet();

    kafkaSohExtractReceiver.receive(
      Duration.ofSeconds(1),
      extractsReceivedList::addAll,
      extraExtracts,
      intervalScheduler
    );

    try {
      intervalScheduler.advanceTimeBy(Duration.ofSeconds(1));

      Assertions.assertEquals(
        extracts.size() + extraExtracts.size(),
        extractsReceivedList.size()
      );

    } finally {
      kafkaSohExtractReceiver.stop();
      intervalScheduler.dispose();
    }
  }

  @ParameterizedTest
  @MethodSource("addToCacheTestSource")
  void testAddToCache(
    AcquiredStationSohExtract extract,
    Instant now,
    Map<String, Duration> stationCachingDurations,
    Supplier<Map<String, NavigableMap<Instant, Set<AcquiredStationSohExtract>>>> originalCacheSupplier,
    Supplier<Map<String, NavigableMap<Instant, Set<AcquiredStationSohExtract>>>> expectedCacheSupplier
  ) {

    var cache = originalCacheSupplier.get();

    ReactorKafkaSohExtractReceiver.addToCache(
      extract,
      now,
      stationCachingDurations,
      cache
    );

    Assertions.assertEquals(
      cache,
      expectedCacheSupplier.get()
    );
  }

  private static Stream<Arguments> addToCacheTestSource() throws IOException {

    var now = Instant.now();

    var extracts = modifyPayloadTimes(
      TestFixture.loadExtracts(),
      List.of(
        now.minusSeconds(60),  //LBTB
        now.minusSeconds(61),  //ZALV
        now.minusSeconds(120), //PLCA 1st
        now.minusSeconds(80),  //PLCA 2nd
        now.minusSeconds(50)   //I37NO
      )
    );

    return Stream.of(
      Arguments.arguments(
        extracts.get(3), now,
        Map.of(
          "LBTB", Duration.ofSeconds(61),
          "ZALV", Duration.ofSeconds(62),
          "PLCA", Duration.ofSeconds(121),
          "I37NO", Duration.ofSeconds(51)
        ),
        (Supplier<Map<String, NavigableMap<Instant, Set<AcquiredStationSohExtract>>>>) () ->
          createCacheFromExtracts(
            extracts.get(0),
            extracts.get(1),
            extracts.get(2),
            extracts.get(4)
          ),
        (Supplier<Map<String, NavigableMap<Instant, Set<AcquiredStationSohExtract>>>>) () ->
          createCacheFromExtracts(
            extracts.get(0),
            extracts.get(1),
            extracts.get(2),
            extracts.get(3),
            extracts.get(4)
          )
      ),

      Arguments.arguments(
        extracts.get(2), now,
        Map.of(
          "LBTB", Duration.ofSeconds(61),
          "ZALV", Duration.ofSeconds(62),
          "PLCA", Duration.ofSeconds(119),
          "I37NO", Duration.ofSeconds(51)
        ),
        (Supplier<Map<String, NavigableMap<Instant, Set<AcquiredStationSohExtract>>>>) () ->
          createCacheFromExtracts(
            extracts.get(0),
            extracts.get(1),
            extracts.get(3),
            extracts.get(4)
          ),
        (Supplier<Map<String, NavigableMap<Instant, Set<AcquiredStationSohExtract>>>>) () ->
          createCacheFromExtracts(
            extracts.get(0),
            extracts.get(1),
            extracts.get(3),
            extracts.get(4)
          )
      ),

      Arguments.arguments(
        extracts.get(0), now,
        Map.of(
          "LBTB", Duration.ofSeconds(59),
          "ZALV", Duration.ofSeconds(62),
          "PLCA", Duration.ofSeconds(121),
          "I37NO", Duration.ofSeconds(51)
        ),
        (Supplier<Map<String, NavigableMap<Instant, Set<AcquiredStationSohExtract>>>>) () ->
          createCacheFromExtracts(
            extracts.get(1),
            extracts.get(2),
            extracts.get(3),
            extracts.get(4)
          ),
        (Supplier<Map<String, NavigableMap<Instant, Set<AcquiredStationSohExtract>>>>) () ->
          createCacheFromExtracts(
            extracts.get(1),
            extracts.get(2),
            extracts.get(3),
            extracts.get(4)
          )
      )
    );
  }

  private static Map<String, Duration> createDumbDurationsMap(
    List<AcquiredStationSohExtract> extractList
  ) {

    return extractList.stream()
      .map(extract -> extract.getAcquisitionMetadata().get(0).getStationName())
      .distinct()
      .collect(Collectors.toMap(Function.identity(), stationName -> Duration.ofDays(20000)));
  }

  private static Map<String, NavigableMap<Instant, Set<AcquiredStationSohExtract>>> createCacheFromExtracts(
    AcquiredStationSohExtract... extracts
  ) {
    return createCacheFromExtracts(Arrays.asList(extracts));
  }

  private static Map<String, NavigableMap<Instant, Set<AcquiredStationSohExtract>>> createCacheFromExtracts(
    List<AcquiredStationSohExtract> extracts
  ) {
    var cache = new HashMap<String, NavigableMap<Instant, Set<AcquiredStationSohExtract>>>();

    extracts.forEach(
      extract -> {

        var payloadEndTime = extract.getAcquisitionMetadata()
          .get(0)
          .getPayloadEndTime();

        var stationName = extract.getAcquisitionMetadata()
          .get(0)
          .getStationName();

        cache.computeIfAbsent(
          stationName,
          dummy -> new TreeMap<>()
        ).computeIfAbsent(
          payloadEndTime, instant -> new HashSet<>()
        ).add(extract);
      }
    );

    return cache;
  }

  private static List<AcquiredStationSohExtract> modifyPayloadTimes(
    List<AcquiredStationSohExtract> extractList,
    List<Instant> newPayloadTimes
  ) {

    var newExtractList = new ArrayList<AcquiredStationSohExtract>();

    IntStream.range(0, extractList.size()).forEach(i -> {

      var oldRsdf = extractList.get(i)
        .getAcquisitionMetadata()
        .get(0);

      var newRsdf = RawStationDataFrameMetadata.builder()
        .setStationName(oldRsdf.getStationName())
        .setPayloadEndTime(newPayloadTimes.get(i))
        .setAuthenticationStatus(oldRsdf.getAuthenticationStatus())
        .setPayloadFormat(oldRsdf.getPayloadFormat())
        .setPayloadStartTime(oldRsdf.getPayloadStartTime())
        .setChannelNames(oldRsdf.getChannelNames())
        .setReceptionTime(oldRsdf.getReceptionTime())
        .setWaveformSummaries(oldRsdf.getWaveformSummaries())
        .build();

      var acquiredChannelEnvironmentIssueList = extractList.get(i)
        .getAcquiredChannelEnvironmentIssues();

      var newAsse = AcquiredStationSohExtract.create(
        List.of(newRsdf),
        acquiredChannelEnvironmentIssueList
      );

      newExtractList.add(newAsse);
    });

    return newExtractList;
  }

  private static KafkaReceiver<String, String> getKafkaReceiver(
    Flux<AcquiredStationSohExtract> asseBatchFlux
  ) {

    return new KafkaReceiver<>() {
      @Override
      public Flux<ReceiverRecord<String, String>> receive() {
        AtomicInteger offsetRef = new AtomicInteger();

        return asseBatchFlux
          .map(acquiredStationSohExtract -> {
              var objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

              String serializedAsse;
              try {
                serializedAsse = objectMapper.writeValueAsString(
                  acquiredStationSohExtract
                );
              } catch (JsonProcessingException e) {
                throw new IllegalStateException(e);
              }

              int offset = offsetRef.incrementAndGet();

              return new ReceiverRecord<>(
                new ConsumerRecord<>(
                  "DUMMY-TOPIC",
                  0,
                  offset,
                  Instant.now().toEpochMilli(),
                  TimestampType.CREATE_TIME,
                  0,
                  0,
                  serializedAsse.getBytes().length,
                  null,
                  serializedAsse
                ),
                new ReceiverOffset() {
                  @Override
                  public TopicPartition topicPartition() {
                    return new TopicPartition(
                      "DUMMY-TOPIC",
                      0
                    );
                  }

                  @Override
                  public long offset() {
                    return offset;
                  }

                  @Override
                  public void acknowledge() {
                  }

                  @Override
                  public Mono<Void> commit() {
                    return null;
                  }
                }
              );

            }
          );
      }

      @Override
      public Flux<Flux<ConsumerRecord<String, String>>> receiveAutoAck() {
        return null;
      }

      @Override
      public Flux<ConsumerRecord<String, String>> receiveAtmostOnce() {
        return null;
      }

      @Override
      public Flux<Flux<ConsumerRecord<String, String>>> receiveExactlyOnce(
        TransactionManager transactionManager) {
        return null;
      }

      @Override
      public <T> Mono<T> doOnConsumer(Function<Consumer<String, String>, ? extends T> function) {
        return null;
      }
    };
  }

  static List<AcquiredStationSohExtract> loadAlternateExtracts() throws IOException {

    InputStream is = TestFixture.class.getResourceAsStream("/sohextracts-fakestations1.json");
    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    JavaType extractListType = objectMapper.getTypeFactory()
      .constructCollectionType(List.class, AcquiredStationSohExtract.class);
    return objectMapper.readValue(is, extractListType);
  }

}
