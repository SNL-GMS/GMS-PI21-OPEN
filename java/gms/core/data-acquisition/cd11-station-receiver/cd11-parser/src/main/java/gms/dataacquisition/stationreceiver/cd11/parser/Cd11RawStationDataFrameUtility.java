package gms.dataacquisition.stationreceiver.cd11.parser;

import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ChannelSubframe;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ChannelSubframeHeader;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Data;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame.AuthenticationStatus;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrameMetadata;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFramePayloadFormat;
import gms.shared.frameworks.osd.coi.waveforms.WaveformSummary;
import net.logstash.logback.argument.StructuredArguments;
import net.logstash.logback.marker.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public class Cd11RawStationDataFrameUtility {

  private static final Logger logger = LoggerFactory.getLogger(Cd11RawStationDataFrameUtility.class);
  public static final String STATION_NAME_KEY = "station";
  private static final String SUBFRAME_PRIMARY_IDENTIFIER_KEY = "subframe_primary";
  private static final String SUBFRAME_SECONDARY_IDENTIFIER_KEY = "subframe_secondary";

  private Cd11RawStationDataFrameUtility() {
  }

  /**
   * Parses frame metadata and wraps acquired Cd11Data as a COI RawStationDataFrame
   *
   * @param cd11DataFrame Input {@link Cd11Frame} to parse
   * @param stationIdentifier Identifier for the station associated with the data frame
   * @param receptionTime Time the data frame was acquired
   * @param channelLookup Lookup function for retrieving a channel name given a subframe identifier
   * @return The parsed Cd11Data wrapped as a coi RawStationDataFrame
   */
  public static RawStationDataFrame parseAcquiredDataFrame(Cd11Frame cd11DataFrame,
    String stationIdentifier, Instant receptionTime, Function<String, Optional<String>> channelLookup) {
    checkNotNull(cd11DataFrame, "Cannot parse null dataframe");
    var cd11Data = FrameUtilities.<Cd11Data>asPayloadType(cd11DataFrame.getPayload(), FrameType.DATA);

    var rsdfMetadataBuilder = RawStationDataFrameMetadata.builder()
      .setPayloadFormat(RawStationDataFramePayloadFormat.CD11)
      .setStationName(stationIdentifier)
      .setAuthenticationStatus(AuthenticationStatus.NOT_YET_AUTHENTICATED)
      .setReceptionTime(receptionTime);

    Cd11ChannelSubframeHeader header = cd11Data.getChanSubframeHeader();
    rsdfMetadataBuilder.setPayloadStartTime(header.nominalTime)
      .setPayloadEndTime(header.nominalTime.plusMillis(header.frameTimeLength));

    final Map<String, WaveformSummary> waveformSummaries = new HashMap<>();
    for (Cd11ChannelSubframe subframe : cd11Data.getChannelSubframes()) {
      String primaryChannelIdentifier = getPrimaryChannelIdentifier(stationIdentifier, subframe);
      String secondaryChannelIdentifier = getSecondaryChannelIdentifier(stationIdentifier,
        subframe);

      channelLookup.apply(primaryChannelIdentifier)
        .or(() -> channelLookup.apply(secondaryChannelIdentifier))
        .ifPresentOrElse(channelName -> waveformSummaries.put(channelName,
            WaveformSummary.from(channelName, subframe.timeStamp, subframe.endTime)),
          () -> logMissingChannel(stationIdentifier, primaryChannelIdentifier,
            secondaryChannelIdentifier));
    }

    rsdfMetadataBuilder
      .setChannelNames(waveformSummaries.keySet())
      .setWaveformSummaries(waveformSummaries);

    return RawStationDataFrame.builder().generatedId()
      .setMetadata(rsdfMetadataBuilder.build())
      .setRawPayload(cd11DataFrame.toBytes())
      .build();
  }

  private static String getPrimaryChannelIdentifier(String stationIdentifier,
    Cd11ChannelSubframe subframe) {
    return format("%s.%s.%s", stationIdentifier, subframe.siteName, subframe.channelName);
  }

  private static String getSecondaryChannelIdentifier(String stationIdentifier,
    Cd11ChannelSubframe subframe) {
    return format("%s.%s%s.%s", stationIdentifier, subframe.siteName, subframe.locationName,
      subframe.channelName);
  }

  private static void logMissingChannel(String stationIdentifier, String primaryChannelIdentifier,
    String secondaryChannelIdentifier) {
    logger.warn(Markers.append(STATION_NAME_KEY, stationIdentifier),
      "Channel name for subframe not found in lookup. Skipping channel subframe {}:{}",
      StructuredArguments.value(SUBFRAME_PRIMARY_IDENTIFIER_KEY, primaryChannelIdentifier),
      StructuredArguments.value(SUBFRAME_SECONDARY_IDENTIFIER_KEY, secondaryChannelIdentifier));
  }
}
