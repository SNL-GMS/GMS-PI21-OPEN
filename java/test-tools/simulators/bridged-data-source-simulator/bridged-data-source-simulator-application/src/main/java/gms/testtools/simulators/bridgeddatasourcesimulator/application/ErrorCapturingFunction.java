package gms.testtools.simulators.bridgeddatasourcesimulator.application;

import gms.testtools.simulators.bridgeddatasourcesimulator.api.BridgedDataSourceDataSimulator;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.coi.ExceptionSummary;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A function that wraps a consumer, handles the errors from that function, and reports the error and source
 */
public class ErrorCapturingFunction implements Function<BridgedDataSourceDataSimulator, DataSimulatorConsumerResult> {

  private final Consumer<BridgedDataSourceDataSimulator> dataSimulatorConsumer;

  private ErrorCapturingFunction(Consumer<BridgedDataSourceDataSimulator> dataSimulatorConsumer) {
    this.dataSimulatorConsumer = dataSimulatorConsumer;
  }

  public static ErrorCapturingFunction create(Consumer<BridgedDataSourceDataSimulator> dataSimulatorFunction) {
    Objects.requireNonNull(dataSimulatorFunction, "dataSimulatorFunction cannot be null");

    return new ErrorCapturingFunction(dataSimulatorFunction);
  }

  @Override
  public DataSimulatorConsumerResult apply(BridgedDataSourceDataSimulator simulator) {
    try {
      dataSimulatorConsumer.accept(simulator);
    } catch (Exception ex) {
      var exceptionSummary = ExceptionSummary.create(Instant.now(),
        "dataSimulatorConsumer", ex.getMessage());
      return DataSimulatorConsumerResult.create(simulator.getClass().getSimpleName(), Optional.of(exceptionSummary));
    }

    return DataSimulatorConsumerResult.create(simulator.getClass().getSimpleName(), Optional.empty());
  }
}
