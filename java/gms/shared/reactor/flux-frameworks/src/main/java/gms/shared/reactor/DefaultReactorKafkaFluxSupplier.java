package gms.shared.reactor;

import com.google.common.base.Preconditions;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

/**
 * Default implementation of {@link ReactorKafkaFluxSupplier}, leveraging a {@link KafkaReceiver} to
 * provide the record flux. Assumes a String record key
 *
 * @param <V> Value type within the {@link ReceiverRecord}
 */
public class DefaultReactorKafkaFluxSupplier<V> implements ReactorKafkaFluxSupplier<V> {

  private final KafkaReceiver<String, V> kafkaReceiver;

  private DefaultReactorKafkaFluxSupplier(
    KafkaReceiver<String, V> kafkaReceiver) {
    this.kafkaReceiver = kafkaReceiver;
  }

  public static <V> DefaultReactorKafkaFluxSupplier<V> create(KafkaReceiver<String, V> kafkaReceiver) {
    return new DefaultReactorKafkaFluxSupplier<>(Preconditions.checkNotNull(kafkaReceiver));
  }

  @Override
  public Flux<ReceiverRecord<String, V>> getFlux() {
    return kafkaReceiver.receive();
  }
}
