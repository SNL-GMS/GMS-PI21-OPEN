package gms.shared.frameworks.messaging;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.function.Function;

class ReactiveFunctionTest {

  @Test
  void testWrap() {
    Function<String, String> toUppercase = String::toUpperCase;
    StepVerifier.create(ReactiveFunction.wrap(toUppercase).apply("test"))
      .expectNext("TEST")
      .verifyComplete();
  }

  @Test
  void testCompose() {
    Function<String, String> toUppercase = String::toUpperCase;
    Function<String, String> concatEnd = s -> s.concat("end");

    ReactiveFunction<String, String> inputFunction = ReactiveFunction.wrap(concatEnd).compose(
      ReactiveFunction.wrap(toUppercase));

    StepVerifier.create(inputFunction.apply("start"))
      .expectNext("STARTend")
      .verifyComplete();
  }

  @Test
  void andThen() {
    Function<String, String> toUppercase = String::toUpperCase;
    Function<String, String> concatEnd = s -> s.concat("end");

    ReactiveFunction<String, String> inputFunction = ReactiveFunction.wrap(concatEnd).andThen(
      ReactiveFunction.wrap(toUppercase));

    StepVerifier.create(inputFunction.apply("start"))
      .expectNext("STARTEND")
      .verifyComplete();
  }

  @Test
  void identity() {
    StepVerifier.create(ReactiveFunction.identity().apply("test"))
      .expectNext("test")
      .verifyComplete();
  }
}