package gms.dataacquisition.stationreceiver.cd11.common.frames;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The Channel Subframe Header describes the Channel Subframes to follow. The nominal time field is the Data Frame's
 * time signature; times for each Channel Subframe are given therein. Other fields in the Channel Subframe Header list
 * the number of channels and their site/channel/location information.
 */
public class Cd11ChannelSubframeHeader {

  // See constructor javadoc for description of the fields.
  public final int numOfChannels;
  public final int frameTimeLength;
  public final Instant nominalTime;     // Defined in CD11 spec as 20 byte string
  public final int channelStringCount; // Must be numOfChannels * 10 + padding for div by 4
  public final String channelString;
  public final int paddedChannelStringLength;

  public static final int NOMTIMELENGTH = 20;

  private static final Logger logger = LoggerFactory.getLogger(Cd11ChannelSubframeHeader.class);

  /**
   * The minimum byte array length of a subframe header. This value does not include the channelString value which is
   * dynamic.
   */
  public static final int MINIMUM_FRAME_LENGTH = (Integer.BYTES * 3) + 20;

  public static Cd11ChannelSubframeHeader read(ByteBuffer bodyByteBuffer) {
    if (bodyByteBuffer.remaining() < MINIMUM_FRAME_LENGTH) {
      logger
        .error("ChannelSubframeHeader minimum size is {}  but byte buffer only contains {} bytes",
          MINIMUM_FRAME_LENGTH, bodyByteBuffer.remaining());
      throw new BufferUnderflowException();
    }

    //In the case of the Data Frame, the Channel Subframe Header
    //immediately follows the Frame Header (i.e it is the first
    //thing in the body.
    var inNumOfChannelsInt = bodyByteBuffer.getInt();
    var inFrameTimeLengthInt = bodyByteBuffer.getInt();

    var nominalTimeString = FrameUtilities.readBytesAsString(bodyByteBuffer, NOMTIMELENGTH);
    if (FrameUtilities.validJulianDate(nominalTimeString)) {
      var inNominalTimeInstant = FrameUtilities.jdToInstant(nominalTimeString);

      var inChannelStringCount = bodyByteBuffer.getInt();

      var padding = FrameUtilities.calculateNeededPadding(inChannelStringCount, Integer.BYTES);

      byte[] channelStringAsBytes = FrameUtilities.readBytes(bodyByteBuffer, inChannelStringCount);
      var inChannelString = new String(channelStringAsBytes, StandardCharsets.UTF_8);
      var offsetInt = 0;
      for (var i = 0; i < inNumOfChannelsInt; ++i) {
        var subChannelString = inChannelString.substring(offsetInt, offsetInt + 10);
        offsetInt += 10;
        if (!FrameUtilities.validChannelString(subChannelString)) {
          logger.info("Invalid channelString for subchannel string {}", (i + 1));
        }
      }

      /*
       * Channel String Padding Validation: CD1.1 protocol states that the channel string field contains 10 bytes per
       * single channel, and is padded to a multiple of 4 bytes. This means the field will only ever have 0, 2, or 4
       * bytes of padding.
       */
      if (padding > 0) {
        /*
         * NOTE: A major assumption is currently made that the next field (channel length) is an integer smaller than
         * 65,536 bytes. By assuming this, we fetch the next 4 bytes as an int and check if the value is 0 to prove the
         * padding exists. If this assumption is ever disproved then this code will need to change.
         */
        var tempInt = bodyByteBuffer.getInt();

        /*
         * We reset our position based on whether or not padding was found. The final position of the ByteBuffer should
         * be at the next field (channel length)
         */
        if (tempInt != 0) {
          logger.info("Channel string did not end on a 4 byte boundary.");
          bodyByteBuffer.position(bodyByteBuffer.position() - Integer.BYTES);
        } else {
          bodyByteBuffer.position(bodyByteBuffer.position() - padding);
        }

      }

      return new Cd11ChannelSubframeHeader(inNumOfChannelsInt, inFrameTimeLengthInt, inNominalTimeInstant, inChannelStringCount, inChannelString);
    } else {
      throw new IllegalArgumentException("Bad formatted nominalTime string " + nominalTimeString);
    }
  }

