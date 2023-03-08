package gms.shared.frameworks.osd.coi.dataacquisition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.channel.ChannelSegment;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.waveforms.Waveform;

import java.util.List;

@AutoValue
public abstract class WaveformAcquiredChannelSohPair {
  public abstract List<ChannelSegment<Waveform>> getWaveforms();

  public abstract List<AcquiredChannelEnvironmentIssue<?>> getAcquiredChannelEnvironmentIssues();

  @JsonCreator
  public static WaveformAcquiredChannelSohPair from(
    @JsonProperty("waveforms") List<ChannelSegment<Waveform>> waveforms,
    @JsonProperty("acquiredChannelSohs") List<AcquiredChannelEnvironmentIssue<?>> acquiredChannelEnvironmentIssues) {
    return new AutoValue_WaveformAcquiredChannelSohPair(waveforms, acquiredChannelEnvironmentIssues);
  }
}
