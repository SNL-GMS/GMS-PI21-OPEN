package gms.shared.stationdefinition.coi.utils.comparator;

import gms.shared.stationdefinition.coi.channel.ChannelGroup;

import java.util.Comparator;

public class ChannelGroupComparator implements Comparator<ChannelGroup> {
  @Override
  public int compare(ChannelGroup c1, ChannelGroup c2) {
    return Comparator.comparing(ChannelGroup::getName)
      .thenComparing(c -> c.getEffectiveAt().orElse(null),
        Comparator.nullsLast(Comparator.naturalOrder()))
      .compare(c1, c2);
  }
}
