package gms.shared.frameworks.osd.coi.signaldetection;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.DoubleValue;
import gms.shared.frameworks.osd.coi.Units;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkState;

/**
 * A value class for retrieving a Frequency Amplitude and Phase (FAP) object, given a specified
 * frequency
 */
@AutoValue
@JsonSerialize(as = FrequencyAmplitudePhase.class)
@JsonDeserialize(builder = AutoValue_FrequencyAmplitudePhase.Builder.class)
public abstract class FrequencyAmplitudePhase {

  @SuppressWarnings("mutable")
  public abstract double[] getFrequencies();

  public abstract Units getAmplitudeResponseUnits();

  @SuppressWarnings("mutable")
  public abstract double[] getAmplitudeResponse();

  @SuppressWarnings("mutable")
  public abstract double[] getAmplitudeResponseStdDev();

  public abstract Units getPhaseResponseUnits();

  @SuppressWarnings("mutable")
  public abstract double[] getPhaseResponse();

  @SuppressWarnings("mutable")
  public abstract double[] getPhaseResponseStdDev();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_FrequencyAmplitudePhase.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setFrequencies(double[] frequencies);

    public abstract Builder setAmplitudeResponseUnits(Units amplitudeResponseUnits);

    public abstract Builder setAmplitudeResponse(double[] amplitudeResponse);

    public abstract Builder setAmplitudeResponseStdDev(double[] amplitudeResponseStdDev);

    public abstract Builder setPhaseResponseUnits(Units phaseResponseUnits);

    public abstract Builder setPhaseResponse(double[] phaseResponse);

    public abstract Builder setPhaseResponseStdDev(double[] phaseResponseStdDev);

    protected abstract FrequencyAmplitudePhase autoBuild();

