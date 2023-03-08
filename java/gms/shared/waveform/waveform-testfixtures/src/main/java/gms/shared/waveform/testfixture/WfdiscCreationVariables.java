package gms.shared.waveform.testfixture;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Range;

import java.time.Instant;

@AutoValue
public abstract class WfdiscCreationVariables {

  public abstract Range<Instant> getRange();

  public abstract String getFileName();

  public abstract Integer getNumSamples();

  public abstract Long getFoff();


  public static Builder builder() {
    return new AutoValue_WfdiscCreationVariables.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setRange(Range<Instant> range);

    public abstract Builder setFileName(String fileName);

    public abstract Builder setNumSamples(Integer numSamples);

    public abstract Builder setFoff(Long foff);

    abstract WfdiscCreationVariables autoBuild();

    public WfdiscCreationVariables build() {
      return autoBuild();
    }
  }


}
