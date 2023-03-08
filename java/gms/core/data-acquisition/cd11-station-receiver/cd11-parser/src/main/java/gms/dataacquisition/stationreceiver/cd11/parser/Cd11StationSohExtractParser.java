package gms.dataacquisition.stationreceiver.cd11.parser;

import gms.core.dataacquisition.receiver.DataFrameReceiverConfiguration;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11FrameReader;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11OrMalformedFrame;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ChannelSubframe;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Data;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.soh.AcquiredStationSohExtract;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public class Cd11StationSohExtractParser {

  private static final Logger logger = LoggerFactory.getLogger(Cd11StationSohExtractParser.class);

  private final DataFrameReceiverConfiguration dataFrameReceiverConfiguration;

  private Cd11StationSohExtractParser(DataFrameReceiverConfiguration dataFrameReceiverConfiguration) {
    this.dataFrameReceiverConfiguration = dataFrameReceiverConfiguration;
  }

  public static Cd11StationSohExtractParser create(DataFrameReceiverConfiguration dataFrameReceiverConfiguration) {
    return new Cd11StationSohExtractParser(dataFrameReceiverConfiguration);
  }

  /**
   * Parses a {@link RawStationDataFrame}, building up a collection of data frame metadata and
   * State-of-Health
   *
   * @param rsdf The Data frame to parse
   * @return {@link AcquiredStationSohExtract} representing State-of-Health data and data frame
   * metadata
   * @throws IOException If there were errors in reading the data frame
   * @throws IllegalArgumentException If the input data frame was malformed in any way
   */
  public AcquiredStationSohExtract parseStationSohExtract(RawStationDataFrame rsdf) throws IOException {
    checkNotNull(rsdf, "Cannot parse null RawStationDataFrame");
    logger.info("Parsing StationSohExtract for RawStationDataFrame {}:{}", rsdf.getMetadata().getStationName(),
      rsdf.getId());

    Cd11OrMalformedFrame cd11OrMalformed = Cd11FrameReader.readFrame(ByteBuffer.wrap(rsdf.getRawPayload()
      .orElseThrow(() -> new IllegalStateException("rsdf did not contain RawPayload"))));
    if (Cd11OrMalformedFrame.Kind.MALFORMED.equals(cd11OrMalformed.getKind())) {
      throw new IOException("Error reading Rsdf Payload", cd11OrMalformed.malformed().getCause());
    }

    var cd11Frame = cd11OrMalformed.cd11();
    if (!(cd11Frame.getType().equals(FrameType.DATA) || cd11Frame.getType().equals(FrameType.CD_ONE_ENCAPSULATION))) {
      throw new IllegalArgumentException(
        format("Rsdf Payload is of unexpected type %s, must be DATA or CD_ONE_ENCAPSULATION", cd11Frame.getType()));
    }
    var cd11Data = (Cd11Data) cd11Frame.getPayload();

    //Parse each subframe (1 subframe = 1 channel)
    //Resolve channel name from config, if not present, skip.
    // Parse the channel status bits and then save to the OSD.
    List<AcquiredChannelEnvironmentIssue<?>> statesOfHealth = new ArrayList<>();
    for (Cd11ChannelSubframe subframe : cd11Data.getChannelSubframes()) {
      var subFrameName = format("%s.%s.%s", rsdf.getMetadata().getStationName(), subframe.siteName,
        subframe.channelName);

      dataFrameReceiverConfiguration.getChannelName(subFrameName).ifPresentOrElse(
        channelName -> statesOfHealth.addAll(
          Cd11AcquiredChannelEnvironmentIssuesParser.parseAcquiredChannelSoh(subframe.channelStatusData, channelName,
            subframe.timeStamp, subframe.endTime)),
        () -> logger.warn(
          "Channel name for subframe with name {} not found in configuration. Skipping channel subframe.",
          subFrameName));
    }

    return AcquiredStationSohExtract.create(List.of(rsdf.getMetadata()), statesOfHealth);
  }
}
