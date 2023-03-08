package gms.core.performancemonitoring.soh.control.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import gms.core.performancemonitoring.soh.control.kafka.KafkaSohExtractConsumerFactory.SohExtractKafkaConsumer;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.soh.AcquiredStationSohExtract;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

/**
 * Implementation of {@link SohExtractReceiver} that consumes {@link AcquiredStationSohExtract}
 * instances from a Kafka topic.
 */

public class ReactorKafkaSohExtractReceiver implements SohExtractReceiver {

  private static final Logger logger = LoggerFactory.getLogger(ReactorKafkaSohExtractReceiver.class);

  private final Map<String, Duration> stationCachingDurations;

  private final KafkaReceiver<String, String> asseKafkaReceiver;

  //
  // Cache of extracts.
  //
  // Keyed by station name, so that the per-station cache duration can be applied when cleaning out
  // the cache
  //
  private final Map<String, NavigableMap<Instant, Set<AcquiredStationSohExtract>>> extractCacheByStation = new ConcurrentHashMap<>();

  private final Map<TopicPartition, ReceiverOffset> offsetMap = new HashMap<>();

  private Disposable extractFluxDisposable;

  private boolean isReceiving;

  /**
   * Constructor
   *
   * @param bootstrapServers the bootstrap servers setting for connecting to Kafka. This must not be
   * null.
   * @param topic the topic from which to read extracts. This must not be null.
   * @param applicationId the application id, used to set the group id for consumption from the
   * Kafka topic. This must not be null.
   * @param stationCachingDurations Map of station name to how long to keep data for the station
   */
  public ReactorKafkaSohExtractReceiver(
    String bootstrapServers,
    String topic,
    String applicationId,
    Map<String, Duration> stationCachingDurations
  ) {

    this.stationCachingDurations = stationCachingDurations;

    Map<String, Object> properties = Map.of(
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
      ConsumerConfig.CLIENT_ID_CONFIG, applicationId,
      ConsumerConfig.GROUP_ID_CONFIG, applicationId,
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"
    );

    ReceiverOptions<String, String> receiverOptions = ReceiverOptions.create(properties);

    asseKafkaReceiver = KafkaReceiver.create(
      receiverOptions.subscription(Collections.singleton(topic)));
  }

  /**
   * Construct using a supplied kafka receiver and initial cache duration. This is meant for testing,
   * so it is package-private.
   *
   * @param mockKafkaReceiver Kafka receiver to use. The name of this parameter is to emphasize this
   * constructors purpose for testing.
   * @param stationCachingDurations Map of station name to how long to keep data for the station
   */
  ReactorKafkaSohExtractReceiver(
    KafkaReceiver<String, String> mockKafkaReceiver,
    Map<String, Duration> stationCachingDurations
  ) {
    this.stationCachingDurations = stationCachingDurations;
    this.asseKafkaReceiver = mockKafkaReceiver;
  }


  /**
   * Begin receiving SohExtracts, by collecting them into a set eache processsing interval,
   * amd then applying sohExtractKafkaConsumer to that set.
   *
   * @param processingInterval how long to buffer extracts consumed before forwarding
   * them to a subscriber. This must not be null.
   * @param sohExtractKafkaConsumer consumer which consumes the set of extracts.
   * @param cacheData Set of AcquiredStationExtracts to start with
   */
  public void receive(
    Duration processingInterval,
    SohExtractKafkaConsumer sohExtractKafkaConsumer,
    List<AcquiredStationSohExtract> cacheData
  ) {
    receive(
      processingInterval,
      sohExtractKafkaConsumer,
      cacheData,

      // No need for more than one thread here
      Schedulers.single()
    );
  }

