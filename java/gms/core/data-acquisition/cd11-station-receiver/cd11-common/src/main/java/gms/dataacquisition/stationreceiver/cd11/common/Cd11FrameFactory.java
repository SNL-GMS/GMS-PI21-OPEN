package gms.dataacquisition.stationreceiver.cd11.common;

import com.google.common.annotations.VisibleForTesting;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Acknack;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Alert;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11CommandRequest;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11CommandResponse;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ConnectionExchange;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Data;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Header;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11OptionExchange;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Payload;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Trailer;
import gms.dataacquisition.stationreceiver.cd11.common.frames.CustomReset;

import java.nio.ByteBuffer;

public class Cd11FrameFactory {

  // Frame factory specific constants
  private static final int UNAUTHENTICATED_KEY_IDENTIFIER = 0;
  private static final String FRAME_CREATOR = "TEST";
  private static final String FRAME_DESTINATION = "0";

  // private frame factory fields
  private final int authenticationKeyIdentifier;
  private final String frameCreator;
  private final String frameDestination;

  private Cd11FrameFactory(int authenticationKeyIdentifier, String frameCreator,
    String frameDestination) {
    this.authenticationKeyIdentifier = authenticationKeyIdentifier;
    this.frameCreator = frameCreator;
    this.frameDestination = frameDestination;
  }

  public static Cd11FrameFactory create(int authenticationKeyIdentifier, String frameCreator,
    String frameDestination) {
    return new Cd11FrameFactory(authenticationKeyIdentifier, frameCreator, frameDestination);
  }

  @VisibleForTesting
  public static Cd11FrameFactory createDefault() {
    return create(UNAUTHENTICATED_KEY_IDENTIFIER, FRAME_CREATOR, FRAME_DESTINATION);
  }

  public static Cd11FrameFactory createUnauthenticated(String frameCreator,
    String frameDestination) {
    return create(UNAUTHENTICATED_KEY_IDENTIFIER, frameCreator, frameDestination);
  }

  public static Cd11FrameFactory createUnauthDefaultDest(String frameCreator) {
    return create(UNAUTHENTICATED_KEY_IDENTIFIER, frameCreator, FRAME_DESTINATION);
  }

  public int getAuthenticationKeyIdentifier() {
    return authenticationKeyIdentifier;
  }

  public String getFrameCreator() {
    return frameCreator;
  }

  public String getFrameDestination() {
    return frameDestination;
  }

  public Cd11Frame wrap(Cd11Acknack acknack) {
    return wrapPayload(FrameType.ACKNACK, acknack);
  }

  public Cd11Frame wrap(Cd11Alert alert) {
    return wrapPayload(FrameType.ALERT, alert);
  }

  public Cd11Frame wrap(CustomReset customReset) {
    return wrapPayload(FrameType.CUSTOM_RESET_FRAME, customReset);
  }

  public Cd11Frame wrap(Cd11CommandRequest commandRequest) {
    return wrapPayload(FrameType.COMMAND_REQUEST, commandRequest);
  }

  public Cd11Frame wrap(Cd11CommandResponse commandResponse, long sequenceNumber) {
    return wrapPayload(FrameType.COMMAND_RESPONSE, commandResponse, sequenceNumber);
  }

  public Cd11Frame wrapRequest(Cd11ConnectionExchange connectionRequest) {
    return wrapPayload(FrameType.CONNECTION_REQUEST, connectionRequest);
  }

  public Cd11Frame wrapResponse(Cd11ConnectionExchange connectionResponse) {
    return wrapPayload(FrameType.CONNECTION_RESPONSE, connectionResponse);
  }

  public Cd11Frame wrap(Cd11Data data, long sequenceNumber) {
    return wrapPayload(FrameType.DATA, data, sequenceNumber);
  }

  public Cd11Frame wrapRequest(Cd11OptionExchange optionRequest) {
    return wrapPayload(FrameType.OPTION_REQUEST, optionRequest);
  }

  public Cd11Frame wrapResponse(Cd11OptionExchange optionResponse) {
    return wrapPayload(FrameType.OPTION_RESPONSE, optionResponse);
  }

  private Cd11Frame wrapPayload(FrameType type, Cd11Payload payload) {
    return wrapPayload(type, payload, 0);
  }

  private Cd11Frame wrapPayload(FrameType type, Cd11Payload payload, long sequenceNumber) {
    // Generate the frame body byte array.
    var frameBodyByteArray = payload.toBytes();

    // Use the frame body byte array to generate the frame header.
    var frameHeader = createFrameHeader(type, frameBodyByteArray.length, sequenceNumber);

    // Generate the frame header and body byte arrays.
    var frameHeaderAndBodyByteBuffer = ByteBuffer.allocate(
      Cd11Header.FRAME_LENGTH + frameBodyByteArray.length);
    frameHeaderAndBodyByteBuffer.put(frameHeader.toBytes());
    frameHeaderAndBodyByteBuffer.put(frameBodyByteArray);

    // Generate the frame trailer.
    var frameTrailer = createFrameTrailer(frameHeaderAndBodyByteBuffer.array());

    return Cd11Frame.builder()
      .setHeader(frameHeader)
      .setPayload(payload)
      .setTrailer(frameTrailer)
      .build();
  }

  /**
   * Create Cd11Header
   *
   * @param frameType - type
   * @param bodyLength - byte body length
   * @param sequenceNumber - sequence number
   * @return Cd11Header
   */
  private Cd11Header createFrameHeader(FrameType frameType, int bodyLength, long sequenceNumber) {
    return Cd11Header.create(
      frameType, Cd11Header.FRAME_LENGTH + bodyLength,
      frameCreator, frameDestination, sequenceNumber, 0);
  }

  /**
   * Create Cd11Trailer
   *
   * @param frameHeaderAndBody - frame header and body byte buffer
   * @return Cd11Trailer
   */
  private Cd11Trailer createFrameTrailer(byte[] frameHeaderAndBody) {
    return Cd11Trailer.fromBytes(authenticationKeyIdentifier, frameHeaderAndBody);
  }

}
