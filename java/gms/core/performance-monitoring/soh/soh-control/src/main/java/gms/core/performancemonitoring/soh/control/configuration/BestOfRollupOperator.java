package gms.core.performancemonitoring.soh.control.configuration;

import com.google.auto.value.AutoValue;
import gms.core.performancemonitoring.soh.control.capabilityrollup.RollupOperatorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;

import java.util.List;

/**
 * Represents the "best of" rollup operator, which returns the "best" status of its operands.
 */
@AutoValue
public abstract class BestOfRollupOperator implements RollupOperator {

  BestOfRollupOperator() {

  }

  public static BestOfRollupOperator from(
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

    return new AutoValue_BestOfRollupOperator(
      RollupOperatorType.BEST_OF,
      stationOperands,
      channelOperands,
      sohMonitorTypeOperands,
      rollupOperatorOperands
    );
  }
}
