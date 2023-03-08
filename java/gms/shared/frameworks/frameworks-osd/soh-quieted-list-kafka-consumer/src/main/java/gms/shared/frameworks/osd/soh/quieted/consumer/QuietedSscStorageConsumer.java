package gms.shared.frameworks.osd.soh.quieted.consumer;

import gms.shared.frameworks.common.annotations.Component;
import gms.shared.frameworks.messaging.ReactiveKafkaStorageConsumer;
import gms.shared.frameworks.messaging.ReactorKafkaUtilities;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiDeserializer;
import gms.shared.frameworks.osd.coi.soh.quieting.QuietedSohStatusChange;
import gms.shared.frameworks.osd.repository.performancemonitoring.SohStatusChangeRepositoryJpa;
import gms.shared.frameworks.osd.repository.utils.CoiEntityManagerFactory;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component("soh-quieted-list-kafka-consumer")
public class QuietedSscStorageConsumer {

  private static final Logger logger = LoggerFactory.getLogger(QuietedSscStorageConsumer.class);

  public static void main(String[] args) {
    var systemConfig = SystemConfig.create("soh-quieted-list-kafka-consumer");
    var repository = new SohStatusChangeRepositoryJpa(CoiEntityManagerFactory.create(systemConfig));
    var storageConsumer = new ReactiveKafkaStorageConsumer<>(
      ReactorKafkaUtilities.getValues(),
      repository::storeQuietedSohStatusChangeList,
      ReactorKafkaUtilities.acknowledgeAll());
    var batchRecordFlux = ReactorKafkaUtilities.createBatchRecordFlux(systemConfig, new CoiDeserializer<>(
      QuietedSohStatusChange.class));

    storageConsumer.store(batchRecordFlux)
      .retryWhen(ReactorKafkaUtilities.retryForever(logger))
      .block();
  }
}
