package gms.core.performancemonitoring.soh.control;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.core.performancemonitoring.soh.control.StationSohControlConfiguration.ConfigurationPair;
import gms.core.performancemonitoring.soh.control.configuration.CapabilitySohRollupDefinition;
import gms.core.performancemonitoring.soh.control.configuration.ChannelSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.SohControlDefinition;
import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.StationSohMonitoringDefinition;
import gms.core.performancemonitoring.soh.control.configuration.TimeWindowDefinition;
import gms.core.performancemonitoring.soh.control.kafka.KafkaSohExtractConsumerFactory.SohExtractKafkaConsumer;
import gms.core.performancemonitoring.soh.control.kafka.SohExtractReceiver;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.soh.AcquiredStationSohExtract;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.soh.ChannelSoh;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaOutbound;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;
import reactor.kafka.sender.TransactionManager;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static gms.core.performancemonitoring.soh.control.StationSohControlConstants.CAPABILITY_SOH_ROLLUP_OUTPUT_TOPIC;
import static gms.core.performancemonitoring.soh.control.StationSohControlConstants.INPUT_TOPIC;
import static gms.core.performancemonitoring.soh.control.StationSohControlConstants.MONITOR_LOGGING_PERIOD;
import static gms.core.performancemonitoring.soh.control.StationSohControlConstants.STATION_SOH_OUTPUT_TOPIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class StationSohControlTests {

  @Test
  void testWithNoInputData() throws Exception {
    testKafkaConsumerAndProducerThreads(false);
  }

  @Test
  void testWithInputData() throws Exception {
    testKafkaConsumerAndProducerThreads(true);
  }

  /**
   * Method will test whether the consumer and producer threads are properly working. Note Kafka
   * producer and consumer will be mocked. This will also test the correct time interval is
   * respected.
   */
  private void testKafkaConsumerAndProducerThreads(boolean withData) throws Exception {

    // Extracts that are read in from the Kafka topic by the control.
    List<AcquiredStationSohExtract> extracts = TestFixture.loadExtracts();
    // Lists to capture what the control writes to the output Kafka topics.
    List<StationSoh> stationSohs = new ArrayList<>();
    List<CapabilitySohRollup> capabilitySohRollups = new ArrayList<>();

    SecureRandom random = new SecureRandom("mySeed".getBytes());

    Duration reprocessingPeriod = Duration.ofMillis(500L);

    Set<StationSohDefinition> stationSohDefinitions = TestFixture
      .computeStationSohDefinitions(
        extracts, random
      );

    Set<CapabilitySohRollupDefinition> capabilitySohRollupDefinitions =
      TestFixture.computeCapabilitySohRollupDefinitions(extracts, random);

    Set<String> stationGroups = capabilitySohRollupDefinitions.stream()
      .map(CapabilitySohRollupDefinition::getStationGroup)
      .collect(Collectors.toSet());

    StationSohMonitoringDefinition stationSohMonitoringDefinition =
      StationSohMonitoringDefinition.create(
        Duration.ofDays(1000),
        stationGroups,
        stationSohDefinitions,
        capabilitySohRollupDefinitions
      );

    StationSohControlConfiguration config = mock(
      StationSohControlConfiguration.class
    );

    SohControlDefinition sohControlDefinition = SohControlDefinition.create(
      reprocessingPeriod
    );

    when(config.getInitialConfigurationPair()).thenReturn(
      new ConfigurationPair(
        stationSohMonitoringDefinition,
        sohControlDefinition
      )
    );

    SystemConfig systemConfig = mock(SystemConfig.class);

    when(systemConfig.getValue(INPUT_TOPIC)).thenReturn("test.soh.extracts");
    when(systemConfig.getValue(STATION_SOH_OUTPUT_TOPIC))
      .thenReturn("test.soh.rollup");
    when(systemConfig.getValue(CAPABILITY_SOH_ROLLUP_OUTPUT_TOPIC))
      .thenReturn("test.soh.capability.rollup");
    when(systemConfig.getValue(MONITOR_LOGGING_PERIOD))
      .thenReturn("PT0.000000001S");

    SohExtractReceiver sohExtractReceiver = new TestSohExtractReceiver(withData ?
      extracts : Collections.emptyList());

    Map<String, StationSohDefinition> definitionMap =
      stationSohMonitoringDefinition
        .getStationSohDefinitions()
        .stream()
        .collect(Collectors.toMap(StationSohDefinition::getStationName, def -> def));

    CountDownLatch stationSohsCountDownLatch = new CountDownLatch(definitionMap.size());
    CountDownLatch capabilitySohRollupsCountDownLatch = new CountDownLatch(capabilitySohRollupDefinitions.size());

    KafkaSender<String, String> kafkaSender = new MockKafkaSender(
      stationSohs,
      capabilitySohRollups,
      stationSohsCountDownLatch,
      capabilitySohRollupsCountDownLatch
    );

    StationSohControl stationSohControl = new StationSohControl(
      () -> config,
      systemConfig,
      mock(OsdRepositoryInterface.class),
      sohExtractReceiver,
      kafkaSender);

    assertSame(
      systemConfig,
      stationSohControl.getSystemConfig()
    );

    try {

      // passing an empty lambda, no need to do anything with the callback
      stationSohControl.start();


      capabilitySohRollupsCountDownLatch.await(10, TimeUnit.SECONDS);
      stationSohsCountDownLatch.await(10, TimeUnit.SECONDS);

    } finally {
      stationSohControl.stop();
    }

    assertEquals(definitionMap.size(), stationSohs.size());

    assertEquals(definitionMap.keySet(),
      stationSohs.stream().map(StationSoh::getStationName).collect(Collectors.toSet()));

    assertEquals(capabilitySohRollupDefinitions.size(), capabilitySohRollups.size());

    for (StationSoh soh : stationSohs) {
      checkStationSohMatchesDefinition(soh, definitionMap.get(soh.getStationName()));
    }
  }

  /**
   * Checks that a {@code StationSoh} computed by the control matches the appropriate {@code
   * StationSohDefinition}
   */
  void checkStationSohMatchesDefinition(
    StationSoh stationSoh, StationSohDefinition stationSohDefinition) {

    // First of all, the station names better match.
    assertEquals(stationSoh.getStationName(), stationSohDefinition.getStationName());

    Map<String, ChannelSohDefinition> channelDefMap = stationSohDefinition
      .getChannelSohDefinitions()
      .stream()
      .collect(Collectors.toMap(ChannelSohDefinition::getChannelName, cd -> cd));

    // Must have the expected ChannelSohs
    assertEquals(channelDefMap.keySet(), stationSoh.getChannelSohs()
      .stream()
      .map(ChannelSoh::getChannelName).collect(Collectors.toSet()));
  }

  @Test
  void testGetStationCacheDurations() {

    var station1SohDefinition = Mockito.mock(StationSohDefinition.class);
    var station2SohDefinition = Mockito.mock(StationSohDefinition.class);

    Mockito.when(station1SohDefinition.getTimeWindowBySohMonitorType()).thenReturn(
      Map.of(
        SohMonitorType.MISSING, TimeWindowDefinition.create(
          Duration.ofMinutes(5), Duration.ofMinutes(5)
        ),
        SohMonitorType.LAG, TimeWindowDefinition.create(
          Duration.ofMinutes(3), Duration.ofMinutes(2)
        )
      )
    );

    Mockito.when(station1SohDefinition.getStationName()).thenReturn("Station1");

    Mockito.when(station2SohDefinition.getTimeWindowBySohMonitorType()).thenReturn(
      Map.of(
        SohMonitorType.MISSING, TimeWindowDefinition.create(
          Duration.ofMinutes(2), Duration.ofMinutes(3)
        ),
        SohMonitorType.LAG, TimeWindowDefinition.create(
          Duration.ofMinutes(4), Duration.ofMinutes(2)
        )
      )
    );

    Mockito.when(station2SohDefinition.getStationName()).thenReturn("Station2");

    var durationMap = StationSohControl.getStationCacheDurations(
      Set.of(station1SohDefinition, station2SohDefinition)
    );

    Assertions.assertEquals(2, durationMap.size());

    Assertions.assertEquals(
      Duration.ofMinutes(11), durationMap.get("Station1")
    );

    Assertions.assertEquals(
      Duration.ofMinutes(8), durationMap.get("Station2")
    );
  }

  private static class MockKafkaSender implements KafkaSender<String, String> {

    private List<StationSoh> stationSohListToPopulate;
    private List<CapabilitySohRollup> capabilitySohRollupListToPopulate;
    private CountDownLatch stationSohsCountDownLatch;
    private CountDownLatch capabilitySohRollupsCountDownLatch;

    MockKafkaSender(
      List<StationSoh> stationSohListToPopulate,
      List<CapabilitySohRollup> capabilitySohRollupListToPopulate,
      CountDownLatch stationSohsCountDownLatch,
      CountDownLatch capabilitySohRollupsCountDownLatch
    ) {
      this.stationSohListToPopulate = stationSohListToPopulate;
      this.capabilitySohRollupListToPopulate = capabilitySohRollupListToPopulate;
      this.stationSohsCountDownLatch = stationSohsCountDownLatch;
      this.capabilitySohRollupsCountDownLatch = capabilitySohRollupsCountDownLatch;
    }

    @Override
    public <T> Flux<SenderResult<T>> send(
      Publisher<? extends SenderRecord<String, String, T>> records) {

      List<SenderResult<T>> results = new ArrayList<>();
      CountDownLatch latch = new CountDownLatch(1);
      AtomicLong offset = new AtomicLong(0);
      ObjectMapper om = CoiObjectMapperFactory.getJsonObjectMapper();

      records.subscribe(new BaseSubscriber<SenderRecord<String, String, T>>() {
        @Override
        protected void hookOnNext(SenderRecord<String, String, T> value) {

          AtomicReference<Exception> exceptionRef = new AtomicReference<>();
          try {
            //productList.add(om.readValue(value.value(), clazz));
            if (value.correlationMetadata() == StationSoh.class) {
              synchronized (stationSohListToPopulate) {
                stationSohListToPopulate.add(
                  om.readValue(value.value(), StationSoh.class)
                );
              }
              stationSohsCountDownLatch.countDown();
            } else if (value.correlationMetadata() == CapabilitySohRollup.class) {
              synchronized (capabilitySohRollupListToPopulate) {
                capabilitySohRollupListToPopulate.add(
                  om.readValue(value.value(), CapabilitySohRollup.class)
                );
              }
              capabilitySohRollupsCountDownLatch.countDown();
            }
          } catch (JsonProcessingException e) {
            exceptionRef.set(e);
          }

          RecordMetadata recordMetadata = new RecordMetadata(
            new TopicPartition(value.topic(), 0),
            0L,
            offset.getAndIncrement(),
            System.currentTimeMillis(),
            0L,
            value.key() != null ? value.key().getBytes().length : 0,
            value.value() != null ? value.value().getBytes().length : 0);

          results.add(new SenderResult<T>() {
            @Override
            public RecordMetadata recordMetadata() {
              return recordMetadata;
            }

            @Override
            public Exception exception() {
              return exceptionRef.get();
            }

            @Override
            public T correlationMetadata() {
              return value.correlationMetadata();
            }
          });
        }

        @Override
        protected void hookOnComplete() {
          latch.countDown();
        }

        @Override
        protected void hookOnError(Throwable throwable) {
          latch.countDown();
        }
      });

      try {
        latch.await();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      return Flux.fromIterable(results);
    }

    // The rest of the methods do nothing, since send() is the only one used.

    @Override
    public <T> Flux<Flux<SenderResult<T>>> sendTransactionally(
      Publisher<? extends Publisher<? extends SenderRecord<String, String, T>>> records) {
      return null;
    }

    @Override
    public TransactionManager transactionManager() {
      return null;
    }

    @Override
    public KafkaOutbound<String, String> createOutbound() {
      return null;
    }

    @Override
    public <T> Mono<T> doOnProducer(Function<Producer<String, String>, ? extends T> function) {
      return null;
    }

    @Override
    public void close() {
    }
  }

  private static class TestSohExtractReceiver implements SohExtractReceiver {

    private volatile boolean consuming;
    private Disposable disposable;
    private Collection<AcquiredStationSohExtract> extracts;

    TestSohExtractReceiver(Collection<AcquiredStationSohExtract> extracts) {
      this.extracts = extracts;
    }

    @Override
    public void receive(
      Duration processingInterval,
      SohExtractKafkaConsumer extractSubscriber,
      List<AcquiredStationSohExtract> cacheData) {

      if (consuming) {
        throw new IllegalStateException("already consuming");
      }

      consuming = true;

      AtomicBoolean firstCall = new AtomicBoolean(true);

      disposable = Flux.fromIterable(extracts)
        .bufferTimeout(10000000, processingInterval)
        .switchIfEmpty(Mono.just(List.of()))
        .map(
          extracts1 -> {

            if (firstCall.compareAndExchange(true, false)) {
              return new HashSet<>(extracts1);
            } else {
              return Collections.<AcquiredStationSohExtract>emptySet();
            }
          }
        )
        .subscribe(
          extractSubscriber
        );
    }

    @Override
    public void stopProcessingInterval() {
      if (consuming) {
        try {
          if (disposable != null) {
            disposable.dispose();
          }
        } finally {
          disposable = null;
          consuming = false;
        }
      }
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isReceiving() {
      return consuming;
    }

  }
}
