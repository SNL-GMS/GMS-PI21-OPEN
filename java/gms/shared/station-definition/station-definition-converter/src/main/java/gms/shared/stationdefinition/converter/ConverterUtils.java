package gms.shared.stationdefinition.converter;

import gms.shared.stationdefinition.coi.channel.Channel;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Instant;
import java.util.Collection;

public class ConverterUtils {

  private ConverterUtils() {
  }

  /**
   * Called by a converter to determine if a Channel was updated by a response or not.
   * This is used to determine if the start/end times should be set to 12:00:00/11:59:59
   *
   * @param channels
   * @param versionStartTime
   * @param versionEndTime
   * @return
   */
  public static Pair<Boolean, Boolean> getUpdatedByResponse(Collection<Channel> channels, Instant versionStartTime,
    Instant versionEndTime) {
    var effectiveAtUpdatedByResponse = false;
    var effectiveUntilUpdatedByResponse = false;

    for (Channel channel : channels) {
      if (channel.getData().isEmpty()) {
        continue;
      }
      if (channel.getEffectiveAt().orElse(Instant.MIN).equals(versionStartTime) &&
        channel.getEffectiveAtUpdatedByResponse().orElse(false)) {
        effectiveAtUpdatedByResponse = true;
      }
      if (channel.getEffectiveUntil().orElse(Instant.MAX).equals(versionEndTime) &&
        channel.getEffectiveUntilUpdatedByResponse().orElse(false)) {
        effectiveUntilUpdatedByResponse = true;
      }
    }
    return Pair.of(effectiveAtUpdatedByResponse, effectiveUntilUpdatedByResponse);
  }
}
