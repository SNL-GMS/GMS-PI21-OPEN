package gms.core.performancemonitoring.ssam.control;

import com.google.common.primitives.ImmutableDoubleArray;
import com.google.common.primitives.ImmutableLongArray;
import gms.core.performancemonitoring.ssam.control.api.DecimationRequestParams;
import gms.core.performancemonitoring.ssam.control.api.HistoricalSohMonitorValuesAnalysisView;
import gms.core.performancemonitoring.ssam.control.api.HistoricalStationSohAnalysisView;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType.SohValueType;
import gms.shared.frameworks.osd.dto.soh.DurationSohMonitorValues;
import gms.shared.frameworks.osd.dto.soh.HistoricalStationSoh;
import gms.shared.frameworks.osd.dto.soh.PercentSohMonitorValues;
import gms.shared.frameworks.osd.dto.soh.SohMonitorValues;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility for performing decimation on historical data. The utility class assumes that we
 * want to avoid boxing and unboxing primitives, since the performance of boxing and unboxing
 * could become important as the volume of data increases.
 */
public class DecimationUtility {

  private DecimationUtility() {

  }

  /**
   * This class holds the decimated and undecimated values. Since SohMonitorValues does not
   * have the values field, we must know explicitly which subclass we are dealing with.
   * <p>
   * This class is designed to only have one type of values field populated, hence a vague resemblence
   * to a C union.
   * <p>
   * This class also does all the low-level work of decimating.
   */
  private static class DecimatedValuesUnion {

    PercentSohMonitorValues undecimatedPercentValues;
    DurationSohMonitorValues undecimatedDurationValues;

    PercentSohMonitorValues decimatedPercentValues;
    DurationSohMonitorValues decimatedDurationValues;

    DecimatedValuesUnion(
      SohMonitorValues sohMonitorValues,
      boolean[] decimationFlags
    ) {
      if (sohMonitorValues.getType() == SohValueType.PERCENT) {
        undecimatedPercentValues = (PercentSohMonitorValues) sohMonitorValues;
      } else if (sohMonitorValues.getType() == SohValueType.DURATION) {
        undecimatedDurationValues = (DurationSohMonitorValues) sohMonitorValues;
      }

      decimateMe(decimationFlags);
    }

    SohMonitorValues getUndecimatedSohMonitorValues() {
      if (undecimatedDurationValues != null) {
        return undecimatedDurationValues;
      } else {
        return undecimatedPercentValues;
      }
    }

    SohMonitorValues getDecimatedSohMonitorValues() {
      if (decimatedDurationValues != null) {
        return decimatedDurationValues;
      } else {
        return decimatedPercentValues;
      }
    }

    /**
     * Do the decimating
     *
     * @param decimationFlags flags use during decimation process
     */
    void decimateMe(boolean[] decimationFlags) {

      var isDuration = undecimatedPercentValues == null;

      var doubleBuilder = ImmutableDoubleArray.builder();
      var longBuilder = ImmutableLongArray.builder();

      IntStream.range(0, decimationFlags.length)
        .forEach(index -> {

          if (isDuration && decimationFlags[index]) {
            longBuilder.add(undecimatedDurationValues.getValues()[index]);
          } else if (!isDuration && decimationFlags[index]) {
            doubleBuilder.add(undecimatedPercentValues.getValues()[index]);
          }

        });

      if (isDuration) {
        decimatedDurationValues = DurationSohMonitorValues.create(longBuilder.build().toArray());
      } else {
        decimatedPercentValues = PercentSohMonitorValues.create(doubleBuilder.build().toArray());
      }
    }
  }

