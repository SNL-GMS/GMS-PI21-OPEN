package gms.shared.frameworks.osd.coi.soh.quieting;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@AutoValue
public abstract class UnacknowledgedSohStatusChange {

  private static final Logger logger = LoggerFactory.getLogger(UnacknowledgedSohStatusChange.class);

  @JsonCreator
  public static UnacknowledgedSohStatusChange from(
    @JsonProperty("station") String station,
    @JsonProperty("sohStatusChanges") Set<SohStatusChange> sohStatusChanges) {

    validateSohStatusChangeSet(sohStatusChanges);

    return new AutoValue_UnacknowledgedSohStatusChange(station,
      Collections.unmodifiableSet(sohStatusChanges));
  }

  /**
   * Verify that the Set does not contain entries with the same values for channel name and monitor
   * type.
   *
   * @param sohStatusChanges a Set of {@link SohStatusChange} objects.
   */
  private static void validateSohStatusChangeSet(Set<SohStatusChange> sohStatusChanges) {
    Map<Integer, SohStatusChange> map = new HashMap<>();

    sohStatusChanges.forEach(item -> {
      int hash = Objects.hash(item.getChangedChannel(), item.getSohMonitorType());

      // If entries found with the same channel name with the same monitor type, this is an error.
      if (map.containsKey(hash)) {
        logger.info(
          "Duplicate values for channel and monitor type found in SohStatusChange {} object",
          item);

        throw new IllegalArgumentException(
          "SohStatusChange objects cannot have duplicate channel names and monitor types");
      } else {
        map.computeIfAbsent(
          hash,
          k -> item);
      }
    });
  }

  public abstract String getStation();

  public abstract Set<SohStatusChange> getSohStatusChanges();
}
