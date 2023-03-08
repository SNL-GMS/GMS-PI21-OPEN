package gms.shared.reactor;

import org.apache.kafka.common.TopicPartition;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.kafka.receiver.ReceiverRecord;

/**
 * Extension of {@link FluxSupplier} to incorporate grouping by kafka partitions and extracting
 * of values from {@link ReceiverRecord}s
 *
 * @param <V>
 */
@FunctionalInterface
public interface ReactorKafkaFluxSupplier<V> extends FluxSupplier<ReceiverRecord<String, V>> {

  default Flux<V> getValueFlux() {
    return getFlux().map(ReceiverRecord::value);
  }

  default Flux<GroupedFlux<TopicPartition, ReceiverRecord<String, V>>> getPartitionFlux() {
    return toGroupedFlux(receiverRecord -> receiverRecord.receiverOffset().topicPartition());
  }

  default Flux<GroupedFlux<TopicPartition, V>> getPartitionValueFlux() {
    return toGroupedFlux(receiverRecord -> receiverRecord.receiverOffset().topicPartition(), ReceiverRecord::value);
  }
}
