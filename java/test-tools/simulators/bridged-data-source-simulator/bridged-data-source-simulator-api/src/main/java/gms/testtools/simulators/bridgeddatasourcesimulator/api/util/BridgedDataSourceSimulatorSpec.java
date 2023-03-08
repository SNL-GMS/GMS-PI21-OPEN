package gms.testtools.simulators.bridgeddatasourcesimulator.api.util;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import org.apache.commons.lang3.Validate;

import java.time.Duration;
import java.time.Instant;

@AutoValue
@JsonSerialize(as = AutoValue_BridgedDataSourceSimulatorSpec.class)
@JsonDeserialize(builder = AutoValue_BridgedDataSourceSimulatorSpec.Builder.class)
public abstract class BridgedDataSourceSimulatorSpec {

  public static BridgedDataSourceSimulatorSpec.Builder builder() {
    return new AutoValue_BridgedDataSourceSimulatorSpec.Builder();
  }

  public abstract BridgedDataSourceSimulatorSpec.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {
    BridgedDataSourceSimulatorSpec.Builder setSeedDataStartTime(Instant seedDataStartTime);

    BridgedDataSourceSimulatorSpec.Builder setSeedDataEndTime(Instant seedDataEndTime);

    BridgedDataSourceSimulatorSpec.Builder setSimulationStartTime(Instant simulationStartTime);

    BridgedDataSourceSimulatorSpec.Builder setOperationalTimePeriod(Duration operationalTimePeriod);

    BridgedDataSourceSimulatorSpec.Builder setCalibUpdateFrequency(Duration calibUpdateFrequency);

    BridgedDataSourceSimulatorSpec autoBuild();

    default BridgedDataSourceSimulatorSpec build() {
      var bridgedDataSourceSimulatorSpec = autoBuild();
      //first make sure everything is correctly populated...
      Validate.isTrue(bridgedDataSourceSimulatorSpec.getSeedDataStartTime().isBefore(Instant.now()),
        "Seed Data Start Time has to be in the past.");
      Validate.isTrue(bridgedDataSourceSimulatorSpec.getSeedDataEndTime().isBefore(Instant.now()),
        "Seed Data End Time has to be in the past.");
      Validate.isTrue(bridgedDataSourceSimulatorSpec.getSeedDataStartTime().isBefore(bridgedDataSourceSimulatorSpec.getSeedDataEndTime()),
        "Start Time has to be before End Time.");
      Validate.isTrue((!bridgedDataSourceSimulatorSpec.getOperationalTimePeriod().isZero() &&
          !bridgedDataSourceSimulatorSpec.getOperationalTimePeriod().isNegative()),
        "An Operational Time Period has to be set (in hours) as greater than 0.");
      Validate.isTrue((!bridgedDataSourceSimulatorSpec.getCalibUpdateFrequency().isZero() &&
          !bridgedDataSourceSimulatorSpec.getCalibUpdateFrequency().isNegative()),
        "A Calibration Update Frequency has to be set (in hours) as greater to 0.");
      return bridgedDataSourceSimulatorSpec;
    }
  }

  public abstract Instant getSeedDataStartTime();

  public abstract Instant getSeedDataEndTime();

  public abstract Instant getSimulationStartTime();

  public abstract Duration getOperationalTimePeriod();

  public abstract Duration getCalibUpdateFrequency();

}
