package gms.shared.frameworks.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.function.Consumer;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReactiveConsumerTest {

  @Mock
  Consumer<String> mockConsumer;

  @Test
  void testWrap() {
    StepVerifier.create(ReactiveConsumer.wrap(mockConsumer).accept("test"))
      .verifyComplete();

    verify(mockConsumer).accept("test");
  }

  @Test
  void testAndThen() {
    var consumeTwice = ReactiveConsumer.wrap(mockConsumer).andThen(ReactiveConsumer.wrap(mockConsumer));

    StepVerifier.create(consumeTwice.accept("test"))
      .verifyComplete();

    verify(mockConsumer, times(2)).accept("test");
  }

  @Test
  void testDoNothing() {
    StepVerifier.create(ReactiveConsumer.doNothing().accept("test"))
      .verifyComplete();
  }
}