package gms.shared.frameworks.osd.repository.performancemonitoring.transform;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType.SohValueType;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.dao.soh.HistoricalSohMonitorValue;
import gms.shared.frameworks.osd.dto.soh.DurationSohMonitorValues;
import gms.shared.frameworks.osd.dto.soh.HistoricalSohMonitorValues;
import gms.shared.frameworks.osd.dto.soh.HistoricalStationSoh;
import gms.shared.frameworks.osd.dto.soh.PercentSohMonitorValues;
import gms.shared.frameworks.osd.dto.soh.SohMonitorValues;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class HistoricalStationSohTransformerTests {

  @Test
  void checkPercentValueDefaultForNull() {
    var hsmv1 = new HistoricalSohMonitorValue("MKAR", "MKAR.MK08.SHZ", Timestamp.valueOf("2020-07-14 19:00:00.000000").toInstant(),
      SohMonitorType.MISSING);
    hsmv1.setStatus(SohStatus.GOOD);

    var hsmv2 = new HistoricalSohMonitorValue("MKAR", "MKAR.MK08.SHZ", Timestamp.valueOf("2020-07-14 19:00:20.000000").toInstant(),
      SohMonitorType.MISSING);
    hsmv2.setStatus(SohStatus.GOOD);

    HistoricalStationSoh historicalStationSoh =
      HistoricalStationSohTransformer.createHistoricalStationSoh("MKAR", List.of(hsmv1, hsmv2));

    historicalStationSoh.getMonitorValues()
      .stream()
      .map(HistoricalSohMonitorValues::getValuesByType)
      .map(ImmutableMap::values)
      .forEach(values -> values.forEach(sohMonitorValues -> {
        final var percentSohMonitorValues = (PercentSohMonitorValues) sohMonitorValues;
        final var valuesSize = percentSohMonitorValues.size();
        assertEquals(valuesSize,
          Arrays.stream(percentSohMonitorValues.getValues()).filter(v -> v == 100).count());
      }));
  }

  @ParameterizedTest
  @MethodSource("getHistoricalSohMonitorValues")
  void createHistoricalStationSohCalculationTimesTest(List<HistoricalSohMonitorValue> historicalSohMonitorValues) {
    HistoricalStationSoh historicalStationSoh =
      HistoricalStationSohTransformer.createHistoricalStationSoh("MKAR", historicalSohMonitorValues);

    assertEquals(2, historicalStationSoh.getCalculationTimes().length,
      "Calculation times is the wrong length, should be 2 but was: " +
        historicalStationSoh.getCalculationTimes().length);

    assertEquals(Timestamp
        .valueOf("2020-07-14 19:00:00.000000").getTime(),
      historicalStationSoh.getCalculationTimes()[0],
      "Calculation Time not set with correct value");

    for (int i = 0; i < historicalStationSoh.getCalculationTimes().length - 1; i++) {
      assertTrue(historicalStationSoh.getCalculationTimes()[i] < historicalStationSoh
          .getCalculationTimes()[i + 1],
        "CalculationTimes are not sorted correctly");
    }
  }

  @ParameterizedTest
  @MethodSource("getHistoricalSohMonitorValues")
  void validateHistoricalStationSohValueArrays(List<HistoricalSohMonitorValue> historicalSohMonitorValues) {
    HistoricalStationSoh historicalStationSoh =
      HistoricalStationSohTransformer
        .createHistoricalStationSoh("MKAR", historicalSohMonitorValues);

    for (HistoricalSohMonitorValues hmv : historicalStationSoh.getMonitorValues()) {
      ArrayList<Long> iterableCalculationTimes = new ArrayList<>();
      Arrays.stream(historicalStationSoh.getCalculationTimes())
        .forEach(iterableCalculationTimes::add);
      for (Map.Entry<SohMonitorType, SohMonitorValues> es : hmv.getValuesByType().entrySet()) {
        assertEquals(2, es.getValue().size(), "value array not correct size");
        if (es.getValue() instanceof DurationSohMonitorValues) {
          DurationSohMonitorValues smv = (DurationSohMonitorValues) es.getValue();
          for (int i = 0; i < smv.getValues().length - 1; i++) {
            assertTrue(smv.getValues()[i] > smv.getValues()[i + 1],
              "DurationSohMonitorValues are not sorted correctly");
          }
        }
        if (es.getValue() instanceof PercentSohMonitorValues) {
          PercentSohMonitorValues smv = (PercentSohMonitorValues) es.getValue();
          ArrayList<Double> iterableValues = new ArrayList<>();
          Arrays.stream(smv.getValues()).forEach(iterableValues::add);
          if (Ordering.natural().isOrdered(iterableCalculationTimes)) {
            assertTrue(Ordering.natural().isOrdered(iterableValues),
              "PercentSohMonitorValues are not sorted correctly");
          } else {
            assertTrue(Ordering.natural().reverse().isOrdered(iterableValues),
              "PercentSohMonitorValues are not sorted correctly");
          }
        }
      }
    }
  }

  static Stream<Arguments> getHistoricalSohMonitorValues() {


    return
      Arrays.stream(SohMonitorType.values())
        .filter(
          sohMonitorType -> !sohMonitorType.getSohValueType().equals(SohValueType.INVALID))
        .map(smvsType -> {
          ArrayList<HistoricalSohMonitorValue> historicalSohMonitorValueArrayList = new ArrayList<>();
          if (smvsType.getSohValueType().equals(SohValueType.DURATION)) {

            var historicalSohMonitorValue1 = new HistoricalSohMonitorValue("MKAR", "MKAR.MK08.SHZ",
              Timestamp.valueOf("2020-07-14 19:00:00.000000").toInstant(), smvsType);
            historicalSohMonitorValue1.setStatus(SohStatus.GOOD);
            historicalSohMonitorValue1.setDuration(654321);

            historicalSohMonitorValueArrayList.add(historicalSohMonitorValue1);
            //test duplicate database data should be ignored
            historicalSohMonitorValueArrayList.add(historicalSohMonitorValue1);

            var historicalSohMonitorValue2 = new HistoricalSohMonitorValue("MKAR", "MKAR.MK08.SHZ",
              Timestamp.valueOf("2020-07-14 19:00:20.000000").toInstant(), smvsType);
            historicalSohMonitorValue2.setStatus(SohStatus.GOOD);
            historicalSohMonitorValue2.setDuration(123456);

            historicalSohMonitorValueArrayList.add(historicalSohMonitorValue2);
          } else {

            var historicalSohMonitorValue3 = new HistoricalSohMonitorValue("MKAR", "MKAR.MK08.SHZ",
              Timestamp.valueOf("2020-07-14 19:00:00.000000").toInstant(), smvsType);
            historicalSohMonitorValue3.setStatus(SohStatus.GOOD);
            historicalSohMonitorValue3.setPercent(0.1f);

            var historicalSohMonitorValue4 = new HistoricalSohMonitorValue("MKAR", "MKAR.MK08.SHZ",
              Timestamp.valueOf("2020-07-14 19:00:20.000000").toInstant(), smvsType);
            historicalSohMonitorValue4.setStatus(SohStatus.GOOD);
            historicalSohMonitorValue4.setPercent(0.2f);

            historicalSohMonitorValueArrayList.add(historicalSohMonitorValue3);
            historicalSohMonitorValueArrayList.add(historicalSohMonitorValue4);
          }
          return arguments(historicalSohMonitorValueArrayList);
        });
  }
}
