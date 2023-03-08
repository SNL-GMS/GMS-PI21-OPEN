package gms.shared.frameworks.osd.coi.soh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrameMetadata;

import java.util.List;
import java.util.Objects;

@AutoValue
public abstract class AcquiredStationSohExtract {

  // TODO: Make this a single RSDF Metadata object OR check the assumption
  //  that there is only ever one!
  public abstract List<RawStationDataFrameMetadata> getAcquisitionMetadata();

  public abstract List<AcquiredChannelEnvironmentIssue<?>> getAcquiredChannelEnvironmentIssues();

  /**
   * Create a new StationSohInput
   *
   * @param acquisitionMetadata the collection of RawStationDataFrames
   * @param acquiredChannelEnvironmentIssues the collection of AcquiredChannelSohs
   * @return the populated StationSohInput
   */
  @JsonCreator
  public static AcquiredStationSohExtract create(
    @JsonProperty("acquisitionMetadata") List<RawStationDataFrameMetadata> acquisitionMetadata,
    @JsonProperty("acquiredChannelEnvironmentIssues") List<AcquiredChannelEnvironmentIssue<?>> acquiredChannelEnvironmentIssues) {

    Objects
      .requireNonNull(acquisitionMetadata,
        "Cannot create AcquiredStationSohExtract from null acquisitionMetadata");
    Objects.requireNonNull(acquiredChannelEnvironmentIssues,
      "Cannot create StationSohInput from null acquiredChannelEnvironmentIssues");

    return new AutoValue_AcquiredStationSohExtract(acquisitionMetadata,
      acquiredChannelEnvironmentIssues);
  }
}
