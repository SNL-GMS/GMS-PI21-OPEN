package gms.core.performancemonitoring.soh.control.configuration;

import gms.core.performancemonitoring.soh.control.capabilityrollup.RollupOperatorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Interface for a "rollup operator", an operator that takes in the Soh of a set of things
 * (stations, channels, channel monitor types, or results of nested operators) and performs an
 * operation on them to come up with a new Soh status. Only one of these lists can be non-empty.
 */
public interface RollupOperator {

  /**
   * Type of the operator.
   *
   * @return the rollup operator type
   */
  RollupOperatorType getOperatorType();

  /**
   * List of stations that have a rolled-up status that can be operated on.
   *
   * @return the station operands
   */
  List<String> getStationOperands();

  /**
   * List of channels that have a rolled-up status that can be operated on.
   *
   * @return the channel operands
   */
  List<String> getChannelOperands();

  /**
   * List of monitor types that are mapped to a status that can be operated on.
   *
   * @return the {@link SohMonitorType}s that can be operated on
   */
  List<SohMonitorType> getSohMonitorTypeOperands();

  /**
   * List of RollupOperators that calculate soh status that can be operated on by this higher-level
   * rollup operator.
   *
   * @return the {@link RollupOperator}s used by this RollupOperator
   */
  List<RollupOperator> getRollupOperatorOperands();

  /**
   * Get the low-level operation, which takes in a list of statuses and returns a new single
   * status.
   *
   * @return the low-level operation
   */
  default Function<List<SohStatus>, SohStatus> operation() {
    return getOperatorType().getOperation();
  }

  /**
   * Validation utility method that can be used by implementing classes.
   *
   * @param stationOperands the stationOperands to validate
   * @param channelOperands the channelOperands to validate
   * @param sohMonitorTypeOperands the sohMonitorTypeOperands to validate
   * @param rollupOperatorOperands the rollupOperatorOperands to validate
   */
  static void validate(
    List<String> stationOperands,
    List<String> channelOperands,
    List<SohMonitorType> sohMonitorTypeOperands,
    List<RollupOperator> rollupOperatorOperands
  ) {

    Validate.isTrue(IntStream.of(
          stationOperands.size(),
          channelOperands.size(),
          sohMonitorTypeOperands.size(),
          rollupOperatorOperands.size())
        .filter(i -> i > 0)
        .count() <= 1,

      "Only one parameter list can be nonempty. Sizes are: \n" +
        "   stationOperands: %d\n" +
        "   channelOperands: %d\n" +
        "   sohMonitorTypeOperands: %d\n" +
        "   rollupOperatorOperands: %d\n",
      stationOperands.size(),
      channelOperands.size(),
      sohMonitorTypeOperands.size(),
      rollupOperatorOperands.size()
    );
  }

}
