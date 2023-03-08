package gms.shared.utilities.signalprocessing.filter;

import static java.lang.String.format;
import static java.util.Map.entry;

import gms.shared.frameworks.osd.coi.signaldetection.FilterDefinition;
import gms.shared.frameworks.osd.coi.signaldetection.FilterType;
import gms.shared.frameworks.osd.coi.waveforms.Waveform;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Utility operations to filter {@link Waveform}s
 */
public class Filter {

  private Filter() {
  }

  private static Map<FilterType, BiFunction<Waveform, FilterDefinition, Waveform>> filterFunctionsByFilterType = Map
    .ofEntries(
      entry(FilterType.FIR_HAMMING, Fir::filter),
      entry(FilterType.IIR_BUTTERWORTH, Iir::filter)
    );

  /**
   * Filters a {@link Waveform} using a {@link FilterDefinition}.  Only applies the filter if the
   * {@link Waveform#getSampleRate()} is in the inclusive range of {@link
   * FilterDefinition#getSampleRate()} +/- {@link FilterDefinition#getSampleRateTolerance()}
   *
   * @param waveform Waveform to filter, not null
   * @param filterDefinition FilterDefinition to apply, not null
   * @return filtered Waveform, not null
   * @throws NullPointerException if waveform or filterDefinition are null
   * @throws IllegalArgumentException if filterDefinition is not an FIR filter
   * @throws IllegalArgumentException if waveform sampleRate is not within tolerance of the filter's
   * sampleRate
   */
  public static Waveform filter(Waveform waveform, FilterDefinition filterDefinition) {

    Objects.requireNonNull(waveform, "Filter requires non-null waveform");
    Objects.requireNonNull(filterDefinition, "Filter requires non-null filterDefinition");

    // Verify filter sample rate matches waveform sample rate
    assertSampleRateWithinTolerance(waveform, filterDefinition);

    return getFilterFunction(filterDefinition.getFilterType())
      .map(f -> f.apply(waveform, filterDefinition))
      .orElseThrow(() -> new IllegalArgumentException(
        format("Error filtering: no filter function found for type %s",
          filterDefinition.getFilterType())));
  }

  /**
   * Determines if the {@link Waveform#getSampleRate()} is in the inclusive range of {@link
   * FilterDefinition#getSampleRate()} +/- {@link FilterDefinition#getSampleRateTolerance()}
   *
   * @param waveform {@link Waveform}, not null
   * @param filter {@link FilterDefinition}, not null
   * @throws IllegalArgumentException if waveform sampleRate is not within tolerance of the filter's
   * sampleRate
   */
  private static void assertSampleRateWithinTolerance(Waveform waveform, FilterDefinition filter) {
    final double minSampleRate = filter.getSampleRate() - filter.getSampleRateTolerance();
    final double maxSampleRate = filter.getSampleRate() + filter.getSampleRateTolerance();

    if (waveform.getSampleRate() < minSampleRate || waveform.getSampleRate() > maxSampleRate) {
      throw new IllegalArgumentException(
        "Filter requires input waveform with sampleRate in [" + minSampleRate + ", "
          + maxSampleRate + "]");
    }
  }

  private static Optional<BiFunction<Waveform, FilterDefinition, Waveform>> getFilterFunction(
    FilterType filterType) {
    return Optional.ofNullable(filterFunctionsByFilterType.get(filterType));
  }
}
