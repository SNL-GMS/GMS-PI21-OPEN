package gms.shared.waveform.coi.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import gms.shared.waveform.coi.Timeseries;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TimeseriesUtility {

  private TimeseriesUtility() {
    // prevent instantiation
  }

  /**
   * Computes the length of the Timeseries collection
   *
   * @param timeseries The collection of timeseries of which to find the duration
   * @return
   */
  public static Range<Instant> computeSpan(Collection<? extends Timeseries> timeseries) {
    Preconditions.checkNotNull(timeseries);

    return timeseries.stream().map(Timeseries::computeTimeRange)
      .reduce(Range::span)
      .orElseThrow(() -> new IllegalArgumentException("Must provide at least 1 timeseries"));
  }

  /**
   * Determines if the Timeseries in the provided collection do not overlap
   *
   * @param series The collection of timeseries to determine if there are overlaps
   * @return
   */
  public static boolean noneOverlapped(Collection<? extends Timeseries> series) {
    Preconditions.checkNotNull(series);
    List<Range<Instant>> ranges = computeSortedTimeRanges(series);

    for (int i = 0; i < ranges.size() - 1; i++) {
      if (ranges.get(i).contains(ranges.get(i + 1).lowerEndpoint())) {
        return false;
      }
    }

    return true;
  }

  /**
   * Collects the provided timeseries by those that have connecting time ranges
   *
   * @param series The timeseries to cluster
   * @param <T> The type of the Timeseries
   * @return
   */
  public static <T extends Timeseries> Map<Range<Instant>, List<T>> clusterByConnected(
    List<T> series) {
    Objects.requireNonNull(series);

    if (series.isEmpty()) {
      return Map.of();
    }

    Map<Range<Instant>, List<T>> clusters = new HashMap<>();

    Iterator<T> iterator = series.stream().sorted().iterator();
    T next = iterator.next();

    //initialize starting cluster
    List<T> cluster = new ArrayList<>();
    cluster.add(next);
    Range<Instant> clusterRange = next.computeTimeRange();

    Range<Instant> nextRange;
    while (iterator.hasNext()) {
      next = iterator.next();
      nextRange = next.computeTimeRange();

      if (clusterRange.isConnected(nextRange)) {
        //can be clustered
        cluster.add(next);
        clusterRange = clusterRange.span(nextRange);
      } else {
        //finish this cluster and create a new one
        clusters.put(clusterRange, cluster);
        cluster = new ArrayList<>();
        cluster.add(next);
        clusterRange = nextRange;
      }
    }

    //put the final cluster
    clusters.put(clusterRange, cluster);
    return clusters;
  }

  /**
   *
   * [----------]
   *        [--]
   *     [--------]
   *                 [---]
   * result: 1, 3, 4
   */

  /**
   * filters out all series contained by other series
   */
  public static <T extends Timeseries> List<T> filterEnclosed(List<T> series) {
    Objects.requireNonNull(series);

    if (series.size() < 2) {
      return series;
    }

    Iterator<T> sorted = series.stream()
      .sorted(Comparator.comparing(Timeseries::getStartTime)
        .thenComparing(Comparator.comparing(Timeseries::getEndTime).reversed()))
      .iterator();

    List<T> filtered = new ArrayList<>();
    T previous = sorted.next();
    filtered.add(previous);
    while (sorted.hasNext()) {
      T current = sorted.next();
      if (!previous.computeTimeRange().encloses(current.computeTimeRange())) {
        filtered.add(current);
        previous = current;
      }
    }

    return filtered;
  }

  /**
   * returns time ranges sorted by a timeseries' start time
   *
   * @param series
   * @return
   */
  private static List<Range<Instant>> computeSortedTimeRanges(Collection<? extends Timeseries> series) {
    return series.stream()
      .sorted(Comparator.comparing(Timeseries::getStartTime))
      .map(Timeseries::computeTimeRange)
      .collect(Collectors.toList());
  }
}
