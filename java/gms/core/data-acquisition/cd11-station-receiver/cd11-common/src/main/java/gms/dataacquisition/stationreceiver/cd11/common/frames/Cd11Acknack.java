package gms.dataacquisition.stationreceiver.cd11.common.frames;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11Validator;
import gms.dataacquisition.stationreceiver.cd11.common.gaps.Cd11GapList;

import java.nio.ByteBuffer;

import static com.google.common.base.Preconditions.checkState;
import static gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities.padToLength;


/**
 * The Acknack Frame is provided for error control. It also delivers a heartbeat (to prevent
 * timeout) if there are no Data Frames or other frames to send. Status fields include the name of
 * the frame set being acknowledged, the range of valid sequence numbers, and a list of gaps in the
 * sequence. A frame set is a container for the frames that will or have been exchanged with a
 * protocol peer. Each frame set contains frames from a unique source. The gaps are presented in
 * increasing order and must be non-overlapping intervals within the range of lowest seq no through
 * highest seq no. When an Acknack Frame is sent for heartbeat/keep-alive purposes, its content is
 * the same as at any other time, in other words, the list of sequence number gaps. Acknack Frames
 * provide heartbeat signals. The provisional policy is that Acknack frames will be sent at least
 * once a minute. Acknack Frames employ the standard frame header. However, the header sequence
 * number is undefined in the Acknack Frame. Further, Acknack Frames do not report on the delivery
 * of other Acknack Frames. This would not be possible because Acknack frames have no sequence
 * number.
 */
@AutoValue
public abstract class Cd11Acknack implements Cd11Payload {

  /**
   * The minimum byte array length of an acknack frame. This value does not include the size of gaps
   * which is dynamic.
   */
  public static final int MINIMUM_FRAME_LENGTH = (Long.BYTES * 2) + Integer.BYTES + 20;

  // The size required in the gaps byte[] buffer for each gap is long * 2 (for start and end time of gap). */
  public static final int SIZE_PER_GAP = Long.BYTES * 2;

  // Defined in CD11 spec as 20 bytes
  public abstract String getFrameSetAcked();

  public abstract long getLowestSeqNum();

  public abstract long getHighestSeqNum();

  public abstract int getGapCount();

  public abstract long[] getGapRanges();

  /**
   * Returns this acknack frame as bytes.
   *
   * @return byte[], representing the frame in wire format
   */
  @Override
  @Memoized
  public byte[] toBytes() {
    var outputByteBuffer = ByteBuffer.allocate(Cd11Acknack.MINIMUM_FRAME_LENGTH +
      (getGapRanges().length * Long.BYTES));
    outputByteBuffer.put(padToLength(getFrameSetAcked(), 20).getBytes());
    outputByteBuffer.putLong(getLowestSeqNum());
    outputByteBuffer.putLong(getHighestSeqNum());
    outputByteBuffer.putInt(getGapCount());
    for (long l : getGapRanges()) {
      outputByteBuffer.putLong(l);
    }

    return outputByteBuffer.array();
  }

  public static Builder builder() {
    return new AutoValue_Cd11Acknack.Builder();
  }

  public static Builder withGapList(Cd11GapList gapList) {
    return builder()
      .setLowestSeqNum(gapList.getLowestSequenceNumber())
      .setHighestSeqNum(gapList.getHighestSequenceNumber())
      .setGapCount(gapList.getGaps().length / 2)
      .setGapRanges(gapList.getGaps());
  }

  @AutoValue.Builder
  public interface Builder {

    // Defined in CD11 spec as 20 bytes
    Builder setFrameSetAcked(String frameSetAcked);

    Builder setLowestSeqNum(long lowestSeqNum);

    Builder setHighestSeqNum(long highestSeqNum);

    Builder setGapCount(int gapCount);

    Builder setGapRanges(long[] gapRanges);

    Cd11Acknack autoBuild();

    default Cd11Acknack build() {
      Cd11Acknack acknackFrame = autoBuild();
      validate(acknackFrame);
      return acknackFrame;
    }

    /**
     * Validates this object. Throws an exception if there are any problems with it's fields.
     *
     * @param acknackFrame Acknack frame to validate
     */
    private static void validate(Cd11Acknack acknackFrame) {
      Cd11Validator.validFrameSetAcked(acknackFrame.getFrameSetAcked());
      checkState(acknackFrame.getGapRanges().length % 2 == 0,
        "Must have an even number of gap ranges");
      checkState(acknackFrame.getGapCount() * 2 == acknackFrame.getGapRanges().length,
        "Gap count must be twice as long as the length of gap ranges");
    }
  }

}
