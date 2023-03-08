package gms.dataacquisition.cssreader.data;


import com.github.ffpojo.metadata.DefaultFieldDecorator;
import com.github.ffpojo.metadata.delimited.annotation.DelimitedField;
import com.github.ffpojo.metadata.delimited.annotation.DelimitedRecord;

/**
 * Represents a FAP (frequency, amplitude, phase) record. These are rows in response files
 * whose directory path and file name are specified in the instrument table.
 */
@DelimitedRecord(delimiter = " ")
public class FapRecord {

  protected Double frequency;
  protected Double amplitude;
  protected Double amplitudeError;
  protected Double phase;
  protected Double phaseError;

  @DelimitedField(positionIndex = 1, decorator = DoubleDecorator.class)
  public Double getFrequency() {
    return frequency;
  }

  public void setFrequency(Double frequency) {
    this.frequency = frequency;
  }

  @DelimitedField(positionIndex = 2, decorator = DoubleDecorator.class)
  public Double getAmplitude() {
    return amplitude;
  }

  public void setAmplitude(Double amplitude) {
    this.amplitude = amplitude;
  }

  @DelimitedField(positionIndex = 3, decorator = DoubleDecorator.class)
  public Double getPhase() {
    return phase;
  }

  public void setPhase(Double phase) {
    this.phase = phase;
  }

  @DelimitedField(positionIndex = 4, decorator = DoubleDecorator.class)
  public Double getAmplitudeError() {
    return amplitudeError;
  }

  public void setAmplitudeError(Double amplitudeError) {
    this.amplitudeError = amplitudeError;
  }

  @DelimitedField(positionIndex = 5, decorator = DoubleDecorator.class)
  public Double getPhaseError() {
    return phaseError;
  }

  public void setPhaseError(Double phaseError) {
    this.phaseError = phaseError;
  }


  public static class DoubleDecorator extends DefaultFieldDecorator {
    @Override
    public Double fromString(String str) {
      return Double.valueOf(str);
    }
  }
}
