package gms.core.dataacquisition.reactor;

import gms.shared.reactor.DefaultReactorKafkaFluxSupplier;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.test.StepVerifier;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DefaultReactorKafkaFluxSupplierTests {

  @Mock
  private KafkaReceiver<String, String> mockReceiver;

  @Test
  void testCreate() {
    assertDoesNotThrow(() -> DefaultReactorKafkaFluxSupplier.create(mockReceiver));
  }

  @Test
  void testGetFluxSinglePartition() {
    TopicPartition inputTopicPartition = new TopicPartition("test", 0);
    ReceiverOffset receiverOffset = mockReceiverOffset(inputTopicPartition);

    final var consumerRecords = Stream.of("test1", "test2")
      .map(val -> {
        ReceiverRecord<String, String> record = mock(ReceiverRecord.class);
        given(record.receiverOffset()).willReturn(receiverOffset);
        given(record.value()).willReturn(val);
        return record;
      }).collect(Collectors.toList());

    given(mockReceiver.receive()).willReturn(Flux.fromIterable(consumerRecords));

    DefaultReactorKafkaFluxSupplier<String> fluxProvider = DefaultReactorKafkaFluxSupplier
      .create(mockReceiver);

    StepVerifier.create(fluxProvider.getValueFlux())
      .expectNext("test1", "test2")
      .verifyComplete();

    StepVerifier.create(fluxProvider.getPartitionValueFlux().map(GroupedFlux::key))
      .expectNext(inputTopicPartition)
      .verifyComplete();

    StepVerifier.create(fluxProvider.getPartitionValueFlux().flatMap(identity()))
      .expectNext("test1", "test2")
      .verifyComplete();
  }

  @Test
  void testGetFluxMultiplePartitions() {
    TopicPartition topicPartition1 = new TopicPartition("test", 0);
    Pair<ReceiverOffset, String> partitionedVal1 = Pair
      .of(mockReceiverOffset(topicPartition1), "test1");
    TopicPartition topicPartition2 = new TopicPartition("test", 1);
    Pair<ReceiverOffset, String> partitionedVal2 = Pair
      .of(mockReceiverOffset(topicPartition2), "test2");

    final var consumerRecords = Stream.of(partitionedVal1, partitionedVal2)
      .map(partitionPair -> {
        ReceiverRecord<String, String> record = mock(ReceiverRecord.class);
        given(record.receiverOffset()).willReturn(partitionPair.getKey());
        given(record.value()).willReturn(partitionPair.getValue());
        return record;
      }).collect(Collectors.toList());

    given(mockReceiver.receive()).willReturn(Flux.fromIterable(consumerRecords));

    DefaultReactorKafkaFluxSupplier<String> fluxProvider = DefaultReactorKafkaFluxSupplier
      .create(mockReceiver);

    StepVerifier.create(fluxProvider.getValueFlux())
      .expectNext("test1", "test2")
      .verifyComplete();

    StepVerifier.create(fluxProvider.getPartitionFlux().map(GroupedFlux::key).sort(
        Comparator.comparing(TopicPartition::partition)))
      .expectNext(topicPartition1, topicPartition2)
      .verifyComplete();

    StepVerifier.create(fluxProvider.getPartitionValueFlux().map(GroupedFlux::key).sort(
        Comparator.comparing(TopicPartition::partition)))
      .expectNext(topicPartition1, topicPartition2)
      .verifyComplete();

    StepVerifier.create(
        fluxProvider.getPartitionFlux().flatMap(identity()).map(ReceiverRecord::value).sort())
      .expectNext("test1", "test2")
      .verifyComplete();

    StepVerifier.create(fluxProvider.getPartitionValueFlux().flatMap(identity()).sort())
      .expectNext("test1", "test2")
      .verifyComplete();
  }

  private static ReceiverOffset mockReceiverOffset(TopicPartition inputTopicPartition) {
    ReceiverOffset receiverOffset = mock(ReceiverOffset.class);
    given(receiverOffset.topicPartition()).willReturn(inputTopicPartition);
    return receiverOffset;
  }

}