  /**
   * Decimate a single HistoricalStationSoh and transform it into a HistoricalStationSohAnalysisView
   *
   * @param decimationRequestParams parameters for decimation
   * @param historicalStationSoh the HistoricalStationSoh to decimate
   * @return HistoricalStationSohAnalysisView with decimated data
   */
  public static HistoricalStationSohAnalysisView decimateHistoricalStationSoh(
    DecimationRequestParams decimationRequestParams,
    HistoricalStationSoh historicalStationSoh
  ) {

    var decimatedTimesBuilder = ImmutableLongArray.builder();

    var decimationFlags = flagSurvivingPoints(
      historicalStationSoh.getCalculationTimes(),
      decimationRequestParams.getSamplesPerChannel(),
      decimatedTimesBuilder
    );

    var historicalSohMonitorValuesAnalysisViewList = historicalStationSoh.getMonitorValues()
      .stream()
      .map(historicalSohMonitorValues -> Map.entry(
        historicalSohMonitorValues.getChannelName(),
        new DecimatedValuesUnion(
          getSohMonitorValues(historicalSohMonitorValues.getValuesByType().values()),
          decimationFlags
        )
      ))
      .map(entry -> {
          var average = getAverage(entry.getValue().getUndecimatedSohMonitorValues());

          return HistoricalSohMonitorValuesAnalysisView.create(
            entry.getKey(),
            entry.getValue().getDecimatedSohMonitorValues(),
            average
          );
        }
      )
      .collect(Collectors.toList());

    var decimatedTimes = decimatedTimesBuilder.build().stream().toArray();

    double percentageSent;

    if (historicalStationSoh.getCalculationTimes().length == 0) {
      percentageSent = 0;
    } else {
      percentageSent = 100 * ((double) decimatedTimes.length) / historicalStationSoh
        .getCalculationTimes().length;
    }

    return HistoricalStationSohAnalysisView.create(
      historicalStationSoh.getStationName(),
      decimatedTimes,
      historicalSohMonitorValuesAnalysisViewList,
      percentageSent
    );
  }

  private static SohMonitorValues getSohMonitorValues(
    Collection<SohMonitorValues> sohMonitorValues) {

    if (sohMonitorValues == null) {
      return null;
    }
    //should only ever be one thing in the collection
    return sohMonitorValues.iterator().next();
  }

  private static double getAverage(SohMonitorValues sohMonitorValues) {
    if (sohMonitorValues.getType() == SohValueType.DURATION) {
      return Arrays.stream(((DurationSohMonitorValues) sohMonitorValues).getValues())
        .filter(value -> value != -1l)
        .average()
        .orElse(-1);
    } else if (sohMonitorValues.getType() == SohValueType.PERCENT) {
      return Arrays.stream(((PercentSohMonitorValues) sohMonitorValues).getValues())
        .filter(value -> value != -1l)
        .average()
        .orElse(-1);
    }
    return -1;
  }

  /**
   * Build a boolean array that indicates whether the data at the index makes it into the final
   * decimated array. Also, add those times that "survive" to the given ImmutableLongArray builder.
   *
   * @param times Times of historical data
   * @param samplesPerChannel How many samples to keep
   * @param decimatedTimesBuilder The ImmutableLongArray builder that we add the surviving times to
   * @return boolean array
   */
  private static boolean[] flagSurvivingPoints(
    long[] times,
    int samplesPerChannel,
    ImmutableLongArray.Builder decimatedTimesBuilder
  ) {

    if (samplesPerChannel == 0 || times.length == 0) {
      return new boolean[]{};
    }

    //
    // Determine how many of original points fit between two decimated points.
    //
    var pointsPerDecimatedRange = Math.round(
      (double) times.length / Math.min(samplesPerChannel, times.length)
    );

    var flagArray = new boolean[times.length];

    IntStream.range(0, times.length).forEach(index -> {

        //
        // Use modulo arithmetic with the pointsPerDecimatedRange to determine if the data
        // at this index "survives".
        //
        if (index % pointsPerDecimatedRange == 0) {
          flagArray[index] = true;
          decimatedTimesBuilder.add(times[index]);
        } else {
          flagArray[index] = false;
        }
      }
    );

    return flagArray;
  }
}
