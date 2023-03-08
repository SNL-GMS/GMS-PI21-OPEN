package gms.testtools.simulators.bridgeddatasourcesimulator.application;

import com.google.auto.value.AutoValue;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.coi.ExceptionSummary;

import java.util.Optional;

@AutoValue
public abstract class DataSimulatorConsumerResult {

  public abstract String getSimulatorName();

  public abstract Optional<ExceptionSummary> getExceptionSummary();

  public static DataSimulatorConsumerResult create(String simulatorName,
    Optional<ExceptionSummary> exception) {
    return new AutoValue_DataSimulatorConsumerResult(simulatorName, exception);
  }

}
