package gms.shared.frameworks.osd.station.soh.consumer;

import gms.shared.frameworks.common.annotations.Component;
import gms.shared.frameworks.messaging.ReactiveKafkaStorageConsumer;
import gms.shared.frameworks.messaging.ReactorKafkaUtilities;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiDeserializer;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import gms.shared.frameworks.osd.repository.performancemonitoring.PerformanceMonitoringRepositoryJpa;
import gms.shared.frameworks.osd.repository.utils.CoiEntityManagerFactory;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component("osd-station-soh-kafka-consumer")
public class StationSohStorageConsumer {

  private static final Logger logger = LoggerFactory.getLogger(StationSohStorageConsumer.class);

  public static void main(String[] args) {
    var systemConfig = SystemConfig.create("osd-station-soh-kafka-consumer");
    var repository = new PerformanceMonitoringRepositoryJpa(CoiEntityManagerFactory.create("gms_station_soh_consumer", systemConfig));
    var storageConsumer = new ReactiveKafkaStorageConsumer<>(
      ReactorKafkaUtilities.getValues(),
      repository::storeStationSoh,
      ReactorKafkaUtilities.acknowledgeAll());

    var batchRecordFlux = ReactorKafkaUtilities.createBatchRecordFlux(systemConfig, new CoiDeserializer<>(
      StationSoh.class));

    storageConsumer.store(batchRecordFlux)
      .retryWhen(ReactorKafkaUtilities.retryForever(logger))
      .block();
  }

}