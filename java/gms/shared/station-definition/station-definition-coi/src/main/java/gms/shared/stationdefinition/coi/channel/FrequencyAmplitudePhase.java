package gms.shared.stationdefinition.coi.channel;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.stationdefinition.coi.utils.Units;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkState;
import static gms.shared.stationdefinition.coi.utils.StationDefinitionCoiUtils.FREQUENCY_AMPLITUDE_PHASE_COMPARATOR;

/**
 * A value class for retrieving a Frequency Amplitude and Phase (FAP) object, given a specified
 * frequency
 */
@AutoValue
@JsonSerialize(as = FrequencyAmplitudePhase.class)
@JsonDeserialize(builder = AutoValue_FrequencyAmplitudePhase.Builder.class)
@JsonPropertyOrder(alphabetic = true)
public abstract class FrequencyAmplitudePhase implements Comparable<FrequencyAmplitudePhase> {

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_FrequencyAmplitudePhase.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    Builder setId(UUID id);

    UUID getId();

    @JsonUnwrapped
    default Builder setData(Data data) {
      return setData(Optional.ofNullable(data));
    }

    Builder setData(Optional<Data> data);

    FrequencyAmplitudePhase autoBuild();

    default FrequencyAmplitudePhase build() {
      return autoBuild();
    }
  }

  public static FrequencyAmplitudePhase createEntityReference(UUID id) {
    return new AutoValue_FrequencyAmplitudePhase.Builder()
      .setId(id)
      .build();
  }

  /**
   * Planning to add some sort of conversion between uuid and channelName.
   * This conversion is intended to be cached at the level of the accessor.
   */
  public abstract UUID getId();

  @JsonIgnore
  public AmplitudePhaseResponse getResponseAtFrequency(Double freq) {
    return getDataOrThrow().getResponseAtFrequency(freq);
  }

  @JsonIgnore
  public boolean isPresent() {
    return getData().isPresent();
  }

  @JsonUnwrapped
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public abstract Optional<FrequencyAmplitudePhase.Data> getData();

  private FrequencyAmplitudePhase.Data getDataOrThrow() {
    return getDataOptional().orElseThrow(() -> new IllegalStateException("Only contains ID facet"));
  }

  @JsonIgnore
  Optional<FrequencyAmplitudePhase.Data> getDataOptional() {
    return getData();
  }

  @AutoValue
  @JsonSerialize(as = FrequencyAmplitudePhase.Data.class)
  @JsonDeserialize(builder = AutoValue_FrequencyAmplitudePhase_Data.Builder.class)
  @JsonPropertyOrder(alphabetic = true)
  public abstract static class Data {

    public static FrequencyAmplitudePhase.Data.Builder builder() {
      return new AutoValue_FrequencyAmplitudePhase_Data.Builder();
    }

    public abstract FrequencyAmplitudePhase.Data.Builder toBuilder();

    @AutoValue.Builder
    @JsonPOJOBuilder(withPrefix = "set")
    public interface Builder {

      FrequencyAmplitudePhase.Data.Builder setFrequencies(double[] frequencies);

      Optional<double[]> getFrequencies();

      FrequencyAmplitudePhase.Data.Builder setAmplitudeResponseUnits(Units amplitudeResponseUnits);

      Optional<Units> getAmplitudeResponseUnits();

      FrequencyAmplitudePhase.Data.Builder setAmplitudeResponse(double[] amplitudeResponse);

      Optional<double[]> getAmplitudeResponse();

      FrequencyAmplitudePhase.Data.Builder setAmplitudeResponseStdDev(double[] amplitudeResponseStdDev);

      Optional<double[]> getAmplitudeResponseStdDev();

      FrequencyAmplitudePhase.Data.Builder setPhaseResponseUnits(Units phaseResponseUnits);

      Optional<Units> getPhaseResponseUnits();

      FrequencyAmplitudePhase.Data.Builder setPhaseResponse(double[] phaseResponse);

      Optional<double[]> getPhaseResponse();

      FrequencyAmplitudePhase.Data.Builder setPhaseResponseStdDev(double[] phaseResponseStdDev);

      Optional<double[]> getPhaseResponseStdDev();


      FrequencyAmplitudePhase.Data autoBuild();

      default FrequencyAmplitudePhase.Data build() {
        final List<Optional<?>> allFields = List
          .of(getFrequencies(), getAmplitudeResponseUnits(), getAmplitudeResponse(),
            getAmplitudeResponseStdDev(), getPhaseResponseUnits(), getPhaseResponse(),
            getPhaseResponseStdDev());
        final long numPresentFields = allFields.stream().filter(Optional::isPresent).count();

        if (0 == numPresentFields) {
          return null;
        } else if (allFields.size() == numPresentFields) {

          FrequencyAmplitudePhase.Data fap = autoBuild();
          Validate.isTrue(fap.getFrequencies().length == fap.getAmplitudeResponse().length &&
              fap.getFrequencies().length == fap.getAmplitudeResponseStdDev().length &&
              fap.getFrequencies().length == fap.getPhaseResponse().length &&
              fap.getFrequencies().length == fap.getPhaseResponseStdDev().length,
            "All Double[] must be the same length");

          // Check if array is already sorted so we don't sort something that is sorted already
          double[] fapFrequencies = fap.getFrequencies();
          double[] sortedFrequencies = Arrays.copyOf(fapFrequencies, fapFrequencies.length);
          Arrays.sort(sortedFrequencies);
          if (Arrays.equals(sortedFrequencies, fapFrequencies)) {
            return fap;
          }

          // Sort all Double arrays by frequency
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

        throw new IllegalStateException(
          "Either all FacetedDataClass fields must be populated or none of them can be populated");
      }
    }

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
    public AmplitudePhaseResponse getResponseAtFrequency(Double freq) {
      if (this.getFrequencies().length == 0) {
        throw new IndexOutOfBoundsException("Cannot retrieve response for frequency: " + freq +
          ". frequency list is empty");
      }

      int freqIndex = Arrays.binarySearch(this.getFrequencies(), freq);
      if (freqIndex >= 0) {
        return AmplitudePhaseResponse.from(
          DoubleValue.from(
            this.getAmplitudeResponse()[freqIndex],
            Optional.of(this.getAmplitudeResponseStdDev()[freqIndex]),
            this.getAmplitudeResponseUnits()),
          DoubleValue.from(
            this.getPhaseResponse()[freqIndex],
            Optional.of(this.getPhaseResponseStdDev()[freqIndex]),
            this.getPhaseResponseUnits()));
      }

      if (this.getFrequencies().length < 2) {
        throw new IndexOutOfBoundsException("Cannot retrieve response for frequency: " + freq +
          "; cannot interpolate response since size of frequency list is < 2");
      }

      int lastFreqIndex = this.getFrequencies().length - 1;
      Double f1 = this.getFrequencies()[0];
      Double f2 = this.getFrequencies()[lastFreqIndex];

      checkState(freq >= f1 && freq <= f2, "Frequency does not fall within frequency bounds [%s, %s]",
        f1, f2);

      Double amp1 = this.getAmplitudeResponse()[0];
      Double amp2 = this.getAmplitudeResponse()[lastFreqIndex];

      Double phase1 = this.getPhaseResponse()[0];
      Double phase2 = this.getPhaseResponse()[lastFreqIndex];

      Double interpAmplitude = amp1 + ((freq - f1) * (amp2 - amp1) / (f2 - f1));
      Double interpPhase = phase1 + ((freq - f1) * (phase2 - phase1) / (f2 - f1));

      Double stdDevA;
      Double stdDevP;

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
        .from(interpAmplitude, Optional.of(stdDevA), this.getAmplitudeResponseUnits());
      DoubleValue iph = DoubleValue.from(interpPhase, Optional.of(stdDevP), this.getPhaseResponseUnits());

      return new AutoValue_AmplitudePhaseResponse(iamp, iph);

    }
  }

  @Override
  public int compareTo(FrequencyAmplitudePhase otherResponse) {
    return FREQUENCY_AMPLITUDE_PHASE_COMPARATOR.compare(this, otherResponse);
  }

  @Override
  public abstract int hashCode();

  @Override
  public abstract boolean equals(Object obj);
}