  /**
   * Creates data channel subframe header with all arguments.
   *
   * @param numOfChannels number of channels in this frame
   * @param frameTimeLength time in milliseconds this frame encompasses
   * @param nominalTime 20-byte ASCII nominal UTC start time of all channels in frame;
   * @param chanStringCount unpadded length in bytes of the channel string; must be ten times the number of channels
   * field
   * @param chanString channel string listing the Channel Subframes to follow, 10 bytes per subframe The entire channel
   * string is null-padded to a multiple of four bytes. Each 10-byte tring is formatted as follows: five bytes for the
   * site name left justified and padded with ASCII null (if necessary), three bytes for the channel name left justified
   * and padded with ASCII null (if necessary), and two bytes for the location name left justified and padded with ASCII
   * null (if necessary). For example, the site name 'KCC' would be followed by two null characters before the
   * specification of a channel name. Note that if only one channel of data is provided, then the channel description
   * field must be padded with 2 null bytes (at the end) to satisfy the requirement of being evenly divisible by 4.
   */
  @JsonCreator
  public Cd11ChannelSubframeHeader(
    @JsonProperty("numOfChannels") int numOfChannels,
    @JsonProperty("frameTimeLength") int frameTimeLength,
    @JsonProperty("nominalTime") Instant nominalTime,
    @JsonProperty("channelStringCount") int chanStringCount,
    @JsonProperty("channelString") String chanString) {
    this.numOfChannels = numOfChannels;
    this.frameTimeLength = frameTimeLength;
    this.nominalTime = nominalTime;
    this.channelStringCount = chanStringCount;
    this.paddedChannelStringLength = FrameUtilities
      .calculatePaddedLength(channelStringCount, Integer.BYTES);
    this.channelString = chanString;

    validate();
  }

  /**
   * The size of the channel subframe header can be dynamic because the channel_string field is dependent upon the
   * number of subframes.
   *
   * @return The size in bytes of the channel subframe header
   */
  public int getSize() {
    return MINIMUM_FRAME_LENGTH + this.paddedChannelStringLength;
  }

  /**
   * Turns this data subframe header into a byte[]
   *
   * @return a byte[] representation of this data subframe header
   */
  public byte[] toBytes() {

    var outputByteBuffer = ByteBuffer.allocate(getSize());
    outputByteBuffer.putInt(numOfChannels);
    outputByteBuffer.putInt(frameTimeLength);
    outputByteBuffer.put(FrameUtilities.instantToJd(nominalTime).getBytes());
    outputByteBuffer.putInt(channelStringCount);
    outputByteBuffer.put(FrameUtilities.padToLength(channelString, this.paddedChannelStringLength)
      .getBytes());

    return outputByteBuffer.array();
  }

  /**
   * Validates this object. Throws an exception if there are any problems with it's fields.
   */
  private void validate() {
    checkArgument(this.numOfChannels > 0,
      "ChannelSubframeHeader.NumOfChannels must be > 0, but value is: " + this.numOfChannels);

    //NOTE: Due to open bounds on end time, frameTimeLength can be zero for a single-sample frame.
    checkArgument(this.frameTimeLength >= 0,
      "ChannelSubframeHeader.FrameTimeLength must be >= 0, but value is: " + this.frameTimeLength);

    checkNotNull(this.nominalTime);

    checkArgument(this.channelStringCount == 10 * this.numOfChannels,
      "ChannelSubframeHeader.ChannelStringCount must be 10 times NumOfChannels, but value is: "
      + this.channelStringCount);

    checkArgument(channelString.length() == this.channelStringCount,
      "ChannelSubframeHeader.ChannelString length must be equal to ChannelStringCount, but value is: "
      + this.channelString.length());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Cd11ChannelSubframeHeader that = (Cd11ChannelSubframeHeader) o;

    if (numOfChannels != that.numOfChannels) {
      return false;
    }
    if (frameTimeLength != that.frameTimeLength) {
      return false;
    }
    if (channelStringCount != that.channelStringCount) {
      return false;
    }
    if (!Objects.equals(nominalTime, that.nominalTime)) {
      return false;
    }
    return Objects.equals(channelString, that.channelString);
  }

  @Override
  public int hashCode() {
    int result = numOfChannels;
    result = 31 * result + frameTimeLength;
    result = 31 * result + (nominalTime != null ? nominalTime.hashCode() : 0);
    result = 31 * result + channelStringCount;
    result = 31 * result + (channelString != null ? channelString.hashCode() : 0);
    result = 31 * result + NOMTIMELENGTH;
    return result;
  }

  @Override
  public String toString() {
    return "Cd11ChannelSubframeHeader { " + "numOfChannels: " + numOfChannels + ", "
      + "frameTimeLength: " + frameTimeLength + ", "
      + "nominalTime: \"" + nominalTime + "\", "
      + "channelStringCount: " + channelStringCount + ", "
      + "channelString: \"" + channelString + "\", "
      + "NOM_TIME_LENGTH: " + NOMTIMELENGTH + " "
      + "}";
  }

}
