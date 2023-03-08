package gms.dataacquisition.stationreceiver.cd11.common.frames;

import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;

import static com.google.common.base.Preconditions.checkArgument;

// TODO: Remove this class entirely (it is only used by unit tests).

/**
 * Helper methods to create CD1.1 frame headers for different frame types. Created by trsault on
 * 11/30/17.
 */
public class FrameHeaderTestUtility {

  private static final String
    DEFAULT_FRAME_CREATOR = "creator ", // 8 character
    DEFAULT_FRAME_DESTINATION = "destnatn";  // 8 character

  /**
   * Create a connection request or response frame header with default sequence number.
   */
  public static Cd11Header createHeaderForConnectionExchange(String frameCreator,
    String frameDestination, FrameType frameType) {
    checkArgument(
      frameType == FrameType.CONNECTION_REQUEST || frameType == FrameType.CONNECTION_RESPONSE,
      "Invalid frame type, must be CONNECTION_REQUEST or CONNECTION_RESPONSE");
    int sequenceNumber = 0;  // ConnectionResponse frames do not have a sequence number.
    int trailerOffset = Cd11Header.FRAME_LENGTH + Cd11ConnectionExchange.FRAME_LENGTH;

    return Cd11Header.create(frameType, trailerOffset, frameCreator, frameDestination,
      sequenceNumber, 0);

  }

  // NOT YET IMPLEMENTED // /**
  // NOT YET IMPLEMENTED //  * Create an OptionRequest frame header with default sequence number.
  // NOT YET IMPLEMENTED //  */
  // NOT YET IMPLEMENTED // public static Cd11Header createHeaderForOptionRequest(String frameCreator, String frameDestination, int series) throws Exception {
  // NOT YET IMPLEMENTED //   int frameType = FrameType.OPTION_REQUEST.getValue();
  // NOT YET IMPLEMENTED //   int sequenceNumber = 0;  // OptionRequest frames do not have a sequence number.
  // NOT YET IMPLEMENTED //   int trailerOffset = Cd11Header.FRAME_LENGTH + Cd11OptionRequestFrame.FRAME_LENGTH;
  // NOT YET IMPLEMENTED //   return new Cd11Header(frameType, trailerOffset, frameCreator, frameDestination, sequenceNumber);
  // NOT YET IMPLEMENTED // }

  // NOT YET IMPLEMENTED // /**
  // NOT YET IMPLEMENTED //  * Create an OptionResponse frame header with default sequence number.
  // NOT YET IMPLEMENTED //  */
  // NOT YET IMPLEMENTED // public static Cd11Header createHeaderForOptionResponse(String frameCreator, String frameDestination, int series) throws Exception {
  // NOT YET IMPLEMENTED //   int frameType = FrameType.OPTION_RESPONSE.getValue();
  // NOT YET IMPLEMENTED //   int sequenceNumber = 0;  // OptionResponse frames do not have a sequence number.
  // NOT YET IMPLEMENTED //   int trailerOffset = Cd11Header.FRAME_LENGTH + Cd11OptionResponseFrame.FRAME_LENGTH;
  // NOT YET IMPLEMENTED //   return new Cd11Header(frameType, trailerOffset, frameCreator, frameDestination, sequenceNumber);
  // NOT YET IMPLEMENTED // }

  public static Cd11Header createHeaderForAcknack() {
    return createHeaderForAcknack(DEFAULT_FRAME_CREATOR, DEFAULT_FRAME_DESTINATION, 0);
  }

  /**
   * Create an Acknack frame header with default sequence number using number of gaps to calculate
   * trailer offset.
   */
  public static Cd11Header createHeaderForAcknack(String frameCreator, String frameDestination,
    int gapCount) {

    FrameType frameType = FrameType.ACKNACK;
    int sequenceNumber = 0;  // Acknack frames do not have a sequence number.
    int trailerOffset = Cd11Header.FRAME_LENGTH +
      Cd11Acknack.MINIMUM_FRAME_LENGTH +
      (gapCount * Cd11Acknack.SIZE_PER_GAP);

    return Cd11Header.create(frameType, trailerOffset, frameCreator, frameDestination,
      sequenceNumber, 0);
  }

  public static Cd11Header createHeaderForData(int trailerOffset) {
    return createHeaderForData(trailerOffset, DEFAULT_FRAME_CREATOR,
      DEFAULT_FRAME_DESTINATION, 0);
  }

  public static Cd11Header createHeaderForData(int trailerOffset, String frameCreator,
    String frameDestination, long sequenceNumber) {

    FrameType frameType = FrameType.DATA;
    return Cd11Header.create(frameType, trailerOffset, frameCreator, frameDestination,
      sequenceNumber, 0);
  }

  public static Cd11Header createHeaderForAlert(int paddedMessageLength) {
    return createHeaderForAlert(DEFAULT_FRAME_CREATOR, DEFAULT_FRAME_DESTINATION,
      paddedMessageLength);
  }

  public static Cd11Header createHeaderForAlert(String frameCreator, String frameDestination,
    int paddedMessageLength) {

    FrameType frameType = FrameType.ALERT;
    int sequenceNumber = 0;  // Protocol doesn't say, but assuming Alert frames do not have a sequence number.
    int trailerOffset = Cd11Header.FRAME_LENGTH +
      Cd11Alert.MINIMUM_FRAME_LENGTH +
      paddedMessageLength;

    return Cd11Header.create(frameType, trailerOffset, frameCreator, frameDestination,
      sequenceNumber, 0);
  }
}
