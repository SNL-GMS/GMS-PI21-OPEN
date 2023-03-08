package gms.shared.frameworks.osd.coi.event;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class FinalEventHypothesis {

  public abstract EventHypothesis getEventHypothesis();

  public static FinalEventHypothesis from(EventHypothesis eventHypothesis) {
    return new AutoValue_FinalEventHypothesis(eventHypothesis);
  }
}
