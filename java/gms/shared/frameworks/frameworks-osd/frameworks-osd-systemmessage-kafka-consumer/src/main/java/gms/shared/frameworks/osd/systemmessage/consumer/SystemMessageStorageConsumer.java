package gms.shared.frameworks.osd.systemmessage.consumer;

import gms.shared.frameworks.common.annotations.Component;
import gms.shared.frameworks.messaging.ReactiveKafkaStorageConsumer;
import gms.shared.frameworks.messaging.ReactorKafkaUtilities;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiDeserializer;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.repository.systemmessage.SystemMessageRepositoryJpa;
import gms.shared.frameworks.osd.repository.utils.CoiEntityManagerFactory;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component("osd-systemmessage-kafka-consumer")
public class SystemMessageStorageConsumer {

  private static final Logger logger = LoggerFactory.getLogger(SystemMessageStorageConsumer.class);

  public static void main(String[] args) {
    var systemConfig = SystemConfig.create("osd-systemmessage-kafka-consumer");
    var repository = new SystemMessageRepositoryJpa(CoiEntityManagerFactory.create(systemConfig));
    var storageConsumer = new ReactiveKafkaStorageConsumer<>(
      ReactorKafkaUtilities.getValues(),
      repository::storeSystemMessages,
      ReactorKafkaUtilities.acknowledgeAll());
    var batchRecordFlux = ReactorKafkaUtilities.createBatchRecordFlux(systemConfig, new CoiDeserializer<>(
      SystemMessage.class));

    storageConsumer.store(batchRecordFlux)
      .retryWhen(ReactorKafkaUtilities.retryForever(logger))
      .block();
  }
}
