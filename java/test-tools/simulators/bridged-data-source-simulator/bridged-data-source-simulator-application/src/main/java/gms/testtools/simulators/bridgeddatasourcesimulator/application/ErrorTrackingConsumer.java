package gms.testtools.simulators.bridgeddatasourcesimulator.application;

import gms.testtools.simulators.bridgeddatasourcesimulator.api.coi.ExceptionSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class ErrorTrackingConsumer implements Consumer<DataSimulatorConsumerResult> {

  private final Map<String, List<ExceptionSummary>> simulatorExceptions;

  private ErrorTrackingConsumer(Map<String, List<ExceptionSummary>> simulatorExceptions) {
    this.simulatorExceptions = simulatorExceptions;
  }

  public static ErrorTrackingConsumer create(Map<String, List<ExceptionSummary>> simulatorExceptions) {
    Objects.requireNonNull(simulatorExceptions, "simulatorExceptions cannot be null");

    return new ErrorTrackingConsumer(simulatorExceptions);
  }

  @Override
  public void accept(DataSimulatorConsumerResult result) {
    result.getExceptionSummary().ifPresent(exceptionSummary -> {
      simulatorExceptions.computeIfAbsent(result.getSimulatorName(), simName -> new ArrayList<>());
      simulatorExceptions.get(result.getSimulatorName()).add(exceptionSummary);
    });
  }
}
