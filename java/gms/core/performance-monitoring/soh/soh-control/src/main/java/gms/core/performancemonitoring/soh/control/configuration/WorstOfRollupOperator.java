package gms.core.performancemonitoring.soh.control.configuration;

import com.google.auto.value.AutoValue;
import gms.core.performancemonitoring.soh.control.capabilityrollup.RollupOperatorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;

import java.util.List;

/**
 * Represents the "worst of" rollup operator, which returns the "worst" status of its operands.
 */
@AutoValue
public abstract class WorstOfRollupOperator implements RollupOperator {

  WorstOfRollupOperator() {

  }

  public static WorstOfRollupOperator from(
    List<String> stationOperands,
    List<String> channelOperands,
    List<SohMonitorType> sohMonitorTypeOperands,
    List<RollupOperator> rollupOperatorOperands
  ) {

    RollupOperator.validate(
      stationOperands,
      channelOperands,
      sohMonitorTypeOperands,
      rollupOperatorOperands
    );

    return new AutoValue_WorstOfRollupOperator(
      RollupOperatorType.WORST_OF,
      stationOperands,
      channelOperands,
      sohMonitorTypeOperands,
      rollupOperatorOperands
    );
  }
}
