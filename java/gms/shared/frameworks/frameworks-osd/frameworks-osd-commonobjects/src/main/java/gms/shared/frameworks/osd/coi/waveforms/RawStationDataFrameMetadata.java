package gms.shared.frameworks.osd.coi.waveforms;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame.AuthenticationStatus;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;

@AutoValue
@JsonSerialize(as = RawStationDataFrameMetadata.class)
@JsonDeserialize(builder = AutoValue_RawStationDataFrameMetadata.Builder.class)
public abstract class RawStationDataFrameMetadata {

  public abstract String getStationName();

  public abstract ImmutableSet<String> getChannelNames();

  public abstract Instant getPayloadStartTime();

  public abstract Instant getPayloadEndTime();

  public abstract Instant getReceptionTime();

  public abstract RawStationDataFramePayloadFormat getPayloadFormat();

  public abstract AuthenticationStatus getAuthenticationStatus();

  public abstract ImmutableMap<String, WaveformSummary> getWaveformSummaries();

  public static RawStationDataFrameMetadata.Builder builder() {
    return new AutoValue_RawStationDataFrameMetadata.Builder();
  }

  public abstract RawStationDataFrameMetadata.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract RawStationDataFrameMetadata.Builder setPayloadStartTime(Instant startTime);

    public abstract RawStationDataFrameMetadata.Builder setPayloadEndTime(Instant endTime);

    public abstract RawStationDataFrameMetadata.Builder setReceptionTime(Instant receptionTime);

    public abstract RawStationDataFrameMetadata.Builder setPayloadFormat(
      RawStationDataFramePayloadFormat format);

    public abstract RawStationDataFrameMetadata.Builder setAuthenticationStatus(
      AuthenticationStatus status);

    public abstract RawStationDataFrameMetadata.Builder setStationName(String stationName);

    abstract RawStationDataFrameMetadata.Builder setChannelNames(
      ImmutableCollection<String> channels);

    public RawStationDataFrameMetadata.Builder setChannelNames(Collection<String> channels) {
      return setChannelNames(ImmutableList.copyOf(channels));
    }

    abstract RawStationDataFrameMetadata.Builder setWaveformSummaries(
      ImmutableMap<String, WaveformSummary> waveformSummaries);

    public RawStationDataFrameMetadata.Builder setWaveformSummaries(
      Map<String, WaveformSummary> waveformSummaries) {
      return setWaveformSummaries(ImmutableMap.copyOf(waveformSummaries));
    }

    public abstract RawStationDataFrameMetadata build();
  }

  /**
   * Compares the state of this object against another.
   *
   * @param otherRsdf the object to compare against
   * @return true if this object and the provided one have the same state, i.e. their values are
   * equal except for entity ID.  False otherwise.
   */
  public boolean hasSameState(RawStationDataFrameMetadata otherRsdf) {

    return otherRsdf != null &&
      this.getStationName().equals(otherRsdf.getStationName()) &&
      this.getChannelNames().equals(otherRsdf.getChannelNames()) &&
      this.getPayloadStartTime().equals(otherRsdf.getPayloadStartTime()) &&
      this.getPayloadEndTime().equals(otherRsdf.getPayloadEndTime()) &&
      this.getPayloadFormat().equals(otherRsdf.getPayloadFormat()) &&
      this.getAuthenticationStatus().equals(otherRsdf.getAuthenticationStatus()) &&
      this.getWaveformSummaries().equals(otherRsdf.getWaveformSummaries());
  }
}
