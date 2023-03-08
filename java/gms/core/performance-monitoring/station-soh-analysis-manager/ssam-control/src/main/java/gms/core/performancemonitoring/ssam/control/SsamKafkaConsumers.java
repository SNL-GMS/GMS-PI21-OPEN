package gms.core.performancemonitoring.ssam.control;

import com.google.auto.value.AutoValue;
import gms.core.performancemonitoring.ssam.control.dataprovider.ReactiveConsumer;
import gms.core.performancemonitoring.uimaterializedview.AcknowledgedSohStatusChange;
import gms.core.performancemonitoring.uimaterializedview.QuietedSohStatusChangeUpdate;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.soh.StationSoh;

@AutoValue
public abstract class SsamKafkaConsumers {

  public abstract ReactiveConsumer<StationSoh> getStationSohReactiveConsumer();

  public abstract ReactiveConsumer<CapabilitySohRollup> getCapabilitySohRollupReactiveConsumer();

  public abstract ReactiveConsumer<AcknowledgedSohStatusChange> getAcknowledgedSohStatusChangeReactiveConsumer();

  public abstract ReactiveConsumer<QuietedSohStatusChangeUpdate> getQuietedSohStatusChangeUpdateReactiveConsumer();

  public static Builder builder() {
    return new AutoValue_SsamKafkaConsumers.Builder();
  }
  
  public abstract Builder toBuilder();

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setStationSohReactiveConsumer(ReactiveConsumer<StationSoh> signalDetectionHypothesis);

    public abstract Builder setCapabilitySohRollupReactiveConsumer(
      ReactiveConsumer<CapabilitySohRollup> signalDetectionHypothesis);

    public abstract Builder setAcknowledgedSohStatusChangeReactiveConsumer(
      ReactiveConsumer<AcknowledgedSohStatusChange> signalDetectionHypothesis);

    public abstract Builder setQuietedSohStatusChangeUpdateReactiveConsumer(
      ReactiveConsumer<QuietedSohStatusChangeUpdate> signalDetectionHypothesis);


    abstract SsamKafkaConsumers autoBuild();

    public SsamKafkaConsumers build() {
      return autoBuild();
    }

  }
}
