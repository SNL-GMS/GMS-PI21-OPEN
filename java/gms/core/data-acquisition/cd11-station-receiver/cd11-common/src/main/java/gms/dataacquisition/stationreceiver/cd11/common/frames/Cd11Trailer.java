package gms.dataacquisition.stationreceiver.cd11.common.frames;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import gms.dataacquisition.stationreceiver.cd11.common.CRC64;
import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * Represents the trailer of a CD-1.1 frame. The frame trailer provides three fields for frame
 * authentication. These fields are authentication key identifier, authentication size, and
 * authentication value. The three frame authentication fields are provided so that the receiver of
 * the frame has assurance the sender is correctly identified and the contents have not been
 * tampered with. The scope of the authentication field is the frame header and payload. The last
 * field of each frame trailer is the comm verification value used to verify the correct
 * transmission of the frame. The comm verification is a 64-bit CRC value calculated over the entire
 * frame (header, payload, and trailer).
 */
@AutoValue
@JsonSerialize(as = Cd11Trailer.class)
@JsonDeserialize(builder = AutoValue_Cd11Trailer.Builder.class)
public abstract class Cd11Trailer {

  /**
   * The minimum byte array length of a frame trailer. This value does not include the
   * authentication value which is dynamic.
   */
  public static final int MINIMUM_TRAILER_LENGTH = (Integer.BYTES * 2) + Long.BYTES;

  /**
   * Identifier of the public key credential required to verify the authentication value field; if
   * non-zero, then authentication is used to verify communications.
   */
  public abstract int getAuthenticationKeyIdentifier();

  /**
   * Un-padded size of the authenticationValue, so the bytes are able to be read out correctly from
   * the authenticationValue array
   */
  public abstract int getAuthenticationSize();

  abstract ImmutableList<Byte> getImmutableAuthenticationValue();

  /**
   * The bytes representing the authentication value. Per the CD1.1 spec, this array is padded to be
   * divisible by 4. Modifying this value will NOT modify the value stored in the {@link
   * Cd11Trailer}
   */
  public byte[] getAuthenticationValue() {
    return Bytes.toArray(getImmutableAuthenticationValue());
  }

  /**
   * The pre-calculated CRC64 value
   */
  public abstract long getCommVerification();

  private int authValuePadding;

  /**
   * The calculated padding applied to the authenticationSize to have it align with the size of the
   * authenticationValue bytes.
   */
  public int getAuthValuePadding() {
    return authValuePadding;
  }

  /**
   * Read in and parse a {@link Cd11Trailer} from a given {@link ByteBuffer}
   *
   * @param buffer Buffer containing a serialized {@link Cd11Trailer} that follows the CD1.1 spec
   * @return a {@link Cd11Trailer} populated from the raw bytes
   */
  public static Cd11Trailer read(ByteBuffer buffer) {
    var inAuthenticationKeyIdentifierInt = buffer.getInt();
    var inAuthenticationSize = buffer.getInt();
    var paddedAuthValSize = FrameUtilities
      .calculatePaddedLength(inAuthenticationSize, Integer.BYTES);
    var inAuthenticationValueByteArray = FrameUtilities.readBytes(buffer, paddedAuthValSize);
    var inCommVerificationLong = buffer.getLong();

    return Cd11Trailer.builder()
      .setAuthenticationKeyIdentifier(inAuthenticationKeyIdentifierInt)
      .setAuthenticationSize(inAuthenticationSize)
      .setAuthenticationValue(inAuthenticationValueByteArray)
      .setCommVerification(inCommVerificationLong)
      .build();
  }

  /**
   * Creates a trailer given an input stream.
   *
   * @param frameSegment1 first 8 bytes of the CD 1.1 trailer frame.
   * @param frameSegment2 remaining bytes of the CD 1.1 trailer frame.
   */
  public static Cd11Trailer fromSegments(ByteBuffer frameSegment1, ByteBuffer frameSegment2) {
    frameSegment1.rewind();
    frameSegment2.rewind();

    var outStream = new ByteArrayOutputStream();
    try {
      outStream.write(frameSegment1.array());
      outStream.write(frameSegment2.array());
    } catch (IOException e) {
      throw new IllegalArgumentException("Failed to read segments into single byte array", e);
    }

    return read(ByteBuffer.wrap(outStream.toByteArray()));
  }

