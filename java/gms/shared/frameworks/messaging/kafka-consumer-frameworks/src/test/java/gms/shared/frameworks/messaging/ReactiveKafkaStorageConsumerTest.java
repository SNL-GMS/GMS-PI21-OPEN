package gms.shared.frameworks.messaging;

import gms.shared.frameworks.coi.exceptions.StorageUnavailableException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.test.StepVerifier;

import java.util.Collection;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ReactiveKafkaStorageConsumerTest {

  @Mock
  ReactiveStorageRepository<Collection<String>> mockRepository;

  private ReactiveKafkaStorageConsumer<String, Collection<String>> storageConsumer;

  @Test
  void testStoreAndAcknowledge() {
    var firstRecord = record("first");
    var secondRecord = record("second");
    var thirdRecord = record("third");
    var fourthRecord = record("fourth");

    given(mockRepository.store(any(), any())).willCallRealMethod();
    given(mockRepository.store(any())).willCallRealMethod();

    storageConsumer = new ReactiveKafkaStorageConsumer<>(
      ReactorKafkaUtilities.getValues(),
      mockRepository,
      ReactorKafkaUtilities.acknowledgeAll()
    );

    Flux<List<ReceiverRecord<String, String>>> inputFlux = Flux.just(List.of(firstRecord, secondRecord),
      List.of(thirdRecord, fourthRecord));

    StepVerifier.create(storageConsumer.store(inputFlux))
      .verifyComplete();

    verify(mockRepository).storeInternal(List.of("first", "second"));
    verify(mockRepository).storeInternal(List.of("third", "fourth"));
    verifyNoMoreInteractions(mockRepository);

    verify(firstRecord.receiverOffset()).acknowledge();
    verify(secondRecord.receiverOffset()).acknowledge();
    verify(thirdRecord.receiverOffset()).acknowledge();
    verify(fourthRecord.receiverOffset()).acknowledge();
  }

  @Test
  void testStore() {
    var firstRecord = record("first");
    var secondRecord = record("second");

    given(mockRepository.store(any(), any())).willCallRealMethod();
    given(mockRepository.store(any())).willCallRealMethod();

    storageConsumer = new ReactiveKafkaStorageConsumer<>(
      ReactorKafkaUtilities.getValues(),
      mockRepository,
      ReactiveConsumer.doNothing()
    );

    Flux<List<ReceiverRecord<String, String>>> inputFlux = Flux.just(List.of(firstRecord, secondRecord));

    StepVerifier.create(storageConsumer.store(inputFlux))
      .verifyComplete();

    verify(mockRepository).storeInternal(List.of("first", "second"));
    verifyNoMoreInteractions(mockRepository);

    verify(firstRecord.receiverOffset(), never()).acknowledge();
    verify(secondRecord.receiverOffset(), never()).acknowledge();
  }

  @Test
  void testStoreRetriesOnRecoverableFailure() {
    var firstRecord = record("first");
    var secondRecord = record("second");
    var storeValue = List.of("first", "second");

    given(mockRepository.store(any(), any())).willCallRealMethod();
    given(mockRepository.store(any())).willCallRealMethod();

    willThrow(StorageUnavailableException.class)
      .willDoNothing()
      .given(mockRepository).storeInternal(storeValue);

    storageConsumer = new ReactiveKafkaStorageConsumer<>(
      ReactorKafkaUtilities.getValues(),
      mockRepository,
      ReactorKafkaUtilities.acknowledgeAll()
    );

    Flux<List<ReceiverRecord<String, String>>> inputFlux = Flux.just(List.of(firstRecord, secondRecord));

    StepVerifier.create(storageConsumer.store(inputFlux))
      .verifyComplete();

    verify(mockRepository, times(2)).storeInternal(storeValue);
    verifyNoMoreInteractions(mockRepository);

    verify(firstRecord.receiverOffset()).acknowledge();
    verify(secondRecord.receiverOffset()).acknowledge();
  }


  @Test
  void testStoreFailsOnIrrecoverableFailure() {
    var firstRecord = record("first");
    var secondRecord = record("second");
    var storeValue = List.of("first", "second");

    given(mockRepository.store(any(), any())).willCallRealMethod();
    given(mockRepository.store(any())).willCallRealMethod();

    willThrow(IllegalStateException.class)
      .given(mockRepository).storeInternal(storeValue);

    storageConsumer = new ReactiveKafkaStorageConsumer<>(
      ReactorKafkaUtilities.getValues(),
      mockRepository,
      ReactorKafkaUtilities.acknowledgeAll()
    );

    StepVerifier.create(storageConsumer.store(Flux.just(List.of(firstRecord, secondRecord))))
      .verifyError(IllegalStateException.class);

    verify(mockRepository).storeInternal(storeValue);
    verify(firstRecord.receiverOffset(), never()).acknowledge();
    verify(secondRecord.receiverOffset(), never()).acknowledge();
  }

  @Test
  void testEmptyPreprocessDoesNotStoreStillAcknowledges() {
    var firstRecord = record("first");
    var secondRecord = record("second");
    var storeValue = List.of("first", "second");

    storageConsumer = new ReactiveKafkaStorageConsumer<>(
      values -> Mono.empty(),
      mockRepository,
      ReactorKafkaUtilities.acknowledgeAll()
    );

    StepVerifier.create(storageConsumer.store(Flux.just(List.of(firstRecord, secondRecord))))
      .verifyComplete();

    verify(mockRepository, never()).storeInternal(storeValue);
    verify(firstRecord.receiverOffset()).acknowledge();
    verify(secondRecord.receiverOffset()).acknowledge();
  }

  static ReceiverRecord<String, String> record(String value) {
    return new ReceiverRecord<>(new ConsumerRecord<>("test", 0, 0, "test", value), Mockito.mock(ReceiverOffset.class));
  }

}