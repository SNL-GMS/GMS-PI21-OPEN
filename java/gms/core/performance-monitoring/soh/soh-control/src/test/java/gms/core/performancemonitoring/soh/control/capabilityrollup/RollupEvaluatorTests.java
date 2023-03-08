package gms.core.performancemonitoring.soh.control.capabilityrollup;

import gms.core.performancemonitoring.soh.control.configuration.BestOfRollupOperator;
import gms.core.performancemonitoring.soh.control.configuration.MinGoodOfRollupOperator;
import gms.core.performancemonitoring.soh.control.configuration.RollupOperator;
import gms.core.performancemonitoring.soh.control.configuration.WorstOfRollupOperator;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

class RollupEvaluatorTests {

  @ParameterizedTest
  @MethodSource("evaluatorTestSource")
  <V> void testEvaluator(
    RollupOperator rollupOperator,
    Function<RollupOperator, List<V>> operandSelector,
    Function<V, SohStatus> objectMapper,
    SohStatus expectedStatus
  ) {

    var evaluator = RollupEvaluator.create(
      rollupOperator,
      operandSelector,
      objectMapper
    );

    Assertions.assertNotNull(evaluator);

    Assertions.assertSame(
      expectedStatus,
      evaluator.evaluate()
    );
  }

  private static Stream<Arguments> evaluatorTestSource() {

    return Stream.of(
      Arguments.arguments(
        BestOfRollupOperator.from(
          List.of("A", "B"),
          List.of(),
          List.of(),
          List.of()
        ),

        (Function<RollupOperator, List<String>>) RollupOperator::getStationOperands,

        (Function<String, SohStatus>) v -> "A".equals(v) ? SohStatus.BAD : SohStatus.GOOD,

        SohStatus.GOOD
      ),

      Arguments.arguments(
        BestOfRollupOperator.from(

          List.of(),
          List.of("A", "B"),
          List.of(),
          List.of()
        ),

        (Function<RollupOperator, List<String>>) RollupOperator::getChannelOperands,

        (Function<String, SohStatus>) v -> "A".equals(v) ? SohStatus.BAD : SohStatus.GOOD,

        SohStatus.GOOD
      ),

      Arguments.arguments(
        BestOfRollupOperator.from(

          List.of(),
          List.of(),
          List.of(SohMonitorType.MISSING, SohMonitorType.LAG),
          List.of()
        ),

        (Function<RollupOperator, List<SohMonitorType>>) RollupOperator::getSohMonitorTypeOperands,

        (Function<SohMonitorType, SohStatus>) v -> v == SohMonitorType.MISSING ? SohStatus.BAD
          : SohStatus.GOOD,

        SohStatus.GOOD
      ),

      Arguments.arguments(
        WorstOfRollupOperator.from(
          List.of(),
          List.of(),
          List.of(),
          List.of(
            WorstOfRollupOperator.from(
              List.of(),
              List.of(),
              List.of(SohMonitorType.MISSING, SohMonitorType.LAG),
              List.of()
            ),

            BestOfRollupOperator.from(

              List.of(),
              List.of(),
              List.of(SohMonitorType.MISSING, SohMonitorType.LAG),
              List.of()
            )
          )
        ),

        (Function<RollupOperator, List<SohMonitorType>>) RollupOperator::getSohMonitorTypeOperands,

        (Function<SohMonitorType, SohStatus>) v -> v == SohMonitorType.MISSING ? SohStatus.BAD
          : SohStatus.GOOD,

        SohStatus.BAD
      ),

      Arguments.arguments(
        WorstOfRollupOperator.from(
          List.of(),
          List.of(),
          List.of(),
          List.of(
            WorstOfRollupOperator.from(
              List.of(),
              List.of(),
              List.of(SohMonitorType.MISSING, SohMonitorType.LAG),
              List.of()
            ),

            BestOfRollupOperator.from(

              List.of(),
              List.of(),
              List.of(SohMonitorType.MISSING, SohMonitorType.LAG,
                SohMonitorType.ENV_DURATION_OUTAGE),
              List.of()
            )
          )
        ),

        (Function<RollupOperator, List<SohMonitorType>>) RollupOperator::getSohMonitorTypeOperands,

        (Function<SohMonitorType, SohStatus>) v -> v == SohMonitorType.MISSING ? SohStatus.BAD
          : SohStatus.GOOD,

        SohStatus.BAD
      ),

      Arguments.arguments(
        MinGoodOfRollupOperator.from(
          List.of(),
          List.of(),
          List.of(),
          List.of(
            MinGoodOfRollupOperator.from(
              List.of(),
              List.of(),
              List.of(
                SohMonitorType.ENV_DURATION_OUTAGE, //map to GOOD
                SohMonitorType.ENV_MEAN_AMPLITUDE   //mao to BAD
              ),
              List.of(),
              1, 0
            ),
            MinGoodOfRollupOperator.from(
              List.of(),
              List.of(),
              List.of(
                SohMonitorType.ENV_DURATION_OUTAGE, //map to GOOD
                SohMonitorType.ENV_MEAN_AMPLITUDE   //mao to BAD
              ),
              List.of(),
              2, 0
            ),
            MinGoodOfRollupOperator.from(
              List.of(),
              List.of(),
              List.of(
                SohMonitorType.ENV_DURATION_OUTAGE, //map to GOOD
                SohMonitorType.ENV_BACKUP_POWER_UNSTABLE   //mao to GOOD
              ),
              List.of(),
              2, 0
            )
          ),
          2, 0
        ),

        (Function<RollupOperator, List<SohMonitorType>>) RollupOperator::getSohMonitorTypeOperands,

        (Function<SohMonitorType, SohStatus>) v -> v == SohMonitorType.ENV_MEAN_AMPLITUDE
          ? SohStatus.BAD : SohStatus.GOOD,

        SohStatus.GOOD
      ),
      Arguments.arguments(
        BestOfRollupOperator.from( //1
          List.of(),
          List.of(),
          List.of(),
          List.of(
            BestOfRollupOperator.from( //3
              List.of(),
              List.of(),
              List.of(),
              List.of(
                WorstOfRollupOperator.from( //6
                  List.of("GOOD-STATION1", "GOOD-STATION2"),
                  List.of(),
                  List.of(),
                  List.of()
                ),
                WorstOfRollupOperator.from( //7
                  List.of("GOOD-STATION1", "GOOD-STATION2"),
                  List.of(),
                  List.of(),
                  List.of()
                )
              )
            ),
            WorstOfRollupOperator.from( //2
              List.of(),
              List.of(),
              List.of(),
              List.of(
                WorstOfRollupOperator.from( //6
                  List.of("BAD-STATION1", "BAD-STATION2"),
                  List.of(),
                  List.of(),
                  List.of()
                ),
                WorstOfRollupOperator.from( //7
                  List.of("BAD-STATION1", "BAD-STATION2"),
                  List.of(),
                  List.of(),
                  List.of()
                )
              )
            )
          )
        ),

        (Function<RollupOperator, List<String>>) RollupOperator::getStationOperands,

        (Function<String, SohStatus>) v -> v.startsWith("BAD-") ? SohStatus.BAD
          : SohStatus.GOOD,

        SohStatus.GOOD
      )
      ,
      Arguments.arguments(
        BestOfRollupOperator.from( //1
          List.of(),
          List.of(),
          List.of(),
          List.of(
            WorstOfRollupOperator.from( //2
              List.of(),
              List.of(),
              List.of(),
              List.of(
                WorstOfRollupOperator.from( //6
                  List.of("BAD-STATION1", "BAD-STATION2"),
                  List.of(),
                  List.of(),
                  List.of()
                ),
                WorstOfRollupOperator.from( //7
                  List.of("BAD-STATION1", "BAD-STATION2"),
                  List.of(),
                  List.of(),
                  List.of()
                )
              )
            )
            ,
            BestOfRollupOperator.from( //3
              List.of(),
              List.of(),
              List.of(),
              List.of(
                WorstOfRollupOperator.from( //6
                  List.of("GOOD-STATION1", "GOOD-STATION2"),
                  List.of(),
                  List.of(),
                  List.of()
                ),
                WorstOfRollupOperator.from( //7
                  List.of("GOOD-STATION1", "GOOD-STATION2"),
                  List.of(),
                  List.of(),
                  List.of()
                )
              )
            )
          )
        ),

        (Function<RollupOperator, List<String>>) RollupOperator::getStationOperands,

        (Function<String, SohStatus>) v -> v.startsWith("BAD-") ? SohStatus.BAD
          : SohStatus.GOOD,

        SohStatus.GOOD
      )
      ,
      Arguments.arguments(
        BestOfRollupOperator.from( //1
          List.of(),
          List.of(),
          List.of(),
          List.of(
            WorstOfRollupOperator.from( //2
              List.of(),
              List.of(),
              List.of(),
              List.of(
                WorstOfRollupOperator.from( //6
                  List.of("BAD-STATION1", "BAD-STATION2"),
                  List.of(),
                  List.of(),
                  List.of()
                ),
                WorstOfRollupOperator.from( //7
                  List.of("BAD-STATION1", "BAD-STATION2"),
                  List.of(),
                  List.of(),
                  List.of()
                )
              )
            )
            ,
            BestOfRollupOperator.from( //3
              List.of(),
              List.of(),
              List.of(),
              List.of(
                WorstOfRollupOperator.from( //6
                  List.of("GOOD-STATION1", "GOOD-STATION2"),
                  List.of(),
                  List.of(),
                  List.of()
                ),
                WorstOfRollupOperator.from( //7
                  List.of("GOOD-STATION1", "GOOD-STATION2"),
                  List.of(),
                  List.of(),
                  List.of()
                ),
                WorstOfRollupOperator.from( //7
                  List.of("GOOD-STATION1", "GOOD-STATION2"),
                  List.of(),
                  List.of(),
                  List.of()
                ),
                WorstOfRollupOperator.from( //7
                  List.of("GOOD-STATION1", "GOOD-STATION2"),
                  List.of(),
                  List.of(),
                  List.of()
                )
              )
            )
          )
        ),

        (Function<RollupOperator, List<String>>) RollupOperator::getStationOperands,

        (Function<String, SohStatus>) v -> v.startsWith("BAD-") ? SohStatus.BAD
          : SohStatus.GOOD,

        SohStatus.GOOD
      )
      ,
      Arguments.arguments(
        BestOfRollupOperator.from( //1
          List.of(),
          List.of(),
          List.of(),
          List.of(
            BestOfRollupOperator.from( //3
              List.of(),
              List.of(),
              List.of(),
              List.of(
                WorstOfRollupOperator.from( //6
                  List.of("GOOD-STATION1", "GOOD-STATION2"),
                  List.of(),
                  List.of(),
                  List.of()
                ),
                WorstOfRollupOperator.from( //7
                  List.of("GOOD-STATION1", "GOOD-STATION2"),
                  List.of(),
                  List.of(),
                  List.of()
                ),
                WorstOfRollupOperator.from( //7
                  List.of("GOOD-STATION1", "GOOD-STATION2"),
                  List.of(),
                  List.of(),
                  List.of()
                ),
                WorstOfRollupOperator.from( //7
                  List.of("GOOD-STATION1", "GOOD-STATION2"),
                  List.of(),
                  List.of(),
                  List.of()
                )
              )
            )
            ,
            WorstOfRollupOperator.from( //2
              List.of(),
              List.of(),
              List.of(),
              List.of(
                WorstOfRollupOperator.from( //6
                  List.of("BAD-STATION1", "BAD-STATION2"),
                  List.of(),
                  List.of(),
                  List.of()
                ),
                WorstOfRollupOperator.from( //7
                  List.of("BAD-STATION1", "BAD-STATION2"),
                  List.of(),
                  List.of(),
                  List.of()
                )
              )
            )
          )
        ),

        (Function<RollupOperator, List<String>>) RollupOperator::getStationOperands,

        (Function<String, SohStatus>) v -> v.startsWith("BAD-") ? SohStatus.BAD
          : SohStatus.GOOD,

        SohStatus.GOOD
      )
      ,
      Arguments.arguments(
        createNestedOperatorsForSohMonitorType(
          Map.of(
            RollupOperatorType.WORST_OF, //BAD
            Map.of(
              RollupOperatorType.WORST_OF, Map.of(), //BAD
              RollupOperatorType.BEST_OF, Map.of( // GOOD
                // This will return only BAD, so the BEST_OF above is now BAD.
                RollupOperatorType.WORST_OF, Map.of( // BAD
                  RollupOperatorType.WORST_OF, Map.of(), //BAD
                  RollupOperatorType.BEST_OF, Map.of( // GOOD
                    RollupOperatorType.BEST_OF, Map.of(), //GOOD
                    RollupOperatorType.WORST_OF, Map.of( // BAD
                      RollupOperatorType.BEST_OF, Map.of(), // GOOD
                      RollupOperatorType.WORST_OF, Map.of() // BAD
                    )
                  )
                ),
                RollupOperatorType.BEST_OF, Map.of() // GOOD
              )
            )
          ),
          List.of(SohMonitorType.MISSING, SohMonitorType.LAG)
          //SohMonitorType.ENV_DURATION_OUTAGE)
        ).get(0),

        (Function<RollupOperator, List<SohMonitorType>>) RollupOperator::getSohMonitorTypeOperands,

        (Function<SohMonitorType, SohStatus>) v -> v == SohMonitorType.MISSING ? SohStatus.BAD
          : SohStatus.GOOD,

        SohStatus.BAD
      )
    );
  }

