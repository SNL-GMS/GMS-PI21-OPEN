package gms.core.performancemonitoring.soh.control.capabilityrollup;

import gms.core.performancemonitoring.soh.control.configuration.BestOfRollupOperator;
import gms.core.performancemonitoring.soh.control.configuration.RollupOperator;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Extra tests for RollupEvaluator. The idea is to ensure that when evaluating an "expression tree",
 * the structure of the tree is respected/preserved throughout the entire evaluation.
 */
class RollupEvaluatorExtraTests {

  static int rank(SohStatus s) {
    return 2 - s.ordinal();
  }

  static SohStatus fromRank(int rank) {

    return Arrays.stream(SohStatus.values()).filter(
      sohStatus -> rank(sohStatus) == rank
    ).findFirst().get();
  }

  /**
   * Define a `+` operation. This has no use outside of testing. This operation is associative and
   * commutative.
   */
  static class AdditionRollupOperator implements RollupOperator {

    private List<String> stationOperands;

    private List<RollupOperator> rollupOperators;

    public AdditionRollupOperator(List<String> stationOperands,
      List<RollupOperator> rollupOperators) {
      this.stationOperands = stationOperands;
      this.rollupOperators = rollupOperators;
    }

    @Override
    public RollupOperatorType getOperatorType() {
      return null;
    }

    @Override
    public List<String> getStationOperands() {
      return stationOperands;
    }

    @Override
    public List<String> getChannelOperands() {
      return null;
    }

    @Override
    public List<SohMonitorType> getSohMonitorTypeOperands() {
      return null;
    }

    @Override
    public List<RollupOperator> getRollupOperatorOperands() {
      return rollupOperators;
    }

    @Override
    public Function<List<SohStatus>, SohStatus> operation() {
      return sohStatuses -> fromRank(
        sohStatuses.stream()
          .map(RollupEvaluatorExtraTests::rank)
          .reduce(0, Integer::sum) % 3
      );
    }
  }

  /**
   * Define a `*` operation. This has no use outside of testing. This operation is associative and
   * commutative, and distributes acrosss `+`.
   */
  static class MultiplicationRollupOperator implements RollupOperator {

    private List<String> stationOperands;

    private List<RollupOperator> rollupOperators;

    public MultiplicationRollupOperator(List<String> stationOperands,
      List<RollupOperator> rollupOperators) {
      this.stationOperands = stationOperands;
      this.rollupOperators = rollupOperators;
    }

    @Override
    public RollupOperatorType getOperatorType() {
      return null;
    }

    @Override
    public List<String> getStationOperands() {
      return stationOperands;
    }

    @Override
    public List<String> getChannelOperands() {
      return null;
    }

    @Override
    public List<SohMonitorType> getSohMonitorTypeOperands() {
      return null;
    }

    @Override
    public List<RollupOperator> getRollupOperatorOperands() {
      return rollupOperators;
    }

    @Override
    public Function<List<SohStatus>, SohStatus> operation() {
      return sohStatuses -> fromRank(
        sohStatuses.stream()
          .map(RollupEvaluatorExtraTests::rank)
          .reduce(1, (a, b) -> a * b) % 3
      );
    }
  }

  /**
   * Test that if we have operators '+' and '*', where both are associative and commutative, and
   * where '*' distributes across '+', then those properties are preserved during evaluation.
   * <p>
   * This validates that the tree structure is respected in the the algorithm in the following
   * ways:
   * <p>
   * - The commutative property is the equivalent of saying that for a given node, any two of its
   * children can swap positions and the final evaluation will remain the same.
   * <p>
   * - The associative property is equivalent to stating that for a given node, its children can be
   * partitioned across two ore more nodes, of the same type, that are made children of the given
   * node.
   * <p>
   * - The distributive property is the equivalent of saying that if a given node has a distributive
   * operation, that distributes across the operations of any ove its children, it can "migrate" and
   * "split" across the children of that node.
   */
  @ParameterizedTest
  @MethodSource("distributiveCommutativityAndAssociativityTestSource")
  void testDistributiveCommutativityAndAssociativity(
    RollupOperator operation,
    SohStatus exptectedStatus
  ) {

    var actualStatus = RollupEvaluator.create(
      operation,
      RollupOperator::getStationOperands,
      station -> station.startsWith("GOOD") ? SohStatus.GOOD
        : station.startsWith("MARG") ? SohStatus.MARGINAL : SohStatus.BAD).evaluate();

    Assertions.assertSame(
      exptectedStatus, actualStatus
    );

  }

