package gms.shared.frameworks.osd.soh.statuschange.consumer;

import gms.shared.frameworks.common.annotations.Component;
import gms.shared.frameworks.messaging.ReactiveKafkaStorageConsumer;
import gms.shared.frameworks.messaging.ReactorKafkaUtilities;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiDeserializer;
import gms.shared.frameworks.osd.coi.soh.quieting.UnacknowledgedSohStatusChange;
import gms.shared.frameworks.osd.repository.performancemonitoring.SohStatusChangeRepositoryJpa;
import gms.shared.frameworks.osd.repository.utils.CoiEntityManagerFactory;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component("soh-status-change-kafka-consumer")
public class UnacknowledgedSscStorageConsumer {

  private static final Logger logger = LoggerFactory.getLogger(UnacknowledgedSscStorageConsumer.class);

  public static void main(String[] args) {
    var systemConfig = SystemConfig.create("soh-status-change-kafka-consumer");
    var repository = new SohStatusChangeRepositoryJpa(CoiEntityManagerFactory.create(systemConfig));
    var storageConsumer = new ReactiveKafkaStorageConsumer<>(
      ReactorKafkaUtilities.getValues(),
      repository::storeUnacknowledgedSohStatusChange,
      ReactorKafkaUtilities.acknowledgeAll());

    var batchRecordFlux = ReactorKafkaUtilities.createBatchRecordFlux(systemConfig, new CoiDeserializer<>(
      UnacknowledgedSohStatusChange.class));

    storageConsumer.store(batchRecordFlux)
      .retryWhen(ReactorKafkaUtilities.retryForever(logger))
      .block();
  }

}
