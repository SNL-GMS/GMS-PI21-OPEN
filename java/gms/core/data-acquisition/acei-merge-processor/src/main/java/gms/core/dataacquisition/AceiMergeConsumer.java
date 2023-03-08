package gms.core.dataacquisition;

import gms.core.dataacquisition.reactor.AceiDeserializer;
import gms.core.dataacquisition.reactor.util.ConfigurationToleranceResolver;
import gms.shared.frameworks.common.annotations.Component;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.control.ControlContext;
import gms.shared.frameworks.control.ControlFactory;
import gms.shared.frameworks.messaging.ReactiveFunction;
import gms.shared.frameworks.messaging.ReactiveKafkaStorageConsumer;
import gms.shared.frameworks.messaging.ReactorKafkaUtilities;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import gms.shared.frameworks.osd.repository.utils.CoiEntityManagerFactory;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Service class responsible for reading, merging, and storing {@link AcquiredChannelEnvironmentIssue}s from a Kafka
 * record Flux.
 */
@Component("acei-merge-processor")
public class AceiMergeConsumer {

  private static final Logger logger = LoggerFactory.getLogger(AceiMergeConsumer.class);

  private final SystemConfig systemConfig;
  private final ConfigurationConsumerUtility processingConfig;

  public AceiMergeConsumer(SystemConfig systemConfig, ConfigurationConsumerUtility processingConfig) {
    this.systemConfig = systemConfig;
    this.processingConfig = processingConfig;
  }

  public Mono<Void> run() {
    var toleranceResolver = ConfigurationToleranceResolver.create(processingConfig);
    var aceiRepository = new AceiMergeRepository(CoiEntityManagerFactory.create(systemConfig), toleranceResolver);

    ReactiveFunction<Collection<ReceiverRecord<String, AcquiredChannelEnvironmentIssue<?>>>, Set<AcquiredChannelEnvironmentIssue<?>>> preprocessor =
      ReactorKafkaUtilities.<AcquiredChannelEnvironmentIssue<?>>getValues()
        .andThen(AceiMergeConsumer::filterBooleans);

    var storageConsumer = new ReactiveKafkaStorageConsumer<>(
      preprocessor,
      aceiRepository::store,
      ReactorKafkaUtilities.acknowledgeAll()
    );

    var batchRecordFlux = ReactorKafkaUtilities.createBatchRecordFlux(systemConfig, new AceiDeserializer());

    return storageConsumer.store(batchRecordFlux);
  }

  public static AceiMergeConsumer create(ControlContext context) {
    checkNotNull(context, "Cannot create AceiMergeConsumer from null context");
    return new AceiMergeConsumer(context.getSystemConfig(), context.getProcessingConfigurationConsumerUtility());
  }

  private static Mono<Set<AcquiredChannelEnvironmentIssue<?>>> filterBooleans(
    Collection<AcquiredChannelEnvironmentIssue<?>> values) {
    return Flux.fromIterable(values)
      .filter(AcquiredChannelEnvironmentIssueBoolean.class::isInstance)
      .collect(Collectors.toSet());
  }

  public static void main(String[] args) {
    logger.info("Initializing AceiMergeConsumer...");
    try {
      AceiMergeConsumer aceiMergeService = ControlFactory.createControl(AceiMergeConsumer.class);
      aceiMergeService.run()
        .retryWhen(ReactorKafkaUtilities.retryForever(logger))
        .block();
    } catch (Exception e) {
      logger.error("AceiMergeConsumer encountered an unrecoverable exception", e);
      System.exit(1);
    }
  }

}