  private static Stream<Arguments> distributiveCommutativityAndAssociativityTestSource() {

    return Stream.of(
      //
      // Group 1:
      // BAD
      // = (GOOD + MARGINAL) * [(MARGINAL + GOOD) + (BAD + BAD)]                       (1)
      // = [(GOOD + MARGINAL) * (MARGINAL + GOOD)] + [(GOOD + MARGINAL) * (BAD + BAD)] (2)
      // = (GOOD + MARGINAL) * (MARGINAL + GOOD + BAD + BAD)                           (3)
      // = (GOOD + MARGINAL) * (MARGINAL + GOOD + (BAD * BAD))                         (4)
      // = (MARGINAL + GOOD + BAD + BAD) * (GOOD + MARGINAL)                           (5)
      //
      // 1:
      Arguments.arguments(
        new MultiplicationRollupOperator(
          List.of(),
          List.of(
            new AdditionRollupOperator(
              List.of("GOOD-STATION", "MARG-STATION"),
              List.of()
            ),
            new AdditionRollupOperator(
              List.of(),
              List.of(
                new AdditionRollupOperator(
                  List.of("MARG-STATION", "GOOD-STATION"),
                  List.of()
                ),
                new AdditionRollupOperator(
                  List.of("BAD-STATION", "BAD-STATION"),
                  List.of()
                )
              )
            )
          )
        ),
        SohStatus.BAD
      ),
      //
      // 2:
      Arguments.arguments(
        new AdditionRollupOperator(
          List.of(),
          List.of(
            new MultiplicationRollupOperator(
              List.of(),
              List.of(
                new AdditionRollupOperator(
                  List.of("GOOD-STATION", "MARG-STATION"),
                  List.of()
                ),
                new AdditionRollupOperator(
                  List.of("MARG-STATION", "GOOD-STATION"),
                  List.of()
                )
              )
            ),
            new MultiplicationRollupOperator(
              List.of(),
              List.of(
                new AdditionRollupOperator(
                  List.of("GOOD-STATION", "MARG-STATION"),
                  List.of()
                ),
                new AdditionRollupOperator(
                  List.of("BAD-STATION", "BAD-STATION"),
                  List.of()
                )
              )
            )
          )
        ),
        SohStatus.BAD
      ),
      //
      // 3:
      Arguments.arguments(
        new MultiplicationRollupOperator(
          List.of(),
          List.of(
            new AdditionRollupOperator(
              List.of("GOOD-STATION", "MARG-STATION"),
              List.of()
            ),
            new AdditionRollupOperator(
              List.of("MARG-STATION", "GOOD-STATION", "BAD-STATION", "BAD-STATION"),
              List.of()
            )
          )
        ),
        SohStatus.BAD
      ),

      //
      // 4:
      Arguments.arguments(
        new MultiplicationRollupOperator(
          List.of(),
          List.of(
            new AdditionRollupOperator(
              List.of("GOOD-STATION", "MARG-STATION"),
              List.of()
            ),
            new AdditionRollupOperator(
              List.of(),
              List.of(
                new AdditionRollupOperator(
                  List.of("MARG-STATION", "GOOD-STATION"),
                  List.of()
                ),
                new MultiplicationRollupOperator(
                  List.of("BAD-STATION", "BAD-STATION"),
                  List.of()
                )
              )
            )
          )
        ),
        SohStatus.BAD
      ),
      //
      // 5:
      Arguments.arguments(
        new MultiplicationRollupOperator(
          List.of(),
          List.of(
            new AdditionRollupOperator(
              List.of("MARG-STATION", "GOOD-STATION", "BAD-STATION", "BAD-STATION"),
              List.of()
            ),
            new AdditionRollupOperator(
              List.of("GOOD-STATION", "MARG-STATION"),
              List.of()
            )
          )
        ),
        SohStatus.BAD
      ),
      //
      // Group 2:
      // GOOD
      // = [[[(MARGINAL * GOOD * BAD * BAD)+[(MARGINAL* GOOD * BAD * BAD)+(MARGINAL * GOOD)]]*(GOOD * MARGINAL)] + (GOOD * MARGINAL) ] * (GOOD + MARGINAL)  (1)
      // = (GOOD + MARGINAL) * [[[(MARGINAL * GOOD * BAD * BAD)+[(MARGINAL* GOOD * BAD * BAD)+(MARGINAL * GOOD)]]*(GOOD * MARGINAL)] + (GOOD * MARGINAL) ]  (2)
      //
      // 1:
      Arguments.arguments(
        new MultiplicationRollupOperator(
          List.of(),
          List.of(
            new AdditionRollupOperator(
              List.of(),
              List.of(
                new MultiplicationRollupOperator(
                  List.of(),
                  List.of(
                    new AdditionRollupOperator(
                      List.of(),
                      List.of(
                        new MultiplicationRollupOperator(
                          List.of("MARG-STATION", "GOOD-STATION",
                            "BAD-STATION", "BAD-STATION"),
                          List.of()
                        ),
                        new AdditionRollupOperator(
                          List.of(),
                          List.of(
                            new MultiplicationRollupOperator(
                              List.of("MARG-STATION", "GOOD-STATION",
                                "BAD-STATION", "BAD-STATION"),
                              List.of()),
                            new MultiplicationRollupOperator(
                              List.of("MARG-STATION", "GOOD-STATION"),
                              List.of())
                          )
                        )
                      )
                    ),
                    new MultiplicationRollupOperator(
                      List.of("MARG-STATION", "GOOD-STATION"),
                      List.of()
                    )
                  )
                ),
                new MultiplicationRollupOperator(
                  List.of("MARG-STATION", "GOOD-STATION"),
                  List.of()
                )
              )
            ),
            new AdditionRollupOperator(
              List.of("GOOD-STATION", "MARG-STATION"),
              List.of()
            )
          )
        ),
        SohStatus.GOOD
      ),
      //
      // 2:
      Arguments.arguments(
        new MultiplicationRollupOperator(
          List.of(),
          List.of(
            new AdditionRollupOperator(
              List.of("GOOD-STATION", "MARG-STATION"),
              List.of()
            ),
            new AdditionRollupOperator(
              List.of(),
              List.of(
                new MultiplicationRollupOperator(
                  List.of(),
                  List.of(
                    new AdditionRollupOperator(
                      List.of(),
                      List.of(
                        new MultiplicationRollupOperator(
                          List.of("MARG-STATION", "GOOD-STATION",
                            "BAD-STATION", "BAD-STATION"),
                          List.of()
                        ),
                        new AdditionRollupOperator(
                          List.of(),
                          List.of(
                            new MultiplicationRollupOperator(
                              List.of("MARG-STATION", "GOOD-STATION",
                                "BAD-STATION", "BAD-STATION"),
                              List.of()),
                            new MultiplicationRollupOperator(
                              List.of("MARG-STATION", "GOOD-STATION"),
                              List.of())
                          )
                        )
                      )
                    ),
                    new MultiplicationRollupOperator(
                      List.of("MARG-STATION", "GOOD-STATION"),
                      List.of()
                    )
                  )
                ),
                new MultiplicationRollupOperator(
                  List.of("MARG-STATION", "GOOD-STATION"),
                  List.of()
                )
              )
            )

          )
        ),
        SohStatus.GOOD
      ),

      //
      // "Antidistributive" tests. Make sure that BEST_OF retains its property that it does NOT
      // distribute over our new Addition operator.
      //
      // Let ^ represent BEST_OF, then:
      //
      // MARGINAL = (GOOD + MARGINAL) ^ [(MARGINAL + GOOD) + (BAD + BAD)]                  (1)
      //
      //  ~ BUT ~
      //
      // BAD = [(GOOD + MARGINAL) ^ (MARGINAL + GOOD)] + [(GOOD + MARGINAL) ^ (BAD + BAD)] (2)
      //
      //
      // 1:
      Arguments.arguments(
        BestOfRollupOperator.from(
          List.of(),
          List.of(),
          List.of(),
          List.of(
            new AdditionRollupOperator(
              List.of("GOOD-STATION", "MARG-STATION"),
              List.of()
            ),
            new AdditionRollupOperator(
              List.of(),
              List.of(
                new AdditionRollupOperator(
                  List.of("MARG-STATION", "GOOD-STATION"),
                  List.of()
                ),
                new AdditionRollupOperator(
                  List.of("BAD-STATION", "BAD-STATION"),
                  List.of()
                )
              )
            )
          )
        ),
        SohStatus.MARGINAL
      ),

      //
      // 2:
      Arguments.arguments(
        new AdditionRollupOperator(
          List.of(),
          List.of(
            BestOfRollupOperator.from(
              List.of(),
              List.of(),
              List.of(),
              List.of(
                new AdditionRollupOperator(
                  List.of("GOOD-STATION", "MARG-STATION"),
                  List.of()
                ),
                new AdditionRollupOperator(
                  List.of("MARG-STATION", "GOOD-STATION"),
                  List.of()
                )
              )
            ),
            BestOfRollupOperator.from(
              List.of(),
              List.of(),
              List.of(),
              List.of(
                new AdditionRollupOperator(
                  List.of("GOOD-STATION", "MARG-STATION"),
                  List.of()
                ),
                new AdditionRollupOperator(
                  List.of("BAD-STATION", "BAD-STATION"),
                  List.of()
                )
              )
            )
          )
        ),
        SohStatus.BAD
      )

    );
  }
}
