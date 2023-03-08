package gms.dataacquisition.stationreceiver.cd11.common.frames;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11Validator;
import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11ConnectionConfig;

import java.nio.ByteBuffer;
import java.util.OptionalInt;

import static com.google.common.base.Preconditions.checkState;
import static gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities.padToLength;

@AutoValue
public abstract class Cd11ConnectionExchange implements Cd11Payload {

  /**
   * The byte array length of a connection response frame.
   */
  private static final int STRINGS_LENGTH = 8 + 4 + 4;
  public static final int FRAME_LENGTH = (Short.BYTES * 4) + (Integer.BYTES * 2) + STRINGS_LENGTH;

  // See constructor javadoc for description of the fields.
  public abstract short getMajorVersion();

  public abstract short getMinorVersion();

  // Defined in CD11 spec as 8 byte()s
  public abstract String getStationOrResponderName();

  // Defined in CD11 spec as 4 byte()s
  public abstract String getStationOrResponderType();

  // Defined in CD11 spec as 4 byte()s
  public abstract String getServiceType();

  public abstract int getIpAddress();

  public abstract int getPort();

  public abstract OptionalInt getSecondIpAddress();

  public abstract OptionalInt getSecondPort();

  /**
   * Returns this connection response frame as bytes.
   *
   * @return byte[], representing the frame in wire format
   */
  @Override
  @Memoized
  public byte[] toBytes() {
    var outputByteBuffer = ByteBuffer.allocate(FRAME_LENGTH);
    outputByteBuffer.putShort(getMajorVersion());
    outputByteBuffer.putShort(getMinorVersion());
    outputByteBuffer.put(padToLength(getStationOrResponderName(), 8).getBytes());
    outputByteBuffer.put(padToLength(getStationOrResponderType(), 4).getBytes());
    outputByteBuffer.put(padToLength(getServiceType(), 4).getBytes());
    outputByteBuffer.putInt(getIpAddress());
    outputByteBuffer.putChar((char) getPort()); // Convert Java "int" to "unsigned short".
    outputByteBuffer.putInt(getSecondIpAddress()
      // To adhere to CD1.1 Spec, this field must be populated when serialized
      .orElse(0));
    outputByteBuffer.putChar((char) getSecondPort() // Convert Java "int" to "unsigned short".
      // To adhere to CD1.1 Spec, this field must be populated when serialized
      .orElse(0));

    return outputByteBuffer.array();
  }

  public static Builder builder() {
    return new AutoValue_Cd11ConnectionExchange.Builder();
  }

  abstract Builder toBuilder();

  public static Builder withConfig(Cd11ConnectionConfig config) {
    return builder()
      .setMajorVersion(config.getProtocolMajorVersion())
      .setMinorVersion(config.getProtocolMinorVersion())
      .setServiceType(config.getServiceType());
  }

  @AutoValue.Builder
  public interface Builder {

    // See constructor javadoc for description of the fields.
    Builder setMajorVersion(short majorVersion);

    Builder setMinorVersion(short minorVersion);

    Builder setStationOrResponderName(String stationOrResponderName); // Defined in CD11 spec as 8 byte()s

    Builder setStationOrResponderType(String stationOrResponderType); // Defined in CD11 spec as 4 byte()s

    Builder setServiceType(String serviceType);   // Defined in CD11 spec as 4 byte()s

    Builder setIpAddress(int ipAddress);

    Builder setPort(int port);

    Builder setSecondIpAddress(int secondIpAddress);

    Builder setSecondPort(int secondPort);

    Cd11ConnectionExchange autoBuild();

    default Cd11ConnectionExchange build() {
      Cd11ConnectionExchange connectionFrame = autoBuild();
      validate(connectionFrame);
      return connectionFrame;
    }

    /**
     * Validates this object. Throws an exception if there are any problems with it's fields.
     *
     * @param connectionExchange The connection frame to validate
     */
    private static void validate(Cd11ConnectionExchange connectionExchange) {
      Cd11Validator.validStationOrResponderName(connectionExchange.getStationOrResponderName());
      Cd11Validator.validStationOrResponderType(connectionExchange.getStationOrResponderType());
      Cd11Validator.validServiceType(connectionExchange.getServiceType());
      checkState(connectionExchange.getMajorVersion() >= 0,
        "Major version of command request/response frame must be greater than 0");
      checkState(connectionExchange.getMinorVersion() >= 0,
        "Minor version of command request/response frame must be greater than 0");
      Cd11Validator.validIpAddress(connectionExchange.getIpAddress());
      Cd11Validator.validPortNumber(connectionExchange.getPort());
      connectionExchange.getSecondIpAddress().ifPresent(Cd11Validator::validIpAddress);
      connectionExchange.getSecondPort().ifPresent(Cd11Validator::validPortNumber);
    }

  }

}



