package gms.dataacquisition.stationreceiver.cd11.datamanipulator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ChannelSubframe;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Data;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Header;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Trailer;
import gms.dataacquisition.stationreceiver.cd11.parser.Cd11AcquiredChannelEnvironmentIssuesParser;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Cd11DataFrameSoh {

  public final List<List<AcquiredChannelEnvironmentIssue<?>>> cd11AcquiredSohList;
  public final Cd11Header header;
  public final Cd11Data dataPayload;
  public final Cd11Trailer trailer;

  Cd11DataFrameSoh(Cd11Frame cd11Frame) {
    this.cd11AcquiredSohList = new ArrayList<>();
    this.header = cd11Frame.getHeader();
    this.dataPayload = FrameUtilities.asPayloadType(cd11Frame.getPayload(), FrameType.DATA);
    this.trailer = cd11Frame.getTrailer();
    for (Cd11ChannelSubframe frame : dataPayload.getChannelSubframes()) {
      List<AcquiredChannelEnvironmentIssue<?>> soh = Cd11AcquiredChannelEnvironmentIssuesParser
        .parseAcquiredChannelSoh(frame.channelStatusData, frame.channelName,
          frame.timeStamp, frame.endTime);
      cd11AcquiredSohList.add(soh);
    }
  }

  @JsonCreator
  public Cd11DataFrameSoh(
    @JsonProperty("cd11AcquiredSohList") List<List<AcquiredChannelEnvironmentIssue<?>>> cd11AcquiredSohList,
    @JsonProperty("header") Cd11Header header,
    @JsonProperty("dataPayload") Cd11Data dataPayload,
    @JsonProperty("trailer") Cd11Trailer trailer
  ) {
    this.cd11AcquiredSohList = cd11AcquiredSohList;
    this.header = header;
    this.dataPayload = dataPayload;
    this.trailer = trailer;
  }

  /**
   * Takes the internal state of health and changes the bytes and returns a new Cd11Data
   *
   * @return Cd11Data with new bytes to match soh object
   */
  Cd11Frame transcribeSohToBytes() {
    var newChannelSubframes = new ArrayList<Cd11ChannelSubframe>();
    for (var i = 0; i < dataPayload.getChannelSubframes().size(); i++) {
      var channelStatusDataArray = acquiredSohListToBytes(this.cd11AcquiredSohList.get(i), dataPayload
        .getChannelSubframes().get(i).channelStatusData);
      var newChannelSubframe = new Cd11ChannelSubframe(
        dataPayload.getChannelSubframes().get(i), channelStatusDataArray.length, channelStatusDataArray);
      newChannelSubframes.add(newChannelSubframe);
    }
    return Cd11Frame.builder()
      .setHeader(header)
      .setPayload(Cd11Data.builder()
        .setChanSubframeHeader(dataPayload.getChanSubframeHeader())
        .setChannelSubframes(newChannelSubframes)
        .build())
      .setTrailer(trailer)
      .build();
  }

  /**
   * Convert acquired soh list back to bytes that it was originally derived from This was back
   * converted from the toChannelStatusList method in Cd11RawStationDataFrameUtility
   *
   * <p>
   * channel status field breakdown when SOH exists for a station byte           bit description
   * 1	format code byte 1 = this format
   * <p>
   * 2	data status byte: bit 1       1 = dead sensor channel bit 21 = zeroed data bit 3       1 =
   * clipped bit 4       1 = calibration underway bits 5–8 future use
   * <p>
   * 3	channel security byte: bit 1       1 = equipment housing open bit 21 = digitizing equipment
   * open bit 3 1 = vault door opened bit 4       1 = authentication seal broken bit 5 1 = equipment
   * moved bits        6–8 future use
   * <p>
   * 4	miscellaneous status byte: bit 1       1 = clock differential too large bit 2       1 = GPS
   * receiver off bit 3       1 = GPS receiver unlocked bit 4       1 = digitizer analog input
   * shorted bit 5 1 = digitizer calibration loop back bits 6–8 future use
   * <p>
   * 5	voltage indicator byte bit 1       1 = main power failure bit 2       1 = backup power
   * unstable bits 3–8 future use
   * <p>
   * bytes 6–8	undefined
   * <p>
   * bytes 9-28                 20-byte ASCII	time of last GPS synchronization
   * <p>
   * bytes 29-32                 IEEE integer	clock differential in microseconds
   * </p>
   *
   * @param sohList list of AcquireChannelSoh that represents the status of t
   * @param oldBytes old bytes from the json file
   * @return List of bytes that is written to
   */
  byte[] acquiredSohListToBytes(List<AcquiredChannelEnvironmentIssue<?>> sohList, byte[] oldBytes) {

    byte[] newSohBytes = oldBytes.clone();
    int byteIdx;
    if (!sohList.isEmpty()) {
      byteIdx = 1;
      newSohBytes[byteIdx] = getNewByteFromSohStatus(sohList.get(0).getStatus(),
        newSohBytes[byteIdx], 0);
      newSohBytes[byteIdx] = getNewByteFromSohStatus(sohList.get(1).getStatus(),
        newSohBytes[byteIdx], 1);
      newSohBytes[byteIdx] = getNewByteFromSohStatus(sohList.get(2).getStatus(),
        newSohBytes[byteIdx], 2);
      newSohBytes[byteIdx] = getNewByteFromSohStatus(sohList.get(3).getStatus(),
        newSohBytes[byteIdx], 3);

      byteIdx = 2;
      newSohBytes[byteIdx] = getNewByteFromSohStatus(sohList.get(4).getStatus(),
        newSohBytes[byteIdx], 0);
      newSohBytes[byteIdx] = getNewByteFromSohStatus(sohList.get(5).getStatus(),
        newSohBytes[byteIdx], 1);
      newSohBytes[byteIdx] = getNewByteFromSohStatus(sohList.get(6).getStatus(),
        newSohBytes[byteIdx], 2);
      newSohBytes[byteIdx] = getNewByteFromSohStatus(sohList.get(7).getStatus(),
        newSohBytes[byteIdx], 3);
      newSohBytes[byteIdx] = getNewByteFromSohStatus(sohList.get(8).getStatus(),
        newSohBytes[byteIdx], 4);

      byteIdx = 3;
      newSohBytes[byteIdx] = getNewByteFromSohStatus(sohList.get(9).getStatus(),
        newSohBytes[byteIdx], 0);
      newSohBytes[byteIdx] = getNewByteFromSohStatus(sohList.get(10).getStatus(),
        newSohBytes[byteIdx], 1);
      newSohBytes[byteIdx] = getNewByteFromSohStatus(sohList.get(11).getStatus(),
        newSohBytes[byteIdx], 2);
      newSohBytes[byteIdx] = getNewByteFromSohStatus(sohList.get(12).getStatus(),
        newSohBytes[byteIdx], 3);
      newSohBytes[byteIdx] = getNewByteFromSohStatus(sohList.get(13).getStatus(),
        newSohBytes[byteIdx], 4);

      byteIdx = 4;
      newSohBytes[byteIdx] = getNewByteFromSohStatus(sohList.get(14).getStatus(),
        newSohBytes[byteIdx], 0);
      newSohBytes[byteIdx] = getNewByteFromSohStatus(sohList.get(15).getStatus(),
        newSohBytes[byteIdx], 1);

      byteIdx = 28;
      var byteBuffer = ByteBuffer.allocate(4);
      byteBuffer.putInt(((Double) sohList.get(16).getStatus()).intValue());
      byteBuffer.flip();
      var byteIntArr = byteBuffer.array();

      for (var byteValue : byteIntArr) {
        newSohBytes[byteIdx] = byteValue;
        byteIdx++;
      }
    }

    return newSohBytes;
  }

  /**
   * Makes a new byte from a SOH status
   *
   * @param status Object that comes from the AcquiredSohStatus
   * @param oldByte the byte that's being converted
   * @param bitLocation location of bit to change
   * @return new changed byte
   */
  private static byte getNewByteFromSohStatus(Object status, byte oldByte, int bitLocation) {
    int bitValue = (boolean) status ? 0x1 : 0x0;
    int mask = ~(0x1 << bitLocation);
    return (byte) ((mask & oldByte) | (bitValue << bitLocation));
  }
}
