package gms.shared.frameworks.osd.capability.soh.rollup.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gms.shared.frameworks.common.annotations.Component;
import gms.shared.frameworks.messaging.ReactiveKafkaStorageConsumer;
import gms.shared.frameworks.messaging.ReactorKafkaUtilities;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiDeserializer;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.repository.performancemonitoring.CapabilitySohRollupRepositoryJpa;
import gms.shared.frameworks.osd.repository.utils.CoiEntityManagerFactory;
import gms.shared.frameworks.systemconfig.SystemConfig;

@Component("capability-soh-rollup-kafka-consumer")
public class CsrStorageConsumer {

  private static final Logger logger = LoggerFactory.getLogger(CsrStorageConsumer.class);

  public static void main(String[] args) {
    var systemConfig = SystemConfig.create("capability-soh-rollup-kafka-consumer");
    var repository = new CapabilitySohRollupRepositoryJpa(CoiEntityManagerFactory.create("gms_csr_consumer", systemConfig));
    var storageConsumer = new ReactiveKafkaStorageConsumer<>(
      ReactorKafkaUtilities.getValues(),
      repository::storeCapabilitySohRollup,
      ReactorKafkaUtilities.acknowledgeAll());
    var batchRecordFlux = ReactorKafkaUtilities.createBatchRecordFlux(systemConfig, new CoiDeserializer<>(
      CapabilitySohRollup.class));

    storageConsumer.store(batchRecordFlux)
      .retryWhen(ReactorKafkaUtilities.retryForever(logger))
      .block();
  }
}