  /**
   * Begin receiving SohExtracts, by collecting them into a set eache processsing interval,
   * amd then applying sohExtractKafkaConsumer to that set.
   *
   * @param processingInterval how long to buffer extracts consumed before forwarding
   * them to a subscriber. This must not be null.
   * @param sohExtractKafkaConsumer consumer which consumes the set of extracts.
   * @param cacheData Set of AcquiredStationExtracts to start with
   * @param scheduler The scheduler to use. When testing, this can be a virtual scheduler.
   */
  void receive(
    Duration processingInterval,
    SohExtractKafkaConsumer sohExtractKafkaConsumer,
    List<AcquiredStationSohExtract> cacheData,
    Scheduler scheduler
  ) {

    if (isReceiving) {
      throw new IllegalStateException(
        "Already receiving - please stop this ReactorKafkaSohExtractReceiver before calling receive again");
    }

    cacheData.forEach(this::addToCache);

    var asseFlux = receiveAsse();

    extractFluxDisposable = asseFlux
      //
      // Use window and flatMap, so that we always get a list of ASSEs, even when nothing has arrived
      // in the processing interval (in which case, the list will be empty)
      //
      .window(processingInterval, scheduler)
      //
      // Transform into a flux of lists:
      // Flux<Flux<AcquiredStationSohExtract>>
      //    ---> collectList ---> Flux<Mono<List<AcquiredStationSohExtract>>>
      //    ---> flatMap     ---> Flux<List<AcquiredStationSohExtract>>
      //
      .flatMap(Flux::collectList)
      .map(acquiredStationSohExtractList ->
        {
          logger.info(
            "{} new extracts were received for this processing interval",
            acquiredStationSohExtractList.size()
          );

          synchronized (extractCacheByStation) {

            acquiredStationSohExtractList.forEach(this::addToCache);

            extractCacheByStation.forEach((stationName, subCache) ->
              subCache.headMap(Instant.now().minus(stationCachingDurations.get(stationName)))
                .clear());

            offsetMap.values().forEach(ReceiverOffset::commit);

            return extractCacheByStation.values()
              .stream().map(Map::values)
              .flatMap(Collection::stream)
              .flatMap(Collection::stream)
              .collect(Collectors.toSet());
          }
        }
      )
      .subscribe(sohExtractKafkaConsumer);

    isReceiving = true;
  }

  /**
   * Stop the flux that runs on the processing interval. Used to update configuraton.
   */
  public void stopProcessingInterval() {

    this.extractFluxDisposable.dispose();
  }

  /**
   * Stop both the raw ASSE flux and the processing interval flux. Used when we are completely
   * done with this receiver.
   */
  public void stop() {
    stopProcessingInterval();
    this.isReceiving = false;
  }

  /**
   * Answers whether we are receiving extracts.
   *
   * @return whether we are receiving extracts.
   */
  public boolean isReceiving() {
    return isReceiving;
  }

  private void addToCache(AcquiredStationSohExtract extract) {

    addToCache(
      extract,
      Instant.now(),
      stationCachingDurations,
      extractCacheByStation
    );
  }

  /**
   * Start receiving raw AcquiredStationSohExtracts, deserializing them, and caching them.
   */
  private Flux<AcquiredStationSohExtract> receiveAsse() {

    Flux<ReceiverRecord<String, String>> kafkaFlux = asseKafkaReceiver.receive();

    var objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

    return kafkaFlux
      .retryWhen(Retry.backoff(8, Duration.ofSeconds(1)))
      .doOnNext(receiverRecord -> {
        var receiverOffset = receiverRecord.receiverOffset();

        offsetMap.compute(receiverOffset.topicPartition(), (tp, ro) -> {
          if (ro == null || receiverOffset.offset() > ro.offset()) {
            return receiverOffset;
          }
          return ro;
        });
      })
      .map(receiverRecord -> {

        try {
          return Optional.of(objectMapper.readValue(
              receiverRecord.value(),
              AcquiredStationSohExtract.class
            )
          );

        } catch (JsonProcessingException e) {
          logger.info(
            "Error parsing JSON, continuing to next record"
          );
          return Optional.<AcquiredStationSohExtract>empty();
        }
      })
      .filter(Optional::isPresent)
      .map(Optional::get);
  }

  private static Instant payloadEndTime(AcquiredStationSohExtract extract) {
    return extract.getAcquisitionMetadata()
      .get(0) // ASSUMPTION! There is only a single RSDF metadata object in the extract.
      .getPayloadEndTime();
  }

  /**
   * Add an extract to a given cache, given that it is newer than then (now - stationduration)
   * where stationduration is the cache duration for the station tha the extract is associated with.
   * <p>
   * This is package-private so that it can be tested.
   *
   * @param extract the extract to add
   * @param now the callers concept of "now" (so that tests can be consisitent)
   * @param stationCachingDurations Map od station name to station cache duration
   * @param extractCacheByStation the cache to add to
   */
  static void addToCache(
    AcquiredStationSohExtract extract,
    Instant now,
    Map<String, Duration> stationCachingDurations,
    Map<String, NavigableMap<Instant, Set<AcquiredStationSohExtract>>> extractCacheByStation
  ) {

    var stationName = extract.getAcquisitionMetadata()
      .get(0) // ASSUMPTION! There is only a single RSDF metadata object in the extract.
      .getStationName();

    if (stationCachingDurations.containsKey(stationName)) {
      var payloadEndTimeLimit = now.minus(stationCachingDurations.get(stationName));
      var possibleEnd = payloadEndTime(extract);

      if (possibleEnd.isAfter(payloadEndTimeLimit)) {
        extractCacheByStation
          .computeIfAbsent(
            stationName, dummyKey -> new ConcurrentSkipListMap<>()
          )
          .computeIfAbsent(
            possibleEnd, instant -> new HashSet<>()
          ).add(extract);
      }
    } else {
      logger.debug("addToCache was called with a station that is not configured: {}", stationName);
    }

  }
}
