package gms.core.performancemonitoring.ssam.control.dataprovider;

import gms.shared.frameworks.systemconfig.SystemConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.test.StepVerifier;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull; 
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerTest {

  @Mock
  KafkaReceiver<String, Integer> mockReciever;

  @Mock
  ReceiverRecord<String, Integer> mockReceiverRecord1;

  @Mock
  ReceiverRecord<String, Integer> mockReceiverRecord2;

  @Mock
  ReceiverOffset mockReceiverOffset;

  @ParameterizedTest
  @MethodSource("getCreateArguments")
  void testCreateValidation(Class<? extends Exception> expectedException,
    String topicName,
    SystemConfig systemConfig) {
    assertThrows(expectedException, () -> KafkaConsumer
      .getBuilder(systemConfig)
      .withTopic(topicName)
      .build(String.class)
    );
  }

  static Stream<Arguments> getCreateArguments() {

    var mockSystemConfig = Mockito.mock(SystemConfig.class);
    Mockito.when(mockSystemConfig.getValue(anyString())).thenReturn("MOCKED-VALUE");

    return Stream.of(
      arguments(NullPointerException.class, null, mockSystemConfig),
      arguments(NullPointerException.class, "testTopic", null)
    );
  }

  @Test
  void testCreate() {
    var mockSystemConfig = Mockito.mock(SystemConfig.class);
    Mockito.when(mockSystemConfig.getValue(anyString())).thenReturn("MOCKED-VALUE");

    assertNotNull(KafkaConsumer.getBuilder(mockSystemConfig)
      .withTopic("TestTopic")
      .build(String.class)
    );
  }

  @Test
  void testReceive() {

    Mockito.when(mockReciever.receive()).thenReturn(
      Flux.just(mockReceiverRecord1, mockReceiverRecord2)
    );

    Mockito.when(mockReceiverRecord1.receiverOffset())
      .thenReturn(mockReceiverOffset);
    Mockito.when(mockReceiverRecord1.value())
      .thenReturn(1);

    Mockito.when(mockReceiverRecord2.receiverOffset())
      .thenReturn(mockReceiverOffset);
    Mockito.when(mockReceiverRecord2.value())
      .thenReturn(2);

    var kafkaConsumer = new KafkaConsumer<Integer>(
      mockReciever
    );

    StepVerifier.create(kafkaConsumer.receive())
      .verifyComplete();

    Mockito.verify(mockReceiverOffset, times(2)).acknowledge();
    Mockito.verify(mockReceiverRecord1, times(1)).value();
    Mockito.verify(mockReceiverRecord2, times(1)).value();
  }
}
