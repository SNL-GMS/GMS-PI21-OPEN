package gms.shared.frameworks.osd.rsdf.consumer;

import gms.shared.frameworks.common.annotations.Component;
import gms.shared.frameworks.messaging.ReactiveKafkaStorageConsumer;
import gms.shared.frameworks.messaging.ReactorKafkaUtilities;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiDeserializer;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import gms.shared.frameworks.osd.repository.rawstationdataframe.RawStationDataFrameRepositoryJpa;
import gms.shared.frameworks.osd.repository.utils.CoiEntityManagerFactory;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component("osd-rsdf-kafka-consumer")
public class RsdfStorageConsumer {

  private static final Logger logger = LoggerFactory.getLogger(RsdfStorageConsumer.class);

  public static void main(String[] args) {
    var systemConfig = SystemConfig.create("osd-rsdf-kafka-consumer");
    var rsdfRepository = new RawStationDataFrameRepositoryJpa(CoiEntityManagerFactory.create(systemConfig));

    var storageConsumer = new ReactiveKafkaStorageConsumer<>(
      ReactorKafkaUtilities.getValues(),
      rsdfRepository::storeRawStationDataFrames,
      ReactorKafkaUtilities.acknowledgeAll()
    );

    var batchRecordFlux = ReactorKafkaUtilities.createBatchRecordFlux(systemConfig,
      new CoiDeserializer<>(RawStationDataFrame.class));

    storageConsumer.store(batchRecordFlux)
      .retryWhen(ReactorKafkaUtilities.retryForever(logger))
      .block();
  }

}


