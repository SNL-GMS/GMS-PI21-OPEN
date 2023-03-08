package gms.dataacquisition.stationreceiver.cd11.common.frames;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;

import java.nio.ByteBuffer;

import static com.google.common.base.Preconditions.checkState;
import static gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities.calculatePaddedLength;
import static gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities.padToLength;


/**
 * This frame and its companion, the Option Response Frame, are only exchanged as part of the
 * connection establishment process. However, future developments may support using these frames to
 * designate desired parameters such as a start time or list of channels, where multiple Option
 * Request Frames may be sent if necessary.  Therefore, only a single option is implemented at this
 * time. This class will require an update to make it more dynamic should new options be
 * implemented. NOTE: This class implements the "Connection establishment" option only!  This is the
 * only option defined by the current protocol specification.
 */
@AutoValue
public abstract class Cd11OptionExchange implements Cd11Payload {

  // See constructor javadoc for description of the fields.
  public abstract int getOptionType();

  // Defined in CD11 spec as 8 bytes for a "Connection establishment" option
  public abstract String getOptionValue();

  /**
   * Returns this option request frame as bytes.
   *
   * @return byte[], representing the frame in wire format
   */
  @Override
  @Memoized
  public byte[] toBytes() {
    var optionValuePaddedSizeInt = calculatePaddedLength(getOptionValue().length(), 4);
    var outputByteBuffer = ByteBuffer.allocate(
      Integer.BYTES +       // Option count.
        (2 * Integer.BYTES) + // Option type + Option size.
        optionValuePaddedSizeInt // Option response
    );

    outputByteBuffer.putInt(1); // Hard coded, since we only accept one option at this time.
    outputByteBuffer.putInt(getOptionType());
    outputByteBuffer.putInt(getOptionValue().length()); // Unpadded length of the option request.
    outputByteBuffer.put(padToLength(getOptionValue(), optionValuePaddedSizeInt).getBytes());

    return outputByteBuffer.array();
  }

  public static Builder builder() {
    return new AutoValue_Cd11OptionExchange.Builder();
  }

  @AutoValue.Builder
  public
  interface Builder {
    // See constructor javadoc for description of the fields.
    Builder setOptionType(int optionType);

    // Defined in CD11 spec as 8 bytes for a "Connection establishment" option
    Builder setOptionValue(String optionValue);

    Cd11OptionExchange autoBuild();

    default Cd11OptionExchange build() {
      Cd11OptionExchange optionRequestFrame = autoBuild();
      validate(optionRequestFrame);
      return optionRequestFrame;
    }

    private static void validate(Cd11OptionExchange optionaRequestFrame) {
      // Validate option type.
      checkState(optionaRequestFrame.getOptionType() == 1, "Only OptionType 1 is currently accepted.");

      // Validate option request.
      checkState(optionaRequestFrame.getOptionValue().length() > 0 && optionaRequestFrame.getOptionValue().length() < 9,
        "Option request length must be between 1 - 8 characters (received [%1$s]).",
        optionaRequestFrame.getOptionValue().length());
    }
  }

}
