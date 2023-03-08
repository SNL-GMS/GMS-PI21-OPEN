package gms.dataacquisition.stationreceiver.cd11.common.gaps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

public class GapList {

  private final AtomicLong min = new AtomicLong();
  private final AtomicLong max = new AtomicLong();
  private final SortedSet<Gap> gapsList = new TreeSet<>();
  private boolean firstSeqNum = true;

  /**
   * Constructs the object, and sets the initial min and max range values.
   *
   * @param min initial minimum range value of sequence numbers from received frames
   * @param max initial maximum range value of sequence numbers from received frames
   */
  @JsonCreator
  public GapList(
    @JsonProperty("min") long min,
    @JsonProperty("max") long max) {
    Validate.isTrue(
      Long.compareUnsigned(min, max) <= 0,
      "Minimum value must be less than or equal to the maximum value; min: "
        + min + ", max: " + max);

    // Set the min and max values.
    this.min.set(min);
    this.max.set(max);

    // Create the first gap.
    synchronized (gapsList) {
      this.gapsList.add(new Gap(min, max));
    }
  }

  /**
   * Fills in the gap list with the given value.
   *
   * @param value Value to fill in.
   * @return If a gap was found and filled
   */
  boolean addValue(long value) {
    synchronized (gapsList) {
      // Check if there are no gaps.
      if (this.gapsList.isEmpty()) {
        return false;
      }

      //Because we initialize with min = 0, max = -1 and only update with frames, the ifs in the else block will
      //never catch until this happens
      if (firstSeqNum) {
        this.min.set(value);
        this.max.set(value);
        firstSeqNum = false;
      } else {
        if (Long.compareUnsigned(value, this.min.get()) < 0) {
          this.min.set(value);
        }
        if (Long.compareUnsigned(value, this.max.get()) > 0) {
          this.max.set(value);
        }
      }
      // Check if the value falls within a gap.
      Optional<Gap> matchedGap = this.gapsList.stream().filter(x -> x.contains(value)).findFirst();
      matchedGap.ifPresent(gap -> fillGap(gap, value));
      return matchedGap.isPresent();
    }
  }

  private void fillGap(Gap gap, long value) {

    synchronized (gapsList) {
      // SCENARIO 1: Check if the gap was simply eliminated.
      if ((Long.compareUnsigned(gap.getStart(), value) == 0) && (
        Long.compareUnsigned(gap.getEnd(), value)
          == 0)) {
        // Remove the gap from the gapsList.
        this.gapsList.remove(gap);
      }

      // SCENARIO 2: Check if the gap's lower limit needs to be incremented.
      else if (Long.compareUnsigned(gap.getStart(), value) == 0) {
        gap.setStart(gap.getStart() + 1);

        // Update the gap's "modified" time.
        gap.setModifiedTime(Instant.now());
      }

      // SCENARIO 3: Check if the gap's upper limit needs to be decremented.
      else if (Long.compareUnsigned(gap.getEnd(), value) == 0) {
        gap.setEnd(gap.getEnd() - 1);

        // Update the gap's "modified" time.
        gap.setModifiedTime(Instant.now());
      }

      // SCENARIO 4: Check if the gap needs to be split into two gaps.
      else {
        var now = Instant.now();

        // Store the current end value of the gap.
        long oldEnd = gap.getEnd();

        // Modify the existing gap to span the range of the lower split.
        gap.setEnd(value - 1);

        // Update the existing gap's "modified" time.
        gap.setModifiedTime(now);

        // Add a new gap to span the range of the upper split.
        this.gapsList.add(new Gap(value + 1, oldEnd, now));
      }
    }
  }