    public FrequencyAmplitudePhase build() {
      FrequencyAmplitudePhase fap = autoBuild();
      Validate.isTrue(fap.getFrequencies().length == fap.getAmplitudeResponse().length &&
          fap.getFrequencies().length == fap.getAmplitudeResponseStdDev().length &&
          fap.getFrequencies().length == fap.getPhaseResponse().length &&
          fap.getFrequencies().length == fap.getPhaseResponseStdDev().length,
        "All double[] must be the same length");

      // Check if array is already sorted so we don't sort something that is sorted already
      double[] fapFrequencies = fap.getFrequencies();
      double[] sortedFrequencies = Arrays.copyOf(fapFrequencies, fapFrequencies.length);
      Arrays.sort(sortedFrequencies);
      if (Arrays.equals(sortedFrequencies, fapFrequencies)) {
        return fap;
      }

      // Sort all double arrays by frequency
      TreeMap<Double, List<Double>> sortedByFreqMap = new TreeMap<>();
      IntStream.range(0, fapFrequencies.length).forEach(i ->
        sortedByFreqMap.put(fapFrequencies[i],
          List.of(fap.getAmplitudeResponse()[i], fap.getAmplitudeResponseStdDev()[i],
            fap.getPhaseResponse()[i], fap.getPhaseResponseStdDev()[i])));

      // Rebuild all double[] sorted by frequency
      int index = 0;
      sortedFrequencies = new double[fapFrequencies.length];
      double[] sortedAmplitudeResponse = new double[fapFrequencies.length];
      double[] sortedAmplitudeResponseStdDev = new double[fapFrequencies.length];
      double[] sortedPhaseResponse = new double[fapFrequencies.length];
      double[] sortedPhaseResponseStdDev = new double[fapFrequencies.length];
      for (Map.Entry<Double, List<Double>> mapEntry : sortedByFreqMap.entrySet()) {
        sortedFrequencies[index] = mapEntry.getKey();
        List<Double> freqList = mapEntry.getValue();
        sortedAmplitudeResponse[index] = freqList.get(0);
        sortedAmplitudeResponseStdDev[index] = freqList.get(1);
        sortedPhaseResponse[index] = freqList.get(2);
        sortedPhaseResponseStdDev[index] = freqList.get(3);
        index++;
      }
      setFrequencies(sortedFrequencies);
      setAmplitudeResponse(sortedAmplitudeResponse);
      setAmplitudeResponseStdDev(sortedAmplitudeResponseStdDev);
      setPhaseResponse(sortedPhaseResponse);
      setPhaseResponseStdDev(sortedPhaseResponseStdDev);

      return autoBuild();
    }
  }

  /**
   * Interpolates any frequencies requested that are not in the map, given that the frequency is
   * within the range of max/min of frequencies in the map, or if the frequency IS in the map,
   * return that AmplitudePhaseResponse object. SME guidance for this implementation: For frequency
   * fx, find the nearest lower and higher frequency (f1 and f2), then the interpolated amplitude is
   * ax = a1 + ( (fx - f1) * (a2 - a1)  / (f2 - f1) ). Same for phase. You can throw an
   * OutOfRangeException if fx less than f1 or fx greater than f2.
   *
   * @param freq The frequency to fetch amplitude and phase for.
   * @return AmplitudePhaseResponse Object, either containing the map entry, or the interpolated
   * values, if the frequency is within range.
   */
  public AmplitudePhaseResponse getResponseAtFrequency(double freq) {
    if (this.getFrequencies().length == 0) {
      throw new IndexOutOfBoundsException("Cannot retrieve response for frequency: " + freq +
        ". frequency list is empty");
    }

    int freqIndex = Arrays.binarySearch(this.getFrequencies(), freq);
    if (freqIndex >= 0) {
      return AmplitudePhaseResponse.from(
        DoubleValue.from(
          this.getAmplitudeResponse()[freqIndex],
          this.getAmplitudeResponseStdDev()[freqIndex],
          this.getAmplitudeResponseUnits()),
        DoubleValue.from(
          this.getPhaseResponse()[freqIndex],
          this.getPhaseResponseStdDev()[freqIndex],
          this.getPhaseResponseUnits()));
    }

    if (this.getFrequencies().length < 2) {
      throw new IndexOutOfBoundsException("Cannot retrieve response for frequency: " + freq +
        "; cannot interpolate response since size of frequency list is < 2");
    }

    int lastFreqIndex = this.getFrequencies().length - 1;
    double f1 = this.getFrequencies()[0];
    double f2 = this.getFrequencies()[lastFreqIndex];

    checkState(freq >= f1 && freq <= f2, "Frequency does not fall within frequency bounds [%s, %s]",
      f1, f2);

    double amp1 = this.getAmplitudeResponse()[0];
    double amp2 = this.getAmplitudeResponse()[lastFreqIndex];

    double phase1 = this.getPhaseResponse()[0];
    double phase2 = this.getPhaseResponse()[lastFreqIndex];

    double interpAmplitude = amp1 + ((freq - f1) * (amp2 - amp1) / (f2 - f1));
    double interpPhase = phase1 + ((freq - f1) * (phase2 - phase1) / (f2 - f1));

    double stdDevA;
    double stdDevP;

    if (this.getAmplitudeResponseStdDev()[0] == this
      .getAmplitudeResponseStdDev()[lastFreqIndex]) {
      stdDevA = this.getAmplitudeResponseStdDev()[0];
    } else {
      stdDevA = this.getAmplitudeResponseStdDev()[0] + (
        (freq - f1) * (this.getAmplitudeResponseStdDev()[lastFreqIndex] - this
          .getAmplitudeResponseStdDev()[0]) / (f2 - f1));
    }

    if (this.getPhaseResponseStdDev()[0] == this.getPhaseResponseStdDev()[lastFreqIndex]) {
      stdDevP = this.getPhaseResponseStdDev()[0];
    } else {
      stdDevP = this.getPhaseResponseStdDev()[0] + (
        (freq - f1) * (this.getPhaseResponseStdDev()[lastFreqIndex] - this
          .getPhaseResponseStdDev()[0]) / (f2 - f1));
    }

    DoubleValue iamp = DoubleValue
      .from(interpAmplitude, stdDevA, this.getAmplitudeResponseUnits());
    DoubleValue iph = DoubleValue.from(interpPhase, stdDevP, this.getPhaseResponseUnits());

    return new AutoValue_AmplitudePhaseResponse(iamp, iph);

  }
}