  private static List<RollupOperator> createNestedOperatorsForSohMonitorType(
    Map<RollupOperatorType, Object> tree,
    List<SohMonitorType> sohMonitorTypes) {

    List<RollupOperator> nestedOperators = new ArrayList<>();

    tree.forEach((k, v) -> {

      Map<RollupOperatorType, Object> castedV = (Map<RollupOperatorType, Object>) v;

      Map<RollupOperatorType, Function<Boolean, RollupOperator>> rollupOperatorTypeSupplierMap = Map
        .of(
          RollupOperatorType.BEST_OF, isRecursive -> BestOfRollupOperator.from(
            List.of(),
            List.of(),
            isRecursive ? List.of() : sohMonitorTypes,
            isRecursive ? createNestedOperatorsForSohMonitorType(castedV, sohMonitorTypes)
              : List.of()
          ),

          RollupOperatorType.WORST_OF, isRecursive -> WorstOfRollupOperator.from(
            List.of(),
            List.of(),
            isRecursive ? List.of() : sohMonitorTypes,
            isRecursive ? createNestedOperatorsForSohMonitorType(castedV, sohMonitorTypes)
              : List.of()
          )
        );

      nestedOperators.add(
        rollupOperatorTypeSupplierMap.get(k).apply(!castedV.isEmpty())
      );
    });

    return nestedOperators;
  }
}
