package gms.core.performancemonitoring.soh.control;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.stream.Stream;

class AggregatorTests {

  @ParameterizedTest
  @MethodSource("cannedAggregatorsTestSource")
  <T> void testCannedAggregators(
    Aggregator<T> cannedAggregator,
    T expectedAggregateValue
  ) {

    var actualAggregateValue = cannedAggregator.aggregate().get();

    Assertions.assertEquals(
      expectedAggregateValue,
      actualAggregateValue
    );
  }

  private static Stream<Arguments> cannedAggregatorsTestSource() {

    var avgAggregator1 = Aggregator.getDurationAverager();
    avgAggregator1.accept(Duration.ofMillis(10));
    avgAggregator1.accept(Duration.ofMillis(20));

    var avgAggregator2 = Aggregator.getDurationAverager();
    avgAggregator2.accept(Duration.ofMillis(30));
    avgAggregator2.combine(avgAggregator1);


    var maxAggregator1 = Aggregator.getDurationMaximizer();
    maxAggregator1.accept(Duration.ofMillis(10));
    maxAggregator1.accept(Duration.ofMillis(20));

    var maxAggregator2 = Aggregator.getDurationMaximizer();
    maxAggregator2.accept(Duration.ofMillis(30));
    maxAggregator2.combine(maxAggregator1);

    return Stream.of(
      Arguments.arguments(
        avgAggregator1,
        Duration.ofMillis(15)
      ),

      Arguments.arguments(
        avgAggregator2,
        Duration.ofMillis(20)
      ),

      Arguments.arguments(
        maxAggregator1,
        Duration.ofMillis(20)
      ),

      Arguments.arguments(
        maxAggregator2,
        Duration.ofMillis(30)
      )
    );
  }
}