  /**
   * Creates a new frame trailer with all arguments.
   *
   * @param authenticationKeyIdentifier identifier of the public key credential required to verify
   * the authentication value field
   * @param frameHeaderAndBody Byte array representing the frame header and body, used to generate
   * the CRC64 value.
   */
  public static Cd11Trailer fromBytes(int authenticationKeyIdentifier,
    byte[] frameHeaderAndBody) {
    var authenticationSizeInt = 0; // TODO: Generate this value.
    var authenticationValueByteArray = new byte[0]; // TODO: Generate this value.

    byte[] entireFrame = Bytes.concat(
      frameHeaderAndBody,
      Ints.toByteArray(authenticationKeyIdentifier),
      Ints.toByteArray(authenticationSizeInt),
      authenticationValueByteArray,
      Longs
        .toByteArray(0L)); // Have to add CRC as a long filled with zeroes to compute correctly.

    return Cd11Trailer.builder()
      .setAuthenticationKeyIdentifier(authenticationKeyIdentifier)
      .setAuthenticationSize(authenticationSizeInt)
      .setAuthenticationValue(authenticationValueByteArray)
      .setCommVerification(CRC64.compute(entireFrame))
      .build();
  }

  /**
   * Constructor from stored JSON fields
   *
   * @param authenticationKeyIdentifier identifier of the public key credential required to verify
   * the authentication value field
   * @param authenticationSize un-padded size of the authenticationValue, so the bytes are able to
   * be read out correctly
   * @param authenticationValue the bytes representing the authentication value
   * @param commVerification the already calculated CRC64 value
   */
  public static Cd11Trailer from(
    int authenticationKeyIdentifier,
    int authenticationSize,
    byte[] authenticationValue,
    long commVerification) {
    return Cd11Trailer.builder()
      .setAuthenticationKeyIdentifier(authenticationKeyIdentifier)
      .setAuthenticationSize(authenticationSize)
      .setAuthenticationValue(authenticationValue)
      .setCommVerification(commVerification)
      .build();
  }

  public abstract Cd11Trailer.Builder toBuilder();

  public static Cd11Trailer.Builder builder() {
    return new AutoValue_Cd11Trailer.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    /**
     * @param authenticationKeyIdentifier Identifier of the public key credential required to verify the authentication value field; if
     * non-zero, then authentication is used to verify communications.
     */
    public abstract Builder setAuthenticationKeyIdentifier(int authenticationKeyIdentifier);

    /**
     * @param authenticationSize Un-padded size of the authenticationValue, so the bytes are able to be read out correctly from
     * the authenticationValue array
     */
    public abstract Builder setAuthenticationSize(int authenticationSize);

    public abstract int getAuthenticationSize();

    abstract Builder setImmutableAuthenticationValue(
      ImmutableList<Byte> immutableAuthenticationValue);

    /**
     * @param authenticationValue The bytes representing the authentication value. Per the CD1.1 spec, this array must be padded to be
     * divisible by 4.
     * @throws IllegalArgumentException if the provided authenticationValue's size is not divisible by 4
     */
    public Builder setAuthenticationValue(byte[] authenticationValue) {
      if (authenticationValue.length % 4 != 0) {
        throw new IllegalArgumentException("Authentication value bytes must be padded to be divisible by 4, per CD1.1 specification.");
      }
      return setImmutableAuthenticationValue(
        ImmutableList.copyOf(Bytes.asList(authenticationValue)));
    }

    /**
     * @param commVerification The pre-calculated CRC64 value
     */
    public abstract Builder setCommVerification(long commVerification);

    abstract Cd11Trailer autoBuild();

    public Cd11Trailer build() {
      Cd11Trailer frameTrailer = autoBuild();

      frameTrailer.authValuePadding = FrameUtilities
        .calculateNeededPadding(getAuthenticationSize(), Integer.BYTES);

      if (frameTrailer.getImmutableAuthenticationValue().size()
        != frameTrailer.getAuthenticationSize() + frameTrailer.authValuePadding) {
        throw new IllegalArgumentException(
          String.format(
            "Bad input; expected padded bytes do not match size of read authenticationValue byte[], expected %d actual = %d",
            frameTrailer.getAuthenticationSize() + frameTrailer.authValuePadding,
            frameTrailer.getImmutableAuthenticationValue().size()));
      }
      return frameTrailer;
    }
  }

  /**
   * Turns this frame trailer into a byte[]
   *
   * @return a byte[] representation of this trailer
   */
  public byte[] toBytes() {
    var trailerAllocationInt = MINIMUM_TRAILER_LENGTH + getAuthenticationSize() + authValuePadding;
    var outputByteBuffer = ByteBuffer
      .allocate(trailerAllocationInt);
    outputByteBuffer.putInt(getAuthenticationKeyIdentifier());
    outputByteBuffer.putInt(getAuthenticationSize());
    outputByteBuffer.put(getAuthenticationValue());
    outputByteBuffer.putLong(getCommVerification());
    return outputByteBuffer.array();
  }
}
