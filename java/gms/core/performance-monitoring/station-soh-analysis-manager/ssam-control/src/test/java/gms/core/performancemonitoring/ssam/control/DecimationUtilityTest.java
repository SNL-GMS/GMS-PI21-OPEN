package gms.core.performancemonitoring.ssam.control;

import gms.core.performancemonitoring.ssam.control.api.DecimationRequestParams;
import gms.core.performancemonitoring.ssam.control.api.HistoricalSohMonitorValuesAnalysisView;
import gms.core.performancemonitoring.ssam.control.api.HistoricalStationSohAnalysisView;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType.SohValueType;
import gms.shared.frameworks.osd.dto.soh.DurationSohMonitorValues;
import gms.shared.frameworks.osd.dto.soh.HistoricalSohMonitorValues;
import gms.shared.frameworks.osd.dto.soh.HistoricalStationSoh;
import gms.shared.frameworks.osd.dto.soh.PercentSohMonitorValues;
import gms.shared.frameworks.osd.dto.soh.SohMonitorValues;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class DecimationUtilityTest {

  @ParameterizedTest
  @MethodSource("decimationTestSource")
  void testDecimation(
    DecimationRequestParams decimationRequestParams,
    HistoricalStationSoh historicalStationSoh,
    HistoricalStationSohAnalysisView expectedHistoricalStationSohAnalysisView
  ) {

    Function<SohMonitorValues, double[]> valuesExtractor = (SohMonitorValues sohMonitorValues) -> {
      if (sohMonitorValues.getType() == SohValueType.DURATION) {
        return Arrays.stream(((DurationSohMonitorValues) sohMonitorValues).getValues())
          .mapToDouble(value -> value).toArray();
      } else {
        return ((PercentSohMonitorValues) sohMonitorValues).getValues();
      }
    };

    var actualHistoricalStationSohAnalysisView = DecimationUtility.decimateHistoricalStationSoh(
      decimationRequestParams,
      historicalStationSoh
    );

    Assertions.assertEquals(
      expectedHistoricalStationSohAnalysisView.getStationName(),
      actualHistoricalStationSohAnalysisView.getStationName()
    );

    Assertions.assertEquals(
      expectedHistoricalStationSohAnalysisView.getPercentageSent(),
      actualHistoricalStationSohAnalysisView.getPercentageSent()
    );

    Assertions.assertArrayEquals(
      expectedHistoricalStationSohAnalysisView.getCalculationTimes(),
      actualHistoricalStationSohAnalysisView.getCalculationTimes()
    );

    Assertions.assertEquals(
      expectedHistoricalStationSohAnalysisView.getMonitorValues().size(),
      actualHistoricalStationSohAnalysisView.getMonitorValues().size()
    );

    var expectedMonitorValuesList = expectedHistoricalStationSohAnalysisView.getMonitorValues();
    var actualMonitorValuesList = actualHistoricalStationSohAnalysisView.getMonitorValues();

    IntStream.range(0, expectedMonitorValuesList.size())
      .forEach(index -> {

        var expectedMonitorValues = expectedMonitorValuesList.get(index);
        var actualMonitorValues = actualMonitorValuesList.get(index);

        Assertions.assertEquals(
          expectedMonitorValues.getChannelName(),
          actualMonitorValues.getChannelName()
        );

        Assertions.assertEquals(
          expectedMonitorValues.getAverage(),
          actualMonitorValues.getAverage()
        );

        Assertions.assertEquals(
          expectedMonitorValues.getValues().size(),
          actualMonitorValues.getValues().size()
        );

        Assertions.assertSame(
          expectedMonitorValues.getValues().getType(),
          actualMonitorValues.getValues().getType()
        );

        Assertions.assertArrayEquals(
          valuesExtractor.apply(expectedMonitorValues.getValues()),
          valuesExtractor.apply(actualMonitorValues.getValues())
        );
      });
  }

  private static Stream<Arguments> decimationTestSource() {

    return Stream.of(
      Arguments.arguments(
        DecimationRequestParams.create(
          Instant.EPOCH,
          Instant.EPOCH.plusMillis(900),
          2,
          "StationA",
          SohMonitorType.MISSING
        ),
        HistoricalStationSoh.create(
          "StationA",
          new long[]{0, 100, 200, 300, 400, 500, 600, 700, 800, 900},
          List.of(
            HistoricalSohMonitorValues.create(
              "Channel1",
              Map.of(
                SohMonitorType.MISSING,
                PercentSohMonitorValues.create(
                  new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}
                )
              )
            ),
            HistoricalSohMonitorValues.create(
              "Channel2",
              Map.of(
                SohMonitorType.MISSING,
                PercentSohMonitorValues.create(
                  new double[]{11, 21, 31, 41, 51, 61, 71, 81, 91, 101}
                )
              )
            )
          )
        ),
        HistoricalStationSohAnalysisView.create(
          "StationA",
          new long[]{0, 500},
          List.of(
            HistoricalSohMonitorValuesAnalysisView.create(
              "Channel1",
              PercentSohMonitorValues.create(
                new double[]{1, 6}
              ),
              5.5
            ),
            HistoricalSohMonitorValuesAnalysisView.create(
              "Channel2",
              PercentSohMonitorValues.create(
                new double[]{11, 61}
              ),
              Arrays.stream(new double[]{11, 21, 31, 41, 51, 61, 71, 81, 91, 101})
                .average().getAsDouble()
            )
          ),
          20.0

        )
      ),

      Arguments.arguments(
        DecimationRequestParams.create(
          Instant.EPOCH,
          Instant.EPOCH.plusMillis(900),
          2,
          "StationA",
          SohMonitorType.LAG
        ),
        HistoricalStationSoh.create(
          "StationA",
          new long[]{0, 100, 200, 300, 400, 500, 600, 700, 800, 900},
          List.of(
            HistoricalSohMonitorValues.create(
              "Channel1",
              Map.of(
                SohMonitorType.LAG,
                DurationSohMonitorValues.create(
                  new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}
                )
              )
            ),
            HistoricalSohMonitorValues.create(
              "Channel2",
              Map.of(
                SohMonitorType.LAG,
                DurationSohMonitorValues.create(
                  new long[]{11, 21, 31, 41, 51, 61, 71, 81, 91, 101}
                )
              )
            )
          )
        ),
        HistoricalStationSohAnalysisView.create(
          "StationA",
          new long[]{0, 500},
          List.of(
            HistoricalSohMonitorValuesAnalysisView.create(
              "Channel1",
              DurationSohMonitorValues.create(
                new long[]{1, 6}
              ),
              5.5
            ),
            HistoricalSohMonitorValuesAnalysisView.create(
              "Channel2",
              DurationSohMonitorValues.create(
                new long[]{11, 61}
              ),
              Arrays.stream(new double[]{11, 21, 31, 41, 51, 61, 71, 81, 91, 101})
                .average().getAsDouble()
            )
          ),
          20.0

        )
      ),

      Arguments.arguments(
        DecimationRequestParams.create(
          Instant.EPOCH,
          Instant.EPOCH.plusMillis(900),
          2,
          "StationA",
          SohMonitorType.MISSING
        ),
        HistoricalStationSoh.create(
          "StationA",
          new long[]{0, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000},
          List.of(
            HistoricalSohMonitorValues.create(
              "Channel1",
              Map.of(
                SohMonitorType.MISSING,
                PercentSohMonitorValues.create(
                  new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}
                )
              )
            ),
            HistoricalSohMonitorValues.create(
              "Channel2",
              Map.of(
                SohMonitorType.MISSING,
                PercentSohMonitorValues.create(
                  new double[]{11, 21, 31, 41, 51, 61, 71, 81, 91, 101, 111}
                )
              )
            )
          )
        ),
        HistoricalStationSohAnalysisView.create(
          "StationA",
          new long[]{0, 600},
          List.of(
            HistoricalSohMonitorValuesAnalysisView.create(
              "Channel1",
              PercentSohMonitorValues.create(
                new double[]{1, 7}
              ),
              6.0
            ),
            HistoricalSohMonitorValuesAnalysisView.create(
              "Channel2",
              PercentSohMonitorValues.create(
                new double[]{11, 71}
              ),
              Arrays.stream(new double[]{11, 21, 31, 41, 51, 61, 71, 81, 91, 101, 111})
                .average().getAsDouble()
            )
          ),
          100 * 2.0 / 11.0
        )
      ),

      Arguments.arguments(
        DecimationRequestParams.create(
          Instant.EPOCH,
          Instant.EPOCH.plusMillis(900),
          10,
          "StationA",
          SohMonitorType.MISSING
        ),
        HistoricalStationSoh.create(
          "StationA",
          new long[]{0, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000},
          List.of(
            HistoricalSohMonitorValues.create(
              "Channel1",
              Map.of(
                SohMonitorType.MISSING,
                PercentSohMonitorValues.create(
                  new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}
                )
              )
            ),
            HistoricalSohMonitorValues.create(
              "Channel2",
              Map.of(
                SohMonitorType.MISSING,
                PercentSohMonitorValues.create(
                  new double[]{11, 21, 31, 41, 51, 61, 71, 81, 91, 101, 111}
                )
              )
            )
          )
        ),
        HistoricalStationSohAnalysisView.create(
          "StationA",
          new long[]{0, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000},
          List.of(
            HistoricalSohMonitorValuesAnalysisView.create(
              "Channel1",
              PercentSohMonitorValues.create(
                new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}
              ),
              6.0
            ),
            HistoricalSohMonitorValuesAnalysisView.create(
              "Channel2",
              PercentSohMonitorValues.create(
                new double[]{11, 21, 31, 41, 51, 61, 71, 81, 91, 101, 111}
              ),
              Arrays.stream(new double[]{11, 21, 31, 41, 51, 61, 71, 81, 91, 101, 111})
                .average().getAsDouble()
            )
          ),
          100
        )
      ),

      Arguments.arguments(
        DecimationRequestParams.create(
          Instant.EPOCH,
          Instant.EPOCH.plusMillis(900),
          15,
          "StationA",
          SohMonitorType.MISSING
        ),
        HistoricalStationSoh.create(
          "StationA",
          new long[]{0, 100, 200, 300, 400, 500, 600, 700, 800, 900},
          List.of(
            HistoricalSohMonitorValues.create(
              "Channel1",
              Map.of(
                SohMonitorType.MISSING,
                PercentSohMonitorValues.create(
                  new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}
                )
              )
            ),
            HistoricalSohMonitorValues.create(
              "Channel2",
              Map.of(
                SohMonitorType.MISSING,
                PercentSohMonitorValues.create(
                  new double[]{11, 21, 31, 41, 51, 61, 71, 81, 91, 101}
                )
              )
            )
          )
        ),
        HistoricalStationSohAnalysisView.create(
          "StationA",
          new long[]{0, 100, 200, 300, 400, 500, 600, 700, 800, 900},
          List.of(
            HistoricalSohMonitorValuesAnalysisView.create(
              "Channel1",
              PercentSohMonitorValues.create(
                new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}
              ),
              5.5
            ),
            HistoricalSohMonitorValuesAnalysisView.create(
              "Channel2",
              PercentSohMonitorValues.create(
                new double[]{11, 21, 31, 41, 51, 61, 71, 81, 91, 101}
              ),
              Arrays.stream(new double[]{11, 21, 31, 41, 51, 61, 71, 81, 91, 101})
                .average().getAsDouble()
            )
          ),
          100.0

        )
      ),

      Arguments.arguments(
        DecimationRequestParams.create(
          Instant.EPOCH,
          Instant.EPOCH.plusMillis(900),
          1,
          "StationA",
          SohMonitorType.MISSING
        ),
        HistoricalStationSoh.create(
          "StationA",
          new long[]{0, 100, 200, 300, 400, 500, 600, 700, 800, 900},
          List.of(
            HistoricalSohMonitorValues.create(
              "Channel1",
              Map.of(
                SohMonitorType.MISSING,
                PercentSohMonitorValues.create(
                  new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}
                )
              )
            ),
            HistoricalSohMonitorValues.create(
              "Channel2",
              Map.of(
                SohMonitorType.MISSING,
                PercentSohMonitorValues.create(
                  new double[]{11, 21, 31, 41, 51, 61, 71, 81, 91, 101}
                )
              )
            )
          )
        ),
        HistoricalStationSohAnalysisView.create(
          "StationA",
          new long[]{0},
          List.of(
            HistoricalSohMonitorValuesAnalysisView.create(
              "Channel1",
              PercentSohMonitorValues.create(
                new double[]{1}
              ),
              5.5
            ),
            HistoricalSohMonitorValuesAnalysisView.create(
              "Channel2",
              PercentSohMonitorValues.create(
                new double[]{11}
              ),
              Arrays.stream(new double[]{11, 21, 31, 41, 51, 61, 71, 81, 91, 101})
                .average().getAsDouble()
            )
          ),
          10.0

        )
      ),

      Arguments.arguments(
        DecimationRequestParams.create(
          Instant.EPOCH,
          Instant.EPOCH.plusMillis(900),
          2,
          "StationA",
          SohMonitorType.MISSING
        ),
        HistoricalStationSoh.create(
          "StationA",
          new long[]{},
          List.of(
            HistoricalSohMonitorValues.create(
              "Channel1",
              Map.of(
                SohMonitorType.MISSING,
                PercentSohMonitorValues.create(
                  new double[]{}
                )
              )
            ),
            HistoricalSohMonitorValues.create(
              "Channel2",
              Map.of(
                SohMonitorType.MISSING,
                PercentSohMonitorValues.create(
                  new double[]{}
                )
              )
            )
          )
        ),
        HistoricalStationSohAnalysisView.create(
          "StationA",
          new long[]{},
          List.of(
            HistoricalSohMonitorValuesAnalysisView.create(
              "Channel1",
              PercentSohMonitorValues.create(
                new double[]{}
              ),
              -1
            ),
            HistoricalSohMonitorValuesAnalysisView.create(
              "Channel2",
              PercentSohMonitorValues.create(
                new double[]{}
              ),
              -1
            )
          ),
          0.0

        )
      )
      ,Arguments.arguments(
        DecimationRequestParams.create(
          Instant.EPOCH,
          Instant.EPOCH.plusMillis(900),
          2,
          "StationA",
          SohMonitorType.MISSING
        ),
        HistoricalStationSoh.create(
          "StationA",
          new long[]{0, 100, 200, 300, 400, 500, 600, 700, 800, 900},
          List.of(
            HistoricalSohMonitorValues.create(
              "Channel1",
              Map.of(
                SohMonitorType.MISSING,
                PercentSohMonitorValues.create(
                  new double[]{1, -1, -1, 4, 5, 6, -1, -1, -1, 10}
                )
              )
            ),
            HistoricalSohMonitorValues.create(
              "Channel2",
              Map.of(
                SohMonitorType.MISSING,
                PercentSohMonitorValues.create(
                  new double[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1}
                )
              )
            )
          )
        ),
        HistoricalStationSohAnalysisView.create(
          "StationA",
          new long[]{0, 500},
          List.of(
            HistoricalSohMonitorValuesAnalysisView.create(
              "Channel1",
              PercentSohMonitorValues.create(
                new double[]{1, 6}
              ),
              5.2
            ),
            HistoricalSohMonitorValuesAnalysisView.create(
              "Channel2",
              PercentSohMonitorValues.create(
                new double[]{-1, -1}
              ),
              -1
            )
          ),
          20.0

        )
      )
    );
  }
}
