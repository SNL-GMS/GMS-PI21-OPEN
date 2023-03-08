package gms.shared.stationdefinition.coi.utils.comparator;

import gms.shared.stationdefinition.coi.channel.Response;

import java.util.Comparator;

public class ResponseComparator implements Comparator<Response> {
  @Override
  public int compare(Response c1, Response c2) {
    return Comparator.comparing(Response::getId)
      .thenComparing(c -> c.getEffectiveAt().orElse(null),
        Comparator.nullsLast(Comparator.naturalOrder()))
      .compare(c1, c2);
  }
}
