package gms.shared.stationdefinition.coi.utils.comparator;

import gms.shared.stationdefinition.coi.channel.Channel;

import java.util.Comparator;

public class ChannelComparator implements Comparator<Channel> {
  @Override
  public int compare(Channel c1, Channel c2) {
    return Comparator.comparing(Channel::getName)
      .thenComparing(c -> c.getEffectiveAt().orElse(null),
        Comparator.nullsLast(Comparator.naturalOrder()))
      .compare(c1, c2);
  }
}
