package gms.dataacquisition.cd11.rsdf.processor;

import gms.core.dataacquisition.receiver.DataFrameReceiverConfiguration;
import gms.shared.frameworks.common.annotations.Component;
import gms.shared.frameworks.control.ControlContext;
import gms.shared.frameworks.control.ControlFactory;
import gms.shared.frameworks.osd.coi.waveforms.AcquisitionProtocol;
import gms.shared.utilities.kafka.KafkaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Service class responsible for mapping component information to allow configuration and control
 * frameworks to setup appropriately
 */
@Component("cd11-rsdf-processor")
public class Cd11RsdfProcessorService {
  private static final Logger logger = LoggerFactory.getLogger(Cd11RsdfProcessorService.class);

  private final Cd11RsdfProcessor cd11RsdfProcessor;

  public Cd11RsdfProcessorService(Cd11RsdfProcessor processor) {
    this.cd11RsdfProcessor = processor;
  }

  public static Cd11RsdfProcessorService create(ControlContext context) {
    checkNotNull(context, "Cannot create Cd11RsdfProcessorService from null context");

    var systemConfig = context.getSystemConfig();
    var processingConfig = context.getProcessingConfigurationRepository();
    var kafkaConfig = KafkaConfiguration.create(systemConfig);
    var frameReceiverConfig = DataFrameReceiverConfiguration.create(AcquisitionProtocol.CD11, processingConfig,
      systemConfig);

    return new Cd11RsdfProcessorService(Cd11RsdfProcessor.create(kafkaConfig, frameReceiverConfig));
  }

  public Cd11RsdfProcessor getCd11RsdfProcessor() {
    return cd11RsdfProcessor;
  }

  public static void main(String[] args) {
    try {
      var processor = ControlFactory.createControl(Cd11RsdfProcessorService.class).getCd11RsdfProcessor();
      processor.process()
        .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofMillis(500))
          .doBeforeRetry(rs -> logger.warn("RSDF Processing encountered an exception. Retrying...", rs.failure())))
        .onErrorResume(Cd11RsdfProcessorService::shutdown)
        .subscribe();
    } catch (Exception e) {
      shutdown(e).block();
    }
  }

  private static Mono<Void> shutdown(Throwable cause) {
    return Mono.fromRunnable(() -> {
      logger.error("Fatal Error Encountered in Cd11RsdfProcessorService. Shutting Down...", cause);
      System.exit(1);
    });
  }
}
