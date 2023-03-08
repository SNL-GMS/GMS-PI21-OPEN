package gms.shared.frameworks.osd.dao.stationreference;

import com.google.common.base.Preconditions;
import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.signaldetection.FrequencyAmplitudePhase;
import gms.shared.frameworks.osd.dao.util.DoublePrecisionArrayType;
import gms.shared.frameworks.osd.dao.util.UnitsConverter;
import org.apache.commons.lang3.Validate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Arrays;
import java.util.Objects;

/**
 * JPA data access object for {@link FrequencyAmplitudePhase}
 */
@TypeDef(
  name = "double-precision-array",
  typeClass = DoublePrecisionArrayType.class
)
@Entity
@Table(name = "frequency_amplitude_phase")
public class FrequencyAmplitudePhaseDao {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "frequency_amplitude_phase_sequence")
  @SequenceGenerator(name = "frequency_amplitude_phase_sequence", sequenceName = "frequency_amplitude_phase_sequence", allocationSize = 5)
  private long id;

  @Type(type = "double-precision-array")
  @Column(name = "frequencies",
    columnDefinition = "double precision[]")
  private double[] frequencies;

  @Column(name = "amplitude_response_units")
  @Convert(converter = UnitsConverter.class)
  private Units amplitudeResponseUnits;

  @Type(type = "double-precision-array")
  @Column(name = "amplitude_response",
    columnDefinition = "double precision[]")
  private double[] amplitudeResponse;

  @Type(type = "double-precision-array")
  @Column(name = "amplitude_response_standard_deviation",
    columnDefinition = "double precision[]")
  private double[] amplitudeResponseStdDev;

  @Column(name = "phase_response_units")
  @Convert(converter = UnitsConverter.class)
  private Units phaseResponseUnits;

  @Type(type = "double-precision-array")
  @Column(name = "phase_response",
    columnDefinition = "double precision[]")
  private double[] phaseResponse;

  @Type(type = "double-precision-array")
  @Column(name = "phase_response_standard_deviation",
    columnDefinition = "double precision[]")
  private double[] phaseResponseStandardDeviation;

  protected FrequencyAmplitudePhaseDao() {
  }

  private FrequencyAmplitudePhaseDao(FrequencyAmplitudePhase fap) {
    this.frequencies = fap.getFrequencies();
    this.amplitudeResponseUnits = fap.getAmplitudeResponseUnits();
    this.amplitudeResponse = fap.getAmplitudeResponse();
    this.amplitudeResponseStdDev = fap.getAmplitudeResponseStdDev();
    this.phaseResponseUnits = fap.getPhaseResponseUnits();
    this.phaseResponse = fap.getPhaseResponse();
    this.phaseResponseStandardDeviation = fap.getPhaseResponseStdDev();
  }

  /**
   * Create a DAO from a COI
   */
  public static FrequencyAmplitudePhaseDao from(FrequencyAmplitudePhase fap) {
    Preconditions.checkNotNull(fap,
      "Cannot create FrequencyAmplitudePhaseDao from null FrequencyAmplitudePhase");
    return new FrequencyAmplitudePhaseDao(fap);
  }

  /**
   * Create a COI from a DAO
   */
  public FrequencyAmplitudePhase toCoi() {
    Validate.isTrue(this.frequencies.length > 0, "frequencies length must be > 0");
    Validate.isTrue(this.amplitudeResponse.length > 0, "amplitudeResponse length must be > 0");
    Validate.isTrue(this.amplitudeResponseStdDev.length > 0,
      "amplitudeResponseStdDev length must be > 0");
    Validate.isTrue(this.phaseResponse.length > 0, "phaseResponse length must be > 0");
    Validate.isTrue(this.phaseResponseStandardDeviation.length > 0,
      "phaseResponseStdDev length must be > 0");
    Validate.isTrue(this.frequencies.length == this.amplitudeResponse.length &&
        this.frequencies.length == this.amplitudeResponseStdDev.length &&
        this.frequencies.length == this.phaseResponse.length &&
        this.frequencies.length == this.phaseResponseStandardDeviation.length,
      "frequencies, amplitudePhaseResponse, amplitudePhaseResponseStdDev, phaseResponse,"
        + " and phaseResponseStdDev lengths must be the same.");

    return FrequencyAmplitudePhase.builder()
      .setFrequencies(this.frequencies)
      .setAmplitudeResponseUnits(this.amplitudeResponseUnits)
      .setAmplitudeResponse(this.amplitudeResponse)
      .setAmplitudeResponseStdDev(this.amplitudeResponseStdDev)
      .setPhaseResponseUnits(this.phaseResponseUnits)
      .setPhaseResponse(this.phaseResponse)
      .setPhaseResponseStdDev(this.phaseResponseStandardDeviation)
      .build();
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public double[] getFrequencies() {
    return frequencies;
  }

  public void setFrequencies(double[] frequencies) {
    this.frequencies = frequencies;
  }

  public Units getAmplitudeResponseUnits() {
    return amplitudeResponseUnits;
  }

  public void setAmplitudeResponseUnits(
    Units amplitudeResponseUnits) {
    this.amplitudeResponseUnits = amplitudeResponseUnits;
  }

  public double[] getAmplitudeResponse() {
    return amplitudeResponse;
  }

  public void setAmplitudeResponse(double[] amplitudeResponse) {
    this.amplitudeResponse = amplitudeResponse;
  }

  public double[] getAmplitudeResponseStdDev() {
    return amplitudeResponseStdDev;
  }

  public void setAmplitudeResponseStdDev(double[] amplitudeResponseStdDev) {
    this.amplitudeResponseStdDev = amplitudeResponseStdDev;
  }

  public Units getPhaseResponseUnits() {
    return phaseResponseUnits;
  }

  public void setPhaseResponseUnits(
    Units phaseResponseUnits) {
    this.phaseResponseUnits = phaseResponseUnits;
  }

  public double[] getPhaseResponse() {
    return phaseResponse;
  }

  public void setPhaseResponse(double[] phaseResponse) {
    this.phaseResponse = phaseResponse;
  }

  public double[] getPhaseResponseStandardDeviation() {
    return phaseResponseStandardDeviation;
  }

  public void setPhaseResponseStandardDeviation(double[] phaseResponseStandardDeviation) {
    this.phaseResponseStandardDeviation = phaseResponseStandardDeviation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FrequencyAmplitudePhaseDao that = (FrequencyAmplitudePhaseDao) o;
    return id == that.id &&
      Arrays.equals(frequencies, that.frequencies) &&
      amplitudeResponseUnits == that.amplitudeResponseUnits &&
      Arrays.equals(amplitudeResponse, that.amplitudeResponse) &&
      Arrays.equals(amplitudeResponseStdDev, that.amplitudeResponseStdDev) &&
      phaseResponseUnits == that.phaseResponseUnits &&
      Arrays.equals(phaseResponse, that.phaseResponse) &&
      Arrays.equals(phaseResponseStandardDeviation, that.phaseResponseStandardDeviation);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(id, amplitudeResponseUnits, phaseResponseUnits);
    result = 31 * result + Arrays.hashCode(frequencies);
    result = 31 * result + Arrays.hashCode(amplitudeResponse);
    result = 31 * result + Arrays.hashCode(amplitudeResponseStdDev);
    result = 31 * result + Arrays.hashCode(phaseResponse);
    result = 31 * result + Arrays.hashCode(phaseResponseStandardDeviation);
    return result;
  }

  @Override
  public String toString() {
    return "FrequencyAmplitudePhaseDao{" +
      "id=" + id +
      ", frequencies=" + Arrays.toString(frequencies) +
      ", amplitudeResponseUnits=" + amplitudeResponseUnits +
      ", amplitudeResponse=" + Arrays.toString(amplitudeResponse) +
      ", amplitudeResponseStdDev=" + Arrays.toString(amplitudeResponseStdDev) +
      ", phaseResponseUnits=" + phaseResponseUnits +
      ", phaseResponse=" + Arrays.toString(phaseResponse) +
      ", phaseResponseStandardDeviation=" + Arrays.toString(phaseResponseStandardDeviation) +
      '}';
  }
}
