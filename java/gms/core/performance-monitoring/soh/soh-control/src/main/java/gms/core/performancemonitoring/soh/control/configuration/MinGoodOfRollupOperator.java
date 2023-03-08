package gms.core.performancemonitoring.soh.control.configuration;

import com.google.auto.value.AutoValue;
import gms.core.performancemonitoring.soh.control.capabilityrollup.RollupOperatorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohStatus;

import java.util.List;
import java.util.function.Function;

/**
 * Represents the "min good of" rollup operator, which returns a status that reflects the number
 * of GOOD status in its operands. see RollupOperatorType.MIN_GOOD_OF.
 */
@AutoValue
public abstract class MinGoodOfRollupOperator implements RollupOperator {

  MinGoodOfRollupOperator() {

  }

  /**
   * The number of GOOD statuses that need to be present before returning a GOOD status.
   */
  public abstract int getGoodThreshold();

  /**
   * The number of GOOD statuses that need to be present before returnining a MARGINAL status.
   */
  public abstract int getMarginalThreshold();

  public static MinGoodOfRollupOperator from(
    List<String> stationOperands,
    List<String> channelOperands,
    List<SohMonitorType> sohMonitorTypeOperands,
    List<RollupOperator> rollupOperatorOperands,
    int goodThreshold,
    int marginalThreshold) {

    RollupOperator.validate(
      stationOperands,
      channelOperands,
      sohMonitorTypeOperands,
      rollupOperatorOperands
    );

    return new AutoValue_MinGoodOfRollupOperator(
      RollupOperatorType.MIN_GOOD_OF,
      stationOperands,
      channelOperands,
      sohMonitorTypeOperands,
      rollupOperatorOperands,
      goodThreshold,
      marginalThreshold
    );

  }

  @Override
  public Function<List<SohStatus>, SohStatus> operation() {
    return this.getOperatorType().getOperation(
      getGoodThreshold(),
      getMarginalThreshold()
    );
  }
}