  /**
   * Fills in the gap list with the given range of values.
   *
   * @param startValue range start value
   * @param endValue range end value
   */
  void addValueRange(long startValue, long endValue) {
    Validate.isTrue(Long.compareUnsigned(startValue, this.min.get()) >= 0,
      "Start value must be greater than or equal to the current minimum.");
    Validate.isTrue(Long.compareUnsigned(startValue, this.max.get()) <= 0,
      "Start value must be less than or equal to the current maximum.");
    Validate.isTrue(Long.compareUnsigned(endValue, this.min.get()) >= 0,
      "End value must be greater than or equal to the current minimum.");
    Validate.isTrue(Long.compareUnsigned(endValue, this.max.get()) <= 0,
      "End value must be less than or equal to the current maximum.");

    synchronized (gapsList) {
      // Check if there are no gaps.
      if (this.gapsList.isEmpty()) {
        return;
      }

      // Modify or remove existing gaps that fall within the specified range.
      var now = Instant.now();
      List<Gap> removeList = new ArrayList<>();
      List<Gap> adjustList = new ArrayList<>();
      for (Gap gap : this.gapsList) {
        if (Long.compareUnsigned(gap.getEnd(), startValue) < 0) {
          // Leave these gaps along, since they occur before the specified range.
        } else if (Long.compareUnsigned(gap.getStart(), endValue) > 0) {
          // Skip all gaps past this point, since they will all occur after the specified range.
          break;
        } else if (Long.compareUnsigned(gap.getStart(), startValue) >= 0 &&
          Long.compareUnsigned(gap.getEnd(), endValue) <= 0) {
          // Remove these gaps entirely, since their full range fits inside the specified range.
          removeList.add(gap);
        } else if (Long.compareUnsigned(gap.getStart(), startValue) < 0 &&
          Long.compareUnsigned(gap.getEnd(), endValue) > 0) {
          // This gap spans both before the start and after end of the specified range. It must be split.
          removeList.add(gap);
          adjustList.add(new Gap(gap.getStart(), startValue - 1, now));
          adjustList.add(new Gap(endValue + 1, gap.getEnd(), now));
        } else if (Long.compareUnsigned(gap.getStart(), startValue) < 0 &&
          Long.compareUnsigned(gap.getEnd(), endValue) <= 0) {
          // This gap spans before the start of the specified range, and must be adjusted.
          removeList.add(gap);
          adjustList.add(new Gap(gap.getStart(), startValue - 1, now));
        } else if (Long.compareUnsigned(gap.getStart(), startValue) >= 0 &&
          Long.compareUnsigned(gap.getEnd(), endValue) > 0) {
          // This gap spans after the end of the specified range, and must be adjusted.
          removeList.add(gap);
          adjustList.add(new Gap(endValue + 1, gap.getEnd(), now));
        }
      }
      removeList.forEach(this.gapsList::remove);
      this.gapsList.addAll(adjustList);
    }
  }

  /**
   * Returns the minimum range value.
   *
   * @return minimum range value
   */
  public long getMin() {
    return this.min.get();
  }

  /**
   * Returns the maximum range value.
   *
   * @return maximum range value
   */
  public long getMax() {
    return this.max.get();
  }

  /**
   * Method returns an immutable copy of the gapList and is thread safe
   *
   * @return Immutable copy of the currently used gapList
   */
  public SortedSet<Gap> getGapsList() {
    synchronized (gapsList) {
      return ImmutableSortedSet.copyOfSorted(this.gapsList);
    }
  }

  /**
   * Returns the total number of gaps in the gap list.
   *
   * @return total number of gaps
   */
  @JsonIgnore
  public int getTotalGaps() {
    synchronized (gapsList) {
      return this.gapsList.size();
    }
  }

  /**
   * Removes gaps that were last modified before the specified expiration.
   *
   * @param expiration expiration time
   */
  void removeGapsModifiedBefore(Instant expiration) {
    synchronized (gapsList) {
      this.gapsList.removeIf(x ->
        x.getModifiedTime().isBefore(expiration));
    }
  }

  /**
   * List of gaps. NOTE: start and end positions are inclusive of the gap.
   *
   * @return List of gaps.
   */
  ArrayList<ImmutablePair<Long, Long>> getGaps() {
    return getGaps(false, false);
  }

  /**
   * List of gaps.
   *
   * @param exclusiveStart Gap start position will be exclusive to the gap.
   * @param exclusiveEnd Gap end position will be exclusive to the gap.
   * @return List of gaps.
   */
  ArrayList<ImmutablePair<Long, Long>> getGaps(
    boolean exclusiveStart, boolean exclusiveEnd) {
    synchronized (gapsList) {
      ArrayList<ImmutablePair<Long, Long>> gapRanges = new ArrayList<>();
      for (Gap gap : this.gapsList) {
        long lower = (exclusiveStart) ? gap.getStart() - 1 : gap.getStart();
        long upper;
        //If upper is Max unsigned, leave it
        if (isMaxUnsignedValue(gap.getEnd())) {
          upper = gap.getEnd();
        }
        //Otherwise do the exclusive end check
        else {
          upper = (exclusiveEnd) ? gap.getEnd() + 1 : gap.getEnd();
        }
        gapRanges.add(new ImmutablePair<>(lower, upper));
      }
      return gapRanges;
    }
  }

  /*
    Check if max is -1 (unsigned max)
   */
  static boolean isMaxUnsignedValue(long val) {
    return Long.compareUnsigned(-1, val) == 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var gapList = (GapList) o;
    return min.get() == gapList.min.get() &&
      max.get() == gapList.max.get() &&
      Objects.equals(gapsList, gapList.gapsList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(min.get(), max.get(), gapsList);
  }

  @Override
  public String toString() {
    return "GapList{" +
      "min=" + min.get() +
      ", max=" + max.get() +
      ", gapsList=" + gapsList +
      '}';
  }
}
