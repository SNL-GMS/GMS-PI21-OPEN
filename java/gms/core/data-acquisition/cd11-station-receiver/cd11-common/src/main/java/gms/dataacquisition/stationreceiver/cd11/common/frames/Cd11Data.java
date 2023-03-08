package gms.dataacquisition.stationreceiver.cd11.common.frames;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.base.Preconditions;
import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * The CD-1.1 Data Frame contains both a description of its data and the actual data values
 * themselves. Thus, the frame combines the fields of both the Data and Data Format Frames of the
 * CD-1 protocol. In addition, the status field of the Channel Subframe is allowed to be of variable
 * size, and its definition may vary among different implementations of CD-1.1. As with all frames,
 * the standard frame header and frame trailer surround the payload. In normal operation, Data
 * Frames comprise the bulk of the transmission. Each Data Frame consists of the standard Frame
 * Header, a header for the Channel Subframes, one or more Channel Subframes, and a standard Frame
 * Trailer. When uncompressed, data must be provided in network byte order; no translation is
 * provided by transport processing.
 */
@AutoValue
@JsonSerialize(as = Cd11Data.class)
@JsonDeserialize(builder = AutoValue_Cd11Data.Builder.class)
public abstract class Cd11Data implements Cd11Payload {

  public abstract Cd11ChannelSubframeHeader getChanSubframeHeader();

  public abstract List<Cd11ChannelSubframe> getChannelSubframes();

  /**
   * Turns this data frame into a byte[]
   *
   * @return a byte[] representation of this data frame
   */
  @Override
  @Memoized
  public byte[] toBytes() {
    int size = getChanSubframeHeader().getSize();
    for (Cd11ChannelSubframe subframe : getChannelSubframes()) {
      size += subframe.getSize();
    }

    var outputByteBuffer = ByteBuffer.allocate(size);
    outputByteBuffer.put(getChanSubframeHeader().toBytes());
    for (Cd11ChannelSubframe subframe : getChannelSubframes()) {
      outputByteBuffer.put(subframe.toBytes());
    }

    return outputByteBuffer.array();
  }

  public static Builder builder() {
    return new AutoValue_Cd11Data.Builder();
  }

  public static String channelsString(Cd11ChannelSubframe[] subframes) {
    checkNotNull(subframes);
    String s = Arrays.stream(subframes).map(Cd11ChannelSubframe::channelString)
      .collect(Collectors.joining());
    int requiredSize = s.length() + FrameUtilities.calculateNeededPadding(s.length(), 4);
    return FrameUtilities.padToLength(s, requiredSize);
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    Builder setChanSubframeHeader(Cd11ChannelSubframeHeader chanSubframeHeader);

    Builder setChannelSubframes(Collection<Cd11ChannelSubframe> channelSubframes);

    Cd11Data autoBuild();

    default Cd11Data build() {
      Cd11Data dataFrame = autoBuild();
      validate(dataFrame);
      return dataFrame;
    }

    /**
     * Validates this object. Throws an exception if there are any problems with it's fields.
     *
     * @param dataFrame Data frame to validate
     * @throws IllegalStateException if channelSubframes is empty
     */
    private static void validate(Cd11Data dataFrame) {
      checkState(!dataFrame.getChannelSubframes().isEmpty());
      dataFrame.getChannelSubframes().forEach(Preconditions::checkNotNull);
    }
  }

}
