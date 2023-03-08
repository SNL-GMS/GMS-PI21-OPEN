package gms.dataacquisition.stationreceiver.cd11.dataprovider.rsdfsource;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11FrameReader;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11OrMalformedFrame;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11OrMalformedFrame.Kind;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ChannelSubframe;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ChannelSubframeHeader;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFramePayloadFormat;

import java.nio.ByteBuffer;
import java.time.Instant;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Utility class for extracting information from a {@link RawStationDataFrame}'s raw payload
 */
public class ProviderUtils {

  private ProviderUtils() {
  }

  public static long getSequenceNumber(Cd11Frame frame) {
    return frame.getHeader().getSequenceNumber();
  }

  public static long getSequenceNumber(RawStationDataFrame rsdFrame) {
    return getSequenceNumber(readCd11Rsdf(rsdFrame));
  }

  public static Cd11Frame readCd11Rsdf(RawStationDataFrame rsdFrame) {
    checkArgument(
      RawStationDataFramePayloadFormat.CD11.equals(rsdFrame.getMetadata().getPayloadFormat()),
      "Can only parse RawStationDataFrames from Cd1.1 data into the Cd1.1 format");

    Cd11OrMalformedFrame cd11OrMalformed = Cd11FrameReader
      .readFrame(ByteBuffer.wrap(rsdFrame.getRawPayload()
        .orElseThrow(() -> new IllegalArgumentException("RawStationDataFrame contains no raw payload"))));

    if (Kind.MALFORMED.equals(cd11OrMalformed.getKind())) {
      throw new IllegalArgumentException("RawStationDataFrame contains malformed byte data",
        cd11OrMalformed.malformed().getCause());
    }

    return cd11OrMalformed.cd11();
  }

  /**
   * Creates a copy of a CD 1.1 Channel Subframe, with a modified timestamp. <p> NOTE: For testing
   * purposes only!!!
   *
   * @param subframe Subframe used to copy.
   * @param timeStamp new timestamp
   * @return Modified frame.
   */
  public static Cd11ChannelSubframe cloneAndModifyChannelSubframe(Cd11ChannelSubframe subframe,
    Instant timeStamp) {
    return new Cd11ChannelSubframe(
      subframe.channelLength,
      subframe.authOffset,
      subframe.authenticationOn,
      subframe.compressionFormat,
      subframe.sensorType,
      subframe.isCalib,
      subframe.siteName,
      subframe.channelName,
      subframe.locationName,
      subframe.cd11DataFormat,
      subframe.calibrationFactor,
      subframe.calibrationPeriod,
      timeStamp,
      subframe.subframeTimeLength,
      subframe.samples,
      subframe.channelStatusSize,
      subframe.channelStatusData,
      subframe.dataSize,
      subframe.channelData,
      subframe.subframeCount,
      subframe.authKeyIdentifier,
      // TODO: if the frame is modified auth probably needs to be recalculated
      subframe.authSize,          // and these fields
      subframe.authValue);        // adjusted
  }

  public static Cd11ChannelSubframeHeader cloneAndModifyChannelSubframeHeader(
    Cd11ChannelSubframeHeader subframeHeader,
    Instant timeStamp) {
    return new Cd11ChannelSubframeHeader(
      subframeHeader.numOfChannels,
      subframeHeader.frameTimeLength,
      timeStamp,
      subframeHeader.channelStringCount,
      subframeHeader.channelString
    );
  }
}
