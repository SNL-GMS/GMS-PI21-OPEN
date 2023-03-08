package gms.core.performancemonitoring.ssam.control;

import com.google.auto.value.AutoValue;
import gms.core.performancemonitoring.uimaterializedview.QuietedSohStatusChangeUpdate;
import gms.shared.frameworks.osd.coi.soh.quieting.UnacknowledgedSohStatusChange;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import reactor.core.publisher.Sinks;

@AutoValue
public abstract class SsamMessageSinks {

  public abstract Sinks.Many<UnacknowledgedSohStatusChange> getUnacknowledgedSohStatusChangeSink();

  public abstract Sinks.Many<QuietedSohStatusChangeUpdate> getQuietedSohStatusChangeUpdateSink();

  public abstract Sinks.Many<SystemMessage> getSystemMessageEmitterSink();

  public static SsamMessageSinks.Builder builder() {
    return new AutoValue_SsamMessageSinks.Builder();
  }

  public abstract SsamMessageSinks.Builder toBuilder();

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract SsamMessageSinks.Builder setUnacknowledgedSohStatusChangeSink(
      Sinks.Many<UnacknowledgedSohStatusChange> signalDetectionHypothesis);

    public abstract SsamMessageSinks.Builder setQuietedSohStatusChangeUpdateSink(
      Sinks.Many<QuietedSohStatusChangeUpdate> signalDetectionHypothesis);

    public abstract SsamMessageSinks.Builder setSystemMessageEmitterSink(
      Sinks.Many<SystemMessage> signalDetectionHypothesis);


    abstract SsamMessageSinks autoBuild();

    public SsamMessageSinks build() {
      return autoBuild();
    }

  }

}
