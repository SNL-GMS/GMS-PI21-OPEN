package gms.core.performancemonitoring.soh.control.capabilityrollup;

import gms.shared.frameworks.osd.coi.soh.SohStatus;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Operations on lists of SohStatuses.
 */
public enum RollupOperatorType {

  BEST_OF {
    /**
     * BEST_OF: Returns the best SohStatus in the list of operands. Parameters are not used.
     *
     * @param parameters unused.
     */
    @Override
    public Function<List<SohStatus>, SohStatus> getOperation(int... parameters) {
      return operands -> operands.parallelStream()
        .max(Comparator.naturalOrder())
        .orElseThrow(() -> new IllegalArgumentException(
          "Tried to perform BEST_OF on an empty list of SohStatuses."
        ));
    }
  },

  WORST_OF {
    /**
     * WORST_OF: Returns the worst SohStatus in the list of operands. Parameters are not used.
     *
     * @param parameters unused.
     */
    @Override
    public Function<List<SohStatus>, SohStatus> getOperation(int... parameters) {
      return operands -> operands.parallelStream()
        .min(Comparator.naturalOrder())
        .orElseThrow(() -> new IllegalArgumentException(
          "Tried to perform WORST_OF on an empty list of SohStatuses."
        ));
    }
  },

  MIN_GOOD_OF {
    /**
     * MIN_GOOD_OF: Returns an Soh status that is reflective of the number of GOOD statuses in the
     * provided list.
     *
     * @param parameters
     *     First parameter: good threshold - how many GOOD statuses are required to
     * return GOOD.
     *     Second parameter: marginal threshold: how many GOOD statuses are required
     * to return marginal.
     */
    @Override
    public Function<List<SohStatus>, SohStatus> getOperation(int... parameters) {
      return operands -> {

        Stream<SohStatus> goodStatusStream = operands.parallelStream()
          .filter(sohStatus -> sohStatus == SohStatus.GOOD);

        long goodCount = goodStatusStream.count();

        if (goodCount >= parameters[0]) {
          return SohStatus.GOOD;
        } else if (goodCount >= parameters[1]) {
          return SohStatus.MARGINAL;
        } else {
          return SohStatus.BAD;
        }
      };
    }
  };

  /**
   * Returns a closure that operates on a list of SohStatuses. The operation can depend on a
   * list of parameters.
   *
   * @param parameters Parameters that are used to tweak the calculation.
   * @return a new SohStatus object that is the result of the calculation.
   */
  public abstract Function<List<SohStatus>, SohStatus> getOperation(int... parameters);

}

